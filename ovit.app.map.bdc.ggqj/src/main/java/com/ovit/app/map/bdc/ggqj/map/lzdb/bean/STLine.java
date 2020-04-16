package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

@Table("ST_Line")
public class STLine {
    @Ignore
    private transient static final long serialVersionUID = 1L;
    // 设置为主键,自增
    @PrimaryKey(AssignType.BY_MYSELF)
    // 取名为“_id”,如果此处不重新命名,就采用属性名称
    @Column("SmID")
    public int SmID;
    public int SmUserID;
    public double SmLength;
    public int SmTopoError;
    public int ClassID;
    public double Wall_Width;
    public int Line_Style;
    public String Geometry;

}
