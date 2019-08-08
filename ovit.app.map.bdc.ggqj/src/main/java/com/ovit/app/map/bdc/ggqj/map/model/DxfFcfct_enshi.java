package com.ovit.app.map.bdc.ggqj.map.model;

import android.text.TextUtils;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZRZ;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.LineLabel;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfHelper;
import com.ovit.app.util.gdal.cad.DxfTemplet;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by XW on 2018/9/29. 房产图 固定1:200 比例尺
 * 恩施版房产分层图
 */

public class DxfFcfct_enshi {

    MapInstance mapInstance;
    SpatialReference spatialReference;

    DxfAdapter dxf;
    private String dxfpath;
    private String bdcdyh;
    private List<Feature> fs_all;
    private List<Feature> fs_hAndFs;
    private List<Feature> fs_zAndFs;
    private Feature f_zd;
    private Point o_center;   // o 点
    private Envelope o_extend;// 真实图形的范围
    private final double o_split = 2d;// 单元间隔
    private final double p_width = 28d;// 页面宽
    private final double p_height = 41d;// 页面高
    private final double h = 1.5d; // 行高
    private float o_fontsize=0.6f;// 字体大小
    private String o_fontstyle = "宋体";// 字体大小
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;

    public DxfFcfct_enshi(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfFcfct_enshi set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfFcfct_enshi set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfFcfct_enshi set(String bdcdyh, Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_h_fsjg) {
        this.bdcdyh = bdcdyh;
        this.f_zd = f_zd;
        this.fs_zrz=fs_zrz;
        fs_hAndFs = new ArrayList<>();

        fs_hAndFs.addAll(fs_z_fsjg);
        fs_hAndFs.addAll(fs_h);
        fs_hAndFs.addAll(fs_h_fsjg);
        // 建立副本
        List<Feature> fs_z_fsjg_clone = MapHelper.cloneFeature(fs_h_fsjg);
        List<Feature> fs_h_fsjg_clone = MapHelper.cloneFeature(fs_z_fsjg);

        this.fs_zrz = fs_zrz;
        fs_all = new ArrayList<>();
        fs_all.addAll(fs_zrz);
        fs_all.addAll(fs_hAndFs);

        fs_zAndFs = new ArrayList<>();
        fs_zAndFs.addAll(fs_zrz);
        fs_zAndFs.addAll(fs_z_fsjg_clone);
        fs_zAndFs.addAll(fs_h_fsjg_clone);
        return this;
    }

    // 获取范围
    public Envelope getExtend() {
        o_extend = MapHelper.geometry_combineExtents_Feature(fs_all);
        o_extend = MapHelper.geometry_get(o_extend, spatialReference); //  图形范围
        o_center = o_extend.getCenter(); // 中心点
        // 比例尺
        o_fontsize = 0.63f;
        double x_min = o_center.getX() - (p_width / 2);
        double x_max = o_center.getX() + (p_width / 2);
        double y_min = o_center.getY() - (p_height / 2);
        double y_max = o_center.getY() + (p_height / 2);
        // 单元格范围
        p_extend = new Envelope(x_min, y_min, x_max, y_max, o_extend.getSpatialReference());
        return p_extend;
    }

    public class C {
        public static final String HALF_AREA = "0.5";
        String lc;
        List<Feature> fs;
        List<Feature> fs_zrz;
        private double cjzmj = 0d;
        private double cHalfArea = 0d;
        private double cwholeArea = 0d;
        private String jzcl = "";
        private String bz = "";

        public C(String lc, List<Feature> fs, List<Feature> fs_zrz) {
            this.lc = lc;
            this.fs = fs;
            this.fs_zrz = fs_zrz;
            init();
        }

        private void init() {
            List<Feature> featureHs = new ArrayList<>();
            List<Feature> halfAreaFsjc = new ArrayList<>();
            List<Feature> wholeAreaFeatures = new ArrayList<>();
            for (Feature f : fs) {
                FeatureTable featureTable = f.getFeatureTable();
                if (f != null && featureTable != mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG)) {
                    featureHs.add(f);
                }
                // 半面积 附属结构
                if (featureTable == mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG) || featureTable == mapInstance.getTable(FeatureHelper.LAYER_NAME_Z_FSJG)) {
                    if (HALF_AREA.equals(FeatureHelper.Get(f, "TYPE", ""))) {
                        halfAreaFsjc.add(f);// 半面积
                    } else if (featureTable != mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG)){
                        wholeAreaFeatures.add(f);
                    }
                }else{
                    wholeAreaFeatures.add(f);
                }
                cjzmj = FeatureViewZRZ.hsmj_jzmj(featureHs);
                cHalfArea = FeatureViewZRZ.hsmj_jzmj(halfAreaFsjc);
                cwholeArea = FeatureViewZRZ.hsmj_jzmj(wholeAreaFeatures);
            }
        }
        public String getJzcl() {
            if (TextUtils.isEmpty(jzcl)){
                List<String> fwjgs = new ArrayList<>();
                List<String> bzs = new ArrayList<>();
                List<Feature> featureFsfw = new ArrayList<>();
                for (Feature f : fs_zrz) {
                    String fwjg = DicUtil.dic("fwjg", FeatureHelper.Get(f, "FWJG", "4"));
                    boolean b=Integer.parseInt(AiUtil.GetValue(f.getAttributes().get("ZCS"),1)+"")>=Integer.parseInt(lc);// 自然幢 楼层是否大于当前层
                    String fwjg_ =fwjg;
                    if(b){
                        if (!TextUtils.isEmpty(fwjg)) {
                            if (fwjg.contains("结构")) {
                                fwjg = fwjg.substring(fwjg.lastIndexOf("]") + 1,fwjg.lastIndexOf("结构"));
                            } else {
                                fwjg = fwjg.substring(fwjg.lastIndexOf("]") + 1);
                            }
                            fwjgs.add(fwjg);
                        }
                        String jzwmc= FeatureHelper.Get(f,"JZWMC","");
                        if (TextUtils.isEmpty(jzwmc)||!jzwmc.contains("主房")) {
                            String zh = FeatureHelper.Get(f, "ZRZH", "");
                            for (Feature feature : fs) {
                                FeatureTable featureTable = feature.getFeatureTable();
                                if (f != null && featureTable != mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG)) {
                                    String id = FeatureHelper.Get(feature, "ID", "");
                                    if (id.contains(zh)) {
                                        featureFsfw.add(feature);
                                    }
                                }
                            }
                            double fsfwArea = FeatureViewZRZ.hsmj_jzmj(featureFsfw);
                            bzs.add("其中含" + fwjg_.substring(fwjg_.lastIndexOf("]") + 1) + String.format("%.2f",fsfwArea )+ "平方米");
                        }
                    }
             }
                jzcl= StringUtils.join(fwjgs,"/");
                bz= StringUtils.join(bzs,"\n");
            }
            return jzcl;
        }

        public String getName() {
            String zh = "0001";
            int zh_i = AiUtil.GetValue(StringUtil.getTextOnlyIn(zh, "0123456789"), 0);
            String name = zh;
            if (zh_i > 0) {
                name = zh_i + "";
            }
            name = name.replace("幢", "") + "幢" + lc.replace("层", "") + "层";
            return name;
        }

        //        public String getZcshz(String cshz){
//            int zh_i =   AiUtil.GetValue(StringUtil.getTextOnlyIn(zh,"0123456789"),0);
//            String  name  = zh;
//            if(zh_i>0){name =  zh_i+"";}
//            name = name.replace("幢","") +"幢"+ FeatureHelper.Get(zrz,"ZCS",1)+"层";
//            if (!cshz.contains(name)){
//                zcshz+=name+"\n"  ;
//            }
//            return zcshz;
//        }
        public double getJzmj() {
            List<Feature> featureHs = new ArrayList<>();
            for (Feature f : fs) {
                if (f != null && f.getFeatureTable() != mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG)) {
                    featureHs.add(f);
                }
            }
            return FeatureViewZRZ.hsmj_jzmj(featureHs);
        }

        public double getWholeArea() {
            List<Feature> featureHs = new ArrayList<>();
            for (Feature f : fs) {
                if (f != null && f.getFeatureTable() != mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG)) {
                    featureHs.add(f);
                }
            }
            return FeatureViewZRZ.hsmj_jzmj(featureHs);
        }

        public double getHalfArea() {
            List<Feature> featureHs = new ArrayList<>();
            for (Feature f : fs) {
                if (f != null && f.getFeatureTable() != mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG)) {
                    featureHs.add(f);
                }
            }
            return FeatureViewZRZ.hsmj_jzmj(featureHs);
        }
    }

    public DxfFcfct_enshi write() throws Exception {
        getExtend(); // 多大范围
        ArrayList<Map.Entry<String, List<Feature>>> fs_map_croup = FeatureViewZRZ.GroupbyC_Sort(fs_hAndFs);

        if (dxf == null) {
            dxf = new DxfAdapter();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            int page_count = fs_map_croup.size();  //  多少页
            float alpha=0.001f;
            for (int page = 0; page < page_count; page++) {

                Map.Entry<String, List<Feature>> fs_c_map = fs_map_croup.get(page);
                List<Feature> fs_c = fs_c_map.getValue();
                C c = new C(fs_c_map.getKey(),fs_c_map.getValue(),fs_zrz);
                Envelope envelope = getPageExtend(page);
                Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
                dxf.writeText(p_title, "房屋分层图", 1.0f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
                double w = p_width; //行宽
                // 左上角
                double x = envelope.getXMin();
                double y = envelope.getYMax();

                double x_ = x;
                double y_ = y;
                String fwjg = "砖混";
                int zcs = 1;
                // 单元格1-1
                Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 1 / 4, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_1, null, "层次", 0.5f, null, false, 0, 0);
                // 单元格1-2
                x_ = x_ + w * 1 / 4;
                String lc =String.format("%02d",Integer.parseInt(fs_c_map.getKey())) + "层";

                Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 1 / 4, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_2, null, lc, 0.5f, null, false, 0, 0);
                // 单元格1-3
                x_ = x_ + w * 1 / 4;
                Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 1 / 4, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_3, null, "层高", 0.5f, null, false, 0, 0);
                // 单元格1-4
                x_ = x_ + w * 1 / 4;
                Envelope cel_1_4 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_4, null, "3.50", 0.5f, null, false, 0, 0);
                // 单元格2-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_2_1 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_1, null, "面积汇总", 0.5f, null, false, 0, 0);
                // 单元格3-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_3_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.write(cel_3_1, null, "名称或编号", 0.5f, null, false, 0, 0);
                // 单元格3-2
                x_ = x_ + w * 1 / 6;
                Envelope cel_3_2 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_2, null, "计算建筑面积的建筑部分", 0.5f, null, false, 0, 0);

                // 单元格4-1
                x_ = x + w * 1 / 6;
                y_ = y_ - h;
                Envelope cel_4_1 = new Envelope(x_, y_, x_ + w * 1 / 7, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_1, null, "全面积", 0.5f, null, false, 0, 0);

                // 单元格4-2
                x_ = x_ + w * 1 / 7;
                Envelope cel_4_2 = new Envelope(x_, y_, x_ + w * 1 / 7, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_2, null, "半面积", 0.5f, null, false, 0, 0);

                // 单元格4-3
                x_ = x_ + w * 1 / 7;
                Envelope cel_4_3 = new Envelope(x_, y_, x_ + w * 1 / 7, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_3, null, "小计", 0.5f, null, false, 0, 0);
                // 单元格4-4
                x_ = x_ + w * 1 / 7;
                Envelope cel_4_4 = new Envelope(x_, y_, x_ + (w - w * 1 / 6 - w * 3 / 7) * 1 / 2, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_4, null, "建筑材料", 0.5f, null, false, 0, 0);

                // 单元格4-5
                x_ = x_ + (w - w * 1 / 6 - w * 3 / 7) * 1 / 2;
                Envelope cel_4_5 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_5, null, "备注", 0.5f, null, false, 0, 0);

                // 单元格5-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_5_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_5_1, null, lc, 0.5f, null, false, 0, 0);

                // 单元格5-2
                x_ = x + w * 1 / 6;
                Envelope cel_5_2 = new Envelope(x_, y_, x_ + w * 1 / 7, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_5_2, null, String.format("%.2f",c.cwholeArea), 0.5f, null, false, 0, 0);

                // 单元格5-3
                x_ = x_ + w * 1 / 7;
                String halfAreaText=c.cHalfArea==0d?"--":String.format("%.2f",c.cHalfArea*2)+"/2";
                Envelope cel_5_3 = new Envelope(x_, y_, x_ + w * 1 / 7, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_5_3, null, halfAreaText, 0.5f, null, false, 0, 0);

                // 单元格5-4
                x_ = x_ + w * 1 / 7;
                Envelope cel_5_4 = new Envelope(x_, y_, x_ + w * 1 / 7, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_5_4, null, String.format("%.2f",c.cjzmj), 0.5f, null, false, 0, 0);
                // 单元格5-5
                x_ = x_ + w * 1 / 7;
                Envelope cel_5_5 = new Envelope(x_, y_, x_ + (w - w * 1 / 6 - w * 3 / 7) * 1 / 2, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_5_5, null, c.getJzcl(), 0.5f, null, false, 0, 0);

                // 单元格5-6
                x_ = x_ + (w - w * 1 / 6 - w * 3 / 7) * 1 / 2;
                Envelope cel_5_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_5_6, null, c.bz, 0.3f, null, false, 0, 0);

                // 单元格6-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_6_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_6_1, null, "合计", 0.5f, null, false, 0, 0);

                // 单元格6-2
                x_ = x + w * 1 / 6;
                Envelope cel_6_2 = new Envelope(x_, y_, x_ + w * 1 / 7, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_6_2, null, String.format("%.2f",c.cwholeArea), 0.5f, null, false, 0, 0);

                // 单元格6-3
                x_ = x_ + w * 1 / 7;
                Envelope cel_6_3 = new Envelope(x_, y_, x_ + w * 1 / 7, y_ - h, p_extend.getSpatialReference());
                String cHalfAreaHz=c.cHalfArea<=0d?"":String.format("%.2f",c.cHalfArea);
                dxf.write(cel_6_3, null,cHalfAreaHz , 0.5f, null, false, 0, 0);

                // 单元格6-4
                x_ = x_ + w * 1 / 7;
                Envelope cel_6_4 = new Envelope(x_, y_, x_ + w * 1 / 7, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_6_4, null, String.format("%.2f",c.cjzmj), 0.5f, null, false, 0, 0);
                // 单元格6-5
                x_ = x_ + w * 1 / 7;
                Envelope cel_6_5 = new Envelope(x_, y_, x_ + (w - w * 1 / 6 - w * 3 / 7) * 1 / 2, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_6_5, null, "----", 0.5f, null, false, 0, 0);

                // 单元格6-6
                x_ = x_ + (w - w * 1 / 6 - w * 3 / 7) * 1 / 2;
                Envelope cel_6_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_6_6, null, "----", 0.5f, null, false, 0, 0);

                // 单元格 7-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_7_1 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_7_1, null, "现状绘制图", 0.5f, null, false, 0, 0);

                // 单元格 8-1   分层图形
                x_ = x;
                y_ = y_ - h;
                Envelope cel_8_1 = new Envelope(x_, y_, x + w, y - p_height + 2 * h, p_extend.getSpatialReference());
                dxf.write(cel_8_1);
                GeodeticDistanceResult d_move_1 = null;
                if (!MapHelper.geometry_equals(o_center, cel_8_1.getCenter())) {
                    d_move_1 = GeometryEngine.distanceGeodetic(o_center, cel_8_1.getCenter(), MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
                }

                List<Feature> gs_ = new ArrayList<>();
//                dxf.write(f_zd.getGeometry());
                if (alpha==0.001f){
                    alpha = (float) (Math.PI * MapHelper.geometry_get_azimuth(f_zd.getGeometry()) / 180);
                }
                Envelope e_c = MapHelper.geometry_combineExtents_Feature(fs_c);
                e_c = MapHelper.geometry_get(e_c, f_zd.getGeometry().getSpatialReference());
                Point p_c = e_c.getCenter();
//                com.esri.arcgisruntime.geometry.Geometry g=MapHelper.geometry_get(f_zd.getGeometry(),p_c,-alpha);
                List<Feature> fs = MapHelper.geometry_get(fs_c, p_c, -alpha);

                List<Feature> fs_fuse = new ArrayList<>(); //  需要融合的附属结构
                for (Feature f : fs) {
                    FeatureTable featureTable = f.getFeatureTable();
                    if(featureTable==mapInstance.getTable(FeatureHelper.TABLE_NAME_H_FSJG)){
                        String type= FeatureHelper.Get(f,"TYPE","");
                        String fhmc= FeatureHelper.Get(f,"FHMC","");
                        if (fhmc.equals("阳台")||fhmc.equals("飘窗")){
                            if (type.equals("1")){
                                fs_fuse.add(f);
                            }
                        }
                    }
                }
                //  需要融合的附属结构
                List<List<Feature>>fs_fuses = new ArrayList<>(); //  需要融合的附属结构
                List<Feature> fs_fuse_;
                List<Feature> fs_=new ArrayList<>();
                fs_.addAll(fs);
                for (Feature f : fs_) {
                    if (f.getFeatureTable() == mapInstance.getTable(FeatureHelper.TABLE_NAME_H)) {
                        fs_fuse_ = new ArrayList<>();
                        String id = FeatureHelper.Get(f, "ID", "");
                        for (Feature feature : fs_fuse) {
                            if (!TextUtils.isEmpty(id) && id.equals(FeatureHelper.Get(feature, "HID", ""))) {
                                fs_fuse_.add(feature);
                                fs.remove(feature);
                            }
                        }
                        if (fs_fuse_.size() != 0) {
                            fs_fuse_.add(f);
                            fs.remove(f);
                        }
                        if (fs_fuse_ != null && fs_fuse_.size() > 0) {
                            fs_fuses.add(fs_fuse_);
                        }

                    }
                }
                for (List<Feature> fs_fus : fs_fuses) {
                    List<Geometry> gs = MapHelper.geometry_get(fs_fus);
                    Geometry g = GeometryEngine.union(gs);
//                    g = MapHelper.geometry_move(g, d_move_1);
//                    dxf.write(g);
                    Feature f=null;
                    for (Feature fsFus : fs_fus) {
                        if (FeatureHelper.TABLE_NAME_H.equals(fsFus.getFeatureTable().getTableName())){
                            if (f==null){
                                f= MapHelper.cloneFeature(fsFus);
                            }
                        }
                    }
                    if (f!=null){
                        f.setGeometry(g);
                        fs.add(f);
                    }
                }
                if (fs_c != null&&fs.size()>0) {
//                    dxf.write(mapInstance, fs_1, null, d_move_1);
                    dxf.write(mapInstance, fs, null, d_move_1,0,1,new LineLabel());
                }
                // 单元格 9-1

                Envelope cel_p_n = new Envelope(x + w - 2 * o_split, y_, x_ + w, y_ - 2 * o_split, p_extend.getSpatialReference());
                Point p_n = cel_p_n.getCenter();
                writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split, -alpha);

                x_ = x;
                y_ = y - p_height + 2 * h;
                Envelope cel_9_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_9_1, null, "丈量者", 0.5f, null, false, 0, 0);

                // 单元格 9-2
                x_ = x_ + w * 1 / 6;
                Envelope cel_9_2 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_9_2, null, "", 0.5f, null, false, 0, 0);

                // 单元格 9-3
                x_ = x_ + w * 1 / 6;
                Envelope cel_9_3 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_9_3, null, "丈量日期", 0.5f, null, false, 0, 0);

                // 单元格 9-4   日期
                Calendar calendar = Calendar.getInstance();
                String auditDate = calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + (calendar.get(Calendar.DAY_OF_MONTH) + "日");
                x_ = x_ + w * 1 / 6;
                Envelope cel_9_4 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_9_4, null, auditDate, 0.5f, null, false, 0, 0);

                // 单元格 9-5
                x_ = x_ + w * 1 / 6;
                Envelope cel_9_5 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.write(cel_9_5, null, "概略比例尺", 0.5f, null, false, 0, 0);

                // 单元格 9-6
                x_ = x_ + w * 1 / 6;
                Envelope cel_9_6 = new Envelope(x_, y_, x + w, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.write(cel_9_6, null, "1:200", 0.5f, null, false, 0, 0);

                // 单元格 10-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_10_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_1, null, "检查者", 0.5f, null, false, 0, 0);

                // 单元格 9-2
                x_ = x_ + w * 1 / 6;
                Envelope cel_10_2 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_2, null, "", 0.5f, null, false, 0, 0);

                // 单元格 9-3
                x_ = x_ + w * 1 / 6;
                Envelope cel_10_3 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_3, null, "检查日期", 0.5f, null, false, 0, 0);

                // 单元格 9-4   日期
                String date = calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + (calendar.get(Calendar.DAY_OF_MONTH) + "日");
                x_ = x_ + w * 1 / 6;
                Envelope cel_10_4 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_4, null, auditDate, 0.5f, null, false, 0, 0);

            }
            // write 房产图
            {
                Envelope envelope = getPageExtend(fs_map_croup.size());
                double w = p_width; //行宽
                // 左上角
                double x = envelope.getXMin();
                double y = envelope.getYMax();

                double x_ = x;
                double y_ = y;

                // 单元格1-1
                Envelope cel_1_1 = new Envelope(x_, y_, x_ + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_1, null, "房屋平面图（总）", 0.5f, null, false, 0, 0);

                // 单元格4-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_4_1 = new Envelope(x_, y_, x + w, y - p_height + 2 * h, p_extend.getSpatialReference());
                dxf.write(cel_4_1);

                Envelope e_c = MapHelper.geometry_combineExtents_Feature(fs_zAndFs);
                e_c = MapHelper.geometry_get(e_c, f_zd.getGeometry().getSpatialReference());
                Point p_c = e_c.getCenter();
                List<Feature> fs = MapHelper.geometry_get(fs_zAndFs, p_c, -alpha);

                GeodeticDistanceResult d_move_1 = null;
                if (!MapHelper.geometry_equals(o_center, cel_4_1.getCenter())) {
                    d_move_1 = GeometryEngine.distanceGeodetic(o_center, cel_4_1.getCenter(), MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
                }
                dxf.write(mapInstance, fs, null, d_move_1,0,1,new LineLabel(),true);

                Point p_n = new Point(cel_4_1.getXMax() - o_split , cel_4_1.getYMax() - o_split);
                writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split,-alpha);

                x_ = x;
                y_ = y - p_height + 2 * h;
                Envelope cel_9_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_9_1, null, "丈量者", 0.5f, null, false, 0, 0);

                // 单元格 9-2
                x_ = x_ + w * 1 / 6;
                Envelope cel_9_2 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_9_2, null, "", 0.5f, null, false, 0, 0);

                // 单元格 9-3
                x_ = x_ + w * 1 / 6;
                Envelope cel_9_3 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_9_3, null, "丈量日期", 0.5f, null, false, 0, 0);

                // 单元格 9-4   日期
                Calendar calendar = Calendar.getInstance();
                String auditDate = calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + (calendar.get(Calendar.DAY_OF_MONTH) + "日");
                x_ = x_ + w * 1 / 6;
                Envelope cel_9_4 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_9_4, null, auditDate, 0.5f, null, false, 0, 0);

                // 单元格 9-5
                x_ = x_ + w * 1 / 6;
                Envelope cel_9_5 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.write(cel_9_5, null, "概略比例尺", 0.5f, null, false, 0, 0);

                // 单元格 9-6
                x_ = x_ + w * 1 / 6;
                Envelope cel_9_6 = new Envelope(x_, y_, x + w, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.write(cel_9_6, null, "1:200", 0.5f, null, false, 0, 0);

                // 单元格 10-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_10_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_1, null, "检查者", 0.5f, null, false, 0, 0);

                // 单元格 9-2
                x_ = x_ + w * 1 / 6;
                Envelope cel_10_2 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_2, null, "", 0.5f, null, false, 0, 0);

                // 单元格 9-3
                x_ = x_ + w * 1 / 6;
                Envelope cel_10_3 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_3, null, "检查日期", 0.5f, null, false, 0, 0);

                // 单元格 9-4   日期
                String date = calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + (calendar.get(Calendar.DAY_OF_MONTH) + "日");
                x_ = x_ + w * 1 / 6;
                Envelope cel_10_4 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_4, null, auditDate, 0.5f, null, false, 0, 0);

            }

        } catch (Exception es) {
            dxf.error("生成失败", es, true);
        }
        return this;
    }

    private void writeN(Point p, double w, float alpha) throws Exception {
        PointCollection ps = new PointCollection(p.getSpatialReference());
        ps.add(p);
        ps.add(new Point(p.getX() - w / 6, p.getY() - w / 2));
        ps.add(new Point(p.getX(), p.getY() + w / 2));
        ps.add(new Point(p.getX() + w / 6, p.getY() - w / 2));
        Polygon polygon = new Polygon(ps, p.getSpatialReference());
        Envelope extent = polygon.getExtent();
        dxf.write(MapHelper.geometry_get(polygon, p, alpha), null);
        // N
        PointCollection p_ = new PointCollection(p.getSpatialReference());
        p_.add(new Point(p.getX() - w / 12, p.getY() - w / 8 + w));
        p_.add(new Point(p.getX() - w / 12, p.getY() + w / 8 + w));
        p_.add(new Point(p.getX() + w / 12, p.getY() - w / 8 + w));
        p_.add(new Point(p.getX() + w / 12, p.getY() + w / 8 + w));
        Polyline polyline = new Polyline(p_, p.getSpatialReference());
        Geometry geometry = MapHelper.geometry_get(polyline, p, alpha);
        dxf.writeLine(DxfTemplet.Get_POLYLINE(MapHelper.geometry_getPoints(geometry), "Continuous", 0, 0, "0", ""));

    }

    public DxfFcfct_enshi save() throws Exception {
        if (dxf != null) {
            dxf.save();
        }
        return this;
    }

    public Envelope getPageExtend(int page) {
        double m = page * (p_width + o_split);
        double x_min = p_extend.getXMin() + m;
        double x_max = p_extend.getXMax() + m;
        double y_min = p_extend.getYMin();
        double y_max = p_extend.getYMax();
        return new Envelope(x_min, y_min, x_max, y_max, p_extend.getSpatialReference());
    }

}
