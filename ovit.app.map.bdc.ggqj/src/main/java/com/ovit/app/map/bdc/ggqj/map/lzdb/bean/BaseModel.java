package com.ovit.app.map.bdc.ggqj.map.lzdb.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.Ignore;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.enums.AssignType;

import java.io.Serializable;

/**
 * Created by Lichun on 2016/10/13.
 */

public class BaseModel implements Serializable {
    @Ignore
    private transient static final long serialVersionUID = 1L;
    // 设置为主键,自增
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    // 取名为“_id”,如果此处不重新命名,就采用属性名称
    @Column("F_ID")
    public transient int F_ID;

}
