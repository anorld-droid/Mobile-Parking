package com.karanja.Api.Responses.Park;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ParkingSpaceDataResponse<T> {

    @SerializedName("status")
    @Expose
    private boolean status;
    @SerializedName("result")
    @Expose
    private T parkingSpace;
    @SerializedName("message")
    @Expose
    private String message;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public T getParkingSpace() {
        return parkingSpace;
    }

    public void setParkingSpace(T parkingSpace) {
        this.parkingSpace = parkingSpace;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}