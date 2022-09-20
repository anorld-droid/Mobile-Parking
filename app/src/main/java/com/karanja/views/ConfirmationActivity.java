package com.karanja.views;

import static com.karanja.utils.Constants.BUSINESS_SHORT_CODE;
import static com.karanja.utils.Constants.CALLBACKURL;
import static com.karanja.utils.Constants.PARTYB;
import static com.karanja.utils.Constants.PASSKEY;
import static com.karanja.utils.Constants.TRANSACTION_TYPE;
import static com.karanja.utils.Utils.getPassword;
import static com.karanja.utils.Utils.getTimestamp;
import static com.karanja.utils.Utils.sanitizePhoneNumber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karanja.Api.ParkingApi;
import com.karanja.Api.Responses.Park.ParkingSpaceDataResponse;
import com.karanja.Api.RetrofitClient;
import com.karanja.Model.AccessToken;
import com.karanja.Model.Booking.BookingSchedule;
import com.karanja.Model.Park.ParkingSpace;
import com.karanja.Model.Park.UserPackedSpace;
import com.karanja.Model.STKPush;
import com.karanja.R;
import com.karanja.utils.DarajaApiClient;
import com.karanja.utils.SharePreference;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmationActivity extends BaseActivity {
    private TextView park, address, date_in, date_out, time_in, time_out, vehicle_name, vehicle_number, payable_amount;
    private EditText phoneNumber;
    private final String PREFERENCE_FILE_KEY = "location_pref";
    private BookingSchedule bookingSchedule;
    private Button confirm_button;
    private ProgressBar progressBar;
    private int car_park_id;
    private DarajaApiClient mApiClient;
    private ProgressDialog mProgressDialog;
    private long payment;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        getSupportActionBar().setTitle("Confirmation");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bookingSchedule = new BookingSchedule();

        park = findViewById(R.id.park_name);
        address = findViewById(R.id.park_address);
        date_in = findViewById(R.id.tv20);
        date_out = findViewById(R.id.tv21);
        time_in = findViewById(R.id.tv22);
        time_out = findViewById(R.id.tv23);
        vehicle_name = findViewById(R.id.tv25);
        vehicle_number = findViewById(R.id.tv26);
        payable_amount = findViewById(R.id.payable_amount);
        phoneNumber = findViewById(R.id.phone_number);
        confirm_button = findViewById(R.id.confirm_button);
        progressBar = findViewById(R.id.progressBar4);

        date_in.setText(SharePreference.getINSTANCE(getApplicationContext()).getINFormattedDay());
        date_out.setText(SharePreference.getINSTANCE(getApplicationContext()).getOutFormattedDay());
        time_in.setText(SharePreference.getINSTANCE(getApplicationContext()).getINFormattedTime());
        time_out.setText(SharePreference.getINSTANCE(getApplicationContext()).getOutFormattedTime());
        vehicle_name.setText(SharePreference.getINSTANCE(getApplicationContext()).getMainVehicleName());
        vehicle_number.setText(SharePreference.getINSTANCE(getApplicationContext()).getMainVehicleNumber());
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
            Date dateIn = formatter.parse(SharePreference.getINSTANCE(getApplicationContext()).getCheckIn());
            Date dateOut = formatter.parse(SharePreference.getINSTANCE(getApplicationContext()).getCheckOut());

            long time_difference = dateOut.getTime() - dateIn.getTime();
            long hours_difference = (time_difference / (1000 * 60 * 60)) % 24;
            payment = hours_difference * 50;
            payable_amount.setText(String.valueOf("Ksh. " + payment));
        } catch (Exception e) {
            Log.d("TAG", "" + e);
        }
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String park_name = sharedPref.getString("Park_Name", "null");
        String park_address = sharedPref.getString("Park_Address", "null");
        car_park_id = sharedPref.getInt("id", 0);

        park.setText(park_name);
        address.setText(park_address);

        mProgressDialog = new ProgressDialog(this);
        mApiClient = new DarajaApiClient();
        mApiClient.setIsDebug(true); //Set True to enable logging, false to disable.

        getAccessToken();
        mDatabase  = FirebaseFirestore.getInstance();
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmBooking();
            }
        });
    }

    public void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {

                if (response.isSuccessful()) {
                    mApiClient.setAuthToken(response.body().accessToken);
                    showToast("Access " + response.code());

                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {
                showToast("Failure " + "d");
            }
        });
    }


    public void confirmBooking() {
        confirm_button.setClickable(false);
        progressBar.setVisibility(View.VISIBLE);
        performSTKPush(phoneNumber.getText().toString(), (int) payment);
    }


    private void bookSpace() {
        String token = SharePreference.getINSTANCE(getApplicationContext()).getAccessToken();

        ParkingApi parkingApi = RetrofitClient.getInstance().create(ParkingApi.class);
        bookingSchedule.setCheckIn(SharePreference.getINSTANCE(this).getCheckIn());
        bookingSchedule.setCheckOut(SharePreference.getINSTANCE(this).getCheckOut());
        bookingSchedule.setVehicleNo(SharePreference.getINSTANCE(getApplicationContext()).getMainVehicleNumber());
        parkingApi.scheduleParkingSpace(token, car_park_id, bookingSchedule).enqueue(new Callback<ParkingSpaceDataResponse<UserPackedSpace>>() {
            @Override
            public void onResponse(Call<ParkingSpaceDataResponse<UserPackedSpace>> call, Response<ParkingSpaceDataResponse<UserPackedSpace>> response) {
                if (response.isSuccessful()) {
                    showAlert();
                    progressBar.setVisibility(View.INVISIBLE);

                } else if (response.code() == 500) {
                    showAlert();
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    showToast("Failure " + response.code());
                    confirm_button.setClickable(true);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ParkingSpaceDataResponse<UserPackedSpace>> call, Throwable t) {
                showToast("Please Try again");
                confirm_button.setClickable(true);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    public void editBooking(View view) {
        Intent intent = new Intent(ConfirmationActivity.this, ScheduleActivity.class);
        startActivity(intent);
        finish();
    }

    private void showAlert() {
        Button invoice;
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        final View customView = getLayoutInflater().inflate(R.layout.confirmation_dialogue, null);
        invoice = customView.findViewById(R.id.invoice_btn);
        myDialog.setView(customView);
        final AlertDialog dialog = myDialog.create();
        dialog.show();
        SharePreference.getINSTANCE(this).setDuration("-----");
        SharePreference.getINSTANCE(this).setINFormattedDate("-----");
        SharePreference.getINSTANCE(this).setOutFormattedDate("-----");
        dialog.setCanceledOnTouchOutside(false);
        invoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DocumentReference parkingSpace = mDatabase.collection("parkingspaces")
                        .document("Naivas");
                parkingSpace.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        ParkingSpace parkingSpace1 = documentSnapshot.toObject(ParkingSpace.class);
                        int status = parkingSpace1.getStatus() - 1;
                        parkingSpace
                                .update("status", status)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("TAG", "DocumentSnapshot successfully updated!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("TAG", "Error updating document", e);
                                    }
                                });
                    }
                });


                Intent intent = new Intent(ConfirmationActivity.this, InvoiceActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void performSTKPush(String phone_number, int amount) {
        mProgressDialog.setMessage("Processing your request");
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        String timestamp = getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
                timestamp,
                TRANSACTION_TYPE,
                String.valueOf(amount),
                sanitizePhoneNumber(phone_number),
                PARTYB,
                sanitizePhoneNumber(phone_number),
                CALLBACKURL,
                "Karanja Ltd", //Account reference
                "Car Parking Lot STK PUSH by Karanja"  //Transaction description
        );


        mApiClient.setGetAccessToken(false);
        //Sending the data to the Mpesa API, remember to remove the logging when in production.

        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
                mProgressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        showAlert();
                    } else {
                        Toast.makeText(getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();
                        Log.d("TAGElse", response.toString());
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    Log.d("TAGcat", response.toString());
                    e.printStackTrace();
                }
            }

        @Override
        public void onFailure (@NonNull Call < STKPush > call, @NonNull Throwable t){
            Log.d("TAGTHRO", t.toString());
            mProgressDialog.dismiss();
        }
    });
}
}
