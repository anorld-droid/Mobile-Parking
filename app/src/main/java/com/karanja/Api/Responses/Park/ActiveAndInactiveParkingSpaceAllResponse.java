package com.karanja.Api.Responses.Park;

import com.karanja.Model.Park.ParkingSpace;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;



public class ActiveAndInactiveParkingSpaceAllResponse {

    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("status")
    @Expose
    private boolean status;
    @SerializedName("details")
    @Expose
    private List<ParkingSpace> parkingSpaces = null;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<ParkingSpace> getParkingSpaces() {
        return parkingSpaces;
    }

    public void setParkingSpaces(List<ParkingSpace> parkingSpaces) {
        this.parkingSpaces = parkingSpaces;
    }
}
