package com.plus.navanguilla;

import org.json.JSONObject;

public class CalendarEvent {
    public String eid;
    public String eventDate;
    public String eventTitle;
    public String eventDesc;
    public String eventImage;
    public String showtime;
    public String eventLat;
    public String eventLng;
    public double distance;
    public int drivingMins;

    public static CalendarEvent fromJson(JSONObject obj) {
        CalendarEvent e = new CalendarEvent();
        e.eid = obj.optString("eid", "");
        e.eventDate = obj.optString("event_date", "").trim();
        e.eventTitle = obj.optString("event_title", "").trim();
        e.eventDesc = obj.optString("event_desc", "").trim();
        e.eventImage = obj.optString("event_image", "").trim();
        e.showtime = obj.optString("showtime", "").trim();
        e.eventLat = obj.optString("event_lat", "").trim();
        e.eventLng = obj.optString("event_lng", "").trim();
        e.distance = obj.optDouble("distance", -1);
        e.drivingMins = obj.optInt("driving_mins", 0);
        return e;
    }

    public boolean hasCoordinates() {
        return !eventLat.isEmpty() && !eventLng.isEmpty();
    }

    public String getFormattedDistance() {
        return String.format("%.2f", distance);
    }

    public String getFormattedDate() {
        try {
            java.text.SimpleDateFormat input = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.text.SimpleDateFormat output = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US);
            java.util.Date date = input.parse(eventDate);
            return output.format(date);
        } catch (Exception e) {
            return eventDate;
        }
    }

    public String getShortDate() {
        try {
            java.text.SimpleDateFormat input = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.text.SimpleDateFormat output = new java.text.SimpleDateFormat("MMM d", java.util.Locale.US);
            java.util.Date date = input.parse(eventDate);
            return output.format(date);
        } catch (Exception e) {
            return eventDate;
        }
    }

    public String getDayOfWeek() {
        try {
            java.text.SimpleDateFormat input = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            java.text.SimpleDateFormat output = new java.text.SimpleDateFormat("EEEE", java.util.Locale.US);
            java.util.Date date = input.parse(eventDate);
            return output.format(date).toUpperCase();
        } catch (Exception e) {
            return "";
        }
    }
}
