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
import com.esri.arcgisruntime.geometry.Polyline;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

public class DxfFcfct extends BaseDxf {
    private String bdcdyh;
    private String fwjg;
    private ArrayList<Map.Entry<String, List<Feature>>> fs_map_croup=new ArrayList<>();
    private Feature f_zd;
    private List<Feature> fs_zrz;
    private List<Feature> fs_zd;
    private List<Feature> fs_z_fsjg;
    private List<Feature> fs_all;
    private List<Feature> fs_hAndFs;
    private float alpha=0.001f;
    private boolean isHorizontal=false;
    public DxfFcfct(MapInstance mapInstance) {
        super(mapInstance);
    }

    @Override
    public DxfFcfct set(String dxfpath) {
        super.set(dxfpath);
        return this;
    }
    public DxfFcfct set(String bdcdyh, Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_h_fsjg) {
        this.bdcdyh = bdcdyh;
        this.f_zd = f_zd;
        this.fs_z_fsjg=fs_z_fsjg;
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
    public Envelope getextend() {
        o_extend = MapHelper.geometry_combineExtents_Feature(fs_all);
        o_extend = MapHelper.geometry_get(o_extend, spatialReference); //  图形范围
        o_center = o_extend.getCenter(); // 中心点
        // 比例尺
        double height = o_extend.getHeight();
        double width = o_extend.getWidth();
        int zcs=fs_map_croup.size();
        double v_h=0d;
        double v_w=0d;

        if (zcs==1){
            v_h = height / (p_height-6*h);
            v_w = width/ p_width;
            double niceScale_h= DxfHelper.getNiceScale(v_h);
            double niceScale_w= DxfHelper.getNiceScale(v_w);
            scale=niceScale_w>niceScale_h?niceScale_w:niceScale_h;
        }else if (zcs==2){
            // 1  橫放
            double v_h_h = height / (p_height-6*h);
            double v_w_h = width*2/ p_width;
            double scale_h=v_w_h>v_h_h?v_w_h:v_h_h;

            // 2  上下摆放
            double  v_h_v = height*2 / (p_height-6*h);
            double  v_w_v = width/ p_width;
            double scale_v=v_w_v>v_h_v?v_w_v:v_h_v;

            if (scale_v<=scale_h ){
                scale= DxfHelper.getNiceScale(scale_v);
                isHorizontal=false;
            }else {
                scale= DxfHelper.getNiceScale(scale_h);
                isHorizontal=true;
            }

        }else {
            v_h = height*2 / (p_height-6*h);
            v_w = width*2/ p_width;
            double niceScale_h= DxfHelper.getNiceScale(v_h);
            double niceScale_w= DxfHelper.getNiceScale(v_w);
            scale=niceScale_w>niceScale_h?niceScale_w:niceScale_h;
        }

        if (scale>1){
            p_width=p_width*scale;
            p_height=p_height*scale;
            boxWidth=boxWidth*scale;
            boxHeight=boxHeight*scale;
            h=h*scale;
            blc=blc*scale;
            o_split=o_split*scale;
            o_fontsize = (float) (o_fontsize*scale);
        }
        p_extend = new Envelope(o_center,p_width,p_height);
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
            iniT();
        }

        private void iniT() {
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
    }

    @Override
    public DxfFcfct write() throws Exception {
        getextend(); // 多大范围
        fs_map_croup = FeatureViewZRZ.GroupbyC_Sort(fs_hAndFs);
        if (dxf == null) {
            dxf = DxfAdapter.getInstance();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setFontSize(o_fontsize);
        }
        try {
            fwjg=getFwjg(fs_zrz,fs_map_croup.size());
            getHeader();
            getBody();
            getFooter();
        }catch(Exception es){
                dxf.error("生成失败", es, true);
            }
            return this;

    }

    @Override
    protected void getHeader() throws Exception {

    }

    @Override
    protected void getBody() throws Exception {
        List<String[]> list_z=new ArrayList<>();
        for(Feature f_zrz :fs_zrz){
            String[] z={"","","","","","","","",""};
            z[0]="("+ FeatureHelper.Get(f_zrz,"ZH","")+")";
            String fwjgName= DicUtil.dic("fwjg", FeatureHelper.Get(f_zrz, "fwjg", ""));
            String fwjg_=  TextUtils.isEmpty(fwjgName)? DicUtil.dic("fwjg_e", "B"):fwjgName;
            if (fwjg_!=null){
                String fwjg=fwjg_.substring(fwjg_.lastIndexOf("[")+1,fwjg_.lastIndexOf("]"));
                if(fwjg.equalsIgnoreCase("混")){
                    z[1]="混合";
                } else if(fwjg.equalsIgnoreCase("砖")){
                    z[1]="砖木";
                } else if(fwjg.equalsIgnoreCase("土")||fwjg.equalsIgnoreCase("木")){
                    z[1]="土木";
                } else{
                    z[1]=fwjg;
                }
            }
            String zrzh = Get(f_zrz,"ZRZH","");
            List<Feature> fs = new ArrayList<>();
            for(Feature f :fs_hAndFs){
                String id = Get(f,"ID","");
                if(id.startsWith(zrzh)){
                    fs.add(f);
                }
            }
            ArrayList<Map.Entry<String, List<Feature>>> fs_s =  FeatureViewZRZ.GroupbyC_Sort(fs);
            for(Map.Entry<String, List<Feature>> c :fs_s){
                z[2+fs_s.indexOf(c)]=String.format("%.2f",FeatureViewZRZ.hsmj_jzmj(c.getValue()));
            }
            z[7]=String.format("%.2f", FeatureHelper.Get(f_zrz,"ZZDMJ",0.00d));
            z[8]=String.format("%.2f", FeatureHelper.Get(f_zrz,"SCJZMJ",0.00d));


            list_z.add(z);
        }

        alpha = (float) (Math.PI * MapHelper.geometry_get_azimuth(f_zd.getGeometry()) / 180);
        int page_count = (int) Math.ceil(fs_map_croup.size() / 2f);  //  多少页
        for (int page = 1; page < page_count + 1; page++) {
            List<DxfFcfct.C> cs = new ArrayList<>();
            int i1 = 1 + (page - 1) * 2;
            for (; i1 <= page * 2; i1++) {
                if (i1 <= fs_map_croup.size()) {
                    Map.Entry<String, List<Feature>> fs_c_map = fs_map_croup.get(i1 - 1);
                    List<Feature> fs_c = fs_c_map.getValue();
                    if (fs_c != null && fs_c.size() > 0) {
                        DxfFcfct.C c = new DxfFcfct.C(fs_c_map.getKey(), fs_c_map.getValue(), fs_zrz);
                        cs.add(c);
                    }
                }
            }

            Envelope envelope = getPageExtend(page);
            dxf.write(new Envelope(envelope.getCenter(), boxWidth/1.15+0.6, boxHeight/1.15+0.6)); // 图框

            Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split / 2, envelope.getSpatialReference());
            dxf.writeText(p_title, "房产分层分户图", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

            Point p_unit = new Point(envelope.getXMax(), envelope.getYMax() + o_split * 0.5, envelope.getSpatialReference());
            dxf.writeText(p_unit, "单位：m·㎡ ", o_fontsize * 0.8f, DxfHelper.FONT_WIDTH_DEFULT, DxfHelper.FONT_STYLE_SONGTI, 0, 2, 2, DxfHelper.COLOR_BYLAYER, null, null);
            double x = envelope.getXMin();
            double y = envelope.getYMax();
            double x_ = x;
            double y_ = y;
            double w = p_width;

            Envelope cel_1_1 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_1, DxfHelper.LINETYPE_SOLID_LINE, "不动产单元号", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_1_2 = new Envelope(x_, y_, x_ + w * 3 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_2, DxfHelper.LINETYPE_SOLID_LINE, FeatureHelper.Get(f_zd, "BDCDYH", ""), o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 3 / 8;
            Envelope cel_1_3 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_3, DxfHelper.LINETYPE_SOLID_LINE, "结构", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_1_4 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_4, DxfHelper.LINETYPE_SOLID_LINE, fwjg, o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);


            x_ = x_ + w * 1 / 8;
            Envelope cel_1_5 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_5, DxfHelper.LINETYPE_SOLID_LINE, "专有建筑面积", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_1_6 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_1_6, DxfHelper.LINETYPE_SOLID_LINE, FeatureHelper.Get(f_zd, "JZMJ", "").replaceAll("0*$", "") , o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x;
            y_ = y_ - h;
            Envelope cel_2_1 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_1, DxfHelper.LINETYPE_SOLID_LINE, "专有土地面积", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_2_2 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_2, DxfHelper.LINETYPE_SOLID_LINE, "/", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_2_3 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_3, DxfHelper.LINETYPE_SOLID_LINE, "分摊土地面积", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_2_4 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_4, DxfHelper.LINETYPE_SOLID_LINE, "/", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_2_5 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_5, DxfHelper.LINETYPE_SOLID_LINE, "总层数", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_2_6 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            List<Integer> max= new ArrayList<>();
            for(Feature zrz:fs_zrz){
                Integer lcs= FeatureHelper.Get(zrz, "ZCS", 1);
                max.add(lcs);
            }
            int maxcs=Collections.max(max);
            dxf.write(cel_2_6, DxfHelper.LINETYPE_SOLID_LINE, maxcs + "", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_2_7 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_7, DxfHelper.LINETYPE_SOLID_LINE, "分摊建筑面积", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_2_8 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_2_8, DxfHelper.LINETYPE_SOLID_LINE, "/", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x;
            y_ = y_ - h;
            Envelope cel_3_1 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_1, DxfHelper.LINETYPE_SOLID_LINE, "权利人", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_3_2 = new Envelope(x_, y_, x_ + w * 3 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_2, DxfHelper.LINETYPE_SOLID_LINE, FeatureHelper.Get(f_zd, "QLRXM", ""), o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);


            x_ = x_ + w * 3 / 8;
            Envelope cel_3_5 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_5, DxfHelper.LINETYPE_SOLID_LINE, "所在层次", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_3_6 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_6, DxfHelper.LINETYPE_SOLID_LINE, "1-" + maxcs, o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_3_7 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_7, DxfHelper.LINETYPE_SOLID_LINE, "建筑面积", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_3_8 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_3_8, DxfHelper.LINETYPE_SOLID_LINE, FeatureHelper.Get(f_zd, "JZMJ", "").replaceAll("0*$", ""), o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x;
            y_ = y_ - h;
            Envelope cel_4_1 = new Envelope(x_, y_, x_ + w * 1 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_4_1, DxfHelper.LINETYPE_SOLID_LINE, "坐落", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x_ + w * 1 / 8;
            Envelope cel_4_2 = new Envelope(x_, y_, x_ + w * 7 / 8, y_ - h, p_extend.getSpatialReference());
            dxf.write(cel_4_2, DxfHelper.LINETYPE_SOLID_LINE, FeatureHelper.Get(f_zd, "ZL", ""), o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_ = x;
            y_ = y_ - h;
            Envelope cel_5_1 = new Envelope(x_, y_, x_ + w, y_ - p_height + 4 * h, p_extend.getSpatialReference());
            dxf.write(cel_5_1);

            y_ = y_ - 3 * h;
            Envelope cel_3_1_1 = new Envelope(x_, y_, x_ + w * 1 / 2, y_ - w * 1 / 3, p_extend.getSpatialReference());
            if (cs.size() == 1) {
                cel_3_1_1 = new Envelope(x_, y_, x_ + w, y_ - w * 1/3, p_extend.getSpatialReference());
            } else if (cs.size() == 2) {
                cel_3_1_1 = new Envelope(x_, y_, x_ + w * 1 / 2, y_ - w * 1/3, p_extend.getSpatialReference());
            }
            writeC(cs, 0, cel_3_1_1);

            // 单元格3-2
            x_ = x_ + w * 1 / 2;
            Envelope cel_3_2_2 = new Envelope(x_, y_, x + w, y_ - w * 1 / 3, p_extend.getSpatialReference());
            if (cs.size() == 2) {
                cel_3_2_2 = new Envelope(x_, y_, x + w, y_ - w * 1/3, p_extend.getSpatialReference());
            }
            writeC(cs, 1, cel_3_2_2);

            // 单元格4-1
            x_ = x;
            y_ = y_ - w * 1 / 3;
            Envelope cel_4_1_1 = new Envelope(x_, y_, x_ + w * 1 / 2, y_ - w * 1 / 3, p_extend.getSpatialReference());
            writeC(cs, 2, cel_4_1_1);

            // 单元格4-2
            x_ = x_ + w * 1 / 2;
            Envelope cel_4_2_2 = new Envelope(x_, y_, x + w, y_ - w * 1 / 3, p_extend.getSpatialReference());
            writeC(cs, 3, cel_4_2_2);


            Point p_n = new Point(cel_5_1.getXMax() - o_split, cel_5_1.getYMax() - o_split);
            writeN(new Point(p_n.getX(), p_n.getY() - o_split, p_n.getSpatialReference()), o_split, -alpha);
        //右下角框
            x_=x+ w*7/16;
            y_=envelope.getYMin()+h*7/2;
            Envelope cel_10=new Envelope(x_,y_,x_+w/16,y_-h,p_extend.getSpatialReference());
            dxf.write(cel_10, DxfHelper.LINETYPE_SOLID_LINE, "房屋幢号", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_=x_+w*1/16;
            Envelope cel_11=new Envelope(x_,y_,x_+w/16,y_-h,p_extend.getSpatialReference());
            dxf.write(cel_11, DxfHelper.LINETYPE_SOLID_LINE, "房屋结构", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_=x_+w*1/16;
            Envelope cel_12=new Envelope(x_,y_,x_+w/16,y_-h,p_extend.getSpatialReference());
            dxf.write(cel_12, DxfHelper.LINETYPE_SOLID_LINE, "1层", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_=x_+w*1/16;
            Envelope cel_13=new Envelope(x_,y_,x_+w/16,y_-h,p_extend.getSpatialReference());
            dxf.write(cel_13, DxfHelper.LINETYPE_SOLID_LINE, "2层", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_=x_+w*1/16;
            Envelope cel_14=new Envelope(x_,y_,x_+w/16,y_-h,p_extend.getSpatialReference());
            dxf.write(cel_14, DxfHelper.LINETYPE_SOLID_LINE, "3层", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_=x_+w*1/16;
            Envelope cel_15=new Envelope(x_,y_,x_+w/16,y_-h,p_extend.getSpatialReference());
            dxf.write(cel_15, DxfHelper.LINETYPE_SOLID_LINE, "4层", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_=x_+w*1/16;
            Envelope cel_16=new Envelope(x_,y_,x_+w/16,y_-h,p_extend.getSpatialReference());
            dxf.write(cel_16, DxfHelper.LINETYPE_SOLID_LINE, "5层", o_fontsize, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_=x_+w*1/16;
            Envelope cel_17=new Envelope(x_,y_,x_+w/16,y_-h,p_extend.getSpatialReference());
            dxf.write(cel_17, DxfHelper.LINETYPE_SOLID_LINE, "幢占地\r\n"+"面积", o_fontsize*5/6, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            x_=x_+w*1/16;
            Envelope cel_18=new Envelope(x_,y_,x_+w/16,y_-h,p_extend.getSpatialReference());
            dxf.write(cel_18, DxfHelper.LINETYPE_SOLID_LINE, "幢建筑\r\n"+"面积", o_fontsize*5/6, o_fontstyle, false, DxfHelper.COLOR_BYLAYER, 0);

            y_ = y_ - h;
            Envelope cel_i_j=null;
            for (int i=0;i<5;i++) {
                x_=x+ w*7/16;
                String[] z =null;
                if (i<list_z.size()){
                    z = list_z.get(i);
                }
                for (int j = 0; j < 9; j++) {
                    cel_i_j = new Envelope(x_, y_, x_ + w / 16, y_ -h/2, p_extend.getSpatialReference());
                    if (z != null) {
                        try {
                            if (j==0){
                                dxf.write(cel_i_j, null, z[j], o_fontsize*5/6, null, false, 0, 0);
                            }else {
                                dxf.write(cel_i_j, null, z[j], o_fontsize*5/6, null, false, 0, 0);
                            }
                        } catch (Exception e) {
                            dxf.write(cel_i_j, null, "", o_fontsize*5/6, null, false, 0, 0);
                        }
                    } else {
                        dxf.write(cel_i_j, null, "", o_fontsize*5/6, null, false, 0, 0);
                    }
                    x_ = x_ + w / 16;
                }
                y_=y_-h/2;
            }
         //绘制单位
            x_=x;
            y_ = y - p_height;
            String hzdw = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZDW", "");
            Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
            Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
            dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw, "\n"), o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);

            Calendar c = Calendar.getInstance();
            String date = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
            String drawDate = "绘图日期:" + date;
            Point p_drawDate = new Point(x, envelope.getYMin() - h * 0.5, envelope.getSpatialReference());
            dxf.writeText(p_drawDate,  "测绘者：" + GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZR", ""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_BYLAYER, null, null);
            Point p_blc = new Point(envelope.getCenter().getX(), envelope.getYMin() - h * 0.5, envelope.getSpatialReference());
            dxf.writeText(p_blc, "1:" + (int) blc, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);
            Point p_chr = new Point(x + w, envelope.getYMin() - h * 0.5, envelope.getSpatialReference());
            dxf.writeText(p_chr,drawDate , o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, null, null);
        }
    }

    @Override
    protected void getFooter() throws Exception {

    }

    @Override
    public double getHeightScale() {
        return o_extend.getHeight() / p_height;
    }

    @Override
    public double getWidthScale() {
        return o_extend.getWidth() / (p_width - 4*h);
    }

    @Override
    public Envelope getChildExtend() {
        Geometry buffer = GeometryEngine.buffer(f_zd.getGeometry(), DxfHelper.getZdctBuffer());
        return MapHelper.geometry_get(buffer.getExtent(), spatialReference);
    }
    public double getPictureBoxWidth() {
        return 54.9d;
    }

    public double getPictureBoxHeight() {
        return 42d;
    }
    @Override
    public double getPictureBoxBufferFactor() {
        return 0.15;
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

    public String getFwjg(String fwjg) {
        if (fwjg == null) {
            fwjg = "[混]混合结构";
        }
        if (fwjg.contains("结构") && fwjg.contains("]")) {
            fwjg = fwjg.substring(fwjg.indexOf("]") + 1, fwjg.length() - 2);
        } else {
            fwjg = fwjg.substring(fwjg.indexOf("]") + 1);
        }

        return fwjg;
    }
    private void writeC(List<DxfFcfct.C> cs , int index , Envelope cell)throws Exception{
        if(index<cs.size()) {
            DxfFcfct.C c = cs.get(index);
            GeodeticDistanceResult d_move = null;
            List<Feature> fs = c.fs;

            List<Feature> fs_clone = MapHelper.cloneFeature(fs);
            List<Feature> fs_m = MapHelper.getFeatureSp(fs_clone,f_zd.getGeometry().getSpatialReference());

            Point p_c = MapHelper.Geometry_get(fs_m.get(0).getGeometry(), DxfHelper.POINT_TYPE_RIGHT_BOTTOM);
            List<Feature> fs_ = MapHelper.geometry_get(MapHelper.cloneFeature(fs), p_c, -alpha);

            Envelope e = MapHelper.geometry_combineExtents_Feature(fs);
            e = MapHelper.geometry_get(e, spatialReference); //  图形范围
            String lc = c.lc;
            if (!MapHelper.geometry_equals(e.getCenter(), cell.getCenter())) {
                d_move = GeometryEngine.distanceGeodetic(e.getCenter(), cell.getCenter(), MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
            }
            dxf.write(mapInstance, fs_, null, d_move, DxfHelper.TYPE_HUANGPI, DxfHelper.LINE_LABEL_OUTSIDE,new LineLabel());
            for (Feature f : fs_) {
                if (FeatureHelper.TABLE_NAME_H.equals(f.getFeatureTable().getTableName())){
                    Geometry g = MapHelper.geometry_get(f.getGeometry(), spatialReference);
                    g = MapHelper.geometry_move(g, d_move);
                    // lc
                    Point last = MapHelper.Geometry_get(g, DxfHelper.POINT_TYPE_LEFT_BOTTOM);
                    if (last!=null){
                        Geometry buffer = GeometryEngine.buffer(last, o_fontsize*2f);
                        Geometry intersectionG = GeometryEngine.intersection(g, buffer);
                        Envelope envelope = intersectionG.getExtent();
                        Point p = envelope.getCenter();
                        dxf.writeMText(p,"("+lc+")", o_fontsize* DxfHelper.FONT_SIZE_FACTOR , o_fontstyle, 0, 1, 2, DxfHelper.COLOR_CYAN, "JZD", "");
                    }
                }
            }
        }
    }
    public Envelope getPageExtend( int page){
        double m = page *(p_width+ 3*o_split);
        double x_min = p_extend.getXMin() + m;
        double x_max = p_extend.getXMax() + m;
        double y_min = p_extend.getYMin();
        double y_max = p_extend.getYMax();
        return  new Envelope( x_min,y_min , x_max,y_max ,p_extend.getSpatialReference());
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
}
