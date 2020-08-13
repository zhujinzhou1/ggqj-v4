package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.model.DxfFcfhtDefault;
import com.ovit.app.map.bdc.ggqj.map.model.DxfFcfht_neimeng;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.ListUtil;
import com.ovit.app.util.ReportUtils;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewH extends FeatureView {

    //region 常量
    final static String TAG = "FeatureViewH";
    ///endregion

    //region 字段
    ///endregion

    //region 构造函数
    ///endregion

    //region 重写函数和回调
    @Override
    public void onCreate() {
        super.onCreate();
        iconColor = Color.rgb(0, 197, 255);
    }

    @Override
    public String addActionBus(String groupname) {
        int count = mapInstance.getSelFeatureCount();
        // 根据画宗地推荐

        mapInstance.addAction(groupname, "画户", R.mipmap.app_map_layer_h, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String zrzh = FeatureHelper.Get(feature, "ZRZH", "");
                draw_h(mapInstance.getOrid_Parent(feature), "1", null);
            }
        });

        if (count > 0) {
            mapInstance.addAction(groupname, "画飘窗", R.mipmap.app_map_layer_h_pc, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    draw_h_fsjg(feature, "飘窗", null);
                }
            });
            mapInstance.addAction(groupname, "画阳台", R.mipmap.app_map_layer_h_yt, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    draw_h_fsjg(feature, "阳台", null);
                }
            });
        }

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
    public void fillFeature(Feature feature, Feature feature_ljz) {
        super.fillFeature(feature, feature_ljz);
        String id = FeatureHelper.Get(feature, "ID", "");
        String zrzh = FeatureHelper.Get(feature, "ZRZH", "");
        if (feature_ljz != null) {
            FeatureHelper.Set(feature, "LJZH", FeatureHelper.Get(feature_ljz, "LJZH", ""));
            zrzh = FeatureHelper.Get(feature_ljz, "ZRZH", "");
        }
        FeatureHelper.Set(feature, "ZRZH", zrzh);
        String zddm = StringUtil.substr_last(zrzh, 0, FeatureHelper.FEATURE_ZD_ZDDM_LENG);
        FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ZDDM, zddm);

        String bdcdyh = FeatureHelper.Get(feature_ljz, FeatureHelper.TABLE_ATTR_BDCDYH, FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_BDCDYH, ""));
        boolean isF99990001 = bdcdyh.endsWith("F99990001");
        bdcdyh = isF99990001 ? (zddm + "F99990001") : (zrzh + StringUtil.substr_last(id, 4));
        FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_BDCDYH, bdcdyh);
        FeatureHelper.Set(feature, "FWBM", StringUtil.substr_last(bdcdyh, 9));

        int lc = FeatureHelper.Get(feature, "SZC", 1);
        FeatureHelper.Set(feature, "SZC", lc);
        FeatureHelper.Set(feature, "SJCS", lc);

        int hh = FeatureHelper.Get(feature, "HH", 1);
        // 户号字段表示一个层当中的户顺序号
        FeatureHelper.Set(feature, "HH", hh);

        String mph = FeatureHelper.Get(feature_ljz, "MPH", "");
        mph += mph.length() > 0 ? "-" : "";
        mph += lc;
        mph += String.format("%02d", hh);

        FeatureHelper.Set(feature, "MPH", mph, true, false);
        FeatureHelper.Set(feature, "SHBW", mph, true, false);

        double area = MapHelper.getArea(mapInstance, feature.getGeometry());
        if (0d == FeatureHelper.Get(feature, "SCJZMJ", 0d)) {
            FeatureHelper.Set(feature, "SCJZMJ", area);
        }
        FeatureHelper.Set(feature, "YCJZMJ", area);
        FeatureHelper.Set(feature, "FWJG", FeatureHelper.Get(feature_ljz, "FWJG1", "5"), true, false);//砖木结构

        FeatureHelper.Set(feature, "CQLY", "自建", true, false);// 自建
        FeatureHelper.Set(feature, "FWLX", "1", true, false); //[1]住宅
        FeatureHelper.Set(feature, "FWXZ", "99", true, false); // [99]其它
        FeatureHelper.Set(feature, "ZT", "自用", true, false); // // 自用
        FeatureHelper.Set(feature, "YT", "10", true, false); // 住宅

        FeatureHelper.Set(feature, "CB", "6", true, false); //  [6]私有房产
        FeatureHelper.Set(feature, "QTGSD", "自墙", true, false); //自有墙
        FeatureHelper.Set(feature, "QTGSN", "自墙", true, false); //自有墙
        FeatureHelper.Set(feature, "QTGSX", "自墙", true, false); //自有墙
        FeatureHelper.Set(feature, "QTGSB", "自墙", true, false); //自有墙
    }

    // 列表项，点击加载户附属
    @Override
    public void listAdapterConvert(BaseAdapterHelper helper, final Feature item, final int deep) {
        super.listAdapterConvert(helper, item, deep);
        final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
        ll_list_item.setVisibility(View.GONE);
        helper.getView(R.id.ll_head).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapHelper.centerPoint(mapInstance.map, item.getGeometry());
                MapHelper.selectFeature(map, item);
                boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                if (!flag) {
                    final List<Feature> fs = new ArrayList<>();
                    queryChildFeature(FeatureHelper.TABLE_NAME_H_FSJG, item, fs, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            List<Feature> fs_p = new ArrayList<>();
                            fs_p.add(item);
                            mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_H_FSJG).buildListView(ll_list_item, fs, deep + 1);
                            return null;
                        }
                    });
                }
                ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
            }
        });
    }
    ///endregion

    //region 公有函数
    public static FeatureViewH From(MapInstance mapInstance, Feature f) {
        FeatureViewH fv = From(mapInstance);
        fv.set(f);
        return fv;
    }

    public static FeatureViewH From(MapInstance mapInstance) {
        FeatureViewH fv = new FeatureViewH();
        fv.set(mapInstance).set(mapInstance.getTable(FeatureHelper.TABLE_NAME_H));
        return fv;
    }

    public static FeatureTable GetTable(MapInstance mapInstance) {
        return MapHelper.getLayer(mapInstance.map, FeatureHelper.TABLE_NAME_H, FeatureHelper.LAYER_NAME_H).getFeatureTable();
    }

    public static void LoadAll(final MapInstance mapInstance, Feature f, final List<Feature> fs, AiRunnable callback) {
        LoadAll(mapInstance, mapInstance.getOrid(f), fs, callback);
    }

    public static void LoadAll(final MapInstance mapInstance, String orid, final List<Feature> fs, AiRunnable callback) {
        mapInstance.newFeatureView().queryChildFeature(FeatureHelper.TABLE_NAME_H, orid, "HH", "asc", fs, callback);
    }

    public static void Load(MapInstance mapInstance, String orid, final AiRunnable callback) {
        mapInstance.newFeatureView().findFeature(FeatureHelper.TABLE_NAME_H, orid, callback);
    }

    public static void InitFeatureAll(final MapInstance mapInstance, final Feature featureLJZ ,final AiRunnable callback) {
        if (featureLJZ != null) {
            // 户的id 是根据ZRZH 来遍的，要注意
            final String zrzh = FeatureHelper.Get(featureLJZ, "ZRZH", "");
            GetMaxID(mapInstance, zrzh, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    int count = 1;
                    if (objects.length > 1) {
                        count = AiUtil.GetValue(objects[1], 0) + 1;
                    }
                    int zcs = FeatureHelper.Get(featureLJZ, "ZCS", 1);
                    int szc = FeatureHelper.Get(featureLJZ, "SZC", 1);
                    final List<Feature> features = new ArrayList<Feature>();
                    for (int c = szc; c < szc + zcs; c++) {
                        String id = zrzh + String.format("%04d", count);
                        Feature f = GetTable(mapInstance).createFeature();
                        FeatureHelper.Set(f, "ID", id);
                        FeatureHelper.Set(f, "SZC", c);
                        f.setGeometry(MapHelper.geometry_copy(featureLJZ.getGeometry()));
                        mapInstance.fillFeature(f, featureLJZ);
                        features.add(f);
                        count++;
                    }
                    MapHelper.saveFeature(features, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            AiRunnable.Ok(callback, features);
                            return null;
                        }
                    });
                    return null;
                }
            });
        } else {
            ToastMessage.Send("没有幢信息！");
            AiRunnable.No(callback, null);
        }
    }

    public static void InitFeatureAll(final MapInstance mapInstance, final Feature featureLJZ, final List<Feature> fs_h, final AiRunnable callback) {
        if (featureLJZ != null) {
            // 户的id 是根据ZRZH 来遍的，要注意
            final String zrzh = FeatureHelper.Get(featureLJZ, "ZRZH", "");
            GetMaxID(mapInstance, zrzh, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    int count = 1;
                    if (objects.length > 1) {
                        count = AiUtil.GetValue(objects[1], 0) + 1;
                    }
                    int zcs = FeatureHelper.Get(featureLJZ, "ZCS", 1);
                    int szc = FeatureHelper.Get(featureLJZ, "SZC", 1);
                    final List<Feature> features = new ArrayList<Feature>();
                    for (int c = szc; c < szc + zcs; c++) {
                        if (!isExistenceFeature(fs_h, c)) {
                            String id = zrzh + String.format("%04d", count);
                            Feature f = GetTable(mapInstance).createFeature();
                            FeatureHelper.Set(f, "ID", id);
                            FeatureHelper.Set(f, "SZC", c);
                            f.setGeometry(MapHelper.geometry_copy(featureLJZ.getGeometry()));
                            mapInstance.fillFeature(f, featureLJZ);
                            features.add(f);
                            count++;
                        }
                    }
                    MapHelper.saveFeature(features, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            AiRunnable.Ok(callback, features);
                            return null;
                        }
                    });
                    return null;
                }
            });
        } else {
            ToastMessage.Send("没有幢信息！");
            AiRunnable.No(callback, null);
        }
    }

    private static boolean isExistenceFeature(List<Feature> fs_h, int c) {
        if (FeatureHelper.isExistElement(fs_h)) {
            for (Feature feature : fs_h) {
                String szc = FeatureHelper.Get(feature, "SZC", "");
                if ((c + "").equals(szc)) {
                    return true;
                }
            }

        }
        return false;
    }

    public static void ClearFeatureAll(final MapInstance mapInstance, final Feature featureLJZ, final AiRunnable callback) {
        if (featureLJZ != null) {
            mapInstance.newFeatureView().delChildFeature(FeatureHelper.TABLE_NAME_H, featureLJZ, callback);

        } else {
            ToastMessage.Send("没有逻辑幢信息！");
            AiRunnable.No(callback, null);
        }
    }

    // 带有诸多属性画户
    public static void CreateFeature(final MapInstance mapInstance, final Feature feature_ljz, final String cs, final AiRunnable callback) {
        CreateFeature(mapInstance, feature_ljz, cs, null, callback);
    }

    // 带有诸多属性画户
    public static void CreateFeature(final MapInstance mapInstance, final String orid, final String cs, final Feature feature_h, final AiRunnable callback) {
        FeatureViewLJZ.From(mapInstance).load(orid, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Feature featureLJZ = (Feature) t_;
                CreateFeature(mapInstance, featureLJZ, cs, feature_h, callback);
                return null;
            }
        });
    }

    public static void CreateFeature(final MapInstance mapInstance, String orid, final Feature feature, final AiRunnable callback) {
        // 去查宗地
        FeatureViewH.From(mapInstance).load(orid, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Feature f_p = (Feature) t_;
                CreateFeature(mapInstance, f_p, feature, callback);
                return null;
            }
        });
    }

    public static void CreateFeature(final MapInstance mapInstance, final Feature f_p, Feature f, final AiRunnable callback) {

        if (f_p != null && f_p.getFeatureTable() != mapInstance.getTable(FeatureHelper.TABLE_NAME_LJZ)) {
            // 如果不是逻辑
            String orid = mapInstance.getOrid_Match(f, FeatureHelper.TABLE_NAME_LJZ);
            if (StringUtil.IsNotEmpty(orid)) {
                CreateFeature(mapInstance, orid, f, callback);
                return;
            }
        }
        final FeatureViewH fv = From(mapInstance, f);
        final Feature feature;
        if (f == null) {
            feature = fv.table.createFeature();
        } else {
            feature = f;
        }
        if (f_p == null) {
            ToastMessage.Send("注意：缺少逻辑幢信息");
        }
        if (feature.getGeometry() == null && f_p != null) {
            feature.setGeometry(MapHelper.geometry_copy(f_p.getGeometry()));
        }
        final List<Feature> fs_update = new ArrayList<>();
        // 绘图
        fv.draw(new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 设置新的zddm
                fv.newHid(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String id = t_ + "";
                        FeatureHelper.Set(feature, "LJZH", id);
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
                return null;
            }
        });
    }

    // 逻辑幢带有诸多属性画户
    public static void CreateFeature(final MapInstance mapInstance, final Feature featureLJZ, final String cs, final Feature feature_h, final AiRunnable callback) {
        final Feature feature;
        if (feature_h == null) {
            feature = GetTable(mapInstance).createFeature();
        } else {
            feature = feature_h;
        }
        if (featureLJZ != null && featureLJZ.getFeatureTable() != mapInstance.getTable(FeatureHelper.TABLE_NAME_LJZ)) {
            String orid = mapInstance.getOrid_Match(feature, FeatureHelper.TABLE_NAME_LJZ);
            if (StringUtil.IsNotEmpty(orid)) {
                CreateFeature(mapInstance, orid, cs, feature, callback);
                return;
            }
        }
        if (featureLJZ != null && StringUtil.IsNotEmpty(cs)) {
            ToastMessage.Send("注意：逻辑幢等关联信息");
        }
        if (feature.getGeometry() == null && featureLJZ != null) {
            feature.setGeometry(MapHelper.geometry_copy(featureLJZ.getGeometry()));
        }
        final String cs_ = AiUtil.GetValue(cs, "1");
        mapInstance.command_draw(feature, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 户的id 是根据ZRZH 来遍的，要注意
                final String zrzh = FeatureHelper.Get(featureLJZ, "ZRZH", "");
                final String ljzh = FeatureHelper.Get(featureLJZ, "LJZH", "");
                NewID(mapInstance, zrzh, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String id = t_ + "";
                        FeatureHelper.Set(feature, "ID", id);
                        FeatureHelper.Set(feature, "SZC", cs_);
                        mapInstance.fillFeature(feature, featureLJZ);
                        mapInstance.newFeatureView(feature).fillFeatureAddSave(feature, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                AiRunnable.Ok(callback, feature);
                                mapInstance.viewFeature(feature);
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

    ///endregion

    //region 私有函数
    ///endregion

    //region 面积计算
    // 更新户分摊面积
    public void update_Area(Feature feature, List<Feature> fs_ftqk) {
        String orid = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID, "");
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        double f_ftjzmj = 0d;
        if (g != null) {
            area = MapHelper.getArea(mapInstance, g);
            for (Feature f : fs_ftqk) {
                String ftqx = FeatureHelper.Get(f, "FTQX_ID", "");
                if (orid.equals(ftqx)) {
                    f_ftjzmj += FeatureHelper.Get(f, "FTJZMJ", 0d);
                }
            }
            FeatureHelper.Set(feature, "SCFTJZMJ", AiUtil.Scale(f_ftjzmj, 2));
        }
    }

    // 核算宗地 占地面积、建筑面积
    public void update_Area(final AiRunnable callback) {
        final List<Feature> fs_h_fsjg = new ArrayList<>();
        final List<Feature> fs = new ArrayList<>();
        final List<Feature> update_fs = new ArrayList<>();
        String where = StringUtil.WhereByIsEmpty(getOrid()) + " FTQX_ID like '%" + getOrid() + "%' ";
        queryFeature(mapInstance.getTable(FeatureConstants.FTQK_TABLE_NAME), where, fs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                update_Area(feature, fs);
                AiRunnable.Ok(callback, t_, objects);
                return null;
            }
        });
    }

    public static void hsmj(Feature feature, MapInstance mapInstance, List<Feature> f_h_fsjgs) {
        String id = AiUtil.GetValue(feature.getAttributes().get(FeatureHelper.TABLE_ATTR_ORID));
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        if (g != null) {
            area = MapHelper.getArea(mapInstance, g);
            if (area > 0) {
                hsmj = area;
            }
        }
        for (Feature f : f_h_fsjgs) {
            String hid = FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
            if (hid.contains(id)) {
                double f_hsmj = AiUtil.GetValue(f.getAttributes().get("HSMJ"), 0d);
                hsmj += f_hsmj;
            }
        }
        feature.getAttributes().put("YCJZMJ", AiUtil.Scale(area, 2));
        feature.getAttributes().put("SCJZMJ", AiUtil.Scale(hsmj, 2));
    }
    ///endregion

    //region 图形识别
    public void identyH_FSJG(List<Feature> fs_h_fsjg, final AiRunnable callback) {
        String szc = FeatureHelper.Get(feature, "SZC", "1");
        String where = "LC='0' or LC='" + szc + "'";
        MapHelper.Query(mapInstance.getTable(FeatureHelper.TABLE_NAME_H_FSJG), feature.getGeometry(), 0.02, where, fs_h_fsjg, callback);
    }

    // 默认识别保存
    public void identyH_FSJG(final AiRunnable callback) {
        identyH_FSJG(feature, true, callback);
    }

    // 识别显示结果返回
    public void identyH_FSJG(final Feature f_h, final boolean isShow, final AiRunnable callback) {
        final List<Feature> fs_h_fsjg = new ArrayList<>();
        identyH_FSJG(fs_h_fsjg, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                FeatureViewH fv_ = FeatureViewH.From(mapInstance);
                Point p_h_l = GeometryEngine.labelPoint((Polygon) f_h.getGeometry());
                for (Feature f : fs_h_fsjg) {
                    // 判断附属结构是否属于本户
                    if (isNotBzdFsjg(f_h,f)){
                        continue;
                    }
                    fv.fillFeature(f, f_h);
                }

                if (isShow) {
                    fv_.fs_ref = ListUtil.asList(f_h);
                    QuickAdapter<Feature> adapter = fv_.getListAdapter(fs_h_fsjg, 0);
                    AiDialog dialog = AiDialog.get(mapInstance.activity, adapter);
                    dialog.setHeaderView(R.mipmap.app_map_layer_zrz, "识别到" + fs_h_fsjg.size() + "个户附属");
                    dialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapHelper.saveFeature(fs_h_fsjg, callback);
                            dialog.dismiss();
                        }
                    });
                } else {
                    MapHelper.saveFeature(fs_h_fsjg, callback);
                }
                return null;
            }
        });
    }


    public void identyZ_Fsjg(List<Feature> features_zrz, final AiRunnable callback) {
        MapHelper.Query(mapInstance.getTable(FeatureHelper.TABLE_NAME_Z_FSJG), feature.getGeometry(), features_zrz, callback);
    }

    // 默认识别保存
    public void identyZ_Fsjg(final AiRunnable callback) {
        identyZ_Fsjg(false, callback);
    }

    // 识别显示结果返回
    public void identyZ_Fsjg(final boolean isShow, final AiRunnable callback) {
        final List<Feature> fs_z_fsjg = new ArrayList<>();
        identyZ_Fsjg(fs_z_fsjg, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                FeatureViewZ_FSJG fv_ = FeatureViewZ_FSJG.From(mapInstance);
                fv_.fillFeature(fs_z_fsjg, feature);
                if (isShow) {
                    fv_.fs_ref = ListUtil.asList(feature);
                    QuickAdapter<Feature> adapter = fv_.getListAdapter(fs_z_fsjg, 0);
                    AiDialog dialog = AiDialog.get(mapInstance.activity, adapter);
                    dialog.setHeaderView(R.mipmap.app_map_layer_zrz, "识别到" + fs_z_fsjg.size() + "个附属");
                    dialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapHelper.saveFeature(fs_z_fsjg, callback);
                            dialog.dismiss();
                        }
                    });
                } else {
                    MapHelper.saveFeature(fs_z_fsjg, callback);
                }
                return null;
            }
        });
    }

    public void identyFtqk(final AiRunnable callback) {

    }
    ///endregion

    //region 编号
    //  获取最大的编号 户的id 是根据ZRZH 来遍的，要注意
    public static void GetMaxID(MapInstance mapInstance, String zrzh, AiRunnable callback) {
        MapHelper.QueryMax(GetTable(mapInstance), StringUtil.WhereByIsEmpty(zrzh) + "ID like '" + zrzh + "____'", "ID", zrzh.length(), 0, zrzh + "0000", callback);
    }

    //户的id 是根据ZRZH 来遍的，要注意
    public static void NewID(MapInstance mapInstance, final String zrzh, final AiRunnable callback) {
        GetMaxID(mapInstance, zrzh, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = "";
                if (objects.length > 1) {
                    // 最大号加1
                    int count = AiUtil.GetValue(objects[1], 0) + 1;
                    id = zrzh + String.format("%04d", count);
                }
                AiRunnable.Ok(callback, id);
                return null;
            }
        });
    }

    //  获取最大的编号
    public void getMaxHid(AiRunnable callback) {
        String id = getZrzh();
        MapHelper.QueryMax(table, StringUtil.WhereByIsEmpty(id) + "ID like '" + id + "____'", "ID", id.length(), 0, id + "0000", callback);
    }

    public void newHid(final AiRunnable callback) {
        getMaxHid(new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = "";
                if (objects.length > 1) {
                    String maxid = objects[0] + "";
                    // 最大号加1
                    int count = AiUtil.GetValue(objects[1], 0) + 1;

                }
                AiRunnable.Ok(callback, id);
                return null;
            }
        });
    }

    public void loadByHid(String hid, AiRunnable callback) {
        MapHelper.QueryOne(table, StringUtil.WhereByIsEmpty(hid) + " ID like '%'" + hid + "%' ", callback);
    }

    public static String GetID(Feature feature) {
        return AiUtil.GetValue(feature.getAttributes().get("ID"), "");
    }

    public void createBdcdyFromH(final Feature f_h, final AiRunnable callback) {
        if (TextUtils.isEmpty(FeatureHelper.Get(f_h, "ZRZH", "")) || TextUtils.isEmpty(FeatureHelper.Get(f_h, "LJZH", ""))) {
            ToastMessage.Send("缺少幢信息，请检查！");
            return;
        }
        FeatureEditQLR.CreateFeature(mapInstance, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final Feature featureBdcdy = (Feature) t_;
                String oridPath = FeatureHelper.Get(f_h, FeatureHelper.TABLE_ATTR_ORID_PATH, "")
                        + FeatureHelper.SEPARATORS_BASE + FeatureHelper.Get(f_h, FeatureHelper.TABLE_ATTR_ORID, "");
                String bdcdyh = FeatureHelper.Get(f_h, FeatureHelper.TABLE_ATTR_ID, "");
                FeatureHelper.Set(f_h, FeatureHelper.TABLE_ATTR_BDCDYH, bdcdyh);
                FeatureHelper.Set(featureBdcdy, FeatureHelper.TABLE_ATTR_BDCDYH, bdcdyh);
                FeatureHelper.Set(featureBdcdy, FeatureHelper.TABLE_ATTR_ORID_PATH, oridPath);
                List<Feature> updateFeatures = new ArrayList<>();
                updateFeatures.add(f_h);
                updateFeatures.add(featureBdcdy);
                MapHelper.saveFeature(updateFeatures, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        AiRunnable.Ok(callback, featureBdcdy, t_);
                        return null;
                    }
                });
                return null;
            }
        });
    }
    ///endregion
    //region 内部类或接口
    ///endregion

    //region 生成资料
    public void createDOCX(final MapInstance mapInstance, final Feature featureBdcdy, final boolean isRelaod, final AiRunnable callback) {
        final String bdcdyh = FeatureEditQLR.GetBdcdyh(featureBdcdy);
        String file_dcb_doc = FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh);
        if (FileUtils.exsit(file_dcb_doc) && !isRelaod) {
            AiRunnable.Ok(callback, file_dcb_doc);
        } else {
            FeatureEditBDC.LoadZD(mapInstance, bdcdyh, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            final Feature f_zd = (Feature) t_;
                            final List<Feature> fs_zrz = new ArrayList<Feature>();
                            final List<Feature> fs_ljz = new ArrayList<Feature>();
                            final List<Feature> fs_ftqk = new ArrayList<Feature>();
                            final List<Feature> fs_h = new ArrayList<Feature>();
                            final List<Feature> fs_jzd = new ArrayList<Feature>();
                            final List<Feature> fs_jzx = new ArrayList<Feature>();
                            final List<Feature> fs_z_fsjg = new ArrayList<Feature>();
                            final List<Feature> fs_bdc_h = new ArrayList<Feature>();
                            final Map<String, Feature> map_jzx = new HashMap<>();
                            final List<Map<String, Object>> fs_jzqz = new ArrayList<>();
                            final LinkedHashMap<Feature, List<Feature>> fs_c_all = new LinkedHashMap<>();

                            LoadAll(mapInstance, featureBdcdy, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_z_fsjg, fs_ljz, fs_ftqk, fs_bdc_h, fs_h, fs_c_all, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    final Feature f_h = (Feature) t_;
                                    if (f_h != null && fs_zrz.size() > 0) {
                                        createDOCX(mapInstance, featureBdcdy, f_h, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_z_fsjg, fs_h, isRelaod, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                // 数据归集
                                                final String docPath = (String) t_;
                                                final ArrayList<Map.Entry<String, List<Feature>>> zrz_c_s = new ArrayList<>();
                                                FeatureEditC.Load_FsAndH_GroupbyC_Sort(mapInstance, fs_zrz.get(0), zrz_c_s, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        Map.Entry<String, List<Feature>> c = null;
                                                        for (Map.Entry<String, List<Feature>> zrz_c : zrz_c_s) {
                                                            if (zrz_c.getKey().equals(FeatureHelper.Get(f_h, "SZC", 1) + "")) {
                                                                c = zrz_c;
                                                                break;
                                                            }
                                                        }
                                                        outputData(mapInstance, featureBdcdy, f_zd, fs_zrz, new ArrayList<Feature>(), fs_h, new ArrayList<Feature>(), fs_ftqk, f_h, c.getValue());
                                                        AiRunnable.Ok(callback, docPath, objects);
                                                        return null;
                                                    }
                                                });
                                                return null;
                                            }
                                        });

                                    } else {
                                        AiRunnable.Ok(callback, t_, objects);
                                    }
                                    return null;
                                }
                            });
                            return null;
                        }
                    }
            );
        }
    }

    public void LoadAll(final MapInstance mapInstance,
                        final Feature featureBdcdy,
                        final Feature f_zd,
                        final List<Feature> fs_jzd,
                        final List<Feature> fs_jzx,
                        final Map<String, Feature> map_jzx,
                        final List<Map<String, Object>> fs_jzqz,
                        final List<Feature> fs_zrz,
                        final List<Feature> fs_z_fsjg,
                        final List<Feature> fs_ljz,
                        final List<Feature> fs_ftqk,
                        final List<Feature> fs_bdc_h,
                        final List<Feature> fs_h,
                        final LinkedHashMap<Feature, List<Feature>> fs_c,
                        final AiRunnable callback) {
        final String bdcdyh = FeatureHelper.Get(featureBdcdy, FeatureHelper.TABLE_ATTR_BDCDYH, "");
        final String orid_bdc = FeatureHelper.GetLastOrid(featureBdcdy);
        FeatureEditBDC.LoadJZDXQZ(mapInstance, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                MapHelper.QueryOne(FeatureEdit.GetTable(mapInstance, FeatureConstants.H_TABLE_NAME), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID= '" + orid_bdc + "' ", new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        final Feature f_h = (Feature) t_;
                        fs_h.add(f_h);
                        MapHelper.Query(FeatureEdit.GetTable(mapInstance, FeatureConstants.FTQK_TABLE_NAME), StringUtil.WhereByIsEmpty(orid_bdc) + " FTQX_ID= '" + orid_bdc + "' ", -1, fs_ftqk, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                MapHelper.Query(FeatureEdit.GetTable(mapInstance, FeatureConstants.LJZ_TABLE_NAME), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID= '" + FeatureHelper.GetOrid(FeatureHelper.Get(f_h, FeatureHelper.TABLE_ATTR_ORID_PATH, ""), FeatureConstants.LJZ_TABLE_NAME) + "' ", "LJZH", "asc", -1, fs_ljz, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        MapHelper.Query(FeatureEdit.GetTable(mapInstance, FeatureConstants.ZRZ_TABLE_NAME), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID= '" + FeatureHelper.GetOrid(FeatureHelper.Get(f_h, FeatureHelper.TABLE_ATTR_ORID_PATH, ""), FeatureConstants.ZRZ_TABLE_NAME) + "' ", "ZRZH", "asc", -1, fs_zrz, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                FeatureEditBDC.LoadAllCAndFsToH(mapInstance, fs_zrz.get(0), FeatureHelper.Get(f_h, "SZC", ""), fs_c, callback);
                                                String where = "ORID_PATH like '" + FeatureHelper.GetOrid(FeatureHelper.Get(f_h, FeatureHelper.TABLE_ATTR_ORID_PATH, ""), FeatureConstants.ZRZ_TABLE_NAME) + "' "
                                                        + "and SZC='" + FeatureHelper.Get(f_h, "SZC", "") + "'";
                                                MapHelper.Query(FeatureEdit.GetTable(mapInstance, FeatureConstants.H_TABLE_NAME), where, -1, fs_h, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        AiRunnable.Ok(callback, f_h);
                                                        return null;
                                                    }
                                                });
                                                // TODO 还需要查询附属结构 阳台 等
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
                return null;
            }
        });
    }

    public void createDOCX(final MapInstance mapInstance, final Feature featureBdc, final Feature f, final Feature f_zd,
                           final List<Feature> fs_jzd,
                           final List<Feature> fs_jzx,
                           final Map<String, Feature> map_jzx,
                           final List<Map<String, Object>> fs_jzqz,
                           final List<Feature> fs_zrz,
                           final List<Feature> fs_z_fsjg,
                           final List<Feature> fs_h
            , boolean isRelaod, final AiRunnable callback) {
        final String bdcdyh = FeatureHelper.Get(featureBdc, FeatureHelper.TABLE_ATTR_BDCDYH, "");
        String file_dcb_doc = FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh);
        if (FileUtils.exsit(file_dcb_doc) && !isRelaod) {
            Log.i(TAG, "生成资料: 已经存在跳过");
            AiRunnable.Ok(callback, file_dcb_doc);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String zddm = FeatureHelper.Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, "");
                        Map<String, Object> map_ = new LinkedHashMap<>();
                        //  设置系统参数
                        FeatureEditBDC.Put_data_sys(map_);
                        //  设置宗地参数
                        FeatureEditBDC.Put_data_zd(mapInstance, map_, bdcdyh, f_zd);
                        // 在全局放一所以的不动产单元
                        FeatureEditBDC.Put_data_bdcdy(mapInstance, map_, featureBdc);
                        // 界址签字
                        FeatureEditBDC.Put_data_jzqz(map_, fs_jzd, fs_jzqz);
                        // 界址点
                        FeatureEditBDC.Put_data_jzdx(mapInstance, map_, zddm, fs_jzd, fs_jzx, map_jzx);
                        // 设置界址线
                        FeatureEditBDC.Put_data_jzx(mapInstance, map_, fs_jzx);
                        // 自然幢
                        FeatureEditBDC.Put_data_zrz(mapInstance, map_, bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h,null, new ArrayList<Feature>());
                        // 在全局放一个幢
                        FeatureEditBDC.Put_data_zrz(mapInstance, map_, fs_zrz);
                        // 在全局放所有户
                        FeatureEditBDC.Put_data_hs(mapInstance, map_, fs_h);
                        // 在全局放一个户
                        FeatureEditBDC.Put_data_h(mapInstance, map_, fs_h);
                        // 宗地草图
                        FeatureEditBDC.Put_data_zdct(mapInstance, map_, f_zd);
                        // 附件材料
                        FeatureEditBDC.Put_data_fjcl(mapInstance, map_, f_zd);

                        final String templet = FileUtils.getAppDirAndMK(FeatureEditBDC.GetPath_Templet()) + "不动产权籍调查表.docx";
                        final String file_dcb_doc = FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh);
                        String file_zd_zip = FeatureEditBDC.GetPath_ZD_zip(mapInstance, f_zd);
                        if (FileUtils.exsit(templet)) {
                            ReportUtils.exportWord(templet, file_dcb_doc, map_);
                            // 资料已经发生改变，移除压缩包
                            FileUtils.deleteFile(file_zd_zip);
                            Log.i(TAG, "生成资料: 生成完成");
                            AiRunnable.U_Ok(mapInstance.activity, callback, file_dcb_doc);
                        } else {
                            ToastMessage.Send("《不动产权籍调查表》模板文件不存在！");
                            AiRunnable.U_No(mapInstance.activity, callback, null);
                        }
                    } catch (Exception es) {
                        Log.i(TAG, "生成资料: 生成失败");
                        ToastMessage.Send("生成《不动产权籍调查表》失败", es);
                        AiRunnable.U_No(mapInstance.activity, callback, null);
                    }
                }
            }).start();
        }
    }

    public void outputData(final MapInstance mapInstance,
                           final Feature feature_bdc,
                           final Feature f_zd,
                           final List<Feature> fs_zrz,
                           List<Feature> fs_z_fsjg,
                           final List<Feature> fs_h,
                           List<Feature> fs_h_fsjg,
                           final List<Feature> fs_ftqk,
                           final Feature f_h,
                           final List<Feature> fs_c_all) {
        try {
            String bdcdyh = FeatureHelper.Get(feature_bdc, FeatureHelper.TABLE_ATTR_BDCDYH, "");
            final String file_dcb = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + "不动产权籍调查表" + bdcdyh + ".docx";
            FileUtils.copyFile(FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh), file_dcb);

            if (DxfHelper.TYPE == DxfHelper.TYPE_NEIMENG) {
                // 内蒙 城镇 陈总
                String dxf_fcfht = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + bdcdyh + "房产分户图.dxf";// fs_zrz =0
                new DxfFcfht_neimeng(mapInstance).set(dxf_fcfht).set(feature_bdc, f_h, f_zd, fs_zrz, fs_c_all).write().save();
            } else {
                String dxfDefault = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + bdcdyh + "房产分户图.dxf";
                new DxfFcfhtDefault(mapInstance).set(dxfDefault).set(feature_bdc, f_h, f_zd, fs_zrz, fs_c_all).write().save();
            }

        } catch (Exception es) {
            Log.e(TAG, "导出数据失败", es);
        }
    }
    ///endregion


}
