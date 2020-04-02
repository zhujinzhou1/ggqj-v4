package com.ovit.app.map.bdc.ggqj.map.model;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.util.Calendar;
import java.util.List;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * Created by XW on 2018/9/29. 房产图 固定1:200 比例尺
 */

public class DxfZdt_shannan {

    MapInstance mapInstance;
    SpatialReference spatialReference;

    DxfAdapter dxf;
    private String dxfpath;
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

    public DxfZdt_shannan(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfZdt_shannan set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfZdt_shannan set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfZdt_shannan set( Feature f_zd, List<Feature> fs_all) {
        this.f_zd = f_zd;
        this.fs_all =fs_all;
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

    public DxfZdt_shannan write() throws Exception {
        getExtend(); // 多大范围
        if (dxf == null) {
            dxf = new DxfAdapter();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            Envelope envelope = p_extend;
            Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
            dxf.writeText(p_title, "宗地图", 1.0f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

            Point p_unit = new Point(envelope.getXMax() , envelope.getYMax() + o_split*0.5, envelope.getSpatialReference());
            dxf.writeText(p_unit, "单位：m.m ", 0.5f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, 0, null, null);
            Point p_2 = new Point(p_unit.getX(), p_unit.getY()+0.2, envelope.getSpatialReference());
            dxf.writeText(p_2, "2",  0.3f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, 0, null, null);
            double w = p_width; //行宽
            // 左上角
            double x = p_extend.getXMin();
            double y = p_extend.getYMax();

            double x_ = x;
            double y_ = y;

            // 单元格1-1
            Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
//                dxf.write(cel_1_1,"不动产单元号");
            dxf.write(cel_1_1, null, "宗地代码", 0.5f, null, false, 0, 0);
            // 单元格1-2
            x_ = x_ + w * 2 / 15;
            Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_2, null, Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, ""), 0.5f, null, false, 0, 0);
            // 单元格1-3
            x_ = x_ + w * 4 / 15;
            Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 2 /15, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_3, null, "权利人", 0.5f, null, false, 0, 0);
            // 单元格1-4
            x_ = x_ +w * 2 /15;
            Envelope cel_1_4 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_4, null, FeatureHelper.Get(f_zd,"QLRXM",""), 0.5f, null, false, 0, 0);

            // 单元格2-1
            x_ = x;
            y_ = y_ - h;
            Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_1, null, "宗地坐落", 0.5f, null, false, 0, 0);
            // 单元格2-2
            x_ = x_ + w * 2 / 15;
            Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_2, null, FeatureHelper.Get(f_zd,"ZL",""), 0.5f, null, false, 0, 0);

            // 单元格2-3
            x_ = x_ + w * 4 / 15;
            Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_3, null, "宗地图幅号", 0.5f, null, false, 0, 0);

            // 单元格2-4
            x_ = x_ + w * 2 / 15;
            Envelope cel_2_4 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_4, null, FeatureHelper.Get(f_zd,"TFH",""), 0.5f, null, false, 0, 0);

            // 单元格3-1
            x_ = x;
            y_ = y_ - h;
            Envelope cel_3_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_1, null, "宗地面积", 0.5f, null, false, 0, 0);
            // 单元格3-2
            x_ = x_ + w * 2 / 15;
            Envelope cel_3_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_2, null, FeatureHelper.Get(f_zd,"ZDMJ",0.00)+"", 0.5f, null, false, 0, 0);

            // 单元格3-3
            x_ = x_ + w * 4 / 15;
            Envelope cel_3_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_3, null, "其中", 0.5f, null, false, 0, 0);

            // 单元格3-4
            x_ = x_ + w * 2 / 15;
            Envelope cel_3_4 = new Envelope(x_, y_, x_ + w * 1 / 10, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_4, null, "确权面积", 0.5f, null, false, 0, 0);
            // 单元格3-5
            x_ = x_ + w * 1 / 10;
            Envelope cel_3_5 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_5, null, FeatureHelper.Get(f_zd,"ZDMJ",0.00)+"", 0.5f, null, false, 0, 0);
            // 单元格3-6
            x_ = x_ + w * 2 / 15;
            Envelope cel_3_6 = new Envelope(x_, y_, x_ + w * 1 / 10, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_6, null, "超占面积", 0.5f, null, false, 0, 0);
            // 单元格3-7
            x_ = x_ + w * 1 / 10;
            Envelope cel_3_7 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_7, null, "", 0.5f, null, false, 0, 0);

            // 单元格4-1
            x_ = x;
            y_ = y_-h;
            Envelope cel_4_1 = new Envelope(x_, y_, x_ + w , y, p_extend.getSpatialReference());
            dxf.write(cel_4_1);
            dxf.write(mapInstance, fs_all, null);
            // 单元格4-2
            Envelope cel_4_2 = new Envelope(x_, y_, x_ + w, y -p_height, p_extend.getSpatialReference());
            dxf.write(cel_4_2);
            Point p_n = new Point(cel_4_2.getXMax() - o_split , cel_4_2.getYMax() - o_split);
            dxf.write(p_n, null, "N", 0.45f, null, false, 0, 0);
            writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split);

            // 宗地四至
            Envelope e= MapHelper.geometry_combineExtents_Feature(fs_all);;
            double e_height = e.getHeight();
            Point point_d = new Point(e.getXMax() + o_split / 2, e. getYMin() + e_height / 2, e.getSpatialReference());
            Point point_x = new Point(e.getXMin() - o_split / 2, e.getYMin() + e_height / 2, e.getSpatialReference());
            Point point_n = new Point(e.getCenter().getX(), e.getYMax() + o_split / 3, e.getSpatialReference());
            Point point_b = new Point(e.getCenter().getX(), e.getYMin() - o_split / 3, e.getSpatialReference());
            dxf.writeText(point_n, FeatureHelper.Get(f_zd,"ZDSZN",""), o_fontsize , DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
            dxf.writeText(point_b,  FeatureHelper.Get(f_zd,"ZDSZB",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
            dxf.writeMText(point_d, StringUtil.GetDxfStrFormat( FeatureHelper.Get(f_zd,"ZDSZD",""),"\n"), o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);
            dxf.writeMText(point_x, StringUtil.GetDxfStrFormat( FeatureHelper.Get(f_zd,"ZDSZX",""),"\n"), o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);

            //单元格5-1  宗地四址界线
            x_=x+w*2/3;
            y_=y-p_height+5*h;
            Envelope cel_5_1 = new Envelope(x_, y_, x+ w, y_ -h, p_extend.getSpatialReference());
            dxf.write(cel_5_1, null, "宗地四址界线", 0.5f, null, false, 0, 0);
            //单元格6-1  东
            y_=y_-h;
            Envelope cel_6_1 = new Envelope(x_, y_, x_+ w*1/30, y_ -h, p_extend.getSpatialReference());
            dxf.write(cel_6_1, null, "东", 0.5f, null, false, 0, 0);
            //单元格6-2  东
            x_=x_+ w*1/30;
            Envelope cel_6_2 = new Envelope(x_, y_, x+ w, y_ -h, p_extend.getSpatialReference());
            dxf.write(cel_6_2, null, "", 0.5f, null, false, 0, 0);
            //单元格7-1  东
            x_=x+w*2/3;
            y_=y_-h;
            Envelope cel_7_1 = new Envelope(x_, y_, x_+ w*1/30, y_ -h, p_extend.getSpatialReference());
            dxf.write(cel_7_1, null, "南", 0.5f, null, false, 0, 0);
            //单元格7-1  东
            x_=x_+ w*1/30;
            Envelope cel_7_2 = new Envelope(x_, y_, x+ w, y_ -h, p_extend.getSpatialReference());
            dxf.write(cel_7_2, null, "", 0.5f, null, false, 0, 0);
            //单元格8-1  东
            x_=x+w*2/3;
            y_=y_-h;
            Envelope cel_8_1 = new Envelope(x_, y_, x_+ w*1/30, y_ -h, p_extend.getSpatialReference());
            dxf.write(cel_8_1, null, "西", 0.5f, null, false, 0, 0);
            //单元格8-2  东
            x_=x_+ w*1/30;
            Envelope cel_8_2 = new Envelope(x_, y_, x+ w, y_ -h, p_extend.getSpatialReference());
            dxf.write(cel_8_2, null, "", 0.5f, null, false, 0, 0);
            //单元格9-1  东
            x_=x+w*2/3;
            y_=y_-h;
            Envelope cel_9_1 = new Envelope(x_, y_, x_+ w*1/30, y_ -h, p_extend.getSpatialReference());
            dxf.write(cel_9_1, null, "北", 0.5f, null, false, 0, 0);
            //单元格9-1  东
            x_=x_+ w*1/30;
            Envelope cel_9_2 = new Envelope(x_, y_, x+ w, y-p_height, p_extend.getSpatialReference());
            dxf.write(cel_9_2, null, "", 0.5f, null, false, 0, 0);

            // 单元格4-0  绘制单位
            x_=x;
            y_ = y - p_height;
            String hzdw =  GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZDW","");
            Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
            Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
            dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw,"\n"), o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);

            Calendar c = Calendar.getInstance();
            String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
            c.add(Calendar.DATE, -1);
            String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");

            Point p_auditDate = new Point(envelope.getCenter().getX()- w * 1 / 3-w * 1 / 20, envelope.getYMin() -3* o_split * 0.3, envelope.getSpatialReference());
            Point p_drawDate = new Point(envelope.getCenter().getX()- w * 1 / 3-w * 1 / 20 , envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
            dxf.writeText(p_drawDate, "绘图日期:"+drawDate, 0.5f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
            dxf.writeText(p_auditDate,"审核日期:"+auditDate, 0.5f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

            Point p_blc = new Point(envelope.getCenter().getX() , envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
            dxf.writeText(p_blc, "1:200", 0.5f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

            Point p_chr = new Point(envelope.getCenter().getX() + w * 1 / 3+w*1/10, envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
            dxf.writeText(p_chr, "测绘员："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR",""), 0.5f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
            Point p_shr = new Point(envelope.getCenter().getX() + w * 1 / 3+w*1/10, envelope.getYMin() - 3*o_split * 0.3, envelope.getSpatialReference());
            dxf.writeText(p_shr, "审核员："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR",""), 0.5f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

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
        dxf.write(new Polygon(ps, p.getSpatialReference()), null);
    }

    public DxfZdt_shannan save() throws Exception {
        if (dxf != null) {
            dxf.save();
        }
        return this;
    }

}
