package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.ai.component.custom.CustomImagesView;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.DialogBuilder;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.FileUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LiuSheng on 2017/7/28.
 */

public class FeatureEditLJZ extends FeatureEdit {

    final static String TAG = "FeatureEditLJZ";
    private static final String MAP_LAYER_Z_FSJG = "Z_FSJG";
    private static final String MAP_LAYER_H_FSJG = "H_FSJG";
    ///region 属性
    FeatureViewLJZ fv;

    TextView et_dscs;
    TextView et_dxcs;
    TextView et_zcs;
    View view_h;
    View view_ftqk;
    ///endregion

    public FeatureEditLJZ() {
        super();
    }

    public FeatureEditLJZ(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }

    ///region  重写父类方法
    @Override
    public void onCreate() {
        super.onCreate();
        // 使用 fv
        if (super.fv instanceof FeatureViewLJZ) {
            this.fv = (FeatureViewLJZ) super.fv;
        }
    }

    // 初始化
    @Override
    public void init() {
        super.init();
        // 菜单
        menus = new int[]{R.id.ll_info, R.id.ll_hinfo, R.id.ll_ft};
    }

    // 显示数据
    @Override
    public void build() {
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_ljz, v_content);
        try {
            if (feature != null) {
                mapInstance.fillFeature(feature);
                fillView(v_feature);

                TextWatcher tw_cs = new TextWatcher() {

                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        cs_change(null);
                    }
                };
                ((TextView) v_feature.findViewById(R.id.et_szc)).addTextChangedListener(tw_cs);
                ((TextView) v_feature.findViewById(R.id.et_mchs)).addTextChangedListener(tw_cs);

                et_dscs = ((TextView) v_feature.findViewById(R.id.et_dscs));
                et_dxcs = ((TextView) v_feature.findViewById(R.id.et_dxcs));
                et_zcs = ((TextView) v_feature.findViewById(R.id.et_zcs));
                et_dscs.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        cs_change(et_dscs);
                    }
                });
                et_dxcs.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        cs_change(et_dxcs);
                    }
                });
                et_zcs.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        cs_change(et_zcs);
                    }
                });

                CustomImagesView civ_fwzp = (CustomImagesView) v_feature.findViewById(R.id.civ_fwzp);
                String fileDescription = AiUtil.GetValue(civ_fwzp.getContentDescription(), "材料");
                civ_fwzp.setName(fileDescription, activity).setDir(FileUtils.getAppDirAndMK(getpath_root() + "附件材料/" + fileDescription + "/"));

                v_feature.findViewById(R.id.et_tv_autodrawh).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        h_init();
                    }
                });
                v_feature.findViewById(R.id.tv_cqsyh).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearAll();
                    }
                });

                v_feature.findViewById(R.id.et_tv_drawtj).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawTj();
                    }
                });
                // 智能识别户
                v_feature.findViewById(R.id.tv_znsb).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        fv.InitFeatureLjz(new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                MapHelper.saveFeature((List<Feature>) t_, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        ToastMessage.Send(activity, "初始化完成！");
                                        load_hinfo(true);
                                        return null;
                                    }

                                });
                                return null;
                            }
                        });

                    }
                });
                //  识别户
                v_feature.findViewById(R.id.tv_sbh).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 识别户
                        fv.identyH(new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                // 识别幢附属
                                fv.identyH_Fsjg((List<Feature>) t_, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        fv.identyZ_Fsjg(new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                fv.update_Area(new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        fillView(v_feature, feature, "YCJZMJ");
                                                        fillView(v_feature, feature, "SCJZMJ");
                                                        load_hinfo(true);
                                                        ToastMessage.Send(activity, "识别完成！");
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
                });

                TextView tv_to_z_fsjg = (TextView) v_feature.findViewById(R.id.tv_to_z_fsjg);
                tv_to_z_fsjg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 转为幢附属结构
                        conversionToZfsjg();
                    }
                });
                TextView tv_to_h_fsjg = (TextView) v_feature.findViewById(R.id.tv_to_h_fsjg);
                tv_to_h_fsjg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        conversionToHfsjg();
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

        addMenu("基本信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_info);
            }
        });

        addMenu("分户信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_hinfo);
                load_hinfo(false);
            }
        });
        addMenu("分摊情况", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_ft);
                load_ftqk();
//                View ft_view = view.findViewById(R.id.ll_ft_content);
//                FeatureEditFTQK.load_ft(mapInstance, feature, ft_view);
            }
        });
        addAction("画户", R.mipmap.app_map_layer_h, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        drawH();
                        return null;
                    }
                });
            }
        });
        addAction("幢附属", R.mipmap.app_map_layer_h_pc, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fv.init_fsjg("z_fsjg_lx", FeatureEditLJZ.MAP_LAYER_Z_FSJG);

            }
        });
        addAction("户附属", R.mipmap.app_map_layer_h_pc, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fv.init_fsjg("h_fsjg_lx", FeatureEditLJZ.MAP_LAYER_H_FSJG);
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

    private void cs_change(TextView v) {
        int dscs = AiUtil.GetValue(et_dscs.getText(), 1);
        int dxcs = AiUtil.GetValue(et_dxcs.getText(), 0);
        int zcs = AiUtil.GetValue(et_zcs.getText(), 1);
        if (v == et_zcs) {
            // 总层数发生变化，改变地上
            int dscs_ = zcs - Math.abs(dxcs);
            if (dscs_ != dscs) {
                et_dscs.setText(dscs_ + "");
            }
        } else {
            // 地上或是地下发生变化，改变总层数
            int zcs_ = Math.abs(dscs) + Math.abs(dxcs);
            if (zcs_ != zcs) {
                et_zcs.setText(zcs_ + "");
            }
        }
        view_h = null;
    }

    // 快速绘制户
    private void h_init() {
        {
            AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_more_blue, "快速绘制户")
                    .addContentView("确定要给该逻辑幢绘制户么?", "该操作将根据逻辑幢的形状给每一层都绘制一个户。操作不可逆转，请根据需要处理！")
                    .setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            fv.h_init(new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    // 加载界面
                                    load_hinfo(true);
                                    dialog.dismiss();
                                    return null;
                                }
                            });

                        }
                    }).show();
        }

    }

    //清除户和附属结构
    private void clearAll() {
        AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_more_blue, "清除所有的户和附属")
                .addContentView("确定要清除该逻辑幢所有的户么?", "该操作将根据逻辑幢的所有的户和幢的附属结构。操作不可逆转，请根据需要处理！")
                .setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mapInstance.newFeatureView().delChildFeature("H,H_FSJG,Z_FSJG".split(","), feature, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                load_hinfo(true);
                                return null;
                            }
                        });
                        dialog.dismiss();
                    }
                }).show();
    }

    // 绘制天井
    private void drawTj() {

        AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_more_blue, "绘制天井")
                .addContentView("确定要绘制天井么，建议绘制完成户后再绘制天井（该操作不可逆）",
                        "绘制天井时，会挖空天井范围内所有幢、户、附属的图形，建议绘制完户相关结构后最后绘制天井。操作不可逆转，请谨慎处理！")
                .setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fv.draw_tj(feature, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                load_hinfo(true);
                                ToastMessage.Send(activity, "绘制天井完成！");
                                return null;
                            }
                        });
                        dialog.dismiss();
                    }
                }).show();
    }

    // 逻辑幢转为幢附属结构
    private void conversionToZfsjg() {
        DialogBuilder.confirm(activity, "图形转换提示", "确定要转幢附属结构？幢附属结构根据（全、半、无）三类去核算建筑面积。", null, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fv.featureConversionToZfsjg(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Feature f_zfsjg = (Feature) t_;
                        mapInstance.viewFeature(f_zfsjg);
                        return null;
                    }
                });
            }
        }, "取消", null).show();

    }

    // 逻辑幢转为户附属结构
    private void conversionToHfsjg() {
        DialogBuilder.confirm(activity, "图形转换提示", "确定要转幢附属结构？幢附属结构根据（全、半、无）三类去核算建筑面积。", null, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fv.featureConversionToHfsjg(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Feature f_hfsjg = (Feature) t_;
                        mapInstance.viewFeature(f_hfsjg);
                        return null;
                    }
                });
            }
        }, "取消", null).show();
    }

    // 分户信息
    private void load_hinfo(boolean relaod) {
        if (relaod) {
            view_h = null;
        }
        if (view_h == null) {
            LinearLayout ll_hinfo_content = (LinearLayout) view.findViewById(R.id.ll_hinfo_content);
            view_h = FeatureEditC.GetView_C(mapInstance, feature);
            ll_hinfo_content.removeAllViews();
            ll_hinfo_content.addView(view_h);
            view_h.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    // 分摊情况
    private void load_ftqk() {
        if (view_ftqk == null) {
            ViewGroup ftqk_view = (ViewGroup) view.findViewById(R.id.ll_ft_list);
            ftqk_view.setTag(null); //强制重新生成adapter
            mapInstance.newFeatureView("FTQK").buildListView(ftqk_view, fv.queryChildWhere());
            view_ftqk = ftqk_view;
        }
    }

    // 逻辑幢 绘制户
    private void drawH() {
        try {
            fv.draw_h(feature, "1", new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    // 显示界面
                    load_hinfo(true);
                    ToastMessage.Send(activity, "画户成功");
                    return null;
                }
            });
        } catch (Exception es) {
            ToastMessage.Send(activity, "画户失败", es);
        }
    }

    // 绘制附属结构
    private void fsjg_init(final String resname, final AiRunnable callback) {
        String desc = "该操作主要是绘制幢附属结构到指定楼层！";
        final Map<String, Object> map = new LinkedHashMap<>();
        final Map<String, String> dataconfig = new LinkedHashMap<>();
        final AiDialog aiDialog = AiDialog.get(mapInstance.activity);
        final String szc = "SZC";
        map.put(szc, "1");
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

//    //  获取最大的编号   LJZH 只有20 ，42068100100100000001
//    public static void GetMaxID(MapInstance mapInstance, String djzq, AiRunnable callback) {
//        MapHelper.QueryMax(GetTable(mapInstance), StringUtil.WhereByIsEmpty(djzq)+"LJZH like '" + djzq + "________'", "LJZH", djzq.length(), 0,djzq+"00000000",callback );
//    }

//    public static void NewID(MapInstance mapInstance, final String djzq, final AiRunnable callback) {
//        GetMaxID(mapInstance,djzq, new AiRunnable(callback) {
//            @Override
//            public <T_> T_ ok(T_ t_, Object... objects) {
//                String id = "";
//                if (objects.length > 1) {
//                    // 最大号加1
//                    int count = AiUtil.GetValue(objects[1], 0) + 1;
//                    id = djzq+ String.format("%08d", count);
//                }
//                AiRunnable.Ok(callback, id);
//                return null;
//            }
//        });
//    }

//    // 带有诸多属性画逻辑幢
//    public static void CreateFeature(final MapInstance mapInstance,  final String orid ,final Feature feature_ljz ,  final AiRunnable callback) {
//        mapInstance.newFeatureView().findFeature("ZRZ",orid,new AiRunnable(callback){
//            @Override
//            public <T_> T_ ok(T_ t_, Object... objects) {
//                Feature featureZRZ = (Feature) t_;
//                CreateFeature(mapInstance,featureZRZ,feature_ljz,callback);
//                return null;
//            }
//        });
//    }
//   // 带有诸多属性画逻辑幢
//    public static void CreateFeature(final MapInstance mapInstance,final Feature featureZRZ, final Feature feature_ljz ,AiRunnable callback) {
//        final Feature feature ;
//        if(feature_ljz==null) {
//            feature =  GetTable(mapInstance).createFeature();
//        }else{
//            feature = feature_ljz;
//        }
//        if(featureZRZ!=null&& featureZRZ.getFeatureTable() != mapInstance.getTable("ZRZ")) {
//            String orid = mapInstance.getOrid_Match(feature,"ZRZ");
//            if(StringUtil.IsNotEmpty(orid)){
//                CreateFeature(mapInstance,orid,feature,callback);
//                return;
//            }
//        }
//
//        if (featureZRZ == null ) {
//            ToastMessage.Send("注意：缺少幢信息");
//        }
//        if (feature.getGeometry() == null && featureZRZ != null) {
//            feature.setGeometry(MapHelper.geometry_copy(featureZRZ.getGeometry()));
//        }
//        mapInstance.command_draw(feature, new AiRunnable() {
//            @Override
//            public <T_> T_ ok(T_ t_, Object... objects) {
//                final String zrzh = FeatureHelper.Get(featureZRZ, "ZRZH", "");
//                String djzq = StringUtil.substr(mapInstance.aiMap.getProjectXmbm(),0,12);
//                NewID(mapInstance, djzq, new AiRunnable() {
//                    @Override
//                    public <T_> T_ ok(T_ t_, Object... objects) {
//                        String id = t_ + "";
//                        FeatureHelper.Set(feature,"LJZH", id);
//                        FeatureHelper.Set(feature,"ZRZH", zrzh);
//                        mapInstance.fillFeature(feature,featureZRZ);
//                        mapInstance.newFeatureView(feature).fillFeatureAddSave(feature, new AiRunnable() {
//                            @Override
//                            public <T_> T_ ok(T_ t_, Object... objects) {
//                                mapInstance.viewFeature(feature);
//                                return null;
//                            }
//                        });
//                        return null;
//                    }
//                });
//                return null;
//            }
//        });
//    }

//    //  幢识别逻辑幢
//    public static void IdentyZ_LJZ(final MapInstance mapInstance, final Feature f_z, final AiRunnable callback) {
//        final String zrzh = FeatureHelper.Get(f_z, "ZRZH", "");
//        if( StringUtil.IsNotEmpty(zrzh)) {
//            final List<Feature> features_ljz = new ArrayList<Feature>();
//            final List<Feature> features_update = new ArrayList<Feature>();
//            final List<Feature> features_save = new ArrayList<Feature>();
//            MapHelper.Query(GetTable(mapInstance), f_z.getGeometry(), features_ljz, new AiRunnable(callback) {
//                @Override
//                public <T_> T_ ok(T_ t_, Object... objects) {
//                    double area_jzmj =FeatureHelper.Get(f_z,"SCJZMJ", 0d);
//                    int count = 0;
//                    for (Feature f : features_ljz) {
//                        final String f_ljzh = AiUtil.GetValue(f.getAttributes().get("LJZH"), "");
//                        final String f_zrzh = AiUtil.GetValue(f.getAttributes().get("ZRZH"), "");
//                        double f_hsmj = AiUtil.GetValue(f.getAttributes().get("HSMJ"), 0d);
//                        //  只检查 没有被识别过的和本幢的
//                        if (StringUtil.IsEmpty(f_zrzh)||f_zrzh.equals(zrzh) ) {
//                            area_jzmj += f_hsmj;
//                            // 通用部分更新
//                            features_save.add(f);
//                            // 只检查 没有层的，和本层的
//                            if (StringUtil.IsNotEmpty(f_zrzh) && zrzh.equals(f_zrzh) && f_ljzh.startsWith(f_zrzh)) {
//                                int i = AiUtil.GetValue(f_ljzh.substring(f_zrzh.length()), 0);
//                                if (count < i) {
//                                    count = i;
//                                }
//                            } else {
//                                features_update.add(f);
//                            }
//
//                        }
//                    }
//                    if (features_update.size() > 0) {
//                        for (Feature f : features_update) {
//                            count++;
//                            String i = String.format("%04d", count);
//                            FeatureHelper.Set(f,"LJZH", zrzh + i);
//                        }
//                    }
//                    mapInstance.fillFeature(features_save,f_z);
//                    FeatureHelper.Set(f_z,"SCJZMJ", AiUtil.GetValue(String.format("%.2f", area_jzmj), area_jzmj));
//                    features_save.add(f_z);
//                    MapHelper.saveFeature(features_save, callback);
//
//                    return null;
//                }
//            });
//        }else{
//            AiRunnable.Ok(callback,null);
//        }
//    }
//

//
//    // 核算幢 占地面积、建筑面积
//    public static void IdentyLJZ_Area(final MapInstance instance, final Feature f_zrz, final AiRunnable callback) {
////        final String zrzh = AiUtil.GetValue(f_zrz.getAttributes().get("ZRZH"), "");
//        final List<Feature> f_zrz_fsjgs = new ArrayList<>();
//        final List<Feature> f_zrz_hs = new ArrayList<>();
//        final List<Feature> f_zrz_h_fsjgs = new ArrayList<>();
//        final List<Feature> update_fs = new ArrayList<>();
//        FeatureView.LoadH_And_Fsjg(instance, f_zrz, f_zrz_fsjgs, f_zrz_hs, f_zrz_h_fsjgs, new AiRunnable(callback) {
//            @Override
//            public <T_> T_ ok(T_ t_, Object... objects) {
//                for (Feature f : f_zrz_h_fsjgs) {
//                    FeatureEditH_FSJG.hsmj(f,instance);
//                    update_fs.add(f);
//                }
//                for (Feature f : f_zrz_fsjgs) {
//                    FeatureEditZ_FSJG.hsmj(f,instance);
//                    update_fs.add(f);
//                }
//                for (Feature f : f_zrz_hs) {
//                    FeatureEditH.hsmj(f,instance,f_zrz_h_fsjgs);
//                    update_fs.add(f);
//                }
////                FeatureEditLJZ.hsmj(f_zrz,instance, f_zrz_hs, f_zrz_fsjgs);
//
//                update_fs.add(f_zrz);
//                MapHelper.saveFeature(update_fs, callback);
//                return null;
//            }
//        });
//    }

//    public static View GetView_LJZ(MapInstance mapInstance,Feature f_zrz,  final AiRunnable callback) {
//        LinearLayout ll_view = (LinearLayout) LayoutInflater.from(mapInstance.activity).inflate(
//                R.layout.app_ui_ai_aimap_ljz, null);
//        LinearLayout ll_list = (LinearLayout) ll_view.findViewById(R.id.ll_list);
//        FeatureEditLJZ.BuildView_LJZ(mapInstance,ll_list, f_zrz, 0, callback);
//
//        return ll_view;
//    }


//
//    public static void BuildView_LJZ(final MapInstance mapInstance,final LinearLayout ll_list,final Feature f_zrz,  final int deep, final AiRunnable callback) {
//        if (ll_list.getTag() == null) {
//            QuickAdapter<Feature> adpter = new QuickAdapter<Feature>(mapInstance.activity, R.layout.app_ui_ai_aimap_ljz_item, new ArrayList<Feature>()) {
//                @Override
//                protected void convert(final BaseAdapterHelper helper, final Feature item) {
//                    final String name = FeatureHelper.Get(item,"MPH", "");
//                    final String id =  FeatureHelper.Get(item,"LJZH", "");
//                    final String desc = id.length() > 12 ? id.substring(12) : id;
//
//                    final LinearLayout ll_list_item = helper.getView(R.id.ll_list_item);
//                    helper.setText(R.id.tv_name, name);
//                    helper.setText(R.id.tv_desc, desc);
//
//                    int s = (int) (deep * mapInstance.activity.getResources().getDimension(R.dimen.app_size_smaller));
//                    helper.getView(R.id.v_split).getLayoutParams().width = s;
//
//                    Bitmap bm = MapHelper.geometry_icon(new Feature[]{f_zrz,item}, 100, 100, new int []{ R.color.app_theme_fore, Color.BLUE}, new int []{1,5});
//                    if (bm != null) {
//                        helper.setImageBitmap(R.id.v_icon, bm);
//                    } else {
//                        helper.setImageResource(R.id.v_icon, R.mipmap.app_icon_building);
//                    }
//
//                    helper.getView().setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
//                            if (!flag) {
//                                FeatureEditH.BuildView_H(mapInstance,ll_list_item, item,"", deep + 1);
//                            }
//                            ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
//                        }
//                    });
//
//                    helper.getView(R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            mapInstance.viewFeature(item);
//                        }
//                    });
//                    helper.getView(R.id.iv_position).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            MapHelper.selectAddCenterFeature(mapInstance.map, item);
//                        }
//                    });
//
//
////                    MapHelper.Query(GetTable(mapInstance,"H","户"), StringUtil.WhereByIsEmpty(id)+" ZRZH ='" + id + "' ", 0, features, new AiRunnable(callback) {
////                        @Override
////                        public <T_> T_ ok(T_ t_, Object... objects) {
////                            helper.setText(R.id.tv_count, features.size() + "");
////                            AiRunnable.Ok(callback,t_,objects); return  null;
////                        }
////                    });
//                }
//            };
////                lv_list.setAdapter(adpter);
//            ll_list.setTag(adpter);
//            adpter.adpter(ll_list);
//        }
//        final List<Feature> features = new ArrayList<Feature>();
//        LoadAll(mapInstance, f_zrz, features, new AiRunnable() {
//            @Override
//            public <T_> T_ ok(T_ t_, Object... objects) {
//                QuickAdapter<Feature> adpter = (QuickAdapter<Feature>) ll_list.getTag();
//                adpter.clear();
//                adpter.addAll(features);
//                return null;
//            }
//        });
//    }

    class TextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

}
