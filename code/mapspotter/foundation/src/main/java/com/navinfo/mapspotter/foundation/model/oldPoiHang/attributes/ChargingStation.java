package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

import java.util.List;

public class ChargingStation {

    private int type;

    private String mode;

    private String plugType;

    private String payment;

    private String servicePro;

    private String chargingNum;

    private String exchangeNum;

    private String parkingNum;

//	private String plotsNum;

    private List<Plot> plots;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getPlugType() {
        return plugType;
    }

    public void setPlugType(String plugType) {
        this.plugType = plugType;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getServicePro() {
        return servicePro;
    }

    public void setServicePro(String servicePro) {
        this.servicePro = servicePro;
    }

    public String getChargingNum() {
        return chargingNum;
    }

    public void setChargingNum(String chargingNum) {
        this.chargingNum = chargingNum;
    }

    public String getExchangeNum() {
        return exchangeNum;
    }

    public void setExchangeNum(String exchangeNum) {
        this.exchangeNum = exchangeNum;
    }

    public String getParkingNum() {
        return parkingNum;
    }

    public void setParkingNum(String parkingNum) {
        this.parkingNum = parkingNum;
    }

//	public String getPlotsNum() {
//		return plotsNum;
//	}
//
//	public void setPlotsNum(String plotsNum) {
//		this.plotsNum = plotsNum;
//	}

    public List<Plot> getPlots() {
        return plots;
    }

    public void setPlots(List<Plot> plots) {
        this.plots = plots;
    }
}
