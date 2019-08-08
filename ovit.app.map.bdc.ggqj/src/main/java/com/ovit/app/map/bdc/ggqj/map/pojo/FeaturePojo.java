package com.ovit.app.map.bdc.ggqj.map.pojo;

import android.text.TextUtils;

import com.esri.arcgisruntime.data.Feature;
import com.ovit.app.map.custom.FeatureHelper;
import com.ovit.app.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Time        : 2019/5/20
 * Author      : xw
 * Description :
 */

public class FeaturePojo {
    private String zrzh;
    private String ljzh;
    private List <String> lc=new ArrayList();
    private String hh;
    private String name;
   public FeaturePojo(Feature f,String key){
        init(f,key);
   }
    public String getZrzh() {
        return zrzh;
    }

    public void setZrzh(String zrzh) {
        this.zrzh = zrzh;
    }

    public String getLjzh() {
        return ljzh;
    }

    public void setLjzh(String ljzh) {
        this.ljzh = ljzh;
    }

    public List<String> getLc() {
        return lc;
    }

    public void setLc(List<String> lc) {
        this.lc = lc;
    }

    public String getHh() {
        return hh;
    }
    public void setHh(String hh) {
        this.hh = hh;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    private void init(Feature f,String key) {
        if (f != null) {
            String value = FeatureHelper.Get(f, key, "");
            if (StringUtil.IsNotEmpty(value)&&isVaildFeatureDsc(value)){
                String tableName = f.getFeatureTable().getTableName();
                if (tableName.equals(FeatureHelper.TABLE_NAME_LJZ)) {
                    initFeatureLjz(value);
                } else if (tableName.equals(FeatureHelper.TABLE_NAME_Z_FSJG)) {
                    initFeatureZfsjg(value);
                } else if (tableName.equals(FeatureHelper.TABLE_NAME_H_FSJG)) {
                    initFeatureHfsjg(value);
                }
            }
        }
    }
    private boolean isVaildFeatureDsc(String value) {
        return true;
    }
    private void initFeatureHfsjg(String value) {
        String[] split = value.split(";");
        if (split.length == 2) {
            this.name = split[0];
            String c = split[1];
            String[] hs = c.split("-");
            if (hs.length == 4) {
                this.zrzh = hs[0];
                this.ljzh = hs[1];
                if (hs[2].contains("~")) {
                    String[] c_ = hs[2].split("~");
                    for (int j = Integer.parseInt(c_[0]); j <= Integer.parseInt(c_[1]); j++) {
                        lc.add(j + "");
                    }
                }
            }
        } else {

        }
    }

    private void initFeatureZfsjg(String value) {
        String[] split = value.split(";");
        if (split.length == 2) {
            this.name = split[0];
            String c = split[1];
            String[] hs = c.split("-");
            if (hs.length == 3) {
                this.zrzh = hs[0];
                this.ljzh = hs[1];
                if (hs[2].contains("~")) {
                    String[] c_ = hs[2].split("~");
                    for (int j = Integer.parseInt(c_[0]); j <= Integer.parseInt(c_[1]); j++) {
                        lc.add(j + "");
                    }
                }
            }
        } else {

        }
    }

    private void initFeatureLjz(String value) {
        if (StringUtil.IsNotEmpty(value)) {
            String[] split = value.split("-");
            if (split.length == 4) {
                this.zrzh = split[0];
                this.ljzh = split[1];
                this.hh = split[3];
                String c = split[2];
                String[] cs = c.split(",");
                for (int i = 0; i < cs.length; i++) {
                    if (StringUtil.IsNotEmpty(cs[i])) {
                        if (!cs[i].contains("~")) {
                            lc.add(cs[i]);
                        } else {
                            String[] c_ = cs[i].split("~");
                            for (int j = Integer.parseInt(c_[0]); j <= Integer.parseInt(c_[1]); j++) {
                                lc.add(j + "");
                            }

                        }
                    }
                }
            }
        }
    }
}
