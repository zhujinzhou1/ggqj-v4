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
import com.ovit.app.util.gdal.dxf.DxfConstant;
import com.ovit.app.util.gdal.dxf.DxfHelper;
import com.ovit.app.util.gdal.dxf.DxfPaint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * Created by xw on 2020/6/28.
 */

public class DxfZdct_HuNan extends BaseDxf {
    private Feature f_zd;
    private List<Feature> fs_all;
    private List<Feature> fs_jzd;

    public DxfZdct_HuNan(MapInstance mapInstance) {
        super(mapInstance);
    }

    @Override
    public DxfZdct_HuNan set(String dxfpath) {
        super.set(dxfpath);
        return this;
    }

    public DxfZdct_HuNan set(Feature f_zd, Map<String, List<Feature>> mapfs) {
        List<Feature> fs_all = new ArrayList<>();
        List<Feature> fs_fsjg = new ArrayList<>();
        List<Feature> mfs_fsjg = new ArrayList<>(); // 筛选后的附属结构
        fs_all.addAll(mapfs.get(FeatureHelper.TABLE_NAME_ZD));
        fs_all.addAll(mapfs.get(FeatureHelper.TABLE_NAME_ZRZ));
        fs_all.addAll(mapfs.get(FeatureHelper.TABLE_NAME_LJZ));

        fs_fsjg.addAll(mapfs.get(FeatureHelper.TABLE_NAME_H_FSJG));
        fs_fsjg.addAll(mapfs.get(FeatureHelper.TABLE_NAME_Z_FSJG));
        mfs_fsjg = getFilterFsjg(fs_fsjg);// 筛选后的附属结构
        fs_all.addAll(mfs_fsjg);

        fs_all.addAll(mapfs.get(FeatureHelper.TABLE_NAME_ZJX));
//        fs_all.addAll(mapfs.get(FeatureHelper.TABLE_NAME_JZD));
        fs_all.addAll(mapfs.get(FeatureHelper.TABLE_NAME_XZDW));
        fs_all.addAll(mapfs.get(FeatureHelper.TABLE_NAME_MZDW));
        fs_all.addAll(mapfs.get(FeatureHelper.TABLE_NAME_FSSS));
        fs_all.addAll(mapfs.get(FeatureHelper.TABLE_NAME_SJ));
        this.f_zd = f_zd;
        this.fs_jzd = mapfs.get(FeatureHelper.TABLE_NAME_JZD);
        this.fs_all = fs_all;
        return this;
    }

    @Override
    protected void getDxfRenderer() {
        dxfRenderer.setLayerLableFlag(FeatureHelper.LAYER_NAME_ZRZ,false);
        dxfRenderer.setLayerLableFlag(FeatureHelper.LAYER_NAME_ZD,true);
        dxfRenderer.setLayerLableFlag(FeatureHelper.LAYER_NAME_JZD,true);
        dxfRenderer.setLayerLableFlag(FeatureHelper.LAYER_NAME_LJZ,false);
        dxfRenderer.setZDDM(mapInstance.getId(f_zd));
    }

    @Override
    protected void getHeader() throws Exception {
        getDefultHeader();
    }

    @Override
    protected void getBody() throws Exception {
        double x = p_extend.getXMin();
        double y = p_extend.getYMax();
        double x_ = x;
        double y_ = y;
        double w = p_width;

        // 单元格4-2
        paint.setFontstyle(o_fontstyle);
        paint.setFontsize(o_fontsize);

        paint.setLayer(DxfConstant.DXF_LAYER_BKZJ);
        Envelope cel_4_2 = new Envelope(x_, y_, x_ + w, y - p_height, spatialReference);
        Point p_n = new Point(cel_4_2.getXMax() -h, y_ -3*h);
        dxf.writeText(p_n, DxfConstant.DXF_ZBZ_B);
        writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split);

        // 单元格1-1
        Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 3 / 15, y_ - h, spatialReference);
        dxf.writeText(cel_1_1.getCenter(),"宗地代码",paint);
        // 单元格1-2
        x_ = x_ + w * 3 / 15;
        Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 1 / 15, y_ - h, spatialReference);
        dxf.writeText(cel_1_2.getCenter(),Get(f_zd, "ZDDM", ""), paint);

        // 单元格1-3
        x_ = x_ + w * 6 / 15;
        Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 2 /15, y_ - h, spatialReference);
        dxf.writeText(cel_1_3.getCenter(),"土地权利人", paint);

        // 单元格1-4
        x_ = x_ +w * 2 /15;
        Envelope cel_1_4 = new Envelope(x_, y_, x+w, y_ - h, spatialReference);
        dxf.writeText(cel_1_4.getCenter(),FeatureHelper.Get(f_zd,"QLRXM",""), paint);

        // 单元格2-1
        x_ = x;
        y_ = y_ - h;
        Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 3 / 15, y_ - h, spatialReference);
        dxf.writeText(cel_2_1.getCenter(),"所在图幅号", paint);

        // 单元格2-2
        x_ = x_ + w * 3 / 15;
        Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 1 / 15, y_ - h, spatialReference);
        dxf.writeText(cel_2_2.getCenter(),FeatureHelper.Get(f_zd,"TFH",""), paint);

        // 单元格2-3
        x_ = x_ + w * 6 / 15;
        Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, spatialReference);
        dxf.writeText(cel_2_3.getCenter(),"宗地面积", paint);

        // 单元格2-4
        x_ = x_ + w * 2 / 15;
        Envelope cel_2_4 = new Envelope(x_, y_, x+w, y_ - h, spatialReference);
        Double dsyqmj =Get(f_zd,"SYQMJ",0.00);
        String syqmj = dsyqmj == 0? "/" : String.format("%.2f",dsyqmj);
        dxf.writeText(cel_2_4.getCenter(),syqmj+"", paint);

        Envelope cel_4_1 = new Envelope(x, y_-h, x+ w, y-p_height,spatialReference);
        dxf.write(cel_4_1);



//        final double buffer = DxfHelper.getBufferDistance();
//        Geometry cutGeometey = GeometryEngine.buffer(f_zd.getGeometry(), buffer);


        Envelope cel_4_1_2 = new Envelope(x, y-2*h, x+ w, y-p_height+h,spatialReference);
        Envelope cel_4_1_n = new Envelope(x+w-2*h, y-2*h, x+ w, y-6*h,spatialReference);
        Geometry cut = GeometryEngine.difference(cel_4_1_2, cel_4_1_n);
        paint.setCutArea(cut);



        dxf.write(mapInstance, fs_all);
        dxf.writeJZDS(fs_jzd);
        writeZdsz(dxf, f_zd);

        paint.setTextAlign(DxfPaint.Align.RIGHT);
        Point p_z = new Point(x+w, p_extend.getYMin() + h*0.5, spatialReference);
        dxf.writeText(p_z, "注：实际建筑占地面积为"+Get(f_zd,"ZDMJ",0.00)+"平方米" );

    }

    @Override
    protected void getFooter() throws Exception {
        getDefultFooter();
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
