package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;
import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

@Table("T_Layer")
public class TLayer {
    @Ignore
    private transient static final long serialVersionUID = 1L;
    // 设置为主键,自增
    @PrimaryKey(AssignType.AUTO_INCREMENT)
 //  @PrimaryKey(AssignType.BY_MYSELF)
    // 取名为“_id”,如果此处不重新命名,就采用属性名称
    @Column("SmID")
    public int SmID;
    public int SmUserID;

    public int Layer_NUM;
    public String ZH;
    public int Basic_Layer;
    public String Layer_Name;
    public Double TNJZ_AREA;
    public Double JZ_AREA;
    public Double Public_AREA;
    public Double WALL_AREA;
    public Double YT_AREA;
    public Double Layer_Height;
    public Double AllPublic_AREA;
    public int Layer_Count;
}
