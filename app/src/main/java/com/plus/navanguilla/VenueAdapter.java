package com.plus.navanguilla;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.VenueViewHolder> {

    public interface OnVenueClickListener {
        void onNavigateClick(Venue venue);
        void onCallClick(Venue venue);
        void onRateClick(Venue venue, int position);
    }

    private static final Map<String, Integer> CATEGORY_ICONS = new HashMap<>();
    static {
        CATEGORY_ICONS.put("1", R.drawable.beach);
        CATEGORY_ICONS.put("2", R.drawable.pineat);
        CATEGORY_ICONS.put("3", R.drawable.mmpin);
        CATEGORY_ICONS.put("4", R.drawable.hospitalabr);
        CATEGORY_ICONS.put("5", R.drawable.luggage);
        CATEGORY_ICONS.put("6", R.drawable.villa);
        CATEGORY_ICONS.put("7", R.drawable.petroloutline);
        CATEGORY_ICONS.put("10", R.drawable.retail);
        CATEGORY_ICONS.put("11", R.drawable.crental);
        CATEGORY_ICONS.put("12", R.drawable.trophy);
    }

    private List<Venue> allVenues = new ArrayList<>();
    private List<Venue> venues = new ArrayList<>();
    private final String itemId;
    private final OnVenueClickListener listener;

    public VenueAdapter(String itemId, OnVenueClickListener listener) {
        this.itemId = itemId;
        this.listener = listener;
    }

    public void setVenues(List<Venue> venues) {
        this.allVenues = new ArrayList<>(venues);
        this.venues = new ArrayList<>(venues);
        notifyDataSetChanged();
    }

    private boolean localOnly = false;
    private String searchQuery = "";

    public void filter(String query) {
        searchQuery = query != null ? query : "";
        applyFilters();
    }

    public void setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
        applyFilters();
    }

    private void applyFilters() {
        venues = new ArrayList<>();
        for (Venue v : allVenues) {
            if (localOnly && (v.isLocal == null || !v.isLocal.equals("1"))) {
                continue;
            }
            if (!searchQuery.trim().isEmpty()) {
                String lower = searchQuery.toLowerCase().trim();
                if (!v.name.toLowerCase().contains(lower) &&
                        (v.site.isEmpty() || !v.site.toLowerCase().contains(lower))) {
                    continue;
                }
            }
            venues.add(v);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_venue, parent, false);
        return new VenueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VenueViewHolder holder, int position) {
        Venue venue = venues.get(position);
        boolean isAd = venue.isAdvertiser.equals("1");

        // Card styling: advertisers get warm bg + extra elevation
        if (isAd) {
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.card_advertiser));
            holder.card.setCardElevation(8f);
        } else {
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.card_white));
            holder.card.setCardElevation(2f);
        }

        // Image gallery + Featured badge
        List<String> imageUrls = venue.getImageUrls();
        boolean hasImage = !imageUrls.isEmpty();
        if (isAd || hasImage) {
            holder.imageContainer.setVisibility(View.VISIBLE);
            holder.featuredBadge.setVisibility(isAd ? View.VISIBLE : View.GONE);
            setupImageGallery(holder, imageUrls);
        } else {
            holder.imageContainer.setVisibility(View.GONE);
            holder.featuredBadge.setVisibility(View.GONE);
        }

        // Category icon
        int iconRes;
        if (itemId.equals("3") && venue.isInterest.equals("1")) {
            iconRes = R.drawable.outdoor;
        } else {
            Integer icon = CATEGORY_ICONS.get(itemId);
            iconRes = icon != null ? icon : R.drawable.pineat;
        }
        holder.venueIcon.setImageResource(iconRes);

        // Local badge
        if (venue.isLocal != null && venue.isLocal.equals("1")) {
            holder.localBadge.setVisibility(View.VISIBLE);
        } else {
            holder.localBadge.setVisibility(View.GONE);
        }

        // Badge emoji (only for badge items)
        if (venue.badgeEmoji != null && !venue.badgeEmoji.isEmpty()) {
            holder.badgeEmoji.setVisibility(View.VISIBLE);
            holder.badgeEmoji.setText(venue.badgeEmoji);
        } else {
            holder.badgeEmoji.setVisibility(View.GONE);
        }

        // Venue name — advertisers use clean site field
        holder.venueName.setText(venue.getDisplayName());

        // Advertiser names get the accent color
        if (isAd) {
            holder.venueName.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.accent_advertiser));
        } else {
            holder.venueName.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        }

        // Distance
        holder.venueDistance.setText(venue.getFormattedDistance() + " miles away");

        // Hours row (all restaurants — tappable open/closed status + expandable schedule)
        boolean showHoursRow = itemId.equals("2")
                && venue.openingTimes != null && !venue.openingTimes.isEmpty()
                && !venue.openingTimes.equals("Not available") && !venue.openingTimes.equals("N/A");
        if (showHoursRow) {
            holder.hoursRow.setVisibility(View.VISIBLE);
            holder.hoursDetail.setVisibility(View.GONE);

            // Format hours for display: replace ", " with newlines
            String hoursFormatted = venue.openingTimes.replace(", ", "\n");
            holder.hoursDetail.setText(hoursFormatted);

            // Determine open/closed status for today
            String[] dayAbbrs = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK); // 1=Sun..7=Sat
            String todayAbbr = dayAbbrs[dow - 1];
            String statusText = "";
            int statusColor = Color.GRAY;

            // Find today's entry in the opening times string
            String timesLower = venue.openingTimes.toLowerCase();
            String searchKey = todayAbbr.toLowerCase() + ":";
            int idx = timesLower.indexOf(searchKey);
            if (idx >= 0) {
                // Extract this day's value (up to next comma or end)
                int start = idx + searchKey.length();
                int end = venue.openingTimes.indexOf(",", start);
                String todayVal = (end > 0 ? venue.openingTimes.substring(start, end) : venue.openingTimes.substring(start)).trim();

                if (todayVal.equalsIgnoreCase("closed")) {
                    statusText = "Closed today";
                    statusColor = Color.parseColor("#D32F2F");
                } else {
                    statusText = "Open today";
                    statusColor = Color.parseColor("#2E7D32");
                }
            }

            holder.openStatus.setText(statusText);
            holder.openStatus.setTextColor(statusColor);
            holder.openStatus.setVisibility(statusText.isEmpty() ? View.GONE : View.VISIBLE);

            // Toggle expand/collapse on tap
            holder.hoursRow.setOnClickListener(v -> {
                boolean expanded = holder.hoursDetail.getVisibility() == View.VISIBLE;
                holder.hoursDetail.setVisibility(expanded ? View.GONE : View.VISIBLE);
                holder.hoursToggle.setText(expanded ? "\uD83D\uDD52 Hours" : "\uD83D\uDD52 Hours \u25B2");
            });
        } else {
            holder.hoursRow.setVisibility(View.GONE);
            holder.hoursDetail.setVisibility(View.GONE);
        }

        // Discount tag
        if (venue.hasDiscount.equals("1")) {
            holder.discountTag.setVisibility(View.VISIBLE);
            // Also show on premium hero image
            if (isAd) {
                holder.premiumDiscountTag.setVisibility(View.VISIBLE);
            } else {
                holder.premiumDiscountTag.setVisibility(View.GONE);
            }
        } else {
            holder.discountTag.setVisibility(View.GONE);
            holder.premiumDiscountTag.setVisibility(View.GONE);
        }

        // Advertiser details section
        if (isAd) {
            holder.advertiserDetails.setVisibility(View.VISIBLE);
            // Hide static opening times for restaurants — the hours row handles it
            if (itemId.equals("2")) {
                holder.openingTimes.setVisibility(View.GONE);
                holder.openingTimesOther.setVisibility(View.GONE);
            } else {
                holder.openingTimes.setVisibility(View.VISIBLE);
                holder.openingTimes.setText(venue.openingTimes);
                holder.openingTimesOther.setVisibility(View.VISIBLE);
                holder.openingTimesOther.setText(venue.openingTimesOther);
            }
            holder.venueAbout.setText(venue.about);

            // Weblink
            if (venue.hasWeblink()) {
                holder.venueWeblink.setVisibility(View.VISIBLE);
                String displayUrl = venue.weblink.replaceFirst("^https?://", "");
                holder.venueWeblink.setText("\uD83C\uDF10 " + displayUrl);
                holder.venueWeblink.setOnClickListener(v -> {
                    Context ctx = holder.itemView.getContext();
                    Intent intent = new Intent(ctx, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.EXTRA_URL, venue.weblink);
                    intent.putExtra(WebViewActivity.EXTRA_TITLE, venue.getDisplayName());
                    ctx.startActivity(intent);
                });
            } else {
                holder.venueWeblink.setVisibility(View.GONE);
            }
        } else {
            holder.advertiserDetails.setVisibility(View.GONE);
            holder.venueWeblink.setVisibility(View.GONE);
        }

        // Driving time in action row
        holder.drivingTime.setText(venue.drivingTime);

        // Rating row
        if (venue.ratingsEnabled != null && venue.ratingsEnabled.equals("1")) {
            holder.ratingRow.setVisibility(View.VISIBLE);
            renderStars(holder.starsContainer, venue.avgRating);
            if (venue.totalRatings > 0) {
                holder.ratingText.setText(String.format("%.1f (%d)", venue.avgRating, venue.totalRatings));
            } else {
                holder.ratingText.setText("No ratings yet");
            }
            holder.rateBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRateClick(venue, holder.getAdapterPosition());
                }
            });
        } else {
            holder.ratingRow.setVisibility(View.GONE);
        }

        // Action row click
        holder.actionRow.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNavigateClick(venue);
            }
        });
    }

    public void updateRating(int position, double avgRating, int totalRatings) {
        if (position >= 0 && position < venues.size()) {
            venues.get(position).avgRating = avgRating;
            venues.get(position).totalRatings = totalRatings;
            notifyItemChanged(position);
        }
    }

    private void setupImageGallery(VenueViewHolder holder, List<String> imageUrls) {
        holder.imageFlipper.removeAllViews();
        holder.imageDots.removeAllViews();

        if (imageUrls.isEmpty()) {
            // No images — show placeholder
            ImageView placeholder = new ImageView(holder.itemView.getContext());
            placeholder.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            placeholder.setScaleType(ImageView.ScaleType.CENTER_CROP);
            placeholder.setImageResource(R.drawable.placeholder);
            holder.imageFlipper.addView(placeholder);
            holder.imageDots.setVisibility(View.GONE);
            return;
        }

        // Add each image to the flipper
        for (String url : imageUrls) {
            ImageView img = new ImageView(holder.itemView.getContext());
            img.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(img);
            holder.imageFlipper.addView(img);
        }

        // Dot indicators (only if more than 1 image)
        if (imageUrls.size() > 1) {
            holder.imageDots.setVisibility(View.VISIBLE);
            List<View> dots = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                View dot = new View(holder.itemView.getContext());
                int size = (int) (8 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(size / 2, 0, size / 2, 0);
                dot.setLayoutParams(params);
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.OVAL);
                shape.setColor(i == 0 ? Color.WHITE : 0x80FFFFFF);
                dot.setBackground(shape);
                holder.imageDots.addView(dot);
                dots.add(dot);
            }

            // Tap to advance image
            holder.imageFlipper.setOnClickListener(v -> {
                holder.imageFlipper.showNext();
                int current = holder.imageFlipper.getDisplayedChild();
                for (int i = 0; i < dots.size(); i++) {
                    GradientDrawable s = new GradientDrawable();
                    s.setShape(GradientDrawable.OVAL);
                    s.setColor(i == current ? Color.WHITE : 0x80FFFFFF);
                    dots.get(i).setBackground(s);
                }
            });
        } else {
            holder.imageDots.setVisibility(View.GONE);
            holder.imageFlipper.setOnClickListener(null);
        }
    }

    private void renderStars(LinearLayout container, double rating) {
        container.removeAllViews();
        int fullStars = (int) rating;
        boolean hasHalf = (rating - fullStars) >= 0.3;
        for (int i = 0; i < 5; i++) {
            TextView star = new TextView(container.getContext());
            star.setTextSize(16);
            if (i < fullStars) {
                star.setText("\u2605");
                star.setTextColor(ContextCompat.getColor(container.getContext(), R.color.star_filled));
            } else if (i == fullStars && hasHalf) {
                star.setText("\u2605");
                star.setTextColor(ContextCompat.getColor(container.getContext(), R.color.star_filled));
                star.setAlpha(0.5f);
            } else {
                star.setText("\u2605");
                star.setTextColor(ContextCompat.getColor(container.getContext(), R.color.star_empty));
            }
            container.addView(star);
        }
    }

    @Override
    public int getItemCount() {
        return venues.size();
    }

    static class VenueViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        FrameLayout imageContainer;
        ViewFlipper imageFlipper;
        LinearLayout imageDots;
        TextView featuredBadge;
        ImageView venueIcon;
        TextView badgeEmoji;
        TextView venueName;
        TextView localBadge;
        TextView venueDistance;
        LinearLayout advertiserDetails;
        TextView openingTimes;
        TextView openingTimesOther;
        TextView venueAbout;
        TextView venueWeblink;
        LinearLayout hoursRow;
        TextView hoursToggle;
        TextView openStatus;
        TextView hoursDetail;
        TextView discountTag;
        TextView premiumDiscountTag;
        TextView drivingTime;
        View actionRow;
        View ratingRow;
        LinearLayout starsContainer;
        TextView ratingText;
        TextView rateBtn;

        VenueViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.venue_card);
            imageContainer = itemView.findViewById(R.id.venue_image_container);
            imageFlipper = itemView.findViewById(R.id.venue_image_flipper);
            imageDots = itemView.findViewById(R.id.venue_image_dots);
            featuredBadge = itemView.findViewById(R.id.featured_badge);
            venueIcon = itemView.findViewById(R.id.venue_icon);
            badgeEmoji = itemView.findViewById(R.id.badge_emoji);
            venueName = itemView.findViewById(R.id.venue_name);
            localBadge = itemView.findViewById(R.id.venue_local_badge);
            venueDistance = itemView.findViewById(R.id.venue_distance);
            advertiserDetails = itemView.findViewById(R.id.advertiser_details);
            openingTimes = itemView.findViewById(R.id.venue_opening_times);
            openingTimesOther = itemView.findViewById(R.id.venue_opening_times_other);
            venueAbout = itemView.findViewById(R.id.venue_about);
            venueWeblink = itemView.findViewById(R.id.venue_weblink);
            hoursRow = itemView.findViewById(R.id.venue_hours_row);
            hoursToggle = itemView.findViewById(R.id.venue_hours_toggle);
            openStatus = itemView.findViewById(R.id.venue_open_status);
            hoursDetail = itemView.findViewById(R.id.venue_hours_detail);
            discountTag = itemView.findViewById(R.id.venue_discount_tag);
            premiumDiscountTag = itemView.findViewById(R.id.venue_premium_discount_tag);
            drivingTime = itemView.findViewById(R.id.venue_driving_time);
            actionRow = itemView.findViewById(R.id.venue_action_row);
            ratingRow = itemView.findViewById(R.id.venue_rating_row);
            starsContainer = itemView.findViewById(R.id.venue_stars_container);
            ratingText = itemView.findViewById(R.id.venue_rating_text);
            rateBtn = itemView.findViewById(R.id.venue_rate_btn);
        }
    }
}
