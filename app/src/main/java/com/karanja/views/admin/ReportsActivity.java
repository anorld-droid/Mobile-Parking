package com.karanja.views.admin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karanja.Model.Park.Report;
import com.karanja.R;
import com.karanja.adapter.ReportAdapter;

import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
public class ReportsActivity extends AppCompatActivity {
    private FirebaseFirestore mDatabase;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private List<Report> reports;
    private TextView noReports;
    private Button saveReport;
    // declaring width and height
    // for our PDF file.
    private int pageHeight = 1120;
    private int pagewidth = 792;

    private static final int PERMISSION_REQUEST_CODE = 200;


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
        saveReport = findViewById(R.id.save_report);
        reportAdapter = new ReportAdapter(this, reports);

        noReports.setVisibility(View.INVISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(reportAdapter);

        getReports();
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestPermission();
            }
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
        saveReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDF();
            }
        });

    }

    private void generatePDF() {
        PdfDocument document = new PdfDocument();

        ArrayList<LinearLayout> linearLayoutArrayList = reportAdapter.getCardViewList();
        for (int i = 0; i < linearLayoutArrayList.size(); i++) {
            // Iterate till the last of the array list and add each view individually to the document.
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(linearLayoutArrayList.get(i).getMeasuredWidth(),
                    linearLayoutArrayList.get(i).getMeasuredHeight(), i).create();

            // create a new page from the PageInfo
            PdfDocument.Page page = document.startPage(pageInfo);
            linearLayoutArrayList.get(i).draw(page.getCanvas());
            // do final processing of the page
            document.finishPage(page);
        }

        // all created files will be saved at path /sdcard/PDFDemo_AndroidSRC/
        File outputFile = new File(Environment.getExternalStorageDirectory().getPath(), "CarParkReport.pdf");

        try {
            outputFile.createNewFile();
            OutputStream out = new FileOutputStream(outputFile);
            document.writeTo(out);
            document.close();
            out.close();
            Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestPermission() {
        // requesting permissions if not provided.
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}