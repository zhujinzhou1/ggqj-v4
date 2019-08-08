package com.ovit.app.map.bdc.ggqj.map.view.dw;

import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.esri.arcgisruntime.data.Feature;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;

/**
 * Created by LiuSheng on 2017/7/28.
 */

public class FeatureEditXZDW extends FeatureEdit {

    final static String TAG = "FeatureEditXZDW";

    public FeatureEditXZDW(){ super();}
    public FeatureEditXZDW(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }

    //region  重写父类方法
    // 初始化
    @Override
    public void init() {
        super.init();
        // 标题
//        id =  AiUtil.GetValue(feature.getAttributes().get("TITLE"), "");
//        // 内容
//        name =  AiUtil.GetValue(feature.getAttributes().get("CONTENT"), "");
        // 必填字段
//        requiredFileds.addAll(Arrays.asList(new String [] {"JZWMC","ZDDM","BDCDYH","ZRZH","ZCS","ZH"}));
        // 菜单
        menus = new int []{   R.id.ll_info };
    }

    // 显示数据
    @Override
    public void build() {
        LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_xzdw, v_content);
        try {
            if (feature != null) {
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
//    @Override
//    public void show(String name, AiRunnable callback) {
//        super.show_dialog(name, callback);
//    }
}
