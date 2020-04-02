package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.text.TextUtils;
import android.view.View;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.pojo.FeaturePojo;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewH.GetTable;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewH_FSJG extends FeatureView {

    //region 常量
    final static String TAG = "FeatureViewH_FSJG";
    ///endregion
    //region 字段
    ///endregion
    //region 构造函数
    ///endregion

    //region 重写函数和回调
    @Override
    public void onCreate() {
        super.onCreate();
        iconColor = Color.rgb(0, 169, 230);

        iconDash = new DashPathEffect(new float[]{4, 4}, 0);// 虚线
    }

    @Override
    public String addActionBus(String groupname) {
        int count = mapInstance.getSelFeatureCount();
        // 根据画宗地推荐
//            mapInstance.addAction(groupname, "画阳台", R.mipmap.app_map_h, new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    draw_h_fsjg(feature, "阳台");
//                }
//            });
//        }

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
        return groupname;
    }

    @Override
    public void hsmj(Feature feature, MapInstance mapInstance) {
        String fhmc = AiUtil.GetValue(feature.getAttributes().get("FHMC"), "");
        double type = AiUtil.GetValue(feature.getAttributes().get("TYPE"), 0d);
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        if (g != null) {
            area = MapHelper.getArea(mapInstance, g);
            if (area > 0) {
                hsmj = type * area;
            }
        }
        if (fhmc.contains("门廊")) {
            feature.getAttributes().put("MC", fhmc + "");
        } else if (fhmc.contains("阳台")) {
            feature.getAttributes().put("MC", (type == 0 ? "" : (type == 1 ? "封闭" : "未封闭")) + fhmc);
        } else if (fhmc.contains("凹槽")) {
            feature.getAttributes().put("MC", (type == 0 ? "" : (type == 1 ? "（全）" : "")) + fhmc);
        } else {
            feature.getAttributes().put("MC", fhmc + "" + (type == 0 ? "" : (type == 1 ? "（全）" : "（半）")));
        }
        feature.getAttributes().put("MJ", AiUtil.Scale(area, 2));
        feature.getAttributes().put("HSMJ", AiUtil.Scale(hsmj, 2));
    }

    @Override
    public void fillFeature(Feature feature, Feature feature_h) {
        super.fillFeature(feature, feature_h);
        String id = FeatureHelper.Get(feature, "ID", "");
        String hid = FeatureHelper.Get(feature, "HID", "");
        int lc = FeatureHelper.Get(feature, "LC", 1);
        if (feature_h != null) {
            String hid_ = FeatureHelper.Get(feature_h, "HID", "");
            id = hid_ + StringUtil.substr(id, hid_.length());
            hid = hid_;
            FeatureHelper.Set(feature, "ID", id);
            lc = FeatureHelper.Get(feature_h, "SZC", lc);
        }
        String hh = "";
        // id有效
        if (id.length() == 32) {
            hid = StringUtil.substr(id, 0, id.length() - 4);
            FeatureHelper.Set(feature, "HID", hid);
        }
        // hid 有效
        if (hid.length() == 28) {
            hh = StringUtil.substr_last(hid, 4);
            FeatureHelper.Set(feature, "HH", hh);
        }
        FeatureHelper.Set(feature, "LC", lc);
    }
    ///endregion

    //region 公有函数
    // 智能识别生成户
    public void initFeatureH_fsjg(AiRunnable callback) {
        FeaturePojo featurePojo = new FeaturePojo(feature, "BZ");
        List<String> lcs = featurePojo.getLc();
        List<Feature> fs_h = new ArrayList<>();
        for (String lc : lcs) {
            Feature f_h = mapInstance.getTable(FeatureHelper.TABLE_NAME_H_FSJG).createFeature();
            f_h.setGeometry(feature.getGeometry());
            FeatureHelper.Set(f_h, "LC", lc);
            FeatureHelper.Set(f_h, "FHMC", featurePojo.getName());
            FeatureHelper.Set(f_h, "ZRZH ", featurePojo.getZrzh());
            FeatureHelper.Set(f_h, "TYPE", "0.5");
            mapInstance.fillFeature(f_h);
            fs_h.add(f_h);
        }
        AiRunnable.Ok(callback, fs_h);
    }

    public void identyH_fsjg(final MapInstance mapInstance, final Feature f_h, final AiRunnable callback) {
        final String hid = FeatureHelper.Get(f_h, "ID", "");
        final int hch = FeatureHelper.Get(f_h, "SZC", 1);
        final String orid = FeatureHelper.Get(f_h, FeatureHelper.TABLE_ATTR_ORID, "");
        final List<Feature> features_hfsjg = new ArrayList<Feature>();
        final List<Feature> features_update = new ArrayList<Feature>();
        final List<Feature> features_save = new ArrayList<Feature>();
        // 放到 0.2米的范围
        MapHelper.Query(GetTable(mapInstance), f_h.getGeometry(), 0.1, features_hfsjg, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                double h_area = MapHelper.getArea(mapInstance, f_h.getGeometry());
                double area_jzmj = h_area;
                int count = 0;
                for (Feature f : features_hfsjg) {
                    final String orid_path = FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                    final String[] fs_orid = orid_path.split("/");
                    int f_lc = FeatureHelper.Get(f, "LC", 0);
                    double f_hsmj = FeatureHelper.Get(f, "HSMJ", 0d);

                    //  只检查 没有被识别过或本户的 和 没有层或本层的
                    if ((fs_orid.length <= 1 || fs_orid[fs_orid.length].equals(orid)) && (f_lc == 0 || hch == f_lc)) {
                        area_jzmj += f_hsmj;
                        mapInstance.fillFeature(f, f_h);
                        features_save.add(f);
                        if (fs_orid[fs_orid.length].equals(orid)) {
                            // 本户
                        } else {
                            features_update.add(f);
                        }
                    }
                }
                if (features_update.size() > 0) {
//                    for (Feature f : features_update) {
//                        count++;
//                        String i = String.format("%04d", count);
//                        FeatureHelper.Set(f,"ID", hid + i);
//                    }
                }
                mapInstance.fillFeature(features_save, f_h);
                f_h.getAttributes().put("YCJZMJ", AiUtil.GetValue(String.format("%.2f", h_area), h_area));
                f_h.getAttributes().put("SCJZMJ", AiUtil.GetValue(String.format("%.2f", area_jzmj), area_jzmj));
                features_save.add(f_h);
                MapHelper.saveFeature(features_save, callback);
                return null;
            }
        });
    }

    public FeatureTable getFeatureTable() {
        return mapInstance.getTable(FeatureHelper.TABLE_NAME_H_FSJG);
    }

    public void fsjg_init(final AiRunnable callback) {
        final Feature h_fsjg = getFeatureTable().createFeature();
        mapInstance.command_draw(h_fsjg, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                mapInstance.newFeatureView(feature).fillFeatureAddSave(h_fsjg, callback);
                return null;
            }
        });

    }

    /**
     * 附属结构修剪
     *
     * @param fs_fsjg_temp
     * @return
     */
    public static List<Feature> screenFSJG(List<Feature> fs_fsjg_temp) {
        if (fs_fsjg_temp.size() <= 1) {
            return fs_fsjg_temp;
        }
        int i = 0;
        List<Feature> fs = new ArrayList<>();
        while (i < fs_fsjg_temp.size() - 1) {
            for (int j = i + 1; j < fs_fsjg_temp.size(); j++) {
                if (MapHelper.geometry_feature_equals(fs_fsjg_temp.get(i), fs_fsjg_temp.get(j))) {
                    fs.add(fs_fsjg_temp.get(j));
                }
            }
            i++;
        }
        fs_fsjg_temp.removeAll(fs);
        return fs_fsjg_temp;
    }
    ///endregion

    //region 私有函数
    ///endregion

    //region 内部类或接口
    ///endregion

}
