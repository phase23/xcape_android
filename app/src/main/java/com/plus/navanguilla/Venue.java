package com.plus.navanguilla;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Venue {
    public String placeId;
    public String name;
    public String site; // Clean name without driving time baked in
    public String phone;
    public double distance;
    public String drivingTime;
    public String isInterest;
    public String isAdvertiser;
    public String openingTimes;
    public String openingTimesOther;
    public String about;
    public String imageUrl;
    public String imageUrl2;
    public String imageUrl3;
    public String weblink;
    public String hasDiscount;
    public String badgeEmoji;
    public String isLocal;
    public String ratingsEnabled;
    public double avgRating;
    public int totalRatings;

    public static Venue fromJson(JSONObject obj) {
        Venue v = new Venue();
        v.placeId = obj.optString("placeid", "");
        v.name = obj.optString("whichsite", "").trim();
        v.site = obj.optString("site", "").trim();
        v.phone = obj.optString("phone", "").trim();
        v.distance = obj.optDouble("distance", 0);
        v.drivingTime = obj.optString("drivingtime", "");
        v.isInterest = obj.optString("isinterest", "0");
        v.isAdvertiser = obj.optString("isAdvertiser", "0");
        v.openingTimes = obj.optString("openingTimes", "N/A");
        v.openingTimesOther = obj.optString("openingTimesOther", "");
        v.about = obj.optString("slug", "No details available");
        v.imageUrl = obj.optString("imageUrl", "");
        v.imageUrl2 = obj.optString("imageUrl2", "");
        v.imageUrl3 = obj.optString("imageUrl3", "");
        v.weblink = obj.optString("weblink", "");
        v.hasDiscount = obj.optString("hasDiscount", "0");
        v.isLocal = obj.optString("isLocal", "0");
        v.badgeEmoji = obj.optString("badgeEmoji", "");
        v.ratingsEnabled = obj.optString("ratingsEnabled", "0");
        v.avgRating = obj.optDouble("avgRating", 0);
        v.totalRatings = obj.optInt("totalRatings", 0);
        return v;
    }

    /** Returns list of valid image URLs (non-empty, not ending in /adverts/) */
    public List<String> getImageUrls() {
        List<String> urls = new ArrayList<>();
        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.endsWith("/adverts/")) urls.add(imageUrl);
        if (imageUrl2 != null && !imageUrl2.isEmpty()) urls.add(imageUrl2);
        if (imageUrl3 != null && !imageUrl3.isEmpty()) urls.add(imageUrl3);
        return urls;
    }

    public boolean hasWeblink() {
        return weblink != null && !weblink.isEmpty();
    }

    public String getFormattedDistance() {
        return String.format("%.2f", distance);
    }

    /** Returns the clean site name for advertisers, or sentence-cased name for others */
    public String getDisplayName() {
        // Advertisers get the clean site field (no driving time suffix)
        if (isAdvertiser.equals("1") && !site.isEmpty()) {
            return site.substring(0, 1).toUpperCase() + site.substring(1).toLowerCase();
        }
        String sentenceCase = name.isEmpty() ? name :
                name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        return sentenceCase;
    }
}
