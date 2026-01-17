package com.transiap.model;

public class LocationUpdate {
    private final double latitude;
    private final double longitude;
    private final String vehicleId;

    public LocationUpdate(double latitude, double longitude, String vehicleId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.vehicleId = vehicleId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getVehicleId() {
        return vehicleId;
    }
}
