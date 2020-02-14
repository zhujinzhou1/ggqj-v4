package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.text.TextUtils;
import android.view.View;

import com.esri.arcgisruntime.data.ArcGISFeatureTable;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.layers.Layer;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.pojo.FeaturePojo;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.Callback;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewZ_FSJG extends FeatureView {
    //region 常量
    final static String TAG = "FeatureEditZD";
    ///endregion

    //region 字段
    ///endregion

    //region 构造函数
    public static FeatureViewZ_FSJG From(MapInstance mapInstance, Feature f) {
        FeatureViewZ_FSJG fv = From(mapInstance);
        fv.set(f);
        return fv;
    }

    public static FeatureViewZ_FSJG From(MapInstance mapInstance) {
        FeatureViewZ_FSJG fv = new FeatureViewZ_FSJG();
        fv.set(mapInstance).set(mapInstance.getTable("Z_FSJG"));
        return fv;
    }
    ///endregion

    //region 重写函数和回调
    @Override
    public void onCreate() {
        super.onCreate();
        iconColor = Color.rgb(0, 92, 230);
        iconDash = new DashPathEffect(new float[]{4, 4}, 0);// 虚线
    }

    public void fillFeature(Feature feature, Feature feature_zrz) {
        super.fillFeature(feature, feature_zrz);
        String id = FeatureHelper.Get(feature, "ID", "");
        String zid = FeatureHelper.Get(feature, "ZID", "");
        if (feature_zrz != null) {
            String zrzh = FeatureHelper.Get(feature_zrz, "ZRZH", "");
            id = zrzh + StringUtil.substr(id, zrzh.length());
            zid = zrzh;
            FeatureHelper.Set(feature, "ID", id);
        }
        String zz = "";
        // id有效
        if (id.length() == 28) {
            zid = StringUtil.substr(id, 0, id.length() - 4);
            FeatureHelper.Set(feature, "ZID", zid);
        }
        // zid 有效
        if (zid.length() == 24) {
            zz = StringUtil.substr_last(zid, 4);
            FeatureHelper.Set(feature, "ZH", zz);
        }
        int lc = FeatureHelper.Get(feature, "LC", 1);
        FeatureHelper.Set(feature, "LC", lc);
    }

    @Override
    public String addActionBus(String groupname) {
        int count = mapInstance.getSelFeatureCount();
        // 根据画宗地推荐

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
    public String queryChildWhere() {
        return super.queryChildWhere();
    }

    @Override
    public void hsmj(Feature feature, MapInstance mapInstance) {
        String fhmc = FeatureHelper.Get(feature, "FHMC", "");
        double type = FeatureHelper.Get(feature, "TYPE", 0d);
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
            // feature.getAttributes().put("MC",(type==0?"":(type==1?"双柱门廊，多柱门廊":"单柱门廊，凹槽式门廊")));
            FeatureHelper.Set(feature, "MC", fhmc + "");
        } else {
            FeatureHelper.Set(feature, "MC", fhmc + "" + (type == 0 ? "" : (type == 1 ? "（全）" : "（半）")));
        }
        FeatureHelper.Set(feature, "MJ", AiUtil.Scale(area, 2));
        FeatureHelper.Set(feature, "HSMJ", AiUtil.Scale(hsmj, 2));

    }

    @Override
    public void listAdapterConvert(BaseAdapterHelper helper, Feature item, int deep) {
        super.listAdapterConvert(helper, item, deep);
    }

    ///endregion

    //region 公有函数
    public void queryFatherFeature(MapInstance mapInstance, String fathertable, String orid_, Feature feature, List<Feature> features, AiRunnable callback) {
        String orid_path = FeatureHelper.Get(feature, "ORID_PATH", "");
        if (StringUtil.IsEmpty(orid_path)) {
            return;
        }
        String fatherOrid = "";
        String[] orids = orid_path.split("/");
        for (String orid : orids) {
            if (orid.contains(orid_.toUpperCase())) {
                fatherOrid = orid;
                break;
            }
        }
        String where = "ORID_PATH like '%" + fatherOrid + "%'";
//        MapHelper.Query(table,where_,orderby,sort,0,true,fs,callback);
        MapHelper.Query(mapInstance.getTable(fathertable), where, 0, features, callback);
    }

    /**
     * 智能识别幢附属结构，通过标注生成附属结构。
     *
     * @param callback
     */
    public void initFeatureZ_fsjg(AiRunnable callback) {
        FeaturePojo featurePojo = new FeaturePojo(feature, "BZ");
        List<String> lcs = featurePojo.getLc();
        List<Feature> fs_h = new ArrayList<>();
        for (String lc : lcs) {
            Feature f_h = mapInstance.getTable(FeatureHelper.TABLE_NAME_Z_FSJG).createFeature();
            f_h.setGeometry(feature.getGeometry());
            FeatureHelper.Set(f_h, "LC", lc);
            FeatureHelper.Set(f_h, "FHMC", featurePojo.getName());
            FeatureHelper.Set(f_h, "ZRZH", featurePojo.getZrzh());
            FeatureHelper.Set(f_h, "TYPE", "1");
            mapInstance.fillFeature(f_h);
            fs_h.add(f_h);
        }
        AiRunnable.Ok(callback, fs_h);
    }

    public void fsjg_init(final AiRunnable callback) {
        final Feature z_fsjg = getFeatureTable().createFeature();
        mapInstance.command_draw(z_fsjg, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                mapInstance.newFeatureView(feature).fillFeatureAddSave(z_fsjg, callback);
                return null;
            }
        });

    }

    public FeatureTable getFeatureTable() {
        return mapInstance.getTable(FeatureHelper.TABLE_NAME_Z_FSJG);
    }

    public static FeatureTable GetTable(MapInstance mapInstance) {
        return MapHelper.getLayer(mapInstance.map, "Z_FSJG", "幢附属结构").getFeatureTable();
    }

    public static void LoadAll(final MapInstance mapInstance, Feature f, final List<Feature> fs, AiRunnable callback) {
        LoadAll(mapInstance, mapInstance.getOrid(f), fs, callback);
    }

    public static void LoadAll(final MapInstance mapInstance, String orid, final List<Feature> fs, AiRunnable callback) {
        mapInstance.newFeatureView().queryChildFeature("Z_FSJG", orid, "ID", "asc", fs, callback);
    }

    public static void Load(MapInstance mapInstance, String orid, final AiRunnable callback) {
        mapInstance.newFeatureView().findFeature("Z_FSJG", orid, callback);
    }

    // 获取id
    public static String GetID(Feature feature) {
        return FeatureHelper.Get(feature, "ID", "");
    }

    //  获取最大的编号 幢附属的id 是根据ZRZH 来遍的，要注意
    public static void GetMaxID(MapInstance mapInstance, String zrzh, AiRunnable callback) {
        MapHelper.QueryMax(GetTable(mapInstance), StringUtil.WhereByIsEmpty(zrzh) + "ID like '" + zrzh + "____'", "ID", zrzh.length(), 0, zrzh + "0000", callback);
    }

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

    // 带有诸多属性画户
    public static void CreateFeature(final MapInstance mapInstance, final String orid, final Feature feature, final String lc, final AiRunnable callback) {
        FeatureViewLJZ.From(mapInstance).load(orid, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {

                Feature featureLJZ = (Feature) t_;
                CreateFeature(mapInstance, featureLJZ, feature, lc, callback);

                return null;
            }
        });
    }

    // 带有诸多属性画户附属结构 同时挖空范围内的 幢、户、附属
    public static void CreateFeatureHollow(final MapInstance mapInstance, final Feature featureLJZ, final Feature feature_fsjg, final String lc, final AiRunnable callback) {
        final Feature feature;
        if (feature_fsjg == null) {
            feature = GetTable(mapInstance).createFeature();
        } else {
            feature = feature_fsjg;
        }
        if (featureLJZ != null) {
            ToastMessage.Send("注意：缺少逻辑幢信息");
        }
        mapInstance.command_draw(feature, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                List<Layer> layers = MapHelper.getLayers(mapInstance.map, "Z_FSJG", "H", "H_FSJG");
                List<Feature> fs_ref = new ArrayList<>();
                final List<Feature> fs_update = new ArrayList<>();
                MapHelper.Hollow(mapInstance.map, layers, (Polygon) feature.getGeometry(), fs_ref, fs_update, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        final String ljzh = FeatureHelper.Get(featureLJZ, "LJZH", "");
                        final String zrzh = FeatureHelper.Get(featureLJZ, "ZRZH", "");
                        NewID(mapInstance, zrzh, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                String id = t_ + "";
                                FeatureHelper.Set(feature, "ID", id);
                                FeatureHelper.Set(feature, "ZRZH", zrzh);
                                FeatureHelper.Set(feature, "LJZH", ljzh);
                                FeatureHelper.Set(feature, "LC", lc + "");
                                mapInstance.fillFeature(feature, featureLJZ);

                                MapHelper.saveFeature(fs_update, new AiRunnable(callback) {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        mapInstance.viewFeature(feature);
                                        AiRunnable.Ok(callback, t_);
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

    // 带有诸多属性画户附属结构
    public static void CreateFeature(final MapInstance mapInstance, final Feature featureLJZ, final Feature feature_fsjg, final String lc, final AiRunnable callback) {
        final Feature feature;
        if (feature_fsjg == null) {
            feature = GetTable(mapInstance).createFeature();
        } else {
            feature = feature_fsjg;
        }
        if (featureLJZ != null && featureLJZ.getFeatureTable() != mapInstance.getTable("LJZ")) {
            String orid = mapInstance.getOrid_Match(feature, "LJZ");
            if (StringUtil.IsNotEmpty(orid)) {
                CreateFeature(mapInstance, orid, feature, lc, callback);
                return;
            }
        }

        if (featureLJZ == null) {
            ToastMessage.Send("注意：缺少幢信息");
        }
        mapInstance.command_draw(feature, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final String zrzh = FeatureHelper.Get(featureLJZ, "ZRZH", "");
                final String ljzh = FeatureHelper.Get(featureLJZ, "LJZH", "");
                NewID(mapInstance, zrzh, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String id = t_ + "";
                        FeatureHelper.Set(feature, "ID", id);
                        FeatureHelper.Set(feature, "ZRZH", zrzh);
                        FeatureHelper.Set(feature, "LJZH", ljzh);
                        FeatureHelper.Set(feature, "LC", lc + "");
                        mapInstance.fillFeature(feature, featureLJZ);
                        mapInstance.newFeatureView(feature).fillFeatureAddSave(feature, callback);
                        return null;
                    }
                });
                return null;
            }
        });
    }

    public static void ClearFeatureAll(final MapInstance mapInstance, final Feature featureLJZ, final AiRunnable callback) {
        if (featureLJZ != null) {
            mapInstance.newFeatureView().delChildFeature("Z_FSJG", featureLJZ, callback);
        } else {
            ToastMessage.Send("没有幢信息！");
            AiRunnable.No(callback, null);
        }
    }

    //  逻辑幢识别附属
    public static void IdentyLJZ_FSJG(final MapInstance mapInstance, final Feature f_ljz, final AiRunnable callback) {
        final String ljzh = FeatureHelper.Get(f_ljz, "LJZH", "");
        final String zrzh = FeatureHelper.Get(f_ljz, "ZRZH", "");
        final int szc = FeatureHelper.Get(f_ljz, "SZC", 1);
        final int zcs = FeatureHelper.Get(f_ljz, "ZCS", 1);
        final String oridLjz = FeatureHelper.Get(f_ljz, "ORID", "");

        if (StringUtil.IsNotEmpty(ljzh)) {
            final List<Feature> features_zfsjg = new ArrayList<Feature>();
            final List<Feature> features_update = new ArrayList<Feature>();
            final List<Feature> features_save = new ArrayList<Feature>();
            // 放到 0.2米的范围
            MapHelper.Query(GetTable(mapInstance), f_ljz.getGeometry(), 0.05, features_zfsjg, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    double area_jzmj = FeatureHelper.Get(f_ljz, "SCJZMJ", 0d);
                    int count = 0;
                    for (Feature f : features_zfsjg) {


                        String orid = FeatureHelper.GetOrid(FeatureHelper.Get(f, "ORID_PATH", ""), "LJZ");
                        if (StringUtil.IsNotEmpty(oridLjz) && (TextUtils.isEmpty(orid) || oridLjz.equals(orid))) {
                            // 幢附属结构 orid 为空或者 或者包含 oridLjz
                            String path = FeatureHelper.Get(f_ljz, "ORID_PATH", "") + "/" + FeatureHelper.Get(f_ljz, "ORID", "");
                            FeatureHelper.Set(f, "ORID_PATH", path);
                        }
                        final String h_id = FeatureHelper.Get(f, "ID", "");
                        final String h_ljzh = FeatureHelper.Get(f, "LJZH", "");
                        final String h_zrzh = FeatureHelper.Get(f, "ZRZH", "");
                        final int h_lc = FeatureHelper.Get(f, "LC", 1);

                        // 自然幢号范围唯一
                        if (h_id.startsWith(zrzh) && h_zrzh.equals(zrzh)) {
                            int i = AiUtil.GetValue(h_id.substring(zrzh.length()), 0);
                            if (count < i) {
                                count = i;
                            }
                        } else {
                            // 楼层区间 正确
                            if (szc <= h_lc && h_lc < szc + zcs) {
                                features_update.add(f);
                            }
                        }

                    }
                    if (features_update.size() > 0) {
                        for (Feature f : features_update) {
                            count++;
                            String i = String.format("%04d", count);
                            FeatureHelper.Set(f, "ID", zrzh + i);
                        }
                    }
                    // 更新其他属性
                    for (Feature f : features_zfsjg) {

                        final int f_lc = FeatureHelper.Get(f, "LC", 1);
                        final String f_ljzh = FeatureHelper.Get(f, "LJZH", "");
                        double f_hsmj = FeatureHelper.Get(f, "HSMJ", 0d);
                        // 楼层区间 正确、本逻辑幢的
                        if (szc <= f_lc && f_lc < szc + zcs && (f_ljzh.equals(ljzh) || TextUtils.isEmpty(f_ljzh))) {

                            double area_item = MapHelper.getArea(mapInstance, f.getGeometry());
                            double area_scjzmj = FeatureHelper.Get(f, "SCJZMJ", 0d);
                            final int h_szc = FeatureHelper.Get(f, "SZC", 1);
                            if (area_scjzmj < area_item - 1) {
                                area_scjzmj = area_item;
                            }
                            FeatureHelper.Set(f, "SCJZMJ", area_scjzmj);
                            FeatureHelper.Set(f, "SZC", h_szc + "");
                            features_save.add(f);
                            area_jzmj += f_hsmj;
                        }
                    }
                    FeatureHelper.Set(f_ljz, "SCJZMJ", AiUtil.GetValue(String.format("%.2f", area_jzmj), area_jzmj));
                    mapInstance.fillFeature(features_save, f_ljz);
                    features_save.add(f_ljz);
                    MapHelper.saveFeature(features_save, callback);
                    return null;
                }
            });
        } else {
            AiRunnable.Ok(callback, null);
        }
    }

    public static void initAllZ_fsjgToLc(MapInstance mapInstance, Feature feature) {
        String szc = FeatureHelper.Get(feature, "SZC", "");
        if (szc.trim().length() == 1) {
            return;
        }
        HashSet<Integer> list = new HashSet<>();
        if (szc.contains("-") && szc.contains(",")) {
            String[] strs = szc.split(",");
            for (String str : strs) {
                if (str.contains("-")) {
                    String[] s_ = str.split("-");
                    if (s_.length != 2 || StringUtil.IsEmpty(s_[0].trim()) || StringUtil.IsEmpty(s_[1].trim())) {
                        ToastMessage.Send("附属结构SZC属性输入有误请重新输入！");
                    } else {
                        int i1 = Integer.parseInt(s_[0].trim());
                        int i2 = Integer.parseInt(s_[1].trim());
                        int j1, j2;
                        if (i1 >= i2) {
                            j1 = i2;
                            j2 = i1;
                        } else {
                            j1 = i1;
                            j2 = i2;
                        }
                        for (int i = j1; i <= j2; i++) {
                            list.add(i); //去重复
                        }
                    }
                } else {
                    if (StringUtil.IsNotEmpty(str.trim())) {
                        list.add(Integer.parseInt(str.trim()));
                    }
                }
            }

        } else if (FeatureHelper.Get(feature, "SZC", "").contains("-")) {
            String[] s_ = szc.split("-");
            if (s_.length != 2 || StringUtil.IsEmpty(s_[0].trim()) || StringUtil.IsEmpty(s_[1].trim())) {
                ToastMessage.Send("附属结构SZC属性输入有误请重新输入！");
            } else {
                int i1 = Integer.parseInt(s_[0].trim());
                int i2 = Integer.parseInt(s_[1].trim());
                int j1, j2;
                if (i1 >= i2) {
                    j1 = i2;
                    j2 = i1;
                } else {
                    j1 = i1;
                    j2 = i2;
                }
                for (int i = j1; i <= j2; i++) {
                    list.add(i); //去重复
                }
            }
        } else if (szc.contains(",")) {
            String[] split = szc.split(",");
            for (String s : split) {
                if (StringUtil.IsNotEmpty(s.trim())) {
                    list.add(Integer.parseInt(s.trim()));
                }
            }
        }
        List<Feature> fsSave = new ArrayList<>();
        List<Feature> fsDel = new ArrayList<Feature>();
        fsDel.add(feature);
        for (int c : list) {
            Feature f_ = FeatureHelper.Copy(feature);
            f_.setGeometry(feature.getGeometry());
            FeatureHelper.Set(f_, "SZC", c);
            FeatureHelper.Set(f_, "ORID_PATH", FeatureHelper.Get(feature, "ORID_PATH", ""));
            fsSave.add(f_);
        }
        MapHelper.saveFeature(fsSave, fsDel, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                return null;
            }
        });
    }

    // copy 附属结构
    public static void copylc(final MapInstance mapInstance, final Feature featureZ_fsjg, final String lc, final AiRunnable callback) {
        final Map<String, Object> map = new LinkedHashMap<>();
        final int lc_f = AiUtil.GetValue(lc, 1);
        map.put("szc", (lc_f + 1) + "");
        final AiDialog aidialog = AiDialog.get(mapInstance.activity);
        aidialog.setHeaderView(R.mipmap.app_icon_warning_red, "不可逆操作提醒")
                .setContentView("该操作主要是将选中的附属结构，复制到新的楼层，请谨慎处理！")
                .setContentView("请输入需要复制到的层数，多层使用“,”、“-”隔开：如：3,5,7 或是 2-7 层")
                .addContentView("如：3,5,7 将复制到3、5、7层")
                .addContentView("如：3-7 将复制到3、4、5、6、7层")
                .addContentView(aidialog.getEditView("请输入要复制到的楼层", map, "szc"))
                .setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String szc = map.get("szc") + "";
                        final List<Integer> nubs = StringUtil.GetNumbers(szc);
                        aidialog.setContentView("输入的楼层为“" + szc + "”：将复制到" + StringUtil.Join(nubs) + "层");
                        aidialog.setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    List<Feature> fs_save = new ArrayList<>();
                                    for (int szc : nubs) {
                                        // 复制幢附属结构
                                        Feature f_ = FeatureHelper.Copy(featureZ_fsjg);
                                        f_.setGeometry(featureZ_fsjg.getGeometry());
                                        FeatureHelper.Set(f_, "LC", szc);
                                        fs_save.add(f_);
                                    }
                                    mapInstance.fillFeature(fs_save);
                                    MapHelper.saveFeature(fs_save, callback);
                                } catch (Exception es) {
                                    ToastMessage.Send("复制层失败！", es);
                                }
                                aidialog.dismiss();
                            }
                        });
                    }
                });
    }


    public static void addFtqk(MapInstance mapInstance, Feature new_feature_ft, Feature feature) {
        FeatureEditFTQK.initAddFt(new_feature_ft, feature);
        FeatureEditFTQK.initAfterAddFt(new_feature_ft, feature);
        mapInstance.featureView.fillFeature(new_feature_ft);
    }
    ///endregion

    //region 私有函数
    ///endregion

    //region 面积计算
    ///endregion

    //region 内部类或接口
    ///endregion


    // 通过幢附属结构查feature


}
