package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.ai.component.custom.CustomImagesView;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.DialogBuilder;
import com.ovit.app.ui.dialog.ProgressDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.ui.view.CView;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.openCV.OpenCVDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Lichun on 2017/4/5.
 */

public class FeatureEditQLR extends FeatureEdit {

    //region 常量
    final static String TAG = "FeatureEditQLR";
    ///endregion

    //region 字段
    private EditText et_xm;
    private String old_qlrzjh;
    private String old_qlrxm;
    FeatureViewQLR fv;

    View view_qlr;
    View view_bdc;
    ///endregion

    //region 构造函数

    public FeatureEditQLR() {
        super();
    }

    public FeatureEditQLR(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }

    ///endregion

    //region 重写函数和回调
    @Override
    public void onCreate() {
        super.onCreate();
        if (super.fv instanceof FeatureViewQLR) {
            this.fv = (FeatureViewQLR) super.fv;
        }
    }

    // 初始化
    @Override
    public void init() {
        super.init();
        // 菜单
        //menus = new int[]{R.id.ll_info, R.id.ll_item};
        Log.i(TAG, "init FeatureEditQLR!");
        menus = new int[]{R.id.ll_info, R.id.ll_bdc, R.id.ll_qlr};
    }

    // 显示数据
    @Override
    public void build() {
        Log.i(TAG, "build qlr");
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_qlr, v_content);

        try {
            if (feature != null) {
                mapInstance.fillFeature(feature);
                TextView tv_path = (TextView) v_feature.findViewById(R.id.tv_path);
                tv_path.setText(fv.getOrid());

                et_xm = (EditText) v_feature.findViewById(R.id.et_xm);
                EditText et_zjh = (EditText) v_feature.findViewById(R.id.et_zjh);
                old_qlrzjh = et_zjh.getText().toString();
                old_qlrxm = et_xm.getText().toString();

                CustomImagesView civ_zjh = (CustomImagesView) v_feature.findViewById(R.id.civ_zjh);
                //   String filename = dir + "/" + FeatureHelper.Get(feature, "XM", "")+"/"+FeatureHelper.Get(feature, "ZJH", "")+"/" + AiUtil.GetValue(new Date(), "") + ".jpg";
                String filename = AiUtil.GetValue(civ_zjh.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
                civ_zjh.setName(filename + "(正反面)").setDir(FileUtils.getAppDirAndMK(getpath_root() + FeatureHelper.FJCL + filename + "/")).setOnRecognize_SFZ(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Map<String, String> datas = (Map<String, String>) t_;
                        String xm = datas.get("xm");
                        String sfzh = datas.get("sfzh");
                        String zz = datas.get("zz");
                        if (StringUtil.IsNotEmpty(xm)) {
                            ((EditText) v_feature.findViewById(R.id.et_xm)).setText(xm);
                        }
                        if (StringUtil.IsNotEmpty(sfzh)) {
                            ((EditText) v_feature.findViewById(R.id.et_zjh)).setText(sfzh);
                        }
                        if (StringUtil.IsNotEmpty(zz)) {
                            ((EditText) v_feature.findViewById(R.id.et_dz)).setText(zz);
                        }
                        return null;
                    }
                });

                CustomImagesView civ_hkb = (CustomImagesView) v_feature.findViewById(R.id.civ_hkb);
                filename = AiUtil.GetValue(civ_hkb.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
                String s = getpath_root();
                civ_hkb.setName(filename, activity).setDir(FileUtils.getAppDirAndMK(getpath_root() + FeatureHelper.FJCL + filename + "/"));

                CustomImagesView civ_tdqslyzm = (CustomImagesView) v_feature.findViewById(R.id.civ_tdqslyzm);
                filename = AiUtil.GetValue(civ_tdqslyzm.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
                civ_tdqslyzm.setName(filename, activity).setDir(FileUtils.getAppDirAndMK(getpath_root() + "/" + FeatureHelper.FJCL + filename + "/"));
                fillView(v_feature);

                CustomImagesView civ_fwzp = (CustomImagesView) v_feature.findViewById(R.id.civ_fwzp);
                String fileDescription = AiUtil.GetValue(civ_fwzp.getContentDescription(), FeatureHelper.CDES_DEFULT_NAME);
                civ_fwzp.setName(fileDescription, activity).setDir(FileUtils.getAppDirAndMK(getpath_root() + FeatureHelper.FJCL + fileDescription + "/"));

                //新增附属宗地
                v_feature.findViewById(R.id.tv_add_fszd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fv.addFszd(new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                reload_bdc();
                                return null;
                            }
                        });

                    }
                });

                //清除附属宗地
                v_feature.findViewById(R.id.tv_clear_fszd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fv.clearFszd(new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                String oridPath = (String) t_;
                                if (StringUtil.IsNotEmpty(oridPath)) {
                                    FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ORID_PATH, oridPath);
                                    MapHelper.saveFeature(feature, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            reload_bdc();
                                            return null;
                                        }
                                    });
                                }
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
        Log.i(TAG, "build opt");
        /* ！！！期待removeAction()方法 以根据菜单模式添加action */
        /* 添加操作 */
        addAction("权利人", R.mipmap.app_icon_add_thin, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeatureEditGYR.createNewGYR(mapInstance, feature);  //调用权利人信息编辑面板 20180717
            }
        });
             /* 添加操作 */
        addAction("生成资料", R.mipmap.app_icon_excel_blue, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dy(feature, mapInstance, true);
            }
        });

        //添加菜单
        addMenu("基本信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "load jbxx");
                setMenuItem(R.id.ll_info);
            }
        });

        addMenu("不动产", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "load all bdc of qlr");
                setMenuItem(R.id.ll_bdc);
                load_bdc();
            }
        });

        addMenu("权利人", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_qlr);
                load_qlr();
            }
        });


//        addMenu("户籍信息", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "load hjxx");
//                setMenuItem(R.id.ll_hjxx);
//                View hjxx_view = view.findViewById(com.ovit.R.id.ll_hjxx);
//                FeatureEditHJXX.loadHJXX(mapInstance, feature, hjxx_view);
//            }
//        });

    }

    @Override
    public void update(final AiRunnable callback) {
        try {
            mapInstance.fillFeature(feature);
            super.update(new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    fv.update_gyrxx(callback);
                    return null;
                }
            });

        } catch (Exception e) {

        }
    }
    ///endregion

    //region 公有函数
    ///endregion

    //region 私有函数
    private void load_qlr() {
        if (view_qlr == null) {
            ViewGroup ll_qlr_list = (ViewGroup) view.findViewById(R.id.ll_qlr_list);
            ll_qlr_list.setTag(null);//强制重新生成adapter
            mapInstance.newFeatureView(FeatureConstants.GYRXX_TABLE_NAME).buildListView(ll_qlr_list, fv.queryChildWhere());
            view_qlr = ll_qlr_list;
        }
    }
    private void reload_qlr() {
        view_qlr = null;
        load_qlr();
    }

    private void load_bdc() {
        if (view_bdc == null) {
            ViewGroup ll_bdc_list = (ViewGroup) view.findViewById(R.id.ll_bdc_list);
            ll_bdc_list.setTag(null);//强制重新生成adapter
            List<String> orids = FeatureHelper.GetOridsFormOridPath(feature, FeatureHelper.TABLE_NAME_ZD);
            if (orids != null && orids.size() > 0) {
                String where = "";
                for (String orid : orids) {
                    if (orids.indexOf(orid) == 0) {
                        where += "orid = '" + orid + "'";
                    } else {
                        where += " OR orid='" + orid + "'";
                    }
                }
                String tableName = FeatureHelper.getTableNameForOrid(orids.get(0));
                mapInstance.newFeatureView(tableName).buildListView(ll_bdc_list, where);
                view_bdc = ll_bdc_list;
            }
        }
    }
    private void reload_bdc() {
        view_bdc = null;
        load_bdc();
    }
    ///endregion

    //region 面积计算
    ///endregion

    //region 内部类或接口
    ///endregion


    //region  重写父类方法

    public static void createNewQlrByBdc(final MapInstance mapInstance, final Feature feature_bdc, final AiRunnable callback) {
        try {
            final Feature feature_new_qlr = mapInstance.getTable("GYRXX").createFeature();
            feature_new_qlr.getAttributes().put("YHZGX", "户主");
            feature_new_qlr.getAttributes().put("XM", FeatureHelper.Get(feature_bdc, "XM"));
            feature_new_qlr.getAttributes().put("ZJH", FeatureHelper.Get(feature_bdc, "ZJH"));
            feature_new_qlr.getAttributes().put("ZJZL", FeatureHelper.Get(feature_bdc, "ZJZL"));
            feature_new_qlr.getAttributes().put("CSRQ", FeatureHelper.Get(feature_bdc, "CSRQ"));
            feature_new_qlr.getAttributes().put("DH", FeatureHelper.Get(feature_bdc, "DH"));
            feature_new_qlr.getAttributes().put("TDZH", FeatureHelper.Get(feature_bdc, "BDCQZH"));
            feature_new_qlr.getAttributes().put(FeatureHelper.TABLE_ATTR_ORID_PATH, FeatureHelper.Get(feature_bdc, FeatureHelper.TABLE_ATTR_ORID) + "/"); //权利人关联不动产
            mapInstance.featureView.fillFeature(feature_new_qlr, feature_bdc);

            //拷贝资料
            String f_zd_path = mapInstance.getpath_feature(feature_bdc); //     不动产d单元
            String f_qlr_path = mapInstance.getpath_feature(feature_new_qlr);// 权利人 gyrxx

            String f_zd_zjh_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/证件号/");
            String f_qlr_zjh_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/证件号/");
            FileUtils.copyFile(f_zd_zjh_path, f_qlr_zjh_path);

            String f_zd_zmcl_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/土地权属来源证明材料/");
            String f_qlr_zmcl_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/土地权属来源证明材料/");
            FileUtils.copyFile(f_zd_zmcl_path, f_qlr_zmcl_path);

            String f_zd_hkb_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/户口簿/");
            String f_qlr_hkb_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/户口簿/");

            FileUtils.copyFile(f_zd_hkb_path, f_qlr_hkb_path);
            MapHelper.saveFeature(feature_new_qlr, callback);

        } catch (Exception es) {
            Log.e(TAG, "通过宗地创建新权利人失败!" + es);
            AiRunnable.Ok(callback, false, false);
        }
    }

    //获得权利人ORID_PATH中包含的所有不动产ORID 20180815
    public static List<String> getOridsByQlr(Feature feature_qlr) {
        final List<String> orid_list = new ArrayList<>();
        try {
            final String qlr_orid_path = FeatureHelper.Get(feature_qlr, FeatureHelper.TABLE_ATTR_ORID_PATH) + "";
            final String orids[] = qlr_orid_path.split("/");
            for (int i = 0; i < orids.length; i++) {
                orid_list.add(orids[i]);
            }
        } catch (Exception es) {
            Log.e(TAG, "获取ORID列表失败！" + es);
        }
        return orid_list;
    }

    //获得所有的权利人 20180815
    public static void getAllQlr(final MapInstance mapInstance, final AiRunnable callback) {
        try {
            FeatureTable table_qlr = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX);
            MapHelper.Query(table_qlr, "1=1", new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    AiRunnable.Ok(callback, (List<Feature>) t_, (List<Feature>) t_);
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "获得所有权利人失败！" + es);
            AiRunnable.Ok(callback, new ArrayList<Feature>(), new ArrayList<Feature>());
        }
    }

    public static void getAllBdcdy(final MapInstance mapInstance, final AiRunnable callback) {
        try {
            FeatureTable table_qlr = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX);
            MapHelper.Query(table_qlr, "1=1", new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    AiRunnable.Ok(callback, (List<Feature>) t_, (List<Feature>) t_);
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "获得所有权利人失败！" + es);
            AiRunnable.Ok(callback, new ArrayList<Feature>(), new ArrayList<Feature>());
        }
    }

    //获得所有不动产查询范围列表 20180815
    public static List<FeatureTable> getAllSearchTable(final MapInstance mapInstance) {
        final List<FeatureTable> search_table_list = new ArrayList<FeatureTable>();
        try {
            search_table_list.add(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZD));
            search_table_list.add(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ));
            search_table_list.add(mapInstance.getTable(FeatureHelper.TABLE_NAME_LJZ));
            search_table_list.add(mapInstance.getTable(FeatureHelper.TABLE_NAME_H));
        } catch (Exception es) {
            Log.e(TAG, "获得所有不动产查询范围列表失败!" + es);
        }
        return search_table_list;
    }

    //TODO 获取不动产单元下的定着物
    public static void getAllBdcByBDCDY(final MapInstance mapInstance, final Feature feature_qlr, final AiRunnable callback) {
        final List<Feature> features_bdc = new ArrayList<>();
        try {
            String lastOrid = FeatureHelper.GetLastOrid(feature_qlr);
            String tableName = lastOrid.substring(lastOrid.indexOf("[") + 1, lastOrid.lastIndexOf("]"));
            FeatureTable table = mapInstance.getTable(tableName);
            String where = "ORID ='" + lastOrid + "'";
            MapHelper.QueryOne(table, where, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if (t_ != null) {
                        features_bdc.add((Feature) t_);
                    }
                    AiRunnable.Ok(callback, features_bdc, features_bdc);
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "获得权利人名下所有的不动产失败!" + es);
            AiRunnable.Ok(callback, features_bdc, features_bdc);
        }

    }

    public static void getAllBdcByQLR(final MapInstance mapInstance, final Feature feature_qlr, final AiRunnable callback) {
        final List<Feature> features_bdc = new ArrayList<>();

        try {
            if (StringUtil.IsNotEmpty(FeatureHelper.Get(feature_qlr, FeatureHelper.TABLE_ATTR_ORID_PATH))) {
                final List<String> orid_list_ = getOridsByQlr(feature_qlr);
                final List<String> orid_list = new ArrayList<>();
                orid_list.add(orid_list_.get(orid_list_.size() - 1));//
                final List<FeatureTable> search_table_list = getAllSearchTable(mapInstance);

                new AiForEach<FeatureTable>(search_table_list, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        AiRunnable.Ok(callback, features_bdc, features_bdc);
                        return null;
                    }
                }) {
                    public void exec() {
                        new AiForEach<String>(orid_list, getNext()) {
                            final FeatureTable table = search_table_list.get(postion); // TODO 权利人业务要变成不动产单元

                            public void exec() {
                                String orid = orid_list.get(postion);
//                                String tableName = orid.substring(orid.lastIndexOf("[") + 1, orid.lastIndexOf("]"));
//                                FeatureTable table = mapInstance.getTable(FeatureConstants.QLRXX_TABLE_NAME);
                                String where = "ORID ='" + orid + "'";
                                MapHelper.QueryOne(table, where, new AiRunnable(getNext()) {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        if (t_ != null) {
                                            features_bdc.add((Feature) t_);
                                        }
                                        AiRunnable.Ok(getNext(), true, true);
                                        return null;
                                    }
                                });
                            }
                        }.start();
                    }
                }.start();
            } else {
                AiRunnable.Ok(callback, features_bdc, features_bdc);
            }
        } catch (Exception es) {
            Log.e(TAG, "获得权利人名下所有的不动产失败!" + es);
            AiRunnable.Ok(callback, features_bdc, features_bdc);
        }

    }

    //得到所有已和权利人绑定的不动产 20180814
    public static void getAllBoundBDC(final MapInstance mapInstance, final AiRunnable callback) {
        final List<Feature> features_bound_bdc = new ArrayList<>();
        try {
            getAllQlr(mapInstance, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if (t_ != null) {
                        final List<Feature> features_exist_qlr = (List<Feature>) t_;
                        new AiForEach<Feature>(features_exist_qlr, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                AiRunnable.Ok(callback, features_bound_bdc, features_bound_bdc);
                                return null;
                            }
                        }) {
                            public void exec() {
                                Feature feature_qlr = features_exist_qlr.get(postion);
                                getAllBdcByQLR(mapInstance, feature_qlr, new AiRunnable(getNext()) {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        if (t_ != null) {
                                            features_bound_bdc.addAll((List<Feature>) t_);
                                        }
                                        AiRunnable.Ok(getNext(), true, true);
                                        return null;
                                    }
                                });
                            }
                        }.start();
                    } else {
                        AiRunnable.Ok(callback, features_bound_bdc, features_bound_bdc);
                    }
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "获得所有已绑定不动产失败!" + es);
            ToastMessage.Send("获得所有已绑定不动产失败!" + es);
            AiRunnable.Ok(callback, features_bound_bdc, features_bound_bdc);
        }
    }

    //得到所有的不动产  20180814
    public static void getAllBDC(final MapInstance mapInstance, final AiRunnable callback) {
        final List<Feature> all_bdc_list = new ArrayList<Feature>();
        try {
            final List<FeatureTable> identy_table_list = getAllSearchTable(mapInstance);

            new AiForEach<FeatureTable>(identy_table_list, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    AiRunnable.Ok(callback, all_bdc_list, all_bdc_list);
                    return null;
                }
            }) {
                public void exec() {
                    MapHelper.Query(identy_table_list.get(postion), "1=1", new AiRunnable(getNext()) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            if (t_ != null) {
                                all_bdc_list.addAll((List<Feature>) t_);
                            }
                            AiRunnable.Ok(getNext(), true, true);
                            return null;
                        }
                    });
                }
            }.start();
        } catch (Exception es) {
            Log.e(TAG, "获得所有不动产失败!" + es);
            ToastMessage.Send("获得所有不动产!" + es);
            AiRunnable.Ok(callback, all_bdc_list, all_bdc_list);
        }
    }

    public static FeatureTable GetTable(MapInstance mapInstance) {
        return MapHelper.getLayer(mapInstance.map, FeatureHelper.TABLE_NAME_QLRXX, FeatureHelper.LAYER_NAME_QLRXX).getFeatureTable();
    }

    public static void Load(MapInstance mapInstance, String qlrdm, final AiRunnable callback) {
        if (StringUtil.IsNotEmpty(qlrdm)) {
            MapHelper.QueryOne(GetTable(mapInstance), "QLRDM='" + qlrdm + "'", callback);
//            MapHelper.QueryOne(GetTable(mapInstance), "QLRDM='" + qlrdm + "'", new AiRunnable(callback) {
//                @Override
//                public <T_> T_ ok(T_ t_, Object... objects) {
//                    if (objects != null && objects.length > 0) {
//                        AiRunnable.Ok(callback, objects[0]);
//                    } else {
//                        AiRunnable.Ok(callback, null);
//                    }
//                    return null;
//                }
//            });
        } else {
            AiRunnable.Ok(callback, null);
        }
    }

    public static void LoadByZJH(MapInstance mapInstance, String qlrzjh, final AiRunnable callback) {
        if (StringUtil.IsNotEmpty(qlrzjh)) {
            MapHelper.QueryOne(GetTable(mapInstance), "ZJH='" + qlrzjh + "'", callback);
//            MapHelper.QueryOne(GetTable(mapInstance), "ZJH='" + qlrzjh + "'", new AiRunnable(callback) {
//                @Override
//                public <T_> T_ ok(T_ t_, Object... objects) {
//                    if (objects != null && objects.length > 0) {
//                        AiRunnable.Ok(callback, objects[0]);
//                    } else {
//                        AiRunnable.Ok(callback, null);
//                    }
//                    return null;
//                }
//            });
        } else {
            AiRunnable.Ok(callback, null);
        }
    }

    // 获取id
    public static String GetID(Feature feature) {
        return FeatureHelper.Get(feature, "QLRDM", "");
    }

    //  获取最大的编号
    public static void GetMaxID(MapInstance mapInstance, String pid, AiRunnable callback) {
        MapHelper.QueryMax(GetTable(mapInstance), " QLRDM like '" + pid + "_______' ", "QLRDM", pid.length(), 0, pid + "0000000", callback);
    }

    public static void NewID(MapInstance mapInstance, final String pid, final String oldid, final AiRunnable callback) {
        GetMaxID(mapInstance, pid, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = "";
                if (objects.length > 1) {
                    String maxid = objects[0] + "";
                    if (maxid.equals(oldid)) {
                        id = oldid;
                    } else {
                        // 最大号加1
                        int count = AiUtil.GetValue(objects[1], 0) + 1;
                        id = pid + String.format("%07d", count);
                    }
                }
                AiRunnable.Ok(callback, id);
                return null;
            }
        });
    }

    public static void CreateFeature(final MapInstance mapInstance, AiRunnable callback) {
        CreateFeature(mapInstance, GetTable(mapInstance).createFeature(), callback);
    }

    public static void CreateFeature(final MapInstance mapInstance, final Feature feature, final AiRunnable callback) {
        final String pid = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "", "XMBM", "xmbm");
        NewID(mapInstance, pid, "", new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = t_ + "";
                FeatureHelper.Set(feature, "QLRDM", id);
                mapInstance.fillFeature(feature);
                mapInstance.newFeatureView(feature).fillFeatureAddSave(feature, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
//                        mapInstance.viewFeature(feature);
                        AiRunnable.Ok(callback, feature, true);
                        return null;
                    }
                });
                return null;
            }
        });
    }

    public static void NewFeature(final MapInstance mapInstance, final String qlrzjh, final AiRunnable callback) {
        final String pid = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "", "XMBM", "xmbm");
        LoadByZJH(mapInstance, qlrzjh, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final Feature f = (Feature) t_;
                AiRunnable runnable = new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String qlrdm = t_ + "";
                        FeatureHelper.Get(f, "QLRDM", qlrdm);
                        Feature feature = f;
                        if (feature == null) {
                            feature = GetTable(mapInstance).createFeature();
                            feature.getAttributes().put("QLRDM", qlrdm);
                            FeatureHelper.Get(f, "ZJH", qlrzjh);
                        }
                        AiRunnable.Ok(callback, feature);
                        return null;
                    }
                };
                String qlrdm = FeatureHelper.Get(f, "QLRDM", "");
                if (f == null || StringUtil.IsEmpty(qlrdm)) {
                    NewID(mapInstance, pid, "", runnable);
                } else {
                    AiRunnable.Ok(runnable, qlrdm);
                }
                return null;
            }
        });
    }

    //清除权利人ORID_PATH中已无效的不动产ORID 20180815
    public static void deleteInvalidOridByQlr(final MapInstance mapInstance, final Feature feature_qlr, final AiRunnable callback) {
        try {
            final List<String> orid_list = getOridsByQlr(feature_qlr);
            if (orid_list.size() > 0) {
                getAllBdcByQLR(mapInstance, feature_qlr, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        if (t_ != null) {
                            String valid_orid = "";
                            for (int i = 0; i < ((List<Feature>) t_).size(); i++) {
                                valid_orid += FeatureHelper.Get(((List<Feature>) t_).get(i), FeatureHelper.TABLE_ATTR_ORID) + "/";
                            }
                            FeatureHelper.Set(feature_qlr, FeatureHelper.TABLE_ATTR_ORID_PATH, valid_orid);
                            MapHelper.updateFeature(feature_qlr, callback);
                        } else {
                            FeatureHelper.Set(feature_qlr, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                            MapHelper.updateFeature(feature_qlr, callback);
                        }
                        return null;
                    }
                });
            } else {
                AiRunnable.Ok(callback, true, true);
            }
        } catch (Exception es) {
            Log.e(TAG, "清除无效ORID失败!" + es);
            AiRunnable.Ok(callback, false, false);
        }

    }

    //清除所有权利人ORID_PATH中已无效的不动产ORID 20180815
    public static void deleteInvalidOridByQlr(final MapInstance mapInstance, final List<Feature> features_qlr, final AiRunnable callback) {
        try {
            new AiForEach<Feature>(features_qlr, callback) {
                public void exec() {
                    deleteInvalidOridByQlr(mapInstance, features_qlr.get(postion), getNext());
                }
            }.start();
        } catch (Exception es) {
            Log.e(TAG, "清除全局无效ORID失败!" + es);
            AiRunnable.Ok(callback, false, false);
        }

    }

    //去除传入权利人集合中重复的权利人 (信息合并) 20180814
    public static void removeRepeatQLR(final MapInstance mapInstance, final List<Feature> features_qlr, boolean delete, final AiRunnable callback) {
        try {
            if (features_qlr.size() > 1) {
                final List<Feature> need_to_delete = new ArrayList<>();

                for (int i = 0; i < features_qlr.size(); i++) {
                    String qlrxm = FeatureHelper.Get(features_qlr.get(i), "XM") + "";
                    String qlrzjh = FeatureHelper.Get(features_qlr.get(i), "ZJH") + "";
                    for (int j = i + 1; j < features_qlr.size(); j++) {
                        String qlrxm_ = FeatureHelper.Get(features_qlr.get(j), "XM") + "";
                        String qlrzjh_ = FeatureHelper.Get(features_qlr.get(j), "ZJH") + "";
                        if (qlrxm.equals(qlrxm_) && qlrzjh.equals(qlrzjh_)) {
                            //合并资料
                            String orid_path = FeatureHelper.Get(features_qlr.get(i), FeatureHelper.TABLE_ATTR_ORID_PATH) + "/" + FeatureHelper.Get(features_qlr.get(j), FeatureHelper.TABLE_ATTR_ORID_PATH);
                            FeatureHelper.Set(features_qlr.get(i), FeatureHelper.TABLE_ATTR_ORID_PATH, orid_path);

                            String f_path = mapInstance.getpath_feature(features_qlr.get(j));
                            String f_qlr_path = mapInstance.getpath_feature(features_qlr.get(i));

                            String f_zd_zjh_path = FileUtils.getAppDirAndMK(f_path + "/" + "附件材料/权利人证件号/");
                            String f_qlr_zjh_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/证件号/");
                            FileUtils.copyFile(f_zd_zjh_path, f_qlr_zjh_path);

                            String f_zd_zmcl_path = FileUtils.getAppDirAndMK(f_path + "/" + "附件材料/土地权属来源证明材料/");
                            String f_qlr_zmcl_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/土地权属来源证明材料/");
                            FileUtils.copyFile(f_zd_zmcl_path, f_qlr_zmcl_path);

                            String f_zd_hkb_path = FileUtils.getAppDirAndMK(f_path + "/" + "附件材料/户口簿/");
                            String f_qlr_hkb_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/户口簿/");
                            FileUtils.copyFile(f_zd_hkb_path, f_qlr_hkb_path);

                            need_to_delete.add(features_qlr.get(j));
                            features_qlr.remove(j);
                            j--;
                        }
                    }
                }

                if (need_to_delete.size() > 0) {
                    features_qlr.removeAll(need_to_delete);
                    if (delete) {
                        MapHelper.deleteFeature(need_to_delete, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                AiRunnable.Ok(callback, features_qlr, features_qlr);
                                return null;
                            }
                        });
                    } else {
                        AiRunnable.Ok(callback, features_qlr, features_qlr);
                    }
                } else {
                    AiRunnable.Ok(callback, features_qlr, features_qlr);
                }
            } else {
                AiRunnable.Ok(callback, features_qlr, features_qlr);
            }
        } catch (Exception es) {
            Log.e(TAG, "去除重复权利人失败!" + es);
            AiRunnable.Ok(callback, features_qlr, features_qlr);
        }

    }

    //删除所有不合法权利人 目前删除依据为删除姓名或证件号为空的权利人 同时去除重复的权利人和权利人ORID_PATH中无效的orid
    public static void delInvalidQlr(final MapInstance mapInstance, final AiRunnable callback) {
        try {
            getAllQlr(mapInstance, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if (t_ != null) {
                        final List<Feature> features_all_qlr = (List<Feature>) t_;
                        final List<Feature> features_qlr_del = new ArrayList<>();
                        for (int i = 0; i < features_all_qlr.size(); i++) {
                            if (StringUtil.IsEmpty(FeatureHelper.Get(features_all_qlr.get(i), "ZJH")) || StringUtil.IsEmpty(FeatureHelper.Get(features_all_qlr.get(i), "XM"))) {
                                features_qlr_del.add(features_all_qlr.get(i));
                                features_all_qlr.remove(i);
                                i--;
                            }
                        }
                        MapHelper.deleteFeature(features_qlr_del, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                removeRepeatQLR(mapInstance, features_all_qlr, true, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        final int repeat_count = features_all_qlr.size() - ((List<Feature>) t_).size();
                                        features_all_qlr.clear();
                                        features_all_qlr.addAll((List<Feature>) t_);
                                        deleteInvalidOridByQlr(mapInstance, features_all_qlr, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                AiRunnable.Ok(callback, features_qlr_del.size() + repeat_count, features_qlr_del.size() + repeat_count);
                                                return null;
                                            }
                                        });
                                        return null;
                                    }
                                });
                                return null;
                            }
                        });
                    } else {
                        AiRunnable.Ok(callback, 0, 0);
                    }
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "删除所有不合法权利人失败!" + es);
            AiRunnable.Ok(callback, false, false);
        }

    }

    //解除权利人和不动产的关联 (目前仅简单解除ORID和ORID_PATH之间的关联 对于ZD和H可选是否深度解除绑定)20180814
    public static void unassociateQlrAndBdc(final MapInstance mapInstance, final Feature feature_bdc, final boolean deep_unassociate, final AiRunnable callback) {
        try {
            final FeatureTable table_qlr = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX);
            final String bdc_orid = FeatureHelper.Get(feature_bdc, FeatureHelper.TABLE_ATTR_ORID) + "";
            String where_qlr = "ORID_PATH like '%" + bdc_orid + "%'";
            MapHelper.QueryOne(table_qlr, where_qlr, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if (t_ != null) {
                        final List<String> orid_list = getOridsByQlr((Feature) t_);

                        for (int j = 0; j < orid_list.size(); j++) {
                            if (orid_list.get(j).equals(bdc_orid)) {
                                orid_list.remove(j);
                                break;
                            }
                        }
                        String new_qlr_orid = "";
                        for (int k = 0; k < orid_list.size(); k++) {
                            new_qlr_orid += orid_list.get(k) + "/";
                        }
                        FeatureHelper.Set((Feature) t_, FeatureHelper.TABLE_ATTR_ORID_PATH, new_qlr_orid);

                        MapHelper.updateFeature((Feature) t_, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (deep_unassociate) {
                                    if (StringUtil.IsNotEmpty(FeatureHelper.Get(feature_bdc, "QLRXM")) || StringUtil.IsNotEmpty(FeatureHelper.Get(feature_bdc, "QLRZJH"))) {  //此处待优化 期待完全重置为默认空值！！！20180816
                                        FeatureHelper.Set(feature_bdc, "QLRXM", null);
                                        FeatureHelper.Set(feature_bdc, "QLRZJH", null);
                                    }
                                    MapHelper.updateFeature(feature_bdc, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            AiRunnable.Ok(callback, true, true);
                                            return null;
                                        }
                                    });
                                } else {
                                    AiRunnable.Ok(callback, true, true);
                                }
                                return null;
                            }
                        });
                    } else {
                        AiRunnable.Ok(callback, true, true);
                    }
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "解除权利人和不动产之间的绑定失败!" + es);
            AiRunnable.Ok(callback, false, false);
        }
    }

    //识别权利人 根据不动产ORID识别 （目前仅识别ZD和H范围内的QLR 深度识别 包含资料拷贝） 20180814
    public static void IdentyQlr(final MapInstance mapInstance, final AiRunnable callback) {
        final AiDialog dialog = AiDialog.get(mapInstance.activity).setHeaderView(com.ovit.R.mipmap.app_map_layer_qlrxx, "识别权利人");
        dialog.setContentView(dialog.getProgressView("正在处理，请稍后..."));

        try {
            delInvalidQlr(mapInstance, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    final int invalid_count = (Integer) t_;
                    getAllBDC(mapInstance, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            if (t_ != null) {
                                final List<Feature> all_bdc_list = new ArrayList<>((List<Feature>) t_);
                                getAllBoundBDC(mapInstance, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        if (t_ != null) { //移除所有已绑定的不动产
                                            final List<Feature> all_bound_bdc_list = new ArrayList<>((List<Feature>) t_);
                                            for (int i = 0; i < all_bound_bdc_list.size(); i++) {
                                                String orid_bound = FeatureHelper.Get(all_bound_bdc_list.get(i), FeatureHelper.TABLE_ATTR_ORID) + "";
                                                for (int j = 0; i < all_bdc_list.size(); j++) {
                                                    String orid = FeatureHelper.Get(all_bdc_list.get(j), FeatureHelper.TABLE_ATTR_ORID) + "";
                                                    if (orid.equals(orid_bound)) {
                                                        all_bdc_list.remove(all_bdc_list.get(j));
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        //移除关键字（QLRXM、QLRZJH）有一者为空的不动产
                                        for (int i = 0; i < all_bdc_list.size(); i++) {
                                            if (StringUtil.IsEmpty(FeatureHelper.Get(all_bdc_list.get(i), "QLRXM")) || StringUtil.IsEmpty(FeatureHelper.Get(all_bdc_list.get(i), "QLRZJH"))) {
                                                all_bdc_list.remove(i);
                                                i--;
                                            }
                                        }
                                        //判断剩余的不动产是需要新建权利人还是需要绑定已存在的权利人
                                        if (all_bdc_list.size() > 0) {
                                            final List<Feature> need_to_create = new ArrayList<>();
                                            final List<Feature> need_to_bind = new ArrayList<>();
                                            final FeatureTable table_qlr = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX);
                                            new AiForEach<Feature>(all_bdc_list, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) { //后续处理
                                                    final List<Feature> features_new_qlr = new ArrayList<>();
                                                    new AiForEach<Feature>(need_to_create, new AiRunnable() {
                                                        @Override
                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                            removeRepeatQLR(mapInstance, features_new_qlr, false, new AiRunnable() {
                                                                @Override
                                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                                    if (t_ != null) {
                                                                        final List<Feature> need_to_save = new ArrayList<>((List<Feature>) t_);
                                                                        //重新编号
                                                                        final String pid = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "", "XMBM", "xmbm");
                                                                        GetMaxID(mapInstance, pid, new AiRunnable() {
                                                                            @Override
                                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                String id = "";
                                                                                for (int i = 1; i < need_to_save.size() + 1; i++) {
                                                                                    int count = AiUtil.GetValue(objects[1], 0) + i;
                                                                                    id = pid + String.format("%07d", count);
                                                                                    FeatureHelper.Set(need_to_save.get(i - 1), "QLRDM", id);
                                                                                }
                                                                                MapHelper.saveFeature(need_to_save, new AiRunnable() {
                                                                                    @Override
                                                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                        dialog.setContentView("成功识别权利人" + need_to_save.size() + "个|绑定权利人" + need_to_bind.size() + "个|删除不合法权利人" + invalid_count + "个").setCancelable(true);
                                                                                        AiRunnable.Ok(callback, true, true);
                                                                                        return null;
                                                                                    }
                                                                                });
                                                                                return null;
                                                                            }
                                                                        });
                                                                    } else {
                                                                        dialog.setContentView("成功绑定权利人" + need_to_bind.size() + "个|删除不合法权利人" + invalid_count + "个").setCancelable(true);
                                                                        AiRunnable.Ok(callback, true, true);
                                                                    }
                                                                    return null;
                                                                }
                                                            });
                                                            return null;
                                                        }
                                                    }) {
                                                        public void exec() {
                                                            switch (need_to_create.get(postion).getFeatureTable().getTableName()) {
                                                                case FeatureHelper.TABLE_NAME_ZD: {
                                                                    FeatureEditZD.createNewQlrByZD(mapInstance, need_to_create.get(postion), false, false, false, new AiRunnable(getNext()) {
                                                                        @Override
                                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                                            if (t_ != null) {
                                                                                features_new_qlr.add((Feature) t_);
                                                                            }
                                                                            AiRunnable.Ok(getNext(), true, true);
                                                                            return null;
                                                                        }
                                                                    });
                                                                    break;
                                                                }
                                                                case FeatureHelper.TABLE_NAME_H: {
                                                                    FeatureEditH.createNewQlrByH(mapInstance, need_to_create.get(postion), false, false, false, new AiRunnable(getNext()) {
                                                                        @Override
                                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                                            if (t_ != null) {
                                                                                features_new_qlr.add((Feature) t_);
                                                                            }
                                                                            AiRunnable.Ok(getNext(), true, true);
                                                                            return null;
                                                                        }
                                                                    });
                                                                    break;
                                                                }
                                                                default:
                                                                    AiRunnable.Ok(getNext(), true, true);
                                                            }
                                                        }
                                                    }.start();
                                                    return null;
                                                }
                                            }) {
                                                public void exec() {
                                                    String qlrxm = FeatureHelper.Get(all_bdc_list.get(postion), "QLRXM") + "";
                                                    String qlrzjh = FeatureHelper.Get(all_bdc_list.get(postion), "QLRZJH") + "";
                                                    String where_qlr = "XM ='" + qlrxm + "' and ZJH ='" + qlrzjh + "'";
                                                    MapHelper.QueryOne(table_qlr, where_qlr, new AiRunnable(getNext()) {
                                                        @Override
                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                            if (t_ != null) { //需要绑定
                                                                String orid_path = FeatureHelper.Get((Feature) t_, FeatureHelper.TABLE_ATTR_ORID_PATH) + "/" + FeatureHelper.Get(all_bdc_list.get(postion), FeatureHelper.TABLE_ATTR_ORID);
                                                                FeatureHelper.Set((Feature) t_, FeatureHelper.TABLE_ATTR_ORID_PATH, orid_path);
                                                                need_to_bind.add((Feature) t_);
                                                                MapHelper.updateFeature((Feature) t_, new AiRunnable(getNext()) {
                                                                    @Override
                                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                                        AiRunnable.Ok(getNext(), true, true);
                                                                        return null;
                                                                    }
                                                                });
                                                            } else { //需要新建
                                                                need_to_create.add(all_bdc_list.get(postion));
                                                                AiRunnable.Ok(getNext(), true, true);
                                                            }
                                                            return null;
                                                        }
                                                    });
                                                }
                                            }.start();
                                        } else {
                                            dialog.setContentView("未识别到新权利人!删除不合法权利人" + invalid_count + "个").setCancelable(true);
                                            AiRunnable.Ok(callback, true, true);
                                        }
                                        return null;
                                    }
                                });
                            } else {
                                dialog.setContentView("未识别到新权利人!删除不合法权利人" + invalid_count + "个").setCancelable(true);
                                AiRunnable.Ok(callback, true, true);
                            }
                            return null;
                        }
                    });
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "识别权利人失败!" + es);
            dialog.setContentView("识别权利人失败!" + es).setCancelable(true);
            AiRunnable.Ok(callback, false, false);
        }
    }


    public static void ScanQlr(final MapInstance mapInstance, final AiRunnable callback) {
        final OpenCVDialog dialog = new OpenCVDialog(mapInstance.activity);
        dialog.show();
        mapInstance.aiMap.view.postDelayed(new Runnable() {
            @Override
            public void run() {
                final Map<String, String> map_sfz = new HashMap<String, String>();
                dialog.setImageSize(1d * 428 / 270);
                dialog.recognize(new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Map<String, String> datas = (Map<String, String>) t_;
                        map_sfz.putAll(datas);
                        return null;
                    }
                });
                dialog.callback = new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {

                        final byte[] data_image = (byte[]) t_;
                        dialog.dismiss();
                        final String xm = map_sfz.get("xm");
                        final String xb = map_sfz.get("xb");
                        final String mz = map_sfz.get("mz");
                        final String zz = map_sfz.get("zz");
                        final String zjh = map_sfz.get("sfzh");
                        final String csrq = map_sfz.get("cs");
                        // 身份证信息可用
                        if (StringUtil.IsNotEmpty(xm) && StringUtil.IsNotEmpty(zjh)) {
                            NewFeature(mapInstance, zjh, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    final Feature feature = (Feature) t_;
                                    if (feature != null) {
                                        FeatureHelper.Set(feature, "ZJZL", "1");//[1]居民身份证
                                        FeatureHelper.Set(feature, "ZJH", zjh);
                                        FeatureHelper.Set(feature, "XM", xm, true, true);
                                        FeatureHelper.Set(feature, "XB", xb, true, true);
                                        FeatureHelper.Set(feature, "MZ", mz, true, true);
                                        FeatureHelper.Set(feature, "DZ", zz, true, true);
                                        FeatureHelper.Set(feature, "CSRQ", AiUtil.GetValue(csrq, (Date) null), true, true); //

                                        mapInstance.fillFeature(feature);
                                        // FeatureHelper.Set(feature, "CSRQ", AiUtil.GetValue(csrq,(Date)null), true, true);
                                        mapInstance.newFeatureView(feature).fillFeatureAddSave(feature, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                //FileUtils.getAppDirAndMK(getpath_root() + FeatureHelper.Get(feature, "XM", "")+"/"+FeatureHelper.FJCL + filename + "/"
                                                // String dir = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + FeatureHelper.Get(feature, "XM", "")+"/"+"附件材料/证件号/");
                                                String dir = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "/" + "附件材料/证件号/");
                                                String filename = dir + "/" + FeatureHelper.Get(feature, "XM", "") + AiUtil.GetValue(new Date(), "") + ".jpg";
                                                filename = filename.replaceAll(":", "：");
                                                try {
                                                    FileUtils.writeFile(filename, data_image);
                                                } catch (Exception es) {
                                                    ToastMessage.Send("写入文件失败", es);
                                                }
                                                AiRunnable.Ok(callback, feature);
                                                mapInstance.viewFeature(feature);
                                                return null;
                                            }
                                        });
                                    } else {
                                        ToastMessage.Send("无法增加权利人");
                                        AiRunnable.No(callback, null);
                                    }
                                    return null;
                                }
                            });
                        } else {
                            ToastMessage.Send("无法识别，请重新扫描");
                            AiRunnable.No(callback, null);
                        }
                        return null;
                    }

                    @Override
                    public <T_> T_ no(T_ t_, Object... objects) {
                        dialog.dismiss();
                        return null;
                    }
                };
            }
        }, 500);
    }

    //刷新权利人列表 20180816
    public static void reloadQlr(final MapInstance mapInstance, final CView cview) {
        getAllQlr(mapInstance, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                cview.setView(BuildView_QLRXX(mapInstance, cview, (List<Feature>) t_));
                return null;
            }
        });

    }

    //支持局部刷新
    public static void GetView_QLR(final MapInstance mapInstance, final CView cview, final AiRunnable callback) {
        getAllQlr(mapInstance, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final AiRunnable reload = new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        reloadQlr(mapInstance, cview);
                        return null;
                    }
                };

                cview.addAction("识别权利人", R.mipmap.app_search_pressed, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FeatureEditQLR.IdentyQlr(mapInstance, reload);
                    }
                });

                cview.addAction("添加权利人", R.mipmap.app_icon_add_thin, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FeatureEditQLR.CreateFeature(mapInstance, reload);
                    }
                });

                cview.setFloatRightAction(R.mipmap.app_icon_scan_white, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        FeatureEditQLR.ScanQlr(mapInstance, reload);
                        return null;
                    }
                });
                cview.setView(BuildView_QLRXX(mapInstance, cview, (List<Feature>) t_));
                AiRunnable.Ok(callback, true, true);
                return null;
            }
        });

//        final AiRunnable reload = new AiRunnable() {
//            @Override
//            public <T_> T_ ok(T_ t_, Object... objects) {
//                reloadQlr(mapInstance, cview);
//                return null;
//            }
//        };
//
//        cview.addAction("识别权利人", R.mipmap.app_search_pressed, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FeatureEditQLR.IdentyQlr(mapInstance, reload);
//            }
//        });
//
//        cview.addAction("添加权利人", R.mipmap.app_icon_add_thin, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FeatureEditQLR.CreateFeature(mapInstance, reload);
//            }
//        });
//
//        cview.setFloatRightAction(R.mipmap.app_icon_scan_white, new AiRunnable() {
//            @Override
//            public <T_> T_ ok(T_ t_, Object... objects) {
//                FeatureEditQLR.ScanQlr(mapInstance, reload);
//                return null;
//            }
//        });
//        View bdcView = mapInstance.newFeatureView(mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX)).getListView("", 0, null);
//        if(((ViewGroup)bdcView).getChildCount()>0) {
//            ((ViewGroup)bdcView).getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
//        }
//        cview.setView(bdcView);
//        cview.setView(BuildView_QLRXX(mapInstance, cview, (List<Feature>) t_));
//        AiRunnable.Ok(callback, true, true);

    }

//    // 根据传入的权利人集合生成可展开的权利人列表 局部可刷新 20180815
//    public static View BuildView_QLRXX(final MapInstance mapInstance, final CView cview, final List<Feature> features_qlr) {
//        try {
//            final int listItemRes = com.ovit.R.layout.app_ui_ai_aimap_xm_item;
//            final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
//            final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
//            ll_list_item.setVisibility(View.VISIBLE);
//            if (ll_view.getChildCount() > 0) {
//                ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
//            }
//
//            QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, R.layout.app_ui_ai_aimap_xm_item, features_qlr) {
//                @Override
//                protected void convert(final BaseAdapterHelper helper, final Feature item) {
//                    final String name = item.getAttributes().get("XM") + "";
//                    final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
//                    helper.setText(R.id.tv_name, name);
//
//                    int s = (int) (0 * mapInstance.activity.getResources().getDimension(com.ovit.R.dimen.app_size_smaller));
//                    helper.getView(com.ovit.R.id.v_split).getLayoutParams().width = s;
//
//                    //不动产列表
//                    helper.getView().setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
//                            if (!flag) {
//                                ll_list_item.removeAllViews();
//                                getAllBdcByQLR(mapInstance, item, new AiRunnable() {
//                                    @Override
//                                    public <T_> T_ ok(T_ t_, Object... objects) {
//                                        ll_list_item.addView(FeatureViewQLR.buildBdcViewByList(mapInstance, (List<Feature>) t_, true, 0));
//                                        return null;
//                                    }
//                                });
//                            }
//                            ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
//                        }
//                    });
//
//                    //长按删除权利人
//                    helper.getView().setOnLongClickListener(new View.OnLongClickListener() {
//                        @Override
//                        public boolean onLongClick(View v) {
//                            AiDialog dialog = AiDialog.get(mapInstance.activity);
//                            dialog.setHeaderView("确认删除此不动产单元吗?");
//                            dialog.setContentView("此操作不可逆转，请谨慎执行！");
//                            dialog.setFooterView(AiDialog.CENCEL, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(final DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            }, null, null, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(final DialogInterface dialog, int which) {
//                                    MapHelper.deleteFeature(item, new AiRunnable() {
//                                        @Override
//                                        public <T_> T_ ok(T_ t_, Object... objects) {
//                                            ToastMessage.Send("删除成功!");
//                                            reloadQlr(mapInstance, cview);
//                                            dialog.dismiss();
//                                            return null;
//                                        }
//                                    });
//                                }
//                            }).show();
//                            return false;
//                        }
//                    });
//
//                    helper.getView(R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            mapInstance.viewFeature(item);
//                        }
//                    });
//
//                    helper.getView(R.id.iv_add).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            ll_list_item.setVisibility(View.GONE);
//                            FeatureEditGYR.createNewGYR(mapInstance, item);  //调用权利人信息编辑面板 20180717
//                        }
//                    });
//                }
//            };
//
//            ll_list_item.setTag(adapter);
//            adapter.adpter(ll_list_item);
//            adapter.notifyDataSetChanged();
//            return ll_view;
//        } catch (Exception es) {
//            Log.e(TAG, "生成可展开的权利人列表失败!" + es);
//            return null;
//        }
//
//    }

    // 根据传入的权利人集合 生成可展开的
    public static View BuildView_QLRXX(final MapInstance mapInstance, final CView cview, final List<Feature> features_qlr) {
        try {
            final int listItemRes = com.ovit.R.layout.app_ui_ai_aimap_xm_item;
            final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
            final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
            ll_list_item.setVisibility(View.VISIBLE);
            if (ll_view.getChildCount() > 0) {
                ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
            }

            QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, R.layout.app_ui_ai_aimap_xm_item, features_qlr) {
                @Override
                protected void convert(final BaseAdapterHelper helper, final Feature item) {
                    final String name = item.getAttributes().get("XM") + "";
                    final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                    helper.setText(R.id.tv_name, name);

                    int s = (int) (0 * mapInstance.activity.getResources().getDimension(com.ovit.R.dimen.app_size_smaller));
                    helper.getView(com.ovit.R.id.v_split).getLayoutParams().width = s;

                    //不动产列表
                    helper.getView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                            if (!flag) {
                                ll_list_item.removeAllViews();
                                getAllBdcByQLR(mapInstance, item, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        ll_list_item.addView(FeatureViewQLR.buildBdcViewByList(mapInstance, (List<Feature>) t_, true, 0));
                                        return null;
                                    }
                                });
                            }
                            ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
                        }
                    });

                    //长按删除权利人
                    helper.getView().setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            AiDialog dialog = AiDialog.get(mapInstance.activity);
                            dialog.setHeaderView("确认删除此不动产单元吗?");
                            dialog.setContentView("此操作不可逆转，请谨慎执行！");
                            dialog.setFooterView(AiDialog.CENCEL, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }, null, null, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    MapHelper.deleteFeature(item, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            ToastMessage.Send("删除成功!");
                                            reloadQlr(mapInstance, cview);
                                            dialog.dismiss();
                                            return null;
                                        }
                                    });
                                }
                            }).show();
                            return false;
                        }
                    });

                    helper.getView(R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mapInstance.viewFeature(item);
                        }
                    });

                    helper.getView(R.id.iv_add).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ll_list_item.setVisibility(View.GONE);
                            FeatureEditGYR.createNewGYR(mapInstance, item);  //调用权利人信息编辑面板 20180717
                        }
                    });
                }
            };

            ll_list_item.setTag(adapter);
            adapter.adpter(ll_list_item);
            adapter.notifyDataSetChanged();
            return ll_view;
        } catch (Exception es) {
            Log.e(TAG, "生成可展开的权利人列表失败!" + es);
            return null;
        }

    }


    //权利人绑定宗地 (不持久化 不级联)20180815
    public static void bindZD(final MapInstance mapInstance, final Feature feature_qlr, final Feature feature_zd) {
        try {
            feature_zd.getAttributes().put("QLRXM", FeatureHelper.Get(feature_qlr, "XM"));
            feature_zd.getAttributes().put("QLRZJH", FeatureHelper.Get(feature_qlr, "ZJH"));
            feature_zd.getAttributes().put("QLRZJZL", FeatureHelper.Get(feature_qlr, "ZJZL"));
            feature_zd.getAttributes().put("QLRTXDZ", FeatureHelper.Get(feature_qlr, "DZ"));
            feature_zd.getAttributes().put("QLRDH", FeatureHelper.Get(feature_qlr, "DH"));
            feature_zd.getAttributes().put("TDZH", FeatureHelper.Get(feature_qlr, "BDCQZH"));
            //关联权利人和宗地
            String qlr_orid_path = FeatureHelper.Get(feature_qlr, FeatureHelper.TABLE_ATTR_ORID_PATH) + "";
            qlr_orid_path += FeatureHelper.Get(feature_zd, FeatureHelper.TABLE_ATTR_ORID) + "/";
            FeatureHelper.Set(feature_qlr,FeatureHelper.TABLE_ATTR_ORID_PATH, qlr_orid_path);

            //拷贝资料
            String f_zd_path = mapInstance.getpath_feature(feature_zd);
            String f_qlr_path = mapInstance.getpath_feature(feature_qlr);

            String f_zd_zjh_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/权利人证件号/");
            String f_qlr_zjh_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/证件号/");
            FileUtils.copyFile(f_qlr_zjh_path, f_zd_zjh_path);

            String f_zd_zmcl_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/土地权属来源证明材料/");
            String f_qlr_zmcl_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/土地权属来源证明材料/");
            FileUtils.copyFile(f_qlr_zmcl_path, f_zd_zmcl_path);

            String f_zd_hkb_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/户口簿/");
            String f_qlr_hkb_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/户口簿/");
            FileUtils.copyFile(f_qlr_hkb_path, f_zd_hkb_path);
        } catch (Exception es) {
            Log.e(TAG, "权利人绑定宗地失败!" + es);
        }

    }

    //权利人绑定自然幢 (不持久化 不级联)20180815
    public static void bindZRZ(final MapInstance mapInstance, final Feature feature_qlr, final Feature feature_zrz) {
        try {
            String qlr_orid_path = FeatureHelper.Get(feature_qlr, FeatureHelper.TABLE_ATTR_ORID_PATH) + "";
            qlr_orid_path += FeatureHelper.Get(feature_zrz, FeatureHelper.TABLE_ATTR_ORID) + "/";
            feature_qlr.getAttributes().put(FeatureHelper.TABLE_ATTR_ORID_PATH, qlr_orid_path);
        } catch (Exception es) {
            Log.e(TAG, "权利人绑定自然幢失败!" + es);
        }

    }

    //权利人绑定逻辑幢 (不持久化 不级联)20180815
    public static void bindLJZ(final MapInstance mapInstance, final Feature feature_qlr, final Feature feature_ljz) {
        try {
            String qlr_orid_path = FeatureHelper.Get(feature_qlr, FeatureHelper.TABLE_ATTR_ORID_PATH) + "";
            qlr_orid_path += FeatureHelper.Get(feature_ljz, FeatureHelper.TABLE_ATTR_ORID) + "/";
            feature_qlr.getAttributes().put(FeatureHelper.TABLE_ATTR_ORID_PATH, qlr_orid_path);
        } catch (Exception es) {
            Log.e(TAG, "权利人绑定逻辑幢失败!" + es);
        }

    }

    //权利人绑定户 (不持久化 不级联)20180815
    public static void bindH(final MapInstance mapInstance, final Feature feature_qlr, final Feature feature_h) {
        try {
            feature_h.getAttributes().put("QLRXM", FeatureHelper.Get(feature_qlr, "XM"));
            feature_h.getAttributes().put("QLRZJH", FeatureHelper.Get(feature_qlr, "ZJH"));
            feature_h.getAttributes().put("QLRZJZL", FeatureHelper.Get(feature_qlr, "ZJZL"));
            feature_h.getAttributes().put("QLRTXDZ", FeatureHelper.Get(feature_qlr, "DZ"));
            feature_h.getAttributes().put("QLRDH", FeatureHelper.Get(feature_qlr, "DH"));

            String qlr_orid_path = FeatureHelper.Get(feature_qlr, FeatureHelper.TABLE_ATTR_ORID_PATH) + "";
            qlr_orid_path += FeatureHelper.Get(feature_h, FeatureHelper.TABLE_ATTR_ORID) + "/";
            feature_qlr.getAttributes().put(FeatureHelper.TABLE_ATTR_ORID_PATH, qlr_orid_path);

            //拷贝资料
            String f_h_path = mapInstance.getpath_feature(feature_h);
            String f_qlr_path = mapInstance.getpath_feature(feature_qlr);

            String f_h_zjh_path = FileUtils.getAppDirAndMK(f_h_path + "/" + "附件材料/证件号/");
            String f_qlr_zjh_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/证件号/");
            FileUtils.copyFile(f_qlr_zjh_path, f_h_zjh_path);
        } catch (Exception es) {
            Log.e(TAG, "权利人绑定户失败!" + es);
        }

    }

    //选择要绑定的不动产 20180723
    private void selectBDC(final MapInstance mapInstance, final Feature qlr_feature) {
        String where = "QLRXM is '' or QLRXM is null"; //此为查询语句

        final List<Feature> selected_feature_list = new ArrayList<>(); //用于存放选中的feature

        final AiDialog dialog = FeatureViewQLR.getBindBDC_View(mapInstance, where, selected_feature_list);

        if (dialog != null) {
            dialog.setFooterView(AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    bindBDC(mapInstance, selected_feature_list, qlr_feature, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            if (selected_feature_list.size() > 0) {
                                ToastMessage.Send("成功绑定" + selected_feature_list.size() + "个不动产");
                                mapInstance.tool.map_opt_per();
                                mapInstance.viewFeature(qlr_feature);
                            } else {
                                ToastMessage.Send("没有绑定任何不动产!");
                            }
                            dialog.dismiss();
                            return null;
                        }
                    });
                }
            }, null, null, AiDialog.CENCEL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ToastMessage.Send("您取消了绑定不动产！");
                    dialog.dismiss();
                }
            });
        } else {
            ToastMessage.Send("获得不动产列表失败!");
        }

    }

    //在这里完成不动产绑定逻辑
    private static void bindBDC(final MapInstance mapInstance, final List<Feature> selected_feature_list, Feature feature_qlr, final AiRunnable callback) {
        try {
            if (StringUtil.IsEmpty(FeatureHelper.Get(feature_qlr, FeatureHelper.TABLE_ATTR_ORID_PATH))) {
                FeatureHelper.Set(feature_qlr, FeatureHelper.TABLE_ATTR_ORID_PATH, "/");
            }

            for (Feature feature : selected_feature_list) {
                switch (feature.getFeatureTable().getTableName()) {
                    case FeatureHelper.TABLE_NAME_ZD:
                        bindZD(mapInstance, feature_qlr, feature);
                        break;
                    case FeatureHelper.TABLE_NAME_ZRZ:
                        bindZRZ(mapInstance, feature_qlr, feature);
                        break;
                    case FeatureHelper.TABLE_NAME_LJZ:
                        bindLJZ(mapInstance, feature_qlr, feature);
                        break;
                    case FeatureHelper.TABLE_NAME_H:
                        bindH(mapInstance, feature_qlr, feature);
                        break;
                    default: {
                        String qlr_orid_path = FeatureHelper.Get(feature_qlr, FeatureHelper.TABLE_ATTR_ORID_PATH) + "";
                        qlr_orid_path += FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID) + "/";
                        feature_qlr.getAttributes().put(FeatureHelper.TABLE_ATTR_ORID_PATH, qlr_orid_path);
                        break;
                    }
                }
            }

            MapHelper.updateFeature(feature_qlr, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    new AiForEach<Feature>(selected_feature_list, callback) {
                        public void exec() {
                            MapHelper.updateFeature(selected_feature_list.get(postion), getNext());
                        }
                    }.start();
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "绑定不动产失败！" + es);
            AiRunnable.Ok(callback, false, false);
        }
    }

    public static void load_bdcdy(final MapInstance mapInstance, final Feature feature_bdc, final View ft_view) {
        if (feature_bdc.getFeatureTable().getTableName().equals(FeatureHelper.TABLE_NAME_ZD)) {
            String where = "ORID_PATH like'%" + FeatureHelper.Get(feature_bdc, FeatureHelper.TABLE_ATTR_ORID) + "%'";
            FeatureTable table = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX);
            MapHelper.Query(table, where, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    ((ViewGroup) ft_view).removeAllViews();
                    ((ViewGroup) ft_view).addView(buildBdcdyViewByList(mapInstance, (List<Feature>) t_, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {

                            return null;
                        }
                    }));
                    return null;
                }
            });
        }
    }

    //外部接口 根据给定的集合生成不动产单元情况视图 20180804
    public static View buildBdcdyViewByList(final MapInstance mapInstance, List<Feature> features, final AiRunnable callback) {
        try {
            final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
            final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
            final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
            if (ll_view.getChildCount() > 0) {
                ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
            }

            final QuickAdapter adapter = geBdcdyAdapter(mapInstance, listItemRes, features, callback);

            if (adapter != null) {
                ll_list_item.setTag(adapter);
                adapter.adpter(ll_list_item);
                adapter.notifyDataSetChanged();
            }

            return ll_view;
        } catch (Exception es) {
            Log.e(TAG, "加载不动产单元失败!" + es);
            ToastMessage.Send("宗地加载不动产单元失败!" + es);
            return null;
        }

    }

    //辅助函数 生成不动产单元记录适配器 20180802
    private static QuickAdapter<Feature> geBdcdyAdapter(final MapInstance mapInstance, final int listItemRes, final List<Feature> features, final AiRunnable callback) {
        try {
            final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, features) {
                @Override
                protected void convert(final BaseAdapterHelper helper, final Feature item) {
                    initBdcdyItem(mapInstance, helper, item, 0, callback);
                }
            };

            return adapter;
        } catch (Exception es) {

            return null;
        }
    }

    //辅助函数 初始化不动产单元情况列表中每条记录的样式和事件 20180802
    private static void initBdcdyItem(final MapInstance mapInstance, final BaseAdapterHelper helper, final Feature item, final int deep, final AiRunnable callback) {
        try {
            helper.getView().findViewById(com.ovit.R.id.iv_position).setVisibility(View.GONE);
            String tvName = FeatureHelper.Get(item, "XM", "");
            helper.setText(com.ovit.app.map.R.id.tv_groupname, "不动产");
            helper.setText(com.ovit.app.map.R.id.tv_name, tvName);
            helper.setText(com.ovit.R.id.tv_desc, item.getAttributes().get(FeatureHelper.TABLE_ATTR_ORID_PATH) + "");

            int s = (int) (deep * mapInstance.activity.getResources().getDimension(com.ovit.R.dimen.app_size_smaller));
            helper.getView(com.ovit.R.id.v_split).getLayoutParams().width = s;
            //异步设置icon和name

            //监听函数
            helper.getView().findViewById(com.ovit.R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapInstance.viewFeature(item);
                }
            });
            //长按删除分摊情况
            helper.getView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteBdcdy(mapInstance, item, callback);
                    return false;
                }
            });

        } catch (Exception es) {
            Log.e(TAG, "加载不动产单元列表失败！" + es);
        }

    }

    /**
     * 删除不动产单元，解除与不动产的绑定
     *
     * @param mapInstance
     * @param item        不动产单元
     * @param callback
     */
    private static void deleteBdcdy(MapInstance mapInstance, final Feature item, final AiRunnable callback) {
        // Todo 1、删除不动产单元,2、解除房产与不动产的绑定 3、解除权利人与不动产单元的绑定
        AiDialog dialog = AiDialog.get(mapInstance.activity);
        dialog.setHeaderView("确认删除此不动产单元吗?");
        dialog.setContentView("此操作不可逆转，请谨慎执行！");
        dialog.setFooterView(AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                MapHelper.deleteFeature(item, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        ToastMessage.Send("删除成功!");
                        AiRunnable.Ok(callback, true, true);
                        dialog.dismiss();
                        return null;
                    }
                });
            }
        }, null, null, AiDialog.CENCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    public static String GetBdcdyh(Feature featureBdc) {
        return FeatureHelper.Get(featureBdc, FeatureHelper.TABLE_ATTR_BDCDYH, "");
    }

    public void dy(Feature f, final MapInstance mapInstance, boolean isRelaod) {

        final ProgressDialog progressDialog = DialogBuilder.loadingDialog(mapInstance.activity,
                "加载中...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        FeatureEditBDC.CreateDOCXFromFeatureBdc(mapInstance, feature, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                progressDialog.dismiss();
                FileUtils.openFile(mapInstance.activity, t_ + "", true);
                return null;
            }

            @Override
            public <T_> T_ no(T_ t_, Object... objects) {
                progressDialog.dismiss();
                return super.no(t_, objects);
            }

            @Override
            public <T_> T_ error(T_ t_, Object... objects) {
                progressDialog.dismiss();
                return super.error(t_, objects);
            }
        });
    }

}
