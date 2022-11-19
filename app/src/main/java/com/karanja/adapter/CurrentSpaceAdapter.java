package com.karanja.adapter;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karanja.Model.Park.UserPackedSpace;
import com.karanja.Model.review.NotificationModel;
import com.karanja.Model.review.ParkingHistoryModel;
import com.karanja.R;
import com.karanja.utils.SharePreference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CurrentSpaceAdapter extends RecyclerView.Adapter<CurrentSpaceAdapter.CustomViewHolder> {
    private static final String TAG = "CURRENTADAPTERCLASS";
    private Context context;
    private List<UserPackedSpace> userPackedSpaces;


    private FirebaseFirestore mDatabase;
    private String userID;


    public CurrentSpaceAdapter(Context context, List<UserPackedSpace> userPackedSpaces) {
        this.context = context;
        this.userPackedSpaces = userPackedSpaces;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.current_space, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM, HH:mm:ss");
        try {
            Date dn = new Date();
            String formatted = formatter.format(dn);
            Date today = formatter.parse(formatted);
            Date dateIn = formatter.parse(userPackedSpaces.get(position).getCheckIn());
            Date dateOut = formatter.parse(userPackedSpaces.get(position).getCheckOut());
            SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM");
            SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
            assert dateOut != null;
            String dateTo = date.format(dateOut);
            String timeTo = time.format(dateOut);
            if (dateOut.after(today)) {
                holder.bookingID.setText(userPackedSpaces.get(position).getCarParkBookingId());
                holder.parkingSlot.setText(String.valueOf("SLOT " + userPackedSpaces.get(position).getUserId()));

                assert dateIn != null;
                String inDate = date.format(dateIn);
                holder.fromDate.setText(inDate);
                String inTime = time.format(dateIn);
                holder.fromTime.setText(inTime);


                holder.toDate.setText(dateTo);

                holder.toTime.setText(timeTo);
                holder.vehicle_name.setText(userPackedSpaces.get(position).getOwner());
                holder.vehicle.setText(userPackedSpaces.get(position).getVehicleNo().toUpperCase());
            } else {
                setNotification("Your parking session has ended.");
                convert_to_history(position);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.check_out.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setNotification("You have checkout out of your parking session.");
                convert_to_history(holder.getLayoutPosition());
                Toast.makeText(context, "Checked Out", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return userPackedSpaces.size();
    }

    private void setNotification(String message) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM, HH:mm:ss");
        Date dn = new Date();
        String formatted = formatter.format(dn);
        NotificationModel notificationModel = new NotificationModel(message, R.drawable.notification_image_three, formatted);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        String userID = SharePreference.getINSTANCE(getApplicationContext()).getUser();
        reference.child(userID).child(formatted).setValue(notificationModel);
    }

    private void convert_to_history(int position) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM, HH:mm:ss");
        try {
            Date dateOut = formatter.parse(userPackedSpaces.get(position).getCheckOut());
            SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM");
            SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
            assert dateOut != null;
            String dateTo = date.format(dateOut);
            String timeTo = time.format(dateOut);
            Random rand = new Random();
            int index = rand.nextInt(10);
            String[] qrCode = {"SZEK0932", "SEJK0965", "SXEQ2313", "SARO0079", "SWER4829", "STUY1212", "DEUY7804", "YTU7654E", "HYUT4453", "OILK897Y"};
            ParkingHistoryModel phm = new ParkingHistoryModel();
            phm.setParkingHistoryDate(dateTo);
            phm.setParkingHistoryTime(timeTo);
            phm.setUserId(userPackedSpaces.get(position).getUserId());
            phm.setAmount(userPackedSpaces.get(position).getAmount());
            phm.setLocation(userPackedSpaces.get(position).getAddress());
            phm.setQrCode(qrCode[index]);
            phm.setCheckIn(userPackedSpaces.get(position).getCheckIn());
            phm.setCheckOut(userPackedSpaces.get(position).getCheckOut());
            phm.setOwner(userPackedSpaces.get(position).getOwner());
            phm.setVehicleNo(userPackedSpaces.get(position).getVehicleNo());
            phm.setCarParkBookingId(userPackedSpaces.get(position).getCarParkBookingId());
            mDatabase.collection("parkingspaces").document(userID).collection("current").document(userPackedSpaces.get(position).getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("CURRENT", "DocumentSnapshot successfully deleted!"))
                    .addOnFailureListener(e -> Log.w("CURRENT", "Error deleting document", e));
            removeBooking(userPackedSpaces.get(position).getCarParkBookingId());
            removeSchedule(userPackedSpaces.get(position).getCarParkBookingId());
            mDatabase.collection("parkingspaces").document(userID).collection("history").add(phm)
                    .addOnSuccessListener(documentReference -> Log.d("HISTORY", "DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w("TAG", "Error writing document", e));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        userPackedSpaces.remove(userPackedSpaces.get(position));
    }

    private void removeBooking(String id) {
        mDatabase.collection("bookings")
                .whereEqualTo("id", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mDatabase.collection("bookings").document(document.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> Log.d("CURRENT", "DocumentSnapshot successfully deleted!"))
                                        .addOnFailureListener(e -> Log.w("CURRENT", "Error deleting document", e));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void removeSchedule(String bookingId) {
        mDatabase.collection("schedule")
                .whereEqualTo("id", bookingId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mDatabase.collection("schedule").document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Log.d("CURRENT", "DocumentSnapshot successfully deleted!"))
                                    .addOnFailureListener(e -> Log.w("CURRENT", "Error deleting document", e));
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        TextView bookingID;
        TextView parkingSlot;
        TextView fromDate;
        TextView fromTime;
        TextView toDate;
        TextView toTime;
        TextView vehicle;
        TextView vehicle_name;
        Button check_out;

        CustomViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            bookingID = view.findViewById(R.id.booking_id);
            parkingSlot = view.findViewById(R.id.parking_slot);
            fromDate = view.findViewById(R.id.from_date);
            fromTime = view.findViewById(R.id.from_time);
            toDate = view.findViewById(R.id.to_date);
            toTime = view.findViewById(R.id.to_time);
            vehicle = view.findViewById(R.id.vehicle_number);
            vehicle_name = view.findViewById(R.id.vehicle_name);
            check_out = view.findViewById(R.id.btn_check_out);

            mDatabase = FirebaseFirestore.getInstance();
            userID = SharePreference.getINSTANCE(getApplicationContext()).getUser();


        }
    }
}
