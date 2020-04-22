package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.layers.Layer;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.LayerConfig;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.DialogBuilder;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewQLR extends FeatureView {

    //region 常量
    ///endregion

    //region 字段
    ///endregion

    //region 构造函数
    ///endregion

    //region 重写函数和回调
    @Override
    public void fillFeature(Feature feature) {
        super.fillFeature(feature);
        String id = FeatureHelper.Get(feature, "QLRBM", "");
        String xm = FeatureHelper.Get(feature, "XM", "");
        FeatureHelper.Set(feature, "QLRMC", xm, false, true);
        FeatureHelper.Set(feature, "YHZGX", "户主", false, false);
        FeatureHelper.Set(feature, "ZJZL", "1", false, false);//[1]身份证
    }

    @Override
    public void listAdapterConvert(BaseAdapterHelper helper, final Feature item, final int deep) {
        super.listAdapterConvert(helper, item, deep);
        final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
        helper.setImageResource(com.ovit.R.id.v_icon, com.ovit.app.map.bdc.ggqj.R.mipmap.app_map_layer_qlrxx);
        helper.setText(R.id.tv_groupname, fv.getLayerName() + getOrid_Path());
        helper.setText(R.id.tv_desc, FeatureHelper.Get(item, FeatureHelper.TABLE_ATTR_BDCDYH, ""));
        helper.setText(R.id.tv_name, FeatureHelper.Get(item, "XM", "无") + "[持证人]");
        helper.getView(R.id.ll_head).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                if (!flag) {
                    final List<Feature> fs = new ArrayList<>();
                    queryChildFeature(FeatureConstants.GYRXX_TABLE_NAME, item, fs, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            com.ovit.app.map.view.FeatureView fv_gyr = mapInstance.newFeatureView(FeatureConstants.GYRXX_TABLE_NAME);
                            fv_gyr.fs_ref.add(item);
                            fv_gyr.buildListView(ll_list_item, fs, deep + 1);
                            return null;
                        }
                    });
                }
                ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
            }
        });
    }
    ///endregion

    //region 公有函数
    //通过不动产集合生成不动产View （单级不支持展开） 20180814
    public static View buildBdcViewByList(final MapInstance mapInstance, final List<Feature> features_bdc, final boolean setOnLongClick, final int deep) {
        try {
            final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
            final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
            final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
            if (ll_view.getChildCount() > 0) {
                ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
            }

            QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, features_bdc) {
                @Override
                protected void convert(final BaseAdapterHelper helper, final Feature item) {
                    helper.getView().findViewById(com.ovit.R.id.cb_select).setVisibility(View.GONE);

                    final String name = mapInstance.getName(item);
                    final String desc = mapInstance.getDesc(item);

                    helper.setText(com.ovit.app.map.R.id.tv_groupname, mapInstance.getLayerName(item));
                    helper.setText(com.ovit.R.id.tv_name, name);
                    helper.setText(com.ovit.R.id.tv_desc, desc);
                    helper.setVisible(com.ovit.R.id.tv_desc, StringUtil.IsNotEmpty(desc));

                    int s = (int) (deep * mapInstance.activity.getResources().getDimension(com.ovit.R.dimen.app_size_smaller));
                    helper.getView(com.ovit.R.id.v_split).getLayoutParams().width = s;

                    com.ovit.app.map.view.FeatureView fv = mapInstance.newFeatureView(item.getFeatureTable());
                    fv.setIcon(fv.fs_ref, item, helper.getView(com.ovit.R.id.v_icon));

                    // 定位
                    helper.setVisible(com.ovit.R.id.iv_position, item.getGeometry() != null);
                    helper.getView(com.ovit.R.id.iv_position).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MapHelper.selectAddCenterFeature(mapInstance.map, item);
                        }
                    });

                    // 详情
                    helper.setVisible(com.ovit.R.id.iv_detial, item.getGeometry() != null);
                    helper.getView(com.ovit.R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mapInstance.viewFeature(item);
                        }
                    });

                    if (setOnLongClick) {
                        //长按解除绑定
                        helper.getView().setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                AiDialog dialog = AiDialog.get(mapInstance.activity);
                                dialog.setHeaderView("确认解除此不动产与权利人的绑定吗?");
                                dialog.setContentView("此操作不可逆转，请谨慎执行！");
                                dialog.setFooterView(AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {
                                        FeatureEditQLR.unassociateQlrAndBdc(mapInstance, item, true, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                ToastMessage.Send("解除绑定成功!");
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
                                return false;
                            }
                        });
                    }

                }
            };

            ll_list_item.setTag(adapter);
            adapter.adpter(ll_list_item);
            adapter.notifyDataSetChanged();
            return ll_view;
        } catch (Exception es) {
            Log.e(TAG, "加载权利人名下不动产失败！" + es);
            return null;
        }

    }

    //非递归获得从宗地开始的所有符合条件的可选择不动产列表(当前展开至户 where为条件) 20180730
    public static View getAllSelectBDC(final MapInstance mapInstance, String where, final List<Feature> selected_feature_list) {
        where = StringUtil.IsEmpty(where) ? "1=1" : where;

        final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
        final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
        final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
        if (ll_view.getChildCount() > 0) {
            ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
        }

        FeatureTable table = mapInstance.getTable(FeatureHelper.TABLE_NAME_ZD);
        final List<Feature> fs = new ArrayList<Feature>();

        final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, fs) {
            @Override
            protected void convert(BaseAdapterHelper helper, final Feature item) {
                final com.ovit.app.map.view.FeatureView fv_zd = mapInstance.newFeatureView(item.getFeatureTable());
                initSelectList(mapInstance, helper, item, 0, selected_feature_list);

                //展开自然幢
                final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                ll_list_item.setVisibility(View.GONE);
                helper.getView(com.ovit.app.map.R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                        if (!flag) {
                            final List<Feature> fs = new ArrayList<>();
                            fv_zd.queryChildFeature(FeatureHelper.TABLE_NAME_ZRZ, item, fs, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    final com.ovit.app.map.view.FeatureView fv_zrz = mapInstance.newFeatureView(FeatureHelper.TABLE_NAME_ZRZ);

                                    QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, fs) {
                                        @Override
                                        protected void convert(BaseAdapterHelper helper, final Feature item) {
                                            initSelectList(mapInstance, helper, item, 1, selected_feature_list);

                                            //展开逻辑幢
                                            final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                                            ll_list_item.setVisibility(View.GONE);
                                            helper.getView(com.ovit.app.map.R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                                                    if (!flag) {
                                                        final List<Feature> fs = new ArrayList<>();
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
                            });
                        }
                        ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
                    }
                });

            }
        };

        ll_list_item.setTag(adapter);
        adapter.adpter(ll_list_item);

        LayerConfig config = LayerConfig.get(table);
        MapHelper.Query(table, where, config.getCol_orderby(), config.col_sort, 0, true, fs, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                adapter.notifyDataSetChanged();
                return null;
            }
        });

        return ll_view;
    }

    //生成不动产绑定面板顶部搜索工具栏的视图 20180731
    public static View getSerachTool(final MapInstance mapInstance, final AiDialog dialog, String left_tag, String hint_search, String default_where, final List<Feature> selected_feature_list) {
        final String where = StringUtil.IsEmpty(default_where) ? "1=1" : default_where;

        LinearLayout search_tool = new LinearLayout(mapInstance.activity);
        search_tool.setOrientation(LinearLayout.HORIZONTAL); //水平布局

        TextView tv_left_tag = new TextView(mapInstance.activity);
        tv_left_tag.setGravity(Gravity.CENTER); //设置文字居中
        tv_left_tag.setTextColor(mapInstance.activity.getResources().getColor(R.color.app_theme_dark)); //设置文字颜色
        tv_left_tag.setText(left_tag);

        final EditText et_search = new EditText(mapInstance.activity);
        et_search.setBackgroundResource(R.drawable.app_search_bg_groy);
        et_search.setHint(hint_search);
        et_search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        et_search.setTextColor(mapInstance.activity.getResources().getColor(R.color.app_theme_dark));
        et_search.setSingleLine(true);

        //搜索
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String key = et_search.getText().toString().trim();
                if (StringUtil.IsNotEmpty(key)) {
                    selected_feature_list.clear(); //若采用搜索的方式 则清空之前选择的内容
                    dialog.setContentView(getSearchSelectBDC(mapInstance, selected_feature_list, key, FeatureEditQLR.getAllSearchTable(mapInstance), ""));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (StringUtil.IsEmpty(et_search.getText().toString().trim())) {
                    selected_feature_list.clear(); //再次加载默认列表前清空之前选择的内容
                    dialog.setContentView(getAllSelectBDC(mapInstance, where, selected_feature_list));
                }
            }
        });

        search_tool.addView(tv_left_tag, 0);
        search_tool.addView(et_search, 1);

        //设置各子控件权重和布局
        LinearLayout.LayoutParams tv_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        tv_left_tag.setLayoutParams(tv_params);
        LinearLayout.LayoutParams et_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.75f);
        et_search.setLayoutParams(et_params);

        return search_tool;
    }

    //获得绑定不动产面板的视图 20180801
    public static AiDialog getBindBDC_View(final MapInstance mapInstance, String where, List<Feature> selected_feature_list) {
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

            dialog.setContentView(getAllSelectBDC(mapInstance, where, selected_feature_list));
            dialog.setHeaderView(getSerachTool(mapInstance, dialog, "请选择不动产", "搜索", where, selected_feature_list));

            return dialog;
        } catch (Exception es) {
            Log.e(TAG, "获得不动产列表失败！" + es);
            dialog.dismiss();
            return null;
        }
    }

    public static String GetBdcdyhFromFeature(List<Feature> fs, String zddm) {
        if (fs == null || fs.size() == 0) {
            return zddm + FeatureHelper.FEATURE_W00000000;
        }
        if (fs.size() > 1) {
            return zddm + FeatureHelper.FEATURE_F99990001;
        } else {
            return FeatureHelper.Get(fs.get(0), "ZRZH", "") + FeatureHelper.FEATURE_0001;
        }
    }

    public  void createNewQlrByBdc(final MapInstance mapInstance, final Feature feature_bdc, final AiRunnable callback) {
        try {
            final Feature feature_new_qlr = mapInstance.getTable("GYRXX").createFeature();
            feature_new_qlr.getAttributes().put("YHZGX", "户主");
            feature_new_qlr.getAttributes().put("XM", FeatureHelper.Get(feature_bdc, "XM"));
            feature_new_qlr.getAttributes().put("ZJH", FeatureHelper.Get(feature_bdc, "ZJH"));
            feature_new_qlr.getAttributes().put("ZJZL", FeatureHelper.Get(feature_bdc, "ZJZL"));
            feature_new_qlr.getAttributes().put("CSRQ", FeatureHelper.Get(feature_bdc, "CSRQ"));
            feature_new_qlr.getAttributes().put("DH", FeatureHelper.Get(feature_bdc, "DH"));
            feature_new_qlr.getAttributes().put("TDZH", FeatureHelper.Get(feature_bdc, "BDCQZH"));
            feature_new_qlr.getAttributes().put(FeatureHelper.TABLE_ATTR_ORID_PATH, FeatureHelper.Get(feature_bdc, FeatureHelper.TABLE_ATTR_ORID) + File.separator); //权利人关联不动产单元

            mapInstance.fillFeature(feature_new_qlr);
//            String f_zd_zjh_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/证件号/");
//            String f_qlr_zjh_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/证件号/");
//            FileUtils.copyFile(f_zd_zjh_path, f_qlr_zjh_path);
//
//            String f_zd_zmcl_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/土地权属来源证明材料/");
//            String f_qlr_zmcl_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/土地权属来源证明材料/");
//            FileUtils.copyFile(f_zd_zmcl_path, f_qlr_zmcl_path);
//
//            String f_zd_hkb_path = FileUtils.getAppDirAndMK(f_zd_path + "/" + "附件材料/户口簿/");
//            String f_qlr_hkb_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/户口簿/");
//
//            FileUtils.copyFile(f_zd_hkb_path, f_qlr_hkb_path);
            MapHelper.saveFeature(feature_new_qlr, callback);

        } catch (Exception es) {
            Log.e(TAG, "通过不动产单元创建新权利人失败!" + es);
            AiRunnable.Ok(callback, false, false);
        }
    }

    public void queryBdc(final AiRunnable callback) {
        String bdcdyh = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_BDCDYH, "");
        if (FeatureHelper.isBDCDYHValid(bdcdyh)) {
            if (bdcdyh.endsWith("F99990001")) {
                // 与 宗地 设定一个不动产单元 ，并且宗地内有多幢
                String zddm = StringUtil.substr(bdcdyh, 0, FeatureHelper.FEATURE_ZD_ZDDM_LENG);
                MapHelper.QueryOne(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZD, FeatureHelper.LAYER_NAME_ZD)
                        , "ZDDM='" + zddm + "'", new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (t_ != null && t_ instanceof Feature) {
                                    AiRunnable.Ok(callback, t_);
                                } else {
                                    // todo  为找到宗地，需要日志提示
                                    String tip = "不动产单元未查找到宗地："
                                            + "权利人，" + FeatureHelper.Get(feature, "XM", "")
                                            + ";不动产单元号，" + FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_BDCDYH, "");
                                    AiRunnable.Ok(callback, null, tip);
                                }
                                return null;
                            }
                        });

            } else if (bdcdyh.endsWith("0001")) {
                // 与宗地内的单幢
                String zrzh = StringUtil.substr(bdcdyh, 0, FeatureHelper.FEATURE_ZRZ_ZRZH_LENG);
                MapHelper.QueryOne(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ, FeatureHelper.LAYER_NAME_ZRZ)
                        , "ZRZH='" + zrzh + "'", new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (t_ != null && t_ instanceof Feature) {
                                    AiRunnable.Ok(callback, t_);
                                } else {
                                    // todo  未找到自然幢，需要日志提示
                                    String tip = "不动产单元未查找到自然幢："
                                            + "权利人，" + FeatureHelper.Get(feature, "XM", "")
                                            + ";不动产单元号，" + FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_BDCDYH, "");
                                    AiRunnable.Ok(callback, null, tip);
                                }
                                return null;
                            }
                        });

            } else {
                // 与宗地内的某户设置一个不动产单元
//                String ljzh=StringUtil.substr(bdcdyh,FeatureHelper.FEATURE_DJZQDM_LENG,FeatureHelper.FEATURE_ZRZ_ZRZH_LENG)
//                        +"0"+bdcdyh.charAt(FeatureHelper.FEATURE_ZRZ_ZRZH_LENG);
                String zh = StringUtil.substr(bdcdyh, FeatureHelper.FEATURE_ZD_ZDDM_LENG + 1, FeatureHelper.FEATURE_ZD_ZDDM_LENG + 1 + 4);
                String mph = Integer.parseInt(zh)
                        + "-" + bdcdyh.charAt(FeatureHelper.FEATURE_ZD_ZDDM_LENG + 1 + 4)
                        + "-" + StringUtil.substr_last(bdcdyh, 3);

                int szc = Integer.parseInt(bdcdyh.charAt(FeatureHelper.FEATURE_ZRZ_ZRZH_LENG + 1) + "");
                String hh = Integer.parseInt(StringUtil.substr_last(bdcdyh, 2)) + "";
                String where = "MPH='" + mph + "'" + " and SZC=" + szc + " and HH='" + hh + "'";
                MapHelper.QueryOne(mapInstance.getTable(FeatureHelper.TABLE_NAME_H, FeatureHelper.LAYER_NAME_H)
                        , where, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (t_ != null && t_ instanceof Feature) {
                                    AiRunnable.Ok(callback, t_);
                                } else {
                                    // todo  未找到户，需要日志提示
                                    String tip = "不动产单元未查找到户："
                                            + "权利人，" + FeatureHelper.Get(feature, "XM", "")
                                            + ";不动产单元号，" + FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_BDCDYH, "");
                                    AiRunnable.Ok(callback, null, tip);
                                }
                                return null;
                            }
                        });
            }

        } else {
            // 不动产单元无效
            AiRunnable.Ok(callback, null);
        }

    }

    // 新建权利人：
    public void update_gyrxx(final AiRunnable callback) {
        final String qlrxm = AiUtil.GetValue(feature.getAttributes().get("XM"), "");
        final String qlrzjh = AiUtil.GetValue(feature.getAttributes().get("ZJH"), "");
        final List<Feature> fs_gyrxx = new ArrayList<>();

//        // 权利人姓名，证件号未发生变化时。。。 TODO 取消了判断
//        if (old_qlrzjh.equals(qlrzjh) && old_qlrxm.equals(qlrxm)) {
//            AiRunnable.Ok(callback, null);
//            return;
//        }
        // 发生变化时
        fv.queryChildFeature(FeatureConstants.GYRXX_TABLE_NAME, feature, fs_gyrxx, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 1 不动产单元没有与权利人绑定，
                if (fs_gyrxx.size() == 0) {
                    String xm = FeatureHelper.Get(feature, "XM", "");
                    String where = "ZJH = '" + qlrzjh + "'";
                    MapHelper.QueryOne(MapHelper.getTable(map, FeatureConstants.GYRXX_TABLE_NAME), where, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            if (t_ == null) {
                                // 2 该权利人不存在时新建权利人 新建权利人
                                createNewQlrByBdc(mapInstance, feature, callback);
                            } else {
                                // 3 权利人存在
                                Feature f_gyrxx = (Feature) t_;
                                if (StringUtil.IsNotEmpty(qlrxm) && qlrxm.equals(FeatureHelper.Get(f_gyrxx, "XM", ""))) {
                                    //权利人存在 关联 宗地与权利人关联 TODO 需要解决一个用户多宗宅基地与附属宗地的冲突问题
                                    FeatureHelper.Set(f_gyrxx
                                            , FeatureHelper.TABLE_ATTR_ORID_PATH
                                            , FeatureHelper.Get(f_gyrxx, FeatureHelper.TABLE_ATTR_ORID_PATH, "")
                                                    + FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID, "") + "/");
                                    MapHelper.saveFeature(f_gyrxx, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            AiRunnable.Ok(callback, null);
                                            return null;
                                        }
                                    });

                                } else {
                                    // 不动产单元 权利人与查询到的权利人姓名不一致
                                    mapInstance.viewFeature(f_gyrxx);
                                    ToastMessage.Send("保存数据失败，权利人名字或证件号有误请检查");
                                    return null;
                                }
                            }
                            return null;
                        }
                    });

                } else {
                    // 不动产单元 有权利人时候
                    //TODO ... 姓名与证件号
                    AiRunnable.Ok(callback, null);
                }
                return null;
            }
        });
    }

    public void loadByOrid(String orid, AiRunnable callback) {
        MapHelper.QueryOne(table, StringUtil.WhereByIsEmpty(orid) + " ORID_PATH like '%" + orid + "%' ", callback);
    }

    /**
     * 新增附属宗地
     * @param callback
     */
    public void addFszd(final AiRunnable callback) {
        ToastMessage.Send(activity, "请谨慎选择附属宗地！");
        Layer layer = MapHelper.getLayer(map, FeatureHelper.TABLE_NAME_ZD);
        mapInstance.setSelectLayer(layer, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final Feature f = (Feature) t_;
                if (f != null && FeatureHelper.LAYER_NAME_ZD.equals(mapInstance.getLayerName(f))) {
                    // 不动产单元 与 宗地 关联
                    DialogBuilder.confirm(activity, "添加附属宗地", "宗地是否关联该不动产单元？", null, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            String orid_path = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                            orid_path += FeatureHelper.SEPARATORS_BDC + mapInstance.getOrid(f);
                            FeatureHelper.Set(feature, FeatureHelper.TABLE_ATTR_ORID_PATH, orid_path);// 与不动产单元与宗地关联
                            MapHelper.saveFeature(feature, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    dialog.dismiss();
                                    ToastMessage.Send("新增附属宗地成功。");
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

    /**
     * 清除附属宗地
     * @param callback
     */
    public void clearFszd(final AiRunnable callback) {

        DialogBuilder.confirm(activity, "清除附属宗地", "确定清除该宗地的附属宗地？", null, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                String orid_path = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID_PATH, "");
                String updateOridPath = "";
                if (StringUtil.IsNotEmpty(orid_path) && orid_path.contains(FeatureHelper.SEPARATORS_BDC)) {
                    updateOridPath = StringUtil.substr(orid_path, 0, FeatureHelper.SEPARATORS_BDC);
                }
                AiRunnable.Ok(callback, updateOridPath);
            }
        }, "放弃", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();


    }
    ///endregion

    //region 私有函数
    ///endregion

    //region 面积计算
    ///endregion

    //region 内部类或接口
    ///endregion

}
