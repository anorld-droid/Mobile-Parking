package com.karanja.views.homefragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.karanja.R;
import com.karanja.utils.SharePreference;

import co.paystack.android.PaystackSdk;
// import com.karanja.views.BarterActivity;

public class PaymentMethodsFragment extends Fragment {
    private TextView   mpesa;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_payment_methods, container, false);
        mpesa = root.findViewById(R.id.mpesa_paystack);
        mpesa.setText(SharePreference.getINSTANCE(getContext()).getPhoneNumber());
        PaystackSdk.initialize(getActivity());
        return root;
    }
}
