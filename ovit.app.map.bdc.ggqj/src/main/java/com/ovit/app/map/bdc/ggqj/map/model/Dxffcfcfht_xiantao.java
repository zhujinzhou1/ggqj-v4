package com.ovit.app.map.bdc.ggqj.map.model;

import android.text.TextUtils;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.ovit.app.map.bdc.ggqj.map.BaseDxf;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditC;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZRZ;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.AppConfig;
import com.ovit.app.util.ArabicToChineseUtils;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfHelper;
import com.ovit.app.util.gdal.dxf.DxfConstant;
import com.ovit.app.util.gdal.dxf.DxfPaint;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * 仙桃房产分层图
 */

public class Dxffcfcfht_xiantao extends BaseDxf {
    private List<Feature> fs_all;
    private List<Feature> fs_hAndFs;
    private Feature f_zd;
    private List<Feature> fs_zrz;
    private List<Feature> fs_c;
    private String bdcdyh;
    private float alpha = 0.001f;

    public Dxffcfcfht_xiantao(MapInstance mapInstance) {
        super(mapInstance);
    }

    @Override
    public Dxffcfcfht_xiantao set(String dxfpath) {
        super.set(dxfpath);
        return this;
    }

    public Dxffcfcfht_xiantao set(String bdcdyh, Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_c, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_h_fsjg) {
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
        if (AppConfig.PHSZ_IMG_ADJUST_UPRIGHT.equals(AppConfig.get(AppConfig.APP_BDCQJDC_PHSZ_IMG_ADJUST, AppConfig.PHSZ_IMG_ADJUST_BASIC))) {
            alpha = (float) (Math.PI * MapHelper.geometry_get_azimuth(f_zd.getGeometry()) / 180);
        }
        ArrayList<Map.Entry<String, List<Feature>>> fs_map_croup = FeatureViewZRZ.GroupbyC_Sort(fs_hAndFs);
        try {

            int item = 3;
            int page_count = (int) Math.ceil(fs_map_croup.size() / (float) item);  //  多少页
            for (int page = 1; page < page_count + 1; page++) {
                List<String[]> listZrzData = getListZrzData(fs_zrz, page, item);
                String fwjg = "";
                List<C> cs = new ArrayList<>();
                int i1 = 1 + (page - 1) * item;
                for (; i1 <= page * item; i1++) {
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
                paint.setFontsize(o_fontsize * 2);
//              dxf.write(new Envelope(envelope.getCenter(), p_width + 0.6, p_height + 0.6)); // 图框
                paint.setTextAlign(DxfPaint.Align.CENTER);
                Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 0.5 + o_fontsize, envelope.getSpatialReference());
                dxf.writeMText(p_title, "房产分层分户图", paint);
                paint.setFontsize(o_fontsize * 0.8f);
                paint.setTextAlign(DxfPaint.Align.RIGHT);
                Point p_unit = new Point(envelope.getXMax(), envelope.getYMax() + o_fontsize, envelope.getSpatialReference());
                dxf.writeMText(p_unit, "单位：m·㎡ ", paint);

                double w = p_width; //行宽
                // 左上角
                double x = envelope.getXMin();
                double y = envelope.getYMax();

                double x_ = x;
                double y_ = y;

                Envelope cel_0 = envelope;
                dxf.write(cel_0, "", paint);

                paint.setFontsize(o_fontsize);
                Envelope cel_3_1_1 = new Envelope(x_, y_, x_ + w / 2, y_ - p_height * 2 / 3, spatialReference);
                dxf.write(cel_3_1_1, "", paint);
                writeC(cs, 0, cel_3_1_1);
//
                // 单元格3-2
                x_ = x_ + w / 2;
                Envelope cel_3_2_2 = new Envelope(x_, y_, x + w, y_ - p_height * 2 / 3, spatialReference);
                dxf.write(cel_3_2_2, "", paint);
                writeC(cs, 1, cel_3_2_2);

                // 单元格3-2
                x_ = x;
                y_ = y_ - p_height * 2 / 3;
                Envelope cel_4_1_1 = new Envelope(x_, y_, x_ + w / 2, y - p_height, spatialReference);
                writeC(cs, 2, cel_4_1_1);
                dxf.write(cel_4_1_1, "", paint);
                Envelope cel_4_2_2 = new Envelope(x_ + w / 2, y_, x_ + w, y - p_height, spatialReference);
                dxf.write(cel_4_2_2, "", paint);
                dxf.write(cel_4_1_1, "", paint);


                x_ = x + w / 2;
                y_ = y - p_height;

                // 5
                Envelope cel_5_1_1 = new Envelope(x_, y_, x_ + w / 12, y_ + h, spatialReference);
                dxf.write(cel_5_1_1, "产权人", paint);

                x_ = x_ + w / 12;
                Envelope cel_5_1_2 = new Envelope(x_, y_, x_ + w / 6, y_ + h, spatialReference);
                dxf.write(cel_5_1_2, FeatureHelper.Get(f_zd, "QLRXM", ""), paint);

                x_ = x_ + w / 6;
                Envelope cel_5_1_3 = new Envelope(x_, y_, x_ + w / 12, y_ + h, spatialReference);
                dxf.write(cel_5_1_3, "宗地代码", paint);

                x_ = x_ + w / 12;
                Envelope cel_5_1_4 = new Envelope(x_, y_, x + w, y_ + h, spatialReference);
                String zddm = FeatureHelper.Get(f_zd, "ZDDM", "");
                if (FeatureHelper.isZDDMHValid(zddm)) {
                    zddm = StringUtil.substr_last(zddm, 13);
                }
                dxf.write(cel_5_1_4, zddm, paint);
                //6
                x_ = x + w / 2;
                y_ = y_ + h;
                Envelope cel_6_1_1 = new Envelope(x_, y_, x_ + w / 12, y_ + h, spatialReference);
                dxf.write(cel_6_1_1, "坐落", paint);

                x_ = x_ + w / 12;
                Envelope cel_6_1_2 = new Envelope(x_, y_, x_ + w / 4, y_ + h, spatialReference);
                dxf.write(cel_6_1_2, FeatureHelper.Get(f_zd, "ZL", ""), paint);

                x_ = x_ + w / 4;
                Envelope cel_6_1_3 = new Envelope(x_, y_, x_ + w * 5 / 48, y_ + h, spatialReference);
                dxf.write(cel_6_1_3, "总建筑面积", paint);

                x_ = x_ + w * 5 / 48;
                Envelope cel_6_1_4 = new Envelope(x_, y_, x + w, y_ + h, spatialReference);
                String jzmj = String.format("%.2f", FeatureHelper.Get(f_zd, "JZMJ", 0.00));
                dxf.write(cel_6_1_4, jzmj, paint);

                // 自然幢信息
                Envelope cel_i_j = null;
                int size = listZrzData.size();
                y_ = y_ + h + size * h + h;
                for (int i = 0; i < size; i++) {
                    x_ = x + w / 2;
                    y_ = y_ - h;
                    String[] z = listZrzData.get(i);
                    for (int j = 0; j < z.length; j++) {
                        cel_i_j = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                        if (z != null) {
                            try {
                                if (j == z.length - 1) {
                                    cel_i_j = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                                }
                                dxf.write(cel_i_j, z[j], paint);

                            } catch (Exception e) {
                                dxf.write(cel_i_j, "", paint);
                            }
                        } else {
                            dxf.write(cel_i_j, "", paint);
                        }
                        x_ = x_ + w / 12;

                    }
                }

                //7
                x_ = x + w / 2;
                y_ = y - p_height + 2 * h + size * h + h;
                Envelope cel_7_1_1 = new Envelope(x_, y_, x_ + w / 12, y_ - h, spatialReference);
                dxf.write(cel_7_1_1, "", paint);

                dxf.writeLine(Arrays.asList(new Point[]{new Point(x_, y_ - h / 2), new Point(x_ + w / 12, y_ - h)}), "", false, 0, 0);
                dxf.writeLine(Arrays.asList(new Point[]{new Point(x_ + w / 24, y_), new Point(x_ + w / 12, y_ - h)}), "", false, 0, 0);
                // 写文字
                Envelope cel_7_1_1_1 = new Envelope(x_, y_ - h * 3 / 5, x_ + w / 24, y_ - h, p_extend.getSpatialReference());
                Envelope cel_7_1_1_2 = new Envelope(x_, y_, x_ + w / 24, y_ - h / 2, p_extend.getSpatialReference());
                Envelope cel_7_1_1_3 = new Envelope(x_ + w * 3 / 48, y_, x_ + w / 12, y_ - h / 2, p_extend.getSpatialReference());
                paint.setFontsize(o_fontsize * 0.5f);
                dxf.writeMText(cel_7_1_1_1.getCenter(), "幢号", paint);
                dxf.writeMText(cel_7_1_1.getCenter(), "层面积", paint);
                dxf.writeMText(cel_7_1_1_3.getCenter(), "层次", paint);
                paint.setFontsize(o_fontsize);

                x_ = x_ + w / 12;
                Envelope cel_7_1_2 = new Envelope(x_, y_, x_ + w / 12, y_ - h, spatialReference);
                String c1 = ArabicToChineseUtils.formatFractionalPart(1 + ((page - 1) * item));
                dxf.write(cel_7_1_2, c1, paint);

                x_ = x_ + w / 12;
                Envelope cel_7_1_3 = new Envelope(x_, y_, x_ + w / 12, y_ - h, spatialReference);
                String c2 = ArabicToChineseUtils.formatFractionalPart(2 + ((page - 1) * item));
                dxf.write(cel_7_1_3, c2, paint);

                x_ = x_ + w / 12;
                Envelope cel_7_1_4 = new Envelope(x_, y_, x_ + w / 12, y_ - h, spatialReference);
                String c3 = ArabicToChineseUtils.formatFractionalPart(3 + ((page - 1) * item));
                dxf.write(cel_7_1_4, c3, paint);

                x_ = x_ + w / 12;
                Envelope cel_7_1_5 = new Envelope(x_, y_, x + w, y_ - h, spatialReference);
                dxf.write(cel_7_1_5, "幢建筑面积", paint);

                // 绘制单位绘制人
                Point p_n = new Point(cel_0.getXMax() - o_split, cel_0.getYMax() - 1.5 * o_split);
                writeN(p_n, o_split, -alpha);
                //  绘制单位
                x_ = x;
                y_ = y - p_height;
//                String hzdw = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZDW", "");
                String hzdw ="仙桃市自然资源和规划局";
                if (DxfHelper.TYPE == DxfHelper.TYPE_ANQIU){
                    hzdw ="安丘市自然资源和规划局";
                }
                Point p_4_0 = new Point(x - o_fontsize, y - p_height, spatialReference);
                paint.setTextAlign(DxfPaint.Align.CENTER_BOTTOM);
                paint.setFontsize(o_fontsize);
                dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw, "\n"), paint);
                // 测绘日期
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, -1);
                String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH) + "日");
                // 测绘日期
                String strRight = "测绘日期：" + drawDate;
                y_ = envelope.getYMin() - o_fontsize*1.5;
                Point p_auditDate = new Point(x, y_);
                paint.setTextAlign(DxfPaint.Align.LEFT);
                dxf.writeText(p_auditDate, strRight, paint);
                //比例尺
                Point p_blc = new Point(envelope.getCenter().getX(), y_, spatialReference);
                paint.setTextAlign(DxfPaint.Align.CENTER);
                dxf.writeText(p_blc, "1:" + (int) blc, paint);

                // 绘图员
                String hzr = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZR", "");
                Point p_chr = new Point(x + w, y_, spatialReference);
                paint.setTextAlign(DxfPaint.Align.RIGHT);
                dxf.writeMText(p_chr, "制图员：" + hzr, paint);

            }

        } catch (Exception es) {
            dxf.error("生成失败", es, true);
        }

    }

    private  List<String[]> getListZrzData(List<Feature> fs_zrz,int page,int item) {
        List<String[]> list_z = new ArrayList<>();
        Map<String, String> map_lable = new HashMap<>();
        for (Feature f_zrz : fs_zrz) {
            String[] z = {"", "", "", "", ""};
            z[0] = FeatureHelper.Get(f_zrz, "ZH", 1) + "";
            map_lable.put("", z[1]);// 户lable
            String zrzh = Get(f_zrz, "ZRZH", "");
            List<Feature> fs = new ArrayList<>();
            for (Feature f : fs_hAndFs) {
                String id = Get(f, "ID", "");
                if (id.startsWith(zrzh)) {
                    fs.add(f);
                }
            }
            ArrayList<Map.Entry<String, List<Feature>>> fs_s = FeatureEditC.GroupbyC_Sort(fs);
            for (Map.Entry<String, List<Feature>> c : fs_s) {
                int i = fs_s.indexOf(c) + 1;
                if (i > (page - 1) * item && i <= item * page) {
                    z[i-(page - 1) * item] = String.format("%.2f", FeatureViewZRZ.hsmj_jzmj(c.getValue()));
                }
            }
            z[4] = String.format("%.2f", FeatureHelper.Get(f_zrz, "SCJZMJ", 0.00d));
            list_z.add(z);
        }
        return list_z;
    }


    @Override
    protected void getFooter() throws Exception {

    }
    @Override
    public double getHeightScale() {
        return o_extend.getHeight() / (p_height);
    }

    @Override
    public double getWidthScale() {
        return o_extend.getWidth() / p_width;
}

    @Override
    public Envelope getChildExtend() {
        Envelope extent = GeometryEngine.buffer(f_zd.getGeometry(),2).getExtent();
        double width = extent.getWidth();
        double height = extent.getHeight();
        extent = new Envelope(extent.getCenter(), width * 2, height * 2);
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
            List<Feature> fs_ = MapHelper.geometry_get(fs_m, p_b, -alpha);
            // 描述
            Point p = new Point(cell.getXMin() + cell.getWidth() / 2, cell.getYMin() + o_split / 2);
            String text = ArabicToChineseUtils.formatFractionalPart(Integer.valueOf(lc)) + "平面图";
            dxf.write(p, text, paint);
            for (Feature f_zrz : fs_zrz) {
                String lable = getLable(f_zrz);
                String orid = FeatureHelper.Get(f_zrz, FeatureHelper.TABLE_ATTR_ORID, "");
                List<Feature> fs_h = new ArrayList<>();
                // 归整数据
                for (Feature f : fs_) {
                    String f_oridPath = FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                    if (FeatureHelper.TABLE_NAME_H.equals(f.getFeatureTable().getTableName())) {
                        if ( f_oridPath.contains(orid)) {
                            fs_h.add(f);
                        }
                    } else {
                        dxf.writeFSJG(mapInstance, f, "", null, paint.getLineLabel(), d_move, DxfHelper.LINE_LABEL_OUTSIDE, 0, true);
                    }
                }

                // 绘制户  // 绘制合逻辑幢后的户
                if (FeatureHelper.isExistElement(fs_h)) {
                    List<Geometry> gs = MapHelper.geometry_get(fs_h);
                    Geometry g = GeometryEngine.union(gs);
                    g = MapHelper.geometry_move(g, d_move);
                    g = MapHelper.geometry_trim(g);
                    paint.setTextAlign(DxfPaint.Align.CENTER);
                    Point p_c = GeometryEngine.labelPoint((Polygon) g);
                    dxf.writeMText(p_c, lable, paint);

                    // 绘制幢号
                    String zrzh = FeatureHelper.Get(f_zrz, "ZRZH", "");
                    int zh = 1;
                    if (FeatureHelper.isZRZHValid(zrzh)) {
                        zh = AiUtil.GetValue(StringUtil.substr_last(zrzh, 4), 1);
                    }
                    Point last = MapHelper.Geometry_get(g, DxfHelper.POINT_TYPE_LEFT_BOTTOM);
                    if (last != null) {
                        Geometry buffer = GeometryEngine.buffer(last, o_fontsize * 2f);
                        Geometry intersectionG = GeometryEngine.intersection(g, buffer);
                        Envelope envelope = intersectionG.getExtent();
                        Point p_zh = envelope.getCenter();
                        dxf.writeMText(p_zh, "(" + zh + ")", paint);
                    }
                    dxf.write(g
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
    // 获取仙桃注记 35011993
    private String getLable(Feature f_zrz) {
        String lable = "";
        if (FeatureHelper.isPolygonFeatureValid(f_zrz)) {
            FeatureViewZRZ fv = new FeatureViewZRZ();
            fv.set(f_zrz);
            String fwjg = fv.getFwjg();
            int zcs = FeatureHelper.Get(f_zrz, "ZCS", 1);
            String jgrq = FeatureHelper.Get(f_zrz, "JGRQ", "####");
            jgrq = jgrq.substring(0, 4);
            if (TextUtils.isEmpty(fwjg) || fwjg.contains("混")) {
                fwjg = "34";
            } else if (fwjg.contains("砖")) {
                fwjg = "35";
            } else if (fwjg.contains("土")) {
                fwjg = "36";
            } else {
                fwjg = "34";
            }
            lable = fwjg + String.format("%02d", zcs) + jgrq;
        }
        return lable;
    }
}
