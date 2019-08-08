package com.ovit.app.map.bdc.ggqj.map.view.bz;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;


public class FeatureEditBZ_TY extends FeatureEdit {
    final static String TAG = "FeatureEditBZ_TY";

    public FeatureEditBZ_TY(MapInstance mapInstance,Feature feature) {
        super(mapInstance, feature);
    }

    @Override
    public void build() {
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_bz_ty, v_content);
        try {
            if(feature != null){

                fillView(v_feature);

                if(feature.getGeometry()!=null){
                    v_feature.findViewById(R.id.iv_position).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MapHelper.selectAddCenterFeature(map,feature);
                        }
                    });
                   final Bitmap bm = MapHelper.geometry_icon(feature.getGeometry(),600,600, Color.RED,2);
                    ((ImageView) v_feature.findViewById(R.id.iv_icon)).setImageBitmap(bm);
                    v_feature.findViewById(R.id.iv_icon).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AiDialog.get(activity).setContentView(bm).show();
                        }
                    });

                }
            }
        }catch (Exception es){
            Log.e(TAG, "build: 构建失败", es);
        }
    }


    public static FeatureTable GetTable(final MapInstance mapInstance){
        return  mapInstance.getTable("BZ_TY");
    }


}
