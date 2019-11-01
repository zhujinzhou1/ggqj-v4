package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.layers.Layer;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by LiuSheng on 2017/7/28.
 */

public class FeatureEditZ_FSJG extends FeatureEdit {
    final static String TAG = "FeatureEditZ_FSJG";
    FeatureViewZ_FSJG fv;
    public FeatureEditZ_FSJG(){ super();}
    public FeatureEditZ_FSJG(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }
    //region  重写父类方法
    // 初始化

    @Override
    public void onCreate() {
        super.onCreate();
        if (super.fv instanceof FeatureViewZ_FSJG) {
            this.fv = (FeatureViewZ_FSJG) super.fv;
        }
    }

    @Override
    public void init() {
        super.init();
        // 菜单
        menus = new int []{   R.id.ll_info ,R.id.ll_ft};
    }
    // 显示数据
    @Override
    public void build() {
        final   LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_z_fsjg, v_content);
        try {
            if (feature != null) {
                mapInstance.fillFeature(feature);
                fillView(v_feature);
                hsmj();
                final Spinner spn_type = (Spinner) v_feature.findViewById(R.id.spn_type);
                final Spinner spn_fhmc = (Spinner) v_feature.findViewById(R.id.spn_fhmc);

                spn_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String value = AiUtil.GetValue(spn_type.getAdapter().getItem(position), "");
                        setDicValue(feature, "TYPE", value);
                        hsmj();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        setValue(feature, "TYPE", null);
                    }
                });
                spn_fhmc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String value = AiUtil.GetValue(spn_fhmc.getAdapter().getItem(position), "");
                        setDicValue(feature, "FHMC", value);
                        hsmj();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        setValue(feature, "FHMC", null);
                    }
                });

                v_feature.findViewById(R.id.tv_jcgx).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((EditText) v_feature.findViewById(R.id.et_id)).setText("");
                        ((EditText) v_feature.findViewById(R.id.et_zh)).setText("");
//                        ((EditText) v_feature.findViewById(R.id.et_zid)).setText("");
                        FeatureHelper.Set(feature,"ID","");
                        FeatureHelper.Set(feature,"ZH","");
                        FeatureHelper.Set(feature,"ZID","");
                        FeatureHelper.Set(feature,"LJZH","");
                        FeatureHelper.Set(feature,"ORID_PATH","");
                        ToastMessage.Send(activity,"保存后，该附属结构将解除与幢的关系！");
                    }
                });

                //添加分摊按钮 20180802
                v_feature.findViewById(R.id.add_ft).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View ft_view = view.findViewById(R.id.ll_ft_content);
                        FeatureEditFTQK.addFt(mapInstance,feature,ft_view);
                    }
                });
                // 宗地分摊
                v_feature.findViewById(R.id.add_ft_zd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                     final AiDialog aiDialog = AiDialog.get(mapInstance.activity).setHeaderView(R.mipmap.app_map_layer_zrz, "添加宗地分摊");
                        aiDialog.addContentView("确定要设置该附属结构为宗地分摊吗？");
                        aiDialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Feature new_feature_ft = mapInstance.getTable("FTQK").createFeature();
                                fv.addFtqk(mapInstance,new_feature_ft,feature);
                                MapHelper.saveFeature(new_feature_ft, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        ToastMessage.Send("新增一条宗地分摊成功！");
                                        return null;
                                    }
                                });

                            }
                        });


                    }
                });

                // 智能识别幢附属结构
                v_feature.findViewById(R.id.tv_znsbzfsjg).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fv.initFeatureZ_fsjg(new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                MapHelper.saveFeature((List<Feature>) t_, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        ToastMessage.Send(activity, "初始化完成！");
                                        return null;
                                    }

                                });
                                return null;
                            }
                        });

                    }
                });


            }
        } catch (Exception es) {
            Log.e(TAG, "build: 构建失败", es);
        }
    }

    @Override
    public void build_opt() {
        super.build_opt();

        addMenu("基本信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_info);
            }
        });

        addMenu("分摊情况", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_ft);
                View ft_view = view.findViewById(R.id.ll_ft_content);
                FeatureEditFTQK.load_ft(mapInstance, feature, ft_view);

            }
        });

        addAction("复制", R.mipmap.app_icon_copy, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeatureViewZ_FSJG.copylc(mapInstance,feature,FeatureHelper.Get(feature,"LC",""),new AiRunnable(){
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        ToastMessage.Send("复制成功");
                        return null;
                    }
                });
            }
        });

    }

    public void hsmj(){
        fv.hsmj(feature,mapInstance);
        fillView(v_content,feature,"MC");
        fillView(v_content,feature,"MJ");
        fillView(v_content,feature,"HSMJ");
    }

    public static void  hsmj(Feature feature,MapInstance mapInstance){
        String fhmc = FeatureHelper.Get(feature,"FHMC","");
        double type  = FeatureHelper.Get(feature,"TYPE",0d);
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        if(g!=null) {
            area = MapHelper.getArea(mapInstance,g);
            if(area>0){
                hsmj = type * area;
            }
        }
        if (fhmc.contains("门廊")){
            // feature.getAttributes().put("MC",(type==0?"":(type==1?"双柱门廊，多柱门廊":"单柱门廊，凹槽式门廊")));
            FeatureHelper.Set(feature,"MC",fhmc+"");
        }else{
            FeatureHelper.Set(feature,"MC",fhmc+""+(type==0?"":(type==1?"（全）":"（半）")));
        }
        FeatureHelper.Set(feature,"MJ", AiUtil.Scale(area,2));
        FeatureHelper.Set(feature,"HSMJ",AiUtil.Scale(hsmj,2));

    }

    // 保存数据
    @Override
    public void update(final AiRunnable callback) {
        try {

            super.update(new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    String lc = FeatureHelper.Get(feature, "LC", "");

                    return null;
                }
            });
        } catch (Exception es) {
            ToastMessage.Send(activity, "更新属性失败!", TAG, es);
        }
    }
    // endregion

}
