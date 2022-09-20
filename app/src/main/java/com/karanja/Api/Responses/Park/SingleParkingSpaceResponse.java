package com.karanja.Api.Responses.Park;


import com.karanja.Model.Park.ParkingSpace;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class SingleParkingSpaceResponse {
    @SerializedName("status")
    @Expose
    private boolean status;
    @SerializedName("details")
    @Expose
    private ParkingSpace parkingSpace;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public ParkingSpace getParkingSpace() {
        return parkingSpace;
    }

    public void setParkingSpace(ParkingSpace parkingSpace) {
        this.parkingSpace = parkingSpace;
    }

}
