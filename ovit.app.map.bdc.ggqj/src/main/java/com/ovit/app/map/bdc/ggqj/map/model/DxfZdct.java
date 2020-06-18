package com.ovit.app.map.bdc.ggqj.map.model;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.dxf.DxfAdapter;
import com.ovit.app.util.gdal.dxf.DxfHelper;
import com.ovit.app.util.gdal.dxf.DxfPaint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * Created by XW on 2018/9/29. 洪湖宗地草图--默认-湖北版
 */

public class DxfZdct {

    MapInstance mapInstance;
    SpatialReference spatialReference;

    DxfAdapter dxf;
    private String dxfpath;
    private List<Feature> fs_all;
    private List<Feature> fs_hAndFs;
    private Feature f_zd;
    private Point o_center;   // o 点
    private Envelope o_extend;// 真实图形的范围
    private double o_split=2d;// 单元间隔
    private double p_width = 36d;// 页面宽
    private double p_height = 48d;// 页面高
    private double h = 1.5d; // 行高
    private float o_fontsize=0.6f;// 字体大小
    private double scale;
    private double blc=200d;
    private String o_fontstyle = "宋体";// 字体大小
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;
    private List<Feature> fs_z_fsjg;
    private List<Feature> fs_h_fsjg;
    private List<Feature> fs_jzd;
    private List<Feature> fs_fsjg;

    public DxfZdct(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfZdct set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfZdct set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfZdct set(Feature f_zd, List<Feature> fs_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h_fsjg, List<Feature> fs_jzd) {
        List<Feature> fs_all=new ArrayList<>();
        List<Feature> fs_fsjg=new ArrayList<>();
        fs_all.add(f_zd);
        fs_all.addAll(fs_zrz);
        fs_all.addAll(fs_z_fsjg);
        fs_all.addAll(fs_h_fsjg);
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

        double v_h = height *(1+0.2)/ p_height;
        double v_w = width *(1+0.2)/ p_width;

        double niceScale_h= DxfHelper.getNiceScale(v_h);
        double niceScale_w= DxfHelper.getNiceScale(v_w);

        scale=niceScale_w>niceScale_h?niceScale_w:niceScale_h;
        if (scale>1){
            p_width=p_width*scale;
            p_height=p_height*scale;
            h=h*scale;
            o_split=o_split*scale;
            blc=blc*scale;
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

    public DxfZdct write() throws Exception {
        getExtend(); // 多大范围
        if (dxf == null) {
            dxf = new DxfAdapter();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            DxfPaint paint = new DxfPaint();  // dxf 画笔
            paint.setColor(DxfHelper.COLOR_BYLAYER);
            paint.setFontsize(o_fontsize);
            paint.setFontstyle(o_fontstyle);
            paint.setTextAlign(DxfPaint.Align.CENTER);

            Envelope envelope = p_extend;
            Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
            dxf.writeText(p_title, "宗地草图", paint);

            paint.setTextAlign(DxfPaint.Align.RIGHT);
            paint.setFontsize(o_fontsize * 0.8f);
            Point p_unit = new Point(envelope.getXMax() , envelope.getYMax() + o_split*0.5, envelope.getSpatialReference());
            dxf.writeText(p_unit, "单位：m.㎡",paint);


            double w = p_width; //行宽
            // 左上角
            double x = p_extend.getXMin();
            double y = p_extend.getYMax();

            double x_ = x;
            double y_ = y;
            dxf.write(p_extend, null, "", o_fontsize, null, false, 0, 0.4f);

            // 单元格1-1
            Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.writeMText(cel_1_1.getCenter(),"宗地代码：");
            // 单元格1-2
            x_ = x_ + w * 2 / 15;
            Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.writeMText(cel_1_2.getCenter(),Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, ""));
            // 单元格1-3
            x_ = x_ + w * 8/ 15;
            Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 2 /15, y_ - h, p_extend.getSpatialReference());
            dxf.writeMText(cel_1_3.getCenter(),"土地权利人：");

            // 单元格1-4
            x_ = x_ +w * 2 /15;
            Envelope cel_1_4 = new Envelope(x_, y_, x_ + w * 2 /15, y_ - h, p_extend.getSpatialReference());
            dxf.writeMText(cel_1_4.getCenter(), FeatureHelper.Get(f_zd,"QLRXM",""));

            // 单元格2-1
            x_ = x;
            y_ = y_ - h;
            Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.writeMText(cel_2_1.getCenter(),"所在图幅号：");

            // 单元格2-2
            x_ = x_ + w * 2 / 15;
            Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.writeMText(cel_2_2.getCenter(), FeatureHelper.Get(f_zd,"TFH",""));

            // 单元格2-3
            x_ = x_ + w * 8 / 15;
            Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.writeMText(cel_2_3.getCenter(),"宗地面积：");

            // 单元格2-4
            x_ = x_ + w * 2 / 15;
            Envelope cel_2_4 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.writeMText(cel_2_4.getCenter(), FeatureHelper.Get(f_zd,FeatureHelper.TABLE_ATTR_ZDMJ,0.00)+"");

            // 单元格4-1
            x_ = x;
            y_ = y_-h;
            Envelope cel_4_1 = new Envelope(x_, y_, x_ + w , y, p_extend.getSpatialReference());
            dxf.write(cel_4_1);
            for (Feature feature : fs_zrz) {
                dxf.writeZRZ(mapInstance, feature, null, null, null, false, 2, DxfHelper.LINE_LABEL_OUTSIDE);
            }
            for (Feature f_fsjg : fs_fsjg) {
                dxf.write(f_fsjg.getGeometry(), DxfHelper.LINETYPE_DOTTED_LINE, "", 0, "", false, DxfHelper.COLOR_BLUE, 0);
            }
            dxf.writeZD(f_zd,null, DxfHelper.LINE_LABEL_OUTSIDE);

            // 单元格4-2
            Envelope cel_4_2 = new Envelope(x_, y_, x_ + w, y -p_height, p_extend.getSpatialReference());
            dxf.write(cel_4_2);
            Point p_n = new Point(cel_4_2.getXMax() - o_split , cel_4_2.getYMax() - o_split);
            dxf.write(p_n, null, "N", o_fontsize, null, false, 0, 0);
            DxfHelper.writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split,dxf);

            // 宗地四至
            Envelope e= MapHelper.geometry_combineExtents_Feature(fs_all);
            DxfHelper.writeZdsz(dxf,f_zd,e,o_split,o_fontsize,o_fontstyle);
           
            // 单元格4-0  绘制单位
            x_=x;
            y_ = y - p_height;
            String hzdw =  GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZDW","");
            Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
            Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
            dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw,"\n"), o_fontsize, o_fontstyle, 0, 0, 3, DxfHelper.COLOR_CYAN, null, null);

            Calendar c = Calendar.getInstance();
            String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
            c.add(Calendar.DATE, -1);
            String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");

            Point p_auditDate = new Point(envelope.getCenter().getX()- w * 1 / 3-w * 1 / 20, envelope.getYMin() -3* o_split * 0.3, envelope.getSpatialReference());
            Point p_drawDate = new Point(envelope.getCenter().getX()- w * 1 / 3-w * 1 / 20 , envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
            dxf.writeText(p_drawDate, "绘图日期:"+drawDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_CYAN, null, null);
            dxf.writeText(p_auditDate,"审核日期:"+auditDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_CYAN, null, null);

            Point p_blc = new Point(envelope.getCenter().getX() , envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
            dxf.writeText(p_blc, "1:"+(int)blc,o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_CYAN, null, null);

            Point p_chr = new Point(envelope.getCenter().getX() + w * 1 / 3+w*1/10, envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
            dxf.writeText(p_chr, "测绘员："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_CYAN, null, null);
            Point p_shr = new Point(envelope.getCenter().getX() + w * 1 / 3+w*1/10, envelope.getYMin() - 3*o_split * 0.3, envelope.getSpatialReference());
            dxf.writeText(p_shr, "审核员："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_CYAN, null, null);

        } catch (Exception es) {
            dxf.error("生成失败", es, true);
        }
        return this;
    }

    public DxfZdct save() throws Exception {
        if (dxf != null) {
            dxf.save();
        }
        return this;
    }
}
