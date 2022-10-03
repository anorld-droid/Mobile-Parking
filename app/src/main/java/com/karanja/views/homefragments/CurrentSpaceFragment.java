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
import com.karanja.utils.SharePreference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class CurrentSpaceFragment extends Fragment {
    private TextView bookingID, parkingSlot, fromDate, fromTime, toTime, toDate, vehicle, vehicle_name;
    private FirebaseFirestore mDatabase;
    private String userID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.current_space, container, false);
        bookingID = root.findViewById(R.id.booking_id);
        parkingSlot = root.findViewById(R.id.parking_slot);
        fromDate = root.findViewById(R.id.from_date);
        fromTime = root.findViewById(R.id.from_time);
        toDate = root.findViewById(R.id.to_date);
        toTime = root.findViewById(R.id.to_time);
        vehicle = root.findViewById(R.id.vehicle_number);
        vehicle_name = root.findViewById(R.id.vehicle_name);
        userID = SharePreference.getINSTANCE(getApplicationContext()).getUser();

        mDatabase = FirebaseFirestore.getInstance();
        DocumentReference docRef = mDatabase.collection("parkingspaces").document(userID).collection("current").document("space");
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM, HH:mm:ss");
                    UserPackedSpace userPackedSpace = documentSnapshot.toObject(UserPackedSpace.class);
                    assert userPackedSpace != null;

                    try {
                        Date dn = new Date();
                        String formatted = formatter.format(dn);
                        Date today = formatter.parse(formatted);
                        Date dateIn = formatter.parse(userPackedSpace.getCheckIn());
                        Date dateOut = formatter.parse(userPackedSpace.getCheckOut());
                        SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM");
                        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
                        String dateTo = date.format(dateOut);
                        String timeTo = time.format(dateOut);
                        if (dateOut.after(today)) {
                            bookingID.setText(userPackedSpace.getCarParkBookingId());
                            parkingSlot.setText(userPackedSpace.getAddress());

                            assert dateIn != null;
                            String inDate = date.format(dateIn);
                            fromDate.setText(inDate);
                            String inTime = time.format(dateIn);
                            fromTime.setText(inTime);
                            assert dateOut != null;


                            toDate.setText(dateTo);

                            toTime.setText(timeTo);
                            vehicle_name.setText(userPackedSpace.getOwner());
                            vehicle.setText(userPackedSpace.getVehicleNo().toUpperCase());
                            bookingID.setText(userPackedSpace.getCarParkBookingId());
                            parkingSlot.setText(userPackedSpace.getAddress());
                        } else {
                            Random rand = new Random();
                            int index = rand.nextInt(10);
                            String[] qrCode = {"SZEK0932", "SEJK0965", "SXEQ2313", "SARO0079", "SWER4829", "STUY1212", "DEUY7804", "YTU7654E", "HYUT4453", "OILK897Y"};
                            ParkingHistoryModel phm = new ParkingHistoryModel();
                            phm.setParkingHistoryDate(dateTo);
                            phm.setParkingHistoryTime(timeTo);
                            phm.setAmount(userPackedSpace.getAmount());
                            phm.setLocation(userPackedSpace.getAddress());
                            phm.setQrCode(qrCode[index]);
                            phm.setCheckIn(userPackedSpace.getCheckIn());
                            phm.setCheckOut(userPackedSpace.getCheckOut());
                            phm.setOwner(userPackedSpace.getOwner());
                            phm.setVehicleNo(userPackedSpace.getVehicleNo());
                            phm.setCarParkBookingId(userPackedSpace.getCarParkBookingId());
                            mDatabase.collection("parkingspaces").document(userID).collection("history").add(phm)
                                    .addOnSuccessListener(documentReference -> Log.d("HISTORY", "DocumentSnapshot successfully written!"))
                                    .addOnFailureListener(e -> Log.w("TAG", "Error writing document", e));
                            mDatabase.collection("parkingspaces").document(userID).collection("current").document("space")
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("CURRENT", "DocumentSnapshot successfully deleted!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("CURRENT", "Error deleting document", e);
                                        }
                                    });
                            DocumentReference parkingSpace = mDatabase.collection("parkingspaces")
                                    .document("Naivas");
                            parkingSpace.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    ParkingSpace parkingSpace1 = documentSnapshot.toObject(ParkingSpace.class);
                                    assert parkingSpace1 != null;
                                    int status = parkingSpace1.getStatus() + 1;;
                                    parkingSpace
                                            .update("status", status)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("TAG", "DocumentSnapshot successfully updated!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("TAG", "Error updating document", e);
                                                }
                                            });
                                }
                            });

                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }
            }
        });
        return root;
    }


}
