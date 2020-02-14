package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Geometry;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.core.License;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.util.Excel;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.ui.ai.component.custom.CustomImagesView;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.DialogBuilder;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.GsonUtil;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created by Lichun on 2017/4/5.
 */

public class FeatureEditH extends FeatureEdit {
    final static String TAG = "FeatureEditH";
    FeatureViewH fv;
    private String old_qlrxm;
    private String old_qlrzjh;

    public FeatureEditH() {
        super();
    }

    public FeatureEditH(MapInstance mapInstance, Feature feature) {
        super(mapInstance, feature);
    }

    //region  重写父类方法
    @Override
    public void onCreate() {
        super.onCreate();
        // 使用 fv
        if (super.fv instanceof FeatureViewH) {
            this.fv = (FeatureViewH) super.fv;
        }
    }

    // 初始化
    @Override
    public void init() {
        super.init();
        // 必填字段
//        requiredFileds.addAll(Arrays.asList(new String[]{"BDCDYH", "ZDDM", "ZRZH", "SZC", "HH", "QLRXM"}));
        // 菜单
        menus = new int[]{R.id.ll_info, R.id.ll_zdinfo, R.id.ll_fsjginfo, R.id.ll_ct, R.id.ll_ftqk, R.id.ll_bdcdy};
    }

    // 显示数据
    @Override
    public void build() {
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.app_ui_ai_aimap_feature_h, v_content);
        try {
            if (feature != null) {
                mapInstance.fillFeature(feature);
                old_qlrxm = FeatureHelper.Get(feature, "QLRXM", "");
                old_qlrzjh = FeatureHelper.Get(feature, "QLRZJH", "");
                fillView(v_feature);

                CustomImagesView civ_qlrzjh = (CustomImagesView) v_feature.findViewById(R.id.civ_qlrzjh);
                final String filename = AiUtil.GetValue(civ_qlrzjh.getContentDescription(), "材料");
                // String hh = AiUtil.GetValue((String) feature.getAttributes().get("HH"), "0000");
                civ_qlrzjh.setName(filename).setDir(FileUtils.getAppDirAndMK(getpath_root() + "附件材料/" + filename + "/")).setOnRecognize_SFZ(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Map<String, String> datas = (Map<String, String>) t_;
                        String xm = datas.get("xm");
                        String sfzh = datas.get("sfzh");
                        String zz = datas.get("zz");
                        if (StringUtil.IsNotEmpty(xm)) {
                            ((EditText) v_feature.findViewById(R.id.et_qlrxm)).setText(xm);
                        }
                        if (StringUtil.IsNotEmpty(sfzh)) {
                            ((EditText) v_feature.findViewById(R.id.et_qlrzjh)).setText(sfzh);
                        }
                        if (StringUtil.IsNotEmpty(zz)) {
                            ((EditText) v_feature.findViewById(R.id.et_qlrtxdz)).setText(zz);
                        }
                        return null;
                    }
                });

                TextView tv_sbhfsjg = (TextView) v_feature.findViewById(R.id.tv_sbhfsjg);
                tv_sbhfsjg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fv.identyH_FSJG(new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                IdentyH_Area(mapInstance, feature, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        fillView(v_content, feature, "YCJZMJ");
                                        fillView(v_content, feature, "SCJZMJ");
                                        loadhfsjg();
                                        ToastMessage.Send(activity, "识别完成！");
                                        return null;
                                    }
                                });
                                return null;
                            }
                        });
                    }
                });
                v_feature.findViewById(R.id.tv_sbft).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        identyFtqk(null);
                    }
                });

                v_feature.findViewById(R.id.tv_create_bdcdy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addBdcdy();
                    }
                });
                v_feature.findViewById(R.id.tv_reload_bdcdy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reload_bdcdy();
                    }
                });

            }

        } catch (Exception es) {
            Log.e(TAG, "build: 构建失败", es);
        }
    }


    @Override
    public void build_opt() {
        super.build_opt();
        addMenu("房屋情况", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_info);
            }
        });

        addMenu("宗地信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_zdinfo);
            }
        });
        addMenu("附属结构", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_fsjginfo);
                loadhfsjg();
            }
        });
        addMenu("分摊情况", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_ftqk);
                loadhftqk();
            }
        });
        addMenu("不动产单元", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_bdcdy);
                load_bdcdy();
            }
        });
        addMenu("权界线示意图", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_ct);
                loadct();
            }
        });
        addAction("画附属", R.mipmap.app_map_layer_h_yt, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        try {
                            fv.draw_h_yt(feature, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    loadhfsjg();
                                    return null;
                                }
                            });
                        } catch (Exception es) {
                            ToastMessage.Send(activity, "画附属失败", es);
                        }
                        return null;
                    }
                });
            }
        });
    }

    private void addBdcdy() {
        final AiDialog aiDialog = AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_more_blue, "不动产单元设定");
        String oridPath = fv.getOrid_Path();
        if (StringUtil.IsNotEmpty(oridPath) || !oridPath.contains(FeatureHelper.TABLE_NAME_ZD)) {
            // 图形已经关联成功
            fv.checkBdcdy(feature, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if (t_ != null) {
                        //可以设定不动产单元
                        aiDialog.addContentView("确定要生成一个不动产单元吗?", "该操作将根据宗地与该户共同设定一个不动产单元！");
                        aiDialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                // 加载界面
                                fv.createBdcdyFromH(feature, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        mapInstance.viewFeature((Feature) t_);
                                        dialog.dismiss();
                                        return null;
                                    }
                                });
                            }
                        }).show();
                    } else {
                        aiDialog.addContentView("不能设定不动产单元", (String) objects[0] + "已经设定了不动产单元！");
                        aiDialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, null).show();
                    }
                    return null;
                }
            });
            // 需要识别
        } else {
            aiDialog.addContentView("不能设定不动产单元", "户没有与宗地关联，请智能识别户！");
            aiDialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, null).show();
        }

    }

    @Override
    public void update(final AiRunnable callback) {
        try {
            super.update(new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    update_qlrxx(callback);
                    return null;
                }
            });

        } catch (Exception es) {
            ToastMessage.Send(activity, "更新属性失败!", TAG, es);
        }
    }

    private void update_qlrxx(final AiRunnable callback) {
        final String qlrxm = AiUtil.GetValue(feature.getAttributes().get("QLRXM"), "");
        final String qlrzjh = AiUtil.GetValue(feature.getAttributes().get("QLRZJH"), "");
        if ((!qlrxm.equals(old_qlrxm)) || (!qlrxm.equals(old_qlrzjh))) { /*在权利人和H一对多的情况下，一旦QLRXM或QLRZJH发生变化，则需要新建权利人 而清除多余权利人的操作则应在FeatureEditQLR中做 20180813*/
            if (StringUtil.IsEmpty(qlrxm) || StringUtil.IsEmpty(qlrzjh)) {
                ToastMessage.Send("权利人姓名和权利人证件号不能为空！");
                AiRunnable.Ok(callback, true);
            } else {
                createNewQlrByH(mapInstance, feature, true, true, false, callback);
            }

        } else {
            AiRunnable.Ok(callback, null);
        }
    }

    private void loadhfsjg() {
        LinearLayout ll_list = (LinearLayout) view.findViewById(R.id.ll_fsjginfo_content);
//        String hid = AiUtil.GetValue(feature.getAttributes().get("ID"), "");
//        if (StringUtil.IsNotEmpty(hid)) {
        FeatureEditH_FSJG.BuildView_H_FSJG(mapInstance, ll_list, feature, 0);
//        }
    }

    View view_ftqk;

    private void loadhftqk() {
        if (view_ftqk == null) {
            ViewGroup ftqk_view = (ViewGroup) view.findViewById(R.id.ll_ftqk_list);
            ftqk_view.setTag(null); //强制重新生成adapter
            String orid = getOrid();
            String where_ = StringUtil.WhereByIsEmpty(orid) + " FTQX_ID like '%" + orid + "%' ";
            mapInstance.newFeatureView("FTQK").buildListView(ftqk_view, where_);
            view_ftqk = ftqk_view;
        }
    }

    private void reloadFtqk() {
        view_ftqk = null;
        loadhftqk();
    }

    View view_bdcdy;

    public void load_bdcdy() {
        if (view_bdcdy == null) {
            ViewGroup bdcdy_view = (ViewGroup) view.findViewById(R.id.ll_bdcdy_list);
            bdcdy_view.setTag(null); //强制重新生成adapter
            mapInstance.newFeatureView("QLRXX").buildListView(bdcdy_view, fv.queryChildWhere());
            view_bdcdy = bdcdy_view;
        }
    }

    /**
     * 重新加载不动产单元列表
     */
    private void reload_bdcdy() {
        view_bdcdy = null;
        load_bdcdy();
    }

    //宗地识别自然幢
    private void identyFtqk(final AiRunnable callback) {

        fv.update_Area(new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                // 更新界面
                fillView(v_content, feature, "SCFTJZMJ");
                reloadFtqk();
                return null;
            }
        });
    }


    private Bitmap bitmap;

    private void loadct() {
        if (bitmap == null) {
            MapHelper.geometry_ct(map, feature.getGeometry(), null, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    bitmap = (Bitmap) t_;
                    if (bitmap != null) {
                        ImageView iv_ct = (ImageView) view.findViewById(R.id.iv_ct);
                        Bitmap dm = Bitmap.createScaledBitmap(bitmap, iv_ct.getMeasuredWidth(), iv_ct.getMeasuredWidth() * bitmap.getHeight() / bitmap.getWidth(), false);
                        iv_ct.setImageBitmap(dm);
                        iv_ct.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                    AiWindow aiWindow = AiWindow.show(activity,"宗地草图",bitmap);
                                //要是自定义应用打开图片必须添加ACTION_VIEW的Intent
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                //系统提供了现成的API让用户可以将自己喜欢的图片保存到系统相册中.
                                String uriString = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, AiUtil.GetValue(feature.getAttributes().get("QLR"), ""), "房屋权界线示意图");
                                Uri uri = Uri.parse(uriString);
                                //设置数据和类型可以用setData( )或setType( ) ,但是同时设置的话是不生效的，
                                // 只能使用setDataAndType( ) ,image代表图片，星号代表图片中所有格式，
                                // 可根据自己需要筛选，如只想打开jpg类型图片可用“image/jpg”
                                intent.setDataAndType(uri, "image/*");
                                activity.startActivity(intent);

                            }
                        });
                    }

                    return null;
                }
            });

        }
    }

    public static void hsmj(Feature feature, MapInstance mapInstance, List<Feature> f_h_fsjgs) {
//        String id = AiUtil.GetValue(feature.getAttributes().get("ID"));
        String id = AiUtil.GetValue(feature.getAttributes().get("ORID"));
        Geometry g = feature.getGeometry();
        double area = 0;
        double hsmj = 0;
        if (g != null) {
            area = MapHelper.getArea(mapInstance, g);
            if (area > 0) {
                hsmj = area;
            }
        }
        for (Feature f : f_h_fsjgs) {
//            String hid = AiUtil.GetValue(f.getAttributes().get("ORID_PATH"), "");
//            if (id.equals(hid)) {
            String hid = AiUtil.GetValue(f.getAttributes().get("ORID_PATH"), "");
            if (hid.contains(id)) {
                double f_hsmj = AiUtil.GetValue(f.getAttributes().get("HSMJ"), 0d);
                hsmj += f_hsmj;
            }
        }
        feature.getAttributes().put("YCJZMJ", AiUtil.Scale(area, 2));
        feature.getAttributes().put("SCJZMJ", AiUtil.Scale(hsmj, 2));
    }


//    public static void Load(MapInstance mapInstance, String hid, final AiRunnable callback){
//        if (StringUtil.IsNotEmpty(hid)) {
//            MapHelper.QueryOne(GetTable(mapInstance), StringUtil.WhereByIsEmpty(hid)+"ID='" + hid + "'", new AiRunnable(callback) {
//                @Override
//                public <T_> T_ ok(T_ t_, Object... objects) {
//                    if (objects != null && objects.length > 0) {
//                        AiRunnable.Ok(callback,objects[0]);
//                    }else {
//                        AiRunnable.Ok(callback, null);
//                    }
//                    return null;
//                }
//            });
//        }else{
//            AiRunnable.Ok(callback,null);
//        }
//    }

    // 获取id

//    public static void InitFeatureAll(final MapInstance mapInstance, final Feature featureZD, final Feature featureZRZ,int i, final AiRunnable callback)  {
//        final LinkedHashMap<String, List<Feature>> map_all = new LinkedHashMap<String, List<Feature>>();
//        if (featureZRZ != null) {
//            final String zrzh =FeatureHelper.Get(featureZRZ,"ZRZH", "");
//            GetMaxID(mapInstance, zrzh, new AiRunnable(callback) {
//                @Override
//                public <T_> T_ ok(T_ t_, final Object... objectsMax) {
//                    final int zcs = FeatureHelper.Get(featureZRZ,"ZCS", 1);
//                    final List<Feature> features = new ArrayList<Feature>();
//                    // 查询户
//                    FeatureEditZD.Load_FsAndH_GroupbyC(mapInstance, zrzh, map_all, new AiRunnable() {
//                        @Override
//                        public <T_> T_ ok(T_ t_, Object... objects) {
//                            int count = 1;
//                            if (objectsMax.length > 1) {
//                                count = AiUtil.GetValue(objectsMax[1], 0) + 1;
//                            }
//                            final List<Integer> cs = new ArrayList<Integer>();
//                            for (int i = 1; i <= zcs; i++) {
//                                cs.add(i);
//                            }
//                            for (String key : map_all.keySet()) {
//                                int c = AiUtil.GetValue(key, 0);
//                                if (!cs.contains(c)) {
//                                    cs.add(c);
//                                }
//                            }
//                            Collections.sort(cs);
//
//                            List<Feature> features_h= GetMaxHH(mapInstance, map_all, features, featureZD, featureZRZ, zrzh, count, zcs, 1);
//                            MapHelper.saveFeature(features_h, callback);
//                            return null;
//                        }
//                    });
//
//                    return null;
//                }
//            });
//
//        } else {
//            ToastMessage.Send( "没有幢信息！");
//            AiRunnable.No(callback,null);
////            draw("H", callback);
//        }
//    }
//    public static List<Feature> GetMaxHH(MapInstance mapInstance, LinkedHashMap<String, List<Feature>> map_all, List<Feature> features ,Feature featureZD, Feature featureZRZ , String zrzh , int maxId, int zcs, int c) {
//        if (c<=zcs){
//            List<Feature> features_c = map_all.get(c+"");
//            int hh_max =1;
//            if (features_c!=null&&features_c.size()>0){
//                for (Feature feature : features_c) {
//                    String hh = FeatureHelper.Get(feature,"HH","");
//                    int bh = AiUtil.GetValue(StringUtil.substr_last(hh,2),1);
//                    hh_max = bh>hh_max?bh:hh_max;
//                }
//            }
//            String hh= String.format("%02d",c)+String.format("%02d",hh_max+1);
//            String bdcdyh=zrzh+hh;
//            String  id = zrzh+ String.format("%04d", maxId);
//            Feature f= GetTable(mapInstance).createFeature();
//            FeatureHelper.Set(f,"ID",id);
//            FeatureHelper.Set(f,"BDCDYH",bdcdyh);
//            FeatureHelper.Set(f,"SZC",c);
//            f.setGeometry(MapHelper.geometry_copy(featureZRZ.getGeometry()));
//            mapInstance.fillFeature(f,featureZRZ);
////            FillFeature(featureZD,featureZRZ,c+"",f);
//            features.add(f);
//            maxId++;
//            GetMaxHH(mapInstance,map_all,features,featureZD,featureZRZ,zrzh,maxId,zcs,c+1);
//        }
//
//        return features;
//    }

    public static void IdentyLJZ_H(final MapInstance mapInstance, final Feature f_ljz, final AiRunnable callback) {
        final String ljzh = FeatureHelper.Get(f_ljz, "LJZH", "");
        final String zrzh = FeatureHelper.Get(f_ljz, "ZRZH", "");
        final int szc = FeatureHelper.Get(f_ljz, "SZC", 1);
        final int zcs = FeatureHelper.Get(f_ljz, "ZCS", 1);

        if (StringUtil.IsNotEmpty(ljzh)) {
            final List<Feature> features_h = new ArrayList<Feature>();
            final List<Feature> features_update = new ArrayList<Feature>();
            final List<Feature> features_save = new ArrayList<Feature>();
            MapHelper.Query(GetTable(mapInstance, "H", "户"), f_ljz.getGeometry(), features_h, new AiRunnable(callback) {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    double area = MapHelper.getArea(mapInstance, f_ljz.getGeometry()) * FeatureHelper.Get(f_ljz, "ZCS", 1);
                    double area_items = 0;
                    int count = 0;
                    for (Feature f : features_h) {
                        // 编号是否有效
                        final String h_id = FeatureHelper.Get(f, "ID", "");
                        final String h_ljzh = FeatureHelper.Get(f, "LJZH", "");
                        final String h_zrzh = FeatureHelper.Get(f, "ZRZH", "");
                        final int h_lc = FeatureHelper.Get(f, "LC", 1);
                        // 自然幢号范围唯一
                        if (h_id.startsWith(zrzh) && h_zrzh.equals(zrzh)) {
                            int i = AiUtil.GetValue(h_id.substring(zrzh.length()), 0);
                            if (count < i) {
                                count = i;
                            }
                        } else {
                            // 楼层区间 正确
                            if (szc <= h_lc && h_lc < szc + zcs) {
                                features_update.add(f);
                            }
                        }
                    }
                    // 更新编号
                    if (features_update.size() > 0) {
                        for (Feature f : features_update) {
                            count++;
                            // 编号部分调整后的更新
                            String i = String.format("%04d", count);
                            FeatureHelper.Set(f, "ID", zrzh + i);
                            FeatureHelper.Set(f, "HH", i);
                            FeatureHelper.Set(f, "LJZH", ljzh);
                        }
                    }
                    // 更新其他属性
                    for (Feature f : features_h) {

                        final int h_lc = FeatureHelper.Get(f, "LC", 1);
                        final String h_ljzh = FeatureHelper.Get(f, "LJZH", "");
                        // 楼层区间 正确、本逻辑幢的
                        if (szc <= h_lc && h_lc < szc + zcs && h_ljzh.equals(ljzh)) {
                            double area_item = MapHelper.getArea(mapInstance, f.getGeometry());
                            double area_scjzmj = FeatureHelper.Get(f, "SCJZMJ", 0d);
                            final int h_szc = FeatureHelper.Get(f, "SZC", 1);
                            if (area_scjzmj < area_item - 1) {
                                area_scjzmj = area_item;
                            }
                            FeatureHelper.Set(f, "SCJZMJ", area_scjzmj);
                            FeatureHelper.Set(f, "SZC", h_szc + "");
                            features_save.add(f);
                            area_items += area_scjzmj;
                        }
                    }
                    area = area_items > 0 ? area_items : area;
                    // f_zrz.getAttributes().put("SCJZMJ",AiUtil.Scale(area,2,0));
                    FeatureHelper.Set(f_ljz, "SCJZMJ", area);
                    mapInstance.fillFeature(features_save, f_ljz);
                    features_save.add(f_ljz);
                    MapHelper.saveFeature(features_save, callback);
                    return null;
                }
            });
        } else {

            AiRunnable.Ok(callback, null);
        }
    }

    // 查询所有逻辑幢，识别户和幢附属
    public static void LaodAllH_IdentyHFSJG(final MapInstance mapInstance, final AiRunnable callback) {
        final List<Feature> fs = new ArrayList<>();
        MapHelper.Query(GetTable(mapInstance), "", -1, fs, new AiRunnable(callback) {
            // 递归调用，直到全部完成
            void identy(final List<Feature> fs, final int index, final AiRunnable identy_callback) {
                if (index < fs.size()) {
                    FeatureEditH_FSJG.IdentyH_FSJG(mapInstance, fs.get(index), new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            identy(fs, index + 1, identy_callback);
                            return null;
                        }
                    });
                } else {
                    AiRunnable.Ok(identy_callback, index);
                }
            }

            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                identy(fs, 0, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        AiRunnable.Ok(callback, fs, fs.size());
                        return null;
                    }
                });
                return null;
            }
        });
    }


    // 核算户 占地面积、建筑面积
    public static void IdentyH_Area(final MapInstance mapInstance, final Feature f_h, final AiRunnable callback) {
//        final String hid =FeatureHelper.Get(f_h,"HID", "");
        final String hid = FeatureHelper.Get(f_h, FeatureHelper.TABLE_ATTR_ORID, "");
        final List<Feature> f_zrz_h_fsjgs = new ArrayList<>();
        final List<Feature> update_fs = new ArrayList<>();

        MapHelper.Query(GetTable(mapInstance, FeatureHelper.TABLE_NAME_H_FSJG), StringUtil.WhereByIsEmpty(hid) + " ORID_PATH like '%" + hid + "%' ", "LC", "asc", -1, f_zrz_h_fsjgs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                for (Feature f : f_zrz_h_fsjgs) {
                    FeatureEditH_FSJG.hsmj(f, mapInstance);
                    update_fs.add(f);
                }
                FeatureEditH.hsmj(f_h, mapInstance, f_zrz_h_fsjgs);
                update_fs.add(f_h);
                MapHelper.saveFeature(update_fs, callback);
                return null;
            }
        });
    }

    // 核算户 占地面积、建筑面积 、分摊面积
    public static void IdentyH_Area(final MapInstance mapInstance, final Feature f_h, final List<Feature> f_ftqk, final AiRunnable callback) {
        final String hid = FeatureHelper.Get(f_h, "HID", "");
        final String hOrid = FeatureHelper.Get(f_h, FeatureHelper.TABLE_ATTR_ORID, "");
        final List<Feature> f_zrz_h_fsjgs = new ArrayList<>();
        final List<Feature> update_fs = new ArrayList<>();

        MapHelper.Query(GetTable(mapInstance, FeatureConstants.H_FSJG_TABLE_NAME), StringUtil.WhereByIsEmpty(hid) + " HID like '" + hid + "' ", "HID", "asc", -1, f_zrz_h_fsjgs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {

                MapHelper.Query(GetTable(mapInstance, FeatureConstants.FTQK_TABLE_NAME), StringUtil.WhereByIsEmpty(hOrid) + " ORID_PATH like '" + hOrid + "' ", -1, f_ftqk, new AiRunnable(callback) {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {

                        for (Feature f : f_zrz_h_fsjgs) {
                            FeatureEditH_FSJG.hsmj(f, mapInstance);
                            update_fs.add(f);
                        }
                        for (Feature f : f_ftqk) {
//                            FeatureEditFTQK.hsmj(f, mapInstance);
                            update_fs.add(f);
                        }

                        FeatureEditH.hsmj(f_h, mapInstance, f_zrz_h_fsjgs);
                        update_fs.add(f_h);
                        MapHelper.saveFeature(update_fs, callback);
                        return null;
                    }
                });
                return null;
            }
        });
    }

//    public static View GetView_H(MapInstance mapInstance, Feature f_zrz, String zcs) {
//        LinearLayout ll_view = (LinearLayout) LayoutInflater.from(mapInstance.activity).inflate(
//                R.layout.app_ui_ai_aimap_c, null);
//        ll_view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
////        ListView lv_list = (ListView)ll_view.findViewById(R.id.lv_list);
//        LinearLayout ll_list = (LinearLayout) ll_view.findViewById(R.id.ll_list);
//        BuildView_H(mapInstance,ll_list, f_zrz, zcs, 0);
//        return ll_view;
//    }

//    public static void BuildView_H(MapInstance mapInstance,final LinearLayout ll_list, final String zrzh, final int deep) {
//        BuildView_H(mapInstance,ll_list, zrzh, null, deep);
//    }

    public static void BuildView_H(final MapInstance mapInstance, final LinearLayout ll_list, final Feature f_ljz, final String cs, final int deep) {
        if (ll_list.getTag() == null) {
            QuickAdapter<Feature> adpter = new QuickAdapter<Feature>(mapInstance.activity, R.layout.app_ui_ai_aimap_h_item, new ArrayList<Feature>()) {

                @Override
                protected void convert(final BaseAdapterHelper helper, final Feature item) {
                    final String name = FeatureHelper.Get(item, "MPH", FeatureHelper.Get(item, "FHMC", ""));
                    final String desc = FeatureHelper.Get(item, "QLRXM", FeatureHelper.Get(item, "MC", ""));
                    final String id = FeatureHelper.Get(item, "ID", "");
                    final LinearLayout ll_list_item = helper.getView(R.id.ll_list_item);
                    helper.setText(R.id.tv_name, name);
                    helper.setText(R.id.tv_desc, desc);

                    int s = (int) (deep * mapInstance.activity.getResources().getDimension(R.dimen.app_size_smaller));
                    helper.getView(R.id.v_split).getLayoutParams().width = s;

                    Bitmap bm = MapHelper.geometry_icon(new Feature[]{f_ljz, item}, 100, 100, new int[]{R.color.app_theme_fore, Color.GREEN}, new int[]{1, 5});
                    if (bm != null) {
                        helper.setImageBitmap(R.id.v_icon, bm);
                    } else {
                        helper.setImageResource(R.id.v_icon, R.mipmap.app_map_layer_h);
                    }

                    helper.getView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean flag = ll_list_item.getVisibility() == View.VISIBLE;
                            if (!flag) {
                                FeatureEditH_FSJG.BuildView_H_FSJG(mapInstance, ll_list_item, item, deep + 1);
                            }
                            ll_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
                        }
                    });
                    helper.getView(R.id.iv_detial).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mapInstance.viewFeature(item);
                        }
                    });
                    helper.getView(R.id.iv_position).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MapHelper.selectAddCenterFeature(mapInstance.map, item);
                        }
                    });
                }
            };
            ll_list.setTag(adpter);
            adpter.adpter(ll_list);
        }
        final List<Feature> features = new ArrayList<Feature>();
        String where = StringUtil.IsEmpty(cs) ? "" : " and SZC='" + cs + "'";
        mapInstance.newFeatureView().queryChildFeature("H", mapInstance.getOrid(f_ljz), where, "HH", "asc", features, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                QuickAdapter<Feature> adpter = (QuickAdapter<Feature>) ll_list.getTag();
                adpter.clear();
                adpter.addAll(features);
                return null;
            }
        });

//            MapHelper.Query(GetTable(mapInstance,"H","户"), StringUtil.WhereByIsEmpty(ljzh)+" LJZH ='" + ljzh + "' " + where, 0, features, new AiRunnable() {
//                @Override
//                public <T_> T_ ok(T_ t_, Object... objects) {
//                    QuickAdapter<Feature> adpter = (QuickAdapter<Feature>) ll_list.getTag();
//                    adpter.clear();
//                    adpter.addAll(features);
//                    return null;
//                }
//            });

    }

    public static void BuildView_H(final MapInstance mapInstance, final GridView gv_list, final Feature f_ljz, final List<Feature> fs, final int deep) {
        QuickAdapter<Feature> adpter = new QuickAdapter<Feature>(mapInstance.activity, R.layout.app_ui_ai_aimap_h_item_g, fs) {
            @Override
            protected void convert(final BaseAdapterHelper helper, final Feature item) {
                Log.d(TAG, "构建户子项[" + helper.getPosition() + "]... ");
                final String id = FeatureHelper.Get(item, "ID", "");
                final String name = FeatureHelper.Get(item, "MPH", FeatureHelper.Get(item, "FHMC", ""));
                final String desc = FeatureHelper.Get(item, "QLRXM", FeatureHelper.Get(item, "MC", ""));
//                        final ListView lv_list_item = (ListView) helper.getView(R.id.lv_list_item);
                helper.setText(R.id.tv_name, name);
                helper.setText(R.id.tv_desc, desc);
                int color = item.getFeatureTable().getTableName().toUpperCase().equals("H") ? Color.GREEN : Color.GRAY;
//                    Bitmap bm = MapHelper.geometry_icon(item.getGeometry(), 100, 100, color, 5);
                Bitmap bm = MapHelper.geometry_icon(new Feature[]{f_ljz, item}, 100, 100, new int[]{R.color.app_theme_fore, color}, new int[]{1, 5});

                if (bm != null) {
                    helper.setImageBitmap(R.id.v_icon, bm);
                } else {
                    helper.setImageResource(R.id.v_icon, R.mipmap.app_map_layer_h);
                }

                helper.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mapInstance.viewFeature(item);
                    }
                });
                helper.getView().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        DialogBuilder.confirm(mapInstance.activity, "提示", "确定要删除改图形么？", null, "确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MapHelper.deleteFeature(item, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        fs.remove(item);
                                        notifyDataSetChanged();
                                        return null;
                                    }
                                });
                            }
                        }, "取消", null).show();
                        return true;
                    }
                });
            }
        };
        gv_list.setAdapter(adpter);
    }

    public static void Sjjc_h(final MapInstance mapinstance) {
        final String funcdesc = "该功能将逐一对项目中所有户进行更新。";
        License.vaildfunc(mapinstance.activity, funcdesc, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final AiDialog aidialog = AiDialog.get(mapinstance.activity);
                aidialog.setHeaderView(R.mipmap.app_icon_dangan_blue, "")
                        .setContentView("注意：属于不可逆操作，请注意备份谨慎处理！", funcdesc)
                        .setFooterView("取消", "确定，我要继续", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 完成后的回掉
                                final AiRunnable callback = new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据完成。");
                                        aidialog.setFooterView(null, "关闭", null);
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ no(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据失败！");
                                        aidialog.setFooterView(null, "关闭", null);
                                        return null;
                                    }

                                    @Override
                                    public <T_> T_ error(T_ t_, Object... objects) {
                                        aidialog.addContentView("处理数据异常！");
                                        aidialog.setFooterView(null, "关闭", null);
                                        return null;
                                    }
                                };
                                // 设置不可中断
//                                aidialog.setCancelable(false).setFooterView("正在处理中，可能需要一段时间，暂时不允许操作！");
                                aidialog.setCancelable(false).setFooterView(aidialog.getProgressView("正在处理，可能需要较长时间，暂时不允许操作"));
                                aidialog.setContentView("开始处理数据");
                                aidialog.addContentView(null, AiUtil.GetValue(new Date(), AiUtil.F_TIME) + " 查找所有户，并修改属性");

                                // 查看列表

                                final List<Feature> fs_h = new ArrayList<Feature>();
                                final boolean[] isUpdate = {false};
                                String value = "共有墙";
                                final String where = " QTGSD = '" + value + "' or  QTGSX = '" + value + "'or  QTGSN = '" + value + "'or  QTGSB = '" + value + "'";
                                //" QTGSD = '" + qlrxm + "' or  ZJH = '" + qlrzjh + "'  "
                                //String where=" QTGSD = '共有墙' or  QTGSX = '共有墙' or  QTGSN = '共有墙' or  QTGSB = '共有墙' ";
                                MapHelper.Query(FeatureEdit.GetTable(mapinstance, "H", "户"), "", -1, fs_h, new AiRunnable() {

                                    void zljc_h(final List<Feature> fs, final int i, final AiRunnable identy_callback) {
                                        if (fs.size() > i) {
//                                            mapinstance.tool.load_djzq(fs.get(i).getGeometry(), new AiRunnable() {
//                                                @Override
//                                                public <T_> T_ ok(T_ t_, Object... objects) {
//                                                    String djzq = t_ + "";
//
//                                                    ZnjcUpdateZd(mapinstance, djzq, fs.get(i), new AiRunnable() {
//                                                        @Override
//                                                        public <T_> T_ ok(T_ t_, Object... objects) {
//
//                                                            return super.ok(t_, objects);
//                                                        }
//                                                    });
//                                                    return super.ok(t_, objects);
//                                                }
//                                            });

                                            if ("共有墙".equals(FeatureHelper.Get(fs.get(i), "QTGSD", ""))) {
                                                FeatureHelper.Set(fs.get(i), "QTGSD", "共墙");
                                                isUpdate[0] = true;
                                            }
                                            if ("共有墙".equals(FeatureHelper.Get(fs.get(i), "QTGSN", ""))) {
                                                FeatureHelper.Set(fs.get(i), "QTGSN", "共墙");
                                                isUpdate[0] = true;
                                            }
                                            if ("共有墙".equals(FeatureHelper.Get(fs.get(i), "QTGSX", ""))) {
                                                FeatureHelper.Set(fs.get(i), "QTGSX", "共墙");
                                                isUpdate[0] = true;
                                            }
                                            if ("共有墙".equals(FeatureHelper.Get(fs.get(i), "QTGSB", ""))) {
                                                FeatureHelper.Set(fs.get(i), "QTGSB", "共墙");
                                                isUpdate[0] = true;
                                            }
                                            if (isUpdate[0]) {
                                                MapHelper.updateFeature(fs.get(i), new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        isUpdate[0] = false;
                                                        zljc_h(fs_h, i + 1, identy_callback);
                                                        return null;
//                                                        return super.ok(t_, objects);
                                                    }
                                                });
                                            } else {
                                                zljc_h(fs_h, i + 1, identy_callback);
                                            }


                                        } else {
                                            AiRunnable.Ok(identy_callback, i);
                                        }
                                    }

                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        zljc_h(fs_h, 0, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                AiRunnable.Ok(callback, null);
                                                return null;
                                            }
                                        });
                                        return null;
                                    }
                                });


                            }
                        }).show();

                return null;
            }
        });
    }

    //20180709
    private void outputSouthExcel() {
        final List<List<Feature>> values = new ArrayList<>();
        final List<Feature> fs_h = new ArrayList<>();
        final List<Feature> fs_zrz = new ArrayList<>();
        final List<Feature> fs_zd = new ArrayList<>();
        Feature f_zd = null;
        final Feature f_zrz = null;
        MapHelper.QueryOne(MapHelper.getTable(mapInstance.map, "ZD", "宗地"), "ZDDM='" + FeatureHelper.Get(feature, "ZDDM") + "'", new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                final Feature f_zd = (Feature) objects[0];
                MapHelper.QueryOne(MapHelper.getTable(mapInstance.map, "ZRZ", "自然幢"), "ZRZH='" + FeatureHelper.Get(feature, "ZRZH") + "'", new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        fs_zd.add(f_zd);
                        fs_zrz.add((Feature) objects[0]);
                        fs_h.add(feature);
                        values.add(fs_zd);
                        values.add(fs_zrz);
                        values.add(fs_h);
                        Excel.CreateSouthExcelToGdal(feature, FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/表格/") + FeatureViewH.GetID(feature) + ".xls", values);
                        return null;
                    }
                });
                return null;
            }
        });
    }

    //通过户信息创建新权利人 20180814
    public static void createNewQlrByH(final MapInstance mapInstance, final Feature feature_h, final boolean save, final boolean unassociate, final boolean deep_unassociate, final AiRunnable callback) {
        try {
            final String pid = GsonUtil.GetValue(mapInstance.aiMap.JsonData, "", "XMBM", "xmbm");
            FeatureEditQLR.NewID(mapInstance, pid, "", new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    String id = t_ + "";
                    final Feature feature_new_qlr = mapInstance.getTable("QLRXX").createFeature();

                    feature_new_qlr.getAttributes().put("QLRDM", id);
                    feature_new_qlr.getAttributes().put("YHZGX", "户主");
                    feature_new_qlr.getAttributes().put("XM", FeatureHelper.Get(feature_h, "QLRXM"));
                    feature_new_qlr.getAttributes().put("ZJH", FeatureHelper.Get(feature_h, "QLRZJH"));
                    feature_new_qlr.getAttributes().put("ZJZL", FeatureHelper.Get(feature_h, "QLRZJZL"));
                    feature_new_qlr.getAttributes().put("DZ", FeatureHelper.Get(feature_h, "QLRTXDZ"));
                    feature_new_qlr.getAttributes().put("DH", FeatureHelper.Get(feature_h, "QLRDH"));
                    feature_new_qlr.getAttributes().put("ORID_PATH", FeatureHelper.Get(feature_h, "ORID") + "/"); //关联权利人和户
                    mapInstance.featureView.fillFeature(feature_new_qlr);

                    //拷贝资料
                    String f_h_path = mapInstance.getpath_feature(feature_h);
                    String f_qlr_path = mapInstance.getpath_feature(feature_new_qlr);

                    String f_h_zjh_path = FileUtils.getAppDirAndMK(f_h_path + "/" + "附件材料/证件号/");
                    String f_qlr_zjh_path = FileUtils.getAppDirAndMK(f_qlr_path + "/" + "附件材料/证件号/");
                    FileUtils.copyFile(f_h_zjh_path, f_qlr_zjh_path);

                    if (unassociate) {
                        FeatureEditQLR.unassociateQlrAndBdc(mapInstance, feature_h, deep_unassociate, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                if (save) {
                                    MapHelper.saveFeature(feature_new_qlr, callback);
                                } else {
                                    AiRunnable.Ok(callback, feature_new_qlr, feature_new_qlr);
                                }
                                return null;
                            }
                        });
                    } else {
                        if (save) {
                            MapHelper.saveFeature(feature_new_qlr, callback);
                        } else {
                            AiRunnable.Ok(callback, feature_new_qlr, feature_new_qlr);
                        }
                    }
                    return null;
                }
            });
        } catch (Exception es) {
            Log.e(TAG, "通过户创建新权利人失败!" + es);
            AiRunnable.Ok(callback, false, false);
        }
    }

}
