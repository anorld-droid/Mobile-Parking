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
import android.content.Intent;
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

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.karanja.Model.AccessToken;
import com.karanja.Model.Booking.BookingSchedule;
import com.karanja.Model.Park.Report;
import com.karanja.Model.Park.SlotDetails;
import com.karanja.Model.Park.UserPackedSpace;
import com.karanja.Model.STKPush;
import com.karanja.Model.review.ParkingHistoryModel;
import com.karanja.R;
import com.karanja.utils.DarajaApiClient;
import com.karanja.utils.SharePreference;
import com.karanja.views.ConfirmationActivity;
import com.karanja.views.ScheduleActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkingHistoryAdapter extends RecyclerView.Adapter<ParkingHistoryAdapter.CustomViewHolder> {
    private String TAG = "ParkingHistoryAdapter";
    private Context context;
    private List<ParkingHistoryModel> parkingHistory;
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
    private SimpleDateFormat dateFormatter;

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
            dateFormatter = new SimpleDateFormat("EEE, dd MMM, HH:mm:ss");
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
        holder.re_book.setOnClickListener(v -> changeDate(parkingHistory.get(holder.getLayoutPosition())));

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
                        convert_to_current(parkingHistoryModel);
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
                try {
                    String slot = "SLOT " + parkingHistory.getUserId();
                    Date checkIn = dateFormatter.parse(dateFormatter.format(checkInDate.getTime()));
                    Date checkOut = dateFormatter.parse(dateFormatter.format(checkOutDate.getTime()));
                    assert checkOut != null;
                    assert checkIn != null;
                    if (checkOut.after(checkIn)) {
                        Task<QuerySnapshot> collectionRef = mDatabase.collection("schedule").get();
                        collectionRef.addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.exists()) {
                                        BookingSchedule bookingSchedule = document.toObject(BookingSchedule.class);
                                        try {
                                            Date exDateIn = dateFormatter.parse(bookingSchedule.getCheckIn());
                                            Date exDateOut = dateFormatter.parse(bookingSchedule.getCheckOut());
                                            if (!(checkIn.after(exDateOut) || checkOut.before(exDateIn)) && bookingSchedule.getSlot().equals(slot)) {
                                                Toast.makeText(getApplicationContext(), "Booking time unavailable, change slot or time", Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            }else {
                                                performSTKPush(phoneNumber.getText().toString(), parkingHistory);
                                                break;
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to retrieve items, check your internet connection and try again.", Toast.LENGTH_LONG).show();
                                Log.d("TAG", "Error getting documents: ", task.getException());
                            }
                        });
                    }else {
                        Toast.makeText(getApplicationContext(), "You cannot checkout before check in.", Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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

    private void convert_to_current(ParkingHistoryModel parkingHistoryModel) {
        String mCheckIn = getFormattedDay(checkInDate) + ", " + getFormattedTime(checkInDate);
        String mCheckOut = getFormattedDay(checkOutDate) + ", " + getFormattedTime(checkOutDate);
        String slot = "SLOT " + parkingHistoryModel.getUserId();
        Random rand = new Random();
        String bookingId = String.format("%06d", rand.nextInt(999999));
        UserPackedSpace userPackedSpace = new UserPackedSpace();
        userPackedSpace.setCarParkBookingId(bookingId);
        userPackedSpace.setUserId(parkingHistoryModel.getUserId());
        userPackedSpace.setAddress(parkingHistoryModel.getLocation());
        userPackedSpace.setCheckIn(mCheckIn);
        userPackedSpace.setCheckOut(mCheckOut);
        userPackedSpace.setVehicleNo(parkingHistoryModel.getVehicleNo());
        userPackedSpace.setOwner(parkingHistoryModel.getOwner());
        userPackedSpace.setAmount(String.valueOf(payment));
        Report report = new Report();
        report.setSlot(parkingHistoryModel.getUserId());
        report.setPayment(payment);
        report.setOccupant(userID);
        report.setCheckIn(mCheckIn);
        report.setCheckOut(mCheckOut);
        saveReport(report);
        BookingSchedule bookingSchedule = new BookingSchedule(bookingId, mCheckIn, mCheckOut,slot);
        addSchedule(bookingSchedule);
        mDatabase.collection("parkingspaces").document(userID).collection("current")
                .add(userPackedSpace)
                .addOnSuccessListener(aVoid -> Log.d("R/B", "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w("R/B", "Error writing document", e));

        mDatabase.collection("parkingspaces").document(userID).collection("history").document(parkingHistoryModel.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("CURRENT", "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w("CURRENT", "Error deleting document", e));
        addBooking(parkingHistoryModel.getUserId(), bookingId);
        Toast.makeText(context, "Re-book Successful", Toast.LENGTH_LONG).show();
    }


    public void addBooking(int slot, String bookingID) {
        SlotDetails slotDetails = new SlotDetails();
        slotDetails.setId(bookingID);
        slotDetails.setSlot(slot);
        slotDetails.setOccupant(SharePreference.getINSTANCE(getApplicationContext()).getUser());
        slotDetails.setCheckIn(getFormattedDay(checkInDate) + ", " + getFormattedTime(checkInDate));
        slotDetails.setCheckOut(getFormattedDay(checkOutDate) + ", " + getFormattedTime(checkOutDate));
        mDatabase.collection("bookings").add(slotDetails).addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
    }

    private void saveReport(Report report) {
        mDatabase.collection("reports")
                .add(report)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
    }

    private void addSchedule(BookingSchedule bookingSchedule) {
        mDatabase.collection("schedule")
                .add(bookingSchedule)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
    }

}
