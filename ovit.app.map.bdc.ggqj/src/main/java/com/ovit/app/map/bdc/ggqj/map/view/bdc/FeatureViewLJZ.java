package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.graphics.Bitmap;
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
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.MapImage;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.pojo.FeaturePojo;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.ImageUtil;
import com.ovit.app.util.ListUtil;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZ_FSJG.CreateFeatureHollow;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewLJZ extends FeatureView {

    @Override
    public void onCreate() {
        super.onCreate();
        iconColor = Color.rgb(169, 0, 230);
    }

    @Override
    public void fillFeature(Feature feature, Feature feature_zrz) {
        super.fillFeature(feature, feature_zrz);
        String ljzh = FeatureHelper.Get(feature, "LJZH", "");
        if (feature_zrz != null) {
            String zrzh = FeatureHelper.Get(feature_zrz, "ZRZH", "");
            FeatureHelper.Set(feature, "ZRZH", zrzh);
            FeatureHelper.Set(feature, "BDCDYH", FeatureHelper.Get(feature_zrz, "BDCDYH", ""));
            FeatureHelper.Set(feature, "ZCS", FeatureHelper.Get(feature_zrz, "ZCS", "1"));
        }
        FeatureHelper.Set(feature, "MPH", FeatureHelper.Get(feature, "MPH", getMph(ljzh))); //  门牌号
        FeatureHelper.Set(feature, "FWJG1", FeatureHelper.Get(feature_zrz, "FWJG", "4"), true, false);// [4][B][混]混合结构
        FeatureHelper.Set(feature, "FWYT1", FeatureHelper.Get(feature_zrz, "FWYT", "10"), true, false);// [10]住宅
        FeatureHelper.Set(feature, "MPH", FeatureHelper.Get(feature_zrz, "JZWMC", "1"), true, false);
        FeatureHelper.Set(feature, "JZWZT ", FeatureHelper.Get(feature_zrz, "JZWZT", "1"), true, false);// [1]历史
        FeatureHelper.Set(feature, "SZC", FeatureHelper.Get(feature, "SZC", "1"));
        FeatureHelper.Set(feature, "DSCS", FeatureHelper.Get(feature, "DSCS", "1"));
        FeatureHelper.Set(feature, "DXCS", FeatureHelper.Get(feature, "DXCS", "0"));
    }

    @Override
    public String addActionBus(String groupname) {
        int count = mapInstance.getSelFeatureCount();
        // 推荐
        mapInstance.addAction(groupname, "画逻辑幢", R.mipmap.app_map_layer_ljz, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            if (iscantqzrz) {
                mapInstance.addAction(groupname, "提取自然幢", R.mipmap.app_map_layer_zrz, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AiDialog aiDialog = AiDialog.get(mapInstance.activity).setHeaderView(R.mipmap.app_map_layer_zrz, "提取自然幢");
                        aiDialog.addContentView("确定要提取选中的逻辑幢图形合并后，创建自然幢么？", "该操作不会对现有的逻辑幢进行修改，提取自然幢成功后进行关联。")
                                .setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        List<Geometry> gs = MapHelper.geometry_get(fs);
                                        final Geometry g = GeometryEngine.union(gs);

                                        if (g instanceof Polygon) {
                                            int ps = ((Polygon) g).getParts().size();
                                            if (ps == 0) {
                                                aiDialog.setContentView("合并后的图型是空的！").setFooterView(AiDialog.CENCEL, null, null);
                                                return;
                                            } else if (ps == 1) {
                                                aiDialog.setContentView("合并后的图型是一整块，确定要继续提取么？", "接下来将提取成逻辑幢，并关联。");
                                            } else if (ps > 1) {
                                                aiDialog.setContentView("合并后的图型并非是一整块，确定要继续提取么？", "合并后的图层可能存在多个圈，根据实际情况选择。");
                                            }
                                            final Bitmap bitmap = new MapImage(100, 100).setColor(Color.BLACK).setSw(1).draw(gs).setColor(Color.RED).setSw(2).draw(g).getValue();
                                            aiDialog.addContentView(aiDialog.getView(bitmap)).setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    Feature f = mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ).createFeature();
                                                    f.setGeometry(g);
                                                    f.getAttributes().put("ZCS", getMaxFloor(fs, "ZCS"));
                                                    f.getAttributes().put("FWJG", getZrzStructure(fs));
                                                    FeatureViewZRZ.CreateFeature(mapInstance, (Feature) null, f, null);
                                                    ImageUtil.recycle(bitmap);
                                                    dialog.dismiss();
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                });
            }
//                mapInstance.addAction(groupname, "加层", R.mipmap.app_map_h, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        draw_c(feature, "1", null);
//                    }
//                });

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

//
//        if (count > 0) {
//            mapInstance.addAction(groupname, "画飘窗", R.mipmap.app_map_layer_ljz, new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    draw_h_fsjg(feature, "飘窗");
//                }
//            });
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

    // 列表项，点击加载自然幢
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
                    queryChildFeature(FeatureHelper.TABLE_NAME_H, item, fs, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            queryChildFeature(FeatureHelper.TABLE_NAME_Z_FSJG, item, fs, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    com.ovit.app.map.view.FeatureView fv_h = mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_H);
                                    fv_h.fs_ref.add(item);
                                    fv_h.buildListView(ll_list_item, fs, deep + 1);
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

    public static FeatureTable GetTable(MapInstance mapInstance) {
        return MapHelper.getLayer(mapInstance.map, FeatureHelper.TABLE_NAME_LJZ).getFeatureTable();
    }

    //  获取最大的编号
    public void getMaxLjzh(String zrzh, final AiRunnable callback) {
        if (FeatureHelper.isZRZHValid(zrzh)) {
            String id = StringUtil.substr(zrzh, FeatureHelper.FEATURE_DJZQDM_LENG);
            MapHelper.QueryMax(table, StringUtil.WhereByIsEmpty(id) + "LJZH like '" + id + "__'", "LJZH", id.length(), 0, id + "00", callback);
        } else {
            AiRunnable.Ok(callback, null, "00", "00");
        }
    }

    private String getId(String zddm) {
        if (TextUtils.isEmpty(zddm)) {
            String xmbm = mapInstance.aiMap.getProjectXmbm();
            if (!TextUtils.isEmpty(xmbm) && xmbm.length() == 12) {
                return xmbm + "F000";
            }
        }
        return StringUtil.substr(zddm, 0, 12) + "F000";
    }

    // 新的逻辑幢号
    public void newLjzh(String zrzh, final AiRunnable callback) {
        getMaxLjzh(zrzh, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = "";
                if (objects.length > 1) {
                    String maxid = objects[0] + "";
                    // 最大号加1
                    int count = AiUtil.GetValue(objects[1], 0) + 1;
                    id = StringUtil.fill(String.format("%02d", count), maxid, true);
                }
                AiRunnable.Ok(callback, id);
                return null;
            }
        });
    }

    public void loadByLjzh(String ljzh, AiRunnable callback) {
        MapHelper.QueryOne(table, StringUtil.WhereByIsEmpty(ljzh) + " LJZH like '%'" + ljzh + "%' ", callback);
    }

    public static FeatureViewLJZ From(MapInstance mapInstance, Feature f) {
        FeatureViewLJZ fv = From(mapInstance);
        fv.set(f);
        return fv;
    }

    public static FeatureViewLJZ From(MapInstance mapInstance) {
        FeatureViewLJZ fv = new FeatureViewLJZ();
        fv.set(mapInstance).set(mapInstance.getTable("LJZ"));
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

    //    绘制逻辑幢
    public static void CreateFeature(final MapInstance mapInstance, final Feature f_p, Feature f, final AiRunnable callback) {

        if (f_p != null && f_p.getFeatureTable() != mapInstance.getTable("ZRZ")) {
            // 如果不是自然幢
            String orid = mapInstance.getOrid_Match(f, "ZRZ");
            if (StringUtil.IsNotEmpty(orid)) {
                CreateFeature(mapInstance, orid, f, callback);
                return;
            }
        }

        final FeatureViewLJZ fv = From(mapInstance, f);
        final Feature feature;
        if (f == null) {
            feature = fv.table.createFeature();
            fv.set(feature);// 设置 Feature
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
        final String zddm = FeatureHelper.Get(f_p, "ZDDM", "");
        final String zrzh = FeatureHelper.Get(f_p, "ZRZH", "");
        // 绘图
        fv.drawAndAutoCompelet(feature, fs_update, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 设置新的zddm
                fv.newLjzh(zrzh, new AiRunnable() {
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

    // 查询所有逻辑幢，识别户和幢附属
    public static void LaodAllLJZ_IdentyHAndZFSJG(final MapInstance mapInstance, final AiRunnable callback) {
        final List<Feature> fs = new ArrayList<>();
        MapHelper.Query(GetTable(mapInstance), "", -1, fs, new AiRunnable(callback) {
            // 递归调用，直到全部完成
            void identy(final List<Feature> fs, final int index, final AiRunnable identy_callback) {
                if (index < fs.size()) {
                    FeatureViewLJZ.IdentyLJZ_HAndZFSJG(mapInstance, fs.get(index), new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            identy(fs, index + 1, identy_callback);
                            return null;
                        }
                    });
                } else {
                    AiRunnable.Ok(identy_callback, index);
                }
            }

            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                identy(fs, 0, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        AiRunnable.Ok(callback, fs, fs.size());
                        return null;
                    }
                });
                return null;
            }
        });
    }

    public void update_Area(Feature feature, final List<Feature> f_hs, List<Feature> f_z_fsjgs, List<Feature> f_h_fsjgs, final AiRunnable callback) {
        String id = FeatureHelper.Get(feature, "ZRZH", "");
        int zcs = FeatureHelper.Get(feature, "ZCS", 1);
        Geometry g = feature.getGeometry();
        final List<Feature> upd_fs = new ArrayList<>();
        double area = 0;
        double hsmj = 0;
        if (g != null) {
            area = MapHelper.getArea(mapInstance, g);

            for (Feature f : f_hs) {
                String zrzh = FeatureHelper.Get(f, "ZRZH", "");
                if (id.equals(zrzh)) {
//                    double f_hsmj = FeatureHelper.Get(f,"SCJZMJ", 0d);
                    double f_hsmj = MapHelper.getArea(mapInstance, f.getGeometry());
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
            for (Feature f : f_h_fsjgs) {
                String hid = FeatureHelper.Get(f, "HID", "");
                if (hid.contains(id)) {
                    double f_hsmj = FeatureHelper.Get(f, "HSMJ", 0d);
                    hsmj += f_hsmj;
                }
            }
            if (hsmj <= 0) {
                hsmj = area * zcs;
            }
//            FeatureHelper.Set(feature,"ZZDMJ", AiUtil.Scale(area, 2));
            // 更新逻辑幢建筑面积
            FeatureHelper.Set(feature, "SCJZMJ", AiUtil.Scale(hsmj, 2));
            upd_fs.add(feature);

            new AiForEach<Feature>(f_hs, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    MapHelper.saveFeature(upd_fs, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            AiRunnable.Ok(callback, null);
                            return null;
                        }
                    });
                    return null;
                }
            }
            ) {
                public void exec() {
                    String where_ft = "ftqx_name ='" + FeatureHelper.Get(f_hs.get(postion), "ORID") + "'";
                    MapHelper.Query(mapInstance.getTable("分摊情况", "FTQK"), where_ft, new AiRunnable(getNext()) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            if (t_ != null) {
                                double ftjzmj = 0;
                                double scjzmj = 0;
                                ArrayList<Feature> h_ftqk = (ArrayList<Feature>) t_;
                                for (Feature f : h_ftqk) {
                                    ftjzmj += FeatureHelper.Get(f, "FTJZMJ", 0d);
                                }
                                Feature f_h = f_hs.get(postion);
                                scjzmj = MapHelper.getArea(mapInstance, f_h.getGeometry()) + ftjzmj;
                                FeatureHelper.Set(f_h, "SCJZMJ", scjzmj);
                                FeatureHelper.Set(f_h, "SCFTJZMJ", ftjzmj);
                                upd_fs.add(f_h);
                                AiRunnable.Ok(getNext(), true, true);
                            } else {
                                AiRunnable.Ok(getNext(), true, true);
                            }
                            return null;
                        }
                    });
                }
            }.start();

        }
    }

    @Override
    public String getZddm() {
        String zrzh = getZrzh();
        if (StringUtil.IsEmpty(zrzh)) {
            return "";
        }
        return StringUtil.substr(zrzh, 0, zrzh.length() - 5);
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
                update_Area(feature, fs_h, fs_z_fsjg, fs_h_fsjg, callback);
                return null;
            }
        });
    }

    private void loadH_And_Fsjg(MapInstance mapInstance, Feature feature, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_h_fsjg, AiRunnable aiRunnable) {

    }

    public void identyH(List<Feature> fs_h, final AiRunnable callback) {
        MapHelper.Query(mapInstance.getTable("H"), feature.getGeometry(), -1, fs_h, callback);
    }

    // 默认识别保存
    public void identyH(final AiRunnable callback) {
        identyH(true, callback);
    }

    // 识别显示结果返回
    public void identyH(final boolean isShow, final AiRunnable callback) {
        final List<Feature> fs_h = new ArrayList<>();
        identyH(fs_h, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final FeatureViewH fv_ = FeatureViewH.From(mapInstance);
                fv.fillFeature(fs_h, feature);
                if (isShow) {
                    fv_.fs_ref = ListUtil.asList(feature);
                    QuickAdapter<Feature> adapter = fv_.getListAdapter(fs_h, 0);
                    AiDialog dialog = AiDialog.get(mapInstance.activity, adapter);
                    dialog.setHeaderView(R.mipmap.app_map_layer_zrz, "识别到" + fs_h.size() + "个户属");
                    dialog.setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            fv_.fillFeature(fs_h, feature);
                            MapHelper.saveFeature(fs_h, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    AiRunnable.Ok(callback, fs_h);
                                    dialog.dismiss();
                                    return null;
                                }
                            });

                        }
                    });
                } else {
                    fv_.fillFeature(fs_h, feature);
                    MapHelper.saveFeature(fs_h, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            AiRunnable.Ok(callback, fs_h);
                            return null;
                        }
                    });

                }
                return null;
            }
        });
    }

    public void identyZ_Fsjg(List<Feature> features_zrz, final AiRunnable callback) {
        MapHelper.Query(mapInstance.getTable("Z_FSJG"), feature.getGeometry(), 0.1, features_zrz, callback);
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
                    dialog.setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
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

    // 识别显示结果返回
    public void identyH_Fsjg(List<Feature> featuresH, final AiRunnable callback) {
        new AiForEach<Feature>(featuresH, callback) {
            @Override
            public void exec() {
                Feature f_h = getValue();
                FeatureViewH fv = (FeatureViewH) mapInstance.newFeatureView(f_h);
                Log.i(TAG, "户识别户附属结构: " + FeatureHelper.Get(f_h, "ORID", "") + "====" + this.postion);
                fv.identyH_FSJG(f_h, false, getNext());
            }
        }.start();

    }

    public void loadCs(List<Feature> fs_c, AiRunnable callback) {
        String orid_zrzh = mapInstance.getOrid_Match(feature, "ZRZ");
        List<Feature> fs = new ArrayList<>();
        int szc = FeatureHelper.Get(feature, "SZC", 1);
        int zcs = FeatureHelper.Get(feature, "ZCS", 1);
        String where = " SJC > '" + (szc - 1) + "'  and  SJC < '" + (szc + zcs) + "' ";
        queryChildFeature("ZRZ_C", orid_zrzh, "", "CH", "", fs, callback);
    }

    // 快速绘制户
    public void h_init(final AiRunnable callback) {
        FeatureViewH.InitFeatureAll(mapInstance, feature, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiRunnable.Ok(callback, t_, objects);
                return null;
            }
        });
    }

    // 生成层
    public void draw_tj(Feature feature, AiRunnable callback) {
        String type = "天井";
        FeatureLayer layer = MapHelper.getLayer(map, "Z_FSJG", "幢附属结构");
        final Feature f = layer.getFeatureTable().createFeature();
        f.getAttributes().put("LC", "1");
        f.getAttributes().put("MC", type);
        f.getAttributes().put("TYPE", "1");
        f.getAttributes().put("FHMC", type);
        mapInstance.fillFeature(f);
        CreateFeatureHollow(mapInstance, feature, f, "1", callback);
    }

    public static void IdentyLJZ_HAndZFSJG(final MapInstance mapInstance, final Feature f_ljz, final AiRunnable callback) {
        String ljzh = FeatureHelper.Get(f_ljz, "ZRZH", "");
        if (StringUtil.IsEmpty(ljzh)) {
            AiRunnable.Ok(callback, ljzh);
            return;
        }
        FeatureEditH.IdentyLJZ_H(mapInstance, f_ljz, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                FeatureViewZ_FSJG.IdentyLJZ_FSJG(mapInstance, f_ljz, callback);
                return null;
            }
        });
    }

    public void featureConversionToZfsjg(final AiRunnable callback) {
        String type = "沿廊";
        FeatureLayer layer = MapHelper.getLayer(map, "Z_FSJG", "幢附属结构");
        final Feature f = layer.getFeatureTable().createFeature();
        //   f.getAttributes().put("ZID", AiUtil.GetValue(feature.getAttributes().get("ZRZH"), ""));
        // f.getAttributes().put("ZH", AiUtil.GetValue(feature.getAttributes().get("ZH"), ""));
        // f.getAttributes().put("LC", "1");
        f.getAttributes().put("MC", type);
        f.getAttributes().put("TYPE", "1");
        f.getAttributes().put("FHMC", type);
        f.setGeometry(feature.getGeometry());
        mapInstance.fillFeature(f);
        MapHelper.saveFeature(Arrays.asList(new Feature[]{f}), Arrays.asList(feature), new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiRunnable.Ok(callback, f, objects);
                return null;
            }
        });
    }

    public void featureConversionToHfsjg(final AiRunnable callback) {
        String type = "阳台";
        FeatureLayer layer = MapHelper.getLayer(map, "H_FSJG", "户附属结构");
        final Feature f = layer.getFeatureTable().createFeature();
//                                f.getAttributes().put("HID",AiUtil.GetValue(featureH.getAttributes().get("ID"),""));
//                                f.getAttributes().put("HH",AiUtil.GetValue(featureH.getAttributes().get("HH"),""));
//                                f.getAttributes().put("LC",AiUtil.GetValue(featureH.getAttributes().get("CH"),""));
        f.getAttributes().put("MC", type);
        f.getAttributes().put("TYPE", "1");
        f.getAttributes().put("FHMC", type);
        f.setGeometry(feature.getGeometry());
        mapInstance.fillFeature(f);
        MapHelper.saveFeature(Arrays.asList(new Feature[]{f}), Arrays.asList(feature), new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiRunnable.Ok(callback, f, objects);
                return null;
            }
        });
    }

    // 实例化户
    public void InitFeatureLjz(final AiRunnable callback) {
        FeaturePojo featurePojo = new FeaturePojo(feature, "BZ");
        List<String> lcs = featurePojo.getLc();
        List<Feature> fs_h = new ArrayList<>();
        for (String lc : lcs) {
            Feature f_h = mapInstance.getTable("H").createFeature();
            f_h.setGeometry(feature.getGeometry());
            FeatureHelper.Set(f_h, "SZC", lc);
            FeatureHelper.Set(f_h, "LJZH", featurePojo.getLjzh());
            FeatureHelper.Set(f_h, "ZRZH", featurePojo.getZrzh());
            FeatureHelper.Set(f_h, "HH", featurePojo.getHh());
            FeatureHelper.Set(f_h, "MPH", featurePojo.getZrzh() + "-" + featurePojo.getLjzh() + "-" + lc + featurePojo.getHh());
            mapInstance.fillFeature(f_h, feature);
            fs_h.add(f_h);
        }
        AiRunnable.Ok(callback, fs_h);
    }

    public void init_fsjg(String fsjg_lx, final String tableName) {
        setFsjgAttribute(fsjg_lx, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final AiDialog dialog = (AiDialog) t_;
                dialog.dismiss();
                final Map<String, Object> map = (Map<String, Object>) objects[0];
                final Map<String, String> dataconfig = (Map<String, String>) objects[1];
                final FeatureView featureView = (FeatureView) mapInstance.newFeatureView(tableName);
                featureView.fsjg_init(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        if (t_ instanceof Feature) {
                            final Feature f_t = (Feature) t_;
                            String szc = map.get("SZC") + "";
                            final List<Integer> nubs = StringUtil.GetNumbers(szc);
                            final ArrayList<Feature> features_save = new ArrayList<>();
                            for (int nub : nubs) {
                                Feature f;
                                if (nubs.indexOf(nub) == 0) {
                                    f = f_t;
                                } else {
                                    f = mapInstance.getTable(tableName).createFeature();
                                }
                                f.setGeometry(f_t.getGeometry());
                                f.getAttributes().put("LC", nub + "");
                                String dataconfigType = dataconfig.get("TYPE");
                                String type = dataconfigType.substring(dataconfigType.indexOf("[") + 1, dataconfigType.indexOf("]"));
                                f.getAttributes().put("TYPE", type);
                                f.getAttributes().put("FHMC", dataconfig.get("FHMC"));
                                featureView.hsmj(f, mapInstance);
                                fv.fillFeature(f, feature);
                                features_save.add(f);
                            }
                            MapHelper.saveFeature(features_save, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    mapInstance.viewFeature(f_t);
                                    return null;
                                }
                            });
                        }
                        return null;
                    }
                });
                return null;
            }
        });

    }


    private void setFsjgAttribute(final String resname, final AiRunnable callback) {
        String desc = "该操作主要是绘制幢附属结构到指定楼层！";
        final Map<String, Object> map = new LinkedHashMap<>();
        final Map<String, String> dataconfig = new LinkedHashMap<>();
        final AiDialog aiDialog = AiDialog.get(mapInstance.activity);
        final String szc = "SZC";
        map.put(szc, "1");
        dataconfig.put("TYPE", "1");
        aiDialog.addContentView(aiDialog.getSelectView("类型", resname, dataconfig, "FHMC"));
        aiDialog.addContentView(aiDialog.getSelectView("面积计算", "hsmjlx", dataconfig, "TYPE"));
        aiDialog.setHeaderView(R.mipmap.app_icon_warning_red, desc)
                .addContentView(aiDialog.getEditView("请输入附属结构所在的楼层", map, szc));
        aiDialog.setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AiRunnable.Ok(callback, aiDialog, map, dataconfig);
            }
        });
    }

}
