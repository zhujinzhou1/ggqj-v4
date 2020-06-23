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
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.LineLabel;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.AppConfig;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfHelper;
import com.ovit.app.util.gdal.cad.DxfTemplet;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.ovit.app.util.gdal.cad.DxfHelper.FONT_STYLE_HZ;

public class DxfFwqjxsyt_tongshan extends BaseDxf {

    private String bdcdyh;
    private Feature f_zd;
    private List<Feature> fs_zrz;
    private List<Feature> fs_zd;
    private List<Feature> fs_z_fsjg;
    private List<Feature> fs_h_fsjg;
    private List<Feature> fs_jzd;
    private List<Feature> fs_fsjg;
    private List<Feature> fs_all;
    private List<Feature> fs_hAndFs;
    private int o_fontcolor = DxfHelper.COLOR_BYLAYER;// 图框颜色

    public DxfFwqjxsyt_tongshan(MapInstance mapInstance) {
        super(mapInstance);
    }

    @Override
    public DxfFwqjxsyt_tongshan set(String dxfpath) {
        super.set(dxfpath);
        return this;
    }

    public DxfFwqjxsyt_tongshan set(Feature f_zd, List<Feature> fs_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h_fsjg, List<Feature> fs_jzd
            , List<Feature> fs_zj_x, List<Feature> fs_xzdw, List<Feature> fs_mzdw) {
        List<Feature> fs_all = new ArrayList<>();
        List<Feature> fs_fsjg = new ArrayList<>();
        List<Feature> mfs_fsjg = new ArrayList<>(); // 筛选后的附属结构
        fs_all.addAll(fs_zd);
        fs_all.addAll(fs_zrz);

        fs_fsjg.addAll(fs_h_fsjg);
        fs_fsjg.addAll(fs_z_fsjg);
        this.fs_fsjg = fs_fsjg;
        mfs_fsjg = getFilterFsjg(fs_fsjg);

        fs_all.addAll(mfs_fsjg);
        fs_all.addAll(fs_jzd);
        fs_all.addAll(fs_zj_x);
        fs_all.addAll(fs_xzdw);
        fs_all.addAll(fs_mzdw);
        this.f_zd = f_zd;
        this.fs_zrz = fs_zrz;
        this.fs_zd = fs_zd;
        this.fs_z_fsjg = fs_z_fsjg;
        this.fs_h_fsjg = fs_h_fsjg;
        this.fs_jzd = fs_jzd;
        this.fs_all = fs_all;

        return this;
    }

    @Override
    protected void getDxfRenderer() {
        dxf.setFlagLable2ZD(true);
        dxf.setFlagLable2JZD(true);
        dxf.setFlagLable2ZRZ(true);
        dxf.setZDDM(mapInstance.getId(f_zd));
    }


    @Override
    protected void getHeader() throws Exception {
        //        getDefultHeader();
        Envelope envelope = p_extend;
        dxf.writetk(new Envelope(envelope.getCenter(), boxWidth, boxHeight),DxfHelper.LINETYPE_SOLID_LINE,"",o_fontsize,DxfHelper.FONT_STYLE_HZ,false,DxfHelper.COLOR_WHITE,DxfHelper.LINE_WIDTH_DEFULT); // 图框

        //  图形范围
        dxf.write(p_extend, DxfHelper.LINETYPE_SOLID_LINE, "", o_fontsize, FONT_STYLE_HZ, false, DxfHelper.COLOR_BYLAYER, DxfHelper.LINE_WIDTH_3);
        Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() - o_split * 0.5, envelope.getSpatialReference());
        dxf.writeText(p_title, "房屋权界线示意图", o_fontsize * 12 / 5, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

        Envelope line = new Envelope(envelope.getXMin(), envelope.getYMax(), envelope.getXMax(), envelope.getYMax() - o_split * 1.2, p_extend.getSpatialReference());
        dxf.write(line, null, "", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

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

       /* final double buffer = DxfHelper.getBufferDistance();
        Geometry extentBuffer = GeometryEngine.buffer(f_zd.getGeometry(), buffer);
        dxf.write(mapInstance, fs_all, DxfHelper.TYPE, extentBuffer, null, null, new LineLabel(), DxfHelper.LINE_LABEL_OUTSIDE, "");
        DxfHelper.writeZdsz(dxf, f_zd, o_extend, 0, o_fontsize, o_fontstyle);

*/

        dxf.write(mapInstance, fs_all);
        Envelope cel_4_2 = new Envelope(x_, y_, x_ + w, y - p_height, p_extend.getSpatialReference());
//        dxf.write(cel_4_2, null, "", o_fontsize, FONT_STYLE_HZ, false, o_fontcolor, 0);
        Point p_n = new Point(cel_4_2.getXMax() - o_split, cel_4_2.getYMax() - o_split * 1.5);
        dxf.write(p_n, null, "北", o_fontsize, FONT_STYLE_HZ, false, o_fontcolor, 0);
        writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split);
        // 宗地四至
        DxfHelper.writeZdsz(dxf,f_zd,f_zd.getGeometry().getExtent(),o_split,o_fontsize,FONT_STYLE_HZ);
        // 单元格4-0  绘制单位
        x_ = x;
        y_ = y - p_height + 2 * h;
        SharedPreferences sp = mapInstance.map.getContext().getSharedPreferences("name", MODE_PRIVATE);
        String auditDate = getRQ(sp.getString("date", ""));
        String drawDate = getRQ(sp.getString("date", ""));

        String drawPerson = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZR", "");
        String auditPerson = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "SHR", "");
        if (drawPerson.length() < 3) {
            drawPerson = getHZR(drawPerson);
        }
        if (auditPerson.length() < 3) {
            auditPerson = getHZR(auditPerson);
        }

        Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_1_1, null, "丈量者", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
        // 1-2
        x_ = x_ + w * 1 / 6;
        Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_1_2, null, drawPerson, o_fontsize, o_fontstyle, false, o_fontcolor, 0);

        // 1-3
        x_ = x_ + w * 1 / 6;
        Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_1_3, null, "丈量日期", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
        x_ = x_ + w * 1 / 8;
        Envelope cel_1_4 = new Envelope(x_, y_, x_ + w * 5 / 24, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_1_4, null, drawDate, o_fontsize, o_fontstyle, false, o_fontcolor, 0);

        // 1-3
        x_ = x_ + w * 5 / 24;
        Envelope cel_1_5 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - 2 * h, p_extend.getSpatialReference());
        dxf.write(cel_1_5, null, "概略比例尺", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
        x_ = x_ + w * 1 / 6;
        Envelope cel_1_6 = new Envelope(x_, y_, x + w, y_ - 2 * h, p_extend.getSpatialReference());
        dxf.write(cel_1_6, null, "1:" + (int) blc, o_fontsize, o_fontstyle, false, o_fontcolor, 0);

        x_ = x;
        y_ = y_ - h;

        Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
//                dxf.write(cel_1_1,"不动产单元号");
        dxf.write(cel_2_1, null, "检查者", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
        // 1-2
        x_ = x_ + w * 1 / 6;
        Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_2_2, null, auditPerson, o_fontsize, o_fontstyle, false, o_fontcolor, 0);

        // 1-3
        x_ = x_ + w * 1 / 6;
        Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_2_3, null, "检查日期", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
        x_ = x_ + w * 1 / 8;
        Envelope cel_2_4 = new Envelope(x_, y_, x_ + w * 5 / 24, y_ - h, p_extend.getSpatialReference());
        dxf.write(cel_2_4, null, auditDate, o_fontsize, o_fontstyle, false, o_fontcolor, 0);

        Point point = new Point(x + w * 21 / 24, y - p_height + 2.5 * h);
        dxf.write(point, null, "说明：图中单位为米。", o_fontsize, o_fontstyle, false, o_fontcolor, 0);

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
        Geometry buffer = GeometryEngine.buffer(f_zd.getGeometry(),5);
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
//        dxf.write(new Polygon(ps, p.getSpatialReference()), null);
        dxf.write(new Polygon(ps, p.getSpatialReference()), null, "", o_fontsize, o_fontstyle, false, o_fontcolor, 0);
    }


    public String getHZR(String s) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0) {
                result.append("  ");
            }
            result.append(s.charAt(i));
        }
        return result.toString();
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
