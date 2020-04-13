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
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.ai.component.custom.CustomImagesView;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.DialogBuilder;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.StringUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LiuSheng on 2017/7/28.
 */

public class FeatureEditLJZ extends FeatureEdit {
    //region 常量
    final static String TAG = "FeatureEditLJZ";
    private static final String MAP_LAYER_Z_FSJG = "Z_FSJG";
    private static final String MAP_LAYER_H_FSJG = "H_FSJG";
    ///endregion

    //region 字段
    FeatureViewLJZ fv;

    TextView et_dscs;
    TextView et_dxcs;
    TextView et_zcs;
    View view_h;
    View view_ftqk;
    ///endregion

    //region 构造函数
    public FeatureEditLJZ() {
        super();
    }

    public FeatureEditLJZ(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }

    ///endregion

    //region  重写函数和回调
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
            FeatureHelper.Set(feature,"FWJG",fv.getFwjg());
            super.update(callback);
        } catch (Exception es) {
            ToastMessage.Send(activity, "更新属性失败!", TAG, es);
        }
    }
    ///endregion

    //region 公有函数
    ///endregion

    //region 私有函数
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
                    .setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
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
                .setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
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
                .setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
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
        DialogBuilder.confirm(activity, "图形转换提示", "确定要转幢附属结构？幢附属结构根据（全、半、无）三类去核算建筑面积。", null, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
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
        }, AiDialog.CENCEL, null).show();

    }

    // 逻辑幢转为户附属结构
    private void conversionToHfsjg() {
        DialogBuilder.confirm(activity, "图形转换提示", "确定要转幢附属结构？幢附属结构根据（全、半、无）三类去核算建筑面积。", null, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
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
        }, AiDialog.CENCEL, null).show();
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
        aiDialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AiRunnable.Ok(callback, aiDialog, map, dataconfig);
            }
        });
    }
    ///endregion

    //region 内部类或接口
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
    ///endregion

}
