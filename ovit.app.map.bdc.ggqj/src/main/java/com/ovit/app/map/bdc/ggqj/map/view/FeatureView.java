package com.ovit.app.map.bdc.ggqj.map.view;

import android.content.DialogInterface;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.ImmutablePart;
import com.esri.arcgisruntime.geometry.Multipart;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.Segment;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.R;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditH_FSJG;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditJZD;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureEditJZX;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewH;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewLJZ;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZD;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZRZ;
import com.ovit.app.map.bdc.ggqj.map.view.bdc.FeatureViewZ_FSJG;
import com.ovit.app.map.bdc.ggqj.map.view.bz.FeatureEditBZ_ZP;
import com.ovit.app.map.bdc.ggqj.map.view.bz.FeatureViewBZ_TY;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.LayerConfig;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.ai.component.AiWindow;
import com.ovit.app.ui.dialog.DialogBuilder;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.AppConfig;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.Session;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureView extends com.ovit.app.map.view.FeatureView {

    public final static String TAG = "FeatureView";
    // 覆盖父类
    public MapInstance mapInstance;

    //region

    public FeatureView() {
        super();
    }
    public FeatureView(MapInstance mapInstance) {
        super(mapInstance);
    }

    public FeatureView(MapInstance mapInstance, FeatureTable featureTable) {
        super(mapInstance,featureTable);
    }

    public FeatureView(MapInstance mapInstance, FeatureLayer layer) {
        super(mapInstance,layer);
    }

    public FeatureView(MapInstance mapInstance, Feature feature) {
        super(mapInstance,feature);
    }

    public FeatureView(MapInstance mapInstance, String... names) {
        super(mapInstance, mapInstance.getTable(names));
    }

    @Override
    public void onCreate() {
        if(super.mapInstance instanceof MapInstance) {
            this.mapInstance = (MapInstance) super.mapInstance;
        }
    }

    public void fillFeature(Feature feature,Feature feature_p) {
        super.fillFeature(feature,feature_p);

        FeatureHelper.Set(feature, "PRO_USERID", Session.get("Userid"));
        FeatureHelper.Set(feature, "PRO_UPDATETIME", new Date());
        FeatureHelper.Set(feature, "DCRQ", new Date(), true, false);
        if (FeatureHelper.Exist(feature, "SCMJ_M")) {
            FeatureHelper.Set(feature, "SCMJ_M", MapHelper.getArea_M(feature.getGeometry()));
        }
        if (FeatureHelper.Exist(feature, "SCMJ")) {
            FeatureHelper.Set(feature, "SCMJ", MapHelper.getArea(feature.getGeometry()));
        }
        String xmbm = getXmbm();
        if (xmbm.length() == 12 && !xmbm.endsWith("000")) {
            // 有效项目代码
            FeatureHelper.Set(feature, "XMDM", xmbm);
            FeatureHelper.Set(feature, "XMBM", xmbm);
            FeatureHelper.Set(feature, "DJZQDM", xmbm);
        }
    }

    public void fillFeature( final Feature feature,final AiRunnable callback) {
        if(feature.getGeometry()!=null){
            load_djzq(feature.getGeometry(), new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    final String  djzqdm = t_+"";
                    if (StringUtil.IsNotEmpty(djzqdm)){
                        FeatureHelper.Set(feature,"XMDM",djzqdm);
                        FeatureHelper.Set(feature,"XMBM",djzqdm);
                        FeatureHelper.Set(feature,"DJZQDM",djzqdm);
                    }
                    load_tf(feature.getGeometry(), new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            String  tfh = t_+"";
                            if (StringUtil.IsNotEmpty(tfh)){
                                FeatureHelper.Set(feature,"TFH",tfh);
                            }
                            fillFeature(feature);
                            AiRunnable.Ok(callback,t_,objects);
                            return null;
//                           return super.ok(t_,objects);
                        }
                    });
                    return null;
                }
            });
        }else{
            fillFeature(feature);
            AiRunnable.Ok(callback,feature);
        }
    }


    //endregion

    public String addActionBus(String groupname){
        addActionTY(groupname);
        addActionPZ(groupname);
        addActionSJ(groupname);
        addActionDW("地物" );
        addActionBZ("标注",false);
        return  groupname;
    }

    public void addActionTY(String groupname){
        mapInstance.addAction(groupname, "涂鸦", R.mipmap.app_map_layer_bz_ty, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeatureViewBZ_TY.DrawTy(mapInstance,null);
            }
        });
    }

    public void addActionPZ(String groupname){
        mapInstance.addAction(groupname, "拍照", R.mipmap.app_map_layer_bz_zp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeatureEditBZ_ZP.Camera(mapInstance, getLayerName(), getId(), null);
            }
        });
    }
    public void addActionSJ(String groupname){
        mapInstance.addAction(groupname, "栓距", R.mipmap.app_map_layer_bz_sj, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_bz_sj( null);
            }
        });
    }

    public void addActionBZ(String groupname,boolean isall){
        if(StringUtil.IsEmpty(groupname)) {
            groupname="标注";
        }
        if(isall) {
            addActionTY(groupname);
//            addActionPZ(groupname);
        }
        mapInstance.addAction(groupname, "文字", R.mipmap.app_map_layer_bz_wz, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_dwOrbz("BZ_WZ","","文字");
            }
        });
        mapInstance.addAction(groupname, "点", R.mipmap.app_map_layer_bz_d, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_dwOrbz("BZ_D","","点");
            }
        });
        mapInstance.addAction(groupname, "线", R.mipmap.app_map_layer_bz_x, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_dwOrbz("BZ_X","","线");
            }
        });
        mapInstance.addAction(groupname, "面", R.mipmap.app_map_layer_bz_m, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_dwOrbz("BZ_M","","面");
            }
        });
    }
    public void addActionDW(String groupname){
        if(StringUtil.IsEmpty(groupname)) {
            groupname="地物";
        }
        mapInstance.addAction(groupname, "围墙（单）", R.mipmap.app_icon_weiqiang_d, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_dwOrbz("XZDW","不依比例围墙","围墙");
            }
        });
        mapInstance.addAction(groupname, "围墙（双）", R.mipmap.app_icon_weiqiang_s, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_dwOrbz("XZDW","依比例围墙","围墙");
            }
        });
        mapInstance.addAction(groupname, "点", R.mipmap.app_map_layer_dzdw, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_dwOrbz("DZDW","","点地物");
            }
        });
        mapInstance.addAction(groupname, "线", R.mipmap.app_map_layer_xzdw, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_dwOrbz("XZDW","","线地物");
            }
        });
        mapInstance.addAction(groupname, "面", R.mipmap.app_map_layer_mzdw, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw_dwOrbz("MZDW","","面地物");
            }
        });
    }

    public  String getZddm() {
        return FeatureHelper.Get(feature,"ZDDM","");
    }
    public  String getZrzh() {
        return FeatureHelper.Get(feature,"ZRZH","");
    }
    public  String getLjzh() {
        return FeatureHelper.Get(feature,"LJZH","");
    }
    public  String getMph(String ljzh) {
        String mph="1-1";
         if (FeatureHelper.isLJZHValid(ljzh)){
             String ljzhIndex = StringUtil.substr_last(ljzh, 2);
             String zh = ljzh.substring(8,12);
             try {
              mph=Integer.parseInt(zh)+"-"+Integer.parseInt(ljzhIndex);
             }catch (Exception e){

             }
         }
         return mph;
    }
    public  String getBdcdyh() {
        return FeatureHelper.Get(feature,"BDCDYH","");
    }

    // 详情
    @Override
    public void command_info() {
        if(feature!=null){
            String name = feature.getFeatureTable().getFeatureLayer().getName();

            if (mapInstance.bindZDFeature != null && "宗地".equals(name)) {
                String bindZDFeatureName = mapInstance.bindZDFeature.getFeatureTable().getFeatureLayer().getName();
                // zrz 与 zd 关联
                if ("自然幢".equals(bindZDFeatureName)){
                    DialogBuilder.confirm(activity, "绑定宗地", "自然幢是否绑定该宗地？", null, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String zddm = FeatureHelper.Get(feature, "ZDDM", "");
                            final String bdcdyh = FeatureHelper.Get(feature, "BDCDYH", "");
                            FeatureViewZRZ.From(mapInstance,feature).getMaxZrzh(zddm, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    String id = "";
                                    if (objects.length > 1) {
                                        // 最大号加1
                                        int count = AiUtil.GetValue(objects[1], 0) + 1;
                                        id = zddm + "F" + String.format("%04d", count);
                                    }
                                    mapInstance.bindZDFeature.getAttributes().put("ZRZH", id);
                                    mapInstance.fillFeature(mapInstance.bindZDFeature);
//                                    FeatureEditZRZ.FillFeature(mapInstance.bindZDFeature, bdcdyh.endsWith("F99990001"));
                                    MapHelper.saveFeature(mapInstance.bindZDFeature, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            mapInstance.bindZDFeature = null;
                                            if (feature != null) {
                                                mapInstance.viewFeature(feature);
                                            }
//                                            return super.ok(t_, objects);
//                                            AiRunnable.Ok(callback,t_,objects);
                                            return  null;
                                        }
                                    });
                                    return null;
                                }
                            });
                        }
                    }, "放弃", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mapInstance.bindZDFeature = null;
                        }
                    },"继续",null).create().show();
                }else if ("权利人".equals(bindZDFeatureName)){
                    // 权利人 绑定宗地
                    //String filename = AiUtil.GetValue(civ_zjh.getContentDescription(), "材料");
                    //civ_zjh.setName(filename + "(正反面)").setDir(FileUtils.getAppDirAndMK(getpath_root() + FeatureHelper.Get(feature, "XM", "")+"/"+"附件材料/" + filename + "/"))
                    DialogBuilder.confirm(activity, "关联宗地", "权利人是否关联该宗地？", null, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String qlrdm = (String) mapInstance.bindZDFeature.getAttributes().get("QLRDM");
                            if (StringUtil.IsNotEmpty(qlrdm)){
                                feature.getAttributes().put("QLRDM",qlrdm);
                            }
                            String xm = (String) mapInstance.bindZDFeature.getAttributes().get("XM");
                            if (StringUtil.IsNotEmpty(xm)){
                                feature.getAttributes().put("QLRXM",xm);
                            }
                            String dh = (String) mapInstance.bindZDFeature.getAttributes().get("DH");
                            if (StringUtil.IsNotEmpty(dh)){
                                feature.getAttributes().put("QLRDH",dh);
                            }
                            String dz = (String) mapInstance.bindZDFeature.getAttributes().get("DZ");
                            if (StringUtil.IsNotEmpty(dz)){
                                feature.getAttributes().put("QLRTXDZ",dz);
                            }
                            String zjzl = (String) mapInstance.bindZDFeature.getAttributes().get("ZJZL");
                            if (StringUtil.IsNotEmpty(zjzl)){
                                feature.getAttributes().put("QLRZJZL",zjzl);
                            }
                            String zjh = (String) mapInstance.bindZDFeature.getAttributes().get("ZJH");
                            if (StringUtil.IsNotEmpty(zjh)){
                                feature.getAttributes().put("QLRZJH",zjh);
                            }
                            String bdcqzh = (String) mapInstance.bindZDFeature.getAttributes().get("BDCQZH");
                            if (StringUtil.IsNotEmpty(bdcqzh)){
                                feature.getAttributes().put("TDZH",bdcqzh);
                            }
// 写身份证照片
//                                String filename = AiUtil.GetValue(civ_zjh.getContentDescription(), "材料");
//                                civ_zjh.setName(filename + "(正反面)").setDir(FileUtils.getAppDirAndMK(getpath_root() +"附件材料/" + filename + "/"))
                            String f_path = mapInstance.getpath_feature(mapInstance.bindZDFeature);
                            String fzd_path = mapInstance.getpath_feature(feature);

                            String f_zjh_path= FileUtils.getAppDirAndMK(f_path+"/"+"附件材料/证件号/");
                            String fzd_zjh_path=FileUtils.getAppDirAndMK(fzd_path +"/"+"附件材料/权利人证件号/");
                            FileUtils.copyFile(f_zjh_path,fzd_zjh_path);

                            String f_zmcl_path=FileUtils.getAppDirAndMK(f_path +"/"+"附件材料/土地权属来源证明材料/");
                            String fzd_zmcl_path=FileUtils.getAppDirAndMK(fzd_path+"/"+"附件材料/土地权属来源证明材料/");
                            FileUtils.copyFile(f_zmcl_path,fzd_zmcl_path);

                            String f_hkb_path=FileUtils.getAppDirAndMK(f_path +"/"+"附件材料/户口簿/");
                            String fzd_hkb_path=FileUtils.getAppDirAndMK(fzd_path +"/"+"附件材料/户口簿/");
                            FileUtils.copyFile(f_hkb_path,fzd_hkb_path);

                            MapHelper.saveFeature(feature, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    mapInstance.bindZDFeature = null;
                                    if (feature != null) {
                                        mapInstance.viewFeature(feature);
                                    }
//                                        return super.ok(t_, objects);
//                                        AiRunnable.Ok(callback,t_,objects);
                                    return  null;
                                }
                            });
                        }
                    }, "放弃", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mapInstance.bindZDFeature = null;
                        }
                    },"继续",null).create().show();

                }

            } else {
                //tool.map_opt_showfeature("map_opt_showfeature", MapHelper.getLayerName(feature), feature);
                mapInstance.viewFeature(feature);
            }

        }
    }

    public void command_cut(final AiRunnable callback){
        final List<Feature> fs_adds = new ArrayList<>();
        final List<Feature> fs_updates = new ArrayList<>();

                command_cut(fs_adds, fs_updates, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        final AiRunnable run = new AiRunnable(callback) {
                             @Override
                             public <T_> T_ ok(T_ t_, Object... objects) {
                                 final List<Feature> fs_save = new ArrayList<>();
                                 fs_save.addAll(fs_adds);
                                 fs_save.addAll(fs_updates);
                                 mapInstance.fillFeature(fs_save);
                                 MapHelper.saveFeature(fs_save, new AiRunnable(callback) {
                                     @Override
                                     public <T_> T_ ok(T_ t_, Object... objects) {
                                         mapInstance.clearSelectFeature();
                                         mapInstance.setSelectFeature(fs_save);
                                         layer.retryLoadAsync();
                                         AiRunnable.Ok(callback, fs_save);
                                         return null;
                                     }
                                 });
                                return null;
                             }
                         };
                        if (fs_adds.size() > 0) {
                            if (layer == MapHelper.getLayer(map, "ZD")) {
                                Feature f = fs_adds.get(0);
                                FeatureViewZD.GetMaxZddm(mapInstance,f, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        String maxid = objects[0] + "";
                                        if (objects.length > 1) {
                                            // 最大号加1
                                            int count = AiUtil.GetValue(objects[1], 0) + 1;
                                            for (Feature feature : fs_adds) {
                                                String id = StringUtil.fill(String.format("%05d", count),maxid,true);
                                                FeatureHelper.Set(feature,"ZDDM",id);

                                                count++;
                                            }
                                        }
                                        AiRunnable.Ok(run,null);
                                        return null;
                                    }
                                });
                                return null;
                            } else {
                                for (Feature f_a : fs_adds) {
                                    FeatureHelper.Set(f_a,"ZDDM","");
                                    FeatureHelper.Set(f_a,"BDCDYH","");
                                    FeatureHelper.Set(f_a,"ZRZH","");
                                    FeatureHelper.Set(f_a,"ZH","");
                                    FeatureHelper.Set(f_a,"ZID","");
                                    FeatureHelper.Set(f_a,"HH","");
                                    FeatureHelper.Set(f_a,"HID","");
                                    FeatureHelper.Set(f_a,"ID","");
                                }
                            }
                        }
                        AiRunnable.Ok(run,null);
                        return null;
                    }
                });

    }
    // 栓距
    public void command_shuanjiu() {

        final FeatureTable ft = MapHelper.getLayer(map, "ZJ_X").getFeatureTable();

        Feature f = ft.createFeature();
        f.getAttributes().put("FHMC", "栓距");
        f.getAttributes().put("TITLE", "栓距");
        mapInstance.requireFeatureView(f).command_change(null);


//        Feature feature = ft.createFeature();
//        tool.collection(null).setSelectOneFeature(feature).setOnCompleteListener(new CollectionTool.OnListener() {
//            @Override
//            public void complete(Coord coord) {
//                List<Feature> fs = new ArrayList<Feature>();
//                ArrayList<Point> ps =  coord.points;
//                Point p = null;
//                for (Point p_ :ps  ) {
//                    if(p!=null){
//                        Feature f = ft.createFeature();
//                        f.getAttributes().put("FHMC", "栓距");
//                        f.getAttributes().put("TITLE", "栓距");
//                        MapHelper.setFeatureGeomtry(f,Arrays.asList(new Point[]{ p,p_}));
//                        BaseTool.fillFeature(f,aiMap.JsonData);
//                        fs.add(f);
//                    }
//                    p=p_;
//                }
//                MapHelper.saveFeature(fs,null);
//            }
//
//            @Override
//            public void hide() {
//
//            }
//        }); ;
    }

    // 地物
    public void command_diwu(View view) {
         selectDw(view);
    }

    // 注记
    public void command_zhuji(View view) {
         selectZj(view);
    }



    // 选择地物
    public void selectDw(final View view) {
        final AiWindow aiWindow = new AiWindow(activity);
        aiWindow.autoShow = true;
        aiWindow.layout_resid = com.ovit.R.layout.app_ui_ai_aipop;
        aiWindow.showing = new Runnable() {
            @Override
            public void run() {
                LinearLayout view_ll = (LinearLayout) LayoutInflater.from(activity).inflate(
                        com.ovit.R.layout.app_ui_ai_aimap_action_dw, null);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            command_change(v);
                            aiWindow.window.dismiss();
                        } catch (Exception es) {
                            Log.e(TAG, "绘制:地物 ", es);
                        }
                    }
                };
                view_ll.findViewById(com.ovit.R.id.ll_dzdw).setOnClickListener(listener);
                view_ll.findViewById(com.ovit.R.id.ll_xzdw).setOnClickListener(listener);
                view_ll.findViewById(com.ovit.R.id.ll_mzdw).setOnClickListener(listener);
                view_ll.findViewById(com.ovit.R.id.ll_wq).setOnClickListener(listener);

                aiWindow.addView(view_ll);
                aiWindow.setDefOpt();
                aiWindow.setSizeAuto();
                aiWindow.window.showAsDropDown(view);
            }
        };
        aiWindow.Build();
    }
    // 选择注记
    public void selectZj(final View view) {
        final AiWindow aiWindow = new AiWindow(activity);
        aiWindow.autoShow = true;
        aiWindow.layout_resid = com.ovit.R.layout.app_ui_ai_aipop;
        aiWindow.showing = new Runnable() {
            @Override
            public void run() {
                LinearLayout view_ll = (LinearLayout) LayoutInflater.from(activity).inflate(
                        com.ovit.R.layout.app_ui_ai_aimap_action_zj, null);

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            command_change(v);
                            aiWindow.window.dismiss();
                        } catch (Exception es) {
                            Log.e(TAG, "绘制:注记 ", es);
                        }
                    }
                };
                view_ll.findViewById(com.ovit.R.id.ll_wzzj).setOnClickListener(listener);
                view_ll.findViewById(com.ovit.R.id.ll_dzj).setOnClickListener(listener);
                view_ll.findViewById(com.ovit.R.id.ll_xzj).setOnClickListener(listener);
                view_ll.findViewById(com.ovit.R.id.ll_mzj).setOnClickListener(listener);
//                view_ll.findViewById(R.id.ll_sj).setOnClickListener(listener);

                aiWindow.addView(view_ll);
                aiWindow.setDefOpt();
                aiWindow.setSizeAuto();
                aiWindow.window.showAsDropDown(view);
            }
        };
        aiWindow.Build();
    }

    public void command_change(View v) {
        Feature feature = null;
        if (v.getId() == com.ovit.R.id.ll_wzzj) {
            draw_dwOrbz("BZ_WZ","文字");
        } else if (v.getId() == com.ovit.R.id.ll_dzj) {
            draw_dwOrbz("BZ_D","标注点");
        } else if (v.getId() == com.ovit.R.id.ll_xzj) {
            draw_dwOrbz("BZ_X","标注线");
        } else if (v.getId() == com.ovit.R.id.ll_mzj) {
            draw_dwOrbz("BZ_M","标注面");
        } else if (v.getId() == com.ovit.R.id.ll_dzdw) {
            draw_dwOrbz("DZDW","点状地物");
        } else if (v.getId() == com.ovit.R.id.ll_xzdw) {
            draw_dwOrbz("XZDW","线状地物");
        } else if (v.getId() == com.ovit.R.id.ll_mzdw) {
            draw_dwOrbz("MZDW","面状地物");
        } else if (v.getId() == com.ovit.R.id.ll_wq) {
            draw_dwOrbz("XZDW","依比例围墙","围墙");
        }
        mapInstance.requireFeatureView(feature).command_change(null);
    }

    public void draw_dwOrbz(String layername,String mc) {
        draw_dwOrbz(layername,mc,mc);
    }
    public void draw_dwOrbz(String layername,String fhmc,String dwmc) {
        Feature feature =  MapHelper.getLayer(map, layername).getFeatureTable().createFeature();
        FeatureHelper.Set(feature,"FHMC", fhmc);
        FeatureHelper.Set(feature,"DWMC", dwmc);
        mapInstance.requireFeatureView(feature).command_change(null);
    }


    public void draw_zd(AiRunnable callback) {

        FeatureViewZD.CreateFeature(mapInstance, callback);

    }

//    public void draw_zrz(String zddm) {
//        FeatureEditZRZ.CreateFeature(mapInstance,zddm,null,null);
//
//    }
    public void draw_zrz( String orid, final AiRunnable callback) {
    FeatureViewZRZ.CreateFeature(mapInstance,orid,null,callback);
}
    public void draw_zrz( Feature featureZD, final AiRunnable callback) {
        FeatureViewZRZ.CreateFeature(mapInstance,featureZD,null,callback);

    }
    public void draw_ljz(String orid, final String cs, final AiRunnable callback) {
        FeatureViewLJZ.CreateFeature(mapInstance,orid,null,callback);
    }
    public void draw_ljz(Feature featureZRZ, final String cs, final AiRunnable callback) {
        FeatureViewLJZ.CreateFeature(mapInstance,featureZRZ,null,callback);
    }

//    public void draw_h(String ljzh, final String cs, final AiRunnable callback) {
//        FeatureEditH.CreateFeature(mapInstance,ljzh,cs,null,callback);
//
//    }

    public void draw_h(String orid , final String cs, final AiRunnable callback) {
        FeatureViewH.CreateFeature(mapInstance,orid,cs,null,callback);
    }
    public void draw_h(Feature featureLJZ, final String cs, final AiRunnable callback) {
        FeatureViewH.CreateFeature(mapInstance,featureLJZ,cs,null,callback);
    }
    public void draw_h_yt(final Feature featureH, final AiRunnable callback) {
        draw_h_fsjg(featureH,"阳台",callback);
    }
    public void draw_h_fsjg(Feature featureH,String type, final AiRunnable callback) {
        try {
            String lc = FeatureHelper.Get(featureH,"SZC","");
            FeatureLayer layer = MapHelper.getLayer(map, "H_FSJG", "户附属结构");
            Feature f = layer.getFeatureTable().createFeature();
            f.getAttributes().put("MC",type);
            f.getAttributes().put("TYPE", type);
            f.getAttributes().put("FHMC", type);
            FeatureEditH_FSJG.CreateFeature(mapInstance,featureH,f,lc,callback);

        } catch (Exception es) {
            ToastMessage.Send(activity, "绘制"+type+"失败", es);
        }
    }
    public void draw_h_fsjg(Feature featureLjz,String type,String lc, final AiRunnable callback) {
        try {
//            FeatureEditH_FSJG.CreateFeatureToLjz(mapInstance,featureLjz,f,lc,callback);
        } catch (Exception es) {
            ToastMessage.Send(activity, "绘制"+type+"失败", es);
        }
    }

    public void draw_z_fsjg( Feature featureLJZ,String type,String lc, AiRunnable callback) {
        try {
            FeatureLayer layer = mapInstance.getLayer( "Z_FSJG", "幢附属结构");
            Feature f = layer.getFeatureTable().createFeature();
            FeatureHelper.Set(f,"MC",type);
            FeatureHelper.Set(f,"TYPE",type);
            FeatureHelper.Set(f,"FHMC",type);
            FeatureHelper.Set(f,"LC", lc);
            FeatureViewZ_FSJG.CreateFeature(mapInstance,featureLJZ,f,lc,callback);
        } catch (Exception es) {
            ToastMessage.Send(activity, "绘制"+type+"失败", es);
        }
    }
    public void draw_bz_sj(final AiRunnable callback){
        try {
            MapHelper.Draw(mapInstance, GeometryType.POLYLINE, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    Polyline line = MapHelper.geometry_get((Polyline)t_,table.getSpatialReference());
                    if(line!=null) {
                        FeatureTable table = mapInstance.getTable("BZ_SJ");
                        List<Feature> fs = new ArrayList<>();
                        for (ImmutablePart part : line.getParts()) {
                            for (Segment s : part) {
                                Polyline p = new Polyline(new PointCollection(Arrays.asList(new Point[]{s.getStartPoint(), s.getEndPoint()})), table.getSpatialReference());
                                Feature f = mapInstance.getTable("BZ_SJ").createFeature();
                                f.setGeometry(p);
                                FeatureHelper.Set(f, "TITLE", AiUtil.GetValue(MapHelper.getLength(p, MapHelper.U_L),"",AiUtil.F_FLOAT2));
                                fs.add(f);
                            }
                        }
                        mapInstance.fillFeature(fs);
                        MapHelper.saveFeature(fs, callback);
                        return null;
                    }
                    AiRunnable.Ok(callback,null);
                    return null;
                }
            });

        } catch (Exception es) {
            ToastMessage.Send(activity, "绘制栓距失败", es);
        }
    }

    public void to_bdc(Feature featureZDorH){
        String name = FeatureHelper.Get(featureZDorH,"QLRXM","");
        mapInstance.tool.map_opt_showfeature_bdc(activity, "map_opt_showfeature_bdc", "[" + name + "]", feature, null);
    }

    // 地籍子区
    public  void load_djzq (Geometry geometry , final AiRunnable callback){
        final List<Feature> features = new ArrayList<>();
        MapHelper.Query(map, "DJZQ", geometry, features, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id =getDjzq();
                String name = getXmmc();
                if(features.size()>0){
                    String djzqdm = FeatureHelper.Get(features.get(0),"DJZQDM","");
                    // 如果查出的地籍子区的前六位与设定的不一致，则一查出来的为准
                    if (id.length()!=12||(djzqdm.length() == 12 && (!id.substring(0,6).equals(djzqdm.substring(0,6))))){
                        ToastMessage.Send("该项目地级子区编码有误请检查！！！");
                        id = djzqdm;
                        name = FeatureHelper.Get(features.get(0),"DJZQMC","");
                    }
                }
                AiRunnable.Ok(callback,id,name);
                return null;
            }
        });
    }
    // 图幅
    public  void load_tf (Geometry geometry ,final  AiRunnable callback){
        final List<Feature> features = new ArrayList<>();
        MapHelper.Query(map, "TF", geometry, features, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = "";
                String name = "";
                if(features.size()>0){
                    id =FeatureHelper.Get(features.get(0),"NEWMAPNO","");
                    name = FeatureHelper.Get(features.get(0),"MAPNAME","");
                }
                AiRunnable.Ok(callback,id,name);
                return null;
            }
        });
    }

    public String  getDjzq (){
        // 地籍只区填充到12位
        String xmbm =  mapInstance.aiMap.getProjectXmbm();
//        xmbm = StringUtil.substr(xmbm,0,12);
//        return StringUtil.fill(xmbm,"0",12);
        return StringUtil.substr(xmbm,0,12);
    }
    public void  getDjzq (AiRunnable callback){
         if(feature!=null && feature.getGeometry()!=null){
             load_djzq(feature.getGeometry(),callback);
         }else {
             AiRunnable.Ok(callback, getDjzq(), getXmbm());
         }
    }


    //数据结构 用于支撑loadJzds 20180705
    private class struc_loadJzds
    {
        private Point p_pre;
        private Point p;
        private Point p_next;
        private double distance_p_pre = 0;
        private double getDistance_p_next = 0;

        public struc_loadJzds(Point p_pre, Point p, Point p_next, double distance_p_pre, double getDistance_p_next)
        {
            this.p_pre = p_pre;
            this.p = p;
            this.p_next = p_next;
            this.distance_p_pre = distance_p_pre;
            this.getDistance_p_next = getDistance_p_next;
        }

        public Point getP_pre() {
            return p_pre;
        }

        public Point getP() {
            return p;
        }

        public Point getP_next() {
            return p_next;
        }

        public double getDistance_p_pre() {
            return distance_p_pre;
        }

        public double getDistance_p_next() {
            return getDistance_p_next;
        }

    }

    // 获取界址点
    public void loadJzds(final List<Feature> fs_jzd, final AiRunnable callback) {
        final List<Feature> features_jzd = new ArrayList<>();
        String zddm = getZddm();
        MapHelper.Query(mapInstance.getTable("JZD"), StringUtil.WhereByIsEmpty(zddm)+ "ZDZHDM like '%" + zddm + "%' ", -1, features_jzd, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Map<String,Feature> map_jzd = new HashMap<>();
                for (Feature f_jzd : features_jzd) {
                    String key = FeatureEditJZD.GetKey((Point)f_jzd.getGeometry());
                    map_jzd.put(key,f_jzd);
                }
                Geometry g = feature.getGeometry();
                if(g instanceof Multipart) {
                    for (ImmutablePart part : ((Multipart) g).getParts()) {
                        for (Point p : part.getPoints()) {
                            String key = FeatureEditJZD.GetKey(p);
                            if(map_jzd.containsKey(key)){
                                fs_jzd.add(map_jzd.get(key));
                            }
                        }
                    }

                    /*根据容差去掉容易产生注记压盖的界址点，同时处理弧线问题 20180705
                     * 算法思想：遍历fs_jzd链表 每次取出前中后相邻的三个界址点，计算前点和中点及中点和后点的距离，将两个距离和三个
                     * 点同时存储在list_struc<struc_loadJzds>中备用；依次计算fs_jzd表中相邻界址点的距离，令rc=最大距离*容差系数；
                     * 遍历list_struc，若前距离和后距值均<rc，从fs_jzd中删除对应的中点，若只有前距值<rc,从fs_jzd中删除对应的前点，
                     * 若只有后距值<rc,从fs_jzd中删除对应的后点，若两值均>rc,则不做操作；遍历完成 算法结束*/
                    int start_jzd_size = fs_jzd.size();  //开始处理前总界址点数
                    Log.i(TAG,"start fs_jzd.size:" + start_jzd_size);
                    ArrayList<struc_loadJzds> list_struc = new ArrayList<struc_loadJzds>();  //辅助表
                    double rc = 0; //容差
                    for (int i = 0;i < fs_jzd.size();i++)
                    {
                        Point p_pre = (Point) fs_jzd.get((i < 1 ? fs_jzd.size() : i) - 1).getGeometry();
                        Point p = (Point) fs_jzd.get(i).getGeometry();
                        Point p_next = (Point) fs_jzd.get((i < (fs_jzd.size() - 1) ? i : -1) + 1).getGeometry();
                        list_struc.add(new struc_loadJzds(p_pre,p,p_next,GeometryEngine.distanceBetween(p_pre,p),GeometryEngine.distanceBetween(p,p_next)));
                    }

                    for(int j = 0;j < list_struc.size();j++)
                    {
                        rc = rc < list_struc.get(j).getDistance_p_pre()?list_struc.get(j).getDistance_p_pre():rc;
                        rc = rc < list_struc.get(j).getDistance_p_next()?list_struc.get(j).getDistance_p_next():rc;
                    }

                    rc = rc * 0.05; //设置容差 默认系数0.05

                    for(int k = 0;k < list_struc.size();k++)
                    {
                        Log.i(TAG,"界址点处理及容差设置参考|前距值："+list_struc.get(k).getDistance_p_pre()+"|后距值："+list_struc.get(k).getDistance_p_next()+"|"+"容差："+rc);//参考
                        if(list_struc.get(k).getDistance_p_pre()<rc && list_struc.get(k).getDistance_p_next()<rc)
                        {
                            for(int t = 0;t < fs_jzd.size();t++)
                            {
                                if(fs_jzd.get(t).getGeometry().equals(list_struc.get(k).getP()))
                                {
                                    fs_jzd.remove(t);
                                }
                            }
                        }
                        else if(list_struc.get(k).getDistance_p_pre()<rc)
                        {
                            for(int t = 0;t < fs_jzd.size();t++)
                            {
                                if(fs_jzd.get(t).getGeometry().equals(list_struc.get(k).getP_pre()))
                                {
                                    fs_jzd.remove(t);
                                }
                            }
                        }
                        else if(list_struc.get(k).getDistance_p_next()<rc)
                        {
                            for(int t = 0;t < fs_jzd.size();t++)
                            {
                                if(fs_jzd.get(t).getGeometry().equals(list_struc.get(k).getP_next()))
                                {
                                    fs_jzd.remove(t);
                                }
                            }
                        }

                    }

                    if(start_jzd_size == fs_jzd.size())
                    {  //为提高效率 若剔除前后界址点数量一致 则不执行界址点编号调整操作
                        Log.i(TAG,"未执行界址点编号调整");
                    }
                    else{//对处理后的界址点编号进行调整 依据偏好设置选择简码或原码
                        String encodingMethod = "";
                        for(int i = 0;i < fs_jzd.size();i++)
                        {
                            if (fs_jzd.get(i).getAttributes().containsKey("JZDH"))
                            {
                                String jzd_y = fs_jzd.get(i).getAttributes().get("JZDH").toString();
                                Log.i(TAG,"原界址点号:"+jzd_y);
                                fs_jzd.get(i).getAttributes().remove("JZDH");

                                if(AiUtil.GetValue(AppConfig.get("APP_ZD_JZD_FSSYJM"),true))
                                {
                                    encodingMethod = "简码";
                                    fs_jzd.get(i).getAttributes().put("JZDH","J"+(i+1));
                                }
                                else {
                                    encodingMethod = "原码";
                                    if (jzd_y.length() > 2)
                                    {
                                        if(i<9)
                                        {
                                            fs_jzd.get(i).getAttributes().put("JZDH",jzd_y.substring(0,jzd_y.length()-2)+"0"+(i+1));
                                        }
                                        else{
                                            fs_jzd.get(i).getAttributes().put("JZDH",jzd_y.substring(0,jzd_y.length()-2)+(i+1));
                                        }
                                    }
                                }

                                Log.i(TAG,encodingMethod+"调整后界址点号:"+fs_jzd.get(i).getAttributes().get("JZDH").toString());
                            }
                        }
                    }

                    Log.i(TAG,"end fs_jzd.size:"+fs_jzd.size()); //
                    //------------------------------------end 20180705----------------------------------------------

                    AiRunnable.Ok(callback, fs_jzd);
                }
                return null;
            }
        });
    }
    public void loadJzxs(List<Feature> fs_jzx,AiRunnable callback){
        String zddm = getZddm();
        MapHelper.Query(mapInstance.getTable("JZX"), StringUtil.WhereByIsEmpty(zddm)+ "ZDZHDM like '%" + zddm + "%' ", -1, fs_jzx,callback);
    }
    // 获取界址线
    public void loadJzxs( final List<Feature> fs_jzx,final Map<String,Feature> map_jzx, final AiRunnable callback) {
        loadJzxs(fs_jzx, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                for (Feature f_jzx : fs_jzx) {
                    String key = FeatureEditJZX.GetKey(f_jzx.getGeometry());
                    if (StringUtil.IsNotEmpty(key) && !map_jzx.containsKey(key)) {
                        map_jzx.put(key, f_jzx);
                    }
                }
                AiRunnable.Ok(callback, fs_jzx);
                return null;
            }
        });
    }
    // 获取界址线
    public void loadJzqzs(final List<Feature> fs_jzd , final List<Map<String,Object>> fs_jzqz ) {
        String zddm = getZddm();
        if (fs_jzd.size() > 0) {
            Map<String, Object> map = null;
            String xlzdqlr = "";
            String jzxqdh = "";
            List<String> jsxzjh = new ArrayList<>();
            String jzxzdh = "";
            PointCollection ps = null;

            for (int i=0;i<fs_jzd.size();i++) {
                int j = i<(fs_jzd.size()-1)?(i+1):0;
                Feature f_jzd1 = fs_jzd.get(i);
                Feature f_jzd2 = fs_jzd.get(j);

                String key = FeatureViewZD.GetZdzhdm_LZ(zddm, f_jzd1,f_jzd2);
                String jzdh1 = FeatureHelper.Get(f_jzd1, "JZDH", "");
                String jzdh2 = FeatureHelper.Get(f_jzd2, "JZDH", "");

                if(AiUtil.GetValue(AppConfig.get("APP_ZD_JZD_FSSYJM"),true)){
                    jzdh1 = "J"+i;
                    jzdh2 = "J"+j;
                }

                if (map != null && !xlzdqlr.equalsIgnoreCase(key)) {
                    Polyline line = new Polyline(ps);
                    map.put("Geometry", line);
                    fs_jzqz.add(map);
                    map = null;
                }
                if (map == null) {
                    // 开始
                    xlzdqlr = key;
                    jzxqdh = jzdh1;
                    jsxzjh = new ArrayList<>();
                    jzxzdh = "";
                    map = new ArrayMap<>();
                    jsxzjh = new ArrayList<>();
                    map.put("ZDZHDM", zddm);
                    map.put("JZXQDH", jzxqdh);
                    map.put("XLZDQLR", xlzdqlr);
                    ps = new PointCollection(f_jzd1.getGeometry().getSpatialReference());
                    ps.add((Point) f_jzd1.getGeometry());
                }

//                if (!jzdh2.equalsIgnoreCase(jzxqdh)) {
                // 与下面的是同一个
                jsxzjh.add(jzxzdh);
                jzxzdh = jzdh2;
                map.put("JZXZJH", StringUtil.Join(jsxzjh, ",", false));
                map.put("JZXZDH", jzxzdh);
//                }
                ps.add((Point) f_jzd2.getGeometry());
            }
            Polyline line = new Polyline(ps,ps.getSpatialReference());
            map.put("Geometry", line);
            fs_jzqz.add(map);
        }
    }
    public void loadZrzs(List<Feature> fs_zrz,AiRunnable callback){
        queryChildFeature("ZRZ",getOrid(),"ZRZH","",fs_zrz,callback);
    }
    public void loadCs(List<Feature> fs_c,AiRunnable callback){
        queryChildFeature("ZRZ_C",getOrid(),"CH","",fs_c,callback);
    }
    public void loadLjz(List<Feature> fs_ljz,AiRunnable callback){
        queryChildFeature("LJZ",getOrid(),"LJZH","",fs_ljz,callback);
    }
    public void loadFtqk(List<Feature> fs_ftqk,AiRunnable callback){
        queryChildFeature("FTQK",getOrid(),"","",fs_ftqk,callback);
    }
    public void loadBdcdy(List<Feature> fs_bdcdy,AiRunnable callback){
        queryChildFeature("QLRXX",getOrid(),"ID","",fs_bdcdy,callback);
    }
    public void loadHs(List<Feature> fs_h,AiRunnable callback){
        queryChildFeature("H",getOrid(),"HH","",fs_h,callback);
    }

    public void loadZ_FSJGs(List<Feature> fs_z_fsjg,AiRunnable callback){
        queryChildFeature("Z_FSJG",getOrid(),"ID","",fs_z_fsjg,callback);
    }
    public void loadH_FSJGs(List<Feature> fs_h_fsjg,AiRunnable callback){
        queryChildFeature("H_FSJG",getOrid(),"ID","",fs_h_fsjg,callback);
    }

    public static void LoadAllZD(final MapInstance mapInstance,  final List<Feature> fs_zd,AiRunnable callback){
        MapHelper.Query(mapInstance.getTable("ZD"), "","ZDDM","", -1, fs_zd, callback);
    }

    // 适合 逻辑幢、自然幢 调用
    public static  void LoadH_And_Fsjg(final MapInstance mapInstance,final Feature f,
                                       final List<Feature>fs_z_fsjg,
                                       final List<Feature>fs_h,
                                       final List<Feature>fs_h_fsjg,
                                       final AiRunnable callback) {
        final FeatureView fv = mapInstance.newFeatureView(f);
        LoadH_And_Z_Fsjg(mapInstance, f, fs_z_fsjg, fs_h, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                 fv.loadH_FSJGs(  fs_h_fsjg, callback);
                return null;
            }
        });
    }


    // 适合 逻辑幢、自然幢 调用
    public static  void LoadH_And_Fsjg(final MapInstance mapInstance,final Feature f,
                                       final String lc ,
                                       final List<Feature>fs_z_fsjg,
                                       final List<Feature>fs_h,
                                       final List<Feature>fs_h_fsjg,
                                       final AiRunnable callback) {
        final FeatureView fv = mapInstance.newFeatureView(f);
        final String orid = fv.getOrid();
        fv.queryChildFeature("H", orid, " and SZC = '"+lc+"' ","HH", "", fs_h, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                fv.queryChildFeature("Z_FSJG",orid,"  and LC='"+lc+"' ","ID","",fs_z_fsjg,new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        fv.queryChildFeature("H_FSJG",orid,"  and LC='"+lc+"'  ","ID","",fs_h_fsjg,callback);
                        return  null;
                    }
                });
                return  null;
            }
        });
    }


    // 适合 逻辑幢、自然幢 调用
    public static  void LoadH_And_Z_Fsjg(final MapInstance mapInstance,final Feature f,
                                         final List<Feature> fs_z_fsjg,
                                         final List<Feature>fs_h,
                                         final AiRunnable callback) {
        final FeatureView fv = mapInstance.newFeatureView(f);
        fv.loadHs(fs_h, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                fv.loadZ_FSJGs(fs_z_fsjg,callback);
                return null;
            }
        });
    }


    public static void LoadJzdx_AndZhfs( final MapInstance mapInstance,
                                final Feature f_zd,
                                final List<Feature> fs_jzd,
                                final List<Feature> fs_jzx,
                                         final List<Feature> fs_zrz,
                                         final List<Feature> fs_z_fsjg,
                                         final List<Feature> fs_h,
                                         final List<Feature> fs_h_fsjg,
                                final AiRunnable callback) {
        LoadJzdx(mapInstance,f_zd,fs_jzd,fs_jzx, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                LoadZ_H_And_Fsjg(mapInstance,f_zd,fs_zrz,fs_z_fsjg,fs_h,fs_h_fsjg,callback);
                return null;
            }
        });
    }

    public static void LoadZ_H_And_Fsjg(final MapInstance mapInstance, final Feature feature,
                               final List<Feature> fs_zrz,
                               final List<Feature> fs_z_fsjg,
                               final List<Feature> fs_h,
                               final List<Feature> fs_h_fsjg,
                               final AiRunnable callback) {
        final FeatureView fv = mapInstance.newFeatureView(feature);
        fv.loadZrzs(fs_zrz, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                fv.loadHs(fs_h, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        fv.loadZ_FSJGs(fs_z_fsjg, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                fv.loadH_FSJGs(fs_h_fsjg, callback);
                                return null;
                            }
                        });
                        return null;
                    }
                });
                return null;
            }
        });
    }
    public static void LoadJzdx( final MapInstance mapInstance,
                                final Feature feature,
                                final List<Feature> f_jzds,
                                final List<Feature> f_jzxs,
                                final AiRunnable callback) {
        final FeatureView fv = mapInstance.newFeatureView(feature);
        fv.loadJzds(f_jzds, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                fv.loadJzxs(f_jzxs, callback);
                return null;
            }
        });
    }
    // 获取界址点、线、签字
    public static void LoadJzdxqz(final MapInstance mapInstance, final Feature feature, final List<Feature> fs_jzd,final List<Feature> fs_jzx,
                                  final Map<String,Feature> map_jzx,final List<Map<String,Object>> fs_jzqz, final AiRunnable callback) {
        final FeatureView fv = mapInstance.newFeatureView(feature);
        fv.loadJzds(  fs_jzd, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                fv.loadJzxs( fs_jzx, map_jzx, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        fv.loadJzqzs( fs_jzd, fs_jzqz);
                        AiRunnable.Ok(callback, t_, objects);
                        return null;
                    }
                });
                return null;
            }
        });
    }
    // 获取分摊情况，谁分摊给我，我又分摊给谁
    public static  void LoadFTQK(final  MapInstance mapInstance, final Feature feature,final  List<Feature> fs_from,final  List<Feature> fs_to,final AiRunnable callback){
        MapHelper.Query(mapInstance.getTable("GNQ"), "",true,fs_from, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                MapHelper.Query(mapInstance.getTable("GNQ"), "",true,fs_from,callback);
                return null;
            }
        }) ;

    }

    //通用方法 填充可选择的不动产列表 20180730
    public static void initSelectList(final MapInstance mapInstance, final BaseAdapterHelper helper, final Feature item, final int deep, final List<Feature> selected_feature_list)
    {
        helper.getView().findViewById(com.ovit.R.id.iv_detial).setVisibility(View.GONE);
        helper.getView().findViewById(com.ovit.R.id.cb_select).setVisibility(View.VISIBLE);

        final String name = mapInstance.getName(item);
        final String desc = mapInstance.getDesc(item);

        helper.setText(com.ovit.app.map.R.id.tv_groupname,mapInstance.getLayerName(item));
        helper.setText(com.ovit.R.id.tv_name, name);
        helper.setText(com.ovit.R.id.tv_desc, desc);
        helper.setVisible(com.ovit.R.id.tv_desc, StringUtil.IsNotEmpty(desc));

        int s = (int) (deep * mapInstance.activity.getResources().getDimension(com.ovit.R.dimen.app_size_smaller));
        helper.getView(com.ovit.R.id.v_split).getLayoutParams().width = s;

        com.ovit.app.map.view.FeatureView fv = mapInstance.newFeatureView(item.getFeatureTable());

        if (item.getGeometry() != null){
            fv.setIcon(fv.fs_ref,item, helper.getView(com.ovit.R.id.v_icon));
        }

        // 定位
        helper.setVisible(com.ovit.R.id.iv_position, item.getGeometry() != null);
        helper.getView(com.ovit.R.id.iv_position).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapHelper.selectAddCenterFeature(mapInstance.map, item);
            }
        });

        //选中、反选
        final CheckBox cb_select = helper.getView(com.ovit.R.id.cb_select);
        cb_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cb_select.isChecked())
                {
                    if(selected_feature_list.contains(item))
                    {

                    }else{
                        selected_feature_list.add(item);
                    }
                }else{
                    if(selected_feature_list.contains(item))
                    {
                        selected_feature_list.remove(item);
                    }else{

                    }
                }
            }
        });

    }
    // 通用方法 获得根据用户搜索关键词得到的可选择不动产列表 可定制搜索范围和附加搜索条件 (更底层的getSearchWhere()方法需完善) 20180731
    public static View getSearchSelectBDC(final MapInstance mapInstance,final List<Feature> selected_feature_list,final String key,final List<FeatureTable> search_table_list,String limit_where)
    {
        final int listItemRes = com.ovit.app.map.R.layout.app_ui_ai_aimap_feature_item;
        final ViewGroup ll_view = (ViewGroup) LayoutInflater.from(mapInstance.activity).inflate(listItemRes, null);
        final ViewGroup ll_list_item = (ViewGroup) ll_view.findViewById(com.ovit.R.id.ll_list_item);
        if(ll_view.getChildCount()>0)
        {
            ll_view.getChildAt(0).setVisibility(View.GONE); //隐藏第一个默认view以优化UI（后期如有需要也可重写该view为面板增添其他功能）
        }

        final List<Feature> fs = new ArrayList<Feature>();

        final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity,listItemRes,fs) {
            @Override
            protected void convert(final BaseAdapterHelper helper,final Feature item) {
                initSelectList(mapInstance,helper,item,0,selected_feature_list);
            }
        };

        ll_list_item.setTag(adapter);
        adapter.adpter(ll_list_item);

        for(FeatureTable table:search_table_list)
        {
            String where = com.ovit.app.map.view.FeatureView.From(mapInstance,table).getSearchWhere(key);
            if (StringUtil.IsNotEmpty(where))
            {
                where = "( " + where + limit_where+" )";
            }
            LayerConfig config = LayerConfig.get(table);
            MapHelper.Query(table,where,config.getCol_orderby(),config.col_sort,0,true,fs,new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    adapter.notifyDataSetChanged();
                    return null;
                }
            });
        }

        return ll_view;
    }
    public void  fsjg_init (AiRunnable callback){}
    public void hsmj(Feature f, MapInstance mapInstance) {
    }


}
