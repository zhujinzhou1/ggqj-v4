package com.ovit.app.map.bdc.ggqj.map.util;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;

import java.util.List;
import java.util.Map;

/**
 * Created by xw on 2020/4/21.
 */

public class LzDB {

    public static String GetSTREGIONSText(Geometry g){
        List<Point> dbPoints = MapHelper.geometry_getPoints(g);
        if (dbPoints.size() > 2) {
            if (MapHelper.geometry_equals(dbPoints.get(0), dbPoints.get(dbPoints.size() - 1))) {
                // 首位相同 移除
                dbPoints.remove(dbPoints.size() - 1);
            }
        }
        String text = "20191022,1,1"
                + "," + dbPoints.size();
        for (Point p : dbPoints) {
            text += "," + p.getX() + "," + (p.getY());
        };

        return text;
    }

    public static Map<String, Object> GetFeatureMap(Feature feature){

        Map<String, Object> map = feature.getAttributes();
        map.put(FeatureHelper.TABLE_NAME,feature.getFeatureTable().getTableName());
        map.put(FeatureHelper.LAYER_NAME,feature.getFeatureTable().getFeatureLayer().getName());
        return map;
    }

}
