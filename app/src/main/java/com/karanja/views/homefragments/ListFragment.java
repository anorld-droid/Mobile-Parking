package com.karanja.views.homefragments;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karanja.Api.ParkingApi;
import com.karanja.Api.Responses.Park.ParkingSpaceAllResponse;
import com.karanja.Api.RetrofitClient;
import com.karanja.Model.Park.ParkingSpace;
import com.karanja.Model.Vehicle;
import com.karanja.utils.SharePreference;
import com.karanja.R;
import com.karanja.adapter.AddressAdapter;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListFragment extends Fragment {
    private RecyclerView recyclerView;
    private AddressAdapter addressAdapter;
    private List<ParkingSpace> ParkingSpace;
    private ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_list, container, false);
        Context context = getActivity();
        ParkingSpace = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        recyclerView = root.findViewById(R.id.map_list_view);
        progressBar = root.findViewById(R.id.progressBar3);
        progressBar.setVisibility(View.VISIBLE);
        addressAdapter = new AddressAdapter(getContext(), ParkingSpace);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(addressAdapter);


        String token = SharePreference.getINSTANCE(getContext()).getAccessToken();
        ParkingApi parkingApi = RetrofitClient.getInstance().create(ParkingApi.class);
        DocumentReference docRef = db.collection("parkingspaces").document("Naivas");
        progressBar.setVisibility(View.VISIBLE);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ParkingSpace parkingSpace = documentSnapshot.toObject(ParkingSpace.class);
                ParkingSpace.add(parkingSpace);
                addressAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

            }
        });
        parkingApi.getAllParkingSpace(token).enqueue(new Callback<ParkingSpaceAllResponse>() {
            @Override
            public void onResponse(Call<ParkingSpaceAllResponse> call, Response<ParkingSpaceAllResponse> response) {
                if (response.isSuccessful()) {
                    Log.e("Response code", String.valueOf(response.code()));
                    assert response.body() != null;
                    ParkingSpace.addAll(response.body().getParkingSpaces());
                    addressAdapter.notifyDataSetChanged();

                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ParkingSpaceAllResponse> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
        return root;
    }
}
