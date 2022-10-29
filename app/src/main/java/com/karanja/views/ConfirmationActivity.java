package com.karanja.views;

import static com.karanja.utils.Constants.BUSINESS_SHORT_CODE;
import static com.karanja.utils.Constants.CALLBACKURL;
import static com.karanja.utils.Constants.PARTYB;
import static com.karanja.utils.Constants.PASSKEY;
import static com.karanja.utils.Constants.TRANSACTION_TYPE;
import static com.karanja.utils.Utils.getPassword;
import static com.karanja.utils.Utils.getTimestamp;
import static com.karanja.utils.Utils.sanitizePhoneNumber;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karanja.Model.AccessToken;
import com.karanja.Model.Booking.BookingSchedule;
import com.karanja.Model.Park.ParkingSpace;
import com.karanja.Model.Park.SlotDetails;
import com.karanja.Model.Park.UserPackedSpace;
import com.karanja.Model.STKPush;
import com.karanja.R;
import com.karanja.utils.DarajaApiClient;
import com.karanja.utils.SharePreference;

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmationActivity extends BaseActivity {
    private String TAG = "PARKING_SLOT";
    private TextView park, address, date_in, date_out, time_in, time_out, vehicle_name, vehicle_number, payable_amount, parking_slot;
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
    String checkIn;
    String checkOut;

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
        parking_slot = findViewById(R.id.parking_slot_tv5);
        checkIn = SharePreference.getINSTANCE(getApplicationContext()).getINFormattedDay();
        date_in.setText(checkIn);
        checkOut = SharePreference.getINSTANCE(getApplicationContext()).getOutFormattedDay();
        date_out.setText(checkOut);
        time_in.setText(SharePreference.getINSTANCE(getApplicationContext()).getINFormattedTime());
        time_out.setText(SharePreference.getINSTANCE(getApplicationContext()).getOutFormattedTime());
        vehicle_name.setText(SharePreference.getINSTANCE(getApplicationContext()).getMainVehicleName());
        vehicle_number.setText(SharePreference.getINSTANCE(getApplicationContext()).getMainVehicleNumber());
        phoneNumber.setText(SharePreference.getINSTANCE(getApplicationContext()).getPhoneNumber());
        parking_slot.setText(SharePreference.getINSTANCE(getApplicationContext()).getPickedSlot());
        try {
            String[] dur = SharePreference.getINSTANCE(getApplicationContext()).getDuration().split(" ");
            long hour = Integer.parseInt(dur[0]) * 50L;
            long minutes = Integer.parseInt(dur[2]) * 50L;
            payment = hour + minutes / 60;
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
        mDatabase = FirebaseFirestore.getInstance();
        confirm_button.setOnClickListener(view -> {
            if (payment < 25) {
                Toast.makeText(getApplicationContext(), "Only bookings above 30mins are allowed", Toast.LENGTH_SHORT).show();
            } else {
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
//                    showToast("Access " + response.code());
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
        checkIn += ", " + SharePreference.getINSTANCE(getApplicationContext()).getINFormattedTime();
        checkOut += ", " + SharePreference.getINSTANCE(getApplicationContext()).getOutFormattedTime();
        dialog.setCanceledOnTouchOutside(false);
        String userID = SharePreference.getINSTANCE(getApplicationContext()).getUser();
        invoice.setOnClickListener(view -> {
            String[] slotNumber = SharePreference.getINSTANCE(getApplicationContext()).getPickedSlot().split(" ");
            int slot = Integer.parseInt(slotNumber[1]);
            UserPackedSpace userPackedSpace = new UserPackedSpace();
            Random rand = new Random();
            String bookingId = String.format("%06d", rand.nextInt(999999));
            SharePreference.getINSTANCE(getApplicationContext()).setLoggedUserId(bookingId);
            userPackedSpace.setUserId(slot);
            userPackedSpace.setCarParkBookingId(bookingId);
            userPackedSpace.setCheckIn(checkIn);
            userPackedSpace.setCheckOut(checkOut);
            userPackedSpace.setVehicleNo(SharePreference.getINSTANCE(getApplicationContext()).getMainVehicleNumber());
            userPackedSpace.setOwner(SharePreference.getINSTANCE(getApplicationContext()).getMainVehicleName());
            userPackedSpace.setAmount(String.valueOf(payment));
            mDatabase.collection("parkingspaces").document(userID).collection("current")
                    .add(userPackedSpace)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
            updateSlots(slot, bookingId);

            Intent intent = new Intent(ConfirmationActivity.this, InvoiceActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void updateSlots(int slot, String id) {
        DocumentReference parkingSpace = mDatabase.collection("parkingspaces")
                .document("Naivas");
        parkingSpace.get().addOnSuccessListener(documentSnapshot -> {
            ParkingSpace parkingSpace1 = documentSnapshot.toObject(ParkingSpace.class);
            assert parkingSpace1 != null;
            int status = parkingSpace1.getStatus() - 1;
            SlotDetails slotDetails = new SlotDetails();
            slotDetails.setId(id);
            slotDetails.setSlot(slot);
            slotDetails.setOccupant(SharePreference.getINSTANCE(getApplicationContext()).getUser());
            slotDetails.setCheckIn(checkIn);
            slotDetails.setCheckOut(checkOut);
            List<SlotDetails> slots = parkingSpace1.getSlots();
            slots.remove(slot - 1);
            slots.add(slot - 1, slotDetails);
            parkingSpace1.setSlots(slots);
            parkingSpace1.setStatus(status);
            mDatabase.collection("parkingspaces")
                    .document("Naivas")
                    .set(parkingSpace1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
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
                        SharePreference.getINSTANCE(getApplicationContext()).setPhoneNumber(phone_number);
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
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                Log.d("TAGTHRO", t.toString());
                mProgressDialog.dismiss();
            }
        });
    }
}
