package com.karanja.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.karanja.Model.Park.SlotDetails;
import com.karanja.R;
import com.karanja.utils.SharePreference;
import com.karanja.views.ScheduleActivity;

import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.CustomViewHolder> {
    private Context context;
    private List<SlotDetails> slots;

    public SlotAdapter(Context context, List<SlotDetails> slots) {
        this.context = context;
        this.slots = slots;
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        TextView mSlotNumber;
        TextView mStatus;
        CardView single_slot;

        CustomViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            mSlotNumber = view.findViewById(R.id.slot_number);
            mStatus = view.findViewById(R.id.status);
            single_slot = view.findViewById(R.id.single_slot_card_view);
        }
    }

    @Override
    public SlotAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.slot_layout_item, parent, false);
        return new SlotAdapter.CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SlotAdapter.CustomViewHolder holder, final int position) {

        holder.mSlotNumber.setText(String.valueOf("SLOT " + slots.get(position).getSlot()));
        holder.mStatus.setText(slots.get(position).getOccupant() == null || slots.get(position).getOccupant().isEmpty() ? "OPEN" : "OCCUPIED");

        holder.single_slot.setOnClickListener(view -> {
            if (slots.get(position).getOccupant() == null || slots.get(position).getOccupant().isEmpty()) {
                SharePreference.getINSTANCE(context).setPickedSlot(String.valueOf("SLOT " + slots.get(holder.getLayoutPosition()).getSlot()));
                Intent i = new Intent(context, ScheduleActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            } else {
                Toast.makeText(context, "SLOT already occupied", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return slots.size();
    }
}
