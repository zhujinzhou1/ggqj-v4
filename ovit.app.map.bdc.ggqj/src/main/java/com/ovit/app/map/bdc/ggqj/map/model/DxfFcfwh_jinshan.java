package com.ovit.app.map.bdc.ggqj.map.model;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.exception.CrashHandler;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZRZ;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.LineLabel;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.custom.FeatureHelper.Get;

/**
 * Created by Lichun on 2018/6/20.
 */

public class DxfFcfwh_jinshan {
    MapInstance mapInstance ;
    SpatialReference spatialReference;

    DxfAdapter dxf  ;
    private  String dxfpath;
    private  String bdcdyh;
    private  List<Feature> fs_all;
    private  List<Feature> fs_hAndFs;

    private  Feature f_zd;
    private  List<Feature> fs_zrz;
    private  List<Feature> fs_z_fsjg;
    private  List<Feature> fs_h;
    private  List<Feature> fs_h_fsjg;

    private Point o_center ;   // o 点
    private Envelope o_extend ;// 真实图形的范围
    private double o_width ;// 真实图形的宽
    private double p_width=36 ;// 真实图形的宽
    private double p_height ;// 真实图形的宽
    private double o_split=2d ;// 单元间隔
    private float o_fontsize=0.6f ;// 字体大小
    private  double h = 1.5d; // 行高
    private String o_fontstyle = "宋体" ;// 字体大小
    private double scale;


    private Envelope p_extend ;// 页面的范围
    private  double p_distence ;// 页面间距

    private Envelope ps_extend ;// 全部的范围


    public DxfFcfwh_jinshan(MapInstance mapInstance){
        this.mapInstance  = mapInstance;
        set(mapInstance.aiMap.getProjectWkid());
    }


    public DxfFcfwh_jinshan set(String dxfpath){
       this.dxfpath = dxfpath;
       return  this;
    }

    public DxfFcfwh_jinshan set(SpatialReference spatialReference){
        this.spatialReference = spatialReference;
        return  this;
    }

    public DxfFcfwh_jinshan set(String bdcdyh, Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_h_fsjg){
        this.bdcdyh = bdcdyh;
        this.f_zd = f_zd;
        this.fs_zrz = fs_zrz;
        this.fs_z_fsjg = fs_z_fsjg;
        this.fs_h = fs_h;
        this.fs_h_fsjg = fs_h_fsjg;

        fs_hAndFs = new ArrayList<>();
        fs_hAndFs.addAll(fs_z_fsjg);
        fs_hAndFs.addAll(fs_h);
        fs_hAndFs.addAll(fs_h_fsjg);

        fs_all = new ArrayList<>();
        fs_all.addAll(fs_zrz);
        fs_all.addAll(fs_hAndFs);
        return this;
    }


    // 获取范围
    public Envelope getExtend(){
        o_extend = MapHelper.geometry_combineExtents_Feature(fs_all);
        o_extend = MapHelper.geometry_get(o_extend,spatialReference); //  图形范围
        o_center= o_extend.getCenter(); // 中心点
        o_width = o_extend.getWidth()>o_extend.getHeight()?o_extend.getWidth():o_extend.getHeight();// 图形宽

         scale = o_width *(1+0.1)*2/p_width;
         scale= DxfHelper.getNiceScale(scale);

        if (scale>1){
            o_fontsize = (float) (o_fontsize*scale);
            o_split=o_split*scale;
            p_width=p_width*scale;
            h=h*scale;
        }
         p_height=p_width+8*h;
          // 页面范围
        p_extend = new Envelope(o_center,p_width,p_height );
        p_distence = p_extend.getWidth()+o_split;
        return p_extend;
    }

    // 获取页面范围  page 从0开始
    public Envelope getPagesExtend( int pagecount){

        double x_min = p_extend.getXMin();
        double x_max = p_extend.getXMax() + pagecount * p_distence ;
        double y_min = p_extend.getYMin();
        double y_max = p_extend.getYMax();
        ps_extend = new Envelope( x_min,y_min , x_max,y_max ,p_extend.getSpatialReference());
        return ps_extend;
    }

    // 获取页面范围  page 从0开始
    public Envelope getPageExtend( int page){
        double m = page *(p_width+3*o_split);
        double x_min = p_extend.getXMin() + m;
        double x_max = p_extend.getXMax() + m;
        double y_min = p_extend.getYMin();
        double y_max = p_extend.getYMax();
        return  new Envelope( x_min,y_min , x_max,y_max ,p_extend.getSpatialReference());
    }

    // 获取页面范围  page 从1开始
    public <T extends Geometry> T getPage(T g , int page){
        if(page>0){
            double m = page * p_distence;
            return MapHelper.geometry_move(g,m,90);
        }
        return  g;
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
        public String getZcshz(String cshz){
            int zh_i =   AiUtil.GetValue(StringUtil.getTextOnlyIn(zh,"0123456789"),0);
            String  name  = zh;
            if(zh_i>0){name =  zh_i+"";}
            name = name.replace("幢","") +"幢"+ FeatureHelper.Get(zrz,"ZCS",1)+"层";
            if (!cshz.contains(name)){
                zcshz+=name+"\n"  ;
            }
            return zcshz;
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
    private String zcshz="";
    public DxfFcfwh_jinshan write() throws Exception{
       getExtend(); // 多大范围
        ArrayList<C> cs = new ArrayList<>();
        for(Feature f_zrz :fs_zrz){
            String zrzh = Get(f_zrz,"ZRZH","");
            String zh = Get(f_zrz,"ZH","");
            List<Feature> fs = new ArrayList<>();
            for(Feature f :fs_hAndFs){
                String id = Get(f,"ID","");
                if(id.startsWith(zrzh)){
                    fs.add(f);
                }
            }
            ArrayList<Map.Entry<String, List<Feature>>> fs_s =  FeatureViewZRZ.GroupbyC_Sort(fs);
            for(Map.Entry<String, List<Feature>> c :fs_s){
                cs.add(new C(zh,f_zrz,c.getKey(),c.getValue()));
            }
        }
        int page_count = (int)Math.ceil(cs.size()/4f);  //  多少页
        getPagesExtend(page_count);

        if(dxf==null) {
            dxf = new DxfAdapter();
            // 创建dxf
            dxf.create(dxfpath, ps_extend,spatialReference).setFontSize(o_fontsize);
        }
        try {
            for(int page = 0;page< page_count;page++){

                Envelope envelope = getPageExtend(page);
                dxf.write(new Envelope(envelope.getCenter(), p_width + o_split * 3, p_height + o_split * 5)); // 图框

                Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + o_split * 1, envelope.getSpatialReference());
                //京山 头部名称
                //dxf.writeText(p_title, "房产分层(分户)图", o_fontsize*2, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);
                //耒阳 房产分层图
                dxf.writeText(p_title, "房产分层图", o_fontsize*2, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_BYLAYER, null, null);

                Point p_unit = new Point(envelope.getXMax() , envelope.getYMax() + o_split*0.5, envelope.getSpatialReference());
                //京山 单位
                //dxf.writeText(p_unit, "单位：米·平方米", o_fontsize * 0.8f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, null, null);
                //耒阳 房产分层图
                dxf.writeText(p_unit, "单位：m·㎡ ", o_fontsize * 0.8f, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_BYLAYER, null, null);
                double w = p_width; //行宽
                // 左上角
                double x = envelope.getXMin();
                double y = envelope.getYMax();
                // 左上角
                double x_ = x;
                double y_ = y;

                // 单元格1-1
                Envelope cel_1_1 = new Envelope(x_,y_,x_+w* 1/6,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_1_1,null,"不动产单元号",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);
                // 单元格1-2
                x_ = x_+w* 1/6;
                Envelope cel_1_2 = new Envelope(x_,y_,x+w,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_1_2,null,bdcdyh,o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格2-1
                x_ = x; y_ = y_-h;
                Envelope cel_2_1 =  new Envelope(x_,y_,x_+w* 1/6,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_2_1,null,"坐落",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格2-2
                x_ = x_+w* 1/6;
                Envelope cel_2_2 = new Envelope(x_,y_,x+w,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_2_2,null,Get(f_zd,"ZL",""),o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格3-1
                x_ = x; y_ = y_-h;
                Envelope cel_3_1 = new Envelope(x_,y_,x_+w* 1/2,y_-w* 1/2,envelope.getSpatialReference()) ;
                dxf.write(cel_3_1);
                writeC(cs,0+page*4,cel_3_1);

                // 单元格3-2
                x_ = x_+w* 1/2;
                Envelope cel_3_2 =  new Envelope(x_,y_,x+w,y_-w* 1/2,envelope.getSpatialReference()) ;
                dxf.write(cel_3_2);
                writeC(cs,1+page*4,cel_3_2);


                // 单元格4-1
                x_ = x; y_ = y_-w* 1/2;
                Envelope cel_4_1 =   new Envelope(x_,y_,x_+w* 1/2,y_-w* 1/2,envelope.getSpatialReference()) ;
                dxf.write(cel_4_1);
                writeC(cs,2+page*4,cel_4_1);

                // 单元格4-2
                x_ = x_+w* 1/2;
                Envelope cel_4_2 = new Envelope(x_,y_,x+w,y_-w* 1/2,envelope.getSpatialReference()) ;
                dxf.write(cel_4_2);
                writeC(cs,3+page*4,cel_4_2);


                // 单元格5-1
                x_ = x; y_ = y_-w* 1/2;
                Envelope cel_5_1 =  new Envelope(x_,y_,x_+w* 1/6,y_-h,envelope.getSpatialReference()) ;
//                dxf.write(cel_5_1,"幢  号");
                dxf.write(cel_5_1,null,"幢  号",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);


                // 单元格5-2
                x_ = x_+w* 1/6;
                Envelope cel_5_2 = new Envelope(x_,y_,x+w* 1/2,y_-h,envelope.getSpatialReference()) ;
                String bdcdyh = FeatureHelper.Get(fs_zrz.get(0), FeatureHelper.TABLE_ATTR_BDCDYH, "");
                dxf.write(cel_5_2,null,bdcdyh.contains("F9999")?"F9999":"F0001" ,o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格5-3
                x_ = x+w* 1/2;
                Envelope cel_5_3 = new Envelope(x_,y_,x_+w* 1/6,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_5_3,null,"户  号",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格5-4
                x_ = x_+w* 1/6;
                Envelope cel_5_4 = new Envelope(x_,y_,x+w,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_5_4,null,"0001",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格6-1
                x_ = x; y_ = y_-h;
                Envelope cel_6_1 =  new Envelope(x_,y_,x_+w* 1/6,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_6_1,null,"房屋结构",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格6-2
                x_ = x_+w* 1/6;
                Envelope cel_6_2 = new Envelope(x_,y_,x+w* 1/2,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_6_2,null,getFwjg(cs,page),o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格6-3
                x_ = x+w* 1/2;
                Envelope cel_6_3 = new Envelope(x_,y_,x_+w* 1/6,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_6_3,null,"建筑面积（㎡）",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格6-4
                x_ = x_+w* 1/6;
                Envelope cel_6_4 = new Envelope(x_,y_,x+w,y_-h,envelope.getSpatialReference()) ;

                // 单元格7-0
                x_ = x; y_ = y_-h;
                Envelope cel_7_0 =  new Envelope(x_-w* 1/6*1/4,y_+2*h,x_,y_-4*h,envelope.getSpatialReference()) ;
                Point p_7_0=new Point(cel_7_0.getCenter().getX(),cel_7_0.getCenter().getY(),envelope.getSpatialReference());

                x_ = x;
                Envelope cel_7_1 =  new Envelope(x_,y_,x_+w* 1/6,y_-4*h,envelope.getSpatialReference()) ;
                dxf.write(cel_7_1,null,"总层数",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);


                // 单元格7-2
                x_ = x_+w* 1/6;
                Envelope cel_7_2 = new Envelope(x_,y_,x+w* 1/2,y_-4*h,envelope.getSpatialReference()) ;
//                dxf.write(cel_7_2,""); //  在后面汇总了写

                // 单元格7-3
                x_ = x+w* 1/2;
                Envelope cel_7_3 = new Envelope(x_,y_,x_+w* 1/6,y_-4*h,envelope.getSpatialReference()) ;
//                dxf.write(cel_7_3,"各层面积（m2）");
                dxf.write(cel_7_3,null,"各层面积（㎡）",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);


                // 单元格7-4
                x_ = x_+w* 1/6;
                Envelope cel_7_4 = new Envelope(x_,y_,x_+w* 1/6,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_7_4,null,getCName(cs,0+page*4),o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格7-5
                x_ = x_+w* 1/6;
                Envelope cel_7_5 = new Envelope(x_,y_,x+w,y_-h,envelope.getSpatialReference()) ;
                double jzmj_c1 =  getCJZMJ(cs,0+page*4);
                dxf.write(cel_7_5,null,jzmj_c1>0?(AiUtil.GetValue(jzmj_c1 ,"", AiUtil.F_FLOAT2)):"",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);


                // 单元格8-4
                x_ = x+w*4/6; y_ = y_-h;
                Envelope cel_8_4 = new Envelope(x_,y_,x_+w* 1/6,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_8_4,null,getCName(cs,1+page*4),o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格8-5
                x_ = x_+w* 1/6;
                Envelope cel_8_5 = new Envelope(x_,y_,x+w,y_-h,envelope.getSpatialReference()) ;
                double jzmj_c2 =  getCJZMJ(cs,1+page*4);
                dxf.write(cel_8_5,null,jzmj_c2>0?(AiUtil.GetValue(jzmj_c2 ,"", AiUtil.F_FLOAT2)):"",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格9-4
                x_ = x+w*4/6; y_ = y_-h;
                Envelope cel_9_4 = new Envelope(x_,y_,x_+w* 1/6,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_9_4,null,getCName(cs,2+page*4),o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格9-5
                x_ = x_+w* 1/6;
                Envelope cel_9_5 = new Envelope(x_,y_,x+w,y_-h,envelope.getSpatialReference()) ;
                double jzmj_c3 =  getCJZMJ(cs,2+page*4);
                dxf.write(cel_9_5,null,jzmj_c3>0?(AiUtil.GetValue(jzmj_c3 ,"", AiUtil.F_FLOAT2)):"",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格10-4
                x_ = x+w*4/6; y_ = y_-h;
                Envelope cel_10_4 = new Envelope(x_,y_,x_+w* 1/6,y_-h,envelope.getSpatialReference()) ;
                dxf.write(cel_10_4,null, getCName(cs,3+page*4),o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格10-5
                x_ = x_+w* 1/6;
                Envelope cel_10_5 = new Envelope(x_,y_,x+w,y_-h,envelope.getSpatialReference()) ;
                double jzmj_c4 =  getCJZMJ(cs,3+page*4);
                dxf.write(cel_10_5,null,jzmj_c4>0?(AiUtil.GetValue(jzmj_c4 ,"", AiUtil.F_FLOAT2)):"",o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格6-4 汇总值 建筑面积汇总
                double jzmj_c  =jzmj_c1+jzmj_c2+jzmj_c3+jzmj_c4;
                dxf.write(cel_6_4,null,jzmj_c>0?(AiUtil.GetValue(jzmj_c,"", AiUtil.F_FLOAT2)):"",o_fontsize,null,false, DxfHelper.COLOR_BYLAYER,0);

                // 单元格7-2 总层数汇总值
                double zcs_hz =jzmj_c1+jzmj_c2+jzmj_c3+jzmj_c4;
                dxf.write(cel_7_2,null,zcshz,o_fontsize,o_fontstyle,false, DxfHelper.COLOR_BYLAYER,0);
                zcshz="";

                x_=x;
                y_ =y_-h;
                String hzdw =  GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZDW","");
                Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
                Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
                dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw,"\n"), o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);
                //京山版本的底部(绘图日期和审核日期在左)
               /* Calendar c = Calendar.getInstance();
                String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
                c.add(Calendar.DATE, -1);
                String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");

                Point p_auditDate = new Point(envelope.getCenter().getX()- w * 1 / 3-w * 1 / 20, envelope.getYMin() -3* o_split * 0.3, envelope.getSpatialReference());
                Point p_drawDate = new Point(envelope.getCenter().getX()- w * 1 / 3-w * 1 / 20 , envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
                dxf.writeText(p_drawDate, "绘图日期:"+drawDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
                dxf.writeText(p_auditDate,"审核日期:"+auditDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                Point p_blc = new Point(envelope.getCenter().getX() , envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
                dxf.writeText(p_blc, "1:200", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                Point p_chr = new Point(envelope.getCenter().getX() + w * 1 / 3+w*1/10, envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
                dxf.writeText(p_chr, "测绘员："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
                Point p_shr = new Point(envelope.getCenter().getX() + w * 1 / 3+w*1/10, envelope.getYMin() - 3*o_split * 0.3, envelope.getSpatialReference());
                dxf.writeText(p_shr, "审核员："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
*/
               //耒阳的绘图日期和审核日期在右  测绘人和审核在左
                Calendar c = Calendar.getInstance();
                String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");
                c.add(Calendar.DATE, -1);
                String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH)+"日");

                Point p_auditDate = new Point(envelope.getCenter().getX()+ w * 1 / 3, envelope.getYMin() -3* o_split * 0.3, envelope.getSpatialReference());
                Point p_drawDate = new Point(envelope.getCenter().getX()+ w * 1 / 3 , envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
                dxf.writeText(p_drawDate, "绘图日期:"+drawDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
                dxf.writeText(p_auditDate,"审核日期:"+auditDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                Point p_blc = new Point(envelope.getCenter().getX() , envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
                dxf.writeText(p_blc, "1:200", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);

                Point p_chr = new Point(envelope.getCenter().getX() - w * 1 / 3-w * 1 / 20, envelope.getYMin() - o_split * 0.3, envelope.getSpatialReference());
                dxf.writeText(p_chr, "制图人："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
                Point p_shr = new Point(envelope.getCenter().getX() - w * 1 / 3-w * 1 / 20, envelope.getYMin() - 3*o_split * 0.3, envelope.getSpatialReference());
                dxf.writeText(p_shr, "审核人："+ GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR",""), o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, 0, null, null);
            }

        }catch (Exception es){
            dxf.error("生成失败",es,true);
        }
        return this;
    }
    public String getCName(List<C> cs,int index){
        if(index<cs.size()) {
            C c = cs.get(index);
            return  c.getName();
        }
        return  "";
    }
    public double getCJZMJ(List<C> cs,int index){
        if(index<cs.size()) {
            C c = cs.get(index);
           return  c.getJzmj();
        }
        return  0;
    }
    public String getFwjg(List<C> cs ,int page){
        List<String > fwjgs = new ArrayList<>();
        String value=null;
        for(int i = page*4;i<cs.size()&& i<(page*4+4);i++){
            try {
                String fwjg= DicUtil.dic("fwjg", FeatureHelper.Get(cs.get(i).zrz,"FWJG",""));
                fwjg=fwjg!=null?fwjg: DicUtil.dic("fwjg_e", FeatureHelper.Get(cs.get(i).zrz,"FWJG",""));
                value  = StringUtil.substr_last( FeatureHelper.Get(cs.get(i).zrz,"ZRZH",""),1)+"幢"
                        + fwjg
                        .substring(0,fwjg.lastIndexOf("]")+1);

            }catch (Exception e){
                ToastMessage.Send("幢的房屋结构为空,自然幢号：" + FeatureHelper.Get(cs.get(i).zrz,"ZRZH",""));
                CrashHandler.WriteLog("幢的房屋结构为空", "自然幢号：" +  FeatureHelper.Get(cs.get(i).zrz,"ZRZH",""));
            }
                  if(StringUtil.IsNotEmpty(value)&&!fwjgs.contains(value)) {
              fwjgs.add(value);
          }
        }
        return  StringUtil.Join(fwjgs,"、",true);
    }

    // 写每层的图形
    private void writeC(List<C> cs ,int index ,Envelope cell)throws Exception{
        if(index<cs.size()) {
            C c = cs.get(index);
            List<Feature> fs = c.fs;//, int type,int labelDierction,LineLabel lineLabel)
            Envelope e = MapHelper.geometry_combineExtents_Feature(fs);
            e = MapHelper.geometry_get(e,spatialReference); //  图形范围

            GeodeticDistanceResult d_move = null;
            if (!MapHelper.geometry_equals(e.getCenter(), cell.getCenter())) {
                d_move = GeometryEngine.distanceGeodetic(e.getCenter(), cell.getCenter(), MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
            }

            dxf.write(mapInstance, fs, null, d_move, DxfHelper.TYPE_DEFULT, DxfHelper.LINE_LABEL_OUTSIDE,new LineLabel());
            Point p = new Point(cell.getXMin() + cell.getWidth() / 2, cell.getYMin() + o_split / 2);
            String text = c.getName() + "平面图";
            zcshz = c.getZcshz(zcshz);
            dxf.write(p, null, text, o_fontsize, null, false, 0, 0);

            int scale = MapHelper.geometry_scale(mapInstance.map, fs_zrz.get(0).getGeometry(), 1.0f);
            double s_d_m = (scale / 100);
            String s_d_m_ = String.format("%.1f", s_d_m).replace(".0", "");
            String scale_1 = "1:" + scale;
            String s_d_m_3 = s_d_m_ + "米";

            double ft = 0.4;
            Point p_blc = new Point(cell.getXMin() + cell.getWidth() / 4 / 2, cell.getYMax() - o_split / 2);
            dxf.write(p_blc, null, scale_1, o_fontsize, null, false, 0, 0);

            dxf.writeLine(Arrays.asList(new Point[]{new Point(p_blc.getX() - Integer.parseInt(s_d_m_) * 0.5, p_blc.getY() - 1.5 * o_fontsize), new Point(p_blc.getX() + 0.5 * Integer.parseInt(s_d_m_), p_blc.getY() - 1.5 * o_fontsize)}), "", false, 0, 0);
            dxf.write(new Point(p_blc.getX(), p_blc.getY() - 3 * o_fontsize), null, s_d_m_3, o_fontsize, null, false, 0, 0);
        }
        Point p_n = new Point(cell.getXMax() - o_split/2 ,cell.getYMax()- o_split);
        dxf.write(p_n,null,"N",o_fontsize*0.8f,null,false,0,0);
        writeN(new Point(p_n.getX(),p_n.getY()-o_split,p_n.getSpatialReference()),o_split);
    }

    private void writeN(Point p , double w)throws Exception{
        PointCollection ps = new PointCollection(p.getSpatialReference());
        ps.add(p);
        ps.add(new Point(p.getX()-w/6,p.getY()-w/2));
        ps.add(new Point(p.getX(),p.getY()+w/2));
        ps.add(new Point(p.getX()+w/6,p.getY()-w/2));
        dxf.write(new Polygon(ps,p.getSpatialReference()),null);
    }

    public DxfFcfwh_jinshan save()throws Exception{
        if(dxf!=null){
            dxf.save();
        }
        return this;
    }
}
