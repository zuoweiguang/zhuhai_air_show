package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

import java.util.List;

public class History {

    private int operateStatus;

    private String operateDate;

    private int evaluateQuality;

    private double evaluateIntegrity;

    private int lifecycle;

	/*private String recordSource = "001000010000";
	
	private String standardChiNameSource = "003000010000";
	
	private String englishNameSource = "002000010000";
	
	private String chiAddressSource = "004000010000";
	
	private String contactSource = "005000010000";*/

    private SourceFlags sourceFlags = new SourceFlags();
	
/*	public String getRecordSource() {
		return recordSource;
	}

	public void setRecordSource(String recordSource) {
		this.recordSource = recordSource;
	}

	public String getStandardChiNameSource() {
		return standardChiNameSource;
	}

	public void setStandardChiNameSource(String standardChiNameSource) {
		this.standardChiNameSource = standardChiNameSource;
	}

	public String getEnglishNameSource() {
		return englishNameSource;
	}

	public void setEnglishNameSource(String englishNameSource) {
		this.englishNameSource = englishNameSource;
	}

	public String getChiAddressSource() {
		return chiAddressSource;
	}

	public void setChiAddressSource(String chiAddressSource) {
		this.chiAddressSource = chiAddressSource;
	}

	public String getContactSource() {
		return contactSource;
	}

	public void setContactSource(String contactSource) {
		this.contactSource = contactSource;
	}*/

    private List<EditHistory> editHistory;


    public SourceFlags getSourceFlags() {
        return sourceFlags;
    }

    public void setSourceFlags(SourceFlags sourceFlags) {
        this.sourceFlags = sourceFlags;
    }

    public int getOperateStatus() {
        return operateStatus;
    }

    public void setOperateStatus(int operateStatus) {
        this.operateStatus = operateStatus;
    }

    public String getOperateDate() {
        return operateDate;
    }

    public void setOperateDate(String operateDate) {
        this.operateDate = operateDate;
    }

    public int getEvaluateQuality() {
        return evaluateQuality;
    }

    public void setEvaluateQuality(int evaluateQuality) {
        this.evaluateQuality = evaluateQuality;
    }

    public double getEvaluateIntegrity() {
        return evaluateIntegrity;
    }

    public void setEvaluateIntegrity(double evaluateIntegrity) {
        this.evaluateIntegrity = evaluateIntegrity;
    }

    public int getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(int lifecycle) {
        this.lifecycle = lifecycle;
    }

    public List<EditHistory> getEditHistory() {
        return editHistory;
    }

    public void setEditHistory(List<EditHistory> editHistory) {
        this.editHistory = editHistory;
    }


}
