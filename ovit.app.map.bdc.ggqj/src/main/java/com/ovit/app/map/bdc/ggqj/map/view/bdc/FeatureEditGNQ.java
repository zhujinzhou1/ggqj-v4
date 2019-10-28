package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.esri.arcgisruntime.data.Feature;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.BaseAdapterListener;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LiuSheng on 2017/7/28.
 */

public class FeatureEditGNQ extends FeatureEdit {
    final static String TAG = "FeatureEditGNQ";


    ///region 属性
    FeatureViewGNQ fv;
    Feature feature_ft;
    FeatureView fv_ft;


    ///endregion

    public FeatureEditGNQ(){ super();}
    public FeatureEditGNQ(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }
    ///region  重写父类方法
    public void onCreate() {
        super.onCreate();
        // 使用 fv
        if(super.fv instanceof FeatureViewGNQ) {
            this.fv = (FeatureViewGNQ) super.fv;
        }
    }
    // 初始化
    @Override
    public void init() {
        super.init();
        // 菜单
        menus = new int[]{R.id.ll_info };
    }

    // 显示数据
    @Override
    public void build() {
     final   LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_gnq, v_content);
        try {
            if (feature != null) {
                mapInstance.fillFeature(feature);
                fillView(v_feature);
                String reftype = FeatureHelper.Get(feature,"REFTYPE","");
                String reforid = FeatureHelper.Get(feature,"REFORID","");

                fv_ft =  (FeatureView) mapInstance.newFeatureView(reftype);
                fv_ft.load(reforid, new AiRunnable() {
                     @Override
                     public <T_> T_ ok(T_ t_, Object... objects) {
                         feature_ft = (Feature) t_;
                         if(feature_ft!=null) {
                             feature.setGeometry(feature_ft.getGeometry());
                             change(feature);
                         }
                         return null;
                     }
                 });

                loadGnqs();


                v_feature.findViewById(R.id.tv_addft).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(fv_ft!=null && feature_ft!=null) {
                            FeatureView fv_p = FeatureViewZRZ.From(mapInstance);
                            String orid_p =  mapInstance.getOrid_Match(feature_ft,"ZD");
                            if(StringUtil.IsEmpty(orid_p)){
                                orid_p = mapInstance.getOrid_Match(feature_ft,"ZRZ");
                                fv_p = FeatureViewLJZ.From(mapInstance);
                            }
                            QuickAdapter<Feature> adapter =  fv_p.buildListView(null,"");
                            setListener( adapter);

                            AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_more_blue, "添加分摊")
                                    .addContentView("请选择分摊范围：")
                                    .addContentView(adapter)
                                    .setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                          if(map_sel!=null && map_sel.size()>0){
                                              List<Feature> fs_save = new ArrayList<>();
                                              for (Map.Entry<String,Feature> p  :map_sel.entrySet()) {
                                                  Feature f = featureTable.createFeature();
                                                  fv.fillFeature(f,p.getValue());
                                                  fs_save.add(f);
                                              }
                                              MapHelper.saveFeature(fs_save, new AiRunnable() {
                                                  @Override
                                                  public <T_> T_ ok(T_ t_, Object... objects) {
                                                      loadGnqs();
                                                      return null;
                                                  }
                                              });
                                          }
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    }
                });
            }
        } catch (Exception es) {
            Log.e(TAG, "build: 构建失败", es);
        }
    }

    private void loadGnqs() {
        ViewGroup ll_list_item = (ViewGroup) view.findViewById(R.id.ll_list_item);
        mapInstance.newFeatureView("GNQ").buildListView(ll_list_item,fv.queryChildWhere());
    }
    Map<String ,Feature> map_sel = new LinkedHashMap<>();

    public void  setListener( QuickAdapter<Feature> adapter){
        adapter.setLayoutResId(R.layout.app_ui_ai_aimap_feature_item_sel);
        adapter.setListener(new BaseAdapterListener<Feature, BaseAdapterHelper>(){
            @Override
            public boolean convertAfter(final BaseAdapterHelper helper,final Feature item) {
                final String orid = mapInstance.getOrid(item);
                helper.setImageResource(R.id.v_icon_check, map_sel.containsKey(orid)?R.mipmap.app_ic_radio_check:R.mipmap.app_ic_radio_uncheck);
                helper.setOnClickListener(R.id.v_icon_check, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if( map_sel.containsKey(orid)){
                            map_sel.remove(orid);
                        }else{
                            map_sel.put(orid,item);
                        }
                        helper.setImageResource(R.id.v_icon_check, map_sel.containsKey(orid)?R.mipmap.app_ic_radio_check:R.mipmap.app_ic_radio_uncheck);
                    }
                });
                return super.convertAfter(helper, item);
            }
        });
    }

//   public  QuickAdapter<Feature>  getSelAdapter(String orid_zrz){
//
//       QuickAdapter<Feature> adapter =  fv_p.buildListView(null,"");
//       setListener( adapter);
//   }






    // 保存数据
    @Override
    public void update(final AiRunnable callback) {
        try {
            super.update(callback);
        } catch (Exception es) {
            ToastMessage.Send(activity, "更新属性失败!", TAG, es);
        }
    }
}
