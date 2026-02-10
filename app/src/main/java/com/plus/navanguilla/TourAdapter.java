package com.plus.navanguilla;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.ViewHolder> {

    public interface OnTourClickListener {
        void onTourClick(Tour tour);
    }

    private List<Tour> tours = new ArrayList<>();
    private final OnTourClickListener listener;

    public TourAdapter(OnTourClickListener listener) {
        this.listener = listener;
    }

    public void setTours(List<Tour> tours) {
        this.tours = tours;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tour, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tour tour = tours.get(position);

        holder.titleText.setText(tour.tourName);

        if (tour.tourDesc != null && !tour.tourDesc.isEmpty()) {
            holder.descText.setText(tour.tourDesc);
            holder.descText.setVisibility(View.VISIBLE);
        } else {
            holder.descText.setVisibility(View.GONE);
        }

        if (tour.tourDuration != null && !tour.tourDuration.isEmpty()) {
            holder.durationText.setText(tour.tourDuration);
            holder.durationText.setVisibility(View.VISIBLE);
        } else {
            holder.durationText.setVisibility(View.GONE);
        }

        holder.card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTourClick(tour);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tours.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView titleText;
        TextView descText;
        TextView durationText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.tour_card);
            titleText = itemView.findViewById(R.id.tour_title);
            descText = itemView.findViewById(R.id.tour_desc);
            durationText = itemView.findViewById(R.id.tour_duration);
        }
    }
}
