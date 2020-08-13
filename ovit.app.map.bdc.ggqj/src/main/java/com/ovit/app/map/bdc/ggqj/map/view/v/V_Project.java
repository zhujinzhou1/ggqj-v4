package com.ovit.app.map.bdc.ggqj.map.view.v;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.core.License;
import com.ovit.app.device.bluetooth.BTClient;
import com.ovit.app.httphandler.pojo.CBXT_PROJECT;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.util.Excel;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditBDC;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditQLR;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditZD;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewGYR;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewH_FSJG;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewLJZ;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewQLR;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZD;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZRZ;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.custom.shape.GpkgUtil;
import com.ovit.app.map.model.LineLabel;
import com.ovit.app.map.model.MapProject;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.DialogBuilder;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.ResourceUtil;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.GdalAdapter;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.cad.DxfFeature;
import com.ovit.app.util.gdal.cad.DxfHelper;
import com.ovit.app.util.gdal.shp.ShpAdapter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jxl.read.biff.BiffException;

import static com.ovit.app.map.view.FeatureEdit.GetTable;

;

/**
 * Created by Lichun on 2017/10/16.
 */

public class V_Project extends com.ovit.app.map.view.V_Project {

    //region 常量
    public final static String STBM = "STBM";
    public final static String GRAPHICS_REPLACE = "图形替换";
    ///endregion

    //region 字段
    MapInstance mapInstance;
    Map<String, String> mapStbm; //实体编码
    ///endregion

    //region 构造函数
    public V_Project(MapInstance mapInstance) {
        super(mapInstance);
        this.mapInstance = mapInstance;
    }
    ///endregion

    //region 重写函数和回调
    ///endregion

    //region 公有函数
    public View getView_Project() {
        CBXT_PROJECT pro_cur = project.getCurrent();
        LinearLayout tool_view = (LinearLayout) LayoutInflater.from(activity).inflate(
                R.layout.app_ui_ai_aimap_project, null);
        ((TextView) tool_view.findViewById(R.id.tv_pro_name)).setText(pro_cur.XMMC);
        ((TextView) tool_view.findViewById(R.id.tv_pro_id)).setText(pro_cur.XMBM);
        // 项目列表
        tool_view.findViewById(R.id.v_pro_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 查看列表
                mapInstance.tool.map_opt_show("map_opt_projectlist", "", "project", getView_ProjectList(), false, true, null);
            }
        });
        //通山  批量写入坐落 2020/07/03
        tool_view.findViewById(R.id.ll_input_zl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputZL((MapInstance) getMapInstance());
            }
        });

        // 导入二调数据
        tool_view.findViewById(R.id.ll_input_cad_edsj).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 导入二调数据
                final String funcdesc = "将两权dxf文件导入到系统！";
                FeatureHelper.vaildfunc(mapInstance, funcdesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        License.vaildfunc(activity, funcdesc, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                show_lqsj_items();
                                return null;
                            }
                        });
                        return null;
                    }
                });
            }
        });
        // 导出二调数据
        tool_view.findViewById(R.id.ll_output_cad_esdj).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fundesc = "生成两权数据！";
                FeatureHelper.vaildfunc(mapInstance, fundesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        output_lqsj(mapInstance);
                        return null;
                    }
                });

            }
        });
        //        批量导出权利人信息
        tool_view.findViewById(R.id.ll_output_qlrxx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output_gyr(mapInstance);
            }
        });
        // 导入权利人
        tool_view.findViewById(R.id.ll_input_excelqlr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fundesc = "导入权利人！";
                FeatureHelper.vaildfunc(mapInstance, fundesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        intput_excels();
                        return null;
                    }
                });
            }
        });
        // 导入不动产单元
        tool_view.findViewById(R.id.ll_input_bdcdy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fundesc = "导入不动产单元！";
                FeatureHelper.vaildfunc(mapInstance, fundesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        input_bdcdy(mapInstance);
                        return null;
                    }
                });
            }
        });
        // 导入不动产单元
        tool_view.findViewById(R.id.ll_zngj_bdcdy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fundesc = "不动产单元与不动产智能挂接！";
                FeatureHelper.vaildfunc(mapInstance, fundesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        zngjBdcFromBdcdy(mapInstance);
                        return null;
                    }
                });
            }
        });
        // 图形替换
        tool_view.findViewById(R.id.ll_txth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fundesc = "图形替换";
                FeatureHelper.vaildfunc(mapInstance, fundesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        mapInstance.tool.layerTool.inputfromshp(true, null);
                        return null;
                    }
                });
            }
        });

        // 自然幢识别宗地
        tool_view.findViewById(R.id.ll_identy_zd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<Feature> fs = new ArrayList<>();
                FeatureViewZRZ.LaodAllZRZ_IdentyZd(mapInstance, fs, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        ToastMessage.Send(activity, "[" + fs.size() + "]自然幢识别宗地成功！");
                        return null;
                    }
                });
            }
        });
        // 户识别自然幢
        tool_view.findViewById(R.id.ll_identy_zrz).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // 识别权利人
        tool_view.findViewById(R.id.ll_identy_qlr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeatureEditQLR.IdentyQlr(mapInstance, null);
            }
        });


        // 智能处理
        tool_view.findViewById(R.id.v_cggl_zlcl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fundesc = "智能处理";
                FeatureHelper.vaildfunc(mapInstance, fundesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Zlcl(mapInstance);
                        return null;
                    }
                });
            }
        });

        // 数据检查
        tool_view.findViewById(R.id.v_cggl_sjjc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 查看列表
                String fundesc = "数据检查";
                FeatureHelper.vaildfunc(mapInstance, fundesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
//                        FeatureEditH.Sjjc_h(mapInstance);
                        Sjjc(mapInstance);
                        return null;
                    }
                });

            }
        });
        // 生成资料
        tool_view.findViewById(R.id.v_cggl_sczl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 查看列表
                String fundesc = "生成资料";
                FeatureHelper.vaildfunc(mapInstance, fundesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Cgsc(mapInstance, false);
                        return null;
                    }
                });

            }
        });

        //图形修复
        tool_view.findViewById(R.id.v_cggl_txxf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fundesc = "图形修复";
                FeatureHelper.vaildfunc(mapInstance, fundesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        txxf(mapInstance, true);
                        return null;
                    }
                });
            }
        });

        //生成台帐
        tool_view.findViewById(R.id.v_cggl_sctz).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fundesc = "生成台账";
                FeatureHelper.vaildfunc(mapInstance, fundesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Sctz(mapInstance, true);
                        return null;
                    }
                });
            }
        });
        //电子签章整理
        tool_view.findViewById(R.id.v_cggl_sign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fundesc = "电子签章归整";
                FeatureHelper.vaildfunc(mapInstance, fundesc, DxfHelper.IsCheckArea, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Sign(mapInstance);
                        return null;
                    }
                });
            }
        });


        // 测距仪
        tool_view.findViewById(R.id.ll_tool_sb_cjy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTClient.Search();
            }
        });
        // 数据同步
        tool_view.findViewById(R.id.ll_tool_sb_rtk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTClient.Search();
            }
        });
        // 数据同步
        tool_view.findViewById(R.id.ll_tool_sb_qzy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_qzy_items();
            }
        });
        // 数据同步
        tool_view.findViewById(R.id.ll_data_tb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapInstance.tool.layerTool.updatatoserver(null);
            }
        });

        // 数据备份
        tool_view.findViewById(R.id.ll_data_backup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gpkgPath = FileUtils.getAppDirAndMK(mapInstance.getpath_root()) + "defult.gpkg";
                GpkgUtil.readGpkg(gpkgPath, mapInstance.getLayer(FeatureHelper.TABLE_NAME_ZD), 10, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        return null;
                    }
                });
            }
        });

        // 数据恢复
        tool_view.findViewById(R.id.ll_data_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return tool_view;
    }

    private void Sign(final MapInstance mapInstance) {
        {
            final String funcdesc = "该功能将逐一对项目中每宗地电子签章 进行整理。";
            License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    final AiDialog aidialog = AiDialog.get(mapInstance.activity);
                    aidialog.setHeaderView(R.mipmap.app_icon_dangan_blue, "整理资料");
                    aidialog.setContentView("注意：属于不可逆操作，如果您已经整理过成果，请注意备份谨慎处理！", funcdesc);
                    aidialog.setFooterView("取消", "确定，我要继续", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 完成后的回掉
                            final AiRunnable callback = new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    aidialog.addContentView("处理数据完成。" + t_);
                                    aidialog.setFooterView(null, "关闭", null);
                                    return null;
                                }

                                @Override
                                public <T_> T_ no(T_ t_, Object... objects) {
                                    aidialog.addContentView("处理数据失败！");
                                    aidialog.setFooterView(null, "关闭", null);
                                    return null;
                                }

                                @Override
                                public <T_> T_ error(T_ t_, Object... objects) {
                                    aidialog.addContentView("处理数据异常！");
                                    aidialog.setFooterView(null, "关闭", null);
                                    return null;
                                }
                            };
                            // 设置不可中断
                            aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                            aidialog.setContentView("开始处理数据");
                            aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有不动产单元，并生成资料");
                            final List<Feature> fs_zd = new ArrayList<>();
                            FeatureViewZD.LoadAllZD(mapInstance, fs_zd, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    final int size = fs_zd.size();
                                    new AiForEach<Feature>(fs_zd, null) {
                                        @Override
                                        public void exec() {
                                            Feature f = fs_zd.get(postion);
                                            if ((postion + 1) % 100 == 0) {
                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "电子签章数据规整进度" + (postion + 1) + "/" + size);
                                            }
                                            FeatureViewZD fv = FeatureViewZD.From(mapInstance, f);
                                            fv.signNeaten(f, getNext());
                                        }

                                        @Override
                                        public void complet() {
                                            AiRunnable.Ok(callback, size);
                                        }
                                    }.start();
                                    return null;
                                }
                            });
                        }
                    }).show();
                    return null;
                }
            });
        }
    }

    public void inputZL(final MapInstance mapInstance) {
        LinearLayout v_d = (LinearLayout) LayoutInflater.from(activity).inflate(
                R.layout.app_ui_ai_dialog_edit, null);
        final EditText et_name = (EditText) v_d.findViewById(R.id.et_name);

        final Dialog dialog = new AlertDialog.Builder(activity).setView(v_d).create();
        v_d.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        v_d.findViewById(R.id.btn_enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String zl = et_name.getText().toString().trim();
                if (TextUtils.isEmpty(zl)) {
                    dialog.dismiss();
                    Toast.makeText(activity, "请输入坐落...", Toast.LENGTH_SHORT).show();
                    return;
                }
                final List<Feature> fs_zd = new ArrayList<>();
                final List<Feature> update_fs_zd = new ArrayList<>();
                MapHelper.Query(FeatureEdit.GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZD, FeatureHelper.LAYER_NAME_ZD), "", MapHelper.QUERY_LENGTH_MAX, fs_zd, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Toast.makeText(activity, "正在导入数据...", Toast.LENGTH_SHORT).show();
                        for (Feature f_zd : fs_zd) {

                            String f_zl = FeatureHelper.Get(f_zd, "ZL", "");
                            if (DxfHelper.TYPE == DxfHelper.TYPE_JINSAN && !TextUtils.isEmpty(f_zl)) {

                            } else if (!TextUtils.isEmpty(f_zl)) {
                                continue;
                            }

                            String zddm = FeatureHelper.Get(f_zd, "ZDDM", "");
                            String group = "";
                            String A = "";
                            if (zddm.contains("JC")) {
                                group = zddm.substring(zddm.indexOf("JC") + 2, zddm.indexOf("JC") + 4) + "组";
                                A = zddm.substring(zddm.indexOf("JC") + 2, zddm.indexOf("JC") + 4);
                            } else if (zddm.contains("JB")) {
                                group = zddm.substring(zddm.indexOf("JB") + 2, zddm.indexOf("JB") + 4) + "组";
                                A = zddm.substring(zddm.indexOf("JB") + 2, zddm.indexOf("JB") + 4);
                            } else if (zddm.contains("GB")) {
                                group = zddm.substring(zddm.indexOf("GB") + 2, zddm.indexOf("GB") + 4) + "组";
                                A = zddm.substring(zddm.indexOf("GB") + 2, zddm.indexOf("GB") + 4);
                            }
                            if (!TextUtils.isEmpty(group)) {
                                group = group.indexOf("0") == 0 ? group.substring(1) : group;
                                if (DxfHelper.TYPE == DxfHelper.TYPE_XIANAN || DxfHelper.TYPE == DxfHelper.TYPE_TONGSHAN) {
                                    String B;
                                    A = A.indexOf("0") == 0 ? A.substring(1) : A;
                                    int q = Integer.parseInt(A);
                                    String[] i = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十", "二十一", "二十二", "二十三", "二十四", "二十五"};
                                    for (int j = 0; j <= i.length; j++) {
                                        if (q == j) {
                                            B = i[j - 1];
                                            group = B + "组";
                                        }
                                    }
                                }
                                FeatureHelper.Set(f_zd, "ZL", zl + group);
                                update_fs_zd.add(f_zd);
                            } else {
                                FeatureHelper.Set(f_zd, "ZL", zl);
                                update_fs_zd.add(f_zd);
                            }

                        }
                        MapHelper.updateFeature(update_fs_zd, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                dialog.dismiss();
                                Toast.makeText(activity, "坐落批量写入完成", Toast.LENGTH_SHORT).show();
                                return null;
                            }
                        });
                        return null;
                    }
                });
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void output_gyr(final MapInstance mapInstance) {
        final String funcdesc = "该功能将逐步导出家庭成员户籍信息表！";
        License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final AiDialog aidialog = AiDialog.get(mapInstance.activity);
                aidialog.setHeaderView(R.mipmap.app_icon_dangan_blue, "导出Excel")
                        .setContentView("注意：属于不可逆操作，如果您已经整理过成果，请注意备份谨慎处理！", funcdesc)
                        .setFooterView("取消", "确定，我要继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 完成后的回掉
                                final AiRunnable callback = new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据完成。");
                                        aidialog.setFooterView(null, "关闭", null);
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ no(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据失败！");
                                        aidialog.setFooterView(null, "关闭", null);
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ error(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据异常！");
                                        aidialog.setFooterView(null, "关闭", null);
                                        return null;
                                    }
                                };
                                // 设置不可中断
                                aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                                aidialog.setContentView("开始处理数据");
                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有权利人与家庭成员");
                                {
                                    final List<Feature> fs_qlr = new ArrayList<>();
                                    final List<Feature> fs = new ArrayList<>();
                                    final List<Feature> hjxx = new ArrayList<>();

                                    MapHelper.Query(mapInstance.getTable(FeatureHelper.LAYER_NAME_GYRXX, FeatureHelper.LAYER_NAME_GYRXX), "", MapHelper.QUERY_LENGTH_MAX, fs_qlr, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            if (FeatureHelper.isExistElement(fs_qlr)) {
                                                new AiForEach<Feature>(fs_qlr, null) {
                                                    @Override
                                                    public void exec() {
                                                        Feature f_qlr = fs_qlr.get(postion);
                                                        fs.add(f_qlr);
                                                        FeatureViewGYR fv = (FeatureViewGYR) mapInstance.newFeatureView(f_qlr);
                                                        fv.queryChildFeature(FeatureHelper.TABLE_NAME_HJXX, f_qlr, hjxx, new AiRunnable() {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                if (FeatureHelper.isExistElement(hjxx)) {
                                                                    fs.addAll(hjxx);
                                                                    hjxx.clear();
                                                                }
                                                                AiRunnable.Ok(getNext(), t_, objects);
                                                                return null;
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void complet() {
                                                        String xmmc = GsonUtil.GetValue(aiMap.JsonData, "XMMC", "");
                                                        String xmbm = GsonUtil.GetValue(aiMap.JsonData, "XMBM", "");
                                                        String name = fs.get(0).getFeatureTable().getFeatureLayer().getName();
                                                        final String filePath = FileUtils.getAppDirAndMK(getMapInstance().getpath_root()) + xmbm + xmmc + name + ".xls";
                                                        Excel.CreateStandingBookToLayer(mapInstance, filePath, fs);
                                                        AiRunnable.Ok(callback, null);
                                                    }

                                                }.start();
                                            } else {
                                                AiRunnable.Ok(callback, null);
                                            }
                                            return null;
                                        }
                                    });
                                }
                            }
                        }).show();
                return null;
            }
        });
    }

    /**
     * 项目管理
     *
     * @return
     */
    public View getView_ProjectList() {
        return MapProject.ListView(mapInstance);
    }

    public String getName() {
        return project.getCurrent().NAME;
    }

    public String getId() {
        return project.getCurrent().ID;
    }

/*    // 智能处理
    public static void Zlcl(final MapInstance mapInstance) {
        final String funcdesc = "该功能将逐一对项目中所有宗地进行处理："
                + "\n 1、宗地范围内幢、户、附属等自动识别；"
                + "\n 2、重新核算建筑面积；"
                + "\n 3、重新生成宗地草图、房产图、分层分户图。";
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
                                                Cgsc(mapInstance, true);
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
//                                aidialog.setCancelable(false).setFooterView("正在数据处理，可能需要一段时间，暂时不允许操作！");
                                aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                                aidialog.setContentView("开始处理数据");

                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有宗地，并识别幢");
                                Log.d(TAG, "智能处理:查找所有宗地，并识别幢");
                                FeatureViewZD.LaodAllZD_IdentyZrz(mapInstance, new AiRunnable(callback) {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "宗地。");
                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有幢，并识别户、幢附属");

//                                        Log.d(TAG, "智能处理:查找所有幢，并识别户、幢附属");
//                                        FeatureEditZRZ.LaodAllZRZ_IdentyHAndZFSJG(mapInstance, new AiRunnable(callback) {
//                                            @Override
//                                            public <T_> T_ ok(T_ t_, Object... objects) {
//                                                aidialog.addContentView(null,AiUtil.GetValue(new Date(),AiUtil.F_TIME)+" 已完成"+objects[0]+"幢。");
//                                                aidialog.addContentView(null,AiUtil.GetValue(new Date(),AiUtil.F_TIME)+" 查找所有户，并识别户附属");
                                        Log.d(TAG, "智能处理:查找所有户，并识别户附属");
                                        FeatureEditH.LaodAllH_IdentyHFSJG(mapInstance, new AiRunnable(callback) {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "户。");
                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有宗地，重新算建筑面积");
                                                Log.d(TAG, "智能处理:查找所有宗地，重新算建筑面积");
                                                FeatureViewZD.LaodAllZDAndUpdateArea(mapInstance, new AiRunnable(callback) {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "宗地。");
                                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有幢，重新生成分层分户图");
                                                        Log.d(TAG, "智能处理:查找所有幢，重新生成分层分户图");
                                                        FeatureViewZRZ.LaodAllZRZ_CreateFCFHT(mapInstance, new AiRunnable(callback) {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "幢。");
                                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有宗地，重新生成宗地草图、房产图");
                                                                Log.d(TAG, "智能处理:查找所有宗地，重新生成宗地草图、房产图");
                                                                FeatureViewZD.LaodAllZDCreateCTAddFCT(mapInstance, new AiRunnable(callback) {
                                                                    @Override
                                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "宗。");
                                                                        Log.d(TAG, "智能处理:已完成" + objects[0] + "宗。");
                                                                        AiRunnable.Ok(callback, null);
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
//                                                return  null;
//                                            }
//                                        });
                                        return null;
                                    }
                                });


                            }
                        }).show();
                ;

                return null;
            }
        });
    }*/


    // 智能处理
    public static void Zlcl(final MapInstance mapInstance) {
        final String funcdesc = "该功能将逐一对项目中所有宗地进行处理："
                + "\n 1、宗地范围内幢、户、附属等自动识别；"
                + "\n 2、重新核算建筑面积；"
                + "\n 3、重新生成宗地草图、房产图、分层分户图。";
        License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final AiDialog aidialog = AiDialog.get(mapInstance.activity);
                aidialog.setHeaderView(R.mipmap.app_icon_rgzl_blue, "智能处理");
                aidialog.setContentView("注意：属于不可逆操作，将对宗地面积、宗地草图、分层分幅图重新智能处理，如果您已经输出过成果，请注意备份谨慎处理！", funcdesc);
                aidialog.setFooterView(AiDialog.CENCEL, "确定，我要继续", new DialogInterface.OnClickListener() {
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
                                        Cgsc(mapInstance, true);
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
                        aidialog.setContentView("开始处理数据");

                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "查找所有宗地并提取自然幢");
                        Log.d(TAG, "智能处理:查找所有附属结构注记，并提取户附属");
                        List<Feature> fs_zd = new ArrayList<>();
                        // 1、加载所有宗地提取自然幢
                        FeatureViewH_FSJG.InitAllFsjgFromDZJ(mapInstance, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if(objects!=null && objects.length>0){
                                    aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "附属。");
                                }
                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "查找所有宗地，并提取自然幢");
                                FeatureViewZD.LaodAllZDExtractZrz(mapInstance, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "宗地。");
                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "查找所有宗地，并识别自然幢");
                                        FeatureViewZD.LaodAllZD_IdentyZrz(mapInstance, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "宗地。");
                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有自然幢，并识别逻辑幢。");
                                                FeatureViewZRZ.LaodAllZRZ_IdentyLjz(mapInstance, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "自然幢。");
                                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有逻辑幢，并识别户和附属结构");
                                                        Log.d(TAG, "智能处理:查找所有户，并识别户附属");
                                                        FeatureViewLJZ.LaodAllLJZ_IdentyHAndZFSJG(mapInstance, new AiRunnable() {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "逻辑幢。");
                                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有宗地，重新算建筑面积");
//                                                FeatureEditH.LaodAllH_IdentyHFSJG(mapInstance, new AiRunnable(callback) {
//                                                    @Override
//                                                    public <T_> T_ ok(T_ t_, Object... objects) {
//                                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "户。");
//                                                        aidialog.addContentView(null, AiU til.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有宗地，重新算建筑面积");
//                                                        Log.d(TAG, "智能处理:查找所有宗地，重新算建筑面积");
                                                                FeatureViewZD.LaodAllZDAndUpdateArea(mapInstance, new AiRunnable() {
                                                                    @Override
                                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "宗地。");
                                                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有幢，重新生成分层分户图");
                                                                        Log.d(TAG, "智能处理:查找所有幢，重新生成分层分户图");
                                                                        FeatureViewZRZ.LaodAllZRZ_CreateFCFHT(mapInstance, new AiRunnable() {
                                                                            @Override
                                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "幢。");
                                                                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有宗地，重新生成宗地草图、房产图");
                                                                                Log.d(TAG, "智能处理:查找所有宗地，重新生成宗地草图、房产图");
                                                                                FeatureViewZD.LaodAllZDCreateCTAddFCT(mapInstance, new AiRunnable() {
                                                                                    @Override
                                                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "宗。");
                                                                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "查找所有宗地，设定不动产单元。");
                                                                                        FeatureViewZD.LaodAllZDAndCreateBDCDY(mapInstance, new AiRunnable() {
                                                                                            @Override
                                                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                                 aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "不动产单元。");
                                                                                                Log.d(TAG, "智能处理:已创建" + objects[0] + "不动产单元。");
                                                                                                AiRunnable.Ok(callback, null);
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

//                                                        return null;
//                                                    }
//                                                });
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
                aidialog.show();
                return null;
            }
        });
    }

    // 成果输出
    public static void Cgsc(final MapInstance mapinstance, final boolean isReload) {
        final String funcdesc = "该功能将逐一对项目中所有不动产单元" + (isReload ? "重新" : "") + "生成word成果。";
        License.vaildfunc(mapinstance.activity, funcdesc, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final AiDialog aidialog = AiDialog.get(mapinstance.activity);
                aidialog.setHeaderView(R.mipmap.app_icon_dangan_blue, "生成资料")
                        .setContentView("注意：属于不可逆操作，如果您已经输出过成果，请注意备份谨慎处理！", funcdesc)
                        .setFooterView(AiDialog.CENCEL, "确定，我要继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 完成后的回掉
                                final AiRunnable callback = new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据完成。");
                                        aidialog.setFooterView(null, AiDialog.COLSE, null);
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
//                                aidialog.setCancelable(false).setFooterView("正在处理中，可能需要一段时间，暂时不允许操作！");
                                aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                                aidialog.setContentView("开始处理数据");
                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有不动产单元，并生成资料");
                                try {
                                    FeatureEditBDC.LaodALLBDC_CreateDOCX(mapinstance, isReload, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 已完成" + objects[0] + "户。");
                                            AiRunnable.Ok(callback, null);
                                            return null;
                                        }

                                        @Override
                                        public <T_> T_ error(T_ t_, Object... objects) {
                                            aidialog.addContentView(null, "不在授权区域，请联系当地服务商！");
                                            AiRunnable.Error(callback, null);
                                            return null;
                                        }
                                    });
                                } catch (Exception e) {
                                    callback.error(e, "");
                                }

                            }
                        }).show();

                return null;
            }
        });
    }

    // 数据检查
    public static void Sjjc(final MapInstance mapInstance) {
        final String funcdesc = "该功能将逐一对项目中所有宗地进行检查。"
                + "\n 1、宗地的界址点检查并重新生成；"
                + "\n 2、宗地范围内的户进行检查并更新"
                + "\n 3、宗地范围内的自然幢进行检查并更新";
        License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final AiDialog aidialog = AiDialog.get(mapInstance.activity);
                aidialog.setHeaderView(R.mipmap.app_icon_dangan_blue, "")
                        .setContentView("注意：属于不可逆操作，请注意备份谨慎处理！", funcdesc)
                        .setFooterView("取消", "确定，我要继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 完成后的回掉
                                final AiRunnable callback = new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据完成。");
                                        aidialog.setFooterView(null, "关闭", null);
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ no(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据失败！");
                                        aidialog.setFooterView(null, "关闭", null);
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ error(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据异常！");
                                        aidialog.setFooterView(null, "关闭", null);
                                        return null;
                                    }
                                };
                                aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                                aidialog.setContentView("开始处理数据");
                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有宗地，并检查界址点线");
                                // 查看列表
                                FeatureViewZD.initAllJzdx(mapInstance, callback);
                            }
                        }).show();
                ;
                return null;
            }
        });
    }
//    // 数据检查
//    public void Sjjc(final MapInstance mapinstance) {
//        final String funcdesc = "该功能将逐一对项目中所有宗地的代码进行更新。";
//        License.vaildfunc(mapinstance.activity, funcdesc, new AiRunnable() {
//            @Override
//            public <T_> T_ ok(T_ t_, Object... objects) {
//                final AiDialog aidialog = AiDialog.get(mapinstance.activity);
//                aidialog.setHeaderView(R.mipmap.app_icon_dangan_blue, "")
//                        .setContentView("注意：属于不可逆操作，请注意备份谨慎处理！", funcdesc)
//                        .setFooterView(AiDialog.CENCEL, "确定，我要继续", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                // 完成后的回掉
//                                final AiRunnable callback = new AiRunnable() {
//                                    @Override
//                                    public <T_> T_ ok(T_ t_, Object... objects) {
//                                        aidialog.addContentView("处理数据完成。");
//                                        aidialog.setFooterView(null, AiDialog.COLSE, null);
//                                        return null;
//                                    }
//
//                                    @Override
//                                    public <T_> T_ no(T_ t_, Object... objects) {
//                                        aidialog.addContentView(AiRunnable.NO);
//                                        aidialog.setFooterView(null, AiDialog.COLSE, null);
//                                        return null;
//                                    }
//
//                                    @Override
//                                    public <T_> T_ error(T_ t_, Object... objects) {
//                                        aidialog.addContentView(AiRunnable.ERROR);
//                                        aidialog.setFooterView(null, AiDialog.COLSE, null);
//                                        return null;
//                                    }
//                                };
//                                // 设置不可中断
////                                aidialog.setCancelable(false).setFooterView("正在处理中，可能需要一段时间，暂时不允许操作！");
//                                aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
//                                aidialog.setContentView("开始处理数据");
//                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有宗地，并生成资料");
//
////                                FeatureEditBDC.LaodAlLBDC_CreateDOCX(mapinstance, isReload, new AiRunnable() {
////                                    @Override
////                                    public <T_> T_ ok(T_ t_, Object... objects) {
////                                        aidialog.addContentView(null,AiUtil.GetValue(new Date(),AiUtil.F_TIME)+" 已完成"+objects[0]+"宗。");
////                                        AiRunnable.Ok(callback,null);
////                                        return  null;
////                                    } 15527836688
////                                });
//
//
//                                // 查看列表
//
//                                final List<Feature> fs_zd = new ArrayList<Feature>();
//                                MapHelper.Query(FeatureEdit.GetTable(mapinstance, FeatureHelper.TABLE_NAME_ZD, FeatureHelper.LAYER_NAME_ZD), "", -1, fs_zd, new AiRunnable() {
//
//                                    void zljc_zd(final List<Feature> fs, final int i, final AiRunnable identy_callback) {
//                                        if (fs.size() > i) {
//                                            mapinstance.newFeatureView().load_djzq(fs.get(i).getGeometry(), new AiRunnable() {
//                                                @Override
//                                                public <T_> T_ ok(T_ t_, Object... objects) {
//                                                    String djzq = t_ + "";
//
//                                                    ZnjcUpdateZd(mapinstance, djzq, fs.get(i), new AiRunnable() {
//                                                        @Override
//                                                        public <T_> T_ ok(T_ t_, Object... objects) {
//                                                            zljc_zd(fs, i + 1, identy_callback);
//                                                            return null;
//                                                        }
//                                                    });
//
//                                                    return null;
//                                                }
//                                            });
//                                        } else {
//                                            AiRunnable.Ok(identy_callback, i);
//                                        }
//                                    }
//
//                                    @Override
//                                    public <T_> T_ ok(T_ t_, Object... objects) {
//                                        zljc_zd(fs_zd, 0, new AiRunnable() {
//                                            @Override
//                                            public <T_> T_ ok(T_ t_, Object... objects) {
////                                                AiRunnable.Ok(callback,null);
////                                                return super.ok(t_, objects);
//                                                AiRunnable.Ok(callback, t_, objects);
//                                                return null;
//                                            }
//                                        });
//                                        return null;
//                                    }
//                                });
//
//
//                            }
//                        }).show();
//                ;
//
//                return null;
//            }
//        });
//    }

    // 智能检测宗地代码
    public void ZnjcUpdateZd(final MapInstance mapInstance, final String djzq, final Feature f_zd, final AiRunnable callback) {
        String zddm_zd = (String) f_zd.getAttributes().get(FeatureHelper.TABLE_ATTR_ZDDM);
        final List<Feature> update_fs = new ArrayList<>();
        if (djzq.equals(zddm_zd.substring(0, 12))) {
            MapHelper.saveFeature(update_fs, callback);
            return;
        }
        final List<Feature> f_zrzs = new ArrayList<>();
        final List<Feature> f_zrz_fsjgs = new ArrayList<>();
        final List<Feature> f_zrz_hs = new ArrayList<>();
        final List<Feature> f_zrz_h_fsjgs = new ArrayList<>();

        FeatureView.LoadZ_H_And_Fsjg(mapInstance, f_zd, f_zrzs, f_zrz_fsjgs, f_zrz_hs, f_zrz_h_fsjgs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String zddm = djzq + mapInstance.getId(f_zd).substring(12);
                for (Feature f : f_zrzs) {
                    String oldDirZrz = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f));
                    String bdcdyh_zrz = (String) f.getAttributes().get(FeatureHelper.TABLE_ATTR_BDCDYH);

                    if (!TextUtils.isEmpty(bdcdyh_zrz) && bdcdyh_zrz.length() > 12) {
                        f.getAttributes().put(FeatureHelper.TABLE_ATTR_BDCDYH, djzq + bdcdyh_zrz.substring(12));
                    }
                    String zrzh = (String) f.getAttributes().get("ZRZH");
                    if (!TextUtils.isEmpty(zrzh) && zrzh.length() > 12) {
                        f.getAttributes().put("ZRZH", djzq + zrzh.substring(12));
                    }
                    f.getAttributes().put(FeatureHelper.TABLE_ATTR_ZDDM, zddm);
                    String newDirZrz = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f));
                    FileUtils.renameToNewFile(oldDirZrz, newDirZrz);
                    update_fs.add(f);
                }

                String bdcdyh = (String) f_zd.getAttributes().get(FeatureHelper.TABLE_ATTR_BDCDYH);
                if (!TextUtils.isEmpty(bdcdyh) && bdcdyh.length() > 12) {
                    f_zd.getAttributes().put(FeatureHelper.TABLE_ATTR_BDCDYH, djzq + bdcdyh.substring(12));
                }
                String oldDirZD = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd));

                f_zd.getAttributes().put(FeatureHelper.TABLE_ATTR_ZDDM, zddm);
                String newDirZD = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd));
                FileUtils.renameToNewFile(oldDirZD, newDirZD);
                update_fs.add(f_zd);
                MapHelper.saveFeature(update_fs, callback);
                return null;
            }
        });
    }

    ///endregion

    //region 私有函数
    // 图形修复
    private void txxf(final MapInstance mapInstance, final boolean isReload) {
        final String funcdesc = "该功能将逐一对项目中宗地，自然幢图形" + (isReload ? "重新" : "") + "进行修整。";
        License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final AiDialog aidialog = AiDialog.get(mapInstance.activity);
                aidialog.setHeaderView(R.mipmap.app_icon_dangan_blue, "修整图形")
                        .setContentView("注意：属于不可逆操作，如果您已经修整过图形，请注意备份谨慎处理！", funcdesc)
                        .setFooterView(AiDialog.CENCEL, "确定，我要继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 完成后的回掉
                                final AiRunnable callback = new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据完成。");
                                        aidialog.setFooterView(null, AiDialog.COLSE, null);
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
                                FeatureViewZD.LaodAllZD_IdentyZrz(mapInstance, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        aidialog.setContentView("开始修整图形");
                                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有宗地，并生修整图形");
                                        sjcl(mapInstance, callback);
                                        return null;
                                    }
                                });
                            }
                        }).show();
                return null;
            }
        });
    }

    //生成台帐
    private void Sctz(final MapInstance mapInstance, final boolean isReload) {
        final String funcdesc = "该功能将逐一对项目中每宗地资料" + (isReload ? "重新" : "") + "进行整理。";
        License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final AiDialog aidialog = AiDialog.get(mapInstance.activity);
                aidialog.setHeaderView(R.mipmap.app_icon_dangan_blue, "整理资料");
                aidialog.setContentView("注意：属于不可逆操作，如果您已经整理过成果，请注意备份谨慎处理！", funcdesc);
                aidialog.setFooterView("取消", "确定，我要继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 完成后的回掉
                        final AiRunnable callback = new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                aidialog.addContentView("处理数据完成。");
                                aidialog.setFooterView(null, "关闭", null);
                                return null;
                            }

                            @Override
                            public <T_> T_ no(T_ t_, Object... objects) {
                                aidialog.addContentView("处理数据失败！");
                                aidialog.setFooterView(null, "关闭", null);
                                return null;
                            }

                            @Override
                            public <T_> T_ error(T_ t_, Object... objects) {
                                aidialog.addContentView("处理数据异常！");
                                aidialog.setFooterView(null, "关闭", null);
                                return null;
                            }
                        };
                        // 设置不可中断
                        aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                        aidialog.setContentView("开始处理数据");
                        aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有不动产单元，并生成资料");
                        {
                            final List<Feature> fs_zd = new ArrayList<>();
                            final List<Feature> fs_bdc = new ArrayList<>();

                            MapHelper.Query(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZD, FeatureHelper.LAYER_NAME_ZD), "", MapHelper.QUERY_LENGTH_MAX, fs_zd, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    final List<Map<String, Object>> maps = new ArrayList<>();
                                    new AiForEach<Feature>(fs_zd, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            String filePath = FileUtils.getAppDirAndMK(FeatureEditBDC.GetPath_Templet()) + "不动产权籍调查入库模板.xls"; // 入库模板
                                            if (FileUtils.exsit(filePath)) {
                                                try {
                                                    // TODO... 根据excel 模板生成 生成入库成果
                                                    final Map<Integer, String> xlsData = Excel.getXlsMap(filePath);
                                                    String xmmc = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "XMMC", "");
                                                    String xmbm = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "XMBM", "");
                                                    String filePath_badong = FileUtils.getAppDirAndMK(mapInstance.getpath_root()) + xmbm + xmmc + ".xls";
                                                    Excel.CreateStandingBook(maps, xlsData, filePath, filePath_badong);
                                                } catch (Exception e) {
                                                    AiRunnable.Error(callback, null);
                                                    e.printStackTrace();
                                                }
                                            }
                                            AiRunnable.Ok(callback, null);
                                            return null;
                                        }
                                    }) {
                                        @Override
                                        public void exec() {
                                            final Feature f_zd = fs_zd.get(postion);

                                            final Feature f_bdcdy = fs_bdc.get(postion);

                                            final List<Feature> fs_zrz = new ArrayList<>();
                                            final List<Feature> fs_ljz = new ArrayList<>();

                                            final List<Feature> fs_qlrxx = new ArrayList<>();
                                            final List<Feature> fs_hjxx = new ArrayList<>();
                                            final String orid = mapInstance.getOrid(f_zd);

                                            final String where = " ORID_PATH like '%" + orid + "%' ";
                                            MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZRZ), StringUtil.WhereByIsEmpty(orid) + where, FeatureHelper.TABLE_ATTR_ZRZH, "asc", -1, fs_zrz, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_LJZ), StringUtil.WhereByIsEmpty(orid) + where, FeatureHelper.TABLE_ATTR_LJZH, "asc", -1, fs_ljz, new AiRunnable() {
                                                        @Override
                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                            Map<String, Object> map_ = new LinkedHashMap<>();
                                                            FeatureEditBDC.Put_data_zd(mapInstance, map_, FeatureHelper.Get(f_zd, "BDCDYH", ""), f_zd);
                                                            FeatureEditBDC.Put_data_zrz(mapInstance, map_, f_zd, fs_zrz);
                                                            FeatureEditBDC.Put_data_ljz(mapInstance, map_, f_zd, fs_ljz);
                                                            FeatureEditBDC.Put_data_ljz(mapInstance, map_,f_zd,fs_ljz);

                                                            FeatureEditBDC.Put_data_fjcl( mapInstance, map_, f_zd, f_bdcdy,fs_qlrxx, fs_hjxx);

                                                            maps.add(map_);
                                                            AiRunnable.Ok(getNext(), t_, objects);
                                                            return null;
                                                        }
                                                    });
                                                    return null;
                                                }
                                            });

                                        }
                                    }.start();
                                    return null;
                                }
                            });
                        }
                    }
                }).show();
                return null;
            }
        });
    }

    // 生成两权数据
    private void output_lqsj(final MapInstance mapInstance) {
        final String funcdesc = "该功能将逐生成完整的dxf成果！";

        License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
            final AiDialog aidialog = AiDialog.get(mapInstance.activity);

            void setMessage(final String message) {
                if (StringUtil.IsNotEmpty(message)) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastMessage.Send(message);
                            aidialog.setContentView(message);
                        }
                    });
                }
            }

            void addMessage(final String title, final String mssage) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        aidialog.addContentView(title, mssage);
                    }
                });
            }


            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {

                aidialog.setHeaderView(R.mipmap.app_icon_rgzl_blue, "dxf成果输出")
                        .setContentView("注意：属于不可逆操作，如果您已经输出过成果，请注意备份谨慎处理！", funcdesc)
                        .setFooterView(AiDialog.CENCEL, "确定，我要继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {

                                // 完成后的回掉
                                final AiRunnable callback = new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                aidialog.addContentView("处理数据完成，你可能还需要重新生成成果。");
                                                aidialog.setFooterView("重新生成成果", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }, null, null, "完成", null);
                                            }
                                        });
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ no(final T_ t_, Object... objects) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                aidialog.addContentView(AiRunnable.NO, t_ + "");
                                                aidialog.setFooterView(null, AiDialog.COLSE, null);
                                            }
                                        });
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ error(final T_ t_, Object... objects) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                aidialog.addContentView(AiRunnable.ERROR, t_ + "");
                                                aidialog.setFooterView(null, AiDialog.COLSE, null);
                                            }
                                        });
                                        return null;
                                    }
                                };
                                aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                                aidialog.setContentView("开始处理数据");
                                final List<Feature> fs = new ArrayList<>();
                                String name = GsonUtil.GetValue(aiMap.JsonData, "XMMC", "");
                                final String dxfpath = FileUtils.getAppDirAndMK(mapInstance.getpath_root()) + name + ".dxf";

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Envelope extent_ZD = com.ovit.app.map.view.FeatureEdit.GetTable(getMapInstance(), FeatureHelper.TABLE_NAME_ZD, FeatureHelper.LAYER_NAME_ZD).getExtent();
                                            Envelope envelope = MapHelper.geometry_get(extent_ZD, MapHelper.GetSpatialReference(mapInstance));
//                                          final DxfAdapter dxf = DxfAdapter.getInstance();
                                            com.ovit.app.util.gdal.dxf.DxfRenderer dxfRenderer = new com.ovit.app.util.gdal.dxf.DxfRenderer();

                                            final com.ovit.app.util.gdal.dxf.DxfAdapter dxf = new com.ovit.app.util.gdal.dxf.DxfAdapter();
                                            dxf.create(dxfpath, envelope, MapHelper.GetSpatialReference(mapInstance)).setDxfRenderer(dxfRenderer);

                                            final List<String> tableNames = new ArrayList<>();
                                            tableNames.add(FeatureHelper.TABLE_NAME_ZD);
                                            tableNames.add(FeatureHelper.TABLE_NAME_ZRZ);
                                            tableNames.add(FeatureHelper.TABLE_NAME_LJZ); // 导出失败
                                            tableNames.add(FeatureHelper.TABLE_NAME_H);
                                            tableNames.add(FeatureHelper.TABLE_NAME_ZRZ_C);

                                            tableNames.add(FeatureHelper.TABLE_NAME_H_FSJG);
                                            tableNames.add(FeatureHelper.TABLE_NAME_Z_FSJG);
                                            tableNames.add(FeatureHelper.TABLE_NAME_JZD);
                                            tableNames.add(FeatureHelper.TABLE_NAME_JZX);

                                            tableNames.add(FeatureHelper.TABLE_NAME_XZDW);
                                            tableNames.add(FeatureHelper.TABLE_NAME_MZDW);
                                            tableNames.add(FeatureHelper.TABLE_NAME_DZDW);
                                            tableNames.add(FeatureHelper.TABLE_NAME_ZJD);
                                            tableNames.add(FeatureHelper.TABLE_NAME_ZJX);
                                            tableNames.add(FeatureHelper.TABLE_NAME_ZJWZ);
                                            tableNames.add(FeatureHelper.TABLE_NAME_ZJM);
                                            tableNames.add(FeatureHelper.TABLE_NAME_FSSS);
                                            tableNames.add(FeatureHelper.TABLE_NAME_SJ);
                                            final LineLabel lineLabel = new LineLabel();
                                            new AiForEach<String>(tableNames, null) {
                                                @Override
                                                public void exec() {
                                                    final String tableName = tableNames.get(postion);
                                                    FeatureTable featureTable = FeatureEdit.GetTable(mapInstance, tableName);
                                                    if (featureTable == null) {
                                                        addMessage("", "缺少" + tableName + "图层！");
                                                        AiRunnable.Ok(getNext(), null);
                                                    }
                                                    MapHelper.Query(featureTable, "", MapHelper.QUERY_LENGTH_MAX, fs, new AiRunnable() {
                                                        @Override
                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                            try {
                                                                if (!tableName.equals(FeatureHelper.TABLE_NAME_H) && !tableName.equals(FeatureHelper.TABLE_NAME_ZRZ_C) && !tableName.equals(FeatureHelper.TABLE_NAME_JZX)) {
                                                                    dxf.write(getMapInstance(), fs, null, null, DxfHelper.TYPE, DxfHelper.LINE_LABEL_OUTSIDE, lineLabel);
                                                                }
                                                                addMessage("", "读取到" + fs.size() + "条" + tableName + "数据，正在输出成果...");
                                                                String xmdm = GsonUtil.GetValue(aiMap.JsonData, "XMBM", "");
                                                                String Shpath = FileUtils.getAppDirAndMK(getMapInstance().getpath_root() + "资料库/两权shp/") + xmdm + "_" + tableName + ".shp";
                                                                ShpAdapter.writeShp(Shpath, fs, MapHelper.GetSpatialReference(mapInstance), mapInstance);
                                                                fs.clear();
                                                                addMessage("", tableName + "数据导出成功！");
                                                                AiRunnable.Ok(getNext(), t_);
                                                            } catch (Exception e) {
                                                                addMessage("导出成功两权数据失败", e.getMessage() + tableName);
                                                                AiRunnable.Error(callback, null);
                                                            }
                                                            return null;
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void complet() {
                                                    try {
                                                        dxf.save();
                                                        AiRunnable.Ok(callback, null);
                                                    } catch (Exception e) {
                                                        addMessage("导出成功两权数据失败", "数据保存失败" + e.getMessage());
                                                        AiRunnable.Error(callback, null);
                                                    }
                                                }
                                            }.start();

                                        } catch (Exception e) {
                                            addMessage("操作失败", e.getMessage());
                                            AiRunnable.Error(callback, null);
                                        }
                                    }
                                }).start();


                            }
                        }).show();
                return null;
            }
        });
    }

    /**
     * 数据处理
     *
     * @param mapInstance
     * @param callback
     */
    private void sjcl(final MapInstance mapInstance, final AiRunnable callback) {
        FeatureTable featureTableZD = FeatureEditZD.GetTable(mapInstance, FeatureHelper.TABLE_NAME_ZD, FeatureHelper.LAYER_NAME_ZD);
        final List<Feature> fs_zd = new ArrayList<>();
        final List<Feature> fs_all = new ArrayList<>();
        MapHelper.Query(featureTableZD, "", MapHelper.QUERY_FEATURE_MAX_RESULTS, fs_zd, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                new AiForEach<Feature>(fs_zd, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        fs_all.addAll(fs_zd);
                        MapHelper.saveFeature(fs_all, callback);
                        return null;
                    }
                }) {
                    @Override
                    public void exec() {
                        final Feature f_zd = fs_zd.get(postion);
                        String where = "ZDDM='" + FeatureEditZD.GetID(f_zd) + "'";
                        final List<Feature> fs_zrz = new ArrayList<>();
//                        // 通过范围去查询
//                        MapHelper.Query(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ), f_zd.getGeometry(), -0.015, fs_zrz, new AiRunnable() {
//                            @Override
//                            public <T_> T_ ok(T_ t_, Object... objects) {
//                                fs_all.addAll(fs_zrz);
//                                for (int i = 0; i < fs_zrz.size(); i++) {
//                                    Feature f_zrz = fs_zrz.get(i);
//                                    for (int j=i+1; j < fs_zrz.size(); j++){
//                                        Feature f_zrz_next = fs_zrz.get(j);
//                                        Geometry g = MapHelper.geometry_trim(f_zrz.getGeometry(), f_zrz_next.getGeometry());
//                                        f_zrz.setGeometry(g);
//                                        Geometry g_next = MapHelper.geometry_trim( f_zrz_next.getGeometry(),f_zrz.getGeometry());
//                                        f_zrz_next.setGeometry(g_next);
//                                    }
//                                }
//
//                                // 宗地 与 自然幢 加点
//                                for (int i = 0; i < fs_zrz.size(); i++) {
//                                    Geometry geometry = MapHelper.geometry_trim(f_zd.getGeometry(), fs_zrz.get(i).getGeometry());
//                                    f_zd.setGeometry(geometry);
//                                }
//                                AiRunnable.Ok(getNext (),null);
//                                return null;
//                            }
//                        });

                        MapHelper.Query(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ), where, MapHelper.QUERY_FEATURE_MAX_RESULTS, fs_zrz, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
//                                Geometry f_zd_g=f_zd.getGeometry();
                                fs_all.addAll(fs_zrz);
                                for (int i = 0; i < fs_zrz.size(); i++) {
                                    Feature f_zrz = fs_zrz.get(i);
                                    for (int j = i + 1; j < fs_zrz.size(); j++) {
                                        Feature f_zrz_next = fs_zrz.get(j);
                                        Geometry g = MapHelper.geometry_trim(f_zrz.getGeometry(), f_zrz_next.getGeometry());
                                        f_zrz.setGeometry(g);
                                        Geometry g_next = MapHelper.geometry_trim(f_zrz_next.getGeometry(), f_zrz.getGeometry());
                                        f_zrz_next.setGeometry(g_next);
                                    }
                                }

                                // 宗地 与 自然幢 加点
                                for (int i = 0; i < fs_zrz.size(); i++) {
                                    Geometry geometry = MapHelper.geometry_trim(f_zd.getGeometry(), fs_zrz.get(i).getGeometry());
                                    f_zd.setGeometry(geometry);
                                }
                                AiRunnable.Ok(getNext(), null);
                                return null;
                            }
                        });
                    }
                }.start();
                return null;
            }
        });
    }

    /**
     * 导入不动产单元
     *
     * @param mapInstance
     */
    private void input_bdcdy(final MapInstance mapInstance) {
        final String funcdesc = "该功能将逐步导入不动产单元！";
        License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
            final AiDialog aidialog = AiDialog.get(mapInstance.activity);

            void setMessage(final String message) {
                if (StringUtil.IsNotEmpty(message)) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastMessage.Send(message);
                            aidialog.setContentView(message);
                        }
                    });
                }
            }

            void addMessage(final String title, final String mssage) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        aidialog.addContentView(title, mssage);
                    }
                });
            }

            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {

                aidialog.setHeaderView(R.mipmap.app_icon_rgzl_blue, "dxf成果输出")
                        .setContentView("注意：属于不可逆操作，如果您已经输出过成果，请注意备份谨慎处理！", funcdesc)
                        .setFooterView(AiDialog.CENCEL, "确定，我要继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {

                                // 完成后的回掉
                                final AiRunnable callback = new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                aidialog.addContentView("处理数据完成，你可能还需要重新生成成果。");
                                                aidialog.setFooterView("重新生成成果", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }, null, null, "完成", null);
                                            }
                                        });
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ no(final T_ t_, Object... objects) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                aidialog.addContentView(AiRunnable.NO, t_ + "");
                                                aidialog.setFooterView(null, AiDialog.COLSE, null);
                                            }
                                        });
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ error(final T_ t_, Object... objects) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                aidialog.addContentView(AiRunnable.ERROR, t_ + "");
                                                aidialog.setFooterView(null, AiDialog.COLSE, null);
                                            }
                                        });
                                        return null;
                                    }
                                };
                                aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                                aidialog.setContentView("开始处理数据");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            // TODO... 根据excel 模板生成 生成入库成果
                                            String xmmc = GsonUtil.GetValue(aiMap.JsonData, "XMMC", "");
                                            String xmbm = GsonUtil.GetValue(aiMap.JsonData, "XMBM", "");
                                            final String filePath = FileUtils.getAppDirAndMK(mapInstance.getpath_root()) + xmbm + xmmc + "不动产单元" + ".xls";
                                            final List<Map<String, String>> xlsData = Excel.getXlsMap(filePath, FeatureHelper.TABLE_NAME_QLRXX, 0);
                                            final List<Feature> fs = new ArrayList<>();

                                            new AiForEach<Map<String, String>>(xlsData, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    MapHelper.saveFeature(fs, callback);
                                                    return null;
                                                }
                                            }) {
                                                @Override
                                                public void exec() {
                                                    final Map<String, String> map = xlsData.get(postion);
                                                    Feature feature = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX).createFeature();
                                                    mapInstance.fillFeature(feature);
                                                    for (Field field : feature.getFeatureTable().getFields()) {
                                                        if (field.isEditable()) {
                                                            String key = field.getName();
                                                            String value_ = map.get(key);
                                                            try {
                                                                if (StringUtil.IsNotEmpty(value_)) {
                                                                    Field filed = feature.getFeatureTable().getField(key);
                                                                    Field.Type type = filed.getFieldType();
                                                                    if (type.equals(Field.Type.TEXT)) {
                                                                        feature.getAttributes().put(key, value_);
                                                                    } else if (type.equals(Field.Type.DOUBLE)) {
                                                                        feature.getAttributes().put(key, AiUtil.GetValue(value_, 0d));
                                                                    } else if (type.equals(Field.Type.FLOAT)) {
                                                                        feature.getAttributes().put(key, AiUtil.GetValue(value_, 0f));
                                                                    } else if (type.equals(Field.Type.SHORT)) {
                                                                        feature.getAttributes().put(key, Short.valueOf(AiUtil.GetValue(value_, (short) 0) + ""));
                                                                    } else if (type.equals(Field.Type.DATE)) {
                                                                        GregorianCalendar gregorianCalendar = new GregorianCalendar();
                                                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                                        final Date parse = sdf.parse(value_);
                                                                        gregorianCalendar.setTime(parse);
                                                                        feature.getAttributes().put(key, gregorianCalendar);
                                                                    } else if (type.equals(Field.Type.INTEGER)) {
                                                                        feature.getAttributes().put(key, AiUtil.GetValue(value_, 0));
                                                                    } else {
                                                                        feature.getAttributes().put(key, value_);
                                                                    }
                                                                }
                                                            } catch (Exception es) {
                                                                Log.e(TAG, "不支持更新的属性[" + key + ":" + value_ + "]", es);
                                                            }
                                                        }
                                                    }
                                                    fs.add(feature);
                                                    AiRunnable.Ok(getNext(), null);
                                                }
                                            }.start();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (BiffException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }).start();
                            }
                        }).show();
                return null;
            }
        });
    }

    /**
     * 输入excel
     */
    private void intput_excels() {
        final AiDialog dialog = AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_zd_fw, "导入Excel数据");

        final Map<String, Object> dataconfig = new HashMap<>();
        dialog.addContentView(dialog.getSelectView("图层", "map_tc", dataconfig, "tc"));

        dialog.addContentViewMenu(Arrays.asList(
                new Object[]{R.mipmap.app_icon_excel_blue, "导入家庭成员", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FileUtils.showChooser(activity, new AiRunnable() {
                            void setMessage(final String message) {
                                if (StringUtil.IsNotEmpty(message)) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastMessage.Send(message);
                                            dialog.setContentView(message);
                                        }
                                    });
                                }
                            }

                            void addMessage(final String title, final String mssage) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.addContentView(title, mssage);
                                    }
                                });
                            }

                            void setComplete(final List<Feature> fs_hz, final List<Feature> fs_hjxx, final int icon, final String text, final boolean cancelable) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.setCancelable(cancelable);
                                        dialog.setFooterView("取消", "自动挂接", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, int which) {
                                                if (fs_hz == null || fs_hz.size() == 0) {
                                                    dialog.dismiss();
                                                    ToastMessage.Send("未检测到户主！");
                                                } else {
                                                    ToastMessage.Send("开始挂接，请稍候！");
                                                    GYRAutoLinkZD(mapInstance, fs_hz, fs_hjxx, dialog);
                                                }
                                            }
                                        });
                                        addMessage(text, "");

                                    }
                                });
                            }

                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                String message = "";
                                final String filePath = (String) t_;
                                if (filePath.toLowerCase().endsWith(".xls")) {
                                    dialog.setCancelable(false)
                                            .setFooterView(dialog.getProgressView("正在导入数据..."))
                                            .setContentView("正在读取Excel内容导入中，所耗时间取决于文件大小，请耐心等待...");

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Map<String, List<Map<String, String>>> sql_results = Excel.getXlsMapHjxx(filePath, "SQL Results", 0);
                                                final List<Feature> fs_hz = new ArrayList<>();
                                                final List<Feature> fs_hjxx = new ArrayList<>();
                                                for (String key : sql_results.keySet()) {
                                                    List<Map<String, String>> familys = sql_results.get(key);
                                                    final List<Feature> fill_fs_hjxx = new ArrayList<>();
                                                    final List<Feature> fill_fs_hz = new ArrayList<>();
                                                    for (final Map<String, String> map : familys) {
                                                        if ("户主".equals(map.get("YHZGX"))) {
                                                            Feature f_hz = fillGYROrHJXX(mapInstance.getTable(FeatureHelper.TABLE_NAME_GYRXX).createFeature(), map);
                                                            mapInstance.fillFeature(f_hz);
                                                            fs_hz.add(f_hz);
                                                            fill_fs_hz.add(f_hz);
                                                        } else {
                                                            Feature f_hjxx = fillGYROrHJXX(mapInstance.getTable(FeatureHelper.TABLE_NAME_HJXX).createFeature(), map);
                                                            mapInstance.fillFeature(fs_hjxx);
                                                            fs_hjxx.add(f_hjxx);
                                                            fill_fs_hjxx.add(f_hjxx);
                                                        }
                                                    }
                                                    if (fill_fs_hjxx.size() > 1 && fill_fs_hz.size() == 1) {
                                                        for (int i = 0; i < fill_fs_hjxx.size(); i++) {
                                                            mapInstance.fillFeature(fill_fs_hjxx.get(i), fill_fs_hz.get(0));
                                                        }
                                                    }
                                                }

                                                MapHelper.saveFeature(fs_hjxx, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        MapHelper.saveFeature(fs_hz, new AiRunnable() {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                setComplete(fs_hz, fs_hjxx, R.mipmap.app_icon_ok_f, "导入成功！如需与不动产单元自动挂接，请点击确定！\n 若导入的表格中没有ZDDM字段，则无法进行挂接！", true);
                                                                return null;
                                                            }
                                                        });
                                                        return null;
                                                    }
                                                });

                                            } catch (Exception es) {
                                                setMessage("读取文件失败：" + es.getMessage());
                                                setComplete(null, null, R.mipmap.app_icon_error_smaller, "读取文件失败", true);
                                            }
                                        }
                                    }).start();
                                }
                                return null;
                            }
                        });

                    }
                }},
                new Object[]{R.mipmap.app_icon_cad, "导入Excel文件", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FileUtils.showChooser(activity, new AiRunnable() {
                            void setMessage(final String message) {
                                if (StringUtil.IsNotEmpty(message)) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastMessage.Send(message);
                                            dialog.setContentView(message);
                                        }
                                    });
                                }
                            }

                            void addMessage(final String title, final String mssage) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.addContentView(title, mssage);
                                    }
                                });
                            }

                            void setComplete(final int icon, final String text, final boolean cancelable) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.setCancelable(cancelable);
                                        dialog.setFooterView(dialog.getStateView(icon, text));
                                        addMessage(text, "");

                                    }
                                });
                            }

                            void save(final Map<String, Integer> map_fs, List<Feature> features, final boolean isComplete) {
                                final List<Feature> fs = new ArrayList<>(features);
                                features.clear();
                                String messge_ = "";
//                                for (String key:map_fs.keySet()){ messge_+="["+key+"："+map_fs.get(key)+"]"; }
                                final String messge = messge_;
                                MapHelper.saveFeature(fs, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        addMessage("", AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "导入" + fs.size() + "个图形成功:" + messge + "...");
                                        if (isComplete)
                                            setComplete(R.mipmap.app_icon_ok_f, "导入" + messge + "成功！", true);
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ error(T_ t_, Object... objects) {
                                        addMessage("导入失败:", ((Exception) t_).getMessage());
                                        if (isComplete)
                                            setComplete(R.mipmap.app_icon_error_smaller, "导入失败", true);
                                        return null;
                                    }
                                });
                            }

                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                String message = "";
                                // TODO... 根据excel 模板生成 生成入库成果
                                final String filePath = (String) t_;
                                String tc = AiUtil.GetValue(dataconfig.get("tc"), "");
                                final String sel_tc = StringUtil.substr(tc, tc.indexOf("[") + 1, tc.indexOf("]"));

                                if (filePath.toLowerCase().endsWith(".xls")) {
                                    dialog.setCancelable(false)
                                            .setFooterView(dialog.getProgressView("正在导入数据..."))
                                            .setContentView("正在读取Excel内容导入中，所耗时间取决于文件大小，请耐心等待...");

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                final List<Feature> features_upt = new ArrayList<>();
                                                final List<Map<String, String>> xlsData = Excel.getXlsMap(filePath, sel_tc, 0);
                                                new AiForEach<Map<String, String>>(xlsData, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        save(null, features_upt, true);
                                                        return null;
                                                    }
                                                }) {
                                                    @Override
                                                    public void exec() {
                                                        final Map<String, String> map = xlsData.get(postion);
                                                        String where = getQueryWhereFromXlsData(map, sel_tc);
                                                        if (where.contains("0=1")) {
                                                            // 数据无效
                                                            AiRunnable.Ok(getNext(), "");
                                                        } else {
                                                            MapHelper.QueryOne(mapInstance.getTable(sel_tc), where, new AiRunnable() {
                                                                @Override
                                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                                    Feature feature = null;
                                                                    if (t_ != null && t_ instanceof Feature) {
                                                                        feature = (Feature) t_;
                                                                    } else {
                                                                        if (FeatureHelper.TABLE_NAME_QLRXX.equals(sel_tc)) {
                                                                            feature = mapInstance.getTable(sel_tc).createFeature();
                                                                        }
                                                                    }
                                                                    if (feature != null) {
                                                                        for (String key : map.keySet()) {
                                                                            String value = map.get(key);
                                                                            if (key.equalsIgnoreCase("JGRQ")) {
                                                                                value = value.replaceAll("/", "-").trim();
                                                                            }
                                                                            FeatureHelper.Set(feature, key, value, true);
                                                                        }
                                                                        features_upt.add(feature);
                                                                    }
                                                                    AiRunnable.Ok(getNext(), t_);
                                                                    return null;
                                                                }
                                                            });
                                                        }
                                                    }
                                                }.start();

                                            } catch (Exception es) {
                                                setMessage("读取文件失败：" + es.getMessage());
                                                setComplete(R.mipmap.app_icon_error_smaller, "读取文件失败", true);
                                            }
                                        }
                                    }).start();
                                }


                                return null;
                            }
                        });
                    }
                }}))
                .addContentView("导入Excel数据：",
                        "1、支持的Excel文件时数据为.xls文件",
                        "2、dxf文件为二权标准成果文件dwg转换的dxf文件",
                        "3、文件中对应的实体编码会导入到相应的图层"
                ).show();

    }

    /**
     * 图层导入的excel 数据获取查询条件
     *
     * @param sel_tc 当前选择的图层
     * @returnz
     */
    private String getQueryWhereFromXlsData(Map<String, String> map, String sel_tc) {
        String where = "";
        if (FeatureHelper.TABLE_NAME_QLRXX.equals(sel_tc)) {
            String qlrzjh = AiUtil.GetValue(map.get("ZJH"), "");
            String qlrdm = AiUtil.GetValue(map.get("QLRDM"), "");
            String qlrxm = AiUtil.GetValue(map.get("XM"), "");

            if (TextUtils.isEmpty(qlrzjh) && TextUtils.isEmpty(qlrdm)) {
                where = "0=1";
            } else if (StringUtil.IsNotEmpty(qlrzjh) && StringUtil.IsNotEmpty(qlrdm)) {
                where = "ZJH='" + qlrzjh + "' and QLRDM='" + qlrdm + "'";
            } else if (StringUtil.IsNotEmpty(qlrzjh)) {
                where = "ZJH='" + qlrzjh + "'";
            } else {
                where = "QLRDM='" + qlrdm + "'";
            }
            where = "XM='" + qlrxm + "'" + " and " + where;
        } else if (FeatureHelper.TABLE_NAME_ZD.equals(sel_tc)) {
            String zddm = AiUtil.GetValue(map.get(FeatureHelper.TABLE_ATTR_ZDDM), "");
            if (TextUtils.isEmpty(zddm)) {
                where = "0=1";
            } else {
                where = "ZDDM='" + zddm + "'";
            }
        } else {
            where = "0=1";
        }
        return where;
    }

    /**
     * 获取到excel信息后填充共有人或户籍信息对象
     */
    private Feature fillGYROrHJXX(Feature feature, Map<String, String> map) {
        for (String key1 : map.keySet()) {
            String value = map.get(key1);
            if (StringUtil.IsNotEmpty(value) && StringUtil.IsNotEmpty(key1)) {
                if ("XB".equals(key1)) {
                    if ("1".equals(value)) {
                        value = "男";
                    } else if ("2".equals(value)) {
                        value = "女";
                    }
                }
                FeatureHelper.Set(feature, key1, value, true);
            }
        }

        // 设置性别
        String xb = FeatureHelper.Get(feature, "XB", "");
        if (TextUtils.isEmpty(xb)) {
            String iCard = map.get("ZJH");
            FeatureHelper.Set(feature, "XB", StringUtil.GetSex(iCard));
        }
        return feature;
    }

    /**
     * 导入家庭成员表后，自动进行以宗地为准的不动产单元挂接
     *
     * @param mapInstance
     * @param fs_hz
     * @param dialog
     */
    FeatureView fv;

    private void GYRAutoLinkZD(final MapInstance mapInstance, final List<Feature> fs_hz, final List<Feature> fs_hjxx, final DialogInterface dialog) {
        if (fs_hz.size() > 0) {
            fv = new FeatureView();
            final FeatureTable table_hjxx = mapInstance.getTable(FeatureHelper.TABLE_NAME_HJXX);
            new AiForEach<Feature>(fs_hz, null) {
                @Override
                public void exec() {
                    final Feature f_hz = fs_hz.get(postion);
                    String zddm = FeatureHelper.Get(f_hz, FeatureHelper.TABLE_ATTR_ZDDM, "");
                    if (FeatureHelper.isZDDMHValid(zddm)) {
                        FeatureViewZD.From(mapInstance).loadByZddm(zddm, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (FeatureHelper.isExistFeature(t_)) {
                                    final Feature f_zd = (Feature) t_;
                                    final String orid_hz = mapInstance.getOrid(f_hz);
                                    final String orid_zd = mapInstance.getOrid(f_zd);
                                    final String where_hz = StringUtil.WhereByIsEmpty(orid_hz) + " ORID_PATH like '%" + orid_hz + "%' ";
                                    final String where_zd = StringUtil.WhereByIsEmpty(orid_zd) + " ORID_PATH like '%" + orid_zd + "%' ";
                                    final FeatureTable table_qlr = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX);
                                    final List<Feature> fs_bdcdy = new ArrayList<>();
                                    final List<Feature> fs_hjxx = new ArrayList<>();
//                                    String orid=FeatureHelper.Get(f_hz,"ORID","");
                                    mapInstance.newFeatureView(f_zd).queryFeature(table_hjxx, where_hz, fs_hjxx, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            mapInstance.newFeatureView(f_zd).queryFeature(table_qlr, where_zd, fs_bdcdy, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    List<Feature> fs_bdcdy = (List<Feature>) t_;
                                                    //有且只有一个以宗地创建的不动产单元
                                                    if (fs_bdcdy.size() == 1) {
                                                        String orid_path = AiUtil.GetValue(fs_bdcdy.get(0).getAttributes().get("ORID_PATH"), "");
                                                        if (orid_path.contains("[ZD]")) {
                                                            //已经生成了的，删除后再进行绑定
                                                            MapHelper.deleteFeature(fs_bdcdy.get(0), new AiRunnable() {
                                                                @Override
                                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                                    ((FeatureViewZD) mapInstance.newFeatureView(f_zd)).createBdcdy(f_zd, f_hz, new AiRunnable() {
                                                                        @Override
                                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                                            final Feature featureBdcdy = (Feature) t_;
                                                                            mapInstance.fillFeature(f_hz, featureBdcdy);
                                                                            for (Feature fs_hjxx : fs_hjxx) {
                                                                                mapInstance.fillFeature(fs_hjxx, f_hz);
                                                                            }
                                                                            List<Feature> fs_upt = new ArrayList<>();
                                                                            fs_upt.add(f_hz);
                                                                            fs_upt.add(f_zd);
                                                                            fs_upt.addAll(fs_hjxx);
                                                                            fs_upt.add(featureBdcdy);
                                                                            MapHelper.saveFeature(fs_upt, new AiRunnable() {
                                                                                @Override
                                                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                    return null;
                                                                                }
                                                                            });
                                                                            return null;
                                                                        }
                                                                    });
                                                                    return null;
                                                                }
                                                            });

                                                            // 更新不动产单元
                                                        }
                                                        //没有不动产单元的，以宗地创建不动产单元后进行绑定
                                                    } else if (fs_bdcdy.size() == 0) {
                                                        ((FeatureViewZD) mapInstance.newFeatureView(f_zd)).createBdcdy(f_zd, f_hz, new AiRunnable() {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                final Feature featureBdcdy = (Feature) t_;
                                                                mapInstance.fillFeature(f_hz, featureBdcdy);
                                                                for (Feature fs_hjxx : fs_hjxx) {
                                                                    mapInstance.fillFeature(fs_hjxx, f_hz);
                                                                }
                                                                List<Feature> fs_upt = new ArrayList<>();
                                                                fs_upt.add(f_hz);
                                                                fs_upt.add(f_zd);
                                                                fs_upt.addAll(fs_hjxx);
                                                                fs_upt.add(featureBdcdy);
                                                                MapHelper.saveFeature(fs_upt, new AiRunnable() {
                                                                    @Override
                                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                                        return null;
                                                                    }
                                                                });
                                                                return null;
                                                            }
                                                        });
                                                        //有多个的，不进行绑定操作
                                                    } else {
                                                        ToastMessage.Send("有多个不动产单元，绑定失败！");
                                                    }
                                                    return null;
                                                }
                                            });
                                            return null;
                                        }
                                    });
                                }
                                AiRunnable.Ok(getNext(), true, true);
                                return null;
                            }
                        });
                    } else {
                        AiRunnable.Ok(getNext(), true, true);
                    }
                }

                @Override
                public void complet() {
                    dialog.dismiss();
                    ToastMessage.Send("挂接成功！");
                }
            }.

                    start();
        } else {
            dialog.dismiss();
            ToastMessage.Send("导入的表中没有户主信息，无法进行挂接操作！");
        }
    }


    /**
     * 不动产单元与不动产进行挂接
     *
     * @param mapInstance
     */
    private void zngjBdcFromBdcdy(final MapInstance mapInstance) {
        final String funcdesc = "该功能将逐步完成不动产产单元与不动产挂接！";

        License.vaildfunc(mapInstance.activity, funcdesc, new AiRunnable() {
            final AiDialog aidialog = AiDialog.get(mapInstance.activity);

            void setMessage(final String message) {
                if (StringUtil.IsNotEmpty(message)) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastMessage.Send(message);
                            aidialog.setContentView(message);
                        }
                    });
                }
            }

            void addMessage(final String title, final String mssage) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        aidialog.addContentView(title, mssage);
                    }
                });
            }

            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                aidialog.setHeaderView(R.mipmap.app_icon_rgzl_blue, "不动产单元正在智能关联...")
                        .setContentView("注意：属于不可逆操作，如果您已经输出过成果，请注意备份谨慎处理！", funcdesc)
                        .setFooterView(AiDialog.CENCEL, "确定，我要继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                // 完成后的回掉
                                final AiRunnable callback = new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                aidialog.addContentView("处理数据完成，你可能还需要重新生成成果。");
                                                aidialog.setFooterView("重新生成成果", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }, null, null, "完成", null);
                                            }
                                        });
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ no(final T_ t_, Object... objects) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                aidialog.addContentView(AiRunnable.NO, t_ + "");
                                                aidialog.setFooterView(null, AiDialog.COLSE, null);
                                            }
                                        });
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ error(final T_ t_, Object... objects) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                aidialog.addContentView(AiRunnable.ERROR, t_ + "");
                                                aidialog.setFooterView(null, AiDialog.COLSE, null);
                                            }
                                        });
                                        return null;
                                    }
                                };
                                aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                                aidialog.setContentView("开始处理数据");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            {

                                                final List<Feature> fs_bdcdy = new ArrayList<>();
                                                final List<Feature> fs_upt = new ArrayList<>();
                                                MapHelper.Query(FeatureEdit.GetTable(mapInstance, FeatureHelper.TABLE_NAME_QLRXX, FeatureHelper.LAYER_NAME_QLRXX), "", -1, fs_bdcdy, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        // 1 查询所有的不动产单元
                                                        new AiForEach<Feature>(fs_bdcdy, new AiRunnable() {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                MapHelper.saveFeature(fs_upt, null);
                                                                AiRunnable.Ok(callback, null);
                                                                return null;
                                                            }
                                                        }) {
                                                            @Override
                                                            public void exec() {
                                                                // 2 根据不动产单元查找不动产
                                                                final Feature f_bdc = fs_bdcdy.get(postion);
                                                                String orid = FeatureHelper.Get(f_bdc, FeatureHelper.TABLE_ATTR_ORID, "");
                                                                String oridPath = FeatureHelper.Get(f_bdc, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                                                                if (TextUtils.isEmpty(orid)) {
                                                                    // TODO 2.1 不动产单元orid 为null 或 “”
                                                                    AiRunnable.Ok(getNext(), null);
                                                                } else if (TextUtils.isEmpty(oridPath)) {
                                                                    // TODO 2.2 查找不动产单元并关联
                                                                    final FeatureViewQLR fvBdc = (FeatureViewQLR) mapInstance.newFeatureView(f_bdc);
                                                                    fvBdc.queryBdc(new AiRunnable() {
                                                                        @Override
                                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                                            if (t_ != null && t_ instanceof Feature) {
                                                                                Feature f = (Feature) t_;
                                                                                fvBdc.fillFeature(f_bdc, f);
                                                                                fs_upt.add(f_bdc);
                                                                                fs_upt.add(f);
                                                                            }
                                                                            AiRunnable.Ok(getNext(), null);
                                                                            return null;
                                                                        }
                                                                    });
                                                                    // TODO 2.2.1 不动产单元orid_path 为 null 或 “”，没有与不动产关联
                                                                } else {
                                                                    // TODO 2.3 不动产单元 orid_path 不为空
                                                                    // TODO 2.3.1 不动产单元 orid_path 不为空,没有查找到对应的不动产
                                                                    AiRunnable.Ok(getNext(), null);
                                                                }

                                                            }
                                                        }.start();

                                                        return null;
                                                    }
                                                });

                                            }
                                        } catch (Exception e) {
                                            Log.d(TAG, "不动产单元智能挂接失败！" + e.getMessage());
                                        }
                                    }
                                }).start();
                            }
                        }).show();
                ;

                return null;
            }
        });
    }

    private void show_qzy_items() {
        AlertDialog.Builder builder = DialogBuilder.createAlertDialogBuilder(activity);
        final View view = View.inflate(activity, R.layout.app_ui_ai_aimap_too_qzy_item, null);
        final Dialog dialog = builder.setView(view).setCancelable(true).show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // 连接设备
        view.findViewById(R.id.ll_lj).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTClient.Search();
            }
        });
        // 打开目录
        view.findViewById(R.id.ll_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUtils.showChooser(activity, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String path = (String) t_;
                        try {
                            String str_zb = FileUtils.readFile(path) + "";
                            String[] lines = str_zb.split("\n");
                            final List<String[]> ls = new ArrayList<String[]>();
                            for (String line : lines) {
                                String[] vs = line.replace("/r", "").split(",");
                                if (vs.length > 3) {
                                    String[] l = new String[]{vs[0], vs[1].trim(), vs[2].trim(), vs[3].trim(), line};
                                    if (StringUtil.IsNotEmpty(l[1], l[2])) {
                                        ls.add(l);
                                    }
                                }
                            }

                            AiDialog dialog = AiDialog.get(activity, new QuickAdapter<String[]>(activity, R.layout.app_ui_ai_list_item, ls) {
                                @Override
                                protected void convert(BaseAdapterHelper helper, String[] item) {
                                    helper.setImageResource(R.id.v_icon, R.mipmap.app_map_layer_kzd);
                                    helper.setText(R.id.v_text, item[0]);
                                    helper.setText(R.id.v_desc, "北(N)：" + item[1] + "  东(E)：" + item[2] + " 高(Z)：" + item[3]);
                                    helper.setVisible(R.id.v_desc, true);
                                }
                            }).show();
                            dialog.setHeaderView(R.mipmap.app_icon_folder_blue, "获取到个" + ls.size() + "测量点");
                            ViewGroup v_zbx = (ViewGroup) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_tool_qzy_input_cld, dialog.getHeaderView());
                            final Spinner spn_zbx = (Spinner) v_zbx.findViewById(R.id.spn_zbx);
                            String resname = AiUtil.GetValue(view.getContentDescription(), "map_zbx");
                            final CharSequence[] strings = activity.getResources().getTextArray(ResourceUtil.getResourceId(activity, resname, "array"));
                            final ArrayAdapter adapter = new ArrayAdapter(activity, R.layout.app_ui_ai_aimap_feature_spn_item, strings);
                            spn_zbx.setAdapter(adapter);
                            dialog.setFooterView(AiDialog.CENCEL, "导入", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    // 授权功能检查
//                                         String funcdesc = "导入测量点，根据选择的坐标系进行转换，导入到测量点图层，便于图形";
//                                         License.vaildfunc(activity,funcdesc, new AiRunnable() {
//                                                     @Override
//                                                     public <T_> T_ ok(T_ t_, Object... objects) {
                                    inputCld(spn_zbx.getSelectedItem() + "", ls, new AiRunnable(activity) {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            dialog.dismiss();
                                            return null;
                                        }
                                    });
//
//                                         });
                                }
                            });
                        } catch (Exception es) {
                            // 文件读取失败
                            ToastMessage.Send(activity, "文件读取失败", es);
                        }
                        return null;
                    }
                });
            }
        });
    }

    /**
     * 导入控制点
     *
     * @param zbx      投影坐标系
     * @param ls       数据集，1、y、x、z 例 ：1,3339131.752,573089.173,74.307,
     * @param callback
     */
    private void inputCld(String zbx, List<String[]> ls, final AiRunnable callback) {
        FeatureLayer layer = MapHelper.getLayer(map, "KZD");
        final List<Feature> fs = new ArrayList<Feature>();
        String sel_zbx = AiUtil.GetValue(zbx, "");
        sel_zbx = StringUtil.substr(sel_zbx, sel_zbx.indexOf("[") + 1, sel_zbx.indexOf("]"));
        int wkid = AiUtil.GetValue(sel_zbx, map.getSpatialReference().getWkid());
        for (String[] l : ls) {
            String name = l[0];
            double x = AiUtil.GetValue(l[2], 0d);
            double y = AiUtil.GetValue(l[1], 0d);
            double z = AiUtil.GetValue(l[3], 0d);
            String bz = l[4];
            if (x > 0 && y > 0) {
                Feature f = layer.getFeatureTable().createFeature();
                f.getAttributes().put("MC", name);
                f.getAttributes().put("XZB", x);
                f.getAttributes().put("YZB", y);
                f.getAttributes().put("ZZB", z);
                f.getAttributes().put("BZ", bz);
                f.setGeometry(new Point(x, y, MapHelper.GetSpatialReference(mapInstance, wkid)));
                fs.add(f);
            }
        }
        MapHelper.saveFeature(fs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                MapHelper.selectAddCenterFeature(map, fs);
                ToastMessage.Send(activity, "导入成功" + fs.size() + "个点");
                AiRunnable.Ok(callback, t_);
                return null;
            }
        });
    }

    ///endregion
    //region 内部类或接口
    ///endregion

    //region 导入dxf
    // 导入两权数据
    private void show_lqsj_items() {

        final AiDialog dialog = AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_zd_fw, "导入两权数据")
                .addContentView("可以从SHP、CAD中导入两权数据，请选择文件格式");

        final Map<String, Object> dataconfig = new HashMap<>();
        dialog.addContentView(dialog.getSelectView("操作模式", "map_modle", dataconfig, "modle"));
        dialog.addContentView(dialog.getSelectView("坐标系", "map_zbx", dataconfig, "zbx"));
        dialog.addContentView(dialog.getSelectView("字符集", "file_encode", dataconfig, "encode"));

        dialog.addContentViewMenu(Arrays.asList(
                new Object[]{R.mipmap.app_icon_shp, "导入shape", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToastMessage.Send("功能正在建设中...");


                    }
                }},
                new Object[]{R.mipmap.app_icon_cad, "导入DXF文件", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        final String modle = AiUtil.GetValue(dataconfig.get("modle"), "");
                        FileUtils.showChooser(activity, new AiRunnable() {
                            void setMessage(final String message) {
                                if (StringUtil.IsNotEmpty(message)) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastMessage.Send(message);
                                            dialog.setContentView(message);
                                        }
                                    });
                                }
                            }

                            void addMessage(final String title, final String mssage) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.addContentView(title, mssage);
                                    }
                                });
                            }

                            void setComplete(final int icon, final String text, final boolean cancelable) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.setCancelable(cancelable);
                                        dialog.setFooterView(dialog.getStateView(icon, text));
                                        addMessage(text, "");

                                    }
                                });
                            }

                            void save(final Map<String, Integer> map_fs, List<Feature> features, final boolean isComplete) {
                                final List<Feature> fs = new ArrayList<>(features);
                                features.clear();
                                String messge_ = "";
                                for (String key : map_fs.keySet()) {
                                    messge_ += "[" + key + "：" + map_fs.get(key) + "]";
                                }
                                final String messge = messge_;
                                if (GRAPHICS_REPLACE.equals(modle)) {
                                    final List<Feature> fs_upt = new ArrayList<>();
                                    new AiForEach<Feature>(fs, null) {
                                        @Override
                                        public void exec() {
                                            Feature mFeature = fs.get(postion);
                                            Geometry g = mFeature.getGeometry();
                                            MapHelper.GraphicReplacement(mapInstance, FeatureHelper.TABLE_NAME_ZD, g, fs_upt, getNext());
                                        }

                                        @Override
                                        public void complet() {
                                            MapHelper.saveFeature(fs_upt, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    addMessage("", AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "替换" + fs_upt.size() + "个图形成功:" + messge + "...");
                                                    if (isComplete)
                                                        setComplete(R.mipmap.app_icon_ok_f, "替换" + messge + "成功！", true);
                                                    return null;
                                                }

                                                @Override
                                                public <T_> T_ error(T_ t_, Object... objects) {
                                                    addMessage("替换失败:", ((Exception) t_).getMessage());
                                                    if (isComplete)
                                                        setComplete(R.mipmap.app_icon_error_smaller, "替换失败", true);
                                                    return null;
                                                }
                                            });

                                        }
                                    }.start();
                                } else {
                                    MapHelper.saveFeature(fs, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            addMessage("", AiUtil.GetValue(new Date(), AiUtil.F_TIME) + "导入" + fs.size() + "个图形成功:" + messge + "...");
                                            if (isComplete)
                                                setComplete(R.mipmap.app_icon_ok_f, "导入" + messge + "成功！", true);
                                            return null;
                                        }

                                        @Override
                                        public <T_> T_ error(T_ t_, Object... objects) {
                                            addMessage("导入失败:", ((Exception) t_).getMessage());
                                            if (isComplete)
                                                setComplete(R.mipmap.app_icon_error_smaller, "导入失败", true);
                                            return null;
                                        }
                                    });

                                }

                            }

                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                String message = "";
                                try {
                                    final String path = (String) t_;
                                    String sel_zbx = AiUtil.GetValue(dataconfig.get("zbx"), "");
                                    sel_zbx = StringUtil.substr(sel_zbx, sel_zbx.indexOf("[") + 1, sel_zbx.indexOf("]"));

                                    final int wkid = AiUtil.GetValue(sel_zbx, map.getSpatialReference().getWkid());

                                    final String encode = AiUtil.GetValue(dataconfig.get("encode"), "");
                                    mapStbm = new HashMap<>();
                                    if (path.toLowerCase().endsWith(".dxf")) {
                                        dialog.setCancelable(false)
                                                .setFooterView(dialog.getProgressView("正在导入数据..."))
                                                .setContentView("正在读取dxf内容导入中，所耗时间取决于导入文件的大小，请耐心等待...");

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    FeatureTable table_zd = MapHelper.getLayer(map, FeatureHelper.TABLE_NAME_ZD).getFeatureTable();
                                                    FeatureTable table_ljz = MapHelper.getLayer(map, FeatureHelper.TABLE_NAME_LJZ).getFeatureTable();
//                                                  FeatureTable table_zrz = MapHelper.getLayer(map, FeatureHelper.TABLE_NAME_ZRZ).getFeatureTable();
                                                    FeatureTable table_z_fsjg = MapHelper.getLayer(map, FeatureHelper.TABLE_NAME_Z_FSJG).getFeatureTable();
                                                    FeatureTable table_h_fsjg = MapHelper.getLayer(map, FeatureHelper.TABLE_NAME_H_FSJG).getFeatureTable();
                                                    FeatureTable table_zjd = MapHelper.getLayer(map, FeatureHelper.TABLE_NAME_ZJD).getFeatureTable();
                                                    FeatureTable table_cld = MapHelper.getLayer(map, "KZD").getFeatureTable();
                                                    FeatureTable table_dzdw = MapHelper.getLayer(map, "DZDW").getFeatureTable();
                                                    FeatureTable table_xzdw = MapHelper.getLayer(map, "XZDW").getFeatureTable();
                                                    FeatureTable table_mzdw = MapHelper.getLayer(map, "MZDW").getFeatureTable();
                                                    final List<Feature> fs = new ArrayList<>();
                                                    final Map<String, Integer> map_fs = new HashMap<>();
                                                    DxfAdapter dxf = new DxfAdapter().open(path);
                                                    DxfFeature f;
                                                    int index = 0;
                                                    while ((f = dxf.readNext(encode)) != null) {
                                                        try {
                                                            String stdm = f.getExtendeds(0);
                                                            if (StringUtil.IsEmpty(stdm))
                                                                continue;

                                                            if (index >= 100) {
                                                                save(map_fs, fs, false);
                                                                index = 0;
                                                            }
                                                            index++;
                                                            Log.d(TAG, "：-> " + f.toString());
                                                            if (GRAPHICS_REPLACE.equals(modle)) {
                                                                if (readZd(table_zd, fs, map_fs, f, wkid)) {
                                                                    continue;
                                                                }
                                                            } else {
                                                                if (readZd(table_zd, fs, map_fs, f, wkid)) {
                                                                    continue;
                                                                }
                                                                if (readLjz(table_ljz, fs, map_fs, f, wkid)) {
                                                                    continue;
                                                                }
//                                                           if(readZrz(table_zrz,fs,map_fs,f,wkid)){ continue;  }
                                                                if (readFSJG(table_z_fsjg, table_h_fsjg, fs, map_fs, f, wkid)) {
                                                                    continue;
                                                                }
                                                                if (readCLD(table_cld, fs, map_fs, f, wkid)) {
                                                                    continue;
                                                                }
                                                                if (readDw(table_dzdw, table_xzdw, table_mzdw, fs, map_fs, f, wkid)) {
                                                                    continue;
                                                                }
                                                                if (readZJD(table_zjd, fs, map_fs, f, wkid)) {
                                                                    continue;
                                                                }
                                                                if ("JZD".equals(f.Layer) && (f.SubClasses + "").contains("Polyline")) {
                                                                    Log.d(TAG, f.getExtendeds(0) + "： -> 【未处理】" + f.toString());
                                                                }
                                                            }


                                                            index--;
                                                        } catch (Exception es) {
                                                            addMessage("", "读取实体失败,跳过：" + es.getMessage());
                                                        }
                                                    }
                                                    save(map_fs, fs, true);
                                                } catch (Exception es) {
                                                    addMessage("解析文件失败!", es.getMessage());
                                                    setComplete(R.mipmap.app_icon_error_smaller, "解析文件失败", true);
                                                }
                                            }
                                        }).start();
                                    } else {
                                        ToastMessage.Send("文件格式不正确");
                                    }
                                } catch (Exception es) {
                                    setMessage("读取文件失败：" + es.getMessage());
                                    setComplete(R.mipmap.app_icon_error_smaller, "读取文件失败", true);
                                }
                                return null;
                            }
                        });
                    }
                }}))
                .addContentView("导入SHP数据：",
                        "1、导入SHP文件时数据导入到文件名称与图层名称一致的图层中，如：宗地.shp 导入到宗地图层",
                        "2、shp的字符集为标准UTF8格式"
                )
                .addContentView("导入CAD数据：",
                        "1、支持的CAD文件时数据为.dxf文件，如果是.dwg文件请事先转换为.dxf文件",
                        "2、dxf文件为二权标准成果文件dwg转换的dxf文件",
                        "3、文件中对应的实体编码会导入到相应的图层"
                ).show();

    }

    private boolean isEmptyStdm(DxfFeature f, String stdm) {
        for (String s : f.getExtendeds()) {
            if (StringUtil.IsNotEmpty(s) && StringUtil.IsNotEmpty(stdm)) {
                if (stdm.equals(s)) {
                    return true;
                }
                ;
            }
        }
        return false;
    }

    private boolean isEmptyStdm(DxfFeature
                                        f, Map<String, String> mapStbm, List<String> stdms) {
        for (String s : f.getExtendeds()) {
            if (StringUtil.IsNotEmpty(s)) {
                if (stdms.contains(s)) {
                    mapStbm.put(STBM, s);
                    return true;
                }
                ;
            }
        }
        return false;
    }

    private boolean isEmptyStdm(DxfFeature
                                        f, Map<String, String> mapStbm, Map<String, String> stdms) {
        for (String s : f.getExtendeds()) {
            if (StringUtil.IsNotEmpty(s)) {
                if (stdms.containsKey(s)) {
                    mapStbm.put(STBM, s);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEmptyStdm(DxfFeature f, String stdm, String s) {
        if (StringUtil.IsNotEmpty(stdm) && s.equals(stdm)) {
            return true;
        }
        for (String v : f.getExtendeds()) {
            if (StringUtil.IsNotEmpty(s)) {
                if (s.equals(v)) {
                    stdm = s;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLc(String value) {
        try {
            if (value.contains(".")) {
                if (Float.parseFloat(value) < 99) {
                    return true;
                }
            } else {
                if (Integer.parseInt(value) < 99) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public boolean readZd(FeatureTable
                                  table, List<Feature> fs, Map<String, Integer> map_fs, DxfFeature f, int wkid) {
        String name = FeatureHelper.LAYER_NAME_ZD;
//        if ("JZD".equals(f.Layer) && (f.SubClasses + "").contains("Polyline")) {
        //  300000 宗地
        String stdm = f.getExtendeds(0);
        boolean flag = isEmptyStdm(f, stdm, "300000");// 是否包含实体编码
            if (flag) {
                Geometry g = GdalAdapter.convert(f.getGeometry());
                g = MapHelper.geometry_get(g, MapHelper.GetSpatialReference(mapInstance, wkid));
                g = MapHelper.geometry_new(GeometryType.POLYGON, ((Polyline) g).getParts());
                if (g == null || !FeatureHelper.isPolygonGeometryValid(g)) {
                    return false;
                }
                Feature f_zd = table.createFeature();
                // 实体编码为 300000
                for (String value : f.getExtendeds()) {
                    if (TextUtils.isEmpty(value)) {
                        continue;
                    }
                    if (TextUtils.isEmpty(FeatureHelper.Get(f_zd, "TFH", ""))
                            && value.contains("-") && value.contains(".")
                            && !StringUtil.isChinese(value)) {
                        FeatureHelper.Set(f_zd, "TFH", value); // 宗地代码
                        continue;
                    }

                    if (TextUtils.isEmpty(FeatureHelper.Get(f_zd, "QLRXM", ""))
                            && StringUtil.isQLRName(value)) {
                        FeatureHelper.Set(f_zd, "QLRXM", value);
                        continue;
                    }

                    if (TextUtils.isEmpty(FeatureHelper.Get(f_zd, "ZL", ""))
                            && value.contains("镇") && value.contains("村")) {
                        FeatureHelper.Set(f_zd, "ZL", value); // 宗地代码
                        continue;
                    }
                    if (value.contains("北至") && value.contains(FeatureHelper.LAYER_NAME_ZD)) {
                        FeatureHelper.Set(f_zd, "ZDSZB", value); // 宗地代码
                        continue;
                    }
                    if (value.contains("东至") && value.contains(FeatureHelper.LAYER_NAME_ZD)) {
                        FeatureHelper.Set(f_zd, "ZDSZD", value); // 宗地代码
                        continue;
                    }
                    if (value.contains("西至") && value.contains(FeatureHelper.LAYER_NAME_ZD)) {
                        FeatureHelper.Set(f_zd, "ZDSZX", value); // 宗地代码
                        continue;
                    }
                    if (value.contains("南至") && value.contains(FeatureHelper.LAYER_NAME_ZD)) {
                        FeatureHelper.Set(f_zd, "ZDSZN", value); // 宗地代码
                        continue;
                    }
                    String valueUp = value.toUpperCase();
                    if (TextUtils.isEmpty(FeatureHelper.Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, ""))) {
                        if (valueUp.contains("JC") && !valueUp.contains("F")) {
                            String s0 = StringUtil.substr(valueUp, 0, valueUp.indexOf("JC") + 2);
                            valueUp = s0 + String.format("%05d", Integer.parseInt(StringUtil.substr(valueUp, valueUp.indexOf("JC") + 2)));
                            FeatureHelper.Set(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, valueUp);
                            FeatureHelper.Set(f_zd, "QLLX", "6");
                            continue;
                        } else if (valueUp.contains("JB")&& !valueUp.contains("F")) {
                            String s0 = StringUtil.substr(valueUp, 0, valueUp.indexOf("JB") + 2);
                            valueUp = s0 + String.format("%05d", Integer.parseInt(StringUtil.substr(valueUp, valueUp.indexOf("JB") + 2)));
                            FeatureHelper.Set(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, valueUp);
                            FeatureHelper.Set(f_zd, "QLLX", "7");
                            continue;

                        } else if (valueUp.contains("GB")&& !valueUp.contains("F")) {
                            String s0 = StringUtil.substr(valueUp, 0, valueUp.indexOf("GB") + 2);
                            valueUp = s0 + String.format("%05d", Integer.parseInt(StringUtil.substr(valueUp, valueUp.indexOf("GB") + 2)));
                            FeatureHelper.Set(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, valueUp);
                            FeatureHelper.Set(f_zd, "QLLX", "4");
                            continue;

                        }
                    }
                }
                f_zd.setGeometry(MapHelper.Geometry_get(g));
                FeatureHelper.Set(f_zd, "PZYT", "0702");
                FeatureHelper.Set(f_zd, "YT", "0702");
                FeatureHelper.Set(f_zd, "SJYT", "0702");
                mapInstance.fillFeature(f_zd);
                new FeatureViewZD().fillFeature(f_zd);
                fs.add(f_zd);
                map_fs.put(name, AiUtil.GetValue(map_fs.get(name), 0) + 1);
                return true;
            } else {
                return false;
            }
    }

    public boolean readZdAndReplace(FeatureTable
                                            table, List<Feature> fs, Map<String, Integer> map_fs, DxfFeature f, int wkid) {
        String name = FeatureHelper.LAYER_NAME_ZD;
        if ("JZD".equals(f.Layer) && (f.SubClasses + "").contains("Polyline")) {
            //  300000 宗地
            String stdm = f.getExtendeds(0);
            boolean flag = isEmptyStdm(f, stdm, "300000");// 是否包含实体编码
            if (flag) {
                Geometry g = GdalAdapter.convert(f.getGeometry());
                g = MapHelper.geometry_get(g, MapHelper.GetSpatialReference(mapInstance, wkid));
                g = MapHelper.geometry_new(GeometryType.POLYGON, ((Polyline) g).getParts());
                if (g == null || !FeatureHelper.isPolygonGeometryValid(g)) {
                    return false;
                }

                MapHelper.GraphicReplacement(mapInstance, FeatureHelper.TABLE_NAME_ZD, g, fs, null);
//                MapHelper.Geometry_get(g);

//                fs.add(f_zd);
                map_fs.put(name, AiUtil.GetValue(map_fs.get(name), 0) + 1);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }


    public boolean readZrz(FeatureTable
                                   table, List<Feature> fs, Map<String, Integer> map_fs, DxfFeature f, int wkid) {

        String name = "幢";
        List<String> stdms = Arrays.asList("141101,141111,141121,141131,141141,141151,141161,141104,141103,141200,141300,141400".split(","));

//        141101	0	一般房屋
//        141111	砼	砼房屋
//        141121	砖	砖房屋
//        141131	铁	铁房屋
//        141141	钢	钢房屋
//        141151	木	木房屋
//        141161	混	混房屋
//        141104	411b	突出房屋
//        141103	411b	1:2000房屋
//        141200	简	简单房屋
//        141300	建	建筑房屋
//        141400	破	破坏房屋
        boolean flag = isEmptyStdm(f, mapStbm, stdms);// 是否包含实体编码
        if (flag) {
            String stdm = mapStbm.get(STBM);
            Geometry g = GdalAdapter.convert(f.getGeometry());
            g = MapHelper.geometry_get(g, MapHelper.GetSpatialReference(mapInstance, wkid));
            if (g instanceof Polyline) {
                if (MapHelper.geometry_isclose(g) || "JMD".equals(f.Layer) && (f.SubClasses + "").contains("Polyline")) {
//        if ("JMD".equals(f.Layer) && (f.SubClasses + "").contains("Polyline") && flag) {
                    // 幢
                    g = MapHelper.geometry_new(GeometryType.POLYGON, ((Polyline) g).getParts());
                    if (g != null) {
                        Feature f_zrz = table.createFeature();
                        FeatureHelper.Set(f_zrz, "ZCS", f.getExtendeds(1)); // 层数
                        FeatureHelper.Set(f_zrz, "LSZD", f.getExtendeds(2)); //隶属宗地（地籍号）
                        FeatureHelper.Set(f_zrz, "ZLDZ", f.getExtendeds(3)); // 坐落地址
                        FeatureHelper.Set(f_zrz, "SCJZMJ", f.getExtendeds(4)); // 建筑面积
                        FeatureHelper.Set(f_zrz, "ZZDMJ", f.getExtendeds(5));// 占地面积

                        if ("141111".equalsIgnoreCase(stdm)) {
                            // 砼 房屋
                            FeatureHelper.Set(f_zrz, "FWJG", "A");// 房屋结构 [A][砼]钢筋混凝土结构
                        } else if ("141121".equalsIgnoreCase(stdm)) {
                            // 砖 房屋
                            FeatureHelper.Set(f_zrz, "FWJG", "C");// 房屋结构 [C][砖]砖木结构
                        } else if ("141131".equalsIgnoreCase(stdm)) {
                            //  铁房子
                            FeatureHelper.Set(f_zrz, "FWJG", "M");// 房屋结构 [M][砼]钢结构
                        } else if ("141141".equalsIgnoreCase(stdm)) {
                            //  钢房屋
                            FeatureHelper.Set(f_zrz, "FWJG", "M");// 房屋结构 [M][砼]钢结构
                        } else if ("141151".equalsIgnoreCase(stdm)) {
                            //  木房子
                            FeatureHelper.Set(f_zrz, "FWJG", "T");// [T][土]土木结构
                        } else if ("141161".equalsIgnoreCase(stdm)) {
                            //  混房子
                            FeatureHelper.Set(f_zrz, "FWJG", "B");// [B][混]混合结构
                        } else if ("141200".equalsIgnoreCase(stdm)) {
                            // 简 房屋
                            FeatureHelper.Set(f_zrz, "FWJG", "简");// 房屋结构 [简][简]简单房屋
                        } else if ("141300".equalsIgnoreCase(stdm)) {
                            // 建筑中房屋
                            FeatureHelper.Set(f_zrz, "FWJG", "建");// [建][建]建筑中房屋
                        } else if ("141400".equalsIgnoreCase(stdm)) {
                            // 破坏房屋
                            FeatureHelper.Set(f_zrz, "FWJG", "破");// 房屋结构 [破][破]破坏房屋
                        } else {
                            //141101	0	一般房屋
                            FeatureHelper.Set(f_zrz, "FWJG", "其");// 房屋结构 [C][砖]砖木结构
                        }

                        f_zrz.setGeometry(g);
                        mapInstance.fillFeature(f_zrz);
                        fs.add(f_zrz);
                        map_fs.put(name, AiUtil.GetValue(map_fs.get(name), 0) + 1);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean readLjz(FeatureTable
                                   table, List<Feature> fs, Map<String, Integer> map_fs, DxfFeature f, int wkid) {

        String name = FeatureHelper.LAYER_NAME_LJZ;
        List<String> stdms = Arrays.asList("141500,141101,141111,141121,141131,141141,141151,141161,141104,141103,141200,141300,141400".split(","));

//        141101	0	一般房屋
//        141111	砼	砼房屋
//        141121	砖	砖房屋
//        141131	铁	铁房屋
//        141141	钢	钢房屋
//        141151	木	木房屋
//        141161	混	混房屋
//        141104	411b	突出房屋
//        141103	411b	1:2000房屋
//        141200	简	简单房屋
//        141300	建	建筑房屋
//        141400	破	破坏房屋
        boolean flag = isEmptyStdm(f, mapStbm, stdms);
//        if ("JMD".equals(f.Layer) && (f.SubClasses + "").contains("Polyline") && flag)
        if (flag) {
            // 幢
            String stdm = mapStbm.get(STBM);
            Geometry g = GdalAdapter.convert(f.getGeometry());
            g = MapHelper.geometry_get(g, MapHelper.GetSpatialReference(mapInstance, wkid));
            g = MapHelper.geometry_new(GeometryType.POLYGON, ((Polyline) g).getParts());
            if (g != null && FeatureHelper.isPolygonGeometryValid(g)) {
                Feature f_ljz = table.createFeature();
                String fwjg1 = "FWJG1";
                String fwjg = "FWJG";
                for (String value : f.getExtendeds()) {
                    if (isLc(value)) {
                        FeatureHelper.Set(f_ljz, "ZCS", value); // 层数
                        break;
                    }
                }

                if ("141111".equalsIgnoreCase(stdm)) {
                    // 砼 房屋
                    FeatureHelper.Set(f_ljz, fwjg1, "3");// 房屋结构 [A][砼]钢筋混凝土结构
                    FeatureHelper.Set(f_ljz, "FWJG", "砼");
                } else if ("141121".equalsIgnoreCase(stdm)) {
                    // 砖 房屋
                    FeatureHelper.Set(f_ljz, fwjg1, "5");// 房屋结构 [C][砖]砖木结构
                    FeatureHelper.Set(f_ljz, "FWJG", "砖");
                } else if ("141131".equalsIgnoreCase(stdm)) {
                    //  铁房子
                    FeatureHelper.Set(f_ljz, fwjg1, "铁");// 房屋结构 [铁][铁]铁结构
                    FeatureHelper.Set(f_ljz, "FWJG", "铁");

                } else if ("141141".equalsIgnoreCase(stdm)) {
                    //  钢房屋
                    FeatureHelper.Set(f_ljz, fwjg1, "1");// 房屋结构 [1][钢]铁结构
                    FeatureHelper.Set(f_ljz, "FWJG", "钢");

                } else if ("141151".equalsIgnoreCase(stdm)) {
                    //  木房子
                    FeatureHelper.Set(f_ljz, fwjg1, "木");// [T][土]土木结构
                    FeatureHelper.Set(f_ljz, "FWJG", "木");
                } else if ("141161".equalsIgnoreCase(stdm)) {
                    //  混房子
                    FeatureHelper.Set(f_ljz, fwjg1, "4");// [B][混]混合结构
                    FeatureHelper.Set(f_ljz, "FWJG", "混");

                } else if ("141200".equalsIgnoreCase(stdm)) {
                    // 简 房屋
                    FeatureHelper.Set(f_ljz, fwjg1, "简");// 房屋结构 [简][简]简单房屋
                    FeatureHelper.Set(f_ljz, "FWJG", "简");
                } else if ("141300".equalsIgnoreCase(stdm)) {
                    // 建筑中房屋
                    FeatureHelper.Set(f_ljz, fwjg1, "建");// [建][建]建筑中房屋
                    FeatureHelper.Set(f_ljz, "FWJG", "建");
                } else if ("141400".equalsIgnoreCase(stdm)) {
                    // 破坏房屋
                    FeatureHelper.Set(f_ljz, fwjg1, "破");// 房屋结构 [破][破]破坏房屋
                    FeatureHelper.Set(f_ljz, "FWJG", "破");
                } else if ("141500".equalsIgnoreCase(stdm)) {
                    // 破坏房屋
                    FeatureHelper.Set(f_ljz, fwjg1, "棚");// 房屋结构 [棚][棚]棚房
                    FeatureHelper.Set(f_ljz, "FWJG", "棚房");
                } else {
                    //141101	0	一般房屋
                    FeatureHelper.Set(f_ljz, fwjg1, "6");// 其他房屋
                    FeatureHelper.Set(f_ljz, "FWJG", "其");
                }
                f_ljz.setGeometry(g);
                mapInstance.fillFeature(f_ljz);
                fs.add(f_ljz);
                map_fs.put(name, AiUtil.GetValue(map_fs.get(name), 0) + 1);
                return true;
            }
        }
        return false;
    }

    public boolean readFSJG(FeatureTable table_z, FeatureTable
            table_h, List<Feature> fs, Map<String, Integer> map_fs, DxfFeature f, int wkid) {

        String name = "幢附属";
        List<String> stdms = Arrays.asList("143130,141510,141600,141700,141800,143111,143112,143800,140001,143131,143140".split(","));
        List<String> zStdm = Arrays.asList("143130,141510,141600,141700,143111,143112,143800,143131,143140".split(","));
        List<String> hStdms = Arrays.asList("141800,140001".split(","));

//        141500	0	棚房  逻辑幢
//        143130        檐廊
//        141510	0	无墙的棚房
//        141600	0	架空房屋
//        141700	0	廊房
//        141800	0	飘楼

//        143111	0	无墙壁柱廊
//        143112	0	柱廊有墙壁边
//        143800	0	门顶雨罩
//        140001	0	阳台
//        143130	0	檐廊
//        143131	0	挑廊
//        143140	0	悬空通廊

        boolean flag = isEmptyStdm(f, mapStbm, stdms);// 是否包含实体编码

//        if (("JMD".equals(f.Layer) || "FWFS".equals(f.Layer)) && (f.SubClasses + "").contains("Polyline") && flag) {
        if ( flag) {
            String stdm = mapStbm.get(STBM);
            Geometry g = GdalAdapter.convert(f.getGeometry());
            g = MapHelper.geometry_get(g, MapHelper.GetSpatialReference(mapInstance, wkid));
            g = MapHelper.geometry_new(GeometryType.POLYGON, ((Polyline) g).getParts());
            if (FeatureHelper.isPolygonGeometryValid(g)) {
                Feature ff = null;
                if (zStdm.contains(stdm)) {
                    ff = table_h.createFeature();
//                    ff = table_z.createFeature();
                    if ("141500".equalsIgnoreCase(stdm)) {
                        // 棚房 房屋附属
                        FeatureHelper.Set(ff, "FHMC", "棚房");
                        FeatureHelper.Set(ff, "TYPE", "0");// 核算面积类型 0 不算面积
                    } else if ("143130".equalsIgnoreCase(stdm)) {
                        FeatureHelper.Set(ff, "FHMC", "檐廊");
                        FeatureHelper.Set(ff, "TYPE", "0");// 核算面积类型 0 不算面积
                    } else if ("143111".equalsIgnoreCase(stdm)) {
                        FeatureHelper.Set(ff, "FHMC", "柱廊");
                        FeatureHelper.Set(ff, "TYPE", "0");// 核算面积类型 0 不算面积
                    } else if ("143112".equalsIgnoreCase(stdm)) {
                        FeatureHelper.Set(ff, "FHMC", "柱廊");
                        FeatureHelper.Set(ff, "TYPE", "0");// 核算面积类型 0 不算面积
                    } else if ("143131".equalsIgnoreCase(stdm)) {
                        FeatureHelper.Set(ff, "FHMC", "挑廊");
                        FeatureHelper.Set(ff, "TYPE", "0");// 核算面积类型 0 不算面积
                    } else if ("143140".equalsIgnoreCase(stdm)) {
                        FeatureHelper.Set(ff, "FHMC", "悬空通廊");
                        FeatureHelper.Set(ff, "TYPE", "0");// 核算面积类型 0 不算面积
                    } else if ("141700".equalsIgnoreCase(stdm)) {
                        FeatureHelper.Set(ff, "FHMC", "廊房");
                        FeatureHelper.Set(ff, "TYPE", "0");// 核算面积类型 0 不算面积
                    } else if ("143800".equalsIgnoreCase(stdm)) {
                        FeatureHelper.Set(ff, "FHMC", "门顶");
                        FeatureHelper.Set(ff, "TYPE", "0");// 核算面积类型 0 不算面积
                    } else {
                        FeatureHelper.Set(ff, "FHMC", "柱廊");
                        FeatureHelper.Set(ff, "TYPE", "0");// 核算面积类型 0 不算面积
                    }
                } else {
                    ff = table_h.createFeature();
                    if ("141800".equalsIgnoreCase(stdm)) {
                        FeatureHelper.Set(ff, "FHMC", "飘楼");
                        FeatureHelper.Set(ff, "TYPE", "0");// 核算面积类型 0 不算面积
                    } else if ("140001".equalsIgnoreCase(stdm)) {
                        FeatureHelper.Set(ff, "FHMC", "阳台");
                        FeatureHelper.Set(ff, "TYPE", "0");// 核算面积类型 0 不算面积
                    }
                }

                FeatureHelper.Set(ff, "FHDM", stdm);
                FeatureHelper.Set(ff, "SZC", 1);
                ff.setGeometry(g);
                mapInstance.fillFeature(ff);
                fs.add(ff);
                map_fs.put(name, AiUtil.GetValue(map_fs.get(name), 0) + 1);
                return true;
            }
        }
        return false;
    }

    public boolean readCLD(FeatureTable
                                   table, List<Feature> fs, Map<String, Integer> map_fs, DxfFeature f, int wkid) {
        String name = "测量点";
        Map<String, String> stdms = DxfFeature.GetCassSTDM_KZD();

        boolean flag = isEmptyStdm(f, mapStbm, stdms);// 是否包含实体编码
        if ((f.SubClasses + "").contains("Text") && flag) {
            String stdm = mapStbm.get(STBM);
            Geometry g = GdalAdapter.convert(f.getGeometry());
            if (g != null && g instanceof Point) {
                g = MapHelper.geometry_get(g, MapHelper.GetSpatialReference(mapInstance, wkid));
                Feature ff = table.createFeature();
                FeatureHelper.Set(ff, "MC", stdms.get(stdm));
                FeatureHelper.Set(ff, "XZB", ((Point) g).getX());
                FeatureHelper.Set(ff, "YZB", ((Point) g).getY());
                FeatureHelper.Set(ff, "BZ", f.ExtendedEntity);
                ff.setGeometry(g);
                FeatureHelper.Set(ff, "FHDM", stdm);
                mapInstance.fillFeature(ff);
                fs.add(ff);
                map_fs.put(name, AiUtil.GetValue(map_fs.get(name), 0) + 1);
                return true;
            }
        }
        return false;
    }


    public boolean readZJD(FeatureTable
                                   table, List<Feature> fs, Map<String, Integer> map_fs, DxfFeature f, int wkid) {
        String name = "附属结构注记";
//        boolean flag = isEmptyStdm(f, mapStbm, stdms);// 是否包含实体编码
//        if ("H_FSJG".equals(f.Layer)&&((f.SubClasses + "").contains("Text")) && flag) {
        if ("H_FSJG".equals(f.Layer)) {
            String stdm = mapStbm.get(STBM);
            Geometry g = GdalAdapter.convert(f.getGeometry());
            if (g != null && g instanceof Point) {
                g = MapHelper.geometry_get(g, MapHelper.GetSpatialReference(mapInstance, wkid));
                String des = f.Text; // 1,门斗,1;2,飘楼,1
                Feature ff = table.createFeature();
                ff.setGeometry(g);
                FeatureHelper.Set(ff, "TITLE", des);
                FeatureHelper.Set(ff, "CONTENT", des);
                FeatureHelper.Set(ff, "FHMC", "H_FSJG_ZJ");
                FeatureHelper.Set(ff, "FHDM", f.ExtendedEntity);
                mapInstance.fillFeature(ff);
                fs.add(ff);
                map_fs.put(name, AiUtil.GetValue(map_fs.get(name), 0) + 1);
                return true;
            }
        }
        return false;
    }

    public boolean readDw(FeatureTable table_d, FeatureTable table_x, FeatureTable
            table_m, List<Feature> fs, Map<String, Integer> map_fs, DxfFeature f, int wkid) {
        String name = "地物";
        Map<String, String> stdms = DxfFeature.GetCassSTDM_DW();

        boolean flag = isEmptyStdm(f, mapStbm, stdms);// 是否包含实体编码
        if (flag) {
            String stdm = mapStbm.get(STBM);
            Geometry g = GdalAdapter.convert(f.getGeometry());
            g = MapHelper.geometry_get(g, MapHelper.GetSpatialReference(mapInstance, wkid));
            if (g != null) {
                Feature ff = null;
                if (g instanceof Point) {
                    ff = table_d.createFeature();
                } else if (g instanceof Polygon && FeatureHelper.isPolygonGeometryValid(g)) {
                    ff = table_m.createFeature();
                } else if (g instanceof Polyline && MapHelper.geometry_getPoints(g).size() > 1) {
                    if (MapHelper.geometry_isclose(g)) {
                        g = MapHelper.geometry_new(GeometryType.POLYGON, ((Polyline) g).getParts());
                        if (g instanceof Polygon && FeatureHelper.isPolygonGeometryValid(g)) {
                            ff = table_m.createFeature();
                        } else {
                            return false;
                        }
                    } else {
                        ff = table_x.createFeature();
                    }
                } else {
                    return false;
                }
                FeatureHelper.Set(ff, "FHMC", stdms.get(stdm));
                FeatureHelper.Set(ff, "FHDM", stdm);
                FeatureHelper.Set(ff, "DWMC", stdms.get(stdm));
                ff.setGeometry(g);
                mapInstance.fillFeature(ff);
                fs.add(ff);
                map_fs.put(name, AiUtil.GetValue(map_fs.get(name), 0) + 1);
                return true;
            }
        }
        return false;
    }

    ///endregion

}
