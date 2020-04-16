package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;
import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

import java.util.Date;


@Table("QLRXX")
public class QLRXX {
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
	public String XM;
	public String XB;
	public String MZ;
	public String XMDM;
	public String QLRLX;
	public String QLRDM;
	public String YSDM;
	public String BDCDYH;
	public String SXH;
	public String QLRMC;
	public String BDCQZH;
	public String QZYSXLH;
	public String SFCZR;
	public String ZJH;
	public String FZJG;
	public String SSHY;
	public String GJ;
	public String HJSZSS;
	public String DH;
	public String DZ;
	public String YB;
	public String GZDW;
	public String DZYJ;
	public String QLBL;
	public String GYFS;
	public String GYQK;
	public String BZ;
    public Date CSRQ;
	public String YHZGX;
	public String ZJZL;
	public String GLOBALID;
	public String gdb_archive_oid;
	public Date gdb_from_date;
	public Date gdb_to_date;
}
