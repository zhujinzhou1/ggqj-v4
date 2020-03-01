package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
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
import com.ovit.app.util.ReportUtils;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditC.Load_FsAndH_GroupbyC_Sort;
import static com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditC.calcuZrzcMj;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewC extends FeatureView {

    //region 常量
    final static String TAG = "FeatureViewC";
    ///endregion

    //region 字段
    // 上级，幢或是自然幢
    public Feature feature_p;
    ///endregion

    //region 构造函数
    ///endregion

    //region 重写函数和回调
    @Override
    public void onCreate() {
        super.onCreate();
        iconColor = Color.rgb(169, 0, 230);
    }
    @Override
    public void fillFeature(Feature feature, Feature feature_zrz) {
        super.fillFeature(feature, feature_zrz);
        String ch = FeatureHelper.Get(feature, "CH", "");
        if (feature_zrz != null) {
            String zrzh = FeatureHelper.Get(feature_zrz, "ZRZH", "");
            ch = StringUtil.substr(zrzh, 0, 12) + StringUtil.substr_last(ch, 8);
            FeatureHelper.Set(feature, "ZRZH", zrzh);
            FeatureHelper.Set(feature, "CH", ch);
        }
    }

    @Override
    public String addActionBus(String groupname) {
        int count = mapInstance.getSelFeatureCount();
        // 推荐

        mapInstance.addAction(groupname, "加层", R.mipmap.app_map_layer_ljz, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String zrzh = FeatureHelper.Get(feature, "ZRZH", "");
                draw_ljz(mapInstance.getOrid_Parent(feature), "1", null);
            }
        });
        if (count > 0) {
            boolean iscantqzrz = true;
            final List<Feature> fs = new ArrayList<>(mapInstance.features_sel);
            for (Feature f : fs) {
                String orid_path = mapInstance.getOrid_Path(f);
                if (StringUtil.IsNotEmpty(orid_path)) {
                    iscantqzrz = false;
                    break;
                }
            }
            mapInstance.addAction(groupname, "画户", R.mipmap.app_map_layer_h, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    draw_h(feature, "1", null);
                }
            });
            mapInstance.addAction(groupname, "画飘楼", R.mipmap.app_map_layer_fsjg, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    draw_z_fsjg(feature, "飘楼", "2", null);
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

    // 列表项，点击加载层信息
    @Override
    public void listAdapterConvert(BaseAdapterHelper helper, final Feature item, final int deep) {
        super.listAdapterConvert(helper, item, deep);
        final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
        ll_list_item.setVisibility(View.GONE);
        final String szc = FeatureHelper.Get(item, "SJC", "1");
        helper.setText(com.ovit.R.id.tv_name, "[" + szc + "楼]");
        helper.getView(R.id.ll_head).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapHelper.centerPoint(mapInstance.map, item.getGeometry());
                MapHelper.selectFeature(map, item);
                boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                if (!flag) {
                    final List<Feature> fs = new ArrayList<>();
//                    String where="ORID_PATH like '%"+FeatureHelper.Get(item,"ORID_PATH","")+"%' and SZC= '"+FeatureHelper.Get(item,"SJC","")+"'";
                    String where = "and SZC= '" + szc + "'";
                    queryChildFeature("H", FeatureHelper.Get(item, "ORID_PATH", ""), where, "HH", "asc", fs, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
//                            queryChildFeature("H_FSJG", FeatureHelper.Get(item, "ORID_PATH", ""), "and LC= '"+FeatureHelper.Get(item,"SJC","")+"'", "HH", "asc", fs, new AiRunnable() {
//                                @Override
//                                public <T_> T_ ok(T_ t_, Object... objects) {
//
//                                    return null;
//                                }
//                            });
                            queryChildFeature("Z_FSJG", FeatureHelper.Get(item, "ORID_PATH", ""), "and LC= '" + szc + "'", "ID", "asc", fs, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    mapInstance.newFeatureView("H").buildListView(ll_list_item, fs, deep + 1);
                                    return null;
                                }
                            });
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
    public void getMaxCh(AiRunnable callback) {
        String id = getZrzh();
        MapHelper.QueryMax(table, StringUtil.WhereByIsEmpty(id) + "CH like '" + id + "____'", "CH", id.length(), 0, id + "0000", callback);
    }

    public void newCh(final AiRunnable callback) {
        getMaxCh(new AiRunnable(callback) {
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

    public void loadByCh(String ch, AiRunnable callback) {
        MapHelper.QueryOne(table, StringUtil.WhereByIsEmpty(ch) + " CH like '%'" + ch + "%' ", callback);
    }

    public static FeatureViewC From(MapInstance mapInstance, Feature f) {
        FeatureViewC fv = From(mapInstance);
        fv.set(f);
        return fv;
    }

    public static FeatureViewC From(MapInstance mapInstance) {
        FeatureViewC fv = new FeatureViewC();
        fv.set(mapInstance).set(mapInstance.getTable("ZRZ_C"));
        return fv;
    }

    public static void CreateFeature(final MapInstance mapInstance, final Feature f_p, final AiRunnable callback) {
        CreateFeature(mapInstance, f_p, null, callback);
    }

    public static void CreateFeature(final MapInstance mapInstance, String orid, final Feature feature, final AiRunnable callback) {
        // 去查宗地
        FeatureViewZRZ.From(mapInstance).load(orid, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Feature f_p = (Feature) t_;
                CreateFeature(mapInstance, f_p, feature, callback);
                return null;
            }
        });
    }

    public static void CreateFeature(final MapInstance mapInstance, final Feature f_p, Feature f, final AiRunnable callback) {

        if (f_p != null && f_p.getFeatureTable() != mapInstance.getTable("ZRZ")) {

            String orid = mapInstance.getOrid_Match(f, "ZRZ");
            if (StringUtil.IsNotEmpty(orid)) {
                CreateFeature(mapInstance, orid, f, callback);
                return;
            }
        }
        final FeatureViewC fv = From(mapInstance, f);
        final Feature feature;
        if (f == null) {
            feature = fv.table.createFeature();
        } else {
            feature = f;
        }
        if (f_p == null) {
            ToastMessage.Send("注意：缺少自然幢信息");
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
                fv.newCh(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String id = t_ + "";
                        FeatureHelper.Set(feature, "CH", id);
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

    public static void InitFeatureAll(final MapInstance mapInstance, final Feature feature, AiRunnable callback_) {
        final AiDialog dialog = AiDialog.get(mapInstance.activity).setHeaderView(R.mipmap.app_map_layer_zrz_c, "生成层")
                .addContentView("将根据自然幢的范围删除原有的层，重新层", "该操作不可逆转，如果已经生成过层请谨慎操作");
        dialog.setFooterView(dialog.getProgressView("正在处理，请稍后..."));//.setCancelable(false);

        final AiRunnable callback = new AiRunnable(callback_) {
            @Override
            public <T_> void finlly(T_ t_, Object... objects) {
                dialog.setCancelable(true).dismiss();
            }
        };
        {
            MapHelper.Query(mapInstance.getTable("ZRZ_C"), "ORID_PATH like '%" + FeatureHelper.Get(feature, "ORID") + "%'", new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    MapHelper.deleteFeature((List<Feature>) t_, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            final ArrayList<Map.Entry<String, List<Feature>>> zrz_c_s = new ArrayList<>();
                            Load_FsAndH_GroupbyC_Sort(mapInstance, feature, zrz_c_s, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    final List<Feature> featuresC = new ArrayList<>();
                                    for (Map.Entry<String, List<Feature>> zrz_c : zrz_c_s) {
                                        Feature feature_c = mapInstance.getTable("ZRZ_C").createFeature();
                                        mapInstance.fillFeature(feature_c, feature);
                                        FeatureHelper.Set(feature_c, "CH", zrz_c.getKey());
                                        FeatureHelper.Set(feature_c, "SJC", zrz_c.getKey());
                                        FeatureHelper.Set(feature_c, "ZRZH", FeatureHelper.Get(feature, "ZRZH"));
                                        FeatureHelper.Set(feature_c, "CJZMJ", calcuZrzcMj(zrz_c.getValue(), "CJZMJ"));
                                        FeatureHelper.Set(feature_c, "CFTJZMJ", calcuZrzcMj(zrz_c.getValue(), "CFTJZMJ"));
                                        List<Feature> featuresH = new ArrayList<>();
                                        for (Feature feature : zrz_c.getValue()) {
                                            if ("H".equals(feature.getFeatureTable().getTableName())) {
                                                featuresH.add(feature);
                                            }
                                        }
                                        if (featuresH.size() > 0) {
                                            com.esri.arcgisruntime.geometry.Geometry geometry = GeometryEngine.union(MapHelper.geometry_get(featuresH));
                                            feature_c.setGeometry(geometry);
                                        }
                                        featuresC.add(feature_c);
                                    }
                                    MapHelper.saveFeature(featuresC, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            AiRunnable.Ok(callback, featuresC);
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
    }

    public void createBdcdyFromC(final Feature f_c, final AiRunnable callback) {
        String orid_path = FeatureHelper.Get(f_c, "ORID_PATH", "");
        if (TextUtils.isEmpty(orid_path) || !orid_path.contains("[ZRZ]")) {
            ToastMessage.Send("缺少幢信息，请检查！");
            return;
        }
        String id = FeatureHelper.Get(f_c, "ZRZH", "");
        MapHelper.QueryMax(mapInstance.getTable(FeatureConstants.QLRXX_TABLE_NAME), StringUtil.WhereByIsEmpty(FeatureHelper.Get(f_c, "ZRZH", "")) + "BDCDYH like '" + id + "____'", "BDCDYH", id.length(), 0, id + "0000", new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = "";
                if (objects.length > 1) {
                    String maxid = objects[0] + "";
                    // 最大号加1
                    int count = AiUtil.GetValue(objects[1], 0) + 1;
                    id = StringUtil.fill(String.format("%04d", count), maxid, true);
                }
                final Feature feature_new_qlr = mapInstance.getTable("QLRXX").createFeature();
                mapInstance.featureView.fillFeature(feature_new_qlr, f_c);
                feature_new_qlr.getAttributes().put("BDCDYH", id);

                MapHelper.saveFeature(feature_new_qlr, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        AiRunnable.Ok(callback, feature_new_qlr, t_);
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
    public void updateArea(Feature feature, List<Feature> f_hs, List<Feature> f_z_fsjgs) {
        String id = FeatureHelper.Get(feature, "CH", "");
        int zcs = FeatureHelper.Get(feature, "ZCS", 1);
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        if (g != null) {
            area = MapHelper.getArea(mapInstance, g);

            for (Feature f : f_hs) {
                String zrzh = FeatureHelper.Get(f, "ZRZH", "");
                if (id.equals(zrzh)) {
                    double f_hsmj = FeatureHelper.Get(f, "SCJZMJ", 0d);
                    hsmj += f_hsmj;
                }
            }
            for (Feature f : f_z_fsjgs) {
                String zid = FeatureHelper.Get(f, "ZID", "");
                if (id.equals(zid)) {
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

    // 核算宗地 占地面积、建筑面积
    public void updateArea(final AiRunnable callback) {
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
                updateArea(feature, fs_h, fs_z_fsjg);
                return null;
            }
        });
    }
    ///endregion

    //region 内部类或接口
    ///endregion

    //region 生成资料
    public  void createDOCX(final MapInstance mapInstance, final Feature featureBdcdy, final boolean isRelaod, final AiRunnable callback) {
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
                            final List<Feature> fs_z_fsjg = new ArrayList<Feature>();
                            final List<Feature> fs_jzd = new ArrayList<Feature>();
                            final List<Feature> fs_jzx = new ArrayList<Feature>();
                            final List<Feature> fs_bdc_h = new ArrayList<Feature>();
                            final Map<String, Feature> map_jzx = new HashMap<>();
                            final List<Map<String, Object>> fs_jzqz = new ArrayList<>();
                            final LinkedHashMap<Feature, List<Feature>> fs_c_all = new LinkedHashMap<>();

                            LoadAll(mapInstance, featureBdcdy, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz,fs_z_fsjg, fs_ljz, fs_ftqk, fs_bdc_h, fs_h, fs_c_all, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    final Feature f_c = (Feature) t_;
                                    if (f_c != null && fs_zrz.size() > 0) {
                                        createDOCX(mapInstance, featureBdcdy, f_c, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz,fs_z_fsjg, fs_h, isRelaod, new AiRunnable() {
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
                                                            if (zrz_c.getKey().equals(FeatureHelper.Get(f_c, "SZC", 1) + "")) {
                                                                c = zrz_c;
                                                                break;
                                                            }
                                                        }
                                                        outputData(mapInstance, featureBdcdy, f_zd, fs_zrz, new ArrayList<Feature>(), fs_h, new ArrayList<Feature>(), fs_ftqk, f_c, c.getValue());
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
    public  void LoadAll(final MapInstance mapInstance,
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
                MapHelper.QueryOne(FeatureEdit.GetTable(mapInstance, FeatureConstants.C_TABLE_NAME), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID= '" + orid_bdc + "' ", new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        final Feature f_c = (Feature) t_;
//                        fs_h.add(f_c);
                        MapHelper.Query(FeatureEdit.GetTable(mapInstance, FeatureConstants.FTQK_TABLE_NAME), StringUtil.WhereByIsEmpty(orid_bdc) + " FTQX_ID= '" + orid_bdc + "' ", -1, fs_ftqk, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                MapHelper.Query(FeatureEdit.GetTable(mapInstance, FeatureConstants.LJZ_TABLE_NAME), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID= '" + FeatureHelper.GetOrid(FeatureHelper.Get(f_c, "ORID_PATH", ""), FeatureConstants.LJZ_TABLE_NAME) + "' ", "LJZH", "asc", -1, fs_ljz, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        MapHelper.Query(FeatureEdit.GetTable(mapInstance, FeatureConstants.ZRZ_TABLE_NAME), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID= '" + FeatureHelper.GetOrid(FeatureHelper.Get(f_c, "ORID_PATH", ""), FeatureConstants.ZRZ_TABLE_NAME) + "' ", "ZRZH", "asc", -1, fs_zrz, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
//                                                FeatureEditBDC.LoadAllCAndFsToH(mapInstance,fs_zrz.get(0),FeatureHelper.Get(f_c,"SZC",""),fs_c,callback);
                                                String where = "ORID_PATH like '" + FeatureHelper.GetOrid(FeatureHelper.Get(f_c, "ORID_PATH", ""), FeatureConstants.ZRZ_TABLE_NAME) + "' "
                                                        + "and CH='" + FeatureHelper.Get(f_c, "CH", "") + "'";
                                                MapHelper.Query(FeatureEdit.GetTable(mapInstance, FeatureConstants.H_TABLE_NAME), where, -1, fs_h, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        AiRunnable.Ok(callback, f_c);
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

    public  void createDOCX(final MapInstance mapInstance, final Feature featureBdc, final Feature f, final Feature f_zd,
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
                        String zddm = FeatureHelper.Get(f_zd, "ZDDM", "");
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
                        FeatureEditBDC.Put_data_zrz(mapInstance, map_, bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h);
                        // 在全局放所有户
                        FeatureEditBDC.Put_data_hs(mapInstance, map_, fs_h);
                        // 在全局放一个户
                        FeatureEditBDC.Put_data_h(mapInstance, map_, fs_h);
                        // 在全局放一个幢
                        FeatureEditBDC.Put_data_zrz(mapInstance, map_, fs_zrz);
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

    public  void outputData(final MapInstance mapInstance,
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
            final String file_dcb = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/") + "不动产权籍调查表" + bdcdyh + ".docx";
            FileUtils.copyFile(FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh), file_dcb);

            String dxfDir = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/") + bdcdyh ;
            if (DxfHelper.TYPE == DxfHelper.TYPE_NEIMENG) {
                // 内蒙 城镇 陈总
                String dxf_fcfht = dxfDir + "房产分层图.dxf";// fs_zrz =0
                new DxfFcfhtDefault(mapInstance).set(dxf_fcfht).set(feature_bdc, f_h, f_zd, fs_zrz, fs_c_all).write().save();
            } else {
                String dxfDefault = dxfDir + "房产分层图.dxf";
                new DxfFcfhtDefault(mapInstance).set(dxfDefault).set(feature_bdc, f_h, f_zd, fs_zrz, fs_c_all).write().save();
            }

        } catch (Exception es) {
            Log.e(TAG, "导出数据失败", es);
        }
    }
    ///endregion

}
