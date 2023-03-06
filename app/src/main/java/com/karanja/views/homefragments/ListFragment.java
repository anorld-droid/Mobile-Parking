package com.karanja.views.homefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karanja.Model.Park.ParkingSpace;
import com.karanja.R;
import com.karanja.adapter.AddressAdapter;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {
    private RecyclerView recyclerView;
    private AddressAdapter addressAdapter;
    private List<ParkingSpace> ParkingSpace;
    private ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_list, container, false);
        ParkingSpace = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        recyclerView = root.findViewById(R.id.map_list_view);
        progressBar = root.findViewById(R.id.progressBar3);
        progressBar.setVisibility(View.VISIBLE);
        addressAdapter = new AddressAdapter(getContext(), ParkingSpace);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(addressAdapter);

        DocumentReference docRef = db.collection("parkingspaces").document("Maseno");
        progressBar.setVisibility(View.VISIBLE);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            ParkingSpace parkingSpace = documentSnapshot.toObject(ParkingSpace.class);
            ParkingSpace.add(parkingSpace);
            addressAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);

        });

        return root;
    }
}
