package com.ovit.app.map.bdc.ggqj.map.view;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditFTQK;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditH;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditH_FSJG;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditZ_FSJG;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.LayerConfig;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lichun on 2017/4/5.
 */

public class FeatureEdit extends com.ovit.app.map.view.FeatureEdit {
    final static String TAG = "FeatureEdit";
    public MapInstance mapInstance;
    public FeatureView fv;

    public FeatureEdit(){ super(); }
    public FeatureEdit(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }

    public FeatureEdit(MapInstance mapInstance, Feature feature, int resid) {
        super(mapInstance,feature,resid);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        if(super.mapInstance instanceof MapInstance) {
            this.mapInstance = (MapInstance)super.mapInstance;
            this.fv = (FeatureView) super.fv;
        }
    }

    //分摊相关
    //View v_ft ;
    //public void load_ft(MapInstance mapInstance,Feature bdc_feature,View ft_view)
    //{
//        if(v_ft==null) {
//            MapHelper.QueryOne(mapInstance.getTable("GNQ"), "", new AiRunnable() {
//                @Override
//                public <T_> T_ ok(T_ t_, Object... objects) {
//                    Feature f_gnq = (Feature) t_;
//                    if(f_gnq ==null){
//                        f_gnq  =   mapInstance.getTable("GNQ").createFeature();
//                        FeatureHelper.Set(f_gnq,"REFORID",mapInstance.getOrid(feature));
//                        FeatureHelper.Set(f_gnq,"REFTYPE",mapInstance.getTableName(feature));
//                        FeatureHelper.Set(f_gnq,"GNQMC",mapInstance.getName(feature));
//
//                        FeatureHelper.Set(f_gnq,"GNQMC",mapInstance.getName(feature));
//                        f_gnq.setGeometry(feature.getGeometry());
//                    }
//                    FeatureView fv_gnq =  mapInstance.newFeatureView(f_gnq);
//                    fv_gnq.fillFeature(f_gnq);
//                    v_ft =  fv_gnq.getEditView().get();
//
//                    LinearLayout ll_ft_content = (LinearLayout )view.findViewById(com.ovit.R.id.ll_ft_content);
//                    if(ll_ft_content!=null) {
//                        ll_ft_content.removeAllViews();
//                        ll_ft_content.addView(v_ft, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                    }
//                    return null;
//                }
//            });
//        }

    //}





}
