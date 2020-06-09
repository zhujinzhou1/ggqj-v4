package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Point;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.util.AiUtil;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewJZD extends FeatureView {

    public void fillFeature(Feature f){
        super.fillFeature(f);

        if(f!=null && f.getGeometry()!=null) {
            Point p = (Point) f.getGeometry();
            p = MapHelper.geometry_get(p, MapHelper.GetSpatialReference(mapInstance));
            // 覆盖的属性
            FeatureHelper.Set(f, "XZBZ", AiUtil.Scale(p.getX(), 3, 0d));// x
            FeatureHelper.Set(f, "YZBZ", AiUtil.Scale(p.getY(), 3, 0d));// y
        }
        FeatureHelper.Set(f, "JBLX","4", true, false);//: 界标种类 喷涂
        FeatureHelper.Set(f, "JZDLX","4", true, false);//: 界标种类 喷涂
        FeatureHelper.Set(f, "JZXLB","2", true, false);//: 界址线类别 墙壁 （已经舍弃）
        FeatureHelper.Set(f, "JZXWZ", "3", true, false);//: 界址线位置间距外 （已经舍弃）
    }

}
