package com.karanja.Model.Booking;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class BookingSchedule {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("check_in")
    @Expose
    private String checkIn;
    @SerializedName("check_out")
    @Expose
    private String checkOut;
    @SerializedName("slot")
    @Expose
    private String slot;


    public BookingSchedule() {
        //No args constructor for use in serialization
    }

    public BookingSchedule(String id, String checkIn, String checkOut, String slot) {
        super();
        this.id = id;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.slot = slot;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String vehicleNo) {
        this.slot = vehicleNo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}