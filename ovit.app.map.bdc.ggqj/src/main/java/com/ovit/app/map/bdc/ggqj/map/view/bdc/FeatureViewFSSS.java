package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.graphics.Color;
import android.view.View;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xw on 2020/4/29.
 */

public class FeatureViewFSSS extends FeatureView {
    final static String TAG = "FeatureViewFSSS";

    // region 重写父类方法
    @Override
    public void onCreate() {
        super.onCreate();
        iconColor = Color.rgb(0, 92, 230);
    }

    @Override
    public void fillFeature(Feature feature, Feature feature_zd) {
        super.fillFeature(feature, feature_zd);
        if (feature_zd != null) {
            String zddm = FeatureHelper.Get(feature_zd, FeatureHelper.TABLE_ATTR_ZDDM, "");
            String qlrxm = FeatureHelper.Get(feature_zd, "QLRXM", "");
            FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ZDDM, zddm);
            FeatureHelper.Set(feature,"QLR", qlrxm);
        }
    }

    @Override
    public String addActionBus(String groupname) {

        addActionTY(groupname);
        addActionSJ(groupname);

        groupname = "操作";
        if (feature != null && feature.getFeatureTable() == table) {
            mapInstance.addAction(groupname, "定位", R.mipmap.app_icon_opt_location, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    command_postion();
                }
            });

            if (table.getGeometryType().equals(GeometryType.POLYGON) || table.getGeometryType().equals(GeometryType.POLYLINE)) {
                mapInstance.addAction(groupname, "切割", R.mipmap.app_icon_map_cut, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        command_cut(null);
                    }
                });
                if (mapInstance.getSelFeatureCount() > 1) {
                    mapInstance.addAction(groupname, "合并", R.mipmap.app_icon_merge, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            command_merge(null);
                        }
                    });
                }
                mapInstance.addAction(groupname, "修边", R.mipmap.app_icon_xiubian, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        command_xiubian(null);
                    }
                });
                mapInstance.addAction(groupname, "挖空", R.mipmap.app_icon_hollow, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        command_hollow();
                    }
                });

                mapInstance.addAction(groupname, "删除", R.mipmap.ic_action_clear, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        command_del(null);
                    }
                });
            }
        }
        mapInstance.addAction(groupname, "画附属设施", R.mipmap.app_map_layer_zrz, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_fsss();
            }
        });

        addActionDW("");
        addActionBZ("", false);
        return groupname;
    }

    //endregion 重写父类方法
    //region 界面方法

    public void fillFeature(Feature feature, boolean isF99990001) {

        String zrzh = FeatureHelper.Get(feature, "ZRZH", "");
        String zddm = StringUtil.substr(zrzh, 0, zrzh.length() - 5);
        String zh = StringUtil.substr_last(zrzh, 4);

        FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ZDDM, zddm);
        FeatureHelper.Set(feature, "ZH", zh);


        // 单位米
        double area = MapHelper.getArea(mapInstance, feature.getGeometry());
        if (0d == FeatureHelper.Get(feature, "ZZDMJ", 0d)) {
            FeatureHelper.Set(feature, "ZZDMJ", area);
        }
        if (0d == FeatureHelper.Get(feature, "SCJZMJ", 0d)) {
            double scjzmj = area * AiUtil.GetValue(feature.getAttributes().get("ZCS"), 1);
            feature.getAttributes().put("SCJZMJ", scjzmj);
        }

        if (isF99990001) {
            FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_BDCDYH, zddm + "F99990001");
        } else {
            FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_BDCDYH, zrzh + "0001");
        }
        if (StringUtil.IsNotEmpty(zh) && StringUtil.IsEmpty(AiUtil.GetValue(feature.getAttributes().get("JZWMC"), ""))) {
            FeatureHelper.Set(feature, "JZWMC", AiUtil.GetValue(zh, 1) + "");
        }
        if (StringUtil.IsEmpty(AiUtil.GetValue(feature.getAttributes().get("FWJG"), ""))) {
            FeatureHelper.Set(feature, "FWJG", "4");// [4][B][混]混合结构
        }

    }

    public static FeatureViewFSSS From(MapInstance mapInstance, Feature f) {
        FeatureViewFSSS fv = From(mapInstance);
        fv.set(f);
        return fv;
    }

    public static FeatureViewFSSS From(MapInstance mapInstance) {
        FeatureViewFSSS fv = new FeatureViewFSSS();
        fv.set(mapInstance).set(mapInstance.getTable(FeatureHelper.TABLE_NAME_FSSS));
        return fv;
    }

    public static void CreateFeature(final MapInstance mapInstance, final Feature f_p, final AiRunnable callback) {
        CreateFeature(mapInstance, f_p, null, callback);
    }

    public static void CreateFeature(final MapInstance mapInstance, String orid, final Feature feature, final AiRunnable callback) {
        // 去查宗地
        FeatureViewZD.From(mapInstance).load(orid, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Feature f_p = (Feature) t_;
                CreateFeature(mapInstance, f_p, feature, callback);
                return null;
            }
        });
    }

    //    带有诸多属性fsss
    public static void CreateFeature(final MapInstance mapInstance, final Feature f_p, Feature f, final AiRunnable callback) {

        if (f_p != null && f_p.getFeatureTable() != mapInstance.getTable(FeatureConstants.ZD_TABLE_NAME)) {
            // 如果不是宗地
            String orid = mapInstance.getOrid_Match(f, FeatureConstants.ZD_TABLE_NAME);
            if (StringUtil.IsNotEmpty(orid)) {
                CreateFeature(mapInstance, orid, f, callback);
                return;
            }
        }
        final FeatureViewFSSS fv = From(mapInstance, f);
        final Feature feature;
        if (f == null) {
            feature = fv.table.createFeature();
        } else {
            feature = f;
        }
        if (f_p == null) {
            ToastMessage.Send("注意：缺少宗地信息");
        }
        final List<Feature> fs_update = new ArrayList<>();
        fv.drawAndAutoCompelet(feature, fs_update, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 填充
                fv.fillFeature(feature, f_p);
                fs_update.add(feature);
                // 保存
                MapHelper.saveFeature(fs_update, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        // 返回显示
                        AiRunnable.Ok(callback, feature);
                        mapInstance.viewFeature(feature);
                        return null;
                    }
                });
                return null;
            }
        });
    }

    //endregion 输出生果
}
