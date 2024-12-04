package com.example.SerraDomotica;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GreenhouseAdapter extends RecyclerView.Adapter<GreenhouseAdapter.GreenhouseViewHolder> {
    private final List<String> greenhouseList;
    private final Context context;

    public GreenhouseAdapter(List<String> greenhouseList, Context context) {
        this.greenhouseList = greenhouseList;
        this.context = context;
    }

    @NonNull
    @Override
    public GreenhouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_greenhouse, parent, false);
        return new GreenhouseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GreenhouseViewHolder holder, int position) {
        String[] greenhouseInfo = greenhouseList.get(position).split(":");
        String greenhouseName = greenhouseInfo[0];
        String greenhouseId = greenhouseInfo[1];
        holder.textViewGreenhouseName.setText(greenhouseName);
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GreenhouseDetailsActivity.class);
            intent.putExtra("greenhouse_name", greenhouseName);
            intent.putExtra("greenhouse_id", greenhouseId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return greenhouseList.size();
    }

    public static class GreenhouseViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGreenhouseName;
        ImageView buttonDetails;
        CardView cardView;

        public GreenhouseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGreenhouseName = itemView.findViewById(R.id.textViewGreenhouseName);
            buttonDetails = itemView.findViewById(R.id.buttonDetails);
            cardView = (CardView) itemView;
        }
    }
}