package com.ovit.app.map.bdc.ggqj.map.view.bz;

import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.esri.arcgisruntime.data.Feature;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;



public class FeatureEditBZ_WZ extends FeatureEdit {

    final static String TAG = "FeatureEditBZ_WZ";

    public FeatureEditBZ_WZ(){ super();}
    public FeatureEditBZ_WZ(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }


    // 显示数据
    @Override
    public void build() {
        LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_wzzj, v_content);
        try {
            if (feature != null) {
                fillView(v_feature);
            }
        } catch (Exception es) {
            Log.e(TAG, "build: 构建失败", es);
        }
    }
}


