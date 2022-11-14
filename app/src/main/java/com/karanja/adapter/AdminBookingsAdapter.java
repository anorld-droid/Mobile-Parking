package com.karanja.adapter;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karanja.Model.Park.SlotDetails;
import com.karanja.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminBookingsAdapter extends RecyclerView.Adapter<AdminBookingsAdapter.CustomViewHolder> {
    private static final String TAG = "ADMIN/BOOKING/ADAPTER";
    private final Context context;
    private List<SlotDetails> slotDetails;
    private FirebaseFirestore mDatabase;

    public AdminBookingsAdapter(Context context, List<SlotDetails> slotDetails) {
        this.context = context;
        this.slotDetails = slotDetails;
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        private final TextView slotItemNumber;
        private final TextView occupant;
        private final TextView checkInDate;
        private final TextView checkInTime;
        private final TextView checkOutDate;
        private final TextView checkOutTime;

        Button cancelBooking;

        CustomViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            slotItemNumber = view.findViewById(R.id.slot_number_item);
            occupant = view.findViewById(R.id.occupant);
            checkInDate = view.findViewById(R.id.from_date);
            checkInTime = view.findViewById(R.id.from_time);
            checkOutDate = view.findViewById(R.id.to_date);
            checkOutTime = view.findViewById(R.id.to_time);
            cancelBooking = view.findViewById(R.id.cancel_booking);
            mDatabase = FirebaseFirestore.getInstance();
        }
    }

    @Override
    public AdminBookingsAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.admin_current_space_item, parent, false);
        return new AdminBookingsAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdminBookingsAdapter.CustomViewHolder holder, final int position) {

        holder.slotItemNumber.setText(String.valueOf("SLOT" + slotDetails.get(position).getSlot()));
        holder.occupant.setText(slotDetails.get(position).getOccupant());
        holder.checkInDate.setText(getFormattedDate(slotDetails.get(position).getCheckIn()));
        holder.checkInTime.setText(getFormattedTime(slotDetails.get(position).getCheckIn()));
        holder.checkOutDate.setText(getFormattedDate(slotDetails.get(position).getCheckOut()));
        holder.checkOutTime.setText(getFormattedTime(slotDetails.get(position).getCheckOut()));

        holder.cancelBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                revokeBooking(slotDetails.get(holder.getLayoutPosition()).getOccupant(), slotDetails.get(holder.getLayoutPosition()).getId());
                slotDetails.remove(holder.getLayoutPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return slotDetails.size();
    }


    public String getFormattedTime(String date) {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM, HH:mm:ss");
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date dateIn = null;
        try {
            dateIn = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert dateIn != null;
        return formatter.format(dateIn.getTime());
    }

    public String getFormattedDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM, HH:mm:ss");
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM");
        Date dateIn = null;
        try {
            dateIn = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert dateIn != null;
        return formatter.format(dateIn.getTime());
    }

    private void revokeBooking(String userId, String bookingId) {
        mDatabase.collection("parkingspaces").document(userId).collection("current").whereEqualTo("carParkBookingId", bookingId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    deleteCurrentBooking(userId, document.getId());
                                    removeSchedule(bookingId);
                                    removeBooking(bookingId);
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        mDatabase.collection("bookings")
                .whereEqualTo("id", bookingId)
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

    private void deleteCurrentBooking(String userID, String docID) {
        mDatabase.collection("parkingspaces").document(userID).collection("current").document(docID)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("CURRENT", "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w("CURRENT", "Error deleting document", e));
        Toast.makeText(context, "Booking revoked", Toast.LENGTH_SHORT).show();
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
}
