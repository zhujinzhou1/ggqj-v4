package com.ovit.app.map.bdc.ggqj.map.model;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.ovit.app.map.bdc.ggqj.map.BaseDxf;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.gdal.dxf.DxfConstant;
import com.ovit.app.util.gdal.dxf.DxfHelper;
import com.ovit.app.util.gdal.dxf.DxfPaint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xw on 2020/4/1.
 */

public class DxfZdctDefult extends BaseDxf {
    private Feature f_zd;
    private List<Feature> fs_all;

    public DxfZdctDefult(MapInstance mapInstance) {
        super(mapInstance);
    }

    @Override
    public DxfZdctDefult set(String dxfpath) {
        super.set(dxfpath);
        return this;
    }

    public DxfZdctDefult set(Feature f_zd, List<Feature> fs_zd, List<Feature> fs_zrz
            , List<Feature> fs_z_fsjg, List<Feature> fs_h_fsjg, List<Feature> fs_jzd
            , List<Feature> fs_zj_x, List<Feature> fs_xzdw, List<Feature> fs_mzdw) {
        List<Feature> fs_all = new ArrayList<>();
        List<Feature> fs_fsjg = new ArrayList<>();
        List<Feature> mfs_fsjg = new ArrayList<>(); // 筛选后的附属结构
        fs_all.addAll(fs_zd);
        fs_all.addAll(fs_zrz);

        fs_fsjg.addAll(fs_h_fsjg);
        fs_fsjg.addAll(fs_z_fsjg);
        mfs_fsjg = getFilterFsjg(fs_fsjg);// 筛选后的附属结构

        fs_all.addAll(mfs_fsjg);
        fs_all.addAll(fs_jzd);
        fs_all.addAll(fs_zj_x);
        fs_all.addAll(fs_xzdw);
        fs_all.addAll(fs_mzdw);
        this.f_zd = f_zd;
        this.fs_all = fs_all;
        return this;
    }

    @Override
    protected void getDxfRenderer() {
        dxfRenderer.setLayerLableFlag(FeatureHelper.LAYER_NAME_ZRZ,false);
        dxfRenderer.setLayerLableFlag(FeatureHelper.LAYER_NAME_ZD,true);
        dxfRenderer.setLayerLableFlag(FeatureHelper.LAYER_NAME_JZD,true);
        dxfRenderer.setZDDM(mapInstance.getId(f_zd));
    }

    @Override
    protected void getHeader() throws Exception {
        Point p_c = p_extend.getCenter();

        paint.setLayer(DxfConstant.DXF_LAYER_TK);
        paint.setFontstyle(DxfHelper.FONT_STYLE_HZ);
        paint.setLinewidth(DxfHelper.LINE_WIDTH_3);
        dxf.write(new Envelope(p_c, boxWidth, boxHeight), ""); // 图框
        //  图形范围
        paint.setLayer(DxfConstant.DXF_LAYER_BKZJ);
        paint.setLinewidth(DxfHelper.LINE_WIDTH_DEFULT);
        dxf.write(p_extend, "");

        Point p_title = new Point(p_c.getX(), p_extend.getYMax() - 2 * o_split, spatialReference);
        paint.setFontsize(o_fontsize * 2);
        dxf.writeText(p_title, DxfConstant.DXF_ZDCT);
    }

    @Override
    protected void getBody() throws Exception {
        double x = p_extend.getXMin();
        double y = p_extend.getYMax();
        double x_ = x;
        double y_ = y;
        double w = p_width;

        // 单元格4-1
        x_ = x;
        y_ = y_ - h;
        // 单元格4-2
        paint.setFontsize(o_fontsize);
        final double buffer = DxfHelper.getBufferDistance();
        Geometry cutGeometey = GeometryEngine.buffer(f_zd.getGeometry(), buffer);
        paint.setCutArea(cutGeometey);

        dxf.write(mapInstance, fs_all);
        paint.setLayer(DxfConstant.DXF_LAYER_ZDSZZJ);
        writeZdsz(dxf, f_zd);

        paint.setLayer(DxfConstant.DXF_LAYER_BKZJ);
        Envelope cel_4_2 = new Envelope(x_, y_, x_ + w, y - p_height, spatialReference);
        Point p_n = new Point(cel_4_2.getXMax() - o_split, cel_4_2.getYMax() - o_split * 1.5);
        dxf.writeText(p_n, DxfConstant.DXF_ZBZ_B);
        writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split);
        // 宗地四至
        // 单元格4-0  绘制单位
        x_ = x;
        y_ = y - p_height + 2 * h;
        String auditDate = "2020-12-08";
        String drawDate = "2020-12-07";

        String drawPerson = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZR", "");
        String auditPerson = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "SHR", "");

        Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h,spatialReference);
        dxf.write(cel_1_1, DxfConstant.DXF_ZLZ);
        // 1-2
        x_ = x_ + w * 1 / 6;
        Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, spatialReference);
        dxf.write(cel_1_2, drawPerson);

        // 1-3
        x_ = x_ + w * 1 / 6;
        Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, spatialReference);
        dxf.write(cel_1_3, DxfConstant.DXF_ZLRQ);
        x_ = x_ + w * 1 / 8;
        Envelope cel_1_4 = new Envelope(x_, y_, x_ + w * 5 / 24, y_ - h, spatialReference);
        dxf.write(cel_1_4, drawDate);

        // 1-3
        x_ = x_ + w * 5 / 24;
        Envelope cel_1_5 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - 2 * h, spatialReference);
        dxf.write(cel_1_5, DxfConstant.DXF_BLC);
        x_ = x_ + w * 1 / 6;
        Envelope cel_1_6 = new Envelope(x_, y_, x + w, y_ - 2 * h, spatialReference);
        dxf.write(cel_1_6, "1:" + (int) blc);

        x_ = x;
        y_ = y_ - h;

        Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, spatialReference);
        dxf.write(cel_2_1, DxfConstant.DXF_ZLZ);
        // 1-2
        x_ = x_ + w * 1 / 6;
        Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, spatialReference);
        dxf.write(cel_2_2, auditPerson);

        // 1-3
        x_ = x_ + w * 1 / 6;
        Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, spatialReference);
        dxf.write(cel_2_3, DxfConstant.DXF_JCRQ);
        x_ = x_ + w * 1 / 8;
        Envelope cel_2_4 = new Envelope(x_, y_, x_ + w * 5 / 24, y_ - h, spatialReference);
        dxf.write(cel_2_4, auditDate);

        Point point = new Point(x + w * 21 / 24, y - p_height + 2.5 * h);
        paint.setTextAlign(DxfPaint.Align.LEFT);
        dxf.writeText(point, "说明：图中单位为米。");
    }

    @Override
    protected void getFooter() throws Exception {
        // 单元格4-0  绘制单位
//        getDefultFooter();
    }

    @Override
    public double getHeightScale() {
        return o_extend.getHeight() / (p_height - 4 * h);
    }

    @Override
    public double getWidthScale() {
        return o_extend.getWidth() / p_width;
    }

    @Override
    public Envelope getChildExtend() {
        Geometry buffer = GeometryEngine.buffer(f_zd.getGeometry(), DxfHelper.getBufferDistance());
        return MapHelper.geometry_get(buffer.getExtent(), spatialReference);
    }

    @Override
    public double getPictureBoxBufferFactor() {
        return 0.15;
    }

    private void writeN(Point p, double w) throws Exception {
        PointCollection ps = new PointCollection(p.getSpatialReference());
        ps.add(p);
        ps.add(new Point(p.getX() - w / 6, p.getY() - w / 2));
        ps.add(new Point(p.getX(), p.getY() + w / 2));
        ps.add(new Point(p.getX() + w / 6, p.getY() - w / 2));
        dxf.write(new Polygon(ps, p.getSpatialReference()),  "");
    }

    private List<Feature> getFilterFsjg(List<Feature> fs_fsjg) {

        if (FeatureHelper.isExistElement(fs_fsjg)) {
            if (fs_fsjg.size() == 1) {
                return fs_fsjg;
            }
            List<Feature> fs = new ArrayList<>();
            for (Feature fsjg : fs_fsjg) {
                if (FeatureHelper.isPolygonFeatureValid(fsjg)) {
                    Feature f = getMaxLCFs(fs_fsjg, fs, fsjg);
                    if (f != null) {
                        fs.add(f);
                    }
                }
            }
            return fs;

        } else {
            return fs_fsjg;
        }
    }

    private Feature getMaxLCFs(List<Feature> fs, List<Feature> fs_container, Feature fsjg) {
        Point point = GeometryEngine.labelPoint((Polygon) fsjg.getGeometry());
        Feature mFeature = fsjg;
        for (Feature f : fs) {
            int szc = FeatureHelper.Get(mFeature, "SZC", 1);
            int cszc = FeatureHelper.Get(f, "SZC", 1);
            if (MapHelper.geometry_contains(f.getGeometry(), point) && cszc > szc) {
                mFeature = f;
            }
        }
        for (Feature feature : fs_container) {
            if (MapHelper.geometry_contains(feature.getGeometry(), point)) {
                return null;
            }

        }
        return mFeature;
    }

}
