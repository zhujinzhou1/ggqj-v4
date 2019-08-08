package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
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

public class FeatureEditJZD extends FeatureEdit {
    final static String TAG = "FeatureEditJZD";

    public FeatureEditJZD(){ super();}
    public FeatureEditJZD(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }

    //region  重写父类方法

    // 显示数据
    @Override
    public void build() {
     final   LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_jzd, v_content);
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
        return MapHelper.getLayer(mapInstance.map, "JZD", "界址点").getFeatureTable();
    }
    // 相关宗地代码
    public static List<String> GetRefZDDM(List<Feature> fs_zd,Point p ){
        List<String> zddms = new ArrayList<String>();
        for(Feature f :fs_zd){
            PointCollection ps = MapHelper.geometry_getPoints(f.getGeometry());
            if(MapHelper.geometry_list_contains(ps,p)){
                String zddm =  FeatureEditZD.GetID(f);
                if(StringUtil.IsNotEmpty(zddm)) {
                    zddms.add(zddm);
                }
            }
        }
        return zddms;
    }
    public static String  GetKey(Point p){
        return AiUtil.Scale(p.getX(),3,0)+","+ AiUtil.Scale(p.getY(),3,0);
    }


    public static void UpdateJZD(final MapInstance mapInstance,final  Feature f_zd, AiRunnable callback_) {
        final AiDialog dialog = AiDialog.get(mapInstance.activity).setHeaderView(R.mipmap.app_map_layer_kzd, "生成界址点")
                .addContentView("将根据宗地的范围删除原有的界址点，重新生成界址点", "该操作不可逆转，如果已经生成过界址点请谨慎操作");
        dialog.setFooterView(dialog.getProgressView("正在处理，请稍后..."));//.setCancelable(false);

        final AiRunnable callback = new AiRunnable(callback_) {
            @Override
            public <T_> void finlly(T_ t_, Object... objects) {
                dialog.setCancelable(true).dismiss();
            }
        };
        final List<Feature> fs_jzd = new ArrayList<>();
        final String zddm = FeatureEditZD.GetID(f_zd);
        final FeatureTable table = GetTable(mapInstance);
        final List<Feature> fs_zd = new ArrayList<>(); // 相关宗地
        final List<Feature> fs_save = new ArrayList<>();
        final List<Feature> fs_del = new ArrayList<>();

        // 查出所有相关的界址点
        MapHelper.Query(table, StringUtil.WhereByIsEmpty(zddm)+"ZDZHDM like '%" + zddm + "%' ", -1, fs_jzd, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                //查出范围内的界址点,注意不要查重复了
                MapHelper.Query(table, f_zd.getGeometry(), 0.01, StringUtil.WhereByIsEmpty(zddm)+"ZDZHDM not like '%" + zddm + "%' ", "", "", -1, true, fs_jzd, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        // 查出周围的宗地,其中可能包含本宗地
                        MapHelper.Query(mapInstance.getTable("ZD"), f_zd.getGeometry(), 0.01, fs_zd, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                // 根据宗地去识别界址点
                                IndentyJZD(mapInstance, fs_jzd, fs_save, fs_del, f_zd, fs_zd, new AiRunnable() {
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


    // 生成本宗地的界址点，处于效率考虑，先根据范围初始化界址点，然后处理肯能与其它宗共用而飞掉的点
    public static void  IndentyJZD(MapInstance instance, List<Feature> fs_jzd,List<Feature> fs_save,List<Feature> fs_del,Feature f_zd,List<Feature> fs_zd,AiRunnable callback) {
        if (fs_zd == null) { fs_zd = new ArrayList<>(); }
        if (f_zd == null && fs_zd.size() > 0) {  f_zd = fs_zd.get(0);  }
        if (f_zd == null) {  return;  }

        String  zddm = FeatureEditZD.GetID(f_zd);
        FeatureTable table = GetTable(instance);

        Map<String, Feature> map_old = new HashMap<>();
        Map<String, Feature> map_new = new LinkedHashMap<>(); // 有排序功能的
        for (Feature f : fs_jzd) {
            String key = GetKey((Point) f.getGeometry());
            if(StringUtil.IsEmpty(key)||map_old.containsKey(key)){
                // 删除无效或者重复
                fs_del.add(f);
            }else {
                map_old.put(key, f);
            }
        }

        // 根据宗地的形状再重新设置界址点
        Geometry g = f_zd.getGeometry();
        if (g != null) {
            final PointCollection ps = MapHelper.geometry_getPoints(g);
            if (ps.size() > 2) {
                if (MapHelper.geometry_equals(ps.get(0), ps.get(ps.size() - 1))) {
                    // 首位相同 移除
                    ps.remove(ps.size() - 1);
                }
            }
            int index = 1;
            for (Point p : ps) {
                String key = GetKey(p);
                if(StringUtil.IsNotEmpty(key) && !map_new.containsKey(key)) {
                    Feature f = map_old.get(key);
                    if (f == null) {
                        // 不存在添加
                        f = table.createFeature();
                    }
                    f.setGeometry(p);
                    fs_save.add(f);
                    map_new.put(key, f);
//                    FeatureEditJZD.FillFeature(instance, f);
                    instance.fillFeature(f);
                    IndentyJZD(instance,f, index, zddm, f_zd, fs_zd);
                    index++;
                }
            }
        }
        List<Feature> fs_jzd_ = new ArrayList<>();
        for (String key : map_old.keySet()) {
            if (!map_new.containsKey(key)) {
                // 剩余的界址点
                fs_jzd_.add(map_old.get(key));
            }
        }
        IndentyJZD(instance, fs_jzd_, fs_save, fs_del, callback);
    }


    public static boolean  IndentyJZD(MapInstance instance,Feature f_jzd,List<Feature> fs_zd){
       return IndentyJZD(instance,f_jzd,0,"",null,fs_zd);
    }
    // 根据给定的宗地去完善相关信息
    public static boolean  IndentyJZD(MapInstance mapInstance,Feature f_jzd, int index, String zddm,Feature f_zd,List<Feature> fs_zd){
        if(f_jzd==null || f_jzd.getGeometry()==null || (!(f_jzd.getGeometry() instanceof Point))){ return false;  }
        Point p = (Point)f_jzd.getGeometry();
        if(fs_zd==null){ fs_zd = new ArrayList<>();  }
        if(f_zd == null && fs_zd.size()>0){  f_zd = fs_zd.get(0); }
        if(f_zd==null){  return false;  }
        if(StringUtil.IsEmpty(zddm)){ zddm = FeatureEditZD.GetID(f_zd);}
        if(index <1){ index = MapHelper.geometry_list_findIndex(MapHelper.geometry_getPoints(f_zd.getGeometry()),p)+1; }
        if(index<1){return false;}

        // 宗地宗海代码，获取相关宗地，是否有效，并保证宗地代码的顺序
        String zdzhdm = FeatureHelper.Get(f_jzd, "ZDZHDM", zddm);
        // 相关宗地代码
        List<String> zdzhdms_ = GetRefZDDM(fs_zd,p);
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
        FeatureHelper.Set(f_jzd, "ZDZHDM", zdzhdm);

        // 界址点号、序号
        String jzdh = FeatureHelper.Get(f_jzd, "JZDH", "");
        if (zdzhdms.size() == 1 || zdzhdms.get(0).equals(zddm)) {
            // 如果是本宗地
            jzdh = "J" + zddm.substring(zddm.length() - 5) + (index > 10 ? (index + 1) + "" : "0" + index);
            FeatureHelper.Set(f_jzd, "SXH", "" + index);
        }
        FeatureHelper.Set(f_jzd, "JZDH", jzdh);
//        FeatureEditJZD.FillFeature(f_jzd);
        mapInstance.fillFeature(f_jzd);
        return true;
    }

    // 根据每个界址点去识别宗地，然后填充其内容
    public static void  IndentyJZD(final MapInstance instance, final List<Feature> fs_jzd, final List<Feature> fs_save,final List<Feature> fs_del,final AiRunnable callback) {
        new AiRunnable (){
            int index = 0;
            @Override
            public void run() {
                exec(callback);
            }
            // 递归执行
            void exec(final AiRunnable callback){
                if(index<fs_jzd.size()){
                    IndentyJZD(instance, fs_jzd.get(index), fs_save, fs_del, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            index ++;
                            exec(callback);
                            return  null;
                        }
                    });
                }else{
                    AiRunnable.Ok(callback,fs_jzd);
                }
            }
        }.run();
    }
    // 根据界址点去识别宗地，然后填充其内容
    public static void  IndentyJZD(final MapInstance instance, final  Feature f_jzd, final List<Feature> fs_save,final List<Feature> fs_del,final AiRunnable callback) {
        Point p = (Point )f_jzd.getGeometry();
        FeatureTable table = instance.getTable("ZD");
        final List<Feature> fs_zd  = new ArrayList<>();
        MapHelper.Query(table, p, 0.01, fs_zd, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                if(IndentyJZD(instance,f_jzd,fs_zd)){
//                    FeatureEditJZD.FillFeature(instance,f_jzd);
                    instance.fillFeature(f_jzd);
                    fs_save.add(f_jzd);
                }else{
                    fs_del.add(f_jzd);
                }
                AiRunnable.Ok(callback,f_jzd);
                return  null;
            }
        });
    }
    // 加载界址点
}
