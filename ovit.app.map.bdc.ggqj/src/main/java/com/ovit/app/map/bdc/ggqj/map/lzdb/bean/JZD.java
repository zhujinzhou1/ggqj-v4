package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;
import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

import java.util.Date;

@Table("JZD")
public class JZD {
    @Ignore
    private transient static final long serialVersionUID = 1L;
    // 设置为主键,自增
    @PrimaryKey(AssignType.AUTO_INCREMENT)
 //  @PrimaryKey(AssignType.BY_MYSELF)
    // 取名为“_id”,如果此处不重新命名,就采用属性名称
    @Column("SmID")
    public int SmID;
    public int SmUserID;

    public String OBJECTID;
    public String BSM;       //标识码
    public String ZDZHDM;   //宗地代码
    public String YSDM;     //要素代码
    public String JBLX;     //界标类型（1-10） 1钢筋 2水泥桩  3石灰桩  4喷漆  5瓷标志 6 无标志  7其它  10 墙角
    public String JZDH;     //界址点号
    public String SXH;     //顺序号
    public String JZDLX;    //界址点类型
    public String XMDM;     //项目代码
    public Double XZBZ;     //X坐标轴
    public Double YZBZ;     //Y坐标轴
    public String JZXWZ;   //界址线位置
    public String JZXLB;   //界址线类别
    public String JZJG;
    public String GLOBALID;
    public String SHAPE;
    public String gdb_archive_oid;
    public Date gdb_from_date;
    public Date gdb_to_date;
}
