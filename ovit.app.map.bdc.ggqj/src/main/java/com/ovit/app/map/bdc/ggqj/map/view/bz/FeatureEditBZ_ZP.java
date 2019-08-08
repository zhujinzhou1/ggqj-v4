package com.ovit.app.map.bdc.ggqj.map.view.bz;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.ovit.App;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.CameraDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.FileUtils;

import java.io.File;
import java.util.Date;


public class FeatureEditBZ_ZP extends FeatureEdit {
    final static String TAG = "FeatureEditBZ_ZP";

    public FeatureEditBZ_ZP(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }


    @Override
    public void build() {
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_bz_zp, v_content);
        try {
            if (feature != null) {
                fillView(v_feature);
                final String file = GetImagePath(mapInstance, feature);
                if (FileUtils.exsit(file)) {
                    ((ImageView) v_feature.findViewById(R.id.iv_icon)).setImageURI(Uri.fromFile(new File(file)));
                    v_feature.findViewById(R.id.iv_icon).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AiDialog.get(activity).setContentView(new File(file)).show();
                        }
                    });
                }
                v_feature.findViewById(R.id.iv_position).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        MapHelper.selectAddCenterFeature(map,feature);
                        Position(mapInstance,feature);
                    }
                });
            }
        } catch (Exception es) {
            Log.e(TAG, "build: 构建失败", es);
        }
    }
    public static FeatureTable GetTable(final MapInstance mapInstance) {
        return mapInstance.getTable("BZ_ZP");
    }

    public static void Fill(MapInstance mapInstance,Feature feature, final String type, final String tbbsm) {
//        String name = type + tbbsm + AiUtil.GetValue(new Date(), "").replace(":", "：");
        String name  = mapInstance.getOrid(feature)+".jpg";
        mapInstance.fillFeature(feature);
        FeatureHelper.Set(feature, "ZPMC", name, true, false);
        FeatureHelper.Set(feature, "PZSJ", new Date(), true, false);
        FeatureHelper.Set(feature, "PZR", App.getInstance().getUser().getPersonname(), true, false);
//        FeatureHelper.Set(feature, "ZPCCLJ", Path(type, tbbsm, name), true, false);
        FeatureHelper.Set(feature, "ZPCCLJ", mapInstance.getpath_feature(feature)+name, true, false);
        FeatureHelper.Set(feature, "PZZPLX", type, true, false);
        FeatureHelper.Set(feature, "TBBSM", tbbsm, true, false);
        if (feature.getGeometry() != null && feature.getGeometry() instanceof Point) {
            Point p = MapHelper.geometry_get((Point) feature.getGeometry(), SpatialReference.create(4490));
            if (p != null) {
                FeatureHelper.Set(feature, "PSWZZB_X", AiUtil.GetValue(p.getX(), "", AiUtil.F_DECIMAL), true, true);
                FeatureHelper.Set(feature, "PSWZZB_Y", AiUtil.GetValue(p.getY(), "", AiUtil.F_DECIMAL), true, true);
            }
        }
    }

    public static String Dir(String type, String tbbsm) {
        return "外业照片/" + type + "/" + tbbsm;
    }

    public static String Path(String type, String tbbsm, String name) {
        String filepath = Dir(type, tbbsm) + "/" + name + ".jpg";
        filepath = filepath.replace(":", "：");
        return filepath;
    }

    public static String GetImagePath(MapInstance mapInstance, Feature feature) {
        return FileUtils.getAppDirAndMK(mapInstance.getpath_root()) + FeatureHelper.Get(feature, "ZPCCLJ", "");
    }

    public static void Position(final MapInstance mapInstance,Feature feature) {
        String file = GetImagePath(mapInstance, feature);
        String id = mapInstance.getId(feature);
        String name = mapInstance.getName(feature);
        mapInstance.tool.commonTool.markImage((Point) feature.getGeometry(), 0, id, name, file, "", file);
    }

    public static void Camera(final MapInstance mapInstance, final String type, final String tbbsm, final AiRunnable callback){
        final FeatureTable table =GetTable(mapInstance);
        if(table==null){
            AiRunnable.No(callback,null);
            ToastMessage.Send("缺少对应的图层信息！");
            return;
        }
        Point p =   MapInstance.GetLocation(table.getSpatialReference().getWkid());

        if(p!=null){
            final   Feature feature  = table.createFeature();
            p= new Point(p.getX(),p.getY(),table.getSpatialReference());
            MapHelper.geometry_center(p);
            feature.setGeometry(p);
            Fill(mapInstance,feature,type,tbbsm);
            final CameraDialog dialog = new CameraDialog(mapInstance.activity);
            dialog.pictureTaken = new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    try {
                        String filepath =GetImagePath(mapInstance,feature);
                        FileUtils.writeFile(filepath, (byte[] )t_);
                        Point p_ =   MapInstance.GetLocation(table.getSpatialReference().getWkid());
                        if(p_!=null) {
                            p_ = new Point(p_.getX(), p_.getY(), table.getSpatialReference());
                            feature.setGeometry(p_);
                        }
                        MapHelper.saveFeature(feature, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                Position(mapInstance,feature);
                                AiRunnable.Ok(callback,feature);
                                return  null;
                            }
                        });
                    } catch (Exception es) {
                        ToastMessage.Send("写入文件失败", es);
                    }
                    dialog.dismiss();
                    return  null;
                }
            };
            dialog.show();

        }else{
            ToastMessage.Send("暂时无法获取当前位置,请稍后再试");
        }
    }
}
