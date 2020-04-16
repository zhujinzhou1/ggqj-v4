package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

@Table("ST_Region")
public class STRegion {
    @Ignore
    private transient static final long serialVersionUID = 1L;
    // 设置为主键,自增
    @PrimaryKey(AssignType.AUTO_INCREMENT)
//    @PrimaryKey(AssignType.BY_MYSELF)
    // 取名为“_id”,如果此处不重新命名,就采用属性名称
    @Column("SmID")
    public int SmID;
    public int SmUserID;
    public double SmArea;
    public double SmPerimeter;
    public String Door_NUM;
    public String Cell_NUM;
    public int REGION_NUM;
    public int Basic_Layer;
    public int Layer_NUM;
    public int Area_Type;
    public int Door_Layer;
    public int ClassID;
    public double Area_Coeff;
    public int Line_Style;
    public String ZH;
    public int VALUE_EX;
    public double FT_COEFF;
    public String MYLayer_List;
    public int STRUCT;
    public int BIRTH_DATE;
    public int MainID;
    public double FT_Area;
    public int FT_ClassID;
    public int SvSet;
    public int CUSTOM_TYPE;
    public String GHYT;
    public double Area_Diff;
    public double WallWidth;
    public String Basic_Code;
    public double SvArea;
    public String Name;
    public String Layer_List;
    public String FT_Name;
    public String Area_Exp;
    public String FW_ADDRESS;
    public String FT_RegionNum;
    public String Geometry;
}
