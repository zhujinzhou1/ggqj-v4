package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.ListUtil;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lichun on 2018/1/24.
 */

public class FeatureViewH extends FeatureView {
    @Override
    public void onCreate() {
        super.onCreate();
        iconColor = Color.rgb(0, 197, 255);
    }
    @Override
    public void fillFeature(Feature feature, Feature feature_ljz){
        super.fillFeature(feature,feature_ljz);
        String id = FeatureHelper.Get(feature, "ID", "");
        String zrzh = FeatureHelper.Get(feature,"ZRZH","");
        if(feature_ljz!=null) {
            FeatureHelper.Set(feature,"LJZH", FeatureHelper.Get(feature_ljz,"LJZH",""));
            zrzh = FeatureHelper.Get(feature_ljz, "ZRZH", "");
        }
        FeatureHelper.Set(feature, "ZRZH", zrzh);
        String zddm = StringUtil.substr_last(zrzh, 0, FeatureHelper.FEATURE_ZD_ZDDM_LENG);
        FeatureHelper.Set(feature,"ZDDM", zddm);

        String bdcdyh = FeatureHelper.Get(feature_ljz, "BDCDYH", FeatureHelper.Get(feature, "BDCDYH","" ));
        boolean isF99990001 = bdcdyh.endsWith("F99990001");
        bdcdyh = isF99990001 ? (zddm + "F99990001") :(zrzh+  StringUtil.substr_last(id, 4));
        FeatureHelper.Set(feature,"BDCDYH", bdcdyh);
        FeatureHelper.Set(feature,"FWBM", StringUtil.substr_last(bdcdyh, 9));

        int lc = FeatureHelper.Get(feature,"SZC", 1);
        FeatureHelper.Set(feature,"SZC", lc);
        FeatureHelper.Set(feature,"SJCS", lc);

        int hh =  FeatureHelper.Get(feature,"HH", 1);
        // 户号字段表示一个层当中的户顺序号
        FeatureHelper.Set(feature,"HH", hh);

        String mph = FeatureHelper.Get(feature_ljz, "MPH", "");
        mph += mph.length()>0?"-":"";
        mph += lc;
        mph += String.format("%02d", hh);

        FeatureHelper.Set(feature,"MPH", mph,true,false);
        FeatureHelper.Set(feature,"SHBW", mph,true,false);

        double area =    MapHelper.getArea(mapInstance,feature.getGeometry());
        if (0d == FeatureHelper.Get(feature,"SCJZMJ", 0d)) {
            FeatureHelper.Set(feature,"SCJZMJ", area);
        }
        FeatureHelper.Set(feature,"YCJZMJ", area);
        FeatureHelper.Set(feature,"FWJG",FeatureHelper.Get(feature_ljz,"FWJG","4"),true,false);//4[B][混]混合结构

        FeatureHelper.Set(feature,"CQLY", "自建",true, false);// 自建
        FeatureHelper.Set(feature,"FWLX", "1",true,false); //[1]住宅
        FeatureHelper.Set(feature,"FWXZ", "99",true,false); // [99]其它
        FeatureHelper.Set(feature,"ZT", "自用",true,false); // // 自用
        FeatureHelper.Set(feature,"YT", "10",true,false); // 住宅

        FeatureHelper.Set(feature,"CB", "6",true,false); //  [6]私有房产
        FeatureHelper.Set(feature,"QTGSD", "自墙",true,false); //自有墙
        FeatureHelper.Set(feature,"QTGSN", "自墙",true,false); //自有墙
        FeatureHelper.Set(feature,"QTGSX", "自墙",true,false); //自有墙
        FeatureHelper.Set(feature,"QTGSB", "自墙",true,false); //自有墙
    }

    @Override
    public String addActionBus(String groupname) {
        int count = mapInstance.getSelFeatureCount();
        // 根据画宗地推荐

        mapInstance.addAction(groupname, "画户", R.mipmap.app_map_layer_h, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String zrzh = FeatureHelper.Get(feature, "ZRZH", "");
                draw_h(mapInstance.getOrid_Parent(feature), "1", null);
            }
        });

        if (count > 0) {
            mapInstance.addAction(groupname, "画飘窗", R.mipmap.app_map_layer_h_pc, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    draw_h_fsjg(feature, "飘窗", null);
                }
            });
            mapInstance.addAction(groupname, "画阳台", R.mipmap.app_map_layer_h_yt, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    draw_h_fsjg(feature, "阳台",null);
                }
            });
        }

        addActionTY(groupname);
        addActionPZ(groupname);
        addActionSJ(groupname);

        groupname = "操作";

        if (feature != null && feature.getFeatureTable() == table) {
            mapInstance.addAction(groupname, "定位", R.mipmap.app_icon_opt_location, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    command_postion();
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
                            command_merge(null);
                        }
                    });
                }
                mapInstance.addAction(groupname, "修边", R.mipmap.app_icon_xiubian, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        command_xiubian(null);
                    }
                });
                mapInstance.addAction(groupname, "挖空", R.mipmap.app_icon_hollow, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        command_hollow();
                    }
                });

                mapInstance.addAction(groupname, "删除", R.mipmap.ic_action_clear, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        command_del(null);
                    }
                });
            }
        }
        return groupname;
    }
    public static FeatureViewH From(MapInstance mapInstance, Feature f){
        FeatureViewH fv =From(mapInstance);
        fv.set(f);
        return  fv;
    }
    public static FeatureViewH From(MapInstance mapInstance){
        FeatureViewH fv = new FeatureViewH();
        fv.set(mapInstance).set(mapInstance.getTable("H"));
        return  fv;
    }
    // 列表项，点击加载户附属
    @Override
    public void listAdapterConvert(BaseAdapterHelper helper, final Feature item, final int deep) {
        super.listAdapterConvert(helper,item,deep);
        final ViewGroup ll_list_item = helper.getView(R.id.ll_list_item);
        ll_list_item.setVisibility(View.GONE);
        helper.getView(R.id.ll_head).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapHelper.centerPoint(mapInstance.map,item.getGeometry());
                MapHelper.selectFeature(map,item);
                boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                if (!flag) {
                    final List<Feature> fs = new ArrayList<>();
                    queryChildFeature("H_FSJG", item, fs, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                                    List<Feature> fs_p = new ArrayList<>();
                                    fs_p.add(item);
                                    mapInstance.newFeatureView("H_FSJG").buildListView(ll_list_item,fs,deep+1);
                                    return  null;
                        }
                    });
                }
                ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
            }
        });
    }

    //  获取最大的编号
    public  void getMaxHid(AiRunnable callback) {
        String id  = getZrzh();
        MapHelper.QueryMax(table, StringUtil.WhereByIsEmpty(id)+"ID like '" + id + "____'", "ID", id.length(), 0,id+"0000",callback );
    }
    public  void newHid( final AiRunnable callback) {
        getMaxHid( new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = "";
                if (objects.length > 1) {
                    String maxid = objects[0] + "";
                    // 最大号加1
                    int count = AiUtil.GetValue(objects[1], 0) + 1;

                }
                AiRunnable.Ok(callback, id);
                return null;
            }
        });
    }
    public void loadByHid(String hid,AiRunnable callback){
        MapHelper.QueryOne(table,StringUtil.WhereByIsEmpty(hid)+" ID like '%'" + hid + "%' ",callback);
    }
    public static FeatureTable GetTable(MapInstance mapInstance) {
        return MapHelper.getLayer(mapInstance.map, "H", "户").getFeatureTable();
    }
    public static  void LoadAll(final MapInstance mapInstance,Feature f,final List<Feature>fs,AiRunnable callback){
        LoadAll(mapInstance,mapInstance.getOrid(f),fs,callback);
    }
    public static  void LoadAll(final MapInstance mapInstance,String orid,final List<Feature>fs,AiRunnable callback){
        mapInstance.newFeatureView().queryChildFeature("H",orid,"HH","asc",fs,callback);
    }
    public static void Load(MapInstance mapInstance, String orid, final AiRunnable callback){
        mapInstance.newFeatureView().findFeature("H",orid,callback);
    }

    public static String GetID(Feature feature) {
        return AiUtil.GetValue(feature.getAttributes().get("ID"), "");
    }

    //  获取最大的编号 户的id 是根据ZRZH 来遍的，要注意
    public static void GetMaxID(MapInstance mapInstance, String zrzh, AiRunnable callback) {
        MapHelper.QueryMax(GetTable(mapInstance), StringUtil.WhereByIsEmpty(zrzh)+"ID like '" + zrzh + "____'", "ID", zrzh.length(), 0,zrzh+"0000",callback );
    }
    //户的id 是根据ZRZH 来遍的，要注意
    public static void NewID(MapInstance mapInstance, final String zrzh, final AiRunnable callback) {
        GetMaxID(mapInstance,zrzh, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                String id = "";
                if (objects.length > 1) {
                    // 最大号加1
                    int count = AiUtil.GetValue(objects[1], 0) + 1;
                    id = zrzh+ String.format("%04d", count);
                }
                AiRunnable.Ok(callback, id);
                return null;
            }
        });
    }

    public static void InitFeatureAll(final MapInstance mapInstance,   final Feature featureLJZ, final AiRunnable callback)  {
        if (featureLJZ != null) {
            // 户的id 是根据ZRZH 来遍的，要注意
            final String zrzh =FeatureHelper.Get(featureLJZ,"ZRZH", "");
            GetMaxID(mapInstance, zrzh, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    int count = 1;
                    if (objects.length > 1) {
                        count = AiUtil.GetValue(objects[1], 0) + 1;
                    }
                    int zcs =FeatureHelper.Get(featureLJZ,"ZCS", 1);
                    int szc =FeatureHelper.Get(featureLJZ,"SZC", 1);
                    final List<Feature> features = new ArrayList<Feature>();
                    for (int c = szc; c < szc+ zcs; c++) {
                        String  id = zrzh+ String.format("%04d", count);
                        Feature f= GetTable(mapInstance).createFeature();
                        FeatureHelper.Set(f,"ID",id);
                        FeatureHelper.Set(f,"SZC",c);
                        f.setGeometry(MapHelper.geometry_copy(featureLJZ.getGeometry()));
                        mapInstance.fillFeature(f,featureLJZ);
                        features.add(f);
                        count++;
                    }
                    MapHelper.saveFeature(features, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            AiRunnable.Ok(callback,features);
                            return null;
                        }
                    });
                    return null;
                }
            });
        } else {
            ToastMessage.Send( "没有幢信息！");
            AiRunnable.No(callback,null);
        }
    }

    public static void ClearFeatureAll(final MapInstance mapInstance,   final Feature featureLJZ, final AiRunnable callback)  {
        if (featureLJZ != null) {
            mapInstance.newFeatureView().delChildFeature("H",featureLJZ,callback);

        } else {
            ToastMessage.Send( "没有逻辑幢信息！");
            AiRunnable.No(callback,null);
        }
    }
    // 带有诸多属性画户
    public static void CreateFeature(final MapInstance mapInstance, final Feature feature_ljz,  final String cs, final AiRunnable callback) {
        CreateFeature(mapInstance,feature_ljz,cs,null,callback);
    }
    // 带有诸多属性画户
    public static void CreateFeature(final MapInstance mapInstance,  final String orid,  final String cs, final Feature feature_h , final AiRunnable callback) {
        FeatureViewLJZ.From(mapInstance).load(orid, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Feature featureLJZ = (Feature) t_;
                CreateFeature(mapInstance,featureLJZ,cs,feature_h,callback);
                return null;
            }
        });
    }
    public static void CreateFeature(final MapInstance mapInstance,String orid,final Feature feature, final AiRunnable callback) {
        // 去查宗地
        FeatureViewH.From(mapInstance).load(orid, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                Feature f_p = (Feature)t_;
                CreateFeature(mapInstance,f_p,feature,callback);
                return  null;
            }
        });
    }
    public static void CreateFeature(final MapInstance mapInstance, final Feature f_p, Feature f, final AiRunnable callback) {

        if(f_p!=null&& f_p.getFeatureTable() != mapInstance.getTable("LJZ")) {
            // 如果不是逻辑
            String orid = mapInstance.getOrid_Match(f,"LJZ");
            if(StringUtil.IsNotEmpty(orid)){
                CreateFeature(mapInstance,orid,f,callback);
                return;
            }
        }
        final FeatureViewH fv = From(mapInstance,f);
        final Feature feature ;
        if(f==null){
            feature =fv.table.createFeature();
        }else{
            feature = f;
        }
        if (f_p == null ) {
            ToastMessage.Send("注意：缺少逻辑幢信息");
        }
        if (feature.getGeometry() == null && f_p != null) {
            feature.setGeometry(MapHelper.geometry_copy(f_p.getGeometry()));
        }
        final List<Feature> fs_update = new ArrayList<>();
        // 绘图
        fv.draw(new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 设置新的zddm
                fv.newHid( new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String id = t_ + "";
                        FeatureHelper.Set(feature,"LJZH",id);
                        // 填充
                        fv.fillFeature(feature,f_p);
                        fs_update.add(feature);
                        // 保存
                        MapHelper.saveFeature(fs_update, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                // 返回显示
                                AiRunnable.Ok(callback, feature);
                                mapInstance.viewFeature(feature);
                                return null;
                            }
                        });
                        return  null;
                    }
                });
                return null;
            }
        });
    }
    // 逻辑幢带有诸多属性画户
    public static void CreateFeature(final MapInstance mapInstance, final Feature featureLJZ, final String cs, final Feature feature_h ,final AiRunnable callback) {
        final Feature feature ;
        if(feature_h==null) {
            feature =  GetTable(mapInstance).createFeature();
        }else{
            feature = feature_h;
        }
        if(featureLJZ!=null&& featureLJZ.getFeatureTable() != mapInstance.getTable("LJZ")) {
            String orid = mapInstance.getOrid_Match(feature,"LJZ");
            if(StringUtil.IsNotEmpty(orid)){
                CreateFeature(mapInstance,orid,cs,feature,callback);
                return;
            }
        }
        if (featureLJZ != null && StringUtil.IsNotEmpty(cs)) {
            ToastMessage.Send("注意：逻辑幢等关联信息");
        }
        if (feature.getGeometry() == null && featureLJZ != null) {
            feature.setGeometry(MapHelper.geometry_copy(featureLJZ.getGeometry()));
        }
        final String cs_ = AiUtil.GetValue(cs,"1");
        mapInstance.command_draw(feature, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 户的id 是根据ZRZH 来遍的，要注意
                final String zrzh = FeatureHelper.Get(featureLJZ, "ZRZH", "");
                final String ljzh = FeatureHelper.Get(featureLJZ, "LJZH", "");
                NewID(mapInstance, zrzh, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        String id = t_ + "";
                        FeatureHelper.Set(feature,"ID",id);
                        FeatureHelper.Set(feature,"SZC",cs_);
                        mapInstance.fillFeature(feature,featureLJZ);
                        mapInstance.newFeatureView(feature).fillFeatureAddSave(feature, new AiRunnable(callback) {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                AiRunnable.Ok(callback,feature);
                                mapInstance.viewFeature(feature);
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
    // 更新户分摊面积
    public void update_Area(Feature feature, List<Feature> fs_ftqk) {
        String orid = FeatureHelper.Get(feature,"ORID","");
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        double f_ftjzmj=0d;
        if (g != null) {
            area = MapHelper.getArea(mapInstance, g);
            for (Feature f : fs_ftqk) {
                String ftqx = FeatureHelper.Get(f,"FTQX_ID","");
                if (orid.equals(ftqx)) {
                    f_ftjzmj += FeatureHelper.Get(f,"FTJZMJ", 0d);
                }
            }
            FeatureHelper.Set(feature,"SCFTJZMJ", AiUtil.Scale(f_ftjzmj, 2));
        }
    }
    // 核算宗地 占地面积、建筑面积
    public  void update_Area(final AiRunnable callback) {
        final List<Feature> fs_h_fsjg = new ArrayList<>();
        final List<Feature> fs = new ArrayList<>();
        final List<Feature> update_fs = new ArrayList<>();
       String where=StringUtil.WhereByIsEmpty(getOrid())+ " FTQX_ID like '%" + getOrid() + "%' ";
        queryFeature(mapInstance.getTable(FeatureConstants.FTQK_TABLE_NAME),where,fs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                update_Area(feature,fs);
                AiRunnable.Ok(callback,t_,objects);
                return  null;
            }
        });
    }

    public void identyH_FSJG(List<Feature> fs_h_fsjg,final AiRunnable callback){
        String szc=FeatureHelper.Get(feature,"SZC","1");
        String where="LC='0' or LC='"+szc+"'";
        MapHelper.Query(mapInstance.getTable("H_FSJG"),feature.getGeometry(),0.1,where, fs_h_fsjg,callback);
    }
    // 默认识别保存
    public void identyH_FSJG(final AiRunnable callback){
        identyH_FSJG(feature, true,callback);
    }
    // 识别显示结果返回
    public void identyH_FSJG(Feature f_h, final boolean isShow, final AiRunnable callback) {
        final List<Feature> fs_h_fsjg = new ArrayList<>();
        identyH_FSJG(fs_h_fsjg, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                FeatureViewH fv_ =  FeatureViewH.From(mapInstance);
                for (Feature f : fs_h_fsjg) {
                    Geometry bufferHfsjg = GeometryEngine.buffer(f.getGeometry(), 0.02);
                    Geometry g = GeometryEngine.intersection(feature.getGeometry(), bufferHfsjg);
                    if (MapHelper.getArea(mapInstance,g)<1*0.02){
                        continue;
                    }
                    fv.fillFeature(f, feature);
                }

                if(isShow) {
                    fv_.fs_ref = ListUtil.asList(feature);
                    QuickAdapter<Feature> adapter = fv_.getListAdapter(fs_h_fsjg, 0);
                    AiDialog dialog = AiDialog.get(mapInstance.activity, adapter);
                    dialog.setHeaderView(R.mipmap.app_map_layer_zrz, "识别到" + fs_h_fsjg.size() + "个户附属");
                    dialog.setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapHelper.saveFeature(fs_h_fsjg, callback);
                            dialog.dismiss();
                        }
                    });
                }else {
                    MapHelper.saveFeature(fs_h_fsjg, callback);
                }
                return null;
            }
        });
    }

    public void identyZ_Fsjg(List<Feature> features_zrz ,final AiRunnable callback){
        MapHelper.Query(mapInstance.getTable("Z_FSJG"),feature.getGeometry(), features_zrz,callback);
    }
    // 默认识别保存
    public void identyZ_Fsjg(final AiRunnable callback){
        identyZ_Fsjg(false,callback);
    }
    // 识别显示结果返回
    public void identyZ_Fsjg(final boolean isShow ,final AiRunnable callback) {
        final List<Feature> fs_z_fsjg = new ArrayList<>();
        identyZ_Fsjg(fs_z_fsjg, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                FeatureViewZ_FSJG fv_=  FeatureViewZ_FSJG.From(mapInstance);
                fv_.fillFeature(fs_z_fsjg, feature);
                if(isShow) {
                    fv_.fs_ref = ListUtil.asList(feature);
                    QuickAdapter<Feature> adapter = fv_.getListAdapter( fs_z_fsjg, 0);
                    AiDialog dialog = AiDialog.get(mapInstance.activity, adapter);
                    dialog.setHeaderView(R.mipmap.app_map_layer_zrz, "识别到" + fs_z_fsjg.size() + "个附属");
                    dialog.setFooterView("取消", "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapHelper.saveFeature(fs_z_fsjg, callback);
                            dialog.dismiss();
                        }
                    });
                }else {
                    MapHelper.saveFeature(fs_z_fsjg, callback);
                }
                return null;
            }
        });
    }

    public void identyFtqk( final AiRunnable callback) {

    }
    public void create_h_bdcfy(Feature f_h, final AiRunnable callback) {
        if (TextUtils.isEmpty(FeatureHelper.Get(f_h,"ZRZH",""))||TextUtils.isEmpty(FeatureHelper.Get(f_h,"LJZH",""))){
            ToastMessage.Send("缺少幢信息，请检查！");
            return;
        }
        final Feature feature_new_qlr = mapInstance.getTable("QLRXX").createFeature();
        mapInstance.featureView.fillFeature(feature_new_qlr,f_h);
        feature_new_qlr.getAttributes().put("BDCDYH",f_h.getAttributes().get("ID"));
        MapHelper.saveFeature(feature_new_qlr, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                AiRunnable.Ok(callback,feature_new_qlr,t_);
                return null;
            }
        });
    }

    public static void hsmj(Feature feature,MapInstance mapInstance, List<Feature> f_h_fsjgs) {
        String id = AiUtil.GetValue(feature.getAttributes().get("ORID"));
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        if (g != null) {
            area = MapHelper.getArea(mapInstance,g);
            if (area > 0) {
                hsmj = area;
            }
        }
        for (Feature f : f_h_fsjgs) {
            String hid =FeatureHelper.Get(f,"ORID_PATH","");
            if (hid.contains(id)) {
                double f_hsmj = AiUtil.GetValue(f.getAttributes().get("HSMJ"), 0d);
                hsmj += f_hsmj;
            }
        }
        feature.getAttributes().put("YCJZMJ", AiUtil.Scale(area, 2));
        feature.getAttributes().put("SCJZMJ", AiUtil.Scale(hsmj, 2));
    }
}
