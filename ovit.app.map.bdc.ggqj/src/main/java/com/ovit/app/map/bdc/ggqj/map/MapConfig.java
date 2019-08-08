package com.ovit.app.map.bdc.ggqj.map;

import com.ovit.app.ui.ai.component.AiMap;
import com.ovit.app.ui.ai.component.AiView;

/**
 * Created by Lichun on 2018/5/28.
 */

public class MapConfig extends com.ovit.app.map.MapConfig {
    public static  void Init(){
        // 父类的不要忘记了
        com.ovit.app.map.MapConfig.Init();
        // 给AiMap 注册类型
        AiView.TypeMap.put("AiMap", AiMap.class);
        AiMap.InstanceType = MapInstance.class;
    }

}
