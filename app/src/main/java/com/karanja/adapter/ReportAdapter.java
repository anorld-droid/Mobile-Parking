package com.karanja.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.karanja.Model.Park.Report;
import com.karanja.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.CustomViewHolder> {
    private List<Report> reports;
    private FirebaseFirestore mDatabase;
    private static ArrayList<LinearLayout> linearLayoutArrayList = new ArrayList<>();

    public ReportAdapter(Context context, List<Report> reports) {
        this.reports = reports;
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        private final TextView slotItemNumber;
        private final TextView occupant;
        private final TextView checkInDate;
        private final TextView checkInTime;
        private final TextView checkOutDate;
        private final TextView checkOutTime;
        private final TextView payment;
        private final LinearLayout linearLayout;

        CustomViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            slotItemNumber = view.findViewById(R.id.slot_number_item);
            occupant = view.findViewById(R.id.occupant);
            checkInDate = view.findViewById(R.id.from_date);
            checkInTime = view.findViewById(R.id.from_time);
            checkOutDate = view.findViewById(R.id.to_date);
            checkOutTime = view.findViewById(R.id.to_time);
            payment = view.findViewById(R.id.payment);
            linearLayout = view.findViewById(R.id.lyt_parent);
            mDatabase = FirebaseFirestore.getInstance();
        }
    }

    @Override
    public ReportAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.report_layout_item, parent, false);
        return new ReportAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReportAdapter.CustomViewHolder holder, final int position) {

        holder.slotItemNumber.setText(String.valueOf("SLOT" + reports.get(position).getSlot()));
        holder.occupant.setText(reports.get(position).getOccupant());
        holder.checkInDate.setText(getFormattedDate(reports.get(position).getCheckIn()));
        holder.checkInTime.setText(getFormattedTime(reports.get(position).getCheckIn()));
        holder.checkOutDate.setText(getFormattedDate(reports.get(position).getCheckOut()));
        holder.checkOutTime.setText(getFormattedTime(reports.get(position).getCheckOut()));
        holder.payment.setText(String.valueOf("Ksh." + reports.get(position).getPayment()));

        addCardView(holder.linearLayout);

    }

    @Override
    public int getItemCount() {
        return reports.size();
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
    private static void addCardView(LinearLayout linearLayout)
    {
        linearLayoutArrayList.add(linearLayout);
    }

    public static ArrayList<LinearLayout> getCardViewList()
    {
        return linearLayoutArrayList;
    }
}

