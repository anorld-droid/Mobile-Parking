package com.karanja.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharePreference {
    private static final String ID_KEY = "com.carpark_ID_KEY";
    private static final String ID_ACCESS_KEY = "com.carpark_ID_ACCESS_KEY";
    private static final String ID_EXPIRE_KEY = "com.carpark_ID_EXPIRE_KEY";
    private static final String ID_LOGGED_IN_KEY = "com.carpark_ID_LOGGED_IN_KEY";
    private static final String VEHICLE_NAME = "com.carpark_VEHICLE_NAME";
    private static final String VEHICLE_ID = "com.carpark_VEHICLE_ID";
    private static final String VEHICLE_NUMBER = "com.carpark_VEHICLE_NUMBER";
    private static final String FORMATTED_DAY = "com.carpark_FORMATTED_DAY";
    private static final String FORMATTED_TIME = "com.carpark_FORMATTED_TIME";
    private static final String FORMATTED_DAY_OUT = "com.carpark_FORMATTED_DAY_OUT";
    private static final String FORMATTED_TIME_OUT = "com.carpark_FORMATTED_TIME_OUT";
    private static final String FORMATTED_DATE_IN = "com.carpark_FORMATTED_DATE_IN";
    private static final String FORMATTED_DATE_OUT = "com.carpark_FORMATTED_DATE_OUT";
    private static final String CHECK_IN = "com.carpark_CHECK_IN";
    private static final String CHECK_OUT = "com.carpark_CHECK_OUT";
    private static final String DURATION = "com.carpark_DURATION";
    private static final String USER = "com.karanja.user";
    private static final String ADDRESS = "com.address";
    private static final String PHONE_NUMBER = "com.phone_number";
    private static final String PICKED_SLOT = "com.picked_slot";
    private static final String USER_TYPE = "com.karanja.user_type";



    private static SharePreference INSTANCE;

    public static synchronized SharePreference getINSTANCE(Context context) {
        if (INSTANCE == null) {
            //noinspection deprecation
            INSTANCE = new SharePreference(PreferenceManager.getDefaultSharedPreferences(context));
        }
        return INSTANCE;
    }


    private SharedPreferences sharedPreferences;

    private SharePreference(SharedPreferences sharedPreferences) {
        //this.sharedPreferences = context.getSharedPreferences("SharedPref",Context.MODE_PRIVATE);
        this.sharedPreferences = sharedPreferences;

    }

    public void setLoggedUserId(String id) {
        sharedPreferences.edit().putString(ID_KEY, id).apply();
    }

    public String getLoggedUserId() {
        return sharedPreferences.getString(ID_KEY, "000000");
    }

    public void setAccesstoken(String accesstoken) {
        String fullToken = "Bearer " + accesstoken;
        sharedPreferences.edit().putString(ID_ACCESS_KEY, fullToken).apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(ID_ACCESS_KEY, "null");
    }

    public void setExpiresIn(int expiresIn) {
        sharedPreferences.edit().putInt(ID_EXPIRE_KEY, expiresIn).apply();
    }

    public int getExpiresIn() {
        return sharedPreferences.getInt(ID_EXPIRE_KEY, -1);
    }

    public void setIsUserLoggedIn(boolean isUserLoggedIn) {
        sharedPreferences.edit().putBoolean(ID_LOGGED_IN_KEY, isUserLoggedIn).apply();
    }

    public boolean getIsUserLoggedIn() {
        return sharedPreferences.getBoolean(ID_LOGGED_IN_KEY, false);
    }

    public void setMainVehicleName(String vehicleName) {
        sharedPreferences.edit().putString(VEHICLE_NAME, vehicleName).apply();
    }

    public String getMainVehicleName() {
        return sharedPreferences.getString(VEHICLE_NAME, "_____");
    }

    public void setMainVehicleNumber(String vehicleNumber) {
        sharedPreferences.edit().putString(VEHICLE_NUMBER, vehicleNumber).apply();
    }

    public String getMainVehicleNumber() {
        return sharedPreferences.getString(VEHICLE_NUMBER, "_____");
    }

    public void setMainVehicleId(int vehicleId) {
        sharedPreferences.edit().putInt(VEHICLE_ID, vehicleId).apply();
    }

    public int getMainVehicleId() {
        return sharedPreferences.getInt(VEHICLE_ID, 0);
    }

    public void setINFormattedTime(String formattedTime) {
        sharedPreferences.edit().putString(FORMATTED_TIME, formattedTime).apply();
    }

    public String getINFormattedTime() {
        return sharedPreferences.getString(FORMATTED_TIME, "-----");
    }

    public void setINFormattedDay(String formattedDay) {
        sharedPreferences.edit().putString(FORMATTED_DAY, formattedDay).apply();
    }

    public String getINFormattedDay() {
        return sharedPreferences.getString(FORMATTED_DAY, "-----");
    }

    public void setOutFormattedTime(String formattedTime) {
        sharedPreferences.edit().putString(FORMATTED_TIME_OUT, formattedTime).apply();
    }

    public String getOutFormattedTime() {
        return sharedPreferences.getString(FORMATTED_TIME_OUT, "-----");
    }

    public void setOutFormattedDay(String formattedDay) {
        sharedPreferences.edit().putString(FORMATTED_DAY_OUT, formattedDay).apply();
    }

    public String getOutFormattedDay() {
        return sharedPreferences.getString(FORMATTED_DAY_OUT, "-----");
    }

    public void setOutFormattedDate(String formattedDay) {
        sharedPreferences.edit().putString(FORMATTED_DATE_OUT, formattedDay).apply();
    }

    public String getOutFormattedDate() {
        return sharedPreferences.getString(FORMATTED_DATE_OUT, "-----");
    }

    public void setINFormattedDate(String formattedDay) {
        sharedPreferences.edit().putString(FORMATTED_DATE_IN, formattedDay).apply();
    }

    public String getINFormattedDate() {
        return sharedPreferences.getString(FORMATTED_DATE_IN, "-----");
    }


    public void setCheckOut(String mCheckOut) {
        sharedPreferences.edit().putString(CHECK_OUT, mCheckOut).apply();
    }

    public String getCheckOut() {
        return sharedPreferences.getString(CHECK_OUT, "-----");
    }

    public void setUser(String mUser) {
        sharedPreferences.edit().putString(USER, mUser).apply();
    }

    public String getUser() {
        return sharedPreferences.getString(USER, "-----");
    }

    public void setUserType(String mUserType) {
        sharedPreferences.edit().putString(USER_TYPE, mUserType).apply();
    }

    public String getUserType() {
        return sharedPreferences.getString(USER_TYPE, "-----");
    }

    public void setCheckIn(String mCheckOut) {
        sharedPreferences.edit().putString(CHECK_IN, mCheckOut).apply();
    }

    public String getCheckIn() {
        return sharedPreferences.getString(CHECK_IN, "-----");
    }

    public void setDuration(String duration) {
        sharedPreferences.edit().putString(DURATION, duration).apply();
    }

    public String getDuration() {
        return sharedPreferences.getString(DURATION, "-----");
    }

    public void setPhoneNumber(String phoneNumber) {
        sharedPreferences.edit().putString(PHONE_NUMBER, phoneNumber).apply();
    }

    public String getPhoneNumber() {
        return sharedPreferences.getString(PHONE_NUMBER, "------");
    }

    public void setAddress(String address) {
        sharedPreferences.edit().putString(ADDRESS, address).apply();
    }

    public String getAddress() {
        return sharedPreferences.getString(ADDRESS, "-----");
    }

    public void setPickedSlot(String slot) {
        sharedPreferences.edit().putString(PICKED_SLOT, slot).apply();
    }

    public String getPickedSlot() {
        return sharedPreferences.getString(PICKED_SLOT, "Not selected");
    }

}
