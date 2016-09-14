package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

import java.util.List;

public class Poi {


    private String rowkey;

//	private String longitude;
//	
//	private String latitude;
//	
//	private String guideLongitude;
//	
//	private String guideLatitude;

    private Location location;

    private Guide guide;

    private int pid;

    private String meshid;

    private String name;

    private String address;

    private String telephone;

    private String postCode;

    private int open24H;

    private String kindCode;

    private String level;

//	private String operateStatus;
//	
//	private String operateDate;
//	
//	private String evaluateQuality;
//	
//	private String evaluateIntegrity;

    private String fid;

    private String adminCode;

    private RelateParent relateParent;

    private List<RelateChildren> relateChildren;

    private List<Address> addresses;

    private List<Contact> contacts;

    private Foodtype foodtypes;

    private Parkings parkings;

    private Hotel hotel;

    private SportsVenues sportsVenues;

    private ChargingStation chargingStation;

    private GasStation gasStation;

    private Indoor indoor;

    private List<Attachment> attachments;

    private List<Brand> brands;

    private List<Name> names;

//	private List<Export> exports;
//	
//	private List<EditHistory> editHistory;
//	
//	private List<Source> sources;

    private int adminReal;

    private int importance;

    private String airportCode;

    private String website;

    private Attraction attraction;

    private Rental rental;

    private String regionInfo;

    private Hospital hospital;

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public Rental getRental() {
        return rental;
    }

    public void setRental(Rental rental) {
        this.rental = rental;
    }

    public Attraction getAttraction() {
        return attraction;
    }

    public void setAttraction(Attraction attraction) {
        this.attraction = attraction;
    }

    public int getAdminReal() {
        return adminReal;
    }

    public void setAdminReal(int adminReal) {
        this.adminReal = adminReal;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }


    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public List<Name> getNames() {
        return names;
    }

    public void setNames(List<Name> names) {
        this.names = names;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }


    public RelateParent getRelateParent() {
        return relateParent;
    }

    public void setRelateParent(RelateParent relateParent) {
        this.relateParent = relateParent;
    }

    public List<RelateChildren> getRelateChildren() {
        return relateChildren;
    }

    public void setRelateChildren(List<RelateChildren> relateChildren) {
        this.relateChildren = relateChildren;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }


    public Foodtype getFoodtypes() {
        return foodtypes;
    }

    public void setFoodtypes(Foodtype foodtypes) {
        this.foodtypes = foodtypes;
    }

    public Parkings getParkings() {
        return parkings;
    }

    public void setParkings(Parkings parkings) {
        this.parkings = parkings;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public SportsVenues getSportsVenues() {
        return sportsVenues;
    }

    public void setSportsVenues(SportsVenues sportsVenues) {
        this.sportsVenues = sportsVenues;
    }

    public ChargingStation getChargingStation() {
        return chargingStation;
    }

    public void setChargingStation(ChargingStation chargingStation) {
        this.chargingStation = chargingStation;
    }

    public GasStation getGasStation() {
        return gasStation;
    }

    public void setGasStation(GasStation gasStation) {
        this.gasStation = gasStation;
    }

    public Indoor getIndoor() {
        return indoor;
    }

    public void setIndoor(Indoor indoor) {
        this.indoor = indoor;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public List<Brand> getBrands() {
        return brands;
    }

    public void setBrands(List<Brand> brands) {
        this.brands = brands;
    }

/*	public List<Export> getExports() {
        return exports;
	}

	public void setExports(List<Export> exports) {
		this.exports = exports;
	}

	public List<EditHistory> getEditHistory() {
		return editHistory;
	}

	public void setEditHistory(List<EditHistory> editHistory) {
		this.editHistory = editHistory;
	}

	public List<Source> getSources() {
		return sources;
	}

	public void setSources(List<Source> sources) {
		this.sources = sources;
	}

	public String getOperateStatus() {
		return operateStatus;
	}

	public void setOperateStatus(String operateStatus) {
		this.operateStatus = operateStatus;
	}

	public String getOperateDate() {
		return operateDate;
	}

	public void setOperateDate(String operateDate) {
		this.operateDate = operateDate;
	}

	public String getEvaluateQuality() {
		return evaluateQuality;
	}

	public void setEvaluateQuality(String evaluateQuality) {
		this.evaluateQuality = evaluateQuality;
	}

	public String getEvaluateIntegrity() {
		return evaluateIntegrity;
	}

	public void setEvaluateIntegrity(String evaluateIntegrity) {
		this.evaluateIntegrity = evaluateIntegrity;
	}*/

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Guide getGuide() {
        return guide;
    }

    public void setGuide(Guide guide) {
        this.guide = guide;
    }


    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }


    public String getMeshid() {
        return meshid;
    }

    public void setMeshid(String meshid) {
        this.meshid = meshid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }


    public int getOpen24H() {
        return open24H;
    }

    public void setOpen24H(int open24h) {
        open24H = open24h;
    }

    public String getKindCode() {
        return kindCode;
    }

    public void setKindCode(String kindCode) {
        this.kindCode = kindCode;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getRegionInfo() {
        return regionInfo;
    }

    public void setRegionInfo(String regionInfo) {
        this.regionInfo = regionInfo;
    }

}
