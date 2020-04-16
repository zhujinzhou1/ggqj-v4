package com.ovit.app.map.bdc.ggqj.map.lzdb;

import android.util.Log;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.lzdb.bean.CalLayer;
import com.ovit.app.map.bdc.ggqj.map.lzdb.bean.JZD;
import com.ovit.app.map.bdc.ggqj.map.lzdb.bean.QLRXX;
import com.ovit.app.map.bdc.ggqj.map.lzdb.bean.STAnno;
import com.ovit.app.map.bdc.ggqj.map.lzdb.bean.STRegion;
import com.ovit.app.map.bdc.ggqj.map.lzdb.bean.SvMetadata;
import com.ovit.app.map.bdc.ggqj.map.lzdb.bean.TDoor;
import com.ovit.app.map.bdc.ggqj.map.lzdb.bean.TLayer;
import com.ovit.app.map.bdc.ggqj.map.lzdb.bean.ZD;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.GsonUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ovit.app.util.TypeUtil.TAG;
import static rx.Single.error;

/**
 * Created by Lichun on 2018/4/14.
 */

public class DbTemplet {
    public static final String ANGLE="ANGLE";
    public static final String FONT_WIDTH="FONT_WIDTH";
    public static final String FONT_SIZE="FONT_SIZE";
    public static final String COLOR="COLOR";
    public static final String FONT_STYLE="FONT_STYLE";
    public static final String TEXT_CONTENT="TEXT_CONTENT";

    //力智DB
    public static final String DB_C_CALLAYERS ="CALLAYERS";
    public static final String DB_SHAPE_STREGIONS ="STREGIONS";
    public static final String DB_TEXT_STANNOS ="STANNOS";
    public static final String DB_ZD ="DB_ZD";
    public static final String DB_JZD ="DB_JZD";
    public static final String DB_QLRXX ="DB_QLRXX";
    public static final String DB_SVMETADATA ="DB_SVMETADATA";
    public static final String DB_TDOOR = "DB_TDOOR";
    public static final String DB_TLAYER = "DB_TLAYER";
    public static Map<String,List> GetDbMap() {

        List<CalLayer> calLayers = new ArrayList<>();
        List<STRegion> sTRegions = new ArrayList<>();
        List<STAnno> sTAnnos = new ArrayList<>();
        List<SvMetadata> svMetadata = new ArrayList<>();
        List<TDoor> tDoors = new ArrayList<>();
        List<ZD> zds = new ArrayList<>();
        List<QLRXX> qlr = new ArrayList<>();
        List<JZD> jzd = new ArrayList<>();
        List<TLayer> tLayers = new ArrayList<>();
        Map<String,List> dbMap = new HashMap<>();
        dbMap.put(DB_C_CALLAYERS,calLayers);
        dbMap.put(DB_SHAPE_STREGIONS,sTRegions);
        dbMap.put(DB_TEXT_STANNOS,sTAnnos);
        dbMap.put(DB_ZD,zds);
        dbMap.put(DB_JZD,jzd);
        dbMap.put(DB_QLRXX,qlr);
        dbMap.put(DB_SVMETADATA,svMetadata);
        dbMap.put(DB_TDOOR,tDoors);
        dbMap.put(DB_TLAYER,tLayers);
        return dbMap;
    }
    public static int GetClassId(Feature f) {
        if (FeatureHelper.isPolygonFeatureValid(f)) {
            String tableName = f.getFeatureTable().getTableName();
            if (FeatureHelper.TABLE_NAME_H.equals(tableName)) {
                return 280230;
            } else if (tableName.contains("FSJG")) {
                String fhmc = FeatureHelper.Get(f, "FHMC", "");
                if (fhmc.contains("阳台")) {
                    double type = FeatureHelper.Get(f, "TYPE", 0.0);
                    if (type == 0.5d) {
                        return 221230;
                    } else if (type == 1d) {
                        return 222230;
                    }
                } else {
                    return 280231;
                }
            }
            return 0;
        } else {
            return 0;
        }
    }

    public static String GetStAnnoGeometry(Point point, String textContent, double angle, float fontWidth
            , double fontSize, int color, String fontStyle, int locateMode) {
        String gText = "20191022,3,"
                + point.getX() + "," + (point.getY())//定位点X,定位点Y
                + "," + locateMode                //定位方式
                + "," + angle              //旋转角度
                + "," +fontWidth        //字宽
                + "," + fontSize               //字高
                + "," + 0              //是否斜体
                + "," + 0               //是否粗体
                + "," + 0                //是否下划线
                + "," +color                  //颜色Red,Green,Blue
                + "," + fontStyle             //字体名称
                + "," + textContent;           //文字内容
        return gText;
    }

    public static STAnno GetStAnno(Point point,Map<String, List> dbMap) {
        if (dbMap != null) {
            STAnno sTAnno = new STAnno();
            String gText = "20191022,3,"
                    + point.getX() + "," + (point.getY())//定位点X,定位点Y
                    + "," + "1"                //定位方式
                    + "," + "[#"+ANGLE+"#]"              //旋转角度
                    + "," + "[#"+FONT_WIDTH+"#]"        //字宽
                    + "," + "[#"+FONT_SIZE+"#]"                //字高
                    + "," + 0              //是否斜体
                    + "," + 0               //是否粗体
                    + "," + 0                //是否下划线
                    + "," + "[#"+COLOR+"#]"                  //颜色Red,Green,Blue
                    + "," + "[#"+FONT_STYLE+"#]"             //字体名称
                    + "," +"[#"+TEXT_CONTENT+"#]";           //文字内容
            gText = gText.replace("[#"+ANGLE+"#]", "0");

            sTAnno.Geometry = gText;
            dbMap.get(DB_TEXT_STANNOS).add(sTAnno);
            return sTAnno;
        }
        return null;
    }

    public static STRegion GetSTRegion(Feature feature, Geometry g) {
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
        }

        STRegion sTRegion = new STRegion();
        String feaName = feature.getFeatureTable().getTableName();
        if(feaName.equals(FeatureHelper.TABLE_NAME_H_FSJG) && FeatureHelper.Get(feature, "TYPE", 0.0)==0.5 ){
            if(!FeatureHelper.Get(feature,"FHMC","").contains("阳台")){
                sTRegion.Area_Coeff = FeatureHelper.Get(feature, "TYPE", 0.0) ;
            }
        }
      //  sTRegion.Basic_Layer = FeatureHelper.Get(feature, "SZC", 0);
        sTRegion.Basic_Layer = feaName.equals(FeatureHelper.TABLE_NAME_H) ? FeatureHelper.Get(feature, "CH", 0) : FeatureHelper.Get(feature, "LC", 0);
        sTRegion.Geometry = text;
        sTRegion.ClassID = DbTemplet.GetClassId(feature);
        sTRegion.Layer_NUM =  1;
        sTRegion.SmUserID = feaName.equals(FeatureHelper.TABLE_NAME_H) ? FeatureHelper.Get(feature,"CH",1) :  FeatureHelper.Get(feature, "LC", 0);
        return sTRegion;
    }

    public static List<CalLayer> GetalLayer(ArrayList<Map.Entry<String, List<Feature>>> fs_map_croup) {
        List<CalLayer> calLayers = null;
        if (fs_map_croup != null && !fs_map_croup.isEmpty()) {
            calLayers = new ArrayList<>();
            for (Map.Entry<String, List<Feature>> map : fs_map_croup) {
                String key = map.getKey();
                int lc = Integer.parseInt(key);
                CalLayer calLayer = new CalLayer();
                calLayer.SmUserID = 0;
                calLayer.Layer_NUM = lc;
                calLayer.Kc = 0;
                calLayer.REGION_NUM = 1;
                calLayer.Basic_Layer = lc;
                calLayer.Layer_Name = key + "层";
                calLayer.Layer_Height = 0;
                calLayers.add(calLayer);
            }

        }
        return calLayers;
    }

    public static List<SvMetadata> GetSvMetadata(List<Feature> fs_zrz, Feature f_zd, MapInstance mapInstance) {
        List<SvMetadata> svMetadatas = null;
        if (fs_zrz != null && !fs_zrz.isEmpty()) {
            svMetadatas = new ArrayList<>();
            for (Feature f_zrz : fs_zrz) {
                SvMetadata svMetadata = new SvMetadata();
                String data = FeatureHelper.Get(f_zrz,"JGRQ","20201");
                svMetadata.SmUserID = 0;
                svMetadata.FW_ADDRESS = FeatureHelper.Get(f_zd,"ZL","");
                svMetadata.FILE_CODE =  FeatureHelper.Get(f_zd,"ZDDM","") + "F"+ FeatureHelper.Get(f_zrz,"ZH","");
                svMetadata.XQ_NAME = FeatureHelper.Get(f_zd,"XMMC","");
                svMetadata.MAPID = FeatureHelper.Get(f_zd,"TFH","");
                svMetadata.Auditing = GsonUtil.GetValue(mapInstance.aiMap.JsonData,"SHR","");
                svMetadata.SurveyCorp = GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZDW","");
                svMetadata.First_Surveyer = GsonUtil.GetValue(mapInstance.aiMap.JsonData,"HZR","");
                svMetadata.DESIGN_YT = FeatureHelper.Get(f_zrz,"JZWJBYT",10);
                svMetadata.TOTAL_JZAREA = FeatureHelper.Get(f_zrz,"SCJZMJ",0.00);
                svMetadata.TDYT =  FeatureHelper.Get(f_zd,"PZYT","072");
                svMetadata.TD_ZDMJ = FeatureHelper.Get(f_zd,"ZDMJ",0.00);
                svMetadata.TDQLXZ = 200;
                svMetadata.BIRTH_DATE = Integer.parseInt(data.substring(0,4));
                svMetadata.First_Auditing = FeatureHelper.Get(f_zd,"JCZ","");
                svMetadata.STRUCT = FeatureHelper.Get(f_zrz,"FWJG",4);
                svMetadata.ZH_SG = FeatureHelper.Get(f_zd,"ZDDM","");
                svMetadata.MAPID = "F"+ FeatureHelper.Get(f_zrz,"ZH","");
                svMetadata.FLOOR_NUM = FeatureHelper.Get(f_zrz,"ZCS","");
                svMetadata.BUILDER = FeatureHelper.Get(f_zd,"QLRXM","");
                svMetadata.SURVEY_DATE = FeatureHelper.Get(f_zd,"ZLRQ","2020-01-01").substring(0,10);
                svMetadata.FW_CB =  FeatureHelper.Get(f_zd,"QLLX",6);
                svMetadata.TOTAL_TNAREA = FeatureHelper.Get(f_zrz,"SCJZMJ",0.00);
                svMetadata.TOTAL_DSJZAREA = FeatureHelper.Get(f_zrz,"SCJZMJ",0.00);
                svMetadata.ZD_AREA = FeatureHelper.Get(f_zrz,"ZZDMJ",0.00);
                svMetadatas.add(svMetadata);
            }

        }
        return svMetadatas;
    }

    public static List<TDoor> GetTDoor(List<Feature> fs_h, Feature f_zd, List<Feature> h_fsjg) {
        List<TDoor> tDoors = null;
        if (fs_h != null && !fs_h.isEmpty()) {
            tDoors = new ArrayList<>();
            for (Feature f_h : fs_h) {
                TDoor tDoor = new TDoor();
                Double ytMj = 0.00;
                for(Feature fsjg:h_fsjg){
                    if(FeatureHelper.Get(fsjg,"LC","").equalsIgnoreCase(FeatureHelper.Get(f_h,"CH",""))){
                        ytMj = ytMj + FeatureHelper.Get(fsjg,"HSMJ",0.00);
                    }
                }
                tDoor.YT_AREA = ytMj;
                tDoor.SmUserID =  FeatureHelper.Get(f_h,"CH",1);
                tDoor.Right_NUMEX = FeatureHelper.Get(f_h,"HH","");
                tDoor.FW_LAYERS =  FeatureHelper.Get(f_h,"CH","1");
    //            tDoor.ZH = FeatureHelper.Get(f_h,"ZRZH","");
                tDoor.STRUCT = FeatureHelper.Get(f_h,"FWJG",4);
                tDoor.FW_ADDRESS =  FeatureHelper.Get(f_zd,"ZL","") + FeatureHelper.Get(f_h,"HH","");
                tDoor.GHYT = FeatureHelper.Get(f_h,"GHYT","宅基地");
                tDoor.FW_CB = FeatureHelper.Get(f_h,"CB",6);
                tDoor.WALL_EAST = getQtgs(FeatureHelper.Get(f_h,"QTGSD",""));
                tDoor.WALL_SOUTH = getQtgs(FeatureHelper.Get(f_h,"QTGSN",""));
                tDoor.WALL_WEST = getQtgs(FeatureHelper.Get(f_h,"QTGSX",""));
                tDoor.WALL_NORTH = getQtgs(FeatureHelper.Get(f_h,"QTGSB",""));
                tDoor.Layer_NUM = FeatureHelper.Get(f_h,"CH",1);
                tDoor.DOOR_NUM = FeatureHelper.Get(f_h,"HH","");
                tDoor.DOORNUM_SORT = Integer.parseInt(FeatureHelper.Get(f_h,"HH",""));
                tDoor.FACT_YT = FeatureHelper.Get(f_h,"FWLX",1);
                tDoor.DESIGN_YT = FeatureHelper.Get(f_h,"FWXZ",10);
                tDoor.TNJZ_AREA = FeatureHelper.Get(f_h,"YCJZMJ",0.00);
                tDoor.JZ_AREA = FeatureHelper.Get(f_h,"YCJZMJ",0.00);
                tDoor.FW_LAYERSEX = FeatureHelper.Get(f_h,"CH","1");
                tDoor.MAP_ID = FeatureHelper.Get(f_h,"CH",1);
                tDoor.CQLY = FeatureHelper.Get(f_h,"CQLY","自建");
                tDoor.ZJD_AREA = FeatureHelper.Get(f_h,"YCQTJZMJ",0.00);
                tDoors.add(tDoor);
            }

        }
        return tDoors;
    }

    public static List<TLayer> GetTLayer(List<Feature> fs_h, List<Feature> h_fsjg) {
        List<TLayer> tLayers = null;
        if (fs_h != null && !fs_h.isEmpty()) {
            tLayers = new ArrayList<>();
            for (Feature f_h : fs_h) {
                TLayer tLayer = new TLayer();
                Double ytMj = 0.00;
                for(Feature fsjg:h_fsjg){
                    if(FeatureHelper.Get(fsjg,"LC","").equalsIgnoreCase(FeatureHelper.Get(f_h,"CH",""))){
                        ytMj = ytMj + FeatureHelper.Get(fsjg,"HSMJ",0.00);
                    }
                }
                tLayer.YT_AREA = ytMj;
                tLayer.SmUserID = FeatureHelper.Get(f_h, "CH", 1);
                tLayer.Basic_Layer =FeatureHelper.Get(f_h, "CH", 1);
                tLayer.Layer_NUM = FeatureHelper.Get(f_h, "CH", 1);
                tLayer.ZH = FeatureHelper.Get(f_h, "ZRZH", "");
                tLayer.Layer_Name = FeatureHelper.Get(f_h, "CH", "1") + "层";
                tLayer.TNJZ_AREA = FeatureHelper.Get(f_h, "YCJZMJ", 0.00);
                tLayer.JZ_AREA = FeatureHelper.Get(f_h, "YCJZMJ", 0.00);
                tLayer.Layer_Count = 1;
                tLayers.add(tLayer);
            }
        }
        return tLayers;
    }
    public static ZD GetDbZds(Feature f) {
        try {
            // 创建要素
            ZD zd = new ZD();
            Class zdClazz = zd.getClass();
            for (com.esri.arcgisruntime.data.Field field : f.getFeatureTable().getFields()) {
                String key = field.getName();
                Object value = f.getAttributes().get(field.getName());
                try {
                    if (value == null) {
                        continue;
                    }
                    Field mField = zdClazz.getField(key);
                    boolean accessible = mField.isAccessible();
                    mField.setAccessible(true);
                    if (com.esri.arcgisruntime.data.Field.Type.INTEGER.equals(field.getFieldType())) {
                        mField.set(zd, AiUtil.GetValue(value, 0));
                    } else if (com.esri.arcgisruntime.data.Field.Type.OID.equals(field.getFieldType())) {
                        mField.set(zd, AiUtil.GetValue(value, ""));
                    } else if (com.esri.arcgisruntime.data.Field.Type.GLOBALID.equals(field.getFieldType())) {
                        mField.set(zd, AiUtil.GetValue(value, ""));
                    } else if (com.esri.arcgisruntime.data.Field.Type.DOUBLE.equals(field.getFieldType())) {
                        mField.set(zd, AiUtil.GetValue(value, 0d));
                    } else if (com.esri.arcgisruntime.data.Field.Type.FLOAT.equals(field.getFieldType())) {
                        mField.set(zd, AiUtil.GetValue(value, 0f));
                    } else if (com.esri.arcgisruntime.data.Field.Type.DATE.equals(field.getFieldType())) {
                          /*  Date date = AiUtil.GetValue(value, (Date) null);
                            if (date != null) {
                                oFeature.SetField(field.getName(), date.getYear(), date.getMonth(), date.getDay(), date.getHours(), date.getMinutes(), date.getSeconds(), 0);
                            }*/
//
                        mField.set(zd, AiUtil.GetValue(value,"2020-01-01").substring(0,10));
                    } else {
                        mField.set(zd, value);
//                        Log.e(TAG, "跳过字段值: [" + key + ":" + value + "]");
                    }
                    mField.setAccessible(accessible);

                } catch (Exception es) {
                    Log.e(TAG, "设置字段值: [" + field.getFieldType() + ":" + key + ":" + value + "]" + es.getMessage());
                }
            }
            return zd;
        } catch (Exception es) {
            error(es);
            return null;
        }
    }

    public static List<JZD> GetDbJzd(List<Feature> fs_jzd) {
        try {
            List<JZD> jzdList = new ArrayList<>();
            // 创建要素
            for(Feature f:fs_jzd) {
                JZD jzd = new JZD();
                //   String log="";
                Class jzdClazz = jzd.getClass();
                for (com.esri.arcgisruntime.data.Field field : f.getFeatureTable().getFields()) {
                    String key = field.getName();
                    Object value = f.getAttributes().get(field.getName());
                    //     log+=field.getName()+":"+field.getFieldType()+"\n";
                    try {
                        if (value == null) {
                            continue;
                        }
                        Field mField = jzdClazz.getField(key);
                        boolean accessible = mField.isAccessible();
                        mField.setAccessible(true);
                        if (com.esri.arcgisruntime.data.Field.Type.INTEGER.equals(field.getFieldType())) {
                            mField.set(jzd, AiUtil.GetValue(value, 0));
                        } else if (com.esri.arcgisruntime.data.Field.Type.OID.equals(field.getFieldType())) {
                            mField.set(jzd, AiUtil.GetValue(value, ""));
                        } else if (com.esri.arcgisruntime.data.Field.Type.GLOBALID.equals(field.getFieldType())) {
                            mField.set(jzd, AiUtil.GetValue(value, ""));
                        } else if (com.esri.arcgisruntime.data.Field.Type.DOUBLE.equals(field.getFieldType())) {
                            mField.set(jzd, AiUtil.GetValue(value, 0d));
                        } else if (com.esri.arcgisruntime.data.Field.Type.FLOAT.equals(field.getFieldType())) {
                            mField.set(jzd, AiUtil.GetValue(value, 0f));
                        } else if (com.esri.arcgisruntime.data.Field.Type.DATE.equals(field.getFieldType())) {
                            /*Date date = AiUtil.GetValue(value, (Date) null);
                            if (date != null) {
                                oFeature.SetField(field.getName(), date.getYear(), date.getMonth(), date.getDay(), date.getHours(), date.getMinutes(), date.getSeconds(), 0);
                            }*/
//
                        } else {
                            mField.set(jzd, value);
//                        Log.e(TAG, "跳过字段值: [" + key + ":" + value + "]");
                        }
                        mField.setAccessible(accessible);

                    } catch (Exception es) {
                        Log.e(TAG, "设置字段值: [" + field.getFieldType() + ":" + key + ":" + value + "]" + es.getMessage());
                    }
                }
                jzdList.add(jzd);
                //       Log.d("Templet",log);
            }
            return jzdList;
        } catch (Exception es) {
            error(es);
            return null;
        }
    }
    public static List<QLRXX> GetDbQlrxx(List<Feature> fs_hjxx) {
        try {
            List<QLRXX> qlrList = new ArrayList<>();
            // 创建要素
            for(Feature f:fs_hjxx) {
                QLRXX qlr = new QLRXX();
             //   String log="";
                Class qlrClazz = qlr.getClass();
                for (com.esri.arcgisruntime.data.Field field : f.getFeatureTable().getFields()) {
                    String key = field.getName();
                    Object value = f.getAttributes().get(field.getName());
               //     log+=field.getName()+":"+field.getFieldType()+"\n";
                    try {
                        if (value == null) {
                            continue;
                        }
                        Field mField = qlrClazz.getField(key);
                        boolean accessible = mField.isAccessible();
                        mField.setAccessible(true);
                        if (com.esri.arcgisruntime.data.Field.Type.INTEGER.equals(field.getFieldType())) {
                            mField.set(qlr, AiUtil.GetValue(value, 0));
                        } else if (com.esri.arcgisruntime.data.Field.Type.OID.equals(field.getFieldType())) {
                            mField.set(qlr, AiUtil.GetValue(value, ""));
                        } else if (com.esri.arcgisruntime.data.Field.Type.GLOBALID.equals(field.getFieldType())) {
                            mField.set(qlr, AiUtil.GetValue(value, ""));
                        } else if (com.esri.arcgisruntime.data.Field.Type.DOUBLE.equals(field.getFieldType())) {
                            mField.set(qlr, AiUtil.GetValue(value, 0d));
                        } else if (com.esri.arcgisruntime.data.Field.Type.FLOAT.equals(field.getFieldType())) {
                            mField.set(qlr, AiUtil.GetValue(value, 0f));
                        } else if (com.esri.arcgisruntime.data.Field.Type.DATE.equals(field.getFieldType())) {
                            /*Date date = AiUtil.GetValue(value, (Date) null);
                            if (date != null) {
                                oFeature.SetField(field.getName(), date.getYear(), date.getMonth(), date.getDay(), date.getHours(), date.getMinutes(), date.getSeconds(), 0);
                            }*/
//
                        } else {
                            mField.set(qlr, value);
//                        Log.e(TAG, "跳过字段值: [" + key + ":" + value + "]");
                        }
                        mField.setAccessible(accessible);
                    } catch (Exception es) {
                        Log.e(TAG, "设置字段值: [" + field.getFieldType() + ":" + key + ":" + value + "]" + es.getMessage());
                    }
                }
                qlrList.add(qlr);
         //       Log.d("Templet",log);
            }
            return qlrList;
        } catch (Exception es) {
            error(es);
            return null;
        }
    }

    public static Integer getQtgs(String qtgs){
        int i = 1;
        String zq = "自墙";
        String gq = "共墙";
        if(!qtgs.isEmpty()){
            if(qtgs.equalsIgnoreCase(zq)){
                i=1;
            } else if(qtgs.equalsIgnoreCase(gq)){
                i= 2;
            } else {
                i=3;
            }
        }
        return i;
    }
}
