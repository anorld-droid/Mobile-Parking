package com.karanja.views.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karanja.Model.Park.ParkingSpace;
import com.karanja.Model.Park.SlotDetails;
import com.karanja.R;
import com.karanja.Register.LoginActivity;
import com.karanja.adapter.AdminBookingsAdapter;
import com.karanja.views.HomeActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity {
    private FirebaseFirestore mDatabase;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private AdminBookingsAdapter adminBookingsAdapter;
    private List<SlotDetails> slots;
    private TextView noActiveSlots;
    private Button generateReport;

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
        generateReport = findViewById(R.id.generate_report);
        adminBookingsAdapter = new AdminBookingsAdapter(this, slots);

        noActiveSlots.setVisibility(View.INVISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adminBookingsAdapter);

        generateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminHomeActivity.this, ReportsActivity.class));
            }
        });

        getBookings();

    }

    private void getBookings() {
        progressBar.setVisibility(View.VISIBLE);
        Task<QuerySnapshot> collectionRef = mDatabase.collection("bookings").get();
        collectionRef.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.exists()) {
                        SlotDetails slotDetails = document.toObject(SlotDetails.class);
                        slots.add(slotDetails);
                        adminBookingsAdapter.notifyDataSetChanged();
                    }
                }
                if (slots.isEmpty()) {
                    noActiveSlots.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Failed to retrieve items, check your internet connection and try again.", Toast.LENGTH_LONG).show();
                Log.d("TAG", "Error getting documents: ", task.getException());
            }
            progressBar.setVisibility(View.GONE);
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                Intent intent2 = new Intent(AdminHomeActivity.this, LoginActivity.class);
                startActivity(intent2);
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}