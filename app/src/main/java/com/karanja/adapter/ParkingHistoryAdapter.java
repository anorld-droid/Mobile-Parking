package com.karanja.adapter;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.karanja.utils.Constants.BUSINESS_SHORT_CODE;
import static com.karanja.utils.Constants.CALLBACKURL;
import static com.karanja.utils.Constants.PARTYB;
import static com.karanja.utils.Constants.PASSKEY;
import static com.karanja.utils.Constants.TRANSACTION_TYPE;
import static com.karanja.utils.Utils.getPassword;
import static com.karanja.utils.Utils.getTimestamp;
import static com.karanja.utils.Utils.sanitizePhoneNumber;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karanja.Model.AccessToken;
import com.karanja.Model.Park.ParkingSpace;
import com.karanja.Model.Park.SlotDetails;
import com.karanja.Model.Park.UserPackedSpace;
import com.karanja.Model.STKPush;
import com.karanja.Model.review.ParkingHistoryModel;
import com.karanja.R;
import com.karanja.utils.DarajaApiClient;
import com.karanja.utils.SharePreference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkingHistoryAdapter extends RecyclerView.Adapter<ParkingHistoryAdapter.CustomViewHolder> {
    private Context context;
    private List<ParkingHistoryModel> parkingHistory;
    private Button re_book;
    private DarajaApiClient mApiClient;
    private ProgressDialog mProgressDialog;
    private FirebaseFirestore mDatabase;
    private String userID;
    final Calendar checkInDate = Calendar.getInstance();
    final Calendar checkOutDate = Calendar.getInstance();
    private TextView checkOut, tvDuration;
    private TextView checkIn;
    private long payment;
    private EditText phoneNumber;
    private String duration;

    public ParkingHistoryAdapter(Context context, List<ParkingHistoryModel> parkingHistory) {
        this.context = context;
        this.parkingHistory = parkingHistory;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        TextView date_time;
        TextView location;
        TextView qr_code;
        TextView amount;
        Button re_book;

        CustomViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            date_time = view.findViewById(R.id.ph_date_time);
            location = view.findViewById(R.id.ph_location);
            qr_code = view.findViewById(R.id.ph_qr_code);
            amount = view.findViewById(R.id.ph_amount);
            re_book = view.findViewById(R.id.ph_btn_rebook);
            mProgressDialog = new ProgressDialog(context);
            mApiClient = new DarajaApiClient();
            mApiClient.setIsDebug(true); //Set True to enable logging, false to disable.
            getAccessToken();

            mDatabase = FirebaseFirestore.getInstance();
            userID = SharePreference.getINSTANCE(getApplicationContext()).getUser();


        }
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.parking_history_item, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {

        //For testing purposes
        holder.date_time.setText(String.valueOf(parkingHistory.get(position).getParkingHistoryDate() + " " + parkingHistory.get(position).getParkingHistoryTime()));
        holder.location.setText(parkingHistory.get(position).getLocation());
        holder.qr_code.setText(parkingHistory.get(position).getQrCode());
        holder.amount.setText(String.valueOf("Ksh." + parkingHistory.get(position).getAmount()));

        holder.re_book.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DocumentReference docRef = mDatabase.collection("parkingspaces").document("Naivas");
                docRef.get().addOnSuccessListener(documentSnapshot -> {
                    ParkingSpace parkingSpace = documentSnapshot.toObject(ParkingSpace.class);
                    int slot = Integer.parseInt(parkingHistory.get(holder.getLayoutPosition()).getId());
                    assert parkingSpace != null;
                    SlotDetails slotDetails = parkingSpace.getSlots().get(slot - 1);
                    if (parkingSpace.getStatus() > 0) {
                        if (slotDetails.getOccupant() == null) {
                            changeDate(parkingHistory.get(holder.getLayoutPosition()));
                        } else {
                            Toast.makeText(context, "Slot " + slot + "not available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "No parking space available", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    @Override
    public int getItemCount() {
        return parkingHistory.size();
    }


    public void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {
                if (response.isSuccessful()) {
                    mApiClient.setAuthToken(response.body().accessToken);

                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {
            }
        });
    }

    public void performSTKPush(String phone_number, ParkingHistoryModel parkingHistoryModel) {
        try {
            String[] dur = duration.split(" ");
            long time_difference = Integer.parseInt(dur[0]) + (Integer.parseInt(dur[2]) / 60);
            payment = time_difference * 50;
        } catch (Exception e) {
            Log.d("TAG", "" + e);
        }
        mProgressDialog.setMessage("Processing your request");
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        String timestamp = getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
                timestamp,
                TRANSACTION_TYPE,
                String.valueOf(payment),
                sanitizePhoneNumber(phone_number),
                PARTYB,
                sanitizePhoneNumber(phone_number),
                CALLBACKURL,
                "Karanja Ltd", //Account reference
                "Car Parking Lot STK PUSH by Karanja"  //Transaction description
        );


        mApiClient.setGetAccessToken(false);
        //Sending the data to the Mpesa API, remember to remove the logging when in production.

        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
                mProgressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        Random rand = new Random();
                        String bookingId = String.format("%06d", rand.nextInt(999999));
                        UserPackedSpace userPackedSpace = new UserPackedSpace();
                        userPackedSpace.setCarParkBookingId(bookingId);
                        userPackedSpace.setAddress(parkingHistoryModel.getLocation());
                        userPackedSpace.setCheckIn(getFormattedDay(checkInDate) + ", " + getFormattedTime(checkInDate));
                        userPackedSpace.setCheckOut(getFormattedDay(checkOutDate) + ", " + getFormattedTime(checkOutDate));
                        userPackedSpace.setVehicleNo(parkingHistoryModel.getVehicleNo());
                        userPackedSpace.setOwner(parkingHistoryModel.getOwner());
                        userPackedSpace.setAmount(String.valueOf(payment));

                        mDatabase.collection("parkingspaces").document(userID).collection("current")
                                .add(userPackedSpace)
                                .addOnSuccessListener(aVoid -> Log.d("R/B", "DocumentSnapshot successfully written!"))
                                .addOnFailureListener((OnFailureListener) e -> Log.w("R/B", "Error writing document", e));
                        mDatabase.collection("parkingspaces").document(userID).collection("history").document(parkingHistoryModel.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("CURRENT", "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                .addOnFailureListener(e -> Log.w("CURRENT", "Error deleting document", e));
                        DocumentReference parkingSpace = mDatabase.collection("parkingspaces")
                                .document("Naivas");
                        parkingSpace.get().addOnSuccessListener(documentSnapshot -> {
                            ParkingSpace parkingSpace1 = documentSnapshot.toObject(ParkingSpace.class);
                            assert parkingSpace1 != null;
                            int status = parkingSpace1.getStatus() - 1;
                            parkingSpace
                                    .update("status", status)
                                    .addOnSuccessListener(aVoid -> Log.d("TAG", "DocumentSnapshot successfully updated!"))
                                    .addOnFailureListener(e -> Log.w("TAG", "Error updating document", e));
                        });
                        Toast.makeText(context, "Re-book Successful", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(context, String.valueOf(response.raw()), Toast.LENGTH_SHORT).show();
                        Log.d("TAGElse", response.toString());
                    }
                } catch (Exception e) {
                    Log.d("TAGcat", response.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                Log.d("TAGTHRO", t.toString());
                mProgressDialog.dismiss();
            }
        });
    }

    private void changeDate(ParkingHistoryModel parkingHistory) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.change_date_dialog);
        dialog.setTitle("Date and Time");
        TextView change_checkIn = (TextView) dialog.findViewById(R.id.tv_change_checkIn);
        checkIn = (TextView) dialog.findViewById(R.id.textView7);
        change_checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIn(v);
            }
        });
        TextView change_checkOut = (TextView) dialog.findViewById(R.id.tv_change_checkOut);
        checkOut = (TextView) dialog.findViewById(R.id.textView10);
        change_checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOut(v);
            }
        });
        tvDuration = (TextView) dialog.findViewById(R.id.textView13);
        phoneNumber = (EditText) dialog.findViewById(R.id.phone_number);
        phoneNumber.setText(SharePreference.getINSTANCE(getApplicationContext()).getPhoneNumber());
        Button dialogButton = (Button) dialog.findViewById(R.id.change);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, phoneNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                performSTKPush(phoneNumber.getText().toString(), parkingHistory);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void setDuration() {
        long secs = (this.checkOutDate.getTimeInMillis() - this.checkInDate.getTimeInMillis()) / 1000;
        int hours = (int) (secs / 3600);
        secs = secs % 3600;
        int mins = (int) (secs / 60);
        secs = secs % 60;
        duration = String.valueOf(hours) + " hrs " + String.valueOf(mins) + " mins ";
        tvDuration.setText(duration);
    }

    public void checkIn(View view) {
        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                checkInDate.set(year, monthOfYear, dayOfMonth);
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        checkInDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        checkInDate.set(Calendar.MINUTE, minute);
                        checkIn.setText(String.valueOf(getFormattedDay(checkInDate) + ", " + getFormattedTime(checkInDate)));
                        setDuration();
                    }
                }, checkInDate.get(Calendar.HOUR_OF_DAY), checkInDate.get(Calendar.MINUTE), false).show();
            }
        }, checkInDate.get(Calendar.YEAR), checkInDate.get(Calendar.MONTH), checkInDate.get(Calendar.DATE)).show();
    }

    public void checkOut(View view) {
        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                checkOutDate.set(year, monthOfYear, dayOfMonth);
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        checkOutDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        checkOutDate.set(Calendar.MINUTE, minute);
                        checkOut.setText(String.valueOf(getFormattedDay(checkOutDate) + ", " + getFormattedTime(checkOutDate)));
                        setDuration();
                    }
                }, checkOutDate.get(Calendar.HOUR_OF_DAY), checkOutDate.get(Calendar.MINUTE), false).show();
            }
        }, checkOutDate.get(Calendar.YEAR), checkOutDate.get(Calendar.MONTH), checkOutDate.get(Calendar.DATE)).show();
    }

    public String getFormattedDay(Calendar date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM");
        return formatter.format(date.getTime());
    }


    public String getFormattedTime(Calendar date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(date.getTime());
    }


}
