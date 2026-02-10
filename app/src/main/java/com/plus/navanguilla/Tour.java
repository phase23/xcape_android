package com.plus.navanguilla;

import org.json.JSONObject;

public class Tour {
    public String tid;
    public String tourName;
    public String tourDesc;
    public String tourDuration;
    public String destLat;
    public String destLng;

    public static Tour fromJson(JSONObject obj) {
        Tour t = new Tour();
        t.tid = obj.optString("tid", "");
        t.tourName = obj.optString("tour_name", "").trim();
        t.tourDesc = obj.optString("tour_desc", "").trim();
        t.tourDuration = obj.optString("tour_duration", "").trim();
        t.destLat = obj.optString("dest_lat", "").trim();
        t.destLng = obj.optString("dest_lng", "").trim();
        return t;
    }
}
