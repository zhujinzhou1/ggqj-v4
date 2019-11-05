package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Geometry;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewFTQK extends FeatureView {
    private static String TAG = "FeatureViewFTQK";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void loadByRef(String reforid,String reftype,AiRunnable callback){
        MapHelper.QueryOne(table,StringUtil.WhereByIsEmpty(reforid)+" REFORID = '" + reforid + "'  REFTYPE = '" + reftype + "' ",callback);
    }

    public void ft(AiRunnable callback){

    }

    public void update_Area(Feature feature, List<Feature> f_hs, List<Feature> f_z_fsjgs) {
        String id = FeatureHelper.Get(feature,"ZRZH","");
        int zcs =  FeatureHelper.Get(feature,"ZCS",1);
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        if (g != null) {
            area = MapHelper.getArea(mapInstance, g);

            for (Feature f : f_hs) {
                String zrzh = FeatureHelper.Get(f,"ZRZH","");
                if (id.equals(zrzh)) {
                    double f_hsmj = FeatureHelper.Get(f,"SCJZMJ", 0d);
                    hsmj += f_hsmj;
                }
            }
            for (Feature f : f_z_fsjgs) {
                String zid = FeatureHelper.Get(f,"ZID", "");
                if (id.equals(zid)) {
                    double f_hsmj = FeatureHelper.Get(f,"HSMJ",0d);
                    hsmj += f_hsmj;
                }
            }
            if (hsmj <= 0) {
                hsmj = area * zcs;
            }
            FeatureHelper.Set(feature,"ZZDMJ", AiUtil.Scale(area, 2));
            FeatureHelper.Set(feature,"SCJZMJ", AiUtil.Scale(hsmj, 2));
        }
    }


    public static FeatureViewFTQK From(MapInstance mapInstance, Feature f){
        FeatureViewFTQK fv =From(mapInstance);
        fv.set(f);
        return  fv;
    }
    public static FeatureViewFTQK From(MapInstance mapInstance){
        FeatureViewFTQK fv = new FeatureViewFTQK();
        fv.set(mapInstance).set(mapInstance.getTable("GNQ"));
        return  fv;
    }

    @Override
    public void listAdapterConvert(BaseAdapterHelper helper, Feature item, int deep) {
        super.listAdapterConvert(helper, item, deep);
        helper.setImageResource(com.ovit.R.id.v_icon, com.ovit.app.map.bdc.ggqj.R.mipmap.app_map_layer_ftqk);



    }
    //------------------------------------------20180802 新内容----------------------------------------------------------

    //获得限定范围内的可选择分摊去向视图 （选择范围为对应的逻辑幢下） 20180802
    public static View getSelectFtView(final MapInstance mapInstance,final Feature feature_z_fsjg,final List<Feature> selected_feature_list)
    {
        try{
            final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
            final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
            final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
            if(ll_view.getChildCount()>0)
            {
                ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
            }

            final List<Feature> features_bdc = new ArrayList<>();

            final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity,listItemRes,features_bdc)
            {
                @Override
                protected void convert(BaseAdapterHelper helper, final Feature item) {
                    // 已经分摊 不能选择
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
                                com.ovit.app.map.view.FeatureView fv_ljz=mapInstance.newFeatureView(item);
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
            FeatureViewZ_FSJG fv = (FeatureViewZ_FSJG) mapInstance.newFeatureView(feature_z_fsjg);
            fv.queryFatherFeature(mapInstance,"LJZ","ZRZ",feature_z_fsjg,features_bdc,new AiRunnable(){
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    adapter.notifyDataSetChanged();
                    return null;
                }
            });

//            String z_fsjg_orid_path = FeatureHelper.Get(feature_z_fsjg,"ORID_PATH")+"";
//            String paths[] = z_fsjg_orid_path.split("/");
////            String ljz_orid = paths.length>1?paths[paths.length-1]:"";
//            String zd_orid = paths.length>1?paths[1]:"";
//            String where_zd = "ORID_PATH like '%"+zd_orid+"%'";
//            FeatureTable table_bdc = mapInstance.getTable("QLRXX");
//            MapHelper.Query(table_bdc, where_zd,-1,features_bdc, new AiRunnable() {
//                @Override
//                public <T_> T_ ok(T_ t_, Object... objects) {
//                    adapter.notifyDataSetChanged();
////                    if(t_!=null)
////                    {
////                        final com.ovit.app.map.view.FeatureView fv_ljz = mapInstance.newFeatureView(((Feature)t_).getFeatureTable().getTableName());
////                        fv_ljz.queryChildFeature("H", (Feature) t_,features_bdc, new AiRunnable() {
////                            @Override
////                            public <T_> T_ ok(T_ t_, Object... objects) {
////                                adapter.notifyDataSetChanged();
////                                return null;
////                            }
////                        });
////                    }
//                    return null;
//                }
//            });

            return ll_view;
        }catch (Exception es){
            Log.e(TAG,"生成分摊去向选择列表失败!"+es);
            return null;
        }

    }

    //生成选择分摊去向面板顶部搜索工具栏的视图 20180801
//    public static View getSerachTool(final MapInstance mapInstance, final AiDialog dialog,final Feature feature_bdc,String left_tag, String hint_search,final List<Feature> selected_feature_list)
//    {
//        LinearLayout search_tool = new LinearLayout(mapInstance.activity);
//        search_tool.setOrientation(LinearLayout.HORIZONTAL); //水平布局
//
//        TextView tv_left_tag = new TextView(mapInstance.activity);
//        tv_left_tag.setGravity(Gravity.CENTER); //设置文字居中
//        tv_left_tag.setTextColor(mapInstance.activity.getResources().getColor(R.color.app_theme_dark)); //设置文字颜色
//        tv_left_tag.setText(left_tag);
//
//        final EditText et_search = new EditText(mapInstance.activity);
//        et_search.setBackgroundResource(R.drawable.app_search_bg_groy);
//        et_search.setHint(hint_search);
//        et_search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
//        et_search.setTextColor(mapInstance.activity.getResources().getColor(R.color.app_theme_dark));
//        et_search.setSingleLine(true);
//
//        //搜索
//        et_search.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String key = et_search.getText().toString().trim();
//                if (StringUtil.IsNotEmpty(key))
//                {
//                    selected_feature_list.clear(); //若采用搜索的方式 则清空之前选择的内容
//                    dialog.setContentView(getSearchSelectBDC(mapInstance,selected_feature_list,key,FeatureEditFTQK.getSearchTableList(mapInstance),""));
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if(StringUtil.IsEmpty(et_search.getText().toString().trim()))
//                {
//                    selected_feature_list.clear(); //再次加载默认列表前清空之前选择的内容
//                    dialog.setContentView(getSelectFtView(mapInstance,feature_bdc,selected_feature_list));
//                }
//            }
//        });
//
//        search_tool.addView(tv_left_tag,0);
//        search_tool.addView(et_search,1);
//
//        //设置各子控件权重和布局
//        LinearLayout.LayoutParams tv_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
//        tv_left_tag.setLayoutParams(tv_params);
//        LinearLayout.LayoutParams et_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1.75f);
//        et_search.setLayoutParams(et_params);
//
//        return search_tool;
//    }

    //获得添加分摊去向面板的视图 20180802
    public static AiDialog getSelectFTQX_View(final MapInstance mapInstance,final Feature feature_bdc,final List<Feature> selected_feature_list)
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

            dialog.setContentView(getSelectFtView(mapInstance,feature_bdc,selected_feature_list));
            dialog.setHeaderView("请选择分摊去向");

            return dialog;
        }catch(Exception es){
            Log.e(TAG,"获得不动产列表失败！"+es);
            dialog.dismiss();
            return null;
        }
    }

    //获得添加分摊列表的视图 幢附属结构 20191104
    public static AiDialog getSelectFTQX_FSJG_View(final MapInstance mapInstance,final Feature feature_bdc,final List<Feature> selected_feature_list)
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

            dialog.setContentView(getSelectFtViewToFsjg(mapInstance,feature_bdc,selected_feature_list));
            dialog.setHeaderView("请选择分摊去向");

            return dialog;
        }catch(Exception es){
            Log.e(TAG,"获得不动产列表失败！"+es);
            dialog.dismiss();
            return null;
        }
    }
    //获得限定范围内的可选择分摊去向视图 （选择范围为对应的逻辑幢下） 20180802
    public static View getSelectFtViewToFsjg(final MapInstance mapInstance, final Feature feature_zd, final List<Feature> selected_feature_list)
    {
        try{
            final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
            final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
            final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
            if(ll_view.getChildCount()>0)
            {
                ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
            }

            final List<Feature> features_bdc = new ArrayList<>();

            final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity,listItemRes,features_bdc)
            {
                @Override
                protected void convert(BaseAdapterHelper helper, final Feature item) {
                    // 已经分摊 不能选择
                    initSelectList(mapInstance, helper, item, 1, selected_feature_list);
                    //展开户
                    final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
                    ll_list_item.setVisibility(View.GONE);
                }
            };

            ll_list_item.setTag(adapter);
            adapter.adpter(ll_list_item);
            FeatureViewZD fv = (FeatureViewZD) mapInstance.newFeatureView(feature_zd);
            fv.queryChildFeature("Z_FSJG", fv.getOrid(), "", "LC", "asc", features_bdc, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    adapter.notifyDataSetChanged();
                    return null;
                }
            });

            return ll_view;
        }catch (Exception es){
            Log.e(TAG,"生成分摊去向选择列表失败!"+es);
            return null;
        }

    }

}
