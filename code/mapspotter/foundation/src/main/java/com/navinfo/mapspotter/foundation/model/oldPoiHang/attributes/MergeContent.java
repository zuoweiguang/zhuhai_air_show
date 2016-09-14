package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

public class MergeContent {

//	private String attribute;

    private String newValue;

    private String oldValue;

    private int validationMethod;

//	public String getAttribute() {
//		return attribute;
//	}
//
//	public void setAttribute(String attribute) {
//		this.attribute = attribute;
//	}

    public String getNewValue() {
        return newValue;
    }

    public int getValidationMethod() {
        return validationMethod;
    }

    public void setValidationMethod(int validationMethod) {
        this.validationMethod = validationMethod;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
}
