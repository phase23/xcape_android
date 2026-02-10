package com.plus.navanguilla;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.ViewHolder> {

    public interface OnCalendarEventClickListener {
        void onCalendarEventClick(CalendarEvent event);
        void onCalendarEventNavigate(CalendarEvent event);
    }

    private List<CalendarEvent> events = new ArrayList<>();
    private final OnCalendarEventClickListener listener;

    public CalendarEventAdapter(OnCalendarEventClickListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<CalendarEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarEvent event = events.get(position);

        holder.dayLabel.setText(event.getDayOfWeek());
        holder.dateLabel.setText(event.getFormattedDate());
        holder.titleText.setText(event.eventTitle);

        // Time
        if (event.showtime != null && !event.showtime.isEmpty()) {
            holder.timeText.setText(event.showtime);
            holder.timeText.setVisibility(View.VISIBLE);
        } else {
            holder.timeText.setVisibility(View.GONE);
        }

        // Description
        if (event.eventDesc != null && !event.eventDesc.isEmpty()) {
            holder.descText.setText(event.eventDesc);
            holder.descText.setVisibility(View.VISIBLE);
        } else {
            holder.descText.setVisibility(View.GONE);
        }

        // Load image
        if (event.eventImage != null && !event.eventImage.isEmpty()) {
            String imageUrl = justhelper.BASE_URL + "/navigation/" + event.eventImage;
            Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .centerCrop()
                    .into(holder.eventImage);
            holder.eventImage.setVisibility(View.VISIBLE);
        } else {
            holder.eventImage.setVisibility(View.GONE);
        }

        // Action row with distance (only if event has coordinates)
        if (event.hasCoordinates()) {
            holder.divider.setVisibility(View.VISIBLE);
            holder.actionRow.setVisibility(View.VISIBLE);

            if (event.distance > 0) {
                holder.distanceText.setText(event.getFormattedDistance() + " miles away (" + event.drivingMins + " mins)");
            } else {
                holder.distanceText.setText("Navigate here");
            }

            holder.actionRow.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCalendarEventNavigate(event);
                }
            });
        } else {
            holder.divider.setVisibility(View.GONE);
            holder.actionRow.setVisibility(View.GONE);
        }

        // Card tap shows details
        holder.card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCalendarEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView dayLabel;
        TextView dateLabel;
        TextView titleText;
        TextView timeText;
        TextView descText;
        ImageView eventImage;
        View divider;
        View actionRow;
        TextView distanceText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cal_event_card);
            dayLabel = itemView.findViewById(R.id.cal_event_day);
            dateLabel = itemView.findViewById(R.id.cal_event_date);
            titleText = itemView.findViewById(R.id.cal_event_title);
            timeText = itemView.findViewById(R.id.cal_event_time);
            descText = itemView.findViewById(R.id.cal_event_desc);
            eventImage = itemView.findViewById(R.id.cal_event_image);
            divider = itemView.findViewById(R.id.cal_event_divider);
            actionRow = itemView.findViewById(R.id.cal_event_action_row);
            distanceText = itemView.findViewById(R.id.cal_event_distance);
        }
    }
}
