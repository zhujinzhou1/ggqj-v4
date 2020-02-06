package com.ovit.app.map.bdc.ggqj.map.view.bdc;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.ovit.R;
import com.ovit.app.adapter.BaseAdapterHelper;
import com.ovit.app.adapter.QuickAdapter;
import com.ovit.app.core.License;
import com.ovit.app.map.MapImage;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureEdit;
import com.ovit.app.map.bdc.ggqj.map.view.FeatureView;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.map.custom.shape.ShapeUtil;
import com.ovit.app.map.model.FwPc;
import com.ovit.app.ui.dialog.AiDialog;
import com.ovit.app.ui.dialog.ToastMessage;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.AppConfig;
import com.ovit.app.util.ConvertUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.ImageUtil;
import com.ovit.app.util.ReportUtils;
import com.ovit.app.util.StringUtil;
import com.ovit.app.util.gdal.cad.DxfAdapter;
import com.ovit.app.util.gdal.shp.ShpAdapter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Lichun on 2017/4/5.
 */

public class FeatureEditC extends FeatureEdit {
    //region 常量
    final static String TAG = "FeatureEditC";
    ///endregion

    //region 字段
    FeatureViewC fv;
    View view_bdcdy;
    ///endregion

    //region 构造函数
    ///endregion

    //region 重写函数和回调
    @Override
    public void onCreate() {
        super.onCreate();
        if (super.fv instanceof FeatureViewC){
            this.fv = (FeatureViewC) super.fv;
        }
    }

    @Override
    public void init() {
        Log.i(TAG, "init featureEditC!");
        super.init();
        // 菜单
        menus = new int[]{R.id.ll_info,R.id.ll_bdcdy};
    }
    // 显示数据
    @Override
    public void build() {
        Log.i(TAG, "build zrz_c");
        final LinearLayout v_feature = (LinearLayout) LayoutInflater.from(activity).inflate(com.ovit.app.map.bdc.ggqj.R.layout.app_ui_ai_aimap_feature_zrz_c, v_content);
        try {
            if (feature != null) {
                mapInstance.fillFeature(feature);
            }
            fillView(v_feature);
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

        } catch (Exception e) {
            Log.e(TAG, "构建失败 " + e);
        }

    }
    @Override
    public void build_opt() {
        Log.i(TAG, "build zrz_c opt");
        super.build_opt();

        addAction("设定不动产", R.mipmap.app_map_layer_add_bdcdy, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBdcdy();
            }
        });
        addMenu("基本信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_info);
            }
        });

        addMenu("不动产单元", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuItem(R.id.ll_bdcdy);
                load_bdcdy();
            }
        });
    }
    ///endregion

    //region 公有函数
    public static void LoadAll(final MapInstance mapInstance, Feature f, final List<Feature> fs, AiRunnable callback) {
        LoadAll(mapInstance, mapInstance.getOrid(f), fs, callback);
    }

    public static void LoadAll(final MapInstance mapInstance, String orid, final List<Feature> fs, AiRunnable callback) {
        mapInstance.newFeatureView().queryChildFeature("ZRZ_C", orid, "SJC", "asc", fs, callback);
    }

    public static void Load(MapInstance mapInstance, String orid, final AiRunnable callback) {
        mapInstance.newFeatureView().findFeature("ZRZ_C", orid, callback);
    }

    public static void Load_FsAndH_GroupbyC(final MapInstance mapInstance, Feature feature, final LinkedHashMap<String, List<Feature>> keys, final AiRunnable callback) {
        final List<Feature> fs = new ArrayList<Feature>();
        FeatureView.LoadH_And_Z_Fsjg(mapInstance, feature, fs, fs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                keys.putAll(FeatureEditC.GroupbyC(fs));
                AiRunnable.Ok(callback, t_, objects);
                return null;
            }
        });
    }

    public static void Load_FsAndH_GroupbyC_Sort(final MapInstance mapInstance, Feature feature, final ArrayList<Map.Entry<String, List<Feature>>> keys, final AiRunnable callback) {
        final List<Feature> fs = new ArrayList<Feature>();
        FeatureView.LoadH_And_Fsjg(mapInstance, feature, fs, fs, fs, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                keys.addAll(FeatureEditC.GroupbyC_Sort(fs));
                AiRunnable.Ok(callback, t_, objects);
                return null;
            }
        });
    }

    public static LinkedHashMap<String, List<Feature>> GroupbyC(List<Feature> features) {
        LinkedHashMap<String, List<Feature>> map = new LinkedHashMap();
        for (Feature f : features) {
            // 默认一层
            String ch = FeatureHelper.Get(f, "SZC", FeatureHelper.Get(f, "LC", "1"));
            if (map.keySet().contains(ch)) {
                map.get(ch).add(f);
            } else {
                map.put(ch, new ArrayList<Feature>(Arrays.asList(new Feature[]{f})));
            }
        }
        return map;
    }

    public static ArrayList<Map.Entry<String, List<Feature>>> GroupbyC_Sort(List<Feature> features) {
        LinkedHashMap<String, List<Feature>> map = GroupbyC(features);
        ArrayList<Map.Entry<String, List<Feature>>> keys = new ArrayList<Map.Entry<String, List<Feature>>>(map.entrySet());
        Collections.sort(keys, new Comparator<Map.Entry<String, List<Feature>>>() {
            public int compare(Map.Entry<String, List<Feature>> o1, Map.Entry<String, List<Feature>> o2) {
                return new Integer(AiUtil.GetValue(o1.getKey(), 0)).compareTo(new Integer(AiUtil.GetValue(o2.getKey(), 0)));
            }
        });
        return keys;
    }

    public static String text_replace_lc(String text, int lc1, int lc2) {
        return text_replace_lc(text, 4, lc1, lc2);
    }

    public static String text_replace_lc(String text, int last, int lc1, int lc2) {
        text = AiUtil.GetValue(text, "");
        if (text.length() >= 4) {
            String lc_1 = String.format("%02d", lc1);
            String lc_2 = String.format("%02d", lc2);
            return StringUtil.substr(text, 0, text.length() - last) + StringUtil.substr_last(text, 2, 2).replace(lc_1, lc_2) + StringUtil.substr_last(text, last - 2);
        }
        return text;
    }

    public static View GetView_C(MapInstance mapInstance, Feature f_zrzOrLjz) {
        LinearLayout ll_view = (LinearLayout) LayoutInflater.from(mapInstance.activity).inflate(R.layout.app_ui_ai_aimap_c, null);
        ll_view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout ll_list = (LinearLayout) ll_view.findViewById(R.id.ll_list);
        BuildView_C(mapInstance, ll_list, f_zrzOrLjz, new LinkedHashMap<String, List<Feature>>(), 0);
        Log.d(TAG, "绘制层完成");
        return ll_view;
    }

    public static void BuildView_C(final MapInstance mapInstance, final LinearLayout ll_list, final Feature f_zrzOrLjz, final LinkedHashMap<String, List<Feature>> map_all, final int deep) {
        int dxcs = FeatureHelper.Get(f_zrzOrLjz, "DXCS", 0);
        final int qsc = dxcs>0?-dxcs:1;
        final int zcs = FeatureHelper.Get(f_zrzOrLjz, "ZCS", 1);

        final QuickAdapter<Integer> adapter;
        if (ll_list.getTag() == null) {
            adapter = new QuickAdapter<Integer>(mapInstance.activity, R.layout.app_ui_ai_aimap_c_item, new ArrayList<Integer>()) {
                int selectIndex = 0;

                @Override
                protected void convert(final BaseAdapterHelper helper, final Integer item) {
                    final String id = item + "";
                    List<Feature> fs_ = map_all.get(id);
                    if (fs_ == null) {
                        fs_ = new ArrayList<Feature>();
                        map_all.put(id, fs_);
                    }
                    final List<Feature> fs = fs_;

                    final GridView gv_list_item = helper.getView(R.id.gv_list_item);
                    helper.setText(R.id.tv_name, id);
                    helper.getView(R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean flag = gv_list_item.getVisibility() == View.VISIBLE;
                            gv_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
                        }
                    });
                    Log.d(TAG, "构建户[" + helper.getPosition() + "]... ");
                    FeatureEditH.BuildView_H(mapInstance, gv_list_item, f_zrzOrLjz, fs, deep + 1);
                    gv_list_item.measure(gv_list_item.getLayoutParams().width, gv_list_item.getLayoutParams().height);

                    boolean isLjz = mapInstance.getTable(FeatureHelper.TABLE_NAME_LJZ) == f_zrzOrLjz.getFeatureTable();
                    helper.setVisible(R.id.iv_add, isLjz);
                    helper.getView(R.id.iv_add).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mapInstance.newFeatureView().draw_h(f_zrzOrLjz, id, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    if (t_ instanceof Feature) {
                                        fs.add((Feature) t_);
                                    }
                                    FeatureEditH.BuildView_H(mapInstance, gv_list_item, f_zrzOrLjz, fs, deep + 1);
                                    gv_list_item.measure(gv_list_item.getLayoutParams().width, gv_list_item.getLayoutParams().height);
                                    return null;
                                }
                            });
                        }
                    });

                    helper.setVisible(R.id.iv_copy, isLjz);
                    helper.getView(R.id.iv_copy).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            copylc(mapInstance, f_zrzOrLjz, id, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
//                                    BuildView_C(mapInstance, ll_list, f_zrzOrLjz, map_all, deep);
                                    FeatureView fv = mapInstance.newFeatureView(f_zrzOrLjz);
                                    final List<Feature> fs_h = new ArrayList<>();
                                    fv.queryChildFeature(FeatureHelper.TABLE_NAME_H, f_zrzOrLjz, fs_h, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            new AiForEach<Feature>(fs_h, new AiRunnable() {
                                                @Override
                                                public <T_> T_ ok(T_ t_, Object... objects) {
                                                    BuildView_C(mapInstance, ll_list, f_zrzOrLjz, map_all, deep);
                                                    return null;
                                                }
                                            }){
                                                @Override
                                                public void exec() {
                                                    FeatureEditH_FSJG.IdentyH_FSJG(mapInstance,fs_h.get(postion),getNext());
                                                }
                                            }.start();
                                            return null;
                                        }
                                    });
                                    return null;
                                }
                            });
                        }
                    });
                    helper.setText(R.id.tv_count, fs.size() + "");

                    boolean visible = helper.getPosition() == selectIndex;
                    helper.setImageResource(R.id.iv_state, visible ? R.mipmap.app_state_eye_visible : R.mipmap.app_state_eye_invisible);
                    MapHelper.geometry_visible(fs, visible);

                    helper.getView(R.id.iv_state).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = helper.getPosition();
                            selectIndex = selectIndex == position ? -1 : position;
                            // 本来此次可以直接notifyDataSetChanged () 但是刷新速度太慢，故先响应操作
                            boolean visible = helper.getPosition() == selectIndex;
                            helper.setImageResource(R.id.iv_state, visible ? R.mipmap.app_state_eye_visible : R.mipmap.app_state_eye_invisible);
                            notifyDataSetChanged();
                            MapHelper.geometry_visible(fs, visible);
                        }
                    });
                }
            };
            ll_list.setTag(adapter);
            adapter.adpter(ll_list);
        } else {
            adapter = (QuickAdapter<Integer>) ll_list.getTag();
        }

        Log.d(TAG, "加载附属、户... ");
        map_all.clear();
        Load_FsAndH_GroupbyC(mapInstance, f_zrzOrLjz, map_all, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {

                Log.d(TAG, "加载附属、户完成、排序...");
                int zcs_ = AiUtil.GetValue(zcs, 1);
                final List<Integer> cs = new ArrayList<Integer>();
                for (int i = 0; i < zcs_; i++) {
                    if (qsc<0){
                        if (i>=-qsc){
                            cs.add(qsc + i+1);
                        }else {
                            cs.add(qsc + i);
                        }
                    }else {
                        cs.add(qsc + i);
                    }
                }
                for (String key : map_all.keySet()) {
                    int c = AiUtil.GetValue(key, 0);
                    if (!cs.contains(c)) {
                        cs.add(c);
                    }
                }
                Collections.sort(cs);
                Log.d(TAG, "更新界面...");
                adapter.replaceAll(cs);
                Log.d(TAG, "更新界面完成");
                return null;
            }
        });

    }

    //生成带可展开每层分户图图形的适配器 20180817
    public static QuickAdapter<Map.Entry<String, List<Feature>>> getFcfhtAdapter(final MapInstance mapInstance, final Feature feature, final String jpg_type, final AiRunnable callback) {
        return new QuickAdapter<Map.Entry<String, List<Feature>>>(mapInstance.activity, R.layout.app_ui_ai_aimap_c_item_fcfht, new ArrayList<Map.Entry<String, List<Feature>>>()) {
            @Override
            protected void convert(final BaseAdapterHelper helper, Map.Entry<String, List<Feature>> item) {
                String id = item.getKey();
                helper.setText(R.id.tv_name, item.getKey());
                helper.setText(R.id.tv_count, item.getValue().size() + "");
                final ImageView iv_image = helper.getView(R.id.iv_image);
                helper.setImageResource(R.id.iv_extend, iv_image.getVisibility() == View.VISIBLE ? R.mipmap.app_icon_opt_to_right : R.mipmap.app_icon_opt_to_bottom);
                helper.getView(R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = iv_image.getVisibility() == View.VISIBLE;
                        iv_image.setVisibility(flag ? View.GONE : View.VISIBLE);
                        helper.setImageResource(R.id.iv_extend, flag ? R.mipmap.app_icon_opt_to_right : R.mipmap.app_icon_opt_to_bottom);
                    }
                });
                final String filename = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/" + jpg_type + "/") + jpg_type + "_" + id + ".jpg";
                ImageUtil.set(iv_image, filename);
            }
        };
    }

    //生成带可展开每层分层图图形的适配器 20180817
    public static QuickAdapter<String> getFctAdapter(final MapInstance mapInstance, final Feature feature, final String jpg_type, final AiRunnable callback) {
        return new QuickAdapter<String>(mapInstance.activity, R.layout.app_ui_ai_aimap_c_item_fcfht, new ArrayList<String>()) {
            @Override
            protected void convert(final BaseAdapterHelper helper, final String item) {
                String id = item;
                helper.setText(R.id.tv_name, item);
                final ImageView iv_image = helper.getView(R.id.iv_image);
                helper.setVisible(R.id.tv_count, false);
                helper.setImageResource(R.id.iv_extend, iv_image.getVisibility() == View.VISIBLE ? R.mipmap.app_icon_opt_to_right : R.mipmap.app_icon_opt_to_bottom);
                helper.getView(R.id.ll_head).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = iv_image.getVisibility() == View.VISIBLE;
                        iv_image.setVisibility(flag ? View.GONE : View.VISIBLE);
                        helper.setImageResource(R.id.iv_extend, flag ? R.mipmap.app_icon_opt_to_right : R.mipmap.app_icon_opt_to_bottom);
                    }
                });
                final String filename = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/" + jpg_type + "/") + jpg_type + "_" + id + ".jpg";
                ImageUtil.set(iv_image, filename);
            }
        };
    }

    public static String getpath(final MapInstance mapInstance, final Feature feature, String type, String cs, String dex) {
        return FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/" + type + "/") + type + "_" + cs + dex;
    }

    public static void clearpath(final MapInstance mapInstance, final Feature feature, String type) {
        FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/" + type + "/", true);
    }

    public String getpath_FCFHT_DXF(final MapInstance mapInstance, final Feature feature, String cs) {
        return FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature) + "附件材料/分层分户图/") + "分层分户图_" + cs + ".dxf";
    }

    // 分层分户图汇总 其中fct为图片路径集合
    public static void createFCT(final MapInstance mapInstance, final Feature feature, ArrayList<Map.Entry<String, List<Feature>>> cs, List<String> fct, FwPc pc) {
        String type = "分层图";
        clearpath(mapInstance, feature, type);
        List<String> cgs_key = new ArrayList<>();
        List<Feature> cgs__value = null;

        int i = 0;
        boolean isfrist = true;
        while (i < cs.size()) {
            Map.Entry<String, List<Feature>> item = cs.get(i);
            boolean islast = i == cs.size() - 1;
            // 才开始
            if (isfrist) {
                cgs_key.add(item.getKey());
                cgs__value = item.getValue();
                i++;
                if (!islast) {
                    isfrist = false;
                    continue;
                }
            }
            // 相同的
            if ((!isfrist) && MapHelper.geometry_feature_equals(cgs__value, item.getValue())) {
                cgs_key.add(item.getKey());
                i++;
                if (!islast) continue;
            }
            //不一样就生成
            String key = cgs_key.size() == 1 ? cgs_key.get(0) : (cgs_key.get(0) + "-" + cgs_key.get(cgs_key.size() - 1));
            createFCFHT_JPG(mapInstance, feature, key, getpath(mapInstance, feature, type, key, ".jpg"), cgs__value, pc);
            createFCFHT_DXF(mapInstance, feature, key, getpath(mapInstance, feature, type, key, ".dxf"), cgs__value, pc);
            cgs_key = new ArrayList<>();
            cgs__value = null;
            isfrist = true;
            fct.add(key);
        }

    }

    // 分层分户图
    public static void createFCFHT(final MapInstance mapInstance, final Feature feature, ArrayList<Map.Entry<String, List<Feature>>> cs, FwPc pc) {
        String type = "分层分户图";
        FeatureEditC.clearpath(mapInstance, feature, type);
        for (Map.Entry<String, List<Feature>> item : cs) {
            String path_shp = FeatureEditC.getpath(mapInstance, feature, type, item.getKey(), ".shp");
            ShpAdapter.writeShp(path_shp, item.getValue());

            String path_jpg = FeatureEditC.getpath(mapInstance, feature, type, item.getKey(), ".jpg");
            createFCFHT_JPG(mapInstance, feature, item.getKey(), path_jpg, item.getValue(), pc);

            String path_dxf = FeatureEditC.getpath(mapInstance, feature, type, item.getKey(), ".dxf");
            createFCFHT_DXF(mapInstance, feature, item.getKey(), path_dxf, item.getValue(), pc);
        }
    }

    public static void createFCFHT_DXF(final MapInstance mapInstance, final Feature feature, String cs, String dxfpath, List<Feature> fs, FwPc pc) {
        try {
            if (StringUtil.IsEmpty(dxfpath)) {
                dxfpath = getpath(mapInstance, feature, "分层分户图", cs, ".dxf");
            }
            List<Feature> fs_ = new ArrayList<>(fs);
            fs_.add(feature);
            Envelope extent = MapHelper.geometry_combineExtents_Feature(fs_);
            DxfAdapter dxf = new DxfAdapter();
            dxf.create(dxfpath, extent, mapInstance.aiMap.getProjectWkid());
            dxf.write(mapInstance, fs, pc); // 不绘制幢
            dxf.writeMText(new Point((extent.getXMin() + extent.getXMax()) / 2d, extent.getYMax() + 2, extent.getSpatialReference()), "第" + cs + "层", 0, "", 1.5f, 1, 2, 7, "JZD", "");
            dxf.save();
        } catch (Exception es) {
            Log.e(TAG, "生成分层分户图失败");
        }
    }

    public static void createFCFHT_JPG(final MapInstance mapInstance, final Feature feature, String cs, String filename, List<Feature> fs, FwPc pc) {
        List<com.esri.arcgisruntime.geometry.Geometry> gs = new ArrayList<>();
        for (Feature f : fs) {
            gs.add(f.getGeometry());
        }
        gs.add(feature.getGeometry());
        Envelope extent = MapHelper.geometry_combineExtents(gs);
        // 获取适合比例
        int scale = MapHelper.geometry_scale(mapInstance.map, extent, 1.0f);
        // 地理上的距离
        double d_s_w = MapHelper.getLength(MapHelper.U_L, new Point(extent.getXMin(), extent.getYMin(), extent.getSpatialReference()), new Point(extent.getXMax(), extent.getYMin(), extent.getSpatialReference()));
        double d_s_h = MapHelper.getLength(MapHelper.U_L, new Point(extent.getXMin(), extent.getYMin(), extent.getSpatialReference()), new Point(extent.getXMin(), extent.getYMax(), extent.getSpatialReference()));
        // 1米
        double px = 5000d / scale;
        // 实际像素
        int px_w = (int) (d_s_w * px);
        int px_h = (int) (d_s_h * px);
        int px_size = px_w > px_h ? px_w : px_h;

        int left = 60;
        int top = 60;
        int right = 60;
        int bottom = 60;
        int w = px_size + left + right;
        int h = px_size + top + bottom;
        // 白色画布
        MapImage img = new MapImage(extent, w, h, left, top, right, bottom).draw(Color.WHITE);

        // 画幢
//       img.setColor(Color.BLUE).setSw(3).draw(feature,mapInstance.getLabel(feature));
        // 单层户
        img.setColor(Color.BLACK).setSw(1);


        for (Feature f : fs) {
            if (MapHelper.getLayerName(f).contains("附属")) {
                img.getPaint().setPathEffect(new DashPathEffect(new float[]{4, 4}, 0));
            } else {
                img.getPaint().setPathEffect(null);
                img.getPaint().setTextSize(12);
            }
//                    android.graphics.PorterDuff.Mode.DST_IN
//                    PorterDuffXfermode mode=new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
//                    img.getPaint().setXfermode(mode);
            img.draw(f, mapInstance.getLabel(f), 16, pc);
        }
//        img.draw(new android.graphics.Point(w / 2, 40), "第" + cs + "层", 0, 0, 0, 20);
        // 是否摆正幢
        if (AppConfig.PHSZ_IMG_ADJUST_UPRIGHT.equals(AppConfig.get(AppConfig.APP_BDCQJDC_PHSZ_IMG_ADJUST, AppConfig.PHSZ_IMG_ADJUST_BASIC))) {

            // 摆正幢
            float alpha = (float) MapHelper.geometry_get_azimuth(feature.getGeometry());
            img.set(img.rotate(alpha));
            // 加指北针，并旋转
            Bitmap bm_zbz = ImageUtil.getBitmap(mapInstance.map.getContext(), R.mipmap.app_map_zbz);
            bm_zbz = MapImage.Rotate(bm_zbz, alpha);
            img.draw(bm_zbz, img.w - bm_zbz.getWidth(), 0, bm_zbz.getWidth(), bm_zbz.getHeight());
        }
        // 是否启用比例尺
        if (AppConfig.PHSZ_BLC_OPEN.equals(AppConfig.get(AppConfig.APP_BDCQJDC_PHSZ_BLC, AppConfig.PHSZ_BLC_OPEN))) {
            // 比例尺
            double s_d_m = (scale / 100);
            String s_d_m_ = String.format("%.1f", s_d_m).replace(".0", "");
            // 比例尺长 像素
//        int s_d_s = (int) getScreenDistance(map, s_d_m);
            int s_d_s = (int) (px * s_d_m);
            // 边距 像素
            int p_l = 20;
            int p_t = p_l * 4;

            // 画比例尺
            p_t = p_l * 2;
            img.draw("1:" + (int) (scale * 0.5), (float) (p_l + s_d_s / 2), (float) (p_t));
            p_t = p_l * 3;
            //比例尺划线
            img.draw((float) p_l, (float) p_t, (float) (s_d_s + p_l), (float) p_t);
            // 比例尺两端的线
            img.draw((float) p_l, (float) p_t + 1, (float) p_l, (float) (p_t - 8));
            img.draw((float) (s_d_s + p_l), (float) p_t + 1, (float) (s_d_s + p_l), (float) (p_t - 8));
            p_t = p_l * 4;
            // 比例尺长度
            img.draw(s_d_m_ + "米", (float) (p_l + s_d_s / 2), (float) (p_t));
        }

        if (!License.check()) {
            // 水印
            Bitmap bm_sy = ImageUtil.getBitmap(mapInstance.map.getContext(), R.mipmap.ovit_sy);
            img.draw(bm_sy, w / 3, h / 3, w / 3, h / 3);
        }

        Bitmap bitmap = img.getValue();
        img.set((Bitmap) null);
        if (bitmap != null) {
            try {
                if (StringUtil.IsEmpty(filename)) {
                    filename = getpath(mapInstance, feature, "分层分户图", cs, ".jpg");
                }
                FileUtils.writeFile(filename, ConvertUtil.convert(bitmap));
            } catch (Exception es) {
                Log.e(TAG, "生成分层分户图失败");
            }
        }

    }

    //计算层相关的面积 (目前可计算 层建筑面积、层分摊建筑面积)20180821
    public static String calcuZrzcMj(final List<Feature> features, String mode) {
        BigDecimal result = new BigDecimal("0");
        switch (mode) {
            case "CJZMJ": {
                for (Feature feature : features) {
                    if (feature.getFeatureTable().getTableName().equals("H")) {

                        result = result.add(new BigDecimal(StringUtil.IsEmpty(FeatureHelper.Get(feature, "YCJZMJ")) ? "0" : FeatureHelper.Get(feature, "YCJZMJ", "")));

                    } else if (feature.getFeatureTable().getTableName().equals("H_FSJG")) {

                        result = result.add(new BigDecimal(StringUtil.IsEmpty(FeatureHelper.Get(feature, "HSMJ")) ? "0" : FeatureHelper.Get(feature, "HSMJ", "")));

                    } else if (feature.getFeatureTable().getTableName().equals("Z_FSJG")) {

                        result = result.add(new BigDecimal(StringUtil.IsEmpty(FeatureHelper.Get(feature, "HSMJ")) ? "0" : FeatureHelper.Get(feature, "HSMJ", "")));
                    }
                }
                break;
            }
            case "CFTJZMJ": {
                for (Feature feature : features) {
                    if (feature.getFeatureTable().getTableName().equals("H")) {
                        result = result.add(new BigDecimal(StringUtil.IsEmpty(FeatureHelper.Get(feature, "SCFTJZMJ")) ? "0" : FeatureHelper.Get(feature, "SCFTJZMJ").toString()));
                    }
                }
                break;
            }
        }
        return result.toString();
    }

    //根据自然幢提取 ZRZ_C feature 20180820
    public static void extractZRZ_C(final MapInstance mapInstance, final Feature feature, final boolean save, final AiRunnable callback) {
        MapHelper.Query(mapInstance.getTable("ZRZ_C"), "ORID_PATH like '%" + FeatureHelper.Get(feature, "ORID") + "%'", new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                MapHelper.deleteFeature((List<Feature>) t_, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        final ArrayList<Map.Entry<String, List<Feature>>> zrz_c_s = new ArrayList<>();
                        Load_FsAndH_GroupbyC_Sort(mapInstance, feature, zrz_c_s, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                final LinkedHashMap<Feature, List<Feature>> features_c = new LinkedHashMap();
                                for (Map.Entry<String, List<Feature>> zrz_c : zrz_c_s) {
                                    Feature feature_c = mapInstance.getTable("ZRZ_C").createFeature();
                                    mapInstance.fillFeature(feature_c, feature);
                                    FeatureHelper.Set(feature_c, "CH", zrz_c.getKey());
                                    FeatureHelper.Set(feature_c, "SJC", zrz_c.getKey());
                                    FeatureHelper.Set(feature_c, "ZRZH", FeatureHelper.Get(feature, "ZH"));
                                    FeatureHelper.Set(feature_c, "CJZMJ", calcuZrzcMj(zrz_c.getValue(), "CJZMJ"));
                                    FeatureHelper.Set(feature_c, "CFTJZMJ", calcuZrzcMj(zrz_c.getValue(), "CFTJZMJ"));
                                    features_c.put(feature_c, zrz_c.getValue());
                                }
                                if (save) {
                                    final List<Feature> fs_c = new ArrayList<>();
                                    Iterator iterator = features_c.entrySet().iterator();
                                    while (iterator.hasNext()) {
                                        Map.Entry entry = (Map.Entry) iterator.next();
                                        Feature key = (Feature) entry.getKey();
                                        fs_c.add(key);
                                    }
                                    MapHelper.saveFeature(fs_c, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            AiRunnable.Ok(callback, features_c, features_c);
                                            return null;
                                        }
                                    });
                                } else {
                                    AiRunnable.Ok(callback, features_c, features_c);
                                }
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

    // 根据自然幢提取层
    public static void InitFeatureAll(final MapInstance mapInstance, final Feature feature, final AiRunnable callback) {
        MapHelper.Query(mapInstance.getTable("ZRZ_C"), "ORID_PATH like '%" + FeatureHelper.Get(feature, "ORID") + "%'", new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                MapHelper.deleteFeature((List<Feature>) t_, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        final ArrayList<Map.Entry<String, List<Feature>>> zrz_c_s = new ArrayList<>();
                        Load_FsAndH_GroupbyC_Sort(mapInstance, feature, zrz_c_s, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
//                                final LinkedHashMap<Feature, List<Feature>> features_c = new LinkedHashMap();
                                final List<Feature> featuresC = new ArrayList<>();
                                for (Map.Entry<String, List<Feature>> zrz_c : zrz_c_s) {
                                    Feature feature_c = mapInstance.getTable("ZRZ_C").createFeature();
                                    mapInstance.fillFeature(feature_c, feature);
                                    FeatureHelper.Set(feature_c, "CH", zrz_c.getKey());
                                    FeatureHelper.Set(feature_c, "SJC", zrz_c.getKey());
                                    FeatureHelper.Set(feature_c, "ZRZH", FeatureHelper.Get(feature, "ZH"));
                                    FeatureHelper.Set(feature_c, "CJZMJ", calcuZrzcMj(zrz_c.getValue(), "CJZMJ"));
                                    FeatureHelper.Set(feature_c, "CFTJZMJ", calcuZrzcMj(zrz_c.getValue(), "CFTJZMJ"));
                                    List<Feature> featuresH = new ArrayList<>();
                                    for (Feature feature : zrz_c.getValue()) {
                                        if ("H".equals(feature.getFeatureTable().getTableName())) {
                                            featuresH.add(feature);
                                        }
                                    }
                                    if (featuresH.size() > 0) {
                                        com.esri.arcgisruntime.geometry.Geometry geometry = GeometryEngine.union(MapHelper.geometry_get(featuresH));
                                        feature_c.setGeometry(geometry);
                                    }
                                    featuresC.add(feature_c);
                                }
                                MapHelper.saveFeature(featuresC, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        AiRunnable.Ok(callback, featuresC);
                                        return null;
                                    }
                                });
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

    //根据传入的zrz_c集合生成view视图 (可展开户信息) 20180821
    public static View build_zrz_c_view(final MapInstance mapInstance, final Feature feature_zrz, final LinkedHashMap<Feature, List<Feature>> features_c, final int deep, final AiRunnable callback) {
        LinearLayout ll_view = (LinearLayout) LayoutInflater.from(mapInstance.activity).inflate(R.layout.app_ui_ai_aimap_c, null);
        ll_view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout ll_list = (LinearLayout) ll_view.findViewById(R.id.ll_list);

        final List<Feature> fs_c = new ArrayList<>();
        if (features_c.size() > 0) {
            Iterator iterator = features_c.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                fs_c.add((Feature) entry.getKey());
            }
        }
        final QuickAdapter<Feature> adapter = new QuickAdapter<Feature>(mapInstance.activity, R.layout.app_ui_ai_aimap_c_item, fs_c) {
            @Override
            protected void convert(final BaseAdapterHelper helper, final Feature item) {
                final String id = FeatureHelper.Get(item, "CH") + "";
                helper.setText(R.id.tv_name, id);

                final List<Feature> fs_ = features_c.get(item);
                final GridView gv_list_item = helper.getView(R.id.gv_list_item);
                gv_list_item.setVisibility(View.INVISIBLE);
                helper.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = gv_list_item.getVisibility() == View.VISIBLE;
                        gv_list_item.setVisibility(flag ? View.GONE : View.VISIBLE);
                        if (gv_list_item.getVisibility() == View.VISIBLE) {
                            FeatureEditH.BuildView_H(mapInstance, gv_list_item, feature_zrz, fs_, deep + 1);
                            gv_list_item.measure(gv_list_item.getLayoutParams().width, gv_list_item.getLayoutParams().height);
                        }
                    }
                });

                helper.getView(R.id.iv_add).setVisibility(View.GONE);
                helper.getView(R.id.iv_copy).setVisibility(View.GONE);
                helper.getView(R.id.iv_state).setVisibility(View.GONE);
                helper.setText(R.id.tv_count, fs_.size() + "");
                helper.getView(R.id.iv_detail).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mapInstance.viewFeature(item);
                    }
                });
            }
        };

        ll_list.setTag(adapter);
        adapter.adpter(ll_list);
        return ll_view;
    }

    // 加载层信息
    public static void loadZrzcInfo(final MapInstance mapInstance, final Feature feature, final ViewGroup zrzc_view, final AiRunnable callback) {
        extractZRZ_C(mapInstance, feature, true, new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                zrzc_view.addView(build_zrz_c_view(mapInstance, feature, (LinkedHashMap<Feature, List<Feature>>) t_, 0, null));
                AiRunnable.Ok(callback, true, true);
                return null;
            }
        });

    }

    public static void loadZrzc(final MapInstance mapInstance, final Feature featureZrz, final ViewGroup zrzc_view, final AiRunnable callback) {
        MapHelper.Query(mapInstance.getTable("ZRZ_C"), "ORID_PATH like '%" + FeatureHelper.Get(featureZrz, "ORID") + "%'", new AiRunnable() {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                List<Feature> featuresC = (List<Feature>) t_;
                final Map<Integer, Feature> mapC = new HashMap<>();
                int key;
                for (Feature featureC : featuresC) {
                    key = (int) featureC.getAttributes().get("SJC");
                    mapC.put(key, featureC);
                }
                final ArrayList<Map.Entry<String, List<Feature>>> zrz_c_s = new ArrayList<>();
                Load_FsAndH_GroupbyC_Sort(mapInstance, featureZrz, zrz_c_s, new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        final LinkedHashMap<Feature, List<Feature>> features_c = new LinkedHashMap();
                        for (Map.Entry<String, List<Feature>> zrz_c : zrz_c_s) {
                            features_c.put(mapC.get(Integer.parseInt(zrz_c.getKey())), zrz_c.getValue());
                        }
                        zrzc_view.addView(build_zrz_c_view(mapInstance, featureZrz, features_c, 0, null));
                        AiRunnable.Ok(callback, true, true);
                        return null;
                    }
                });
                return null;
            }
        });
    }

    // 图通过逻辑幢重新生成
    public static void UpdateC(final MapInstance mapInstance, final Feature feature, AiRunnable callback_) {
        final AiDialog dialog = AiDialog.get(mapInstance.activity).setHeaderView(R.mipmap.app_map_layer_zrz_c, "生成层")
                .addContentView("将根据自然幢的范围删除原有的层，重新层", "该操作不可逆转，如果已经生成过层请谨慎操作");
        dialog.setFooterView(dialog.getProgressView("正在处理，请稍后..."));//.setCancelable(false);

        final AiRunnable callback = new AiRunnable(callback_) {
            @Override
            public <T_> void finlly(T_ t_, Object... objects) {
                dialog.setCancelable(true).dismiss();
            }
        };
        {
            MapHelper.Query(mapInstance.getTable("ZRZ_C"), "ORID_PATH like '%" + FeatureHelper.Get(feature, "ORID") + "%'", new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    MapHelper.deleteFeature((List<Feature>) t_, new AiRunnable() {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            final ArrayList<Map.Entry<String, List<Feature>>> zrz_c_s = new ArrayList<>();
                            Load_FsAndH_GroupbyC_Sort(mapInstance, feature, zrz_c_s, new AiRunnable() {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    final List<Feature> featuresC = new ArrayList<>();
                                    for (Map.Entry<String, List<Feature>> zrz_c : zrz_c_s) {
                                        Feature feature_c = mapInstance.getTable("ZRZ_C").createFeature();
                                        mapInstance.fillFeature(feature_c, feature);
                                        FeatureHelper.Set(feature_c, "CH", zrz_c.getKey());
                                        FeatureHelper.Set(feature_c, "SJC", zrz_c.getKey());
                                        FeatureHelper.Set(feature_c, "ZRZH", FeatureHelper.Get(feature, "ZH"));
                                        FeatureHelper.Set(feature_c, "CJZMJ", calcuZrzcMj(zrz_c.getValue(), "CJZMJ"));
                                        FeatureHelper.Set(feature_c, "CFTJZMJ", calcuZrzcMj(zrz_c.getValue(), "CFTJZMJ"));
                                        List<Feature> featuresH = new ArrayList<>();
                                        for (Feature feature : zrz_c.getValue()) {
                                            if ("H".equals(feature.getFeatureTable().getTableName())) {
                                                featuresH.add(feature);
                                            }
                                        }
                                        if (featuresH.size() > 0) {
                                            com.esri.arcgisruntime.geometry.Geometry geometry = GeometryEngine.union(MapHelper.geometry_get(featuresH));
                                            feature_c.setGeometry(geometry);
                                        }
                                        featuresC.add(feature_c);
                                    }
                                    MapHelper.saveFeature(featuresC, new AiRunnable() {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            AiRunnable.Ok(callback, featuresC);
                                            return null;
                                        }
                                    });
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
    }

    //  图通过户加载所有图形重新生成
    public static void LoadAllFeatureToH() {

    }
    //生成成果
    public static void CreateDOCX(final MapInstance mapInstance, final Feature featureBdcdy, final boolean isRelaod, final AiRunnable callback) {
        final String bdcdyh = FeatureEditQLR.GetBdcdyh(featureBdcdy);
        String file_dcb_doc = FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh);
        if (FileUtils.exsit(file_dcb_doc) && !isRelaod) {
            AiRunnable.Ok(callback, file_dcb_doc);
        } else {
            FeatureEditBDC.LoadZD(mapInstance, bdcdyh, new AiRunnable(callback) {
                        @Override
                        public <T_> T_ ok(T_ t_, Object... objects) {
                            final Feature f_zd = (Feature) t_;
                            final List<Feature> fs_zrz = new ArrayList<Feature>();
                            final List<Feature> fs_ljz = new ArrayList<Feature>();
                            final LinkedHashMap<Feature, List<Feature>> fs_c_all = new LinkedHashMap<>();
                            final List<Feature> fs_h = new ArrayList<Feature>();
                            final List<Feature> fs_jzd = new ArrayList<Feature>();
                            final List<Feature> fs_jzx = new ArrayList<Feature>();
                            final List<Feature> fs_bdc_h = new ArrayList<Feature>();
                            final List<Feature> fs_c = new ArrayList<Feature>();
                            final List<Feature> fs_ftqk = new ArrayList<Feature>();
                            final Map<String, Feature> map_jzx = new HashMap<>();
                            final List<Map<String, Object>> fs_jzqz = new ArrayList<>();

                            LoadAll(mapInstance, bdcdyh, featureBdcdy, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_ljz, fs_bdc_h, fs_h,fs_c,fs_ftqk,fs_c_all, new AiRunnable(callback) {
                                @Override
                                public <T_> T_ ok(T_ t_, Object... objects) {
                                    CreateDOCX(mapInstance, bdcdyh, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, fs_zrz, fs_ljz, fs_h,fs_c_all,isRelaod, new AiRunnable(callback) {
                                        @Override
                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                            // 数据归集
                                            OutputData(mapInstance, featureBdcdy, f_zd, fs_jzd, fs_jzx, fs_zrz ,fs_h,fs_c,fs_ftqk,fs_c_all);
                                            AiRunnable.Ok(callback, t_, objects);
                                            return null;
                                        }
                                    });
                                    return null;
                                }
                            });
                            return null;
                        }
                    }
            );
        }
    }


    public static void LoadAll(final MapInstance mapInstance, final String bdcdyh,
                               final Feature featureBdcdy,
                               final Feature f_zd,
                               final List<Feature> fs_jzd,
                               final List<Feature> fs_jzx,
                               final Map<String, Feature> map_jzx,
                               final List<Map<String, Object>> fs_jzqz,
                               final List<Feature> fs_zrz,
                               final List<Feature> fs_ljz,
                               final List<Feature> fs_bdc_h,
                               final List<Feature> fs_h,
                               final  List<Feature> fs_c,
                               final  List<Feature> fs_ftqk,
                               final LinkedHashMap<Feature, List<Feature>> fs_c_all,
                               final AiRunnable callback) {
        final String orid_bdc = FeatureHelper.GetLastOrid(featureBdcdy);
        FeatureEditBDC.LoadJZDXQZ(mapInstance, f_zd, fs_jzd, fs_jzx, map_jzx, fs_jzqz, new AiRunnable(callback) {
            @Override
            public <T_> T_ ok(T_ t_, Object... objects) {
                MapHelper.QueryOne(GetTable(mapInstance, FeatureConstants.C_TABLE_NAME), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID= '" + orid_bdc + "' ", new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Feature f_c = (Feature) t_;
                        fs_c.add(f_c);
                        MapHelper.Query(GetTable(mapInstance, FeatureConstants.ZRZ_TABLE_NAME), StringUtil.WhereByIsEmpty(bdcdyh) + " ORID_PATH like '%" + orid_bdc + "%' ", "ZRZH", "asc", -1, fs_zrz, new AiRunnable() {
                            @Override
                            public <T_> T_ ok(T_ t_, Object... objects) {
                                new FeatureView().queryChildFeature(FeatureConstants.LJZ_TABLE_NAME, fs_zrz.get(0), fs_ljz, new AiRunnable() {
                                    @Override
                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                        new FeatureView().queryChildFeature(FeatureConstants.H_TABLE_NAME, fs_zrz.get(0), fs_h, new AiRunnable() {
                                            @Override
                                            public <T_> T_ ok(T_ t_, Object... objects) {
                                                FeatureEditBDC.LoadAllCAndFsToH(mapInstance, fs_zrz.get(0), FeatureHelper.Get(fs_c.get(0), "SJC", ""), fs_c_all, new AiRunnable() {
                                                    @Override
                                                    public <T_> T_ ok(T_ t_, Object... objects) {
                                                        Feature f_c=null;
                                                        final List<Feature> fs_all=new ArrayList<>();
                                                        for (Feature feature : fs_c_all.keySet()) {
                                                            f_c=feature;
                                                            fs_all.addAll(fs_c_all.get(feature));
                                                        }
                                                        new AiForEach<Feature>(fs_all,callback){
                                                            @Override
                                                            public void exec() {
                                                                String orid=FeatureHelper.Get(fs_all.get(postion),"ORID","");
                                                                MapHelper.Query(GetTable(mapInstance, FeatureConstants.FTQK_TABLE_NAME)
                                                                        , StringUtil.WhereByIsEmpty(orid) + " FTQX_ID= '" +orid+ "' ", -1, fs_ftqk, getNext());

                                                            }
                                                        }.start();


                                                        return null;
                                                    }
                                                });



                                                return null;
                                            }
                                        });
                                        return null;
                                    }
                                });
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

    public static void CreateDOCX(final MapInstance mapInstance, final String bdcdyh, final Feature f_zd,
                                  final List<Feature> fs_jzd,
                                  final List<Feature> fs_jzx,
                                  final Map<String, Feature> map_jzx,
                                  final List<Map<String, Object>> fs_jzqz,
                                  final List<Feature> fs_zrz,
                                  final List<Feature> fs_ljz,
                                  final List<Feature> fs_h,
                                  final LinkedHashMap<Feature, List<Feature>> fs_c,
                                  boolean isRelaod, final AiRunnable callback) {
        String file_dcb_doc = FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh);
        if (FileUtils.exsit(file_dcb_doc) && !isRelaod) {
            Log.i(TAG, "生成资料: 已经存在跳过");
            AiRunnable.Ok(callback, file_dcb_doc);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String zddm = FeatureHelper.Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, "");
                        Map<String, Object> map_ = new LinkedHashMap<>();
                        //  设置系统参数
                        FeatureEditBDC.Put_data_sys(map_);
                        //  设置宗地参数
                        FeatureEditBDC.Put_data_zd(mapInstance, map_, bdcdyh, f_zd);
                        // 界址签字
                        FeatureEditBDC.Put_data_jzqz(map_, fs_jzd, fs_jzqz);
                        // 界址点
                        FeatureEditBDC.Put_data_jzdx(mapInstance, map_, zddm, fs_jzd, fs_jzx, map_jzx);
                        // 设置界址线
                        FeatureEditBDC.Put_data_jzx(mapInstance, map_, fs_jzx);
                        // 自然幢
//                        FeatureEditBDC.Put_data_zrz(mapInstance, map_, bdcdyh, f_zd, fs_zrz, fs_z_fsjg, fs_h);
                        // 在全局放所有户
                        //  Put_data_hs(mapInstance, map_, fs_h);
                        // 在全局放一个户
                        FeatureEditBDC.Put_data_h(mapInstance, map_, fs_h);
                        // 在全局放一个幢
                        FeatureEditBDC.Put_data_zrz(mapInstance, map_, fs_zrz);
                        // 在全局放一所以的户
                        // 宗地草图
                        FeatureEditBDC.Put_data_zdct(mapInstance, map_, f_zd);
                        // 附件材料
                        FeatureEditBDC.Put_data_fjcl(mapInstance, map_, f_zd);

                        final String templet = FileUtils.getAppDirAndMK(FeatureEditBDC.GetPath_Templet()) + "不动产地籍调查表.docx";
                        final String file_dcb_doc = FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh);
                        String file_zd_zip = FeatureEditBDC.GetPath_ZD_zip(mapInstance, f_zd);
                        if (FileUtils.exsit(templet)) {
                            ReportUtils.exportWord(templet, file_dcb_doc, map_);
                            // 资料已经发生改变，移除压缩包
                            FileUtils.deleteFile(file_zd_zip);
                            Log.i(TAG, "生成资料: 生成完成");
                            AiRunnable.U_Ok(mapInstance.activity, callback, file_dcb_doc);
                        } else {
                            ToastMessage.Send("《不动产权籍调查表》模板文件不存在！");
                            AiRunnable.U_No(mapInstance.activity, callback, null);
                        }
                    } catch (Exception es) {
                        Log.i(TAG, "生成资料: 生成失败");
                        ToastMessage.Send("生成《不动产权籍调查表》失败", es);
                        AiRunnable.U_No(mapInstance.activity, callback, null);
                    }
                }
            }).start();
        }
    }

    public static void OutputData(final MapInstance mapInstance,
                                  final Feature feature_bdc,
                                  final Feature f_zd,
                                  final List<Feature> fs_jzd,
                                  final List<Feature> fs_jzx,
                                  final List<Feature> fs_zrz,
                                  final List<Feature> fs_h,
                                  final List<Feature> fs_c,
                                  final List<Feature> fs_ftqk,
                                  final LinkedHashMap<Feature,List<Feature>> fs_c_all
    ) {
        try {
            String bdcdyh=FeatureHelper.Get(feature_bdc,FeatureHelper.TABLE_ATTR_BDCDYH,"");
            final String file_dcb = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/") + "不动产地籍调查表" + bdcdyh + ".docx";
            FileUtils.copyFile(FeatureEditBDC.GetPath_BDC_doc(mapInstance, bdcdyh), file_dcb);
            // 导出shp 文件
            final String shpfile_zd = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + "宗地" + ".shp";
            ShapeUtil.writeShp(shpfile_zd, f_zd);
            final String shpfile_jzd = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + "界址点" + ".shp";
            ShapeUtil.writeShp(shpfile_jzd, fs_jzd);
            final String shpfile_jzx = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + "界址线" + ".shp";
            ShapeUtil.writeShp(shpfile_jzx, fs_jzx);
            final String shpfile_zrz = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + "自然幢" + ".shp";
            ShapeUtil.writeShp(shpfile_zrz, fs_zrz);
            final String shpfile_h = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + "户" + ".shp";
            ShapeUtil.writeShp(shpfile_h, fs_h);
//            final String shpfile_zfsjg = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + "幢附属结构" + ".shp";
//            ShapeUtil.writeShp(shpfile_zfsjg, fs_z_fsjg);
//            final String shpfile_hfsjg = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/shp/") + mapInstance.getId(f_zd) + "户附属结构" + ".shp";
//            ShapeUtil.writeShp(shpfile_hfsjg, fs_h_fsjg);
            Feature f_c=null;
            List<Feature> fs_all=new ArrayList<>();
            for (Feature feature : fs_c_all.keySet()) {
                f_c=feature;
                fs_all=fs_c_all.get(feature);
            }
            final String dxf_fcfht = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(feature_bdc) + "附件材料/") + bdcdyh + "分层分户图.dxf"; //20180709
//            new DxfFcfcfht_c(mapInstance).set(dxf_fcfht).set(feature_bdc, f_zd, fs_zrz.get(0),fs_ftqk,f_c,fs_all).write().save();
        } catch (Exception es) {
            Log.e(TAG, "导出数据失败", es);
        }
    }

    ///endregion

    //region 私有函数
    //层设定不动产单元
    private void addBdcdy() {
        final AiDialog aiDialog = AiDialog.get(activity).setHeaderView(R.mipmap.app_icon_more_blue, "不动产单元设定");
        String oridPath = FeatureHelper.Get(feature, FeatureHelper.TABLE_ATTR_ORID_PATH, "");

        if (StringUtil.IsNotEmpty(oridPath)||!oridPath.contains(FeatureHelper.TABLE_NAME_ZRZ)) {
            fv.checkBdcdy(feature, new AiRunnable() {
                @Override
                public <T_> T_ ok(T_ t_, Object... objects) {
                    if (t_ != null) {
                        //可以设定不动产单元
                        aiDialog.addContentView("确定要生成一个不动产单元吗?", "该操作将根据宗地与该层共同设定一个不动产单元！");
                        aiDialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                // 加载界面
                                fv.createBdcdyFromC(feature, new AiRunnable() {
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
        } else {
            aiDialog.addContentView("不能设定不动产单元", "自然幢层没有与自然幢关联！");
            aiDialog.setFooterView(AiDialog.CENCEL, AiDialog.COMFIRM, null).show();
        }

    }

    private void load_bdcdy() {
        if (view_bdcdy == null) {
            ViewGroup bdcdy_view = (ViewGroup) view.findViewById(R.id.ll_bdcdy_list);
            bdcdy_view.setTag(null); //强制重新生成adapter
            mapInstance.newFeatureView("QLRXX").buildListView(bdcdy_view,fv.queryChildWhere());
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
    /**
     * 拷贝楼层
     * @param mapInstance
     * @param f_ljz
     * @param lc
     * @param callback
     */
    private  static void copylc(final MapInstance mapInstance, final Feature f_ljz, final String lc, final AiRunnable callback) {
        String desc = "该操作主要是将选中的层，复制到新的楼层，请谨慎处理！";
        final AiDialog aidialog = AiDialog.get(mapInstance.activity);
        aidialog.setHeaderView(R.mipmap.app_icon_warning_red, "不可逆操作提醒")
                .addContentView("确定要复制么？", desc)
                .setFooterView(AiDialog.COMFIRM, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        final Map<String, Object> map = new LinkedHashMap<>();
                        final int lc_f = AiUtil.GetValue(lc, 1);
                        map.put("szc", (lc_f + 1) + "");
                        aidialog.setContentView("请输入需要复制到的层数，多层使用“,”、“-”隔开：如：3,5,7 或是 2-7 层")
                                .addContentView("如：3,5,7 将复制到3、5、7层")
                                .addContentView("如：3-7 将复制到3、4、5、6、7层")
                                .addContentView(aidialog.getEditView("请输入要复制到的楼层", map, "szc"))
                                .setFooterView(AiDialog.COMFIRM, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String szc = map.get("szc") + "";
                                        final List<Integer> nubs = StringUtil.GetNumbers(szc);
                                        aidialog.setContentView("输入的楼层为“" + szc + "”：将复制到" + StringUtil.Join(nubs) + "层");
                                        aidialog.setFooterView(AiDialog.COMFIRM, AiDialog.COMFIRM, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    final List<Feature> fs = new ArrayList<>();
                                                    FeatureViewLJZ.LoadH_And_Fsjg(mapInstance, f_ljz, lc, fs, fs, fs, new AiRunnable() {
                                                        @Override
                                                        public <T_> T_ ok(T_ t_, Object... objects) {
                                                            List<Feature> fs_save = new ArrayList<>();
                                                            for (int szc : nubs) {
                                                                for (Feature f : fs) {
                                                                    // 复制户、幢附属
                                                                    Feature f_ = FeatureHelper.Copy(f);
                                                                    f_.setGeometry(f.getGeometry());
                                                                    FeatureHelper.Set(f_, "SZC", szc);
                                                                    FeatureHelper.Set(f_, "LC", szc);
                                                                    FeatureHelper.Set(f_, "CH", szc);
                                                                    // 将倒数地3、4位，替换位新的层
                                                                    FeatureHelper.Set(f_, "HH", text_replace_lc(FeatureHelper.Get(f, "HH", ""), lc_f, szc));
                                                                    FeatureHelper.Set(f_, "HID", text_replace_lc(FeatureHelper.Get(f, "HH", ""), lc_f, szc));
                                                                    FeatureHelper.Set(f_, "BDCDYH", text_replace_lc(FeatureHelper.Get(f, "BDCDYH", ""), lc_f, szc));
                                                                    FeatureHelper.Set(f_, "FWDM", text_replace_lc(FeatureHelper.Get(f, "FWDM", ""), lc_f, szc));

                                                                    if (f_.getFeatureTable().getTableName().equalsIgnoreCase(FeatureConstants.H_FSJG_TABLE_NAME)) {
                                                                        FeatureHelper.Set(f_, "ID", text_replace_lc(FeatureHelper.Get(f, "ID", ""), lc_f, szc, 8));
                                                                        FeatureHelper.Set(f_,"ORID_PATH","");
                                                                    } else if (f_.getFeatureTable().getTableName().equalsIgnoreCase(FeatureConstants.Z_FSJG_TABLE_NAME)){
                                                                        FeatureHelper.Set(f_, "ID", text_replace_lc(FeatureHelper.Get(f, "ID", ""), lc_f, szc));
                                                                    } else if (f_.getFeatureTable().getTableName().equalsIgnoreCase(FeatureConstants.H_TABLE_NAME)){
                                                                        FeatureHelper.Set(f_, "ID", text_replace_lc(FeatureHelper.Get(f, "ID", ""), lc_f, szc));
                                                                    }
                                                                    fs_save.add(f_);
                                                                }
                                                            }
                                                            mapInstance.fillFeature(fs_save);
                                                            MapHelper.saveFeature(fs_save, callback);
                                                            return null;
                                                        }
                                                    });
                                                } catch (Exception es) {
                                                    ToastMessage.Send("复制层失败！", es);
                                                }
                                                aidialog.dismiss();
                                            }
                                        });
                                    }
                                });

                    }
                }).show();
    }

    ///endregion

    //region 内部类或接口
    ///endregion


}
