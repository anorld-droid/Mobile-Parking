package com.karanja.views.homefragments;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karanja.Model.Park.ParkingSpace;
import com.karanja.Model.Park.UserPackedSpace;
import com.karanja.Model.Vehicle;
import com.karanja.Model.review.ParkingHistoryModel;
import com.karanja.R;
import com.karanja.adapter.CurrentSpaceAdapter;
import com.karanja.adapter.ParkingHistoryAdapter;
import com.karanja.utils.SharePreference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class CurrentSpaceFragment extends Fragment {
    private RecyclerView recyclerView;
    private CurrentSpaceAdapter currentSpaceAdapter;
    private List<UserPackedSpace> userPackedSpaces;
    private FirebaseFirestore mDatabase;
    private String userID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_current_space, container, false);
        userPackedSpaces = new ArrayList<>();
        mDatabase = FirebaseFirestore.getInstance();

        recyclerView = root.findViewById(R.id.current_space_recycler_view);
        currentSpaceAdapter = new CurrentSpaceAdapter(this.getContext(), userPackedSpaces);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(currentSpaceAdapter);
        userID = SharePreference.getINSTANCE(getApplicationContext()).getUser();

        Task<QuerySnapshot> collectionRef = mDatabase.collection("parkingspaces").document(userID).collection("current").get();
        collectionRef.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.exists()) {
                        UserPackedSpace userPackedSpace = document.toObject(UserPackedSpace.class);
                        userPackedSpace.setId(document.getId());
                        userPackedSpaces.add(userPackedSpace);
                        currentSpaceAdapter.notifyDataSetChanged();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "Failed to retrieve history", Toast.LENGTH_LONG).show();
                Log.d("TAG", "Error getting documents: ", task.getException());
            }
        });

        return root;
    }


}
