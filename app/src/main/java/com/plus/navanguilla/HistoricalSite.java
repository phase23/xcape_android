package com.plus.navanguilla;

import org.json.JSONObject;

public class HistoricalSite {
    public String hid;
    public String siteTitle;
    public String siteDesc;
    public String siteImage;
    public String lat;
    public String longi;
    public String badgeId;
    public String badgeName;
    public String badgeIcon;
    public String ratingsEnabled;
    public double avgRating;
    public int totalRatings;

    public static HistoricalSite fromJson(JSONObject obj) {
        HistoricalSite s = new HistoricalSite();
        s.hid = obj.optString("hid", "");
        s.siteTitle = obj.optString("site_title", "").trim();
        s.siteDesc = obj.optString("site_desc", "").trim();
        s.siteImage = obj.optString("site_image", "").trim();
        s.lat = obj.optString("lat", "").trim();
        s.longi = obj.optString("longi", "").trim();
        s.badgeId = obj.optString("badge_id", "").trim();
        s.badgeName = obj.optString("badge_name", "").trim();
        s.badgeIcon = obj.optString("badge_icon", "").trim();
        s.ratingsEnabled = obj.optString("ratingsEnabled", "0");
        s.avgRating = obj.optDouble("avgRating", 0);
        s.totalRatings = obj.optInt("totalRatings", 0);
        return s;
    }

    public boolean hasBadge() {
        return badgeId != null && !badgeId.isEmpty();
    }

    public String getBadgeEmoji() {
        if (badgeIcon == null) return "\u2B50";
        switch (badgeIcon) {
            case "trophy": return "\uD83C\uDFC6";
            case "medal": return "\uD83C\uDFC5";
            case "gem": return "\uD83D\uDC8E";
            case "crown": return "\uD83D\uDC51";
            case "flag": return "\uD83C\uDFC1";
            case "compass": return "\uD83E\uDDED";
            case "shell": return "\uD83D\uDC1A";
            case "palm": return "\uD83C\uDF34";
            case "anchor": return "\u2693";
            case "fish": return "\uD83D\uDC1F";
            case "sun": return "\u2600";
            case "wave": return "\uD83C\uDF0A";
            case "camera": return "\uD83D\uDCF7";
            case "mountain": return "\u26F0";
            case "castle": return "\uD83C\uDFF0";
            default: return "\u2B50";
        }
    }
}
