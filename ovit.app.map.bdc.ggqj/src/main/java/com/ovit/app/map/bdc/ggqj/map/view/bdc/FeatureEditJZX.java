package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.ImmutablePart;
import com.esri.arcgisruntime.geometry.Multipart;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.Segment;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LiuSheng on 2017/7/28.
 */

public class FeatureEditJZX extends FeatureEdit {
    final static String TAG = "FeatureEditJZX";

    public FeatureEditJZX(){ super();}
    public FeatureEditJZX(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }

    //region  重写父类方法


    // 显示数据
    @Override
    public void build() {
     final   LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_jzx, v_content);
        try {
            if (feature != null) {
                mapInstance.fillFeature(feature);
                fillView(v_feature);
            }
        } catch (Exception es) {
            Log.e(TAG, "build: 构建失败", es);
        }
    }
    // 保存数据
    @Override
    public void update(final AiRunnable callback) {
        try {
            super.update(callback);
        } catch (Exception es) {
            ToastMessage.Send(activity, "更新属性失败!", TAG, es);
        }
    }
    // endregion
    public static FeatureTable GetTable(MapInstance mapInstance) {
        return MapHelper.getLayer(mapInstance.map, "JZX", "界址线").getFeatureTable();
    }


    // 相关宗地代码
    public static List<String> GetRefZDDM(List<Feature> fs_zd,Point p1 ,Point p2 ){
        List<String> zddms = new ArrayList<String>();
        for(Feature f :fs_zd){
            PointCollection ps = MapHelper.geometry_getPoints(f.getGeometry());
            int i1=MapHelper.geometry_list_findIndex(ps,p1);
            int i2=MapHelper.geometry_list_findIndex(ps,p2);
            // 包含两个点，且挨着
            if(i1>-1 && i2>-1 && Math.abs(i1-i2)==1){
                String zddm =  FeatureEditZD.GetID(f);
                if(StringUtil.IsNotEmpty(zddm)) {
                    zddms.add(zddm);
                }
            }
        }
        return zddms;
    }

    public static List<String>  GetRefZDDM( List<Feature> fs_zd,Geometry geometry ){
        List<String> zddms = new ArrayList<String>();
        if(geometry instanceof Polyline ){
            Polyline line = (Polyline) geometry;
            if(line.getParts().size()==1 && line.getParts().get(0).getPointCount()==2){
                zddms = GetRefZDDM(fs_zd,line.getParts().get(0).getStartPoint(),line.getParts().get(0).getEndPoint());
            }
        }
        return zddms;
    }

    // 获取key
    public static String  GetKey( Point p1 ,Point p2 ){
        if(p1==null || p1.isEmpty()||p2==null||p2.isEmpty()) return "";
        String s1 = AiUtil.Scale(p1.getX(),3,0)+","+ AiUtil.Scale(p1.getY(),3,0);
        String s2 = AiUtil.Scale(p2.getX(),3,0)+","+ AiUtil.Scale(p2.getY(),3,0);
        if(s1.equalsIgnoreCase(s2)) return  "";
        if(p1.getX()>p2.getX()||((p1.getX()==p2.getY()&&(p1.getY()<p2.getY())))){
            return   s2+";"+s1;
        }else {
            return  s1+";"+s2;
        }
    }

    public static String  GetKey( Geometry geometry ){
        if(geometry instanceof Polyline ){
            Polyline line = (Polyline) geometry;
            if(line.getParts().size()==1 && line.getParts().get(0).getPointCount()==2){
                return GetKey(line.getParts().get(0).getStartPoint(),line.getParts().get(0).getEndPoint());
            }
        }
        return  "";
    }
    public static Map<String,Feature>  GetMap(  Map<String,Feature> map , List<Feature> fs_jzds ){
        for(Feature f:fs_jzds){
            String key = GetKey(f.getGeometry());
            if(StringUtil.IsNotEmpty(key)){
                map.put(key,f);
            }
        }
        return map;
    }
    public static Feature  Get(  Map<String,Feature> map ,Feature f_jzd1,Feature f_jzd2 ){
        if(f_jzd1.getGeometry() instanceof  Point&&f_jzd2.getGeometry() instanceof  Point){
            String key   = GetKey((Point)f_jzd1.getGeometry(),(Point) f_jzd2.getGeometry());
            if(StringUtil.IsNotEmpty(key) &&map.containsKey(key)){
                return  map.get(key);
            }
        }
        return null;
    }

    public static void UpdateJZX(final MapInstance mapInstance,final  Feature f_zd, AiRunnable callback_) {
        final AiDialog dialog = AiDialog.get(mapInstance.activity).setHeaderView(R.mipmap.app_map_layer_kzd, "生成界址线")
                .addContentView("将根据宗地的范围更新或删除原有的界址线，重新生成界址线", "该操作不可逆转，如果已经生成过界址线请谨慎操作");
        dialog.setFooterView(dialog.getProgressView("正在处理，请稍后..."));//.setCancelable(false);

        final AiRunnable callback = new AiRunnable(callback_) {
            @Override
            public <T_> void finlly(T_ t_, Object... objects) {
                dialog.setCancelable(true).dismiss();
            }
        };
        final List<Feature> fs_jzx = new ArrayList<>();
        final String zddm = FeatureEditZD.GetID(f_zd);
        final FeatureTable table = GetTable(mapInstance);
        final List<Feature> fs_zd = new ArrayList<>(); // 相关宗地
        final List<Feature> fs_save = new ArrayList<>();
        final List<Feature> fs_del = new ArrayList<>();

        // 查出所有相关的界址线
        MapHelper.Query(table, StringUtil.WhereByIsEmpty(zddm)+"ZDZHDM like '%" + zddm + "%' ", -1, fs_jzx, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                //查出范围内的界址线,注意不要查重复了
                MapHelper.Query(table, f_zd.getGeometry(), 0.01, StringUtil.WhereByIsEmpty(zddm)+"ZDZHDM not like '%" + zddm + "%' ", "", "", -1, true, fs_jzx, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        // 查出周围的宗地,其中可能包含本宗地
                        MapHelper.Query(FeatureEditZD.GetTable(mapInstance,"ZD"), f_zd.getGeometry(), 0.01, fs_zd, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                // 根据宗地去识别界址线
                                IndentyJZX(mapInstance, fs_jzx, fs_save, fs_del, f_zd, fs_zd, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        MapHelper.saveFeature(fs_save, fs_del, callback);
                                        return null;
                                    }
                                });
                                return null;
                            }
                        });
                        return null;
                    }
                });
                return null;
            }
        });
    }


    // 生成本宗地的界址线，处于效率考虑，先根据范围初始化界址线，然后处理肯能与其它宗共用而飞掉的点
    public static void  IndentyJZX(MapInstance instance, List<Feature> fs_jzx,List<Feature> fs_save,List<Feature> fs_del,Feature f_zd,List<Feature> fs_zd,AiRunnable callback) {
        if (fs_zd == null) { fs_zd = new ArrayList<>(); }
        if (f_zd == null && fs_zd.size() > 0) {  f_zd = fs_zd.get(0);  }
        if (f_zd == null) {  return;  }

        String  zddm = FeatureEditZD.GetID(f_zd);
        FeatureTable table = GetTable(instance);

        Map<String, Feature> map_old = new HashMap<>();
        Map<String, Feature> map_new = new LinkedHashMap<>(); // 有排序功能的
        for (Feature f : fs_jzx) {
            String key = GetKey(f.getGeometry());
            if(StringUtil.IsEmpty(key)||map_old.containsKey(key)){
                // 删除无效或者重复
                fs_del.add(f);
            }else {
                map_old.put(key, f);
            }
        }

        // 根据宗地的形状再重新设置界址线
        Geometry g = f_zd.getGeometry();
        if (g != null && g instanceof Multipart) {
           for(ImmutablePart part:((Multipart) g).getParts()) {
               for(Segment s :part){
                   String key = GetKey(s.getStartPoint(), s.getEndPoint());
                   if(StringUtil.IsNotEmpty(key) && !map_new.containsKey(key)) {
                       Feature f = map_old.get(key);
                       if (f == null) {
                           // 不存在添加
                           f = table.createFeature();
                       }
                       Polyline line = new Polyline(new PointCollection(Arrays.asList(new Point[]{s.getStartPoint(), s.getEndPoint()})));
                       f.setGeometry(line);
                       fs_save.add(f);
                       map_new.put(key, f);
                       IndentyJZX(instance,f, zddm, f_zd, fs_zd);
                   }
               }
           }
        }
        List<Feature> fs_jzx_ = new ArrayList<>();
        for (String key : map_old.keySet()) {
            if (!map_new.containsKey(key)) {
                // 剩余的界址点
                fs_jzx_.add(map_old.get(key));
            }
        }
        IndentyJZX(instance, fs_jzx_, fs_save, fs_del, callback);
    }


    public static boolean  IndentyJZX(MapInstance instance,Feature f_jzx,List<Feature> fs_zd){
        return IndentyJZX(instance,f_jzx,"",null,fs_zd);
    }
    // 根据给定的宗地去完善相关信息
    public static boolean  IndentyJZX(MapInstance instance,Feature f_jzx, String zddm,Feature f_zd,List<Feature> fs_zd){
        if(f_jzx==null || f_jzx.getGeometry()==null || (!(f_jzx.getGeometry() instanceof Polyline))){ return false;  }
        Polyline line = (Polyline)f_jzx.getGeometry();
        if(fs_zd==null){ fs_zd = new ArrayList<>();  }
        if(f_zd == null && fs_zd.size()>0){  f_zd = fs_zd.get(0); }
        if(f_zd==null){  return false;  }
        if(StringUtil.IsEmpty(zddm)){ zddm = FeatureEditZD.GetID(f_zd);}

        // 宗地宗海代码，获取相关宗地，是否有效，并保证宗地代码的顺序
        String zdzhdm = FeatureHelper.Get(f_jzx, "ZDZHDM", zddm);
        // 相关宗地代码
        List<String> zdzhdms_ = GetRefZDDM(fs_zd,line);
        zdzhdms_.add(zddm);
        // 当前宗地代码
        List<String> zdzhdms = new ArrayList<String>(Arrays.asList(zdzhdm.split("/")));
        // 剔除无效的宗地代码
        for(int i =0 ;i<zdzhdms.size();i++){
            if(!zdzhdms_.contains(zdzhdms.get(i))){
                zdzhdms.remove(i);
                i--;
            }
        }
        // 加入未加入的宗地代码
        for(int i =0 ;i<zdzhdms_.size();i++){
            if(!zdzhdms.contains(zdzhdms_.get(i))){
                zdzhdms.add(zdzhdms_.get(i));
            }
        }
        zdzhdm = StringUtil.Join(zdzhdms,"/",false);
        FeatureHelper.Set(f_jzx, "ZDZHDM", zdzhdm);
//        FeatureEditJZX.FillFeature(f_jzx);
        instance.fillFeature(f_jzx);
        return true;
    }

    // 根据每个界址点去识别宗地，然后填充其内容
    public static void  IndentyJZX(final MapInstance instance, final List<Feature> fs_jzx, final List<Feature> fs_save,final List<Feature> fs_del,final AiRunnable callback) {
        new AiRunnable (){
            int index = 0;
            @Override
            public void run() {
                exec(callback);
            }
            // 递归执行
            void exec(final AiRunnable callback){
                if(index<fs_jzx.size()){
                    IndentyJZX(instance, fs_jzx.get(index), fs_save, fs_del, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            index ++;
                            exec(callback);
                            return  null;
                        }
                    });
                }else{
                    AiRunnable.Ok(callback,fs_jzx);
                }
            }
        }.run();
    }
    // 根据界址点去识别宗地，然后填充其内容
    public static void  IndentyJZX(final MapInstance instance, final  Feature f_jzx, final List<Feature> fs_save,final List<Feature> fs_del,final AiRunnable callback) {
        FeatureTable table = FeatureEditZD.GetTable(instance,"ZD");
        final List<Feature> fs_zd  = new ArrayList<>();
        MapHelper.Query(table, f_jzx.getGeometry(), 0.01, fs_zd, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                if(IndentyJZX(instance,f_jzx,fs_zd)){
                    fs_save.add(f_jzx);
                }else{
                    fs_del.add(f_jzx);
                }
                AiRunnable.Ok(callback,f_jzx);
                return  null;
            }
        });
    }
    // 加载界址点
}
