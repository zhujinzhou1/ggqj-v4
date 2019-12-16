package com.ovit.app.map.bdc.ggqj.map.tool;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.esri.arcgisruntime.data.Feature;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditBDC;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditQLR;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditZD;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.bdc.ggqj.map.view.v.V_Task;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.view.FeatureEdit;
import com.ovit.app.ui.ai.component.AiMap;
import com.ovit.app.ui.ai.component.custom.CircleImageView;
import com.ovit.app.ui.ai.data.AiCommand;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.ui.view.CView;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.StringUtil;


public class ToolManager extends com.ovit.app.map.tool.ToolManager {

    //region 属性
    public transient static final String TAG = "ToolManager";

    public ToolManager(AiMap aiMap) {
        super(aiMap);
    }
    public MapInstance getInstance(){
        return (MapInstance) super.getInstance();
    }


    public  static  MapInstance getMapInstance(AiMap aimap){
        return (MapInstance) aimap.getInstance();
    }
    //region 提供静态接入，主要用与提供AiCommand调用Map_Opt_wz

    public static boolean Map_Opt_xzq(AiMap aimap, AiCommand command) {
        if (getMapInstance(aimap).tool != null) {
            return getMapInstance(aimap).tool.map_opt_xzq(command.CommandType, "行政区", command.CommandType);
        }
        return false;
    }

    public static boolean Map_Opt_wz(AiMap aimap, AiCommand command) {
        if (aimap.getInstance().tool != null) {
            return getMapInstance(aimap).tool.map_opt_wz(command.CommandType, "位置", command.CommandType);
        }
        return false;
    }

    public static boolean Map_Opt_xm(AiMap aimap, AiCommand command) {
        if (aimap.getInstance().tool != null) {
            return getMapInstance(aimap).tool.map_opt_xm(command.CommandType, "权利人", "", command.CommandType);
        }
        return false;
    }

    public static boolean Map_Opt_me(AiMap aimap, AiCommand command) {
        if (aimap.getInstance().tool != null) {
            return getMapInstance(aimap).tool.map_opt_me(command.CommandType, "我的任务", command.CommandType);
        }
        return false;
    }
    //endregion


    public boolean map_opt_search(String id, String name,String key) {
        if (StringUtil.IsNotEmpty((key + "").replace(" ", ""))) {
            return   FeatureView.ViewSearch(getInstance(),name,key);
        }
        return false;
    }
    // 显示宗地
    public boolean map_opt_wz(String id, String name, Object datakey) {
        final View zdview = getInstance().newFeatureView(getInstance().getTable("ZD")).getListView("",0,null);
        if(((ViewGroup)zdview).getChildCount()>0)
        {
            ((ViewGroup)zdview).getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
        }
        final CView cview = map_opt_cview(id,name,datakey,zdview,true,true,null);
        map_opt_show(cview);

        return true;
    }

    // 显示行政区
    public boolean map_opt_xzq(String id, String name, Object datakey) {
        FeatureView.ViewLayer(getInstance(),"XZQ") ;
        return true;
    }

    // 显示项目
    public boolean map_opt_xm(final String id, final String name, String where, final Object datakey) {
        return map_opt_xm(id, name, where, "", datakey);
    }

    // 显示项目
    public boolean map_opt_xm(final String id, final String name, final String where, final String searchWhere, final Object datakey) {
        View v_ = null;
        final CView cview = map_opt_cview(id,name,datakey, v_,false,true,null);
        FeatureEditQLR.GetView_QLR(getInstance(), cview, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                map_opt_show(cview);
                return null;
            }
        });

        return true;
    }

    public boolean map_opt_me(String id, String name, Object datakey) {
        View xmview = new V_Task(getInstance()).getView_Task();
//        View xmview = layerTool.getMeViews();
//
        final CView cview = map_opt_cview(id,name,datakey,xmview,false,true,null);
        map_opt_show(cview);

        cview.setFloatRightAction(R.mipmap.app_icon_more_white, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                map_opt_project("map_opt_project", "项目", "project");
                return null;
            }
        });

        return true;
    }

    public View getProjectView(){
        return  getInstance().getProject().getView_Project();
    }
    public boolean map_opt_project(String id, String name, Object datakey) {
        View v = getProjectView();
        final CView cview = map_opt_cview(id,"",datakey,v,false,true,null);
        map_opt_show(cview);
        return true;
    }

    public boolean map_opt_showfeature_bdc(Activity activity, String id, String name, Feature featureZDorH, final AiRunnable callback) {
        Object datekey = featureZDorH.getAttributes().get("GLOBALID") + "";
        final CView cview =  map_opt_cview(id,name,datekey,null,false,true,callback);
        final FeatureEdit featureEdit = new FeatureEditBDC(getInstance(), featureZDorH, R.layout.app_ui_ai_aimap_opt_feature);
//        final CView cview = CView.get(activity, id, featureZDorH.getAttributes().get("GLOBALID") + "").initLayout();

//        cview.v_head_right_next.setImageResource(R.mipmap.app_icon_opt_ok);
//        featureEdit.v_ok = cview.v_head_right_next;
        featureEdit.onOk = new Runnable() {
            @Override
            public void run() {
                featureEdit.update(new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        //cview.close();
                        ToastMessage.Send("保存成功");

                        AiRunnable.Ok(callback,t_,objects); return  null;
//                        return super.ok(t_, objects);
                    }
                });
            }
        };

        View v_ = featureEdit.get();
//        v_.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        cview.setView(v_,true);
        map_opt_show(cview);
        return true;
    }
}

