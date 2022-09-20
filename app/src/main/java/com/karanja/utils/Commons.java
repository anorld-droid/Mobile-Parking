package com.karanja.utils;

import android.app.Application;

import com.karanja.Model.User;




public class Commons extends Application {
    private static User storedUser;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void setUser(User user) {
        storedUser = user;
    }

    public static User getUser() {
        return storedUser;
    }
}
