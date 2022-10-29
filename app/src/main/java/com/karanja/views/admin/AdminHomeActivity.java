package com.karanja.views.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karanja.Model.Park.ParkingSpace;
import com.karanja.Model.Park.SlotDetails;
import com.karanja.R;
import com.karanja.adapter.AdminBookingsAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity {
    private FirebaseFirestore mDatabase;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private AdminBookingsAdapter adminBookingsAdapter;
    private List<SlotDetails> slots;
    private TextView noActiveSlots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        getSupportActionBar().setTitle("Administrator");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        slots = new ArrayList<>();
        mDatabase = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.loading_progress_bar);
        recyclerView = findViewById(R.id.current_bookings_recycler_view);
        noActiveSlots = findViewById(R.id.no_bookings);
        adminBookingsAdapter = new AdminBookingsAdapter(this, slots);

        noActiveSlots.setVisibility(View.INVISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adminBookingsAdapter);


        getBookings();

    }

    private void getBookings() {
        progressBar.setVisibility(View.VISIBLE);
        DocumentReference docRef = mDatabase.collection("parkingspaces").document("Naivas");
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            ParkingSpace parkingSpace = documentSnapshot.toObject(ParkingSpace.class);
            assert parkingSpace != null;
            List<SlotDetails> newSlots = parkingSpace.getSlots();
            for(SlotDetails slotDetails : newSlots){
                if (slotDetails.getOccupant() != null){
                    slots.add(slotDetails);
                }
            }
            adminBookingsAdapter.notifyDataSetChanged();
            if (slots.isEmpty()) {
                noActiveSlots.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
        });
    }
}