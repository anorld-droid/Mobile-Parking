package com.karanja.views;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karanja.Model.review.NotificationModel;
import com.karanja.R;
import com.karanja.adapter.NotificationAdapter;
import com.karanja.utils.SharePreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


public class NotificationFragment extends Fragment {
    ArrayList<NotificationModel> mNotifications;
    private NotificationAdapter notificationAdapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView = root.findViewById(R.id.notif_recycler);
        initViews();
        getNotifications();
        return root;
    }

    private void initViews() {

        mNotifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(mNotifications, getContext());
        recyclerView.setAdapter(notificationAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }


    private void getNotifications() {
        mNotifications.clear();
        String userID = SharePreference.getINSTANCE(getApplicationContext()).getUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Notifications")
                .child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    reference.child(Objects.requireNonNull(dataSnapshot.getKey())).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            NotificationModel notification = snapshot.getValue(NotificationModel.class);
                            assert notification != null;
                            mNotifications.add(notification);
                            notificationAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}
