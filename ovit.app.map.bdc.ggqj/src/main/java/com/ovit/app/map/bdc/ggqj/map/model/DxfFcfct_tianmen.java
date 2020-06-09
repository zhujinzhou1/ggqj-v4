package com.ovit.app.map.bdc.ggqj.map.model;

import android.text.TextUtils;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.emoticonskeyboard.db.DBHelper;
import com.ovit.app.lzdb.DbTemplet;
import com.ovit.app.lzdb.bean.STAnno;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZRZ;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.LineLabel;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.db.DB;
import com.ovit.app.util.db.DBManager;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfHelper;
import com.ovit.app.util.gdal.cad.DxfTemplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * Created by XW on 2018/9/29. 房产图 固定1:200 比例尺
 * 天门版房产分层图
 */

public class DxfFcfct_tianmen {

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
    private  double o_split = 2d;// 单元间隔
    private  double p_width = 70d;// 页面宽
    private  double p_height = 52d;// 页面高
    private  double h = 1.5d; // 行高
    private float o_fontsize=0.6f;// 字体大小
    private double scale;
    private String o_fontstyle = "宋体";// 字体
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;
    private List<Feature> fs_fsss;
    private String jgrq;
    private Envelope o_extend_1;
    private ArrayList<Map.Entry<String, List<Feature>>> fs_map_croup;

    Map<String,List> dbMap =null;
    private List<Feature> fs_h;
    private List<Feature> fs_c;
    private List<Feature> fs_z_fsjg;
    private List<Feature> fs_h_fsjg;
    private List<Feature> fs_hjxx;
    private List<Feature> fs_qlrxx;
    private List<Feature> fs_jzd;

    public DxfFcfct_tianmen(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfFcfct_tianmen set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfFcfct_tianmen set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfFcfct_tianmen set(String bdcdyh, Feature f_zd,List<Feature> fs_fsss,
                                List<Feature> fs_zrz, List<Feature> fs_z_fsjg,
                                List<Feature> fs_h, List<Feature> fs_h_fsjg,
                                List<Feature> fs_c,
                                List<Feature> fs_qlrxx,
                                List<Feature> fs_hjxx,
                                List<Feature> fs_jzd) {
        this.bdcdyh = bdcdyh;
        this.f_zd = f_zd;
        this.fs_zrz=fs_zrz;
        this.fs_fsss=fs_fsss;
        this.fs_h=fs_h;
        this.fs_c=fs_c;
        this.fs_z_fsjg=fs_z_fsjg;
        this.fs_h_fsjg=fs_h_fsjg;
        this.fs_jzd=fs_jzd;
        this.fs_hjxx=fs_hjxx;
        this.fs_qlrxx=fs_qlrxx;

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
        if (fs_z_fsjg_clone!=null){
         fs_zAndFs.addAll(fs_z_fsjg_clone);
        }
        if (fs_h_fsjg_clone!=null){
        fs_zAndFs.addAll(fs_h_fsjg_clone);
        }
        return this;
    }

    // 获取范围
    public Envelope getExtend() {
        o_extend = MapHelper.geometry_combineExtents_Feature(fs_all);
        o_extend = MapHelper.geometry_get(o_extend, spatialReference); //  图形范围
        o_center = o_extend.getCenter(); // 中心点
        // 比例尺
        double height = o_extend.getHeight();
        double width = o_extend.getWidth();

        double v_h = height *(2+0.4)/ p_height;
        double v_w = width *(2+0.4)/ p_width;

        double niceScale_h = DxfHelper.getNiceScale(v_h);
        double niceScale_w = DxfHelper.getNiceScale(v_w);

        scale=niceScale_w>niceScale_h?niceScale_w:niceScale_h;

        if (scale>1){
            p_width=p_width*scale;
            p_height=p_height*scale;
            h=h*scale;
            o_split=o_split*scale;
            o_fontsize = (float) (0.63f*scale);
        }else {
            scale=1;
        }
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
                if (f != null) {
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
            }
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

        public double getJzmj() {
            List<Feature> featureHs = new ArrayList<>();
            for (Feature f : fs) {
                if (f != null && f.getFeatureTable() != mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG)) {
                    featureHs.add(f);
                }
            }
            return FeatureViewZRZ.hsmj_jzmj(featureHs);
        }

    }

    public DxfFcfct_tianmen write() throws Exception {
        getExtend(); // 多大范围
        ArrayList<Map.Entry<String, List<Feature>>> fs_map_croup = FeatureViewZRZ.GroupbyC_Sort(fs_hAndFs);

        if (dxf == null) {
            dxf=new DxfAdapter();
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
            // 创建dxf
            if (DBManager.Template.getLiteOrm()!=null){
                dbMap= DbTemplet.GetDbMap();
                dxf.setDbMap(dbMap);
            }
        }

        dbMap.get(DbTemplet.DB_C_CALLAYERS).addAll(DbTemplet.GetalLayer(fs_map_croup));
        dbMap.get(DbTemplet.DB_ZD).add(DbTemplet.GetDbZds(f_zd));
        dbMap.get(DbTemplet.DB_QLRXX).addAll(DbTemplet.GetDbQlrxx(fs_hjxx));
        dbMap.get(DbTemplet.DB_QLRXX).addAll(DbTemplet.GetDbQlrxx(fs_qlrxx));
        dbMap.get(DbTemplet.DB_JZD).addAll(DbTemplet.GetDbJzd(fs_jzd));
        dbMap.get(DbTemplet.DB_SVMETADATA).addAll(DbTemplet.GetSvMetadata(fs_zrz,f_zd,mapInstance));
        //dbMap.get(DbTemplet.DB_TDOOR).addAll(DbTemplet.GetTDoor(fs_h,f_zd,fs_h_fsjg,fs_z_fsjg));
        //dbMap.get(DbTemplet.DB_TLAYER).addAll(DbTemplet.GetTLayer(fs_map_croup,fs_c));
        dbMap.get(DbTemplet.DB_FSSS).addAll(DbTemplet.GetDbFsss(fs_fsss));

        try {
            List<String[]> list_z=new ArrayList<>();
            Map<String,String> map_lable=new HashMap<>();
            for(Feature f_zrz :fs_zrz){
                String[] z={"","","","","","","","","","","",""};
                z[0]= StringUtil.GetFormatNumber(FeatureHelper.Get(f_zrz,"ZH",""));
                z[1]= FeatureHelper.Get(f_zrz,"JZWMC","");
                map_lable.put(z[0],z[1]);// 户lable
                String zrzh = Get(f_zrz,"ZRZH","");
                String zh = Get(f_zrz,"ZH","");
                List<Feature> fs = new ArrayList<>();
                for(Feature f :fs_hAndFs){
                    String id = Get(f,"ID","");
                    if(id.startsWith(zrzh)){
                        fs.add(f);
                    }
                }
                ArrayList<Map.Entry<String, List<Feature>>> fs_s =  FeatureViewZRZ.GroupbyC_Sort(fs);
                for(Map.Entry<String, List<Feature>> c :fs_s){
                    z[3+fs_s.indexOf(c)]=String.format("%.2f",FeatureViewZRZ.hsmj_jzmj(c.getValue()));
                }
                z[9]=String.format("%.2f", FeatureHelper.Get(f_zrz,"SCJZMJ",0.00d));
                String fwjgName= DicUtil.dic("fwjg", FeatureHelper.Get(f_zrz, "fwjg", ""));
                String fwjg_=  TextUtils.isEmpty(fwjgName)? DicUtil.dic("fwjg_e", "B"):fwjgName;
                if (fwjg_!=null){
                    z[10]=fwjg_.substring(fwjg_.lastIndexOf("[")+1,fwjg_.lastIndexOf("]"));
                }
                String jgrq = FeatureHelper.Get(f_zrz, "JGRQ", "");
                if (!TextUtils.isEmpty(jgrq)&&jgrq.length()>=4){
                    z[11]=jgrq.substring(0,4);
                }
                list_z.add(z);
            }

            dxf.setLableH(map_lable);
            int page_count = fs_map_croup.size()%3==0?fs_map_croup.size()/3:fs_map_croup.size()/3+1;  //  多少页
            float alpha=0.001f;
            for (int page = 1; page < page_count+1; page++) {
                List<C> cs=new ArrayList<>();
                int i1=1+(page-1)*3;
                for (; i1 <= page*3; i1++) {
                    if (i1<=fs_map_croup.size()){
                        Map.Entry<String, List<Feature>> fs_c_map = fs_map_croup.get(i1-1);
                        List<Feature> fs_c = fs_c_map.getValue();
                        if (fs_c!=null&&fs_c.size()>0){
                            C c = new C(fs_c_map.getKey(),fs_c_map.getValue(),fs_zrz);
                            cs.add(c);
                        }
                    }
                }

                Envelope envelope = getPageExtend(page);
                Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
                dxf.writeText(p_title, "房产分层平面图", o_fontsize*2, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                Point  p_unit =  new Point(envelope.getXMax()-o_split*2,envelope.getYMax() + o_split*0.5,envelope.getSpatialReference());
                dxf.writeText(p_unit,"单位：米·平方米",o_fontsize*1.5f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle,0,1,2,0,null,null);

                 Point p_n = new Point(envelope.getXMax() - o_split*0.75 , envelope.getYMax() - o_split*0.75);
                 writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split,-alpha);
                double w = p_width; //行宽
                // 左上角
                double x = envelope.getXMin();
                double y = envelope.getYMax();

                double x_ = x;
                double y_ = y;
                dxf.write(envelope);
                // 单元格1-1
                Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 1 / 24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_1, null, "宗地代码", o_fontsize, null, false, 0, 0);
//                // 单元格1-2
                x_ = x_ + w * 1 / 24;
                Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 7 / 24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_2, null, FeatureHelper.Get(f_zd,FeatureHelper.TABLE_ATTR_ZDDM,""), o_fontsize, null, false, 0, 0);
                // 单元格1-3
                x_ = x_ + w * 7 / 24;
                Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 1 / 24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_3, null, "权利人", o_fontsize, null, false, 0, 0);
                // 单元格1-4
                x_ = x_ + w * 1 / 24;
                Envelope cel_1_4 = new Envelope(x_, y_, x + w/2, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_4, null, FeatureHelper.Get(f_zd,"QLRXM",""), o_fontsize, null, false, 0, 0);

                // 宗地坐落
                x_ = x;
                y_ = y_ - h;
                Envelope cel_0_1 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_0_1, null, "坐落", o_fontsize, null, false, 0, 0);
                // 宗地坐落
                x_ = x_ + w/24;
                Envelope cel_0_2 = new Envelope(x_, y_, x + w/2, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_0_2, null, FeatureHelper.Get(f_zd,"ZL",""), o_fontsize, null, false, 0, 0);

                // 单元格2-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_2_1 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_1, null, "幢号", o_fontsize, null, false, 0, 0);
                // 单元格2-2  框
                x_ = x_ + w/24;
                Envelope cel_2_2 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_2);
                // 写斜线
                dxf.writeLine(Arrays.asList(new Point[]{new Point(x_, y_), new Point(x_ + w/24,y_ - h)}), "", false, 0, 0);
                // 写文字
                Envelope cel_2_2_1 = new Envelope(x_, y_-h/2-h/10, x_ + w/40, y_ - h, p_extend.getSpatialReference());
//                dxf.writeMText(cel_2_2_1.getCenter(),"房屋用途");
                dxf.writeText(cel_2_2_1.getCenter(), "房屋用途", o_fontsize*0.6f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);


                Envelope cel_2_2_2 = new Envelope(x_+ w/48, y_, x_ + w/24, y_ - h/2, p_extend.getSpatialReference());
//                dxf.writeMText(cel_2_2_2.getCenter(),"层面积");
                dxf.writeText(cel_2_2_2.getCenter(), "层面积", o_fontsize*0.6f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                // 单元格2-3  框
                x_ = x_ + w/24;
                Envelope cel_2_3 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_3, null, "-1", o_fontsize, null, false, 0, 0);
                // 单元格2-4  框
                x_ = x_ + w/24;
                Envelope cel_2_4 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_4, null, "1", o_fontsize, null, false, 0, 0);
                // 单元格2-5  框
                x_ = x_ + w/24;
                Envelope cel_2_5 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_5, null, "2", o_fontsize, null, false, 0, 0);
                // 单元格2-6  框
                x_ = x_ + w/24;
                Envelope cel_2_6 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_6, null, "3", o_fontsize, null, false, 0, 0);
                // 单元格2-7  框
                x_ = x_ + w/24;
                Envelope cel_2_7 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_7, null, "4", o_fontsize, null, false, 0, 0);
                // 单元格2-8  框
                x_ = x_ + w/24;
                Envelope cel_2_8 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_8, null, "5", o_fontsize, null, false, 0, 0);
                // 单元格2-9  框
                x_ = x_ + w/24;
                Envelope cel_2_9 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_9, null, "6", o_fontsize, null, false, 0, 0);
                // 单元格2-10  框
                x_ = x_ + w/24;
                Envelope cel_2_10 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_10, null, "幢面积合计", o_fontsize, null, false, 0, 0);
                // 单元格2-11 框
                x_ = x_ + w/24;
                Envelope cel_2_11 = new Envelope(x_, y_, x_ + w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_11, null, "结构", o_fontsize, null, false, 0, 0);
                // 单元格2-12 框
                x_ = x_ + w/24;
                Envelope cel_2_12 = new Envelope(x_, y_, x+ w/2, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_12, null, "建筑年份", o_fontsize, null, false, 0, 0);
                // 房屋绘制表
                Envelope cel_i_j=null;
                for (int i=0;i<7;i++) {
                    x_ = x;
                    y_ = y_ - h;
                    String[] z =null;
                    if (i<list_z.size()){
                        z = list_z.get(i);
                    }
                    for (int j = 0; j < 12; j++) {
                        cel_i_j = new Envelope(x_, y_, x_ + w / 24, y_ - h, p_extend.getSpatialReference());
                        if (z != null) {
                            try {
                                if (j==0){
                                 dxf.write(cel_i_j, null, z[j], o_fontsize*1.5f, null, false, 0, 0);
                                }else {
                                 dxf.write(cel_i_j, null, z[j], o_fontsize*0.7f, null, false, 0, 0);
                                }
                            } catch (Exception e) {
                                dxf.write(cel_i_j, null, "", o_fontsize*0.7f, null, false, 0, 0);
                            }
                        } else {
                            dxf.write(cel_i_j, null, "", o_fontsize*0.7f, null, false, 0, 0);
                        }
                        x_ = x_ + w / 24;
                    }
                }

                // 单元格4-1 框   层面积汇总
                x_ = x;
                y_ = y_ - h;
                Envelope cel_4_1 = new Envelope(x_, y_, x_+ w*2/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_1, null, "层面积合计", o_fontsize, null, false, 0, 0);

                // 单元格4-2 框
                x_ = x_ + w*2/24;
                Envelope cel_4_2 = new Envelope(x_, y_, x_+ w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_2, null, "", o_fontsize*0.7f, null, false, 0, 0);

                // 单元格4-3 框   一层面积汇总
                x_ = x_ + w/24;
                Envelope cel_4_3 = new Envelope(x_, y_, x_+ w/24, y_ - h, p_extend.getSpatialReference());
                //cs.get(1).getJzmj()+""
                dxf.write(cel_4_3, null, page==1?cjzmj(cs,1):"", o_fontsize*0.7f, null, false, 0, 0);

                // 单元格4-4 框   二层面积汇总
                x_ = x_ + w/24;
                Envelope cel_4_4 = new Envelope(x_, y_, x_+ w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_4, null, page==1?cjzmj(cs,2):"", o_fontsize*0.7f, null, false, 0, 0);

                // 单元格4-5 框   三层面积汇总
                x_ = x_ + w/24;
                Envelope cel_4_5 = new Envelope(x_, y_, x_+ w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_5, null,  page==1?cjzmj(cs,3):"", o_fontsize*0.7f, null, false, 0, 0);

                // 单元格4-6 框   四层面积汇总
                x_ = x_ + w/24;
                Envelope cel_4_6 = new Envelope(x_, y_, x_+ w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_6, null, page==2?cjzmj(cs,1):"", o_fontsize*0.7f, null, false, 0, 0);

                // 单元格4-7 框   五层面积汇总
                x_ = x_ + w/24;
                Envelope cel_4_7 = new Envelope(x_, y_, x_+ w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_7, null, page==2?cjzmj(cs,2):"", o_fontsize*0.7f, null, false, 0, 0);

                // 单元格4-8 框   六层面积汇总
                x_ = x_ + w/24;
                Envelope cel_4_8 = new Envelope(x_, y_, x_+ w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_8, null, page==2?cjzmj(cs,3):"", o_fontsize*0.7f, null, false, 0, 0);

                // 单元格4-9 框   建筑面积汇总
                x_ = x_ + w/24;
                Envelope cel_4_9 = new Envelope(x_, y_, x_+ w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_9, null, String.format("%.2f", FeatureHelper.Get(f_zd,"JZMJ",0.00d)), o_fontsize*0.7f, null, false, 0, 0);

                // 单元格4- 10框   结构汇总
                x_ = x_ + w/24;
                Envelope cel_4_10 = new Envelope(x_, y_, x_+ w/24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_10, null, "", o_fontsize, null, false, 0, 0);

                // 单元格4- 11框   建筑年份
                x_ = x_ + w/24;
                Envelope cel_4_11 = new Envelope(x_, y_, x+ w/2, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_11, null, "", o_fontsize, null, false, 0, 0);

                // 单元格5- 1框  1 层平面图
                x_ = x;
                y_=y_-h;
                //
                String s_pmt_1= StringUtil.GetFormatNumber2(1+(page-1)*3+"")+"层平面图";
                Envelope cel_5_t = new Envelope(x_, y_, x+ w*2/24, y_-1.3*h, p_extend.getSpatialReference());
//                dxf.writeMText(cel_5_t.getCenter(),s_pmt);
                dxf.write(cel_5_t.getCenter(), null, s_pmt_1, o_fontsize*1.5f, null, false, 0, 0);
                Envelope cel_5_b = new Envelope(x_, y_-1.5*h, x+ w*2/24, y_-2.5*h, p_extend.getSpatialReference());
//                dxf.writeMText(cel_5_b.getCenter(),"1:300");
                dxf.write(cel_5_b.getCenter(), null, "1:300", o_fontsize*2, null, false, 0, 0);


                Envelope cel_5_1 = new Envelope(x_, y_, x+ w/2, y-p_height+3*h, p_extend.getSpatialReference());
                dxf.write(cel_5_1, null, "", o_fontsize, null, false, 0, 0);

                Point p_m = new Point(cel_5_1.getCenter().getX(),cel_5_1.getYMin() + 2*o_split);
                if (dbMap != null) {
                    STAnno sTAnno = new STAnno();
                    sTAnno.Geometry = DbTemplet.GetStAnnoGeometry(p_m,s_pmt_1,0,DxfHelper.FONT_WIDTH_DEFULT
                            ,o_fontsize*1.5f,DxfHelper.COLOR_BYLAYER,DxfHelper.FONT_STYLE_SONGTI,1);
                    dbMap.get(DbTemplet.DB_TEXT_STANNOS).add(sTAnno);

                }


                dxf.write(mapInstance, cs.get(0).fs, null, DxfHelper.getDistanceMove(cs.get(0).fs,cel_5_1,spatialReference),2, DxfHelper.LINE_LABEL_OUTSIDE,new LineLabel());

                double y_o_1=y-p_height+3*h+list_z.size()*h;
                x_=x;
                String dsc_1="";
                for (int i = 0; i < list_z.size(); i++) {
                    if (TextUtils.isEmpty(dsc_1)&& StringUtil.IsNotEmpty(list_z.get(i)[2+(1+(page-1)*3)])){
                        dsc_1+=list_z.get(i)[0]+"幢"+ StringUtil.GetFormatNumber2(1+(page-1)*3+"")+"层建筑面积"+list_z.get(i)[3+(page-1)*3]+"㎡";
                    }else if (StringUtil.IsNotEmpty(list_z.get(i)[2+(1+(page-1)*3)])){
                        dsc_1+="\n"+list_z.get(i)[0]+"幢"+ StringUtil.GetFormatNumber2(1+(page-1)*3+"")+"层建筑面积"+list_z.get(i)[3+(page-1)*3]+"㎡";
                    }
                }
                Envelope cel_dsc_1 = new Envelope(x_, y_o_1, x+ w/6,y_o_1 -list_z.size()*h, p_extend.getSpatialReference());
                dxf.write(cel_dsc_1.getCenter(), null, dsc_1, o_fontsize, null, false, 0, 0);


                // 单元格6- 1框  备注
                x_ = x;
                y_=y-p_height+3*h;
                Envelope cel_6_1 = new Envelope(x_, y_, x+ w/2, y_-3*h, p_extend.getSpatialReference());
                dxf.write(cel_6_1, null, "", o_fontsize, null, false, 0, 0);

                // 单元格7- 1框  备注
                Envelope cel_7_1 = new Envelope(x_+w/24, y_, x_+w/24+("说明".length() * o_fontsize*1.5f), y_-h, p_extend.getSpatialReference());
                dxf.writeText(cel_7_1.getCenter(), "说明", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

//                String text= "Y：无  墙  檐  廊    ，    不   计  面  积 "+"\n"
//                            +"F：封 闭 阳 台 或 柱 廊 ，  计   全  面  积"+"\n"
//                            +"W：半封闭阳台或无永久性顶盖室外楼梯，计半面积";
//                Envelope cel_8_1 = new Envelope(x_+w/12, y_, x_+w/2, y_-2*h, p_extend.getSpatialReference());
//                dxf.writeMText(cel_8_1.getCenter(), text, o_fontsize, o_fontstyle, 0, 0, 1, 0, null, null);
                // 单元格7- 1框  备注
                x_=x_+w/24+("说明：".length() * o_fontsize*1.5f);
                Envelope cel_7_2 = new Envelope(x_, y_, x_+("Y：无墙檐廊，不计面积".length() * o_fontsize*1.4f), y_-h, p_extend.getSpatialReference());
                dxf.writeMText(cel_7_2.getCenter(), "Y：无墙檐廊，不计面积", o_fontsize, o_fontstyle, 0, 1, 1, 0, null, null);


                Envelope cel_8_1 = new Envelope(x_, y_-h, x_+("F：封闭阳台或柱廊，计全面积".length() * o_fontsize*1.4f), y_-2*h, p_extend.getSpatialReference());
                dxf.writeMText(cel_8_1.getCenter(), "F：封闭阳台或柱廊，计全面积", o_fontsize, o_fontstyle, 0, 1, 2, 0, null, null);

                Envelope cel_9_1 = new Envelope(x_, y_-2*h, x_+("W：半封闭阳台或无永久性顶盖室外楼梯，计半面积".length() * o_fontsize*1.4f) , y_-3*h, p_extend.getSpatialReference());
                dxf.writeMText(cel_9_1.getCenter(), "W：半封闭阳台或无永久性顶盖室外楼梯，计半面积", o_fontsize, o_fontstyle, 0, 1, 3, 0, null, null);

                // 右边单元格框  二 层平面图
                x_ = x+w/2;
                y_=y;
                Envelope cel_1_1_right = new Envelope(x_, y_, x+w, y_-p_height/2, p_extend.getSpatialReference());
                dxf.write(cel_1_1_right, null, "", o_fontsize, null, false, 0, 0);

                String s_pmt_2= StringUtil.GetFormatNumber2(2+(page-1)*3+"")+"层平面图";
                Envelope cel_t_2 = new Envelope(x_, y_, x_+ w*2/24, y_-1.3*h, p_extend.getSpatialReference());
                dxf.write(cel_t_2.getCenter(), null, s_pmt_2, o_fontsize*1.3f, null, false, 0, 0);
                Envelope cel_blc_2 = new Envelope(x_, y_-1.5*h, x_+ w*2/24, y_-2.5*h, p_extend.getSpatialReference());
                dxf.write(cel_blc_2.getCenter(), null, "1:300", o_fontsize*2, null, false, 0, 0);


                if (cs.size()>=2) {
                    dxf.write(mapInstance, cs.get(1).fs, null,  DxfHelper.getDistanceMove(cs.get(1).fs,cel_1_1_right,spatialReference), 2, 0, new LineLabel());
                    // 二层描述
                    double y_o_2 = y_ - p_height / 2 + list_z.size() * h ;
                    String dsc_2 = "";

                    Point p = new Point(cel_1_1_right.getCenter().getX(),cel_1_1_right.getYMin() + 2*o_split);
                    if (dbMap != null) {
                        STAnno sTAnno = new STAnno();
                        sTAnno.Geometry = DbTemplet.GetStAnnoGeometry(p,s_pmt_2,0,DxfHelper.FONT_WIDTH_DEFULT
                                ,o_fontsize*1.5f,DxfHelper.COLOR_BYLAYER,DxfHelper.FONT_STYLE_SONGTI,1);
                        dbMap.get(DbTemplet.DB_TEXT_STANNOS).add(sTAnno);


                    for (int i = 0; i < list_z.size(); i++) {
                        if (TextUtils.isEmpty(dsc_2) && StringUtil.IsNotEmpty(list_z.get(i)[3 + (1 + (page - 1) * 3)])) {
                            dsc_2 += list_z.get(i)[0] + "幢" + StringUtil.GetFormatNumber2(2 + (page - 1) * 3 + "") + "层建筑面积" + list_z.get(i)[4+(page-1)*3] + "㎡";
                        } else if (StringUtil.IsNotEmpty(list_z.get(i)[3 + (1 + (page - 1) * 3)])) {
                            dsc_2 += "\n" + list_z.get(i)[0] + "幢" + StringUtil.GetFormatNumber2(2 + (page - 1) * 3 + "") + "层建筑面积" + list_z.get(i)[4+(page-1)*3] + "㎡";
                        }
                    }
                    Envelope cel_dsc_2 = new Envelope(x_, y_o_2, x_ + w / 6, y_o_2 - list_z.size() * h, p_extend.getSpatialReference());
                    dxf.write(cel_dsc_2.getCenter(), null, dsc_2, o_fontsize, null, false, 0, 0);
                }

                // 右边单元格 2- 1 框  三层平面图
                y_=y_-p_height/2;
                Envelope cel_2_1_right = new Envelope(x_, y_, x+ w, y-p_height, p_extend.getSpatialReference());
                dxf.write(cel_2_1_right, null, "", o_fontsize, null, false, 0, 0);

                String s_pmt_3= StringUtil.GetFormatNumber2(3+(page-1)*3+"")+"层平面图";
                Envelope cel_t_3 = new Envelope(x_, y_, x_+ w*2/24, y_-h*1.5, p_extend.getSpatialReference());
                dxf.write(cel_t_3.getCenter(), null, s_pmt_3, o_fontsize*1.5f, null, false, 0, 0);
                Envelope cel_blc_3 = new Envelope(x_, y_-h*1.5, x_+ w*2/24, y_-2.5*h, p_extend.getSpatialReference());
                dxf.write(cel_blc_3.getCenter(), null, "1:300", o_fontsize*2, null, false, 0, 0);

                if (cs.size()>=3){
                    dxf.write(mapInstance, cs.get(2).fs, null, DxfHelper.getDistanceMove(cs.get(2).fs,cel_2_1_right,spatialReference),2,0,new LineLabel());
                    // 三层描述
                    double y_o_3 = y_ - p_height / 2 + list_z.size() * h;
                    String dsc_3 = "";

                    Point p2 = new Point(cel_2_1_right.getCenter().getX(),cel_2_1_right.getYMin() + 3*o_split);
                    if (dbMap != null) {
                        STAnno sTAnno = new STAnno();
                        sTAnno.Geometry = DbTemplet.GetStAnnoGeometry(p2,s_pmt_3,0,DxfHelper.FONT_WIDTH_DEFULT
                                ,o_fontsize*1.5f,DxfHelper.COLOR_BYLAYER,DxfHelper.FONT_STYLE_SONGTI,1);
                        dbMap.get(DbTemplet.DB_TEXT_STANNOS).add(sTAnno);

                    }


                    for (int i = 0; i < list_z.size(); i++) {
                        if (TextUtils.isEmpty(dsc_3) && StringUtil.IsNotEmpty(list_z.get(i)[4 + (1 + (page - 1) * 3)])) {
                            dsc_3 += list_z.get(i)[0] + "幢" + StringUtil.GetFormatNumber2(3 + (page - 1) * 3 + "") + "层建筑面积" + list_z.get(i)[5+(page-1)*3] + "㎡";
                        } else if (StringUtil.IsNotEmpty(list_z.get(i)[4 + (1+ (page - 1) * 3)])) {
                            dsc_3 += "\n" + list_z.get(i)[0] + "幢" + StringUtil.GetFormatNumber2(3 + (page - 1) * 3 + "") + "层建筑面积" + list_z.get(i)[5+(page-1)*3] + "㎡";
                        }
                    }
                    Envelope cel_dsc_2 = new Envelope(x_, y_o_3, x_ + w / 6, y_o_3 - list_z.size() * h, p_extend.getSpatialReference());
                    dxf.write(cel_dsc_2.getCenter(), null, dsc_3, o_fontsize, null, false, 0, 0);
                }

                // 单元格4-0  绘制单位
                x_=x;
                y_ = y - p_height;
                String hzdw =  GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZDW","");
                Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
                Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
                dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw,"\n"), o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);

                // 绘制日期，审核日期
                Calendar c = Calendar.getInstance();
                String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
                c.add(Calendar.DATE, -1);
                String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");

                Envelope cel_htrq = new Envelope(x_ , y_ , x_+ (auditDate.length() * o_fontsize) * 1.2, y_-h, p_extend.getSpatialReference());
                Point p_auditDate = cel_htrq.getCenter();

                Envelope cel_drawDate = new Envelope(x_ , y_-h , x_+ (drawDate.length() * o_fontsize) * 1.2, y_-2*h, p_extend.getSpatialReference());
                Point p_drawDate =cel_drawDate.getCenter();

                dxf.writeText(p_drawDate, "绘图日期:"+drawDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
                dxf.writeText(p_auditDate,"审核日期:"+auditDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                Point p_blc = new Point(envelope.getCenter().getX() , envelope.getYMin() - o_split * 0.6, envelope.getSpatialReference());
                dxf.writeText(p_blc, "1:300", o_fontsize*1.5f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                // 测绘员 审核员
                String sChr="测绘员："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR","");
                String sShr="审核员："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR","");
                int maxLenth=sChr.length()>sShr.length()?sChr.length():sShr.length();

                Envelope cel_chr = new Envelope(x+w , y-p_height , x+w- (maxLenth * o_fontsize) * 1.2, y-p_height-h, p_extend.getSpatialReference());
//                Point p_chr = new Point(envelope.getCenter().getX() + w * 1 / 3+w*1/10, envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
                Point p_chr = cel_chr.getCenter();
                dxf.writeText(p_chr, sChr, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                Envelope cel_shr = new Envelope(x+w , y-p_height-h , x+w- (maxLenth * o_fontsize) * 1.2, y-p_height-2*h, p_extend.getSpatialReference());
                Point p_shr = cel_shr.getCenter();
                dxf.writeText(p_shr, sShr, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                    if(page==1) {
                        double e_height = cel_5_1.getHeight();
                        Point point_d = new Point(cel_5_1.getXMax() - o_split, cel_5_1.getYMin() + e_height / 2, cel_5_1.getSpatialReference());
                        Point point_x = new Point(cel_5_1.getXMin() + o_split, cel_5_1.getYMin() + e_height / 2, cel_5_1.getSpatialReference());
                        Point point_b = new Point(cel_5_1.getCenter().getX(), cel_5_1.getYMax() - 3 * o_split, cel_5_1.getSpatialReference());
                        Point point_n = new Point(cel_5_1.getCenter().getX(), cel_5_1.getYMin() + 3 * o_split, cel_5_1.getSpatialReference());
                        if (dbMap != null) {
                            STAnno sTAnnod = new STAnno();
                            STAnno stAnnox = new STAnno();
                            STAnno stAnnon = new STAnno();
                            STAnno stAnnob = new STAnno();
                            sTAnnod.Geometry = DbTemplet.GetStAnnoGeometry(point_d, FeatureHelper.Get(f_zd, "ZDSZD", ""), 0, DxfHelper.FONT_WIDTH_DEFULT
                                    , o_fontsize, DxfHelper.COLOR_BYLAYER, DxfHelper.FONT_STYLE_SONGTI, 1);
                            dbMap.get(DbTemplet.DB_TEXT_STANNOS).add(sTAnnod);
                            stAnnox.Geometry = DbTemplet.GetStAnnoGeometry(point_x, FeatureHelper.Get(f_zd, "ZDSZX", ""), 0, DxfHelper.FONT_WIDTH_DEFULT
                                    , o_fontsize, DxfHelper.COLOR_BYLAYER, DxfHelper.FONT_STYLE_SONGTI, 1);
                            dbMap.get(DbTemplet.DB_TEXT_STANNOS).add(stAnnox);
                            stAnnon.Geometry = DbTemplet.GetStAnnoGeometry(point_b, FeatureHelper.Get(f_zd, "ZDSZB", ""), 0, DxfHelper.FONT_WIDTH_DEFULT
                                    , o_fontsize, DxfHelper.COLOR_BYLAYER, DxfHelper.FONT_STYLE_SONGTI, 1);
                            dbMap.get(DbTemplet.DB_TEXT_STANNOS).add(stAnnon);
                            stAnnob.Geometry = DbTemplet.GetStAnnoGeometry(point_n, FeatureHelper.Get(f_zd, "ZDSZN", ""), 0, DxfHelper.FONT_WIDTH_DEFULT
                                    , o_fontsize, DxfHelper.COLOR_BYLAYER, DxfHelper.FONT_STYLE_SONGTI, 1);
                            dbMap.get(DbTemplet.DB_TEXT_STANNOS).add(stAnnob);
                        }
                    }
                }
            }

        } catch (Exception es) {
            dxf.error("生成失败", es, true);
        }
        return this;
    }
   // 获取每层建筑面积
    private String cjzmj(List<C> cs, int i) {
        String cjzmj = "";
        if (cs.size() >=i) {
            cjzmj =String.format("%.2f",cs.get(i - 1).cjzmj);
        }
        return cjzmj;
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

    public DxfFcfct_tianmen save() throws Exception {
        if (dxf != null) {
            dxf.save();
            dxf=null;

            String path = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/db") ;
            if(FileUtils.exsit(path + DBHelper.DB_NAME)){
                FileUtils.deleteFile(path);
            }
            DB db = new DB(DBHelper.DB_NAME,path);
            db.save(dbMap.get(DbTemplet.DB_JZD));
            db.save(dbMap.get(DbTemplet.DB_QLRXX));
            db.save(dbMap.get(DbTemplet.DB_C_CALLAYERS));
            db.save(dbMap.get(DbTemplet.DB_ZD));
            db.save(dbMap.get(DbTemplet.DB_TDOOR));
            db.save(dbMap.get(DbTemplet.DB_SVMETADATA));
            db.save(dbMap.get(DbTemplet.DB_TEXT_STANNOS));
            db.save(dbMap.get(DbTemplet.DB_SHAPE_STREGIONS));
            db.save(dbMap.get(DbTemplet.DB_TLAYER));
            db.save(dbMap.get(DbTemplet.DB_FSSS));
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
