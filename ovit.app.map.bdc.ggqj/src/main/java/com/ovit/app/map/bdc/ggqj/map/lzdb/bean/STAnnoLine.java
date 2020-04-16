package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

@Table("ST_AnnoLine")
public class STAnnoLine {
    @Ignore
    private transient static final long serialVersionUID = 1L;
    // 设置为主键,自增
//    @PrimaryKey(AssignType.BY_MYSELF)
    @PrimaryKey(AssignType.AUTO_INCREMENT)

    // 取名为“_id”,如果此处不重新命名,就采用属性名称
    @Column("SmID")
    public int SmID;
    public int SmUserID;
    public double SmLength;
    public int SmTopoError;
    public int ClassID;
    public String Door_NUM;
    public String Cell_NUM;
    public int Basic_Layer;
    public int Anno_Type;
    public int Line_Style;
    public String ZH;
    public int AreaID;
    public String Geometry;

}
