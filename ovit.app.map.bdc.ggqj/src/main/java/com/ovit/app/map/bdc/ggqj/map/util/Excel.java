package com.ovit.app.map.bdc.ggqj.map.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.ovit.app.map.bdc.ggqj.map.MapInstance;
import com.ovit.app.map.bdc.ggqj.map.constant.FeatureConstants;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.map.custom.MapHelper;
import com.ovit.app.util.AiForEach;
import com.ovit.app.util.AiRunnable;
import com.ovit.app.util.AiUtil;
import com.ovit.app.util.DicUtil;
import com.ovit.app.util.FileUtils;
import com.ovit.app.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Created by 此生无分起相思 on 2018/7/9.
 */

public class Excel
{
    public static WritableFont arial14font = null;
    public static WritableCellFormat arial14format = null;
    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;
    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;

    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";
    private String name;
    private static String fwdh="";
    private static String jdx;

    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14,
                    WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);
            arial10font = new WritableFont(WritableFont.ARIAL, 10,
                    WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(jxl.format.Colour.LIGHT_BLUE);
            arial12font = new WritableFont(WritableFont.ARIAL, 12);
            arial12format = new WritableCellFormat(arial12font);
            arial12format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN);
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    public static void initExcel(String fileName, String sheetName, String[] colName) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet(sheetName, 0);
            sheet.addCell((WritableCell) new Label(0, 0, fileName,
                    arial14format));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
            }
            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    public static void createSmtrautExcel(String fileName, String sheetName, String[] lineName, String[] colName, List<List<Map<String, Object>>> values) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            boolean isWrite = file.canWrite();
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet(sheetName, 0);
            int ran = 1;
            for (int j = 0; j < values.size(); j++) {
                sheet.setRowView(j * 6 + ran - 1, 600);
                for (int line = 0; line < lineName.length; line++) {
                    sheet.addCell(new Label(line + 1, j * 6 + ran, lineName[line], arial10format));
                }
                for (int col = 0; col < colName.length; col++) {
                    sheet.addCell(new Label(0, col + j * 6 + ran + 1, colName[col], arial10format));
                }

                for (int i = 0; i < values.get(j).size(); i++) {
                    Map<String, Object> m = values.get(j).get(i);
                    int col = (Integer) m.get("col");
                    int line = (Integer) m.get("line");
                    String value = (String) m.get("value");
                    if (col == 0 && line == 0) {
                        sheet.addCell(new Label(col, line + j * 6 + ran - 1, value, arial14format));
                        sheet.addCell(new Label(col, line + j * 6 + ran, "", arial10format));
                        sheet.mergeCells(col, line + j * 6 + ran - 1, col + lineName.length, line + j * 6 + ran - 1);
                    } else {
                        sheet.addCell(new Label(col, line + j * 6 + ran, value, arial10format));
                    }
                }
                ran++;
            }
            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public static void insertSmtrautDatas(WritableWorkbook workbook, String sheetName,
                                          String[] lineName, String[] colName, List<List<Map<String, Object>>> values) {
        try {
            WritableSheet sheet = workbook.getSheet(sheetName);
            if (sheet == null)
                return;

            for (int j = 0; j < values.size(); j++) {
                for (int line = 0; line < lineName.length; line++) {
                    sheet.addCell(new Label(line + j * 6, 0, colName[line], arial10format));
                }
                for (int col = 0; col < colName.length; col++) {
                    sheet.addCell(new Label(j * 6, col, colName[col], arial10format));
                }

                for (int i = 0; i < values.get(j).size(); i++) {
                    Map<String, Object> m = values.get(j).get(i);
                    sheet.addCell(new Label((Integer) m.get("line") + j * 6, (Integer) m.get("col"), (String) m.get("value"), arial10format));
                }
            }

            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void writeObjListToExcel(List<T> objList,
                                               String fileName, Context c) {
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(new File(fileName));
                Workbook workbook = Workbook.getWorkbook(in);
                writebook = Workbook.createWorkbook(new File(fileName),
                        workbook);
                WritableSheet sheet = writebook.getSheet(0);
                for (int j = 0; j < objList.size(); j++) {
                    ArrayList<String> list = (ArrayList<String>) objList.get(j);
                    for (int i = 0; i < list.size(); i++) {
                        sheet.addCell(new Label(i, j + 1, list.get(i),
                                arial12format));
                    }
                }
                writebook.write();
                Toast.makeText(c, "导出到手机存储中文件夹Family成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public static Object getValueByRef(Class cls, String fieldName) {
        Object value = null;
        fieldName = fieldName.replaceFirst(fieldName.substring(0, 1), fieldName
                .substring(0, 1).toUpperCase());
        String getMethodName = "get" + fieldName;
        try {
            Method method = cls.getMethod(getMethodName);
            value = method.invoke(cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void CreateSimpleExcel(Feature feature, String filePath, String sheetName) {
        format();
        List<String> values = new ArrayList<>();
        values.add("身高");
        values.add("年龄");
        values.add("体重");
        WritableWorkbook workbook = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            boolean isWrite = file.canWrite();
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet(sheetName, 0);
            WritableSheet sheet2 = workbook.createSheet(FeatureHelper.LAYER_NAME_ZRZ, 0);
            //5:单元格
            Label label = null;
            for (int i = 0; i < values.size(); i++) {
                //x,y,第一行的列名
                label = new Label(i, 0, values.get(i));
                //7：添加单元格
                sheet.addCell(label);
            }

            //8：模拟数据库导入数据
            for (int i = 1; i < 10; i++) {
                //添加编号，第二行第一列
                label = new Label(0, i, i + "");
                sheet.addCell(label);

                //添加账号
                label = new Label(1, i, "10010" + i);
                sheet.addCell(label);

                //添加密码
                label = new Label(2, i, "123456");
                sheet.addCell(label);
            }
            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 通过Feature 生成 exc
    public static void CreateSouthExcelToGdal( Feature feature, String filePath, List<List<Feature>> values) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            boolean isWrite = file.canWrite();
            workbook = Workbook.createWorkbook(file);
            Map<String, WritableSheet> wSheets = getAllWritableWorkbook_South(workbook);

            //创建sheetname
            for (List<Feature> fs : values) {
                if (fs.size() == 0) {
                    continue;
                }
                String Layername = fs.get(0).getFeatureTable().getTableName();
                if (FeatureHelper.TABLE_NAME_ZRZ.equals(Layername)) {
                    writeZRZ_South(wSheets, getLJZ_AttributeList_South(), fs);
                } else if (FeatureHelper.TABLE_NAME_ZD.equals(Layername)) {
                    writeZd_South(wSheets, getLJZ_AttributeList_South(), fs);
                } else if (FeatureHelper.TABLE_NAME_H.equals(Layername)) {
                    writeH_South(wSheets, getH_AttributeList_South(), fs);
                } else if ("QLR".equals(Layername)) {
                    writeQLR_South(wSheets, getGFR_AttributeList_South(), fs);
                }
            }
            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // region 南方不动产数据库
    private static void writeQLR_South(Map<String, WritableSheet> wSheets, List<String> gfr_attributeList, List<Feature> fs) throws WriteException {

        Label label = null;
        WritableSheet wsGFR = wSheets.get("购房人");
        for (Feature f : fs) {
            if (f == null) {
                continue;
            }
            int index = fs.indexOf(f);
            Iterator<Map.Entry<String, Object>> iterator = f.getAttributes().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                String value = AiUtil.GetValue(entry.getValue(), "");

                if ("XM".equals(key)) {
                    // 0 权利人名称
                    label = new Label(0, index + 1, value);
                } else if ("ZJZL".equals(key)) {
                    // 1 证件类别
                    label = new Label(1, index + 1, getValue("zjzl",key,value));
                } else if ("ZJH".equals(key)) {
//                    2 证件号码
                    label = new Label(2, index + 1, value);
                } else if ("FZJG".equals(key)) {
//                    3 发证机关
                    label = new Label(3, index + 1, value);
                } else if ("SSHY".equals(key)) {
//                    4 所属行业
                    label = new Label(4, index + 1, value);
                } else if ("GJ".equals(key)) {
//                    5 国家/地区
                    label = new Label(5, index + 1, value);
                } else if ("HJSZSS".equals(key)) {
//                    6 户籍所在省市
                    label = new Label(6, index + 1, value);
                } else if ("XB".equals(key)) {
//                    7 性别
                    label = new Label(7, index + 1, value);
                } else if ("XM".equals(key)) {
//                    8 联系人
                    label = new Label(8, index + 1, value);
                } else if ("DH".equals(key)) {
//                    9 联系电话
                    label = new Label(9, index + 1, value);
                } else if ("DZ".equals(key)) {
//                    10 通讯地址
                    label = new Label(10, index + 1, value);
                } else if ("YB".equals(key)) {
//                    11 邮编
                    label = new Label(11, index + 1, value);
                } else if ("GZDW".equals(key)) {
//                    12 工作单位
                    label = new Label(12, index + 1, value);
                } else if ("DZYJ".equals(key)) {
//                    13 电子邮件
                    label = new Label(13, index + 1, value);
                }
                /* write ZD
                else if ("TDZZSJ".equals(key)) {
                    // TODO something....
//                    14 法人名称
                    label = new Label(14, index + 1, value);
                } else if ("TDZZSJ".equals(key)) {
                    // TODO something....
//                    15 法人电话
                    label = new Label(15, index + 1, value);
                } else if ("TDZZSJ".equals(key)) {
                    // TODO something....
//                    16 代理人名称
                    label = new Label(16, index + 1, value);
                } else if ("TDZZSJ".equals(key)) {
                    // TODO something....
//                    17 代理人电话
                    label = new Label(17, index + 1, value);
                } else if ("TDZZSJ".equals(key)) {
                    // TODO something....
//                    18 代理机构
                    label = new Label(18, index + 1, value);
                } else if ("QLRLX".equals(key)) {
//                    19 权利人性质
                    label = new Label(19, index + 1, value);
                } else if ("TDZZSJ".equals(key)) {
                    // TODO something....
//                    20 权利面积
                    label = new Label(20, index + 1, value);
                }*/
                else if ("TDZZSJ".equals(key)) {
                    // TODO something....
//                    21 权利比例
                    label = new Label(21, index + 1, value);
                } else if ("GYFS".equals(key)) {
//                    22 共有方式
                    label = new Label(22, index + 1, value);
                } else if ("GYQK".equals(key)) {
//                    23 共有情况
                    label = new Label(23, index + 1, value);
                } /*else if ("BDCDYH ".equals(key)) {
//                    24 不动产单元号
                    label = new Label(24, index + 1, value);
                } */else if ("BDCQZH".equals(key)) {
//                    25 不动产权证号
                    label = new Label(25, index + 1, value);
                } else if ("BZ".equals(key)) {
//                    26 备注
                    label = new Label(26, index + 1, value);
                }
                /* write LJZ
                else if ("TDZZSJ".equals(key)) {
                    // TODO something...
//                    27 逻辑幢号
                    label = new Label(27, index + 1, value);
                }*/
                /*  write H
                 else if ("TDZZSJ".equals(key)) {
//                    28 层号
                    // TODO something...
                    label = new Label(28, index + 1, value);
                } else if ("TDZZSJ".equals(key)) {
//                    29 户号
                    // TODO something...
                    label = new Label(29, index + 1, value);
                } */
                else if ("TDZZSJ".equals(key)) {
                    // TODO something...
//                    30 合同编号
                    label = new Label(30, index + 1, value);
                }
                if (label != null) {
                    wsGFR.addCell(label);
                    label = null;
                }
            }
        }
    }

    private static void writeH_South(Map<String, WritableSheet> wSheets, List<String> h_attributeList, List<Feature> fs) throws WriteException {
        Label label = null;
        Label labelGFR = null;
        Label labelLJZ = null;
        Label labelC = null;
        WritableSheet wsH = wSheets.get(FeatureHelper.LAYER_NAME_H);
        WritableSheet wsGFR = wSheets.get("购房人");
        WritableSheet wsLJZ = wSheets.get("逻辑幢");
        WritableSheet wsC = wSheets.get("层");
        for (Feature f : fs) {
            if (f == null) {
                continue;
            }
            int index = fs.indexOf(f);
            Iterator<Map.Entry<String, Object>> iterator = f.getAttributes().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                String value = AiUtil.GetValue(entry.getValue(), "");
//               0 房屋编码
                if ("FWBM".equals(key)) {
                    // TODO something...
                    label = new Label(0, index + 1, value);
                }
                if ("QLRXM".equals(key)) {
                    // 0  GFR
                    labelGFR = new Label(0, index + 1, value);
                }
//               1 要素代码
                if ("YSDM".equals(key)) {
                    label = new Label(1, index + 1, "6001030140");
                }
//               2 不动产单元号
//                if ("BDCDYH".equals(key)) {
//                    label = new Label(2, index + 1, value);
//                }
//               3 自然幢号
//                if ("ZRZH".equals(key)) {
//                    label = new Label(3, index + 1, value);
//                }
//               4 逻辑幢号
                if ("LJZH".equals(key)) {
                    // TODO something...
                    label = new Label(4, index + 1, value);
                }
//               5 层号
                if ("CH".equals(key)) {
                    label = new Label(5, index + 1, value);
                    // GFR 28 层号
                    labelGFR = new Label(28, index + 1, value);
                }
//               6 坐落
                if ("ZL".equals(key)) {
                    label = new Label(6, index + 1, value);
                }
//               7 面积单位
                if ("MJDW".equals(key)) {
                    label = new Label(7, index + 1, value);
                }
//               8 实际层
                if ("SZC".equals(key)) {
                    label = new Label(8, index + 1, value);
                    labelC= new Label(4, index + 1, value);
                    wsC.addCell(labelC);
                    labelC= new Label(5, index + 1, value);
                    wsC.addCell(labelC);
                    labelC= new Label(0, index + 1, value);
                }
//               9 名义层
                if ("MYC".equals(key)) {
                    // TODO something...
                    label = new Label(9, index + 1, value);
                }
//               10 户号
                if ("HH".equals(key)) {
                    label = new Label(10, index + 1, value);
                    // GFR 29 户号
                    labelGFR = new Label(29, index + 1, value);
                }
//               11 户型
                if ("HX".equals(key)) {
                    label = new Label(11, index + 1, value);
                }
//               12 室号部位
                if ("SHBW".equals(key)) {
                    label = new Label(12, index + 1, value);
                }
//               13 户型结构
                if ("HXJG".equals(key)) {
                    label = new Label(13, index + 1, value);
                }
//               14 房屋用途
                if ("YT".equals(key)) {
                    label = new Label(14, index + 1, value);
                }
//               15 预测建筑面积
                if ("YCJZMJ".equals(key)) {
                    label = new Label(15, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                // 16 房屋用途
                if ("YT".equals(key)) {
                    labelLJZ = new Label(16, index + 1,  DicUtil.dic("fwyt", FeatureHelper.Get(f,"YT","")));
                }
//               16 预测套内建筑面积
                if ("YCTNJZMJ".equals(key)) {
                    label = new Label(16, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               17 预测分摊建筑面积
                if ("YCFTJZMJ".equals(key)) {
                    label = new Label(17, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               18 预测地下部分建筑面积
                if ("YCDXBFJZMJ".equals(key)) {
                    label = new Label(18, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               19 预测其它建筑面积
                if ("YCQTJZMJ".equals(key)) {
                    label = new Label(19, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               20 预测分摊系数
                if ("YCFTXS".equals(key)) {
                    label = new Label(20, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               21 实测建筑面积
                if ("SCJZMJ".equals(key)) {
                    label = new Label(21, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               22 实测套内建筑面积
                if ("SCTNJZMJ".equals(key)) {
                    label = new Label(22, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               23 实测分摊建筑面积
                if ("SCFTJZMJ".equals(key)) {
                    label = new Label(23, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               24 实测地下部分建筑面积
                if ("SCDXBFJZMJ".equals(key)) {
                    label = new Label(24, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               25 实测其它建筑面积
                if ("SCQTJZMJ".equals(key)) {
                    label = new Label(25, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               26 实测分摊系数
                if ("SCFTXS".equals(key)) {
                    label = new Label(26, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               27 共有土地面积
                if ("GYTDMJ".equals(key)) {
                    label = new Label(27, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               28 分摊土地面积
                if ("FTTDMJ".equals(key)) {
                    label = new Label(28, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               29 独用土地面积
                if ("DYTDMJ".equals(key)) {
                    label = new Label(29, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//               30 房屋类型
                if ("FWLX".equals(key)) {
                    label = new Label(30, index + 1, getValue("fwlx",key,value));
                }
//               31 房屋性质
                if ("FWXZ".equals(key)) {
                    label = new Label(31, index + 1, getValue("fwxz",key,value));
                }
//               32 房屋结构
                if ("FWJG".equals(key)) {
                    label = new Label(32, index + 1, getValue("fwjg",key,value));
                }
//               33 单元信息
                if ("DYXX".equals(key)) {
                    // TODO something
                    label = new Label(33, index + 1, value);
                }
//               34 单元号
                if ("DYH".equals(key)) {
                    // TODO something
                    label = new Label(34, index + 1, value);
                }
                /* write ZD
//               35 权利性质
                if ("QLXZ".equals(key)) {
                    label = new Label(35, index + 1, value);
                }
//               36 土地使用用途
                if ("TDYT".equals(key)) {
                    label = new Label(36, index + 1, value);
                }
//               37 土地起始时间
                if ("TDKSSJ".equals(key)) {
                    label = new Label(37, index + 1, AiUtil.GetValue(value, "", AiUtil.F_DATE));
                }
//               38 土地终止时间
                if ("TDJSSJ".equals(key)) {
                    label = new Label(38, index + 1, AiUtil.GetValue(value, "", AiUtil.F_DATE));
                }*/

                if (label != null) {
                    wsH.addCell(label);
                    label = null;
                }
                if (labelGFR != null) {
                    wsGFR.addCell(labelGFR);
                    labelGFR = null;
                }
                if (labelLJZ != null) {
                    wsLJZ.addCell(labelLJZ);
                    labelLJZ = null;
                }
                if (labelC != null) {
                    wsC.addCell(labelC);
                    labelC = null;
                }
            }
        }
    }

    private static void writeZd_South(Map<String, WritableSheet> wSheets, List<String> ljz_attributeList, List<Feature> fs) throws WriteException {
        Label label = null;
        Label labelGFR = null;
        Label labelH = null;
        WritableSheet wsLJZ = wSheets.get("逻辑幢");
        WritableSheet wsGFR = wSheets.get("购房人");
        WritableSheet wsH = wSheets.get(FeatureHelper.LAYER_NAME_H);
        for (Feature f : fs) {
            if (f == null) {
                continue;
            }
            int index = fs.indexOf(f);
            Iterator<Map.Entry<String, Object>> iterator = f.getAttributes().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                String value = AiUtil.GetValue(entry.getValue(), "");
                // 0 自然幢号
//                if ("ZRZH".equals(key)) {
//                    label = new Label(0, index + 1, value);
//                }
//                // 1 逻辑幢号
//                if ("LJZH".equals(key)) {
//                    label = new Label(1, index + 1, value);
//                }
//                // 2 要素代码
//                if ("YSDM".equals(key)) {
//                    label = new Label(2, index + 1, value);
//                }
                // 3坐落
                if ("ZL".equals(key)) {
                    label = new Label(3, index + 1, value);
                }
                // 4门牌号
                else if ("MPHM".equals(key)) {
                    label = new Label(4, index + 1, value);
                }
//                // 5宗地代码
//                if (FeatureHelper.TABLE_ATTR_ZDDM.equals(key)) {
//                    label = new Label(5, index + 1, value);
//                }
                // 6 小区名称
                else if ("LPMC".equals(key)) {
                    label = new Label(6, index + 1, value);
                } else if ("FZRXM".equals(key)) {
//                    14 GFR 法人名称
                    labelGFR = new Label(14, index + 1, value);
                } else if ("FZRDH".equals(key)) {
//                    15 GFR 法人电话
                    labelGFR = new Label(15, index + 1, value);
                } else if ("DLRXM".equals(key)) {
//                    16  GFR 代理人名称
                    labelGFR = new Label(16, index + 1, value);
                } else if ("DLRDH".equals(key)) {
//                    17 GFR  代理人电话
                    labelGFR = new Label(17, index + 1, value);
                } else if ("TDZZSJ".equals(key)) {
                    // TODO something....
//                    18  GFR 代理机构
                    labelGFR = new Label(18, index + 1, value);
                } else if ("QLRLX".equals(key)) {
//                    19  GFR 权利人性质
                    labelGFR = new Label(19, index + 1, getValue("qlrlx",key,value));
                } else if ("TDZZSJ".equals(key)) {
                    // TODO something....
//                    20  GFR 权利面积
                    labelGFR = new Label(20, index + 1, value);
                } else if ("QLXZ".equals(key)) {
//                  35  H 权利性质
                    labelH = new Label(35, index + 1, getValue("qlxz",key,value));
                } else if ("PZYT".equals(key)) {
//                  36 H 土地使用用途
                    labelH = new Label(36, index + 1, getValue("tdyt",key,value));
                } else if ("TDSYKSSJ".equals(key)) {
//                  37 H 土地起始时间
                    labelH = new Label(37, index + 1, AiUtil.GetValue(value, "", AiUtil.F_DATE));
                } else if ("TDSYJSSJ".equals(key)) {
//                  38 H 土地终止时间
                    labelH = new Label(38, index + 1, AiUtil.GetValue(value, "", AiUtil.F_DATE));
                }
//                //7预测建筑面积
//                if ("YCJZMJ".equals(key)) {
//                    label = new Label(7, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_FLOAT2));
//                }
//                // 8预测地下面积
//                if ("YCDXMJ".equals(key)) {
//                    label = new Label(8, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_FLOAT2));
//                }
//                // 9 预测其它面积
//                if ("YCQTMJ".equals(key)) {
//                    label = new Label(9, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_FLOAT2));
//                }
//                // 10 实测建筑面积
//                if ("SCJZMJ".equals(key)) {
//                    label = new Label(10, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_FLOAT2));
//                }
//                // 	11 实测地下面积
//                if ("SCDXMJ".equals(key)) {
//                    label = new Label(11, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_FLOAT2));
//                }
//                // 12 实测其它面积
//                if ("SCQTMJ".equals(key)) {
//                    label = new Label(12, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_FLOAT2));
//                }
//                // 13 竣工日期
//                if ("JGSJ".equals(key)) {
//                    label = new Label(13, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_DATE));
//                }
//                // 14 房屋结构
//                if ("FWJG".equals(key)) {
//                    label = new Label(14, index + 1, value);
//                }
//                // 15 建筑物状态
//                if ("JZWZT".equals(key)) {
//                    label = new Label(15, index + 1, value);
//                }
//                // 16 房屋用途
//                if ("FWYT".equals(key)) {
//                    label = new Label(16, index + 1, value);
//                }
//                // 17 总层数
//                if ("ZCS".equals(key)) {
//                    label = new Label(17, index + 1,  AiUtil.GetValue(entry.getValue(),"",AiUtil.F_INT));
//                }
//                // 18 地上层数
//                if ("DSCS".equals(key)) {
//                    label = new Label(18, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_INT));
//                }
//                // 19 地下层数
//                if ("DXCS".equals(key)) {
//                    label = new Label(19, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_INT));
//                }
//                // 20 备注
//                if ("BZ".equals(key)) {
//                    label = new Label(20, index + 1, value);
//                }
//                // 21 总套数
//                if ("ZTS".equals(key)) {
//                    label = new Label(21, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_INT));
//                }
                if (label != null) {
                    wsLJZ.addCell(label);
                    label = null;
                }
                if (labelGFR != null) {
                    wsGFR.addCell(labelGFR);
                    labelGFR = null;
                }
                if (labelH != null) {
                    wsH.addCell(labelH);
                    labelH = null;
                }
            }
        }
    }

    public static void writeZRZ_South(Map<String, WritableSheet> wSheets, List<String> ljz_attributeList, List<Feature> fs) throws WriteException {
        Label label = null;
        Label labelC = null;
        Label labelH = null;
        Label labelGFR = null;
        WritableSheet wsLJZ = wSheets.get("逻辑幢");
        WritableSheet wsC = wSheets.get("层");
        WritableSheet wsH = wSheets.get(FeatureHelper.LAYER_NAME_H);
        WritableSheet wsGFR = wSheets.get("购房人");
        for (Feature f : fs) {
            if (f == null) {
                continue;
            }
            int index = fs.indexOf(f);
            Iterator<Map.Entry<String, Object>> iterator = f.getAttributes().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                String value = AiUtil.GetValue(entry.getValue(), "");
                // 0 自然幢号
                /*if ("ZRZH".equals(key)) {
                    label = new Label(0, index + 1, value);
                    labelC = new Label(1, index + 1, value);
                    labelH = new Label(3, index + 1, value);
                }*/
                // 1 逻辑幢号
                if ("ZH".equals(key)) {
                    label = new Label(1, index + 1, value);
                    labelC = new Label(2, index + 1, value);
                    labelH = new Label(4, index + 1, value);
                    //
                    //  GFR 27 逻辑幢号
                    labelGFR = new Label(27, index + 1, value);
                }
                // 2 要素代码
                if ("YSDM".equals(key)) {
                    label = new Label(2, index + 1, "6001030120");
                    labelC = new Label(3, index + 1, "6001030130");
                }
//                // 3坐落
//                if ("ZL".equals(key)) {
//                    label = new Label(3, index + 1, value);
//                }
//                // 4门牌号
//                if ("MPH".equals(key)) {
//                    label = new Label(4, index + 1, value);
//                }
//                // 5宗地代码
//                if ("ZDDM".equals(key)) {
//                    label = new Label(5, index + 1, value);
//                }
//                // 6 小区名称
//                if ("XQMC".equals(key)) {
//                    label = new Label(6, index + 1, value);
//                }
                //7预测建筑面积
                if ("YCJZMJ".equals(key)) {
                    label = new Label(7, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                // 8预测地下面积
                if ("YCDXMJ".equals(key)) {
                    label = new Label(8, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                // 9 预测其它面积
                if ("YCQTMJ".equals(key)) {
                    label = new Label(9, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                // 10 实测建筑面积
                if ("SCJZMJ".equals(key)) {
                    label = new Label(10, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                // 	11 实测地下面积
                if ("SCDXMJ".equals(key)) {
                    label = new Label(11, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                // 12 实测其它面积
                if ("SCQTMJ".equals(key)) {
                    label = new Label(12, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                // 13 竣工日期
                if ("JGRQ".equals(key)) {
                    label = new Label(13, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_DATE));
                }
                // 14 房屋结构

                if ("FWJG".equals(key)) {
                    label = new Label(14, index + 1, getValue("fwjg",key,value));
                }
                // 15 建筑物状态
                if ("JZWZT".equals(key)) {
                    label = new Label(15, index + 1, value);
                }
              /*  // 16 房屋用途
                if ("FWYT".equals(key)) {
                    label = new Label(16, index + 1, value);
                }*/
                // 17 总层数
                if ("ZCS".equals(key)) {
                    label = new Label(17, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_INT));
                }
                // 18 地上层数
                if ("DSCS".equals(key)) {
                    label = new Label(18, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_INT));
                }
                // 19 地下层数
                if ("DXCS".equals(key)) {
                    label = new Label(19, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_INT));
                }
                // 20 备注
                if ("BZ".equals(key)) {
                    label = new Label(20, index + 1, value);
                }
                // 21 总套数
                if ("ZTS".equals(key)) {
                    label = new Label(21, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_INT));
                }
                if (label != null) {
                    wsLJZ.addCell(label);
                    label = null;
                }
                if (labelC != null) {
                    wsC.addCell(labelC);
                    labelC = null;
                }
                if (labelH != null) {
                    wsH.addCell(labelH);
                    labelH = null;
                }
                if (labelGFR != null) {
                    wsGFR.addCell(labelGFR);
                    labelGFR = null;
                }
            }
        }
    }
    // Excel
    public static Map<String, WritableSheet> getAllWritableWorkbook_South(WritableWorkbook workbook) throws WriteException {
        Map<String, WritableSheet> map = new HashMap<String, WritableSheet>();
        // 逻辑幢
        String sheetName = "逻辑幢";
        WritableSheet wsLJZ = workbook.createSheet(sheetName, 0);
        Label label = null;
        for (int i = 0; i < getLJZ_AttributeList_South().size(); i++) {
            label = new Label(i, 0, getLJZ_AttributeList_South().get(i));
            wsLJZ.addCell(label);
        }
        map.put(sheetName, wsLJZ);
        // 层
        sheetName = "层";
        WritableSheet wsC = workbook.createSheet(sheetName, 1);
        for (int i = 0; i < getC_AttributeList_South().size(); i++) {
            label = new Label(i, 0, getC_AttributeList_South().get(i));
            wsC.addCell(label);
        }
        map.put(sheetName, wsC);
        //户
        sheetName = FeatureHelper.LAYER_NAME_H;
        WritableSheet wsH = workbook.createSheet(sheetName, 2);
        for (int i = 0; i < getH_AttributeList_South().size(); i++) {
            label = new Label(i, 0, getH_AttributeList_South().get(i));
            wsH.addCell(label);
        }
        map.put(sheetName, wsH);
        //开发商
        sheetName = "开发商";
        WritableSheet wsKFS = workbook.createSheet(sheetName, 3);
        for (int i = 0; i < getKFS_AttributeList_South().size(); i++) {
            label = new Label(i, 0, getKFS_AttributeList_South().get(i));
            wsKFS.addCell(label);
        }
        map.put(sheetName, wsKFS);
        //购房人
        sheetName = "购房人";
        WritableSheet wsGFR = workbook.createSheet(sheetName, 4);
        for (int i = 0; i < getGFR_AttributeList_South().size(); i++) {
            label = new Label(i, 0, getGFR_AttributeList_South().get(i));
            wsGFR.addCell(label);
        }
        map.put(sheetName, wsGFR);
        sheetName = null;
        return map;
    }

    public static List<String> getLJZ_AttributeList_South() {
        ArrayList<String> listLJZ = new ArrayList<>();
        listLJZ.add("自然幢号");
        listLJZ.add("逻辑幢号");
        listLJZ.add("要素代码");
        listLJZ.add("坐落");
        listLJZ.add("门牌号");
        listLJZ.add("宗地代码");
        listLJZ.add("小区名称");
        listLJZ.add("预测建筑面积");
        listLJZ.add("预测地下面积");
        listLJZ.add("预测其它面积");
        listLJZ.add("实测建筑面积");
        listLJZ.add("实测地下面积");
        listLJZ.add("实测其它面积");
        listLJZ.add("竣工日期");
        listLJZ.add("房屋结构");
        listLJZ.add("建筑物状态");
        listLJZ.add("房屋用途");
        listLJZ.add("总层数");
        listLJZ.add("地上层数");
        listLJZ.add("地下层数");
        listLJZ.add("备注");
        listLJZ.add("总套数");
        return listLJZ;
    }

    public static List<String> getC_AttributeList_South() {
        ArrayList<String> listC = new ArrayList<>();
        listC.add("层号");
        listC.add("自然幢号");
        listC.add("逻辑幢号");
        listC.add("要素代码");
        listC.add("实际层");
        listC.add("名义层");
        listC.add("层建筑面积");
        listC.add("层套内建筑面积");
        listC.add("层阳台面积");
        listC.add("层共有建筑面积");
        listC.add("层分摊建筑面积");
        listC.add("层半墙面积");
        listC.add("层高");
        listC.add("水平投影面积");
        return listC;
    }
    // Excel 户
    public static List<String> getH_AttributeList_South() {
        ArrayList<String> listH = new ArrayList<>();
        listH.add("房屋编码");//0
        listH.add("要素代码");
        listH.add("不动产单元号");
        listH.add("自然幢号");
        listH.add("逻辑幢号");
        listH.add("层号");
        listH.add("坐落");
        listH.add("面积单位");
        listH.add("实际层");
        listH.add("名义层");
        listH.add("户号");// 10
        listH.add("户型");
        listH.add("室号部位");
        listH.add("户型结构");
        listH.add("房屋用途");
        listH.add("预测建筑面积");
        listH.add("预测套内建筑面积");
        listH.add("预测分摊建筑面积");
        listH.add("预测地下部分建筑面积");
        listH.add("预测其它建筑面积");
        listH.add("预测分摊系数");// 20
        listH.add("实测建筑面积");
        listH.add("实测套内建筑面积");
        listH.add("实测分摊建筑面积");
        listH.add("实测地下部分建筑面积");
        listH.add("实测其它建筑面积");
        listH.add("实测分摊系数");
        listH.add("共有土地面积");
        listH.add("分摊土地面积");
        listH.add("独用土地面积");
        listH.add("房屋类型");//30
        listH.add("房屋性质");
        listH.add("房屋结构");
        listH.add("单元信息");
        listH.add("单元号");
        listH.add("权利性质");
        listH.add("土地使用用途");
        listH.add("土地起始时间");
        listH.add("土地终止时间");

        return listH;
    }
    // Excel 开发商
    public static List<String> getKFS_AttributeList_South() {
        ArrayList<String> listKFS = new ArrayList<>();
        listKFS.add("开发商名称");
        listKFS.add("证件编号");
        listKFS.add("通讯地址");
        listKFS.add("联系人");
        listKFS.add("联系电话");
        listKFS.add("法人名称");
        listKFS.add("法人电话");
        listKFS.add("代理人名称");
        listKFS.add("代理人电话");
        listKFS.add("代理机构");
        listKFS.add("开发商登记号");
        listKFS.add("登记日期");
        listKFS.add("资质证书号");
        listKFS.add("经营范围");
        listKFS.add("证件种类");
        listKFS.add("单位性质");
        listKFS.add("注册资金(万元)");
        listKFS.add("资质等级");
        listKFS.add("备注");

        return listKFS;
    }
    // Excel 购房人
    public static List<String> getGFR_AttributeList_South() {
        ArrayList<String> listGFR = new ArrayList<>();
        listGFR.add("权利人名称");
        listGFR.add("证件类别");
        listGFR.add("证件号码");
        listGFR.add("发证机关");
        listGFR.add("所属行业");
        listGFR.add("国家/地区");
        listGFR.add("户籍所在省市");
        listGFR.add("性别");
        listGFR.add("联系人");
        listGFR.add("联系电话");
        listGFR.add("通讯地址");
        listGFR.add("邮编");
        listGFR.add("工作单位");
        listGFR.add("电子邮件");
        listGFR.add("法人名称");
        listGFR.add("法人电话");
        listGFR.add("代理人名称");
        listGFR.add("代理人电话");
        listGFR.add("代理机构");
        listGFR.add("权利人性质");
        listGFR.add("权利面积");
        listGFR.add("权利比例");
        listGFR.add("共有方式");
        listGFR.add("共有情况");
        listGFR.add("不动产单元号");
        listGFR.add("不动产权证号");
        listGFR.add("备注");
        listGFR.add("逻辑幢号");
        listGFR.add("层号");
        listGFR.add("户号");
        listGFR.add("合同编号");
        return listGFR;
    }
    // region 中地不动产数据库
    public static List<String> getZXX_AttributeList_Zondy() {
        ArrayList<String> listZXX = new ArrayList<>();
        listZXX.add("幢号");
        listZXX.add("建筑面积");
        listZXX.add("套内建筑面积");
        listZXX.add("分摊建筑面积");
        listZXX.add("地下面积");
        listZXX.add("街道");
        listZXX.add("街坊");
        listZXX.add("街路巷");
        listZXX.add("门牌号");
        listZXX.add("小区名称");
        listZXX.add("项目名称");
        listZXX.add("坐落");
        listZXX.add("总层数");
        listZXX.add("地上层数");
        listZXX.add("地下层数");
        listZXX.add("单元总数");
        listZXX.add("建筑结构");
        listZXX.add("建筑用途");
        listZXX.add("占地面积");
        listZXX.add("住宅面积");
        listZXX.add("办公面积");
        listZXX.add("商服面积");
        listZXX.add("其他面积");
        listZXX.add("住宅套数");
        listZXX.add("办公套数");
        listZXX.add("商服套数");
        listZXX.add("其他套数");
        listZXX.add("总套数");
        listZXX.add("东至");
        listZXX.add("南至");
        listZXX.add("西至");
        listZXX.add("北至");
        listZXX.add("竣工日期");
        listZXX.add("测绘状态");
        listZXX.add("项目编号");
        listZXX.add("建筑物名称");
        listZXX.add("房屋结构1");
        listZXX.add("房屋结构2");
        listZXX.add("房屋结构3");
        listZXX.add("房屋用途1");
        listZXX.add("房屋用途2");
        listZXX.add("房屋用途3");
        listZXX.add("要素代码");
        listZXX.add("建筑物状态");
        listZXX.add("栋号");
        listZXX.add("幢来源");
        listZXX.add("BMFBuildID");
        listZXX.add("$E");
        return listZXX;
    }

    public static List<String> getFWXX_AttributeList_Zondy() {
        ArrayList<String> listFWXX = new ArrayList<>();
        listFWXX.add("幢号");
        listFWXX.add("街道");
        listFWXX.add("街坊");
        listFWXX.add("街路巷");
        listFWXX.add("门牌号");
        listFWXX.add("单元号");
        listFWXX.add("单元名称");
        listFWXX.add("物理层");
        listFWXX.add("名义层");
        listFWXX.add("室号部位");
        listFWXX.add("户号");
        listFWXX.add("房屋坐落");
        listFWXX.add("房屋性质");
        listFWXX.add("房屋类型");
        listFWXX.add("房屋用途");
        listFWXX.add("户型结构");
        listFWXX.add("户型");
        listFWXX.add("东至");
        listFWXX.add("南至");
        listFWXX.add("西至");
        listFWXX.add("北至");
        listFWXX.add("总层数");
        listFWXX.add("建筑面积");
        listFWXX.add("套内建筑面积");
        listFWXX.add("分摊建筑面积");
        listFWXX.add("分摊系数");
        listFWXX.add("共有土地面积");
        listFWXX.add("分摊土地面积");
        listFWXX.add("独有土地面积");
        listFWXX.add("测绘状态");
        listFWXX.add("备注");
        listFWXX.add("房屋总价");
        listFWXX.add("房屋结构");
        listFWXX.add("栋号");
        listFWXX.add("房屋所有权人");
        listFWXX.add("权利人类型");
        listFWXX.add("证件种类");
        listFWXX.add("证件号码");
        listFWXX.add("电话");
        listFWXX.add("住址");
        listFWXX.add("产权来源");
        listFWXX.add("产别");
        listFWXX.add("要素代码");
        listFWXX.add("共有情况");
        listFWXX.add("规划用途");
        listFWXX.add("专有建筑面积");
        listFWXX.add("面积单位");
        listFWXX.add("房屋用途1");
        listFWXX.add("房屋用途2");
        listFWXX.add("房屋用途3");
        listFWXX.add("地下部分建筑面积");
        listFWXX.add("其他建筑面积");
        listFWXX.add("BMFHouseID");
        listFWXX.add("土地用途");
        listFWXX.add("土地使用权起始时间");
        listFWXX.add("土地使用权结束时间");
        listFWXX.add("土地使用期限");
        listFWXX.add("权利性质");
        listFWXX.add("$E");

        return listFWXX;
    }

    // Excel Zondy
    public static void CreateSondyExcelToGdal(Feature feature, String filePath, List<List<Feature>> values) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            boolean isWrite = file.canWrite();
            workbook = Workbook.createWorkbook(file);
            Map<String, WritableSheet> wSheets = getAllWritableWorkbook_Zondy(workbook);

            //创建sheetname
            for (List<Feature> fs : values) {
                if (fs.size() == 0) {
                    continue;
                }
                String Layername = fs.get(0).getFeatureTable().getTableName();
                if (FeatureHelper.TABLE_NAME_ZRZ.equals(Layername)) {
                    writeZRZ_Zondy(wSheets, fs);
                } else if (FeatureHelper.TABLE_NAME_ZD.equals(Layername)) {
                    writeZD_Zondy(wSheets,fs);
                } else if (FeatureHelper.TABLE_NAME_H.equals(Layername)) {
                    writeH_Zondy(wSheets, fs);
                } else if ("QLR".equals(Layername)) {
                    writeQLR_Zondy(wSheets, fs);
                }
            }
            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeQLR_Zondy(Map<String, WritableSheet> wSheets, List<Feature> fs) {
    }

    public static Map<String, WritableSheet> getAllWritableWorkbook_Zondy(WritableWorkbook workbook) throws WriteException {
        Map<String, WritableSheet> map = new HashMap<String, WritableSheet>();
        // 幢信息
        String sheetName = "幢信息";
        WritableSheet wsZXX = workbook.createSheet(sheetName, 0);
        Label label = null;
        for (int i = 0; i < getZXX_AttributeList_Zondy().size(); i++) {
            label = new Label(i, 0, getZXX_AttributeList_Zondy().get(i));
            wsZXX.addCell(label);
        }
        map.put(sheetName, wsZXX);
        // 房屋信息
        sheetName = "房屋信息";
        WritableSheet wsFWXX = workbook.createSheet(sheetName, 1);
        for (int i = 0; i < getFWXX_AttributeList_Zondy().size(); i++) {
            label = new Label(i, 0, getFWXX_AttributeList_Zondy().get(i));
            wsFWXX.addCell(label);
        }
        map.put(sheetName, wsFWXX);
        sheetName = null;
        return map;
    }

    public static void writeZD_Zondy(Map<String, WritableSheet> wSheets,List<Feature> fs_zd) throws WriteException {
        Label label = null;
        WritableSheet wsZXX = wSheets.get("幢信息");
        for (Feature f : fs_zd) {
            if (f == null) {
                continue;
            }
            int index = fs_zd.indexOf(f);
            Iterator<Map.Entry<String, Object>> iterator = f.getAttributes().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                String value = AiUtil.GetValue(entry.getValue(), "");
/* write zrz zondy in zxx
//               0 幢号
                if ("ZH".equals(key)) {
                    // TODO something...
                    label = new Label(0, index + 1, value);
                }
//               1 建筑面积
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(1, index + 1, value);
                }
//               2 套内建筑面积
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(2, index + 1, value);
                }
//               3 分摊建筑面积
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(3, index + 1, value);
                }
//               4 地下面积
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(4, index + 1, value);
                }
//               5 街道
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(5, index + 1, value);
                }
//               6 街坊
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(6, index + 1, value);
                }
//               7 街路巷
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(7, index + 1, value);
                }*/
//               8 门牌号
                if ("MPHM".equals(key)) {
                    label = new Label(8, index + 1, value);
                }
//               9 小区名称
                if ("LPMC".equals(key)) {
                    label = new Label(9, index + 1, value);
                }
//               10 项目名称
                if ("XMMC".equals(key)) {
                    label = new Label(10, index + 1, value);
                }
//               11 坐落
                if ("ZL".equals(key)) {
                    label = new Label(11, index + 1, value);
                }
/*
//               12 总层数
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(12, index + 1, value);
                }
//               13 地上层数
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(13, index + 1, value);
                }
//               14 地下层数
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(14, index + 1, value);
                }
//              15 单元总数
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(15, index + 1, value);
                }
//               16 建筑结构
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(16, index + 1, value);
                }
//               17 建筑用途
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(17, index + 1, value);
                }
//               18 占地面积
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(18, index + 1, value);
                }
//              19 住宅面积
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(19, index + 1, value);
                }
//              20 办公面积
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(20, index + 1, value);
                }
//              21 商服面积
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(21, index + 1, value);
                }
//              22 其他面积
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(22, index + 1, value);
                }
//              23 住宅套数
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(23, index + 1, value);
                }
//              24 办公套数
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(24, index + 1, value);
                }
//               25 商服套数
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(25, index + 1, value);
                }
//              26 其他套数
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(26, index + 1, value);
                }
//               27 总套数
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(27, index + 1, value);
                }
                */
//              28 东至
                if ("ZDSZD".equals(key)) {
                    label = new Label(28, index + 1, value);
                }
//              29 南至
                if ("ZDSZN".equals(key)) {
                    label = new Label(29, index + 1, value);
                }
//              30 西至
                if ("ZDSZX".equals(key)) {
                    label = new Label(30, index + 1, value);
                }
//             31 北至
                if ("ZDSZB".equals(key)) {
                    label = new Label(31, index + 1, value);
                }
/* write zrz zondy in zxx
//              32 竣工日期
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(32, index + 1, value);
                }
//              33 测绘状态
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(33, index + 1, value);
                }
//              34 项目编号
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(34, index + 1, value);
                }
//               35 建筑物名称
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(35, index + 1, value);
                }
//               36 房屋结构1
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(36, index + 1, value);
                }
//               37 房屋结构2
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(37, index + 1, value);
                }
//               38 房屋结构3
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(38, index + 1, value);
                }
//               39 房屋用途1
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(39, index + 1, value);
                }
//               40 房屋用途2
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(40, index + 1, value);
                }
//              41 房屋用途3
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(41, index + 1, value);
                }
//               42 要素代码
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(42, index + 1, value);
                }
//               43 建筑物状态
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(43, index + 1, value);
                }
//               44 栋号
                if ("FWDH".equals(key)) {
                    // TODO something...
                    label = new Label(44, index + 1, value);
                }
//               45 幢来源
                if ("ZLY".equals(key)) {
                    // TODO something...
                    label = new Label(45, index + 1, value);
                }
//               46  BMFBuildID
                if ("ZL".equals(key)) {
                    // TODO something...
                    label = new Label(47, index + 1, value);
                }*/

                if (label != null) {
                    wsZXX.addCell(label);
                    label = null;
                }
            }
        }
    }
    private static void writeZRZ_Zondy(Map<String, WritableSheet> wSheets, List<Feature> fs) throws WriteException {
        WritableSheet wsZXX = wSheets.get("幢信息");
        Label label = new Label(0, fs.size() + 1, "$E");
        wsZXX.addCell(label);
//       测绘状态
        label = new Label(33, 1, "实测");
        wsZXX.addCell(label);
//      幢来源
        label = new Label(45, 1, "测绘");
        wsZXX.addCell(label);
        label = null;
        for (Feature f : fs) {
            if (f == null) {
                continue;
            }
            fwdh = FeatureHelper.Get(f, "FWDCY","");
            jdx = FeatureHelper.Get(f, "QJXHTR", "");

            int index = fs.indexOf(f);
            Iterator<Map.Entry<String, Object>> iterator = f.getAttributes().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                String value = AiUtil.GetValue(entry.getValue(), "");
                //                0 幢号
                if ("ZH".equals(key)) {
                    label = new Label(0, index + 1, StringUtil.substr(value,2));
                }
//                1 建筑面积
                if ("SCJZMJ".equals(key)) {
                    label = new Label(1, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                    wsZXX.addCell(label);
                    label = new Label(2, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//                2 套内建筑面积
//                if ("ZYJZMJ".equals(key)) {
//                    label = new Label(2, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
//                }
//                3 分摊建筑面积
                if ("FTJZMJ".equals(key)) {
                    label = new Label(3, index + 1, AiUtil.GetValue(entry.getValue(), "0.00", AiUtil.F_FLOAT2));
                }
////                4 地下面积
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(4, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
//                }
////                5 街道
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(5, index + 1, value);
//                }
//                6 街坊
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(6, index + 1, value);
//                }
//                7 街路巷
                if ("QJXHTR".equals(key)) {
                    // TODO something...
                    label = new Label(7, index + 1, value);
                }
/*  write zondy zd
//                8 门牌号
                if ("MPHM".equals(key)) {
                    label = new Label(8, index + 1, value);
                }
//                9 小区名称
                if ("LPMC".equals(key)) {
                    label = new Label(9, index + 1, value);
                }
//                10 项目名称
                if ("XMMC".equals(key)) {
                    label = new Label(10, index + 1, value);
                }
//                11 坐落
                if ("ZL".equals(key)) {
                    label = new Label(11, index + 1, value);
                }
                */
//                12 总层数
                if ("ZCS".equals(key)){
                    label = new Label(12, index + 1, AiUtil.GetValue(entry.getValue(),"1",AiUtil.F_INT));
                }
//                13 地上层数
                if ("DSCS".equals(key)) {
                    label = new Label(13, index + 1, AiUtil.GetValue(entry.getValue(),"1",AiUtil.F_INT));
                }
//                14 地下层数
                if ("DXCS".equals(key)) {
                    label = new Label(14, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_INT));
                }
//                15 单元总数
//                if ("ZTS".equals(key)) {
//                    // TODO something...
//                    label = new Label(15, index + 1, value);
//                }
//                16 建筑结构
                if ("FWJG".equals(key)) {
                    label = new Label(16, index + 1, getValue("fwjg",key,value));
                }
//                17 建筑用途
                if ("JZWJBYT".equals(key)) {
                    label = new Label(17, index + 1,  getValue("jzwjbyt",key,value));
                }
//                18 占地面积
                if ("ZZDMJ".equals(key)) {
                    label = new Label(18, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
////                19 住宅面积
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(19, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
//                }
////                20 办公面积
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(20, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
//                }
////                21 商服面积
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(21, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
//                }
////                22 其他面积
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(22, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
//                }
////                23 住宅套数
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(23, index + 1, value);
//                }
////                24 办公套数
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(24, index + 1, value);
//                }
////                25 商服套数
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(25, index + 1, value);
//                }
////                26 其他套数
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(26, index + 1, value);
//                }
//                27 总套数
                if ("ZTS".equals(key)) {
                    label = new Label(27, index + 1, value);
                }
/* write zd zondy in zxx
//                28 东至
                if ("ZDSZD".equals(key)) {
                    label = new Label(28, index + 1, value);
                }
//                29 南至
                if ("ZDSZN".equals(key)) {
                    label = new Label(29, index + 1, value);
                }
//                30 西至
                if ("ZDSZX".equals(key)) {
                    label = new Label(30, index + 1, value);
                }
//                31 北至
                if ("ZDSZB".equals(key)) {
                    label = new Label(31, index + 1, value);
                }
                */
//                32 竣工日期
                if ("JGRQ".equals(key)) {
                    label = new Label(32, index + 1,AiUtil.GetValue(entry.getValue(),"",AiUtil.F_DATE));
                }
//                33 测绘状态
//                if ("ZL".equals(key)) {
//                    // TODO something...
//                    label = new Label(33, index + 1, value);
//                }
//                34 项目编号
//                if ("XMDM".equals(key)) {
//                    // TODO something...
//                    label = new Label(34, index + 1, value);
//                }
//                35 建筑物名称
                if ("JZWMC".equals(key)) {
                    // TODO something...
                    label = new Label(35, index + 1, value);
                }
//                36 房屋结构1
                if ("FWJG1".equals(key)) {
                    // TODO something...
                    label = new Label(36, index + 1, value);
                }
//                37 房屋结构2
                if ("FWJG2".equals(key)) {
                    // TODO something...
                    label = new Label(37, index + 1, value);
                }
//                38 房屋结构3
                if ("FWJG3".equals(key)) {
                    // TODO something...
                    label = new Label(38, index + 1, value);
                }
//                39 房屋用途1
                if ("FWYT1".equals(key)) {
                    // TODO something...
                    label = new Label(39, index + 1, value);
                }
//                40 房屋用途2
                if ("FWYT2".equals(key)) {
                    // TODO something...
                    label = new Label(40, index + 1, value);
                }
//                41 房屋用途3
                if ("FWYT3".equals(key)) {
                    // TODO something...
                    label = new Label(41, index + 1, value);
                }
//                42 要素代码
                if ("YSDM".equals(key)) {
                    // TODO something...
                    label = new Label(42, index + 1, value);
                }
//                43 建筑物状态
                if ("ZT".equals(key)) {
                    // TODO something...
                    label = new Label(43, index + 1, value);
                }
//                44 栋号
                if ("FWDCY".equals(key)) {
                    // TODO something...
                    label = new Label(44, index + 1, value);
                }
//                45 幢来源
//                if ("ZLY".equals(key)) {
//                    // TODO something...
//                    label = new Label(45, index + 1, value);
//                }
//                46 BMFBuildID
                if ("BMFBuildID".equals(key)) {
                    // TODO something...
                    label = new Label(46, index + 1, value);
                }

                if (label != null) {
                    wsZXX.addCell(label);
                    label = null;
                }
            }
        }
        label = new Label(45, 1, "实测");
        wsZXX.addCell(label);
        label = null;
    }
    private static void writeH_Zondy(Map<String, WritableSheet> wSheets, List<Feature> fs) throws WriteException {
        Label label = null;

        WritableSheet wsFWXX = wSheets.get("房屋信息");
        label = new Label(0, fs.size()+1, "$E");
        wsFWXX.addCell(label);
        label=null;
        for (Feature f : fs) {
            if (f == null) {
                continue;
            }
            int index = fs.indexOf(f);
            label = new Label(29, index + 1, "实测");
            wsFWXX.addCell(label);
            label = new Label(33, index + 1, fwdh);
            wsFWXX.addCell(label);
            label = new Label(3, index + 1, jdx);
            wsFWXX.addCell(label);
            label=null;

            Iterator<Map.Entry<String, Object>> iterator = f.getAttributes().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                String value = AiUtil.GetValue(entry.getValue(), "");
                //0 Zondy FWXX 幢号
                if ("ZRZH".equals(key)) {
                    label = new Label(0, index + 1, StringUtil.substr_last(value,2));
                }
                //1 Zondy FWXX 街道
                if ("JD".equals(key)) {
//                  TODO something...
                    label = new Label(1, index + 1, value);
                }
                //2 Zondy FWXX 街坊
                if ("JF".equals(key)) {
//                  TODO something...
                    label = new Label(2, index + 1, value);
                }
                //3 Zondy FWXX 街路巷
//                if ("JLX".equals(key)) {
////                    TODO something...
//                    label = new Label(3, index + 1, value);
//                }
                //4 Zondy FWXX 门牌号
                if ("MPH".equals(key)) {
                    label = new Label(4, index + 1, value);
                }
                //5 Zondy FWXX 单元号
                if ("DYH".equals(key)) {
//                    TODO something...
                    label = new Label(5, index + 1, value);
                }
                //6 Zondy FWXX 单元名称
                if ("DYMC".equals(key)) {
//                    TODO something...
                    label = new Label(6, index + 1, value);
                }
                //7 Zondy FWXX 物理层
                if ("CH".equals(key)) {
                    label = new Label(7, index + 1, value);
                    wsFWXX.addCell(label);
                    //8 Zondy FWXX 名义层
                    label = new Label(8, index + 1, value);
                }
//                //9 Zondy FWXX 室号部位
//                if ("SHBW".equals(key)) {
//                    label = new Label(9, index + 1, value);
//                }
                //10 Zondy FWXX 户号
                if ("HH".equals(key)) {
                    label = new Label(10, index + 1, value);
                    wsFWXX.addCell(label);
                    //  9 FWXX 室号部位
                    label = new Label(9, index + 1, value);
                }
                //11 Zondy FWXX 房屋坐落
                if ("ZL".equals(key)) {
                    label = new Label(11, index + 1, value);
                }
                //12 Zondy FWXX 房屋性质
                if ("FWXZ".equals(key)) {
                    label = new Label(12, index + 1, getValue("fwxz",key,value));
                }
                //13 Zondy FWXX 房屋类型
                if ("FWLX".equals(key)) {
                    label = new Label(13, index + 1, getValue("fwlx",key,value));
                }
                //14 Zondy FWXX 房屋用途
                if ("YT".equals(key)) {
                    label = new Label(14, index + 1, value);
                }
                //15 Zondy FWXX 户型结构
                if ("HXJG".equals(key)) {
                    label = new Label(15, index + 1, value);
                }
                //16 Zondy FWXX 户型
                if ("HX".equals(key)) {
                    label = new Label(16, index + 1, value);
                }
                /*//17 Zondy FWXX 东至
                if ("ZDSZD".equals(key)) {
                    label = new Label(17, index + 1, value);
                }
                //18 Zondy FWXX 南至
                if ("ZDSZN".equals(key)) {
                    label = new Label(18, index + 1, value);
                }
                //19 Zondy FWXX 西至
                if ("ZDSZX".equals(key)) {
                    label = new Label(19, index + 1, value);
                }
                //20 Zondy FWXX 北至
                if ("ZDSZB".equals(key)) {
                    label = new Label(20, index + 1, value);
                }*/
                //21 Zondy FWXX 总层数
                if ("ZCS".equals(key)) {
                    label = new Label(21, index + 1, AiUtil.GetValue(entry.getValue(),"",AiUtil.F_INT));
                }
                //22 Zondy FWXX 建筑面积
                if ("SCJZMJ".equals(key)) {
                    label = new Label(22, index + 1
                            , AiUtil.GetValue(entry.getValue()
                            , AiUtil.GetValue(FeatureHelper.Get(f,"YCJZMJ"),"",AiUtil.F_FLOAT2)
                            , AiUtil.F_FLOAT2));
                }
                //23 Zondy FWXX 套内建筑面积
                if ("SCTNJZMJ".equals(key)) {
                    label = new Label(23, index + 1
                            , AiUtil.GetValue(entry.getValue()
                            , AiUtil.GetValue(FeatureHelper.Get(f,"YCJZMJ"),"",AiUtil.F_FLOAT2)
                            , AiUtil.F_FLOAT2));
                }
                //24 Zondy FWXX 分摊建筑面积
                if ("SCFTJZMJ".equals(key)) {
                    label = new Label(24, index + 1, AiUtil.GetValue(entry.getValue(), "0.00", AiUtil.F_FLOAT2));
                }
                //25 Zondy FWXX 分摊系数
                if ("SCFTXS".equals(key)) {
                    label = new Label(25, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                //26 Zondy FWXX 共有土地面积
                if ("GYTDMJ".equals(key)) {
                    label = new Label(26, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                //27 Zondy FWXX 分摊土地面积
                if ("FTTDMJ".equals(key)) {
                    label = new Label(27, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                //28 Zondy FWXX 独有土地面积
                if ("DYTDMJ".equals(key)) {
                    label = new Label(28, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
//                //29 Zondy FWXX 测绘状态
//                if ("CHZT".equals(key)) {
////                    TODO something...
//                    label = new Label(29, index + 1, value);
//                }
                //30 Zondy FWXX 备注
                if ("BZ".equals(key)) {
                    label = new Label(30, index + 1, value);
                }
                //31 Zondy FWXX 房屋总价
                if ("FWZJ".equals(key)) {
//                    TODO something...
                    label = new Label(31, index + 1, value);
                }
                //32 Zondy FWXX 房屋结构
                if ("FWJG".equals(key)) {
                    label = new Label(32, index + 1, getValue("fwjg",key,value));
                }
                //33 Zondy FWXX 栋号
//                if ("FWDH".equals(key)) {
//                    // TODO something...
//                    label = new Label(33, index + 1, value);
//                }
                //34 Zondy FWXX 房屋所有权人
                if ("QLRXM".equals(key)) {
                    label = new Label(34, index + 1, value);
                }
                //35 Zondy FWXX 权利人类型
                if ("QLRLX".equals(key)) {
                    label = new Label(35, index + 1, getValue("qllx",key,value));
                }
                //36 Zondy FWXX 证件种类
                if ("QLRZJZL".equals(key)) {
                    label = new Label(36, index + 1, getValue("zjzl",key,value));
                }
                //37 Zondy FWXX 证件号码
                if ("QLRZJH".equals(key)) {
                    label = new Label(37, index + 1, value);
                }
                //38 Zondy FWXX 电话
                if ("QLRDH".equals(key)) {
                    label = new Label(38, index + 1, value);
                }
                //39 Zondy FWXX 住址
                if ("QLRTXDZ".equals(key)) {
                    label = new Label(39, index + 1, value);
                }
                //40 Zondy FWXX 产权来源
                if ("CQLY".equals(key)) {
                    label = new Label(40, index + 1, value);
                }
                //41 Zondy FWXX 产别
                if ("CB".equals(key)) {
                    label = new Label(41, index + 1, getValue("fwcb",key,value));
                }
                //42 Zondy FWXX 要素代码
                if ("YSDM".equals(key)) {
                    label = new Label(42, index + 1, value);
                }
                //43 Zondy FWXX 共有情况
                if ("GYQK".equals(key)) {
                    label = new Label(43, index + 1, value);
                }
                //44 Zondy FWXX 规划用途
                if ("GHYT".equals(key)) {
                    label = new Label(44, index + 1, value);
                }
                //45 Zondy FWXX 专有建筑面积
                if ("DYTDMJ".equals(key)) {
                    label = new Label(45, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                //46 Zondy FWXX 面积单位
                if ("MJDW".equals(key)) {
                    label = new Label(46, index + 1, value);
                }
                //47 Zondy FWXX 房屋用途1
                if ("FWYT1".equals(key)) {
                    label = new Label(47, index + 1, value);
                }
                //48 Zondy FWXX 房屋用途2
                if ("FWYT2".equals(key)) {
                    label = new Label(48, index + 1, value);
                }
                //49 Zondy FWXX 房屋用途3
                if ("FWYT3".equals(key)) {
                    label = new Label(49, index + 1, value);
                }
                //50 Zondy FWXX 地下部分建筑面积
                if ("SCDXBFJZMJ".equals(key)) {
                    label = new Label(50, index + 1, AiUtil.GetValue(entry.getValue(), "", AiUtil.F_FLOAT2));
                }
                //51 Zondy FWXX 其他建筑面积
                if ("SCQTJZMJ".equals(key)) {
                }
                //52 Zondy FWXX BMFHouseID
                if ("BMFHouseID".equals(key)) {
//                    TODO something...
                    label = new Label(52, index + 1, value);
                }
                //53 Zondy FWXX 土地用途
                if ("TDYT".equals(key)) {
//                    TODO something...
                    label = new Label(53, index + 1, value);
                }
                //54 Zondy FWXX 土地使用权起始时间
                if ("TDKSSJ".equals(key)) {
                    label = new Label(54, index + 1, value);
                }
                //55 Zondy FWXX 土地使用权结束时间
                if ("TDJSSJ".equals(key)) {
                    label = new Label(55, index + 1, value);
                }
                //56 Zondy FWXX 土地使用期限
                if ("TDSYQX".equals(key)) {
//                    TODO something...
                    label = new Label(56, index + 1, value);
                }
                //57 Zondy FWXX 权利性质
                if ("QLXZ".equals(key)) {
//                    TODO something...
                    label = new Label(57, index + 1, getValue("qlxz",key,value));
                }
                if (label != null) {
                    wsFWXX.addCell(label);
                    label = null;
                }
            }
        }
    }
    // region 京山台账
    public static void CreateStandingBook_JingShang(com.ovit.app.map.model.MapInstance mapInstance, String sheerName, String filePath, List<Feature> fs_zd){
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            boolean isWrite = file.canWrite();
            workbook = Workbook.createWorkbook(file);
            WritableSheet wSheet = workbook.createSheet(sheerName, 0);
            wSheet.addCell( new Label(0, 1, "宗地代码"));
            wSheet.addCell( new Label(1, 1, "权利人"));
            wSheet.addCell( new Label(2, 1, "身份证证明材料"));
            wSheet.addCell( new Label(3, 1, "建筑面积"));
            wSheet.addCell( new Label(4, 1, "宗地面积"));
            wSheet.addCell( new Label(5, 1, "权属资料收集情况"));
            wSheet.addCell( new Label(6, 1, "农房签字情况"));
            wSheet.addCell( new Label(7, 1, "核实情况"));

            for (Feature f_zd : fs_zd) {
                int index=fs_zd.indexOf(f_zd)+1;
                wSheet.addCell( new Label(0, index+1, FeatureHelper.Get(f_zd,FeatureHelper.TABLE_ATTR_ZDDM,"")));
                wSheet.addCell( new Label(1, index+1, FeatureHelper.Get(f_zd,"QLRXM","")));
                String image_sfzmcl = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/") + "权利人证件号";
                String image_hkb = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/") + "户口簿";
                String sfzzmcl="";
                if (FileUtils.getFileCount(image_sfzmcl) > 0){
                    sfzzmcl="身份证";
                }
                if (FileUtils.getFileCount(image_hkb) > 0){
                    sfzzmcl+=",户口簿";
                }

                wSheet.addCell( new Label(2, index+1, sfzzmcl));
                wSheet.addCell( new Label(3, index+1, FeatureHelper.Get(f_zd,"JZMJ",0.00)+""));
                wSheet.addCell( new Label(4, index+1, FeatureHelper.Get(f_zd,FeatureHelper.TABLE_ATTR_ZDMJ,0.00)+""));
//                wSheet.addCell( new Label(5, index+1, FeatureHelper.Get(f_zd,"ZDDM","")));
//                wSheet.addCell( new Label(6, index+1, FeatureHelper.Get(f_zd,"ZDDM","")));
            }

            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //  恩施总表
    public static void CreateStandingBook_EnShi(com.ovit.app.map.model.MapInstance mapInstance, String sheerName, String filePath, List<Feature> fs_zd) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            boolean isWrite = file.canWrite();
            workbook = Workbook.createWorkbook(file);
            WritableSheet wSheet = workbook.createSheet(sheerName, 0);
//            wSheet.addCell( new Label(0, 1, "宗地代码"));

            wSheet.addCell(new Label(0, 0, "宗地代码"));
            wSheet.addCell(new Label(1, 0, "权利人"));
            wSheet.addCell(new Label(2, 0, "使用权"));
            wSheet.addCell(new Label(3, 0, "权利人类型"));
            wSheet.addCell(new Label(4, 0, "权利类型"));
            wSheet.addCell(new Label(5, 0, "权利性质"));
            wSheet.addCell(new Label(6, 0, "调查日期"));
            wSheet.addCell(new Label(7, 0, "所有权"));
            wSheet.addCell(new Label(8, 0, "权利人证件种类"));
            wSheet.addCell(new Label(9, 0, "身份证编号"));
            wSheet.addCell(new Label(10, 0, "通讯地址"));
            wSheet.addCell(new Label(11, 0, "房屋坐落"));
            wSheet.addCell(new Label(12, 0, "联系电话"));
            wSheet.addCell(new Label(13, 0, "预编宗地代码"));
            wSheet.addCell(new Label(14, 0, "图幅号"));
            wSheet.addCell(new Label(15, 0, "权利设定方式"));
            wSheet.addCell(new Label(16, 0, "批准用途"));
            wSheet.addCell(new Label(17, 0, "批准用途编码"));
            wSheet.addCell(new Label(18, 0, "实际用途"));
            wSheet.addCell(new Label(19, 0, "实际用途编码"));
            wSheet.addCell(new Label(20, 0, "批准面积"));
            wSheet.addCell(new Label(21, 0, "宗地面积"));
            wSheet.addCell(new Label(22, 0, "占地面积"));
            wSheet.addCell(new Label(23, 0, "建筑总面积"));
            wSheet.addCell(new Label(24, 0, "北至"));
            wSheet.addCell(new Label(25, 0, "东至"));
            wSheet.addCell(new Label(26, 0, "南至"));
            wSheet.addCell(new Label(27, 0, "西至"));
            wSheet.addCell(new Label(28, 0, "发证日期"));
            wSheet.addCell(new Label(29, 0, "共有／共用权利人情况"));
            wSheet.addCell(new Label(30, 0, "说明"));
            wSheet.addCell(new Label(31, 0, "界址点位说明"));
            wSheet.addCell(new Label(32, 0, "权属调查记事"));
            wSheet.addCell(new Label(33, 0, "地籍测量记事"));
            wSheet.addCell(new Label(34, 0, "测量日期"));
            wSheet.addCell(new Label(35, 0, "邮政编码"));
            wSheet.addCell(new Label(36, 0, "总层数"));
            wSheet.addCell(new Label(37, 0, "所在层"));
            wSheet.addCell(new Label(38, 0, "房屋结构"));
            wSheet.addCell(new Label(39, 0, "墙体归属东"));
            wSheet.addCell(new Label(40, 0, "墙体归属南"));
            wSheet.addCell(new Label(41, 0, "墙体归属西"));
            wSheet.addCell(new Label(42, 0, "墙体归属北"));
            wSheet.addCell(new Label(43, 0, "产权来源"));
            wSheet.addCell(new Label(44, 0, "门牌号"));
            wSheet.addCell(new Label(45, 0, "竣工时间"));
            wSheet.addCell(new Label(46, 0, "总高度"));
            wSheet.addCell(new Label(47, 0, "检查日期"));
            wSheet.addCell(new Label(48, 0, "-01全面积"));
            wSheet.addCell(new Label(49, 0, "-01半面积"));
            wSheet.addCell(new Label(50, 0, "-01建筑材料"));
            wSheet.addCell(new Label(51, 0, "-01备注"));
            wSheet.addCell(new Label(52, 0, "01全面积"));
            wSheet.addCell(new Label(53, 0, "01半面积"));
            wSheet.addCell(new Label(54, 0, "01建筑材料"));
            wSheet.addCell(new Label(55, 0, "01备注"));
            wSheet.addCell(new Label(56, 0, "02全面积"));
            wSheet.addCell(new Label(57, 0, "02半面积"));
            wSheet.addCell(new Label(58, 0, "02建筑材料"));
            wSheet.addCell(new Label(59, 0, "02备注"));
            wSheet.addCell(new Label(60, 0, "03全面积"));
            wSheet.addCell(new Label(61, 0, "03半面积"));
            wSheet.addCell(new Label(62, 0, "03建筑材料"));
            wSheet.addCell(new Label(63, 0, "03备注"));
            wSheet.addCell(new Label(64, 0, "04全面积"));
            wSheet.addCell(new Label(65, 0, "04半面积"));
            wSheet.addCell(new Label(66, 0, "04建筑材料"));
            wSheet.addCell(new Label(67, 0, "04备注"));
            wSheet.addCell(new Label(68, 0, "05全面积"));
            wSheet.addCell(new Label(69, 0, "05半面积"));
            wSheet.addCell(new Label(70, 0, "05建筑材料"));
            wSheet.addCell(new Label(71, 0, "05备注"));
            wSheet.addCell(new Label(72, 0, "06全面积"));
            wSheet.addCell(new Label(73, 0, "06半面积"));
            wSheet.addCell(new Label(74, 0, "06建筑材料"));
            wSheet.addCell(new Label(75, 0, "06备注"));
            wSheet.addCell(new Label(76, 0, "07全面积"));
            wSheet.addCell(new Label(77, 0, "07半面积"));
            wSheet.addCell(new Label(78, 0, "07建筑材料"));
            wSheet.addCell(new Label(79, 0, "07备注"));
            wSheet.addCell(new Label(80, 0, "08全面积"));
            wSheet.addCell(new Label(81, 0, "08半面积"));
            wSheet.addCell(new Label(82, 0, "08建筑材料"));
            wSheet.addCell(new Label(83, 0, "08备注"));
            wSheet.addCell(new Label(84, 0, "09全面积"));
            wSheet.addCell(new Label(85, 0, "09半面积"));
            wSheet.addCell(new Label(86, 0, "09建筑材料"));
            wSheet.addCell(new Label(87, 0, "09备注"));
            wSheet.addCell(new Label(88, 0, "10全面积"));
            wSheet.addCell(new Label(89, 0, "10半面积"));
            wSheet.addCell(new Label(90, 0, "10建筑材料"));
            wSheet.addCell(new Label(91, 0, "10备注"));
            wSheet.addCell(new Label(92, 0, "原不动产权证书号"));
            wSheet.addCell(new Label(93, 0, "登记原因"));

            for (Feature f_zd : fs_zd) {
                int index = fs_zd.indexOf(f_zd) + 1;
                String zddm=FeatureHelper.Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, "");
                wSheet.addCell(new Label(0, index , FeatureHelper.Get(f_zd, FeatureHelper.TABLE_ATTR_ZDDM, "")));
                wSheet.addCell(new Label(1, index , FeatureHelper.Get(f_zd, "QLRXM", "")));
                String image_sfzmcl = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/") + "权利人证件号";
                String image_hkb = FileUtils.getAppDirAndMK(mapInstance.getpath_feature(f_zd) + "附件材料/") + "户口簿";
                String sfzzmcl = "";
                if (FileUtils.getFileCount(image_sfzmcl) > 0) {
                    sfzzmcl = "身份证";
                }
                if (FileUtils.getFileCount(image_hkb) > 0) {
                    sfzzmcl += ",户口簿";
                }

                wSheet.addCell(new Label(2, index , FeatureHelper.Get(f_zd, "QLRXM", "")+"(户主)"));
                wSheet.addCell(new Label(3, index, DicUtil.dic("qlrlx",FeatureHelper.Get(f_zd,"QLRLX","1"))));
                wSheet.addCell(new Label(4, index ,  DicUtil.dic("qllx",FeatureHelper.Get(f_zd,"QLLX","6"))));
                wSheet.addCell(new Label(5, index ,  DicUtil.dic("qlxz",FeatureHelper.Get(f_zd,"QLXZ","203"))));
                // TODO... 调查日期
                wSheet.addCell(new Label(6, index, ""));
                wSheet.addCell(new Label(7, index, FeatureHelper.Get(f_zd,"ZDSYQ","")));
                wSheet.addCell(new Label(8, index,  DicUtil.dic("zjzl",FeatureHelper.Get(f_zd,"QLRZJZL","1"))));
                wSheet.addCell(new Label(9, index, FeatureHelper.Get(f_zd,"QLRZJH","")));
                wSheet.addCell(new Label(10, index, FeatureHelper.Get(f_zd,"QLRTXDZ","")));
                wSheet.addCell(new Label(11, index, FeatureHelper.Get(f_zd,"ZL","")));
                wSheet.addCell(new Label(12, index, FeatureHelper.Get(f_zd,"QLRDH","")));
                wSheet.addCell(new Label(13, index,zddm.substring(zddm.length()-5)));
                wSheet.addCell(new Label(14, index,FeatureHelper.Get(f_zd,"TFH","")));
                wSheet.addCell(new Label(15, index,  DicUtil.dic("qlsdfs",FeatureHelper.Get(f_zd,"QLSDFS","1"))));
                wSheet.addCell(new Label(16, index,  DicUtil.dic("tdyt",FeatureHelper.Get(f_zd,"PZYT","072"))));
                wSheet.addCell(new Label(17, index, FeatureHelper.Get(f_zd,"PZYT","072")));
                wSheet.addCell(new Label(18, index,  DicUtil.dic("tdyt",FeatureHelper.Get(f_zd,"SJYT","072"))));
                wSheet.addCell(new Label(19, index, FeatureHelper.Get(f_zd,"SJYT","072")));
                wSheet.addCell(new Label(20, index, FeatureHelper.Get(f_zd,"SYQMJ","")));
                wSheet.addCell(new Label(21, index, FeatureHelper.Get(f_zd,"JZZDMJ","")));
                wSheet.addCell(new Label(22, index, FeatureHelper.Get(f_zd,"JZMJ","")));
                wSheet.addCell(new Label(23, index, FeatureHelper.Get(f_zd,"ZDSZB","")));
                wSheet.addCell(new Label(24, index, FeatureHelper.Get(f_zd,"ZDSZD","")));
                wSheet.addCell(new Label(25, index, FeatureHelper.Get(f_zd,"ZDSZN","")));
                wSheet.addCell(new Label(26, index, FeatureHelper.Get(f_zd,"ZDSZX","")));
                //todo 发证日期
                wSheet.addCell(new Label(27, index, ""));
                wSheet.addCell(new Label(28, index, FeatureHelper.Get(f_zd,"QLQKSM","家庭户合法共有人共有")));
                wSheet.addCell(new Label(29, index, FeatureHelper.Get(f_zd,"BZ","")));
                wSheet.addCell(new Label(30, index, FeatureHelper.Get(f_zd,"JZDWSM","")));
                //TODO 权属调查记事
                wSheet.addCell(new Label(31, index, FeatureHelper.Get(f_zd,"JZDWSM","")));
                //TODO 地籍测量记事
                wSheet.addCell(new Label(32, index, FeatureHelper.Get(f_zd,"JZDWSM","")));
                //TODO 测量日期
                wSheet.addCell(new Label(33, index, ""));
                //TODO 邮政编码
                wSheet.addCell(new Label(34, index, ""));
                //TODO 总层数
                wSheet.addCell(new Label(35, index, ""));
                //TODO 所在层
                wSheet.addCell(new Label(36, index, ""));
                //TODO 房屋结构
                wSheet.addCell(new Label(37, index, ""));
                //TODO 墙体归属东
                wSheet.addCell(new Label(38, index, ""));
                //TODO 墙体归属南
                wSheet.addCell(new Label(39, index, ""));
                //TODO 墙体归属西
                wSheet.addCell(new Label(40, index, ""));
                //TODO 墙体归属北
                wSheet.addCell(new Label(41, index, ""));
//                wSheet.addCell( new Label(5, index+1, FeatureHelper.Get(f_zd,"ZDDM","")));
//                wSheet.addCell( new Label(6, index+1, FeatureHelper.Get(f_zd,"ZDDM","")));
            }
            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getValue(String typeName, String key,String value) {
        String value_="";
        if (!TextUtils.isEmpty(value)&&!TextUtils.isEmpty(DicUtil.dic(key, value))){
            value_=DicUtil.dic(typeName, value).substring(DicUtil.dic(typeName, value).lastIndexOf("]")+1);
        }
        return value_;
    }

    public static  List<Map<String, String>> getXlsData(String filePath) throws IOException, BiffException {

        Map<Integer, String> xlsMap = getXlsMap(filePath);

        InputStream stream = new FileInputStream(filePath);
        Workbook rwb = Workbook.getWorkbook(stream);
        final Sheet sheet = rwb.getSheet(0);
        List<Map<String, String>> datas = new ArrayList<>();
        Map<Integer, String> map = new HashMap<>();
        Map<String, String> data;
        //Columns 栏   row行
        for (int i = 2; i < sheet.getRows(); i++) {
            data = new HashMap<>();
//            for (int j = 0; j < sheet.getColumns(); j++) {
//
//                data.put(xlsMap.get(j),)
//            }
            for (Integer integer : xlsMap.keySet()) {
                data.put(xlsMap.get(integer),sheet.getCell(integer,i).getContents().trim());
            }

            datas.add(data);
        }
        return datas;
    }
    // 导入excel 数据
    public static void UpdateDataToExcel(final com.ovit.app.map.model.MapInstance mapInstance, final List<Map<String, String>> datas, final AiRunnable callback){
        final List<Feature> fs_zd=new ArrayList<>();
        new AiForEach<Map<String, String>>(datas,callback){
            @Override
            public void exec() {
                final Map<String, String> item = datas.get(postion);
                MapHelper.QueryOne(mapInstance.getTable(FeatureHelper.TABLE_NAME_ZD, FeatureHelper.LAYER_NAME_ZD), "ZDDM='" + item.get(FeatureHelper.TABLE_ATTR_ZDDM) + "'", new AiRunnable() {
                    @Override
                    public <T_> T_ ok(T_ t_, Object... objects) {
                        Feature f_zd= (Feature) t_;
                        for (String key : item.keySet()) {
                            FeatureHelper.Set(f_zd,key,item.get(key));
                        }
                        fs_zd.add(f_zd);
                        AiRunnable.Ok(getNext(),t_,objects);
                        return null;
                    }
                });
            }
            @Override
            public void complet() {
                AiRunnable.Ok(callback,fs_zd,fs_zd);
            }
        };
    }
    // 获取入库模板key
    public static Map<Integer, String> getXlsMap(String filePath) throws IOException, BiffException {
        InputStream stream = new FileInputStream(filePath);
        Workbook rwb = Workbook.getWorkbook(stream);
        final Sheet sheet =  rwb.getSheet(0);
        Map<Integer, String> map = new HashMap<>();
        for (int i = 1; i < sheet.getRows(); i++) {
            for (int j = 0; j < sheet.getColumns(); j++) {
                if (i == 1) {
                    String value  = sheet.getCell(j, i).getContents().trim();
                    if (StringUtil.IsNotEmpty(value)) {
                        map.put(j, value);
                    }
                }
            }
            break;
        }
        return map;
    }
    // 获取入库模板key-value
    public static  List<Map<String, String>> getXlsMap(String filePath,String sheetName,int keyRow) throws IOException, BiffException {
        InputStream stream = new FileInputStream(filePath);
        Workbook rwb = Workbook.getWorkbook(stream);
        Sheet sheet=rwb.getSheet(sheetName);

        Map<String, String> map = null;
        List<Map<String, String>> maps= new ArrayList<>();
        for (int i = 0; i < sheet.getRows(); i++) {
            if (i <= keyRow) {
                continue;
            }
            map = new HashMap<>();
            for (int j = 0; j < sheet.getColumns(); j++) {

                String value = sheet.getCell(j, i).getContents().trim();
                String key = sheet.getCell(j, keyRow).getContents().trim();
                if (StringUtil.IsNotEmpty(value) && StringUtil.IsNotEmpty(key)) {
                    map.put(key, value);
                }
            }
            if (map!=null&&map.size()>2){
                maps.add(map);
            }
        }
        return maps;
    }

    public static  Map<String,List<Map<String, String>>> getXlsMaps(String filePath) throws IOException, BiffException {
        InputStream stream = new FileInputStream(filePath);
        Workbook rwb = Workbook.getWorkbook(stream);
        Map<Integer, String> map =null;
        List<Map<String, String>> xlsList =null;
        Map<String, String> mapData=null;
        Map<String,List<Map<String, String>>> map_ =null;
        for (Sheet sheet : rwb.getSheets()) {
            xlsList= new ArrayList<>();

            map_ = new HashMap<>();
            for (int i = 1; i < sheet.getRows(); i++) {
                if (i==1){
                    map = new HashMap<>();
                }
                for (int j = 0; j < sheet.getColumns(); j++) {
                    String value = sheet.getCell(j, i).getContents().trim();
                    if (i == 1) {
                        if (StringUtil.IsNotEmpty(value)) {
                            // key
                            map.put(j, value);
                        } else {
                            // 数据
                            mapData = new HashMap<>();
                            mapData.put(map.get(j),value);
                        }
                    }
                }
                xlsList.add(mapData);
            }
            map_.put(sheet.getName(),xlsList);
        }
        return map_;
    }
    //  入库总表
    public static void CreateStandingBook( List< Map<String, Object>> maps, Map<Integer, String> xlsData,String filePath, String filePath_badong) throws IOException, BiffException, WriteException {
        InputStream stream = new FileInputStream(filePath);
        Workbook rwb = Workbook.getWorkbook(stream);
        WritableWorkbook workbook=Workbook.createWorkbook(new File(filePath_badong),rwb);
        final WritableSheet sheet =  workbook.getSheet(0);

        for (int i = 0; i < maps.size(); i++) {
            // 每行
            Map<String, Object> map = maps.get(i);
            for (Integer j : xlsData.keySet()) {
                sheet.addCell(new Label(j,i+1,map.get(xlsData.get(j))+""));
            }
        }
        workbook.write();
        workbook.close();
    }

    // 导出单图层数据
    public static void CreateStandingBookToLayer(com.ovit.app.map.model.MapInstance mapInstance, String filePath, List<Feature> fs){
        if (!FeatureHelper.isExistElement(fs)){
            // fs 没有元素
            return;
        }
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            boolean isWrite = file.canWrite();
            workbook = Workbook.createWorkbook(file);
            WritableSheet wSheet = workbook.createSheet(fs.get(0).getFeatureTable().getTableName(), 0);
            Object [] attrs=fs.get(0).getAttributes().keySet().toArray();
            for (int i = 0; i < fs.size(); i++) {
                Label label=null;
                for (int j = 0; j < attrs.length; j++) {
                    if (i==0){
                        label = new Label(j, i,  attrs[j].toString());
                    }else {
                        label = new Label(j, i,FeatureHelper.Get(fs.get(i), attrs[j].toString(),""));
                    }
                    wSheet.addCell(label);
                }
            }
            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 导出多图层数据
    public static void CreateStandingBookToLayers(com.ovit.app.map.model.MapInstance mapInstance, String filePath, List<List<Feature>> lists){
        if (!FeatureHelper.isExistElement(lists)){
            // fs 没有元素
            return;
        }
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            boolean isWrite = file.canWrite();
            WritableSheet wSheet =null;
            workbook = Workbook.createWorkbook(file);
            for (int a = 0; a < lists.size(); a++) {
                List<Feature> fs=lists.get(a);
                wSheet = workbook.createSheet(fs.get(a).getFeatureTable().getTableName(), a);
                Object [] attrs=fs.get(0).getAttributes().keySet().toArray();
                for (int i = 0; i < fs.size(); i++) {
                    Label label=null;
                    for (int j = 0; j < attrs.length; j++) {
                        if (i==0){
                            label = new Label(j, i,  attrs[j].toString());
                        }else {
                            label = new Label(j, i+1,FeatureHelper.Get(fs.get(i), attrs[j].toString(),""));
                        }
                        wSheet.addCell(label);
                    }
                }
            }
            workbook.write();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}