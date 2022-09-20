package com.karanja.Api.Responses.LoginReg;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class VerificationResponse {

    @SerializedName("registered")
    @Expose
    private boolean registered;

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

}
