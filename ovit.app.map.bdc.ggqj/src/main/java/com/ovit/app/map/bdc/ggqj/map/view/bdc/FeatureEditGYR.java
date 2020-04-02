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
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.view.FeatureView;
import com.ovit.app.ui.ai.component.custom.CustomImagesView;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.ui.view.CView;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 此生无分起相思 on 2018/7/18.
 */

public class FeatureEditGYR extends FeatureEdit
{
    final static String TAG = "FeatureEditGYR";
    FeatureViewGYR fv;
    @Override
    public void onCreate() {
        super.onCreate();
        if(super.fv instanceof FeatureViewZRZ) {
            this.fv = (FeatureViewGYR) super.fv;
        }
    }
    //region  重写父类方法
    // 初始化
    @Override
    public void init()
    {
        Log.i(TAG,"init featureEditGYR!");
        super.init();
        // 菜单
        menus = new int[]{R.id.ll_gyr_info,R.id.ll_hjxx};
    }

    // 显示数据
    @Override
    public void build()
    {
        Log.i(TAG, "build gyr");
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_gyr, v_content);
        try {
            if (feature != null)
            {
                mapInstance.fillFeature(feature);
                CustomImagesView civ_zjh = (CustomImagesView) v_feature.findViewById(R.id.civ_zjh);
                String filename = AiUtil.GetValue(civ_zjh.getContentDescription(), "材料");
                civ_zjh.setName(filename + "(正反面)").setDir(FileUtils.getAppDirAndMK(getpath_root() +"附件材料/" + filename + "/")).setOnRecognize_SFZ(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Map<String, String> datas = (Map<String, String>) t_;
                        if (datas.size()==0){
                            // 身份证反面未识别到
                            return null;
                        }
                        String xm = datas.get("xm");
                        String sfzh = datas.get("sfzh");
                        String zz = datas.get("zz");
                        if (StringUtil.IsNotEmpty(xm)) {
                            ((EditText) v_feature.findViewById(com.ovit.app.map.bdc.ggqj.R.id.et_xm)).setText(xm);
                        }
                        if (StringUtil.IsNotEmpty(sfzh)) {
                            ((EditText) v_feature.findViewById(com.ovit.app.map.bdc.ggqj.R.id.et_zjh)).setText(sfzh);
                        }
                        if (StringUtil.IsNotEmpty(zz)) {
                            ((EditText) v_feature.findViewById(com.ovit.app.map.bdc.ggqj.R.id.et_txdz)).setText(zz);
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

        addAction("户籍",R.mipmap.app_map_layer_add_hjxx, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeatureEditHJXX.createNewHJXX(mapInstance,feature); //新建户籍关系 20180724
            }
        });
        addMenu("基本信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_gyr_info);
            }
        });
        addMenu("家庭成员", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_hjxx);
                View hjxx_view = view.findViewById(R.id.ll_hjxx_content);
                FeatureEditHJXX.loadHJXX(mapInstance,feature,hjxx_view);
            }
        });
        Log.i(TAG,"build gyr opt");
        super.build_opt();
    }

    //在初始化共有人编辑面板时集中填充GYR feature 并根据主键进行绑定
    private static void initFeatureGYR(Feature feature,Feature qlr_feature)
    {
        if(feature != null && qlr_feature!=null)
        {
            try{
                FeatureHelper.Set(feature,"YHZGX","其他");
                FeatureHelper.Set(feature,"HZZJH",FeatureHelper.Get(qlr_feature,"ZJH"));
                FeatureHelper.Set(feature,FeatureHelper.TABLE_ATTR_ORID_PATH,FeatureHelper.Get(qlr_feature,FeatureHelper.TABLE_ATTR_ORID,"")+"/");
            }catch (Exception es){
                Log.e(TAG,"填充共有人 feature 失败！"+es);
            }
        }

    }

    //更新共有人信息 20180810
    private static void updateFeatureGYR(Feature feature, Feature qlr_feature, final AiRunnable callback)
    {
        if(feature != null && qlr_feature!=null)
        {
            try{
                FeatureHelper.Set(feature,"HZZJH",FeatureHelper.Get(qlr_feature,"ZJH"));
                MapHelper.updateFeature(feature, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        AiRunnable.Ok(callback,true,true);
                        return null;
                    }
                });
            }catch (Exception es){
                AiRunnable.Ok(callback,false,false);
                Log.e(TAG,"更新共有人 feature 失败！"+es);
            }
        }

    }

    //外部接口 提供对共有人的创建 全屏(！！！期待滑动控制改进 面板未全部关闭前再次进入会变成全屏滑动)
    public static void createNewGYR(final MapInstance mapInstance, final Feature qlr_feature)
    {
        try {
            final Feature feature = GetTable(mapInstance, "GYRXX", "共有人信息").createFeature();
            initFeatureGYR(feature,qlr_feature);
            mapInstance.fillFeature(feature);
            mapInstance.newFeatureView(feature).fillFeatureAddSave(feature, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    mapInstance.viewFeature(feature);
                    return null;
                }
            });

        } catch (Exception es) {
            Log.e(TAG,"初始化添加共有人失败!"+ es);
            ToastMessage.Send("初始化添加共有人失败!", es);
        }
    }


    //外部接口 根据给定的共有人信息集合创建对应的列表视图 20180719
    public static View buildGRY(final MapInstance mapInstance, final List<Feature> features_gyr,final int deep,final AiRunnable callback)
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

            QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, listItemRes, features_gyr)
            {
                @Override
                protected void convert(final BaseAdapterHelper helper, final Feature item)
                {
                    final String name = item.getAttributes().get("XM") + "";  //获得共有人姓名
                    helper.setText(com.ovit.R.id.tv_name, name);
                    helper.getView(com.ovit.R.id.iv_add).setVisibility(View.GONE); //隐藏添加按钮

                    int s = (int) (deep * mapInstance.activity.getResources().getDimension(com.ovit.R.dimen.app_size_smaller));
                    helper.getView(com.ovit.R.id.v_split).getLayoutParams().width = s;

                    //详情
                    helper.getView(com.ovit.R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            Log.i(TAG,"iv_detail clicked! show gyr detail"); //
                            mapInstance.viewFeature(item); //展示共有人详细信息 全屏(期待滑动控制改进 面板未全部关闭前再次进入会变成全屏滑动)
                        }
                    });

                    //长按删除共有人
                    helper.getView().setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            AiDialog dialog = AiDialog.get(mapInstance.activity);
                            dialog.setHeaderView("确认解除此共有人吗?");
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
            Log.e(TAG,"创建共有人列表失败!"+es);
            return null;
        }

    }

    //得到一个权利人下所有的共有人信息 20180810
    public static void getAllGyrByQlr(final MapInstance mapInstance,final Feature feature_qlr,final AiRunnable callback)
    {
        try {
            final String qlr_orid = feature_qlr.getAttributes().get(FeatureHelper.TABLE_ATTR_ORID).toString(); //获得权利人ORID
            String where = "ORID_PATH like '%"+ qlr_orid +"%'";  //查询条件
            final FeatureTable table_gyr = mapInstance.getTable("GYRXX");
            MapHelper.Query(table_gyr, where, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    AiRunnable.Ok(callback,t_,t_);
                    return null;
                }
            });
        }catch (Exception es){
            Log.e(TAG,"获得共有人失败!"+es);
        }

    }

    /* 通用工具 根据权利人更新传入集合中的所有共有人与权利人的关联信信息
   */
    public static void updateInfoGYR(final MapInstance mapInstance,final List<Feature> features_gyr,final AiRunnable callback)
    {
        try{
            if(features_gyr.size()>0)
            {
                final FeatureTable table_qlr = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX);
                new AiForEach<Feature>(features_gyr,callback)
                {
                    public void exec()
                    {
                        String where_qlr = "ORID ='"+FeatureHelper.Get(features_gyr.get(postion),FeatureHelper.TABLE_ATTR_ORID_PATH)+"'";
                        MapHelper.QueryOne(table_qlr, where_qlr, new AiRunnable(getNext()) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if(t_!=null)
                                {
                                    updateFeatureGYR(features_gyr.get(postion), (Feature) t_, new AiRunnable(getNext()) {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            AiRunnable.Ok(getNext(),true,true);
                                            return null;
                                        }
                                    });
                                }else{
                                    AiRunnable.Ok(getNext(),true,true);
                                }
                                return null;
                            }
                        });
                    }
                }.start();

            }else{
                AiRunnable.Ok(callback,true,true);
            }
        }catch (Exception es){
            Log.e(TAG,"更新共有人信息失败！"+es);
            AiRunnable.Ok(callback,false,false);
        }

    }

    /* 通用工具 删除传入的共有人集合中所有不合法的共有人 并可选是否级联更新合法共有人中与权利人相关联的其他信息 20180810
       删除依据为：绑定的权利人已不存在 或共有人姓名为空
     */
    public static void delInvalidGYR(final MapInstance mapInstance, final List<Feature> features_gyr,final boolean update_gyr_info, final AiRunnable callback)
    {
        try{
            if(features_gyr.size()>0)
            {
                final List<Feature> delete_list = new ArrayList<>();
                for(Feature feature:features_gyr)
                {
                    if(StringUtil.IsEmpty(FeatureHelper.Get(feature,"XM")))
                    {
                        delete_list.add(feature);
                    }
                }
                features_gyr.removeAll(delete_list);

                final FeatureTable table_qlr = mapInstance.getTable(FeatureHelper.TABLE_NAME_QLRXX);
                new AiForEach<Feature>(features_gyr, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        MapHelper.deleteFeature(delete_list, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if(update_gyr_info) //级联更新
                                {
                                    updateInfoGYR(mapInstance, features_gyr, new AiRunnable() {
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
                        return null;
                    }
                })
                {
                    public void exec()
                    {
                        final Feature feature_gyr = features_gyr.get(postion);
                        String where_qlr = "ORID ='"+FeatureHelper.Get(feature_gyr,FeatureHelper.TABLE_ATTR_ORID_PATH)+"'";
                        MapHelper.QueryOne(table_qlr, where_qlr, new AiRunnable(getNext()) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if(t_==null)
                                {
                                    delete_list.add(feature_gyr);
                                }
                                AiRunnable.Ok(getNext(),true,true);
                                return null;
                            }
                        });
                    }
                }.start();

            }else{
                AiRunnable.Ok(callback,true,true);
            }
        }catch(Exception es){
            Log.e(TAG,"删除不合法共有人失败！"+es);
            AiRunnable.Ok(callback,false,false);
        }

    }

    /* 通用工具 删除所有不合法的共有人 并可选是否级联更新合法共有人中与权利人相关联的其他信息
       删除依据为：绑定的权利人已不存在 或共有人姓名为空 (耗时操作) 20180810
     */
    public static void delInvalidGYR(final MapInstance mapInstance, final boolean update_gyr_info, final AiRunnable callback)
    {
        try{
            final FeatureTable table_gyr = mapInstance.getTable("GYRXX");
            MapHelper.Query(table_gyr, "1=1", new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if(t_!=null)
                    {
                        delInvalidGYR(mapInstance, (List<Feature>) t_, update_gyr_info, new AiRunnable(callback) {
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
            Log.e(TAG,"删除不合法共有人失败！"+es);
            AiRunnable.Ok(callback,false,false);
        }

    }

    /* 通用工具 删除一个权利人下所有不合法的共有人 并可选是否级联更新合法共有人中与权利人相关联的其他信息
      删除依据为：共有人姓名为空 20180810
    */
    public static void delInvalidGYR(final MapInstance mapInstance,final Feature feature_qlr, final boolean update_gyr_info, final AiRunnable callback)
    {
        try{
            final FeatureTable table_gyr = mapInstance.getTable("GYRXX");
            String where_gyr = "ORID_PATH ='"+FeatureHelper.Get(feature_qlr,FeatureHelper.TABLE_ATTR_ORID)+"'";
            MapHelper.Query(table_gyr, where_gyr, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if(t_!=null)
                    {
                        delInvalidGYR(mapInstance, (List<Feature>) t_, update_gyr_info, new AiRunnable() {
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
            Log.e(TAG,"删除不合法共有人失败！"+es);
            AiRunnable.Ok(callback,false,false);
        }

    }

    //加载所有共有人 20180719
    public static void loadAllGYR(final MapInstance mapInstance,final Feature feature_qlr,final View gyr_view)
    {
        try{
            FeatureEditGYR.delInvalidGYR(mapInstance, feature_qlr, true, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    FeatureEditGYR.getAllGyrByQlr(mapInstance, feature_qlr, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            ((ViewGroup)gyr_view).removeAllViews();
                            ((ViewGroup)gyr_view).addView(FeatureEditGYR.buildGRY(mapInstance, (List<Feature>) t_, 0, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    loadAllGYR(mapInstance,feature_qlr,gyr_view);
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
            Log.e(TAG,"加载所有共有人失败!"+es);
        }

    }



}
