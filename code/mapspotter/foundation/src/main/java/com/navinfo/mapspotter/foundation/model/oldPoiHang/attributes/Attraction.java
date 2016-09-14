package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

public class Attraction {

    private int sightLevel;

    private String description;

    private String ticketPrice;

    private String openHour;

    private int parking;

    public int getSightLevel() {
        return sightLevel;
    }

    public void setSightLevel(int sightLevel) {
        this.sightLevel = sightLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(String ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getOpenHour() {
        return openHour;
    }

    public void setOpenHour(String openHour) {
        this.openHour = openHour;
    }

    public int getParking() {
        return parking;
    }

    public void setParking(int parking) {
        this.parking = parking;
    }
}
