package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.ai.component.custom.CustomImagesView;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 此生无分起相思 on 2018/7/24.
 */

public class FeatureEditHJXX extends FeatureEdit
{
    final static String TAG = "FeatureEditHJXX";

    public FeatureEditHJXX(){ super();}
    public FeatureEditHJXX(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }

    //region  重写父类方法
    // 初始化
    @Override
    public void init()
    {
        Log.i(TAG,"init featureEditHJXX!");
        super.init();
        // 菜单
        //menus = new int[]{com.ovit.R.id.ll_info, com.ovit.R.id.ll_item};
    }

    // 显示数据
    @Override
    public void build()
    {
        Log.i(TAG, "build hjxx");
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_hjxx, v_content);
        try {
            if (feature != null)
            {
                mapInstance.fillFeature(feature);

                CustomImagesView civ_zjh = (CustomImagesView) v_feature.findViewById(com.ovit.app.map.bdc.ggqj.R.id.civ_zjh);
                String filename = AiUtil.GetValue(civ_zjh.getContentDescription(), "材料");
                civ_zjh.setName(filename + "(正反面)").setDir(FileUtils.getAppDirAndMK(getpath_root() +"附件材料/" + filename + "/")).setOnRecognize_SFZ(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Map<String, String> datas = (Map<String, String>) t_;
                        if (datas.size()==0){
                            return null;
                        }
                        String xm = datas.get("xm");
                        String sfzh = datas.get("sfzh");
                        String zz = datas.get("zz");
                        String xb = datas.get("xb");
                        String mz = datas.get("mz");
                        String cs = datas.get("cs"); // 出生
                        if (StringUtil.IsNotEmpty(xm)) {
                            ((EditText) v_feature.findViewById(com.ovit.app.map.bdc.ggqj.R.id.et_xm)).setText(xm);
                        }
                        if (StringUtil.IsNotEmpty(sfzh)) {
                            ((EditText) v_feature.findViewById(com.ovit.app.map.bdc.ggqj.R.id.et_zjh)).setText(sfzh);
                        }
                        if (StringUtil.IsNotEmpty(zz)) {
                            ((EditText) v_feature.findViewById(com.ovit.app.map.bdc.ggqj.R.id.et_txdz)).setText(zz);
                        }
                        // 民族
                        if (StringUtil.IsNotEmpty(mz)) {
                            ((EditText) v_feature.findViewById(com.ovit.app.map.bdc.ggqj.R.id.et_mz)).setText(mz);
                        }
                        // 性别
                        if (StringUtil.IsNotEmpty(xb)) {
                            ((EditText) v_feature.findViewById(com.ovit.app.map.bdc.ggqj.R.id.et_txdz)).setText(zz);
                        }
                        // 出生
                        if (StringUtil.IsNotEmpty(cs)) {
                            ((EditText) v_feature.findViewById(com.ovit.app.map.bdc.ggqj.R.id.et_mz)).setText(mz);
                        }
                        return null;
                    }
                });
            }

            fillView(v_feature);

        }catch(Exception e){
            Log.e(TAG,"构建失败 "+e);
        }

    }

    public void build_opt()
    {
        Log.i(TAG,"build hjxx opt");
        super.build_opt();
    }
    // 列表项，点击加载自然幢

    //在初始化共有人编辑面板时集中填充HJXX feature 并根据主键进行绑定
    private static void initFeatureHJXX(Feature feature,Feature qlr_feature)
    {
        if(feature != null && qlr_feature!=null)
        {
            try{
                FeatureHelper.Set(feature,"YHZGX","其他");
                FeatureHelper.Set(feature,FeatureHelper.TABLE_ATTR_ORID_PATH,FeatureHelper.Get(qlr_feature,FeatureHelper.TABLE_ATTR_ORID));
            }catch (Exception es){
                Log.e(TAG,"填充共有人 feature 失败！"+es);
            }

            Log.i(TAG,"HJXX:"+feature.getAttributes().toString()); //打印填充之后的feature属性
        }

    }

    //外部接口 提供对户籍信息的创建 全屏(期待滑动控制改进 面板未全部关闭前再次进入会变成全屏滑动)
    public static void createNewHJXX(final MapInstance mapInstance, final Feature qlr_feature)
    {
        try {
            final Feature feature = GetTable(mapInstance, "HJXX", "户籍信息").createFeature();
            initFeatureHJXX(feature,qlr_feature);
//            mapInstance.fillFeature(feature,qlr_feature);
            mapInstance.newFeatureView(feature).fillFeatureAddSave(feature, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    mapInstance.viewFeature(feature);
                    return null;
                }
            });

        } catch (Exception es) {
            ToastMessage.Send("初始化新户籍信息失败!", es);
            Log.e(TAG,"初始化新户籍信息失败!"+ es);
        }

    }

    //外部接口 根据给定的户籍信息集合创建对应的列表视图 20180724
    public static View buildHJXX(final MapInstance mapInstance, final List<Feature> features_hjxx,final int deep,final AiRunnable callback)
    {
        try{
            final int listItemRes = com.ovit.R.layout.app_ui_ai_aimap_xm_item;
            final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
            final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
            ll_list_item.setVisibility(View.VISIBLE);
            if(ll_view.getChildCount()>0)
            {
                ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
            }

            QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, features_hjxx)
            {
                @Override
                protected void convert(final BaseAdapterHelper helper, final Feature item)
                {
                    final String name = item.getAttributes().get("XM") + "";
                    helper.setText(com.ovit.R.id.tv_name, name);
                    helper.getView(com.ovit.R.id.iv_add).setVisibility(View.GONE); //隐藏添加按钮

                    int s = (int) (deep * mapInstance.activity.getResources().getDimension(com.ovit.R.dimen.app_size_smaller));
                    helper.getView(com.ovit.R.id.v_split).getLayoutParams().width = s;

                    //以下部分为ListView上组件的监听函数
                    helper.getView(com.ovit.R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            Log.i(TAG,"iv_detail clicked! show gyr detail"); //
                            mapInstance.viewFeature(item); //展示户籍详细信息 全屏(期待滑动控制改进 面板未全部关闭前再次进入会变成全屏滑动)
                        }
                    });

                    //长按删除户籍信息
                    helper.getView().setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            AiDialog dialog = AiDialog.get(mapInstance.activity);
                            dialog.setHeaderView("确认解除此户籍信息吗?");
                            dialog.setContentView("此操作不可逆转，请谨慎执行！");
                            dialog.setFooterView(AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    MapHelper.deleteFeature(item, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            ToastMessage.Send("删除成功!");
                                            AiRunnable.Ok(callback,true,true);
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
            };

            ll_list_item.setTag(adapter);
            adapter.adpter(ll_list_item);
            adapter.notifyDataSetChanged();
            return ll_view;
        }catch (Exception es){
            Log.e(TAG,"创建户籍信息列表失败!"+es);
            return null;
        }

    }

    //得到一个权利人下所有的共有户籍信息 20180810
    public static void getAllHjxxByQlr(final MapInstance mapInstance,final Feature feature_qlr,final AiRunnable callback)
    {
        try {
            final String qlr_orid = feature_qlr.getAttributes().get(FeatureHelper.TABLE_ATTR_ORID).toString(); //获得权利人ORID
            String where = "ORID_PATH like '%"+ qlr_orid +"%'";  //查询条件
            final FeatureTable table_hjxx = mapInstance.getTable("HJXX");
            MapHelper.Query(table_hjxx, where, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    AiRunnable.Ok(callback,t_,t_);
                    return null;
                }
            });
        }catch (Exception es){
            Log.e(TAG,"获得户籍信息失败!"+es);
        }

    }

    /* 通用工具 删除传入的户籍信息集合中所有不合法的户籍信息
       删除依据为：绑定的权利人已不存在(可选) 或姓名为空 20180810
     */
    public static void delInvalidHJXX(final MapInstance mapInstance, final List<Feature> features_hjxx, boolean check_qlr,final AiRunnable callback)
    {
        try{
            if(features_hjxx.size()>0) {
                final List<Feature> delete_list = new ArrayList<>();
                for (Feature feature_hjxx : features_hjxx)
                {
                    if (StringUtil.IsEmpty(FeatureHelper.Get(feature_hjxx, "XM")))
                    {
                        delete_list.add(feature_hjxx);
                    }
                }
                features_hjxx.removeAll(delete_list);

                if (features_hjxx.size() > 0 && check_qlr)
                {
                    final FeatureTable table_hjxx = mapInstance.getTable("HJXX");
                    new AiForEach<Feature>(features_hjxx, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            MapHelper.deleteFeature(delete_list, new AiRunnable(callback) {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    AiRunnable.Ok(callback,true,true);
                                    return null;
                                }
                            });
                            return null;
                        }
                    })
                    {
                        public void exec()
                        {
                            final Feature feature_hjxx = features_hjxx.get(postion);
                            String where_qlr = "ORID ='"+FeatureHelper.Get(feature_hjxx,FeatureHelper.TABLE_ATTR_ORID_PATH)+"'";
                            MapHelper.QueryOne(table_hjxx, where_qlr, new AiRunnable(getNext()) {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    if(t_==null)
                                    {
                                        delete_list.add(feature_hjxx);
                                    }
                                    AiRunnable.Ok(getNext(),true,true);
                                    return null;
                                }
                            });
                        }
                    }.start();
                }else{
                    MapHelper.deleteFeature(delete_list, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            AiRunnable.Ok(callback,true,true);
                            return null;
                        }
                    });
                }

            }else{
                AiRunnable.Ok(callback,true,true);
            }
        }catch(Exception es){
            Log.e(TAG,"清除不合法户籍信息失败！"+es);
            AiRunnable.Ok(callback,false,false);
        }

    }

    /* 通用工具 删除所有不合法的户籍信息
       删除依据为：绑定的权利人已不存在(可选) 或姓名为空  (耗时操作) 20180810
     */
    public static void delInvalidHJXX(final MapInstance mapInstance, final boolean check_qlr, final AiRunnable callback)
    {
        try{
            final FeatureTable table_hjxx = mapInstance.getTable("HJXX");
            MapHelper.Query(table_hjxx, "1=1", new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if(t_!=null)
                    {
                        delInvalidHJXX(mapInstance, (List<Feature>) t_,check_qlr, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                AiRunnable.Ok(callback,true,true);
                                return null;
                            }
                        });
                    }else{
                        AiRunnable.Ok(callback,true,true);
                    }
                    return null;
                }
            });
        }catch(Exception es){
            Log.e(TAG,"清除不合法户籍信息失败！"+es);
            AiRunnable.Ok(callback,false,false);
        }

    }

    /* 通用工具 删除某一个权利人下所有不合法的户籍信息
       删除依据为：姓名为空            20180810
     */
    public static void delInvalidHJXX(final MapInstance mapInstance, final Feature feature_qlr, final AiRunnable callback)
    {
        try{
            final FeatureTable table_hjxx = mapInstance.getTable("HJXX");
            String where_hjxx = "ORID_PATH ='"+FeatureHelper.Get(feature_qlr,FeatureHelper.TABLE_ATTR_ORID)+"'";
            MapHelper.Query(table_hjxx, where_hjxx, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if(t_!=null)
                    {
                        delInvalidHJXX(mapInstance, (List<Feature>) t_,false, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                AiRunnable.Ok(callback,true,true);
                                return null;
                            }
                        });
                    }else{
                        AiRunnable.Ok(callback,true,true);
                    }
                    return null;
                }
            });
        }catch (Exception es){
            Log.e(TAG,"清除不合法户籍信息失败！"+es);
            AiRunnable.Ok(callback,false,false);
        }

    }

    //生成户籍信息 20180719
    public static void loadHJXX(final MapInstance mapInstance,final Feature feature_qlr,final View hjxx_view)
    {
        try{
            FeatureEditHJXX.delInvalidHJXX(mapInstance, feature_qlr, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    getAllHjxxByQlr(mapInstance, feature_qlr, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            ((ViewGroup)hjxx_view).removeAllViews();
                            ((ViewGroup)hjxx_view).addView(FeatureEditHJXX.buildHJXX(mapInstance, (List<Feature>) t_, 0, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    loadHJXX(mapInstance,feature_qlr,hjxx_view);
                                    return null;
                                }
                            }));
                            return null;
                        }
                    });
                    return null;
                }
            });
        }catch (Exception es){
            Log.e(TAG,"加载户籍信息失败!"+es);
        }

    }

    //生成户籍信息 20180719
    public static void loadQlrAndHJXX(final MapInstance mapInstance,final Feature feature_zd,final View hjxx_view)
    {
        try{
            FeatureViewZD fv = (FeatureViewZD) FeatureView.From(mapInstance, feature_zd);

              fv.loadQlrByZd(new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {



                    return null;
                }
            });


            FeatureEditHJXX.delInvalidHJXX(mapInstance, feature_zd, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    getAllHjxxByQlr(mapInstance, feature_zd, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            ((ViewGroup)hjxx_view).removeAllViews();
                            ((ViewGroup)hjxx_view).addView(FeatureEditHJXX.buildHJXX(mapInstance, (List<Feature>) t_, 0, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    loadHJXX(mapInstance,feature_zd,hjxx_view);
                                    return null;
                                }
                            }));
                            return null;
                        }
                    });
                    return null;
                }
            });
        }catch (Exception es){
            Log.e(TAG,"加载户籍信息失败!"+es);
        }

    }


}
