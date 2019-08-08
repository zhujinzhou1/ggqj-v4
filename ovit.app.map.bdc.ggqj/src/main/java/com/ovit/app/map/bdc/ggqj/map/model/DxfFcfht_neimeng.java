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
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZD;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZRZ;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.LineLabel;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfHelper;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * Created by XW on 2018/9/29. 房产图分户图 内蒙
 */

public class DxfFcfht_neimeng {

    MapInstance mapInstance;
    SpatialReference spatialReference;

    DxfAdapter dxf;
    private String dxfpath;
    private String bdcdyh;
    private List<Feature> fs_all;
    private List<Feature> fs_hAndFs;
    private Feature f_zd;
    private Feature f_h;
    private Point o_center;   // o 点
    private Envelope o_extend;// 真实图形的范围
    private  double o_split=2d;// 单元间隔
    private  double p_width = 56d;// 页面宽
    private  double p_height = 36d;// 页面高
    private  double h = 1.26d; // 行高
    private  double scale = 1.26d; // 行高
    private  double blc = 200d; // 行高
    private float o_fontsize=0.6f;// 字体大小
    private String o_fontstyle = "宋体";// 字体大小
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;

    public DxfFcfht_neimeng(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfFcfht_neimeng set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfFcfht_neimeng set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfFcfht_neimeng set(Feature f_bdc,Feature f_h,Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_c_all) {
        this.bdcdyh = FeatureHelper.Get(f_bdc,FeatureHelper.TABLE_ATTR_BDCDYH,"");
        this.f_zd = f_zd;
        this.f_h = f_h;
        this.fs_zrz=fs_zrz;
        fs_all = new ArrayList<>();
        fs_all.addAll(fs_c_all);
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
        double v_w = width *(1+0.1)/ p_width;

        double niceScale_h = DxfHelper.getNiceScale(v_h);
        double niceScale_w = DxfHelper.getNiceScale(v_w);

        scale=niceScale_w>niceScale_h?niceScale_w:niceScale_h;

        if (scale>1){
            p_width=p_width*scale;
            p_height=p_height*scale;
            h=h*scale;
            o_split=o_split*scale;
            o_fontsize = (float) (o_fontsize*scale);
            blc=scale*blc;
        }
        p_extend = new Envelope(o_center,p_width,p_height);
        return p_extend;
    }

    public DxfFcfht_neimeng write() throws Exception {
        getExtend(); // 多大范围
        if (dxf == null) {
            dxf = new DxfAdapter();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            {
                Envelope envelope = p_extend;
                DxfHelper.writeDaYingKuang(dxf,envelope,o_split,spatialReference);
                Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() +o_fontsize*2, envelope.getSpatialReference());
                dxf.writeText(p_title, "房产分户图", o_fontsize*2, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_CYAN, null, null);
                double w = p_width; //行宽
                // 左上角
                double x = envelope.getXMin();
                double y = envelope.getYMax();

                double x_ = x;
                double y_ = y;

                // 单元格1-1
                Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_1, null, "丘 号", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格1-2
                x_ = x_ + w * 2 / 15;
                Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_2, null,"", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格1-3
                x_ = x_ + w * 4 / 15;
                Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 2 /15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_3, null, "结 构", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格1-4
                x_ = x_ +w * 2 /15;
                Envelope cel_1_4 = new Envelope(x_, y_, x_ + w * 2 /15, y_ - h, p_extend.getSpatialReference());
                String fwjg=DxfHelper.getFwjg( DicUtil.dic("fwjg",FeatureHelper.Get(fs_zrz.get(0),"FWJG","4")));
                dxf.write(cel_1_4, null,fwjg, o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格1-5
                x_ =x_ + w * 2 /15;
                Envelope cel_1_5 = new Envelope(x_, y_, x_ + w * 1 /6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_5, null, "套内面积，㎡", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格1-6
                x_ = x_ + w * 1 /6;
                Envelope cel_1_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                double tnmj=FeatureHelper.Get(f_h,"SCJZMJ",0.00);
                dxf.write(cel_1_6, null, String.format("%.2f",tnmj), o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);

                // 单元格2-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_1, null, "幢 号", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格2-2
                x_ = x_ + w * 2 / 15;
                Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_2, null,FeatureHelper.Get(fs_zrz.get(0),"ZH","0001"), o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);

                // 单元格2-3
                x_ = x_ + w * 4 / 15;
                Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_3, null, "层 数", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);

                // 单元格2-4
                x_ = x_ + w * 2 / 15;
                Envelope cel_2_4 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_4, null, FeatureHelper.Get(fs_zrz.get(0),"ZCS",1)+"", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格2-5
                x_ = x_ + w * 2 / 15;
                Envelope cel_2_5 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_5, null, "分摊面积，㎡", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);

                // 单元格2-6
                x_ = x_ + w * 1 / 6;
                Envelope cel_2_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                double ftmj=tnmj*FeatureHelper.Get(f_zd, FeatureViewZD.TABLE_ATTR_FTXS_ZD,0.00);
                dxf.write(cel_2_6, null, String.format("%.2f",ftmj), o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);

                // 单元格3-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_3_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_1, null, "户 号", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格3-2
                x_ = x_ + w * 2 / 15;
                Envelope cel_3_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_2, null,FeatureHelper.Get(f_h,"MPH",""), o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格3-3
                x_ = x_ + w * 4 / 15;
                Envelope cel_3_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_3, null, "层 次", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);

                // 单元格3-4
                x_ = x_ + w * 2 / 15;
                Envelope cel_3_4 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_4, null, FeatureHelper.Get(f_h,"SZC",1)+"层", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格3-5
                x_ = x_ + w * 2 / 15;
                Envelope cel_3_5 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_5, null, "产权面积，㎡", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格3-6
                x_ = x_ + w * 1 / 6;
                Envelope cel_3_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_6, null, String.format("%.2f",tnmj+ftmj), o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);

                // 单元格4-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_4_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_1, null, "坐 落", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
                // 单元格4-2
                x_ = x_ + w * 2 / 15;
                Envelope cel_4_2 = new Envelope(x_, y_, x+ w , y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_2, null,Get(f_zd,"ZL",""), o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
//                dxf.write(mapInstance,fs_all);
                Envelope e_f = new Envelope(x, y_ - h, x + w, y - p_height, p_extend.getSpatialReference());
                GeodeticDistanceResult d_move = DxfHelper.getDistanceMove(fs_all, e_f, e_f.getSpatialReference());
                LineLabel lineLabel = new LineLabel();
                fs_all.remove(f_h);
                dxf.write(mapInstance, fs_all, null, d_move, DxfHelper.TYPE_NEIMENG, DxfHelper.LINE_LABEL_OUTSIDE, lineLabel);
                dxf.writeH(mapInstance, f_h, "", null, lineLabel, d_move, DxfHelper.LINE_LABEL_INNER, DxfHelper.LINE_WIDTH_3, DxfHelper.TYPE_NEIMENG);

                // 单元格4-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_5_1 = new Envelope(x_, y_, x_ + w, y_ - p_height+4*h, p_extend.getSpatialReference());
                dxf.write(cel_5_1, null,"", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);

                Point p_n = new Point(cel_5_1.getXMax() - o_split , cel_5_1.getYMax() - o_split);
                dxf.write(p_n, null, "北", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_CYAN, 0);
                DxfHelper.writeN2(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split,dxf);

                //  绘制单位
                x_=x;
                y_ = y - p_height;
                String hzdw =  GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZDW","");
                Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
                Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
                dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw,"\n"), o_fontsize, o_fontstyle, 0, 0, 3,  DxfHelper.COLOR_CYAN, null, null);

                Calendar c = Calendar.getInstance();
                String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
                c.add(Calendar.DATE, -1);
                String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
                Point p_auditDate = new Point(envelope.getXMax()-o_split, envelope.getYMin()+o_fontsize, envelope.getSpatialReference());
                dxf.writeText(p_auditDate,auditDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2,  DxfHelper.COLOR_CYAN, null, null);

                Point p_blc = new Point(envelope.getCenter().getX() , envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
                String blc =  "1:"+(int)DxfHelper.getNiceBlc(scale*200);
                dxf.writeText(p_blc, blc, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2,  DxfHelper.COLOR_CYAN, null, null);
            }

        } catch(Exception es){
                dxf.error("生成失败", es, true);
            }
            return this;
        }

    public String getFwjg(String fwjg){
        if (fwjg.contains("结构") && fwjg.contains("]")) {
            fwjg = fwjg.substring(fwjg.indexOf("]") + 1, fwjg.indexOf("]") + 1 + 2);
        } else {
            fwjg = fwjg.substring(fwjg.indexOf("]") + 1);
        }
        return fwjg;
    }

    private void writeN(Point p, double w) throws Exception {
        PointCollection ps = new PointCollection(p.getSpatialReference());
        ps.add(p);
        ps.add(new Point(p.getX() - w / 6, p.getY() - w / 2));
        ps.add(new Point(p.getX(), p.getY() + w / 2));
        ps.add(new Point(p.getX() + w / 6, p.getY() - w / 2));
        Geometry polygon = new Polygon(ps, p.getSpatialReference());
        dxf.write(polygon, null, "", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
    }

    public DxfFcfht_neimeng save() throws Exception {
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
