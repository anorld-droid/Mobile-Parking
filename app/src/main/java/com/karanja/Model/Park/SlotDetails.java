package com.karanja.Model.Park;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class SlotDetails {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("slot")
    @Expose
    private int slot;
    @SerializedName("occupant")
    @Expose
    private String occupant;
    @SerializedName("check_in")
    @Expose
    private String checkIn;
    @SerializedName("check_out")
    @Expose
    private String checkOut;


    public void setSlot(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public void setOccupant(String occupant) {
        this.occupant = occupant;
    }

    public String getOccupant() {
        return occupant;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}