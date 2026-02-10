package com.plus.navanguilla;

import org.json.JSONObject;

public class Event {
    public String placeId;
    public String whichDay;
    public String showtime;
    public String venue;
    public String artist;
    public double distance;
    public String justVenue;
    public String isFeaturedEvent;
    public String eventImage;
    public String eventLat;
    public String eventLng;

    public static Event fromJson(JSONObject obj) {
        Event e = new Event();
        e.placeId = obj.optString("placeid", "");
        e.whichDay = obj.optString("whichday", "").trim();
        e.showtime = obj.optString("showtime", "").trim();
        e.venue = obj.optString("venue", "").trim();
        e.artist = obj.optString("artist", "").trim();
        e.distance = obj.optDouble("distance", 0);
        e.justVenue = obj.optString("justvenue", "").trim();
        e.isFeaturedEvent = obj.optString("isFeaturedEvent", "0");
        e.eventImage = obj.optString("eventImage", "").trim();
        e.eventLat = obj.optString("eventLat", "").trim();
        e.eventLng = obj.optString("eventLng", "").trim();
        return e;
    }

    public boolean isFeatured() {
        return "1".equals(isFeaturedEvent);
    }

    public boolean hasCoordinates() {
        return !eventLat.isEmpty() && !eventLng.isEmpty();
    }

    public String getFormattedDistance() {
        return String.format("%.2f", distance);
    }

    public boolean isAlternateDay() {
        switch (whichDay.toUpperCase().trim()) {
            case "MONDAY":
            case "WEDNESDAY":
            case "FRIDAY":
            case "SUNDAY":
                return true;
            default:
                return false;
        }
    }
}
