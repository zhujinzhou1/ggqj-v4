package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.LayerConfig;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by 此生无分起相思 on 2018/8/2.
 */

public class FeatureEditFTQK extends FeatureEdit {
    //region 常量
    final static String TAG = "FeatureEditFTQK";
    ///endregion

    //region 字段
    ///endregion

    //region 构造函数
    public FeatureEditFTQK(){ super();}
    public FeatureEditFTQK(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }
    ///endregion

    //region 重写函数和回调
    // 初始化
    @Override
    public void init()
    {
        Log.i(TAG,"init featureEditFTQK!");
        super.init();
        // 菜单
        //menus = new int[]{com.ovit.R.id.ll_info, com.ovit.R.id.ll_item};
    }

    // 显示数据
    @Override
    public void build()
    {
        Log.i(TAG, "build ftqk");
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(com.ovit.app.map.bdc.ggqj.R.layout.app_ui_ai_aimap_feature_ftqk, v_content);
        try {
            if (feature != null)
            {
                mapInstance.fillFeature(feature);

                fillView(v_feature);
            }

        }catch(Exception e){
            Log.i(TAG,"构建失败 "+e);
        }

    }

    @Override
    public void build_opt()
    {
        Log.i(TAG,"build ftqk opt");
        super.build_opt();
    }
    ///endregion

    //region 公有函数
    ///endregion

    //region 私有函数
    ///endregion

    //region 内部类或接口
    ///endregion

    //region  重写父类方法


    //辅助函数 获得子层级的layer name 20180802
    private static String getChildLayerName(Feature feature)
    {
        switch(feature.getFeatureTable().getTableName())
        {
            case "ZD":return "ZRZ";
            case "ZRZ":return "LJZ";
            case "LJZ":return "Z_FSJG";
            default:return null;
        }
    }

    //辅助函数 初始化分摊情况列表中每条记录的样式和事件 20180802
    private static void initFtItem(final MapInstance mapInstance,final BaseAdapterHelper helper, final Feature item,final int deep)
    {
        try{
            helper.getView().findViewById(com.ovit.R.id.iv_position).setVisibility(View.GONE);

            final String desc = StringUtil.IsNotEmpty(FeatureHelper.Get(item,"FTJZMJ"))?FeatureHelper.Get(item,"FTJZMJ").toString():"0.0";
            final String groupname = StringUtil.IsNotEmpty(FeatureHelper.Get(item,"FTLY_NAME"))?FeatureHelper.Get(item,"FTLY_NAME").toString():"未知类型";

            helper.setText(com.ovit.app.map.R.id.tv_groupname,groupname);
            helper.setText(com.ovit.R.id.tv_desc, desc);

            int s = (int) (deep * mapInstance.activity.getResources().getDimension(com.ovit.R.dimen.app_size_smaller));
            helper.getView(com.ovit.R.id.v_split).getLayoutParams().width = s;
            //异步设置icon和name
            MapHelper.QueryOne(mapInstance.getTable("H"), "ORID ='" + FeatureHelper.Get(item,"FTQX_ID") + "'", new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if(t_!=null){
                        com.ovit.app.map.view.FeatureView fv = mapInstance.newFeatureView(((Feature)t_).getFeatureTable());
                        fv.setIcon((Feature)t_,helper.getView(com.ovit.R.id.v_icon));
                        helper.setText(com.ovit.R.id.tv_name,(StringUtil.IsNotEmpty(FeatureHelper.Get((Feature)t_,"MPH"))?FeatureHelper.Get((Feature)t_,"MPH").toString():"未知来源"));
                    }else{
                        ((ImageView)helper.getView(com.ovit.R.id.v_icon)).setImageResource(com.ovit.R.mipmap.app_icon_layer_z_fsjg);
                        helper.setText(com.ovit.R.id.tv_name,"未知来源");
                    }
                    return null;
                }
            });

            //监听函数
            helper.getView().findViewById(com.ovit.R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapInstance.viewFeature(item);
                }
            });
        }catch (Exception es){
            Log.e(TAG,"初始化分摊情况列表失败！"+es);
        }

    }

    //辅助函数 生成分摊记录适配器 20180802
    private static QuickAdapter<Feature> getFtAdapter(final MapInstance mapInstance,final int listItemRes,final List<Feature> features)
    {
        try{
            final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity,listItemRes,features) {
                @Override
                protected void convert(final BaseAdapterHelper helper, final Feature item)
                {
                    initFtItem(mapInstance,helper,item,0);
                }
            };

            return adapter;
        }catch(Exception es){
            Log.e(TAG,"生成分摊适配器失败！"+es);
            return null;
        }
    }

    //添加分摊情况时先进行初始信息绑定 20180802
    public static void initAddFt(final Feature feature_ft,final Feature feature_source)
    {
        if(feature_source!=null&&feature_ft!=null)
        {
            switch(feature_source.getFeatureTable().getTableName())
            {
                case "FTQK":{
                        FeatureHelper.Set(feature_ft,"ORID_PATH",FeatureHelper.Get(feature_source,"ORID_PATH"));
                        FeatureHelper.Set(feature_ft,"FTLY_NAME",FeatureHelper.Get(feature_source,"FTLY_NAME"));
                        FeatureHelper.Set(feature_ft,"FTLY_ID",FeatureHelper.Get(feature_source,"FTLY_ID"));
                        FeatureHelper.Set(feature_ft,"FTTDMJ",FeatureHelper.Get(feature_source,"FTTDMJ"));
                        break;
                }
                case "Z_FSJG":{
                    FeatureHelper.Set(feature_ft,"ORID_PATH",FeatureHelper.Get(feature_source,"ORID"));
                    FeatureHelper.Set(feature_ft,"FTLY_NAME",FeatureHelper.Get(feature_source,"MC"));
                    FeatureHelper.Set(feature_ft,"FTLY_ID",FeatureHelper.Get(feature_source,"ORID"));
                    FeatureHelper.Set(feature_ft,"FTTDMJ",FeatureHelper.Get(feature_source,"HSMJ"));
                    FeatureHelper.Set(feature_ft,"FTXS","0");
                    FeatureHelper.Set(feature_ft,"FTJZMJ","0");
                    break;
                }
                default:{
                    FeatureHelper.Set(feature_ft,"ORID_PATH",FeatureHelper.Get(feature_source,"ORID"));
                    break;
                }
            }
        }
    }

    //添加分摊情况后进行信息绑定 20180804
    public static void initAfterAddFt(Feature feature_ft,Feature feature_qx)
    {
        if(feature_ft!=null && feature_qx!=null)
        {
            FeatureHelper.Set(feature_ft,"FTQX_NAME",FeatureHelper.Get(feature_qx,"HH"));
            FeatureHelper.Set(feature_ft,"FTQX_ID",FeatureHelper.Get(feature_qx,"ORID"));
        }
    }

    //选择分摊去向 20180802
    private static void selectFTQX(final MapInstance mapInstance, final Feature feature_z_fsjg, final AiRunnable callback)
    {
        final List<Feature> selected_feature_list = new ArrayList<>();
        AiDialog dialog = FeatureViewFTQK.getSelectFTQX_View(mapInstance,feature_z_fsjg,selected_feature_list);
        if(dialog!=null)
        {
            dialog.setFooterView("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    completeAddFt(mapInstance, selected_feature_list, feature_z_fsjg, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            if((java.lang.Boolean) t_){
                                AiRunnable.Ok(callback,true,true);
                            }else{
                                AiRunnable.Ok(callback,false,false);
                            }
                            return null;
                        }
                    });
                    dialog.dismiss();
                }
            }, null, null, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, "用户取消选择分摊去向！");
                    dialog.dismiss();
                }
            });

        }else{
            AiRunnable.Ok(callback,false,false);
            ToastMessage.Send("获得不动产列表失败!");
        }

    }

    //选择分摊去向 20191104
    private static void selectFTQXToZD(final MapInstance mapInstance, final Feature feature_zd, final AiRunnable callback)
    {
        final List<Feature> selected_feature_list = new ArrayList<>();
        AiDialog dialog = FeatureViewFTQK.getSelectFTQX_FSJG_View(mapInstance, feature_zd,selected_feature_list);
        if(dialog!=null)
        {
            dialog.setFooterView(AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    completeAddFt2ZD(mapInstance, selected_feature_list, feature_zd, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            AiRunnable.Ok(callback, t_, objects);
                            return null;
                        }
                    });
                    dialog.dismiss();
                }
            }, null, null, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, "用户取消选择分摊去向！");
                    dialog.dismiss();
                }
            });

        }else{
            AiRunnable.Ok(callback,"","");
            ToastMessage.Send("获得分摊列表失败!");
        }

    }

    //在这里完成添加分摊去向后的后续逻辑 20180805
    private static void completeAddFt(final MapInstance mapInstance,final List<Feature> selected_feature_list, final Feature feature_z_fsjg,final AiRunnable callback)
    {
        if(selected_feature_list.size()==0)
        {
            ToastMessage.Send("未选择任何内容!");
        }

        else
        {  //若用户选择了分摊去向 则自动创建新的分摊记录
            ToastMessage.Send("将新建"+selected_feature_list.size()+"条分摊记录");
            try{
                final List<Feature> need_to_save = new ArrayList<>();
                for(Feature feature:selected_feature_list)
                {
                    Feature new_feature_ft = mapInstance.getTable("FTQK").createFeature();
                    initAddFt(new_feature_ft,feature_z_fsjg);
                    initAfterAddFt(new_feature_ft,feature);
                    mapInstance.featureView.fillFeature(new_feature_ft);
                    need_to_save.add(new_feature_ft);
                }

                MapHelper.saveFeature(need_to_save, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        // 不更新户的分摊建筑面积
                        updateFtByZ_FSJG(mapInstance, feature_z_fsjg, false,new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                AiRunnable.Ok(callback,true,true);
                                return null;
                            }
                        });
                        return null;
                    }
                });

            }catch(Exception es){
                AiRunnable.Ok(callback,false,false);
                Log.e(TAG,"处理添加分摊去向后续逻辑失败！"+es);
            }
        }

    }

    private static void completeAddFt2ZD(final MapInstance mapInstance, final List<Feature> selected_feature_list, final Feature feature_zd, final AiRunnable callback)
    {
        if(selected_feature_list.size()==0)
        {
            ToastMessage.Send("未选择任何内容!");
        } else
        {  //若用户选择了分摊去向 则自动创建新的分摊记录
            ToastMessage.Send("将新建"+selected_feature_list.size()+"条分摊记录");
            try{
                final List<Feature> need_to_save = new ArrayList<>();
                for(Feature feature:selected_feature_list)
                {
                    Feature new_feature_ft = mapInstance.getTable("FTQK").createFeature();
//                    initAddFt(new_feature_ft,feature);
                    FeatureHelper.Set(new_feature_ft,"ORID_PATH",FeatureHelper.Get(feature,"ORID"));
                    FeatureHelper.Set(new_feature_ft,"FTLY_NAME",FeatureHelper.Get(feature,"MC"));
                    FeatureHelper.Set(new_feature_ft,"FTLY_ID",FeatureHelper.Get(feature,"ORID"));
                    FeatureHelper.Set(new_feature_ft,"FTJZMJ",FeatureHelper.Get(feature,"HSMJ"));
                    FeatureHelper.Set(new_feature_ft,"FTXS","0");

                    FeatureHelper.Set(new_feature_ft,"FTQX_NAME",FeatureHelper.Get(feature_zd,"ZDDM"));
                    FeatureHelper.Set(new_feature_ft,"FTQX_ID",FeatureHelper.Get(feature_zd,"ORID"));
                    mapInstance.featureView.fillFeature(new_feature_ft);
                    need_to_save.add(new_feature_ft);
                }
                MapHelper.saveFeature(need_to_save,callback);
            }catch(Exception es){
                AiRunnable.Ok(callback,"","");
                Log.e(TAG,"处理添加分摊去向后续逻辑失败！"+es);
            }
        }

    }

    //外部接口 通过分摊记录得到对应的Z_FSJG 20180807
    public static void getZ_FSJG(final MapInstance mapInstance, final Feature feature_ft, final AiRunnable callback)
    {
        try{
            String z_fsjg_orid = FeatureHelper.Get(feature_ft,"ORID_PATH")+"";
            String where = "ORID ='"+z_fsjg_orid+"'";
            FeatureTable table_z_fsjg = mapInstance.getTable("Z_FSJG");
            MapHelper.QueryOne(table_z_fsjg, where, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    AiRunnable.Ok(callback,t_,t_);
                    return null;
                }
            });

        }catch (Exception es){
            Log.e(TAG,"获得Z_FSJG失败！"+es);
        }
    }

    //外部接口 根据给定的集合生成分摊情况视图 20180804
    public static View buildFtViewByList(final MapInstance mapInstance,List<Feature> features)
    {
        try{
            final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
            final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
            final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
            if(ll_view.getChildCount()>0)
            {
                ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
            }

            final QuickAdapter adapter = getFtAdapter(mapInstance,listItemRes,features);

            if(adapter!=null)
            {
                ll_list_item.setTag(adapter);
                adapter.adpter(ll_list_item);
                adapter.notifyDataSetChanged();
            }

            return ll_view;
        }catch(Exception es){
            Log.e(TAG,"生成分摊情况视图失败!"+es);
            ToastMessage.Send("生成分摊情况视图失败!"+es);
            return null;
        }

    }

    //外部接口 根据指定查询条件查找该条件下所有分摊情况视图 20180802
    public static View buildFtViewByWhere(final MapInstance mapInstance,String where)
    {
        try{
            final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
            final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
            final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
            if(ll_view.getChildCount()>0)
            {
                ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
            }

            final List<Feature> features = new ArrayList<>();

            final QuickAdapter adapter = getFtAdapter(mapInstance,listItemRes,features);
            ll_list_item.setTag(adapter);
            adapter.adpter(ll_list_item);

            FeatureTable table = mapInstance.getTable("FTQK");
            LayerConfig config = LayerConfig.get(table);
            MapHelper.Query(table,where,config.getCol_orderby(),config.col_sort,0,true,features,new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    adapter.notifyDataSetChanged();
                    return null;
                }
            });
            return ll_view;
        }catch (Exception es){
            Log.e(TAG,"生成分摊情况视图失败！"+es);
            ToastMessage.Send("生成分摊情况视图失败！"+es);
            return null;
        }

    }

    //外部接口 递归得到某个不动产下的所有分摊记录集合 (最终回调在AiForEach的complet方法中)20180803
    public static void getAllFtFeaturesByBDC(final MapInstance mapInstance, final Feature feature_bdc, final List<Feature> features_ft,final AiRunnable callback)
    {
        try{
            if(feature_bdc.getFeatureTable().getTableName().equals("Z_FSJG"))
            {
                String where = "ORID_PATH like'%"+FeatureHelper.Get(feature_bdc,"ORID")+"%'";
                FeatureTable table = mapInstance.getTable("FTQK");
                MapHelper.Query(table, where, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        features_ft.addAll((List<Feature>)t_);
                        AiRunnable.Ok(callback,(List<Feature>)t_,t_);
                        return null;
                    }
                });
            }

            else
            {
                final List<Feature> features_child_bdc = new ArrayList<>();
                final com.ovit.app.map.view.FeatureView fv = mapInstance.newFeatureView(feature_bdc.getFeatureTable().getTableName());
                fv.queryChildFeature(getChildLayerName(feature_bdc), feature_bdc, features_child_bdc, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        new AiForEach<Feature>(features_child_bdc,callback){
                            public void exec(){
                                if(postion == features_child_bdc.size()-1)
                                {
                                    setValues(features_ft);
                                }
                                getAllFtFeaturesByBDC(mapInstance, features_child_bdc.get(postion),features_ft,getNext());
                            }
                        }.start();
                        return null;
                    }
                });
            }
        }catch (Exception es){
            Log.e(TAG,"获得分摊记录集合失败！"+es);
        }

    }

    //外部接口 添加分摊 20180802
    public static void addFt(final MapInstance mapInstance, final Feature feature_bdc, final View ft_view)
    {
        try {
            selectFTQX(mapInstance, feature_bdc, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    ((ViewGroup)ft_view).removeAllViews();
                    load_ft(mapInstance,feature_bdc,ft_view);
                    return null;
                }
            });
        } catch (Exception es) {
            ToastMessage.Send("初始化添加分摊失败!", es);
        }
    }

    //外部接口 添加分摊 以宗地为导向
    public static void addFtToZD(final MapInstance mapInstance, final Feature feature_bdc, final View ft_view, final AiRunnable callback)
    {
        try {
            selectFTQXToZD(mapInstance, feature_bdc, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    ((ViewGroup)ft_view).removeAllViews();
                    AiRunnable.Ok(callback,t_,objects);
                    return null;
                }
            });
        } catch (Exception es) {
            ToastMessage.Send("初始化添加分摊失败!", es);
        }
    }

    //外部接口 查找一个Z_FSJG下所有分摊的分摊去向 20180807
    public static void getAllFtqxByZ_FSJG(final MapInstance mapInstance, final Feature featue_z_fsjg, final AiRunnable callback)
    {
        try{
            getAllFtFeaturesByBDC(mapInstance, featue_z_fsjg, new ArrayList<Feature>(), new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(final T_ t_, Object... objects) {
                    if(t_!=null){
                        final List<Feature> features_ft = new ArrayList<>();
                        features_ft.addAll((List<Feature>)t_);
                        final List<Feature> features_ftqx = new ArrayList<>();
                        final FeatureTable table_h = mapInstance.getTable("H");
                        new AiForEach<Feature>(features_ft,callback){
                            public void exec(){
                                String where_h = "ORID ='"+FeatureHelper.Get(features_ft.get(postion),"FTQX_ID")+"'";
                                MapHelper.QueryOne(table_h, where_h, new AiRunnable(getNext()) {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        if(t_!=null){
                                            features_ftqx.add((Feature)t_);
                                        }
                                        if(postion==features_ft.size()-1)
                                        {
                                            AiRunnable.Ok(callback,features_ftqx,t_);
                                        }else{
                                            AiRunnable.Ok(getNext(),t_,t_);
                                        }
                                        return null;
                                    }
                                });
                            }
                        }.start();
                    }
                    return null;
                }
            });
        }catch (Exception es){
            Log.e(TAG,"查找所有分摊去向失败！"+es);
        }

    }

    //外部接口 计算一个Z_FSJG下所有分摊的每平方米分摊面积 并可选是否级联更新户信息中的分摊建筑面积 20180807
    public static void calculateUnitArea(final MapInstance mapInstance, final Feature featue_z_fsjg, final boolean update_h_info, final AiRunnable callback)
    {
        try{
            final BigDecimal bd_z_fsjg_mj = new BigDecimal(FeatureHelper.Get(featue_z_fsjg,"HSMJ").toString());
            getAllFtqxByZ_FSJG(mapInstance, featue_z_fsjg, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if(t_!=null){
                        BigDecimal bd_all_h_scmj = new BigDecimal("0");
                        BigDecimal bd_unit_ftmj = new BigDecimal("0"); //默认为0
                        for(Feature feature:(List<Feature>)t_)
                        {
                            bd_all_h_scmj = bd_all_h_scmj.add(new BigDecimal(FeatureHelper.Get(feature,"YCJZMJ").toString()));
                        }
                        if(bd_all_h_scmj.doubleValue()>0){
                            bd_unit_ftmj = bd_z_fsjg_mj.divide(bd_all_h_scmj,10,BigDecimal.ROUND_HALF_DOWN); //每平方米建筑面积需要分摊的面积(四舍五入)
                        }

                        if(update_h_info)
                        {  //级联更新户信息
                            for(Feature feature:(List<Feature>)t_)
                            {
                                FeatureHelper.Set(feature,"SCFTJZMJ",(new BigDecimal(FeatureHelper.Get(feature,"YCJZMJ").toString())).multiply(bd_unit_ftmj).toString());
                            }

                            final BigDecimal bd_unit_ftmj_ = bd_unit_ftmj;
                            MapHelper.updateFeature((List<Feature>) t_, new AiRunnable(callback) {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    AiRunnable.Ok(callback,bd_unit_ftmj_.doubleValue(),t_);
                                    return null;
                                }
                            });

                        }else{
                            AiRunnable.Ok(callback,bd_unit_ftmj.doubleValue(),t_);
                        }
                    }
                    return null;
                }
            });
        }catch (Exception es){
            Log.e(TAG,"计算每平米分摊面积失败！"+es);
        }

    }

    //外部接口 更新一个Z_FSJG下所有分摊情况记录的分摊系数和分摊建筑面积 并可选是否级联更新户信息中的分摊建筑面积 20180807
    public static void updateFtByZ_FSJG(final MapInstance mapInstance, final Feature featue_z_fsjg, boolean update_h_info,final AiRunnable callback)
    {
        try{
            final BigDecimal bd_z_fsjg_mj = new BigDecimal(FeatureHelper.Get(featue_z_fsjg,"HSMJ").toString());
            final String z_fsjg_orid = FeatureHelper.Get(featue_z_fsjg,"ORID").toString();
            calculateUnitArea(mapInstance, featue_z_fsjg,update_h_info, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if(t_!=null){
                        final BigDecimal bd_unit_ftmj = new BigDecimal(Double.toString((Double)t_));
                        getAllFtqxByZ_FSJG(mapInstance, featue_z_fsjg, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if(t_!=null){
                                    final List<Feature> features_h = new ArrayList<>();
                                    features_h.addAll((List<Feature>)t_);
                                    final FeatureTable table_ftqk = mapInstance.getTable("FTQK");
                                    new AiForEach<Feature>(features_h,callback){
                                        public void exec(){
                                            Feature feature_h = features_h.get(postion);
                                            final String ftjzmj = (new BigDecimal(FeatureHelper.Get(feature_h,"YCJZMJ").toString())).multiply(bd_unit_ftmj).toString(); //分摊建筑面积
                                            String ftxs_ = "0";
                                            if((new BigDecimal(ftjzmj)).doubleValue()>0){
                                                ftxs_ = (new BigDecimal(ftjzmj)).divide(bd_z_fsjg_mj,10,BigDecimal.ROUND_HALF_DOWN).toString(); //分摊系数 四舍五入
                                            }
                                            final String ftxs = ftxs_;
                                            String where_ftqk = "ORID_PATH like '%"+z_fsjg_orid+"%' and FTQX_ID = '"+FeatureHelper.Get(features_h.get(postion),"ORID")+"'";
                                            MapHelper.QueryOne(table_ftqk, where_ftqk, new AiRunnable(getNext()) {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    if(t_!=null){
                                                        FeatureHelper.Set((Feature)t_,"FTXS",ftxs);
                                                        FeatureHelper.Set((Feature)t_,"FTJZMJ",ftjzmj);
                                                        FeatureHelper.Set((Feature)t_,"FTTDMJ",bd_z_fsjg_mj.toString()); //同时更新Z_FSJG的面积 非必须
                                                        MapHelper.updateFeature((Feature) t_, new AiRunnable(getNext()) {
                                                            @Override
                                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                                AiRunnable.Ok(getNext(),t_,t_);
                                                                return null;
                                                            }
                                                        });
                                                    }
                                                    return null;
                                                }
                                            });

                                        }
                                    }.start();
                                }
                                return null;
                            }
                        });
                    }
                    return null;
                }
            });
        }catch (Exception es){
            Log.e(TAG,"更新分摊系数和分摊建筑面积失败!"+es);
        }
    }

    /*通用工具 删除传入集合中不合法的分摊记录 可选清空后是否重算合法分摊记录的分摊系数和分摊面积 附加选择重算时是否更新分摊去向
       的feature内容 注意：当前仅实现了 orid_path_feature = Z_FSJG ; ftqx_name_feature = H 这一种情形
     (清除依据为：ORID_PATH中记录的父级要素已不存在 或分摊去向中记录的要素已不存在 耗时操作 在适当的地方再调用) 20180810 */
    public static void delInvalidFTQK(final MapInstance mapInstance, final List<Feature> features_ft, final String ftly_name_feature, final String ftqx_name_feature, final boolean recalculate, final boolean update_ftqx_info, final AiRunnable callback)
    {
        try{
            if(features_ft.size()>0)
            {
                final List<Feature> delete_list = new ArrayList<>();
                final FeatureTable table_ftly_name_feature = mapInstance.getTable(ftly_name_feature);
                final FeatureTable table_ftqx_name_feature = mapInstance.getTable(ftqx_name_feature);
                new AiForEach<Feature>(features_ft, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        MapHelper.deleteFeature(delete_list, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if(recalculate) //重新核算
                                {
                                    features_ft.removeAll(delete_list);
                                    if(features_ft.size()>0)
                                    {
                                        final Map<String,String> orid_paths = new HashMap<>();
                                        for(Feature feature_hfft:features_ft)
                                        {
                                            String orid_path = FeatureHelper.Get(feature_hfft,"ORID_PATH").toString();
                                            orid_paths.put(orid_path,"ORID_PATH");
                                        }
                                        Iterator it = orid_paths.entrySet().iterator();
                                        final List<String> need_to_recalcu_paths = new ArrayList<>();
                                        while(it.hasNext())
                                        {
                                            Map.Entry entry = (Map.Entry) it.next();
                                            need_to_recalcu_paths.add(entry.getKey().toString());
                                        }

                                        new AiForEach<String>(need_to_recalcu_paths, new AiRunnable(callback)
                                        {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                AiRunnable.Ok(callback,true,true);
                                                return null;
                                            }
                                        }){
                                            public void exec(){
                                                String where_ftly = "ORID ='"+need_to_recalcu_paths.get(postion)+"'";
                                                MapHelper.QueryOne(table_ftly_name_feature, where_ftly, new AiRunnable(getNext()) {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        if(t_!=null)
                                                        {
                                                            updateFtByZ_FSJG(mapInstance, (Feature) t_, update_ftqx_info, new AiRunnable(getNext()) {
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
                    public void exec(){
                        String where_ftqx = "ORID ='"+FeatureHelper.Get(features_ft.get(postion),"FTQX_ID")+"'";
                        MapHelper.QueryOne(table_ftqx_name_feature, where_ftqx, new AiRunnable(getNext()) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if(t_==null)
                                {
                                    delete_list.add(features_ft.get(postion));
                                    AiRunnable.Ok(getNext(),true,true);
                                }else{
                                    String where_ftly = "ORID ='"+FeatureHelper.Get(features_ft.get(postion),"ORID_PATH")+"'";
                                    MapHelper.QueryOne(table_ftly_name_feature, where_ftly, new AiRunnable(getNext()) {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            if(t_==null)
                                            {
                                                delete_list.add(features_ft.get(postion));
                                            }
                                            AiRunnable.Ok(getNext(),true,true);
                                            return null;
                                        }
                                    });
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
            Log.e(TAG,"删除不合法分摊记录失败！"+es);
            AiRunnable.Ok(callback,false,false);
        }

    }

    /* 通用工具 删除所有不合法的分摊记录 可选清空后是否重算合法分摊记录的分摊系数和分摊面积 附加选择重算时是否更新分摊去向
       的feature内容 注意：当前仅实现了 orid_path_feature = Z_FSJG ; ftqx_name_feature = H 这一种情形
     (清除依据为：ORID_PATH中记录的父级要素已不存在 或分摊去向中记录的要素已不存在 耗时操作 在适当的地方再调用) 20180809 */
    public static void delInvalidFTQK(final MapInstance mapInstance, final String ftly_name_feature, final String ftqx_name_feature, final boolean recalculate, final boolean update_ftqx_info, final AiRunnable callback)
    {
        try{
            FeatureTable table_ftqk = mapInstance.getTable("FTQK");
            MapHelper.Query(table_ftqk, "1=1", new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if(t_!=null)
                    {
                        delInvalidFTQK(mapInstance, (List<Feature>) t_, ftly_name_feature, ftqx_name_feature, recalculate, update_ftqx_info, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if((java.lang.Boolean) t_)
                                {
                                    AiRunnable.Ok(callback,true,true);
                                }else{
                                    AiRunnable.Ok(callback,false,false);
                                }
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
            AiRunnable.Ok(callback,false,false);
            Log.e(TAG,"删除不合法分摊记录失败！"+es);
        }

    }

    /* 通用工具 删除某个不动产下所有不合法的分摊记录 可选清空后是否重算合法分摊记录的分摊系数和分摊面积 附加选择重算时是否更新分摊去向
       的feature内容 注意：当前仅实现了 orid_path_feature = Z_FSJG ; ftqx_name_feature = H 这一种情形
     (清除依据为：ORID_PATH中记录的父级要素已不存在 或分摊去向中记录的要素已不存在 耗时操作 在适当的地方再调用) 20180809 */
    public static void delInvalidFTQK(final MapInstance mapInstance, final Feature feature_bdc, final String ftly_name_feature, final String ftqx_name_feature, final boolean recalculate, final boolean update_ftqx_info, final AiRunnable callback)
    {
        try{
            getAllFtFeaturesByBDC(mapInstance, feature_bdc, new ArrayList<Feature>(), new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if(t_!=null)
                    {
                        delInvalidFTQK(mapInstance, (List<Feature>) t_, ftly_name_feature, ftqx_name_feature, recalculate, update_ftqx_info, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if((java.lang.Boolean) t_)
                                {
                                    AiRunnable.Ok(callback,true,true);
                                }else{
                                    AiRunnable.Ok(callback,false,false);
                                }
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
            AiRunnable.Ok(callback,false,false);
            Log.e(TAG,"删除不合法分摊记录失败！"+es);
        }

    }

    //外部接口 递归查找不动产范围内的所有分摊情况并生成视图 20180802
    public static void load_ft(final MapInstance mapInstance, final Feature feature_bdc, final View ft_view)
    {
        final AiDialog dialog = AiDialog.get(mapInstance.activity).setHeaderView(com.ovit.R.mipmap.app_icon_layer_z_fsjg, "生成分摊记录");
        dialog.setContentView(dialog.getProgressView("正在处理，请稍后..."));

        delInvalidFTQK(mapInstance, feature_bdc, "Z_FSJG", "H", true, true, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                getAllFtFeaturesByBDC(mapInstance, feature_bdc, new ArrayList<Feature>(), new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        ((ViewGroup)ft_view).removeAllViews();
                        ((ViewGroup)ft_view).addView(buildFtViewByList(mapInstance,(List<Feature>)t_));
                        dialog.setCancelable(true).dismiss();
                        return null;
                    }
                });
                return null;
            }
         });

    }


}
