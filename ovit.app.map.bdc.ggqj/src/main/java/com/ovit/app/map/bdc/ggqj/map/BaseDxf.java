package com.ovit.app.map.bdc.ggqj.map;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.MapInstance;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfTemplet;
import com.ovit.app.util.gdal.dxf.DxfAdapter;
import com.ovit.app.util.gdal.dxf.DxfConstant;
import com.ovit.app.util.gdal.dxf.DxfHelper;
import com.ovit.app.util.gdal.dxf.DxfPaint;
import com.ovit.app.util.gdal.dxf.DxfRenderer;

import java.util.Arrays;
import java.util.Calendar;

public abstract class BaseDxf {


    /**
 * Time        : 2019/6/19
 * Author      : xw
 * Description :
 */


    protected MapInstance mapInstance;
    protected SpatialReference spatialReference;
    protected DxfAdapter dxf;
    protected String dxfpath;

    protected Feature f_zd;
    protected Point o_center;   // o 点
    protected Envelope o_extend;// 真实图形的范围
    protected double o_split = 2d;// 单元间隔
    protected double boxWidth;// 图框宽
    protected double boxHeight;// 图框高
    protected double k ;// 缓存系数
    protected double p_width ;// 页面宽  图形范围
    protected double p_height ;// 页面高  图形范围
    protected double blc = 200;
    protected double h = 1.5d; // 行高
    protected float o_fontsize = 0.5f;// 字体大小
    protected float o_fontWidth = 0.8f;// 字体大小
    protected double scale;
    protected String o_fontstyle = "宋体";// 字体大小
    protected Envelope p_extend;// 页面的范围
    protected DxfPaint paint =null;
    protected DxfRenderer dxfRenderer =null;


    public BaseDxf(MapInstance mapInstance) {
        this.mapInstance = mapInstance;
        set(MapHelper.getSpatialReference(mapInstance));
        init();
    }
    public void init() {
        initBox();
    }

    private void initDxfPaint() {
        if (paint == null) {
            dxfRenderer = new DxfRenderer();
            paint = dxfRenderer.getDxfPaint();
            paint.setColor(DxfHelper.COLOR_BYLAYER);
            paint.setFontsize(o_fontsize);
            paint.setFontWidth(o_fontWidth);
            paint.setFontstyle(o_fontstyle);
            paint.setTextAlign(DxfPaint.Align.CENTER);
            dxfRenderer.setDxfPaint(paint);

        }
    }

    private void initExtend() {
        o_extend = getChildExtend(); //    图形范围
        o_center = o_extend.getCenter(); // 中心点
        // 比例尺
        double h_s = getHeightScale();
        double v_s = getWidthScale();

        double niceScale_h = DxfHelper.getNiceScale(h_s);
        double niceScale_w = DxfHelper.getNiceScale(v_s);
        scale = niceScale_w > niceScale_h ? niceScale_w : niceScale_h;

        if (scale > 1) {
            p_width = p_width * scale;
            p_height = p_height * scale;
            boxWidth = boxWidth * scale;
            boxHeight = boxHeight * scale;
            h = h * scale;
            blc = blc * scale;
            o_split = o_split * scale;
            o_fontsize = (float) (o_fontsize * scale);
            o_fontWidth = (float) (o_fontWidth * scale);
        } else {
            scale = 1;
        }
        // 单元格范围
        p_extend = new Envelope(o_center,p_width,p_height);
    }
    private void initBox() {
        boxWidth = getPictureBoxWidth();
        boxHeight = getPictureBoxHeight();
        k = getPictureBoxBufferFactor();
        double dx =0d;
        if (boxHeight>boxWidth){
            p_width = (boxWidth / (1 + k));
            dx = boxWidth -p_width;
            p_height = boxHeight -dx;
        }else {
            p_height = (boxHeight / (1 + k));
            dx = boxHeight -p_height;
            p_width = boxWidth -dx;
        }
    }

    public BaseDxf set(String dxfpath) {
        this.dxfpath = dxfpath;
        return this;
    }
    public BaseDxf set(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
        return this;
    }

    public BaseDxf write() throws Exception {
        initExtend();
        initDxfPaint();
        if (dxf == null) {
            dxf = DxfAdapter.getInstance();
            // 创建dxf
            dxf.create(dxfpath, p_extend, spatialReference).setDxfRenderer(dxfRenderer);
        }
        try {
            getDxfRenderer();
            getHeader();
            getBody();
            getFooter();
        } catch (Exception es) {
            dxf.error("生成失败", es, true);
        }
        return this;
    }
    // 获取范围
    public Envelope getExtend() {
        return p_extend;
    }
    public BaseDxf save() throws Exception {
        if (dxf != null) {
            dxf.save();
            dxf=null;
        }
        return this;
    }
    /**
     * 获取heaad内容
     */
    protected  void getDxfRenderer() {

    }

    /**
     * 获取heaad内容
     */
    protected abstract void getHeader() throws Exception;

    /**
     * 获取主体内容
     */
    protected abstract void getBody() throws Exception;
    /**
     * 获取主体内容
     */
    protected abstract void getFooter() throws Exception;

    public abstract double getHeightScale();

    public abstract double getWidthScale();

    public abstract Envelope getChildExtend();

    public double getPictureBoxWidth() {
        return 42d;
    }

    public double getPictureBoxHeight() {
        return 59.4d;
    }

    public double getPictureBoxBufferFactor() {
        return 0.15d;
    }

    public DxfAdapter  getDxf(){
        return dxf;
    };

    public void getDefultFooter() throws Exception {
        paint.setLayer(DxfConstant.DXF_LAYER_BKZJ);
        Envelope envelope = p_extend;
        double x=envelope.getXMin();
        double y=envelope.getYMax();
        double x_ =  p_extend.getXMin();
        double y_ = y - p_height;
        double w=p_width;
        String hzdw = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZDW", "");
        Envelope cel_4_0 = new Envelope(x_ - o_split, y_ + (hzdw.length() * o_fontsize) * 1.8, x_, y_, p_extend.getSpatialReference());
        Point p_4_0 = new Point(cel_4_0.getCenter().getX(), cel_4_0.getCenter().getY(), p_extend.getSpatialReference());
        dxf.writeMText(p_4_0, StringUtil.GetDxfStrFormat(hzdw, "\n"), o_fontsize, o_fontstyle, 0, 0, 3, 0, null, null);

        Calendar c = Calendar.getInstance();
        String auditDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH) + "日");
        c.add(Calendar.DATE, -1);
        String drawDate = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + (c.get(Calendar.DAY_OF_MONTH) + "日");
        Point p_jxf = new Point(x, envelope.getYMin() - h * o_fontsize, envelope.getSpatialReference());
        Point p_auditDate = new Point(x, envelope.getYMin() -  h * (o_fontsize*2) , envelope.getSpatialReference());
        Point p_drawDate = new Point(x, envelope.getYMin() -  h * (o_fontsize*3) , envelope.getSpatialReference());
        String tjsj = StringUtil.substr(auditDate,0,auditDate.indexOf("月")+1); // 图解时间
        dxf.writeText(p_jxf, tjsj+"解析法测绘界址点", o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_CYAN, paint.getLayer(), paint.getStbm());
        dxf.writeText(p_drawDate, "绘图日期:" + drawDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_CYAN, paint.getLayer(), paint.getStbm());
        dxf.writeText(p_auditDate, "审核日期:" + auditDate, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 0, 2, DxfHelper.COLOR_CYAN, paint.getLayer(), paint.getStbm());

        Point p_blc = new Point(envelope.getCenter().getX(), envelope.getYMin() - h * 0.5, envelope.getSpatialReference());
        dxf.writeText(p_blc, "1:" + (int) blc, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 1, 2, DxfHelper.COLOR_CYAN, paint.getLayer(), paint.getStbm());



        String hzr = GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR","");
        String shr = GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR","");

        if(hzr.length()>shr.length()){
            shr = getNiceString(shr,hzr.length()-shr.length());
        }else if (hzr.length()< shr.length()){
            hzr = getNiceString(hzr,shr.length()-hzr.length());
        }

        Point p_chr = new Point(x + w, envelope.getYMin() - h * 0.5, envelope.getSpatialReference());
        dxf.writeText(p_chr, "制图员：" + hzr, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_CYAN, null, null);
        Point p_shr = new Point(x + w, envelope.getYMin() -  h * (o_fontsize*2), envelope.getSpatialReference());
        dxf.writeText(p_shr, "审核员：" +shr, o_fontsize, DxfHelper.FONT_WIDTH_DEFULT, o_fontstyle, 0, 2, 2, DxfHelper.COLOR_CYAN, null, null);

    }

    public void getDefultHeader() throws Exception {
        //  图形范围
        DxfPaint.Align align = paint.getTextAlign();
        Envelope envelope = p_extend;
        paint.setLayer(DxfConstant.DXF_LAYER_TK);
        paint.setFontstyle(DxfHelper.FONT_STYLE_HZ);
        paint.setLinewidth(DxfHelper.LINE_WIDTH_3);

//        dxf.write(new Envelope(envelope.getCenter(), boxWidth, boxHeight)); // 图框
        writeDaYingKuang(new Envelope(envelope.getCenter(), boxWidth, boxHeight),o_split,spatialReference);
        paint.setLayer(DxfConstant.DXF_LAYER_BKZJ);
        paint.setFontstyle(DxfHelper.FONT_STYLE_SONGTI);
        paint.setFontsize(o_fontsize*2);
        paint.setLinewidth(DxfHelper.LINE_WIDTH_DEFULT);
        dxf.write(p_extend, "");
        Point p_title = new Point(envelope.getCenter().getX(), envelope.getYMax() + h, spatialReference);
        dxf.writeText(p_title, "宗地图", paint);
        paint.setFontsize(o_fontsize*0.8f);
        Point p_unit = new Point(envelope.getXMax(), envelope.getYMax() + h * 0.5, spatialReference);
        paint.setTextAlign(DxfPaint.Align.RIGHT);
        dxf.writeText(p_unit, "单位：m·㎡ ", paint);
        paint.setTextAlign(align);
        paint.setFontsize(o_fontsize);
    }
    public String getRQ(String s) {
        String rq = "";
        if (StringUtil.IsNotEmpty(s) && s.length() >= 10) {
            String year = StringUtil.substr(s, 0, 4);
            String month = StringUtil.substr(s, 5, 7);
            String day = StringUtil.substr(s, 8, 10);
            rq = year + "年" + month + "月" + day + "日";
        } else {
            Calendar calendar = Calendar.getInstance();
            rq = calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + (calendar.get(Calendar.DAY_OF_MONTH) + "日");
        }

        return rq;
    }
    public String getNiceString(String s,int j) {
        StringBuilder result = new StringBuilder();
        result.append(s);
        for (int i = 0; i < j; i++) {
            result.append("  ");
        }
        return result.toString();
    }

    /**
     * @param dxf  dxfAdapter 对象
     * @param f_zd 宗地
     */
    public  void writeZdsz(DxfAdapter dxf, Feature f_zd){
        try {
            String layerName = paint.getLayer();
            paint.setLayer(DxfConstant.DXF_LAYER_ZDSZZJ);
            Envelope e = GeometryEngine.buffer(f_zd.getGeometry(),5).getExtent();
            double e_height = e.getHeight();
            Point point_d = new Point(e.getXMax() + o_split, e. getYMin() + e_height / 2, spatialReference);
            Point point_x = new Point(e.getXMin() - o_split, e.getYMin() + e_height / 2, spatialReference);
            Point point_b = new Point(e.getCenter().getX(), e.getYMax() + o_split , spatialReference);
            Point point_n = new Point(e.getCenter().getX(), e.getYMin() - o_split ,spatialReference);
            dxf.writeText(point_n, FeatureHelper.Get(f_zd, "ZDSZN", ""));
            dxf.writeText(point_b, FeatureHelper.Get(f_zd, "ZDSZB", ""));
            dxf.writeMText(point_d, StringUtil.GetDxfStrFormat(FeatureHelper.Get(f_zd, "ZDSZD", ""), "\n"));
            dxf.writeMText(point_x, StringUtil.GetDxfStrFormat(FeatureHelper.Get(f_zd, "ZDSZX", ""), "\n"));
            paint.setLayer(layerName);
        }catch (Exception exception){
            ToastMessage.Send("绘制宗地四至失败");
        }
    }

    public void writeDaYingKuang( Envelope envelope, double o_split, SpatialReference spatialReference) throws Exception {
        Point p_t_r = new Point(envelope.getXMin() - 0.7 * o_split, envelope.getYMax() + 0.87 * o_split, spatialReference);
        dxf.writeLine(Arrays.asList(new Point[]{new Point(p_t_r.getX(), p_t_r.getY()), new Point(p_t_r.getX(), p_t_r.getY() + 3)}), DxfHelper.LINETYPE_SOLID_LINE, false, DxfHelper.COLOR_BYLAYER, 0,paint.getLayer(),"");
        dxf.writeLine(Arrays.asList(new Point[]{new Point(p_t_r.getX() - 3, p_t_r.getY()), new Point(p_t_r.getX(), p_t_r.getY())}), DxfHelper.LINETYPE_SOLID_LINE, false, DxfHelper.COLOR_BYLAYER, 0,paint.getLayer(),"");
        Point p_r_b = new Point(envelope.getXMax() + 0.7 * o_split, envelope.getYMin() - 1.1 * o_split, spatialReference);
        dxf.writeLine(Arrays.asList(new Point[]{new Point(p_r_b.getX() + 3, p_r_b.getY()), new Point(p_r_b.getX(), p_r_b.getY())}), DxfHelper.LINETYPE_SOLID_LINE, false, DxfHelper.COLOR_BYLAYER, 0,paint.getLayer(),"");
        dxf.writeLine(Arrays.asList(new Point[]{new Point(p_r_b.getX(), p_r_b.getY()), new Point(p_r_b.getX(), p_r_b.getY() - 3)}), DxfHelper.LINETYPE_SOLID_LINE, false, DxfHelper.COLOR_BYLAYER, 0,paint.getLayer(),"");
    }
    public Envelope getPageExtend(int page) {
        double m = page *(p_width+3*o_split);
        double x_min = p_extend.getXMin() + m;
        double x_max = p_extend.getXMax() + m;
        double y_min = p_extend.getYMin();
        double y_max = p_extend.getYMax();
        return  new Envelope( x_min,y_min , x_max,y_max ,p_extend.getSpatialReference());
    }

    protected void writeN(Point p, double w, float alpha) throws Exception {
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
        dxf.writeLine(DxfTemplet.Get_POLYLINE(MapHelper.geometry_getPoints(geometry), "Continuous", 0, 0, "0", "", "JMD"));
    }

}
