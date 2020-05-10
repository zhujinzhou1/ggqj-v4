package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.layers.Layer;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.StringUtil;

/**
 * Created by xw on 2020/4/29.
 */

public class FeatureEditFSSS extends FeatureEdit {

    final static String TAG = "FeatureEditZRZ";
    FeatureViewFSSS fv;
    private String old_zrzh;

    //region  重写父类方法
    @Override
    public void onCreate() {
        super.onCreate();
        // 使用 fv
        if (super.fv instanceof FeatureViewFSSS) {
            this.fv = (FeatureViewFSSS) super.fv;
        }
    }

    // 初始化
    @Override
    public void init() {
        super.init();
//        zcs = FeatureHelper.Get(feature,"ZCS", 1d);
        // 菜单
        menus = new int[]{R.id.ll_info};
    }

    // 显示数据
    @Override
    public void build() {
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_fsss, v_content);
        try {
            if (feature != null) {
                Geometry g = feature.getGeometry();
                if (g!=null){
                    double area = MapHelper.getArea(mapInstance, feature.getGeometry());

                    if (0d == FeatureHelper.Get(feature, "ZYDMJ", 0d)) {
                        FeatureHelper.Set(feature, "ZYDMJ", area);
                    }
                    if (0d == FeatureHelper.Get(feature, "ZZDMJ", 0d)) {
                        feature.getAttributes().put("ZZDMJ", area);
                    }
                    String jzwmc = FeatureHelper.Get(feature,"JZWMC","");
                    if (0d == FeatureHelper.Get(feature, "ZZDMJ", 0d) && !fv.IsSc(jzwmc)) {
                        double scjzmj = area * AiUtil.GetValue(feature.getAttributes().get("ZCS"), 1);
                        feature.getAttributes().put("SCJZMJ", scjzmj);
                    }

                }
                mapInstance.fillFeature(feature);
                fillView(v_feature);
               final Spinner  spn_jzwmc = (Spinner) v_feature.findViewById(R.id.spn_jzwmc);

                  spn_jzwmc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                   @Override
                   public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                     String value = (String) spn_jzwmc.getAdapter().getItem(position);
                       setDicValue(feature, "JZWMC", value);
                       hsmj();
                   }

                   @Override
                   public void onNothingSelected(AdapterView<?> parent) {
//                       setValue(feature, "JZWMC", null);
                   }
               });

                v_feature.findViewById(R.id.tv_fsssglzd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastMessage.Send(activity, "请选择宗地！");
                        Layer layer = MapHelper.getLayer(map, FeatureHelper.TABLE_NAME_ZD);
                        mapInstance.setSelectLayer(layer, null, false);
                    }
                });

                v_feature.findViewById(R.id.tv_jcyzdgx).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String zrzh = FeatureHelper.Get(feature, "ZRZH", "");
                        zrzh = StringUtil.substr_last(zrzh, 5);
                        FeatureHelper.Set(feature, "ZRZH", zrzh);
                        FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                        mapInstance.fillFeature(feature);
                        mapInstance.viewFeature(feature);
                        ToastMessage.Send(activity, "与宗地关系已经解除！");
                    }
                });
            }
        } catch (Exception es) {
            Log.e(TAG, "build: 构建失败", es);
        }
    }

    private void hsmj() {
        fv.hsmj(feature, mapInstance);
        fillView(v_content, feature, "SCJZMJ");
        fillView(v_content, feature, "ZCS");
    }

    @Override
    public void build_opt() {
        super.build_opt();

        addMenu("基本信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_info);
            }
        });

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
    //region 属性页

    //region 属性页

    //end-----------------------------------20180709----------------------------------------------------------------------------------------------
}
