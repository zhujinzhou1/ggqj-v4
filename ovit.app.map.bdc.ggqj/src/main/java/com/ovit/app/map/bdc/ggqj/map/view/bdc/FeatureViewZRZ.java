package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.model.DxfFcfhtDefaultZ;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.FwPc;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.DialogBuilder;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.AppConfig;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.ListUtil;
import com.ovit.app.util.ReportUtils;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.view.FeatureEdit.GetTable;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewZRZ extends FeatureView {
    final static String TAG = "FeatureViewZRZ";

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
            String zrzh = FeatureHelper.Get(feature, "ZRZH", "");
            zrzh = zddm + StringUtil.substr(zrzh, zddm.length());
            FeatureHelper.Set(feature, "ZRZH", zrzh);
        }
        String bdcdyh = FeatureHelper.Get(feature_zd, FeatureHelper.TABLE_ATTR_BDCDYH, FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_BDCDYH, ""));
        fillFeature(feature, bdcdyh.endsWith("F99990001"));
    }

    @Override
    public String addActionBus(String groupname) {

        mapInstance.addAction(groupname, "画自然幢", R.mipmap.app_map_layer_zrz, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_zrz(mapInstance.getOrid_Parent(feature), null);
            }
        });
        mapInstance.addAction(groupname, "画逻辑幢", R.mipmap.app_map_layer_ljz, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_zrz(mapInstance.getOrid_Parent(feature), null);
            }
        });


        addActionTY(groupname);
//        addActionPZ(groupname);
        addActionSJ(groupname);

        groupname = "操作";
        if (feature != null && feature.getFeatureTable() == table) {
            mapInstance.addAction(groupname, "画正门", R.mipmap.app_map_layer_bz_wz, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    draw_zm("BZ_WZ", "正门", "正门");
                }
            });

            mapInstance.addAction(groupname, "定位", com.ovit.R.mipmap.app_icon_opt_location, new View.OnClickListener() {
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
        addActionDW("");
        addActionBZ("", false);
        return groupname;
    }

    private void draw_zm(String layername, String fhmc, String dwmc) {
        Feature mfeature =  MapHelper.getLayer(map, layername).getFeatureTable().createFeature();
        FeatureHelper.Set(mfeature,"FHMC", fhmc);
        FeatureHelper.Set(mfeature,"TITLE", fhmc);
        FeatureHelper.Set(mfeature,"DWMC", dwmc);
        fillFeature(mfeature);
        if (feature != null && FeatureHelper.Exist(mfeature, FeatureHelper.TABLE_ATTR_ORID_PATH)) {
            String path = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID_PATH, "") + "/" + FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID, "");
            FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ORID_PATH, path);
        }
        mapInstance.requireFeatureView(mfeature).command_change(null);
    }

    // 列表项，点击加载自然幢
    @Override
    public void listAdapterConvert(BaseAdapterHelper helper, final Feature item, final int deep) {
        super.listAdapterConvert(helper, item, deep);
        final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
        ll_list_item.setVisibility(View.GONE);
        helper.getView(R.id.ll_head).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapHelper.centerPoint(map, item.getGeometry());
                MapHelper.selectFeature(map, item);
                boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                if (!flag) {
                    final List<Feature> fs = new ArrayList<>();
                    queryChildFeature(FeatureHelper.TABLE_NAME_LJZ, item, fs, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            com.ovit.app.map.view.FeatureView fv_ljz = mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_LJZ);
                            fv_ljz.fs_ref.add(item);
                            fv_ljz.buildListView(ll_list_item, fs, deep + 1);
                            return null;
                        }
                    });
                }
                ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
            }
        });
    }

    //endregion 重写父类方法
    //region 界面方法

    public void fillFeature(Feature feature, boolean isF99990001) {

        String zrzh = FeatureHelper.Get(feature, "ZRZH", "");
        String zddm = StringUtil.substr(zrzh, 0, zrzh.length() - 5);
        String zh = StringUtil.substr_last(zrzh, 4);

        // 以总层数为主
        double zcs = Math.abs(FeatureHelper.Get(feature, "ZCS", 0));
        double dscs = Math.abs(FeatureHelper.Get(feature, "DSCS", 0));
        double dxcs = Math.abs(FeatureHelper.Get(feature, "DXCS", 0));
        zcs = zcs < (dscs + dxcs) ? (dscs + dxcs) : zcs;
        zcs = zcs < 1 ? 1 : zcs;
        dscs = zcs - dxcs;

        FeatureHelper.Set(feature, "ZCS", zcs);
        FeatureHelper.Set(feature, "DSCS", dscs);
        FeatureHelper.Set(feature, "DXCS", dxcs);

        FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ZDDM, zddm);
        FeatureHelper.Set(feature, "ZH", zh);
        FeatureHelper.Set(feature, "ZTS", FeatureHelper.Get(feature,"ZTS","1"));


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

    //  获取最大的编号
    public void getMaxZrzh(String zddm, AiRunnable callback) {
        String id = zddm + "F";
        MapHelper.QueryMax(table, StringUtil.WhereByIsEmpty(id) + "ZRZH like '" + id + "____'", "ZRZH", id.length(), 0, id + "0000", callback);
    }

    public void newZrzh(String zddm, final AiRunnable callback) {
        getMaxZrzh(zddm, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = "";
                if (objects.length > 1) {
                    String maxid = objects[0] + "";
                    // 最大号加1
                    int count = AiUtil.GetValue(objects[1], 0) + 1;
                    id = StringUtil.fill(String.format("%04d", count), maxid, true);
                }
                AiRunnable.Ok(callback, id);
                return null;
            }
        });
    }

    /**
     * 层,户智能生成
     *
     * @param fs_zrz
     */
    public void ipug(final List<Feature> fs_zrz, final AiRunnable callback) {
        new AiForEach<Feature>(fs_zrz, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                creatCFromZrz(fs_zrz, callback);
                return null;
            }
        }) {
            @Override
            public void exec() {
                final List<Feature> fs_ljz = new ArrayList<>();
                FeatureView fv_zrz = mapInstance.newFeatureView(fs_zrz.get(postion));
                fv_zrz.queryChildFeature(FeatureHelper.TABLE_NAME_LJZ, fs_ljz, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        initAllFeatureHFromLjz(fs_ljz, getNext());
                        return null;
                    }
                });

            }
        }.start();
    }

    /**
     * 层,户智能生成
     *
     * @param fs_zrz
     */
    public void ipug(final List<Feature> fs_zrz) {

        final String funcdesc = " 1、自然幢生成层。"
                + "\n 2、逻辑幢快速生成户。"
                + "\n 3、宗地，自然幢，逻辑幢，层，户，附属结构关系建立。";
        final AiDialog aidialog = AiDialog.get(mapInstance.activity);
        aidialog.setHeaderView(R.mipmap.app_icon_rgzl_blue, "快速生成")
                .setContentView("注意：属于不可逆操作，请谨慎处理！", funcdesc)
                .setFooterView(AiDialog.CENCEL, AiDialog.EXECUTE_NEXT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        // 完成后的回掉
                        final AiRunnable callback = new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "快速生成自然幢层完成。");
                                aidialog.addContentView("处理数据完成！");
                                aidialog.setFooterView(null, AiDialog.COMPLET, null);
                                return null;
                            }

                            @Override
                            public <T_> T_ no(T_ t_, Object... objects) {
                                aidialog.addContentView(AiRunnable.NO);
                                aidialog.setFooterView(null, AiDialog.COLSE, null);
                                return null;
                            }

                            @Override
                            public <T_> T_ error(T_ t_, Object... objects) {
                                aidialog.addContentView(AiRunnable.ERROR);
                                aidialog.setFooterView(null, AiDialog.COLSE, null);
                                return null;
                            }
                        };

                        // 设置不可中断
                        aidialog.setCancelable(false);
                        aidialog.setContentView("开始处理数据");
                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "查找所有的自然幢幢，并识别自然幢。");

                        new AiForEach<Feature>(fs_zrz, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "快速生成户完成。");
                                creatCFromZrz(fs_zrz, callback);
                                return null;
                            }
                        }) {
                            @Override
                            public void exec() {
                                final List<Feature> fs_ljz = new ArrayList<>();
                                FeatureView fv_zrz = mapInstance.newFeatureView(fs_zrz.get(postion));
                                fv_zrz.queryChildFeature(FeatureHelper.TABLE_NAME_LJZ, fs_ljz, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        initAllFeatureHFromLjz(fs_ljz, getNext());
                                        return null;
                                    }
                                });

                            }
                        }.start();
                    }
                }).show();

    }


    /**
     * 通过逻辑幢初始化户
     *
     * @param featuresLjz
     * @param callback
     */
    private void initAllFeatureHFromLjz(final List<Feature> featuresLjz, final AiRunnable callback) {
        new AiForEach<Feature>(featuresLjz, callback) {
            @Override
            public void exec() {
                Log.d(TAG, "逻辑幢快速生成户===" + this.postion);
                final Feature featureLjz = this.getValue();
                final AiForEach<Feature> that = this;
                // 逻辑幢识别幢附属结构
                final FeatureViewLJZ fv_ljz = (FeatureViewLJZ) mapInstance.newFeatureView(featureLjz);
                fv_ljz.identyZ_Fsjg(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        fv_ljz.identyH(false, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                final List<Feature> fs_h = (List<Feature>) t_;
                                FeatureViewH.InitFeatureAll(mapInstance, featureLjz, fs_h, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        // 户识别户附属结构
                                        final List<Feature> featuresH = (List<Feature>) t_;
                                        featuresH.addAll(fs_h);
                                        new AiForEach<Feature>(featuresH, that.getNext()) {
                                            @Override
                                            public void exec() {
                                                final Feature featureH = this.getValue();
                                                FeatureViewH featureViewH = (FeatureViewH) mapInstance.newFeatureView(featureH);
                                                final AiForEach<Feature> that_h = this;
                                                Log.i(TAG, "户识别户附属结构===" + featureH.getAttributes().get("ID") + "====" + this.postion);
                                                {
                                                    featureViewH.identyH_FSJG(featureH, false, new AiRunnable() {
                                                        //                                            FeatureEditH_FSJG.IdentyH_FSJG_(mapInstance,featureH, new AiRunnable() {
                                                        @Override
                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                            FeatureEditH.IdentyH_Area(mapInstance, featureH, new AiRunnable() {
                                                                @Override
                                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                                    AiRunnable.Ok(that_h.getNext(), t_, t_);
                                                                    return null;
                                                                }
                                                            });
                                                            return null;
                                                        }
                                                    });
                                                }
                                            }
                                        }.start();
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
        }.start();
    }


    private void creatCFromZrz(final List<Feature> featuresZRZ, final AiRunnable callback) {
        MapHelper.saveFeature(featuresZRZ, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                new AiForEach<Feature>(featuresZRZ, callback) {
                    @Override
                    public void exec() {
                        FeatureEditC.InitFeatureAll(mapInstance, getValue(), getNext());
                    }
                }.start();
                return null;
            }
        });
    }


    public void loadByZrzh(String zrzh, AiRunnable callback) {
        MapHelper.QueryOne(table, StringUtil.WhereByIsEmpty(zrzh) + " ZRZH like '%'" + zrzh + "%' ", callback);
    }

    //    public void update_Area(Feature feature, List<Feature> f_hs, List<Feature> f_z_fsjgs) {
//        String id = FeatureHelper.Get(feature, "ZRZH", "");
//        int zcs = FeatureHelper.Get(feature, "ZCS", 1);
//        Geometry g = feature.getGeometry();
//        double area = 0;
//        double hsmj = 0;
//        if (g != null) {
//            area = MapHelper.getArea(mapInstance, g);
//
//            for (Feature f : f_hs) {
//                String zrzh = FeatureHelper.Get(f, "ZRZH", "");
//                if (id.equals(zrzh)) {
//                    double f_hsmj = FeatureHelper.Get(f, "SCJZMJ", 0d);
//                    hsmj += f_hsmj;
//                }
//            }
//            for (Feature f : f_z_fsjgs) {
//                String zid = FeatureHelper.Get(f, "ZID", "");
//                if (id.equals(zid)) {
//                    double f_hsmj = FeatureHelper.Get(f, "HSMJ", 0d);
//                    hsmj += f_hsmj;
//                }
//            }
//            if (hsmj <= 0) {
//                hsmj = area * zcs;
//            }
//            FeatureHelper.Set(feature, "ZZDMJ", AiUtil.Scale(area, 2));
//            FeatureHelper.Set(feature, "SCJZMJ", AiUtil.Scale(hsmj, 2));
//        }
//    }
    // 2019 06 03 已orid 为关联
    public void update_Area(Feature feature, List<Feature> f_hs, List<Feature> f_h_fsjgs, List<Feature> f_z_fsjgs) {
        String id = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID, "");
        int zcs = FeatureHelper.Get(feature, "ZCS", 1);
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        if (g != null) {
            area = MapHelper.getArea(mapInstance, g);

            for (Feature f : f_hs) {
                String orid_path = FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                if (orid_path.contains(id)) {
                    double f_hsmj = FeatureHelper.Get(f, "YCJZMJ", 0d);
                    hsmj += f_hsmj;
                }
            }
            for (Feature f : f_z_fsjgs) {
                String orid_path = FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                if (orid_path.contains(id)) {
                    double f_hsmj = FeatureHelper.Get(f, "HSMJ", 0d);
                    hsmj += f_hsmj;
                }
            }
            for (Feature f : f_h_fsjgs) {
                String orid_path = FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                if (orid_path.contains(id)) {
                    double f_hsmj = FeatureHelper.Get(f, "HSMJ", 0d);
                    hsmj += f_hsmj;
                }
            }

            if (hsmj <= 0) {
                hsmj = area * zcs;
            }
            FeatureHelper.Set(feature, "ZZDMJ", AiUtil.Scale(area, 2));
            FeatureHelper.Set(feature, "SCJZMJ", AiUtil.Scale(hsmj, 2));
        }
    }


    public static FeatureViewZRZ From(MapInstance mapInstance, Feature f) {
        FeatureViewZRZ fv = From(mapInstance);
        fv.set(f);
        return fv;
    }

    public static FeatureViewZRZ From(MapInstance mapInstance) {
        FeatureViewZRZ fv = new FeatureViewZRZ();
        fv.set(mapInstance).set(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ));
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

    //    带有诸多属性画幢
    public static void CreateFeature(final MapInstance mapInstance, final Feature f_p, Feature f, final AiRunnable callback) {

        if (f_p != null && f_p.getFeatureTable() != mapInstance.getTable(FeatureConstants.ZD_TABLE_NAME)) {
            // 如果不是宗地
            String orid = mapInstance.getOrid_Match(f, FeatureConstants.ZD_TABLE_NAME);
            if (StringUtil.IsNotEmpty(orid)) {
                CreateFeature(mapInstance, orid, f, callback);
                return;
            }
        }
        final FeatureViewZRZ fv = From(mapInstance, f);
        final Feature feature;
        if (f == null) {
            feature = fv.table.createFeature();
        } else {
            feature = f;
        }
        if (f_p == null) {
            ToastMessage.Send("注意：缺少宗地信息");
        }
        if (feature.getGeometry() == null && f_p != null) {
            feature.setGeometry(MapHelper.geometry_copy(f_p.getGeometry()));
        }
        final List<Feature> fs_update = new ArrayList<>();
        final String zddm = FeatureHelper.Get(f_p, FeatureHelper.TABLE_ATTR_ZDDM, "");
        // 绘图
        fv.drawAndAutoCompelet(feature, fs_update, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 设置新的zrzh
                fv.newZrzh(zddm, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String id = t_ + "";
                        FeatureHelper.Set(feature, "ZRZH", id);
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

    //查询、排序、分组数据，生成分层分户图、汇总图
    public void createFCFHT(final ArrayList<Map.Entry<String, List<Feature>>> cs, final List<String> fct, final AiRunnable callback) {
        FeatureEditC.Load_FsAndH_GroupbyC_Sort(mapInstance, feature, cs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                boolean pcClose = AppConfig.PHSZ_PC_CLOSE.equals(AppConfig.get(AppConfig.APP_BDCQJDC_PHSZ_PC, AppConfig.PHSZ_PC_CLOSE));
                final FwPc pc = pcClose ? null : new FwPc();
                AiRunnable run = new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        FeatureEditC.createFCFHT(mapInstance, feature, cs, pc);
                        FeatureEditC.createFCT(mapInstance, feature, cs, fct, pc);
                        AiRunnable.Ok(callback, t_);
                        return null;
                    }
                };
                if (pc != null) {
                    // 平差
                    List<Layer> layers = MapHelper.getLayers(mapInstance.map, FeatureHelper.TABLE_NAME_ZRZ);
                    pc.set(mapInstance, layers, feature.getGeometry(), true, run);
                } else {
                    AiRunnable.Ok(run, t_);
                }
                return null;
            }
        });
    }

    public static void LoadAllZRZ(final MapInstance mapInstance, final List<Feature> fs_zrz, AiRunnable callback) {
        MapHelper.Query(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ), "", "ZRZH", "", -1, fs_zrz, callback);
    }

    // 加载所有的幢，创建分层分户图
    public static void LaodAllZRZ_CreateFCFHT(final MapInstance mapInstance, final AiRunnable callback) {
        final List<Feature> fs = new ArrayList<>();
        LoadAllZRZ(mapInstance, fs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                LaodAllZRZ_CreateFCFHT(mapInstance, fs, callback);
                return null;
            }
        });
    }

    // 加载所有的幢，创建分层分户图
    public static void LaodAllZRZ_CreateFCFHT(final MapInstance mapInstance, final List<Feature> fs, final AiRunnable callback) {
        final FeatureViewZRZ fv = From(mapInstance);
        // 递归执行
        new AiForEach<Feature>(fs, callback) {
            @Override
            public void exec() {
                fv.set(fs.get(postion));
                final ArrayList<Map.Entry<String, List<Feature>>> cs = new ArrayList<Map.Entry<String, List<Feature>>>();
                List<String> fct = new ArrayList<>();
                fv.createFCFHT(cs, fct, getNext());
            }
        }.start();
    }

    // 核算宗地 占地面积、建筑面积
    public void update_Area(final AiRunnable callback) {
        final List<Feature> fs_z_fsjg = new ArrayList<>();
        final List<Feature> fs_h = new ArrayList<>();
        final List<Feature> fs_h_fsjg = new ArrayList<>();
        final List<Feature> update_fs = new ArrayList<>();
        String id = getZddm();
        if (StringUtil.IsEmpty(id)) {
            AiRunnable.Ok(callback, null);
            return;
        }
        LoadH_And_Fsjg(mapInstance, feature, fs_z_fsjg, fs_h, fs_h_fsjg, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                for (Feature f : fs_h_fsjg) {
                    FeatureEditH_FSJG.hsmj(f, mapInstance);
                    update_fs.add(f);
                }
                for (Feature f : fs_z_fsjg) {
                    FeatureEditZ_FSJG.hsmj(f, mapInstance);
                    update_fs.add(f);
                }
                for (Feature f : fs_h) {
                    FeatureEditH.hsmj(f, mapInstance, fs_h_fsjg);
                    update_fs.add(f);
                }

//                hsmj(fs_zrz);
                update_fs.add(feature);
                MapHelper.saveFeature(update_fs, callback);
                return null;
            }
        });
    }

    public void c_init(final AiRunnable callback) {
        FeatureViewC.InitFeatureAll(mapInstance, feature, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiRunnable.Ok(callback, t_, objects);
                return null;
            }
        });
    }

    public void queryLjzs(List<Feature> features_zrz, final AiRunnable callback) {
        MapHelper.Query(mapInstance.getTable(FeatureHelper.TABLE_NAME_LJZ), GeometryEngine.buffer(feature.getGeometry(), -0.5), features_zrz, callback);
    }

    /**
     * 自然幢根据范围识别逻辑幢,默认弹出提示框。
     *
     * @param callback 识别完成回调。
     */
    public void identyLjz(final AiRunnable callback) {
        identyLjz(true, callback);
    }

    /**
     * 自然幢根据范围识别逻辑幢。
     *
     * @param isShow   参数传入true 显示提示框，传入false 不显示。
     * @param callback 识别完成回调。
     */
    public void identyLjz(final boolean isShow, final AiRunnable callback) {
        final List<Feature> fs_ljz = new ArrayList<>();
        queryLjzs(fs_ljz, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final FeatureViewLJZ fv_ljz = FeatureViewLJZ.From(mapInstance);
                if (isShow) {
                    fv_ljz.fs_ref = ListUtil.asList(feature);
                    QuickAdapter<Feature> adapter = fv_ljz.getListAdapter(fs_ljz, 0);
                    AiDialog dialog = AiDialog.get(mapInstance.activity, adapter);
                    dialog.setHeaderView(R.mipmap.app_map_layer_zrz, "识别到" + fs_ljz.size() + "个逻辑幢");
                    dialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fv_ljz.fillFeature(fs_ljz, feature);  // orid
                            identyLjzFromZrz(mapInstance, feature, fs_ljz, callback);
                            dialog.dismiss();
                        }
                    });
                } else {
                    fv_ljz.fillFeature(fs_ljz, feature);  // orid
                    identyLjzFromZrz(mapInstance, feature, fs_ljz, callback);
                }
                return null;
            }
        });
    }

    // 自然幢识别逻辑幢
    public void indentyLjzFromZrzs(final List<Feature> featuresZRZ, AiRunnable callback) {
        new AiForEach<Feature>(featuresZRZ, callback) {
            public void exec() {
                final Feature featureZrz = getValue();
                FeatureViewZRZ fvZrz = (FeatureViewZRZ) mapInstance.newFeatureView(featureZrz);
                fvZrz.identyLjz(false, getNext());
            }
        }.start();
    }

    public void identyLjzFromZrz(com.ovit.app.map.model.MapInstance mapInstance, Feature f_zrz, final List<Feature> fs_ljz, final AiRunnable callback) {
        double area_jzmj = 0;
        double area_jzzdmj = 0;
        String zddm = FeatureHelper.Get(f_zrz, FeatureHelper.TABLE_ATTR_ZDDM, "");
        int zh = AiUtil.GetValue(FeatureHelper.Get(f_zrz, "ZH", ""), 0);
        String ljzhPrefix = StringUtil.substr_last(zddm, FeatureHelper.FEATURE_ZD_ZDDM_F_LENG) + "F" + FeatureHelper.Get(f_zrz, "ZH", "");
        String zrzh = FeatureHelper.Get(f_zrz, "ZRZH", "");
        List<Feature> features_update = new ArrayList<>();
        int maxLJZH = 0;
        double zrz_area = MapHelper.getArea(mapInstance, f_zrz.getGeometry());
        for (Feature f : fs_ljz) {
            final String ljz_ljzh = FeatureHelper.Get(f, "LJZH", "");
            if (ljz_ljzh.startsWith(ljzhPrefix) && ljz_ljzh.length() == FeatureHelper.FEATURE_LJZ_LJZH_LENG) {
                // 逻辑幢号
                int ljzh = AiUtil.GetValue(StringUtil.substr_last(ljz_ljzh, 2), 0);
                if (maxLJZH < ljzh) {
                    maxLJZH = ljzh;
                }
            } else {
                // 逻辑幢号幢号无效需要自己编
                features_update.add(f);
            }
        }
        // 专门来更新逻辑幢号
        if (features_update.size() > 0) {
            for (Feature updateFeature : features_update) {
                maxLJZH++;
                String newZH = String.format("%02d", maxLJZH);
                FeatureHelper.Set(updateFeature, "LJZH", ljzhPrefix + newZH);
            }
        }
        // 来更新逻辑幢其他字段内容
        for (Feature f : fs_ljz) {
            int ljzh = AiUtil.GetValue(StringUtil.substr_last(FeatureHelper.Get(f, "LJZH", ""), 2), 0);
            FeatureHelper.Set(f, FeatureHelper.TABLE_ATTR_ZDDM, zddm);
            FeatureHelper.Set(f, "ZRZH", zrzh);
            FeatureHelper.Set(f, "MPH", zh + "-" + ljzh);
            double z_zcs = AiUtil.GetValue(f.getAttributes().get("ZCS"), 1d);
            double z_area = MapHelper.getArea(mapInstance, f.getGeometry());
            double z_scjzmj = FeatureHelper.Get(f, "SCJZMJ", 0d);
            // 如果建筑面积小于占地面积
            if (z_scjzmj < z_area * 0.5 || (z_zcs > 1 && z_scjzmj <= (z_area + 0.1))) {
                z_scjzmj = z_area * z_zcs;
            }
            area_jzmj += z_scjzmj;
            area_jzzdmj += z_area;

            FeatureHelper.Set(f, "SCJZMJ", AiUtil.Scale(z_scjzmj, 2));
            FeatureHelper.Set(f, "ZCS", z_zcs);
        }
        FeatureHelper.Set(f_zrz, "SCJZMJ", AiUtil.Scale(area_jzmj, 2));
        FeatureHelper.Set(f_zrz, "ZZDMJ", AiUtil.Scale(zrz_area, 2));
        features_update.clear();
        if (StringUtil.IsNotEmpty(zddm)) {
            features_update.addAll(fs_ljz);
        }
        features_update.add(f_zrz);
        MapHelper.saveFeature(features_update, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiRunnable.Ok(callback, fs_ljz);
                return null;
            }
        });
    }

    public void identyZd(boolean isShow, final AiRunnable callback) {
        FeatureViewZD fv_zd = FeatureViewZD.From(mapInstance);
        final List<Feature> fs_zd = new ArrayList<>();
        MapHelper.Query(fv_zd.table, feature.getGeometry(), fs_zd, callback);
        if (isShow) {
            fv_zd.fs_ref = ListUtil.asList(feature);
            QuickAdapter<Feature> adapter = fv_zd.getListAdapter(fs_zd, 0);
            AiDialog dialog = AiDialog.get(mapInstance.activity, adapter);
            dialog.setHeaderView(R.mipmap.app_map_layer_zrz, "识别到" + fs_zd.size() + "个宗地");
            if (fs_zd.size() == 1) {
                dialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fv.fillFeature(feature, fs_zd.get(0));
                        MapHelper.saveFeature(feature, callback);
                        dialog.dismiss();
                    }
                });
            } else if (fs_zd.size() > 1) {
                dialog.setFooterView("识别到" + fs_zd.size() + "宗地，请选择一宗地");
            }
        } else {
            Feature f_zd = null;
            if (fs_zd.size() > 0) {
                f_zd = fs_zd.get(0);
            }
            fv.fillFeature(feature, f_zd);
            MapHelper.saveFeature(feature, callback);
        }
    }

    // 加载所有的幢，识别宗地
    public static void LaodAllZRZ_IdentyZd(final MapInstance mapInstance, final List<Feature> fs_zrz, final AiRunnable callback) {

        LoadAllZRZ(mapInstance, fs_zrz, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final FeatureViewZRZ fv = From(mapInstance);
                // 递归执行
                new AiForEach<Feature>(fs_zrz, callback) {
                    @Override
                    public void exec() {
                        fv.set(fs_zrz.get(postion));
                        fv.identyZd(false, getNext());
                    }
                }.start();
                return null;
            }
        });
    }

    public static double hsmj_jzmj(List<Feature> fs_hAndFs) {
        double scjzmj_count = 0;
        if (fs_hAndFs != null && fs_hAndFs.size() > 0) {
            for (Feature f : fs_hAndFs) {
                // 户
                scjzmj_count += FeatureHelper.Get(f, "SCJZMJ", FeatureHelper.Get(f, "YCJZMJ", 0d));
                // 幢的附属
                scjzmj_count += FeatureHelper.Get(f, "HSMJ", 0d);
            }
        }
        return scjzmj_count;
    }

    public static ArrayList<Map.Entry<String, List<Feature>>> GroupbyC_Sort(List<Feature> features) {
        LinkedHashMap<String, List<Feature>> map = GroupbyC(features);
        ArrayList<Map.Entry<String, List<Feature>>> keys = new ArrayList<Map.Entry<String, List<Feature>>>(map.entrySet());
        Collections.sort(keys, new Comparator<Map.Entry<String, List<Feature>>>() {
            public int compare(Map.Entry<String, List<Feature>> o1, Map.Entry<String, List<Feature>> o2) {
                return new Integer(AiUtil.GetValue(o1.getKey(), 0)).compareTo(new Integer(AiUtil.GetValue(o2.getKey(), 0)));
            }
        });
        return keys;
    }

    public static String GetID(Feature feature) {
        return AiUtil.GetValue(feature.getAttributes().get("ZRZH"), "");
    }
    //endregion 界面方法
    //region 楼盘表

    public static View GetView_LPB(MapInstance mapInstance, final Feature feature, final AiRunnable callback) {
        LinearLayout ll_view = (LinearLayout) LayoutInflater.from(mapInstance.activity).inflate(
                R.layout.app_ui_ai_aimap_lpb, null);
        LinearLayout ll_list = (LinearLayout) ll_view.findViewById(R.id.ll_list);
//        BuildView_LPB_ZDDM(mapInstance, ll_list, zddm, 0, callback);
        BuildView_LPB(mapInstance, ll_list, feature, 0, callback);
        return ll_view;
    }

//    public static void BuildView_LPB_ZDDM(MapInstance instance, final LinearLayout ll_list, final String zddm, final int deep,final AiRunnable callback) {
//        BuildView_LPB(instance, ll_list, " ZDDM ='" + zddm + "' ", deep,callback);
//    }

    public static void BuildView_LPB(final MapInstance mapInstance, final LinearLayout ll_list, final Feature feature, final int deep, final AiRunnable callback) {
        String zddm = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ZDDM, "");
        String where = " ZDDM ='" + zddm + "' ";
        if (ll_list.getTag() == null) {
            final Geometry g_zd = feature.getGeometry();
            QuickAdapter<Feature> adpter = new QuickAdapter<Feature>(mapInstance.activity, R.layout.app_ui_ai_aimap_lpb_item, new ArrayList<Feature>()) {
                @Override
                protected void convert(final BaseAdapterHelper helper, final Feature item) {
                    final String name = AiUtil.GetValue(item.getAttributes().get("JZWMC"), "");
                    final String id = AiUtil.GetValue(item.getAttributes().get("ZRZH"), "");
                    final String desc = id.length() > 12 ? id.substring(12) : id;
                    final String zcs = AiUtil.GetValue(item.getAttributes().get("ZCS"), "");
                    final String zts = AiUtil.GetValue(item.getAttributes().get("ZTS"), "");
                    final LinearLayout ll_list_item = helper.getView(R.id.ll_list_item);
                    helper.setText(R.id.tv_name, name);
                    helper.setText(R.id.tv_desc, desc);

                    int s = (int) (deep * mapInstance.activity.getResources().getDimension(R.dimen.app_size_smaller));
                    helper.getView(R.id.v_split).getLayoutParams().width = s;
                    Bitmap bm = MapHelper.geometry_icon(new Feature[]{feature, item}, 100, 100, new int[]{R.color.app_theme_fore, Color.BLUE}, new int[]{1, 5});

                    if (bm != null) {
                        helper.setImageBitmap(R.id.v_icon, bm);
                    } else {
                        helper.setImageResource(R.id.v_icon, R.mipmap.app_icon_building);
                    }
                    helper.getView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                            if (!flag) {
                                double zts_ = AiUtil.GetValue(zts, 0.0);
                                double zcs_ = AiUtil.GetValue(zcs, 0.0);
//                                if (NumberUtils.isNumber(zts)&&NumberUtils.isNumber(zcs)&&Double.parseDouble(zts)>0&&Double.parseDouble(zcs)>0){
                                if (zts_ > 0 && zcs_ > 0) {
                                    // 楼盘表 设置了总层数
                                    LoadLPB(mapInstance, item, ll_list_item, deep + 1, zcs, zts);
                                } else {
                                    FeatureEditH.BuildView_H(mapInstance, ll_list_item, item, id, deep + 1);
                                }
                            }
                            ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
                        }
                    });

                    helper.getView(R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mapInstance.viewFeature(item);
                        }
                    });
                    helper.getView(R.id.iv_position).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MapHelper.selectAddCenterFeature(mapInstance.map, item);
                        }
                    });

                    final List<Feature> features = new ArrayList<Feature>();
                    MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_H, FeatureHelper.LAYER_NAME_H), StringUtil.WhereByIsEmpty(id) + " ZRZH ='" + id + "' ", 0, features, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            helper.setText(R.id.tv_count, features.size() + "");
//                            return super.ok(t_, objects);
                            AiRunnable.Ok(callback, t_, objects);
                            return null;
                        }
                    });
                }
            };
//          lv_list.setAdapter(adpter);
            ll_list.setTag(adpter);
            adpter.adpter(ll_list);
        }
        final List<Feature> features = new ArrayList<Feature>();
        FeatureViewZD.From(mapInstance, feature).loadZrzs(features, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                QuickAdapter<Feature> adpter = (QuickAdapter<Feature>) ll_list.getTag();
                adpter.clear();
                adpter.addAll(features);
                return null;
            }
        });
    }

    public static void LoadLPB(MapInstance mapInstance, Feature f_zrz, LinearLayout ll_list, int i, String zcs, String zts) {

        int mchs = (int) Math.ceil(Double.parseDouble(zts) / Double.parseDouble(zcs));
        View view_h = GetView_C(mapInstance, f_zrz, zcs, mchs);
        ll_list.removeAllViews();
        ll_list.addView(view_h);
        view_h.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public static View GetView_C(MapInstance mapInstance, Feature f_zrz, String zcs, int mchs) {
        LinearLayout ll_view = (LinearLayout) LayoutInflater.from(mapInstance.activity).inflate(
                R.layout.app_ui_ai_aimap_c_lpb, null);
        ll_view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout ll_list = (LinearLayout) ll_view.findViewById(R.id.ll_list);
        BuildView_C(mapInstance, ll_list, f_zrz, zcs, mchs, 0);
        return ll_view;
    }

    public static void BuildView_C(final MapInstance mapInstance, final LinearLayout ll_list, final Feature f_zrz, final String zcs, final int mchs, final int deep) {
        final LinkedHashMap<String, List<Feature>> map_all = new LinkedHashMap<String, List<Feature>>();
        String orid = mapInstance.getOrid(f_zrz);
        if (StringUtil.IsNotEmpty(orid) != null && StringUtil.IsNotEmpty(zcs)) {
            if (ll_list.getTag() == null) {
                QuickAdapter<Integer> adpter = new QuickAdapter<Integer>(mapInstance.activity, R.layout.app_ui_ai_aimap_c_item_lpb, new ArrayList<Integer>()) {
                    int selectIndex = 0;

                    @Override
                    protected void convert(final BaseAdapterHelper helper, final Integer item) {
                        final String id = item + "";
                        List<Feature> fs_ = map_all.get(id);
                        if (fs_ == null) {
                            fs_ = new ArrayList<Feature>();
                            map_all.put(id, fs_);
                        }
                        final List<Feature> fs = fs_;
                        final List<Feature> fs_f = new ArrayList<>();
                        for (int i = 0; i < mchs; i++) {
                            fs_f.add(i, null);
                        }
                        TextView tv_lc = helper.getView(R.id.tv_lc);
                        if (item == Double.parseDouble(zcs)) {
                            tv_lc.setBackgroundResource(R.drawable.app_lpb_up9);
                        } else {
                            tv_lc.setBackgroundResource(R.drawable.app_lpb_left_normal9);
                        }
                        tv_lc.setText("F" + item);

                        final GridView gv_list_item = helper.getView(R.id.gv_list_item);
                        // 对 户号进行排序　
                        for (Feature f : fs) {
                            String hh = (String) f.getAttributes().get("HH");
                            if (hh != null) {
                                int h_bh = Integer.parseInt(hh.substring(hh.length() - 2));
                                for (int i = 1; i < mchs + 1; i++) {
                                    if (h_bh == i) {
                                        fs_f.set(i - 1, f);
                                    }
                                }
                            }
                        }
                        BuildView_H_LPB(mapInstance, f_zrz, gv_list_item, fs_f, deep + 1, item);
                        gv_list_item.measure(gv_list_item.getLayoutParams().width, gv_list_item.getLayoutParams().height);
                        int columnWidth = gv_list_item.getColumnWidth();
                        gv_list_item.setNumColumns(mchs);
                        gv_list_item.getLayoutParams().width = columnWidth * mchs;
                    }
                };
                ll_list.setTag(adpter);
                adpter.adpter(ll_list);
            }
            Load_FsAndH_GroupbyC(mapInstance, f_zrz, map_all, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    int zcs_ = AiUtil.GetValue(zcs, 1);
                    final List<Integer> cs = new ArrayList<Integer>();
                    for (int i = 1; i <= zcs_; i++) {
                        cs.add(i);
                    }
                    for (String key : map_all.keySet()) {
                        int c = AiUtil.GetValue(key, 0);
                        if (!cs.contains(c)) {
                            cs.add(c);
                        }
                    }
                    Collections.sort(cs);
                    Collections.reverse(cs);
                    QuickAdapter<Integer> adpter = (QuickAdapter<Integer>) ll_list.getTag();
                    adpter.clear();
                    adpter.replaceAll(cs);

                    return null;
                }
            });
        }
    }

    public static void Load_FsAndH_GroupbyC(final MapInstance mapInstance, Feature f_zrz, final LinkedHashMap<String, List<Feature>> keys, final AiRunnable callback) {
        final List<Feature> fs = new ArrayList<Feature>();
        FeatureView.LoadH_And_Z_Fsjg(mapInstance, f_zrz, fs, fs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                keys.putAll(GroupbyC(fs));
                AiRunnable.Ok(callback, t_, objects);
                return null;
            }
        });
    }

    public static LinkedHashMap<String, List<Feature>> GroupbyC(List<Feature> features) {
        LinkedHashMap<String, List<Feature>> map = new LinkedHashMap();
        for (Feature f : features) {
            String ch = FeatureHelper.Get(f, "SZC", FeatureHelper.Get(f, "LC", FeatureHelper.Get(f, "CH", "1")));
            if (map.keySet().contains(ch)) {
                map.get(ch).add(f);
            } else {
                map.put(ch, new ArrayList<Feature>(Arrays.asList(new Feature[]{f})));
            }
        }
        return map;
    }

    public static void BuildView_H_LPB(final MapInstance mapInstance, final Feature f_zrz, final GridView gv_list, final List<Feature> fs, final int deep, final int szc) {
        QuickAdapter<Feature> adpter = new QuickAdapter<Feature>(mapInstance.activity, R.layout.app_ui_ai_aimap_h_item_g_lpb, fs) {
            @Override
            protected void convert(final BaseAdapterHelper helper, final Feature item) {
                int i = helper.getPosition() + 1;
                String lc = szc > 9 ? szc + "" : "0" + szc;
                String index = i > 9 ? i + "" : "0" + i;
                final String hh = lc + index;
                helper.setText(R.id.tv_name, hh);

                if (item != null) {
                    final String id = AiUtil.GetValue(item.getAttributes().get("ID"), "");
                    final String name = AiUtil.GetValue(item.getAttributes().get("HH"), AiUtil.GetValue(item.getAttributes().get("FHMC"), ""));
                    final String desc = AiUtil.GetValue(item.getAttributes().get("QLRXM"), AiUtil.GetValue(item.getAttributes().get("MC"), ""));
//                        final ListView lv_list_item = (ListView) helper.getView(R.id.lv_list_item);
                    helper.setText(R.id.tv_desc, desc);
//                    int color = item.getFeatureTable().getTableName().toUpperCase().equals("H") ? Color.GREEN : Color.GRAY;
//                    Bitmap bm = MapHelper.geometry_icon(item.getGeometry(), 100, 100, color, 5);
//                    if (bm != null) {
//                        helper.setImageBitmap(R.id.v_icon, bm);
//                    } else {
//                        helper.setImageResource(R.id.v_icon, R.mipmap.app_map_h);
//                    }

                } else {
                    //  item 为空
                    //  helper.setImageResource(R.id.v_icon, R.mipmap.app_map_h);
                }
                helper.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final List<Feature> features = new ArrayList<Feature>();
                        if (item != null) {
                            mapInstance.viewFeature(item);
                            return;
                        }
                        FeatureViewH.LoadAll(mapInstance, f_zrz, features, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (features.size() > 0) {
                                    mapInstance.viewFeature(item);
                                    return null;
                                } else {
                                    DialogBuilder.alert(mapInstance.activity, FeatureHelper.LAYER_NAME_H, "该户不存在是否需要创建？").setPositiveButton(AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FeatureLayer layer_h = MapHelper.getLayer(mapInstance.map, FeatureHelper.TABLE_NAME_H, FeatureHelper.LAYER_NAME_H);
                                            final Feature feature_h = layer_h.getFeatureTable().createFeature();//Feature f_d = layer_jzd.getFeatureTable().createFeature();
                                            FeatureViewH.CreateFeature(mapInstance, f_zrz, szc + "", feature_h, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    mapInstance.viewFeature(feature_h);
                                                    ToastMessage.Send("户创建成功");
                                                    return null;
                                                }
                                            });
                                        }
                                    }).setNegativeButton(AiDialog.CENCEL, null).show();
                                }
                                return null;
                            }
                        });

                    }
                });

                helper.getView().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        DialogBuilder.confirm(mapInstance.activity, "提示", "确定要删除改图形么？", null, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MapHelper.deleteFeature(item, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        fs.remove(item);
                                        notifyDataSetChanged();
                                        return null;
                                    }
                                });
                            }
                        }, AiDialog.CENCEL, null).show();
                        return true;
                    }
                });

            }
        };
        gv_list.setAdapter(adpter);
    }

    public void create_zrz_bdcfy(final Feature f_zrz, final AiRunnable callback) {
        if (TextUtils.isEmpty(FeatureHelper.Get(f_zrz, FeatureHelper.TABLE_ATTR_ZDDM, ""))) {
            ToastMessage.Send("缺少宗地信息，请检查！");
            return;
        }
        FeatureEditQLR.CreateFeature(mapInstance, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final Feature featureBdcdy = (Feature) t_;
                String oridPath = FeatureHelper.Get(f_zrz, FeatureHelper.TABLE_ATTR_ORID_PATH, "")
                        + FeatureHelper.SEPARATORS_BASE + FeatureHelper.Get(f_zrz, FeatureHelper.TABLE_ATTR_ORID, "");
                String bdcdyh = f_zrz.getAttributes().get(FeatureHelper.TABLE_ATTR_ZRZH) + FeatureHelper.FEATURE_0001;
                FeatureHelper.Set(f_zrz, FeatureHelper.TABLE_ATTR_BDCDYH, bdcdyh);
                FeatureHelper.Set(featureBdcdy, FeatureHelper.TABLE_ATTR_BDCDYH, bdcdyh);
                FeatureHelper.Set(featureBdcdy, FeatureHelper.TABLE_ATTR_ORID_PATH, oridPath);
                List<Feature> updateFeatures = new ArrayList<>();
                updateFeatures.add(f_zrz);
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

    //endregion 楼盘表
    //region 输出成果

    public void createDOCX(final MapInstance mapInstance, final Feature f_bdc, final boolean isRelaod, final AiRunnable callback) {
        final String bdcdyh = FeatureEditQLR.GetBdcdyh(f_bdc);
        String file_dcb_doc = FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh);
        if (FileUtils.exsit(file_dcb_doc) && !isRelaod) {
            AiRunnable.Ok(callback, file_dcb_doc);
        } else {
            FeatureEditBDC.LoadZD(mapInstance, bdcdyh, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            final Feature f_zd = (Feature) t_;
                            final List<Feature> fs_zrz = new ArrayList<Feature>();
                            final List<Feature> fs_ljz = new ArrayList<Feature>();
                            final List<Feature> fs_c = new ArrayList<Feature>();
                            final List<Feature> fs_z_fsjg = new ArrayList<Feature>();
                            final List<Feature> fs_h = new ArrayList<Feature>();
                            final List<Feature> fs_h_fsjg = new ArrayList<Feature>();
                            final List<Feature> fs_jzd = new ArrayList<Feature>();
                            final List<Feature> fs_jzx = new ArrayList<Feature>();
                            final List<Feature> fs_c_all = new ArrayList<Feature>();
                            final Map<String, Feature> map_jzx = new HashMap<>();
                            final List<Map<String, Object>> fs_jzqz = new ArrayList<>();

                            loadAll(mapInstance, bdcdyh, f_bdc, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_ljz, fs_c, fs_z_fsjg, fs_h, fs_h_fsjg, new AiRunnable(callback) {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {

                                    createDOCX(mapInstance, f_bdc, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_ljz, fs_c, fs_z_fsjg, fs_h, fs_h_fsjg, isRelaod, new AiRunnable(callback) {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            // 数据归集
//                                                outputData(mapInstance, f_bdc, f_zd, fs_jzd, fs_jzx, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg,fs_c_all);
                                            outputData(mapInstance, f_bdc, f_zd, fs_jzd, fs_jzx, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg);
                                            AiRunnable.Ok(callback, t_, objects);
                                            return null;
                                        }
                                    });

                                    return null;
                                }
                            });
                            return null;
                        }
                    }
            );
        }
    }


    public void loadAll(final MapInstance mapInstance, final String bdcdyh,
                        final Feature featureBdcdy,
                        final Feature f_zd,
                        final List<Feature> fs_jzd,
                        final List<Feature> fs_jzx,
                        final Map<String, Feature> map_jzx,
                        final List<Map<String, Object>> fs_jzqz,
                        final List<Feature> fs_zrz,
                        final List<Feature> fs_ljz,
                        final List<Feature> fs_zrz_c,
                        final List<Feature> fs_z_fsjg,
                        final List<Feature> fs_h,
                        final List<Feature> fs_h_fsjg,
                        final AiRunnable callback) {
        final String orid_bdc = FeatureHelper.GetLastOrid(featureBdcdy);
        FeatureEditBDC.LoadJZDXQZ(mapInstance, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZRZ), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID = '" + orid_bdc + "' ", "ZRZH", "asc", -1, fs_zrz, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_LJZ), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID_PATH like '%" + orid_bdc + "%' ", "LJZH", "asc", -1, fs_ljz, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_H), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID_PATH like '%" + orid_bdc + "%' ", "ID", "asc", -1, fs_h, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_Z_FSJG), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID_PATH like '%" + orid_bdc + "%' ", "ID", "asc", -1, fs_z_fsjg, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_H_FSJG), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID_PATH like '%" + orid_bdc + "%' ", "ID", "asc", -1, fs_h_fsjg, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        AiRunnable.Ok(callback, t_, objects);
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
                return null;
            }
        });
    }

    public void createDOCX(final MapInstance mapInstance, final Feature f_bdc, final Feature f_zd,
                           final List<Feature> fs_jzd,
                           final List<Feature> fs_jzx,
                           final Map<String, Feature> map_jzx,
                           final List<Map<String, Object>> fs_jzqz,
                           final List<Feature> fs_zrz,
                           final List<Feature> fs_ljz,
                           final List<Feature> fs_c,
                           final List<Feature> fs_z_fsjg,
                           final List<Feature> fs_h,
                           final List<Feature> fs_h_fsjg, boolean isRelaod, final AiRunnable callback) {

        final String bdcdyh = FeatureHelper.Get(f_bdc, FeatureHelper.TABLE_ATTR_BDCDYH, "");
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
                        // 界址签字
                        FeatureEditBDC.Put_data_jzqz(map_, fs_jzd, fs_jzqz);
                        // 界址点
                        FeatureEditBDC.Put_data_jzdx(mapInstance, map_, zddm, fs_jzd, fs_jzx, map_jzx);
                        // 设置界址线
                        FeatureEditBDC.Put_data_jzx(mapInstance, map_, fs_jzx);
                        // 自然幢
                        FeatureEditBDC.Put_data_zrz(mapInstance, map_, bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h, fs_c, new ArrayList<Feature>());
                        // 在全局放所有户
                        FeatureEditBDC.Put_data_hs(mapInstance, map_, fs_h);
                        // 在全局放一个户
                        FeatureEditBDC.Put_data_h(mapInstance, map_, fs_h);
                        // 在全局放一个幢
                        FeatureEditBDC.Put_data_zrz(mapInstance, map_, fs_zrz);
                        // 在全局放一所以的户
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
                           final Feature f_bdc,
                           final Feature f_zd,
                           final List<Feature> fs_jzd,
                           final List<Feature> fs_jzx,
                           final List<Feature> fs_zrz,
                           final List<Feature> fs_z_fsjg,
                           final List<Feature> fs_h,
                           final List<Feature> fs_h_fsjg) {
        try {
            MapHelper.selectAddCenterFeature(mapInstance.map, f_zd);
            String bdcdyh = FeatureHelper.Get(f_bdc, FeatureHelper.TABLE_ATTR_BDCDYH, "");
            final String file_dcb = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + "不动产权籍调查表" + bdcdyh + ".docx";
            FileUtils.copyFile(FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh), file_dcb);

            if (DxfHelper.TYPE == DxfHelper.TYPE_DEFULT) {
                //以自然幢生成房产分层分户图。
                String dxf_fcfht = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + bdcdyh + "房产分层分户图.dxf";// fs_zrz =0
                new DxfFcfhtDefaultZ(mapInstance).set(dxf_fcfht).set(f_bdc, f_zd, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg).write().save();
            }

        } catch (Exception es) {
            Log.e(TAG, "导出数据失败", es);
        }
    }

    //endregion 输出生果
}
