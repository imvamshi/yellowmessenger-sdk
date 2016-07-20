package com.yellowmessenger.sdk.models;

import java.io.Serializable;

public class Location implements Serializable{
    private Double lat;
    private Double lng;
    private String name;
    private String address;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    @Override
    public String toString() {
        return lat+","+lng;
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

    public static Location fromLocation(android.location.Location location){
        Location location1 = new Location();
        location1.setLat(location.getLatitude());
        location1.setLng(location.getLongitude());
        return location1;
    }
}
