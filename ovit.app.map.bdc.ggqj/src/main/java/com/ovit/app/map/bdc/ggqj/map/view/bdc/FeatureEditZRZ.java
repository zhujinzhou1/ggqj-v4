package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.layers.Layer;
import com.ovit.R;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.ai.component.custom.CustomImagesView;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.AppConfig;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Lichun on 2017/4/5.
 */

public class FeatureEditZRZ extends FeatureEdit {

    final static String TAG = "FeatureEditZRZ";
    FeatureViewZRZ fv;
    private String old_zrzh;

    //region  重写父类方法
    @Override
    public void onCreate() {
        super.onCreate();
        // 使用 fv
        if (super.fv instanceof FeatureViewZRZ) {
            this.fv = (FeatureViewZRZ) super.fv;
        }
    }

    // 初始化
    @Override
    public void init() {
        super.init();
//        zcs = FeatureHelper.Get(feature,"ZCS", 1d);
        // 菜单
        menus = new int[]{R.id.ll_info, R.id.ll_ljz, R.id.ll_c, R.id.ll_fct, R.id.ll_fcfh, R.id.ll_ft, R.id.ll_bdcdy};
    }

    // 显示数据
    @Override
    public void build() {
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_zrz, v_content);
        try {
            if (feature != null) {
                mapInstance.fillFeature(feature);
                fillView(v_feature);

                CustomImagesView civ_fwzp = (CustomImagesView) v_feature.findViewById(R.id.civ_fwzp);
                String fileDescription = AiUtil.GetValue(civ_fwzp.getContentDescription(), "材料");
                old_zrzh = AiUtil.GetValue(feature.getAttributes().get("ZRZH"), "");

                civ_fwzp.setName(fileDescription, activity).setDir(FileUtils.getAppDirAndMK(getpath_root() + "附件材料/" + fileDescription + "/"));
                ((TextView) v_feature.findViewById(R.id.et_zcs)).setText(AiUtil.GetValue(FeatureHelper.Get(feature, "ZCS"), "1", "#.##"));
                if (AppConfig.PHSZ_DSDXC_OPEN.equals(AppConfig.get(AppConfig.APP_BDCQJDC_PHSZ_DSDXC, AppConfig.PHSZ_DSDXC_CLOSE))) {
                    v_feature.findViewById(R.id.ll_dscs).setVisibility(View.VISIBLE);
                    v_feature.findViewById(R.id.ll_dxcs).setVisibility(View.VISIBLE);
                } else {
                    v_feature.findViewById(R.id.ll_dscs).setVisibility(View.GONE);
                    v_feature.findViewById(R.id.ll_dxcs).setVisibility(View.GONE);
                }
                ((TextView) v_feature.findViewById(R.id.et_dscs)).setText(AiUtil.GetValue(FeatureHelper.Get(feature, "DSCS"), "0", "#.##"));
                ((TextView) v_feature.findViewById(R.id.et_dxcs)).setText(AiUtil.GetValue(FeatureHelper.Get(feature, "DXCS"), "0", "#.##"));

                TextWatcher tw_cs = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        Log.i(TAG, "beforeTextChanged---" + s.toString());
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // 层数变化
                        cs_change();
                        Log.i(TAG, "onTextChanged---" + s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Log.i(TAG, "afterTextChanged---" + s.toString());
                    }
                };
//                ((TextView) v_feature.findViewById(R.id.et_dscs)).addTextC hangedListener(tw_cs);
//                ((TextView) v_feature.findViewById(R.id.et_dxcs)).addTextChangedListener(tw_cs);
                ((TextView) v_feature.findViewById(R.id.et_zcs)).addTextChangedListener(tw_cs);

                v_feature.findViewById(R.id.et_tv_drawljz).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fv.draw_ljz(feature, "", new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                load_ljz(true);
                                return null;
                            }
                        });
                    }
                });

                v_feature.findViewById(R.id.tv_sbljz).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fv.identyLjz(new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                load_ljz(true);
                                return null;
                            }
                        });
                    }
                });
                v_feature.findViewById(R.id.tv_zglzd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastMessage.Send(activity, "请选择宗地！");
                        Layer layer = MapHelper.getLayer(map, "ZD");
                        mapInstance.setSelectLayer(layer, null, false);
                    }
                });

                v_feature.findViewById(R.id.tv_jcyzdgx).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String zrzh = FeatureHelper.Get(feature, "ZRZH", "");
                        zrzh = StringUtil.substr_last(zrzh, 5);
                        FeatureHelper.Set(feature, "ZRZH", zrzh);
                        FeatureHelper.Set(feature, "ORID_PATH", "");
                        mapInstance.fillFeature(feature);
                        mapInstance.viewFeature(feature);
                        ToastMessage.Send(activity, "与宗地关系已经解除！");
                    }
                });
                v_feature.findViewById(R.id.tv_cxsccxx).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        initC();
                        c_init();
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
        addMenu("逻辑幢", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_ljz);
                load_ljz(false);
            }
        });

        addMenu("层信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_c);
                loadzrzC();

            }
        });
        addMenu("不动产单元", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_bdcdy);
                load_bdcdy();
            }
        });

        addMenu("分层图", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_fct);
                load_fcfh();
            }
        });

        addMenu("分户图", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_fcfh);
                load_fcfh();
            }
        });
        addMenu("分摊情况", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_ft);
                View ft_view = view.findViewById(R.id.ll_ft_content);
                FeatureEditFTQK.load_ft(mapInstance, feature, ft_view);
            }
        });

        addAction("逻辑幢", R.mipmap.app_map_layer_h, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        drawLjz();
                        return null;
                    }
                });
            }
        });
        addAction("设定不动产", R.mipmap.app_map_layer_add_bdcdy, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initBdcdy();
            }
        });

    }
    //自然幢设定不动产单元
    private void initBdcdy() {
        final AiDialog aiDialog = AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_more_blue, "不动产单元设定");
        String oridPath = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID_PATH, "");

        if (StringUtil.IsNotEmpty(oridPath)||!oridPath.contains(FeatureHelper.TABLE_NAME_ZD)) {
            fv.checkcBdcdy(feature, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if (t_ != null) {
                        //可以设定不动产单元
                        aiDialog.addContentView("确定要生成一个不动产单元吗?", "该操作将根据宗地与该自然幢共同设定一个不动产单元！");
                        aiDialog.setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                // 加载界面
                                fv.create_zrz_bdcfy(feature, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        mapInstance.viewFeature((Feature) t_);
                                        dialog.dismiss();
                                        return null;
                                    }
                                });
                            }
                        }).show();

                    } else {
                        aiDialog.addContentView("不能设定不动产单元", (String) objects[0] + "已经设定了不动产单元！");
                        aiDialog.setFooterView("取消", "确定", null).show();
                    }

                    return null;
                }
            });
        } else {
            aiDialog.addContentView("不能设定不动产单元", "自然幢没有与宗地关联，请识别自然幢！");
            aiDialog.setFooterView("取消", "确定", null).show();
        }

    }

    // 保存数据
    @Override
    public void update(final AiRunnable callback) {
        try {
            update_zrzh(new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                   FeatureEditZRZ.super.update(callback);
                    return null;
                }
            });
//            super.update(callback);
//            super.update(new AiRunnable(callback) {
//                @Override
//                public <T_> T_ ok(T_ t_, Object... objects) {
//                    update_zrzh(callback);
//                    return null;
//                }
//            });
        } catch (Exception es) {
            ToastMessage.Send(activity, "更新属性失败!", TAG, es);
        }
    }
    public void update_zrzh(final AiRunnable callback) {
        final String zh = AiUtil.GetValue(feature.getAttributes().get("ZH"), "");
        // 如果zrzh发生变化的 需要级联更新
        if (!TextUtils.isEmpty(zh)&& !zh.equals(StringUtil.substr_last(old_zrzh,4))) {
            String oldPath =FileUtils.getAppDirPath(mapInstance.getpath_feature(feature));
            final String zrzh = old_zrzh.substring(0,old_zrzh.length()-zh.length())+zh;
            FeatureHelper.Set(feature,"ZRZH",zrzh);
            mapInstance.fillFeature(feature);
            MapHelper.saveFeature(feature, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    mapInstance.viewFeature(feature);
                    return null;
                }
            });
        } else {
            AiRunnable.Ok(callback, null);
        }
    }
    // endregion
    //region 属性页
    View view_bdcdy;
    public void load_bdcdy() {
        if (view_bdcdy == null) {
            ViewGroup bdcdy_view = (ViewGroup) view.findViewById(R.id.ll_bdcdy_list);
            bdcdy_view.setTag(null); //强制重新生成adapter
            mapInstance.newFeatureView("QLRXX").buildListView(bdcdy_view,fv.queryChildWhere());
            view_bdcdy = bdcdy_view;
        }
    }
    View view_ljz;

    //加载逻辑幢
    private void load_ljz(boolean relaod) {
        if (relaod) {
            view_ljz = null;
        }
        if (view_ljz == null) {
            ViewGroup ll_ljz_content = (ViewGroup) view.findViewById(R.id.ll_ljz_content);
            ll_ljz_content.setTag(null); //强制重新生成adapter
            mapInstance.newFeatureView("LJZ").buildListView(ll_ljz_content, fv.queryChildWhere());
            view_ljz = ll_ljz_content;
        }
    }
    private void initC() {
        FeatureEditC.UpdateC(mapInstance, feature, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                reloadC();
                return null;
            }
        });
    }

    // 快速绘制户
    private void c_init() {
        {
            AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_more_blue, "快速生成层")
                    .addContentView("确定要给该自然幢生成层么?", "该操作将根据自然幢的形状给每层都绘制一个层结构。操作不可逆转，请根据需要处理！")
                    .setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            fv.c_init(new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    // 加载界面
                                    reloadC();
                                    dialog.dismiss();
                                    return null;
                                }
                            });

                        }
                    }).show();
        }
    }
    ViewGroup zrzc_view = null;

    // 层信息
    private void loadzrzC() {
        if (zrzc_view==null) {
            ViewGroup ll_c_content = (ViewGroup) view.findViewById(R.id.ll_c_content);
            ll_c_content.setTag(null); //强制重新生成adapter
            mapInstance.newFeatureView(FeatureConstants.C_TABLE_NAME).buildListView(ll_c_content, fv.queryChildWhere());
            zrzc_view = ll_c_content;
        }
    }

    private void reloadC() {
        zrzc_view=null;
        loadzrzC();
    }

    // 绘制幢并重新加载数据
    private void drawLjz() {
        try {
            fv.draw_ljz(feature, "", new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    load_ljz(true);
                    return null;
                }
            });
        } catch (Exception es) {
            ToastMessage.Send(activity, "画自然幢失败", es);
        }
    }

    public void cs_change() {
        Log.i(TAG, "层数改变");
        view_t = null;
    }

    View view_t;

    // 分层分户图
    private void load_fcfh() {
        if (view_t == null) {
            final LinearLayout ll_fcfh_content = (LinearLayout) view.findViewById(R.id.ll_fcfh_content);
            if (ll_fcfh_content.getTag() == null) {
                QuickAdapter<Map.Entry<String, List<Feature>>> adpter_fcfht = FeatureEditC.getFcfhtAdapter(mapInstance, feature, "分层分户图", null);
                ll_fcfh_content.setTag(adpter_fcfht);
                adpter_fcfht.adpter(ll_fcfh_content);
            }
            final LinearLayout ll_fct_content = (LinearLayout) view.findViewById(R.id.ll_fct_content);
            if (ll_fct_content.getTag() == null) {
                QuickAdapter<String> adpter_fct = FeatureEditC.getFctAdapter(mapInstance, feature, "分层图", null);
                ll_fct_content.setTag(adpter_fct);
                adpter_fct.adpter(ll_fct_content);
            }
            final ArrayList<Map.Entry<String, List<Feature>>> cs = new ArrayList<Map.Entry<String, List<Feature>>>();
            final ArrayList<String> fct = new ArrayList<String>();
            fv.createFCFHT(cs, fct, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    ((QuickAdapter<Map.Entry<String, List<Feature>>>) ll_fcfh_content.getTag()).replaceAll(cs);
                    ((QuickAdapter<String>) ll_fct_content.getTag()).replaceAll(fct);
                    return null;
                }
            });
        }
    }

    //region 属性页

    //end-----------------------------------20180709----------------------------------------------------------------------------------------------
}
