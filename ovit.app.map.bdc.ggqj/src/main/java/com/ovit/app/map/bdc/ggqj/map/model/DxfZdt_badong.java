package com.ovit.app.map.bdc.ggqj.map.model;

import android.text.TextUtils;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.LineLabel;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.AppConfig;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfHelper;
import com.ovit.app.util.gdal.cad.DxfTemplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * Created by XW on 2018/9/29. 巴东宗地草图
 */

public class DxfZdt_badong {

    MapInstance mapInstance;
    SpatialReference spatialReference;

    DxfAdapter dxf;
    private String dxfpath;
    private List<Feature> fs_all;
    private List<Feature> fs_hAndFs;
    private Feature f_zd;
    private  List<Feature> fs_zd;
    private  List<Feature> fs_zj_x;
    private  List<Feature> fs_zj_d;
    private  List<Feature> fs_xzdw;
    private  List<Feature> fs_mzdw;
    private  List<Feature> fs_dzdw;
    private Point o_center;   // o 点
    private Envelope o_extend;// 真实图形的范围
    private double o_split=2d;// 单元间隔
    private double width_tk = 42d;// 图框宽
    private double height_tk = 59.4d;// 图框高
    private double k = 0.15d;// 缓存系数
    private String isClose=DxfHelper.GRAPH_CLOSURE;
    private double p_width = width_tk/(1+k);// 页面宽  图形范围
    private double p_height = height_tk/(1+k);// 页面高  图形范围
    private double h = 1.5d; // 行高
    private double blc = 200d;
    private float o_fontsize=0.8f;// 字体大小
    private double scale;
    private String o_fontstyle = DxfHelper.FONT_STYLE_HZ;// 字体样式
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;
    private List<Feature> fs_z_fsjg;
    private List<Feature> fs_fsjg;
    private List<Feature> fs_h_fsjg;
    private List<Feature> fs_jzd;
    private List<Feature> fs_fsss;

    public DxfZdt_badong(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfZdt_badong set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfZdt_badong set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfZdt_badong set(Feature f_zd, List<Feature> fs_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h_fsjg
            , List<Feature> fs_jzd, List<Feature> fs_zj_x, List<Feature> fs_zj_d, List<Feature> fs_xzdw
            , List<Feature> fs_mzdw,List<Feature> fs_dzdw,List<Feature> fs_fsss) {
        this.f_zd = f_zd;
        List<Feature> fs_all=new ArrayList<>();
        List<Feature> fs_fsjg=new ArrayList<>();
        fs_all.addAll(fs_zd);
        fs_all.addAll(fs_zrz);
        fs_all.addAll(fs_z_fsjg);
        fs_all.addAll(fs_h_fsjg);
        fs_all.addAll(fs_jzd);
        this.f_zd = f_zd;
        this.fs_zd = fs_zd;
        this.fs_zj_x = fs_zj_x;
        this.fs_zj_d = fs_zj_d;
        this.fs_xzdw = fs_xzdw;
        this.fs_mzdw = fs_mzdw;
        this.fs_dzdw = fs_dzdw;
        this.fs_fsss = fs_fsss;
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
        Geometry buffer = GeometryEngine.buffer(f_zd.getGeometry(), 5);
        o_extend = MapHelper.geometry_get(buffer.getExtent(), spatialReference);
        o_center = o_extend.getCenter(); // 中心点
        // 比例尺
        double height = o_extend.getHeight();
        double width = o_extend.getWidth();
        double v_h = height / (p_height-3*h);
        double v_w = width/ p_width;

        double niceScale_h= DxfHelper.getNiceScale(v_h);
        double niceScale_w=DxfHelper.getNiceScale(v_w);

        scale=niceScale_w>niceScale_h?niceScale_w:niceScale_h;

        if (scale>1){
            p_width=p_width*scale;
            p_height=p_height*scale;
            width_tk=width_tk*scale;
            height_tk=height_tk*scale;
            h=h*scale;
            blc=blc*scale;
            o_split=o_split*scale;
            o_fontsize = (float) (o_fontsize*scale);
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

    public DxfZdt_badong write() throws Exception {
        getExtend(); // 多大范围
        if (dxf == null) {
            dxf = DxfAdapter.getInstance();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            Envelope envelope = p_extend;
            dxf.writetk(new Envelope(envelope.getCenter(), width_tk, height_tk),DxfHelper.LINETYPE_SOLID_LINE,"",o_fontsize,DxfHelper.FONT_STYLE_HZ,false,DxfHelper.COLOR_BYLAYER,DxfHelper.LINE_WIDTH_DEFULT); // 图框

            Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
            dxf.writeText(p_title, "宗地图", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, "TK", null);

            Point p_unit = new Point(envelope.getXMax() , envelope.getYMax() + o_split*0.5, envelope.getSpatialReference());
            dxf.writeText(p_unit, "单位：m·㎡ ", o_fontsize*0.8f, DxfHelper.FONT_WIDTH_DEFULT, DxfHelper.FONT_STYLE_SONGTI, 0, 2, 2, DxfHelper.COLOR_BYLAYER, "TK", null);
            double w = p_width; //行宽

            // 左上角
            double x = p_extend.getXMin();
            double y = p_extend.getYMax();

            double x_ = x;
            double y_ = y;

            // 单元格1-1
            Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 3 / 15, y_ - h, p_extend.getSpatialReference());
//                dxf.write(cel_1_1,"不动产单元号");
//            dxf.write(cel_1_1, null, "宗地代码", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
            dxf.writeText(cel_1_1.getCenter(),"宗地代码", o_fontsize,0, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, "JZD", null);
            // 单元格1-2
            x_ = x_ + w * 3 / 15;
            Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 1 / 15, y_ - h, p_extend.getSpatialReference());
//            dxf.write(cel_1_2, null, Get(f_zd, "ZDDM", ""), o_fontsize*0.6f, null, false, DxfHelper.COLOR_BYLAYER, 0);
            dxf.writeText(cel_1_2.getCenter(),Get(f_zd, "ZDDM", ""), o_fontsize,0, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, "JZD", null);

            // 单元格1-3
            x_ = x_ + w * 6 / 15;
            Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 2 /15, y_ - h, p_extend.getSpatialReference());
//            dxf.write(cel_1_3, null, "土地权利人", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
            dxf.writeText(cel_1_3.getCenter(),"土地权利人", o_fontsize,0, null, 0, 1, 2, DxfHelper.COLOR_BYLAYER, "JZD", null);

            // 单元格1-4
            x_ = x_ +w * 2 /15;
            Envelope cel_1_4 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
//            dxf.write(cel_1_4, null, FeatureHelper.Get(f_zd,"QLRXM",""), o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
            dxf.writeText(cel_1_4.getCenter(),FeatureHelper.Get(f_zd,"QLRXM",""), o_fontsize,0, null, 0, 1, 2, DxfHelper.COLOR_BYLAYER, "JZD", null);

            // 单元格2-1
            x_ = x;
            y_ = y_ - h;
            Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 3 / 15, y_ - h, p_extend.getSpatialReference());
//            dxf.write(cel_2_1, null, "所在图幅号", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
            dxf.writeText(cel_2_1.getCenter(),"所在图幅号", o_fontsize,0, null, 0, 1, 2, DxfHelper.COLOR_BYLAYER, "JZD", null);

            // 单元格2-2
            x_ = x_ + w * 3 / 15;
            Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 1 / 15, y_ - h, p_extend.getSpatialReference());
//            dxf.write(cel_2_2, null,FeatureHelper.Get(f_zd,"TFH",""), o_fontsize*0.6f, null, false, DxfHelper.COLOR_BYLAYER, 0);
            dxf.writeText(cel_2_2.getCenter(),FeatureHelper.Get(f_zd,"TFH",""), o_fontsize,0, null, 0, 1, 2, DxfHelper.COLOR_BYLAYER, "JZD", null);

            // 单元格2-3
            x_ = x_ + w * 6 / 15;
            Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
//            dxf.write(cel_2_3, null, "宗地面积", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
            dxf.writeText(cel_2_3.getCenter(),"宗地面积", o_fontsize,0, null, 0, 1, 2, DxfHelper.COLOR_BYLAYER, "JZD", null);

            // 单元格2-4
            x_ = x_ + w * 2 / 15;
            Envelope cel_2_4 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
//            dxf.write(cel_2_4, null, FeatureHelper.Get(f_zd,"ZDMJ",0.00)+"", o_fontsize*0.6f, null, false, DxfHelper.COLOR_BYLAYER, 0);
            dxf.writeText(cel_2_4.getCenter(),FeatureHelper.Get(f_zd,"ZDMJ",0.00)+"", o_fontsize,0, null, 0, 1, 2, DxfHelper.COLOR_BYLAYER, "JZD", null);

            // 单元格4-1
            x_ = x;
            y_ = y_-h;
            Envelope cel_4_1 = new Envelope(x_, y_, x_ + w , y, p_extend.getSpatialReference());
            dxf.write(cel_4_1);
            Envelope cel_4_2 = new Envelope(x_, y_, x_ + w, y -p_height, p_extend.getSpatialReference());
            List<Feature> fs=new ArrayList<>();
            String zddm=FeatureHelper.Get(f_zd,"ZDDM","");
            //  fs.addAll(fs_jzd);
            Geometry geometry = MapHelper.geometry_get(f_zd.getGeometry(), spatialReference);
            for(Feature f_jzd : fs_jzd){
                float fontSize = 0.5f;
                com.esri.arcgisruntime.geometry.Geometry g = MapHelper.geometry_get(f_jzd.getGeometry(), spatialReference);
                Point point = (Point) g;
                dxf.writeLine(DxfTemplet.Get_JZD(f_jzd, point));
                String jzdh = FeatureHelper.Get(f_jzd, "JZDH", "").toUpperCase();
                if (AiUtil.GetValue(AppConfig.get("APP_ZD_JZD_FSSYJM"), true) && !TextUtils.isEmpty(jzdh) && jzdh.length() > 2) {
                    jzdh = StringUtil.substr(jzdh, 0, 1) + Integer.parseInt(StringUtil.substr_last(jzdh, 2));
                }
                Envelope envelope1 = new Envelope(point, 2, 1.7);
                Geometry difference = GeometryEngine.difference(envelope1, geometry);
                Point p = GeometryEngine.labelPoint((Polygon) difference);
                dxf.writeText(new Point(p.getX(), p.getY()), jzdh, fontSize * 0.7f, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 0, 2, DxfHelper.COLOR_RED, "JZP", "301001");
            }
            //      fs.add(f_zd);
            for (Feature f_zrz : fs_zrz) {
                if (FeatureHelper.Get(f_zrz,"ZRZH","").contains(zddm)){
                    fs.add(f_zrz);
                }else {
                    com.esri.arcgisruntime.geometry.Geometry g = MapHelper.geometry_get(f_zrz.getGeometry(), spatialReference);
                    Geometry intersection = GeometryEngine.intersection(cel_4_2, g);
                    dxf.write(intersection,DxfHelper.LINETYPE_SOLID_LINE,"",o_fontsize,DxfHelper.FONT_STYLE_HZ,false,DxfHelper.COLOR_PINK,DxfHelper.LINE_WIDTH_DEFULT);
                    String text = mapInstance.getLabel(f_zrz,DxfHelper.TYPE_BADONG);
                    Point point = GeometryEngine.labelPoint((Polygon) intersection);
                    String[] split = text.trim().split("-", 2);
                    if (split[0].contains("混")) {
                        dxf.writeMText(new Point(point.getX() , point.getY()), split[0], o_fontsize * DxfHelper.FONT_SIZE_FACTOR, "", 0, 1, 2, DxfHelper.COLOR_CARMINE, "JMD", "141161-1");
                        dxf.writeMText(new Point(point.getX() + o_fontsize * DxfHelper.FONT_SIZE_FACTOR, point.getY()), split[1], o_fontsize * DxfHelper.FONT_SIZE_FACTOR, "", 0, 1, 2, DxfHelper.COLOR_CARMINE, "JMD", "141161-2");
                    } else if (split[0].contains("石") || split[0].contains("土")) {
                        dxf.writeMText(new Point(point.getX() , point.getY()), split[0], o_fontsize * DxfHelper.FONT_SIZE_FACTOR, "", 0, 1, 2, DxfHelper.COLOR_CARMINE, "JMD", "141101-1");
                        dxf.writeMText(new Point(point.getX() + o_fontsize * DxfHelper.FONT_SIZE_FACTOR, point.getY()), split[1], o_fontsize * DxfHelper.FONT_SIZE_FACTOR, "", 0, 1, 2, DxfHelper.COLOR_CARMINE, "JMD", "141101-2");
                    } else if (split[0].contains("砖")) {
                        dxf.writeMText(new Point(point.getX() , point.getY()), split[0], o_fontsize * DxfHelper.FONT_SIZE_FACTOR, "", 0, 1, 2, DxfHelper.COLOR_CARMINE, "JMD", "141121-1");
                        dxf.writeMText(new Point(point.getX() + o_fontsize * DxfHelper.FONT_SIZE_FACTOR, point.getY()), split[1], o_fontsize * DxfHelper.FONT_SIZE_FACTOR, "", 0, 1, 2, DxfHelper.COLOR_CARMINE, "JMD", "141121-2");
                    } else if (split[0].contains("木")) {
                        dxf.writeMText(new Point(point.getX() , point.getY()), split[0], o_fontsize * DxfHelper.FONT_SIZE_FACTOR, "", 0, 1, 2, DxfHelper.COLOR_CARMINE, "JMD", "141151-1");
                        dxf.writeMText(new Point(point.getX() + o_fontsize * DxfHelper.FONT_SIZE_FACTOR, point.getY()), split[1], o_fontsize * DxfHelper.FONT_SIZE_FACTOR, "", 0, 1, 2, DxfHelper.COLOR_CARMINE, "JMD", "141151-2");
                    }
                }
            }
            for (Feature f_fsjg : fs_fsjg) {
                if (FeatureHelper.Get(f_fsjg,"ID","").contains(zddm)){
                    fs.add(f_fsjg);
                }
            }
            for (Feature f_zd : fs_zd) {
                float ft = 0.5f;
                com.esri.arcgisruntime.geometry.Geometry g = MapHelper.geometry_get(f_zd.getGeometry(), spatialReference);
                if(FeatureHelper.Get(f_zd,"ZDDM","").contains(zddm)) {
                    List<Point> points = MapHelper.geometry_getPoints(g);
                    Point p = GeometryEngine.labelPoint((Polygon) g);
                    dxf.writeLine(DxfTemplet.GetZD_Polygon(f_zd, points));
                    dxf.writeLineLabel(points, 0.36f, "", 0.4f, DxfHelper.COLOR_RED, null, new LineLabel(), "JZD", "302010", 1);
                    dxf.writeText(new Point(p.getX() - 6 * ft, p.getY() - 0.4 * ft), FeatureHelper.Get(f_zd, "QLRXM", ""), 0.6f, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 1, 1, "JZD", "302004");
                    dxf.writeText(new Point(p.getX() - 1.5 * ft, p.getY() + 0.1 * ft), FeatureHelper.Get(f_zd, "PRO_ZDDM_F", ""), 0.5f, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 1, DxfHelper.COLOR_RED, "JZD", "302002");
                    dxf.writeLine(Arrays.asList(new Point[]{new Point(p.getX() - 4 * ft, p.getY()), new Point(p.getX() + 1.1 * ft, p.getY())}), "", false, 1, 0, "JZD", "302001");
                    dxf.writeText(new Point(p.getX() - 1.5 * ft, p.getY() - 0.1 * ft), FeatureHelper.Get(f_zd, "PZYT", ""), 0.5f , DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 3, DxfHelper.COLOR_RED, "JZD", "302003");
                    dxf.writeText(new Point(p.getX()+0.5f, p.getY()), AiUtil.Scale(FeatureHelper.Get(f_zd, "ZDMJ", 0d), 2) + "", 0.5f , DxfHelper.FONT_WIDTH_DEFULT, "", 0, 0, 2, DxfHelper.COLOR_RED, "JZD", "302005");

                } else{
                    Geometry intersection = GeometryEngine.intersection(cel_4_2, g);
                    Point ps = GeometryEngine.labelPoint((Polygon) intersection);
                    dxf.writeText(new Point(ps.getX()-6*ft, ps.getY() -0.4 * ft), FeatureHelper.Get(f_zd, "QLRXM", ""), 0.6f, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 1, 1, "JZD", "302004");
                    dxf.writeText(new Point(ps.getX() - 1.5 * ft, ps.getY() + 0.1 * ft), FeatureHelper.Get(f_zd, "PRO_ZDDM_F", ""),  ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 1, 1, "JZD", "302002");
                    dxf.writeLine(Arrays.asList(new Point[]{new Point(ps.getX() - 4 * ft, ps.getY()), new Point(ps.getX() + 1.1 * ft, ps.getY())}), "", false, 1, 0);
                    dxf.writeText(new Point(ps.getX() - 1.5 * ft, ps.getY() - 0.1 * ft), FeatureHelper.Get(f_zd, "PZYT", ""),  ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 3, 1, "JZD", "302003");
                    dxf.writeText(new Point(ps.getX()+0.5f, ps.getY()), AiUtil.Scale(FeatureHelper.Get(f_zd, "ZDMJ", 0d), 2) + "",  ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 0, 2, 1, "JZD", "302005");
                }
            }
            fs.addAll(fs_zj_x);
            for (int i = 0; i < fs_xzdw.size(); i++) {
                Feature feature=fs_xzdw.get(i);
                com.esri.arcgisruntime.geometry.Geometry g = MapHelper.geometry_get(feature.getGeometry(), spatialReference);
                String name = feature.getFeatureTable().getTableName();

                Geometry intersection = GeometryEngine.intersection(cel_4_2, g);
                List<Point> points = MapHelper.geometry_getPoints(intersection);
                String text = mapInstance.getLabel(feature);

                if (points.size()<2){
                    continue;
                }
                int index=points.size()/2;
                Point p_s=points.get(index-1);
                Point p_e=points.get(index);

                float offsety=o_fontsize*DxfHelper.FONT_LABEL_OFFSET;
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
                                , DxfHelper.FONT_WIDTH_DEFULT, o_fontsize*DxfHelper.FONT_SIZE_FACTOR*0.6f, "", angle, 1, 2, DxfHelper.COLOR_GREEN, "", ""));

                    }
                }

                String stbm=FeatureHelper.Get(feature,"FHDM", "216100");
                dxf.writeLine(DxfTemplet.Get_POLYLINE(points, DxfHelper.LINETYPE_SOLID_LINE, DxfHelper.COLOR_GREEN, 0, "0", stbm));
                //地类界
//                if (stbm.equals(DxfHelper.STBM_DLJ) && points.size() > 0) {
//                    dxf.writeLine(DxfTemplet.Get_POLYLINE(points, DxfHelper.LINETYPE_DLJ_LINE, DxfHelper.COLOR_GREEN, 0,"0" ,stbm, "ZBTZ"));
//                    //加固陡坎
//                } else if (stbm.equals(DxfHelper.STBM_JGDK) && points.size() > 0) {
//                    dxf.writeLine(DxfTemplet.Get_POLYLINE(points, DxfHelper.LINETYPE_JGDK_LINE, DxfHelper.COLOR_GREEN, 0, "0", stbm, "DMTZ"));
//                    //未加固陡坎
//                } else if (stbm.equals(DxfHelper.STBM_WJGDK) && points.size() > 0) {
//                    dxf.writeLine(DxfTemplet.Get_POLYLINE(points, DxfHelper.LINETYPE_WATER_LINE, DxfHelper.COLOR_GREEN, 0, "0", stbm, "DMTZ"));
//                    //依比例乡村路虚线
//                } else if(stbm.equals(DxfHelper.STBM_YBLXX) && points.size() > 0){
//                    dxf.writeLine(DxfTemplet.Get_POLYLINE(points, DxfHelper.LINETYPE_RDOTTED_LINE, DxfHelper.COLOR_T, 0, "0", stbm, "DLSS"));
//                    //依比例乡村路实线
//                } else if(stbm.equals(DxfHelper.STBM_YBLSX) && points.size() > 0){
//                    dxf.writeLine(DxfTemplet.Get_POLYLINE(points, DxfHelper.LINETYPE_SOLID_LINE, DxfHelper.COLOR_T, 0, "0", stbm, "DLSS"));
//                } else {
//                    dxf.writeLine(DxfTemplet.Get_POLYLINE(points, DxfHelper.LINETYPE_SOLID_LINE, DxfHelper.COLOR_GREEN, 0, "0", stbm, "JMD"));
//                }
            }
            for (Feature f_mzdw : fs_mzdw) {
                com.esri.arcgisruntime.geometry.Geometry g = MapHelper.geometry_get(f_mzdw.getGeometry(), spatialReference);
                String text = mapInstance.getLabel(f_mzdw);
                Point p = GeometryEngine.labelPoint((Polygon) g);
                Geometry intersection = GeometryEngine.intersection(cel_4_2, g);
                List<Point> points = MapHelper.geometry_getPoints(intersection);
                //     String stbm=FeatureHelper.Get(f_mzdw,"FHDM", "158800");
                String stbm=FeatureHelper.Get(f_mzdw,"STBM",FeatureHelper.Get(f_mzdw,"FHDM",FeatureHelper.Get(f_mzdw,"XX","")));
                dxf.writeLine(DxfTemplet.Get_POLYGON(points, DxfHelper.LINETYPE_SOLID_LINE, DxfHelper.COLOR_BULEGREEN, 0, stbm));
                dxf.writeMText(p, text, 0.4f, "", 0, 1, 2, DxfHelper.COLOR_BLUE, "JMD", "140009");
//                dxf.write(intersection);
            }
            for (Feature f_fsss : fs_fsss) {
                Geometry g_fsss = f_fsss.getGeometry();
                if (FeatureHelper.isPolygonGeometryValid(g_fsss)) {
                    com.esri.arcgisruntime.geometry.Geometry g = MapHelper.geometry_get(g_fsss, spatialReference);
                    String text = mapInstance.getLabel(f_fsss);
                    Point p = GeometryEngine.labelPoint((Polygon) g);
                    Geometry intersection = GeometryEngine.intersection(cel_4_2, g);
                    if (FeatureHelper.isPolygonGeometryValid(intersection)) {
                        List<Point> points = MapHelper.geometry_getPoints(intersection);
                        //     String stbm=FeatureHelper.Get(f_mzdw,"FHDM", "158800");
                        String stbm = FeatureHelper.Get(f_fsss, "STBM", FeatureHelper.Get(f_fsss, "FHDM", FeatureHelper.Get(f_fsss, "XX", "")));
                        dxf.writeLine(DxfTemplet.Get_POLYGON(points, DxfHelper.LINETYPE_SOLID_LINE, DxfHelper.COLOR_BULEGREEN, 0, stbm,"QM"));
                        dxf.writeMText(p, text, 0.4f, "", 0, 1, 2, DxfHelper.COLOR_BLUE, "QM", "140009");
                    }
                }
            }
            fs.addAll(fs_dzdw);
            fs.addAll(fs_zj_d);
            dxf.write(mapInstance, fs, null,null,DxfHelper.TYPE_BADONG,DxfHelper.LINE_LABEL_OUTSIDE,new LineLabel());
            // 单元格4-2
            dxf.write(cel_4_2, null, "", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
            Point p_n = new Point(cel_4_2.getXMax() - o_split , cel_4_2.getYMax() - o_split);
            dxf.write(p_n, null, "北", o_fontsize*1.5f, null, false, DxfHelper.COLOR_BYLAYER, 0);
//            DxfHelper.writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split,dxf);
            writeN(new Point(p_n.getX(), p_n.getY() - 1.5*o_split, p_n.getSpatialReference()), o_split);
            // 宗地四至
            DxfHelper.writeZdsz(dxf,f_zd,f_zd.getGeometry().getExtent(),o_split,o_fontsize,o_fontstyle);
            // 单元格4-0  绘制单位
            x_=x;
            y_ = y - p_height;
            String hzdw =  GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZDW","");
            Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
            Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
            dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw,"\n"), o_fontsize, o_fontstyle, 0, 0, 3, DxfHelper.COLOR_BYLAYER, "TK", null);

            Calendar c = Calendar.getInstance();
            String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
            c.add(Calendar.DATE, -1);
            String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
            String jxf = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月"+"解析法测绘界址点" ;

            Point ms = new Point(x, envelope.getYMin() -h*0.7, envelope.getSpatialReference());
            Point p_auditDate = new Point(x , envelope.getYMin() -h*1.5, envelope.getSpatialReference());
            Point p_drawDate = new Point(x , envelope.getYMin() -h*2.5, envelope.getSpatialReference());
            dxf.writeText(ms, jxf, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_BYLAYER, "TK", null);
            dxf.writeText(p_drawDate, "绘图日期:"+drawDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_BYLAYER, "TK", null);
            dxf.writeText(p_auditDate,"审核日期:"+auditDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_BYLAYER, "TK", null);

            Point p_blc = new Point(envelope.getCenter().getX() , envelope.getYMin() - h*0.5, envelope.getSpatialReference());
            dxf.writeText(p_blc, "1:"+(int)blc, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, "TK", null);

            Point p_z = new Point(x+w, envelope.getYMin() + h*0.5, envelope.getSpatialReference());
            dxf.writeText(p_z, "注：实际建筑占地面积为"+Get(f_zd,"JZZDMJ",0.00)+"平方米" , o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, "TK", null);


            Point p_chr = new Point(x+w, envelope.getYMin() - h*0.5, envelope.getSpatialReference());
            dxf.writeText(p_chr, "制图人："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, "TK", null);


            Point p_shr = new Point(x+w, envelope.getYMin() - 1.5*h, envelope.getSpatialReference());
            dxf.writeText(p_shr, "审核员："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, "TK", null);

        } catch (Exception es) {
            dxf.error("生成失败", es, true);
        }
        return this;
    }

    public DxfZdt_badong save() throws Exception {
        if (dxf != null) {
            dxf.save();
            dxf=null;
        }
        return this;
    }

    private void writeN(Point p, double w) throws Exception {
        PointCollection ps = new PointCollection(p.getSpatialReference());
        ps.add(p);
        ps.add(new Point(p.getX() - w / 3, p.getY() - w ));
        ps.add(new Point(p.getX(), p.getY() + w ));
        ps.add(new Point(p.getX() + w / 3, p.getY() - w ));
//        dxf.write(new Polygon(ps, p.getSpatialReference()), null);
        dxf.write(new Polygon(ps, p.getSpatialReference()), null, "", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
    }

}
