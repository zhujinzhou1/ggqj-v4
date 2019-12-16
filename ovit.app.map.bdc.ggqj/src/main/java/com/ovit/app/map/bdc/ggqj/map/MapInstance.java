package com.ovit.app.map.bdc.ggqj.map;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.layers.Layer;
import com.ovit.app.map.bdc.ggqj.map.tool.ToolManager;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditC;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditFTQK;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditGNQ;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditGYR;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditHJXX;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditLJZ;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewC;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewFTQK;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewGNQ;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewGYR;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewH;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewHJXX;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewH_FSJG;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewJZD;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewJZX;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewLJZ;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewQLR;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZD;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZRZ;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZ_FSJG;
import com.ovit.app.map.bdc.ggqj.map.view.bz.FeatureEditBZ_D;
import com.ovit.app.map.bdc.ggqj.map.view.bz.FeatureViewBZ_TY;
import com.ovit.app.map.bdc.ggqj.map.view.bz.FeatureViewBZ_ZP;
import com.ovit.app.map.bdc.ggqj.map.view.common.FeatureEditCLD;
import com.ovit.app.map.bdc.ggqj.map.view.dw.FeatureEditDZDW;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditH;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditH_FSJG;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditJZD;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditJZX;
import com.ovit.app.map.bdc.ggqj.map.view.dw.FeatureEditMZDW;
import com.ovit.app.map.bdc.ggqj.map.view.bz.FeatureEditBZ_M;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditQLR;
import com.ovit.app.map.bdc.ggqj.map.view.bz.FeatureEditBZ_WZ;
import com.ovit.app.map.bdc.ggqj.map.view.dw.FeatureEditXZDW;
import com.ovit.app.map.bdc.ggqj.map.view.bz.FeatureEditBZ_X;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditZD;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditZRZ;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditZ_FSJG;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.bdc.ggqj.map.view.v.V_Project;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.LayerConfig;
import com.ovit.app.ui.ai.component.AiMap;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.ItemRunnable;
import com.ovit.app.util.ListUtil;
import com.ovit.app.util.StringUtil;

import java.util.List;

/**
 * Created by Lichun on 2017/7/4.
 */

public class MapInstance extends com.ovit.app.map.model.MapInstance {

    public static String TAG = "MapInstance";

    public Feature bindZDFeature;

    // 工具管理
    public transient ToolManager tool;


    public MapInstance() {
        super();
    }

    public MapInstance(AiMap aiMap) {
        super(aiMap);
    }

    @Override
    public void set(AiMap aiMap) {
        super.set(aiMap);
        registFeatureEditTypeMap();
        registFeatureViewTypeMap();
    }

    @Override
    public ToolManager onCreateTool() {
        tool = new ToolManager(aiMap);
        return tool;
    }

    // 用于被重写
    public FeatureView newFeatureView(){
        return new FeatureView(this);
    }
    public FeatureView newFeatureView(Feature feature){
         FeatureView fv = (FeatureView) FeatureView.From(this,feature);
         if(fv ==null){
             return newFeatureView();
         }
        return fv ;
    }

    public void setSelectLayer(Layer layer, AiRunnable callback, boolean isAutoSwitchView) {
        m_selectLayer = layer;
        mapInstance.setBindCallback(callback);
        if (features_sel != null && !features_sel.isEmpty()) {
            ListUtil.Filter(features_sel, new ItemRunnable<Feature>() {
                @Override
                public boolean filter(Feature item, int index) {
                    return item.getFeatureTable().getFeatureLayer() == m_selectLayer;
                }
            }).getValue();
            if (features_sel.isEmpty() && !features_selected.isEmpty()) {
                List<Feature> sels = ListUtil.Filter(features_selected, true, new ItemRunnable<Feature>() {
                    @Override
                    public boolean filter(Feature item, int index) {
                        return item.getFeatureTable().getFeatureLayer() == m_selectLayer;
                    }
                }).getValue();
                features_sel.addAll(sels);

            }
        }
        updateMenu();
        if( isAutoSwitchView &&!features_selected.isEmpty()){
            if (tool.getLl_opttools_c()!=null&&tool.getLl_opttools_c().getChildCount()>0){
                featureView.command_info();
            }
        }
    }

    @Override
    public String getId(Feature feature) {
        if(feature!=null) {
            LayerConfig config = getLayerConfig(feature);
            if (config != null && StringUtil.IsNotEmpty(config.getCol_id())) {
                return FeatureHelper.Get(feature, config.getCol_id(), "");
            }
        }
        return "";
    }
    @Override
    public String getLabel(Feature feature) {
        String label = "";
        if (feature.getFeatureTable().getTableName().equals("ZD")) {
            //[PRO_ZDDM_F]  & vbCrLf   & [YT] & vbCrLf  & [QLRXM]
            label = AiUtil.GetValue(feature.getAttributes().get("PRO_ZDDM_F"), "") + "\n"
                    + AiUtil.GetValue(feature.getAttributes().get("YT"), "") + "\n"
                    + AiUtil.GetValue(feature.getAttributes().get("QLRXM"), "");
        } else if (feature.getFeatureTable().getTableName().equals("ZRZ")) {
            String fwjg = DicUtil.dic(activity,"fwjg",AiUtil.GetValue(feature.getAttributes().get("FWJG"), ""));
            fwjg = StringUtil.substr(fwjg,"[","]");

            //[ZH] & "#  " & [ZZDMJ] & vbCrLf   & [JZWMC]  & " " &  [ZCS]
            label = "("+ AiUtil.GetValue(feature.getAttributes().get("ZH"), 1)+") "
                    + AiUtil.GetValue(feature.getAttributes().get("SCJZMJ"), "",AiUtil.F_FLOAT2) + "\n"
//                    + AiUtil.GetValue(feature.getAttributes().get("JZWMC"), "")+" "
                    + fwjg+" "
                    + AiUtil.GetValue(feature.getAttributes().get("ZCS"),1);
        } else {
            label = getName(feature);
        }
        return label;
    }


    @Override
    public V_Project getProject() {
        if (v_project == null) {
            v_project = new V_Project(this);
        }
        return (V_Project)v_project;
    }


    public void registFeatureEditTypeMap(){

        FeatureEdit.TypeMap.put("ZD",FeatureEditZD.class);
        FeatureEdit.TypeMap.put("ZRZ",FeatureEditZRZ.class);
        FeatureEdit.TypeMap.put("LJZ",FeatureEditLJZ.class);
        FeatureEdit.TypeMap.put("ZRZ_C",FeatureEditC.class);
        FeatureEdit.TypeMap.put("GNQ",FeatureEditGNQ.class);
        FeatureEdit.TypeMap.put("Z_FSJG",FeatureEditZ_FSJG.class);
        FeatureEdit.TypeMap.put("H",FeatureEditH.class);
        FeatureEdit.TypeMap.put("H_FSJG",FeatureEditH_FSJG.class);

        FeatureEdit.TypeMap.put("QLRXX",FeatureEditQLR.class);
        FeatureEdit.TypeMap.put("GYRXX", FeatureEditGYR.class); //20180719
        FeatureEdit.TypeMap.put("HJXX", FeatureEditHJXX.class); //20180719
        FeatureEdit.TypeMap.put("FTQK", FeatureEditFTQK.class); //20180802
        FeatureEdit.TypeMap.put("JZD",FeatureEditJZD.class);
        FeatureEdit.TypeMap.put("JZX",FeatureEditJZX.class);

        FeatureEdit.TypeMap.put("ZJ_D",FeatureEditBZ_D.class);
        FeatureEdit.TypeMap.put("ZJ_X",FeatureEditBZ_X.class);
        FeatureEdit.TypeMap.put("ZJ_M",FeatureEditBZ_M.class);
        FeatureEdit.TypeMap.put("ZJ_WZ",FeatureEditBZ_WZ.class);

        FeatureEdit.TypeMap.put("DZDW",FeatureEditDZDW.class);
        FeatureEdit.TypeMap.put("XZDW",FeatureEditXZDW.class);
        FeatureEdit.TypeMap.put("MZDW",FeatureEditMZDW.class);

        FeatureEdit.TypeMap.put("KZD",FeatureEditCLD.class);
    }

    public void registFeatureViewTypeMap(){
         // bdc
        FeatureView.TypeMap.put("ZD",FeatureViewZD.class);
        FeatureView.TypeMap.put("ZRZ",FeatureViewZRZ.class);
        FeatureView.TypeMap.put("LJZ",FeatureViewLJZ.class);
        FeatureView.TypeMap.put("ZRZ_C",FeatureViewC.class);
        FeatureView.TypeMap.put("GNQ",FeatureViewGNQ.class);
        FeatureView.TypeMap.put("Z_FSJG",FeatureViewZ_FSJG.class);
        FeatureView.TypeMap.put("H",FeatureViewH.class);
        FeatureView.TypeMap.put("H_FSJG", FeatureViewH_FSJG.class);

        FeatureView.TypeMap.put("QLRXX",FeatureViewQLR.class);
        FeatureView.TypeMap.put("GYRXX",FeatureViewGYR.class); //20180719
        FeatureView.TypeMap.put("HJXX",FeatureViewHJXX.class); //20180719
        FeatureView.TypeMap.put("FTQK",FeatureViewFTQK.class); //20180802
        FeatureView.TypeMap.put("JZX",FeatureViewJZX.class);
        FeatureView.TypeMap.put("JZD",FeatureViewJZD.class);

        FeatureView.TypeMap.put("BZ_TY",FeatureViewBZ_TY.class);
        FeatureView.TypeMap.put("BZ_ZP",FeatureViewBZ_ZP.class);
    }
}
