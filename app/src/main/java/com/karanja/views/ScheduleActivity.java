package com.karanja.views;

import static com.facebook.FacebookSdk.getApplicationContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karanja.Api.ParkingApi;
import com.karanja.Api.Responses.BaseDataResponse;
import com.karanja.Api.RetrofitClient;
import com.karanja.Model.Park.ParkingSpace;
import com.karanja.Model.Park.SlotDetails;
import com.karanja.Model.Vehicle;
import com.karanja.R;
import com.karanja.adapter.BookingVehicleAdapter;
import com.karanja.adapter.SlotAdapter;
import com.karanja.utils.SharePreference;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ScheduleActivity extends AppCompatActivity {
    private TextView tvCheckIn;
    private TextView tvCheckOut;
    private TextView tvDuration;
    final Calendar checkInDate = Calendar.getInstance();
    final Calendar checkOutDate = Calendar.getInstance();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        getSupportActionBar().setTitle("Booking Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        TextView park = findViewById(R.id.textView2);
        TextView address = findViewById(R.id.textView3);
        tvCheckIn = findViewById(R.id.textView7);
        tvCheckOut = findViewById(R.id.textView10);
        tvDuration = findViewById(R.id.textView13);
        TextView parkingSlot = findViewById(R.id.parking_slot_tv2);
        TextView vehicle_number = findViewById(R.id.textView15);
        Button schedule_btn = findViewById(R.id.schedule_button);

        String date_in = SharePreference.getINSTANCE(getApplicationContext()).getINFormattedDate();
        String date_out = SharePreference.getINSTANCE(getApplicationContext()).getOutFormattedDate();
        String mDuration = SharePreference.getINSTANCE(getApplicationContext()).getDuration();


        tvCheckIn.setText(date_in);
        tvCheckOut.setText(date_out);
        tvDuration.setText(mDuration);

        String PREFERENCE_FILE_KEY = "location_pref";
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String park_name = sharedPref.getString("Park_Name", "null");
        String park_address = sharedPref.getString("Park_Address", "null");

        park.setText(park_name);
        address.setText(park_address);
        String vehicle_no = SharePreference.getINSTANCE(this).getMainVehicleNumber();
        String parking_slot = SharePreference.getINSTANCE(this).getPickedSlot();

        vehicle_number.setText(vehicle_no);
        parkingSlot.setText(parking_slot);

        vehicle_number.setOnClickListener(view -> showVehicleAlert());

        parkingSlot.setOnClickListener(view -> showParkingSlotAlert());


        schedule_btn.setOnClickListener(v -> next());

    }

    public void next() {
        if (tvCheckIn.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Kindly set a Check-In time first", Toast.LENGTH_SHORT).show();
        } else if (tvCheckOut.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Kindly set a Check-Out time first", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(ScheduleActivity.this, ConfirmationActivity.class);
            startActivity(intent);
            finish();
        }

    }

    public void checkIn(View view) {
        new DatePickerDialog(ScheduleActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                checkInDate.set(year, monthOfYear, dayOfMonth);
                new TimePickerDialog(ScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        checkInDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        checkInDate.set(Calendar.MINUTE, minute);
                        if (!SharePreference.getINSTANCE(getApplicationContext()).getOutFormattedDate().equals("null")) {
                            setDuration();
                        }
                        tvCheckIn.setText(getFormattedDate(checkInDate));
                        SharePreference.getINSTANCE(getApplicationContext()).setINFormattedDay(getFormattedDay(checkInDate));
                        SharePreference.getINSTANCE(getApplicationContext()).setINFormattedTime(getFormattedTime(checkInDate));
                        SharePreference.getINSTANCE(getApplicationContext()).setINFormattedDate(getFormattedDate(checkInDate));
                        SharePreference.getINSTANCE(getApplicationContext()).setCheckIn(mCheckIn(checkOutDate));
                    }
                }, checkInDate.get(Calendar.HOUR_OF_DAY), checkInDate.get(Calendar.MINUTE), false).show();
            }
        }, checkInDate.get(Calendar.YEAR), checkInDate.get(Calendar.MONTH), checkInDate.get(Calendar.DATE)).show();
    }

    public void checkOut(View view) {
        if (tvCheckIn.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Kindly set a Check-In time first", Toast.LENGTH_SHORT).show();
            return;
        }
        new DatePickerDialog(ScheduleActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                checkOutDate.set(year, monthOfYear, dayOfMonth);
                new TimePickerDialog(ScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        checkOutDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        checkOutDate.set(Calendar.MINUTE, minute);
                        setDuration();
                        tvCheckOut.setText(getFormattedDate(checkOutDate));
                        SharePreference.getINSTANCE(getApplicationContext()).setOutFormattedDay(getFormattedDay(checkOutDate));
                        SharePreference.getINSTANCE(getApplicationContext()).setOutFormattedTime(getFormattedTime(checkOutDate));
                        SharePreference.getINSTANCE(getApplicationContext()).setOutFormattedDate(getFormattedDate(checkOutDate));
                        SharePreference.getINSTANCE(getApplicationContext()).setCheckOut(mCheckOut(checkOutDate));
                    }
                }, checkOutDate.get(Calendar.HOUR_OF_DAY), checkOutDate.get(Calendar.MINUTE), false).show();
            }
        }, checkOutDate.get(Calendar.YEAR), checkOutDate.get(Calendar.MONTH), checkOutDate.get(Calendar.DATE)).show();
    }

    public void setDuration() {
        long secs = (this.checkOutDate.getTimeInMillis() - this.checkInDate.getTimeInMillis()) / 1000;
        int hours = (int) (secs / 3600);
        secs = secs % 3600;
        int mins = (int) (secs / 60);
        secs = secs % 60;
        String duration = String.valueOf(hours) + " hrs " + String.valueOf(mins) + " mins ";
        tvDuration.setText(duration);
        SharePreference.getINSTANCE(getApplicationContext()).setDuration(duration);
    }

    public String getFormattedDay(Calendar date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM");
        return formatter.format(date.getTime());
    }

    public String mCheckIn(Calendar date) {
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return formatter.format(date.getTime());
    }

    public String mCheckOut(Calendar date) {
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        return formatter.format(date.getTime());
    }

    public String getFormattedTime(Calendar date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(date.getTime());
    }

    public String getFormattedDate(Calendar date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM, HH:mm:ss");
        return formatter.format(date.getTime());
    }


    private void showVehicleAlert() {
        final List<Vehicle> vehicleList;
        final RecyclerView recyclerView;
        final BookingVehicleAdapter myVehicleAdapter;
        final ProgressBar progressBar;
        final TextView new_text;
        CardView single_vehicle;


        final AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        final View customView = getLayoutInflater().inflate(R.layout.dialog_my_vehicle, null);

        final View vehicleView = getLayoutInflater().inflate(R.layout.vehicle_item, null);
        single_vehicle = vehicleView.findViewById(R.id.single_vehicle);


        new_text = customView.findViewById(R.id.new_text);
        progressBar = customView.findViewById(R.id.progressBar);
        new_text.setVisibility(View.INVISIBLE);
        vehicleList = new ArrayList<>();
        recyclerView = customView.findViewById(R.id.mv_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(layoutManager);
        myVehicleAdapter = new BookingVehicleAdapter(getApplicationContext(), vehicleList);
        recyclerView.setAdapter(myVehicleAdapter);
        String userID = SharePreference.getINSTANCE(getApplicationContext()).getUser();
        Task<QuerySnapshot> collectionRef = db.collection("vehicles").document(userID).collection("myvehicles").get();
        collectionRef.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    new_text.setVisibility(View.INVISIBLE);
                    vehicleList.add(document.toObject(Vehicle.class));
                    myVehicleAdapter.notifyDataSetChanged();
                    if (vehicleList.isEmpty()) {
                        new_text.setVisibility(View.VISIBLE);
                    }
                    progressBar.setVisibility(View.GONE);
                    Log.d("TAG", document.getId() + " => " + document.toObject(Vehicle.class));
                }
            } else {
                Toast.makeText(getApplicationContext(), "Failed to retrieve items", Toast.LENGTH_LONG).show();
                Log.d("TAG", "Error getting documents: ", task.getException());
                progressBar.setVisibility(View.GONE);

            }
        });
        myDialog.setView(customView);
        final AlertDialog dialog = myDialog.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        single_vehicle.setOnClickListener(view -> dialog.dismiss());
    }

    private void showParkingSlotAlert() {
        final List<SlotDetails> slotList;
        final RecyclerView recyclerView;
        final SlotAdapter mySlotAdapter;
        final ProgressBar progressBar;
        final TextView new_text;
        CardView single_slot;


        final AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        final View customView = getLayoutInflater().inflate(R.layout.dialog_my_vehicle, null);

        final View slotView = getLayoutInflater().inflate(R.layout.slot_layout_item, null);
        single_slot = slotView.findViewById(R.id.single_slot_card_view);


        new_text = customView.findViewById(R.id.new_text);
        progressBar = customView.findViewById(R.id.progressBar);
        new_text.setVisibility(View.INVISIBLE);
        slotList = new ArrayList<>();
        recyclerView = customView.findViewById(R.id.mv_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(layoutManager);
        mySlotAdapter = new SlotAdapter(getApplicationContext(), slotList);
        recyclerView.setAdapter(mySlotAdapter);
        DocumentReference docRef = db.collection("parkingspaces").document("Naivas");
        progressBar.setVisibility(View.VISIBLE);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            ParkingSpace parkingSpace = documentSnapshot.toObject(ParkingSpace.class);
            new_text.setVisibility(View.INVISIBLE);
            assert parkingSpace != null;
            slotList.addAll(parkingSpace.getSlots());
            mySlotAdapter.notifyDataSetChanged();
            if (slotList.isEmpty()) {
                new_text.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
        });
        myDialog.setView(customView);
        final AlertDialog dialog = myDialog.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        single_slot.setOnClickListener(view -> dialog.dismiss());
    }
}
