package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;
import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

@Table("ST_Anno")
public class STAnno {
	@com.litesuits.orm.db.annotation.Ignore
	private transient static final long serialVersionUID = 1L;
	// 设置为主键,自增
	@PrimaryKey(AssignType.AUTO_INCREMENT)
//	@PrimaryKey(AssignType.BY_MYSELF)
	// 取名为“_id”,如果此处不重新命名,就采用属性名称
	@Column("SmID")
	public int SmID;
	public int SmUserID;
	public int ClassID;
	public String Door_NUM;
	public String Cell_NUM;
	public int Basic_Layer;
	public int Anno_Type;
	public String ZH;
	public int AreaID;
	public int DimIdxX;
	public int DimIdxY;
	public int Ignore;
	public int Layer_NUM;
	public String Geometry;
}
