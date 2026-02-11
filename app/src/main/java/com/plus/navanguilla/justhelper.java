package com.plus.navanguilla;

import android.app.Activity;
import android.view.WindowManager;

public class justhelper {

    // ── Environment toggle ──
    // Set to true for production (Google Play), false for local dev
    private static final boolean PRODUCTION = false;

    // ── Production settings ──
    private static final String PROD_DOMAIN = "https://xcape.ai";
    private static final String PROD_FOLDER = "/navigational";

    // ── Development settings ──
    private static final String DEV_DOMAIN = "http://192.168.50.51";
    private static final String DEV_FOLDER = "/xcape";

    // ── Resolved BASE_URL (used throughout the app) ──
    public static final String BASE_URL = PRODUCTION
            ? PROD_DOMAIN + PROD_FOLDER
            : DEV_DOMAIN + DEV_FOLDER;

    public static void setBrightness(Activity activity, int brightnessPercent) {
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = brightnessPercent / 100.0f;
        activity.getWindow().setAttributes(layoutParams);
    }

}
