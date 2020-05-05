package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.ImmutablePart;
import com.esri.arcgisruntime.geometry.ImmutablePartCollection;
import com.esri.arcgisruntime.geometry.Multipart;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.CompositeSymbol;
import com.esri.arcgisruntime.symbology.MarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.core.License;
import com.ovit.app.exception.CrashHandler;
import com.ovit.app.map.MapImage;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.model.DxfFcfct_tianmen;
import com.ovit.app.map.bdc.ggqj.map.model.DxfFcfct_xianan;
import com.ovit.app.map.bdc.ggqj.map.model.DxfFcfht_badong;
import com.ovit.app.map.bdc.ggqj.map.model.DxfFcfwh_jinshan;
import com.ovit.app.map.bdc.ggqj.map.model.DxfFct_xianan;
import com.ovit.app.map.bdc.ggqj.map.model.DxfZdct;
import com.ovit.app.map.bdc.ggqj.map.model.DxfZdctDefult;
import com.ovit.app.map.bdc.ggqj.map.model.DxfZdt_badong;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.LayerConfig;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.custom.shape.ShapeUtil;
import com.ovit.app.map.model.FwPc;
import com.ovit.app.signature.SignActivity;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.DialogBuilder;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.AppConfig;
import com.ovit.app.util.ColorUtil;
import com.ovit.app.util.ConvertUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.ImageUtil;
import com.ovit.app.util.ListUtil;
import com.ovit.app.util.ReportUtils;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditBDC.GetPath_Templet;
import static com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditBDC.GetPath_ZD_zip;
import static com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditBDC.GetPath_doc;
import static com.ovit.app.map.view.FeatureEdit.GetTable;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewZD extends FeatureView {

    //region 常量
    final static String TAG = "FeatureViewZD";
    final public static String TABLE_ATTR_FTXS_ZD = "FTXS";
    ///endregion

    //region 字段
    ///endregion

    //region 构造函数与构造方法
    public static FeatureViewZD From(MapInstance mapInstance, Feature f) {
        FeatureViewZD fv = From(mapInstance);
        fv.set(f);
        return fv;
    }

    public static FeatureViewZD From(MapInstance mapInstance) {
        FeatureViewZD fv = new FeatureViewZD();
        fv.set(mapInstance).set(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZD));
        return fv;
    }
    ///endregion

    //region 重写函数和回调
    @Override
    public void onCreate() {
        super.onCreate();
        iconColor = Color.RED;
    }

    // 菜单控制
    @Override
    public String addActionBus(String groupname) {
        int count = mapInstance.getSelFeatureCount();
        // 根据画宗地推荐
        if (feature != null && feature.getFeatureTable() == table) {
            if (count > 0) {
                mapInstance.addAction(groupname, "一键处理", R.mipmap.app_icon_map_znsb, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        zntq(feature);
                        zncl(feature, null);

                    }
                });

                mapInstance.addAction(groupname, "图形识别", R.mipmap.app_icon_map_txgl, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        txsb(feature);
                    }
                });
                mapInstance.addAction(groupname, "画附属设施", R.mipmap.app_map_layer_ljz, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        draw_fsss(feature, "1", null);
                    }
                });
                mapInstance.addAction(groupname, "关联附属设施", R.mipmap.app_map_layer_ljz, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connect_fsss(null);
                    }
                });
                mapInstance.addAction(groupname, "权属", R.mipmap.app_map_user, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        to_bdc(feature);
                    }
                });


            }
        }

        mapInstance.addAction(groupname, "画宗地", R.mipmap.app_map_layer_zd, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_zd(null);
            }
        });
        mapInstance.addAction(groupname, "画幢", R.mipmap.app_map_layer_zrz, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_zrz(feature, null);
            }
        });
        mapInstance.addAction(groupname, "画逻辑幢", R.mipmap.app_map_layer_ljz, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_ljz("", "1", null);
            }
        });

        addActionTY(groupname);// 涂鸦
        addActionPZ(groupname);// 拍照

        groupname = "操作";

        if (feature != null && feature.getFeatureTable() == table) {
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
        addActionDW(""); // 地物
        addActionBZ("", false);// 标注
        return groupname;
    }

    private void connect_fsss(final AiRunnable callback) {
        ToastMessage.Send(activity, "请谨慎选择附属设施！");
        Layer layer = MapHelper.getLayer(map, FeatureHelper.TABLE_NAME_FSSS);
        mapInstance.setSelectLayer(layer, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final Feature f = (Feature) t_;
                if (f != null && FeatureHelper.LAYER_NAME_FSSS.equals(mapInstance.getLayerName(f))) {
                    // 不动产单元 与 宗地 关联
                    DialogBuilder.confirm(activity, "添加附属附属设施", "宗地是否关联该附属设施？", null, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                           FeatureViewFSSS fvFSSS = FeatureViewFSSS.From(mapInstance, f);
                            fvFSSS.fillFeature(f, feature);
                            MapHelper.saveFeature(f, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    dialog.dismiss();
                                    ToastMessage.Send("新增附属设施成功。");
                                    mapInstance.setBindCallback(null);
                                    AiRunnable.Ok(callback, t_, objects);
                                    return null;
                                }
                            });
                        }
                    }, "放弃", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mapInstance.setBindCallback(null);
                        }
                    }, "继续", null).create().show();
                }
                return null;
            }
        }, false);
    }

    // 填充
    @Override
    public void fillFeature(Feature feature) {
        super.fillFeature(feature);
        String zddm = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ZDDM, "");
        if (!FeatureHelper.isZDDMHValid(zddm)) {
            if (StringUtil.IsNotEmpty(zddm) && !zddm.contains("JC") && !zddm.contains("JB") && !zddm.contains("GB")) {
                // 宗地代码不包含特征码
                zddm = StringUtil.substr(zddm, 0, zddm.length() - 5) + "JC" + StringUtil.substr_last(zddm, 5);
            }
            if (!FeatureHelper.isZDDMHValid(zddm)) {
                String xmbm = getXmbm();
                zddm = StringUtil.substr(xmbm, 0, 19 - zddm.length()) + zddm;
            }
            FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ZDDM, zddm);
            FeatureHelper.Set(feature, "YBZDDM", zddm);
        }
        FeatureHelper.Set(feature, "PRO_ZDDM_F", StringUtil.substr_last(zddm, 7));
        FeatureHelper.Set(feature, "PZYT", "072", true, false);
        FeatureHelper.Set(feature, "YT", "072", true, false);

        if (feature.getGeometry() != null && 0d == FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ZDMJ, 0d)) {
            FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ZDMJ, MapHelper.getArea(feature.getGeometry()));
        }
        int scale = 200;
        FeatureHelper.Set(feature, "GLBLC", "1:" + scale);
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
                    queryChildFeature(FeatureHelper.TABLE_NAME_ZRZ, item, fs, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            com.ovit.app.map.view.FeatureView fv_c = mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_ZRZ);
                            fv_c.fs_ref.add(item);
                            fv_c.listAdapterListener = listAdapterListener;
                            fv_c.buildListView(ll_list_item, fs, deep + 1);
                            return null;
                        }
                    });
                }
                ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
            }
        });
    }

    /**
     * 填充不动产单元属性
     *
     * @param featureBdcdy 不动产单元
     * @param featureZd    宗地
     */
    @Override
    public void fillFeatureBdcdy(Feature featureBdcdy, Feature featureZd) {
        featureBdcdy.getAttributes().put("YHZGX", "户主");
        featureBdcdy.getAttributes().put("XM", FeatureHelper.Get(featureZd, "QLRXM"));
        featureBdcdy.getAttributes().put("ZJH", FeatureHelper.Get(featureZd, "QLRZJH"));
        featureBdcdy.getAttributes().put("ZJZL", FeatureHelper.Get(featureZd, "QLRZJZL"));
        featureBdcdy.getAttributes().put("DZ", FeatureHelper.Get(featureZd, "QLRTXDZ"));
        featureBdcdy.getAttributes().put("DH", FeatureHelper.Get(featureZd, "QLRDH"));
        featureBdcdy.getAttributes().put("BDCQZH", FeatureHelper.Get(featureZd, "TDZH"));
        fillFeature(featureBdcdy, featureZd);
    }
    ///endregion

    //region 公有函数
    /**
     * 通过逻辑幢提取自然幢
     *
     * @param fs_ljz   逻辑幢Feature集合
     * @param callback 提取自然幢成功后回调
     */
    public void extratGraph(final List<Feature> fs_ljz, final AiRunnable callback) {
        final AiDialog aiDialog = AiDialog.get(mapInstance.activity).setHeaderView(R.mipmap.app_map_layer_zrz, "提取自然幢");
        aiDialog.addContentView("确定要提取选中的逻辑幢图形合并后，创建宗地么？", "该操作不会对现有的宗地进行修改，提取宗地成功后进行关联。")
                .setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<Geometry> gs = MapHelper.geometry_get(fs_ljz);
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
                                    Feature f = mapInstance.getTable(FeatureHelper.TABLE_NAME_ZD).createFeature();
                                    f.setGeometry(g);
                                    ImageUtil.recycle(bitmap);
                                    dialog.dismiss();
                                    FeatureViewZD.CreateFeature(mapInstance, f, callback);
                                }
                            });
                        }
                    }
                });
    }

    ///endregion

    //region 私有函数

    /**
     * 宗地智能识别房屋
     *
     * @param f_zd 宗地
     */
    private void txsb(final Feature f_zd) {
        final String funcdesc = "该功能将逐一对宗地内房屋进行处理："
                + "\n 1、宗地识别自然幢；"
                + "\n 2、自然幢识别逻辑幢。";
        final AiDialog aidialog = AiDialog.get(mapInstance.activity);
        aidialog.setHeaderView(R.mipmap.app_icon_rgzl_blue, "图形识别")
                .setContentView("注意：属于不可逆操作，请谨慎处理！", funcdesc)
                .setFooterView(AiDialog.CENCEL, "确定，我要继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        // 完成后的回掉
                        final AiRunnable callback = new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                final List<Feature> fs_zrz = (List<Feature>) t_;

                                aidialog.addContentView("识别成功，您可能需要快速生成户、层。");
                                aidialog.setFooterView(AiDialog.COMPLET, null, null, null, AiDialog.EXECUTE_NEXT, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {
                                        aidialog.dismiss();
                                        FeatureViewZRZ fv_zrz = FeatureViewZRZ.From(mapInstance);
                                        fv_zrz.ipug(fs_zrz);
                                    }
                                });

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
                        aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                        aidialog.setContentView("开始处理数据：");
                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "查找所有的自然幢幢，并识别自然幢。");
                        final FeatureViewZD fv_zd = (FeatureViewZD) mapInstance.newFeatureView(f_zd);
                        final List<Feature> featuresZrz = new ArrayList<>();
                        identyZrz(featuresZrz, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                aidialog.addContentView("宗地识别自然幢", AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "识别到" + featuresZrz.size() + "个自然幢。");
                                fv_zd.identyZrzFromZD(mapInstance, feature, featuresZrz, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        // 自然幢识别逻辑幢
                                        FeatureViewZRZ fv_zrz = (FeatureViewZRZ) mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_ZRZ);
                                        fv_zrz.indentyLjzFromZrzs(featuresZrz, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                aidialog.addContentView("自然幢识别逻辑幢", AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "自然幢识别逻辑幢成功。");
                                                AiRunnable.Ok(callback, featuresZrz, objects);
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
                }).show();

    }

    private void txsb(final Feature f_zd, final AiRunnable callback) {
        final FeatureViewZD fv_zd = (FeatureViewZD) mapInstance.newFeatureView(f_zd);
        final List<Feature> featuresZrz = new ArrayList<>();
        identyZrz(featuresZrz, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                fv_zd.identyZrzFromZD(mapInstance, feature, featuresZrz, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        // 自然幢识别逻辑幢
                        FeatureViewZRZ fv_zrz = (FeatureViewZRZ) mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_ZRZ);
                        fv_zrz.indentyLjzFromZrzs(featuresZrz, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                AiRunnable.Ok(callback, featuresZrz, objects);
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


    /**
     * 通过宗地智能提取自然幢
     *
     * @param f_zd
     */
    private void zntq(Feature f_zd) {
        final String funcdesc = "该功能将逐宗地内逻辑幢提取为自然幢。";
        final AiDialog aidialog = AiDialog.get(mapInstance.activity);
        aidialog.setHeaderView(R.mipmap.app_icon_rgzl_blue, "提取幢")
                .setContentView("注意：属于不可逆操作，将识别宗地范围内的逻辑幢合成自然幢", funcdesc)
                .setFooterView(AiDialog.CENCEL, "确定，我要继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 完成后的回掉
                        final AiRunnable callback = new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                aidialog.addContentView("处理数据完成，你可能还需要进行图形识别。");
                                aidialog.setFooterView("图形识别", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        txsb(feature);
                                    }
                                }, null, null, "完成", null);
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
                        aidialog.setCancelable(false);// 设置不可中断
                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "查找所有的逻辑幢，并合成自然幢");
                        Log.d(TAG, "智能处理:查找所有的逻辑幢，并合成自然幢");
                        // 查询宗地范围内的逻辑幢
                        final List<Feature> fs_zrz = new ArrayList<>();
                        final List<Feature> fs_ljz = new ArrayList<>();
                        queryChildFeature(FeatureHelper.TABLE_NAME_ZRZ, fs_zrz, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                // 通过逻辑幢合成自然幢
                                if (!FeatureHelper.isExistElement(fs_zrz)) {
                                    queryChildFeature(FeatureHelper.TABLE_NAME_LJZ, fs_ljz, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            // 通过逻辑幢合成自然幢
                                            aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查询到" + fs_ljz.size() + "个逻辑幢。");
                                            aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 通过逻辑幢合成自然幢。");
                                            final List<Feature> featuresZRZ = new ArrayList<>();
                                            creatZrzToLjzUnion(fs_ljz, featuresZRZ, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    ToastMessage.Send("通过逻辑幢合成自然幢");
                                                    aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已合成" + featuresZRZ.size() + "自然幢。");
                                                    MapHelper.saveFeature(featuresZRZ, callback);
                                                    return null;
                                                }
                                            });
                                            return null;
                                        }
                                    });

                                } else {
                                    //宗地内有自然幢
                                    aidialog.addContentView("操作中断", AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "该宗地内已存在自然幢。");
                                    AiRunnable.Ok(callback, t_, objects);
                                }

                                return null;
                            }
                        });
                    }
                }).show();
    }

    /**
     * 通过宗地智能提取自然幢
     *
     * @param f_zd
     */
    private void zncl(final Feature f_zd, final AiRunnable callback) {
        final String funcdesc = "该功能将逐宗地内图形识别绘制。";
        final AiDialog aidialog = AiDialog.get(mapInstance.activity);
        aidialog.setHeaderView(R.mipmap.app_icon_rgzl_blue, "一键处理")
                .setContentView("注意：属于不可逆操作，将识别宗地范围内的逻辑幢合成自然幢", funcdesc)
                .setFooterView(AiDialog.CENCEL, AiDialog.EXECUTE_NEXT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 完成后的回掉
                        aidialog.setCancelable(false);// 设置不可中断
                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "查找所有的逻辑幢，并合成自然幢");
                        Log.d(TAG, "智能处理:查找所有的逻辑幢，并合成自然幢");
                        // 查询宗地范围内的逻辑幢
                        final List<Feature> fs_zrz = new ArrayList<>();
                        final List<Feature> fs_ljz = new ArrayList<>();
                        queryChildFeature(FeatureHelper.TABLE_NAME_ZRZ, fs_zrz, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                // 通过逻辑幢合成自然幢
                                if (!FeatureHelper.isExistElement(fs_zrz)) {
                                    queryChildFeature(FeatureHelper.TABLE_NAME_LJZ, fs_ljz, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            // 通过逻辑幢合成自然幢
                                            aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查询到" + fs_ljz.size() + "个逻辑幢。");
                                            aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 通过逻辑幢合成自然幢。");
                                            final List<Feature> featuresZRZ = new ArrayList<>();
                                            creatZrzToLjzUnion(fs_ljz, featuresZRZ, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    ToastMessage.Send("通过逻辑幢合成自然幢");
                                                    aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已合成" + featuresZRZ.size() + "自然幢。");
                                                    MapHelper.saveFeature(featuresZRZ, new AiRunnable() {
                                                        @Override
                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                            aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 开始图形识别");
                                                            txsb(f_zd, new AiRunnable() {
                                                                @Override
                                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                                    aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 图形识别成功");
                                                                    FeatureViewZRZ fv_zrz = FeatureViewZRZ.From(mapInstance);
                                                                    aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "开始生成户与层");
                                                                    final List<Feature> mfs_zrz = (List<Feature>) t_;
                                                                    fv_zrz.ipug(mfs_zrz, new AiRunnable() {
                                                                        @Override
                                                                        public <T_> T_ ok(T_ t_, Object... objects) {

                                                                            aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "户和层绘制成功。");
                                                                            aidialog.addContentView("数据一键处理成功。");
                                                                            aidialog.setFooterView(AiDialog.CENCEL, new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    dialog.dismiss();
                                                                                }
                                                                            }, null, null, AiDialog.COMPLET, null);

                                                                            return null;
                                                                        }
                                                                    });
                                                                    return null;
                                                                }
                                                            });
                                                            return super.ok(t_, objects);
                                                        }
                                                    });
                                                    return null;
                                                }
                                            });
                                            return null;
                                        }
                                    });

                                } else {
                                    //宗地内有自然幢
                                    aidialog.addContentView("数据一键处理中断", AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "该宗地内已存在自然幢，您可能需要进行图形识别功能。");
                                    aidialog.setFooterView(AiDialog.COMPLET, null, null, null, AiDialog.EXECUTE_NEXT, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            txsb(f_zd);
                                        }
                                    });

                                }
                                return null;
                            }
                        });
                    }
                }).show();
    }

    private void creatZrzToLjzUnion(List<Feature> featuresLJZ, final List<Feature> featuresZRZ, final AiRunnable callback) {
        if (featuresLJZ.size() > 0) {
            List<List<Feature>> ls = new ArrayList<>();
            final Map<String, List<Feature>> fs_map = getLsFromFeatureLjz(featuresLJZ, "FWJG1");
            final List<String> keys = new ArrayList<>(fs_map.keySet());
            new AiForEach<String>(keys, null) {

                @Override
                public void exec() {
                    List<Feature> fs = fs_map.get(keys.get(postion));
                    List<Geometry> gs = MapHelper.geometry_get(fs);
                    Geometry g = GeometryEngine.union(gs);
                    if (g instanceof Polygon) {
                        ImmutablePartCollection polygonParts = ((Polygon) g).getParts();
                        int ps = polygonParts.size();
                        if (ps > 0) {
                            for (ImmutablePart segments : polygonParts) {
                                Polygon polygon = new Polygon(new PointCollection(segments.getPoints()));
                                Feature featureZrz = mapInstance.getTable(FeatureHelper.LAYER_NAME_ZRZ).createFeature();
                                featureZrz.setGeometry(polygon);
                                List<Feature> myLjz = new ArrayList<>();
                                for (Feature f_ljz : fs) {
                                    if (MapHelper.geometry_contains(polygon, f_ljz.getGeometry())) {
                                        myLjz.add(f_ljz);
                                    }
                                }
                                featureZrz.getAttributes().put("ZCS", Double.valueOf(getMaxFloor(myLjz, "ZCS")));

                                featureZrz.getAttributes().put("FWJG", getZrzStructure(myLjz));
                                featuresZRZ.add(featureZrz);
                            }
                        }
                    }
                    AiRunnable.Ok(getNext(), null,     null);
                }
                @Override
                public void complet() {
                    AiRunnable.Ok(callback, featuresZRZ);
                }
            }.start();
        } else {
            AiRunnable.Ok(callback, featuresZRZ);
        }
    }


    private Map<String, List<Feature>> getLsFromFeatureLjz(List<Feature> fs, String attr) {
        Map<String, List<Feature>> map = null;
        if (FeatureHelper.isExistElement(fs) && StringUtil.IsNotEmpty(attr)) {
            map = new HashMap<>();
            for (Feature f : fs) {
                String attrValue = FeatureHelper.Get(f, attr, "");
                if (StringUtil.IsNotEmpty(attrValue )){
                    List<Feature> mfs = map.get(attrValue);
                    if (mfs == null) {
                        mfs = new ArrayList<>();
                        map.put(attrValue, mfs);
                    }
                    mfs.add(f);
                }
            }
        }
        return map;

    }

    /**
     * 拷贝身份镇，户口簿，土地权源资料等附件材料
     *
     * @param feature
     * @param feature_new_qlr
     */
    private void capyAttachments(Feature feature, Feature feature_new_qlr) {
        //拷贝资料
        String f_zd_path = mapInstance.getpath_feature(feature); //     不动产d单元
        String f_qlr_path = mapInstance.getpath_feature(feature_new_qlr);// 权利人 gyrxx

        String f_zd_zjh_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/权利人证件号/");
        String f_qlr_zjh_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/证件号/");
        FileUtils.copyFile(f_zd_zjh_path, f_qlr_zjh_path);

        String f_zd_zmcl_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/土地权属来源证明材料/");
        String f_qlr_zmcl_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/土地权属来源证明材料/");
        FileUtils.copyFile(f_zd_zmcl_path, f_qlr_zmcl_path);

        String f_zd_hkb_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/户口簿/");
        String f_qlr_hkb_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/户口簿/");
        FileUtils.copyFile(f_zd_hkb_path, f_qlr_hkb_path);
    }

    // 通过宗地与户设定不动产单元
    private void newBdcdyToH(Feature f_h, final AiRunnable callback) {
        final Feature feature_new_qlr = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX).createFeature();
        mapInstance.featureView.fillFeature(feature_new_qlr, f_h);
        feature_new_qlr.getAttributes().put(FeatureHelper.TABLE_ATTR_BDCDYH, f_h.getAttributes().get("ID"));
//        feature_new_qlr.getAttributes().put(FeatureHelper.TABLE_ATTR_ORID_PATH,f_h.getAttributes().get(FeatureHelper.TABLE_ATTR_ORID));
        MapHelper.saveFeature(feature_new_qlr, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiRunnable.Ok(callback, t_);
                return null;
            }
        });
    }

    // 通过宗地与层设定不动产单元
    private void newBdcdyToC(Feature f_c, final AiRunnable callback) {
        final Feature feature_new_qlr = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX).createFeature();
        mapInstance.featureView.fillFeature(feature_new_qlr, f_c);
        feature_new_qlr.getAttributes().put(FeatureHelper.TABLE_ATTR_BDCDYH, f_c.getAttributes().get("ID"));
        MapHelper.saveFeature(feature_new_qlr, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiRunnable.Ok(callback, t_);
                return null;
            }
        });
    }

    private AiDialog getBindBDC_View(MapInstance mapInstance, Feature f_zd, List<Feature> selected_feature_list) {
        final AiDialog dialog = AiDialog.get(mapInstance.activity);

        try {
            dialog.get().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //使EditText可以使用软键盘
            //根据屏幕分辨率将dialog设置为固定大小
            Display display = mapInstance.activity.getWindowManager().getDefaultDisplay();
            Window dialogWindow = dialog.get().getWindow();
            WindowManager.LayoutParams params = dialogWindow.getAttributes();
            params.width = (int) (display.getWidth() * 0.9);
            params.height = (int) (display.getHeight() * 0.8);
            dialogWindow.setAttributes(params);

            dialog.setContentView(getAllSelectBDC(mapInstance, f_zd, selected_feature_list));
            dialog.setHeaderView(R.mipmap.app_map_layer_zd, "不动产单元设定，ZDDM：" + getZddm());

            return dialog;
        } catch (Exception es) {
            Log.e(TAG, "获得不动产列表失败！" + es);
            dialog.dismiss();
            return null;
        }
    }

    private View getAllSelectBDC(final MapInstance mapInstance, Feature f_zd, final List<Feature> selected_feature_list) {
        final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
        final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
        final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
        if (ll_view.getChildCount() > 0) {
            ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
        }

        FeatureTable table = mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ);
        final List<Feature> fs = new ArrayList<Feature>();

        final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, fs) {
            @Override
            protected void convert(BaseAdapterHelper helper, final Feature item) {
                final com.ovit.app.map.view.FeatureView fv_zrz = mapInstance.newFeatureView(item.getFeatureTable());
                initSelectList(mapInstance, helper, item, 0, selected_feature_list);
                //展开LJZ
                final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                ll_list_item.setVisibility(View.GONE);
                helper.getView(com.ovit.app.map.R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                        if (!flag) {
                            final AiRunnable callbackToLjz = new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
//                                    boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                                    {
                                        final List<Feature> fs = (List<Feature>) t_;

                                        fv_zrz.queryChildFeature(FeatureHelper.TABLE_NAME_LJZ, item, fs, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        final com.ovit.app.map.view.FeatureView fv_ljz = mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_LJZ);

                                                        QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, fs) {
                                                            @Override
                                                            protected void convert(BaseAdapterHelper helper, final Feature item) {
                                                                initSelectList(mapInstance, helper, item, 2, selected_feature_list);
                                                                //展开户
                                                                final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                                                                ll_list_item.setVisibility(View.GONE);
                                                                helper.getView(com.ovit.app.map.R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                                                                        if (!flag) {
                                                                            final List<Feature> fs = new ArrayList<>();
                                                                            fv_ljz.queryChildFeature(FeatureHelper.TABLE_NAME_H, item, fs, new AiRunnable() {
                                                                                @Override
                                                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                    final com.ovit.app.map.view.FeatureView fv_h = mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_H);
                                                                                    QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, fs) {
                                                                                        @Override
                                                                                        protected void convert(BaseAdapterHelper helper, final Feature item) {
                                                                                            initSelectList(mapInstance, helper, item, 3, selected_feature_list);

                                                                                            //在监听函数中展开更次级
                                                                                            final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                                                                                            ll_list_item.setVisibility(View.GONE);
                                                                                            helper.getView(com.ovit.app.map.R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(View v) {
                                                                                                    //若还有次级则在此处继续展开
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    };

                                                                                    ll_list_item.setTag(adapter);
                                                                                    adapter.adpter(ll_list_item);
                                                                                    return null;
                                                                                }
                                                                            });
                                                                        }
                                                                        ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
                                                                    }
                                                                });

                                                            }
                                                        };
                                                        ll_list_item.setTag(adapter);
                                                        adapter.adpter(ll_list_item);
                                                        return null;
                                                    }
                                                }
                                        );
                                    }
                                    return null;
                                }

                            };

                            {
                                final List<Feature> fs = new ArrayList<>();
                                fv_zrz.queryChildFeature(FeatureHelper.TABLE_NAME_ZRZ_C, item, fs, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                final com.ovit.app.map.view.FeatureView fv_ljz = mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_ZRZ_C);

                                                QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, fs) {
                                                    @Override
                                                    protected void convert(BaseAdapterHelper helper, final Feature item) {
                                                        initSelectList(mapInstance, helper, item, 2, selected_feature_list);
                                                        //展开户
                                                        final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                                                        ll_list_item.setVisibility(View.GONE);
                                                        helper.getView(com.ovit.app.map.R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                // 展开层详情
                                                            }
                                                        });
                                                    }
                                                };
                                                AiRunnable.Ok(callbackToLjz, fs);
                                                return null;
                                            }
                                        }

                                );
                            }
                        }
                        ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
                    }

                });

            }
        };

        ll_list_item.setTag(adapter);
        adapter.adpter(ll_list_item);

        LayerConfig config = LayerConfig.get(table);
        String where = "ZDDM='" + getZddm() + "'";
        MapHelper.Query(table, where, config.getCol_orderby(), config.col_sort, 0, true, fs, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                adapter.notifyDataSetChanged();
                return null;
            }
        });

        return ll_view;
    }

    // list 相关
    private View getAllQlrAndHjxx(final MapInstance mapInstance, Feature f_zd, final List<Feature> qlr_feature_list) {
        final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
        final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
        final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
        if (ll_view.getChildCount() > 0) {
            ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
        }

        FeatureTable table = mapInstance.getTable("GYRXX");
        final List<Feature> fs = new ArrayList<Feature>();

        final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, fs) {
            @Override
            protected void convert(BaseAdapterHelper helper, final Feature item) {
                final com.ovit.app.map.view.FeatureView fv_gyr = mapInstance.newFeatureView(item.getFeatureTable());
                initSelectList(mapInstance, helper, item, 0, qlr_feature_list);
                //展开LJZ
                final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                ll_list_item.setVisibility(View.GONE);
                helper.getView(com.ovit.app.map.R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                        if (!flag) {
                            final List<Feature> fs = new ArrayList<>();
                            fv_gyr.queryChildFeature("HJXX", item, fs, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            final com.ovit.app.map.view.FeatureView fv_hjxx = mapInstance.newFeatureView("HJXX");
                                            QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, fs) {
                                                @Override
                                                protected void convert(BaseAdapterHelper helper, final Feature item) {
                                                    initSelectList(mapInstance, helper, item, 2, qlr_feature_list);
                                                    //展开户
                                                    final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                                                    ll_list_item.setVisibility(View.GONE);
                                                    helper.getView(com.ovit.app.map.R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                        }
                                                    });

                                                }
                                            };
                                            ll_list_item.setTag(adapter);
                                            adapter.adpter(ll_list_item);
                                            return null;
                                        }
                                    }
                            );
                        }
                        ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
                    }

                });
            }
        };

        ll_list_item.setTag(adapter);
        adapter.adpter(ll_list_item);

        LayerConfig config = LayerConfig.get(table);
        MapHelper.Query(table, StringUtil.WhereByIsEmpty(getOrid()) + " ORID_PATH like '%" + getOrid() + "%' ", config.getCol_orderby(), config.col_sort, 0, true, fs, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                adapter.notifyDataSetChanged();
                return null;
            }
        });


        return ll_view;
    }

    public void update_ftxs(final AiRunnable callback) {

        List<Feature> fs = new ArrayList<>();
        queryFeature(mapInstance.getTable(FeatureHelper.TABLE_NAME_FTQK), "FTQX_ID='" + fv.getOrid() + "'", fs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiRunnable.Ok(callback, t_, objects);
                return null;
            }
        });
    }

    ///endregion

    //region 内部类或接口
    ///endregion

    //region 宗地代码相关
    //  获取最大的编号
    public void getMaxZddm(final AiRunnable callback) {
        getMaxZddm(getTzm(), callback);
    }

    public void getMaxZddm(final String tzm, final AiRunnable callback) {
        getDjzq(new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String djzq = (String) t_;
                final String id = djzq + tzm;
                MapHelper.QueryMax(table, StringUtil.WhereByIsEmpty(id) + " ZDDM like '" + id + "_____' ", FeatureHelper.TABLE_ATTR_ZDDM, id.length(), 0, id + "00000", callback);
                return null;
            }
        });
    }

    //  获取新的宗地编码
    public void newZddm(final AiRunnable callback) {
        final String zddm_old = getZddm();
        newZddm(zddm_old, getTzm(zddm_old), callback);
    }

    public void newZddm(final String oldZddm, String tzm, final AiRunnable callback) {

        getMaxZddm(tzm, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = "";
                if (objects.length > 1) {
                    String maxid = objects[0] + "";
                    if (maxid.equals(oldZddm)) {
                        id = maxid;
                    } else {
                        // 最大号加1
                        int count = AiUtil.GetValue(objects[1], 0) + 1;
                        id = StringUtil.fill(String.format("%05d", count), maxid, true);
                    }
                }
                AiRunnable.Ok(callback, id);
                return null;
            }
        });
    }

    public static void GetMaxZddm(MapInstance mapInstance, Feature f, AiRunnable callback) {
        From(mapInstance, f).getMaxZddm(callback);
    }

    ///endregion

    //region FeatureViewZD基础方法
    public String getTzm() {
        return getTzm(getZddm());
    }

    public String getTzm(String zddm) {
        String tzm = StringUtil.substr_last(zddm, 2, 5).toUpperCase();
        List<String> tzms = Arrays.asList("JC,JB,GB".split(","));
        if (tzms.contains(tzm)) {
            return tzm;
        }
        return tzms.get(0);
    }

    public String GetID(Feature feature) {
        return AiUtil.GetValue(feature.getAttributes().get(FeatureHelper.TABLE_ATTR_ZDDM), "");
    }

    public void loadByZddm(String zddm, AiRunnable callback) {
        MapHelper.QueryOne(table, StringUtil.WhereByIsEmpty(zddm) + " ZDDM like '%" + zddm + "%' ", callback);
    }

    public void loadByOrid(String orid, AiRunnable callback) {
        MapHelper.QueryOne(table, StringUtil.WhereByIsEmpty(orid) + " ORID like '%" + orid + "%' ", callback);
    }

    // 加载权利人
    public void loadQlrByZd(AiRunnable callback) {
        MapHelper.QueryOne(mapInstance.getTable("GYRXX"), StringUtil.WhereByIsEmpty(getOrid()) + " ORID_PATH like '%" + getOrid() + "%' ", callback);
    }

    public void createFeature(final AiRunnable callback) {
        createFeature(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZD).createFeature(), callback);
    }

    public void createFeature(final Feature feature, final AiRunnable callback) {
        final List<Feature> fs_update = new ArrayList<>();
        final FeatureViewZD fv = From(mapInstance, feature);
        // 绘图
        fv.drawAndAutoCompelet(feature, fs_update, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 设置新的zddm
                fv.newZddm(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String id = t_ + "";
                        FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ZDDM, id);
                        // 填充
                        fv.fillFeature(feature);
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

    public static void CreateFeature(final MapInstance mapInstance, final AiRunnable callback) {
        CreateFeature(mapInstance, mapInstance.getTable(FeatureHelper.TABLE_NAME_ZD).createFeature(), callback);
    }

    public static void CreateFeature(final MapInstance mapInstance, final Feature feature, final AiRunnable callback) {
        final List<Feature> fs_update = new ArrayList<>();
        final FeatureViewZD fv = From(mapInstance, feature);
        // 绘图
        fv.drawAndAutoCompelet(feature, fs_update, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 设置新的zddm
                fv.newZddm(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String id = t_ + "";
                        FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ZDDM, id);
                        // 填充
                        fv.fillFeature(feature);
                        fs_update.add(feature);
                        // 保存
                        MapHelper.saveFeature(fs_update, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                // 返回显示
                                AiRunnable.Ok(callback, feature);
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

    //region 宗地草图，房产图
    public String getPathZdct_dxf() {
        return FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/宗地草图/") + "宗地草图.dxf";
    }

    public String getPathZdct_jpg() {
        return FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/宗地草图/") + "宗地草图.jpg";
    }

    public String getPathFct() {
        return FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/房产图/") + "房产图.jpg";
    }

    // 出宗地草图 dxf
    public void loadZdct_Dxf(final com.ovit.app.map.model.MapInstance mapInstance, final Feature f_zd, List<Feature> fs_zd
            , List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h_fsjg
            , List<Feature> fs_jzd, List<Feature> fs_zj_x,  List<Feature> fs_zj_d
            ,List<Feature> fs_xzdw, List<Feature> fs_mzdw,List<Feature> fs_dzdw,List<Feature> fs_fsss) {
        try {
            final String dxfpath_zdt = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/宗地草图/") + FeatureHelper.Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, "") + "宗地图.dxf";
            if (DxfHelper.TYPE == DxfHelper.TYPE_NEIMENG) {
                final String dxfpath_zdct = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/宗地草图/") + FeatureHelper.Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, "") + "宗地草图.dxf";
                new DxfZdct(mapInstance).set(dxfpath_zdt).set(f_zd, fs_zd, fs_zrz, fs_z_fsjg, fs_h_fsjg, fs_jzd).write().save();
            } else {
//                new DxfZdct(mapInstance).set(dxfpath_zdt).set(f_zd, fs_zd, fs_zrz, fs_z_fsjg, fs_h_fsjg, fs_jzd).write().save();
                new DxfZdt_badong(mapInstance).set(dxfpath_zdt).set(f_zd, fs_zd, fs_zrz, fs_z_fsjg, fs_h_fsjg, fs_jzd, fs_zj_x, fs_zj_d, fs_xzdw, fs_mzdw, fs_dzdw,fs_fsss).write().save();
            }
        } catch (Exception es) {
            Log.e(TAG, "生成分层分户图失败");
        }
    }

    public void loadZdct(boolean reload, final AiRunnable callback) {
        try {
            final String filename = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/宗地草图/") + "宗地草图.jpg";
            if ((!reload) && FileUtils.exsit(filename)) {
                AiRunnable.Ok(callback, filename);
            } else if (!FeatureHelper.isPolygonFeatureValid(feature)) {
                CrashHandler.WriteLog("出宗地图异常", "宗地图形数据异常请检查：编号："
                        + FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ZDDM, "")
                        + "权利人" + FeatureHelper.Get(feature, "QLRXM", ""));
                AiRunnable.Ok(callback, filename);
            } else {
                final List<Feature> features_jzd = new ArrayList<>();
                loadJzds(mapInstance, feature, features_jzd, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        MapHelper.geometry_ct(mapInstance.map, feature.getGeometry(), "", new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                final AiRunnable runnable = (AiRunnable) t_;
                                final Envelope e = (Envelope) objects[0];
                                final GraphicsOverlay glayer = (GraphicsOverlay) objects[1];
                                final List<Layer> layers = (List<Layer>) objects[2];
                                final int scale = (Integer) objects[4];
                                final List<Feature> fs_zd = new ArrayList<Feature>();
                                final List<Feature> fs_zrz = new ArrayList<Feature>();
                                final List<Feature> fs_ljz = new ArrayList<Feature>();
                                final List<Feature> fs_fsss = new ArrayList<Feature>();
                                final List<Feature> fs_h_fsjg = new ArrayList<Feature>();
                                final List<Feature> fs_z_fsjg = new ArrayList<Feature>();
                                final List<Feature> fs_zj_x = new ArrayList<Feature>();
                                final List<Feature> fs_zj_d = new ArrayList<Feature>();
                                final List<Feature> fs_mzdw = new ArrayList<Feature>();
                                final List<Feature> fs_xzdw = new ArrayList<Feature>();
                                final List<Feature> fs_dzdw = new ArrayList<Feature>();
                                final Geometry g = GeometryEngine.bufferGeodetic(feature.getGeometry(), 1, MapHelper.U_L, 0.01, MapHelper.GC);
                                // 范围内所有宗地
                                final double buffer = DxfHelper.getZdctBuffer();
                                MapHelper.Query(mapInstance.map, FeatureHelper.TABLE_NAME_ZD, g, buffer, fs_zd, new AiRunnable(runnable) {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        // 范围内所有自然幢
                                        MapHelper.Query(mapInstance.map, FeatureHelper.TABLE_NAME_ZRZ, g, buffer, fs_zrz, new AiRunnable(runnable) {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                MapHelper.Query(mapInstance.map, FeatureHelper.TABLE_NAME_LJZ, g, buffer, fs_ljz, new AiRunnable(runnable) {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        MapHelper.Query(mapInstance.map, FeatureHelper.TABLE_NAME_FSSS, g, buffer, fs_fsss, new AiRunnable(runnable) {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                MapHelper.Query(mapInstance.map, FeatureHelper.TABLE_NAME_H_FSJG, g, buffer, fs_h_fsjg, new AiRunnable(runnable) {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        // 范围内所有幢附属
                                                        MapHelper.Query(mapInstance.map, FeatureHelper.TABLE_NAME_Z_FSJG, g, buffer, fs_z_fsjg, new AiRunnable(runnable) {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                MapHelper.Query(mapInstance.map, "BZ_X", g, buffer, fs_zj_x, new AiRunnable(runnable) {
                                                                    @Override
                                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                                        MapHelper.Query(mapInstance.map, "BZ_D", g, buffer, fs_zj_d, new AiRunnable(runnable) {
                                                                            @Override
                                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                        MapHelper.Query(mapInstance.map, "MZDW", g, buffer, fs_mzdw, new AiRunnable(runnable) {
                                                                            @Override
                                                                            public <T_> T_ ok(T_ t_, Object... objects) {

                                                                                MapHelper.Query(mapInstance.map, "XZDW", g, buffer, fs_xzdw, new AiRunnable(runnable) {
                                                                                        @Override
                                                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                            MapHelper.Query(mapInstance.map, "DZDW", g, buffer, fs_dzdw, new AiRunnable(runnable) {
                                                                                                @Override
                                                                                                public <T_> T_ ok(T_ t_, Object... objects) {

                                                                                        List<Geometry> lablePoints = new ArrayList<>();
                                                                                        String jxlx = FeatureHelper.Get(feature, "JXLX", "J");
                                                                                        if (true) {
                                                                                            for (Feature feature : features_jzd) {
                                                                                                feature.getAttributes().put("JZDH", jxlx + (features_jzd.indexOf(feature) + 1));
                                                                                            }
                                                                                        }
                                                                                        //生成dxf
                                                                                        loadZdct_Dxf(mapInstance, feature, fs_zd, fs_zrz, fs_z_fsjg, fs_h_fsjg, features_jzd, fs_zj_x,fs_zj_d, fs_xzdw, fs_mzdw,fs_dzdw,fs_fsss);
                                                                                        //这些图层是要隐藏的
                                                                                        List<Layer> ls = MapHelper.getLayers(mapInstance.map, FeatureHelper.TABLE_NAME_ZD, FeatureHelper.TABLE_NAME_ZRZ,"LJZ", "FSSS","JZD", "JZX", FeatureHelper.TABLE_NAME_Z_FSJG, FeatureHelper.TABLE_NAME_H, FeatureHelper.TABLE_NAME_H_FSJG, "KZD");
                                                                                        for (Layer l : ls) {
                                                                                            if (l.isVisible()) {
                                                                                                l.setVisible(false);
                                                                                                layers.add(l);
                                                                                            }
                                                                                        }
                                                                                        // 加粗绘制当前图形
                                                                                        Geometry g = feature.getGeometry();
                                                                                        TextSymbol symbol_t_ = new TextSymbol(7, "", Color.BLACK, TextSymbol.HorizontalAlignment.CENTER, TextSymbol.VerticalAlignment.MIDDLE);
                                                                                        symbol_t_.setAngleAlignment(MarkerSymbol.AngleAlignment.MAP);
                                                                                        glayer.getGraphics().clear();

                                                                                        for (Feature f : fs_zd) {
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                continue;
                                                                                            }
                                                                                            if (!FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ZDDM, "").equals(FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ZDDM, ""))) {
                                                                                                glayer.getGraphics().add(new Graphic(f.getGeometry(), new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 1f)));
                                                                                            }
                                                                                        }
                                                                                        glayer.getGraphics().add(new Graphic(g, new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2)));

                                                                                        for (Feature f : fs_z_fsjg) {
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                continue;
                                                                                            }
                                                                                            glayer.getGraphics().add(new Graphic(f.getGeometry(), new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.BLUE, 0.8f)));

                                                                                        }

                                                                                        for (Feature f : fs_h_fsjg) {
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                continue;
                                                                                            }
                                                                                            glayer.getGraphics().add(new Graphic(f.getGeometry(), new SimpleLineSymbol(SimpleLineSymbol.Style.DASH_DOT, Color.BLUE, 0.8f)));
                                                                                        }

                                                                                        for (Feature f : fs_z_fsjg) {
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                continue;
                                                                                            }
                                                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
                                                                                            textSymbol.setColor(Color.GREEN);
                                                                                            textSymbol.setSize(6);
                                                                                            textSymbol.setText(mapInstance.getLabel(f));
                                                                                            glayer.getGraphics().add(new Graphic(GeometryEngine.labelPoint((Polygon) f.getGeometry()), textSymbol));
                                                                                        }

                                                                                        for (Feature f : fs_zd) {
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                CrashHandler.WriteLog("出宗地图异常", "宗地图形异常：编号："
                                                                                                        + FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ZDDM, "")
                                                                                                        + "权利人" + FeatureHelper.Get(f, "QLRXM", ""));
                                                                                                continue;
                                                                                            }
                                                                                            Geometry intersectionGeometry = GeometryEngine.intersection(e, f.getGeometry());
                                                                                            if (intersectionGeometry == null || MapHelper.getArea(intersectionGeometry) < 0.0001d) {
                                                                                                continue;
                                                                                            }
                                                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
                                                                                            textSymbol.setColor(Color.RED);
                                                                                            textSymbol.setSize(8);
                                                                                            textSymbol.setFontDecoration(TextSymbol.FontDecoration.UNDERLINE);
                                                                                            textSymbol.setText(mapInstance.getLabel(f));
                                                                                            float x_deviation = 0f;
                                                                                            Point p = GeometryEngine.labelPoint((Polygon) intersectionGeometry);
                                                                                            lablePoints.add(intersectionGeometry);

                                                                                            if (DxfHelper.TYPE == DxfHelper.TYPE_BADONG) {
                                                                                                String qlrxm = FeatureHelper.Get(f, "QLRXM", "");
                                                                                                String zddm_f = FeatureHelper.Get(feature, "PRO_ZDDM_F", "");
                                                                                                x_deviation = textSymbol.getSize() * (zddm_f.length() * 1.5f) * scale / 10000;
                                                                                                // 权利人姓名
                                                                                                TextSymbol textSymbol2 = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
                                                                                                textSymbol2.setColor(Color.RED);
                                                                                                textSymbol2.setSize(8);
                                                                                                textSymbol2.setFontDecoration(TextSymbol.FontDecoration.UNDERLINE);
                                                                                                textSymbol2.setText(qlrxm);
                                                                                                glayer.getGraphics().add(new Graphic(new Point(p.getX() - x_deviation, p.getY(), p.getSpatialReference()), textSymbol2));
                                                                                            }
                                                                                            glayer.getGraphics().add(new Graphic(new Point(p.getX() + x_deviation, p.getY(), p.getSpatialReference()), textSymbol));
                                                                                        }

                                                                                        // 宗地四至
                                                                                        float offset = 20;
                                                                                        Envelope e_z = feature.getGeometry().getExtent();
                                                                                        double g_t_x = e_z.getXMax() - e_z.getXMin();
                                                                                        double g_t_y = e_z.getYMax() - e_z.getYMin();
                                                                                        double g_t = g_t_x > g_t_y ? g_t_x : g_t_y;
                                                                                        // 范围扩大一倍
                                                                                        Envelope e_zs = GeometryEngine.buffer(e_z, g_t / 10).getExtent();

                                                                                        TextSymbol symbol_sz = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
                                                                                        symbol_sz.setSize(symbol_sz.getSize());
                                                                                        SpatialReference sp = mapInstance.map.getSpatialReference();
                                                                                        String zdsz = FeatureHelper.Get(feature, "ZDSZD", "");
                                                                                        if (StringUtil.IsNotEmpty(zdsz)) {
                                                                                            Point p_mid = MapHelper.point_getMidPoint(new Point(e_zs.getXMax(), e_zs.getYMin(), sp), new Point(e_zs.getXMax(), e_zs.getYMax(), sp), sp);
                                                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_sz.toJson());
                                                                                            textSymbol.setText(zdsz);
                                                                                            textSymbol.setOffsetX(offset);
                                                                                            textSymbol.setAngle(90);
                                                                                            glayer.getGraphics().add(new Graphic(p_mid, textSymbol));
                                                                                        }
                                                                                        zdsz = FeatureHelper.Get(feature, "ZDSZN", "");
                                                                                        if (StringUtil.IsNotEmpty(zdsz)) {
                                                                                            Point p_mid = MapHelper.point_getMidPoint(new Point(e_zs.getXMin(), e_zs.getYMin(), sp), new Point(e_zs.getXMax(), e_zs.getYMin(), sp), sp);
                                                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_sz.toJson());
                                                                                            textSymbol.setText(zdsz);
                                                                                            // textSymbol.setOffsetY(-offset);
                                                                                            glayer.getGraphics().add(new Graphic(p_mid, textSymbol));
                                                                                        }

                                                                                        zdsz = FeatureHelper.Get(feature, "ZDSZX", "");
                                                                                        if (StringUtil.IsNotEmpty(zdsz)) {
                                                                                            Point p_mid = MapHelper.point_getMidPoint(new Point(e_zs.getXMin(), e_zs.getYMax(), sp), new Point(e_zs.getXMin(), e_zs.getYMin(), sp), sp);
                                                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_sz.toJson());
                                                                                            textSymbol.setText(zdsz);
                                                                                            textSymbol.getHaloWidth();
                                                                                            textSymbol.setOffsetX(-offset);
                                                                                            textSymbol.setAngle(-90);
                                                                                            glayer.getGraphics().add(new Graphic(p_mid, textSymbol));
                                                                                        }
                                                                                        zdsz = FeatureHelper.Get(feature, "ZDSZB", "");
                                                                                        if (StringUtil.IsNotEmpty(zdsz)) {
                                                                                            Point p_mid = MapHelper.point_getMidPoint(new Point(e_zs.getXMax(), e_zs.getYMax(), sp), new Point(e_zs.getXMin(), e_zs.getYMax(), sp), sp);
                                                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_sz.toJson());
                                                                                            textSymbol.setText(zdsz);
                                                                                            glayer.getGraphics().add(new Graphic(p_mid, textSymbol));
                                                                                        }

                                                                                        // 逻辑幢
                                                                                        for (Feature f : fs_ljz) {
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                continue;
                                                                                            }
                                                                                            glayer.getGraphics().add(new Graphic(f.getGeometry(), new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 1)));
                                                                                        }
                                                                                        for (Feature f : fs_ljz) {
                                                                                            // 天门
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                CrashHandler.WriteLog("逻辑幢图形异常",
                                                                                                        "  逻辑幢号：" + FeatureHelper.Get(f, "LJZH", ""));
                                                                                                continue;
                                                                                            }
                                                                                            Geometry intersectionGeometry = GeometryEngine.intersection(e, f.getGeometry());
                                                                                            if (intersectionGeometry == null || MapHelper.getArea(intersectionGeometry) < 0.0001d) {
                                                                                                continue;
                                                                                            }
                                                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
                                                                                            textSymbol.setColor(Color.BLUE);
                                                                                            textSymbol.setHorizontalAlignment(TextSymbol.HorizontalAlignment.LEFT);
                                                                                            textSymbol.setSize(7);
                                                                                            String textLable = mapInstance.getLabel(f, DxfHelper.TYPE);
                                                                                            textSymbol.setText(textLable);
                                                                                            Point p_z_lable = MapHelper.getNiceLablePoint(intersectionGeometry,lablePoints);
                                                                                             if(p_z_lable == null){
                                                                                                 p_z_lable = GeometryEngine.labelPoint((Polygon) intersectionGeometry);
                                                                                             }
                                                                                            glayer.getGraphics().add(new Graphic(p_z_lable, textSymbol));
                                                                                        }
                                                                                        //fsss
                                                                                        for (Feature f : fs_fsss) {
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                continue;
                                                                                            }
                                                                                            glayer.getGraphics().add(new Graphic(f.getGeometry(), new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 1)));
                                                                                        }
                                                                                        for (Feature f : fs_fsss) {
                                                                                            // 天门
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                CrashHandler.WriteLog("逻辑幢图形异常",
                                                                                                        "  逻辑幢号：" + FeatureHelper.Get(f, "LJZH", ""));
                                                                                                continue;
                                                                                            }
                                                                                            Geometry intersectionGeometry = GeometryEngine.intersection(e, f.getGeometry());
                                                                                            if (intersectionGeometry == null || MapHelper.getArea(intersectionGeometry) < 0.0001d) {
                                                                                                continue;
                                                                                            }
                                                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
                                                                                            textSymbol.setColor(Color.BLUE);
                                                                                            textSymbol.setHorizontalAlignment(TextSymbol.HorizontalAlignment.LEFT);
                                                                                            textSymbol.setSize(7);
                                                                                            String textLable = mapInstance.getLabel(f, DxfHelper.TYPE);
                                                                                            textSymbol.setText(textLable);
//                                                                                            Point p_z_lable = MapHelper.getNiceLablePoint(intersectionGeometry,lablePoints);
                                                                                             glayer.getGraphics().add(new Graphic(GeometryEngine.labelPoint((Polygon) intersectionGeometry), textSymbol));
                                                                                        }

                                                                                        if (g instanceof Multipoint) {
                                                                                            //  不做操作
                                                                                        } else if (g instanceof Multipart) {
                                                                                            int jzd_i = 1;
                                                                                            for (ImmutablePart part : ((Multipart) g).getParts()) {

                                                                                                List<Point> ps = new ArrayList<Point>();
                                                                                                for (Point p : part.getPoints()) {
                                                                                                    ps.add(p);
                                                                                                }
                                                                                                if (ps.size() > 1) {
                                                                                                    if (MapHelper.geometry_equals(ps.get(0), ps.get(ps.size() - 1))) {
                                                                                                        // 首位相同 移除
                                                                                                        ps.remove(ps.size() - 1);
                                                                                                    }
                                                                                                    for (int i = 0; i < ps.size(); i++) {
                                                                                                        Point p_per = ps.get((i < 1 ? ps.size() : i) - 1);
                                                                                                        Point p = ps.get(i);
                                                                                                        Point p_next = ps.get((i < (ps.size() - 1) ? i : -1) + 1);

                                                                                                        // 添加长度标识
                                                                                                        Graphic graphic_mid = MapHelper.geometry_get_mid_label(mapInstance.map, p, p_next, symbol_t_, 6);
                                                                                                        glayer.getGraphics().add(graphic_mid);
                                                                                                    }

                                                                                                    for (int i = 0; i < ps.size(); i++) {
                                                                                                        Point p_per = ps.get((i < 1 ? ps.size() : i) - 1);
                                                                                                        Point p = ps.get(i);
                                                                                                        Point p_next = ps.get((i < (ps.size() - 1) ? i : -1) + 1);
                                                                                                        String label = jxlx + (jzd_i++);
                                                                                                        boolean isBriefCode = true;
                                                                                                        if (!true) {
                                                                                                            for (Feature f_jzd : features_jzd) {
                                                                                                                if (MapHelper.geometry_equals(f_jzd.getGeometry(), p)) {
                                                                                                                    label = AiUtil.GetValue(f_jzd.getAttributes().get("JZDH"), label);
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                        // 添加拐点标识
                                                                                                        GeodeticDistanceResult distance_per = MapHelper.geometry_distanceGeodetic(p_per, p, MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
                                                                                                        GeodeticDistanceResult distance_next = MapHelper.geometry_distanceGeodetic(p, p_next, MapHelper.U_L, MapHelper.U_A, MapHelper.GC);

                                                                                                        CompositeSymbol symbol_jzd = new CompositeSymbol();
                                                                                                        SimpleMarkerSymbol symbol_point = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, ColorUtil.get("#ffffff"), 4);
                                                                                                        symbol_point.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 1));
                                                                                                        symbol_jzd.getSymbols().add(symbol_point);
                                                                                                        TextSymbol symbol_text = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
                                                                                                        symbol_text.setColor(Color.RED);
                                                                                                        symbol_text.setText(label);
                                                                                                        MapHelper.geometry_get_vertex_label(distance_per, distance_next, symbol_text, 10);
                                                                                                        symbol_jzd.getSymbols().add(symbol_text);
                                                                                                        glayer.getGraphics().add(new Graphic(p, symbol_jzd));

                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        AiRunnable.Ok(runnable, null);
                                                                                        return null;
                                                                                    }
                                                                                });
                                                                                return null;
                                                                            }
                                                                        });
                                                                        return null;
                                                                    }
                                                                });     return null;
                                                            }
                                                        });
                                                                return null;
                                                            }
                                                        });
                                                        return null;
                                                    }
                                                });      return null;
                                                    }
                                                });      return null;
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
                        }, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                Bitmap bitmap = (Bitmap) t_;
                                int scale_ = (int) objects[0];
                                feature.getAttributes().put("GLBLC", "1:" + scale_);
                                if (bitmap != null) {
                                    try {
                                        FileUtils.writeFile(filename, ConvertUtil.convert(bitmap));
                                        MapImage.getZoomBitmap(filename, filename,1000,1000);
                                        AiRunnable.Ok(callback, filename, objects);
                                        return null;
                                    } catch (Exception es) {
                                        ToastMessage.Send("生成宗地草图失败！", es);
                                    } finally {
                                        ImageUtil.recycle(bitmap);
                                    }
                                }
                                AiRunnable.No(callback, null);
                                return null;
                            }
                        });
                        return null;
                    }
                });
            }
        } catch (Exception es) {
            Log.e(TAG, "load_zdct: 绘制宗地图错误", es);
        }
    }

    // 生成房产图
    public void loadFct(boolean reload, final AiRunnable callback) {
        final String filename = getPathFct();
        if ((!reload) && FileUtils.exsit(filename)) {
            AiRunnable.Ok(callback, filename);
        } else {
            final List<Feature> fs_zrz = new ArrayList<Feature>();
            loadZrzs(fs_zrz, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    loadFct(filename, fs_zrz);
                    AiRunnable.Ok(callback, filename);
                    return null;
                }
            });
        }
    }

    public void loadFct(String path, final List<Feature> fs_zrz) {
        Bitmap bitmap = loadFct(fs_zrz);
        if (bitmap != null) {
            try {
                FileUtils.writeFile(path, ConvertUtil.convert(bitmap));
            } catch (Exception es) {
                ToastMessage.Send("生成房产图失败！", es);
            }
            ImageUtil.recycle(bitmap);
        }
    }

    public Bitmap loadFct(final List<Feature> zrz) {
        Feature zd = feature;
        List<Geometry> gs = MapHelper.geometry_get(zrz);
        gs.add(feature.getGeometry());
        Envelope extent = GeometryEngine.combineExtents(gs);
        int w = 600;
        int h = 600;
        // 白色画布
        MapImage img = new MapImage(extent, w, h, 30, 50, 30, 30).draw(Color.WHITE);
//      img.setColor(Color.BLUE).setSw(3).draw(feature,mapInstance.getLabel(feature));
        img.setColor(Color.BLACK).setSw(1);
        for (Feature f : zrz) {
            String zh = AiUtil.GetValue(f.getAttributes().get("ZH"), 1) + "";
            String fwjg = DicUtil.dic(mapInstance.activity, "fwjg", AiUtil.GetValue(f.getAttributes().get("FWJG"), ""));
            if (fwjg.contains("[") && fwjg.contains("]")) {
                fwjg = StringUtil.substr(fwjg, fwjg.indexOf("[") + 1, fwjg.indexOf("]"));
            }
            String zcs = AiUtil.GetValue(f.getAttributes().get("ZCS"), 1) + "";
            img.draw(f, "(" + zh + ")\r\n" + fwjg + " " + zcs, 18);
        }

        img.setColor(Color.RED).setSw(2);
        img.draw(zd, "", 2);

        if (!License.check()) {
            // 水印
            Bitmap bm_sy = ImageUtil.getBitmap(mapInstance.map.getContext(), R.mipmap.ovit_sy);
            img.draw(bm_sy, w / 3, h / 3, w / 3, h / 3);
        }
        Bitmap bitmap = img.getValue();
        img.set((Bitmap) null);
        if (bitmap != null) {
            return bitmap;
        }
        return null;
    }

    // 房屋分层图
    public void loadFwfct(final List<String> fct, final AiRunnable callback) {
        final List<Feature> fs = new ArrayList<>();
        final List<Feature> fs_off = new ArrayList<>();
        LoadZ_H_And_Fsjg(mapInstance, feature, fs, fs, fs_off, fs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final ArrayList<Map.Entry<String, List<Feature>>> cs = FeatureEditC.GroupbyC_Sort(fs);
                boolean pcClose = AppConfig.PHSZ_PC_CLOSE.equals(AppConfig.get(AppConfig.APP_BDCQJDC_PHSZ_PC, AppConfig.PHSZ_PC_CLOSE));
                final FwPc pc = pcClose ? null : new FwPc();
                AiRunnable run = new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
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

    // 草图和分层图
    public void loadCtAddFct(final AiRunnable callback) {
        String id = getZddm();
        if (StringUtil.IsEmpty(id)) {
            AiRunnable.Ok(callback, null);
            return;
        }
        loadFct(true, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                loadZdct(true, callback);
                return null;
            }
        });
    }

    // 查询所有宗地，创建宗地草图与房产图
    public static void LaodAllZDCreateCTAddFCT(final MapInstance mapInstance, final AiRunnable callback) {

        final List<Feature> fs = new ArrayList<>();
        LoadAllZD(mapInstance, fs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final FeatureViewZD fv = From(mapInstance);
                // 递归执行
                new AiForEach<Feature>(fs, callback) {
                    @Override
                    public void exec() {
                        fv.set(fs.get(postion));
                        fv.loadCtAddFct(getNext());
                    }
                }.start();
                return null;
            }
        });
    }
    ///endregion

    //region 界址点线，界址签章
    public void loadJzds(final com.ovit.app.map.model.MapInstance mapInstance, final Feature feature, final List<Feature> jzds, final AiRunnable callback) {
        final List<Feature> features_jzd = new ArrayList<>();
        final FeatureLayer layer_jzd = MapHelper.getLayer(mapInstance.map, "JZD", "界址点");//"ZRZH like '" + id + "____'"
        String id = GetID(feature);
        MapHelper.Query(layer_jzd.getFeatureTable(), StringUtil.WhereByIsEmpty(id) + "ZDZHDM like'" + "%" + id + "%" + "' ", -1, features_jzd, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Map<String, Feature> map_jzd = new HashMap<>();
                for (Feature f_jzd : features_jzd) {
                    String key = FeatureEditJZD.GetKey((Point) f_jzd.getGeometry());
                    map_jzd.put(key, f_jzd);
                }
                Geometry g = feature.getGeometry();
                if (g instanceof Multipart) {
                    for (ImmutablePart part : ((Multipart) g).getParts()) {
                        for (Point p : part.getPoints()) {
                            String key = FeatureEditJZD.GetKey(p);
                            if (map_jzd.containsKey(key)) {
                                jzds.add(map_jzd.get(key));
                            }
                        }
                    }
                    AiRunnable.Ok(callback, jzds);
                }
                return null;
            }
        });
    }

    public static String GetZdzhdm_LZ(String zddm, String zdzhdm1, String zdzhdm2) {
        String lz = "";
        if (StringUtil.IsEmpty(zdzhdm1) || zdzhdm1.equalsIgnoreCase(zddm) || StringUtil.IsEmpty(zdzhdm2) || zdzhdm2.equalsIgnoreCase(zddm)) {
            lz = "";
        } else {
            List<String> zdzhdm_ls1 = new ArrayList<>(Arrays.asList(zdzhdm1.split("/")));
            List<String> zdzhdm_ls2 = new ArrayList<>(Arrays.asList(zdzhdm2.split("/")));
            List<String> zdzhdm_ls = new ArrayList<>();
            for (String zdzhdm : zdzhdm_ls1) {
                if ((!zdzhdm.equalsIgnoreCase(zddm)) && zdzhdm_ls2.contains(zdzhdm)) {
                    zdzhdm_ls.add(zdzhdm);
                }
            }
            lz = StringUtil.Join(zdzhdm_ls, "/", false);
        }
        return lz;
    }

    public static String GetZdzhdm_LZ(String zddm, Feature f_jzd1, Feature f_jzd2) {
        String zdzhdm1 = FeatureHelper.Get(f_jzd1, "ZDZHDM", "");
        String zdzhdm2 = FeatureHelper.Get(f_jzd2, "ZDZHDM", "");
        return GetZdzhdm_LZ(zddm, zdzhdm1, zdzhdm2);
    }
    ///endregion

    //region 宗地识别自然幢
    public void identyZrz(List<Feature> features_zrz, final AiRunnable callback) {
        MapHelper.Query(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ), feature.getGeometry(), features_zrz, callback);
    }

    // 默认识别保存
    public void identyZrz(final AiRunnable callback) {
        identyZrz(false, callback);
    }

    // 识别显示结果返回
    public void identyZrz(final boolean isShow, final AiRunnable callback) {
        final List<Feature> fs_zrz = new ArrayList<>();
        identyZrz(fs_zrz, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final FeatureViewZRZ fv_zrz = FeatureViewZRZ.From(mapInstance);
                if (isShow) {
                    fv_zrz.fs_ref = ListUtil.asList(feature);
                    QuickAdapter<Feature> adapter = fv_zrz.getListAdapter(fs_zrz, 0);
                    AiDialog dialog = AiDialog.get(mapInstance.activity, adapter);
                    dialog.setHeaderView(R.mipmap.app_map_layer_zrz, "识别到" + fs_zrz.size() + "个自然幢");
                    dialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fv_zrz.fillFeature(fs_zrz, feature);
                            identyZrzFromZD(mapInstance, feature, fs_zrz, callback);
                            dialog.dismiss();
                        }
                    });
                } else {
                    fv_zrz.fillFeature(fs_zrz, feature);
                    identyZrzFromZD(mapInstance, feature, fs_zrz, callback);
                }
                return null;
            }
        });
    }

    public void identyZrzFromZD(com.ovit.app.map.model.MapInstance mapInstance, Feature f_zd, List<Feature> features_zrz, final AiRunnable callback) {
        double area_jzmj = 0;
        double area_jzzdmj = 0;
        String zddm = FeatureHelper.Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, "");
        final List<Feature> features_update = new ArrayList<>();
        int maxZH = 0;
        double zd_area = MapHelper.getArea(mapInstance, f_zd.getGeometry());
        for (Feature f : features_zrz) {
            final String z_zrzh = AiUtil.GetValue(f.getAttributes().get("ZRZH"), "");
            if (z_zrzh.startsWith(zddm + "F") && z_zrzh.length() == 24) {
                // 自然幢号有效
                int zh = AiUtil.GetValue(z_zrzh.substring(20), 0);
                if (maxZH < zh) {
                    maxZH = zh;
                }
            } else {
                // 自然幢号无效需要自己编
                features_update.add(f);
            }
        }
        // 专门来更新自然幢号的问题
        if (features_update.size() > 0) {
            for (Feature updateFeature : features_update) {
                maxZH++;
                String newZH = String.format("%04d", maxZH);
                updateFeature.getAttributes().put("ZRZH", zddm + "" + "F" + newZH);
                updateFeature.getAttributes().put("ZH", maxZH + "");
                mapInstance.fillFeature(updateFeature, f_zd);
            }
        }
        // 来更新其他字段内容
        for (Feature f : features_zrz) {
            double z_zcs = AiUtil.GetValue(f.getAttributes().get("ZCS"), 1d);
            double z_area = MapHelper.getArea(mapInstance, f.getGeometry());
            double z_scjzmj = AiUtil.GetValue(f.getAttributes().get("SCJZMJ"), 0d);
            // 如果建筑面积小于占地面积
            if (z_scjzmj < z_area * 0.5 || (z_zcs > 1 && z_scjzmj <= (z_area + 0.1))) {
                z_scjzmj = z_area * z_zcs;
            }
            area_jzmj += z_scjzmj;
            area_jzzdmj += z_area;

            f.getAttributes().put("SCJZMJ", AiUtil.Scale(z_scjzmj, 2));
            f.getAttributes().put("ZCS", z_zcs);
        }
        f_zd.getAttributes().put("JZMJ", AiUtil.Scale(area_jzmj, 2));
        f_zd.getAttributes().put("JZZDMJ", AiUtil.Scale(area_jzzdmj, 2));
        f_zd.getAttributes().put(FeatureHelper.TABLE_ATTR_ZDMJ, AiUtil.Scale(zd_area, 2));
        features_update.clear();
        if (StringUtil.IsNotEmpty(zddm)) {
            features_update.addAll(features_zrz);
        }
//        features_update.add(f_zd);
        MapHelper.saveFeature(f_zd, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                MapHelper.saveFeature(features_update, callback);
                return null;
            }
        });

    }

    // 查询所有宗地 识别幢
    public static void LaodAllZD_IdentyZrz(final MapInstance mapInstance, final AiRunnable callback) {
        final List<Feature> fs = new ArrayList<>();
        LoadAllZD(mapInstance, fs, new AiRunnable(callback) {

            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final FeatureViewZD fv = FeatureViewZD.From(mapInstance);
                // 递归执行
                new AiForEach<Feature>(fs, callback) {
                    @Override
                    public void exec() {
                        fv.set(fs.get(postion));
                        fv.identyZrz(getNext());
                    }
                }.start();
                return null;
            }
        });
    }
    ///endregion

    //region 不动产单元设定
    // 设立不动产单元
    public void createNewBDCDY(Feature f_zd, final AiRunnable callback) {

        final List<Feature> selected_feature_list = new ArrayList<>(); //用于存放选中的feature

        final AiDialog dialog = getBindBDC_View(mapInstance, f_zd, selected_feature_list);

        if (dialog != null) {
            dialog.setFooterView(AiDialog.CENCEL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    ToastMessage.Send("您取消了绑定不动产！");
                    dialog.dismiss();
                }
            }, "", null, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    List<Feature> fs_zrz = null;
                    if (selected_feature_list.size() > 0) {
                        for (Feature f : selected_feature_list) {
                            String tableName = f.getFeatureTable().getTableName();
                            if (FeatureHelper.TABLE_NAME_ZRZ.equals(tableName)) {
                                if (fs_zrz == null) {
                                    fs_zrz = new ArrayList<>();
                                }
                                fs_zrz.add(f);

                            } else if (FeatureHelper.TABLE_NAME_ZRZ_C.equals(tableName)) {
                                // 宗地与层设定不动产单元，只能与某一层设定
                                if (selected_feature_list.size() > 1) {
                                    ToastMessage.Send("设定不动产单元失败，不能与多层设定一个不动产单元，请重新选择。");
                                    dialog.dismiss();
                                    return;
                                }
                                newBdcdyToC(f, callback);
                                dialog.dismiss();
                                return;

                            } else if (FeatureHelper.TABLE_NAME_H.equals(tableName)) {
                                // 宗地与户设定不动产单元，只能与某一户（套）设定
                                if (selected_feature_list.size() > 1) {
                                    ToastMessage.Send("设定不动产单元失败，不能与多户设定一个不动产单元，请重新选择。");
                                    dialog.dismiss();
                                    return;
                                }
                                newBdcdyToH(f, callback);
                                dialog.dismiss();
                                return;
                            }
                        }
                        newBdcdyToZrz(fs_zrz, callback);
                        dialog.dismiss();

                    } else {

                        if (true) {
                            // 宗地没有房屋 宗地单独设为 一个不动产单元
                        }
                        ToastMessage.Send("设定不动产单元失败，未选择要素。");
                    }
                }
            });
        } else {
            ToastMessage.Send("获得不动产列表失败!");
        }
    }

    // 通过宗地与自然幢设定不动产单元
    public void newBdcdyToZrz(final List<Feature> fs_zrz, final AiRunnable callback) {
        final List<Feature> fs_upt = new ArrayList<>();
        try {
            FeatureEditQLR.CreateFeature(mapInstance, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    final Feature featureBdcdy = (Feature) t_;
                    String bdcdyh = FeatureViewQLR.GetBdcdyhFromFeature(fs_zrz, getZddm());
                    fillFeatureBdcdy(featureBdcdy, feature);

                    featureBdcdy.getAttributes().put(FeatureHelper.TABLE_ATTR_BDCDYH, bdcdyh);
                    //TODO 拷贝附件材料需要谨慎处理附件数据建议放到不动产单元
                    capyAttachments(feature, featureBdcdy);// 拷贝附件材料

                    FeatureHelper.Set(fs_zrz, FeatureHelper.TABLE_ATTR_BDCDYH, bdcdyh);
                    fs_upt.addAll(fs_zrz);
                    fs_upt.add(featureBdcdy);

                    MapHelper.saveFeature(fs_upt, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            AiRunnable.Ok(callback, featureBdcdy);
                            return null;
                        }
                    });
                    return null;
                }
            });

        } catch (Exception es) {
            Log.e(TAG, "通过与自然幢设定不动产单元!" + es);
        }
    }

    public void createBdcdy(Feature feature, final AiRunnable callback) {
        final List<Feature> fs = new ArrayList<>();
        fv.queryChildFeature(FeatureConstants.ZRZ_TABLE_NAME, feature, fs, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                newBdcdyToZrz(fs, callback);
                return null;
            }
        });
    }
    ///endregion

    //region 面积计算
    // 核算宗地 占地面积、建筑面积
    public void update_Area(final AiRunnable callback) {
        final List<Feature> fs_zrz = new ArrayList<>();
        final List<Feature> fs_z_fsjg = new ArrayList<>();
        final List<Feature> fs_h = new ArrayList<>();
        final List<Feature> fs_h_fsjg = new ArrayList<>();
        final List<Feature> update_fs = new ArrayList<>();
        String id = getZddm();
        if (StringUtil.IsEmpty(id)) {
            AiRunnable.Ok(callback, null);
            return;
        }
        LoadZ_H_And_Fsjg(mapInstance, feature, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg, new AiRunnable(callback) {
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
                    FeatureViewH.hsmj(f, mapInstance, fs_h_fsjg);
                    update_fs.add(f);
                }
                for (Feature f : fs_zrz) {
                    FeatureViewZRZ.From(mapInstance, f).update_Area(f, fs_h, fs_h_fsjg,fs_z_fsjg);
                    update_fs.add(f);
                }
                hsmj(fs_zrz);
                update_fs.add(feature);
                MapHelper.saveFeature(update_fs, callback);
                return null;
            }
        });
    }

    public void hsmj(List<Feature> f_zrzs) {
        String orid = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID, "");
        Geometry g = feature.getGeometry();
        double area = 0d;
        double hsmj = 0d;
        double jzzdmj = 0d;
        if (g != null) {
            area = MapHelper.getArea(mapInstance, g);
        }

        for (Feature f : f_zrzs) {
            String orid_parent = FeatureHelper.GetLastOrid(f);
            if (orid.equals(orid_parent)) {
                double f_hsmj = FeatureHelper.Get(f, "SCJZMJ", 0d);
                double f_jzzdmj = FeatureHelper.Get(f, "ZZDMJ", 0d);
                hsmj += f_hsmj;
                jzzdmj += f_jzzdmj;
            }
        }
        if (Math.abs(hsmj - area) < 0.05) {
            hsmj = area;
        }
        FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ZDMJ, AiUtil.Scale(area, 2));
        FeatureHelper.Set(feature, "JZZDMJ", AiUtil.Scale(jzzdmj, 2));
        FeatureHelper.Set(feature, "JZMJ", AiUtil.Scale(hsmj, 2));
    }

    // 加载所有的宗地、核算其面积
    public static void LaodAllZDAndUpdateArea(final MapInstance mapInstance, final AiRunnable callback) {
        final List<Feature> fs = new ArrayList<>();
        LoadAllZD(mapInstance, fs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final FeatureViewZD fv = From(mapInstance);
                // 递归执行
                new AiForEach<Feature>(fs, callback) {
                    @Override
                    public void exec() {
                        fv.set(fs.get(postion));
                        fv.update_Area(getNext());
                    }
                }.start();
                return null;
            }
        });
    }
    ///endregion

    //region 生成资料
    public void createDOCX(final MapInstance mapInstance, final Feature featureBdcdy, final boolean isRelaod, final AiRunnable callback) {
        final String bdcdyh = FeatureEditQLR.GetBdcdyh(featureBdcdy);
        String file_dcb_doc = FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh);
        if (FileUtils.exsit(file_dcb_doc) && !isRelaod) {
            AiRunnable.Ok(callback, file_dcb_doc);
        } else {
            final List<Feature> fs_zd = new ArrayList<>();
            loadZd(mapInstance, featureBdcdy, fs_zd, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    final Feature f_zd = (Feature) t_;
                    final List<Feature> fs_hjxx = new ArrayList<Feature>();
                    final List<Feature> fs_zrz = new ArrayList<Feature>();
                    final List<Feature> fs_ljz = new ArrayList<Feature>();
                    final List<Feature> fs_c = new ArrayList<Feature>();
                    final List<Feature> fs_z_fsjg = new ArrayList<Feature>();
                    final List<Feature> fs_h = new ArrayList<Feature>();
                    final List<Feature> fs_h_fsjg = new ArrayList<Feature>();
                    final List<Feature> fs_jzd = new ArrayList<Feature>();
                    final List<Feature> fs_jzx = new ArrayList<Feature>();
                    final Map<String, Feature> map_jzx = new HashMap<>();
                    final List<Map<String, Object>> fs_jzqz = new ArrayList<>();

                    List<String> orids = FeatureHelper.GetOridsFormOridPath(featureBdcdy, FeatureHelper.TABLE_NAME_ZD);
                    String where = "";
                    if (orids != null && orids.size() > 0) {
                        for (String orid : orids) {
                            if (orids.indexOf(orid) == 0) {
                                where += " ORID_PATH like '%" + orid + "%' ";
                            } else {
                                where += " OR ORID_PATH like '%" + orid + "%' ";
                            }
                        }
                    }

                    loadall(mapInstance, bdcdyh, featureBdcdy,fs_hjxx, f_zd, fs_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_ljz, fs_c, fs_z_fsjg, fs_h, fs_h_fsjg, where, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            createDOCX(mapInstance, bdcdyh,featureBdcdy, f_zd,fs_hjxx, fs_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_ljz, fs_c, fs_z_fsjg, fs_h, fs_h_fsjg, isRelaod, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    outputData(mapInstance, bdcdyh,featureBdcdy, f_zd, fs_jzd, fs_jzx, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg);
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
        }
    }

    private void createDOCX(final MapInstance mapInstance, final String bdcdyh, final Feature f_bdcdy, final Feature f_zd, final List<Feature> fs_hjxx, final List<Feature> fs_zd,
                            final List<Feature> fs_jzd, final List<Feature> fs_jzx, final Map<String, Feature> map_jzx,
                            final List<Map<String, Object>> fs_jzqz, final List<Feature> fs_zrz, List<Feature> fs_ljz
            , final List<Feature> fs_c, final List<Feature> fs_z_fsjg, final List<Feature> fs_h, List<Feature> fs_h_fsjg, boolean isRelaod, final AiRunnable callback) {

        {
            final String file_dcb_doc = FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh);
            if (FileUtils.exsit(file_dcb_doc) && !isRelaod) {
                Log.i(TAG, "生成资料: 已经存在跳过");
                AiRunnable.Ok(callback, file_dcb_doc);
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String zddm = FeatureHelper.Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, "");
                            final Map<String, Object> map_ = new LinkedHashMap<>();
                            //  设置系统参数
                            FeatureEditBDC.Put_data_sys(map_);
                            //  设置宗地参数
                            FeatureEditBDC.Put_data_zd(mapInstance, map_, bdcdyh, f_zd);
                            FeatureEditBDC.Put_data_hjxx(mapInstance, map_,fs_hjxx);
                            // 界址签字
                            FeatureEditBDC.Put_data_jzqz(map_, fs_jzd, fs_jzqz);
                            // 界址点
                            FeatureEditBDC.Put_data_jzdx(mapInstance, map_, zddm, fs_jzd, fs_jzx, map_jzx);

                            FeatureEditBDC.Put_changsha_jzd(map_,fs_jzd,map_jzx);

                            // 设置界址线
                            FeatureEditBDC.Put_data_jzx(mapInstance, map_, fs_jzx);
                            // 自然幢
                            FeatureEditBDC.Put_data_zrz(mapInstance, map_, bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h, fs_c);
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
                            put_data_zd(map_, fs_zd);


                            final ArrayList<String> files = new ArrayList<String>();
                            File file = new File(FileUtils.getAppDirAndMK(GetPath_Templet()));
                            File[] tempList = file.listFiles();
                            for (int i = 0; i < tempList.length; i++) {
                                if (tempList[i].toString().endsWith(".docx")) {
                                    files.add(tempList[i].toString());
                                }
                            }

                            new AiForEach<String>(files, null) {
                                @Override
                                public void exec() {
                                    String file1 = files.get(postion);
                                    String mbID = StringUtil.substr(file1, file1.lastIndexOf("/") + 1, file1.lastIndexOf("."));
                                    final String file_dcb_doc = GetPath_doc(mapInstance, bdcdyh, mbID, f_bdcdy);
                                    String file_zd_zip = GetPath_ZD_zip(mapInstance, f_zd);
                                    if (FileUtils.exsit(file1)) {
                                        ReportUtils.exportWord(file1, file_dcb_doc, map_);
                                        // 资料已经发生改变，移除压缩包
                                        FileUtils.deleteFile(file_zd_zip);
                                        Log.i(TAG, "生成资料: 生成完成");
                                        AiRunnable.U_Ok(mapInstance.activity, callback, file_dcb_doc, map_);
                                    } else {
                                        ToastMessage.Send("《不动产权籍调查表》模板文件不存在！");
                                        AiRunnable.U_No(mapInstance.activity, callback, null);
                                    }
                                    AiRunnable.Ok(getNext(), null);
                                }
                            }.start();
                        } catch (Exception es) {
                            Log.i(TAG, "生成资料: 生成失败");
                            ToastMessage.Send("生成《不动产权籍调查表》失败", es);
                            AiRunnable.U_No(mapInstance.activity, callback, null);
                        }
                    }
                }).start();
            }
        }

    }

    private void put_data_zd(Map<String, Object> map_, List<Feature> fs_zd) {
        if (fs_zd.size() > 1) {
            double zdmj = 0.0d;
            double jzmj = 0.0d;
            double jzzdmj = 0.0d;
            for (Feature feature : fs_zd) {
                zdmj += FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ZDMJ, 0.00d);
                jzzdmj += FeatureHelper.Get(feature, "JZZDMJ", 0.00d);
                jzmj += FeatureHelper.Get(feature, "JZMJ", 0.00d);
            }
            map_.put("ZD.ZDMJ", zdmj);
            map_.put("ZD.JZMJ", jzmj);
            map_.put("ZD.JZZDMJ", jzzdmj);
        }
    }

    private void loadall(final MapInstance mapInstance, final String orid, final Feature featureBdcdy
            , final List<Feature> fs_hjxx, Feature f_zd, List<Feature> fs_zd, List<Feature> fs_jzd, List<Feature> fs_jzx
            , Map<String, Feature> map_jzx, List<Map<String, Object>> fs_jzqz
            , final List<Feature> fs_zrz, final List<Feature> fs_ljz, final List<Feature> fs_c
            , final List<Feature> fs_z_fsjg, final List<Feature> fs_h
            , final List<Feature> fs_h_fsjg, final String where, final AiRunnable callback) {
        FeatureEditBDC.LoadJZDXQZ(mapInstance, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final FeatureView fv = mapInstance.newFeatureView(feature);

                MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZRZ), StringUtil.WhereByIsEmpty(orid) + where, FeatureHelper.TABLE_ATTR_ZRZH, "asc", -1, fs_zrz, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_LJZ), StringUtil.WhereByIsEmpty(orid) + where, FeatureHelper.TABLE_ATTR_LJZH, "asc", -1, fs_ljz, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZRZ_C), StringUtil.WhereByIsEmpty(orid) + where,"SJC", "asc", -1, fs_c, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_H), StringUtil.WhereByIsEmpty(orid) + where, "ID", "asc", -1, fs_h, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_Z_FSJG), StringUtil.WhereByIsEmpty(orid) + where, "ID", "asc", -1, fs_z_fsjg, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_H_FSJG), StringUtil.WhereByIsEmpty(orid) + where, "ID", "asc", -1, fs_h_fsjg, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        String orid_hjxx = mapInstance.getOrid(featureBdcdy);
                                                        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_HJXX), StringUtil.WhereByIsEmpty(orid_hjxx) + " ORID_PATH like '%" + orid_hjxx + "%' ",  -1, fs_hjxx, new AiRunnable() {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                AiRunnable.Ok(callback, t_, objects);
                                                                return super.ok(t_, objects);
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
                        return null;
                    }
                });
                return null;
            }
        });
    }

    private void loadZd(MapInstance mapInstance, Feature f_bdcdy, final List<Feature> fs, final AiRunnable callback) {

        List<String> orids = FeatureHelper.GetOridsFormOridPath(f_bdcdy, FeatureHelper.TABLE_NAME_ZD);
        final String bdcdyh = FeatureHelper.Get(f_bdcdy, FeatureHelper.TABLE_ATTR_BDCDYH, "");
        if (orids != null && orids.size() > 0) {
            String where = "";
            for (String orid : orids) {
                if (orids.indexOf(orid) == 0) {
                    where += "orid = '" + orid + "'";
                } else {
                    where += " OR orid='" + orid + "'";
                }
            }

            queryFeature(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZD), where, fs, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    Feature f_zz = null;
                    for (Feature f : fs) {
                        String zddm = FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ZDDM, "");
                        if (StringUtil.IsNotEmpty(zddm) && bdcdyh.contains(zddm)) {
                            f_zz = f;
                            break;
                        }
                    }
                    AiRunnable.Ok(callback, f_zz, objects);
                    return null;
                }
            });
        }
    }

    private void outputData(final MapInstance mapInstance,
                            final String bdcdyh,
                            final Feature f_bdcdy,
                            final Feature f_zd,
                            final List<Feature> fs_jzd,
                            final List<Feature> fs_jzx,
                            final List<Feature> fs_zrz,
                            final List<Feature> fs_z_fsjg,
                            final List<Feature> fs_h,
                            final List<Feature> fs_h_fsjg) {
        try {
            final String file_dcb = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + "不动产权籍调查表" + bdcdyh + ".docx";
            FileUtils.copyFile(FeatureEditBDC.GetPath_doc(mapInstance, bdcdyh,"不动产权籍调查表",f_bdcdy), file_dcb);
            // 导出shp 文件
            final String shpfile_zd = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + FeatureHelper.LAYER_NAME_ZD + ".shp";
            ShapeUtil.writeShp(shpfile_zd, f_zd);
            final String shpfile_jzd = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + "界址点" + ".shp";
            ShapeUtil.writeShp(shpfile_jzd, fs_jzd);
            final String shpfile_jzx = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + "界址线" + ".shp";
            ShapeUtil.writeShp(shpfile_jzx, fs_jzx);
            final String shpfile_zrz = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + FeatureHelper.LAYER_NAME_ZRZ + ".shp";
            ShapeUtil.writeShp(shpfile_zrz, fs_zrz);
            final String shpfile_h = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + FeatureHelper.LAYER_NAME_H + ".shp";
            ShapeUtil.writeShp(shpfile_h, fs_h);
            final String shpfile_zfsjg = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + FeatureHelper.LAYER_NAME_Z_FSJG + ".shp";
            ShapeUtil.writeShp(shpfile_zfsjg, fs_z_fsjg);
            final String shpfile_hfsjg = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + FeatureHelper.LAYER_NAME_H_FSJG + ".shp";
            ShapeUtil.writeShp(shpfile_hfsjg, fs_h_fsjg);

            String dxf_bdcdyh = bdcdyh;
            if (DxfHelper.TYPE == DxfHelper.TYPE_JINSAN) {
                // 京山 十堰 郧阳 冀保书
                final String dxf_fcfht = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + dxf_bdcdyh + "分层分户图.dxf";// fs_zrz =0
                new DxfFcfwh_jinshan(mapInstance).set(dxf_fcfht).set(dxf_bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg).write().save();
            } else if (DxfHelper.TYPE == DxfHelper.TYPE_BADONG) {
                // 巴东 王总
                final String dxf_fcfht_enshi = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + dxf_bdcdyh + "房产分户图.dxf";// fs_zrz =0
                new DxfFcfht_badong(mapInstance).set(dxf_fcfht_enshi).set(dxf_bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg).write().save();
            } else if (DxfHelper.TYPE == DxfHelper.TYPE_TIANMEN || DxfHelper.TYPE == DxfHelper.TYPE_HONGHU|| DxfHelper.TYPE == DxfHelper.TYPE_LIZHI) {
                // 天门 乔向阳
                final String dxf_fcfht_tianmen = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + dxf_bdcdyh + "房产分层平面图.dxf";// fs_zrz =0
//                new DxfFcfct_tianmen(mapInstance).set(dxf_fcfht_tianmen).set(dxf_bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg).write().save();

                new DxfFcfct_tianmen(mapInstance).set(dxf_fcfht_tianmen).set(dxf_bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg,null,fs_jzd).write().save();
            } else if (DxfHelper.TYPE == DxfHelper.TYPE_XIANAN) {
                // 咸安
                final String dxf_fc_xianan = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + dxf_bdcdyh + "房屋分层平面图.dxf";// fs_zrz =0
                new DxfFcfct_xianan(mapInstance).set(dxf_fc_xianan).set(dxf_bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg).write().save();
                final String dxf_fcfht_xianan = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + dxf_bdcdyh + "房产图.dxf";// fs_zrz =0
                new DxfFct_xianan(mapInstance).set(dxf_fcfht_xianan).set(dxf_bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg).write().save();
            } else {
                final String dxf_fcfht_tianmen = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + dxf_bdcdyh + "房产分层平面图.dxf";// fs_zrz =0
                new DxfZdctDefult(mapInstance).set(dxf_fcfht_tianmen).set(f_zd).save();
            }
        } catch (Exception es) {
            Log.e(TAG, "导出数据失败", es);
        }
    }

    public void setSign(TextView tv_sign, final ImageView iv_sign, final String signDirPath, final String name) {

        if (tv_sign != null && iv_sign != null) {
            final String signName = AiUtil.GetValue(tv_sign.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
            final String signPath = FileUtils.getAppDirAndMK(signDirPath + "/" + signName + "签章举证/") + signName +name+ "电子签章.jpg";

            if (FileUtils.exsit(signPath)) {
                iv_sign.setVisibility(View.VISIBLE);
                SignActivity.LoadImg(activity, signPath, iv_sign);
            }
            tv_sign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SignActivity.Sign(activity, signPath, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            if ((Integer) t_ == 1) {
                                if (iv_sign.getVisibility() != View.VISIBLE) {
                                    iv_sign.setVisibility(View.VISIBLE);
                                }
                                SignActivity.LoadImg(activity, signPath, iv_sign);
                            }
                            String path = FileUtils.getAppDirAndMK(signDirPath + "/" + signName + "签章举证/")  + signName +name+ "电子签章.jpg";
                            MapImage.getZoomBitmap(signPath, path,480,240);
                            return null;
                        }
                    });
                }
            });
        }

    }


    ///endregion

}