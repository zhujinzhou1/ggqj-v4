package com.ovit.app.map.bdc.ggqj.map.model;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZRZ;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * Created by XW on 2018/9/29. 房产图 固定1:200 比例尺
 */

public class DxfFengcengtu_liangzihu {

    MapInstance mapInstance;
    SpatialReference spatialReference;

    DxfAdapter dxf;
    private String dxfpath;
    private String bdcdyh;
    private List<Feature> fs_all;
    private List<Feature> fs_hAndFs;
    private Feature f_zd;
    private Point o_center;   // o 点
    private Envelope o_extend;// 真实图形的范围
    private final double o_split=2d;// 单元间隔
    private final double p_width = 34d;// 页面宽
    private final double p_height = 45d;// 页面高
    private final double h = 1.26d; // 行高
    private float o_fontsize;// 字体大小
    private String o_fontstyle = "宋体";// 字体大小
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;

    public DxfFengcengtu_liangzihu(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfFengcengtu_liangzihu set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfFengcengtu_liangzihu set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfFengcengtu_liangzihu set(String bdcdyh, Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_h_fsjg) {
        this.bdcdyh = bdcdyh;
        this.f_zd = f_zd;
        fs_hAndFs = new ArrayList<>();
        fs_hAndFs.addAll(fs_z_fsjg);
        fs_hAndFs.addAll(fs_h);
        fs_hAndFs.addAll(fs_h_fsjg);
        this.fs_zrz=fs_zrz;
        fs_all = new ArrayList<>();
        fs_all.addAll(fs_zrz);
        fs_all.addAll(fs_hAndFs);
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

    public DxfFengcengtu_liangzihu write() throws Exception {
        getExtend(); // 多大范围
        ArrayList cs = new ArrayList<>();
        ArrayList<Map.Entry<String, List<Feature>>> fs_s = FeatureViewZRZ.GroupbyC_Sort(fs_hAndFs);
        LinkedHashMap<String, List<Feature>> fs_croup = FeatureViewZRZ.GroupbyC(fs_hAndFs);

        int page_count = (int) Math.ceil(fs_croup.size() / 2f);  //  多少页

        if (dxf == null) {
            dxf = new DxfAdapter();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            for (int page = 0; page < page_count; page++) {

                Envelope envelope = getPageExtend(page);
                Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
                dxf.writeText(p_title, "房产图", 1.0f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                Point p_unit = new Point(envelope.getXMax(), envelope.getYMax() + o_split * 0.5, envelope.getSpatialReference());
                dxf.writeText(p_unit, "单位：米·平方米", o_fontsize * 0.5f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, 0, null, null);
                double w = p_width; //行宽
                // 左上角
                double x = envelope.getXMin();
                double y = envelope.getYMax();

                double x_ = x;
                double y_ = y;

                // 单元格1-1
                Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 7 / 45, y_ - h, p_extend.getSpatialReference());
//                dxf.write(cel_1_1,"不动产单元号");
                dxf.write(cel_1_1, null, "宗地代码", 0.5f, null, false, 0, 0);
                // 单元格1-2
                x_ = x_ + w * 7 / 45;
                Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 2 / 5, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_2, null, Get(f_zd, "ZDDM", ""), 0.5f, null, false, 0, 0);
                // 单元格1-3
                x_ = x_ + w * 2 / 5;
                Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_3, null, "权利人", 0.5f, null, false, 0, 0);
                // 单元格1-4
                x_ = x_ + w * 1 / 9;
                Envelope cel_1_4 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_4, null, Get(f_zd, "QLRXM", ""), 0.5f, null, false, 0, 0);

                // 单元格2-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 7 / 45, y_ - 3 * h, p_extend.getSpatialReference());
                dxf.write(cel_2_1, null, "建筑面积", 0.5f, null, false, 0, 0);
                // 单元格2-2
                x_ = x_ + w * 7 / 45;
                Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 8 / 45, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_2, null, "所在层", 0.5f, null, false, 0, 0);

                // 单元格2-3
                x_ = x_ + w * 8 / 45;
                Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_3, null, "第"+(page*2+1)+"层", 0.5f, null, false, 0, 0);

                // 单元格2-4
                x_ = x_ + w * 1 / 9;
                Envelope cel_2_4 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_4, null, "第"+(page*2+2)+"层", 0.5f, null, false, 0, 0);
                // 单元格2-5
                x_ = x_ + w * 1 / 9;
                Envelope cel_2_5 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_5);

                // 单元格2-6
                x_ = x_ + w * 1 / 9;
                Envelope cel_2_6 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_6);
                // 单元格2-6
                x_ = x_ + w * 1 / 9;
                Envelope cel_2_7 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_7);
                // 单元格2-6
                x_ = x_ + w * 1 / 9;
                Envelope cel_2_8 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_8, null, "合计", 0.5f, null, false, 0, 0);

                // 单元格3-2
                x_ = x + w * 7 / 45;
                y_ = y_ - h;
                Envelope cel_3_2 = new Envelope(x_, y_, x_ + w * 8 / 45, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_2, null, "专有建筑面积", 0.5f, null, false, 0, 0);

                // 单元格3-3
                x_ = x_ + w * 8 / 45;
                Envelope cel_3_3 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                double cmj_1=FeatureViewZRZ.hsmj_jzmj(fs_croup.get((page*2+1)+""));
                dxf.write(cel_3_3, null, AiUtil.GetValue(cmj_1,"", AiUtil.F_FLOAT2), 0.5f, null, false, 0, 0);

                // 单元格3-4
                x_ = x_ + w * 1 / 9;
                Envelope cel_3_4 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                double cmj_2=FeatureViewZRZ.hsmj_jzmj(fs_croup.get((page*2+2)+""));
                dxf.write(cel_3_4, null, AiUtil.GetValue(cmj_2,"", AiUtil.F_FLOAT2), 0.5f, null, false, 0, 0);

                // 单元格3-5
                x_ = x_ + w * 1 / 9;
                Envelope cel_3_5 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_5);

                // 单元格3-6
                x_ = x_ + w * 1 / 9;
                Envelope cel_3_6 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_6);
                // 单元格3-7
                x_ = x_ + w * 1 / 9;
                Envelope cel_3_7 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_7);
                // 单元格3-8
                x_ = x_ + w * 1 / 9;

                Envelope cel_3_8 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                double mjhz=cmj_1+cmj_2;
                dxf.write(cel_3_8, null, AiUtil.GetValue(mjhz,"", AiUtil.F_FLOAT2), 0.5f, null, false, 0, 0);

                // 单元格4-2
                x_ = x + w * 7 / 45;
                y_ = y_ - h;
                Envelope cel_4_2 = new Envelope(x_, y_, x_ + w * 8 / 45, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_2, null, "分摊建筑面积", 0.5f, null, false, 0, 0);

                // 单元格4-3
                x_ = x_ + w * 8 / 45;
                Envelope cel_4_3 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_3, null, "", 0.5f, null, false, 0, 0);

                // 单元格4-4
                x_ = x_ + w * 1 / 9;
                Envelope cel_4_4 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_4, null, "", 0.5f, null, false, 0, 0);
                // 单元格4-5
                x_ = x_ + w * 1 / 9;
                Envelope cel_4_5 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_5);

                // 单元格4-6
                x_ = x_ + w * 1 / 9;
                Envelope cel_4_6 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_6);
                // 单元格4-7
                x_ = x_ + w * 1 / 9;
                Envelope cel_4_7 = new Envelope(x_, y_, x_ + w * 1 / 9, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_7);
                // 单元格4-8
                x_ = x_ + w * 1 / 9;
                Envelope cel_4_8 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_8, null, "", 0.5f, null, false, 0, 0);

                // 单元格5-1
                x_ = x;
                y_ = y_ - h;
                // 单元格5-1-1  楼层
                Envelope cel_5 = new Envelope(x_, y_, x + w, y-p_height, p_extend.getSpatialReference());
                dxf.write(cel_5);

                Envelope cel_5_1_1 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                Point p_5_1_1 = new Point(cel_5_1_1.getCenter().getX(), cel_5_1_1.getCenter().getY(), p_extend.getSpatialReference());
                dxf.writeMText(p_5_1_1, "第"+(page*2+1)+"层平面图", o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);

                Envelope cel_5_1 = new Envelope(x_, y_, x + w, y_ - (p_height - 4 * h) / 2, p_extend.getSpatialReference());
                GeodeticDistanceResult d_move_1 = null;
                if (!MapHelper.geometry_equals(o_center, cel_5_1.getCenter())) {
                    d_move_1 = GeometryEngine.distanceGeodetic(o_center, cel_5_1.getCenter(), MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
                }
//                dxf.write(cel_5_1);
                List<Feature> fs_1= fs_croup.get(page*2+1 + "");
                if (fs_1!=null){
                dxf.write(mapInstance, fs_1, null, d_move_1);
                }

                // 单元格5-2
                y_ = y_ - (p_height - 4 * h) / 2;
                // 单元格5-1-2  楼层
                Envelope cel_5_2_1 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                Point p_5_2_1 = new Point(cel_5_2_1.getCenter().getX(), cel_5_2_1.getCenter().getY(), p_extend.getSpatialReference());
                dxf.writeMText(p_5_2_1, "第"+(page*2+2)+"层平面图", o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);

                Envelope cel_5_2 = new Envelope(x_, y_, x + w, y - p_height, p_extend.getSpatialReference());
                GeodeticDistanceResult d_move_2 = null;
                if (!MapHelper.geometry_equals(o_center, cel_5_2.getCenter())) {
                    d_move_2 = GeometryEngine.distanceGeodetic(o_center, cel_5_2.getCenter(), MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
                }
//                dxf.write(cel_5_2);
                List<Feature> fs_2=fs_croup.get(page*2+2 + "");
                if (fs_2!=null){
                    dxf.write(mapInstance, fs_2, null, d_move_2);
                }

                Point p_n = new Point(cel_5_1.getXMax() - o_split, cel_5_1.getYMax() - o_split);
                dxf.write(p_n, null, "N", 0.45f, null, false, 0, 0);
                writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split);

                // 单元格4-0
                x_ = x;
                y_ = y - 3 * h - w;
                Envelope cel_4_0 = new Envelope(x_ - w * 1 / 6 * 1 / 4, y_ + 10 * h, x_, y_ - 3 * h, p_extend.getSpatialReference());
                Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
                dxf.writeMText(p_4_0, "中\n南\n电\n力\n工\n程\n顾\n问\n集\n团\n中\n南\n电\n力\n设\n计\n院\n有\n限\n公\n司\n", o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);
                // 落款
                x_ = x + o_split * 2;
                y_ = y_ - p_height + 3 * h + w;
                Point p_l = new Point(x_ + h, y_ - h);
                dxf.writeMText(p_l, "2018年解析法测绘界址点", o_fontsize, o_fontstyle, 0, 0, 2, 0, null, null);

                x_ = x + w / 2;
                Point p_c = new Point(x_, y_ - h);
                dxf.writeText(p_c, "1:200", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                x_ = x + w - o_split * 2.5;
                Point p_r = new Point(x_, y_ - h);
                Calendar c = Calendar.getInstance();
                String auditDate = c.get(Calendar.YEAR) + "." + (c.get(Calendar.MONTH) + 1);
//                    + "." + (c.get(Calendar.DAY_OF_MONTH));
                c.add(Calendar.DATE, -1);
                String drawDate = c.get(Calendar.YEAR) + "." + (c.get(Calendar.MONTH) + 1);
//                    + "." + c.get(Calendar.DAY_OF_MONTH);
                dxf.writeMText(p_r, "制图 ：潘文州 " + drawDate + "\n审核 ：鲍  迪 " + auditDate, o_fontsize, o_fontstyle, 0, 0, 2, 0, null, null);
            }
        } catch(Exception es){
                dxf.error("生成失败", es, true);
            }
            return this;
        }

    private void writeN(Point p, double w) throws Exception {
        PointCollection ps = new PointCollection(p.getSpatialReference());
        ps.add(p);
        ps.add(new Point(p.getX() - w / 6, p.getY() - w / 2));
        ps.add(new Point(p.getX(), p.getY() + w / 2));
        ps.add(new Point(p.getX() + w / 6, p.getY() - w / 2));
        dxf.write(new Polygon(ps, p.getSpatialReference()), null);
    }

    public DxfFengcengtu_liangzihu save() throws Exception {
        if (dxf != null) {
            dxf.save();
        }
        return this;
    }
    public class C {
        String zh;
        Feature zrz;
        String lc;
        List<Feature> fs;
        public  C(String zh,Feature zrz,String lc,List<Feature> fs){
            this.zh = zh;
            this.zrz = zrz;
            this.lc= lc;
            this.fs = fs;
        }
        public String getName(){
            int zh_i =   AiUtil.GetValue(StringUtil.getTextOnlyIn(zh,"0123456789"),0);
            String  name  = zh;
            if(zh_i>0){name =  zh_i+"";}
            name = name.replace("幢","") +"幢"+lc.replace("层","")+"层";
            return  name ;
        }

        public double getJzmj(){
            List<Feature> featureHs=new ArrayList<>();
            for (Feature f : fs) {
                if (f!=null&&f.getFeatureTable()!=mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG)){
                    featureHs.add(f);
                }
            }
            return FeatureViewZRZ.hsmj_jzmj(featureHs);
        }

    }
    public Envelope getPageExtend( int page){
        double m = page *(p_width+ o_split);
        double x_min = p_extend.getXMin() + m;
        double x_max = p_extend.getXMax() + m;
        double y_min = p_extend.getYMin();
        double y_max = p_extend.getYMax();
        return  new Envelope( x_min,y_min , x_max,y_max ,p_extend.getSpatialReference());
    }

}
