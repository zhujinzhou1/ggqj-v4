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
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.exception.CrashHandler;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.LineLabel;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfHelper;
import com.ovit.app.util.gdal.cad.DxfTemplet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;



public class DxfZdt {

    MapInstance mapInstance;
    SpatialReference spatialReference;

    DxfAdapter dxf;
    private int DxfResultType= DxfHelper.TYPE_HUANGPI;
    private String dxfpath;
    private List<Feature> fs_all;
    private Feature f_zd;
    private  List<Feature> fs_zd;
    private  List<Feature> fs_zj_x;
    private  List<Feature> fs_xzdw;
    private  List<Feature> fs_mzdw;
    private Point o_center;   // o 点
    private Envelope o_extend;// 真实图形的范围
    private double o_split=2d;// 单元间隔
    private double width_tk = 42d;// 图框宽
    private double height_tk = 59.4d;// 图框高
    private double k = 0.15d;// 缓存系数
    private String isClose= DxfHelper.GRAPH_CLOSURE;
    private float fontSize=0.5f;

    private double p_width = width_tk/(1+k);// 页面宽  图形范围
    private double p_height = height_tk/(1+k);// 页面高  图形范围

    private double blc = 200;
    private double h = 1.5d; // 行高
    private float o_fontsize=0.8f;// 字体大小
    private double scale;
    private String o_fontstyle = "宋体";// 字体大小
    private int o_fontcolor = DxfHelper.COLOR_BYLAYER;// 图框颜色
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;
    private List<Feature> fs_z_fsjg;
    private List<Feature> fs_fsjg;
    private List<Feature> fs_h_fsjg;
    private List<Feature> fs_jzd;

    public DxfZdt(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(MapHelper.GetSpatialReference(mapInstance));
    }

    public DxfZdt set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfZdt set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfZdt set(Feature f_zd, List<Feature> fs_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h_fsjg
            , List<Feature> fs_jzd, List<Feature> fs_zj_x, List<Feature> fs_xzdw, List<Feature> fs_mzdw) {
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
        this.fs_xzdw = fs_xzdw;
        this.fs_mzdw = fs_mzdw;
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
        o_extend = MapHelper.geometry_get(buffer.getExtent(), spatialReference); //  图形范围

        o_center = o_extend.getCenter(); // 中心点
        // 比例尺
        double height = o_extend.getHeight();
        double width = o_extend.getWidth();

        double v_h = height / (p_height-4*h);
        double v_w = width/ p_width;

        double niceScale_h= DxfHelper.getNiceScale(v_h);
        double niceScale_w= DxfHelper.getNiceScale(v_w);

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

    public DxfZdt write() throws Exception {
        getExtend(); // 多大范围
        if (dxf == null) {
            dxf = DxfAdapter.getInstance();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            List<String[]> list_z=new ArrayList<>();
            List<Feature> jzd1=new ArrayList<>();
            List<Feature> jzd2=new ArrayList<>();
            List<Double> gap= new ArrayList<>();
            for(int i=0;i<fs_jzd.size();i++){
                Feature f_jzd2;
                Feature f_jzd=fs_jzd.get(i);
                if(i+1==fs_jzd.size()){
                    f_jzd2=fs_jzd.get(0);
                } else{
                    f_jzd2=fs_jzd.get(i+1);
                }
                Geometry geometry = MapHelper.geometry_get(f_jzd.getGeometry(), spatialReference);
                Geometry geometry_1 = MapHelper.geometry_get(f_jzd2.getGeometry(), spatialReference);
                Point point_1 = (Point) geometry;
                Point point_2 = (Point) geometry_1;
                GeodeticDistanceResult d_move = GeometryEngine.distanceGeodetic(point_1, point_2, MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
                double distance=d_move.getDistance();
                if(distance<3d){
                    jzd1.add(f_jzd);
                    jzd2.add(f_jzd2);
                    gap.add(d_move.getDistance());
                }
            }

            for(int j=0;j<jzd1.size();j=j+2) {
                    String[] z = {"", "", "", "", "", ""};
                    z[0] = String.valueOf(getJzdh(jzd1.get(0 + j)));
                    z[1] = String.valueOf(getJzdh(jzd2.get(0 + j)));
                    BigDecimal b=new BigDecimal(gap.get(0 + j));
                    z[2] = b.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
                    if (j + 1 == jzd1.size()) {

                    } else {
                        z[3] = String.valueOf(getJzdh(jzd1.get(1 + j)));
                        z[4] = String.valueOf(getJzdh(jzd2.get(1 + j)));
                        BigDecimal a=new BigDecimal(gap.get(1 + j));
                        z[5] = a.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
                    }
                    list_z.add(z);
            }

            Envelope envelope = p_extend;
            dxf.write(new Envelope(envelope.getCenter(), width_tk, height_tk)); // 图框
            dxf.write(p_extend, DxfHelper.LINETYPE_SOLID_LINE,"",o_fontsize, DxfHelper.FONT_STYLE_HZ,false, DxfHelper.COLOR_BYLAYER, DxfHelper.LINE_WIDTH_3);

            Envelope e =  new Envelope(p_extend.getXMin(), p_extend.getYMax()-3*h, p_extend.getXMax(), p_extend.getYMin(), p_extend.getSpatialReference());; //  图形范围

            Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
            dxf.writeText(p_title, "宗地图", o_fontsize*2, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, o_fontcolor, null, null);

            Point p_unit = new Point(envelope.getXMax() , envelope.getYMax() + o_split*0.5, envelope.getSpatialReference());
            dxf.writeText(p_unit, "单位：m·㎡ ", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, o_fontcolor, null, null);
            double w = p_width; //行宽

            // 左上角
            double x = p_extend.getXMin();
            double y = p_extend.getYMax();

            double x_ = x;
            double y_ = y;

            // 单元格1-1
            Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
//                dxf.write(cel_1_1,"不动产单元号");
            dxf.write(cel_1_1, null, "宗地代码", o_fontsize, null, false, o_fontcolor, 0);
            // 单元格1-2
            x_ = x_ + w * 1 / 5;
            Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 2 / 5, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_2, null, Get(f_zd, "ZDDM", ""), o_fontsize, null, false, o_fontcolor, 0);
            // 单元格1-3
            x_ = x_ + w * 2 / 5;
            Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 1 /5, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_3, null, "土地权利人", o_fontsize, null, false, o_fontcolor, 0);
            // 单元格1-4
            x_ = x_ +w * 1 /5;
            Envelope cel_1_4 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_4, null, FeatureHelper.Get(f_zd,"QLRXM",""), o_fontsize, null, false, o_fontcolor, 0);

            // 单元格2-1
            x_ = x;
            y_ = y_ - h;
            Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_1, null, "所在图幅号", o_fontsize, null, false, o_fontcolor, 0);
            // 单元格2-2
            x_ = x_ + w * 1 / 5;
            Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 2 / 5, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_2, null, FeatureHelper.Get(f_zd,"TFH",""), o_fontsize, null, false, o_fontcolor, 0);

            // 单元格2-3
            x_ = x_ + w * 2 / 5;
            Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_3, null, "宗地面积", o_fontsize, null, false, o_fontcolor, 0);

            // 单元格2-4
            x_ = x_ + w * 1 / 5;
            Envelope cel_2_4 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_4, null, FeatureHelper.Get(f_zd,"ZDMJ",0.00)+"", o_fontsize, null, false, o_fontcolor, 0);

            // 单元格3-1
            // 单元格2-1
            x_ = x;
            y_ = y_ - h;
            Envelope cel_3_1 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_1, null, "土地权属性质", o_fontsize, null, false, o_fontcolor, 0);
            // 单元格2-2
            x_ = x_ + w * 1 / 5;
            Envelope cel_3_2 = new Envelope(x_, y_, x_ + w * 2 / 5, y_ - h, p_extend.getSpatialReference());
            //qllx
            String dicQlType = DicUtil.dic("qllx", FeatureHelper.Get(f_zd, "QLLX", ""));
            if (!TextUtils.isEmpty(dicQlType)&&dicQlType.contains("]")){
                dicQlType= StringUtil.substr(dicQlType,dicQlType.lastIndexOf("]")+1);
            }
            dxf.write(cel_3_2, null,dicQlType, o_fontsize, null, false, o_fontcolor, 0);

            // 单元格2-3
            x_ = x_ + w * 2 / 5;
            Envelope cel_3_3 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_3, null, "登记面积", o_fontsize, null, false, o_fontcolor, 0);

            // 单元格2-4
            x_ = x_ + w * 1 / 5;
            Envelope cel_3_4 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_4, null, FeatureHelper.Get(f_zd,"SYQMJ",""), o_fontsize, null, false, o_fontcolor, 0);

            // 单元格4-1
            x_ = x;
            y_ = y_-h;
            Envelope cel_4_1 = new Envelope(x_, y_, x_ + w , y, p_extend.getSpatialReference());
            dxf.write(cel_4_1);
            List<Feature> fs=new ArrayList<>();
            String zddm= FeatureHelper.Get(f_zd,"ZDDM","");
            Point p_basic = new Point(o_extend.getXMin(), o_extend.getYMax(), spatialReference);
            double minLen=10000d;
            Map<Double,Feature> map_len_jzd= new LinkedHashMap<>();
            if (fs_jzd!=null&&fs_jzd.size()>0){
                for (Feature f_jzd : fs_jzd) {

                    Polyline line = new Polyline(new PointCollection(Arrays.asList(new Point[]{p_basic
                            , MapHelper.geometry_get((Point) f_jzd.getGeometry(), p_basic.getSpatialReference())})));
                    double len = Double.parseDouble(AiUtil.Scale(MapHelper.getLength(line, MapHelper.U_L), 2, 0d));
                    map_len_jzd.put(len,f_jzd);
                    minLen=minLen>len?len:minLen;
                }
                // 界址点重新排序
                List<Feature> fs_jzd_new=new ArrayList<>();
                // j1
                Feature f_j1 = map_len_jzd.get(minLen);// 右上角
                int index = fs_jzd.indexOf(f_j1);

                if (index!=0){
                    for (int i = index; i < fs_jzd.size(); i++) {
                        fs_jzd_new.add(MapHelper.cloneFeature(fs_jzd.get(i)));

                    }
                    for (int i = 0; i <index; i++) {
                        fs_jzd_new.add(MapHelper.cloneFeature(fs_jzd.get(i)));
                    }

                    for (int i = 0 ;i<fs_jzd_new.size();i++) {
                        fs_jzd_new.get(i).getAttributes().put("JZDH","J"+(i+1));
                    }
                }
                if (fs_jzd_new.size()==0){
                    fs_jzd_new=fs_jzd;
                }
                dxf.write(mapInstance, fs_jzd_new);
            }


            for (Feature f_zrz : fs_zrz) {
                float ft = fontSize * DxfHelper.FONT_SIZE_FACTOR;
                if (FeatureHelper.Get(f_zrz,"ZRZH","").contains(zddm)){
                    GeodeticDistanceResult d_move=null;
                    Geometry g = MapHelper.geometry_get(f_zrz.getGeometry(), spatialReference);
                    g = MapHelper.geometry_move(g, d_move);
                    List<Point> points = MapHelper.geometry_getPoints(g);
                    Point point = GeometryEngine.labelPoint((Polygon) g);
                    Point p = new Point(point.getX() + fontSize, point.getY(), point.getSpatialReference());
                    String fwjg = FeatureEdit.dic(mapInstance.activity, "fwjg", AiUtil.GetValue(f_zrz.getAttributes().get("FWJG"), ""));
                    String scjzmj = AiUtil.GetValue(FeatureHelper.Get(f_zrz, "SCJZMJ"), "", AiUtil.F_FLOAT2);
                    int zcs = AiUtil.GetValue(f_zrz.getAttributes().get("ZCS"), 1);
                    dxf.writeLine(DxfTemplet.GetZRZ_Polygon(f_zrz, points));
                    String lable=fwjg.substring(fwjg.indexOf("[")+1,fwjg.indexOf("]")) + String.format("%02d", zcs);
                     dxf.writeText(new Point(p.getX() - 1.5 * ft, p.getY() + 0.1 * ft), lable, 0.5f, DxfHelper.FONT_WIDTH_DEFULT, DxfHelper.FONT_STYLE_SONGTI, 0, 1, 1, DxfHelper.COLOR_BYLAYER, "JZD", "");
                     dxf.writeLine(Arrays.asList(new Point[]{new Point(p.getX() - 3 * ft, p.getY()), new Point(p.getX() - 0.1 * ft, p.getY())}), DxfHelper.LINETYPE_SOLID_LINE, false, DxfHelper.COLOR_CARMINE, 0);
                     dxf.writeText(new Point(p.getX() - 1.5 * ft, p.getY() - 0.1 * ft), scjzmj, 0.5f, DxfHelper.FONT_WIDTH_DEFULT, DxfHelper.FONT_STYLE_SONGTI, 0, 1, 3, DxfHelper.COLOR_BYLAYER, "JZD", "");

                }else {
                    Geometry g = MapHelper.geometry_get(f_zrz.getGeometry(), spatialReference);
                    Geometry intersection = GeometryEngine.intersection(e, g);
                    dxf.write(intersection);
                    String text = mapInstance.getLabel(f_zrz, DxfResultType);
                    Point point = GeometryEngine.labelPoint((Polygon)intersection);
                    dxf.writeMText(point, text, o_fontsize * DxfHelper.FONT_SIZE_FACTOR, "", 0, 1, 2, DxfHelper.COLOR_CARMINE, "JZD", "");
                }
            }
            for (Feature f_fsjg : fs_fsjg) {
                String text = mapInstance.getLabel(f_fsjg);
                if (FeatureHelper.Get(f_fsjg,"ID","").contains(zddm)){
                    if(text.contains("隔热层")){

                    } else {
                        fs.add(f_fsjg);
                    }
                }else {
                    Geometry g = MapHelper.geometry_get(f_fsjg.getGeometry(), spatialReference);
                    Geometry intersection = GeometryEngine.intersection(e, g);
                    dxf.write(intersection);
                }
            }
            for (Feature f_zd : fs_zd) {
                float ft = 0.5f;
                if(FeatureHelper.Get(f_zd,"ZDDM","").contains(zddm)){
                    Geometry g = MapHelper.geometry_get(f_zd.getGeometry(), spatialReference);
                    List<Point> points = MapHelper.geometry_getPoints(g);
                    Point p = GeometryEngine.labelPoint((Polygon) g);
                    dxf.writeLine(DxfTemplet.GetZD_Polygon(f_zd, points));
                    dxf.writeJXLabel(points, ft * DxfHelper.FONT_SIZE_FACTOR, "", ft * DxfHelper.FONT_SIZE_FACTOR*0.8f, DxfHelper.COLOR_RED, null,new LineLabel(), "JZD", "302010", 1);
                    dxf.writeText(new Point(p.getX(), p.getY() + 1 * ft), FeatureHelper.Get(f_zd, "QLRXM", ""), ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 1, 1, "JZD", "302004");
                    dxf.writeText(new Point(p.getX() - 1.5 * ft, p.getY() + 0.1 * ft), FeatureHelper.Get(f_zd, "PRO_ZDDM_F", ""), 0.5f * ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 1, DxfHelper.COLOR_RED, "JZD", "302002");
                    dxf.writeLine(Arrays.asList(new Point[]{new Point(p.getX() - 3 * ft, p.getY()), new Point(p.getX() - 0.1 * ft, p.getY())}), "", false, 1, 0);
                    dxf.writeText(new Point(p.getX() - 1.5 * ft, p.getY() - 0.1 * ft), FeatureHelper.Get(f_zd, "PZYT", ""), 0.5f * ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 3, DxfHelper.COLOR_RED, "JZD", "302003");
                    dxf.writeText(new Point(p.getX(), p.getY()), AiUtil.Scale(FeatureHelper.Get(f_zd, "ZDMJ", 0d), 2) + "", 0.5f * ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 0, 2, DxfHelper.COLOR_RED, "JZD", "302005");

                } else{
                    Geometry g = MapHelper.geometry_get(f_zd.getGeometry(), spatialReference);
                    Geometry g_ = GeometryEngine.intersection(e, g);
                    List<Point> points = MapHelper.geometry_getPoints(g_);
                    Point p = GeometryEngine.labelPoint((Polygon) g_);
                    dxf.writeLine(DxfTemplet.GetZD_Polygon(f_zd, points));
                    dxf.writeText(new Point(p.getX(), p.getY() + 1 * ft), FeatureHelper.Get(f_zd, "QLRXM", ""), ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 1, 1, "JZD", "302004");
                    dxf.writeText(new Point(p.getX() - 1.5 * ft, p.getY() + 0.1 * ft), FeatureHelper.Get(f_zd, "PRO_ZDDM_F", ""), 0.5f * ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 1, 1, "JZD", "302002");
                    dxf.writeLine(Arrays.asList(new Point[]{new Point(p.getX() - 3 * ft, p.getY()), new Point(p.getX() - 0.1 * ft, p.getY())}), "", false, 1, 0);
                    dxf.writeText(new Point(p.getX() - 1.5 * ft, p.getY() - 0.1 * ft), FeatureHelper.Get(f_zd, "PZYT", ""), 0.5f * ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 3, 1, "JZD", "302003");
                    dxf.writeText(new Point(p.getX(), p.getY()), AiUtil.Scale(FeatureHelper.Get(f_zd, "ZDMJ", 0d), 2) + "", 0.5f * ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 0, 2, 1, "JZD", "302005");
                }
            }
            fs.addAll(fs_zj_x);
            for (int i = 0; i < fs_xzdw.size(); i++) {
                Feature feature=fs_xzdw.get(i);
                Geometry g = MapHelper.geometry_get(feature.getGeometry(), spatialReference);
                String name = feature.getFeatureTable().getTableName();

                Geometry intersection = GeometryEngine.intersection(e, g);
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
            dxf.write(mapInstance, fs, null,null,DxfResultType, DxfHelper.LINE_LABEL_OUTSIDE,new LineLabel());
            // 单元格4-2
            Envelope cel_4_2 = new Envelope(x_, y_, x_ + w, y -p_height, p_extend.getSpatialReference());
            dxf.write(cel_4_2, null, "", o_fontsize, null, false, o_fontcolor, 0);
            Point p_n = new Point(cel_4_2.getXMax() - o_split , cel_4_2.getYMax() - o_split);
            dxf.write(p_n, null, "N", o_fontsize, null, false, o_fontcolor, 0);
            writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split);
            // 宗地四至
            DxfHelper.writeZdsz(dxf,f_zd,f_zd.getGeometry().getExtent(),o_split,0.8f,o_fontstyle);
            //短线注记
            x_= envelope.getCenter().getX();
            y_=envelope.getYMin() + o_fontsize*6;
            Envelope cel_0_1=new Envelope(x_,y_,x_+w/12,y_-o_fontsize,p_extend.getSpatialReference());
            dxf.write(cel_0_1,null, "起点号", o_fontsize/2, null, false, o_fontcolor, 0);

            x_=x_+w/12;
            Envelope cel_0_2=new Envelope(x_,y_,x_+w/12,y_-o_fontsize,p_extend.getSpatialReference());
            dxf.write(cel_0_2,null, "终点号", o_fontsize/2, null, false, o_fontcolor, 0);

            x_=x_+w/12;
            Envelope cel_0_3=new Envelope(x_,y_,x_+w/12,y_-o_fontsize,p_extend.getSpatialReference());
            dxf.write(cel_0_3,null, "间距(m)", o_fontsize/2, null, false, o_fontcolor, 0);

            x_=x_+w/12;
            Envelope cel_0_4=new Envelope(x_,y_,x_+w/12,y_-o_fontsize,p_extend.getSpatialReference());
            dxf.write(cel_0_4,null, "起点号", o_fontsize/2, null, false, o_fontcolor, 0);

            x_=x_+w/12;
            Envelope cel_0_5=new Envelope(x_,y_,x_+w/12,y_-o_fontsize,p_extend.getSpatialReference());
            dxf.write(cel_0_5,null, "终点号", o_fontsize/2, null, false, o_fontcolor, 0);

            x_=x_+w/12;
            Envelope cel_0_6=new Envelope(x_,y_,x_+w/12,y_-o_fontsize,p_extend.getSpatialReference());
            dxf.write(cel_0_6,null, "间距(m)", o_fontsize/2, null, false, o_fontcolor, 0);

            Envelope cel_i_j=null;
            //横
            for (int i=0;i<5;i++) {
                x_= envelope.getCenter().getX();
                y_ = y_ - o_fontsize;
                String[] q =null;
                if (i<list_z.size()){
                    q = list_z.get(i);
                }
              //竖
                for (int j = 0; j < 6; j++) {
                    cel_i_j = new Envelope(x_, y_, x_ + w / 12, y_ - o_fontsize, p_extend.getSpatialReference());
                    if (q != null) {
                        try {
                            if (j==0){
                                dxf.write(cel_i_j, null, q[j], o_fontsize/2, null, false, 0, 0);
                            }else {
                                dxf.write(cel_i_j, null, q[j], o_fontsize/2, null, false, 0, 0);
                            }
                        } catch (Exception E) {
                            dxf.write(cel_i_j, null, "", o_fontsize/2, null, false, 0, 0);
                        }
                    } else {
                        dxf.write(cel_i_j, null, "", o_fontsize/2, null, false, 0, 0);
                    }
                    x_ = x_ + w / 12;
                }
            }

            // 单元格4-0  绘制单位
            x_=x;
            y_ = y - p_height;
            String hzdw = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZDW", "");
            Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
            Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
            dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw, "\n"), o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);

            Calendar c = Calendar.getInstance();
            String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
            c.add(Calendar.DATE, -1);
            String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");

            Point p_drawDate =new Point(envelope.getXMin(),envelope.getYMin()-h*0.5f);
            dxf.writeText(p_drawDate,  c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月"+"图解法测绘界址点", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 0, 2, o_fontcolor, null, null);

            Point p_blc = new Point(envelope.getCenter().getX() , envelope.getYMin() -h*0.5f, envelope.getSpatialReference());
            dxf.writeText(p_blc, "1:"+(int)blc, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, o_fontcolor, null, null);

            Point p_chr = new Point(envelope.getXMax(),envelope.getYMin()-h*0.5f);
            String drawMessage="制 图： "+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR","")+" "+drawDate;
            String auditMessage="审 核： "+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR","")+" "+auditDate;
            dxf.writeText(p_chr, drawMessage, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, o_fontcolor, null, null);
            Point p_shr = new Point(envelope.getXMax(),envelope.getYMin()-h*1.5f);
            dxf.writeText(p_shr,auditMessage, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, o_fontcolor, null, null);

        } catch (Exception es) {
            dxf.error("生成失败", es, true);
        }
        return this;
    }

    public DxfZdt save() throws Exception {
        if (dxf != null) {
            dxf.save();
            dxf=null;
        }
        return this;
    }
    private void writeN(Point p, double w) throws Exception {
        PointCollection ps = new PointCollection(p.getSpatialReference());
        ps.add(p);
        ps.add(new Point(p.getX() - w / 6, p.getY() - w / 2));
        ps.add(new Point(p.getX(), p.getY() + w / 2));
        ps.add(new Point(p.getX() + w / 6, p.getY() - w / 2));
        dxf.write(new Polygon(ps, p.getSpatialReference()), null, "", o_fontsize, null, false, o_fontcolor, 0);
    }

        public String getJzdh(Feature feature){
            String jzdh= FeatureHelper.Get(feature, "JZDH", "").toUpperCase();
            if (!TextUtils.isEmpty(jzdh)&&jzdh.length()>2){
                jzdh= StringUtil.substr(jzdh,0,1)+Integer.parseInt(StringUtil.substr_last(jzdh,2));
            }
            return jzdh;
        }
}
