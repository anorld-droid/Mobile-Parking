package com.karanja.views.homefragments;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karanja.Api.ParkingApi;
import com.karanja.Api.Responses.BaseDataResponse;
import com.karanja.Api.RetrofitClient;
import com.karanja.Model.Vehicle;
import com.karanja.R;
import com.karanja.adapter.MyVehicleAdapter;
import com.karanja.utils.SharePreference;
import com.karanja.views.CarDetailsActiviy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyVehicleFragment extends Fragment {
    private List<Vehicle> vehicleList;
    private RecyclerView recyclerView;
    private MyVehicleAdapter myVehicleAdapter;
    private ProgressBar progressBar;
    private TextView new_text;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_my_vehicle, container, false);

        FloatingActionButton fab = root.findViewById(R.id.mv_add_vehicle);
        new_text = root.findViewById(R.id.new_text);
        progressBar = root.findViewById(R.id.progressBar);
        new_text.setVisibility(View.INVISIBLE);
        db = FirebaseFirestore.getInstance();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent transactionIntent = new Intent(getContext(), CarDetailsActiviy.class);
                startActivity(transactionIntent);
            }
        });
        recyclerView = root.findViewById(R.id.mv_recyclerView);
        myVehicleAdapter = new MyVehicleAdapter(this.getContext(), vehicleList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());

        recyclerView.setLayoutManager(layoutManager);
        vehicleList = new ArrayList<>();
        myVehicleAdapter = new MyVehicleAdapter(getContext(), vehicleList);
        recyclerView.setAdapter(myVehicleAdapter);

        getVehicles();
        return root;
    }

    private void getVehicles() {
        String userID = SharePreference.getINSTANCE(getApplicationContext()).getUser();
        Task<QuerySnapshot> collectionRef = db.collection("vehicles").document(userID).collection("myvehicles").get();
        collectionRef.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
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
            }
        });
    }
}
