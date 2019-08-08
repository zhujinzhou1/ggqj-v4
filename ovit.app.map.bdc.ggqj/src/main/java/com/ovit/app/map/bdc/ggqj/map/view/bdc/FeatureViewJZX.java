package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import com.esri.arcgisruntime.data.Feature;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.util.AiUtil;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewJZX extends FeatureView {

    public void fillFeature(Feature f){
        super.fillFeature(f);
        FeatureHelper.Set(f, "JZXCD", AiUtil.Scale(MapHelper.getLength(f.getGeometry(),MapHelper.U_L),2,0d));// x 界址线长度
        // 继承相关属性
        FeatureHelper.Set(f, "JZXLB","2", true, false);//: 界址线类别 墙壁
        FeatureHelper.Set(f, "JZXWZ", "3", true, false);//: 界址线位置间距外
        FeatureHelper.Set(f, "JXXZ", "600001", true, false);//:界线性质 已定界
    }

}
