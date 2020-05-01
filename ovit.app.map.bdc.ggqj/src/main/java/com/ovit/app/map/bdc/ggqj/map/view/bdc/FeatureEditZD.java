package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.ai.component.custom.CustomImagesView;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.AlertDialog;
import com.ovit.app.ui.dialog.DialogBuilder;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.ImageUtil;
import com.ovit.app.util.ResourceUtil;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Lichun on 2017/4/5.
 //region 常量
 ///endregion

 //region 字段
 ///endregion

 //region 构造函数
 ///endregion

 //region 重写函数和回调
 ///endregion

 //region 公有函数
 ///endregion

 //region 私有函数
 ///endregion

 //region 面积计算
 ///endregion

 //region 内部类或接口
 ///endregion

 */

public class FeatureEditZD extends FeatureEdit {
    //region 常量
    final static String TAG = "FeatureEditZD";
    //endregion

    //region基础字段
    FeatureViewZD fv;
    private Map<String, String> map_tzm_zddm = new HashMap<>();
    final List<Feature> fs_jzd = new ArrayList<Feature>();
    final List<Feature> fs_jzx = new ArrayList<>();
    List<Map<String, Object>> fs_jzqz = new ArrayList<>();
    final Map<String, Feature> map_jzx = new HashMap<>();
    //endregion

    //region 界面相关属性
    final int spn_resid = R.layout.app_ui_ai_aimap_feature_spn_item_sm;
    private EditText et_zddm;
    private EditText et_ybzddm;
    private EditText et_bdcdyh;
    private Spinner spn_qllx;

    private String old_bdcdyh;
    private String old_zddm;
    private String old_qlrxm;
    private String old_qlrzjh;
    private EditText et_qslyzmcl;

    View view_zrz;
    View view_hjxx;
    View view_bdcdy;
    View view_ftqk;
    //endregion

    //region  重写父类方法
    // 创建时调用
    @Override
    public void onCreate() {
        super.onCreate();
        // 使用 fv
        if (super.fv instanceof FeatureViewZD) {
            this.fv = (FeatureViewZD) super.fv;
        }
    }

    // 初始化
    @Override
    public void init() {
        super.init();
        // 菜单 基本信息 界址情况 界址签字 宗地草图 自然幢 分摊情况 不动产单元
        menus = new int[]{R.id.ll_info, R.id.ll_jzd, R.id.ll_jzx, R.id.ll_zdct, R.id.ll_zrz, R.id.ll_ft, R.id.ll_bdcdy,R.id.ll_dzqz,R.id.ll_info2};
    }

    // 填充界面
    @Override
    public void build() {
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_zd, v_content);
        try {
            double area = 0;
            int scale = 200;
            Geometry g = feature.getGeometry();
            if (g != null) {
                // 单位米
                area = MapHelper.getArea(mapInstance, g);
            }
            if (0d == AiUtil.GetValue(feature.getAttributes().get("YBZDDM"), 0d)) {
                feature.getAttributes().put("YBZDDM", id);
            }
            if (0d == AiUtil.GetValue(feature.getAttributes().get(FeatureHelper.TABLE_ATTR_ZDMJ), 0d)) {
                feature.getAttributes().put(FeatureHelper.TABLE_ATTR_ZDMJ, area);
            }
            feature.getAttributes().put("GLBLC", "1:" + scale);
            mapInstance.fillFeature(feature);
            old_bdcdyh = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_BDCDYH, "");
            old_zddm = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ZDDM, "");
            old_qlrxm = FeatureHelper.Get(feature, "QLRXM", "");
            old_qlrzjh = FeatureHelper.Get(feature, "QLRZJH", "");
            // 填充控件
            fillView(v_feature);
            map_tzm_zddm.put(fv.getTzm(old_zddm), old_zddm);
            // 特殊操作
            et_zddm = (EditText) v_feature.findViewById(R.id.et_zddm);
            et_ybzddm = (EditText) v_feature.findViewById(R.id.et_ybzddm);
            et_bdcdyh = (EditText) v_feature.findViewById(R.id.et_bdcdyh);
            spn_qllx = (Spinner) v_feature.findViewById(R.id.spn_qllx);
            final String bdcdyh = AiUtil.GetValue(et_bdcdyh.getText(), "");
            et_bdcdyh.setEnabled(false);
            et_zddm.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    changeZddm(v_feature);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            v_feature.findViewById(R.id.tv_autozddm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newZddm();
                }
            });

            String qllx = AiUtil.GetValue(feature.getAttributes().get("QLLX"), "6");
            for (int i = 0; i < spn_qllx.getAdapter().getCount(); i++) {
                String v = spn_qllx.getAdapter().getItem(i) + "";
                if (v.contains("[" + qllx + "][" + fv.getTzm() + "]")) {
                    spn_qllx.setSelection(i);
                    break;
                }
            }

            spn_qllx.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    changeQllx(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    setValue(feature, "QLLX", null);
                }
            });

            CustomImagesView civ_qlrzjh = (CustomImagesView) v_feature.findViewById(R.id.civ_qlrzjh);
            String filename = AiUtil.GetValue(civ_qlrzjh.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
            civ_qlrzjh.setName(filename).setDir(FileUtils.getAppDirAndMK(getpath_root() + FeatureHelper.FJCL + filename + "/")).setOnRecognize_SFZ(new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    updateQlr((Map<String, String>) t_, v_feature);
                    return null;
                }
            });

            CustomImagesView civ_fzrzjbh = (CustomImagesView) v_feature.findViewById(R.id.civ_fzrzjbh);
            filename = AiUtil.GetValue(civ_fzrzjbh.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
            civ_fzrzjbh.setName(filename).setDir(FileUtils.getAppDirAndMK(getpath_root() + FeatureHelper.FJCL + filename + "/")).setOnRecognize_SFZ(new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    updateFr((Map<String, String>) t_, v_feature);
                    return null;
                }
            });

            CustomImagesView civ_hkb = (CustomImagesView) v_feature.findViewById(R.id.civ_hkb);
            filename = AiUtil.GetValue(civ_hkb.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
            civ_hkb.setName(filename, activity).setDir(FileUtils.getAppDirAndMK(getpath_root() + FeatureHelper.FJCL + filename + "/"));

            CustomImagesView civ_dlrzjbh = (CustomImagesView) v_feature.findViewById(R.id.civ_dlrzjbh);
            filename = AiUtil.GetValue(civ_dlrzjbh.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
            civ_dlrzjbh.setName(filename).setDir(FileUtils.getAppDirAndMK(getpath_root() + FeatureHelper.FJCL + filename + "/")).setOnRecognize_SFZ(new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    updateDlr((Map<String, String>) t_, v_feature);
                    return null;
                }
            });

            CustomImagesView civ_tdqslyzm = (CustomImagesView) v_feature.findViewById(R.id.civ_tdqslyzm);
            filename = AiUtil.GetValue(civ_tdqslyzm.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
            civ_tdqslyzm.setName(filename, activity).setDir(FileUtils.getAppDirAndMK(getpath_root() + FeatureHelper.FJCL + filename + "/"));

            v_feature.findViewById(R.id.tv_autojzd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int_jzdxqz();
                }
            });
            v_feature.findViewById(R.id.tv_autojzx).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int_jzdxqz();
                }
            });

            final Spinner spn_auto_jblx = (Spinner) view.findViewById(R.id.spn_auto_jblx);
            final Spinner spn_auto_jzxlb = (Spinner) view.findViewById(R.id.spn_auto_jzxlb);
            final Spinner spn_auto_jzxwz = (Spinner) view.findViewById(R.id.spn_auto_jzxwz);

            fillView(spn_auto_jblx, feature, "jblx", DicUtil.getArray(activity, "jblx"));
            fillView(spn_auto_jzxlb, feature, "jzxlb", DicUtil.getArray(activity, "jzxlb"));
            fillView(spn_auto_jzxwz, feature, "jzxwz", DicUtil.getArray(activity, "jzxwz"));

            final boolean[] first = {true, true, true};
            spn_auto_jblx.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spn_auto_jblx.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (first[0]) {
                        first[0] = false;
                    } else {
                        String value = StringUtil.substr(((TextView) view).getText() + "", "[", "]");
                        autoSettingAttr(fs_jzd, "JBLX", value);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spn_auto_jzxlb.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (first[1]) {
                        first[1] = false;
                    } else {
                        String value = StringUtil.substr(((TextView) view).getText() + "", "[", "]");
                        autoSettingAttr(fs_jzd, "JZXLB", value);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spn_auto_jzxwz.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (first[2]) {
                        first[2] = false;
                    } else {
                        String value = StringUtil.substr(((TextView) view).getText() + "", "[", "]");
                        autoSettingAttr(fs_jzd, "JZXWZ", value);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            TextView tv_sbz = (TextView) v_feature.findViewById(R.id.tv_sbz);
            tv_sbz.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    identyZrz(null);
                }
            });

            v_feature.findViewById(R.id.tv_autohsjzmj).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hsjzmj(v_feature);
                }
            });

            TextView tv_reload_ct = (TextView) v_feature.findViewById(R.id.tv_reload_ct);
            tv_reload_ct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    load_zdct(true);
                }
            });

            TextView tv_reload_fct = (TextView) v_feature.findViewById(R.id.tv_reload_fct);
            tv_reload_fct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    load_fct(true);
                }
            });

            // 不用判断多幢属于同一权利人
//            tv_tobdc.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    fv.to_bdc(feature);
//                }
//            });

            et_qslyzmcl = (EditText) v_feature.findViewById(R.id.et_qslyzmcl);
            et_qslyzmcl.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    selectQslyzm();
                    return false;
                }
            });
            // 备注
            final EditText et_bz = (EditText) v_feature.findViewById(R.id.et_bz);
            final boolean[] checkdItems = {true, false, false, false};
            et_bz.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    selectBz(et_bz, checkdItems);
                    return false;
                }
            });

            final EditText et_qlqksm = (EditText) v_feature.findViewById(R.id.et_qlqksm);
            et_qlqksm.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return selectQlqksm(et_qlqksm);
                }
            });

            v_feature.findViewById(R.id.tv_create_bdcdy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addBdcdy();
                }
            });
            v_feature.findViewById(R.id.tv_reload_bdcdy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                 reload_bdcdy();
                }
            });

            v_feature.findViewById(R.id.tv_create_ft).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFtqk();
                }
            });
            v_feature.findViewById(R.id.tv_reload_ft).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reload_ftqk();
                }
            });


        } catch (Exception es) {
            Log.e(TAG, "build: 构建失败", es);
        }
    }

    // 设置菜单和工具
    @Override
    public void build_opt() {
        super.build_opt();
        addMenu("基本信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_info);
            }
        });
        addMenu(FeatureHelper.LAYER_NAME_ZRZ, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_zrz);
                load_zrz();
            }
        });
        addMenu("不动产单元", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_bdcdy);
                load_bdcdy();
            }
        });
        addMenu("界址情况", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_jzd);
                load_jzdxqx();
            }
        });
        addMenu("界址签字", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_jzx);
                load_jzdxqx();
            }
        });
        addMenu("电子手签", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_dzqz);
                load_dzsq();
            }
        });

        addMenu("宗地草图", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_zdct);
                load_zdct(false);
                load_fct(false);
                load_fwfct();
            }
        });
        addMenu("分摊情况", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_ft);
                load_ftqk();
            }
        });
        addMenu("其他信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_info2);
            }
        });

        addAction("画幢", R.mipmap.app_map_layer_zrz, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        drawZRZ();
                        return null;
                    }
                });
            }
        });
        tabQlr();
    }

    // 保存数据
    @Override
    public void update(final AiRunnable callback) {
        try {
            mapInstance.fillFeature(feature);
            super.update(new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    update_zddm(new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            update_qlrxx(callback);
                            return null;
                        }
                    });
                    return null;
                }
            });
//            }
        } catch (Exception es) {
            ToastMessage.Send(activity, "更新属性失败!", TAG, es);
        }
    }
    ///endregion

    //region 公用方法

    public static String GetID(Feature feature) {
        return FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ZDDM, "");
    }

    //通过ZD创建新权利人，可选是否解除已有关联
    public static void createNewQlrByZD(final MapInstance mapInstance, final Feature feature_zd, final boolean save
            , final boolean unassociate, final boolean deep_unassociate, final AiRunnable callback) {
        try {
            final String pid = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "", "XMBM", "xmbm");
            FeatureEditQLR.NewID(mapInstance, pid, "", new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    String id = t_ + "";
                    final Feature feature_new_qlr = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX).createFeature();

                    feature_new_qlr.getAttributes().put("QLRDM", id);


                    //拷贝资料
                    String f_zd_path = mapInstance.getpath_feature(feature_zd);
                    String f_qlr_path = mapInstance.getpath_feature(feature_new_qlr);

                    String f_zd_zjh_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/权利人证件号/");
                    String f_qlr_zjh_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/证件号/");
                    FileUtils.copyFile(f_zd_zjh_path, f_qlr_zjh_path);

                    String f_zd_zmcl_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/土地权属来源证明材料/");
                    String f_qlr_zmcl_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/土地权属来源证明材料/");
                    FileUtils.copyFile(f_zd_zmcl_path, f_qlr_zmcl_path);

                    String f_zd_hkb_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/户口簿/");
                    String f_qlr_hkb_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/户口簿/");
                    FileUtils.copyFile(f_zd_hkb_path, f_qlr_hkb_path);

                    if (unassociate) {
                        FeatureEditQLR.unassociateQlrAndBdc(mapInstance, feature_zd, deep_unassociate, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (save) {
                                    MapHelper.saveFeature(feature_new_qlr, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            AiRunnable.Ok(callback, feature_new_qlr, feature_new_qlr);
                                            return null;
                                        }
                                    });
                                } else {
                                    AiRunnable.Ok(callback, feature_new_qlr, feature_new_qlr);
                                }
                                return null;
                            }
                        });
                    } else {
                        if (save) {
                            MapHelper.saveFeature(feature_new_qlr, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    AiRunnable.Ok(callback, feature_new_qlr, feature_new_qlr);
                                    return null;
                                }
                            });
                        } else {
                            AiRunnable.Ok(callback, feature_new_qlr, feature_new_qlr);
                        }
                    }
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "通过宗地创建新权利人失败!" + es);
            AiRunnable.Ok(callback, false, false);
        }

    }


    ///endregion

    //region 界面调用方法，私用方法

    //宗地分摊
    private void addFtqk() {
        AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_more_blue, "新增宗地分摊")
                .addContentView("确定要新增宗地分摊吗?", "该操作将根据宗地选择的分摊计算分摊系数！")
                .setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        // 加载界面
                        dialog.dismiss();
                        View ft_view = view.findViewById(R.id.ll_ft_content);
                        FeatureEditFTQK.addFtToZD(mapInstance, feature, ft_view, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (t_ != null) {
                                    reload_ftqk();
                                }
                                return null;
                            }
                        });

                    }
                }).show();

    }

    //不动产单元
    private void addBdcdy() {
        fv.checkBdcdy(feature, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiDialog aiDialog = AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_more_blue, "不动产单元设定");
                if (t_!=null){
                    //可以设定不动产单元
                    aiDialog.addContentView("确定要生成一个不动产单元吗?", "该操作将根据宗地与该宗地上所有定着物共同设定一个不动产单元！");
                    aiDialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            // 加载界面
                            fv.createBdcdy(feature, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    final Feature featureBdcdy = (Feature) t_;
                                    FeatureViewQLR featureViewQLR = (FeatureViewQLR) mapInstance.newFeatureView(featureBdcdy);
                                    featureViewQLR.update_gyrxx(new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            mapInstance.viewFeature(featureBdcdy);
                                            dialog.dismiss();
                                            return null;
                                        }
                                    });

                                    return null;
                                }
                            });
                        }
                    }).show();

                }else {
                    aiDialog.addContentView("不能设定不动产单元", (String) objects[0]+"已经设定了不动产单元！");
                    aiDialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM,null).show();
                }

                return null;
            }
        });
    }

    private void update_zddm(final AiRunnable callback) {
        final String bdcdyh = AiUtil.GetValue(feature.getAttributes().get(FeatureHelper.TABLE_ATTR_BDCDYH), "");
        final String zddm = AiUtil.GetValue(feature.getAttributes().get(FeatureHelper.TABLE_ATTR_ZDDM), "");

        // 如果不动产单元号或是宗地代码发生变化的 需要级联更新
        if ((bdcdyh != null && !bdcdyh.equals(old_bdcdyh)) || (zddm != null && !zddm.equals(old_zddm))) {
            final List<Feature> fs = new ArrayList<>();
            final List<Feature> fs_jzd = new ArrayList<Feature>();
            final List<Feature> fs_jzx = new ArrayList<Feature>();
            final List<Feature> fs_zrz = new ArrayList<Feature>();
            final List<Feature> fs_z_fsjg = new ArrayList<Feature>();
            final List<Feature> fs_h = new ArrayList<Feature>();
            final List<Feature> fs_h_fsjg = new ArrayList<Feature>();

            FeatureView.LoadJzdx_AndZhfs(mapInstance, feature, fs_jzd, fs_jzx, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    for (Feature f : fs_jzd) {
                        FeatureHelper.Set(f, "ZDZHDM", zddm);
                    }
                    mapInstance.fillFeature(fs_jzd);
                    for (Feature f : fs_jzx) {
                        FeatureHelper.Set(f, "ZDZHDM", zddm);
                    }
                    mapInstance.fillFeature(fs_jzx);
                    mapInstance.fillFeature(fs_zrz);
                    for (Feature f : fs_z_fsjg) {
                        String old_id = FeatureHelper.Get(f, "ID", "");
                        String id = zddm + StringUtil.substr(old_id, zddm.length());
                        FeatureHelper.Set(f, "ID", id);
                        String old_zid = FeatureHelper.Get(f, "ZID", "");
                        String zid = zddm + StringUtil.substr(old_zid, zddm.length());
                        FeatureHelper.Set(f, "ZID", zid);
                    }
                    mapInstance.fillFeature(fs_z_fsjg);
                    for (Feature f : fs_h) {
                        String old_id = FeatureHelper.Get(f, "ID", "");
                        String id = zddm + StringUtil.substr(old_id, zddm.length());
                        FeatureHelper.Set(f, "ID", id);
                    }
                    mapInstance.fillFeature(fs_h);
                    for (Feature f : fs_h_fsjg) {
                        String old_id = AiUtil.GetValue(f.getAttributes().get("ID"), "");
                        String id = zddm + StringUtil.substr(old_id, zddm.length());
                        f.getAttributes().put("ID", id);
                        String old_hid = AiUtil.GetValue(f.getAttributes().get("HID"), "");
                        String hid = zddm + StringUtil.substr(old_hid, zddm.length());
                        f.getAttributes().put("HID", hid);
                    }
                    mapInstance.fillFeature(fs_h_fsjg);
                    fs.addAll(fs_jzd);
                    fs.addAll(fs_jzx);
                    fs.addAll(fs_zrz);
                    fs.addAll(fs_z_fsjg);
                    fs.addAll(fs_h);
                    fs.addAll(fs_h_fsjg);
                    MapHelper.saveFeature(fs, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            clear();
                            mapInstance.setSelectFeature(feature);
                            old_zddm = zddm;
                            old_bdcdyh = bdcdyh;
                            AiRunnable.Ok(callback, t_);
                            return null;
                        }
                    });
                    return null;
                }
            });
        } else {
            AiRunnable.Ok(callback, null);
        }
    }

    private void update_qlrxx(final AiRunnable callback) {
        final String qlrxm = AiUtil.GetValue(feature.getAttributes().get("QLRXM"), "");
        final String qlrzjh = AiUtil.GetValue(feature.getAttributes().get("QLRZJH"), "");
        if ((!qlrzjh.equals(old_qlrzjh))) { /*在权利人和ZD一对多的情况下，一旦QLRXM或QLRZJH发生变化，则需要新建权利人 而清除多余权利人的操作则应在FeatureEditQLR中做 20180813*/
            AiRunnable.Ok(callback, null);
        } else {
            AiRunnable.Ok(callback, null);
        }
    }

    private void update_zrzh() {
        final List<Feature> features_zrz = new ArrayList<>();
        String where = StringUtil.WhereByIsEmpty(id) + " ZDDM ='" + id + "'  ";
        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZRZ, FeatureHelper.LAYER_NAME_ZRZ), where, 0, features_zrz, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {

                AiDialog dialog = AiDialog.get(activity, new QuickAdapter<Feature>(activity, R.layout.app_ui_ai_list_item, features_zrz) {
                    @Override
                    protected void convert(BaseAdapterHelper helper, Feature item) {
                        helper.setImageResource(R.id.v_icon, R.mipmap.app_map_layer_kzd);
                        helper.setText(R.id.v_desc, item.getAttributes().get("ZRZH") + "");
                        helper.setVisible(R.id.v_desc, true);

                    }
                }).show();

                dialog.setHeaderView(R.mipmap.app_icon_folder_blue, "获取到" + features_zrz.size() + "个自然幢");
                dialog.setFooterView(AiDialog.CENCEL, "修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });

                return null;
            }
        });
    }

    private boolean selectQlqksm(final EditText et_qlqksm) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle("房屋共用情况");
        dialog.setMessage("本宗地权属有共有人吗?");
        dialog.setNegativeButton("没有", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                et_qlqksm.setText("无共有情况"); //20180712
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton("有", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                et_qlqksm.setText("有(详情请见：共有/共用宗地面积分摊表)");
                dialog.dismiss();
            }
        });
        dialog.show();
        return false;
    }

    // 设定不动产单元
    private void createNewBDCDY() {
        try {
            fv.createNewBDCDY(feature, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    reload_zrz();
                    return null;
                }
            });
        } catch (Exception es) {
            ToastMessage.Send(activity, "设定不动产单元失败", es);
        }
    }

    // 备注
    private void selectBz(final EditText et_bz, final boolean[] checkdItems) {
        try {

            String resname = AiUtil.GetValue(et_bz.getContentDescription(), "");
            final CharSequence[] items = DicUtil.getArray(activity, resname);
//                            final CharSequence[] items = activity.getResources().getTextArray(ResourceUtil.getResourceId(activity, resname, "array"));
            DialogBuilder.confirm(activity, "权属说明", null, null, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 确定
                    String text = "";
                    for (int i = 0; i < checkdItems.length; i++) {
                        if (checkdItems[i]) {
                            text += items[i] + "\n";
                        }
                    }
                    et_bz.setText(text);
                }
            }, AiDialog.CENCEL, null).setMultiChoiceItems(items, checkdItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i, boolean b) {

                }
            }).create().show();

        } catch (Exception es) {
            Log.e(TAG, "fillView: ", es);
        }
    }

    //权属材料来源
    private void selectQslyzm() {
        final String tdzh = FeatureHelper.Get(feature, "TDZH", "");
        try {
            String resname = AiUtil.GetValue(et_qslyzmcl.getContentDescription(), "");
            final CharSequence[] items = DicUtil.getArray(activity, resname);
//                            final CharSequence[] items = activity.getResources().getTextArray(ResourceUtil.getResourceId(activity, resname, "array"));
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            //设置标题
            builder.setTitle("权属材料来源");
            //设置图标

            builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0) {
                        et_qslyzmcl.setText(items[i] + "(" + tdzh + ")");
                    } else {
                        et_qslyzmcl.setText(items[i]);
                    }
                }
            });
            builder.create();
            builder.show();
        } catch (Exception es) {
            Log.e(TAG, "fillView: ", es);
        }
    }

    /**
     * 计算宗地：宗地面积，建筑占地面积，建筑面积
     * @param v_feature
     */
    private void hsjzmj(final LinearLayout v_feature) {
        AiDialog.get(activity, "自动计算面积", "是否要重新计算该宗地面积、建筑占地面积、建筑面积么？", null, "否，取消", "是，重新计算", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                fv.update_Area(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        if(FeatureHelper.Get(feature,FeatureViewZD.TABLE_ATTR_FTXS_ZD,0.0d)>0){
                            fv.update_ftxs(new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    fillView(v_feature, feature, FeatureHelper.TABLE_ATTR_ZDMJ);
                                    fillView(v_feature, feature, "JZMJ");
                                    fillView(v_feature, feature, "JZZDMJ");
                                    fillView(v_feature, feature, FeatureViewZD.TABLE_ATTR_FTXS_ZD);
                                    ToastMessage.Send(activity, "自动计算面积完成！");
                                    dialog.dismiss();
                                    return null;
                                }
                            });
                        }else {
                            fillView(v_feature, feature, FeatureHelper.TABLE_ATTR_ZDMJ);
                            fillView(v_feature, feature, "JZMJ");
                            fillView(v_feature, feature, "JZZDMJ");
                            ToastMessage.Send(activity, "自动计算面积完成！");
                            dialog.dismiss();
                        }
                        return null;
                    }
                });
            }
        }).show();
    }

    //宗地识别自然幢
    private void identyZrz(final AiRunnable callback) {
        fv.identyZrz(true, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                fv.update_Area(new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        // 更新界面
                        fillView(v_content, feature, "YCJZMJ");
                        fillView(v_content, feature, "JZMJ");
                        fillView(v_content, feature, "JZZDMJ");
                        reload_zrz();
                        return null;
                    }
                });
                return null;
            }
        });
    }

    //身份证识别回填代理人信息
    private void updateDlr(Map<String, String> t_, LinearLayout v_feature) {
        Map<String, String> datas = t_;
        String xm = datas.get("xm");
        String sfzh = datas.get("sfzh");
        String zz = datas.get("zz");
        if (StringUtil.IsNotEmpty(xm)) {
            ((EditText) v_feature.findViewById(R.id.et_dlrxm)).setText(xm);
        }
        if (StringUtil.IsNotEmpty(sfzh)) {
            ((EditText) v_feature.findViewById(R.id.et_dlrzjbh)).setText(sfzh);
        }
    }

    //身份证识别回填法人信息
    private void updateFr(Map<String, String> t_, LinearLayout v_feature) {
        Map<String, String> datas = t_;
        String xm = datas.get("xm");
        String sfzh = datas.get("sfzh");
        String zz = datas.get("zz");
        if (StringUtil.IsNotEmpty(xm)) {
            ((EditText) v_feature.findViewById(R.id.et_fzrxm)).setText(xm);
        }
        if (StringUtil.IsNotEmpty(sfzh)) {
            ((EditText) v_feature.findViewById(R.id.et_fzrzjbh)).setText(sfzh);
        }
    }

    //身份证识别回填权利人信息
    private void updateQlr(Map<String, String> datas, LinearLayout v_feature) {
        String xm = datas.get("xm");
        String sfzh = datas.get("sfzh");
        String zz = datas.get("zz");

        try {
            if (StringUtil.IsNotEmpty(xm) && StringUtil.IsNotEmpty(sfzh) && StringUtil.IsNotEmpty(zz)) {
                ((EditText) v_feature.findViewById(R.id.et_qlrxm)).setText(xm);
                ((EditText) v_feature.findViewById(R.id.et_qlrzjh)).setText(sfzh);
                ((EditText) v_feature.findViewById(R.id.et_qlrtxdz)).setText(zz);
            }

        } catch (Exception e) {
            Log.e(TAG, "身份证识别信息填充出错" + xm + ":" + sfzh + ":" + zz, e);
        }
    }

    // 改变权利类型
    private void changeQllx(int position) {
        String value = AiUtil.GetValue(spn_qllx.getAdapter().getItem(position), "");
        if (value.contains("]")) {
            value = value.substring(1, value.indexOf("]"));
        }
        setValue(feature, "QLLX", value);
        value = AiUtil.GetValue(spn_qllx.getAdapter().getItem(position), "");
        int i_b = value.indexOf("[", value.indexOf("]"));
        int i_e = value.indexOf("]", value.indexOf("]") + 1);
        if (i_b > 0 && i_e > 0) {
            String new_tzm = value.substring(i_b + 1, i_e);
            if (!new_tzm.equals(fv.getTzm())) {
                String map_zddm = map_tzm_zddm.get(new_tzm);
                if (map_zddm != null && map_zddm.length() == 19) {
                    et_zddm.setText(map_zddm);
                } else {
                    fv.newZddm(fv.getZddm(), new_tzm, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            String zddm = t_ + "";
                            et_zddm.setText(zddm);
                            return null;
                        }
                    });
                }
            }
        }
    }
    //改变宗地代码
    private void changeZddm(LinearLayout v_feature) {
        String new_zddm = et_zddm.getText().toString();
        map_tzm_zddm.put(fv.getTzm(), fv.getZddm());
        et_ybzddm.setText(new_zddm);
        setValue(feature, "PRO_ZDDM_F", StringUtil.substr_last(id, 7));
//        et_bdcdyh.setText(et_zddm.getText() + "F" + (sh_dzsfty.isChecked() ? "99990001" : "00000000"));
        String resname = "qlxz_j";
        if (et_zddm.getText().toString().contains("G")) {
            resname = "qlxz_g";
        }
        try {
            final CharSequence[] strings;
            strings = activity.getResources().getTextArray(ResourceUtil.getResourceId(activity, resname, "array"));
            fillView((Spinner) v_feature.findViewById(R.id.spn_qlxz), feature, "qlxz", strings);
        } catch (Exception es) {

        }
    }

    // 实例化新的宗地代码逻辑
    private void newZddm() {
        String desc = "该操作主要是在地籍子区范围内重新编制宗地代码，宗地代码发生变化后该宗地范围的房屋需要重新识别，请谨慎处理！";
        final AiDialog aidialog = AiDialog.get(activity);
        aidialog.setHeaderView(R.mipmap.app_icon_warning_red, "不可逆操作提醒")
                .addContentView("确定要重新编号么？", desc)
                .setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        fv.newZddm(new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                String zddm = t_ + "";
                                final Map<String, Object> map = new LinkedHashMap<>();
                                map.put("zddm", zddm);
                                aidialog.setContentView("重新获取的宗地代码为：")
                                        .addContentView(aidialog.getEditView("宗地代码", map, "zddm"))
                                        .setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                et_zddm.setText(AiUtil.GetValue(map.get("zddm")));
                                                aidialog.dismiss();
                                            }
                                        });
                                return null;
                            }
                        });
                    }
                }).show();
    }

    // 权利人、法人、负责人切换
    private void tabQlr() {
        final int[][] qlr_menus = new int[][]{
                new int[]{R.id.tv_qlr, R.id.ll_qlr},
                new int[]{R.id.tv_fr, R.id.ll_fr},
                new int[]{R.id.tv_dlr, R.id.ll_dlr},
        };
        for (final int[] item : qlr_menus) {
            view.findViewById(item[0]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int[] item_ : qlr_menus) {
                        view.findViewById(item_[1]).setVisibility(item_[0] == item[0] ? View.VISIBLE : View.GONE);
                    }
                }
            });
        }
    }

    // 绘制幢并重新加载数据
    private void drawZRZ() {
        try {
            fv.draw_zrz(feature, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    reload_zrz();
                    return null;
                }
            });
        } catch (Exception es) {
            ToastMessage.Send(activity, "画自然幢失败", es);
        }
    }

    // 清除缓存
    private void clear() {
        view_zrz = null;
        clear_jzdxqz();
    }

    private void autoSettingAttr(final List<Feature> fs_jzd, final String type, final String value) {

        AiDialog.get(activity, "一键设置属性", "是否需要快速设置所有界址点的属性？", null, "否，取消", "是，设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                if (fs_jzd.size() > 0) {
                    LinearLayout list_jzd = (LinearLayout) view.findViewById(R.id.ll_list_jzd);
                    for (Feature feature : fs_jzd) {
                        feature.getAttributes().put(type, value);
                    }

                    MapHelper.saveFeature(fs_jzd, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            clear_jzdxqz();
                            load_jzdxqx();
                            dialog.dismiss();
                            return null;
                        }
                    });
                }

            }
        }).show();

    }

    //endregion

    //region 界址点线签字
    // 清除界址点线签字缓存数据
    private void clear_jzdxqz() {
        fs_jzd.clear();
        fs_jzx.clear();
        fs_jzqz.clear();
        map_jzx.clear();
    }

    // 初始化界址点线签字缓存数据
    private void int_jzdxqz() {
        FeatureEditJZD.UpdateJZD(mapInstance, feature, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                FeatureEditJZX.UpdateJZX(mapInstance, feature, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        clear_jzdxqz();
                        load_jzdxqx();
                        return null;
                    }
                });
                return null;
            }
        });
    }

    // 加载界址点线签章
    private void load_jzdxqx() {
        if (fs_jzd.size() > 0) {
            return;
        }
        try {
            clear_jzdxqz();
//             final long time = System.currentTimeMillis();
            FeatureView.LoadJzdxqz(mapInstance, feature, fs_jzd, fs_jzx, map_jzx, fs_jzqz, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
//                    Log.e(TAG, "加载界址点线时间"+((System.currentTimeMillis()-time)/1000)+ "秒" );
                    long time = System.currentTimeMillis();
                    getAdapter_jzdx().replaceAll(fs_jzd);
//                    Log.e(TAG, "填充界址点时间"+((System.currentTimeMillis()-time)/1000)+ "秒" );
                    time = System.currentTimeMillis();
                    getAdapter_jzqz().replaceAll(fs_jzqz);
//                    Log.e(TAG, "填充界址线时间"+((System.currentTimeMillis()-time)/1000)+ "秒" );
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "load_jzdx: ", es);
        }
    }
    //界址点线适配器
    private QuickAdapter<Feature> getAdapter_jzdx() {
        final LinearLayout list_jzd = (LinearLayout) view.findViewById(R.id.ll_list_jzd);
        if (list_jzd.getTag() != null) {
            return (QuickAdapter<Feature>) list_jzd.getTag();
        } else {
            QuickAdapter<Feature> adapter_jzdx = new QuickAdapter<Feature>(activity, R.layout.app_ui_ai_aimap_feature_zd_jzd, new ArrayList<Feature>()) {
                @Override
                protected void convert(BaseAdapterHelper helper, final Feature f_jzd) {
                    View view = helper.getView();
                    int i = helper.getPosition();
                    int j = (i + 1 < getData().size()) ? i + 1 : 0;
                    Feature f_jzd2 = getData().get(j);
                    final String name = FeatureHelper.Get(f_jzd, "JZDH", "");
                    String name2 = FeatureHelper.Get(f_jzd2, "JZDH", "");
                    Feature f_jzx = FeatureEditJZX.Get(map_jzx, f_jzd, f_jzd2);
                    String jzx_zdzhdm = FeatureHelper.Get(f_jzx, "ZDZHDM", "");

                    helper.setText(R.id.tv_l_name, name + "-" + name2);
                    helper.setText(R.id.tv_index, "" + (i + 1));
                    helper.setText(R.id.tv_l_length, AiUtil.Scale(FeatureHelper.Get(f_jzx, "JZXCD", 0d), 2, 0) + "米");

                    fillView(((TextView) view.findViewById(R.id.et_p_name)), f_jzd, "JZDH");
                    fillView(((TextView) view.findViewById(R.id.et_jzxsm)), f_jzx, "JZXSM");

                    fillView(((Spinner) view.findViewById(R.id.spn_jblx)), spn_resid, f_jzd, "jblx", DicUtil.getArray(activity, "jblx"));
                    fillView(((Spinner) view.findViewById(R.id.spn_jzxlb)), spn_resid, f_jzx, "jzxlb", DicUtil.getArray(activity, "jzxlb"));
                    fillView(((Spinner) view.findViewById(R.id.spn_jzxlb)), spn_resid, f_jzd, "jzxlb", DicUtil.getArray(activity, "jzxlb"));

                    if (!jzx_zdzhdm.startsWith(getId())) {
                        fillView(((Spinner) view.findViewById(R.id.spn_jzxwz)), spn_resid, f_jzx, "jzxwz", DicUtil.getArray(activity, "jzxwz"));
                        fillView(((Spinner) view.findViewById(R.id.spn_jzxwz)), spn_resid, f_jzd, "jzxwz", DicUtil.getArray(activity, "jzxwz"));
                    } else {
                        // 邻宗地
                        fillView(((Spinner) view.findViewById(R.id.spn_jzxwz)), spn_resid, f_jzx, "jzxwz", DicUtil.getArray(activity, "jzxwz"));
                        fillView(((Spinner) view.findViewById(R.id.spn_jzxwz)), spn_resid, f_jzd, "jzxwz", DicUtil.getArray(activity, "jzxwz"));
                    }

                    Bitmap bm = MapHelper.geometry_icon(new Geometry[]{feature.getGeometry(), f_jzx.getGeometry(), f_jzd.getGeometry()}, 100, 100, new int[]{R.color.app_theme_fore, Color.RED, Color.RED}, new int[]{1, 5, 15});
                    helper.setImageBitmap(R.id.v_icon, bm);
                    helper.setOnClickListener(R.id.v_icon, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MapHelper.showCallout(mapInstance.map, name, (Point) f_jzd.getGeometry());
                        }
                    });
                }
            };
            adapter_jzdx.adpter(list_jzd);
            list_jzd.setTag(adapter_jzdx);
            return adapter_jzdx;
        }
    }
    //界址签章适配器
    private QuickAdapter<Map<String, Object>> getAdapter_jzqz() {
        final LinearLayout ll_list_jzqz = (LinearLayout) view.findViewById(R.id.ll_list_jzqz);
        if (ll_list_jzqz.getTag() != null) {
            return (QuickAdapter<Map<String, Object>>) ll_list_jzqz.getTag();
        } else {
            QuickAdapter<Map<String, Object>> adapter_jzqz = new QuickAdapter<Map<String, Object>>(activity, R.layout.app_ui_ai_aimap_feature_zd_jzqz, new ArrayList<Map<String, Object>>()) {
                @Override
                protected void convert(BaseAdapterHelper helper, final Map<String, Object> f_jzqz) {
                    int i = helper.getPosition();
                    String zjh = AiUtil.GetValue(f_jzqz.get("JZXZJH"), "");
                    final String name = AiUtil.GetValue(f_jzqz.get("JZXQDH"), "") + " " +
                            (StringUtil.IsEmpty(zjh) ? "" : "[" + zjh + "]") + " " +
                            AiUtil.GetValue(f_jzqz.get("JZXZDH"), "");
                    helper.setText(R.id.tv_l_name, name);

                    helper.setText(R.id.et_jzqzbrq, AiUtil.GetValue(f_jzqz.get("JZQZBRQ"), ""));
                    helper.setText(R.id.et_xlzdqlr, StringUtil.substr_last(AiUtil.GetValue(f_jzqz.get("XLZDQLR"), ""), 7));
                    helper.setText(R.id.et_lzdzjr, AiUtil.GetValue(f_jzqz.get("LZDZJR"), ""));
                    helper.setText(R.id.et_bzdzjr, AiUtil.GetValue(f_jzqz.get("BZDZJR"), ""));

                    //            fillView_seldate(((TextView) view.findViewById(R.id.et_jzqzbrq)), item, "jzqzbrq");
                    //            fillView(((TextView) view.findViewById(R.id.et_xlzdqlr)), item, "xlzdqlr");
                    //            fillView(((TextView) view.findViewById(R.id.et_lzdzjr)), item, "lzdzjr");
                    //            fillView(((TextView) view.findViewById(R.id.et_bzdzjr)), item, "bzdzjr");
                    final Geometry g = (Geometry) f_jzqz.get("Geometry");
                    Bitmap bm = MapHelper.geometry_icon(new Geometry[]{feature.getGeometry(), g}, 100, 100, new int[]{R.color.app_theme_fore, Color.RED, Color.RED}, new int[]{1, 5, 15});
                    helper.setImageBitmap(R.id.v_icon, bm);
                    helper.setOnClickListener(R.id.v_icon, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MapHelper.showCallout(mapInstance.map, name, g);
                        }
                    });

                    ImageView iv_zjrqz = (ImageView) helper.getView().findViewById(R.id.iv_zjrqz);
                    TextView tv_zjrqz = (TextView) helper.getView().findViewById(R.id.tv_zjrqz);
                    final String path = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/电子签章/") ;
                    String signName = AiUtil.GetValue(f_jzqz.get("JZQZ.XLZDZDDM"), "");
                    if (TextUtils.isEmpty(signName)){
                        signName = AiUtil.GetValue(f_jzqz.get("JZQZ.ZDZHDM"), "");
                    }
                    fv.setSign(tv_zjrqz,iv_zjrqz,path, signName);
                }
            };
            adapter_jzqz.adpter(ll_list_jzqz);
            ll_list_jzqz.setTag(adapter_jzqz);
            return adapter_jzqz;
        }
    }

    // 电子手签
    private void load_dzsq() {
        LinearLayout ll_dzqz = (LinearLayout) view.findViewById(R.id.ll_qlrqz);
        LinearLayout ll_zz = (LinearLayout) view.findViewById(R.id.ll_zz);
        LinearLayout ll_clr = (LinearLayout) view.findViewById(R.id.ll_clr);
        LinearLayout ll_dcr = (LinearLayout) view.findViewById(R.id.ll_dcr);
        //权利人拍照
        CustomImagesView civ_qlrpz = (CustomImagesView) ll_dzqz.findViewById(R.id.civ_qlrpz);
        String filename = AiUtil.GetValue(civ_qlrpz.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
        final String signDirPath = mapInstance.getpath_root()+"资料库/电子签章/";
        String qlrsignDirPath = getpath_root()+"附件材料/电子签章/";
        civ_qlrpz.setName(filename, activity).setDir(FileUtils.getAppDirAndMK(qlrsignDirPath + filename + "/"));
        //组长拍照
        CustomImagesView civ_zz = (CustomImagesView) ll_zz.findViewById(R.id.civ_zz);
        String filezz = AiUtil.GetValue(civ_zz.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
        civ_zz.setName(filezz, activity).setDir(FileUtils.getAppDirAndMK(signDirPath + filezz + "/"));
        //测量人拍照
        CustomImagesView civClr = (CustomImagesView) ll_clr.findViewById(R.id.civ_clr);
        String fileClr = AiUtil.GetValue(civClr.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
        civClr.setName(fileClr, activity).setDir(FileUtils.getAppDirAndMK(signDirPath + fileClr + "/"));
        //调查人拍照
        CustomImagesView civDcr = (CustomImagesView) ll_dcr.findViewById(R.id.civ_dcr);
        String fileDcr = AiUtil.GetValue(civDcr.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
        civDcr.setName(fileDcr, activity).setDir(FileUtils.getAppDirAndMK(signDirPath + fileDcr + "/"));
        //权利人电子签章
        TextView tv_sign = (TextView) ll_dzqz.findViewById(R.id.tv_sign);
        final ImageView iv_sign = (ImageView) ll_dzqz.findViewById(R.id.iv_sign);
        // 权利人
        fv.setSign(tv_sign,iv_sign,qlrsignDirPath, "");

        //组长电子签章
        TextView tvZz = (TextView) ll_zz.findViewById(R.id.tv_zz);
        ImageView ivZz = (ImageView) ll_zz.findViewById(R.id.iv_zz);
        fv.setSign(tvZz,ivZz,signDirPath, "");
        //测量人电子签章
        TextView tvClr = (TextView) ll_clr.findViewById(R.id.tv_clr);
        ImageView ivClr = (ImageView) ll_clr.findViewById(R.id.iv_clr);
        fv.setSign(tvClr,ivClr,signDirPath, "");

        //调查人电子签章
        TextView tvDcr = (TextView) ll_dcr.findViewById(R.id.tv_dcr);
        final ImageView ivDcr = (ImageView) ll_dcr.findViewById(R.id.iv_dcr);
        fv.setSign(tvDcr,ivDcr,signDirPath, "");

    }



    //endregion 界址点线签字

    //region menu 菜单功能实现方法
    /**
     * 加载宗地草图
     */
    private void load_zdct(final boolean reload) {
        fv.loadZdct(reload, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                try {
                    final String filename = t_ + "";
                    final ImageView iv_ct = (ImageView) view.findViewById(R.id.iv_ct);

                    if ((!reload) && FileUtils.exsit(filename)) {
                        ImageUtil.set(iv_ct, filename);
                    } else {
                        TextView et_glblc = (TextView) view.findViewById(R.id.et_glblc);
                        et_glblc.setText(objects.length > 0 ? ("1:" + objects[0] + "") : "");
                        ImageUtil.set(iv_ct, filename);
                    }
                } catch (Exception es) {
                    Log.e(TAG, "load_zdct: 绘制宗地图错误", es);
                }
                return null;
            }
        });
    }

    /**
     * 加载房产图
     */
    private void load_fct(boolean reload) {
        final ImageView iv_ct = (ImageView) view.findViewById(R.id.iv_fct);
        fv.loadFct(reload, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                try {
                    String filename = t_ + "";
                    TextView et_glblc = (TextView) view.findViewById(R.id.et_glblc);
                    et_glblc.setText(objects.length > 0 ? ("1:" + objects[0] + "") : "");
                    ImageUtil.set(iv_ct, filename);
                } catch (Exception es) {
                    Log.e(TAG, "load_fct: 绘制房产图错误", es);
                }
                return null;
            }
        });
    }

    /**
     * 加载房屋分层图
     */
    private void load_fwfct() {
        try {
            final LinearLayout ll_fwfct_content = (LinearLayout) view.findViewById(R.id.ll_fwfct_content);
            if (ll_fwfct_content.getTag() == null) {
                QuickAdapter<String> adpter = FeatureEditC.getFctAdapter(mapInstance, feature, "分层图", null);
                ll_fwfct_content.setTag(adpter);
                adpter.adpter(ll_fwfct_content);
            }
            final ArrayList<String> fct = new ArrayList<String>();
            fv.loadFwfct(fct, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    ((QuickAdapter<String>) ll_fwfct_content.getTag()).replaceAll(fct);
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "load_fwfct: 绘制房屋分层图错误", es);

        }

    }

    /**
     * 加载自然幢列表
     */
    private void load_zrz() {
        if (view_zrz == null) {
            ViewGroup ll_zrz_list = (ViewGroup) view.findViewById(R.id.ll_zrz_list);
            ll_zrz_list.setTag(null); //强制重新生成adapter
            mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_ZRZ).buildListView(ll_zrz_list, fv.queryChildWhere());
            view_zrz = ll_zrz_list;
        }
    }

    /**
     * 重新加载自然幢列表
     */
    private void reload_zrz() {
        view_zrz = null;
        load_zrz();
    }

    /**
     * 加载户籍信息列表
     */
    private void load_hjxx() {
        if (view_hjxx == null) {
//            ViewGroup hjxx_view = (ViewGroup) view.findViewById(R.id.ll_hjxx_list);
            ViewGroup hjxx_view = null;
            hjxx_view.setTag(null); //强制重新生成adapter
            mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_GYRXX).buildListView(hjxx_view, fv.queryChildWhere());
            view_hjxx = hjxx_view;
        }
    }

    /**
     * 重新加载户籍信息列表
     */
    private void reload_hjxx() {
        view_hjxx = null;
        load_hjxx();
    }

    /**
     * 加载宗地分摊情况列表
     */
    private void load_ftqk() {
        if (view_ftqk == null) {
            ViewGroup ftqk_view = (ViewGroup) view.findViewById(R.id.ll_ft_list);
            ftqk_view.setTag(null); //强制重新生成adapter
            mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_FTQK).buildListView(ftqk_view, "FTQX_ID='" + fv.getOrid() + "'");
            view_ftqk = ftqk_view;
        }
    }

    /**
     * 重新加载宗地分摊情况列表
     */
    private void reload_ftqk() {
        view_ftqk = null;
        load_ftqk();
    }

    /**
     * 加载不动产单元列表 采用继承
     */
    private void load_bdcdy() {
        if (view_bdcdy == null) {
            ViewGroup bdcdy_view = (ViewGroup) view.findViewById(R.id.ll_bdcdy_list);
            bdcdy_view.setTag(null); //强制重新生成adapter
            mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_QLRXX).buildListView(bdcdy_view, fv.queryChildWhere());
            view_bdcdy = bdcdy_view;
        }
    }

    /**
     * 重新加载不动产单元列表
     */
    private void reload_bdcdy() {
        view_bdcdy = null;
        load_bdcdy();
    }

    //endregion menu 菜单功能
}

