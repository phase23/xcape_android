package com.plus.navanguilla;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private List<Event> events = new ArrayList<>();
    private final OnEventClickListener listener;
    private final int iconRes;

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
        this.iconRes = R.drawable.music;
    }

    public EventAdapter(OnEventClickListener listener, int iconRes) {
        this.listener = listener;
        this.iconRes = iconRes;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        boolean featured = event.isFeatured();

        // Featured event image
        if (featured && !event.eventImage.isEmpty()) {
            holder.featuredImageContainer.setVisibility(View.VISIBLE);
            String imageUrl = justhelper.BASE_URL + "/navigation/" + event.eventImage;
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.featuredImage);
        } else if (featured) {
            holder.featuredImageContainer.setVisibility(View.VISIBLE);
            holder.featuredImage.setImageResource(R.drawable.placeholder);
        } else {
            holder.featuredImageContainer.setVisibility(View.GONE);
        }

        // Card styling
        if (featured) {
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.card_advertiser));
            holder.card.setCardElevation(6f);
        } else if (event.isAlternateDay()) {
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.event_alt_bg));
            holder.card.setCardElevation(2f);
        } else {
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.card_white));
            holder.card.setCardElevation(2f);
        }

        // Day banner
        holder.eventDay.setText(event.whichDay.toUpperCase());

        // Artist + venue
        if (featured) {
            holder.eventArtist.setText(event.justVenue);
            holder.eventArtist.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_advertiser));
            holder.eventVenue.setText(event.artist);
            holder.eventIcon.setImageResource(R.drawable.ic_calendar);
        } else {
            holder.eventArtist.setText(event.artist);
            holder.eventArtist.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
            holder.eventVenue.setText(event.venue);
            holder.eventIcon.setImageResource(iconRes);
        }

        // Showtime + distance
        if (featured) {
            String showLabel = event.showtime.isEmpty() ? "Featured Event" : event.showtime;
            holder.eventShowtime.setText(showLabel);
            if (event.distance > 0) {
                holder.eventDistance.setText(event.getFormattedDistance() + " miles away");
            } else {
                holder.eventDistance.setText("Featured Event");
            }
        } else {
            holder.eventShowtime.setText(event.showtime);
            holder.eventDistance.setText(event.getFormattedDistance() + " miles away");
        }

        // Action row click
        holder.actionRow.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        FrameLayout featuredImageContainer;
        ImageView featuredImage;
        TextView eventDay;
        ImageView eventIcon;
        TextView eventArtist;
        TextView eventVenue;
        TextView eventShowtime;
        TextView eventDistance;
        View actionRow;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.event_card);
            featuredImageContainer = itemView.findViewById(R.id.event_featured_image_container);
            featuredImage = itemView.findViewById(R.id.event_featured_image);
            eventDay = itemView.findViewById(R.id.event_day);
            eventIcon = itemView.findViewById(R.id.event_icon);
            eventArtist = itemView.findViewById(R.id.event_artist);
            eventVenue = itemView.findViewById(R.id.event_venue);
            eventShowtime = itemView.findViewById(R.id.event_showtime);
            eventDistance = itemView.findViewById(R.id.event_distance);
            actionRow = itemView.findViewById(R.id.event_action_row);
        }
    }
}
