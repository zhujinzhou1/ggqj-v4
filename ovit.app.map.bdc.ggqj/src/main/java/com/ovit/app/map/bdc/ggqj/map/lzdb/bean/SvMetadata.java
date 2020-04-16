package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

import java.util.Date;

@Table("SvMetadata")
public class SvMetadata {
    @Ignore
    private transient static final long serialVersionUID = 1L;
    // 设置为主键,自增
//    @PrimaryKey(AssignType.BY_MYSELF)
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    // 取名为“_id”,如果此处不重新命名,就采用属性名称
    @Column("SmID")
    public int SmID;
    public int SmUserID;
    public String FILE_CODE;
    public int CQ_NAME;
    public String XQ_NAME;
    public int STRUCT;
    public int BIRTH_DATE;
    public int Direction;
    public String ZH;
    public String ZH_NAME;
    public String ZH_SG;
    public String FW_DH;
    public String MAPID;
    public String FLOOR_NUM;
    public String FLOORDOWN_NUM;
    public String BUILDER;
    public String First_Surveyer;
    public String Second_Surveyer;
    public String First_Auditing;
    public String Auditing;
    public String SURVEY_DATE;
    public String SurveyCorp;
    public double DrawScale;
    public int DESIGN_YT;
    public int FW_CB;
    public double TOTAL_TNAREA;
    public double TOTAL_FTAREA;
    public double TOTAL_BFTAREA;
    public double TOTAL_JZAREA;
    public int SURVEY_TYPE;
    public int BGCH;
    public String JG_CODE;
    public String YS_CODE;
    public int START_QH;
    public String CJY_TYPE;
    public String CJY_CODE;
    public String SGZ_BH1;
    public String SGZ_BH2;
    public String ZZBH;
    public String CHZZ;
    public String WALL_EAST;
    public String WALL_SOUTH;
    public String WALL_WEST;
    public String WALL_NORTH;
    public String REMARK;
    public int FTJS_SET;
    public int DH_SET;
    public String GH_CODE;
    public String SG_CODE;
    public String TD_CODE;
    public Date TD_QSSJ;
    public Date TD_JSSJ;
    public String TDYT;
    public double TD_ZDMJ;
    public int TDQLXZ;
    public double TOTAL_DXJZAREA;
    public double TOTAL_DSJZAREA;
    public double ZD_AREA;
    public String CHMD;
    public String CLFF;
    public int HalfAreaWallDeal;
    public String FW_ADDRESS;
    public String YSZDZ;
    public String GH_XX;
    public String JG_XX;
    public String TSSM;
    public String KTQK;
    public String ZLQK;
    public String CHQK;
    public String JSXMMC;
    public String FWSGH;
    public String TD_XX;

}
