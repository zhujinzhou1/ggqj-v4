package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LiuSheng on 2017/7/28.
 */

public class FeatureEditH_FSJG extends FeatureEdit {
    //region 常量
    final static String TAG = "FeatureEditH_FSJG";
    ///endregion

    //region 字段
    FeatureViewH_FSJG fv;
    ///endregion

    //region 构造函数
    public FeatureEditH_FSJG() {
        super();
    }

    public FeatureEditH_FSJG(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }
    ///endregion

    //region 重写函数和回调
    @Override
    public void onCreate() {
        super.onCreate();
        // 使用 fv
        if (super.fv instanceof FeatureViewH_FSJG) {
            this.fv = (FeatureViewH_FSJG) super.fv;
        }
    }

    @Override
    public void init() {
        super.init();
        // 菜单
        menus = new int[]{R.id.ll_info};
    }

    // 显示数据
    @Override
    public void build() {
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_h_fsjg, v_content);
        try {
            if (feature != null) {
                mapInstance.fillFeature(feature);
                fillView(v_feature);
                hsmj();

                final Spinner spn_type = (Spinner) v_feature.findViewById(R.id.spn_type);
                final Spinner spn_fhmc = (Spinner) v_feature.findViewById(R.id.spn_fhmc);

                spn_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String value = AiUtil.GetValue(spn_type.getAdapter().getItem(position), "");
                        setDicValue(feature, "TYPE", value);
                        hsmj();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        setValue(feature, "TYPE", null);
                    }
                });
                spn_fhmc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String value = AiUtil.GetValue(spn_fhmc.getAdapter().getItem(position), "");
                        setDicValue(feature, "FHMC", value);
                        //
                        if ("单柱门廊".equals(value) || "凹槽式门廊".equals(value) || "凹槽".equals(value)) {
                            spn_type.setSelection(1);
                        } else if ("多柱门廊".equals(value) || "双柱门廊".equals(value)) {
                            spn_type.setSelection(0);
                        }
                        hsmj();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        setValue(feature, "FHMC", null);
                    }
                });

                v_feature.findViewById(R.id.tv_jcgx).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((EditText) v_feature.findViewById(R.id.et_id)).setText("");
                        ((EditText) v_feature.findViewById(R.id.et_hh)).setText("");
//                        ((EditText) v_feature.findViewById(R.id.et_hid)).setText("");
                        feature.getAttributes().put("ID", "");
                        feature.getAttributes().put("HH", "");
                        feature.getAttributes().put("HID", "");
                        feature.getAttributes().put(FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                        ToastMessage.Send(activity, "保存后，该附属结构将解除与户的关系！");
                    }
                });

                v_feature.findViewById(R.id.tv_znsbhfsjg).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fv.initFeatureH_fsjg(new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                MapHelper.saveFeature((List<Feature>) t_, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        ToastMessage.Send(activity, "初始化完成！");
                                        return null;
                                    }
                                });
                                return null;
                            }
                        });

                    }
                });

            }
        } catch (Exception es) {
            Log.e(TAG, "build: 构建失败", es);
        }
    }

    @Override
    public void build_opt() {
        super.build_opt();
        addAction("复制", R.mipmap.app_icon_copy, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copylc(mapInstance, feature, FeatureHelper.Get(feature, "LC", ""), new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        ToastMessage.Send("复制成功");
                        return null;
                    }
                });
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
    ///endregion

    //region 公有函数
    public static FeatureTable GetTable(MapInstance mapInstance) {
        return MapHelper.getLayer(mapInstance.map, FeatureHelper.TABLE_NAME_H_FSJG, FeatureHelper.LAYER_NAME_H_FSJG).getFeatureTable();
    }

    public static void LoadAll(final MapInstance mapInstance, Feature f, final List<Feature> fs, AiRunnable callback) {
        LoadAll(mapInstance, mapInstance.getOrid(f), fs, callback);
    }

    public static void LoadAll(final MapInstance mapInstance, String orid, final List<Feature> fs, AiRunnable callback) {
        mapInstance.newFeatureView().queryChildFeature(FeatureHelper.TABLE_NAME_H_FSJG, orid, "ID", "asc", fs, callback);
    }

    public static void Load(MapInstance mapInstance, String orid, final AiRunnable callback) {
        mapInstance.newFeatureView().findFeature(FeatureHelper.TABLE_NAME_H_FSJG, orid, callback);
    }

    // 获取id
    public static String GetID(Feature feature) {
        return AiUtil.GetValue(feature.getAttributes().get("ID"), "");
    }

    //  获取最大的编号
    public static void GetMaxID(MapInstance mapInstance, String hid, AiRunnable callback) {
        MapHelper.QueryMax(GetTable(mapInstance), StringUtil.WhereByIsEmpty(hid) + "ID like '" + hid + "____'", "ID", hid.length(), 0, hid + "0000", callback);
    }

    public static void NewID(MapInstance mapInstance, final String hid, final AiRunnable callback) {
        GetMaxID(mapInstance, hid, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = "";
                if (objects.length > 1) {
                    // 最大号加1
                    int count = AiUtil.GetValue(objects[1], 0) + 1;
                    id = hid + String.format("%04d", count);
                }
                AiRunnable.Ok(callback, id);
                return null;
            }
        });
    }

    // 带有诸多属性画户附属
    public static void CreateFeature(final MapInstance mapInstance, final String orid, final Feature feature_fsjg, final String lc, final AiRunnable callback) {
        FeatureViewH.Load(mapInstance, orid, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Feature featureH = (Feature) t_;
                CreateFeature(mapInstance, featureH, feature_fsjg, lc, callback);
                return null;
            }
        });
    }

    // 带有诸多属性画户
    public static void CreateFeature(final MapInstance mapInstance, final Feature featureH, final Feature feature_fsjg, final String lc, final AiRunnable callback) {
        final Feature feature;
        if (feature_fsjg == null) {
            feature = GetTable(mapInstance).createFeature();
        } else {
            feature = feature_fsjg;
        }
        if (featureH != null && featureH.getFeatureTable() != mapInstance.getTable(FeatureHelper.TABLE_NAME_H)) {
            String orid = mapInstance.getOrid_Match(feature, FeatureHelper.TABLE_NAME_H);
            if (StringUtil.IsNotEmpty(orid)) {
                CreateFeature(mapInstance, orid, feature, lc, callback);
                return;
            }
        }
        if (featureH == null) {
            ToastMessage.Send("注意：缺少户信息");
        }
//        if (feature.getGeometry() == null && featureH != null) {
//            feature.setGeometry(MapHelper.geometry_copy(featureH.getGeometry()));
//        }
        mapInstance.command_draw(feature, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final String hid = FeatureHelper.Get(featureH, "ID", "");
                NewID(mapInstance, hid, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String id = t_ + "";
                        feature.getAttributes().put("ID", id);
                        feature.getAttributes().put("HID", hid);
                        feature.getAttributes().put("LC", lc + "");
                        mapInstance.fillFeature(feature, featureH);
                        mapInstance.newFeatureView(feature).fillFeatureAddSave(feature, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
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

    //  户识别附属
    public static void IdentyH_FSJG(final MapInstance mapInstance, final Feature f_h, final AiRunnable callback) {

        final String hid = FeatureHelper.Get(f_h, "ID", "");
        final int hch = FeatureHelper.Get(f_h, "SZC", 1);
        if (StringUtil.IsNotEmpty(hid)) {
            final List<Feature> features_hfsjg = new ArrayList<Feature>();
            final List<Feature> features_update = new ArrayList<Feature>();
            final List<Feature> features_save = new ArrayList<Feature>();
            // 放到 0.2米的范围
            MapHelper.Query(GetTable(mapInstance), f_h.getGeometry(), 0.2, features_hfsjg, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    double h_area = MapHelper.getArea(mapInstance, f_h.getGeometry());
                    double area_jzmj = h_area;
                    int count = 0;
                    for (Feature f : features_hfsjg) {
                        final String f_id = FeatureHelper.Get(f, "ID", "");
                        final String f_hid = FeatureHelper.Get(f, "HID", "");
                        final String orid_path = FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                        final String f_hh = FeatureHelper.Get(f, "HH", "");
                        int f_lc = FeatureHelper.Get(f, "LC", 0);
                        double f_hsmj = FeatureHelper.Get(f, "HSMJ", 0d);

                        //  只检查 没有被识别过或本户的 和 没有层或本层的
                        if ((StringUtil.IsEmpty(f_hid) || StringUtil.IsEmpty(orid_path) || f_hid.equals(hid)) && (f_lc == 0 || hch == f_lc)) {
                            area_jzmj += f_hsmj;
//                            FeatureHelper.Set(f,"HID", hid);
////                            f.getAttributes().put("HH", hid.length()>4?hid.substring(hid.length()-4):hid);
//                            f.getAttributes().put("HH", hh);
//                            f.getAttributes().put("LC", hch+"");
                            features_save.add(f);
                            if (StringUtil.IsNotEmpty(f_hh) && hid.equals(f_hid) && f_id.startsWith(hid)) {
                                int i = AiUtil.GetValue(f_id.substring(f_hid.length()), 0);
                                if (count < i) {
                                    count = i;
                                }
                            } else {
                                features_update.add(f);
                            }
                        }
                    }
                    if (features_update.size() > 0) {
                        for (Feature f : features_update) {
                            count++;
                            String i = String.format("%04d", count);
                            FeatureHelper.Set(f, "ID", hid + i);
                        }
                    }
                    mapInstance.fillFeature(features_save, f_h);
                    f_h.getAttributes().put("YCJZMJ", AiUtil.GetValue(String.format("%.2f", h_area), h_area));
                    f_h.getAttributes().put("SCJZMJ", AiUtil.GetValue(String.format("%.2f", area_jzmj), area_jzmj));
                    features_save.add(f_h);
                    MapHelper.saveFeature(features_save, callback);

                    return null;
                }
            });
        } else {
            AiRunnable.Ok(callback, null);
        }
    }

    // 户识别户附属结构--ZBCL
    public static void IdentyH_FSJG_(final MapInstance mapInstance, final Feature f_h, final AiRunnable callback) {
        final String hid = FeatureHelper.Get(f_h, "ID", "");
        final int hch = FeatureHelper.Get(f_h, "SZC", 1);
        final String orid_path = FeatureHelper.Get(f_h, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
        String orid = orid_path.substring(orid_path.lastIndexOf("/"));
        String ljzOrid = "";
        if (orid.contains(FeatureHelper.TABLE_NAME_LJZ)) {
            ljzOrid = orid;
        }
        final List<Feature> features_hfsjg = new ArrayList<Feature>();
        final List<Feature> features_update = new ArrayList<Feature>();
        final List<Feature> features_save = new ArrayList<Feature>();
        // 放到 0.2米的范围
        final String finalLjzOrid = ljzOrid;
        MapHelper.Query(GetTable(mapInstance), f_h.getGeometry(), 0.2, features_hfsjg, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                double h_area = MapHelper.getArea(mapInstance, f_h.getGeometry());
                double area_jzmj = h_area;
                int count = 0;
                for (Feature f : features_hfsjg) {
                    final String f_id = FeatureHelper.Get(f, "ID", "");
                    final String f_hid = FeatureHelper.Get(f, "HID", "");
                    final String f_hh = FeatureHelper.Get(f, "HH", "");
                    final String f_orid = FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                    if (TextUtils.isEmpty(f_orid) || !f_orid.contains("/")) {
                        break;
                    }
                    final String f_orid_last = f_orid.substring(f_orid.lastIndexOf("/"));
                    int f_lc = FeatureHelper.Get(f, "LC", 0);
                    double f_hsmj = FeatureHelper.Get(f, "HSMJ", 0d);

                    //  只检查 没有被识别过或本户的 和 没有层或本层的
                    if ((StringUtil.IsNotEmpty(finalLjzOrid) && finalLjzOrid.equals(f_orid_last))
                            && ((StringUtil.IsEmpty(f_hid) || f_hid.equals(hid)) && (f_lc == 0 || hch == f_lc))) {
                        area_jzmj += f_hsmj;
//                            FeatureHelper.Set(f,"HID", hid);finalLjzOrid.equals(f_orid_last)
////                            f.getAttributes().put("HH", hid.length()>4?hid.substring(hid.length()-4):hid);
//                            f.getAttributes().put("HH", hh);
//                            f.getAttributes().put("LC", hch+"");
                        mapInstance.fillFeature(f, f_h);
                        features_save.add(f);
                        if (StringUtil.IsNotEmpty(f_hh) && hid.equals(f_hid) && f_id.startsWith(hid)) {
                            int i = AiUtil.GetValue(f_id.substring(f_hid.length()), 0);
                            if (count < i) {
                                count = i;
                            }
                        } else {
                            features_update.add(f);
                        }
                    }
                }
                if (features_update.size() > 0) {
                    for (Feature f : features_update) {
                        count++;
                        String i = String.format("%04d", count);
                        FeatureHelper.Set(f, "ID", hid + i);
                    }
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

    // 递归绑定权属下的
    public static void BuildView_H_FSJG(final MapInstance mapInstance, final LinearLayout ll_list, final Feature feature_h, final int deep) {
        if (ll_list.getTag() == null) {
            QuickAdapter<Feature> adpter = new QuickAdapter<Feature>(mapInstance.activity, R.layout.app_ui_ai_aimap_h_fsjg_item, new ArrayList<Feature>()) {
                @Override
                protected void convert(final BaseAdapterHelper helper, final Feature item) {
                    final String name = AiUtil.GetValue(item.getAttributes().get("MC"), "");
                    final String id = AiUtil.GetValue(item.getAttributes().get("ID"), "");
//                        final ListView lv_list_item = (ListView) helper.getView(R.id.lv_list_item);
                    helper.setText(R.id.tv_name, name);
                    int s = (int) (deep * mapInstance.activity.getResources().getDimension(R.dimen.app_size_smaller));
                    helper.getView(R.id.v_split).getLayoutParams().width = s;

                    Bitmap bm = MapHelper.geometry_icon(item.getGeometry(), 100, 100, Color.GREEN, 5);
                    if (bm != null) {
                        helper.setImageBitmap(R.id.v_icon, bm);
                    } else {
                        helper.setImageResource(R.id.v_icon, R.mipmap.app_map_layer_fsjg);
                    }


                    helper.getView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MapHelper.selectAddCenterFeature(mapInstance.map, item);
                        }
                    });
                    helper.getView(R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mapInstance.viewFeature(item);
                        }
                    });

                }
            };
            ll_list.setTag(adpter);
            adpter.adpter(ll_list);
        }
        final List<Feature> features = new ArrayList<Feature>();

        LoadAll(mapInstance, feature_h, features, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                QuickAdapter<Feature> adpter = (QuickAdapter<Feature>) ll_list.getTag();
                adpter.clear();
                adpter.addAll(features);
                return null;
            }
        });

//            MapHelper.Query(GetTable(mapInstance,"H_FSJG", FeatureHelper.LAYER_NAME_H_FSJG), StringUtil.WhereByIsEmpty(hid)+" HID ='" + hid + "' ", 0, features, new AiRunnable() {
//                @Override
//                public <T_> T_ ok(T_ t_, Object... objects) {
//                    QuickAdapter<Feature> adpter = (QuickAdapter<Feature>) ll_list.getTag();
//                    adpter.clear();
//                    adpter.addAll(features);
//                    return null;
//                }
//            });
    }

    public static void copylc(final MapInstance mapInstance, final Feature featureH_fsjg, final String lc, final AiRunnable callback) {
        final Map<String, Object> map = new LinkedHashMap<>();
        final int lc_f = AiUtil.GetValue(lc, 1);
        final AiDialog aidialog = AiDialog.get(mapInstance.activity);
        map.put("szc", (lc_f + 1) + "");
        aidialog.setHeaderView(R.mipmap.app_icon_warning_red, "不可逆操作提醒")
                .setContentView("该操作主要是将选中的附属结构，复制到新的楼层，请谨慎处理！")
                .setContentView("请输入需要复制到的层数，多层使用“,”、“-”隔开：如：3,5,7 或是 2-7 层")
                .addContentView("如：3,5,7 将复制到3、5、7层")
                .addContentView("如：3-7 将复制到3、4、5、6、7层")
                .addContentView(aidialog.getEditView("请输入要复制到的楼层", map, "szc"))
                .setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String szc = map.get("szc") + "";
                        final List<Integer> nubs = StringUtil.GetNumbers(szc);
                        aidialog.setContentView("输入的楼层为“" + szc + "”：将复制到" + StringUtil.Join(nubs) + "层");
                        aidialog.setFooterView("取消", AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    {
                                        List<Feature> fs_save = new ArrayList<>();
                                        for (int szc : nubs) {
                                            // 复制幢附属结构
                                            Feature f_ = FeatureHelper.Copy(featureH_fsjg);
                                            f_.setGeometry(featureH_fsjg.getGeometry());
                                            FeatureHelper.Set(f_, "LC", szc);
                                            fs_save.add(f_);
                                        }
                                        mapInstance.fillFeature(fs_save);
                                        MapHelper.saveFeature(fs_save, callback);
                                    }
                                } catch (Exception es) {
                                    ToastMessage.Send("复制层失败！", es);
                                }
                                aidialog.dismiss();
                            }
                        });
                    }
                });
    }

    ///endregion

    //region 私有函数
    ///endregion

    //region 面积计算
    public void hsmj() {
        fv.hsmj(feature, mapInstance);
        fillView(v_content, feature, "MC");
        fillView(v_content, feature, "MJ");
        fillView(v_content, feature, "HSMJ");
    }

    public static void hsmj(Feature feature, MapInstance mapInstance) {
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
    ///endregion

    //region 内部类或接口
    ///endregion


}
