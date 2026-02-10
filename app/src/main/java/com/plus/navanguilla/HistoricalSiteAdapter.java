package com.plus.navanguilla;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HistoricalSiteAdapter extends RecyclerView.Adapter<HistoricalSiteAdapter.ViewHolder> {

    public interface OnSiteClickListener {
        void onSiteClick(HistoricalSite site);
        void onRateClick(HistoricalSite site, int position);
    }

    private List<HistoricalSite> sites = new ArrayList<>();
    private final OnSiteClickListener listener;

    public HistoricalSiteAdapter(OnSiteClickListener listener) {
        this.listener = listener;
    }

    public void setSites(List<HistoricalSite> sites) {
        this.sites = sites;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historical_site, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoricalSite site = sites.get(position);

        holder.titleText.setText(site.siteTitle);

        if (site.siteDesc != null && !site.siteDesc.isEmpty()) {
            holder.descText.setText(site.siteDesc);
            holder.descText.setVisibility(View.VISIBLE);
        } else {
            holder.descText.setVisibility(View.GONE);
        }

        // Load image
        if (site.siteImage != null && !site.siteImage.isEmpty()) {
            String imageUrl = justhelper.BASE_URL + "/navigation/" + site.siteImage;
            Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .centerCrop()
                    .into(holder.siteImage);
            holder.siteImage.setVisibility(View.VISIBLE);
        } else {
            holder.siteImage.setVisibility(View.GONE);
        }

        // Badge
        if (site.hasBadge()) {
            holder.badgeContainer.setVisibility(View.VISIBLE);
            holder.badgeText.setText("\uD83C\uDFC6 " + site.badgeName + " — Badge Available");
        } else {
            holder.badgeContainer.setVisibility(View.GONE);
        }

        // Rating row
        if (site.ratingsEnabled != null && site.ratingsEnabled.equals("1")) {
            holder.ratingRow.setVisibility(View.VISIBLE);
            renderStars(holder.starsContainer, site.avgRating);
            if (site.totalRatings > 0) {
                holder.ratingText.setText(String.format("%.1f (%d)", site.avgRating, site.totalRatings));
            } else {
                holder.ratingText.setText("No ratings yet");
            }
            holder.rateBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRateClick(site, holder.getAdapterPosition());
                }
            });
        } else {
            holder.ratingRow.setVisibility(View.GONE);
        }

        // Navigate action row
        holder.actionRow.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSiteClick(site);
            }
        });
    }

    public void updateRating(int position, double avgRating, int totalRatings) {
        if (position >= 0 && position < sites.size()) {
            sites.get(position).avgRating = avgRating;
            sites.get(position).totalRatings = totalRatings;
            notifyItemChanged(position);
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
                star.setTextColor(androidx.core.content.ContextCompat.getColor(container.getContext(), R.color.star_filled));
            } else if (i == fullStars && hasHalf) {
                star.setText("\u2605");
                star.setTextColor(androidx.core.content.ContextCompat.getColor(container.getContext(), R.color.star_filled));
                star.setAlpha(0.5f);
            } else {
                star.setText("\u2605");
                star.setTextColor(androidx.core.content.ContextCompat.getColor(container.getContext(), R.color.star_empty));
            }
            container.addView(star);
        }
    }

    @Override
    public int getItemCount() {
        return sites.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        ImageView siteImage;
        TextView titleText;
        TextView descText;
        LinearLayout badgeContainer;
        TextView badgeText;
        View actionRow;
        View ratingRow;
        LinearLayout starsContainer;
        TextView ratingText;
        TextView rateBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.hist_site_card);
            siteImage = itemView.findViewById(R.id.hist_site_image);
            titleText = itemView.findViewById(R.id.hist_site_title);
            descText = itemView.findViewById(R.id.hist_site_desc);
            badgeContainer = itemView.findViewById(R.id.hist_badge_container);
            badgeText = itemView.findViewById(R.id.hist_badge_text);
            actionRow = itemView.findViewById(R.id.hist_action_row);
            ratingRow = itemView.findViewById(R.id.hist_rating_row);
            starsContainer = itemView.findViewById(R.id.hist_stars_container);
            ratingText = itemView.findViewById(R.id.hist_rating_text);
            rateBtn = itemView.findViewById(R.id.hist_rate_btn);
        }
    }
}
