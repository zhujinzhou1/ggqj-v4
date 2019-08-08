package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.esri.arcgisruntime.data.Feature;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.map.bdc.ggqj.R;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 此生无分起相思 on 2018/7/24.
 */

public class FeatureViewHJXX extends FeatureView
{

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void fillFeature(Feature feature)
    {
        super.fillFeature(feature);
        FeatureHelper.Set(feature, "YHZGX", "其他", false, false);
    }

    @Override
    public String addActionBus(String groupname) {
        return super.addActionBus(groupname);
    }

    @Override
    public void listAdapterConvert(BaseAdapterHelper helper, Feature item, int deep) {
        super.listAdapterConvert(helper, item, deep);;
        helper.setImageResource(com.ovit.R.id.v_icon, R.mipmap.app_map_layer_hjxx);
        helper.setText(com.ovit.R.id.tv_name, FeatureHelper.Get(item,"XM","")+"["+FeatureHelper.Get(item,"YHZGX","其他")+"]");
        String path= FileUtils.getAppDirAndMK(mapInstance.getpath_feature(item) +"附件材料/证件号"  + "/");
        List<String> files_path = FileUtils.getFilePathToSuffix(path, FeatureConstants.SFZ_DSC_FRONT, ".jpg");
        if (files_path.size()>0){
            final String url=files_path.get(0);
            helper.setImageUrl(com.ovit.R.id.v_icon,"file://"+url);
            helper.getView(com.ovit.R.id.v_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView iv = new ImageView(activity);
                    iv.setImageBitmap(BitmapFactory.decodeFile(url));
                    AiDialog.get(activity).setContentView(iv).show();
                }
            });
        }else {
            helper.setImageResource(com.ovit.R.id.v_icon, R.mipmap.app_map_layer_gyrxx);
        }

    }
}
