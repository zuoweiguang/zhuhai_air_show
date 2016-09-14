package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

public class Name {

    private int nameGrpId;

    private int nameId;

    private String nameStr;

    private String nameStrPinyin;

    private int nameClass;


    private int type;

    private String langCode;


    public int getNameGrpId() {
        return nameGrpId;
    }

    public void setNameGrpId(int nameGrpId) {
        this.nameGrpId = nameGrpId;
    }

    public int getNameId() {
        return nameId;
    }

    public void setNameId(int nameId) {
        this.nameId = nameId;
    }


    public String getNameStr() {
        return nameStr;
    }

    public void setNameStr(String nameStr) {
        this.nameStr = nameStr;
    }


    public String getNameStrPinyin() {
        return nameStrPinyin;
    }

    public void setNameStrPinyin(String nameStrPinyin) {
        this.nameStrPinyin = nameStrPinyin;
    }

    public int getNameClass() {
        return nameClass;
    }

    public void setNameClass(int nameClass) {
        this.nameClass = nameClass;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }


}
