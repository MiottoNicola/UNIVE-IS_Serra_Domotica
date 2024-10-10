package com.example.SerraDomotica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GreenhouseAdapter extends RecyclerView.Adapter<GreenhouseAdapter.ViewHolder> {
    private List<String> greenhouseList;
    private OnGreenhouseClickListener listener;

    public interface OnGreenhouseClickListener {
        void onGreenhouseClick(String greenhouseName);
    }

    public GreenhouseAdapter(List<String> greenhouseList, OnGreenhouseClickListener listener) {
        this.greenhouseList = greenhouseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_greenhouse, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String greenhouseName = greenhouseList.get(position);
        holder.textViewGreenhouseName.setText(greenhouseName);
        holder.buttonDetails.setOnClickListener(v -> listener.onGreenhouseClick(greenhouseName));
    }

    @Override
    public int getItemCount() {
        return greenhouseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGreenhouseName;
        Button buttonDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGreenhouseName = itemView.findViewById(R.id.textViewGreenhouseName);
            buttonDetails = itemView.findViewById(R.id.buttonDetails);
        }
    }
}