package com.ovit.app.map.bdc.ggqj.map.model;

import android.text.TextUtils;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
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
import com.ovit.app.util.gdal.cad.DxfTemplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * 房屋分层平面图
 * 通山
 */

public class DxfFwfcpmt_tongshan {

    MapInstance mapInstance;
    SpatialReference spatialReference;

    DxfAdapter dxf;
    private String dxfpath;
    private String bdcdyh;
    private List<Feature> fs_all;
    private List<Feature> fs_hAndFs;
    private List<Feature> fs_zAndFs;
    private Feature f_zd;
    private Point o_center;   // o 点
    private Envelope o_extend;// 真实图形的范围
    private double o_split = 2d;// 单元间隔
    private double p_width = 57.376d;// 页面宽
    private double p_height = 42.408d;// 页面高
    private double h = 0.9d; // 行高
    private float o_fontsize = 0.5f;// 字体大小
    private double scale;
    private String o_fontstyle = "宋体";// 字体
    private String fontstyle = DxfHelper.FONT_STYLE_DENGXIANTI;// 字体 等线体
    private String h_fontstyle = DxfHelper.FONT_STYLE_HEITI;// 字体 黑体
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;
    private String jgrq;
    private Envelope o_extend_1;

    public DxfFwfcpmt_tongshan(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfFwfcpmt_tongshan set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfFwfcpmt_tongshan set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfFwfcpmt_tongshan set(String bdcdyh, Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_h_fsjg) {
        this.bdcdyh = bdcdyh;
        this.f_zd = f_zd;
        this.fs_zrz = fs_zrz;
        fs_hAndFs = new ArrayList<>();

        fs_hAndFs.addAll(fs_z_fsjg);
        fs_hAndFs.addAll(fs_h);
        fs_hAndFs.addAll(fs_h_fsjg);
        // 建立副本
        List<Feature> fs_z_fsjg_clone = MapHelper.cloneFeature(fs_h_fsjg);
        List<Feature> fs_h_fsjg_clone = MapHelper.cloneFeature(fs_z_fsjg);

        this.fs_zrz = fs_zrz;
        fs_all = new ArrayList<>();
        fs_all.addAll(fs_zrz);
        fs_all.addAll(fs_hAndFs);

        fs_zAndFs = new ArrayList<>();
        fs_zAndFs.addAll(fs_zrz);
        if (fs_z_fsjg_clone != null) {
            fs_zAndFs.addAll(fs_z_fsjg_clone);
        }
        if (fs_h_fsjg_clone != null) {
            fs_zAndFs.addAll(fs_h_fsjg_clone);
        }
        return this;
    }

    // 获取范围
    public Envelope getExtend() {
        o_extend = MapHelper.geometry_combineExtents_Feature(fs_all);
        o_extend = MapHelper.geometry_get(o_extend, spatialReference); //  图形范围
        o_center = o_extend.getCenter(); // 中心点
        // 比例尺
       /* double height = o_extend.getHeight();
        double width = o_extend.getWidth();

        double v_h = height *(2+0.05)/ p_height;
        double v_w = width *(2+0.05)/ (p_width*1.3f);

        double niceScale_h = DxfHelper.getNiceScale(v_h);
        double niceScale_w = DxfHelper.getNiceScale(v_w);

        scale=niceScale_w>niceScale_h?niceScale_w:niceScale_h;
//        scale=niceScale_w;

        if (scale>1){
            p_width=p_width*scale;
            p_height=p_height*scale;
            h=h*scale;
            o_split=o_split*scale;
//            o_fontsize = (float) (o_fontsize*scale);
        }else {
            scale=1;
        }*/
        double x_min = o_center.getX() - (p_width / 2);
        double x_max = o_center.getX() + (p_width / 2);
        double y_min = o_center.getY() - (p_height / 2);
        double y_max = o_center.getY() + (p_height / 2);
        // 单元格范围
        p_extend = new Envelope(x_min, y_min, x_max, y_max, o_extend.getSpatialReference());
        return p_extend;
    }

    public class C {
        public static final String HALF_AREA = "0.5";
        String lc;
        List<Feature> fs;
        List<Feature> fs_zrz;
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
            name = name.replace("幢", "") + "幢" + lc.replace("层", "") + "层";
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

    }

    public DxfFwfcpmt_tongshan write() throws Exception {
        getExtend(); // 多大范围
        ArrayList<Map.Entry<String, List<Feature>>> fs_map_croup = FeatureViewZRZ.GroupbyC_Sort(fs_hAndFs);

        if (dxf == null) {
            dxf = new DxfAdapter();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            List<String[]> list_z = new ArrayList<>();
            Map<Integer, String[]> map_z = new HashMap<>();
            Map<String, String> map_lable = new HashMap<>();
            for (Feature f_zrz : fs_zrz) {
                String[] z = {"", "", "", "", "", "", "", "", "", "", "", ""};
                String zrzh = Get(f_zrz, "ZRZH", "");
                String zh = Get(f_zrz, "ZH", "");
                String jzwmc = Get(f_zrz, "JZWMC", "");
                z[0] = jzwmc;
                List<Feature> fs = new ArrayList<>();
                for (Feature f : fs_hAndFs) {
                    String id = Get(f, "ID", "");
                    if (id.startsWith(zrzh)) {
                        fs.add(f);
                    }
                }
                ArrayList<Map.Entry<String, List<Feature>>> fs_s = FeatureViewZRZ.GroupbyC_Sort(fs);
                for (Map.Entry<String, List<Feature>> c : fs_s) {
                    z[fs_s.indexOf(c) + 1] = String.format("%.2f", FeatureViewZRZ.hsmj_jzmj(c.getValue()));
                }
                z[8 + 1] = String.format("%.2f", FeatureHelper.Get(f_zrz, "SCJZMJ", 0.00d));
                String fwjgName = DicUtil.dic("fwjg", FeatureHelper.Get(f_zrz, "fwjg", ""));
                String fwjg = TextUtils.isEmpty(fwjgName) ? DicUtil.dic("fwjg_e", "B") : fwjgName;
                if (fwjg != null) {
                    if (!TextUtils.isEmpty(fwjg)) {
                        if (fwjg.contains("结构") && fwjg.contains("]")) {
                            fwjg = fwjg.substring(fwjg.lastIndexOf("]") + 1, fwjg.lastIndexOf("结构"));
                        } else if (fwjg.contains("结构")) {
                            fwjg = fwjg.substring(0, fwjg.lastIndexOf("结构"));
                        } else if (fwjg.contains("]")) {
                            fwjg = fwjg.substring(fwjg.lastIndexOf("]") + 1);
                        }
                    }
                    z[9 + 1] = fwjg;
                }
                String jgrq = FeatureHelper.Get(f_zrz, "JGRQ", "");
                if (!TextUtils.isEmpty(jgrq) && jgrq.length() >= 4) {
                    z[10 + 1] = jgrq.substring(0, 4);
                }

                if (zh.contains("01")) {
                    map_z.put(0, z);
                } else if (zh.contains("02")) {
                    map_z.put(1, z);
                } else if (zh.contains("03")) {
                    map_z.put(2, z);
                } else if (zh.contains("04")) {
                    map_z.put(3, z);
                } else if (zh.contains("05")) {
                    map_z.put(4, z);
                } else if (zh.contains("09")) {
                    map_z.put(5, z);
                }
                list_z.add(z);
            }

            dxf.setLableH(map_lable);
            int page_count = fs_map_croup.size() % 3 == 0 ? fs_map_croup.size() / 3 : fs_map_croup.size() / 3 + 1;  //  多少页
            float alpha = 0.001f;
            for (int page = 1; page < page_count + 1; page++) {

                List<C> cs = new ArrayList<>();
                int i1 = 1 + (page - 1) * 3;
                for (; i1 <= page * 3; i1++) {
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
//              dxf.write(new Envelope(envelope.getCenter(), p_width + o_split * 3, p_height + o_split * 5)); // 图框
                DxfHelper.writeDaYingKuangfwfct(dxf, envelope, o_split, spatialReference);

                Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
                dxf.writeText(p_title, "房屋分层平面图", o_fontsize * 2, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

                Point p_unit = new Point(envelope.getXMax(), envelope.getYMax() + o_split * 0.5, envelope.getSpatialReference());
                dxf.writeText(p_unit, "单位：m·㎡", 0.63f, DxfHelper.FONT_WIDTH_ONE, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, null, null);

                Point p_n = new Point(envelope.getXMax() - o_split * 0.75, envelope.getYMax() - o_split * 0.75);
                dxf.write(p_n, DxfHelper.LINETYPE_SOLID_LINE, "北", o_fontsize * 1.6f, fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                DxfHelper.writeN(new Point(p_n.getX(), p_n.getY() - o_split * 0.75, p_n.getSpatialReference()), o_split * 0.75, dxf);

//                writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split, -alpha);
                double w = p_width; //行宽
                // 左上角
                double x = envelope.getXMin();
                double y = envelope.getYMax();
                dxf.write(envelope);
                //  左一  二 层平面图
                double x_ = x;
                double y_ = y;
                Envelope cel_1_1 = new Envelope(x_ + w / 2, y_, x + w, y - p_height + 14.5f * h, p_extend.getSpatialReference());
                writeC(cs, 2, page, cel_1_1, 1);

                // 左二 边单元格框  一 层平面图

                Envelope cel_2_1 = new Envelope(x_, y_, x + w / 2, y - p_height * 0.65f, p_extend.getSpatialReference());
                writeC(cs, 1, page, cel_2_1, 2);

                // 右边单元格 2- 1 框  三层平面图
                Envelope cel_1_2 = new Envelope(x_, y - p_height * 0.65f, x + w / 2, y - p_height, p_extend.getSpatialReference());
                writeC(cs, 3, page, cel_1_2, 3);

                // 右下基本信息表
                x_ = x + w / 2;
                y_ = y - p_height + 19 * h;///
                Envelope cel_2_2 = new Envelope(x_, y_, x + w, y - p_height, p_extend.getSpatialReference());
                //建筑面积计算
                y_ = y_ - 1.5 * h;
                Envelope cel_3_1 = new Envelope(x_, y_, x + w, y_ - 1.5 * h, p_extend.getSpatialReference());
                dxf.write(cel_3_1, DxfHelper.LINETYPE_SOLID_LINE, "建筑面积计算表", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                y_ = y_ - 1.5 * h;
                Envelope cel_4_1 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_1, DxfHelper.LINETYPE_SOLID_LINE, "宗地代码：", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                x_ = x_ + w / 12;
                Envelope cel_4_2 = new Envelope(x_, y_, x_ + w / 4 - w / 180, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_2, DxfHelper.LINETYPE_SOLID_LINE, FeatureHelper.Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, ""), o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                x_ = x_ + w / 4 - w / 180;
                Envelope cel_4_3 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_3, DxfHelper.LINETYPE_SOLID_LINE, "所有权人", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                x_ = x_ + w / 12;
                Envelope cel_4_4 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_4, DxfHelper.LINETYPE_SOLID_LINE, FeatureHelper.Get(f_zd, "QLRXM", ""), o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                y_ = y_ - h;
                x_ = x + w / 2 + w / 4 + w / 12;
                Envelope cel_4_5 = new Envelope(x_, y_, x_ + w / 18, y_ - h, p_extend.getSpatialReference());
                dxf.writeMText(cel_4_5.getCenter(), "单位：㎡", o_fontsize, h_fontstyle, 0, 0, 3, DxfHelper.COLOR_BYLAYER, null, null);

                //cel_4_6
                x_ = x + w / 2;
                y_ = y_ - h;
                //不写边框
                //写直线  从幢号到建筑日期
                dxf.writeLine(Arrays.asList(new Point[]{new Point(x_, y_), new Point(x_ + w / 2, y_)}), DxfHelper.LINETYPE_SOLID_LINE, false, DxfHelper.COLOR_BYLAYER, 0);

                // 写斜线
                dxf.writeLine(Arrays.asList(new Point[]{new Point(x_, y_), new Point(x_ + w / 12, y_ - 2 * h)}), DxfHelper.LINETYPE_SOLID_LINE, false, DxfHelper.COLOR_BYLAYER, 0);
                // 写文字
                Envelope cel_4_6_1_1 = new Envelope(x_, y_ - h, x_ + w / 24, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_1.getCenter(), "幢号", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

                Envelope cel_4_6_1_2 = new Envelope(x_ + w / 24, y_, x_ + w / 24, y_ - h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_2.getCenter(), "层面积", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);
                //1楼
                x_ = x_ + w / 12;
                Envelope cel_4_6_1_3 = new Envelope(x_, y_, x_ + w / 32, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_3.getCenter(), "1", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);
                //2楼
                x_ = x_ + w / 32;
                Envelope cel_4_6_1_4 = new Envelope(x_, y_, x_ + w / 32, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_4.getCenter(), "2", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);
                //3楼
                x_ = x_ + w / 32;
                Envelope cel_4_6_1_5 = new Envelope(x_, y_, x_ + w / 32, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_5.getCenter(), "3", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);
                //4楼
                x_ = x_ + w / 32;
                Envelope cel_4_6_1_6 = new Envelope(x_, y_, x_ + w / 32, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_6.getCenter(), "4", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);
                //5楼
                x_ = x_ + w / 32;
                Envelope cel_4_6_1_7 = new Envelope(x_, y_, x_ + w / 32, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_7.getCenter(), "5", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);
                //6楼
                x_ = x_ + w / 32;
                Envelope cel_4_6_1_8 = new Envelope(x_, y_, x_ + w / 32, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_8.getCenter(), "6", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);
                //7楼
                x_ = x_ + w / 32;
                Envelope cel_4_6_1_9 = new Envelope(x_, y_, x_ + w / 32, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_9.getCenter(), "7", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);
                //-1楼
                x_ = x_ + w / 32;
                Envelope cel_4_6_1_10 = new Envelope(x_, y_, x_ + w / 32, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_10.getCenter(), "-1", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

                x_ = x_ + w / 32;
                Envelope cel_4_6_1_11 = new Envelope(x_, y_, x_ + w / 18 + w / 180, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_11.getCenter(), "幢建筑面积合计", o_fontsize * 0.8f, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

                x_ = x_ + w / 18 + w / 180;
                Envelope cel_4_6_1_12 = new Envelope(x_, y_, x_ + w / 36, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_12.getCenter(), "结构", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

                x_ = x_ + w / 36;
                Envelope cel_4_6_1_13 = new Envelope(x_, y_, x_ + 7 * w / 90, y_ - 2 * h, p_extend.getSpatialReference());
                dxf.writeText(cel_4_6_1_13.getCenter(), "建筑年代", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

                // 单元格2-1  绘制单位
                //01
                x_ = x + w / 2;
                y_ = y_ - 2 * h;
                Envelope cel_4_7 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_7, DxfHelper.LINETYPE_SOLID_LINE, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                Envelope cel_4_7_0 = new Envelope(x_, y_, x_ + w / 12, y - p_height + h, p_extend.getSpatialReference());
                dxf.write(cel_4_7_0);
                //02
                y_ = y_ - h;
                Envelope cel_4_8 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_8, DxfHelper.LINETYPE_SOLID_LINE, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                //03
                y_ = y_ - h;
                Envelope cel_4_9 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_9, DxfHelper.LINETYPE_SOLID_LINE, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                //04
                y_ = y_ - h;
                Envelope cel_4_10 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_10, DxfHelper.LINETYPE_SOLID_LINE, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                //05
                y_ = y_ - h;
                Envelope cel_4_11 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_11, DxfHelper.LINETYPE_SOLID_LINE, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                //06
                y_ = y_ - h;
                Envelope cel_4_12 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_12, DxfHelper.LINETYPE_SOLID_LINE, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                //07
                y_ = y_ - h;
                Envelope cel_4_13 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_13, DxfHelper.LINETYPE_SOLID_LINE, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                //08
                y_ = y_ - h;
                Envelope cel_4_14 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_14, DxfHelper.LINETYPE_SOLID_LINE, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                //09
                y_ = y_ - h;
                Envelope cel_4_15 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_15, DxfHelper.LINETYPE_SOLID_LINE, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                //10
                y_ = y_ - h;
                Envelope cel_4_16 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_16, DxfHelper.LINETYPE_SOLID_LINE, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                y_ = y_ - h;
                Envelope cel_4_17 = new Envelope(x_, y_, x_ + w / 12, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_4_17, DxfHelper.LINETYPE_SOLID_LINE, "层建筑面积合计", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                x_ = x + w / 2;
                y_ = y_ - h;
                Envelope cel_4_18 = new Envelope(x_, y_, x_ + w / 12, y - p_height, p_extend.getSpatialReference());
                dxf.write(cel_4_18, DxfHelper.LINETYPE_SOLID_LINE, "坐落", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                x_ = x_ + w / 12;
                Envelope cel_4_18_2 = new Envelope(x_, y_, x + w, y - p_height, p_extend.getSpatialReference());
                dxf.write(cel_4_18_2, DxfHelper.LINETYPE_SOLID_LINE, FeatureHelper.Get(f_zd, "ZL", ""), o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                // 填充自然幢 基本信息
                y_ = y - p_height + 13 * h;
                Envelope cel_i_j = null;
                for (int i = 0; i < 10; i++) {
                    x_ = x + w / 2 + w / 12;
                    y_ = y_ - h;
                    String[] z = map_z.get(i);
                  /*  String[] z = null;
                    if (i < list_z.size()) {
                       z = list_z.get(i);
                   }*/
                    for (int j = 1; j < 9; j++) {
                        cel_i_j = new Envelope(x_, y_, x_ + w / 32, y_ - h, p_extend.getSpatialReference());
                        if (z != null) {
                            try {
                                if (j == 0) {
                                    dxf.write(cel_i_j, null, z[j], o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                                } else {
                                    dxf.write(cel_i_j, null, z[j], o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                                }
                            } catch (Exception e) {
                                dxf.write(cel_i_j, null, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                            }
                        } else {
                            dxf.write(cel_i_j, null, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                        }
                        x_ = x_ + w / 32;
                    }
                    // 建筑物名称x_, y_, x_ + w / 12, y_ - h   + w / 18 + w / 180
                    Envelope cel_i_j_1 = new Envelope(x + w / 2, y_, x + w / 2 + w / 12 , y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_i_j_1, DxfHelper.LINETYPE_SOLID_LINE, z == null ? "" : z[0], o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                    // 5  楼
                    Envelope cel_i_j_5 = new Envelope(x_, y_, x_ + w / 18 + w / 180, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_i_j_5, DxfHelper.LINETYPE_SOLID_LINE, z == null ? "" : z[9], o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                    //结构
                    x_ = x_ + w / 18 + w / 180;
                    Envelope cel_i_j_6 = new Envelope(x_, y_, x_ + w / 36, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_i_j_6, DxfHelper.LINETYPE_SOLID_LINE, z == null ? "" : z[10], o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                    //建筑日期
                    x_ = x_ + w / 36;  //w/2 -w/32 * 8 - w /18 - w / 180 -w/ 36
                    Envelope cel_i_j_7 = new Envelope(x_, y_, x_ + 7 * w / 90, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_i_j_7, DxfHelper.LINETYPE_SOLID_LINE, z == null ? "" : z[11], o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                }

                // 面积汇总
                // 单元格4-3 框   一层面积汇总
                x_ = x + w / 2 + w / 12;
                y_ = y_ - h;
                Envelope cel_10_2 = new Envelope(x_, y_, x_ + w / 32, y_ - h, p_extend.getSpatialReference());
                //cs.get(1).getJzmj()+""
                dxf.write(cel_10_2, null, page == 1 ? cjzmj(cs, 1) : "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                // 单元格4-4 框   二层面积汇总
                x_ = x_ + w / 32;
                Envelope cel_10_3 = new Envelope(x_, y_, x_ + w / 32, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_3, null, page == 1 ? cjzmj(cs, 2) : "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                // 单元格4-5 框   三层面积汇总
                x_ = x_ + w / 32;
                Envelope cel_10_4 = new Envelope(x_, y_, x_ + w / 32, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_4, null, page == 1 ? cjzmj(cs, 3) : "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                // 单元格4-6 框   四层面积汇总
                x_ = x_ + w / 32;
                Envelope cel_10_5 = new Envelope(x_, y_, x_ + w / 32, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_5, null, page == 2 ? cjzmj(cs, 1) : "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                // 单元格4-7 框   五层面积汇总
                x_ = x_ + w / 32;
                Envelope cel_10_6 = new Envelope(x_, y_, x_ + w / 32, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_6, null, page == 2 ? cjzmj(cs, 2) : "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                // 单元格4-8 框   六层面积汇总
                x_ = x_ + w / 32;
                Envelope cel_10_7 = new Envelope(x_, y_, x_ + w / 32, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_7, null, page == 2 ? cjzmj(cs, 1) : "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                // 单元格4-9 框   七层面积汇总
                x_ = x_ + w / 32;
                Envelope cel_10_8 = new Envelope(x_, y_, x_ + w / 32, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_8, null, page == 2 ? cjzmj(cs, 2) : "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);
                // 单元格4-10 框   -1层面积汇总
                x_ = x_ + w / 32;
                Envelope cel_10_9 = new Envelope(x_, y_, x_ + w / 32, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_9, null, page == 2 ? cjzmj(cs, 1) : "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                // 单元格4-10 框   建筑面积
                x_ = x_ + w / 32;
                Envelope cel_10_10 = new Envelope(x_, y_, x_ + w / 18 + w / 180, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_10, null, String.format("%.2f", FeatureHelper.Get(f_zd, "JZMJ", 0.00d)), o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                // 单元格4-11 框   结构
                x_ = x_ + w / 18 + w / 180;
                Envelope cel_10_11 = new Envelope(x_, y_, x_ + w / 36, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_11, null, "", o_fontsize, h_fontstyle, false, 0, 0);

                // 单元格4- 12框   建筑年份
                x_ = x_ + w / 36;
                Envelope cel_10_12 = new Envelope(x_, y_, x + w / 2, y_ - h, p_extend.getSpatialReference());
                dxf.write(cel_10_12, null, "", o_fontsize, h_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

                x_ = x;
                y_ = y - p_height;
                String hzdw = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZDW", "");
                Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize * 1.26f) * 1.8, x_, y_, p_extend.getSpatialReference());
                Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
                //dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw,"\n"), o_fontsize*1.26f, fontstyle, 0, 0, 3, DxfHelper.COLOR_BYLAYER, null, null);
                dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw, "\n"), o_fontsize * 1.26f, h_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);


                // 绘制日期，审核日期
                Calendar c = Calendar.getInstance();
                String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH) + "日");
//                String auditDate ="2019年3月15日";
                c.add(Calendar.DATE, -1);
                //              String drawDate = "2019年3月13日";
                String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH) + "日");

                // 测绘员 审核员
                String sChr = "测绘员：" + GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZR", "");
                String sShr = "审核员：" + GsonUtil.GetValue(mapInstance.aiMap.JsonData, "SHR", "");
                int maxLenth = sChr.length() > sShr.length() ? sChr.length() : sShr.length();
                String strLeft = "绘图员：" + GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZR", "") + "     绘图日期：" + drawDate;
                String strRight = "检查员：" + GsonUtil.GetValue(mapInstance.aiMap.JsonData, "SHR", "") + "    检查日期：" + auditDate;
                Point p_auditDate = new Point(envelope.getXMin() + w / 12, envelope.getYMin() - h);
                dxf.writeText(p_auditDate, strLeft, o_fontsize, DxfHelper.FONT_WIDTH_ONE, h_fontstyle, 1, 0, 2, DxfHelper.COLOR_BYLAYER, null, null);
                Point p_chr = new Point(envelope.getXMax() - w / 12, envelope.getYMin() - h);
                dxf.writeText(p_chr, strRight, o_fontsize, DxfHelper.FONT_WIDTH_ONE, h_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, null, null);
                //绘制比例尺1：300
                Point p_blc = new Point(envelope.getCenter().getX(), envelope.getYMin() - o_split * 0.6, envelope.getSpatialReference());
                dxf.writeText(p_blc, "1:300", o_fontsize * 1.5f, DxfHelper.FONT_WIDTH_DEFULT, h_fontstyle, 0, 1, 2, 0, null, null);

//                Point p_blc = new Point(envelope.getCenter().getX() , envelope.getYMin() - o_split * 0.6, envelope.getSpatialReference());
//                dxf.writeText(p_blc, "1:200", o_fontsize*1.5f, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

            }

        } catch (Exception es) {
            dxf.error("生成失败", es, true);
        }
        return this;
    }

    // 获取每层建筑面积
    private String cjzmj(List<C> cs, int i) {
        String cjzmj = "";
        if (cs.size() >= i) {
            cjzmj = String.format("%.2f", cs.get(i - 1).cjzmj);
        }
        return cjzmj;
    }

    private void writeN(Point p, double w, float alpha) throws Exception {
        PointCollection ps = new PointCollection(p.getSpatialReference());
        ps.add(p);
        ps.add(new Point(p.getX() - w / 6, p.getY() - w / 2));
        ps.add(new Point(p.getX(), p.getY() + w / 2));
        ps.add(new Point(p.getX() + w / 6, p.getY() - w / 2));
        Polygon polygon = new Polygon(ps, p.getSpatialReference());
        Envelope extent = polygon.getExtent();
        dxf.write(MapHelper.geometry_get(polygon, p, alpha), null);
        // N
        PointCollection p_ = new PointCollection(p.getSpatialReference());
        p_.add(new Point(p.getX() - w / 12, p.getY() - w / 8 + w));
        p_.add(new Point(p.getX() - w / 12, p.getY() + w / 8 + w));
        p_.add(new Point(p.getX() + w / 12, p.getY() - w / 8 + w));
        p_.add(new Point(p.getX() + w / 12, p.getY() + w / 8 + w));
        Polyline polyline = new Polyline(p_, p.getSpatialReference());
        Geometry geometry = MapHelper.geometry_get(polyline, p, alpha);
        dxf.writeLine(DxfTemplet.Get_POLYLINE(MapHelper.geometry_getPoints(geometry), "Continuous", 0, 0, "0", ""));
    }

    public DxfFwfcpmt_tongshan save() throws Exception {
        if (dxf != null) {
            dxf.save();
        }
        return this;
    }

    public Envelope getPageExtend(int page) {
        double m = page * (p_width + 3 * o_split);
        double x_min = p_extend.getXMin() + m;
        double x_max = p_extend.getXMax() + m;
        double y_min = p_extend.getYMin();
        double y_max = p_extend.getYMax();
        return new Envelope(x_min, y_min, x_max, y_max, p_extend.getSpatialReference());
    }

    private void writeC(List<C> cs, int index, int page, Envelope envelope, int i) throws Exception {
        Envelope cel_5_t = new Envelope(envelope.getXMin(), envelope.getYMax(), envelope.getXMin(), envelope.getYMax() - h, p_extend.getSpatialReference());
        String s_pmt_1 = StringUtil.GetFormatNumber2(index + (page - 1) * 3 + "") + "层平面图";
        dxf.writeText(cel_5_t.getCenter(), s_pmt_1, 0.5f, 1.0f, h_fontstyle, 0, 0, 2, DxfHelper.COLOR_BYLAYER, null, null);

        if (index != 2) {
            dxf.write(envelope);
        }
        if (index <= cs.size()) {
            C c = cs.get(index - 1);
            List<Feature> fs = c.fs;
            if (FeatureHelper.isExistElement(fs)) {
                dxf.write(mapInstance, fs, null, DxfHelper.getDistanceMove(fs, envelope, envelope.getSpatialReference()), DxfHelper.TYPE_TONGSHAN, DxfHelper.LINE_LABEL_OUTSIDE, new LineLabel());
            }
        }
    }

}
