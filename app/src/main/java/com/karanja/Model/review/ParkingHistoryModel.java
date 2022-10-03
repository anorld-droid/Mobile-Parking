package com.karanja.Model.review;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ParkingHistoryModel {
    private String parkingHistoryDate;
    private String parkingHistoryTime;
    private String location;
    private String qrCode;
    private String amount;
    private String vehicleNo;
    private String carParkBookingId;
    private String checkIn;
    private String checkOut;
    private String owner;
    private String id;


    public ParkingHistoryModel() {

    }


    public void setParkingHistoryDate(String parkingHistoryDate){
        this.parkingHistoryDate = parkingHistoryDate;
    }

    public String getParkingHistoryDate(){
        return parkingHistoryDate;
    }

    public void setParkingHistoryTime(String parkingHistoryTime){
        this.parkingHistoryTime = parkingHistoryTime;
    }

    public String getParkingHistoryTime(){
        return parkingHistoryTime;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public String getLocation(){
        return location;
    }

    public void setQrCode(String qrCode){
        this.qrCode = qrCode;
    }

    public String getQrCode(){
        return qrCode;
    }

    public void setAmount(String amount){
        this.amount = amount;
    }

    public String getAmount(){
        return amount;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCarParkBookingId() {
        return carParkBookingId;
    }

    public void setCarParkBookingId(String carParkBookingId) {
        this.carParkBookingId = carParkBookingId;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
