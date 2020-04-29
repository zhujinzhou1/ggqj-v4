package com.ovit.app.map.bdc.ggqj.map.view.bz;

import android.content.DialogInterface;
import android.view.View;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.sketch.SketchTY;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewBZ_TY extends FeatureView {

    public FeatureEdit getEditView() {
        return new FeatureEditBZ_TY(mapInstance, feature);
    }

    @Override
    public void command_change(AiRunnable callback) {
        if (feature != null) {
            try {
                MapHelper.geometry_visible(feature, true);
//                mapInstance.tool.draw_ty(feature, true, callback);
                DrawTy(mapInstance,null);
            } catch (Exception es) {
                ToastMessage.Send("绘图失败", es);
            }
        }
    }

    @Override
    public void setAction() {
        if (table == null) {
            return;
        }
        String groupname = "涂鸦:" + table.getTotalFeatureCount();
        addActionCommon(groupname);
        addActionBus(groupname);
    }

    @Override
    public String addActionBus(String groupname) {
        mapInstance.addAction(groupname, "绘制", R.mipmap.app_icon_tuya_blue, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mapInstance.tool.draw_ty(null, true, null);
                DrawTy(mapInstance,null);
            }
        });


        if (feature != null && table.equals(feature.getFeatureTable())) {
            mapInstance.addAction(groupname, "编辑", R.mipmap.app_icon_map_pan, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    mapInstance.tool.draw_ty(feature, true, null);
                    DrawTy(mapInstance,feature,null);
                }
            });

            addActionSJ(groupname);
            groupname="操作";
            mapInstance.addAction(groupname, "重绘", R.mipmap.app_icon_cachu_blue, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    feature.setGeometry(null);
//                    mapInstance.tool.draw_ty(feature, true, null);
                    DrawTy(mapInstance,feature,null);
                }
            });
            if (table.getGeometryType().equals(GeometryType.POLYGON) || table.getGeometryType().equals(GeometryType.POLYLINE)) {
                mapInstance.addAction(groupname, "切割", R.mipmap.app_icon_map_cut, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        command_cut(null);
                    }
                });
                if (mapInstance.getSelFeatureCount() > 1) {
                    mapInstance.addAction(groupname, "合并", R.mipmap.app_icon_merge, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            command_merge(false,null);
                        }
                    });
                }
            }

            mapInstance.addAction(groupname, "删除", R.mipmap.ic_action_clear, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AiDialog.get(mapInstance.activity).setHeaderView("删除提示")
                            .setContentView("确定删除么？", "该操作不可恢复")
                            .setFooterView(AiDialog.CENCEL, AiDialog.DELETE, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MapHelper.deleteFeature(feature, null);
                                    dialog.dismiss();
                                }
                            }).show();
                }
            });
        } else {
            addActionBZ("",false);
        }
       return groupname;
    }

    public static SketchTY DrawTy (MapInstance mapInstance, AiRunnable callback){
       return SketchTY.Draw(mapInstance,mapInstance.getLayer("BZ_TY"),null,true,callback).start();
    }
    public static SketchTY DrawTy (MapInstance mapInstance, Feature feature, AiRunnable callback){
        return SketchTY.Draw(mapInstance,mapInstance.getLayer("BZ_TY"),feature,true,callback).start();
    }

}
