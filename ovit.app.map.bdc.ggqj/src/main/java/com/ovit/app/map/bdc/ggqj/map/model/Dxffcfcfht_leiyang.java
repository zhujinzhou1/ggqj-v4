package com.ovit.app.map.bdc.ggqj.map.model;

import android.text.TextUtils;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.ImmutablePart;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.ovit.app.map.bdc.ggqj.map.BaseDxf;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditBDC;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZRZ;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfHelper;
import com.ovit.app.util.gdal.dxf.DxfConstant;
import com.ovit.app.util.gdal.dxf.DxfPaint;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * 耒阳房产分层图
 */

public class Dxffcfcfht_leiyang extends BaseDxf {
    private List<Feature> fs_all;
    private List<Feature> fs_hAndFs;
    private Feature f_zd;
    private List<Feature> fs_zrz;
    private List<Feature> fs_c;
    private String bdcdyh;
    private float alpha = 0.001f;

    public Dxffcfcfht_leiyang(MapInstance mapInstance) {
        super(mapInstance);
    }

    @Override
    public Dxffcfcfht_leiyang set(String dxfpath) {
        super.set(dxfpath);
        return this;
    }

    public Dxffcfcfht_leiyang set(String bdcdyh, Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_c, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_h_fsjg) {
        this.bdcdyh = bdcdyh;
        this.f_zd = f_zd;
        fs_hAndFs = new ArrayList<>();
        fs_hAndFs.addAll(fs_z_fsjg);
        fs_hAndFs.addAll(fs_h);
        fs_hAndFs.addAll(fs_h_fsjg);
        this.fs_zrz = fs_zrz;
        this.fs_c = fs_c;
        fs_all = new ArrayList<>();
        fs_all.addAll(fs_zrz);
        fs_all.addAll(fs_hAndFs);
        return this;
    }

    @Override
    protected void getDxfRenderer() {
        paint.setLayer(DxfConstant.DXF_LAYER_BKZJ);
    }

    @Override
    protected void getHeader() throws Exception {
//        getDefultHeader();
    }

    @Override
    protected void getBody() throws Exception {
        ArrayList<Map.Entry<String, List<Feature>>> fs_map_croup = FeatureViewZRZ.GroupbyC_Sort(fs_hAndFs);
        try {
            int page_count = (int) Math.ceil(fs_map_croup.size() / 2f);  //  多少页
            String fwjg = null;
            if (FeatureHelper.isExistElement(fs_zrz)){
                fwjg = FeatureViewZRZ.GetSfwjg(fs_zrz.get(0));
            }
            for (int page = 1; page < page_count + 1; page++) {
                List<C> cs = new ArrayList<>();
                int i1 = 1 + (page - 1) * 2;
                for (; i1 <= page * 2; i1++) {
                    if (i1 <= fs_map_croup.size()) {
                        Map.Entry<String, List<Feature>> fs_c_map = fs_map_croup.get(i1 - 1);
                        List<Feature> fs_c = fs_c_map.getValue();
                        if (fs_c != null && fs_c.size() > 0) {
                            C c = new C(fs_c_map.getKey(), fs_c_map.getValue(), fs_zrz);
                            cs.add(c);
                        }
                    }
                }

                Envelope envelope = getPageExtend(page);
                Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
                paint.setFontsize(o_fontsize * 2);
                dxf.writeText(p_title, "房产分户图", paint);

                double w = p_width; //行宽
                // 左上角
                double x = envelope.getXMin();
                double y = envelope.getYMax();

                double x_ = x;
                double y_ = y;
                // 单元格1-1
                Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                paint.setFontsize(o_fontsize);
                dxf.write(cel_1_1, "宗地代码", paint);
//                dxf.write(cel_1_1, null, "宗地代码", o_fontsize, null, false, DxfHelper.COLOR_BYLAYER, 0);
                // 单元格1-2
                x_ = x_ + w * 2 / 15;
                Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_2, Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, ""), paint);
                // 单元格1-3
                x_ = x_ + w * 4 / 15;
                Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_3, "结  构", paint);
                // 单元格1-4
                x_ = x_ + w * 2 / 15;
                Envelope cel_1_4 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_4, fwjg, paint);
                // 单元格1-5
                x_ = x_ + w * 2 / 15;
                Envelope cel_1_5 = new Envelope(x_, y_, x_ + w * 5 / 24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_5, "专有建筑面积(㎡)", paint);
                // 单元格1-6
                x_ = x_ + w * 5 / 24;
                Envelope cel_1_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_1_6, "/", paint);

                // 单元格2-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_1, "幢    号", paint);
                // 单元格2-2
                x_ = x_ + w * 2 / 15;
                Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                String zh = fs_zrz.size() > 1 ? "F9999" : "F" + FeatureHelper.Get(fs_zrz.get(0), "ZH", "0001");
                dxf.write(cel_2_2, "F0001", paint);

                // 单元格2-3
                x_ = x_ + w * 4 / 15;
                Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_3, "总层数", paint);

                // 单元格2-4
                x_ = x_ + w * 2 / 15;
                Envelope cel_2_4 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_4, fs_map_croup.size() + "", paint);
                // 单元格2-5
                x_ = x_ + w * 2 / 15;
                Envelope cel_2_5 = new Envelope(x_, y_, x_ + w * 5 / 24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_5, "共有建筑面积(㎡)", paint);

                // 单元格2-6
                x_ = x_ + w * 5 / 24;
                Envelope cel_2_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_2_6, "/", paint);

                // 单元3-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_3_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_1, "房屋坐落", paint);
                // 单元格3-2
                x_ = x_ + w * 2 / 15;
                Envelope cel_3_2 = new Envelope(x_, y_, x_ + w * 8 / 15, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_2, Get(f_zd, "ZL", ""), paint);
                //3-3
                x_ = x_ + w * 8 / 15;
                Envelope cel_3_3 = new Envelope(x_, y_, x_ + w *  5 / 24, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_3, "建筑面积(㎡)", paint);

                //3-4
                x_ = x_ + w *  5 / 24;
                Envelope cel_3_4 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_3_4, String.format("%.2f", FeatureHelper.Get(f_zd, "JZMJ", 0.00d)), paint);

                // 单元格4-1
                x_ = x;
                y_ = y_ - h;
                Envelope cel_5_1 = new Envelope(x_, y_, x_ + w, y_ - p_height + 4 * h, p_extend.getSpatialReference());
                dxf.write(cel_5_1, "", paint);

//               double height = (p_height-4*h)/2;
                double height = (p_height - 8 * h) / 2;
                Envelope cel_3_1_1 = new Envelope(x_, y_, x_ + w, y_ - height, p_extend.getSpatialReference());
                if (cs.size() == 1) {
                    cel_3_1_1 = new Envelope(x_, y_, x_ + w, y - p_height+4*h, p_extend.getSpatialReference());
                }
                writeC(cs, 0, cel_3_1_1);
//
                // 单元格3-2
                y_ = y_ - height-h;
                Envelope cel_3_2_2 = new Envelope(x_, y_, x + w, y_ - height-h, p_extend.getSpatialReference());
                writeC(cs, 1, cel_3_2_2);

                Point p_n = new Point(cel_5_1.getXMax() - o_split, cel_5_1.getYMax() - 1.5*o_split);
                writeN(p_n, o_split, -alpha);
                //  绘制单位
                x_ = x;
                y_ = y - p_height;
                String hzdw = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZDW", "");
                Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize * 1.26f) * 1.8, x_, y_, p_extend.getSpatialReference());
                Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
                dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw, "\n"), paint);
                // 测绘日期
//                Calendar c = Calendar.getInstance();
//                c.add(Calendar.DATE, -1);
//                String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH) + "日");

                // 测绘日期
                String strRight = "测绘日期：2020年7月20日" ;
                Point p_auditDate = new Point(envelope.getXMin() + w * 9 / 12, envelope.getYMin() - o_split * 0.2);
                paint.setTextAlign(DxfPaint.Align.LEFT);
                dxf.writeText(p_auditDate, strRight, paint);
                //比例尺
                Point p_blc = new Point(envelope.getCenter().getX(), envelope.getYMin() - o_split * 0.2, envelope.getSpatialReference());
                paint.setFontsize(o_fontsize * 1.5f);
                paint.setTextAlign(DxfPaint.Align.CENTER);
                dxf.writeText(p_blc, "1:" + (int) blc, paint);
            }

        } catch (Exception es) {
            dxf.error("生成失败", es, true);
        }

    }


    @Override
    protected void getFooter() throws Exception {

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
        Geometry g_zd = f_zd.getGeometry();
        alpha = (float) (Math.PI * MapHelper.geometry_get_azimuth(g_zd) / 180);
        Point p_b = MapHelper.Geometry_get(g_zd, DxfHelper.POINT_TYPE_RIGHT_BOTTOM);
        Geometry g = MapHelper.geometry_get(g_zd, p_b, -alpha);

        Envelope extent = GeometryEngine.buffer(g,2).getExtent();
        double width = extent.getWidth();
        double height = extent.getHeight();
        extent = new Envelope(extent.getCenter(), width, height * 2);
        return MapHelper.geometry_get(extent, spatialReference);
    }

    @Override
    public double getPictureBoxBufferFactor() {
        return 0.15;
    }

    public class C {
        public static final String HALF_AREA = "0.5";
        String lc;
        List<Feature> fs;
        List<Feature> fs_zrz;
        String fs_zf;
        private double cjzmj = 0d;

        public C(String lc, List<Feature> fs, List<Feature> fs_zrz) {
            this.lc = lc;
            this.fs = fs;
            this.fs_zrz = fs_zrz;
            init();
        }

        private void init() {
            List<Feature> featureHs = new ArrayList<>();
            List<Feature> halfAreaFsjc = new ArrayList<>();
            List<Feature> wholeAreaFeatures = new ArrayList<>();
            for (Feature f : fs) {
                FeatureTable featureTable = f.getFeatureTable();
                if (f != null) {
                    featureHs.add(f);
                }
                // 半面积 附属结构
                if (featureTable == mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG) || featureTable == mapInstance.getTable(FeatureHelper.LAYER_NAME_Z_FSJG)) {
                    if (HALF_AREA.equals(FeatureHelper.Get(f, "TYPE", ""))) {
                        halfAreaFsjc.add(f);// 半面积
                    } else if (featureTable != mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG)) {
                        wholeAreaFeatures.add(f);
                    }
                } else {
                    wholeAreaFeatures.add(f);
                }
                cjzmj = FeatureViewZRZ.hsmj_jzmj(featureHs);
            }
        }

        public String getName() {
            String zh = "0001";
            int zh_i = AiUtil.GetValue(StringUtil.getTextOnlyIn(zh, "0123456789"), 0);
            String name = zh;
            if (zh_i > 0) {
                name = zh_i + "";
            }
            name = name.replace("幢", "") + "幢:" + lc.replace("层", "") + "层";
            return name;
        }

        public double getJzmj() {
            List<Feature> featureHs = new ArrayList<>();
            for (Feature f : fs) {
                if (f != null && f.getFeatureTable() != mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG)) {
                    featureHs.add(f);
                }
            }
            return FeatureViewZRZ.hsmj_jzmj(featureHs);
        }

        public String getJzcl() {
            String jzcl = "";
            if (TextUtils.isEmpty(jzcl)) {
                List<String> fwjgs = new ArrayList<>();
                for (Feature f : fs_zrz) {
                    String fwjg = DicUtil.dic("fwjg", FeatureHelper.Get(f, "FWJG", "4"));
                    String jzwmc = FeatureHelper.Get(f, "JZWMC", "其他");
                    if (!TextUtils.isEmpty(fwjg) && jzwmc.contains("主房")) {
                        if (fwjg.contains("结构")) {
                            fwjg = fwjg.substring(fwjg.lastIndexOf("]") + 1, fwjg.lastIndexOf("结构"));
                        } else {
                            fwjg = fwjg.substring(fwjg.lastIndexOf("]") + 1);
                        }
                        if (!fwjgs.contains(fwjg)) {
                            fwjgs.add(fwjg);
                        }
                    }
                }
                if (fwjgs.size() > 0) {
                    jzcl = StringUtils.join(fwjgs, "/");
                } else {
                    jzcl = "其他";
                }
            }
            return jzcl;
        }

        public String getZrzhZhuFang() {
            String jzcl = "";
            if (TextUtils.isEmpty(jzcl)) {
                List<String> zrzh_zf = new ArrayList<>(); // 主房幢号
                for (Feature f : fs_zrz) {
                    String jzwmc = FeatureHelper.Get(f, "JZWMC", "其他");
                    if (jzwmc.contains("主房")) {
                        zrzh_zf.add(FeatureHelper.Get(f, "ZRZH", ""));
                    }
                }
                jzcl = StringUtils.join(zrzh_zf, "/");
            }
            return jzcl;
        }
    }

    private void writeC(List<C> cs, int index, Envelope cell) throws Exception {
        if (index < cs.size()) {
            C c = cs.get(index);
            Map<String, String> map_cg = FeatureEditBDC.GetCGbyZrzOrid(mapInstance.getOrid(fs_zrz.get(0)), fs_c);
            GeodeticDistanceResult d_move = null;
            List<Feature> fs = c.fs;
            Envelope e = MapHelper.geometry_combineExtents_Feature(fs);
            e = MapHelper.geometry_get(e, spatialReference); //  图形范围
            double jzmj = c.getJzmj();
            String lc = c.lc;
            if (!MapHelper.geometry_equals(e.getCenter(), cell.getCenter())) {
                d_move = GeometryEngine.distanceGeodetic(e.getCenter(), cell.getCenter(), MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
            }
            List<Feature> fs_clone = MapHelper.cloneFeature(fs);
            List<Feature> fs_m = MapHelper.features_get(fs_clone, MapHelper.getSpatialReference(mapInstance));  //拷贝原数据后，整体转换坐标系，坐标系统一

            Point p_b = MapHelper.Geometry_get(fs_m.get(0).getGeometry(), DxfHelper.POINT_TYPE_RIGHT_BOTTOM);
            List<Feature> fs_ = MapHelper.geometry_get(fs_m, p_b, -alpha);                                    //先统一坐标系，后计算旋转后的坐标

            Point p = new Point(cell.getXMin() + cell.getWidth() / 2, cell.getYMin() - h);
            String text = c.getName() + "平面图"+"([#CG#]m)";
            try {
                text=text.replace("[#CG#]",map_cg.get(lc));

            }catch (Exception e1){

            }
            dxf.write(p, text, paint);
            List<Feature> fs_h = new ArrayList<>();
            for (Feature f : fs_) {
                if (FeatureHelper.TABLE_NAME_H.equals(f.getFeatureTable().getTableName())) {
                    fs_h.add(f);
                } else {
                    dxf.writeFSJG(mapInstance, f, "", null, paint.getLineLabel(), d_move, DxfHelper.LINE_LABEL_OUTSIDE, 0, true);
                }
            }
            // 绘制合逻辑幢后的户
            List<Geometry> gs = MapHelper.geometry_get(fs_h);
            Geometry g = GeometryEngine.union(gs);
            g = MapHelper.geometry_move(g, d_move);

            if (g instanceof Polygon) {
                for (ImmutablePart segments : ((Polygon) g).getParts()) {
                    Polygon polygon = new Polygon(new PointCollection(segments.getPoints()));
                    Point p_c = GeometryEngine.labelPoint(polygon);
                    Point p_jzmj = new Point(p_c.getX(), p_c.getY() + paint.getFontsize());
                    dxf.writeMText(p_jzmj, String.format("%.2f", jzmj), paint);
                    List<Point> ps = Arrays.asList(new Point[]{new Point(p_c.getX() - 2 * paint.getFontsize(), p_c.getY()), new Point(p_c.getX() + 2 * paint.getFontsize(), p_c.getY())});
                    dxf.writeLine(ps, "", false, DxfHelper.COLOR_BYLAYER, 0, "JZD", "302002");
                    Point p_lc = new Point(p_c.getX(), p_c.getY() - paint.getFontsize());
                    dxf.writeMText(p_lc, lc, paint);
                    dxf.write(MapHelper.geometry_trim(polygon)
                            , paint.getLineType()
                            , ""
                            , paint.getFontstyle(), paint.getFontsize()
                            , paint.getFontWidth()
                            , paint.getColor(), paint.getLinewidth()
                            , true
                            , 1
                            , paint.getLineLabel()
                            , paint.getLayer()
                            , paint.getStbm());
                }
            }



            }


    }


}
