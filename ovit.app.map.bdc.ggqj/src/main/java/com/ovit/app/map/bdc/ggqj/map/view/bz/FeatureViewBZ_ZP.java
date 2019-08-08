package com.ovit.app.map.bdc.ggqj.map.view.bz;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.esri.arcgisruntime.data.Feature;
import com.ovit.R;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.util.FileUtils;

import java.io.File;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewBZ_ZP extends FeatureView {
    @Override
    public void setIcon(Feature feature, View imageView) {
        String filepath = FeatureEditBZ_ZP.GetImagePath(mapInstance,feature);
        if(FileUtils.exsit(filepath)&& imageView instanceof ImageView){
            ((ImageView) imageView).setImageURI(Uri.fromFile(new File(filepath)));
        }else{
            super.setIcon(feature,imageView);
        }
    }
    @Override
    public String addActionBus(String groupname){
        mapInstance.addAction(groupname, "预览", R.mipmap.app_icon_image_blue, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapInstance.viewFeature(feature);
            }
        });
        return super.addActionBus(groupname);
    }
}
