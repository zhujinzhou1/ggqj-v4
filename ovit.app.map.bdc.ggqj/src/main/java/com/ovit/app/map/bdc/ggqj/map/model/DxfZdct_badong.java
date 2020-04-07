package com.ovit.app.map.bdc.ggqj.map.model;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.exception.CrashHandler;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.LineLabel;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfHelper;
import com.ovit.app.util.gdal.cad.DxfTemplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by XW on 2018/9/29. 巴东宗地草图
 */

public class DxfZdct_badong {

    MapInstance mapInstance;
    SpatialReference spatialReference;

    DxfAdapter dxf;
    private String dxfpath;
    private List<Feature> fs_all;
    private List<Feature> fs_hAndFs;
    private Feature f_zd;
    private  List<Feature> fs_zd;
    private  List<Feature> fs_zj_x;
    private  List<Feature> fs_xzdw;
    private  List<Feature> fs_mzdw;
    private Point o_center;   // o 点
    private Envelope o_extend;// 真实图形的范围
    private double o_split=2d;// 单元间隔
    private double p_width = 36d;// 页面宽
    private double p_height = 48d;// 页面高
    private double h = 1.5d; // 行高
    private float o_fontsize=0.6f;// 字体大小
    private double scale;
    private String o_fontstyle = "宋体";// 字体大小
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;
    private List<Feature> fs_z_fsjg;
    private List<Feature> fs_fsjg;
    private List<Feature> fs_h_fsjg;
    private List<Feature> fs_jzd;

    public DxfZdct_badong(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfZdct_badong set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfZdct_badong set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfZdct_badong set(Feature f_zd, List<Feature> fs_zd, List<Feature> fs_zrz
            , List<Feature> fs_z_fsjg, List<Feature> fs_h_fsjg, List<Feature> fs_jzd, List<Feature> fs_zj_x, List<Feature> fs_xzdw, List<Feature> fs_mzdw) {
        this.f_zd = f_zd;
        this.fs_zd = fs_zd;
        this.fs_zj_x = fs_zj_x;
        this.fs_xzdw = fs_xzdw;
        this.fs_mzdw = fs_mzdw;
        List<Feature> fs_all=new ArrayList<>();
        List<Feature> fs_fsjg=new ArrayList<>();
        fs_all.addAll(fs_zd);
        fs_all.addAll(fs_zrz);
        fs_all.addAll(fs_z_fsjg);
        fs_all.addAll(fs_h_fsjg);
        fs_all.addAll(fs_jzd);
        this.f_zd = f_zd;
        this.fs_zrz =fs_zrz;
        this.fs_z_fsjg =fs_z_fsjg;
        this.fs_h_fsjg =fs_h_fsjg;
        this.fs_jzd =fs_jzd;
        this.fs_all =fs_all;

        fs_fsjg.addAll(fs_h_fsjg);
        fs_fsjg.addAll(fs_z_fsjg);
        this.fs_fsjg =fs_fsjg;
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

        double v_h = height *(1+0.15)/ p_height;
        double v_w = width *(1+0.15)/ p_width;

        double niceScale_h=getNiceScale(v_h);
        double niceScale_w=getNiceScale(v_w);

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

    public DxfZdct_badong write() throws Exception {
        getExtend(); // 多大范围
        if (dxf == null) {
            dxf = new DxfAdapter();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(0.8f);
        }
        try {
            Envelope envelope = p_extend;
//            dxf.write(new Envelope(envelope.getCenter(), p_width + o_split * 3, p_height + o_split * 5)); // 图框
            dxf.write(new Envelope(envelope.getCenter(), p_width + o_split * 3, p_height + o_split * 5), DxfHelper.LINETYPE_SOLID_LINE,"",2*o_fontsize, DxfHelper.FONT_STYLE_HZ,false, DxfHelper.COLOR_CYAN,0);

            double w = p_width; //行宽
            // 左上角
            double x = p_extend.getXMin();
            double y = p_extend.getYMax();

            double x_ = x;
            double y_ = y;

            // 单元格1-1
            Envelope cel_1_1 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
//                dxf.write(cel_1_1,"不动产单元号");
            dxf.write(cel_1_1, null, "宗地草图", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
            // 单元格2-1
           x_=x;
           y_=y_-h;

            Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_1, null, "权利人", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
            // 单元格2-2
            x_ = x_ + w * 1 / 6;
            Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_2, null, FeatureHelper.Get(f_zd,"QLRXM",""), o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格2-3
            x_ = x_ + w * 1 / 6;
            Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_3, null, "坐落", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格2-4
            x_ = x_ + w * 1 / 6;
            Envelope cel_2_4 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_4, null, FeatureHelper.Get(f_zd,"ZL",""), o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格3-1
            x_ = x;
            y_ = y_ - h;
            Envelope cel_3_1 = new Envelope(x_, y_, x + w , y- p_height+2*h, p_extend.getSpatialReference());
            dxf.write(cel_3_1, null,"", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);


            List<Feature> fs=new ArrayList<>();
            String zddm= FeatureHelper.Get(f_zd,FeatureHelper.TABLE_ATTR_ZDDM,"");
            fs.addAll(fs_jzd);
            fs.add(f_zd);
            for (Feature f_zrz : fs_zrz) {
                if (FeatureHelper.Get(f_zrz,"ZRZH","").contains(zddm)){
                    fs.add(f_zrz);
                }else {
                dxf.write(f_zrz.getGeometry());
                String text = mapInstance.getLabel(f_zrz,1);
                Point point = GeometryEngine.labelPoint((Polygon) f_zrz.getGeometry());
                dxf.writeMText(point,text, o_fontsize* DxfHelper.FONT_SIZE_FACTOR , "", 0, 1, 2, DxfHelper.COLOR_CARMINE, "JZD", "");
                }
          }
            for (Feature f_fsjg : fs_fsjg) {
                if (FeatureHelper.Get(f_fsjg,"ID","").contains(zddm)){
                    fs.add(f_fsjg);
                }else {
                    dxf.write(f_fsjg.getGeometry());
                 }
            }
            for (Feature f_zd : fs_zd) {
                if (!FeatureHelper.Get(f_zd,FeatureHelper.TABLE_ATTR_ZDDM,"").contains(zddm)) {
                    Geometry g = MapHelper.geometry_get(f_zd.getGeometry(), spatialReference);
                    Point p = GeometryEngine.labelPoint((Polygon) g);
                    float ft = 0.5f;
                    dxf.writeText(new Point(p.getX(), p.getY() + 1 * ft), FeatureHelper.Get(f_zd, "QLRXM", ""), ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 1, 1, "JZD", "302004");
                    dxf.writeText(new Point(p.getX() - 1.5 * ft, p.getY() + 0.1 * ft), FeatureHelper.Get(f_zd, "PRO_ZDDM_F", ""), 0.5f * ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 1, 1, "JZD", "302002");
                    dxf.writeLine(Arrays.asList(new Point[]{new Point(p.getX() - 3 * ft, p.getY()), new Point(p.getX() - 0.1 * ft, p.getY())}), "", false, 1, 0);
                    dxf.writeText(new Point(p.getX() - 1.5 * ft, p.getY() - 0.1 * ft), FeatureHelper.Get(f_zd, "PZYT", ""), 0.5f * ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 3, 1, "JZD", "302003");
                    dxf.writeText(new Point(p.getX(), p.getY()), AiUtil.Scale(FeatureHelper.Get(f_zd, FeatureHelper.TABLE_ATTR_ZDMJ, 0d), 2) + "", 0.5f * ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 0, 2, 1, "JZD", "302005");

                }
            }
            DxfHelper.writeZdsz(dxf,f_zd,f_zd.getGeometry().getExtent(),o_split,0.8f,o_fontstyle);
            fs.addAll(fs_zj_x);
//            fs.addAll(fs_xzdw);
            for (int i = 0; i < fs_xzdw.size(); i++) {
                Feature feature=fs_xzdw.get(i);
                Geometry g = MapHelper.geometry_get(feature.getGeometry(), spatialReference);
                String name = feature.getFeatureTable().getTableName();

                Geometry intersection = GeometryEngine.intersection(envelope, g);
                List<Point> points = MapHelper.geometry_getPoints(intersection);
                String text = mapInstance.getLabel(feature);

                if (points.size()<2){
                    continue;
                }
                int index=points.size()/2;
                Point p_s=points.get(index-1);
                Point p_e=points.get(index);

                float offsety=o_fontsize* DxfHelper.FONT_LABEL_OFFSET;
                Point p_m = MapHelper.point_getMidPoint(p_s, p_e, p_s.getSpatialReference());

                GeodeticDistanceResult distance = MapHelper.geometry_distanceGeodetic(p_s, p_e, MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
                if (!(distance.getDistance() < 0.005)) {
                    if (text!=null){
                        float angle = (float) distance.getAzimuth1();
                        angle = (angle - 90 + 360 * 2) % 360;
//             int a_t = 45;   // 字头  朝北朝东
                        int a_t = 45 + 90; // 字头  朝北朝西
                        offsety = offsety * ((angle >= a_t && angle < a_t + 180) ? -1 : 1);
                        angle = angle + ((angle >= a_t && angle < a_t + 180) ? 180 : 0);
                        double x1 = p_m.getX();
                        double y1 = p_m.getY();
                        // 外
                        x1 += Math.sin(angle * Math.PI / 180d) * offsety;
                        y1 += Math.cos(angle * Math.PI / 180d) * offsety;

                        p_m = new Point(x1, y1, p_m.getSpatialReference());
                        dxf.writeLine(DxfTemplet.Get_TEXT( text, p_m
                                , DxfHelper.FONT_WIDTH_DEFULT, o_fontsize* DxfHelper.FONT_SIZE_FACTOR*0.6f, DxfHelper.FONT_STYLE_SONGTI, angle, 1, 2, DxfHelper.COLOR_GREEN, "", ""));
                    }
                }

                if ("XZDW".equalsIgnoreCase(name)) {
                    if (points == null || points.size() == 0) {
                        ToastMessage.Send("面状地物图形缺失请检查,编号：" + FeatureHelper.Get(feature, "ID"));
                        CrashHandler.WriteLog("面状地物图形缺失请检查", "编号：" + FeatureHelper.Get(feature, "ID"));
                        return this;
                    }
                }
                dxf.writeLine(DxfTemplet.Get_POLYLINE(points, DxfHelper.LINETYPE_SOLID_LINE, DxfHelper.COLOR_GREEN, 0,"0","144301"));
            }
            fs.addAll(fs_mzdw);
            dxf.write(mapInstance, fs, null,null, DxfHelper.TYPE_BADONG, DxfHelper.LINE_LABEL_OUTSIDE,new LineLabel());

            Point p_n = new Point(cel_3_1.getXMax() - o_split , cel_3_1.getYMax() - o_split);
            dxf.write(p_n, null, "N", o_fontsize, null, false,  DxfHelper.COLOR_CYAN, 0);
            writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split);

            Calendar c = Calendar.getInstance();
            String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
            c.add(Calendar.DATE, -1);
            String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");

            // 单元格4-1  丈量者
            x_=x;
            y_ = y- p_height+2*h;
            Envelope cel_4_1 = new Envelope(x_, y_, x_+w/6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_4_1, null, "丈量者", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格4-2  丈量者
            x_=x_+w/6;
            Envelope cel_4_2 = new Envelope(x_, y_, x_+w/6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_4_2, null, GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR",""), o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格4-3  丈量日期
            x_=x_+w/6;
            Envelope cel_4_3 = new Envelope(x_, y_, x_+w/6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_4_3, null, "丈量日期", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格4-4  丈量日期
            x_=x_+w/6;
            Envelope cel_4_4 = new Envelope(x_, y_, x_+w/6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_4_4, null, drawDate, o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格4-5  单位
            x_=x_+w/6;
            Envelope cel_4_5 = new Envelope(x_, y_, x_+w/6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_4_5, null, "单位", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格4-6  米
            x_=x_+w/6;
            Envelope cel_4_6 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_4_6, null, "米", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            //

            // 单元格5-1  米
            x_=x;
            y_=y_-h;
            Envelope cel_5_1 = new Envelope(x_, y_, x_+w/6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_5_1, null, "检查者", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格5-2  检查者
            x_=x_+w/6;
            Envelope cel_5_2 = new Envelope(x_, y_, x_+w/6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_5_2, null,  GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR",""), o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格5-3  检查者
            x_=x_+w/6;
            Envelope cel_5_3 = new Envelope(x_, y_, x_+w/6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_5_3, null, "检查日期", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格5-4  检查者
            x_=x_+w/6;
            Envelope cel_5_4 = new Envelope(x_, y_, x_+w/6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_5_4, null, auditDate, o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格5-5  检查者
            x_=x_+w/6;
            Envelope cel_5_5 = new Envelope(x_, y_, x_+w/6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_5_5, null, "比例尺", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

            // 单元格5-6  检查者
            x_=x_+w/6;
            Envelope cel_5_6 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_5_6, null, "1:200", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

        } catch (Exception es) {
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
//        dxf.write(new Polygon(ps, p.getSpatialReference()), null);
        dxf.write(new Polygon(ps, p.getSpatialReference()), null, "", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
    }

    public DxfZdct_badong save() throws Exception {
        if (dxf != null) {
            dxf.save();
        }
        return this;
    }
    private double getNiceScale(double scale) {
        double niceScale=0d;
        if (scale<=0d){
            return 1;
        }
        if (scale%1>0.85){
            niceScale=(int) scale+1.25;
        }else if (scale%1>0.75){
            niceScale=(int) scale+1;
        }else if (scale%1>0.5){
            niceScale=(int) scale+0.75;
        }else if (scale%1>0.25){
            niceScale=(int) scale+0.5;
        }else {
            niceScale=(int) scale+0.25;
        }
        return niceScale;
    }

}
