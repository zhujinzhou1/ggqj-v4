package com.ovit.app.map.bdc.ggqj.map.model;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
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
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.ovit.app.util.gdal.cad.DxfHelper.FONT_STYLE_HZ;

public class DxfFwqjxsyt_tongshan {
    MapInstance mapInstance;
    SpatialReference spatialReference;

    DxfAdapter dxf;
    private int DxfResultType=DxfHelper.TYPE_JIAYU;
    private String dxfpath;
    private List<Feature> fs_all;
    private Feature f_zd;
    private Point o_center;   // o 点
    private Envelope o_extend;// 真实图形的范围
    private double o_split;// 单元间隔

    private double width_tk ;// 图框宽
    private double height_tk ;// 图框高
    private double k ;// 缓冲系数
    private double p_width ;// 页面宽  图形范围
    private double p_height ;// 页面高  图形范围

    private double blc ;
    private double h ; // 行高
    private float o_fontsize;// 字体大小
    private float fontsize;// 字体大小
    private double scale;
    private String o_fontstyle = "HZ";// 字体大小
    private int o_fontcolor = DxfHelper.COLOR_BYLAYER;// 图框颜色
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;
    private List<Feature> fs_fsjg;
    private List<Feature> fs_h_fsjg;
    private List<Feature> fs_jzd;

    public DxfFwqjxsyt_tongshan(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfFwqjxsyt_tongshan set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfFwqjxsyt_tongshan set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfFwqjxsyt_tongshan set(Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_jzd, List<Feature> fs_h_fsjg, List<Feature> fs_zj_d) {
        this.f_zd = f_zd;
        List<Feature> fs_all=new ArrayList<>();
        List<Feature> fs_fsjg=new ArrayList<>();
        fs_all.addAll(fs_zrz);
        fs_all.addAll(fs_h_fsjg);
        fs_all.addAll(fs_jzd);
        fs_all.addAll(fs_zj_d);
        this.f_zd = f_zd;
        this.fs_zrz =fs_zrz;
        this.fs_h_fsjg =fs_h_fsjg;
        this.fs_jzd =fs_jzd;
        this.fs_all =fs_all;

        fs_fsjg.addAll(fs_h_fsjg);
        this.fs_fsjg =fs_fsjg;
        return this;
    }

    // 获取范围
    public Envelope getExtend() {
        o_split=2d;// 单元间隔
        width_tk = 42d;// 图框宽
        height_tk = 59.4d;// 图框高
        k = 0.1d;// 缓冲系数
        p_width = width_tk/(1+k);// 页面宽  图形范围
        p_height = height_tk/(1+k);// 页面高  图形范围
        blc = 200;
        h = 1.5d; // 行高
        o_fontsize=0.8f;// 字体大小
        fontsize=0.5f;

        Geometry buffer = GeometryEngine.buffer(f_zd.getGeometry(), 5);
        o_extend = MapHelper.geometry_get(buffer.getExtent(), spatialReference); //  图形范围

        o_center = o_extend.getCenter(); // 中心点
        // 比例尺
        double height = o_extend.getHeight();
        double width = o_extend.getWidth();

        double v_h = height / (p_height-4*h);
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
            fontsize = (float) (fontsize*scale);
        }else {
            scale=1;
        }

        p_extend = new Envelope(o_center,p_width,p_height);

        return p_extend;
    }

    public DxfFwqjxsyt_tongshan write() throws Exception {
        getExtend(); // 多大范围
        if (dxf == null) {
            dxf = DxfAdapter.getInstance();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            Envelope envelope = p_extend;

            Envelope e =  new Envelope(p_extend.getXMin(), p_extend.getYMax(), p_extend.getXMax(), p_extend.getYMin() + 2*h, p_extend.getSpatialReference());; //  图形范围

            dxf.write(p_extend,DxfHelper.LINETYPE_SOLID_LINE,"",o_fontsize, FONT_STYLE_HZ,false,DxfHelper.COLOR_BYLAYER,DxfHelper.LINE_WIDTH_3);

            Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() - o_split * 0.5, envelope.getSpatialReference());
            dxf.writeText(p_title, "房屋权界线示意图", fontsize * 12 / 5, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, o_fontcolor, null, null);

            Envelope  line = new Envelope(envelope.getXMin(), envelope.getYMax(), envelope.getXMax(), envelope.getYMax() - o_split*1.2, p_extend.getSpatialReference());
            dxf.write(line, null, "", o_fontsize, o_fontstyle, false, o_fontcolor, 0);

            double w = p_width; //行宽

            // 左上角
            double x = p_extend.getXMin();
            double y = p_extend.getYMax();

            double x_ = x;
            double y_ = y;

            List<Feature> fs=new ArrayList<>();
            String zddm=FeatureHelper.Get(f_zd,"ZDDM","");
            //  fs.addAll(fs_jzd);
            for(Feature f_jzd:fs_jzd){
                Geometry jzd = MapHelper.geometry_get(f_jzd.getGeometry(), spatialReference);
                Point point = (Point) jzd;
                dxf.writeLine(DxfTemplet.Get_JZD(f_jzd, point));
            }
            fs.add(f_zd);
            //自然幢
            for (Feature f_zrz : fs_zrz) {
                if (FeatureHelper.Get(f_zrz,"ZRZH","").contains(zddm)){
                    Geometry zrz = MapHelper.geometry_get(f_zrz.getGeometry(), spatialReference);
                    List<Point> points = MapHelper.geometry_getPoints(zrz);
                  //  dxf.writeLine(DxfTemplet.GetZRZ_Polygon(f_zrz, points,DxfHelper.COLOR_BYLAYER,"ZRZ"));
                    Point point = GeometryEngine.labelPoint((Polygon) zrz);
                    String text = mapInstance.getLabel(f_zrz, DxfResultType);
                    dxf.writeMText(new Point(point.getX(),point.getY()-fontsize), text, o_fontsize * DxfHelper.FONT_SIZE_FACTOR, "", 0, 1, 2, DxfHelper.COLOR_BYLAYER, "ZRZ", "141161-1");
                }else {
                    Geometry g = MapHelper.geometry_get(f_zrz.getGeometry(), spatialReference);
                    Geometry intersection = GeometryEngine.intersection(e, g);
                    dxf.write(intersection);
                    String text = mapInstance.getLabel(f_zrz, DxfResultType);
                    Point point = GeometryEngine.labelPoint((Polygon)intersection);
                    dxf.writeMText(new Point(point.getX(),point.getY()-fontsize), text, o_fontsize * DxfHelper.FONT_SIZE_FACTOR, "", 0, 1, 2, DxfHelper.COLOR_CARMINE, "ZRZ", "141161-1");
                }
            }
            for (Feature f_fsjg : fs_fsjg) {
                if (FeatureHelper.Get(f_fsjg,"ID","").contains(zddm)){
                    //fs.add(f_fsjg);
                }
            }

            dxf.write(mapInstance, fs, null,null,DxfResultType,DxfHelper.LINE_LABEL_OUTSIDE,new LineLabel());
            // 单元格4-2
            Envelope cel_4_2 = new Envelope(x_, y_, x_ + w, y -p_height, p_extend.getSpatialReference());
            dxf.write(cel_4_2, null, "", o_fontsize, FONT_STYLE_HZ, false, o_fontcolor, 0);
            Point p_n = new Point(cel_4_2.getXMax() - o_split , cel_4_2.getYMax() - o_split*1.5);
            dxf.write(p_n, null, "北", o_fontsize, FONT_STYLE_HZ, false, o_fontcolor, 0);
            writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split);
            // 宗地四至  暂时不要宗地四至
            // DxfHelper.writeZdsz(dxf,f_zd,f_zd.getGeometry().getExtent(),o_split,0.8f,FONT_STYLE_HZ);
            // 单元格4-0  绘制单位
            x_=x;
            y_ = y - p_height+2*h;
            SharedPreferences sp= mapInstance.map.getContext().getSharedPreferences("name",MODE_PRIVATE);
            String auditDate = getRQ(sp.getString("date", ""));
            String drawDate = getRQ(sp.getString("date", ""));

            String drawPerson=GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR","");
            String auditPerson=GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR","");
            if(drawPerson.length()<3){
                drawPerson=getHZR(drawPerson);
            }
            if(auditPerson.length()<3){
                auditPerson=getHZR(auditPerson);
            }
            //画界址点  界址点为黑色的
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
                dxf.writeText(new Point(p.getX(), p.getY()), jzdh, fontSize * 0.7f, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 0, 2, DxfHelper.COLOR_BYLAYER, "JZP", "301001");
            }


            Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_1, null, "丈量者", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
            // 1-2
            x_=x_ + w * 1 / 6;
            Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_2, null, drawPerson, o_fontsize, o_fontstyle, false, o_fontcolor, 0);

            // 1-3
            x_=x_ + w * 1 / 6;
            Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_3, null, "丈量日期", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
            x_=x_ + w * 1 / 8;
            Envelope cel_1_4 = new Envelope(x_, y_, x_ + w * 5 / 24, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_4, null, drawDate, o_fontsize, o_fontstyle, false, o_fontcolor, 0);

            // 1-3
            x_=x_ + w * 5 / 24;
            Envelope cel_1_5 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - 2*h, p_extend.getSpatialReference());
            dxf.write(cel_1_5, null, "概略比例尺", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
            x_=x_ + w * 1 / 6;
            Envelope cel_1_6 = new Envelope(x_, y_, x + w , y_ -  2*h, p_extend.getSpatialReference());
            dxf.write(cel_1_6, null, "1:"+(int)blc, o_fontsize, o_fontstyle, false, o_fontcolor, 0);

            x_=x;
            y_=y_-h;

            Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
//                dxf.write(cel_1_1,"不动产单元号");
            dxf.write(cel_2_1, null, "检查者", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
            // 1-2
            x_=x_ + w * 1 / 6;
            Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_2, null, auditPerson, o_fontsize, o_fontstyle, false, o_fontcolor, 0);

            // 1-3
            x_=x_ + w * 1 / 6;
            Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_3, null, "检查日期", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
            x_=x_ + w * 1 / 8;
            Envelope cel_2_4 = new Envelope(x_, y_, x_ + w * 5 / 24, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_4, null, auditDate, o_fontsize, o_fontstyle, false, o_fontcolor, 0);
            //不添加说明
            /*
            Point point=new Point(x+ w * 21 / 24,y-p_height+2.5*h);
            dxf.write(point,null, "说明：图中单位为米。", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
            */
        } catch (Exception es) {
            dxf.error("生成失败", es, true);
        }
        return this;
    }

    public DxfFwqjxsyt_tongshan save() throws Exception {
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
//        dxf.write(new Polygon(ps, p.getSpatialReference()), null);
        dxf.write(new Polygon(ps, p.getSpatialReference()), null, "", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
    }
    private String getRQ(String s){
        String year= StringUtil.substr(s,0,4);
        String month=StringUtil.substr(s,5,7);
        String day=StringUtil.substr(s,8,10);
        String rq= year + "年" + month + "月" + day + "日";
        return rq;
    }
    public String getHZR(String s){
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0) {
                result.append("  ");
            }
            result.append(s.charAt(i));
        }
        return result.toString();
    }
}
