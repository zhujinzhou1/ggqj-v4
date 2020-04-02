package com.ovit.app.map.bdc.ggqj.map.model;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZRZ;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * Created by XW on 2018/9/29. 房产图 固定1:200 比例尺
 */

public class DxfFengcengtu_hunan {

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
    private float o_fontsize=0.6f;// 字体大小
    private String o_fontstyle = "宋体";// 字体大小
    private Envelope p_extend;// 页面的范围
    private List<Feature> fs_zrz;

    public DxfFengcengtu_hunan(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }

    public DxfFengcengtu_hunan set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }

    public DxfFengcengtu_hunan set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public DxfFengcengtu_hunan set(String bdcdyh, Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_h_fsjg) {
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

        double maxLen=height>width?height:width;
        double v_h = maxLen *(1+0.1)*2/ p_height;
        double scale=getNiceScale(v_h);

        if (scale>1){
            p_width=p_width*scale;
            p_height=p_height*scale;
            h=h*scale;
            o_split=o_split*scale;
            o_fontsize = (float) (0.6f*scale);
        }else {
            scale=1;
        }
        double x_min = o_center.getX() - (p_width / 2);
        double x_max = o_center.getX() + (p_width / 2);
        double y_min = o_center.getY() - (p_height / 2);
        double y_max = o_center.getY() + (p_height / 2);
        // 单元格范围
        p_extend = new Envelope(x_min, y_min, x_max, y_max, o_extend.getSpatialReference());
        return p_extend;
    }
    public class C {
        String zh;
        Feature zrz;
        String lc;
        List<Feature> fs;
        public  C(String zh,Feature zrz,String lc,List<Feature> fs){
            this.zh = zh;
            this.zrz = zrz;
            this.lc= lc;
            this.fs = fs;
        }
        public String getName(){
            int zh_i =   AiUtil.GetValue(StringUtil.getTextOnlyIn(zh,"0123456789"),0);
            String  name  = zh;
            if(zh_i>0){name =  zh_i+"";}
            name = name.replace("幢","") +"幢"+lc.replace("层","")+"层";
            return  name ;
        }
//        public String getZcshz(String cshz){
//            int zh_i =   AiUtil.GetValue(StringUtil.getTextOnlyIn(zh,"0123456789"),0);
//            String  name  = zh;
//            if(zh_i>0){name =  zh_i+"";}
//            name = name.replace("幢","") +"幢"+ FeatureHelper.Get(zrz,"ZCS",1)+"层";
//            if (!cshz.contains(name)){
//                zcshz+=name+"\n"  ;
//            }
//            return zcshz;
//        }
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
    public DxfFengcengtu_hunan write() throws Exception {
        getExtend(); // 多大范围
        ArrayList<Map.Entry<String, List<Feature>>> fs_s = FeatureViewZRZ.GroupbyC_Sort(fs_hAndFs);
        LinkedHashMap<String, List<Feature>> fs_croup = FeatureViewZRZ.GroupbyC(fs_hAndFs);

        if (dxf == null) {
            dxf = new DxfAdapter();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            int p_indext=0;
            for (Feature f_zrz : fs_zrz) {
                ArrayList cs = new ArrayList<>();
                String zrzh = Get(f_zrz,"ZRZH","");
                String zh = Get(f_zrz,"ZH","");
                List<Feature> fs = new ArrayList<>();
                for(Feature f :fs_hAndFs){
                    String id = Get(f,"ID","");
                    if(id.startsWith(zrzh)){
                        fs.add(f);
                    }
                }
                ArrayList<Map.Entry<String, List<Feature>>> fs_cs =  FeatureViewZRZ.GroupbyC_Sort(fs);
                for(Map.Entry<String, List<Feature>> c :fs_cs){
                    cs.add(new DxfFengcengtu_hunan.C(zh,f_zrz,c.getKey(),c.getValue()));
                }

                int page_count = (int)Math.ceil(cs.size()/4f);  //  多少页
                for (int page = 0; page < page_count; page++) {
                    Envelope envelope = getPageExtend(p_indext+page);
                    Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
                    dxf.writeText(p_title, "房产图", o_fontsize*2, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                    Point p_unit = new Point(envelope.getXMax() , envelope.getYMax() + o_split*0.5, envelope.getSpatialReference());
                    dxf.writeText(p_unit, "单位：米·平方米", o_fontsize * 0.5f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, 0, null, null);
                    double w = p_width; //行宽
                    // 左上角
                    double x = envelope.getXMin();
                    double y = envelope.getYMax();

                    double x_ = x;
                    double y_ = y;
                    String fwjg="砖混";
                    int zcs=1;
                    // 单元格1-1
                    Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_1, null, "宗地代码", o_fontsize, null, false, 0, 0);
                    // 单元格1-2
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_2, null, Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, ""), o_fontsize, null, false, 0, 0);
                    // 单元格1-3
                    x_ = x_ + w * 4 / 15;
                    Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 2 /15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_3, null, "结构", o_fontsize, null, false, 0, 0);
                    // 单元格1-4
                    x_ = x_ +w * 2 /15;
                    Envelope cel_1_4 = new Envelope(x_, y_, x_ + w * 2 /15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_4, null, fwjg, o_fontsize, null, false, 0, 0);
                    // 单元格1-5
                    x_ =x_ + w * 2 /15;
                    Envelope cel_1_5 = new Envelope(x_, y_, x_ + w * 1 /6, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_5, null, "专有建筑面积", o_fontsize, null, false, 0, 0);
                    // 单元格1-6
                    x_ = x_ + w * 1 /6;
                    Envelope cel_1_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_1_6, null, Get(f_zrz,"SCJZMJ",0.00)+"", o_fontsize, null, false, 0, 0);

                    // 单元格2-1
                    x_ = x;
                    y_ = y_ - h;
                    Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_2_1, null, "幢号", o_fontsize, null, false, 0, 0);
                    // 单元格2-2
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_2_2, null,"F"+zh, o_fontsize, null, false, 0, 0);

                    // 单元格2-3
                    x_ = x_ + w * 4 / 15;
                    Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_2_3, null, "总层数", o_fontsize, null, false, 0, 0);

                    // 单元格2-4
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_2_4 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_2_4, null, FeatureHelper.Get(f_zrz,"ZCS",1)+"", o_fontsize, null, false, 0, 0);
                    // 单元格2-5
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_2_5 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_2_5, null, "分摊建筑面积", o_fontsize, null, false, 0, 0);

                    // 单元格2-6
                    x_ = x_ + w * 1 / 6;
                    Envelope cel_2_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_2_6, null, "", o_fontsize, null, false, 0, 0);

                    // 单元格3-1
                    x_ = x;
                    y_ = y_ - h;
                    Envelope cel_3_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_1, null, "户号", o_fontsize, null, false, 0, 0);
                    // 单元格3-2
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_3_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_2, null,"0001", o_fontsize, null, false, 0, 0);

                    // 单元格3-3
                    x_ = x_ + w * 4 / 15;
                    Envelope cel_3_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_3, null, "所在层次", o_fontsize, null, false, 0, 0);

                    // 单元格3-4
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_3_4 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_4, null, "", o_fontsize, null, false, 0, 0);
                    // 单元格3-5
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_3_5 = new Envelope(x_, y_, x_ + w * 1 / 6, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_5, null, "总建筑面积", o_fontsize, null, false, 0, 0);
                    // 单元格3-6
                    x_ = x_ + w * 1 / 6;
                    Envelope cel_3_6 = new Envelope(x_, y_, x + w, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_3_6, null, Get(f_zrz,"SCJZMJ",0.00)+"", o_fontsize, null, false, 0, 0);

                    // 单元格4-1
                    x_ = x;
                    y_ = y_ - h;
                    Envelope cel_4_1 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_4_1, null, "权利人", o_fontsize, null, false, 0, 0);
                    // 单元格4-2
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_4_2 = new Envelope(x_, y_, x_ + w * 4 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_4_2, null,Get(f_zd,"QLRXM",""), o_fontsize, null, false, 0, 0);

                    // 单元格4-3
                    x_ = x_ + w * 4 / 15;
                    Envelope cel_4_3 = new Envelope(x_, y_, x_ + w * 2 / 15, y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_4_3, null, "坐落", o_fontsize, null, false, 0, 0);

                    // 单元格4-4
                    x_ = x_ + w * 2 / 15;
                    Envelope cel_4_4 = new Envelope(x_, y_, x+ w , y_ - h, p_extend.getSpatialReference());
                    dxf.write(cel_4_4, null, Get(f_zd,"ZL",""), o_fontsize, null, false, 0, 0);

                    // 单元格4-1
                    x_ = x;
                    y_ = y_ - h;
                    Envelope cel_5_1 = new Envelope(x_, y_, x_ + w, y_ - p_height+3*h, p_extend.getSpatialReference());
                    dxf.write(cel_5_1);

                    y_ = y_ - (p_height-p_width-4*h)/2;
                    Envelope cel_3_1_1 = new Envelope(x_,y_,x_+w* 1/2,y_-w* 1/2,p_extend.getSpatialReference()) ;
//                    dxf.write(cel_3_1_1);
                    writeC(cs,0+page*4,cel_3_1_1);
//
                    // 单元格3-2
                    x_ = x_+w* 1/2;
                    Envelope cel_3_2_2 =  new Envelope(x_,y_,x+w,y_-w* 1/2,p_extend.getSpatialReference()) ;
//                    dxf.write(cel_3_2_2);
                    writeC(cs,1+page*4,cel_3_2_2);


                    // 单元格4-1
                    x_ = x; y_ = y_-w* 1/2;
                    Envelope cel_4_1_1 =   new Envelope(x_,y_,x_+w* 1/2,y_-w* 1/2,p_extend.getSpatialReference()) ;
//                    dxf.write(cel_4_1_1);
                    writeC(cs,2+page*4,cel_4_1_1);

                    // 单元格4-2
                    x_ = x_+w* 1/2;
                    Envelope cel_4_2_2 = new Envelope(x_,y_,x+w,y_-w* 1/2,p_extend.getSpatialReference()) ;
//                    dxf.write(cel_4_2_2);
                    writeC(cs,3+page*4,cel_4_2_2);

                    Point p_n = new Point(cel_5_1.getXMax() - o_split , cel_5_1.getYMax() - o_split);
                    dxf.write(p_n, null, "N", o_fontsize, null, false, 0, 0);

                    writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split);

                    // 单元格4-0
                    // 单元格5-1
                    x_ = x;
                    y_ = y - p_height;
                    Point p_blc = new Point(envelope.getCenter().getX(), envelope.getYMin() + o_split * 0.3-h, envelope.getSpatialReference());
                    dxf.writeText(p_blc, "1:200", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
                    // 单元格5-2
                    Calendar c = Calendar.getInstance();
                    String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");

                    Point p_rq = new Point(envelope.getCenter().getX() + w * 1 / 3, envelope.getYMin() + o_split * 0.3-h, envelope.getSpatialReference());
                    dxf.writeText(p_rq, auditDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
                    // 单元格6-1
                    Point p_chr = new Point(envelope.getCenter().getX() - w * 1 / 3, envelope.getYMin() - o_split * 0.3-h, envelope.getSpatialReference());
                    dxf.writeText(p_chr, "测绘人："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
                    // 单元格6-2
                    Point p_jsr = new Point(envelope.getCenter().getX(), envelope.getYMin() - o_split * 0.3-h, envelope.getSpatialReference());
                    dxf.writeText(p_jsr, "计算人："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
                    // 单元格6-3
                    Point p_shr = new Point(envelope.getCenter().getX() + w * 1 / 3, envelope.getYMin() - o_split * 0.3-h, envelope.getSpatialReference());
                    dxf.writeText(p_shr, "审核人："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                    //潜江市房产测绘大队
                    String hzdw =  GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZDW","");
                    Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
                    Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
                    dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw,"\n"), o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);

                }
                p_indext+=page_count;
            }

        } catch(Exception es){
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

    public DxfFengcengtu_hunan save() throws Exception {
        if (dxf != null) {
            dxf.save();
        }
        return this;
    }
    private void writeC(List<C> cs , int index , Envelope cell)throws Exception{
        if(index<cs.size()) {
            DxfFengcengtu_hunan.C c = cs.get(index);
            GeodeticDistanceResult d_move = null;
            List<Feature> fs = c.fs;
            Envelope e = MapHelper.geometry_combineExtents_Feature(fs);
            e = MapHelper.geometry_get(e, spatialReference); //  图形范围
            double jzmj = c.getJzmj();
            String lc = c.lc;

            if (!MapHelper.geometry_equals(e.getCenter(), cell.getCenter())) {
                d_move = GeometryEngine.distanceGeodetic(e.getCenter(), cell.getCenter(), MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
            }

            dxf.write(mapInstance, fs, null, d_move);
            Point p = new Point(cell.getXMin() + cell.getWidth() / 2, cell.getYMin() + o_split / 2);
            String text = c.getName() + "平面图";
            dxf.write(p, null, text, o_fontsize, null, false, 0, 0);

            for (Feature f : fs) {
                if (FeatureHelper.TABLE_NAME_H.equals(f.getFeatureTable().getTableName())){
                    com.esri.arcgisruntime.geometry.Geometry g = MapHelper.geometry_get(f.getGeometry(), spatialReference);
                    g = MapHelper.geometry_move(g, d_move);
                    Point p_c=GeometryEngine.labelPoint((Polygon) g);
                    float ft=o_fontsize;
                    dxf.writeText(new Point(p_c.getX() - 1.2 * ft, p_c.getY() + 0.2 * ft),c.getJzmj()+"", 0.5f * ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 1, 7, "JZD", "302002");
                    dxf.writeLine(Arrays.asList(new Point[]{new Point(p_c.getX() - 2.4 * ft, p_c.getY()), new Point(p_c.getX() - 0.1 * ft, p_c.getY())}), "", false, 7, 0);
                    dxf.writeText(new Point(p_c.getX() - 1.2 * ft, p_c.getY() - 0.2 * ft), c.lc, 0.5f * ft, DxfHelper.FONT_WIDTH_DEFULT, "", 0, 1, 3, 7, "JZD", "302003");
                    return;
                }
            }


//            int scale = MapHelper.geometry_scale(mapInstance.map, fs_zrz.get(0).getGeometry(), 1.0f);
//            double s_d_m = (scale / 100);
//            String s_d_m_ = String.format("%.1f", s_d_m).replace(".0", "");
//            String scale_1 = "1:" + scale;
//            String s_d_m_3 = s_d_m_ + "米";
//
//            double ft = 0.4;
//            Point p_blc = new Point(cell.getXMin() + cell.getWidth() / 4 / 2, cell.getYMax() - o_split / 2);
//            dxf.write(p_blc, null, scale_1, o_fontsize, null, false, 0, 0);
//            dxf.writeLine(Arrays.asList(new Point[]{new Point(p_blc.getX() - Integer.parseInt(s_d_m_) * 0.5, p_blc.getY() - 1.5 * o_fontsize), new Point(p_blc.getX() + 0.5 * Integer.parseInt(s_d_m_), p_blc.getY() - 1.5 * o_fontsize)}), "", false, 0, 0);
//            dxf.write(new Point(p_blc.getX(), p_blc.getY() - 3 * o_fontsize), null, s_d_m_3, o_fontsize, null, false, 0, 0);
        }
//        Point p_n = new Point(cell.getXMax() - o_split/2 ,cell.getYMax()- o_split);
//        dxf.write(p_n,null,"N",o_fontsize*0.8f,null,false,0,0);
//        writeN(new Point(p_n.getX(),p_n.getY()-o_split,p_n.getSpatialReference()),o_split);
    }

    public Envelope getPageExtend( int page){
        double m = page *(p_width+ o_split);
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
