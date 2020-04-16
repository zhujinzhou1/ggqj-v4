package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

import java.util.Date;

@Table("T_Door")
public class TDoor {
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
    public String CELL_NUM;
    public String DOOR_NUM;
    public String ZH;
    public String FW_LAYERS;
    public int STRUCT;
    public double TNJZ_AREA;
    public double FT_AREA;
    public double JZ_AREA;
    public double SY_AREA;
    public int DESIGN_YT;
    public int FACT_YT;
    public String GHYT;
    public int FW_CB;
    public int BIRTH_DATE;
    public String FW_DH;
    public int Right_NUM;
    public int DOOR_TYPE;
    public String YS_ADDRESS;
    public int MAP_ID;
    public String JC_DOORNUM;
    public int FLOOR_NUM;
    public int FLOORDOWN_NUM;
    public double FT_COEFF;
    public double YT_AREA;
    public double YC_AREA;
    public String DOOR_REMARK;
    public int SvEditMark;
    public int BRKBZ;
    public int Region_Num;
    public double ZD_AREA;
    public String Right_NUMEX;
    public int WALL_EAST;
    public int WALL_SOUTH;
    public int WALL_WEST;
    public int WALL_NORTH;
    public double TD_FTTDMJ;
    public double TD_DYTDMJ;
    public double TD_GYTDMJ;
    public Date TD_QSSJ;
    public Date TD_JSSJ;
    public int TD_SYQX;
    public int PSM2_FWID;
    public int DOORNUM_SORT;
    public int YT_COUNT;
    public String FW_LAYERSEX;
    public String FW_ADDRESS;
    public String CQLY;
    public double ZJD_AREA;

}
