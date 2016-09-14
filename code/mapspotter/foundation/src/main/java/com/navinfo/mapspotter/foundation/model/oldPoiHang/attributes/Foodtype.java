package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

public class Foodtype {

    private String foodtype;

    private String creditCards;

    private int parking;

    private String openHour;

    private int avgCost;

//	public Foodtype(){
//		this.setAvgCost("0");
//	}

	/*public String getAvgCost() {
        return avgCost;
	}

	public void setAvgCost(String avgCost) {
		this.avgCost = avgCost;
	}*/

    public String getFoodtype() {
        return foodtype;
    }

    public int getAvgCost() {
        return avgCost;
    }

    public void setAvgCost(int avgCost) {
        this.avgCost = avgCost;
    }

    public void setFoodtype(String foodtype) {
        this.foodtype = foodtype;
    }

    public String getCreditCards() {
        return creditCards;
    }

    public void setCreditCards(String creditCards) {
        this.creditCards = creditCards;
    }


    public int getParking() {
        return parking;
    }

    public void setParking(int parking) {
        this.parking = parking;
    }

    public String getOpenHour() {
        return openHour;
    }

    public void setOpenHour(String openHour) {
        this.openHour = openHour;
    }
}
