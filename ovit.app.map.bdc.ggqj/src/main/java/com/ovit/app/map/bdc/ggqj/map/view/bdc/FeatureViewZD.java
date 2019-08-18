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

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeodeticDistanceResult;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.ImmutablePart;
import com.esri.arcgisruntime.geometry.Multipart;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.Point;
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
import com.ovit.app.map.bdc.ggqj.map.model.DxfZdct;
import com.ovit.app.map.bdc.ggqj.map.model.DxfZdct_badong;
import com.ovit.app.map.bdc.ggqj.map.model.DxfZdt_badong;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.LayerConfig;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.FwPc;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.AppConfig;
import com.ovit.app.util.ColorUtil;
import com.ovit.app.util.ConvertUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.ImageUtil;
import com.ovit.app.util.ListUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewZD extends FeatureView {
    final static String TAG = "FeatureViewZD";
    final public static String TABLE_ATTR_FTXS_ZD = "MPHM";


    @Override
    public void onCreate() {
        super.onCreate();
        iconColor = Color.RED;
    }

    // 填充
    @Override
    public void fillFeature(Feature feature) {
        super.fillFeature(feature);
        String zddm = FeatureHelper.Get(feature, "ZDDM", "");// todo .. 当地级子区有误时 zddm 会存问题。006026
        if (zddm.length() != 19) {
            if (StringUtil.IsNotEmpty(zddm) && !zddm.contains("JC") && !zddm.contains("JB") && !zddm.contains("GB")) {
                // 宗地代码不包含特征码
                zddm = StringUtil.substr(zddm, 0, zddm.length() - 5) + "JC" + StringUtil.substr_last(zddm, 5);
            }
            if (zddm.length() != 19) {
                String xmbm = getXmbm();
                zddm = StringUtil.substr(xmbm, 0, 19 - zddm.length()) + zddm;
            }
            FeatureHelper.Set(feature, "ZDDM", zddm);
            FeatureHelper.Set(feature, "YBZDDM", zddm);
        }
//        FeatureHelper.Set(feature, "YBZDDM", zddm, true, false);
        FeatureHelper.Set(feature, "PRO_ZDDM_F", StringUtil.substr_last(zddm, 7));

        String bdcdyh = FeatureHelper.Get(feature, "BDCDYH", "");
        if ((zddm + "F00000000").equals(bdcdyh) || (zddm + "F99990001").equals(bdcdyh)) {
            // 不动产单元有效
        } else {
            if (bdcdyh.endsWith("F99990001")) {
                bdcdyh = zddm + "F99990001";
            } else {
                bdcdyh = zddm + "F00000000";
            }
            FeatureHelper.Set(feature, "BDCDYH", bdcdyh);
        }
        FeatureHelper.Set(feature, "PZYT", "072", true, false);
        FeatureHelper.Set(feature, "YT", "072", true, false);

        if (feature.getGeometry() != null && 0d == FeatureHelper.Get(feature, "ZDMJ", 0d)) {
            FeatureHelper.Set(feature, "ZDMJ", MapHelper.getArea(feature.getGeometry()));
        }
        int scale = 200;
        FeatureHelper.Set(feature, "GLBLC", "1:" + scale);
    }

    // 菜单控制
    @Override
    public String addActionBus(String groupname) {
        int count = mapInstance.getSelFeatureCount();
        // 根据画宗地推荐
        if (feature != null && feature.getFeatureTable() == table) {
            if (count > 0) {
                mapInstance.addAction(groupname, "权属", R.mipmap.app_map_user, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        to_bdc(feature);
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
        if (feature != null && feature.getFeatureTable() == table) {
            if (count > 0) {
                mapInstance.addAction(groupname, "智能识别", R.mipmap.app_icon_map_znsb, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        znsb(feature);
                    }
                });
            }
        }

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
    // 智能识别
    private void znsb(final Feature f_zd) {
        {
            final String funcdesc = "该功能将逐一对宗地内房屋进行处理："
                    + "\n 1、宗地识别自然幢；"
                    + "\n 2、自然幢识别逻辑幢。";
            License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    final AiDialog aidialog = AiDialog.get(mapInstance.activity);
                    aidialog.setHeaderView(R.mipmap.app_icon_rgzl_blue, "智能处理")
                            .setContentView("注意：属于不可逆操作，请谨慎处理！",funcdesc)
                            .setFooterView("取消", "确定，我要继续", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    // 完成后的回掉
                                    final AiRunnable callback = new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok( T_ t_, Object... objects) {
                                            final List<Feature> fs_zrz= (List<Feature>) t_;
                                            aidialog.addContentView("自然幢，逻辑幢识别成功，您可能需要以下处理：");
                                            aidialog.addContentView(null, "1、自然幢生成层。"
                                                    +"\n 2、逻辑幢快速生成户。"
                                                    + "\n 3、宗地，自然幢，逻辑幢，层，户，附属结构关系建立。"
                                            );

                                            aidialog.setFooterView("继续处理", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(final DialogInterface dialog, int which) {

                                                 new AiForEach<Feature>(fs_zrz, new AiRunnable() {
                                                        @Override
                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                            creatCToZrz(fs_zrz, new AiRunnable() {
                                                                @Override
                                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                                    dialog.dismiss();
                                                                    return null;
                                                                }
                                                            });

                                                            return null;
                                                        }
                                                    }){
                                                        @Override
                                                        public void exec() {
                                                            final List<Feature> fs_ljz=new ArrayList<>();
                                                            MapHelper.Query(mapInstance.getTable("LJZ"), fs_zrz.get(postion).getGeometry(), fs_ljz, new AiRunnable() {
                                                                @Override
                                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                                    initAllFeatureHToLjz(fs_ljz,getNext());
                                                                    return null;
                                                                }
                                                            });

                                                        }
                                                    }.start();

                                                }
                                            },null,null,"完成",null);
                                            return null;
                                        }

                                        @Override
                                        public <T_> T_ no(T_ t_, Object... objects) {
                                            aidialog.addContentView("处理数据失败！");
                                            aidialog.setFooterView(null,"关闭",null);
                                            return  null;
                                        }

                                        @Override
                                        public <T_> T_ error(T_ t_, Object... objects) {
                                            aidialog.addContentView("处理数据异常！");
                                            aidialog.setFooterView(null,"关闭",null);
                                            return  null;
                                        }
                                    };
                                    // 设置不可中断
                                    aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                                    aidialog.setContentView("开始处理数据"+"ZDDM="+FeatureHelper.Get(feature,"ZDDM",""));
                                    aidialog.addContentView(null,AiUtil.GetValue(new Date(),AiUtil.F_TIME)+"查找所有的自然幢幢，并识别自然幢。");
                                    Log.d(TAG, "智能处理:查找所有的逻辑幢，并合成自然幢");
                                    // 查询宗地范围内的逻辑幢
                                    final FeatureViewZD fv_zd = (FeatureViewZD) mapInstance.newFeatureView(f_zd);
                                    final List<Feature> featuresZrz = new ArrayList<>();
                                    MapHelper.Query(mapInstance.getTable("ZRZ"), feature.getGeometry(), featuresZrz, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            {
                                            aidialog.addContentView(null,AiUtil.GetValue(new Date(),AiUtil.F_TIME)+" 宗地识别自然幢");
                                                // zd 识别自然幢;
                                                fv_zd.identyZd_Zrz(mapInstance, feature, featuresZrz, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        // 自然幢识别逻辑幢
                                                        new AiForEach<Feature>(featuresZrz,callback){
                                                            @Override
                                                            public void exec() {
                                                                final Feature featureZrz = getValue();
                                                                FeatureViewZRZ fvZrz = (FeatureViewZRZ) mapInstance.newFeatureView(featureZrz);
                                                                final AiForEach<Feature> that=this;
                                                                fvZrz.identyLjz(false, new AiRunnable() {
                                                                    @Override
                                                                    public <T_> T_ ok(final T_ t_, Object... objects) {
                                                                        final List<Feature> featuresLjz = (List<Feature>) t_;
                                                                        featureZrz.getAttributes().put("JZWJBYT", AiUtil.GetValue(featuresLjz.get(0).getAttributes().get("FWYT1"),"")); // 更新自然幢用途
                                                                        Log.i(TAG, "自然幢识别逻辑幢==="+postion);
                                                                        AiRunnable.Ok(that.getNext(),featuresLjz);
                                                                        return null;
                                                                    }
                                                                });
                                                            }

                                                            @Override
                                                            public void complet() {
                                                                AiRunnable.Ok(callback,featuresZrz);
                                                            }
                                                        }.start();

                                                        return null;
                                                    }
                                                });
                                            }
                                            return null;
                                        }
                                    });
                                }
                            }).show();
                    ;

                    return null;
                }
            });
        }
    }

    // 快速生成
    // 成果输出

    public void creatCToZrz(final List<Feature> featuresZRZ, final AiRunnable callback) {
        MapHelper.saveFeature(featuresZRZ, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                new AiForEach<Feature>(featuresZRZ, callback) {
                    @Override
                    public void exec() {
                        FeatureEditC.InitFeatureAll(mapInstance, getValue(),getNext());
                    }
                }.start();
                return null;
            }
        });
    }



    private void initAllFeatureHToLjz(final List<Feature> featuresLjz, final AiRunnable callback) {
        new AiForEach<Feature>(featuresLjz, callback) {
            @Override
            public void exec() {
                Log.d(TAG, "逻辑幢快速生成户==="+this.postion);
                final Feature featureLjz = this.getValue();
                final AiForEach<Feature> that=this;
                // 逻辑幢识别幢附属结构
                FeatureViewZ_FSJG.IdentyLJZ_FSJG(mapInstance, featureLjz, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        FeatureViewH.InitFeatureAll(mapInstance, featureLjz, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                // 户识别户附属结构
                                final List<Feature> featuresH = (List<Feature>) t_;
                                new AiForEach<Feature>(featuresH, that.getNext()) {
                                    @Override
                                    public void exec() {
                                        final Feature featureH= this.getValue();
                                        final AiForEach<Feature> that_h=this;
                                        Log.i(TAG, "户识别户附属结构==="+featureH.getAttributes().get("ID")+"===="+this.postion);
                                        {
                                            FeatureEditH_FSJG.IdentyH_FSJG_(mapInstance,featureH, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    FeatureEditH.IdentyH_Area(mapInstance,featureH, new AiRunnable() {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                ToastMessage .Send(activity, "识别户附属结构识别完成！");
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
            }
        }.start();
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
                    queryChildFeature("ZRZ", item, fs, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            com.ovit.app.map.view.FeatureView fv_c = mapInstance.newFeatureView("ZRZ");
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
                MapHelper.QueryMax(table, StringUtil.WhereByIsEmpty(id) + " ZDDM like '" + id + "_____' ", "ZDDM", id.length(), 0, id + "00000", callback);
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

    public void loadByZddm(String zddm, AiRunnable callback) {
        MapHelper.QueryOne(table, StringUtil.WhereByIsEmpty(zddm) + " ZDDM like '%" + zddm + "%' ", callback);
    }
    // 加载权利人
    public void loadQlrByZd(AiRunnable callback) {
        MapHelper.QueryOne(mapInstance.getTable("GYRXX"), StringUtil.WhereByIsEmpty(getOrid()) + " ORID_PATH like '%" + getOrid() + "%' ", callback);
    }

    public void identyZrz(List<Feature> features_zrz, final AiRunnable callback) {
        MapHelper.Query(mapInstance.getTable("ZRZ"), feature.getGeometry(), features_zrz, callback);
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
                final FeatureViewZRZ fv_ = FeatureViewZRZ.From(mapInstance);
                if (isShow) {
                    fv_.fs_ref = ListUtil.asList(feature);
                    QuickAdapter<Feature> adapter = fv_.getListAdapter(fs_zrz, 0);
                    AiDialog dialog = AiDialog.get(mapInstance.activity, adapter);
                    dialog.setHeaderView(R.mipmap.app_map_layer_zrz, "识别到" + fs_zrz.size() + "个自然幢");
                    dialog.setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fv_.fillFeature(fs_zrz, feature);
                            identyZd_Zrz(mapInstance, feature, fs_zrz, callback);
                            dialog.dismiss();
                        }
                    });
                } else {
                    fv_.fillFeature(fs_zrz, feature);
                    identyZd_Zrz(mapInstance, feature, fs_zrz, callback);
                }
                return null;
            }
        });
    }

    public void identyZd_Zrz(com.ovit.app.map.model.MapInstance mapInstance, Feature f_zd, List<Feature> features_zrz, final AiRunnable callback) {
        double area_jzmj = 0;
        double area_jzzdmj = 0;
        String zddm = FeatureHelper.Get(f_zd, "ZDDM", "");
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
        f_zd.getAttributes().put("ZDMJ", AiUtil.Scale(zd_area, 2));
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

    public String getPathZdct_dxf() {
        return FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/宗地草图/") + "宗地草图.dxf";
    }

    public String getPathZdct_jpg() {
        return FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/宗地草图/") + "宗地草图.jpg";
    }

    // 出宗地草图 dxf
    public void loadZdct_Dxf(final List<Feature> fs) {

    }
    public void loadZdct_Dxf(final com.ovit.app.map.model.MapInstance mapInstance, final Feature f_zd, List<Feature> fs_zd
            , List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h_fsjg
            , List<Feature> fs_jzd, List<Feature> fs_zj_x, List<Feature> fs_xzdw, List<Feature> fs_mzdw){
        try {
            final String dxfpath_zdt = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/宗地草图/") + FeatureHelper.Get(f_zd, "ZDDM", "") + "宗地图.dxf";
            if (DxfHelper.TYPE==DxfHelper.TYPE_NEIMENG) {
                final String dxfpath_zdct = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/宗地草图/") + FeatureHelper.Get(f_zd, "ZDDM", "") + "宗地草图.dxf";
                new DxfZdct(mapInstance).set(dxfpath_zdt).set(f_zd, fs_zd, fs_zrz, fs_z_fsjg, fs_h_fsjg, fs_jzd).write().save();
            }else {
                new DxfZdct(mapInstance).set(dxfpath_zdt).set(f_zd, fs_zd, fs_zrz, fs_z_fsjg, fs_h_fsjg, fs_jzd).write().save();
            }
        }catch (Exception es){
            Log.e(TAG, "生成分层分户图失败");
        }
    }


    // 出宗地草图
//    public void loadZdct(boolean reload, final AiRunnable callback) {
//        try {
//            final String filename = getPathZdct_jpg();
//
//            if ((!reload) && FileUtils.exsit(filename)) {
//                AiRunnable.Ok(callback, filename);
//            } else {
//
//                MapHelper.geometry_ct(mapInstance.map, feature.getGeometry(), "", new AiRunnable() {
//                    @Override
//                    public <T_> T_ ok(T_ t_, Object... objects) {
//                        final AiRunnable runnable = (AiRunnable) t_;
//                        final Envelope e = (Envelope) objects[0];
//                        final GraphicsOverlay glayer = (GraphicsOverlay) objects[1];
//                        final List<Layer> layers = (List<Layer>) objects[2];
//                        final List<Feature> fs_jzd = new ArrayList<Feature>();
//                        final List<Feature> fs_zd = new ArrayList<Feature>();
//                        final List<Feature> fs_zrz = new ArrayList<Feature>();
//                        final List<Feature> fs_z_fsjg = new ArrayList<Feature>();
//                        final Geometry g = GeometryEngine.bufferGeodetic(feature.getGeometry(), 1, MapHelper.U_L, 0.01, MapHelper.GC);
//
//                        // 范围内所有宗地
//                        MapHelper.Query(mapInstance.map, "ZD", g, 0.55, fs_zd, new AiRunnable(runnable) {
//                            @Override
//                            public <T_> T_ ok(T_ t_, Object... objects) {
//                                // 范围内所有自然幢
//                                loadZrzs(fs_zrz, new AiRunnable(runnable) {
//                                    @Override
//                                    public <T_> T_ ok(T_ t_, Object... objects) {
//                                        // 范围内所有幢附属
//                                        loadZ_FSJGs(fs_z_fsjg, new AiRunnable(runnable) {
//                                            @Override
//                                            public <T_> T_ ok(T_ t_, Object... objects) {
//                                                // 范围内所有界址点
//                                                loadJzds(fs_jzd, new AiRunnable(runnable) {
//                                                    @Override
//                                                    public <T_> T_ ok(T_ t_, Object... objects) {
//                                                        final List<Feature> fs_all = new ArrayList<Feature>();
//                                                        fs_all.add(feature);
//                                                        fs_all.addAll(fs_z_fsjg);
//                                                        fs_all.addAll(fs_zrz);
//                                                        fs_all.addAll(fs_jzd);
//                                                        //生成dxf
//                                                        loadZdct_Dxf(fs_all);
//                                                        LoadZdct_Dxf(mapInstance, feature, fs_zd, fs_zrz, fs_z_fsjg, fs_h_fsjg, features_jzd, fs_zj_x, fs_xzdw, fs_mzdw);
//
//                                                        //这些图层是要隐藏的
//                                                        List<Layer> ls = MapHelper.getLayers(mapInstance.map, "ZD", "ZRZ", "JZD", "JZX", "Z_FSJG", "H", "KZD");
//
//                                                        for (Layer l : ls) {
//                                                            if (l.isVisible()) {
//                                                                l.setVisible(false);
//                                                                layers.add(l);
//                                                            }
//                                                        }
//                                                        // 加粗绘制当前图形
//                                                        Geometry g = feature.getGeometry();
//
//                                                        TextSymbol symbol_t_ = new TextSymbol(7, "", Color.BLACK, TextSymbol.HorizontalAlignment.CENTER, TextSymbol.VerticalAlignment.MIDDLE);
//                                                        symbol_t_.setAngleAlignment(MarkerSymbol.AngleAlignment.MAP);
//
//                                                        glayer.getGraphics().clear();
//                                                        for (Feature f : fs_zd) {
//                                                            glayer.getGraphics().add(new Graphic(f.getGeometry(), new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 1.5f)));
//                                                        }
//
//                                                        glayer.getGraphics().add(new Graphic(g, new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2)));
//
//                                                        for (Feature f : fs_zrz) {
//                                                            glayer.getGraphics().add(new Graphic(f.getGeometry(), new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 1)));
//                                                        }
//                                                        for (Feature f : fs_z_fsjg) {
//                                                            glayer.getGraphics().add(new Graphic(f.getGeometry(), new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GREEN, 0.5f)));
//                                                        }
//
//                                                        for (Feature f : fs_z_fsjg) {
//                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
//                                                            textSymbol.setColor(Color.GREEN);
//                                                            textSymbol.setSize(6);
//                                                            textSymbol.setText(mapInstance.getLabel(f));
//                                                            glayer.getGraphics().add(new Graphic(GeometryEngine.labelPoint((Polygon) f.getGeometry()), textSymbol));
//                                                        }
//                                                        for (Feature f : fs_zrz) {
//                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
//                                                            textSymbol.setColor(Color.BLUE);
//                                                            textSymbol.setHorizontalAlignment(TextSymbol.HorizontalAlignment.LEFT);
//                                                            textSymbol.setSize(7);
//                                                            textSymbol.setText(mapInstance.getLabel(f));
//                                                            Point p = MapHelper.geometry_labelPoint((Polygon) f.getGeometry(), true);
//                                                            glayer.getGraphics().add(new Graphic(p, textSymbol));
//                                                        }
//                                                        for (Feature f : fs_zd) {
//                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
//                                                            textSymbol.setColor(Color.RED);
//                                                            textSymbol.setSize(8);
//                                                            textSymbol.setFontDecoration(TextSymbol.FontDecoration.UNDERLINE);
//                                                            textSymbol.setText(mapInstance.getLabel(f));
//                                                            glayer.getGraphics().add(new Graphic(GeometryEngine.labelPoint((Polygon) f.getGeometry()), textSymbol));
//                                                        }
//
//                                                        float offset = 20;
//
//                                                        Envelope e_z = feature.getGeometry().getExtent();
//                                                        double g_t_x = e_z.getXMax() - e_z.getXMin();
//                                                        double g_t_y = e_z.getYMax() - e_z.getYMin();
//                                                        double g_t = g_t_x > g_t_y ? g_t_x : g_t_y;
//                                                        // 范围扩大一倍
//                                                        Envelope e_zs = GeometryEngine.buffer(e_z, g_t / 10).getExtent();
//
//                                                        TextSymbol symbol_sz = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
//                                                        symbol_sz.setSize(symbol_sz.getSize() * 2);
//                                                        SpatialReference sp = mapInstance.map.getSpatialReference();
//                                                        String zdsz = FeatureHelper.Get(feature, "ZDSZD", "");
//                                                        if (StringUtil.IsNotEmpty(zdsz)) {
//                                                            Point p_mid = MapHelper.point_getMidPoint(new Point(e_zs.getXMax(), e_zs.getYMin(), sp), new Point(e_zs.getXMax(), e_zs.getYMax(), sp), sp);
//                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_sz.toJson());
//                                                            textSymbol.setText(zdsz);
//                                                            textSymbol.setOffsetX(offset);
//                                                            textSymbol.setAngle(90);
//                                                            glayer.getGraphics().add(new Graphic(p_mid, textSymbol));
//                                                        }
//                                                        zdsz = FeatureHelper.Get(feature, "ZDSZN", "");
//                                                        if (StringUtil.IsNotEmpty(zdsz)) {
//                                                            Point p_mid = MapHelper.point_getMidPoint(new Point(e_zs.getXMin(), e_zs.getYMin(), sp), new Point(e_zs.getXMax(), e_zs.getYMin(), sp), sp);
//                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_sz.toJson());
//                                                            textSymbol.setText(zdsz);
//                                                            // textSymbol.setOffsetY(-offset);
//                                                            glayer.getGraphics().add(new Graphic(p_mid, textSymbol));
//                                                        }
//
//                                                        zdsz = FeatureHelper.Get(feature, "ZDSZX", "");
//                                                        if (StringUtil.IsNotEmpty(zdsz)) {
//                                                            Point p_mid = MapHelper.point_getMidPoint(new Point(e_zs.getXMin(), e_zs.getYMax(), sp), new Point(e_zs.getXMin(), e_zs.getYMin(), sp), sp);
//                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_sz.toJson());
//                                                            textSymbol.setText(zdsz);
//                                                            TextSymbol.FontWeight fontWeight = textSymbol.getFontWeight();
//                                                            textSymbol.getHaloWidth();
//                                                            textSymbol.setOffsetY(-offset);
//                                                            textSymbol.setAngle(-90);
//                                                            glayer.getGraphics().add(new Graphic(p_mid, textSymbol));
//                                                        }
//                                                        zdsz = FeatureHelper.Get(feature, "ZDSZB", "");
//                                                        if (StringUtil.IsNotEmpty(zdsz)) {
//                                                            Point p_mid = MapHelper.point_getMidPoint(new Point(e_zs.getXMax(), e_zs.getYMax(), sp), new Point(e_zs.getXMin(), e_zs.getYMax(), sp), sp);
//                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_sz.toJson());
//                                                            textSymbol.setText(zdsz);
//                                                            glayer.getGraphics().add(new Graphic(p_mid, textSymbol));
//                                                        }
//
//
//                                                        if (g instanceof Multipoint) {
//                                                            //  不做操作
//                                                        } else if (g instanceof Multipart) {
//                                                            int jzd_i = 1;
//                                                            for (ImmutablePart part : ((Multipart) g).getParts()) {
//
//                                                                List<Point> ps = new ArrayList<Point>();
//                                                                for (Point p : part.getPoints()) {
//                                                                    ps.add(p);
//                                                                }
//
//                                                                if (ps.size() > 1) {
//                                                                    ArrayList<Graphic> list_mid = new ArrayList<Graphic>(); //存放选出的长度标识 20180703
//                                                                    for (int i = 0; i < ps.size(); i++) {
//                                                                        Point p_per = ps.get((i < 1 ? ps.size() : i) - 1);
//                                                                        Point p = ps.get(i);
//                                                                        Point p_next = ps.get((i < (ps.size() - 1) ? i : -1) + 1);
//
//                                                                        // 添加长度标识  此处逻辑尚需完善（无法标记弧长）
//                                                                        Graphic graphic_mid = MapHelper.geometry_get_mid_label(mapInstance.map, p, p_next, symbol_t_, 6);
//                                                                        list_mid.add(graphic_mid); //将长度标识先放入集合中 20180703
//                                                                        //glayer.getGraphics().add(graphic_mid);
//                                                                    }
//
//                                                                    //对长度标识进行初步处理 剔除其中相同的标识(弥补容差法的不足) 以解决弧线长度标识反复添加的问题 20180703
//                                                                    double mid_del = 0;  //该变量保存所有被剔除的标识的长度总和 改进算法可参考利用此值
//                                                                    for (int i = 0; i < list_mid.size(); i++) {
//                                                                        Graphic mid_i = list_mid.get(i);
//                                                                        for (int j = i + 1; j < list_mid.size(); j++) {
//                                                                            Graphic mid_j = list_mid.get(j);
//                                                                            if ((new JsonParser().parse(mid_i.getSymbol().toJson()).getAsJsonObject().get("text")).equals(new JsonParser().parse(mid_j.getSymbol().toJson()).getAsJsonObject().get("text"))) {
//                                                                                mid_del += Double.parseDouble(new JsonParser().parse(mid_i.getSymbol().toJson()).getAsJsonObject().get("text").toString().replace("\"", ""));
//                                                                                list_mid.remove(mid_j);
//                                                                                j--;
//                                                                            }
//                                                                        }
//                                                                    }
//
//                                                                    //设置容差 再次处理以剔除初步处理中未去掉的标识 20180703
//                                                                    double rc = 0;  // 容差 20180703
//
//                                                                    for (int i = 0; i < list_mid.size(); i++) {
//                                                                        double t = Double.parseDouble(new JsonParser().parse(list_mid.get(i).getSymbol().toJson()).getAsJsonObject().get("text").toString().replace("\"", ""));
//                                                                        if (rc < t) {
//                                                                            rc = t;
//                                                                        }
//                                                                    }
//
//                                                                    rc = rc * 0.05; //暂定容差系数0.05 弧越平则所需容差越大
//                                                                    for (int i = 0; i < list_mid.size(); i++) {  //依据容差剔除初步处理中遗漏的点
//                                                                        double t = Double.parseDouble(new JsonParser().parse(list_mid.get(i).getSymbol().toJson()).getAsJsonObject().get("text").toString().replace("\"", ""));
//                                                                        if (t < rc) {
//                                                                            mid_del += t;  //该变量保存所有被剔除的标识的长度总和
//                                                                            list_mid.remove(i);
//                                                                            i--;
//                                                                        }
//                                                                    }
//
//                                                                    Log.i(TAG, "所有剔除标识总长度:" + mid_del); //
//
//                                                                    //将处理后的长度标识集合选入到图层显示 20180703
//                                                                    for (int i = 0; i < list_mid.size(); i++) {
//                                                                        Log.i(TAG, "在图上显示的长度标识：" + list_mid.get(i).getSymbol().toJson().toString()); //
//                                                                        glayer.getGraphics().add(list_mid.get(i));
//                                                                    }
//
//                                                                    for (int i = 0; i < ps.size(); i++) {
//                                                                        Point p_per = ps.get((i < 1 ? ps.size() : i) - 1);
//                                                                        Point p = ps.get(i);
//                                                                        Point p_next = ps.get((i < (ps.size() - 1) ? i : -1) + 1);
//
//                                                                        String label = "J" + (jzd_i++);
//                                                                        for (Feature f_jzd : fs_jzd) {
//                                                                            if (MapHelper.geometry_equals(f_jzd.getGeometry(), p)) {
//                                                                                label = AiUtil.GetValue(f_jzd.getAttributes().get("JZDH"), label);
//                                                                            }
//                                                                        }
//
//                                                                        // 依据容差（rc）添加拐点标识 20180706
//                                                                        GeodeticDistanceResult distance_per = MapHelper.geometry_distanceGeodetic(p_per, p, MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
//                                                                        GeodeticDistanceResult distance_next = MapHelper.geometry_distanceGeodetic(p, p_next, MapHelper.U_L, MapHelper.U_A, MapHelper.GC);
//
//                                                                        if (distance_next.getDistance() < rc && distance_per.getDistance() < rc) {  //当前距值和后距值均<rc时，则不在宗地草图中绘制该拐点 20180706
//
//                                                                        } else { //绘制拐点
//                                                                            CompositeSymbol symbol_jzd = new CompositeSymbol();
//                                                                            SimpleMarkerSymbol symbol_point = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, ColorUtil.get("#ffffff"), 4);
//                                                                            symbol_point.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 1));
//                                                                            symbol_jzd.getSymbols().add(symbol_point);
//                                                                            TextSymbol symbol_text = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
//                                                                            symbol_text.setColor(Color.RED);
//                                                                            symbol_text.setText(label);
//                                                                            MapHelper.geometry_get_vertex_label(distance_per, distance_next, symbol_text, 10);
//                                                                            symbol_jzd.getSymbols().add(symbol_text);
//                                                                            glayer.getGraphics().add(new Graphic(p, symbol_jzd));
//                                                                        }
//                                                                        //--------------------------------------------end 20180706----------------------------------------------------------------
//
//                                                                    }
//
//                                                                }
//                                                            }
//                                                        }
//
//                                                        AiRunnable.Ok(runnable, null);
//                                                        return null;
//                                                    }
//                                                });
//                                                return null;
//                                            }
//                                        });
//                                        return null;
//                                    }
//                                });
//                                return null;
//                            }
//                        });
//
//                        return null;
//                    }
//                }, new AiRunnable() {
//                    @Override
//                    public <T_> T_ ok(T_ t_, Object... objects) {
//                        Bitmap bitmap = (Bitmap) t_;
//                        int scale_ = (int) objects[0];
//                        feature.getAttributes().put("GLBLC", "1:" + scale_);
//                        if (bitmap != null) {
//                            try {
//                                FileUtils.writeFile(filename, ConvertUtil.convert(bitmap));
//                                AiRunnable.Ok(callback, filename, objects);
//                                return null;
//                            } catch (Exception es) {
//                                ToastMessage.Send("生成宗地草图失败！", es);
//                            } finally {
//                                ImageUtil.recycle(bitmap);
//                            }
//                        }
//                        AiRunnable.No(callback, null);
//                        return null;
//                    }
//                });
//
//            }
//        } catch (Exception es) {
//            Log.e(TAG, "load_zdct: 绘制宗地图错误", es);
//        }
//    }
    public  void loadZdct( boolean reload, final AiRunnable callback) {
        try {
            final String filename = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/宗地草图/") + "宗地草图.jpg";
            if ((!reload) && FileUtils.exsit(filename)) {
                AiRunnable.Ok(callback, filename);
            } else if (!FeatureHelper.isPolygonFeatureValid(feature)) {
                CrashHandler.WriteLog("出宗地图异常", "宗地图形数据异常请检查：编号："
                        + FeatureHelper.Get(feature, "ZDDM", "")
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
                                final List<Feature> fs_h_fsjg = new ArrayList<Feature>();
                                final List<Feature> fs_z_fsjg = new ArrayList<Feature>();
                                final List<Feature> fs_zj_x = new ArrayList<Feature>();
                                final List<Feature> fs_mzdw = new ArrayList<Feature>();
                                final List<Feature> fs_xzdw = new ArrayList<Feature>();
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
                                                                        MapHelper.Query(mapInstance.map, "MZDW", g, buffer, fs_mzdw, new AiRunnable(runnable) {
                                                                            @Override
                                                                            public <T_> T_ ok(T_ t_, Object... objects) {

                                                                                MapHelper.Query(mapInstance.map, "XZDW", g, buffer, fs_xzdw, new AiRunnable(runnable) {
                                                                                    @Override
                                                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                        String jxlx = FeatureHelper.Get(feature,"JXLX","J");
                                                                                        if (true) {
                                                                                            for (Feature feature : features_jzd) {
                                                                                                feature.getAttributes().put("JZDH", jxlx + (features_jzd.indexOf(feature) + 1));
                                                                                            }
                                                                                        }
                                                                                        //生成dxf
                                                                                        loadZdct_Dxf(mapInstance, feature, fs_zd, fs_zrz, fs_z_fsjg, fs_h_fsjg, features_jzd, fs_zj_x, fs_xzdw, fs_mzdw);
                                                                                        //这些图层是要隐藏的
                                                                                        List<Layer> ls = MapHelper.getLayers(mapInstance.map, FeatureHelper.TABLE_NAME_ZD, FeatureHelper.TABLE_NAME_ZRZ, "JZD", "JZX", FeatureHelper.TABLE_NAME_Z_FSJG, FeatureHelper.TABLE_NAME_H, FeatureHelper.TABLE_NAME_H_FSJG, "KZD");
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
//
                                                                                        glayer.getGraphics().clear();
                                                                                        for (Feature f : fs_zrz) {
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                continue;
                                                                                            }
                                                                                            glayer.getGraphics().add(new Graphic(f.getGeometry(), new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 1)));
                                                                                        }
                                                                                        for (Feature f : fs_zd) {
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                continue;
                                                                                            }
                                                                                            if (!FeatureHelper.Get(feature, "ZDDM", "").equals(FeatureHelper.Get(f, "ZDDM", ""))) {
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
                                                                                        for (Feature f : fs_zrz) {
                                                                                            // 天门
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                CrashHandler.WriteLog("自然幢图形异常",
                                                                                                        "  自然幢号：" + FeatureHelper.Get(f, "ZRZH", ""));
                                                                                                continue;
                                                                                            }
                                                                                            Geometry intersection_fzrz_g = GeometryEngine.intersection(f.getGeometry(), e);
                                                                                            if (intersection_fzrz_g == null || MapHelper.getArea(mapInstance, intersection_fzrz_g) < 0.0001d) {
                                                                                                continue;
                                                                                            }
                                                                                            TextSymbol textSymbol = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
                                                                                            textSymbol.setColor(Color.BLUE);
                                                                                            textSymbol.setHorizontalAlignment(TextSymbol.HorizontalAlignment.LEFT);
                                                                                            textSymbol.setSize(7);
                                                                                            String textLable=mapInstance.getLabel(f,DxfHelper.TYPE);
                                                                                            textSymbol.setText(textLable);
                                                                                            Point p_z_lable = GeometryEngine.labelPoint((Polygon) f.getGeometry());
                                                                                            float x_offset = textSymbol.getSize() * 2f * scale / 10000;
                                                                                            glayer.getGraphics().add(new Graphic(new Point(p_z_lable.getX() + x_offset, p_z_lable.getY(), p_z_lable.getSpatialReference()), textSymbol));

                                                                                            // 幢号  河南
                                                                                            TextSymbol textSymbol1 = (TextSymbol) TextSymbol.fromJson(symbol_t_.toJson());
                                                                                            textSymbol1.setColor(Color.BLUE);
                                                                                            textSymbol1.setHorizontalAlignment(TextSymbol.HorizontalAlignment.CENTER);
                                                                                            textSymbol1.setSize(7);
                                                                                            String label = "";
                                                                                            String zrzh = FeatureHelper.Get(f, "ZRZH", "");
                                                                                            if (StringUtil.IsNotEmpty(zrzh) && zrzh.length() > 4) {
                                                                                                label = "(" + AiUtil.GetValue(zrzh.substring(zrzh.length() - 4), 1) + ") ";
                                                                                            }
                                                                                            textSymbol1.setText(label);
                                                                                            Point lastPoint = MapHelper.Geometry_get(intersection_fzrz_g, DxfHelper.POINT_TYPE_LEFT_BOTTOM);
                                                                                            if (lastPoint != null) {
                                                                                                Geometry buffer = GeometryEngine.buffer(lastPoint, scale / 100d);
                                                                                                Geometry intersectionG = GeometryEngine.intersection(intersection_fzrz_g, buffer);
                                                                                                glayer.getGraphics().add(new Graphic(intersectionG.getExtent().getCenter(), textSymbol1));
                                                                                            }

                                                                                        }
                                                                                        for (Feature f : fs_zd) {
                                                                                            if (!FeatureHelper.isPolygonFeatureValid(f)) {
                                                                                                CrashHandler.WriteLog("出宗地图异常", "宗地图形异常：编号："
                                                                                                        + FeatureHelper.Get(f, "ZDDM", "")
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
                                                                                            float x_deviation=0f;
                                                                                            Point p = GeometryEngine.labelPoint((Polygon) intersectionGeometry);
                                                                                            if (DxfHelper.TYPE==DxfHelper.TYPE_BADONG){
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
                                                                                            textSymbol.setOffsetY(-offset);
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
                        }, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                Bitmap bitmap = (Bitmap) t_;
                                int scale_ = (int) objects[0];
                                feature.getAttributes().put("GLBLC", "1:" + scale_);
                                if (bitmap != null) {
                                    try {
                                        FileUtils.writeFile(filename, ConvertUtil.convert(bitmap));
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
    public void loadJzds(final com.ovit.app.map.model.MapInstance mapInstance, final Feature feature, final List<Feature> jzds, final AiRunnable callback) {
        final List<Feature> features_jzd = new ArrayList<>();
        final FeatureLayer layer_jzd = MapHelper.getLayer(mapInstance.map, "JZD", "界址点");//"ZRZH like '" + id + "____'"
        String id =GetID(feature);
        MapHelper.Query(layer_jzd.getFeatureTable(), StringUtil.WhereByIsEmpty(id)+ "ZDZHDM like'" + "%" + id + "%" + "' ", -1, features_jzd, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Map<String,Feature> map_jzd = new HashMap<>();
                for (Feature f_jzd : features_jzd) {
                    String key = FeatureEditJZD.GetKey((Point)f_jzd.getGeometry());
                    map_jzd.put(key,f_jzd);
                }
                Geometry g = feature.getGeometry();
                if(g instanceof Multipart) {
                    for (ImmutablePart part : ((Multipart) g).getParts()) {
                        for (Point p : part.getPoints()) {
                            String key = FeatureEditJZD.GetKey(p);
                            if(map_jzd.containsKey(key)){
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
    public  String GetID(Feature feature) {
        return AiUtil.GetValue(feature.getAttributes().get("ZDDM"), "");
    }
    public String getPathFct() {
        return FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/房产图/") + "房产图.jpg";
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
        // 画幢
//                        img.setColor(Color.BLUE).setSw(3).draw(feature,mapInstance.getLabel(feature));
        // 画幢
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
                    List<Layer> layers = MapHelper.getLayers(mapInstance.map, "ZRZ");
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
                    FeatureEditH_FSJG.hsmj(f,mapInstance);
                    update_fs.add(f);
                }
                for (Feature f : fs_z_fsjg) {
                    FeatureEditZ_FSJG.hsmj(f,mapInstance);
                    update_fs.add(f);
                }
                for (Feature f : fs_h) {
//                  FeatureViewH.From(mapInstance, f).update_Area(f, fs_h, fs_z_fsjg);
//                    FeatureEditH.hsmj(f,mapInstance, fs_h_fsjg);
                    FeatureViewH.hsmj(f,mapInstance, fs_h_fsjg);
                    update_fs.add(f);
                }
                for (Feature f : fs_zrz) {
                    FeatureViewZRZ.From(mapInstance, f).update_Area(f, fs_h, fs_z_fsjg);
//                  FeatureEditZRZ.hsmj(f,mapInstance, fs_h, fs_z_fsjg);
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
        String orid = FeatureHelper.Get(feature,"ORID","");
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        double jzzdmj = 0;
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
        if(Math.abs(hsmj-area)<0.05){
            hsmj = area ;
        }
        FeatureHelper.Set(feature, "ZDMJ", AiUtil.Scale(area, 2));
        FeatureHelper.Set(feature, "JZZDMJ", AiUtil.Scale(jzzdmj, 2));
        FeatureHelper.Set(feature, "JZMJ", AiUtil.Scale(hsmj, 2));
    }


    public static void CreateFeature(final MapInstance mapInstance, final AiRunnable callback) {
        CreateFeature(mapInstance, mapInstance.getTable("ZD").createFeature(), callback);
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
                        FeatureHelper.Set(feature, "ZDDM", id);
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

    public static void GetMaxZddm(MapInstance mapInstance, Feature f, AiRunnable callback) {
        From(mapInstance, f).getMaxZddm(callback);
    }

    public static FeatureViewZD From(MapInstance mapInstance, Feature f) {
        FeatureViewZD fv = From(mapInstance);
        fv.set(f);
        return fv;
    }

    public static FeatureViewZD From(MapInstance mapInstance) {
        FeatureViewZD fv = new FeatureViewZD();
        fv.set(mapInstance).set(mapInstance.getTable("ZD"));
        return fv;
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

    // 加载所有的宗地、核算其面积
    public static void LaodAllZD_Update_Area(final MapInstance mapInstance, final AiRunnable callback) {
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

    // 查询所有自然幢，识别户和幢附属
    public static void LaodAllZD_CreateCTAddFCT(final MapInstance mapInstance, final AiRunnable callback) {

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

    // 设立不动产单元
    public void createNewBDCDY(Feature f_zd, final AiRunnable callback) {

        final List<Feature> selected_feature_list = new ArrayList<>(); //用于存放选中的feature

        final AiDialog dialog = getBindBDC_View(mapInstance, f_zd, selected_feature_list);

        if (dialog != null) {
            dialog.setFooterView("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    ToastMessage.Send("您取消了绑定不动产！");
                    dialog.dismiss();
                }
            }, "", null, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    List<Feature> fs_zrz = null;
                    if (selected_feature_list.size() > 0) {
                        for (Feature f : selected_feature_list) {
                            if ("ZRZ".equals(f.getFeatureTable().getTableName())) {
                                if (fs_zrz == null) {
                                    fs_zrz = new ArrayList<>();
                                }
                                fs_zrz.add(f);

                            } else if ("ZRZ_C".equals(f.getFeatureTable().getTableName())) {
                                // 宗地与层设定不动产单元，只能与某一层设定
                                if (selected_feature_list.size() > 1) {
                                    ToastMessage.Send("设定不动产单元失败，不能与多层设定一个不动产单元，请重新选择。");
                                    dialog.dismiss();
                                    return;
                                }
                                newBdcdyToC(f, callback);
                                dialog.dismiss();
                                return;

                            } else if ("H".equals(f.getFeatureTable().getTableName())) {
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
        final List<Feature> fs = new ArrayList<>();
        final List<Feature> fs_upt = new ArrayList<>();
            try {
                final String pid = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "", "XMBM", "xmbm");
                FeatureEditQLR.NewID(mapInstance, pid, "", new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String id = t_ + "";
                        final Feature feature_new_qlr = mapInstance.getTable("QLRXX").createFeature();
                        //关联权利人和宗地
                        mapInstance.featureView.fillFeature(feature_new_qlr);
                        if (fs_zrz.size() > 1) {
                            // 多幢属同一权利人 与宗地设定不动产单元号
                            feature_new_qlr.getAttributes().put("QLRDM", id);
                            feature_new_qlr.getAttributes().put("YHZGX", "户主");
                            feature_new_qlr.getAttributes().put("XM", FeatureHelper.Get(feature, "QLRXM"));
                            feature_new_qlr.getAttributes().put("ZJH", FeatureHelper.Get(feature, "QLRZJH"));
                            feature_new_qlr.getAttributes().put("ZJZL", FeatureHelper.Get(feature, "QLRZJZL"));
                            feature_new_qlr.getAttributes().put("DZ", FeatureHelper.Get(feature, "QLRTXDZ"));
                            feature_new_qlr.getAttributes().put("DH", FeatureHelper.Get(feature, "QLRDH"));
                            feature_new_qlr.getAttributes().put("BDCQZH", FeatureHelper.Get(feature, "TDZH"));

                            final String bdcdyh = getZddm() + "F99990001";
                            fv.queryChildFeature("ZRZ", feature, fs, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    mapInstance.newFeatureView().fillFeature(feature_new_qlr, feature);
                                    feature_new_qlr.getAttributes().put("BDCDYH", bdcdyh);
                                    fs_upt.add(feature_new_qlr);
                                    for (Feature f : fs) {
                                        f.getAttributes().put("BDCDYH", bdcdyh);
                                        fs_upt.add(f);
                                    }
                                    capyAttachments(feature, feature_new_qlr);// 拷贝附件材料
                                    MapHelper.saveFeature(fs_upt, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            AiRunnable.Ok(callback, feature_new_qlr);
                                            return null;
                                        }
                                    });
                                    return null;
                                }

                            });
                        } else if (fs_zrz.size() == 1) {
                            // 单幢
                            Feature f_zrz = fs_zrz.get(0);
                            String bdcdyh = f_zrz.getAttributes().get("ZRZH") + "0001";
                            mapInstance.newFeatureView().fillFeature(feature_new_qlr, f_zrz); // 与不动产单元与自然幢 关联
                            feature_new_qlr.getAttributes().put("BDCDYH", bdcdyh);
                            fs_upt.add(feature_new_qlr);
                            fs_upt.add(f_zrz);
                            capyAttachments(feature,feature_new_qlr);
                            MapHelper.saveFeature(fs_upt, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    AiRunnable.Ok(callback, feature_new_qlr);
                                    return null;
                                }
                            });
                        }
                        return null;
                    }
                });
            } catch (Exception es) {
                Log.e(TAG, "通过与自然幢设定不动产单元!" + es);
            }
    }

    /**
     * 拷贝身份镇，户口簿，土地权源资料等附件材料
     * @param feature
     * @param feature_new_qlr
     */
    private void capyAttachments(Feature feature,Feature feature_new_qlr) {
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
    private void newBdcdyToH(Feature f_h, final AiRunnable callback){
        final Feature feature_new_qlr = mapInstance.getTable("QLRXX").createFeature();
        mapInstance.featureView.fillFeature(feature_new_qlr,f_h);
        feature_new_qlr.getAttributes().put("BDCDYH",f_h.getAttributes().get("ID"));
//        feature_new_qlr.getAttributes().put("ORID_PATH",f_h.getAttributes().get("ORID"));
        MapHelper.saveFeature(feature_new_qlr, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiRunnable.Ok(callback,t_);
                return null;
            }
        });
    }
    // 通过宗地与层设定不动产单元
    private void newBdcdyToC(Feature f_C, final AiRunnable callback){
        final Feature feature_new_qlr = mapInstance.getTable("QLRXX").createFeature();
        mapInstance.featureView.fillFeature(feature_new_qlr,f_C);
        feature_new_qlr.getAttributes().put("BDCDYH",f_C.getAttributes().get("ID"));
        MapHelper.saveFeature(feature_new_qlr, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiRunnable.Ok(callback,t_);
                return null;
            }
        });
    }

    private AiDialog getBindBDC_View(MapInstance mapInstance, Feature f_zd, List<Feature> selected_feature_list) {
        final AiDialog dialog = AiDialog.get(mapInstance.activity);

        try{
            dialog.get().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //使EditText可以使用软键盘
            //根据屏幕分辨率将dialog设置为固定大小
            Display display = mapInstance.activity.getWindowManager().getDefaultDisplay();
            Window dialogWindow = dialog.get().getWindow();
            WindowManager.LayoutParams params = dialogWindow.getAttributes();
            params.width = (int)(display.getWidth()*0.9);
            params.height = (int)(display.getHeight()*0.8);
            dialogWindow.setAttributes(params);

            dialog.setContentView(getAllSelectBDC(mapInstance,f_zd,selected_feature_list));
            dialog.setHeaderView(R.mipmap.app_map_layer_zd, "不动产单元设定，ZDDM："+getZddm());

            return dialog;
        }catch(Exception es){
            Log.e(TAG,"获得不动产列表失败！"+es);
            dialog.dismiss();
            return null;
        }
    }

    private View getAllSelectBDC(final MapInstance mapInstance, Feature f_zd, final List<Feature> selected_feature_list) {
        final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
        final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
        final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
        if(ll_view.getChildCount()>0)
        {
            ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
        }

        FeatureTable table = mapInstance.getTable("ZRZ");
        final List<Feature> fs = new ArrayList<Feature>();

        final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity,listItemRes,fs) {
            @Override
            protected void convert(BaseAdapterHelper helper,final Feature item) {
                final com.ovit.app.map.view.FeatureView fv_zrz = mapInstance.newFeatureView(item.getFeatureTable());
                initSelectList(mapInstance,helper,item,0,selected_feature_list);
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

                                        fv_zrz.queryChildFeature("LJZ", item, fs, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        final com.ovit.app.map.view.FeatureView fv_ljz = mapInstance.newFeatureView("LJZ");

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
                                                                            fv_ljz.queryChildFeature("H", item, fs, new AiRunnable() {
                                                                                @Override
                                                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                    final com.ovit.app.map.view.FeatureView fv_h = mapInstance.newFeatureView("H");
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
                                fv_zrz.queryChildFeature("ZRZ_C", item, fs, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                final com.ovit.app.map.view.FeatureView fv_ljz = mapInstance.newFeatureView("ZRZ_C");

                                                QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity,listItemRes,fs) {
                                                    @Override
                                                    protected void convert(BaseAdapterHelper helper,final Feature item) {
                                                        initSelectList(mapInstance,helper,item,2,selected_feature_list);
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
                                                AiRunnable.Ok(callbackToLjz,fs);
                                                return  null;
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
        String where="ZDDM='"+getZddm()+"'";
        MapHelper.Query(table,where,config.getCol_orderby(),config.col_sort,0,true,fs,new AiRunnable() {
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
        if(ll_view.getChildCount()>0)
        {
            ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
        }

        FeatureTable table = mapInstance.getTable("GYRXX");
        final List<Feature> fs = new ArrayList<Feature>();

        final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity,listItemRes,fs) {
            @Override
            protected void convert(BaseAdapterHelper helper,final Feature item) {
                final com.ovit.app.map.view.FeatureView fv_gyr = mapInstance.newFeatureView(item.getFeatureTable());
                initSelectList(mapInstance,helper,item,0, qlr_feature_list);
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


    public void create_bdcfy(Feature feature, final AiRunnable callback) {
//        fv_zrz.queryChildFeature("LJZ", item, fs, new AiRunnable() {
        final List<Feature> fs=new ArrayList<>();
        fv.queryChildFeature(FeatureConstants.ZRZ_TABLE_NAME, feature, fs, new AiRunnable() {
           @Override
           public <T_> T_ ok(T_ t_, Object... objects) {
               newBdcdyToZrz(fs, callback);
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

}