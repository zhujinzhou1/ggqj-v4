package com.ovit.app.map.bdc.ggqj.map.model;

import android.text.TextUtils;

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
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.dxf.DxfConstant;
import com.ovit.app.util.gdal.dxf.DxfHelper;
import com.ovit.app.util.gdal.dxf.DxfPaint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * 仙桃宗地图
 */
public class DxfZdt_xiantao extends BaseDxf {
    private Feature f_zd;
    private List<Feature> fs_all;
    private List<Feature> fs_jzd;

    public DxfZdt_xiantao(MapInstance mapInstance) {
        super(mapInstance);
    }

    @Override
    public DxfZdt_xiantao set(String dxfpath) {
        super.set(dxfpath);
        return this;
    }
    @Override
    protected void getDxfRenderer() {
        dxfRenderer.setLayerLableFlag(FeatureHelper.LAYER_NAME_ZRZ, false);
        dxfRenderer.setLayerLableFlag(FeatureHelper.LAYER_NAME_ZD, true);
        dxfRenderer.setLayerLableFlag(FeatureHelper.LAYER_NAME_JZD, true);
        dxfRenderer.setLayerLableFlag(FeatureHelper.LAYER_NAME_LJZ, false);
        dxfRenderer.setZDDM(mapInstance.getId(f_zd));
    }

    public DxfZdt_xiantao set(Feature f_zd, Map<String,List<Feature>> mapfs){
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

    private Feature getMaxLCFs(List<Feature> fs_fsjg, List<Feature> fs, Feature fsjg) {

        Point point = GeometryEngine.labelPoint((Polygon) fsjg.getGeometry());
        Feature mFeature = fsjg;
        for (Feature f : fs) {
            int szc = FeatureHelper.Get(mFeature, "SZC", 1);
            int cszc = FeatureHelper.Get(f, "SZC", 1);
            if (MapHelper.geometry_contains(f.getGeometry(), point) && cszc > szc) {
                mFeature = f;
            }
        }
        for (Feature feature : fs_fsjg) {
            if (MapHelper.geometry_contains(feature.getGeometry(), point)) {
                return null;
            }

        }
        return mFeature;
    }
    private void writeN(Point p, double w) throws Exception {
        PointCollection ps = new PointCollection(p.getSpatialReference());
        ps.add(p);
        ps.add(new Point(p.getX() - w / 6, p.getY() - w / 2));
        ps.add(new Point(p.getX(), p.getY() + w / 2));
        ps.add(new Point(p.getX() + w / 6, p.getY() - w / 2));
        dxf.write(new Polygon(ps, p.getSpatialReference()),  "");
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
        Point p_n = new Point(cel_4_2.getXMax() - h, y_ - 4 * h);
        dxf.writeText(p_n, DxfConstant.DXF_ZBZ_B);
        writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split);


        // 单元格1-1
        Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
//                dxf.write(cel_1_1,"不动产单元号");
        dxf.write(cel_1_1, null, "宗地代码", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
        // 单元格1-2
        x_ = x_ + w * 1 / 5;
        Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 3 / 10, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_1_2, null, FeatureHelper.Get(f_zd, "ZDDM", ""), o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
        // 单元格1-3
        x_ = x_ + w * 3 / 10;
        Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 1 /5, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_1_3, null, "土地权利人", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
        // 单元格1-4
        x_ = x_ +w * 1 /5;
        Envelope cel_1_4 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_1_4, null, FeatureHelper.Get(f_zd,"QLRXM",""), o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);

        // 单元格2-1
        x_ = x;
        y_ = y_ - h;
        Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_2_1, null, "所在图幅号", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
        // 单元格2-2
        x_ = x_ + w * 1 / 5;
        Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 3 / 10, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_2_2, null,FeatureHelper.Get(f_zd,"TFH",""), o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);

        // 单元格2-3
        x_ = x_ + w * 3 / 10;
        Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_2_3, null, "宗地面积", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);

        // 单元格2-4
        x_ = x_ + w * 1 / 5;
        Envelope cel_2_4 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_2_4, null, FeatureHelper.Get(f_zd,"ZDMJ",0.00)+"", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);

        // 单元格3-1
        // 单元格2-1
        x_ = x;
        y_ = y_ - h;
        Envelope cel_3_1 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_3_1, null, "土地权属性质", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
        // 单元格2-2
        x_ = x_ + w * 1 / 5;
        Envelope cel_3_2 = new Envelope(x_, y_, x_ + w * 3 / 10, y_ - h, p_extend.getSpatialReference());
        //qllx
        String dicQlType = DicUtil.dic("qllx", FeatureHelper.Get(f_zd, "QLLX", ""));
        if (!TextUtils.isEmpty(dicQlType)&&dicQlType.contains("]")){
            dicQlType=StringUtil.substr(dicQlType,dicQlType.lastIndexOf("]")+1);
        }
        dxf.write(cel_3_2, null,dicQlType, o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);

        // 单元格2-3
        x_ = x_ + w * 3 / 10;
        Envelope cel_3_3 = new Envelope(x_, y_, x_ + w * 1 / 5, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_3_3, null, "登记面积", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);

        // 单元格2-4
        x_ = x_ + w * 1 / 5;
        Envelope cel_3_4 = new Envelope(x_, y_, x+w, y_ - h, p_extend.getSpatialReference());
        double dsyqmj = FeatureHelper.Get(f_zd,"SYQMJ",0.00);
        String syqmj =dsyqmj==0.00d?"/":String.format("%.2f",dsyqmj);
        dxf.write(cel_3_4, null, syqmj, o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
        Envelope cel_4_1 = new Envelope(x, y_ - h, x + w, y - p_height, spatialReference);
        dxf.write(cel_4_1);

        final double buffer = DxfHelper.getBufferDistance();
        Geometry cutGeometey = GeometryEngine.buffer(f_zd.getGeometry(), buffer);
        paint.setCutArea(cutGeometey);

        dxf.write(mapInstance, fs_all);
        dxf.writeJZDS(fs_jzd);
        // 宗地四至
        DxfHelper.writeZdsz(dxf,f_zd,f_zd.getGeometry().getExtent(),o_split,0.8f,o_fontstyle);
        paint.setTextAlign(DxfPaint.Align.RIGHT);
        Point p_z = new Point(x + w, p_extend.getYMin() + h * 0.5, spatialReference);
        dxf.writeText(p_z, "注：实际建筑占地面积为" + Get(f_zd, "JZZDMJ", 0.00) + "平方米");

    }

    @Override
    protected void getFooter() throws Exception {
        paint.setLayer(DxfConstant.DXF_LAYER_BKZJ);
        Envelope envelope = p_extend;
        double x = envelope.getXMin();
        double y = envelope.getYMax();
        double x_ = p_extend.getXMin();
        double y_ = y - p_height;
        double w = p_width;
        String hzdw = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZDW", "");
        Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
        Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
        dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw, "\n"), o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);

        Calendar c = Calendar.getInstance();
        String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH) + "日");
        c.add(Calendar.DATE, -1);
        String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH) + "日");
        Point p_jxf = new Point(x, envelope.getYMin() - h * (o_fontsize * 1.5), envelope.getSpatialReference());
        String tjsj = StringUtil.substr(auditDate, 0, auditDate.indexOf("月") + 1); // 图解时间
        dxf.writeText(p_jxf, tjsj + "解析法测绘界址点", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_BYLAYER, paint.getLayer(), paint.getStbm());
        Point p_blc = new Point(envelope.getCenter().getX(), envelope.getYMin() - h * 0.5, envelope.getSpatialReference());
        dxf.writeText(p_blc, "1:" + (int) blc, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, paint.getLayer(), paint.getStbm());


        String hzr = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZR", "");
        String shr = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "SHR", "");

        if (hzr.length() > shr.length()) {
            shr = getNiceString(shr, hzr.length() - shr.length());
        } else if (hzr.length() < shr.length()) {
            hzr = getNiceString(hzr, shr.length() - hzr.length());
        }

        Point p_chr = new Point(x + w -w/8 , envelope.getYMin() - h * o_fontsize, envelope.getSpatialReference());
        dxf.writeText(p_chr, "制图：" + hzr, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, null, null);
        Point p_shr = new Point(x + w-w/8, envelope.getYMin() - h * (o_fontsize * 2), envelope.getSpatialReference());
        dxf.writeText(p_shr, "审核：" + shr, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, null, null);
        Point p_auditDate = new Point(x + w-w/10  , envelope.getYMin() - h * o_fontsize , envelope.getSpatialReference());
        dxf.writeText(p_auditDate, "" + auditDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_BYLAYER, paint.getLayer(), paint.getStbm());
        Point p_drawDate = new Point(x + w-w/10 , envelope.getYMin() -h * o_fontsize * 2, envelope.getSpatialReference());
        dxf.writeText(p_drawDate, "" + drawDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_BYLAYER, paint.getLayer(), paint.getStbm());

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
}
