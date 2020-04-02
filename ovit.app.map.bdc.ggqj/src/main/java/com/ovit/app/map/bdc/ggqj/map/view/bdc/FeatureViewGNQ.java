package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Geometry;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewGNQ extends FeatureView {
    @Override
    public void onCreate() {
        super.onCreate();
        iconColor =Color.rgb(0, 92, 230);
    }

    public void fillFeature(Feature feature, Feature feature_zd){
        super.fillFeature(feature,feature_zd);
        if(feature_zd!=null) {
            String zddm = FeatureHelper.Get(feature_zd, FeatureHelper.TABLE_ATTR_ZDDM, "");
            String zrzh = FeatureHelper.Get(feature, "ZRZH", "");
            zrzh = zddm + StringUtil.substr(zrzh, zddm.length());
            FeatureHelper.Set(feature, "ZRZH", zrzh);
        }

    }

    @Override
    public String addActionBus(String groupname) {
        int count = mapInstance.getSelFeatureCount();

        if(count>0) {
            mapInstance.addAction(groupname, "分摊", R.mipmap.app_map_layer_h, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ft(null);
                }
            });
        addActionTY(groupname);
        addActionPZ(groupname);
        addActionSJ(groupname);

        groupname = "操作";

        if (feature != null && feature.getFeatureTable() == table) {
            mapInstance.addAction(groupname, "定位", R.mipmap.app_icon_opt_location, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    command_postion();
                }
            });
            }
        }
        addActionDW("");
        addActionBZ("", false);
        return groupname;
    }


    // 列表项，点击加载自然幢
    @Override
    public void listAdapterConvert(BaseAdapterHelper helper,final Feature item, final int deep) {
        super.listAdapterConvert(helper,item,deep);
        final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
        ll_list_item.setVisibility(View.GONE);
        helper.getView(R.id.ll_head).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapHelper.centerPoint(map,item.getGeometry());
                MapHelper.selectFeature(map,item);
                boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                if (!flag) {
                    final List<Feature> fs = new ArrayList<>();
                    queryChildFeature(FeatureHelper.TABLE_NAME_LJZ, item, fs, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            List<Feature> fs_p = new ArrayList<>();
                            fs_p.add(item);
                            mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_LJZ).buildListView(ll_list_item,fs,deep+1);
                            return  null;
                        }
                    });
                }
                ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
            }
        });
    }

    public void loadByRef(String reforid,String reftype,AiRunnable callback){
        MapHelper.QueryOne(table,StringUtil.WhereByIsEmpty(reforid)+" REFORID = '" + reforid + "'  REFTYPE = '" + reftype + "' ",callback);
    }

    public void ft(AiRunnable callback){

    }

    public void update_Area(Feature feature, List<Feature> f_hs, List<Feature> f_z_fsjgs) {
        String id = FeatureHelper.Get(feature,"ZRZH","");
        int zcs =  FeatureHelper.Get(feature,"ZCS",1);
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        if (g != null) {
            area = MapHelper.getArea(mapInstance, g);

            for (Feature f : f_hs) {
                String zrzh = FeatureHelper.Get(f,"ZRZH","");
                if (id.equals(zrzh)) {
                    double f_hsmj = FeatureHelper.Get(f,"SCJZMJ", 0d);
                    hsmj += f_hsmj;
                }
            }
            for (Feature f : f_z_fsjgs) {
                String zid = FeatureHelper.Get(f,"ZID", "");
                if (id.equals(zid)) {
                    double f_hsmj = FeatureHelper.Get(f,"HSMJ",0d);
                    hsmj += f_hsmj;
                }
            }
            if (hsmj <= 0) {
                hsmj = area * zcs;
            }
            FeatureHelper.Set(feature,"ZZDMJ", AiUtil.Scale(area, 2));
            FeatureHelper.Set(feature,"SCJZMJ", AiUtil.Scale(hsmj, 2));
        }
    }


    public static FeatureViewGNQ From(MapInstance mapInstance, Feature f){
        FeatureViewGNQ fv =From(mapInstance);
        fv.set(f);
        return  fv;
    }
    public static FeatureViewGNQ From(MapInstance mapInstance){
        FeatureViewGNQ fv = new FeatureViewGNQ();
        fv.set(mapInstance).set(mapInstance.getTable("GNQ"));
        return  fv;
    }







}
