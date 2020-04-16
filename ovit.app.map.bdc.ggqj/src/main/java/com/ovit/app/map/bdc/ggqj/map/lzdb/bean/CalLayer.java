package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

@Table("Cal_Layer")
public class CalLayer {
    @Ignore
    private transient static final long serialVersionUID = 1L;
    // 设置为主键,自增
//    @PrimaryKey(AssignType.BY_MYSELF)
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    // 取名为“_id”,如果此处不重新命名,就采用属性名称
    @Column("SmID")
    public int SmID;
    public int SmUserID;
    public int Layer_NUM;
    public double TOTAL_AREA;
    public double PUBLIC_AREA;
    public double FT_AREA;
    public double RIGHT_AREA;
    public double Kc;
    public int REGION_NUM;
    public int Basic_Layer;
    public String Layer_Name;
    public double Layer_Height;

}
