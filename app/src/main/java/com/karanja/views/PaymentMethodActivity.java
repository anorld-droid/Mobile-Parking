package com.karanja.views;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.karanja.R;

public class PaymentMethodActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);
    }

    public void setTitle(String title) {
        getActionBar().setTitle(title);
    }
}