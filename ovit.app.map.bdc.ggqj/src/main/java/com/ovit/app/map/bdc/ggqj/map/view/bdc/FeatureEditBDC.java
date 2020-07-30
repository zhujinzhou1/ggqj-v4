package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.ImmutablePart;
import com.esri.arcgisruntime.geometry.ImmutablePartCollection;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.authentication.pojo.AppDevice;
import com.ovit.app.core.License;
import com.ovit.app.lzdb.bean.JZD;
import com.ovit.app.lzdb.bean.QLRXX;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.model.DxfFcfwh;
import com.ovit.app.map.bdc.ggqj.map.model.Dxfzdct_tongshan;
import com.ovit.app.map.bdc.ggqj.map.util.Excel;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.custom.shape.ShapeUtil;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.DialogBuilder;
import com.ovit.app.ui.dialog.ProgressDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.ReportUtils;
import com.ovit.app.util.SharedUtils;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.Zip;
import com.ovit.app.util.gdal.cad.DxfHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Lichun on 2017/4/5.
 */

public class FeatureEditBDC extends FeatureEdit {
    final static String TAG = "FeatureEditBDC";
    private String zddm;
    private String zl;

    public FeatureEditBDC() {
        super();
    }

    public FeatureEditBDC(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }

    public FeatureEditBDC(MapInstance mapInstance, Feature feature, int resid) {
        super(mapInstance, feature, resid);
    }

    //region  重写父类方法
    // 初始化
    @Override
    public void init() {
        super.init();
        zddm = AiUtil.GetValue(feature.getAttributes().get(FeatureHelper.TABLE_ATTR_ZDDM), "");
        zl = AiUtil.GetValue(feature.getAttributes().get("ZL"), "");

    }

    @Override
    public String getId() {
        return FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
    }

    @Override
    public String getName() {
        return "[" + MapHelper.getLayerName(feature) + "]" + FeatureHelper.Get(feature, "QLRXM", "");
    }

    // 显示数据
    @Override
    public void build() {
        LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_bdc, v_content);

        try {
            if (feature != null) {
                ((EditText) v_feature.findViewById(R.id.et_bdcdyh)).setText(id);
                ((EditText) v_feature.findViewById(R.id.et_zddm)).setText(zddm);
                ((EditText) v_feature.findViewById(R.id.et_zl)).setText(zl);
                ((EditText) v_feature.findViewById(R.id.et_qlrxm)).setText(name);
                // 填充控件
//                 fillView(v_feature);
//                loadzd(null);
//                loadjzd();
//                loadjzx();
//                loadzrz();
//                loadh();
            }
        } catch (Exception es) {
            Log.e(TAG, "build: 构建失败", es);
        }

    }


    // 设置菜单和工具
    @Override
    public void build_opt() {

        addAction("定位", R.mipmap.app_icon_opt_location, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapHelper.selectAddCenterFeature(map, feature);
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.ll_opt_zd) {
                    showzd();
                } else if (id == R.id.ll_opt_zrz) {
                    showzrz();
                } else if (id == R.id.ll_opt_h) {
                    showh();
                } else if (id == R.id.ll_opt_zdct) {
                    showzdct();
                } else if (id == R.id.ll_opt_fcfft) {
                    showfcfft();
                } else if (id == R.id.ll_opt_qjxsyt) {
                    showqjxsyt();
//                } else if (id == R.id.ll_opt_dw) {
//                    MapHelper.center(map, feature.getGeometry());
                } else if (id == R.id.ll_opt_jczl) {
                    ToastMessage.Send(activity, "正在检查资料...");
                } else if (id == R.id.ll_opt_dy) {
                    dy(false);
                } else if (id == R.id.ll_opt_share) {
                    share(false);
                } else if (id == R.id.ll_opt_excel) {
                    //出excel资料 20180822
                    createExcelDialog(mapInstance);
                } else if (id == R.id.ll_opt_zncl) {
                    znclToZd(false, null);
                }
            }
        };

        view.findViewById(R.id.ll_opt_zd).setOnClickListener(listener);
        view.findViewById(R.id.ll_opt_zrz).setOnClickListener(listener);
        view.findViewById(R.id.ll_opt_h).setOnClickListener(listener);
        view.findViewById(R.id.ll_opt_zdct).setOnClickListener(listener);
        view.findViewById(R.id.ll_opt_fcfft).setOnClickListener(listener);
        view.findViewById(R.id.ll_opt_qjxsyt).setOnClickListener(listener);
//        view.findViewById(R.id.ll_opt_dw).setOnClickListener(listener);
        view.findViewById(R.id.ll_opt_jczl).setOnClickListener(listener);
        view.findViewById(R.id.ll_opt_dy).setOnClickListener(listener);
        view.findViewById(R.id.ll_opt_excel).setOnClickListener(listener);
        view.findViewById(R.id.ll_opt_zncl).setOnClickListener(listener);
        view.findViewById(R.id.ll_opt_dy).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dy(true);
                return false;
            }
        });


        view.findViewById(R.id.ll_opt_share).setOnClickListener(listener);
        view.findViewById(R.id.ll_opt_share).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                share(true);
                return false;
            }
        });

    }

    // 保存数据
    @Override
    public void update(final AiRunnable callback) {
        try {
//            super.update(callback);
        } catch (Exception es) {
            ToastMessage.Send(activity, "更新属性失败!", TAG, es);
        }
    }

    public static String GetPath(MapInstance mapInstance, String bdcdyh) {
        return mapInstance.getpath_root() + "/资料库/不动产单元/" + bdcdyh + "/";
    }

    // 模板所在文件夹
    public static String GetPath_Templet() {
        return "config/data/";
    }


    //endregion

    //region  数据加载


    public static boolean IsF00000000(String bdcdyh) {
        return (bdcdyh + "").endsWith("F00000000");
    }

    public static String GetWhere_dcdyh(String bdcdyh) {
        if (IsF00000000(bdcdyh)) {
            return GetZDDM(bdcdyh) + "F________";
        } else {
            return bdcdyh;
        }
    }

    public static String GetZRZH(String bdcdyh) {
        return StringUtil.substr(bdcdyh, 0, bdcdyh.length() - 4);
    }

    public static String GetZDDM(String bdcdyh) {
        return StringUtil.substr(bdcdyh, 0, bdcdyh.length() - 9);
    }

    public static String GetPath_ZD_dir(MapInstance mapInstance, Feature f_zd) {
        return FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd));
    }

    public static String GetPath_ZD_zip(MapInstance mapInstance, Feature f_zd) {
        return FileUtils.getAppDirAndMK(mapInstance.getpath_layer(f_zd.getFeatureTable().getFeatureLayer())) + mapInstance.getOrid(f_zd) + ".ovitmap";
    }

    public static String GetPath_BDC_doc(MapInstance mapInstance, String bdcdyh) {
        return FileUtils.getAppDirAndMK(GetPath(mapInstance, bdcdyh) + FeatureHelper.FJCL) + "不动产权籍调查表" + bdcdyh + ".docx";
    }

    public static String GetPath_doc(MapInstance mapInstance, String bdcdyh, String id, Feature f_bdc) {
        return FileUtils.getAppDirAndMK(GetPath(mapInstance, mapInstance.getOrid(f_bdc)) + FeatureHelper.FJCL) + id + bdcdyh + ".docx";
    }

    public static void LoadZD(MapInstance mapInstance, String bdcdyh, final AiRunnable callback) {
        FeatureViewZD.From(mapInstance).loadByZddm(GetZDDM(bdcdyh), callback);
    }

    public static void LoadJZDXQZ(final MapInstance mapInstance, final Feature f_zd, final List<Feature> fs_jzd, final List<Feature> fs_jzx,
                                  final Map<String, Feature> map_jzx, final List<Map<String, Object>> fs_jzqz, final AiRunnable callback) {
        FeatureView.LoadJzdxqz(mapInstance, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, callback);
    }

    public static void LoadJZD(MapInstance mapInstance, Feature f_zd, List<Feature> fs_jzd, AiRunnable callback) {
        mapInstance.newFeatureView(f_zd).loadJzds(fs_jzd, callback);
    }

    public static void LoadJZX(MapInstance mapInstance, Feature f_zd, List<Feature> fs_jzd, List<Feature> fs_jzx, AiRunnable callback) {
        FeatureView.LoadJzdx(mapInstance, f_zd, fs_jzd, fs_jzx, callback);
    }

    public static void LoadZRZ(MapInstance mapInstance, String bdcdyh, List<Feature> fs_zrz, AiRunnable callback) {
        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZRZ), StringUtil.WhereByIsEmpty(bdcdyh) + " BDCDYH like '" + GetWhere_dcdyh(bdcdyh) + "' ", "ZRZH", "asc", -1, fs_zrz, callback);
    }

    public static void LoadH(MapInstance mapInstance, String bdcdyh, List<Feature> fs_h, AiRunnable callback) {
        //MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_H), " BDCDYH like '" + GetWhere_dcdyh(bdcdyh) + "' ", "ID", "asc", -1, fs_h, callback);
        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_H), StringUtil.WhereByIsEmpty(bdcdyh) + " BDCDYH like '" + GetWhere_dcdyh(bdcdyh) + "' ", "ID", "asc", -1, fs_h, callback);
    }

    public static void LoadZ_FSJG(MapInstance mapInstance, String bdcdyh, List<Feature> fs_z_fsjg, AiRunnable callback) {
        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_Z_FSJG), StringUtil.WhereByIsEmpty(bdcdyh) + " ZID like '" + GetZRZH(GetWhere_dcdyh(bdcdyh)) + "' ", "ZID", "asc", -1, fs_z_fsjg, callback);
    }

    public static void LoadH_FSJG(MapInstance mapInstance, String bdcdyh, List<Feature> fs_h_fsjg, AiRunnable callback) {
        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_H_FSJG), StringUtil.WhereByIsEmpty(bdcdyh) + " HID like '" + GetWhere_dcdyh(bdcdyh) + "' ", "HID", "asc", -1, fs_h_fsjg, callback);
    }

    public static void LoadAll(final MapInstance mapInstance, final String bdcdyh,
                               final Feature f_zd,
                               final List<Feature> fs_jzd,
                               final List<Feature> fs_jzx,
                               final Map<String, Feature> map_jzx,
                               final List<Map<String, Object>> fs_jzqz,
                               final List<Feature> fs_zrz,
                               final List<Feature> fs_z_fsjg,
                               final List<Feature> fs_h,
                               final List<Feature> fs_h_fsjg,
                               final AiRunnable callback) {

        LoadJZDXQZ(mapInstance, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                LoadZRZ(mapInstance, bdcdyh, fs_zrz, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        LoadH(mapInstance, bdcdyh, fs_h, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                LoadZ_FSJG(mapInstance, bdcdyh, fs_z_fsjg, new AiRunnable(callback) {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        LoadH_FSJG(mapInstance, bdcdyh, fs_h_fsjg, callback);
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


    //endregion

    //region  界面方法
    private void showzd() {
        LoadZD(mapInstance, GetWhere_dcdyh(id), new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Feature f = (Feature) t_;
                if (f != null) {
                    mapInstance.viewFeature(f);
                } else {
                    ToastMessage.Send("没有找到宗地信息");
                }
                return null;
            }
        });
    }

    private void showzrz() {
        final List<Feature> fs = new ArrayList<>();
        LoadZRZ(mapInstance, GetWhere_dcdyh(id), fs, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                if (fs.size() > 0) {
                    mapInstance.viewFeature(fs.get(0));
                } else {
                    ToastMessage.Send("没有找到幢信息");
                }
                return null;
            }
        });
    }

    private void showh() {
        final List<Feature> fs = new ArrayList<>();
        LoadH(mapInstance, GetWhere_dcdyh(id), fs, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                if (fs.size() > 0) {
                    mapInstance.viewFeature(fs.get(0));
                } else {
                    ToastMessage.Send("没有找到户信息");
                }
                return null;
            }
        });
    }

    private void showzdct() {
        LoadZD(mapInstance, GetWhere_dcdyh(id), new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Feature f = (Feature) t_;
                if (f != null) {
                    FeatureViewZD.From(mapInstance, f).loadZdct(false, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            String file = t_ + "";
                            Intent intent = FileUtils.getImageFileIntent(file);
                            activity.startActivity(intent);
                            return null;
                        }
                    });
                } else {
                    ToastMessage.Send("没有找到宗地信息");
                }
                return null;
            }
        });
    }

    private void showfcfft() {
        final List<Feature> fs = new ArrayList<>();
        LoadZRZ(mapInstance, GetWhere_dcdyh(id), fs, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                if (fs.size() > 0) {
                    FeatureViewZRZ.LaodAllZRZ_CreateFCFHT(mapInstance, fs, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            ToastMessage.Send(activity, "分层分户画已经绘制完成");
                            return null;
//                            return super.ok(t_, objects);
                        }
                    });
                } else {
                    ToastMessage.Send("没有找到幢信息");
                }
                return null;
            }
        });
    }


    private void showqjxsyt() {
//        loadzrz(new AiRunnable() {
//            @Override
//            public <T_> T_ ok(T_ t_, Object... objects) {
//                MapHelper.geometry_ct(map, f_zd.getGeometry(), new AiRunnable() {
//                    @Override
//                    public <T_> T_ ok(T_ t_, Object... objects) {
//                        try {
//                            Bitmap bp = (Bitmap) t_;
//                            if (bp != null) {
//
//                                String file = FileUtils.writeFile(image_qjxsyt, bp);
//                                Intent intent = FileUtils.getImageFileIntent(file);
//                                activity.startActivity(intent);
//
//                                image_qjxsyt = file;
//                            }
//                        } catch (Exception es) {
//                            ToastMessage.Send(activity, "写入文件失败", es);
//                        }
//                        return null;
//                    }
//                });
//                return null;
//            }
//        });
    }

    // endregion

    //region  方法

//    private void draw_fcfht(final List<Feature> zrzs, final int index, final AiRunnable callback) {
//        if (index < zrzs.size()) {
//            FeatureEditZRZ.LaodAllZRZ_CreateFCFHT(mapInstance,zrzs, new AiRunnable(callback) {
//                @Override
//                public <T_> T_ ok(T_ t_, Object... objects) {
//                    draw_fcfht(zrzs, index + 1, callback);
//                    return null;
//                }
//            });
//        } else {
//            if (callback != null) {
//                callback.ok(zrzs);
//            }
//        }
//    }

    // 打印
    private void dy(boolean isRelaod) {
        if (TextUtils.isEmpty(id)) {
            ToastMessage.Send("没有不动产单元号，请保存宗地！");
            return;
        }
        final ProgressDialog progressDialog = DialogBuilder.loadingDialog(mapInstance.activity,
                "加载中...");
        progressDialog.show();
        progressDialog.setCancelable(false);

        CreateDOCX(mapInstance, id, isRelaod, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                progressDialog.dismiss();
                FileUtils.openFile(activity, t_ + "", true);
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

    // 打包
    private void share(boolean isRelaod) {
        final ProgressDialog progressDialog = DialogBuilder.loadingDialog(mapInstance.activity,
                "加载中...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        Pack(mapInstance, id, isRelaod, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                progressDialog.dismiss();
                shareDialog(activity);
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

    // 智能
    private void znclToZd(boolean isRelaod, final AiRunnable callback) {
        final String funcdesc = "该功能将逐一对宗地内房屋进行智能处理："
                + "\n 1、通过逻辑幢合成自然幢；"
                + "\n 2、自然幢生成层；"
                + "\n 3、逻辑幢快速生成户。"
                + "\n 4、宗地，自然幢，逻辑幢，层，户，附属结构关系建立。";
        License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final AiDialog aidialog = AiDialog.get(mapInstance.activity);
                aidialog.setHeaderView(R.mipmap.app_icon_rgzl_blue, "智能处理")
                        .setContentView("注意：属于不可逆操作，将对宗地面积、宗地草图、分层分幅图重新智能处理，如果您已经输出过成果，请注意备份谨慎处理！", funcdesc)
                        .setFooterView(AiDialog.CENCEL, "确定，我要继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 完成后的回掉
                                final AiRunnable callback = new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据完成，你可能还需要重新生成成果。");
                                        aidialog.setFooterView("重新生成成果", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
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
                                // 设置不可中断
                                aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                                aidialog.setContentView("开始处理数据" + "ZDDM=" + FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ZDDM, ""));
                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "查找所有的逻辑幢，并合成自然幢");
                                Log.d(TAG, "智能处理:查找所有的逻辑幢，并合成自然幢");
                                // 查询宗地范围内的逻辑幢
                                final FeatureViewZD fv_zd = (FeatureViewZD) mapInstance.newFeatureView(feature);
                                final List<Feature> featuresLJZ = new ArrayList<>();
                                MapHelper.Query(mapInstance.getTable(FeatureHelper.TABLE_NAME_LJZ), feature.getGeometry(), featuresLJZ, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        // 通过逻辑幢合成自然幢
                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查询到" + featuresLJZ.size() + "个逻辑幢。");
                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 通过逻辑幢合成自然幢。");
                                        final List<Feature> featuresZRZ = new ArrayList<>();
                                        creatZrzToLjzUnion(featuresLJZ, featuresZRZ, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                ToastMessage.Send("通过逻辑幢合成自然幢");
                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已合成" + featuresZRZ.size() + "自然幢。");
                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 宗地识别自然幢");
                                                // zd 识别自然幢;
                                                fv_zd.identyZrzFromZD(mapInstance, feature, featuresZRZ, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        // 自然幢识别逻辑幢
                                                        ToastMessage.Send("zd 识别自然幢");
                                                        indentyLjzToZrz(featuresZRZ, new AiRunnable() {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                // 自然幢生成层
                                                                ToastMessage.Send("自然幢识别逻辑幢完成");
                                                                creatCToZrz(featuresZRZ, new AiRunnable() {
                                                                    @Override
                                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                                        // 通过宗地生成不动产单元
                                                                        ToastMessage.Send("层生成完成");
                                                                        createBdcdyToZd(feature, new AiRunnable() {
                                                                            @Override
                                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                ToastMessage.Send("宗地生成权利人，不动产单元成功。");
                                                                                AiRunnable.Ok(callback, t_, objects);
//                                                                                        aidialog.dismiss();
                                                                                // 输出成果
//                                                                                         OutputAllBdcdyResultsToZD(feature,null);
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
                        }).show();
                ;

                return null;
            }
        });
    }


    private void OutputAllBdcdyResultsToZD(Feature feature, final AiRunnable callback) {
        if (feature.getFeatureTable().getTableName().equals(FeatureHelper.TABLE_NAME_ZD)) {
            String where = "ORID_PATH like'%" + FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID) + "%'";
            FeatureTable table = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX);
            MapHelper.Query(table, where, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    List<Feature> featuresBdcdy = (List<Feature>) t_;
                    new AiForEach<Feature>(featuresBdcdy, callback) {
                        @Override
                        public void exec() {
                            CreateDOCXFromFeatureBdc(mapInstance, getValue(), getNext());
                        }
                    }.start();

                    return null;
                }
            });
        }
    }

    public void creatCToZrz(final List<Feature> featuresZRZ, final AiRunnable callback) {
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

    private void createBdcdyToZd(Feature featureZD, AiRunnable callback) {
        // 1   只有一个权利人
        //1.1  新建权利人
        //1.2  新建不动产单元
        //1.3  权利人不动产单元绑定
        String type = FeatureHelper.Get(featureZD, "lx", "");
        type = "0";
        if ("0".equals(type)) {
            // 一个使用权人，一个不动产单元
            createBdcdyAndZncl(featureZD, callback);
            return;
        } else if ("1".equals(type)) {
            // 共宗，共有房产，多个使用权人，一个不动产单元
        } else if ("2".equals(type)) {
            // 多个不动产单元
        } else if ("3".equals(type)) {
            // 多个使用权人，并且房产共有
        }
        AiRunnable.Ok(callback, null);
    }

    // zd 一个使用权人，一个不动产单元 情况
    private void createBdcdyAndZncl(Feature featureZD, final AiRunnable callback) {
        final FeatureView fv = mapInstance.newFeatureView(featureZD);
        final List<Feature> featuresZRZ = new ArrayList<>();
        fv.queryChildFeature(FeatureHelper.TABLE_NAME_ZRZ, featureZD, featuresZRZ, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 宗地与自然幢生成不动产单元
                ((FeatureViewZD) fv).newBdcdyToZrz(featuresZRZ, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        //1.1  新建权利人
                        //1.2 权利人不动产单元绑定
                        FeatureEditQLR.createNewQlrByBdc(mapInstance, (Feature) t_, callback);
                        return null;
                    }
                });
                return null;
            }
        });
    }

    private void indentyLjzToZrz(final List<Feature> featuresZRZ, AiRunnable callback) {
        new AiForEach<Feature>(featuresZRZ, callback) {
            public void exec() {
                final Feature featureZrz = getValue();
                FeatureViewZRZ fvZrz = (FeatureViewZRZ) mapInstance.newFeatureView(featureZrz);
                final AiForEach<Feature> that = this;
                fvZrz.identyLjz(false, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(final T_ t_, Object... objects) {
                        final List<Feature> featuresLjz = (List<Feature>) t_;
//                        featuresZRZ.get(postion).getAttributes().put("ZCS", getMaxLc(featuresLjz)); // 更新自然幢楼层
                        featureZrz.getAttributes().put("FWJG", getZrzJc(featuresLjz)); // 更新自然幢结构
//                        featuresZRZ.get(postion).getAttributes().put("JGRQ", AiUtil.GetValue(featuresLjz.get(0).getAttributes().get("JGRQ"),"")); // 更新自然幢竣工日期
                        featureZrz.getAttributes().put("JZWJBYT", AiUtil.GetValue(featuresLjz.get(0).getAttributes().get("FWYT1"), "")); // 更新自然幢用途
                        Log.i(TAG, "自然幢识别逻辑幢===" + postion);
                        initAllFeatureHToLjz(featuresLjz, that.getNext());
                        return null;
                    }
                });
            }
        }.start();
    }

    private String getZrzJc(List<Feature> featuresLjz) {
        if (featuresLjz.size() == 1) {
            return FeatureHelper.Get(featuresLjz.get(0), "FWYT1", "");
        }
        String yt = "";
        for (Feature featureLjz : featuresLjz) {
            if (StringUtil.IsEmpty(yt)) {
                yt = FeatureHelper.Get(featureLjz, "FWYT1", "");
            } else if (!yt.equals(FeatureHelper.Get(featureLjz, "FWYT1", ""))) {
                return "4";
            }
        }
        return yt;
    }

    private double getMaxLc(List<Feature> featuresLjz) {
        if (featuresLjz == null && featuresLjz.size() == 0) {
            return 1d;
        }
        double maxLc = 1d;
        for (Feature featureLjz : featuresLjz) {
            double zcs = (int) featureLjz.getAttributes().get("ZCS");
            maxLc = zcs > maxLc ? zcs : maxLc;
        }
        return maxLc;
    }

    private void initAllFeatureHToLjz(final List<Feature> featuresLjz, final AiRunnable callback) {
        new AiForEach<Feature>(featuresLjz, callback) {
            @Override
            public void exec() {
                Log.d(TAG, "逻辑幢快速生成户===" + this.postion);
                final Feature featureLjz = this.getValue();
                final AiForEach<Feature> that = this;
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
                                        final Feature featureH = this.getValue();
                                        final AiForEach<Feature> that_h = this;
                                        Log.i(TAG, "户识别户附属结构===" + featureH.getAttributes().get("ID") + "====" + this.postion);
                                        {
                                            FeatureEditH_FSJG.IdentyH_FSJG_(mapInstance, featureH, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    FeatureEditH.IdentyH_Area(mapInstance, featureH, new AiRunnable() {
                                                        @Override
                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                            fillView(v_content, feature, "YCJZMJ");
                                                            fillView(v_content, feature, "SCJZMJ");
                                                            ToastMessage.Send(activity, "识别户附属结构识别完成！");
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

    private void creatZrzToLjzUnion(List<Feature> featuresLJZ, final List<Feature> featuresZRZ, final AiRunnable callback) {
        if (featuresLJZ.size() > 0) {
            List<Geometry> gs = MapHelper.geometry_get(featuresLJZ);
            Geometry g = GeometryEngine.union(gs);
            if (g instanceof Polygon) {
                ImmutablePartCollection polygonParts = ((Polygon) g).getParts();
                int ps = polygonParts.size();
                if (ps > 0) {
                    for (ImmutablePart segments : polygonParts) {
                        Feature featureZrz = mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ).createFeature();
                        Polygon polygon = new Polygon(new PointCollection(segments.getPoints()));
                        featureZrz.setGeometry(polygon);
                        featuresZRZ.add(featureZrz);
                    }
                    AiRunnable.Ok(callback, featuresZRZ);
                } else {
                    AiRunnable.Ok(callback, featuresZRZ);
                }
            }
        } else {
            AiRunnable.Ok(callback, featuresZRZ);
        }
    }

//    private void share(){
//        shareDialog(activity);
//    }


//    // 不动产调查表
//    String file_dcb_doc;
//    //宗地目录
//    String file_zd_dir;
//    //宗地压缩目录
//    String file_zd_zip;

    // 报表,把现有资料进行归纳 (缺逻辑幢、层、分摊!!!)
    public static void CreateDOCX(final MapInstance mapInstance, final String bdcdyh, final boolean isRelaod, final AiRunnable callback) {
        String file_dcb_doc = GetPath_BDC_doc(mapInstance, bdcdyh);
        if (FileUtils.exsit(file_dcb_doc) && !isRelaod) {
            AiRunnable.Ok(callback, file_dcb_doc);
        } else {
            LoadZD(mapInstance, bdcdyh, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            final Feature f_zd = (Feature) t_;
                            final List<Feature> fs_hjxx = new ArrayList<Feature>();
                            final List<Feature> fs_zrz = new ArrayList<Feature>();
                            final List<Feature> fs_z_fsjg = new ArrayList<Feature>();
                            final List<Feature> fs_z_c = new ArrayList<Feature>();
                            final List<Feature> fs_h = new ArrayList<Feature>();
                            final List<Feature> fs_h_fsjg = new ArrayList<Feature>();
                            final List<Feature> fs_jzd = new ArrayList<Feature>();
                            final List<Feature> fs_jzx = new ArrayList<Feature>();
                            final Map<String, Feature> map_jzx = new HashMap<>();
                            final List<Map<String, Object>> fs_jzqz = new ArrayList<>();

                            LoadAll(mapInstance, bdcdyh, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg, new AiRunnable(callback) {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    FeatureEditC.LoadAll(mapInstance, mapInstance.getOrid(f_zd), fs_z_c,new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            CreateDOCX(mapInstance, bdcdyh, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_z_fsjg,fs_z_c , fs_h, fs_h_fsjg, isRelaod, new AiRunnable(callback) {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    // 数据归集
                                                    OutputData(mapInstance, bdcdyh, f_zd, fs_jzd, fs_jzx, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg);
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
                    }
            );
        }
    }

    public static void CreateDOCXFromFeatureBdc(final MapInstance mapInstance, final Feature featureBdcdy, final AiRunnable callback) {
        String last_orid = FeatureHelper.GetLastOrid(featureBdcdy);
        if (TextUtils.isEmpty(last_orid)) {
            AiRunnable.Ok(callback, featureBdcdy);
            return;
        }
        //TODO 区域验证
        String bdcdy = FeatureHelper.Get(featureBdcdy,FeatureHelper.TABLE_ATTR_BDCDYH,"");
        String imei = AppDevice.getImei();

        if (!DxfHelper.VERSION_UNIVERSAL && !FeatureHelper.GetBASEIMEI().contains(imei)){
            if (FeatureHelper.isBDCDYHValid(bdcdy)){
                if (!bdcdy.startsWith(DxfHelper.AREA_DJZQDM_BASE)){
                    ToastMessage.Send("操作失败，本系统为区域版，本工程没有此权限，请联系当地服务商！");
                    AiRunnable.Error(callback, featureBdcdy);
                    return ;
                }
            }else {
                // 宗地代码有误
                ToastMessage.Send("不动产单元号有误，请检查数据！");
                AiRunnable.Error(callback, featureBdcdy);
                return ;
            }
        }

        if (last_orid.contains(FeatureConstants.ZD_ORID_PREFIX)) {
            new FeatureViewZD().createDOCX(mapInstance, featureBdcdy, true, callback);
        } else if (last_orid.contains(FeatureConstants.ZRZ_ORID_PREFIX)) {
            new FeatureViewZRZ().createDOCX(mapInstance, featureBdcdy, true, callback);
        } else if (last_orid.contains(FeatureConstants.C_ORID_PREFIX)) {
            new FeatureViewC().createDOCX(mapInstance, featureBdcdy, true, callback);
        } else if (last_orid.contains(FeatureConstants.H_ORID_PREFIX)) {
            Log.i(TAG, "H_ORID_PREFIX" + last_orid);
            new FeatureViewH().createDOCX(mapInstance, featureBdcdy, true, callback);
        } else {
            AiRunnable.Ok(callback, featureBdcdy);
        }
    }

    // 报表,把现有资料进行归纳
    public static void CreateDOCX(final MapInstance mapInstance, final String bdcdyh, final Feature f_zd,
                                  final List<Feature> fs_jzd,
                                  final List<Feature> fs_jzx,
                                  final Map<String, Feature> map_jzx,
                                  final List<Map<String, Object>> fs_jzqz,
                                  final List<Feature> fs_zrz,
                                  final List<Feature> fs_z_fsjg,
                                  final List<Feature> fs_c, final List<Feature> fs_h,
                                  final List<Feature> fs_h_fsjg, boolean isRelaod, final AiRunnable callback) {
        String file_dcb_doc = GetPath_BDC_doc(mapInstance, bdcdyh);
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
                        Put_data_sys(map_);
                        //  设置宗地参数
                        Put_data_zd(mapInstance, map_, bdcdyh, f_zd);
                        // 界址签字
                        Put_data_jzqz(map_, fs_jzd, fs_jzqz);
                        // 界址点
                        Put_data_jzdx(mapInstance, map_, zddm, fs_jzd, fs_jzx, map_jzx);

                        // 设置界址线
                        Put_data_jzx(mapInstance, map_, fs_jzx);
                        // 自然幢
                        Put_data_zrz(mapInstance, map_, bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h,fs_c, new ArrayList<Feature>());
                        // 在全局放所有户
                        //  Put_data_hs(mapInstance, map_, fs_h);
                        // 在全局放一个户
                        Put_data_h(mapInstance, map_, fs_h);
                        // 在全局放一个幢
                        Put_data_zrz(mapInstance, map_, fs_zrz);
                        // 在全局放一所以的户
                        // 宗地草图
                        Put_data_zdct(mapInstance, map_, f_zd);
                        // 附件材料
                        Put_data_fjcl(mapInstance, map_, f_zd);

                        Put_data_ljz(mapInstance,map_,f_zd);

                        final String templet = FileUtils.getAppDirAndMK(GetPath_Templet()) + "不动产权籍调查表.docx";
                        final String file_dcb_doc = GetPath_BDC_doc(mapInstance, bdcdyh);
                        String file_zd_zip = GetPath_ZD_zip(mapInstance, f_zd);
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

    // 查询所有自然幢，识别户和幢附属
    public static void LaodAlLBDC_CreateDOCX(final MapInstance mapInstance, final boolean isReload, final AiRunnable callback) {
        final List<Feature> fs = new ArrayList<>();
        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZD), "", -1, fs, new AiRunnable(callback) {
            // 递归调用，直到全部完成
            void create(final List<String> ids, final int index, final AiRunnable create_callback) {
                if (index < ids.size()) {
                    FeatureEditBDC.CreateDOCX(mapInstance, ids.get(index), isReload, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            create(ids, index + 1, create_callback);
                            return null;
                        }
                    });
                } else {
                    AiRunnable.Ok(create_callback, index);
                }
            }

            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                List<String> bdcdyhs = new ArrayList<String>();
                for (Feature f : fs) {
                    String bdcdyh = FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_BDCDYH, "");
                    String zddm = FeatureHelper.Get(f, FeatureHelper.TABLE_ATTR_ZDDM, "");
                    // 保证宗地代码和不动产单元号有效
                    if (FeatureHelper.isBDCDYHValid(bdcdyh) && !bdcdyhs.contains(bdcdyh) && FeatureHelper.isZDDMHValid(zddm)) {
                        bdcdyhs.add(bdcdyh);
                    }
                }
                create(bdcdyhs, 0, new AiRunnable(callback) {
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

    // 查询所有自然幢，识别户和幢附属
    public static void LaodALLBDC_CreateDOCX(final MapInstance mapInstance, final boolean isReload, final AiRunnable callback) {
        FeatureTable table = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX);
        final List<Feature> fs = new ArrayList<>();
        MapHelper.Query(table, "", -1, fs, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                new AiForEach<Feature>(fs, callback) {
                    @Override
                    public void exec() {
                        final AiForEach ai = this ;
                        CreateDOCXFromFeatureBdc(mapInstance, getValue(), new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                AiRunnable.Ok(getNext(),t_,objects);
                                return null;
                            }

                            @Override
                            public <T_> T_ error(T_ t_, Object... objects) {
                                AiRunnable.Error(callback,t_,objects);
                                return null;
                            }
                        });
                    }
                }.start();

                return null;
            }
        });

    }

    // 报表,把现有资料进行归纳
    public static void Pack(final MapInstance mapInstance, final String bdcdyh, final boolean isRelaod, final AiRunnable callback) {
        String funcdesc = "将该宗地相关的矢量数据、照片、表格进行打包，导出成一个通用文件(.ovitmap)，便于传输或其他系统使用。";
        License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                LoadZD(mapInstance, bdcdyh, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        final Feature f_zd = (Feature) t_;
                        final String file_zd_dir = GetPath_ZD_dir(mapInstance, f_zd);
                        final String file_zd_zip = GetPath_ZD_zip(mapInstance, f_zd);
                        if (FileUtils.exsit(file_zd_zip) && !isRelaod) {
                            AiRunnable.Ok(callback, file_zd_zip);
                        } else {
                            final List<Feature> fs_zrz = new ArrayList<Feature>();
                            final List<Feature> fs_z_fsjg = new ArrayList<Feature>();
                            final List<Feature> fs_z_c = new ArrayList<Feature>();
                            final List<Feature> fs_h = new ArrayList<Feature>();
                            final List<Feature> fs_h_fsjg = new ArrayList<Feature>();
                            final List<Feature> fs_jzd = new ArrayList<Feature>();
                            final List<Feature> fs_jzx = new ArrayList<Feature>();
                            final Map<String, Feature> map_jzx = new LinkedHashMap<>();
                            final List<Map<String, Object>> fs_jzqz = new ArrayList<>();

                            LoadAll(mapInstance, bdcdyh, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    FeatureEditC.LoadAll(mapInstance, mapInstance.getOrid(f_zd), fs_z_c, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            CreateDOCX(mapInstance, bdcdyh, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_z_fsjg,fs_z_c , fs_h, fs_h_fsjg, isRelaod,
                                                    new AiRunnable() {
                                                        @Override
                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                            try {
                                                                // 数据归集
                                                                OutputData(mapInstance, bdcdyh, f_zd, fs_jzd, fs_jzx, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg);
                                                                Zip.zipSubFiles(file_zd_dir, file_zd_zip);
                                                                AiRunnable.Ok(callback, file_zd_zip);
                                                            } catch (Exception es) {
                                                                ToastMessage.Send("打包失败！");
                                                            }
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
                        return null;
                    }
                });
                return null;
            }
        });


    }


    //region  设置打印参数

    // 设置系统常数（年月日）
    public static void Put_data_sys(Map<String, Object> map_) {
        Map<String, Object> map_sys = new LinkedHashMap<>();
        Calendar c = Calendar.getInstance();
        map_sys.put("Y", c.get(Calendar.YEAR));
        map_sys.put("M", c.get(Calendar.MONTH) + 1);
        map_sys.put("D", c.get(Calendar.DAY_OF_MONTH));
        String date = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
        String date_ = c.get(Calendar.YEAR) + "年" + (c.get(Calendar.MONTH) + 1) + "月" + c.get(Calendar.DAY_OF_MONTH) + "日";
        map_sys.put("DATE", date);
        map_.put("SYS.FFDATE", date_);
        GetReplecData(map_, "SYS", map_sys);
    }

    // 设置不动产单元的内容
    public static void Put_data_bdcdy(MapInstance mapInstance, Map<String, Object> map_, Feature featureBdc) {
        GetReplecData(mapInstance.activity, map_, featureBdc, "", "ZJZL", "XB", "");
    }

    public static void Put_data_hjxx(com.ovit.app.map.model.MapInstance mapInstance, Map<String, Object> map_, List<Feature> fs_hjxx) {
        List<Feature> mfs_hjxx = new ArrayList<>(fs_hjxx);
        map_.put("ZD.HJXX", fs_hjxx.size()+"");

        for (int i = mfs_hjxx.size(); i < 4; i++) {
            mfs_hjxx.add(GetTable(mapInstance, FeatureHelper.TABLE_NAME_HJXX).createFeature());
        }
        GetReplecData(mapInstance.activity, map_, "list.hjxx", mfs_hjxx, "");

        List<Map<String,String>> mHjxx = (List<Map<String, String>>) map_.get("list.hjxx");
        for (int i = 0; i < mHjxx.size(); i++) {
           if (i<fs_hjxx.size()){
               mHjxx.get(i).put("HJXX.HB","农业户口");
               mHjxx.get(i).put("HJXX.XH",i+2 + "");
           }else {
               mHjxx.get(i).put("HJXX.HB","");
               mHjxx.get(i).put("ZD.QLRTXDZ","");
               mHjxx.get(i).put("HJXX.XH","");
               mHjxx.get(i).put("HJXX.XB","");
           }
        }
    }

    public static void Put_data_hjxx(com.ovit.app.map.model.MapInstance mapInstance, Map<String, Object> map_, List<Feature> fs_hjxx,int size) {
        List<Feature> mfs_hjxx = new ArrayList<>(fs_hjxx);
        for (int i = mfs_hjxx.size(); i < 6; i++) {
            mfs_hjxx.add(GetTable(mapInstance, FeatureHelper.TABLE_NAME_HJXX).createFeature());
        }



        {

            List<Map<String, Object>> maps_p = new ArrayList<>();
            Map<String, Object> map_p = new LinkedHashMap<>();
            int i = 0;
            String ch1 = "";
            String gcmjhz = "";
            double jzmj = 0;

            for (int i1 = 0; i1 < fs_hjxx.size(); i1++) {
                Feature hjxx=fs_hjxx.get(i1);
                String xm = FeatureHelper.Get(hjxx,"XM","");
                String yhzgx = FeatureHelper.Get(hjxx,"YHZGX","");
                String zjh = FeatureHelper.Get(hjxx,"ZJH","");

                int j = i % size;
                map_p.put("HJXX.XM" + (j + 1), xm);
                map_p.put("HJXX.YHZGX" + (j + 1),yhzgx);
                map_p.put("HJXX.ZJH" + (j + 1), zjh);
                // 当每页最后一个，或是最后一个
                if (j == size - 1 || i == fs_hjxx.size() - 1) {
//                    map_p.put("CP.FZH", StringUtil.substr_last(zrzh, 5));
//                    map_p.put("CP.FHH", StringUtil.substr_last(bdcdyh, 4));
//                    map_p.put("CP.FCHHZ", ch1.equals(fct_ch) ? fct_ch : (ch1.substring(0,1) + "-" + fct_ch.substring(fct_ch.length()-1)));
//                    map_p.put("CP.FJZMJHZ", AiUtil.GetValue(jzmj, "", AiUtil.F_FLOAT2));
//
//                    map_p.put("CP.FGCMJHZ",gcmjhz.substring(0,gcmjhz.length()-1));
                    if (j < size - 1) {
                        for (int n = 1; n <= size - j; n++) {
                            map_p.put("HJXX.XM" + (j + n + 1), "");
                            map_p.put("HJXX.YHZGX" + (j + n + 1), "");
                            map_p.put("HJXX.ZJH" + (j + n + 1), "");
                        }
                    }
                    maps_p.add(map_p);
                    map_p = new LinkedHashMap<>();
                    ch1 = "";
                    jzmj = 0;
                    gcmjhz="";
                }
                i++;
            }
            // 为幢设置层
            map_.put("list."+size+"hjxx", maps_p);
        }




    }


    // 设置宗地的内容
    public static void Put_data_zd(MapInstance mapInstance, Map<String, Object> map_, String bdcdyh, Feature f_zd) {
        Map<String, Object> map = new LinkedHashMap<>();
        // 优先设置，避免key冲突
        map.put("ZD.ZLZ", "");
        map.put("ZD.ZLRQ", "");
        map.put("ZD.XZQDM", StringUtil.substr_last(bdcdyh, 6, 22));

        map.put("ZD.DJQDMFF", StringUtil.substr_last(bdcdyh, 3, 19));
        map.put("ZD.DJQDM", StringUtil.substr_last(bdcdyh, 9, 19));

        map.put("ZD.DJZQDMFF", StringUtil.substr_last(bdcdyh, 3, 16));
        map.put("ZD.DJZQDM", StringUtil.substr_last(bdcdyh, 12, 16));

        map.put("ZD.DZWDM", StringUtil.substr_last(bdcdyh, 9));
        map.put("ZD.ZDDMFF", StringUtil.substr_last(bdcdyh, 7, 9));

        //拼接宗地代码（宗地+附属宗地）
        map.put("ZD.ZDDMPJ",f_zd);


        map.put("ZD.HZR", GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZR", ""));
        map.put("ZD.SHR", GsonUtil.GetValue(mapInstance.aiMap.JsonData, "SHR", ""));
        map.put("ZD.HZDW", GsonUtil.GetValue(mapInstance.aiMap.JsonData, "HZDW", ""));

        // 设置宗地参数
        GetReplecData(mapInstance.activity, map, f_zd, "", "QLRLX", "QLRZJZL:zjzl", "QLLX", "QLXZ", "FZRZJLX:zjzl", "DLRZJLX:zjzl", "PZYT:tdyt", "SJYT:tdyt");
        GetReplecData(map_, "", map);
        // 设置出表时间
        String qzrq = (String) map_.get("ZD.QZRQ");
        if (!TextUtils.isEmpty(qzrq) && qzrq.length() == 10) {
            String[] qzrq_s = qzrq.split("-");
            map_.put("ZD.Y", qzrq_s[0]);
            map_.put("ZD.M", qzrq_s[1]);
            map_.put("ZD.D", qzrq_s[2]);
            return;
        }
        map_.put("ZD.Y", map_.get("SYS.Y"));
        map_.put("ZD.M", map_.get("SYS.M"));
        map_.put("ZD.D", map_.get("SYS.D"));
        map_.put("ZD.FFDJSJ", map_.get("ZD.PZSJ"));
        map_.put("ZD.FFDJMJ", map_.get("ZD.DJMJ"));
        map_.put("ZD.FFDJJZMJ ", map_.get("ZD.DJJZMJ"));
        map_.put("ZD.FFDJZDMJ", map_.get("ZD.DJZDMJ"));

    }

    public static void Put_data_jzqz(Map<String, Object> map_, List<Feature> f_jzd, List<Map<String, Object>> fs_jzqz) {

        // 界址签字数，只留一条
        List<Map<String, Object>> f_jzqzs = new ArrayList<Map<String, Object>>();
        f_jzqzs.addAll(fs_jzqz);
        if (f_jzqzs.size() < 1) {
            Map<String, Object> f_jzqz_def = new LinkedHashMap<>();
            f_jzqz_def.put("JZQZ.JZXQDH", "");
            f_jzqz_def.put("JZQZ.JZXZJDH", "");
            f_jzqz_def.put("JZQZ.JZXZDH", "");
            f_jzqz_def.put("JZQZ.JZQZBRQ", map_.get("SYS.DATE"));
            f_jzqz_def.put("JZQZ.LZDZJR", " ");
            f_jzqz_def.put("img.lzzjqz","");
            f_jzqz_def.put("img.qlr","");
            f_jzqzs.add(f_jzqz_def);

        }
        map_.put("list.jzqz", f_jzqzs);

    }

    public static void Put_data_jzdx(com.ovit.app.map.model.MapInstance mapInstance, Map<String, Object> map_, String zddm, List<Feature> f_jzds, List<Feature> f_jzxs, Map<String, Feature> map_jzx) {
        List<Feature> fs_jzd = new ArrayList<>(f_jzds);
        map_.put("ZD.JZDS", f_jzds.size());
        if (fs_jzd.size() > 0) {
            // 界址点首位相连
            Feature f_jzd_ = GetTable(mapInstance, "JZD").createFeature();
            f_jzd_.getAttributes().put("JZDH", fs_jzd.get(0).getAttributes().get("JZDH"));
            f_jzd_.getAttributes().put("JBLX", fs_jzd.get(0).getAttributes().get("JBLX"));
            f_jzd_.getAttributes().put("JZJG", fs_jzd.get(0).getAttributes().get("JZJG"));
            fs_jzd.add(f_jzd_);
        }

        // 界址点最少14个，通山要求两页
        int jzdSize = 14;
        if (DxfHelper.TYPE == DxfHelper.TYPE_TONGSHAN) {
            jzdSize = 30;//单行倍距
        }
        if (fs_jzd.size() < jzdSize) {
            for (int i = fs_jzd.size(); i < jzdSize; i++) {
                fs_jzd.add(GetTable(mapInstance, "JZD").createFeature());
            }
        }

        // 设置界址点数据，添加在指定值域打勾
        List<Map<String, Object>> maps_jzd = new ArrayList<>();
        int i = 0;
        for (Feature f_jzd : fs_jzd) {
            int j = (i + 1 < f_jzds.size()) ? i + 1 : 0;
            Feature f_jzd2 = fs_jzd.get(j);
            Feature f_jzx = FeatureEditJZX.Get(map_jzx, f_jzd, f_jzd2);
            String jzx_zdzhdm = FeatureHelper.Get(f_jzx, "ZDZHDM", "");

            Map<String, Object> v = GetReplecData(mapInstance.activity, new LinkedHashMap<String, Object>(), f_jzd, "", "JBLX", "JZXLB", "JZXWZ");
            v.put("JZX.JZXSM", ""); // 防止  JZX.JZXSM  为空
            v = GetReplecData(mapInstance.activity, v, f_jzx, "", "JBLX", "JZXLB", "JZXWZ");

            if (!TextUtils.isEmpty(FeatureHelper.Get(f_jzd, "JZDH", ""))) {
                if (i == f_jzds.size()) {
                    v.put("JZD.JZDH", "J1");
                    v.put("JZD.SXH", "1");
                    v.put("JZD.FFXZBZ", "");
                    v.put("JZD.FFYZBZ", "");
                } else {
                    v.put("JZD.JZDH", "J" + (i + 1));
                    v.put("JZD.SXH", (i + 1));
                }
            }

            String jblx = AiUtil.GetValue(v.get("JZD.JBLXFF"), "");
            v.put("JZD.JBLXFFGD", jblx.equals("钢钉") ? "√" : "");
            v.put("JZD.JBLXFFSNZ", jblx.equals("水泥桩") ? "√" : "");
            v.put("JZD.JBLXFFPT", jblx.equals("喷涂") ? "√" : "");
            v.put("JZD.JBLXFFQJ", jblx.equals("墙角") ? "√" : "");
            v.put("JZD.JBLXFFWPT", jblx.equals("未喷涂") ? "√" : "");
            v.put("JZD.JBLXFFQT", Arrays.asList(new String[]{"钢钉", "水泥桩", "喷涂", "未喷涂", "墙角"}).contains(jblx) ? "" : (StringUtil.IsNotEmpty(jblx) ? "√" : ""));
            MoveToEnd(v, "JZD.JBLXFF", "JZD.JBLX");

            String jzxlb = AiUtil.GetValue(v.get("JZD.JZXLBFF"), v.get("JZD.JZXLBFF") + "");
            v.put("JZD.JZXLBFFQB", jzxlb.equals("墙壁") ? "√" : "");
            v.put("JZD.JZXLBFFWQ", jzxlb.equals("围墙") ? "√" : "");
            v.put("JZD.JZXLBFFWL", jzxlb.equals("围栏") ? "√" : "");
            v.put("JZD.JZXLBFFZL", jzxlb.equals("栅栏") ? "√" : "");
            v.put("JZD.JZXLBFFTSW", jzxlb.equals("铁丝网") ? "√" : "");
            v.put("JZD.JZXLBFFDSX", jzxlb.equals("滴水线") ? "√" : "");
            v.put("JZD.JZXLBFFLYX", jzxlb.equals("路涯线") ? "√" : "");
            v.put("JZD.JZXLBFFLX", jzxlb.equals("两点连线") ? "√" : "");
            v.put("JZD.JZXLBFFZXX", jzxlb.equals("中心线") ? "√" : "");
            v.put("JZD.JZXLBFFFY", jzxlb.equals("飞檐") ? "√" : "");
            v.put("JZD.JZXLBFFYZ", jzxlb.equals("阳台") ? "√" : "");
            v.put("JZD.JZXLBFFPL", jzxlb.equals("飘楼") ? "√" : "");
            v.put("JZD.JZXLBFFDL", jzxlb.equals("道路") ? "√" : "");
            v.put("JZD.JZXLBFFGQ", jzxlb.equals("沟渠") ? "√" : "");
            v.put("JZD.JZXLBFFTG", jzxlb.equals("田埂") ? "√" : "");
            v.put("JZD.JZXLBFFJJ", jzxlb.equals("基脚") ? "√" : "");
            v.put("JZD.JZXLBFFBK", jzxlb.equals("堡坎") ? "√" : "");
            v.put("JZD.JZXLBFFTK", jzxlb.equals("土坎") ? "√" : "");
            v.put("JZD.JZXLBFFYK", jzxlb.equals("岩坎") ? "√" : "");
            //   v.put("JZD.JZXLBFFQT", jzxlb.equals("其他") ? "√" : "");
            v.put("JZD.JZXLBFFQT", Arrays.asList(new String[]{"墙壁", "围墙", "围栏", "铁丝网", "滴水线", "路涯线", "两点连线", "中心线", "飞檐", "阳台", "飘楼", "道路", "沟渠", "田埂", "基脚", "堡坎", "土坎", "岩坎"}).contains(jzxlb) ? "" : (StringUtil.IsNotEmpty(jzxlb) ? "√" : ""));
            MoveToEnd(v, "JZD.JZXLBFF", "JZD.JZXLB");

            String jzxwz = AiUtil.GetValue(v.get("JZD.JZXWZFF"), v.get("JZD.JZXWZFF") + "");

            v.put("JZD.JZXWZFFN", jzxwz.equals("内") ? "√" : "");
            v.put("JZD.JZXWZFFZ", jzxwz.equals("中") ? "√" : "");
            v.put("JZD.JZXWZFFW", jzxwz.equals("外") ? "√" : "");
            double xzbz = FeatureHelper.Get(f_jzd,"XZBZ",0.00);
            double yzbz = FeatureHelper.Get(f_jzd,"YZBZ",0.00);
//            v.put("JZD.FFXZBZ", String.format("%.3f", xzbz));
//            v.put("JZD.FFYZBZ", String.format("%.3f",yzbz));
            v.put("JZD.FFXZBZ", xzbz==0 ? "" : String.format("%.3f",xzbz));
            v.put("JZD.FFYZBZ", yzbz==0 ? "" : String.format("%.3f",yzbz));
            //v.put("JZD.FFYZBZ", f_jzd.getAttributes().get("YZBZ") == null ? "" : f_jzd.getAttributes().get("YZBZ") + "");
            if (f_jzd.getGeometry() == null) {
                v.put("JZX.JZXCD", "");
            }
            MoveToEnd(v, "JZD.JZXWZFF", "JZD.JZXWZ");
            maps_jzd.add(v);
            i++;

        }
        List<Map<String, Object>> maps_jzd_ = new ArrayList<>();
        maps_jzd_.addAll(maps_jzd);
        maps_jzd_.remove(f_jzds.size());

        map_.put("list.jzd", maps_jzd);
        map_.put("list.zbjzd", maps_jzd_);
    }

    public static void Put_data_jzx(com.ovit.app.map.model.MapInstance mapInstance, Map<String, Object> map_, List<Feature> fs_jzx) {
        List<Feature> f_jzxs = new ArrayList<>(fs_jzx);
        // 界址点最少20个
        if (f_jzxs.size() < 20) {
            for (int i = f_jzxs.size(); i < 20; i++) {
                f_jzxs.add(GetTable(mapInstance, "JZX").createFeature());
            }
        }
        // 设置界址线
        GetReplecData(mapInstance.activity, map_, "list.jzx", f_jzxs, "");
        map_.put("ZD.JZXS", fs_jzx.size());

    }

    public static void Put_changsha_jzd(Map<String, Object> map_, List<Feature> f_jzds, Map<String, Feature> map_jzx) {

        List<Map<String, Object>> maps_jzd = new ArrayList<>();
        Map<String, Object> map_jzd = new LinkedHashMap<>();
        String jzlx = AiUtil.GetValue(map_.get("ZD.JXLX"), "J");

        for (int i = 0; i < f_jzds.size(); i++) {

            int index = (i + 1) % 3;
            Feature f_jzd = f_jzds.get(i);
            int j = i + 1 >= f_jzds.size() ? 0 : i + 1;

            Feature f_next_jzd = f_jzds.get(j);
            Feature f_jzx = FeatureEditJZX.Get(map_jzx, f_jzd, f_next_jzd);
            String jzxcd = AiUtil.Scale(FeatureHelper.Get(f_jzx, "JZXCD", 0d), 2, 0);

            if (index == 0) {//第一行
                map_jzd.put("CSD.DH" + 3, jzlx + (i + 1));
                map_jzd.put("CSX.CD" + 3, jzxcd);
                map_jzd.put("CSD.DH" + 4, jzlx + (j + 1));
                maps_jzd.add(map_jzd);
                map_jzd = null;
                map_jzd = new LinkedHashMap<>();

            } else {
                if ((i + 1) == f_jzds.size()) {
                    map_jzd.put("CSD.DH" + index, jzlx + (i + 1));
                    map_jzd.put("CSX.CD" + index, jzxcd);
                    map_jzd.put("CSD.DH" + (index + 1), jzlx + (j + 1));

                    for (int n = index + 1; n < 4; n++) {
                        map_jzd.put("CSX.CD" + n, " ");
                        map_jzd.put("CSD.DH" + (n + 1), " ");
                    }
                    maps_jzd.add(map_jzd);
                } else {
                    map_jzd.put("CSD.DH" + index, jzlx + (i + 1));
                    map_jzd.put("CSX.CD" + index, jzxcd);
                }
            }
        }

        map_.put("list.csjzd", maps_jzd);

    }
    public static void Put_changsha_jzd_wangcheng(Map<String, Object> map_, List<Feature> f_jzds, Map<String, Feature> map_jzx) {
        List<Map<String, Object>> maps_jzd = new ArrayList<>();
        for (int i = 0; i < f_jzds.size(); i = i + 2) {
            Map<String, Object> map_jzd = new LinkedHashMap<>();
            String jzlx = AiUtil.GetValue(map_.get("ZD.JXLX"), "J");
            int j = i + 1 < f_jzds.size() ? i + 1 : 0;
            int k = i + 2 < f_jzds.size() ? i + 2 : 0;
            Feature f_jzd = f_jzds.get(i);
            Feature f_jzd1 = f_jzds.get(j);
            Feature f_jzd2 = f_jzds.get(k);
            Feature f_jzx = FeatureEditJZX.Get(map_jzx, f_jzd, f_jzd1);
            Feature f_jzx2 = FeatureEditJZX.Get(map_jzx, f_jzd1, f_jzd2);
            map_jzd.put("JZD.QDH", jzlx + (i + 1));
            map_jzd.put("JZD.ZDH", jzlx + (j + 1));
            if(j!=0){
                map_jzd.put("JZD.ZJDH",jzlx + (j + 1));
                map_jzd.put("JZD.END", jzlx + (k + 1));
            } else {
                map_jzd.put("JZD.ZJDH","");
                map_jzd.put("JZD.END", "");
            }
            //要求把m去掉
            //map_jzd.put("JZX.CDF", AiUtil.Scale(FeatureHelper.Get(f_jzx, "JZXCD", 0d), 2, 0) + "m" );
            map_jzd.put("JZX.CDF", AiUtil.Scale(FeatureHelper.Get(f_jzx, "JZXCD", 0d), 2, 0));
            if(FeatureHelper.Get(f_jzx2, "JZXCD", "").isEmpty()){
                map_jzd.put("JZX.CDT","");
            } else {
//                map_jzd.put("JZX.CDT", AiUtil.Scale(FeatureHelper.Get(f_jzx2, "JZXCD", 0d), 2, 0) + "m" );
                map_jzd.put("JZX.CDT", AiUtil.Scale(FeatureHelper.Get(f_jzx2, "JZXCD", 0d), 2, 0));
            }
            maps_jzd.add(map_jzd);
        }
        map_.put("list.csjzd", maps_jzd);
    }


    public static void Put_data_zdct(MapInstance mapInstance, Map<String, Object> map_, Feature f_zd) {
        // 设置宗地草图
        String image_zdct = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/宗地草图/") + "宗地草图.jpg";
        map_.put("img.zdct", image_zdct);
        String image_fct = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/房产图/") + "房产图.jpg";
        map_.put("img.fct", image_fct);


        //云梦身份证照片和户口薄照片拍到不动产单元下面
        //路径   身份证 data/全国/资料库/运行时/权利人信息/QLRXX/附件材料/证件号
        //路径   户口薄 data/全国/资料库/运行时/权利人信息/QLRXX/附件材料/户口薄
        String signDirPath = mapInstance.getpath_root() + "运行时/权利人信息/[QLRXX]/";  //路径
        String image_sfzzp = FileUtils.getAppDirAndMK(signDirPath + "附件材料/") + "证件号";  //路径
        String image_sfzzp1 = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/权利人信息/QLRXX") + "证件号";

        File file_zjh = new File(image_sfzzp);

        File[] files_zjh = file_zjh.listFiles();
//        if (files_zjh != null) {
//            for (int i = 0; i < files_zjh.length; i++) {
//                FileUtils.rename(files_zjh[i].getAbsolutePath(), files_zjh + "/证件号" + (i + 1) + ".temp");
//            }
//            files_zjh = file_zjh.listFiles();
//            for (int i = 0; i < files_zjh.length; i++) {
//                FileUtils.rename(files_zjh[i].getAbsolutePath(), files_zjh + "/证件号" + (i + 1) + ".jpg");
//            }
//        }
        List<String> sfzzp = new ArrayList<>();
        sfzzp.addAll(FileUtils.getAllImgePath(image_sfzzp));
        if (sfzzp.size()>0) {
            map_.put("img.sfzzp",sfzzp.get(0));
        }
        map_.put("image.sfzzp", image_sfzzp);


//        String image_hkbzp = FileUtils.getAppDirAndMK(signDirPath + "附件材料/") + "户口薄";
//        File file_hkb = new File(image_hkbzp);
//        File[] files_hkb = file_hkb.listFiles();
//        if (files_hkb != null) {
//            for (int i = 0; i < files_hkb.length; i++) {
//                FileUtils.rename(files_hkb[i].getAbsolutePath(), image_hkbzp + "/2020" + (i + 1) + ".temp");
//            }
//
//            files_hkb = file_hkb.listFiles();
//            for (int i = 0; i < files_hkb.length; i++) {
//                FileUtils.rename(files_hkb[i].getAbsolutePath(), image_hkbzp + "/2020" + (i + 1) + ".jpg");
//            }
//        }
//        List<String> hkbzp= new ArrayList<>();
//        hkbzp.addAll(FileUtils.getAllImgePath(image_hkbzp));
//        if (hkbzp.size()> 0) {
//            map_.put("image.hkbzp",image_hkbzp);
//        }
//        map_.put("image.hkbzp", image_hkbzp);


    }

    public static void Put_data_fjcl(MapInstance mapInstance, Map<String, Object> map_, Feature f_zd) {
        //设置电子签章照片
        String signDirPath = mapInstance.getpath_root() + "资料库/电子签章/";

        String img_dzqz = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "/附件材料/电子签章/" + "权利人签章举证/") + "权利人电子签章.jpg";
        String image_fjcl = FileUtils.getAppDirAndMK(signDirPath);
        //力智
        String imgClrqm = FileUtils.getAppDirAndMK(signDirPath + "/测量人签章举证/") + "测量人电子签章.jpg";
        String imgDcrqm = FileUtils.getAppDirAndMK(signDirPath + "/调查人签章举证/") + "调查人电子签章.jpg";
        String imgZz = FileUtils.getAppDirAndMK(signDirPath + "/组长签章举证/") + "组长电子签章.jpg";
        String imgshr = FileUtils.getAppDirAndMK(signDirPath + "/审核人签章举证/") + "审核人电子签章.jpg";
        String imgQlr = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "/附件材料/电子签章/权利人签章举证/") +"权利人电子签章.jpg";

        map_.put("img.clrqm", imgClrqm);
        map_.put("img.dcrqm", imgDcrqm);
        map_.put("img.zzqm", imgZz);
        map_.put("img.qlrqm", imgQlr);
        map_.put("img.shrqm", imgshr);

        List<Map<String, Object>> maps_dzqz = new ArrayList<>();
        File file = new File(FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/电子签章/指界人签章举证/"));
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            String zjPath = tempList[i].toString();
            if (zjPath.endsWith(".jpg") ) {
                Map<String, Object> map_dzqz = new LinkedHashMap<>();
                map_dzqz.put("img.slqz", zjPath);
                maps_dzqz.add(map_dzqz);
            }
        }
        map_.put("list.dzqz", maps_dzqz);
        map_.put("img.dzqz", img_dzqz);
        map_.put("img.fjcl", image_fjcl);


        // 身份证
        String image_sfzmcl = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/") + "权利人证件号";
        File file_zjh = new File(image_sfzmcl);

        File[] files_zjh = file_zjh.listFiles();
        if (DxfHelper.TYPE == DxfHelper.TYPE_TIANMEN && files_zjh != null) {
            for (int i = 0; i < files_zjh.length; i++) {
                FileUtils.rename(files_zjh[i].getAbsolutePath(), files_zjh + "/身份证" + (i + 1) + ".temp");
            }

            files_zjh = file_zjh.listFiles();
            for (int i = 0; i < files_zjh.length; i++) {
                FileUtils.rename(files_zjh[i].getAbsolutePath(), files_zjh + "/身份证" + (i + 1) + ".jpg");
            }
        }
        List<String> sfz = new ArrayList<>();
        sfz.addAll(FileUtils.getAllImgePath(image_sfzmcl));
        if (sfz.size() > 0) {
            map_.put("img.sfzz", sfz.get(0));
        }
        map_.put("img.sfzmcl", image_sfzmcl);

        String image_qzjz = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/") + "权利人签章举证";
        map_.put("img.qlrqzjz", image_qzjz);
        // 户口本
        String image_hkb = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/") + "户口簿";
        File file_hkb = new File(image_hkb);

        File[] files = file_hkb.listFiles();
        if (DxfHelper.TYPE == DxfHelper.TYPE_TIANMEN && files != null) {
            for (int i = 0; i < files.length; i++) {
                FileUtils.rename(files[i].getAbsolutePath(), image_hkb + "/户口簿" + (i + 1) + ".temp");
            }

            files = file_hkb.listFiles();
            for (int i = 0; i < files.length; i++) {
                FileUtils.rename(files[i].getAbsolutePath(), image_hkb + "/户口簿" + (i + 1) + ".jpg");
            }
        }
        map_.put("img.hkb", image_hkb);
        Put_data_hkbs(map_, image_hkb, 2);
        Put_data_hkbs(map_, image_hkb, 4);

        String sfzmcl_s = "1、申请书" + "SYS.ENTER" + "2、调查表" + "SYS.ENTER";
        if (FileUtils.getFileCount(image_sfzmcl) > 0 && FileUtils.getFileCount(image_hkb) > 0) {
            sfzmcl_s += "3、户口簿" + "SYS.ENTER" + "4、身份证";
        } else if (FileUtils.getFileCount(image_sfzmcl) > 0) {
            sfzmcl_s += "3、身份证";
        } else if (FileUtils.getFileCount(image_hkb) > 0) {
            sfzmcl_s += "3、户口簿";
        }
        map_.put("ZD.FJQDFF", sfzmcl_s);
        String image_qslyzm = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/") + "土地权属来源证明材料";
        map_.put("img.qslyzm", image_qslyzm);
//        String qjdcjs=  "经现场核实，申请书上有关栏目与实地情况一致。本宗地权利人" + map_.get("ZD.QLRXM") + " 按规定到现场指界，界址清楚、无争议，并履行了签章手续。\n" +
//                "     调查人员根据现状在实地设置了" + map_.get("ZD.JZDS") + "个界址点，喷涂了" + map_.get("ZD.JZDS") + "个界址点，采用钢尺丈量了" + map_.get("ZD.JZDS") + "条界址边长，可进行细部测量。";
//        String djdcjs= "经检查"
//                + map_.get("ZD.JZDS") +"个界标保存完好，采用瑞得RTS-822R型全站仪。"
//                + "用极坐标法采集了"+ map_.get("ZD.JZDS") +"个界址点，界址点采用边长交会法获得。";
//        if(DxfHelper.TYPE==DxfHelper.TYPE_TIANMEN){
//            qjdcjs="权属调查记事：该宗地系于"
//                    +map_.get("ZRZ.SJGRQ") +"年开始占用，现未收集到该宗地任何权属来源资料。本宗地指界人"
//                    + map_.get("ZD.QLRXM") +""
//                    + "按规定到现场指界，界址清楚、无争议，并履行了签章手续。调查人根据现状实地设置了"
//                    + map_.get("ZD.JZDS") + "个界址点，"
//                    +"喷涂了"+map_.get("ZD.JZDS") + "个界址点。采用钢尺丈量了"
//                    + map_.get("ZD.JZDS") + "条界址边长，可进行细部测量。面积为"
//                    + map_.get("ZD.ZDMJ") + "㎡。\n" ;
//            map_.put("DCJS.QSDCJS", qjdcjs);
//        } else if(DxfHelper.TYPE==DxfHelper.TYPE_BADONG){
//            qjdcjs=  "经现场核实，申请书上有关栏目与实地情况一致。本宗地权利人" + map_.get("ZD.QLRXM") + " 按规定到现场指界，界址清楚、无争议。" +
//                    "调查人员根据现状在实地设置了" + map_.get("ZD.JZDS") + "个界址点，采用钢尺或测距仪丈量了相邻界址边长，经检查可进行细部测量。";
//            String jxlx = map_.get("ZD.JXLX").toString().contains("J")?"解析法":"图解法";
//            djdcjs= "本宗地采用" + jxlx + "测量，经检查，界址清楚，并利用钢尺、激光测距仪或全站仪等测量工具测量界址点的坐标。";
//        } else if(DxfHelper.TYPE == DxfHelper.TYPE_LIZHI){
//            qjdcjs = "   经现场核实，申请书上有关栏目填写与实地情况一致；本宗地及相邻宗地指界人均到现场指界。调查员对"+ map_.get("ZD.JZDS") +"个界址点均设置了"+ "界址标志。采用激光测距仪实地丈量了"+ map_.get("ZD.JZDS") +"条界址边长。\n" ;
//            djdcjs = "   根据权属调查的实际情况，经检查，"+ map_.get("ZD.JZDS") +"个界标保存完好。使用徕卡全站仪，采用解析法测绘界址点，使用不动产权籍调查测绘软件计算出宗地面积为"+ map_.get("ZD.ZDMJ") +"平方米。";
//        }
//
//        map_.put("ZD.QJDXJSFF", qjdcjs);
//        map_.put("DCJS.DJCLJS", djdcjs);
    }

    public static void Put_data_hkbs(Map<String, Object> map_, String image_hkb, int base) {
        Map<String, Object> map_hkbzp_p = new LinkedHashMap<>();
        List<Map<String, Object>> map_hkbzp_ps = new ArrayList<>();
        File file = new File(image_hkb);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    if (f.isFile() && FileUtils.isImage(f)) {
                        map_hkbzp_p.put("img." + base + "hkb" + ((i % base) + 1), f.getPath());
                        if (((i % base) + 1) == base) {
                            map_hkbzp_ps.add(map_hkbzp_p);
                            map_hkbzp_p = new LinkedHashMap<>();
                        } else if (i + 1 >= files.length) {
                            for (int j = (i + 1) % base; j < base; j++) {
                                map_hkbzp_p.put("img." + base + "hkb" + (j + 1), "");
                            }
                            map_hkbzp_ps.add(map_hkbzp_p);
                        }
                    }
                }
            }
        }
        map_.put("list." + base + "hkb", map_hkbzp_ps);

    }
    public static void Put_data_fsss(MapInstance mapInstance, Map<String, Object> map_,Feature f_zd, List<Feature> fs_fsss) {
        List<Feature> fs_zrzs = new ArrayList<>(fs_fsss);
        // 自然幢最少1个
        map_.put("FSSS.ZWZDMJ", "");
        map_.put("FSSS.CSZDMJ", "");
        map_.put("FSSS.SSZDMJ", "");
        map_.put("FSSS.SPZDMJ", "");
        map_.put("FSSS.PFZDMJ", ""); // 棚房占地面积
        map_.put("FSSS.YDFWXZDMJ", ""); // 用地范围线占地面积
        map_.put("FSSS.QTZDMJ", ""); // 其他占地面积
        double zzdmj = FeatureHelper.Get(f_zd,"JZZDMJ",0d);
        map_.put("FSSS.ZZDMJ", String.format("%.2f",AiUtil.Scale(zzdmj, 2)));
        if (fs_zrzs.size() > 1) {

            FeatureViewFSSS fv = FeatureViewFSSS.From(mapInstance);
            double zwmj = fv.GetJZZDMJ(fs_fsss, "杂屋");
            double csmj = fv.GetJZZDMJ(fs_fsss, "厕所");
            double ssmj = fv.GetJZZDMJ(fs_fsss, "畜舍");
            double spmj = fv.GetJZZDMJ(fs_fsss, "庭院晒坪");
            double ydfwxmj = fv.GetJZZDMJ(fs_fsss, "用地范围线");
            double pfmj = fv.GetJZZDMJ(fs_fsss, "棚房");
            zzdmj += zwmj+csmj+ssmj+spmj+pfmj;

            double qtmj = ydfwxmj - zzdmj;
            map_.put("FSSS.ZWZDMJ", String.format("%.2f",AiUtil.Scale(zwmj, 2)));
            map_.put("FSSS.CSZDMJ", String.format("%.2f",AiUtil.Scale(csmj, 2)));
            map_.put("FSSS.SSZDMJ",String.format("%.2f",AiUtil.Scale(ssmj, 2)));
            map_.put("FSSS.SPZDMJ", String.format("%.2f",AiUtil.Scale(spmj, 2)));
            map_.put("FSSS.PFZDMJ",  String.format("%.2f",AiUtil.Scale(pfmj, 2))); // 棚房占地面积
            map_.put("FSSS.YDFWXZDMJ",  String.format("%.2f",AiUtil.Scale(ydfwxmj, 2))); // 用地范围线占地面积
            map_.put("FSSS.ZZDMJ", String.format("%.2f",AiUtil.Scale(zzdmj, 2)));
            map_.put("FSSS.QTZDMJ", String.format("%.2f",AiUtil.Scale(qtmj, 2)));
        }
    }

    // 设置自然幢
    public static void Put_data_zrz(MapInstance mapInstance, Map<String, Object> map_, String bdcdyh, Feature f_zd, List<Feature> fs_zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_c, List<Feature> fs_fsss) {
        List<Feature> fs_zrzs = new ArrayList<>(fs_zrz);
        List<Feature> fs_zrz_fsss = new ArrayList<>(fs_fsss);
        List<Map<String, Object>> maps_fsss = new ArrayList<>();
        List<Map<String, Object>> maps_fssslx = new ArrayList<>();
        List<Map<String, Object>> maps_zrz_fsss = new ArrayList<>();
        List<Map<String, Object>> maps_zrz_fssslx = new ArrayList<>();
        // 自然幢最少1个
        if (fs_zrzs.size() < 1) {
            fs_zrzs.add(GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZRZ).createFeature());
        }
        for (Feature fsFsss : fs_fsss) {
            Map<String, Object> map_fsss = new LinkedHashMap<>();
            String jzwmc = FeatureHelper.Get(fsFsss, "JZWMC", "");
            map_fsss.put("H.FWLXFF",jzwmc);
            map_fsss.put("ZRZ.ZH", "");
            String fwjg = DicUtil.dic("fwjg", FeatureHelper.Get(fsFsss, "FWJG", "4"));
            if (!TextUtils.isEmpty(fwjg) && fwjg.contains("]")) {
                fwjg = StringUtil.substr(fwjg, fwjg.lastIndexOf("]")+1);
            }
//            map_fsss.put("ZRZ.FFJZWJBYT", DicUtil.dic("fwyt", FeatureHelper.Get(fsFsss, "JZWJBYT", "")));
            map_fsss.put("ZRZ.FFJZWJBYT", "");
            map_fsss.put("H.YT", "");
            map_fsss.put("ZRZ.FWDPG", "");
            map_fsss.put("ZRZ.FWJGFF", "");
            map_fsss.put("ZRZ.CG", FeatureHelper.Get(fsFsss,"LCGD",""));
            map_fsss.put("ZRZ.WDLX", "");
            map_fsss.put("ZRZ.SCJZMJ", AiUtil.Scale(FeatureHelper.Get(fsFsss,"SCJZMJ",0d), 2)+"");
            map_fsss.put("ZRZ.QTGSD", FeatureHelper.Get(fsFsss, "QTGSD", "自墙"));
            map_fsss.put("ZRZ.QTGSN", FeatureHelper.Get(fsFsss, "QTGSN", "自墙"));
            map_fsss.put("ZRZ.QTGSX", FeatureHelper.Get(fsFsss, "QTGSX", "自墙"));
            map_fsss.put("ZRZ.QTGSB", FeatureHelper.Get(fsFsss, "QTGSB", "自墙"));


            if (!FeatureViewFSSS.IsSc(jzwmc)){
                map_fsss.put("ZRZ.JGRQ",AiUtil.GetValue(fsFsss.getAttributes().get("JGRQ"),"2020-01-01").substring(0,7));
                map_fsss.put("ZRZ.FWJGFF", fwjg);
                map_fsss.put("ZRZ.ZCS", FeatureHelper.Get(fsFsss, "ZCS", 1)+"");
            }else {
                map_fsss.put("ZRZ.JGRQ", "");
                map_fsss.put("ZRZ.SCJZMJ", String.format("%.2f",AiUtil.Scale(FeatureHelper.Get(fsFsss,"ZZDMJ",0d), 2)));
                map_fsss.put("ZRZ.QTGSD", FeatureHelper.Get(fsFsss, "QTGSD", ""));
                map_fsss.put("ZRZ.QTGSN", FeatureHelper.Get(fsFsss, "QTGSN", ""));
                map_fsss.put("ZRZ.QTGSX", FeatureHelper.Get(fsFsss, "QTGSX", ""));
                map_fsss.put("ZRZ.QTGSB", FeatureHelper.Get(fsFsss, "QTGSB", ""));
                map_fsss.put("ZRZ.ZCS", "");
                map_fsss.put("ZRZ.FWJGFF", "");
            }


            maps_fsss.add(map_fsss);
        }
        for (Feature fsFssslx : fs_fsss) {
            Map<String, Object> map_fssslx = new LinkedHashMap<>();
            String jzwmc = FeatureHelper.Get(fsFssslx, "JZWMC", "");
            map_fssslx.put("H.FWLXFF",jzwmc);
            map_fssslx.put("ZRZ.ZH", "");
            String fwjg = DicUtil.dic("fwjg", FeatureHelper.Get(fsFssslx, "FWJG", "4"));
            if (!TextUtils.isEmpty(fwjg) && fwjg.contains("]")) {
                fwjg = StringUtil.substr(fwjg, fwjg.lastIndexOf("]")+1);
            }
//            map_fsss.put("ZRZ.FFJZWJBYT", DicUtil.dic("fwyt", FeatureHelper.Get(fsFsss, "JZWJBYT", "")));
            map_fssslx.put("ZRZ.FFJZWJBYT", "");
            map_fssslx.put("H.YT", "");
            map_fssslx.put("ZRZ.FWDPG", "");
            map_fssslx.put("ZRZ.FWJGFF", "");
            map_fssslx.put("ZRZ.CG", FeatureHelper.Get(fsFssslx,"LCGD",""));
            map_fssslx.put("ZRZ.WDLX", "");
            map_fssslx.put("ZRZ.SCJZMJ", AiUtil.Scale(FeatureHelper.Get(fsFssslx,"SCJZMJ",0d), 2)+"");
            map_fssslx.put("ZRZ.QTGSD", FeatureHelper.Get(fsFssslx, "QTGSD", "自墙"));
            map_fssslx.put("ZRZ.QTGSN", FeatureHelper.Get(fsFssslx, "QTGSN", "自墙"));
            map_fssslx.put("ZRZ.QTGSX", FeatureHelper.Get(fsFssslx, "QTGSX", "自墙"));
            map_fssslx.put("ZRZ.QTGSB", FeatureHelper.Get(fsFssslx, "QTGSB", "自墙"));


            if (!FeatureViewFSSS.IsSc(jzwmc)){
                map_fssslx.put("ZRZ.JGRQ",AiUtil.GetValue(fsFssslx.getAttributes().get("JGRQ"),"2020-01-01").substring(0,7));
                map_fssslx.put("ZRZ.FWJGFF", fwjg);
                map_fssslx.put("ZRZ.ZCS", FeatureHelper.Get(fsFssslx, "ZCS", 1)+"");
            }else {
                map_fssslx.put("ZRZ.JGRQ", "");
                map_fssslx.put("ZRZ.SCJZMJ", String.format("%.2f",AiUtil.Scale(FeatureHelper.Get(fsFssslx,"ZZDMJ",0d), 2)));
                map_fssslx.put("ZRZ.QTGSD", FeatureHelper.Get(fsFssslx, "QTGSD", ""));
                map_fssslx.put("ZRZ.QTGSN", FeatureHelper.Get(fsFssslx, "QTGSN", ""));
                map_fssslx.put("ZRZ.QTGSX", FeatureHelper.Get(fsFssslx, "QTGSX", ""));
                map_fssslx.put("ZRZ.QTGSB", FeatureHelper.Get(fsFssslx, "QTGSB", ""));
                map_fssslx.put("ZRZ.ZCS", "");
                map_fssslx.put("ZRZ.FWJGFF", "");
            }

            if (jzwmc.contains("庭院晒坪")|| jzwmc.contains("用地范围线")){
                map_fssslx.put("ZRZ.ZH"," ");
                map_fssslx.put("H.FWLXFF"," ");
                map_fssslx.put("ZRZ.FWJGFF"," ");
                map_fssslx.put("ZRZ.SCJZMJ"," ");
                map_fssslx.put("ZRZ.ZCS"," ");
                map_fssslx.put("ZRZ.ZTS"," ");
                map_fssslx.put("ZRZ.SJGRQ"," ");
                map_fssslx.put("ZRZ.ZZDMJ"," ");
                map_fssslx.put("Z.QTGSD"," ");
                map_fssslx.put("Z.QTGSN"," ");
                map_fssslx.put("Z.QTGSX"," ");
                map_fssslx.put("Z.QTGSB"," ");
            }
            maps_fssslx.add(map_fssslx);
        }

        List<Map<String, Object>> maps_zrz = new ArrayList<>();
        List<String> hzszc = new ArrayList<>();
        List<String> hzfwjgff = new ArrayList<>();
        List<String> hzjgrq = new ArrayList<>();
        List<String> hzcg = new ArrayList<>();


        for (Feature zrz : fs_zrzs) {
            // 幢基本信息
            String orid = mapInstance.getOrid(zrz);

            for (Feature f_c : fs_c) {
                String orid_path = mapInstance.getOrid_Path(f_c);
                if (orid_path.contains(orid)){
                    String cg = AiUtil.Scale(FeatureHelper.Get(f_c,"CG",0d),2)+"";
                    hzcg.add(cg);
                }

            }


            String z_zrzh = AiUtil.GetValue(zrz.getAttributes().get("ZRZH"), "");
            if (AiUtil.GetValue(zrz.getAttributes().get("JZWMC"), "").equals("主房")) {
                map_.put("ZRZ.ZFZH", AiUtil.GetValue(zrz.getAttributes().get("ZRZH"), ""));
            }
//            int z_zcs = AiUtil.GetValue(zrz.getAttributes().get("ZCS"), 1);
            Map<String, Object> map_zrz = new LinkedHashMap<>();
            Put_data_zrz(mapInstance, map_zrz, zrz);
            hzszc.add(map_zrz.get("ZRZ.CSHZ") + "/" + map_zrz.get("ZRZ.ZCS"));

            String fwjg_h =  (String) map_zrz.get("ZRZ.FWJGFF");
            if (fwjg_h.contains("结构")) {
                fwjg_h = fwjg_h.substring(0, fwjg_h.lastIndexOf("结构"));
            }
            map_zrz.put("ZRZ.FWJGFF",fwjg_h);

            hzfwjgff.add(map_zrz.get("ZRZ.FWJGFF") + "");
            hzjgrq.add(map_zrz.get("ZRZ.JGRQ") + "");

            // 设置户
            Put_data_hs(mapInstance, map_zrz, z_zrzh, fs_h);
            //  层
            Put_data_cs(mapInstance, map_zrz, z_zrzh, zrz, fs_z_fsjg, fs_h);
            Put_data_cps(mapInstance, map_, bdcdyh, z_zrzh, zrz, 4, fs_z_fsjg, fs_h);
            //  层的分页
//            Put_data_cps(mapInstance, map_zrz, bdcdyh, z_zrzh, zrz, 6, fs_z_fsjg, fs_h);
            Put_data_cps_hz(mapInstance, map_zrz, bdcdyh, z_zrzh, zrz, 2, fs_z_fsjg, fs_h,fs_c);

            String image_fwzp = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(zrz) + FeatureHelper.FJCL) + "房屋照片";
            map_zrz.put("img.fwzp", image_fwzp);
            map_zrz.put("ZRZ.FFJZWJBYT", DicUtil.dic("fwyt", FeatureHelper.Get(zrz, "JZWJBYT", "")));
            map_zrz.put("ZRZ.CG", StringUtil.Join(hzcg, true));
            if (DxfHelper.TYPE == DxfHelper.TYPE_LIZHI){
                String zrzh = (String) map_zrz.get("ZRZ.ZH");
                if (!TextUtils.isEmpty(zrzh)){
                    map_zrz.put("ZRZ.ZH","F"+zrzh);
                }
            }
            //墙体归属取自然幢
            map_zrz.put("Z.QTGSD",FeatureHelper.Get(zrz,"QTGSD", "自墙"));
            map_zrz.put("Z.QTGSN",FeatureHelper.Get(zrz,"QTGSN", "自墙"));
            map_zrz.put("Z.QTGSX",FeatureHelper.Get(zrz,"QTGSX", "自墙"));
            map_zrz.put("Z.QTGSB",FeatureHelper.Get(zrz,"QTGSB", "自墙"));
            maps_zrz.add(map_zrz);
            // 拷贝自然幢的内容
//            FileUtils.copyFile(FileUtils.getAppDirAndMK(mapInstance.getpath_feature(zrz)), file_zrzs + mapInstance.getId(zrz));
        }
        Put_data_all_cps(mapInstance, map_, bdcdyh, fs_zrzs, 4, fs_z_fsjg, fs_h);
        map_.put("ZRZ.HZSZC", StringUtil.Join(hzszc, true));
        map_.put("ZRZ.HZFWJG", StringUtil.Join(hzfwjgff, true));
        map_.put("ZRZ.HZJGRQ", StringUtil.Join(hzjgrq, true));
        map_.put("ZRZ.HZZ", fs_zrzs.size() + "");
        if (DxfHelper.TYPE == DxfHelper.TYPE_TONGSHAN){
            for (int i = 0; i < 7-fs_zrz.size(); i++) {
                Feature f = mapInstance.getTable(FeatureHelper.LAYER_NAME_ZRZ).createFeature();
                Map<String, Object> map_zrz = new LinkedHashMap<>();
                Put_data_zrz(mapInstance, map_zrz, f);
                maps_zrz.add(map_zrz);
                map_zrz.put("ZRZ.ZH"," ");
                map_zrz.put("ZRZ.ZHFS"," ");
                map_zrz.put("H.HH"," ");
                map_zrz.put("ZRZ.ZTS"," ");
                map_zrz.put("ZRZ.ZCS"," ");
                map_zrz.put("ZRZ.FWJGFF"," ");
                map_zrz.put("ZRZ.SJGRQ"," ");
                map_zrz.put("ZRZ.ZZDMJ"," ");
                map_zrz.put("ZRZ.SCJZMJ"," ");
                map_zrz.put("H.CQLY"," ");
                map_zrz.put("Z.QTGSD"," ");
                map_zrz.put("Z.QTGSN"," ");
                map_zrz.put("Z.QTGSX"," ");
                map_zrz.put("Z.QTGSB"," ");
            }
        }
        map_.put("list.zrz", maps_zrz);
        maps_zrz_fssslx.addAll(maps_zrz);
        maps_zrz_fssslx.addAll(maps_fssslx);
        map_.put("list.fszrzlx", maps_zrz_fssslx);
        maps_zrz_fsss.addAll(maps_zrz);
        maps_zrz_fsss.addAll(maps_fsss);
        map_.put("list.fszrz", maps_zrz_fsss);
    }
    public static void Put_data_zrz(MapInstance mapInstance, Map<String, Object > map_, Feature f_zd, List<Feature> fs_zrz) {
        List<Feature> fs_zrzs = new ArrayList<>(fs_zrz);
        // 自然幢最少1个
        if (fs_zrzs.size() < 1) {
            fs_zrzs.add(GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZRZ).createFeature());
        }
        List<Map<String, Object>> maps_zrz = new ArrayList<>();
        List<String> hzszc = new ArrayList<>();
        List<String> hzfwjgff = new ArrayList<>();
        List<String> hzjgrq = new ArrayList<>();
        int zd_cshz = 0;
        String fwsx = "";//  主房/厨房
        String fwzrzh = "";// 自然幢号
        String fwzh = "";//  0001/0002
        boolean iszf = true;
        for (Feature zrz : fs_zrzs) {
            // 幢基本信息
            String jzwmc = FeatureHelper.Get(zrz, "JZWMC", "");
            String zh = FeatureHelper.Get(zrz, "ZH", "");
            String zrzh = FeatureHelper.Get(zrz, "ZRZH", "");

            if (StringUtil.IsNotEmpty(fwsx)) {
                fwsx += "/" + jzwmc;
                fwzh += "/" + zh;
                fwzrzh += "/" + zrzh;
            } else {
                fwsx += jzwmc;
                fwzh += zh;
                fwzrzh += zrzh;
            }
            map_.put("ZRZ.FWSX", fwsx);
            map_.put("ZRZ.FWZH", fwzh);
            map_.put("ZRZ.FWZRZH", fwzrzh);
            zd_cshz += FeatureHelper.Get(zrz, "ZCS", 1);
            if (jzwmc.contains("主房")) {
                map_.put("ZRZ.ZFZH", zh);
            }
            Map<String, Object> map_zrz = new LinkedHashMap<>();
            Put_data_zrz(mapInstance, map_zrz, zrz);
            String fwjg = (String) map_zrz.get("ZRZ.FWJGFF");
            if (!TextUtils.isEmpty(fwjg) && fwjg.contains("]")) {
                fwjg = fwjg.substring(fwjg.lastIndexOf("]") + 1);
                map_zrz.put("ZRZ.FWJGFF", fwjg);
            }

            String fwjg_h =  (String) map_zrz.get("ZRZ.FWJGFF");
            if (fwjg_h.contains("结构")) {
                fwjg_h = fwjg_h.substring(0, fwjg_h.lastIndexOf("结构"));
            }
            map_zrz.put("ZRZ.FWJGFF",fwjg_h);

            hzszc.add(map_zrz.get("ZRZ.CSHZ") + "/" + map_zrz.get("ZRZ.ZCS"));
            hzfwjgff.add(map_zrz.get("ZRZ.FWJGFF") + "");
            String jgrq = map_zrz.get("ZRZ.SJGRQ") + "";
            hzjgrq.add(jgrq);
            if (!TextUtils.isEmpty(jgrq) &&!"null".equals(jgrq) && iszf) {
                map_.put("ZRZ.MJGRQ", jgrq);
                if (jzwmc.contains("主房")) {
                    iszf = false;
                }
            }
            map_zrz.put("ZRZ.ZH", Integer.parseInt(FeatureHelper.Get(zrz, "ZH", "1")) + "");
            map_zrz.put("ZRZ.4ZH", FeatureHelper.Get(zrz, "ZH", "0001"));
            map_zrz.put("ZRZ.2ZH", String.format("%02d", Integer.parseInt(FeatureHelper.Get(zrz, "ZH", "1"))));
            maps_zrz.add(map_zrz);
        }
        map_.put("ZD.ZRZCSHZ", zd_cshz);
        map_.put("ZRZ.HZSZC", StringUtil.Join(hzszc, true));
        map_.put("ZRZ.HZFWJG", StringUtil.Join(hzfwjgff, true));
        map_.put("ZRZ.HZJGRQ", StringUtil.Join(hzjgrq, true));

        map_.put("ZRZ.HZZ", fs_zrzs.size() + "");
        map_.put("list.zrz", maps_zrz);
    }


    public static void Put_data_ljz(MapInstance mapInstance, Map<String, Object> map_,  Feature f_zd,List<Feature> fs_ljz) {
        List<Feature> fs_ljzs = new ArrayList<>(fs_ljz);

        if (fs_ljzs.size() < 1) {
            fs_ljzs.add(GetTable(mapInstance, FeatureHelper.TABLE_NAME_LJZ).createFeature());
        }


        List<Map<String, Object>> maps_ljz = new ArrayList<>();
        List<String> ljzhzjgrq = new ArrayList<>();
        for (Feature ljz : fs_ljzs) {
            String orid = mapInstance.getOrid(ljz);
            Map<String, Object> map_ljz = new LinkedHashMap<>();
            Put_data_ljz(mapInstance, map_ljz, ljz);
            String jgrq = map_ljz.get("LJZ.SJGRQ") + "";
            ljzhzjgrq.add(jgrq);
            maps_ljz.add(map_ljz);
        }
        map_.put("LJZ.HZJGRQ", StringUtil.Join(ljzhzjgrq, true));
    }
    public static void Put_data_ljz(MapInstance mapInstance, Map<String, Object> map_, Feature fs_ljz){
        GetReplecData(mapInstance.activity, map_, fs_ljz, "", "FHYT", "FWJG");
        map_.put("LJZ.SJGRQ", StringUtil.substr(AiUtil.GetValue(map_.get("LJZ.JGRQ"), ""), 0, 4));
        Map<String, Object> map_ljz = new LinkedHashMap<>();

        //幢号？？？

        //户号

        //总套数  1

        //总层数
        map_.put("LJZ.ZCS","1");
        //所在层
        map_.put("LJZ.SZC","");
        //房屋结构

        String fwjg_h = (String) map_ljz.get("LJZ.FWJG1");
        if (fwjg_h.contains("结构")) {
            fwjg_h = fwjg_h.substring(0,fwjg_h.lastIndexOf("结构"));
        }
        map_.put("LJZ.FWJG1",fwjg_h);
//        竣工时间

        //占地面积
        //建筑面积
        //专有建筑面积
        //分摊建筑面积
        //产权来源
        //墙体归属  东南西北

        //墙体归属取逻辑幢
        map_ljz.put("LJZ.QTGSD",FeatureHelper.Get(fs_ljz,"QTGSD", "自墙"));
        map_ljz.put("LJZ.QTGSN",FeatureHelper.Get(fs_ljz,"QTGSN", "自墙"));
        map_ljz.put("LJZ.QTGSX",FeatureHelper.Get(fs_ljz,"QTGSX", "自墙"));
        map_ljz.put("LJZ.QTGSB",FeatureHelper.Get(fs_ljz,"QTGSB", "自墙"));


        map_.put("list.ljz",map_ljz);
    }

    //  设置幢的基本信息
    public static void Put_data_zrz(MapInstance mapInstance, Map<String, Object> map_, Feature zrz) {
        // 幢基本信息
//        String z_zrzh = AiUtil.GetValue(zrz.getAttributes().get("ZRZH"), "");
        int z_zcs = AiUtil.GetValue(zrz.getAttributes().get("ZCS"), 1);
        Map<String, Object> map = new LinkedHashMap<>();
        // 设置单个幢信息
        // 提前防止覆盖
        map.put("ZRZ.ZHFF", AiUtil.GetValue(zrz.getAttributes().get("ZH"), 1) + "");
        if ((zrz.getAttributes().get(FeatureHelper.TABLE_ATTR_BDCDYH) + "").contains("F99990001")) {

        } else {
            map.put("ZRZ.ZHFS", StringUtil.substr_last(AiUtil.GetValue(zrz.getAttributes().get("ZRZH"), ""), 5));
        }
        map.put("ZRZ.ZTS", "");
        map.put("ZRZ.GYQK", "");
        GetReplecData(mapInstance.activity, map, zrz, "", "FHYT", "FWJG");
        map.put("ZRZ.ZCS", AiUtil.GetValue(zrz.getAttributes().get("ZCS"), "", "0.#"));
        map.put("ZRZ.CSHZ", z_zcs > 1 ? ("1-" + z_zcs) : 1);
        map.put("ZRZ.SJGRQ", StringUtil.substr(AiUtil.GetValue(map.get("ZRZ.JGRQ"), ""), 0, 7));
        //建筑日期到年   通山模板   2020/06/04
        if (DxfHelper.TYPE == DxfHelper.TYPE_LIZHI|| DxfHelper.TYPE == DxfHelper.TYPE_TONGSHAN  || DxfHelper.TYPE == DxfHelper.TYPE_XIANTAO) {
            if (map.get("ZRZ.JGRQ").toString().length() > 4) {
            }
            map.put("ZRZ.SJGRQ", StringUtil.substr(AiUtil.GetValue(map.get("ZRZ.JGRQ"), ""), 0, 4));

        }

        String qpkg = FeatureHelper.Get(zrz, "SOVIT2", "");// 切坡数据,宽，高，后墙距离
        map.put("ZRZ.QPKD", "");
        map.put("ZRZ.QPGD", "");
        map.put("ZRZ.HQYPJJL", "");

        String[] qp = qpkg.split("/");
        String[] qpk = {"ZRZ.QPKD","ZRZ.QPGD","ZRZ.HQYPJJL"};;
        for (int i = 0; i < qp.length; i++) {
            map.put(qpk[i],qp[i]);
        }
        if(FeatureHelper.isPolygonGeometryValid(zrz.getGeometry())){
            Point point = GeometryEngine.labelPoint((Polygon) zrz.getGeometry());
            Point p = MapHelper.geometry_get(point, SpatialReference.create(4490));
            map.put("ZRZ.ZXDX", p.getX());
            map.put("ZRZ.ZXDY", p.getY());
        }
        GetReplecData(map_, "", map);
    }


    // 在全局放一个幢 ZRZ
    public static void Put_data_zrz(MapInstance mapInstance, Map<String, Object> map_, List<Feature> fs_zrz) {
        Feature zrz = null;
        if (fs_zrz.size() < 1) {
            zrz = GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZRZ).createFeature();
        } else {
            zrz = fs_zrz.get(0);
        }
        Put_data_zrz(mapInstance, map_, zrz);
    }

    // 幢里面放户
    public static void Put_data_hs(MapInstance mapInstance, Map<String, Object> map_, String zrzh, List<Feature> hs) {
        List<Feature> features_h = GetHbyZrzh(mapInstance, zrzh, hs);
        if (features_h != null && features_h.size() > 0) {
            map_.put("ZRZ.CQLY", AiUtil.GetValue(features_h.get(0).getAttributes().get("CQLY"), "自建"));
        } else {
            map_.put("ZRZ.CQLY", "自建");
        }
        map_.put("Z.QTGSD", AiUtil.GetValue(features_h.get(0).getAttributes().get("QTGSD"), "自有墙"));
        map_.put("Z.QTGSN", AiUtil.GetValue(features_h.get(0).getAttributes().get("QTGSN"), "自有墙"));
        map_.put("Z.QTGSX", AiUtil.GetValue(features_h.get(0).getAttributes().get("QTGSX"), "自有墙"));
        map_.put("Z.QTGSB", AiUtil.GetValue(features_h.get(0).getAttributes().get("QTGSB"), "自有墙"));

        Put_data_hs(mapInstance, map_, features_h);

    }

    // 幢里面放户
    public static void Put_data_hs(MapInstance mapInstance, Map<String, Object> map_, List<Feature> hs) {
        // 设置幢、层、分组等下面的户
        List<Map<String, Object>> maps_h = new ArrayList<>();
        double scjzmj_count = 0;

        for (Feature h : hs) {
            //设置户
            Map<String, Object> map_h = new LinkedHashMap<>();
            Map<String, Object> map = new LinkedHashMap<>();
//            GetReplecData(mapInstance.activity, map, h, "", "YT:fwyt", "GHYT:fwyt", "CB:fwcb", "FWJG", "FWXZ", "QLRZJZL:zjzl", "ZT:fwzt");
//            GetReplecData(map_h, "", map);
            Put_data_h(mapInstance, map_h, h);
            // 户添加到户列表里面
            maps_h.add(map_h);
            scjzmj_count += AiUtil.GetValue(h.getAttributes().get("SCJZMJ"), 0d);
        }
        // 为幢设置户
        map_.put("list.hs", maps_h);
        map_.put("H.HZJZMJ", AiUtil.GetValue(scjzmj_count, "", AiUtil.F_FLOAT2));
    }

    // 在全局放一个户 H
    public static void Put_data_h(MapInstance mapInstance, Map<String, Object> map_, List<Feature> fs_h) {
        List<Feature> fs_hs = new ArrayList<>(fs_h);
        Feature h = null;
        if (fs_hs.size() < 1) {
            h = GetTable(mapInstance, FeatureHelper.TABLE_NAME_H).createFeature();
        } else {
            h = fs_hs.get(0);
        }
        Put_data_h(mapInstance, map_, h);
//        map_ = getReplecDataDic(map_, h,  "YT:fwyt", "FWJG", "FWXZ", "QLRZJZL:zjzl", "ZT:fwzt");
//        getReplecData(map_, "H", h.getAttributes() );
//        GetReplecData(mapInstance.activity, map_, h, "", "YT:fwyt", "GHYT:fwyt", "CB:fwcb", "FWJG", "FWXZ", "QLRZJZL:zjzl", "ZT:fwzt");
    }

    public static void Put_data_h(MapInstance mapInstance, Map<String, Object> map_, Feature h) {
        String bdcdyh = (String) map_.get("QLRXX.BDCDYH");
        GetReplecData(mapInstance.activity, map_, h, "", "YT:fwyt", "GHYT:fwyt", "CB:fwcb", "FWLX", "FWJG", "FWXZ", "QLRZJZL:zjzl", "ZT:fwzt");
        if (FeatureHelper.isBDCDYHValid(bdcdyh)) {
            String bdchh = StringUtil.substr_last(bdcdyh, 4);
            String bdczh = StringUtil.substr_last(bdcdyh, 5, 4);
            String bdczdh = StringUtil.substr_last(bdcdyh, 7, 9);
            String bdcdjzq = StringUtil.substr_last(bdcdyh, 3, 16);
            String bdcdjq = StringUtil.substr_last(bdcdyh, 3, 19);
            String bdcxzq = StringUtil.substr_last(bdcdyh, 6, 22);
            map_.put("H.BDCHH", bdchh);
            map_.put("H.BDCZH", bdczh);
            map_.put("H.BDCZDH", bdczdh);
            map_.put("H.BDCDJZQ", bdcdjzq);
            map_.put("H.BDCDJQ", bdcdjq);
            map_.put("H.BDCXZQ", bdcxzq);
            map_.put("H.HH", 0 + StringUtil.substr_last(bdcdyh, 3));
        }

        Double tnmj = 0d;
        Double ftxs = 0d;
        try {
            Double.parseDouble((String) map_.get("H.SCJZMJ"));
            ftxs = Double.parseDouble(AiUtil.GetValue(map_.get("ZD." + FeatureViewZD.TABLE_ATTR_FTXS_ZD), "0"));
        } catch (Exception e) {

        }

        double ftmj = tnmj * ftxs;
        double jzmj = tnmj + ftmj;
        map_.put("H.SCJZMJ", String.format("%.2f", jzmj));
        map_.put("H.SCFTJZMJ", String.format("%.2f", ftmj));
        map_.put("H.TNMJ", String.format("%.2f", tnmj));

    }


    // 幢里面放附属
    public static void Put_data_zfsjgs(MapInstance mapInstance, Map<String, Object> map_, List<Feature> zfsjg) {
        // 设置幢、层、分组等下面的户
        List<Map<String, Object>> maps_fsjg = new ArrayList<>();
        double scjzmj_count = 0;
        for (Feature fsjg : zfsjg) {
            //设置户
            Map<String, Object> map_fsjg = new LinkedHashMap<>();
            Map<String, Object> map = new LinkedHashMap<>();
            GetReplecData(mapInstance.activity, map, fsjg, "", "FHMC:z_fsjg_lx", "TYPE:hsmjlx");
            GetReplecData(map_fsjg, "", map);
            // 附属列表里面
            maps_fsjg.add(map_fsjg);
            scjzmj_count += AiUtil.GetValue(fsjg.getAttributes().get("HSMJ"), 0d);
        }
        // 为幢设置户
        map_.put("list.zfsjg", maps_fsjg);
        map_.put("HZJZMJ", AiUtil.GetValue(scjzmj_count, "", AiUtil.F_FLOAT2));
    }

    // 层
    public static void Put_data_cs(MapInstance mapInstance, Map<String, Object> map_, String zrzh, Feature zrz, List<Feature> fs_z_fsjg, List<Feature> fs_h) {
        Map<String, List<Feature>> map_chs = GetCbyZrz(zrzh, fs_z_fsjg, fs_h);
        List<Map<String, Object>> maps_c = new ArrayList<>();
        String chzh = "";
        for (String ch : map_chs.keySet()) {
            if (StringUtil.IsEmpty(chzh)) {
                chzh = ch;
            }
            Map<String, Object> map_c = new LinkedHashMap<>();
            map_c.put("C.CH", ch);
            String image_fcfwt = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(zrz) + "附件材料/分层分户图/") + "分层分户图_" + ch + ".jpg";
            map_c.put("img.fcfht", image_fcfwt);
            // 层设置户
            Put_data_hs(mapInstance, map_c, map_chs.get(ch));
            maps_c.add(map_c);
            //最后一个
            if (maps_c.size() == map_chs.size()) {
                if (!chzh.equals(ch)) {
                    chzh += "-" + ch;
                }
            }
        }

        // 为幢设置层
        map_.put("list.cs", maps_c);
        map_.put("ZRZ.SZCSHZ", chzh);
    }

    // 层的分页
    public static void Put_data_cps_hz(com.ovit.app.map.model.MapInstance mapInstance, Map<String, Object> map_, String bdcdyh, String zrzh, Feature zrz, int size, List<Feature> fs_z_fsjg, List<Feature> fs_h, List<Feature> fs_c) {

        Map<String, List<Feature>> map_chs = GetCbyZrz(zrzh, fs_z_fsjg, fs_h);
        Map<String, String> map_cg = GetCGbyZrzOrid(mapInstance.getOrid(zrz), fs_c);
        String image_fcthzPath = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(zrz) + "附件材料/分层图/") ;
        List<String> fctPaths = FileUtils.getFilePathToSuffix(image_fcthzPath, "分层图_", ".jpg");

        List<Map<String, Object>> maps_p = new ArrayList<>();
        Map<String, Object> map_p = new LinkedHashMap<>();
        int i = 0;
        String ch1 = "";
        String gcmjhz = "";
        double jzmj = 0;

        for (int i1 = 0; i1 < fctPaths.size(); i1++) {
            String item=fctPaths.get(i1);
            String fct_ch=item.substring(item.lastIndexOf("_")+1,item.lastIndexOf("."));
            double jzmj_=0;
            if (map_chs.get(fct_ch)!=null){
                if (fct_ch.length()==1){
                    // 每一层
                    jzmj_ = GetJZMJ(map_chs.get(fct_ch));
                    jzmj += jzmj_;
                }else{
                    jzmj_ = GetJZMJ(map_chs.get(fct_ch.substring(0,1)));
                    int zcs = 1 ;
                    try {
                        zcs =Integer.parseInt(fct_ch.substring(fct_ch.length() - 1))- Integer.parseInt(fct_ch.substring(0, 1)) +1 ;
                    }catch (Exception e){
                    }
                    jzmj += jzmj_*zcs;
                }
            }
            int j = i % size;
            gcmjhz+="第"+fct_ch+"层"+jzmj_+",";
            map_p.put("CP.FCH" + (j + 1), "第"+fct_ch+"层平面图");
            map_p.put("CP.CG" + (j + 1),"（层高"+getCg(map_cg,fct_ch)+")");
            map_p.put("CP.FGCMJHZ" + (j + 1), fct_ch);
            map_p.put("CP.FJZMJ" + (j + 1), AiUtil.GetValue(jzmj_, "", AiUtil.F_FLOAT2));
            String image_fcfwt = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(zrz) + "附件材料/分层图/") + "分层图_" + fct_ch + ".jpg";
            map_p.put("img.ffcfht" + (j + 1), image_fcfwt);
            if (StringUtil.IsEmpty(ch1)) {
                ch1 = fct_ch;
            }
            // 当每页最后一个，或是最后一个
            if (j == size - 1 || i == fctPaths.size() - 1) {
                map_p.put("CP.FZH", StringUtil.substr_last(zrzh, 5));
                map_p.put("CP.FHH", StringUtil.substr_last(bdcdyh, 4));
                map_p.put("CP.FCHHZ", ch1.equals(fct_ch) ? fct_ch : (ch1.substring(0,1) + "-" + fct_ch.substring(fct_ch.length()-1)));
                map_p.put("CP.FJZMJHZ", AiUtil.GetValue(jzmj, "", AiUtil.F_FLOAT2));

                map_p.put("CP.FGCMJHZ",gcmjhz.substring(0,gcmjhz.length()-1));
                if (j < size - 1) {
                    for (int n = 1; n <= size - j; n++) {
                        map_p.put("CP.FCH" + (j + n + 1), "");
                        map_p.put("CP.FJZMJ" + (j + n + 1), "");
                        map_p.put("img.ffcfht" + (j + n + 1), "");
                        map_p.put("CP.CG" + (j + n + 1),"");
                    }
                }
                maps_p.add(map_p);
                map_p = new LinkedHashMap<>();
                ch1 = "";
                jzmj = 0;
                gcmjhz="";
            }
            i++;
        }

        // 为幢设置层
        map_.put("list.fcps", maps_p);
    }

    private static String getCg(Map<String, String> map_cg, String fct_ch) {
        String cg ="";
        for (String key : map_cg.keySet()) {
            if (fct_ch.contains(key)){
                if (TextUtils.isEmpty(cg)){
                    cg =map_cg.get(key)+"米";
                }else {
                    cg +=","+map_cg.get(key)+"米";
                }
            }
        }
        return cg;
    }

    private static Map<String, String> GetCGbyZrzOrid(String orid, List<Feature> fs_c) {
        Map<String, String> map = new HashMap<>();
        if (!FeatureHelper.isExistElement(fs_c)){
            return map;
        }
        for (Feature f_c : fs_c) {
            String orid_path = FeatureHelper.Get(f_c,"ORID_PATH","");
            if (StringUtil.IsNotEmpty(orid_path)&&orid_path.contains(orid)){
                String sjc = FeatureHelper.Get(f_c,"SJC","");
                String height = FeatureHelper.Get(f_c,"CG",0.00)+"";
                map.put(sjc,height);

            }
        }
        return map;
    }

    // 层的分页 汇总
    public static void Put_data_cps(com.ovit.app.map.model.MapInstance mapInstance, Map<String, Object> map_, String bdcdyh, String zrzh, Feature zrz, int size, List<Feature> fs_z_fsjg, List<Feature> fs_h) {
        Map<String, List<Feature>> map_chs = GetCbyZrz(zrzh, fs_z_fsjg, fs_h);
        List<Map<String, Object>> maps_p = new ArrayList<>();
        Map<String, Object> map_p = new LinkedHashMap<>();
        int i = 0;
        String ch1 = "";
        double jzmj = 0;
        for (String ch : map_chs.keySet()) {
            int j = i % size;
            // 每一层
            map_p.put("CP.CH" + (j + 1), ch);
            map_p.put("CP.MC" + (j + 1),ch+"层平面图");
            double jzmj_ = GetJZMJ(map_chs.get(ch));
            jzmj += jzmj_;
            map_p.put("CP.JZMJ" + (j + 1), AiUtil.GetValue(jzmj_, "", AiUtil.F_FLOAT2));
            String image_fcfwt = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(zrz) + "附件材料/分层分户图/") + "分层分户图_" + ch + ".jpg";
            map_p.put("img.fcfht" + (j + 1), image_fcfwt);

            if (StringUtil.IsEmpty(ch1)) {
                ch1 = ch;
            }
            // 当每页最后一个，或是最后一个
            if (j == size - 1 || i == map_chs.size() - 1) {
                map_p.put("CP.ZH", StringUtil.substr_last(zrzh, 5));
                map_p.put("CP.HH", StringUtil.substr_last(bdcdyh, 4));
                map_p.put("CP.CHHZ", ch1.equals(ch) ? ch : (ch1 + "-" + ch));
                map_p.put("CP.JZMJHZ", AiUtil.GetValue(jzmj, "", AiUtil.F_FLOAT2));
                if (j < size - 1) {
                    for (int n = 1; n <= size - j; n++) {
                        map_p.put("CP.CH" + (j + n + 1), "");
                        map_p.put("CP.MC" + (j + n + 1),"");
                        map_p.put("CP.JZMJ" + (j + n + 1), "");
                        map_p.put("img.fcfht" + (j + n + 1), "");
                    }
                }
                maps_p.add(map_p);
                map_p = new LinkedHashMap<>();
                ch1 = "";
                jzmj = 0;
            }
            i++;
        }
        // 为幢设置层
        map_.put("list.c"+size+"ps", maps_p);
    }

    // 层的分页户的属性进行分组 多幢显示到一张图表
    public static void Put_data_all_cps(MapInstance mapInstance, Map<String, Object> map_, String bdcdyh, List<Feature> features, int size, List<Feature> fs_z_fsjg, List<Feature> fs_h) {
        List<Map<String, Object>> maps_p = new ArrayList<>();
        Map<String, Object> map_p = new LinkedHashMap<>();
        for (int i = 1; i < size + 1; i++) {
            map_p.put("CP.MC" + i, "");
        }
//        map_p.put("CP.MC2", "");
//        map_p.put("CP.MC2", "");
//        map_p.put("CP.MC3", "");
//        map_p.put("CP.MC4", "");
        String ch1 = "";
        String zrzcshz = "";
        String zrzjghz = "";
        int z = 0;
        double jzmj = 0;
        for (Feature zrz : features) {
            int i = 0;
            String zrzh = AiUtil.GetValue(zrz.getAttributes().get("ZRZH"), "");
            if (zrzh.isEmpty()) {
                break;
            }
            String zh = zrzh.length() > 2 ? AiUtil.GetValue(zrzh.substring(zrzh.length() - 2)) : "";
            String cs = AiUtil.GetValue(zrz.getAttributes().get("ZCS"), "");
            int zcs = (int) Math.floor(Double.parseDouble(cs));

            String[] k_s = "FWJG".split(":");
            String k = zrz.getFeatureTable().getTableName() + "." + k_s[0] + "FF";
            String v = AiUtil.GetValue(dic(mapInstance.activity, zrz, k_s[0], k_s[k_s.length < 2 ? 0 : 1]), "");
            if (v.contains("]")) {
                v = v.substring(v.indexOf("]") + 1);
            }
            zh = zh.indexOf("0") == 0 ? zh.substring(1) : zh.substring(0);
            zrzcshz += zh + "幢" + zcs + "层" + "SYS.ENTER";
            zrzjghz += zh + "幢" + v + ",";

            Map<String, List<Feature>> map_chs = GetCbyZrz(zrzh, fs_z_fsjg, fs_h);
            for (String ch : map_chs.keySet()) {
//                int j = i % size;
                int y = z % size;
                // 每一层
                if (!TextUtils.isEmpty(zrzh) && zrzh.length() > 2) {
                    String zhch = AiUtil.GetValue(zrzh.substring(zrzh.length() - 2));
                    zhch = zhch.indexOf("0") == 0 ? zhch.substring(1) : zhch.substring(0);
                    zhch = zhch + "幢" + ch + "层";
                    map_p.put("CP.MC" + (y + 1), zhch);
                }
                map_p.put("CP.CH" + (y + 1), ch);
                double jzmj_ = GetJZMJ(map_chs.get(ch));
                jzmj += jzmj_;
                map_p.put("CP.JZMJ" + (y + 1), AiUtil.GetValue(jzmj_, "", AiUtil.F_FLOAT2));
                String image_fcfwt = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(zrz) + "附件材料/分层分户图/") + "分层分户图_" + ch + ".jpg";
                map_p.put("img.fcfht" + (y + 1), image_fcfwt);

                if (StringUtil.IsEmpty(ch1)) {
                    ch1 = ch;
                }
                i++;
                z++;
                if (z % size == 0) {
                    maps_p.add(map_p);
                    map_p.put("CP.JZMJHZ", AiUtil.GetValue(jzmj, "", AiUtil.F_FLOAT2));
                    map_p.put("CP.ZRZCSHZ", zrzcshz);
                    if (zrzjghz.length() > 0) {
                        map_p.put("CP.ZRZJGHZ", zrzjghz.substring(0, zrzjghz.length() - 1));
                    }
                    map_p = new LinkedHashMap<>();
                    jzmj = 0;

                    if (Integer.parseInt(ch) == map_chs.size()) {
                        zrzcshz = "";
                        zrzjghz = "";
                    } else {
                        String[] lastZh = zrzcshz.split("SYS.ENTER");
                        String[] lastJc = zrzjghz.split(",");
                        zrzcshz = lastZh[lastZh.length - 1];
                        zrzjghz = lastJc[lastJc.length - 1] + ", ";
                    }
                    // 设置初始值
                    for (int i1 = 1; i < size + 1; i++) {
                        map_p.put("CP.MC" + i1, "");
                    }
                } else {
                    int j1 = z % size;
                    // 最后一个
                    if (z == map_chs.size()) {
                        for (int i1 = j1; i1 < size; i1++) {
                            map_p.put("CP.CH" + (i1 + 1), "");
                            map_p.put("CP.JZMJ" + (i1 + 1), "");
                            map_p.put("img.fcfht" + (i1 + 1), "");
                        }
                        map_p.put("CP.JZMJHZ", AiUtil.GetValue(jzmj, "", AiUtil.F_FLOAT2));
                        if (zrzjghz.length() > 0) {
                            map_p.put("CP.ZRZJGHZ", zrzjghz.substring(0, zrzjghz.length() - 1));
                        }
                        map_p.put("CP.ZRZCSHZ", zrzcshz);
                        maps_p.add(map_p);
                        map_p = new LinkedHashMap<>();
                        ch1 = "";
                        jzmj = 0;
                    }
                }
            }
        }
        map_p = new LinkedHashMap<>();
        ch1 = "";
        jzmj = 0;
        // 为幢设置层
        map_.put("list.cps", maps_p);
    }

    public static void Put_data_hgs(Map<String, Object> map_) {
//
//        List<String> z_cs = new ArrayList<>();
//        // 设置幢下面户的分组
//        List<Map<String, Object>> maps_z_hg = new ArrayList<>();
//        int i = 0;
//        for (Feature h : f_hs) {
//            String h_zrzh = AiUtil.GetValue(h.getAttributes().get("ZRZH"), "");
//            String h_ch = AiUtil.GetValue(h.getAttributes().get("CH"), "");
//
//            if (z_zrzh.equals(h_zrzh)) {
//                //设置户
//                Map<String, Object> map_z_h = new LinkedHashMap<>();
//                getReplecData(map_z_h, h, nullValue, "YT:fwyt", "FWJG", "FWXZ", "QLRZJZL:zjzl", "ZT:fwzt");
//                // 户添加到户列表里面
//                maps_z_hs.add(map_z_h);
//                if (i == 0) {
//                    map_zrz.putAll(map_z_h);
//                }
//                i++;
//
//                if (StringUtil.IsNotEmpty(h_ch)) {
//                    String h_zhid = h_zrzh + "#" + map_z_h.get("H.FWJG") + "#" + map_z_h.get("H.QLRXM") + "#" + map_z_h.get("H.QLRZJZL");
//                    // 户添加到层列表
//                    if (!z_cs.contains(h_ch)) {
//                        z_cs.add(h_ch);
//                        // 设置层的属性
//                        Map<String, Object> map_z_c = new LinkedHashMap<>();
//                        map_z_c.put("C.CH", h_ch);
//                        String image_fcfwt = FileUtils.getAppDirAndMK(getpath_root(zrz) + "附件材料/分层分户图/") + "分层分户图_" + h_ch + ".jpg";
//                        map_z_c.put("img.fcfht", image_fcfwt);
//
//                        maps_z_cs.add(map_z_c);
//                    }
//                    // 户添加到组列表
//                    Map<String, Object> map_h_g = new LinkedHashMap<>();
//                    Map<String, Map<String, Object>> maps_hg_keys = new LinkedHashMap<>();
//                    if (maps_hg_keys.keySet().contains(h_zhid)) {
//                        map_h_g = maps_hg_keys.get(h_zhid);
//                        List<String> szlc = new ArrayList<String>(Arrays.asList(AiUtil.GetValue(map_h_g.get("H.SZLC"), "").split(",")));
//                        if (!szlc.contains(h_ch)) {
//                            szlc.add(h_ch);
//                            Collections.sort(szlc, new Comparator<String>() {
//                                @Override
//                                public int compare(String o1, String o2) {
//                                    try {
//                                        Integer int1 = Integer.valueOf(o1);
//                                        Integer int2 = Integer.valueOf(o2);
//                                        return int1.compareTo(int2);
//                                    } catch (Exception e) {
//                                    }
//                                    return o1.compareTo(o2);
//                                }
//                            });
//                            String szlc_s = StringUtil.Join(szlc);
//                            map_h_g.put("H.SZLC", szlc_s);
//                        }
//
//                    } else {
//                        map_h_g.put("H.SZLC", h_ch);
//                        map_h_g.put("H.YBH", String.format("%03d%n", maps_hg_keys.size() + 1));
//                        map_h_g.putAll(map_z_h);
//                        maps_z_hg.add(map_h_g);
//                        maps_hg_keys.put(h_zhid, map_h_g);
//                    }
//                }
//            }
//        }
//        // 为幢设置户
//        map_zrz.put("list.hs", maps_z_hs);
//        // 为幢设置层
//        map_zrz.put("list.cs", maps_z_cs);
//        // 为幢设置层
//        map_zrz.put("list.hg", maps_z_hg);
////                                                                       for (Feature f : f_hs) {
////                                                                           Map<String, Object> map_h = new HashMap<>();
////                                                                           getReplecData(map_h, f, nullValue, "YT:fwyt", "FWJG", "QLRZJZL:zjzl", "ZT:fwzt");
////                                                                           String key = map_h.get("H.ZRZH") + "#" + map_h.get("H.FWJG") + "#" + map_h.get("H.QLRXM") + "#" + map_h.get("H.QLRZJZL");
////                                                                           String value = AiUtil.GetValue(map_h.get("H.CH"), "");
////                                                                           if (maps_hg_keys.keySet().contains(key)) {
////                                                                               Map<String, Object> map_h_ = maps_hg_keys.get(key);
////                                                                               if (StringUtil.IsNotEmpty(value)) {
////                                                                                   List<String> szlc = new ArrayList<String>(Arrays.asList(AiUtil.GetValue(map_h_.get("H.SZLC"), "").split(",")));
////                                                                                   if (!szlc.contains(value)) {
////                                                                                       szlc.add(value);
////                                                                                       Collections.sort(szlc, new Comparator<String>() {
////                                                                                           @Override
////                                                                                           public int compare(String o1, String o2) {
////                                                                                               try {
////                                                                                                   Integer int1 = Integer.valueOf(o1);
////                                                                                                   Integer int2 = Integer.valueOf(o2);
////                                                                                                   return int1.compareTo(int2);
////                                                                                               } catch (Exception e) {
////                                                                                               }
////                                                                                               return o1.compareTo(o2);
////                                                                                           }
////                                                                                       });
////                                                                                       String szlc_s = StringUtil.Join(szlc);
////                                                                                       map_h_.put("H.SZLC", szlc_s);
////                                                                                   }
////                                                                               }
////                                                                           } else {
////                                                                               map_h.put("H.SZLC", value);
////                                                                               map_h.put("H.YBH", String.format("%03d%n", maps_hg_keys.size() + 1));
////                                                                               maps_hg.add(map_h);
////                                                                               maps_hg_keys.put(key, map_h);
////                                                                           }
////                                                                       }
////                                                                       map_zrz.put("list.hg", maps_hg);
////
////                                                                       maps_zrz.add(map_zrz);
    }

    public static Map<String, List<Feature>> GetCbyZrz(String zrzh, List<Feature> fs_z_fsjg, List<Feature> fs_h) {
        Map<String, List<Feature>> cs = new LinkedHashMap<>();

        // 获取户
        if (fs_h.size() > 0) {
            for (Feature f_h : fs_h) {
                String h_zrzh = AiUtil.GetValue(f_h.getAttributes().get("ZRZH"), "");
                String h_ch = AiUtil.GetValue(f_h.getAttributes().get("SZC"), "");
                if (h_zrzh.equals(zrzh)) {
                    List fs = cs.get(h_ch);
                    if (fs == null) {
                        fs = new ArrayList();
                        cs.put(h_ch, fs);
                    }
                    fs.add(f_h);
                }
            }
        }
        // 获取附属
        if (fs_z_fsjg.size() > 0) {
            for (Feature f : fs_z_fsjg) {
                String f_zid = AiUtil.GetValue(f.getAttributes().get("ZID"), "");
                String f_ch = AiUtil.GetValue(f.getAttributes().get("LC"), "");
                if (f_zid.equals(zrzh)) {
                    List fs = cs.get(f_ch);
                    if (fs == null) {
                        fs = new ArrayList();
                        cs.put(f_ch, fs);
                    }
                    fs.add(f);
                }
            }
        }

        if (cs.size() < 1) {
            cs.put("1", new ArrayList<Feature>());
        }
        return cs;
    }

    public static double GetJZMJ(List<Feature> fs) {
        double scjzmj_count = 0;
        for (Feature f : fs) {
            // 户
            scjzmj_count += AiUtil.GetValue(f.getAttributes().get("SCJZMJ"), 0d);
            // 幢的附属
            scjzmj_count += AiUtil.GetValue(f.getAttributes().get("HSMJ"), 0d);
        }
        return scjzmj_count;
    }


    public static List<Feature> GetHbyZrzh(MapInstance mapInstance, String zrzh, List<Feature> fs_h) {
        List<Feature> hs = new ArrayList<>();
        if (fs_h.size() > 0) {
            for (Feature f_h : fs_h) {
                String h_zrzh = AiUtil.GetValue(f_h.getAttributes().get("ZRZH"), "");
                if (h_zrzh.equals(zrzh)) {
                    hs.add(f_h);
                }
            }
        }
        if (hs.size() < 1) {
            hs.add(GetTable(mapInstance, FeatureHelper.TABLE_NAME_H).createFeature());
        }
        return hs;
    }

    public static void MoveToEnd(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map != null && map.keySet().contains(key)) {
                Object value = map.get(key);
                map.remove(key);
                map.put(key, value);
            }
        }
    }

    // 字典里面需要有值来替换
    public static Map<String, Object> GetReplecDataDic(Context context, Map<String, Object> map_, Feature f, String... keys) {
        if (f != null) {
            for (String key : keys) {
                if (StringUtil.IsNotEmpty(key)) {
                    String[] k_s = key.split(":");
                    String k = f.getFeatureTable().getTableName() + "." + k_s[0] + "FF";
                    String v = AiUtil.GetValue(dic(context, f, k_s[0], k_s[k_s.length < 2 ? 0 : 1]), "");
                    if (v.contains("]")) {
                        v = v.substring(v.indexOf("]") + 1);
                    }
                    map_.put(k, v);
//                    if(k_s.length>2&&"all".equals( k_s[2])){
//                       Map<String ,String > map =  dic(activity,k_s[1]);
//
//                    }else {
//
//                    }
                }
            }
        }
        return map_;
    }

    // 设置替换，且还有字典里面的值
    public static Map<String, Object> GetReplecData(Context context, Map<String, Object> map_, String key, List<Feature> fs, String nullValue, String... keys) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Feature f : fs) {
            Map<String, Object> v = GetReplecData(context, new LinkedHashMap<String, Object>(), f, nullValue, keys);
            maps.add(v);
        }
        map_.put(key, maps);
        return map_;
    }

    // 设置替换，且还有字典里面的值
    public static Map<String, Object> GetReplecData(Context context, Map<String, Object> map_, Feature f, String nullValue, String... keys) {
        if (f != null) {
            String key = f.getFeatureTable().getTableName();
            map_ = GetReplecDataDic(context, map_, f, keys);
            return GetReplecData(map_, key, f.getAttributes(), nullValue);
        }
        return map_;
    }

    // 设置替换值，有空值替换，会设置1 遍
    public static Map<String, Object> GetReplecData(Map<String, Object> map_, String key, Map<String, Object> map, String nullValue) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String k = (StringUtil.IsEmpty(key) ? "" : (key + ".")) + entry.getKey();
            // 时间和小数数字保留两位小数
            String v = AiUtil.GetValue(entry.getValue(), nullValue, AiUtil.F_DATE, AiUtil.F_FLOAT2);
            map_.put(k, v);
        }
        return map_;
    }

    // 【谨慎调用】设置替换值，没有空值替换，会设置2 遍
    public static Map<String, Object> GetReplecData(Map<String, Object> map_, String key, Map<String, Object> map) {
        // 值不一样，满足空数据时划不划线要求
        GetReplecData(map_, key, map, "/");
        GetReplecData(map_, key + "_", map, "");
        return map_;
    }


    //    public Map<String, Object> getReplecData(Map<String, Object> map_, String key, List<Feature> zrzs,List<Feature> hs,String nullValue, String... keys) {
//        List<Map<String, Object>> maps = new ArrayList<>();
//        for (Feature z : zrzs) {
//            Map<String, Object> v = getReplecData(new LinkedHashMap<String, Object>(), f, nullValue,keys);
//            maps.add(v);
//        }
//        map_.put(key, maps);
//        return map_;
//    }

    //endregion


    //region 属性页


    public static void OutputData(final MapInstance mapInstance,
                                  final String bdcdyh,
                                  final Feature f_zd,
                                  final List<Feature> fs_jzd,
                                  final List<Feature> fs_jzx,
                                  final List<Feature> fs_zrz,
                                  final List<Feature> fs_z_fsjg,
                                  final List<Feature> fs_h,
                                  final List<Feature> fs_h_fsjg) {
        try {
            final String file_dcb = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + "不动产地籍调查表" + bdcdyh + ".docx";
            FileUtils.copyFile(GetPath_BDC_doc(mapInstance, bdcdyh), file_dcb);
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

            final String dxf_fcfht = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + bdcdyh + "分层分户图.dxf"; //20180709
            new DxfFcfwh(mapInstance).set(dxf_fcfht).set(FeatureHelper.Get(fs_zrz.get(0), FeatureHelper.TABLE_ATTR_BDCDYH, bdcdyh), f_zd, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg).write().save();

        } catch (Exception es) {
            Log.e(TAG, "导出数据失败", es);
        }
    }

    public static void OutputDataZD(final MapInstance mapInstance,
                                    final String bdcdyh,
                                    final Feature f_zd,
                                    final List<Feature> fs_jzd,
                                    final List<Feature> fs_jzx,
                                    final List<Feature> fs_zrz,
                                    final List<Feature> fs_z_fsjg,
                                    final List<Feature> fs_h,
                                    final List<Feature> fs_h_fsjg) {
        try {
            final String file_dcb = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + "不动产地籍调查表" + bdcdyh + ".docx";
            FileUtils.copyFile(GetPath_BDC_doc(mapInstance, bdcdyh), file_dcb);
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

            final String dxf_fcfht = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + FeatureHelper.FJCL) + bdcdyh + "分层分户图.dxf"; //20180709
            new DxfFcfwh(mapInstance).set(dxf_fcfht).set(FeatureHelper.Get(fs_zrz.get(0), FeatureHelper.TABLE_ATTR_BDCDYH, bdcdyh), f_zd, fs_zrz, fs_z_fsjg, fs_h, fs_h_fsjg).write().save();

        } catch (Exception es) {
            Log.e(TAG, "导出数据失败", es);
        }
    }

    //  分享
    public void shareDialog(final Activity activity) {
        LoadZD(mapInstance, id, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Feature f_zd = (Feature) t_;
                if (f_zd != null) {
                    AlertDialog.Builder builder = DialogBuilder.createAlertDialogBuilder(activity);
                    final View view = View.inflate(activity, R.layout.app_ui_ai_map_feature_bdc_share, null);
                    final Dialog dialog = builder.setView(view).setCancelable(true).show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    final String file_dir = GetPath_ZD_dir(mapInstance, f_zd);
                    final String file_zip = GetPath_ZD_zip(mapInstance, f_zd);


                    // 打开目录
                    view.findViewById(R.id.ll_toDir).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FileUtils.openFile(activity, file_dir, true);
                        }
                    });
                    // 发送到服务器
                    view.findViewById(R.id.ll_toServer).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ToastMessage.Send(activity, "发送到服务器...");
                        }
                    });
                    // 发送到QQ
                    view.findViewById(R.id.ll_toQQ).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedUtils.shareFile(activity, "QQ", "发送到QQ好友", zddm, "", file_zip, true);
                        }
                    });
                    // 发送到微信
                    view.findViewById(R.id.ll_toWX).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedUtils.shareFile(activity, "微信", "发送到微信好友", zddm, "", file_zip, true);
                        }
                    });
                }
                return null;
            }
        });

    }


    public static void BuildView_BDC(final MapInstance mapInstance, final ListView lv_list, final Feature qlr, final int deep) {

        if (lv_list.getAdapter() == null) {
            QuickAdapter<Feature> adpter = new QuickAdapter<Feature>(mapInstance.activity, R.layout.app_ui_ai_aimap_bdc_item, new ArrayList<Feature>()) {
                @Override
                protected void convert(final BaseAdapterHelper helper, final Feature item) {
                    final String id = AiUtil.GetValue(item.getAttributes().get(FeatureHelper.TABLE_ATTR_ORID_PATH), "");
                    String zddm = AiUtil.GetValue(item.getAttributes().get(FeatureHelper.TABLE_ATTR_ZDDM), "");

                    final String name = id.length() > 19 ? id.substring(19) : id;
                    String desc = zddm.length() > 12 ? zddm.substring(12) : id;

                    int color = Color.BLUE;
                    if (FeatureHelper.TABLE_NAME_ZD.equals(item.getFeatureTable().getTableName())) {
                        desc += AiUtil.GetValue(item.getAttributes().get("ZL"), "") + AiUtil.GetValue(item.getAttributes().get("LPMC"), "");
                        color = Color.RED;
                    } else if (FeatureHelper.TABLE_NAME_H.equals(item.getFeatureTable().getTableName())) {
                        desc += AiUtil.GetValue(item.getAttributes().get("ZL"), "") + AiUtil.GetValue(item.getAttributes().get("HH"), "");
                        color = Color.BLUE;
                    } else {
                        desc += desc;
                    }

//                    final ListView lv_list_item = (ListView) helper.getView(R.id.lv_list_item);
                    helper.setText(R.id.tv_name, name);
                    helper.setText(R.id.tv_desc, desc);

                    int s = (int) (deep * mapInstance.activity.getResources().getDimension(R.dimen.app_size_smaller));
                    helper.getView(R.id.v_split).getLayoutParams().width = s;

                    Bitmap bm = MapHelper.geometry_icon(item.getGeometry(), 100, 100, color, 5);
                    if (bm != null) {
                        helper.setImageBitmap(R.id.v_icon, bm);
                    } else {
                        helper.setImageResource(R.id.v_icon, R.mipmap.app_map_layer_zrz);
                    }
                    helper.getView(R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mapInstance.viewFeature(item);
                        }
                    });

                    helper.getView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MapHelper.selectAddCenterFeature(mapInstance.map, item);
                        }
                    });
                }
            };
            lv_list.setAdapter(adpter);
        }
        String value = AiUtil.GetValue(qlr.getAttributes().get("ZJH"), "");
        String where = "";
        if (StringUtil.IsNotEmpty(value)) {
            where = " QLRZJH = '" + value + "' ";
        }
        value = AiUtil.GetValue(qlr.getAttributes().get("QLRDM"), "");
        if (StringUtil.IsNotEmpty(value)) {
            where = where + (StringUtil.IsNotEmpty(where) ? " or " : "") + " QLRDM = '" + value + "' ";
        }
        value = AiUtil.GetValue(qlr.getAttributes().get("XM"), "");
        if (StringUtil.IsNotEmpty(value)) {
            where = where + (StringUtil.IsNotEmpty(where) ? " or " : "") + " QLRXM = '" + value + "' ";
        }
        final String where_ = where;
        final List<Feature> features = new ArrayList<Feature>();
        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZD, FeatureHelper.LAYER_NAME_ZD), where_, 0, features, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_H, FeatureHelper.LAYER_NAME_H), "(" + where_ + ") and BDCDYH not like '%99990001' ", 0, features, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        ((QuickAdapter<Feature>) lv_list.getAdapter()).replaceAll(features);
                        return null;
                    }
                });
                return null;
            }
        });
    }

    //创建出excel资料的选择列表 20180822
    public static View buildSelectExcel(final MapInstance mapInstance, final List<String> select_items, final List<String> selected_items, final AiRunnable callback) {
        try {
            final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
            final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
            final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
            if (ll_view.getChildCount() > 0) {
                ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
            }

            QuickAdapter<String> adapter = new QuickAdapter<String>(mapInstance.activity, listItemRes, select_items) {
                @Override
                protected void convert(final BaseAdapterHelper helper, final String item) {
                    helper.getView(R.id.cb_select).setVisibility(View.VISIBLE);
                    helper.getView(R.id.iv_position).setVisibility(View.GONE);
                    helper.getView(R.id.v_icon).setVisibility(View.GONE);
                    helper.getView(R.id.iv_detial).setVisibility(View.GONE);

                    helper.setText(R.id.tv_name, item);

                    //选中、反选
                    final CheckBox cb_select = helper.getView(com.ovit.R.id.cb_select);
                    cb_select.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (cb_select.isChecked()) {
                                if (selected_items.contains(item)) {

                                } else {
                                    selected_items.add(item);
                                }
                            } else {
                                if (selected_items.contains(item)) {
                                    selected_items.remove(item);
                                } else {

                                }
                            }
                        }
                    });
                }
            };

            ll_list_item.setTag(adapter);
            adapter.adpter(ll_list_item);
            adapter.notifyDataSetChanged();
            return ll_view;
        } catch (Exception es) {
            Log.e(TAG, "创建出excel资料的选择列表失败!" + es);
            return null;
        }
    }

    public static void outputSondyExcel(final MapInstance mapInstance, final Feature feature_zrz, final AiRunnable callback) {
        final List<List<Feature>> values = new ArrayList<>();
        List<Feature> f_zd = new ArrayList<>();
        final List<Feature> fs_h = new ArrayList<>();
        final List<Feature> fs_zrz = new ArrayList<>();
        final List<Feature> fs_zd = new ArrayList<>();
        MapHelper.QueryOne(MapHelper.getTable(mapInstance.map, FeatureHelper.TABLE_NAME_ZD, FeatureHelper.LAYER_NAME_ZD), "ZDDM='" + FeatureHelper.Get(feature_zrz, FeatureHelper.TABLE_ATTR_ZDDM) + "'", new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final Feature f_zd = (Feature) objects[0];
                MapHelper.Query(MapHelper.getTable(mapInstance.map, FeatureHelper.TABLE_NAME_H, FeatureHelper.LAYER_NAME_H), "ZDDM='" + FeatureHelper.Get(feature_zrz, FeatureHelper.TABLE_ATTR_ZDDM) + "'", -1, fs_h, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        fs_zd.add(f_zd);
                        fs_zrz.add(feature_zrz);
                        values.add(fs_zd);
                        values.add(fs_zrz);
                        values.add(fs_h);
                        Excel.CreateSondyExcelToGdal(feature_zrz, FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature_zrz) + "附件材料/表格/") + AiUtil.GetValue(feature_zrz.getAttributes().get("ZRZH"), "") + ".xls", values);
                        AiRunnable.Ok(callback, true, true);
                        return null;
                    }
                });
                return null;
            }
        });
    }

    public static void outputSouthExcel(final MapInstance mapInstance, final Feature feature_h, final AiRunnable callback) {
        final List<List<Feature>> values = new ArrayList<>();
        final List<Feature> fs_h = new ArrayList<>();
        final List<Feature> fs_zrz = new ArrayList<>();
        final List<Feature> fs_zd = new ArrayList<>();
        Feature f_zd = null;
        final Feature f_zrz = null;
        MapHelper.QueryOne(MapHelper.getTable(mapInstance.map, FeatureHelper.TABLE_NAME_ZD, FeatureHelper.LAYER_NAME_ZD), "ZDDM='" + FeatureHelper.Get(feature_h, FeatureHelper.TABLE_ATTR_ZDDM) + "'", new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final Feature f_zd = (Feature) objects[0];
                MapHelper.QueryOne(MapHelper.getTable(mapInstance.map, FeatureHelper.TABLE_NAME_ZRZ, FeatureHelper.LAYER_NAME_ZRZ), "ZRZH='" + FeatureHelper.Get(feature_h, "ZRZH") + "'", new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        fs_zd.add(f_zd);
                        fs_zrz.add((Feature) objects[0]);
                        fs_h.add(feature_h);
                        values.add(fs_zd);
                        values.add(fs_zrz);
                        values.add(fs_h);
                        Excel.CreateSouthExcelToGdal(feature_h, FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature_h) + "附件材料/表格/") + AiUtil.GetValue(feature_h.getAttributes().get("ID"), "") + ".xls", values);
                        AiRunnable.Ok(callback, true, true);
                        return null;
                    }
                });
                return null;
            }
        });
    }

    //处理处Excel资料的后续操作 20180822
    public static void completeSelectExcel(final MapInstance mapInstance, final Feature feature_zd, final List<String> selected_items, final AiRunnable callback) { //在这里处理用户选择
        try {
            if (selected_items.size() > 0) {
                final String bdcdyh = FeatureHelper.Get(feature_zd, FeatureHelper.TABLE_ATTR_BDCDYH) + "";
                final List<Feature> features_ = new ArrayList<>();
                new AiForEach<String>(selected_items, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        AiRunnable.Ok(callback, true, true);
                        return null;
                    }
                }) {
                    public void exec() {
                        switch (selected_items.get(postion)) {
                            case "中地不动产数据": {
                                LoadZRZ(mapInstance, bdcdyh, features_, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        if (features_.size() > 0) {
                                            new AiForEach<Feature>(features_, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    features_.clear();
                                                    AiRunnable.Ok(getNext(), true, true);
                                                    return null;
                                                }
                                            }) {
                                                public void exec() {
                                                    outputSondyExcel(mapInstance, features_.get(postion), getNext());
                                                }
                                            }.start();
                                        } else {
                                            AiRunnable.Ok(getNext(), true, true);
                                        }
                                        return null;
                                    }
                                });
                            }
                            case "南方不动产数据": {
                                LoadH(mapInstance, bdcdyh, features_, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        if (features_.size() > 0) {
                                            new AiForEach<Feature>(features_, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    features_.clear();
                                                    AiRunnable.Ok(getNext(), true, true);
                                                    return null;
                                                }
                                            }) {
                                                public void exec() {
                                                    outputSouthExcel(mapInstance, features_.get(postion), getNext());
                                                }
                                            }.start();
                                        } else {
                                            AiRunnable.Ok(getNext(), true, true);
                                        }
                                        return null;
                                    }
                                });
                            }
                            default:
                                AiRunnable.Ok(getNext(), true, true);
                        }
                    }
                }.start();
            } else {
                ToastMessage.Send("未选择需要的成果");
            }
        } catch (Exception es) {
            Log.e(TAG, "生成Excel成果失败!" + es);
            AiRunnable.Ok(callback, false, false);
        }
    }

    //出excel资料 20180822
    public void createExcelDialog(final MapInstance mapInstance) {
        LoadZD(mapInstance, GetWhere_dcdyh(id), new AiRunnable() {
            @Override
            public <T_> T_ ok(final T_ t_, Object... objects) {
                if (t_ != null) {
                    final List<String> select_items = new ArrayList<>();
                    final List<String> selected_items = new ArrayList<>();
                    //可在select_items中随意添加其他需要输出的不动产数据 与completeSelectExcel保持一致即可
                    select_items.add("中地不动产数据");
                    select_items.add("南方不动产数据");

                    AiDialog dialog = AiDialog.get(mapInstance.activity);
                    dialog.setHeaderView("请选择Excel成果的类型");
                    dialog.addContentView(buildSelectExcel(mapInstance, select_items, selected_items, null));
                    dialog.setFooterView(AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            completeSelectExcel(mapInstance, (Feature) t_, selected_items, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    if ((Boolean) t_) {
                                        ToastMessage.Send("成功为" + id + "生成" + selected_items.size() + "种Excel成果");
                                    } else {
                                        ToastMessage.Send("为" + id + "生成Excel成果失败!");
                                    }
                                    return null;
                                }
                            });
                            dialog.dismiss();
                        }
                    }, null, null, AiDialog.CENCEL, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                } else {
                    ToastMessage.Send("没有找到宗地信息");
                }
                return null;
            }
        });

    }

    //  通过zrz获取层
    public static void LoadAllCAndFsToZRZ(final MapInstance mapInstance
            , final List<Feature> fs_zrz
            , final LinkedHashMap<Feature, List<Feature>> features_c
            , final AiRunnable callback) {

        new AiForEach<Feature>(fs_zrz, callback) {
            @Override
            public void exec() {
                final Feature f_zrz = fs_zrz.get(postion);
                MapHelper.Query(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ_C), "ORID_PATH like '%" + FeatureHelper.Get(f_zrz, FeatureHelper.TABLE_ATTR_ORID) + "%'", new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        List<Feature> featuresC = (List<Feature>) t_;
                        final Map<Integer, Feature> mapC = new HashMap<>();
                        int key;
                        for (Feature featureC : featuresC) {
                            key = (int) featureC.getAttributes().get("SJC");
                            mapC.put(key, featureC);
                        }
                        final ArrayList<Map.Entry<String, List<Feature>>> zrz_c_s = new ArrayList<>();
                        FeatureEditC.Load_FsAndH_GroupbyC_Sort(mapInstance, f_zrz, zrz_c_s, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                for (Map.Entry<String, List<Feature>> zrz_c : zrz_c_s) {
                                    features_c.put(mapC.get(Integer.parseInt(zrz_c.getKey())), zrz_c.getValue());
                                }
                                AiRunnable.Ok(getNext(), t_, objects);
                                return null;
                            }
                        });
                        return null;
                    }
                });

            }
        }.start();
    }

    //  通过c 获取 层所有的
    public static void LoadAllCAndFsToC(final MapInstance mapInstance
            , final Feature f_zrz, final Feature f_c
            , final LinkedHashMap<Feature, List<Feature>> features_c
            , final AiRunnable callback) {
        final ArrayList<Map.Entry<String, List<Feature>>> zrz_c_s = new ArrayList<>();
        FeatureEditC.Load_FsAndH_GroupbyC_Sort(mapInstance, f_zrz, zrz_c_s, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                for (Map.Entry<String, List<Feature>> zrz_c : zrz_c_s) {
                    if ((zrz_c.getKey()).equals(FeatureHelper.Get(f_c, "SZC", ""))) {
                        features_c.put(f_c, zrz_c.getValue());
                    }
                }
                AiRunnable.Ok(callback, features_c, features_c);
                return null;
            }
        });
    }

    //  通过H 获取 层所有的
    public static void LoadAllCAndFsToH(final MapInstance mapInstance
            , final Feature f_zrz, final String szc
            , final LinkedHashMap<Feature, List<Feature>> features_c
            , final AiRunnable callback) {
        MapHelper.QueryOne(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ_C)
                , " SJC=" + szc + " and " + "ORID_PATH like '%" + FeatureHelper.Get(f_zrz, FeatureHelper.TABLE_ATTR_ORID) + "%'"
                , new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        final Feature f_c = (Feature) t_;
                        final ArrayList<Map.Entry<String, List<Feature>>> zrz_c_s = new ArrayList<>();
                        FeatureEditC.Load_FsAndH_GroupbyC_Sort(mapInstance, f_zrz, zrz_c_s, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                for (Map.Entry<String, List<Feature>> zrz_c : zrz_c_s) {
                                    if ((zrz_c.getKey()).equals(FeatureHelper.Get(f_c, "SJC", ""))) {
                                        features_c.put(f_c, zrz_c.getValue());
                                    }
                                }
                                AiRunnable.Ok(callback, features_c, features_c);
                                return null;
                            }
                        });
                        return null;
                    }
                });
    }



}
