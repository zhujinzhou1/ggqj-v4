package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;
import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

@Table("ZD")
public class ZD {
	@Ignore
	private transient static final long serialVersionUID = 1L;
	// 设置为主键,自增
	@PrimaryKey(AssignType.AUTO_INCREMENT)
//	@PrimaryKey(AssignType.BY_MYSELF)
	// 取名为“_id”,如果此处不重新命名,就采用属性名称
	@Column("SmID")
	public int SmID;
	public int SmUserID;

	public String OBJECTID;
    public int BSM;
    public String ZDDM;
	public String YSDM;
	public String BDCDYH;
	public Double ZDMJ;
	public String ZDTZM;
	public String ZL;
	public String DJ;
	public String MJDW;
	public String YT;
	public Double JG;
	public String QLLX;
	public String QLXZ;
	public String QLSDFS;
	public Double RJL;
	public Double JZMD;
	public Double JZXG;
	public String ZDSZD;
	public String ZDSZN;
	public String ZDSZX;
	public String ZDSZB;
	public String TFH;
	public String DJH;
	public String DAH;
	public String BZ;
	public String ZT;
	public String XMMC;
	public String LPMC;
	public String TDSYKSSJ;  //date换string
	public String TDSYJSSJ;  //date换string
	public Double SYQMJ;
	public Double DYMJ;
	public Double FTMJ;
	public String XMDM;
	public String PRO_ZDDM_F;
	public Double PRO_SCMJ_M;
	public Double PRO_SCMJ_Q;
	public String PRO_STATE;
	public String PRO_UPDATETIME;   //date换string
	public String PRO_USERID;
	public String QSLYZMCL;
	public String FZRXM;
	public String FZRZJLX;
	public String FZRZJBH;
	public String DLRXM;
	public String DLRZJLX;
	public String DLRZJBH;
	public String FZRDH;
	public String DLRDH;
	public String BLC;
	public String GMJJHYFLDM;
	public String YBZDDM;
	public String PZYT;
	public String PZYTDLBM;
	public Double JZZDMJ;
	public String SJYT;
	public String SJYTDLBM;
	public Double JZMJ;
	public String ZDSYQ;
	public String QLRXM;
	public String QLRDM;
	public String QLRZJZL;
	public String QLRTXDZ;
	public String QLRDH;
	public String CMXZZQZ;
	public String QZRQ;   //date换string
	public String JZDWSM;
	public String JZXZXSM;
	public String ZLZ;
	public String ZLRQ;  //date换string
	public String JCZ;
    public String JCRQ;   //date换string
	public String DZSFTY;
	public String GLBLC;
	public String QLRZJH;
	public String QLRLX;
	public String QLQKSM;
	public String JXLX;
	public String MPHM;
	public String TDZH;
	public String GLOBALID;
	public String SHAPE;
	public String gdb_from_date;  //date换string
	public String gdb_to_date;   //date换string
}
