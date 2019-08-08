package com.ovit.app.map.bdc.ggqj;


import com.ovit.app.map.bdc.ggqj.map.MapConfig;

/**
 * Created by Lichun on 2018/5/28.
 */

public class App extends com.ovit.App {
    @Override
    public void onCreate() {
         MapConfig.Init();

        super.onCreate();
    }
}
