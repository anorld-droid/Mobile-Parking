package com.karanja.views.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karanja.Model.Park.Report;
import com.karanja.R;
import com.karanja.adapter.ReportAdapter;

import java.util.ArrayList;
import java.util.List;

public class ReportsActivity extends AppCompatActivity {
    private FirebaseFirestore mDatabase;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private List<Report> reports;
    private TextView noReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        getSupportActionBar().setTitle("Reports");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reports = new ArrayList<>();
        mDatabase = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.report_progress_bar);
        recyclerView = findViewById(R.id.report_recycler_view);
        noReports = findViewById(R.id.no_reports);
        reportAdapter = new ReportAdapter(this, reports);

        noReports.setVisibility(View.INVISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(reportAdapter);

        getReports();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, AdminHomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void getReports() {
        progressBar.setVisibility(View.VISIBLE);
        mDatabase.collection("reports").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.exists()) {
                        Report fReports = document.toObject(Report.class);
                        reports.add(fReports);
                        reportAdapter.notifyDataSetChanged();

                    }
                }
                if (reports.isEmpty()) {
                    noReports.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            } else {
                Toast.makeText(getApplicationContext(), "Failed to retrieve history", Toast.LENGTH_LONG).show();
                Log.d("TAG", "Error getting documents: ", task.getException());
            }
        });
    }

}