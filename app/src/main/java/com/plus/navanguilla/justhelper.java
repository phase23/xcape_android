package com.plus.navanguilla;

import android.app.Activity;
import android.view.WindowManager;

public class justhelper {

    // Switch to "https://xcape.ai" for production
    public static final String BASE_URL = "http://192.168.50.51/xcape";

    public static void setBrightness(Activity activity, int brightnessPercent) {
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = brightnessPercent / 100.0f;
        activity.getWindow().setAttributes(layoutParams);
    }



}
