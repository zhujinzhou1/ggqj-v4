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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.LayerConfig;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.model.FeatureZD;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.StringUtil;

import org.w3c.dom.Text;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewQLR extends FeatureView {

    public void fillFeature(Feature feature){
        super.fillFeature(feature);
        String id = FeatureHelper.Get(feature, "QLRBM", "");
        String xm = FeatureHelper.Get(feature, "XM", "");
        FeatureHelper.Set(feature, "QLRMC", xm, false, true);
        FeatureHelper.Set(feature, "YHZGX", "户主", false, false);
        FeatureHelper.Set(feature, "ZJZL", "1", false, false);//[1]身份证
    }

    @Override
    public void listAdapterConvert(BaseAdapterHelper helper, final Feature item, final int deep) {

        final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
        helper.setImageResource(com.ovit.R.id.v_icon, com.ovit.app.map.bdc.ggqj.R.mipmap.app_map_layer_qlrxx);
        helper.setText(com.ovit.R.id.tv_groupname,fv.getLayerName()+getOrid_Path());
        helper.setText(com.ovit.R.id.tv_desc,FeatureHelper.Get(item,"BDCDYH",""));
        helper.setText(com.ovit.R.id.tv_name,FeatureHelper.Get(item,"XM","无")+"[持证人]");
        super.listAdapterConvert(helper, item, deep);
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

    //通过不动产集合生成不动产View （单级不支持展开） 20180814
    public static View buildBdcViewByList(final MapInstance mapInstance,final List<Feature> features_bdc,final boolean setOnLongClick,final int deep)
    {
        try{
            final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
            final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
            final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
            if(ll_view.getChildCount()>0)
            {
                ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
            }

            QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, features_bdc)
            {
                @Override
                protected void convert(final BaseAdapterHelper helper, final Feature item)
                {
                    helper.getView().findViewById(com.ovit.R.id.cb_select).setVisibility(View.GONE);

                    final String name = mapInstance.getName(item);
                    final String desc = mapInstance.getDesc(item);

                    helper.setText(com.ovit.app.map.R.id.tv_groupname,mapInstance.getLayerName(item));
                    helper.setText(com.ovit.R.id.tv_name, name);
                    helper.setText(com.ovit.R.id.tv_desc, desc);
                    helper.setVisible(com.ovit.R.id.tv_desc, StringUtil.IsNotEmpty(desc));

                    int s = (int) (deep * mapInstance.activity.getResources().getDimension(com.ovit.R.dimen.app_size_smaller));
                    helper.getView(com.ovit.R.id.v_split).getLayoutParams().width = s;

                    com.ovit.app.map.view.FeatureView fv = mapInstance.newFeatureView(item.getFeatureTable());
                    fv.setIcon(fv.fs_ref,item, helper.getView(com.ovit.R.id.v_icon));

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

                    if(setOnLongClick)
                    {
                        //长按解除绑定
                        helper.getView().setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                AiDialog dialog = AiDialog.get(mapInstance.activity);
                                dialog.setHeaderView("确认解除此不动产与权利人的绑定吗?");
                                dialog.setContentView("此操作不可逆转，请谨慎执行！");
                                dialog.setFooterView("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {
                                        FeatureEditQLR.unassociateQlrAndBdc(mapInstance, item, true,new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                ToastMessage.Send("解除绑定成功!");
                                                dialog.dismiss();
                                                return null;
                                            }
                                        });
                                    }
                                }, null, null, "取消", new DialogInterface.OnClickListener() {
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
        }catch (Exception es){
            Log.e(TAG,"加载权利人名下不动产失败！"+es);
            return null;
        }

    }


    //不动产绑定相关

    //非递归获得从宗地开始的所有符合条件的可选择不动产列表(当前展开至户 where为条件) 20180730
    public static View getAllSelectBDC(final MapInstance mapInstance,String where,final List<Feature> selected_feature_list)
    {
        where = StringUtil.IsEmpty(where)?"1=1":where;

        final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
        final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
        final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
        if(ll_view.getChildCount()>0)
        {
            ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
        }

        FeatureTable table = mapInstance.getTable("ZD");
        final List<Feature> fs = new ArrayList<Feature>();

        final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity,listItemRes,fs) {
            @Override
            protected void convert(BaseAdapterHelper helper,final Feature item) {
               final com.ovit.app.map.view.FeatureView fv_zd = mapInstance.newFeatureView(item.getFeatureTable());
               initSelectList(mapInstance,helper,item,0,selected_feature_list);

                //展开自然幢
                final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                ll_list_item.setVisibility(View.GONE);
                helper.getView(com.ovit.app.map.R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                        if (!flag) {
                            final List<Feature> fs = new ArrayList<>();
                            fv_zd.queryChildFeature("ZRZ", item, fs, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    final com.ovit.app.map.view.FeatureView fv_zrz = mapInstance.newFeatureView("ZRZ");

                                    QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity,listItemRes,fs) {
                                        @Override
                                        protected void convert(BaseAdapterHelper helper,final Feature item) {
                                            initSelectList(mapInstance,helper,item,1,selected_feature_list);

                                            //展开逻辑幢
                                            final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                                            ll_list_item.setVisibility(View.GONE);
                                            helper.getView(com.ovit.app.map.R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                                                    if (!flag) {
                                                        final List<Feature> fs = new ArrayList<>();
                                                        fv_zrz.queryChildFeature("LJZ", item, fs, new AiRunnable() {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                final com.ovit.app.map.view.FeatureView fv_ljz = mapInstance.newFeatureView("LJZ");

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
                                                                                boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                                                                                if (!flag) {
                                                                                    final List<Feature> fs = new ArrayList<>();
                                                                                    fv_ljz.queryChildFeature("H", item, fs, new AiRunnable() {
                                                                                        @Override
                                                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                                                            final com.ovit.app.map.view.FeatureView fv_h = mapInstance.newFeatureView("H");
                                                                                            QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity,listItemRes,fs) {
                                                                                                @Override
                                                                                                protected void convert(BaseAdapterHelper helper,final Feature item) {
                                                                                                    initSelectList(mapInstance,helper,item,3,selected_feature_list);

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
                                                                return  null;
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
                                    return  null;
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
        MapHelper.Query(table,where,config.getCol_orderby(),config.col_sort,0,true,fs,new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                adapter.notifyDataSetChanged();
                return null;
            }
        });

        return ll_view;
    }

    //生成不动产绑定面板顶部搜索工具栏的视图 20180731
    public static View getSerachTool(final MapInstance mapInstance, final AiDialog dialog, String left_tag, String hint_search, String default_where, final List<Feature> selected_feature_list)
    {
        final String where = StringUtil.IsEmpty(default_where)?"1=1":default_where;

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
                if (StringUtil.IsNotEmpty(key))
                {
                    selected_feature_list.clear(); //若采用搜索的方式 则清空之前选择的内容
                    dialog.setContentView(getSearchSelectBDC(mapInstance,selected_feature_list,key,FeatureEditQLR.getAllSearchTable(mapInstance),""));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(StringUtil.IsEmpty(et_search.getText().toString().trim()))
                {
                    selected_feature_list.clear(); //再次加载默认列表前清空之前选择的内容
                    dialog.setContentView(getAllSelectBDC(mapInstance,where,selected_feature_list));
                }
            }
        });

        search_tool.addView(tv_left_tag,0);
        search_tool.addView(et_search,1);

        //设置各子控件权重和布局
        LinearLayout.LayoutParams tv_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
        tv_left_tag.setLayoutParams(tv_params);
        LinearLayout.LayoutParams et_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1.75f);
        et_search.setLayoutParams(et_params);

        return search_tool;
    }

    //获得绑定不动产面板的视图 20180801
    public static AiDialog getBindBDC_View(final MapInstance mapInstance,String where,List<Feature> selected_feature_list)
    {
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

            dialog.setContentView(getAllSelectBDC(mapInstance,where,selected_feature_list));
            dialog.setHeaderView(getSerachTool(mapInstance,dialog,"请选择不动产","搜索",where,selected_feature_list));

            return dialog;
        }catch(Exception es){
            Log.e(TAG,"获得不动产列表失败！"+es);
            dialog.dismiss();
            return null;
        }
    }

    public void queryBdc(final AiRunnable callback) {
        String bdcdyh = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_BDCDYH, "");
        if(FeatureHelper.isBDCDYHValid(bdcdyh)){
            if (bdcdyh.endsWith("F99990001")) {
                // 与 宗地 设定一个不动产单元 ，并且宗地内有多幢
                String zddm=StringUtil.substr(bdcdyh,0,FeatureHelper.FEATURE_ZD_ZDDM_LENG);
                MapHelper.QueryOne(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZD, FeatureHelper.LAYER_NAME_ZD)
                        , "ZDDM='" + zddm + "'", new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (t_!=null && t_ instanceof Feature){
                                    AiRunnable.Ok(callback,t_);
                                }else {
                                    // todo  为找到宗地，需要日志提示
                                    String tip= "不动产单元未查找到宗地："
                                            +"权利人，"+FeatureHelper.Get(feature,"XM","")
                                            +";不动产单元号，"+FeatureHelper.Get(feature,FeatureHelper.TABLE_ATTR_BDCDYH,"");
                                    AiRunnable.Ok(callback,null,tip);
                                }
                                return null;
                            }
                        });

            }else if (bdcdyh.endsWith("0001")){
                // 与宗地内的单幢
                String zrzh=StringUtil.substr(bdcdyh,0,FeatureHelper.FEATURE_ZRZ_ZRZH_LENG);
                MapHelper.QueryOne(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZRZ, FeatureHelper.LAYER_NAME_ZRZ)
                        , "ZRZH='" + zrzh + "'", new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (t_!=null && t_ instanceof Feature){
                                    AiRunnable.Ok(callback,t_);
                                }else {
                                    // todo  未找到自然幢，需要日志提示
                                    String tip= "不动产单元未查找到自然幢："
                                            +"权利人，"+FeatureHelper.Get(feature,"XM","")
                                            +";不动产单元号，"+FeatureHelper.Get(feature,FeatureHelper.TABLE_ATTR_BDCDYH,"");
                                    AiRunnable.Ok(callback,null,tip);
                                }
                                return null;
                            }
                        });

            }else {
                // 与宗地内的某户设置一个不动产单元
//                String ljzh=StringUtil.substr(bdcdyh,FeatureHelper.FEATURE_DJZQDM_LENG,FeatureHelper.FEATURE_ZRZ_ZRZH_LENG)
//                        +"0"+bdcdyh.charAt(FeatureHelper.FEATURE_ZRZ_ZRZH_LENG);
                String zh=StringUtil.substr(bdcdyh,FeatureHelper.FEATURE_ZD_ZDDM_LENG+1,FeatureHelper.FEATURE_ZD_ZDDM_LENG+1+4);
                String mph=Integer.parseInt(zh)
                        +"-"+bdcdyh.charAt(FeatureHelper.FEATURE_ZD_ZDDM_LENG+1+4)
                        +"-"+StringUtil.substr_last(bdcdyh,3);

                int szc=Integer.parseInt(bdcdyh.charAt(FeatureHelper.FEATURE_ZRZ_ZRZH_LENG+1)+"");
                String hh=Integer.parseInt(StringUtil.substr_last(bdcdyh,2))+"";
                String where="MPH='"+mph+"'" +" and SZC="+szc +" and HH='"+hh+"'";
                MapHelper.QueryOne(mapInstance.getTable(FeatureHelper.TABLE_NAME_H, FeatureHelper.LAYER_NAME_H)
                        , where, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (t_!=null && t_ instanceof Feature){
                                    AiRunnable.Ok(callback,t_);
                                }else {
                                    // todo  未找到户，需要日志提示
                                    String tip= "不动产单元未查找到户："
                                            +"权利人，"+FeatureHelper.Get(feature,"XM","")
                                            +";不动产单元号，"+FeatureHelper.Get(feature,FeatureHelper.TABLE_ATTR_BDCDYH,"");
                                    AiRunnable.Ok(callback,null,tip);
                                }
                                return null;
                            }
                        });
            }

        }else {
            // 不动产单元无效
            AiRunnable.Ok(callback,null);
        }


    }
}
