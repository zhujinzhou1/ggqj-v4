package com.ovit.app.map.bdc.ggqj.map.model;

import android.text.TextUtils;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
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

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * Created by XW on 2018/9/29. 房产图 固定1:200 比例尺   巴东 王总
 */

public class DxfFcfht_badong {

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
    private  double o_split=2d;// 单元间隔
    private  double p_width = 34d;// 页面宽
    private  double p_height = 45d;// 页面高
    private  double h = 1.26d; // 行高
    private  double scale = 1.26d; // 行高
    private float o_fontsize=0.6f;// 字体大小
    private String o_fontstyle = "宋体";// 字体大小
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;

    public DxfFcfht_badong(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfFcfht_badong set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfFcfht_badong set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfFcfht_badong set(String bdcdyh, Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_h_fsjg) {
        this.bdcdyh = bdcdyh;
        this.f_zd = f_zd;
        fs_hAndFs = new ArrayList<>();
        fs_hAndFs.addAll(fs_z_fsjg);
        fs_hAndFs.addAll(fs_h);
        fs_hAndFs.addAll(fs_h_fsjg);
        this.fs_zrz=fs_zrz;
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
        double height = o_extend.getHeight();
        double width = o_extend.getWidth();
        double v_h = width *(1+0.1)*2/p_width;
        scale=getNiceScale(v_h);

        if (scale>1){
            p_width=p_width*scale;
            p_height=p_height*scale;
            h=h*scale;
            o_split=o_split*scale;
            o_fontsize = (float) (o_fontsize*scale);
        }
//        double x_min = o_center.getX() - (p_width / 2);
//        double x_max = o_center.getX() + (p_width / 2);
//        double y_min = o_center.getY() - (p_height / 2);
//        double y_max = o_center.getY() + (p_height / 2);
        // 单元格范围
//        p_extend = new Envelope(x_min, y_min, x_max, y_max, o_extend.getSpatialReference());
        p_extend = new Envelope(o_center,p_width,p_height);
        return p_extend;
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
                    } else if (featureTable != mapInstance.getTable(FeatureHelper.LAYER_NAME_H_FSJG)){
                        wholeAreaFeatures.add(f);
                    }
                }else{
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
        public String getJzcl() {
            String jzcl="";
            if (TextUtils.isEmpty(jzcl)){
                List<String> fwjgs = new ArrayList<>();
                for (Feature f : fs_zrz) {
                    String fwjg = DicUtil.dic("fwjg", FeatureHelper.Get(f, "FWJG", "4"));
                    String jzwmc = FeatureHelper.Get(f,"JZWMC","其他");
                        if (!TextUtils.isEmpty(fwjg)&&jzwmc.contains("主房")) {
                            if (fwjg.contains("结构")) {
                                fwjg = fwjg.substring(fwjg.lastIndexOf("]") + 1,fwjg.lastIndexOf("结构"));
                            } else {
                                fwjg = fwjg.substring(fwjg.lastIndexOf("]") + 1);
                            }
                            if (!fwjgs.contains(fwjg)){
                              fwjgs.add(fwjg);
                            }
                        }
                }
                if (fwjgs.size()>0){
                jzcl= StringUtils.join(fwjgs,"/");
                }else {
                    jzcl="其他";
                }
            }
            return jzcl;
        }
        public String getZrzhZhuFang() {
            String jzcl="";
            if (TextUtils.isEmpty(jzcl)){
                List<String> zrzh_zf = new ArrayList<>(); // 主房幢号
                for (Feature f : fs_zrz) {
                    String jzwmc = FeatureHelper.Get(f,"JZWMC","其他");
                        if (jzwmc.contains("主房")) {
                            zrzh_zf.add(FeatureHelper.Get(f,"ZRZH",""));
                        }
                }
                jzcl= StringUtils.join(zrzh_zf,"/");
            }
            return jzcl;
        }
    }

    public DxfFcfht_badong write() throws Exception {
        getExtend(); // 多大范围
        ArrayList<Map.Entry<String, List<Feature>>> fs_map_croup = FeatureViewZRZ.GroupbyC_Sort(fs_hAndFs);

        if (dxf == null) {
            dxf = new DxfAdapter();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            int page_count = (int)Math.ceil(fs_map_croup.size()/4f);  //  多少页
                for (int page = 1; page < page_count+1; page++) {
                    String fwjg="";
                    List<C> cs=new ArrayList<>();
                    int i1=1+(page-1)*4;
                    for (; i1 <= page*4; i1++) {
                        if (i1<=fs_map_croup.size()){
                            Map.Entry<String, List<Feature>> fs_c_map = fs_map_croup.get(i1-1);
                            List<Feature> fs_c = fs_c_map.getValue();
                            if (fs_c!=null&&fs_c.size()>0){
                                C c =new C(fs_c_map.getKey(),fs_c_map.getValue(),fs_zrz);
                                cs.add(c);
                            }
                        }
                    }

                    Envelope envelope = getPageExtend(page);
//                    dxf.write(new Envelope(envelope.getCenter(), p_width + o_split * 3, p_height + o_split * 5)); // 图框
                    dxf.write(new Envelope(envelope.getCenter(), p_width + o_split * 3, p_height + o_split * 5), DxfHelper.LINETYPE_SOLID_LINE,"",2*o_fontsize, DxfHelper.FONT_STYLE_HZ,false, DxfHelper.COLOR_CYAN,0);
                    Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
                    dxf.writeText(p_title, "房产分户图", o_fontsize*2, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_CYAN, null, null);

                    Point p_unit = new Point(envelope.getXMax() , envelope.getYMax() + o_split*0.5, envelope.getSpatialReference());
                    dxf.writeText(p_unit, "单位：m.㎡", o_fontsize * 0.5f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_CYAN, null, null);
                    double w = p_width; //行宽
                    // 左上角
                    double x = envelope.getXMin();
                    double y = envelope.getYMax();

                    double x_ = x;
                    double y_ = y;
//                    String fwjg=getFwjg(fs_zrz,fs_map_croup.size());  主房结构
                    if (TextUtils.isEmpty(fwjg)){
                          fwjg=cs.get(0).getJzcl();
                    }
                    // 单元格1-1
                    Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_1, null, "宗地代码", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                    // 单元格1-2
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_2, null, Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, ""), o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                    // 单元格1-3
                    x_ = x_ + w * 4 / 15;
                    Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 2 /15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_3, null, "结构", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                    // 单元格1-4
                    x_ = x_ +w * 2 /15;
                    Envelope cel_1_4 = new Envelope(x_, y_, x_ + w * 2 /15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_4, null, fwjg, o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                    // 单元格1-5
                    x_ =x_ + w * 2 /15;
                    Envelope cel_1_5 = new Envelope(x_, y_, x_ + w * 1 /6, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_5, null, "专有建筑面积", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                    // 单元格1-6
                    x_ = x_ + w * 1 /6;
                    Envelope cel_1_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_6, null, "", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

                    // 单元格2-1
                    x_ = x;
                    y_ = y_ - h;
                    Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_2_1, null, "幢号", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                    // 单元格2-2
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                    String  zh=fs_zrz.size()>1?"F9999":"F"+ FeatureHelper.Get(fs_zrz.get(0),"ZH","0001");
                    dxf.write(cel_2_2, null,"F0001", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

                    // 单元格2-3
                    x_ = x_ + w * 4 / 15;
                    Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_2_3, null, "总层数", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

                    // 单元格2-4
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_2_4 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_2_4, null, fs_map_croup.size()+"", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                    // 单元格2-5
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_2_5 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_2_5, null, "分摊建筑面积", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

                    // 单元格2-6
                    x_ = x_ + w * 1 / 6;
                    Envelope cel_2_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_2_6, null, "", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

                    // 单元格3-1
                    x_ = x;
                    y_ = y_ - h;
                    Envelope cel_3_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_1, null, "户号", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                    // 单元格3-2
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_3_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_2, null,"0001", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

                    // 单元格3-3
                    x_ = x_ + w * 4 / 15;
                    Envelope cel_3_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_3, null, "所在层次", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

                    // 单元格3-4
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_3_4 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_4, null, "", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                    // 单元格3-5
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_3_5 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_5, null, "总建筑面积", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                    // 单元格3-6
                    x_ = x_ + w * 1 / 6;
                    Envelope cel_3_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_6, null, Get(f_zd,"JZMJ",0.00)+"", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

                    // 单元格4-1
                    x_ = x;
                    y_ = y_ - h;
                    Envelope cel_4_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_4_1, null, "坐落", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
                    // 单元格4-2
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_4_2 = new Envelope(x_, y_, x+ w , y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_4_2, null,Get(f_zd,"ZL",""), o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

                    // 单元格4-1
                    x_ = x;
                    y_ = y_ - h;
                    Envelope cel_5_1 = new Envelope(x_, y_, x_ + w, y_ - p_height+4*h, p_extend.getSpatialReference());
                    dxf.write(cel_5_1, null,"", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

                    y_ = y_ - (p_height-p_width-4*h)/2;
                    Envelope cel_3_1_1 = new Envelope(x_,y_,x_+w* 1/2,y_-w* 1/2,p_extend.getSpatialReference()) ;
                    if (cs.size()==1){
                        cel_3_1_1 = new Envelope(x_,y_,x_+w,y_-w,p_extend.getSpatialReference());
                    }else if (cs.size()==2){
                        cel_3_1_1 = new Envelope(x_,y_,x_+w* 1/2,y_-w,p_extend.getSpatialReference()) ;
                    }
                    writeC(cs,0,cel_3_1_1);
//
                    // 单元格3-2
                    x_ = x_+w* 1/2;
                    Envelope cel_3_2_2 =  new Envelope(x_,y_,x+w,y_-w* 1/2,p_extend.getSpatialReference()) ;
                    if (cs.size()==2){
                        cel_3_2_2 = new Envelope(x_,y_,x+w,y_-w,p_extend.getSpatialReference());
                    }
                    writeC(cs,1,cel_3_2_2);


                    // 单元格4-1
                    x_ = x; y_ = y_-w* 1/2;
                    Envelope cel_4_1_1 =   new Envelope(x_,y_,x_+w* 1/2,y_-w* 1/2,p_extend.getSpatialReference()) ;
                    writeC(cs,2,cel_4_1_1);

                    // 单元格4-2
                    x_ = x_+w* 1/2;
                    Envelope cel_4_2_2 = new Envelope(x_,y_,x+w,y_-w* 1/2,p_extend.getSpatialReference()) ;
                    writeC(cs,3,cel_4_2_2);

                    Point p_n = new Point(cel_5_1.getXMax() - o_split , cel_5_1.getYMax() - o_split);
                    dxf.write(p_n, null, "N", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);

                    writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split);

                    //  绘制单位
                    x_=x;
                    y_ = y - p_height;
                    String hzdw =  GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZDW","");
                    Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
                    Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
                    dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw,"\n"), o_fontsize, o_fontstyle, 0, 0, 3,  DxfHelper.COLOR_CYAN, null, null);

                    Calendar c = Calendar.getInstance();
                    String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
                    c.add(Calendar.DATE, -1);
                    String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");

                    Point p_auditDate = new Point(envelope.getCenter().getX()- w * 1 / 3-w * 1 / 20, envelope.getYMin() -3* o_split * 0.3, envelope.getSpatialReference());
                    Point p_drawDate = new Point(envelope.getCenter().getX()- w * 1 / 3-w * 1 / 20 , envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
                    dxf.writeText(p_drawDate, "绘图日期:"+drawDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2,  DxfHelper.COLOR_CYAN, null, null);
                    dxf.writeText(p_auditDate,"审核日期:"+auditDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2,  DxfHelper.COLOR_CYAN, null, null);

                    Point p_blc = new Point(envelope.getCenter().getX() , envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
                    dxf.writeText(p_blc, "1:200", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2,  DxfHelper.COLOR_CYAN, null, null);

                    Point p_chr = new Point(envelope.getCenter().getX() + w * 1 / 3+w*1/10, envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
                    dxf.writeText(p_chr, "测绘员："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2,  DxfHelper.COLOR_CYAN, null, null);
                    Point p_shr = new Point(envelope.getCenter().getX() + w * 1 / 3+w*1/10, envelope.getYMin() - 3*o_split * 0.3, envelope.getSpatialReference());
                    dxf.writeText(p_shr, "审核员："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2,  DxfHelper.COLOR_CYAN, null, null);

                }

        } catch(Exception es){
                dxf.error("生成失败", es, true);
            }
            return this;
        }

    private String getFwjg(List<Feature> fs_zrz,int maxLc) {
        if (fs_zrz.size() == 1) {
            String fwjg = DicUtil.dic("fwjg", FeatureHelper.Get(fs_zrz.get(0), "FWJG", "B"));
            return getFwjg(fwjg);
        }
        for (Feature zrz : fs_zrz) {
            String jzwmc = FeatureHelper.Get(zrz, "JZWMC", "");
            if (jzwmc.contains("主房")) {
                String fwjg = DicUtil.dic("fwjg", FeatureHelper.Get(zrz, "FWJG", "B"));
                return getFwjg(fwjg);
            } else {
                if (maxLc == FeatureHelper.Get(zrz, "ZCS", 1)) {
                    String fwjg = DicUtil.dic("fwjg", FeatureHelper.Get(zrz, "FWJG", "B"));
                    return getFwjg(fwjg);
                }
            }
        }
        return "砖混";

    }
    public String getFwjg(String fwjg){
//        if (fwjg.contains("土")||fwjg.contains("木")||fwjg.contains("石")){
//            return "其他";
//        }
        if (fwjg.contains("结构") && fwjg.contains("]")) {
            fwjg = fwjg.substring(fwjg.indexOf("]") + 1, fwjg.indexOf("]") + 1 + 2);
        } else {
            fwjg = fwjg.substring(fwjg.indexOf("]") + 1);
        }
        return fwjg;
    }

    private void writeN(Point p, double w) throws Exception {
        PointCollection ps = new PointCollection(p.getSpatialReference());
        ps.add(p);
        ps.add(new Point(p.getX() - w / 6, p.getY() - w / 2));
        ps.add(new Point(p.getX(), p.getY() + w / 2));
        ps.add(new Point(p.getX() + w / 6, p.getY() - w / 2));
        Geometry polygon = new Polygon(ps, p.getSpatialReference());
        dxf.write(polygon, null, "", o_fontsize, null, false, DxfHelper.COLOR_CYAN, 0);
    }

    public DxfFcfht_badong save() throws Exception {
        if (dxf != null) {
            dxf.save();
        }
        return this;
    }
    private void writeC(List<C> cs , int index , Envelope cell)throws Exception{
        if(index<cs.size()) {
            DxfFcfht_badong.C c = cs.get(index);
            GeodeticDistanceResult d_move = null;
            List<Feature> fs = c.fs;
            Envelope e = MapHelper.geometry_combineExtents_Feature(fs);
            e = MapHelper.geometry_get(e, spatialReference); //  图形范围
            double jzmj = c.getJzmj();
            String lc = c.lc;

            if (!MapHelper.geometry_equals(e.getCenter(), cell.getCenter())) {
                d_move = GeometryEngine.distanceGeodetic(e.getCenter(), cell.getCenter(), MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
            }

            dxf.write(mapInstance, fs, null, d_move, DxfHelper.TYPE_BADONG, DxfHelper.LINE_LABEL_OUTSIDE,new LineLabel());
            String zfzh=c.getZrzhZhuFang();// 主房幢号
            for (Feature f : fs) {
                if (FeatureHelper.TABLE_NAME_H.equals(f.getFeatureTable().getTableName())){
                    Geometry g = MapHelper.geometry_get(f.getGeometry(), spatialReference);
                    g = MapHelper.geometry_move(g, d_move);
                    Point p_c=GeometryEngine.labelPoint((Polygon) g);
                    if (!TextUtils.isEmpty(zfzh)&&zfzh.contains(FeatureHelper.Get(f,"ZRZH",""))){
                        // 面积注记只标注在主房
                     dxf.writeText(p_c,"S="+String.format("%.2f",c.getJzmj())+"", o_fontsize*0.5f , DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 1, DxfHelper.COLOR_BLUE, "JZD", "302002");
                    }else if (TextUtils.isEmpty(zfzh)){
                        dxf.writeText(p_c,"S="+String.format("%.2f",c.getJzmj())+"", o_fontsize*0.5f , DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 1, DxfHelper.COLOR_BLUE, "JZD", "302002");
                    }

                    // lc
                    Point lastPoint = MapHelper.Geometry_get(g, DxfHelper.POINT_TYPE_RIGHT_BOTTOM);
                    if (lastPoint!=null){
                        Geometry buffer = GeometryEngine.buffer(lastPoint, o_fontsize*2.5f);
                        Geometry intersectionG = GeometryEngine.intersection(g, buffer);
                        Envelope envelope = intersectionG.getExtent();
                        Point center = envelope.getCenter();
                        dxf.writeMText(center,"("+lc+")", o_fontsize* DxfHelper.FONT_SIZE_FACTOR , o_fontstyle, 0, 1, 2, DxfHelper.COLOR_CYAN, "JZD", "");
                    }
                }
            }
        }
    }

    public Envelope getPageExtend( int page){
        double m = page *(p_width+3*o_split);
        double x_min = p_extend.getXMin() + m;
        double x_max = p_extend.getXMax() + m;
        double y_min = p_extend.getYMin();
        double y_max = p_extend.getYMax();
        return  new Envelope( x_min,y_min , x_max,y_max ,p_extend.getSpatialReference());
    }
    private double getNiceScale(double scale) {
        double niceScale=0d;
        if (scale<=0d){
            return 1;
        }
        if (scale%1>0.85){
            niceScale=(int) scale+1.25;
        }else if (scale%1>0.75){
            niceScale=(int) scale+1;
        }else if (scale%1>0.5){
            niceScale=(int) scale+0.75;
        }else if (scale%1>0.25){
            niceScale=(int) scale+0.5;
        }else {
            niceScale=(int) scale+0.25;
        }
        return niceScale;
    }

}
