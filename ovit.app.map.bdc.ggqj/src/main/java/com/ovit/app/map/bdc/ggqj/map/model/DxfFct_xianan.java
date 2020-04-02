package com.ovit.app.map.bdc.ggqj.map.model;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.LineLabel;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * Created by XW on 2018/9/29. 房产图 固定1:200 比例尺
 */

public class DxfFct_xianan {

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
    private  double o_split = 2d;// 单元间隔
    private  double p_width = 33.875d;// 页面宽
    private  double p_height = 45.158d;// 页面高
    private  double h = 1.26d; // 行高
    private float o_fontsize=0.5f;// 字体大小
    private double scale=1;
    private String o_fontstyle = "宋体";// 字体
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_ZrzAndFs;

    public DxfFct_xianan(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfFct_xianan set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfFct_xianan set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfFct_xianan set(String bdcdyh, Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_h_fsjg) {
        this.bdcdyh = bdcdyh;
        this.f_zd = f_zd;
        fs_hAndFs = new ArrayList<>();

        fs_ZrzAndFs = new ArrayList<>();
        fs_ZrzAndFs.addAll(fs_h_fsjg);
        fs_ZrzAndFs.addAll(fs_z_fsjg);
//        fs_ZrzAndFs= FeatureEditZD.screenFSJG(fs_ZrzAndFs);
        fs_ZrzAndFs.addAll(fs_zrz);

        fs_hAndFs.addAll(fs_z_fsjg);
        fs_hAndFs.addAll(fs_h);
        fs_hAndFs.addAll(fs_h_fsjg);

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
//        double height = o_extend.getHeight();
//        double width = o_extend.getWidth();

//        double v_h = height *(1+0.1)/ p_height;
//        double v_w = width *(1+0.1)/ p_width;
//
//        double niceScale_h = DxfHelper.getNiceScale(v_h);
//        double niceScale_w = DxfHelper.getNiceScale(v_w);
//
//        scale=niceScale_w>niceScale_h?niceScale_w:niceScale_h;
//
//        if (scale>1){
//            p_width=p_width*scale;
//            p_height=p_height*scale;
//            h=h*scale;
//            o_split=o_split*scale;
////            o_fontsize = (float) (o_fontsize*scale);
//        }else {
//            scale=1;
//        }
        double x_min = o_center.getX() - (p_width / 2);
        double x_max = o_center.getX() + (p_width / 2);
        double y_min = o_center.getY() - (p_height / 2);
        double y_max = o_center.getY() + (p_height / 2);
        // 单元格范围
        p_extend = new Envelope(x_min, y_min, x_max, y_max, o_extend.getSpatialReference());
        return p_extend;
    }

    public DxfFct_xianan write() throws Exception {
        getExtend(); // 多大范围
        if (dxf == null) {
            dxf = new DxfAdapter();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            Envelope envelope = p_extend;
//            dxf.write(new Envelope(p_extend.getCenter(),p_width+o_split*3,p_height+o_split*5));
            DxfHelper.writeDaYingKuang(dxf,envelope,o_split,spatialReference);
            Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 0.6, envelope.getSpatialReference());
            dxf.writeText(p_title, "房产图", o_fontsize * 2, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

            Point p_unit = new Point(envelope.getXMax() , envelope.getYMax() + o_split*0.2, envelope.getSpatialReference());
            dxf.writeText(p_unit, "单位：米、平方米", 0.38f, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, null, null);
            double w = p_width; //行宽
            // 左上角
            double x = p_extend.getXMin();
            double y = p_extend.getYMax();

            double x_ = x;
            double y_ = y;

            // 单元格1-1
            Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 1 / 10+w*1/40, y_ - h, p_extend.getSpatialReference());
//                dxf.write(cel_1_1,"不动产单元号");
            write(dxf, cel_1_1, DxfHelper.LINETYPE_SOLID_LINE, "权利人", o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            // 单元格1-2
            x_ = x_ + w * 1 / 10+w*1/40;
            Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 1 / 2+w*1/20, y_ - h, p_extend.getSpatialReference());
            write(dxf, cel_1_2, DxfHelper.LINETYPE_SOLID_LINE, Get(f_zd, "QLRXM", ""), o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
            // 单元格1-3
            x_ = x_ + w * 1 / 2+w*1/20;
            Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
            write(dxf,cel_1_3, DxfHelper.LINETYPE_SOLID_LINE, "总建筑面积", o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
            // 单元格1-4
            x_ = x_ + w * 1 / 5;
            Envelope cel_1_4 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
            write(dxf, cel_1_4, DxfHelper.LINETYPE_SOLID_LINE, FeatureHelper.Get(f_zd,"JZMJ",0.00)+"", o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
            // 单元格2-1
            x_ = x;
            y_ = y_ - h;
            Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 1 / 10+w*1/40, y_ - h, p_extend.getSpatialReference());
            write(dxf, cel_2_1, DxfHelper.LINETYPE_SOLID_LINE, "宗地代码", o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
            // 单元格2-2
            x_ = x_ + w * 1 / 10+w*1/40;
            Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 1 / 4, y_ - h, p_extend.getSpatialReference());
            write(dxf, cel_2_2, DxfHelper.LINETYPE_SOLID_LINE, Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, ""), o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            // 单元格2-3
            x_ = x_ + w * 1 / 4;
            Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 4 / 20, y_ - h, p_extend.getSpatialReference());
            write(dxf, cel_2_3, DxfHelper.LINETYPE_SOLID_LINE, "不动产单元幢号", o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            // 单元格2-4
            x_ = x_ + w * 4 / 20;
            Envelope cel_2_4 = new Envelope(x_, y_, x_ + w * 1 / 10, y_ - h, p_extend.getSpatialReference());
            write(dxf, cel_2_4, DxfHelper.LINETYPE_SOLID_LINE, bdcdyh.endsWith("F99990001") ? "F9999" : "F0001", o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
            // 单元格2-5
            x_ = x_ + w * 1 / 10;
            Envelope cel_2_5 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
            write(dxf, cel_2_5, DxfHelper.LINETYPE_SOLID_LINE, "不动产单元户号", o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            // 单元格2-6
            x_ = x_ + w * 1 / 5;
            Envelope cel_2_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
            write(dxf, cel_2_6, DxfHelper.LINETYPE_SOLID_LINE, "0001", o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            // 单元格3-1
            x_ = x;
            y_ = y_ - h;
            Envelope cel_3_1 = new Envelope(x_, y_, x + w * 1 / 10 + w * 1 / 40, y_ - h, p_extend.getSpatialReference());
            write(dxf, cel_3_1, DxfHelper.LINETYPE_SOLID_LINE, "坐落", o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
            // 单元格3-2
            x_ = x + w * 1 / 10 + w * 1 / 40;
            Envelope cel_3_2 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
            write(dxf, cel_3_2, DxfHelper.LINETYPE_SOLID_LINE, Get(f_zd, "ZL", ""), o_fontsize, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            // 单元格4-1
            x_ = x;
            y_ = y_ - h;
            Envelope cel_4_1 = new Envelope(x_, y_, x_ + w, y_ - p_height+3*h, p_extend.getSpatialReference());
            dxf.write(cel_4_1);
            dxf.write(mapInstance, fs_ZrzAndFs, null,null, DxfHelper.TYPE_XIANAN, DxfHelper.LINE_LABEL_OUTSIDE,new LineLabel());

            Point p_n = new Point(cel_4_1.getXMax() - o_split , cel_4_1.getYMax() - o_split);
//            dxf.write(p_n, DxfHelper.LINETYPE_SOLID_LINE, "N", 0.454f, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
            dxf.writeText(p_n, "N", 0.454f, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

            DxfHelper.writeN2(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split,dxf);
            // 单元格4-0
            x_ = x;
            y_ = y - p_height;
            String hzdw= GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZDW","");
            Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize*1.26) * 1.8, x_, y_, p_extend.getSpatialReference());
            Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
            dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw,"\n"), o_fontsize*1.26f, DxfHelper.FONT_STYLE_SONGTI, 0, 0, 3, DxfHelper.COLOR_BYLAYER, null, null);
            // 落款
            String hzr="审核人："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR","");
            String shr="绘制人："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR","");

            Point cel_5_1 = new Point(envelope.getXMin(),envelope.getYMin()-h/2);
            dxf.writeText(cel_5_1, shr, o_fontsize*1.26f, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_BYLAYER, null, null);

            Envelope cel_5_2 = new Envelope(x_ , y_ , x+w, y_-h, p_extend.getSpatialReference());
            dxf.writeMText(cel_5_2.getCenter(), "1:200", o_fontsize*1.26f, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

            Calendar c = Calendar.getInstance();
           String auditDate = ("审核日期：")+c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)) + "日";
//            String drawDate = ("绘制日期：")+"2019年3月13日";
           c.add(Calendar.DATE, -1);
           String drawDate =("绘制日期：")+ c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + c.get(Calendar.DAY_OF_MONTH) + "日";
 //           String auditDate =("审核日期：")+ "2019年3月15日";
//          绘制日期
            Point cel_5_3 = new Point(envelope.getXMax(),envelope.getYMin()-h/2);
            dxf.writeText(cel_5_3, drawDate, o_fontsize*1.26f, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, null, null);

            //6-1
            y_=y_-h;
            Point cel_6_1 = new Point(envelope.getXMin(),envelope.getYMin()-1.2*h);
            dxf.writeText(cel_6_1, hzr, o_fontsize*1.26f, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_BYLAYER, null, null);

            //审核日期
            Point cel_6_3 = new Point(envelope.getXMax(),envelope.getYMin()-1.2*h);
            dxf.writeText(cel_6_3, auditDate, o_fontsize*1.26f, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, null, null);

        } catch (Exception es) {
            dxf.error("生成失败", es, true);
        }
        return this;
    }

    private void write(DxfAdapter dxf, Envelope cel, String linetype, String text, float o_fontsize, float fontWidth, String o_fontstyle, boolean islabel, int color, int lineWidth) throws Exception {
        dxf.write(cel, linetype, "", o_fontsize, o_fontstyle, islabel,color, lineWidth);
        dxf.writeText(cel.getCenter(),text, o_fontsize, fontWidth, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);
    }


    public DxfFct_xianan save() throws Exception {
        if (dxf != null) {
            dxf.save();
        }
        return this;
    }
}
