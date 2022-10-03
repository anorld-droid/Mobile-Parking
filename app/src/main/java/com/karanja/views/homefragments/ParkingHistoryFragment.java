package com.karanja.views.homefragments;

import static com.facebook.FacebookSdk.getApplicationContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karanja.Model.Vehicle;
import com.karanja.Model.review.ParkingHistoryModel;
import com.karanja.R;
import com.karanja.adapter.ParkingHistoryAdapter;
import com.karanja.utils.SharePreference;

import java.util.ArrayList;
import java.util.List;

public class ParkingHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ParkingHistoryAdapter parkingHistoryAdapter;
    private List<ParkingHistoryModel> parkingHistory;
    private FirebaseFirestore mDatabase;
    private String userID;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_parking_history, container, false);
        mDatabase = FirebaseFirestore.getInstance();
        userID = SharePreference.getINSTANCE(getApplicationContext()).getUser();



        parkingHistory = new ArrayList<>();
        recyclerView = root.findViewById(R.id.recyclerView);
        parkingHistoryAdapter = new ParkingHistoryAdapter(this.getContext(), parkingHistory);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(parkingHistoryAdapter);
        Task<QuerySnapshot> collectionRef = mDatabase.collection("parkingspaces").document(userID).collection("history").get();
        collectionRef.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.exists()) {
                            ParkingHistoryModel parkingHistoryModel = document.toObject(ParkingHistoryModel.class);
                            parkingHistoryModel.setId(document.getId());
                            parkingHistory.add(parkingHistoryModel);
                            parkingHistoryAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to retrieve history", Toast.LENGTH_LONG).show();
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });








        return root;
    }
}
