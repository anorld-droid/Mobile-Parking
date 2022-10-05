package com.karanja.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.karanja.Model.Park.ParkingSpace;
import com.karanja.R;
import com.karanja.views.ScheduleActivity;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.CustomViewHolder> {
    private Context context;
    private List<ParkingSpace> parkAddresses;
    private final String PREFERENCE_FILE_KEY = "location_pref";

    public AddressAdapter(Context context, List<com.karanja.Model.Park.ParkingSpace> parkingSpace) {
        this.context = context;
        this.parkAddresses  = parkingSpace;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        TextView address_name;
        TextView park_name, count, price;
        CardView park_details;
        CustomViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            address_name = view.findViewById(R.id.park_address);
            count = view.findViewById(R.id.slots_left);
            price = view.findViewById(R.id.price);
            park_name = view.findViewById(R.id.park_name);
            park_details = view.findViewById(R.id.park_location);
        }
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_map, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        final String park_name = parkAddresses.get(position).getName();
        final String park_address = parkAddresses.get(position).getAddress();
        final int count = parkAddresses.get(position).getStatus();
        final int fee = parkAddresses.get(position).getFee();
        final int id = parkAddresses.get(position).getId();
        final String price = "Ksh. "+fee + "/hr";
        holder.address_name.setText(park_address);
        holder.park_name.setText(park_name);
        holder.count.setText(String.valueOf(count));
        holder.price.setText(price);
        holder.park_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(count >0) {
                    SharedPreferences sharedPref = context.getSharedPreferences(
                            PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("Park_Name", park_name);
                    editor.putString("Park_Address", park_address);
                    editor.putInt("Price", fee);
                    editor.putInt("Count", count);
                    editor.putInt("id", id);
                    editor.commit();
                    Intent i = new Intent(context, ScheduleActivity.class);
                    context.startActivity(i);
                    ((Activity)context).finish();

                }else {
                    Toast.makeText(context, "No parking space available", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return parkAddresses.size();
    }
}
