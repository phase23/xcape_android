package com.plus.navanguilla;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Myactivity extends AppCompatActivity {

    TextView setcountry;
    String getload;
    Handler handler2;
    String returnshift;
    String somebits;
    String option;
    String key;
    String locationnow;
    String responseLocation;
    String cid;
    String thiscountry;
    private FirebaseAnalytics firebaseAnalytics;
    private LinearLayout menuContainer;
    private LinearLayout badgeCounter;
    private TextView badgeCountText;
    private ImageView badgeCountIcon;
    private BroadcastReceiver badgeReceiver;
    private View hamburgerOverlay;
    private boolean esimEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_myactivity);
        handler2 = new Handler(Looper.getMainLooper());

        menuContainer = findViewById(R.id.scnf);
        setcountry = findViewById(R.id.tv_title);
        badgeCounter = findViewById(R.id.badge_counter);
        badgeCountText = findViewById(R.id.badge_count_text);
        badgeCountIcon = findViewById(R.id.badge_count_icon);

        // Hamburger menu
        hamburgerOverlay = findViewById(R.id.hamburger_overlay);
        ImageView hamburgerBtn = findViewById(R.id.hamburger_btn);
        ImageView hamburgerClose = findViewById(R.id.hamburger_close);
        View menuEventsCalendar = findViewById(R.id.menu_events_calendar);

        View menuHistoricalSites = findViewById(R.id.menu_historical_sites);

        hamburgerBtn.setOnClickListener(v ->
                hamburgerOverlay.setVisibility(View.VISIBLE));
        hamburgerClose.setOnClickListener(v ->
                hamburgerOverlay.setVisibility(View.GONE));
        menuEventsCalendar.setOnClickListener(v -> {
            hamburgerOverlay.setVisibility(View.GONE);
            Intent intent = new Intent(getApplicationContext(), LoadCalendar.class);
            startActivity(intent);
        });
        menuHistoricalSites.setOnClickListener(v -> {
            hamburgerOverlay.setVisibility(View.GONE);
            Intent intent = new Intent(getApplicationContext(), LoadHistoricalSites.class);
            startActivity(intent);
        });

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);

        justhelper.setBrightness(this, 75);

        cid = shared.getString("cid", "");
        thiscountry = shared.getString("country", "");

        Log.i("tagg url", cid + " / " + thiscountry);
        setcountry.setText("Explore " + thiscountry);

        fetchAppSettings();

        try {
            doGetRequest(justhelper.BASE_URL + "/navigation/loadactivities.php");
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadBadgeCount();

        // Listen for badge collection broadcasts from Myservice
        badgeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = intent.getStringExtra("badge_name");
                String icon = intent.getStringExtra("badge_icon");
                int collected = intent.getIntExtra("collected", 0);
                int total = intent.getIntExtra("total", 0);
                boolean allCollected = intent.getBooleanExtra("all_collected", false);

                // Update the counter pill
                if (allCollected) {
                    badgeCountText.setText("Island Warrior!");
                    badgeCountText.setTextColor(Color.WHITE);
                    badgeCountIcon.setImageResource(R.drawable.warrior_star);
                    badgeCounter.setBackground(ContextCompat.getDrawable(
                            Myactivity.this, R.drawable.badge_counter_complete_bg));
                } else {
                    badgeCountText.setText(collected + " of " + total);
                }
                badgeCounter.setVisibility(View.VISIBLE);

                AlertDialog.Builder dialog = new AlertDialog.Builder(Myactivity.this);

                if (allCollected) {
                    // All badges collected — Island Warrior!
                    dialog.setTitle("\uD83C\uDFC6 Island Warrior!");
                    dialog.setMessage("Congratulations! You've collected every badge on the island!\n\n"
                            + "All " + total + " badges earned.\n\n"
                            + "You are a true Island Warrior!");
                    dialog.setPositiveButton("Amazing!", (d, which) -> d.dismiss());
                } else {
                    // Normal badge collected
                    String emoji = getEmojiForIcon(icon);
                    dialog.setTitle(emoji + " Badge Collected!");
                    dialog.setMessage("You've collected " + name + "!\n\n"
                            + collected + " out of " + total + " badges collected.\n\n"
                            + "Keep exploring to find more!");
                    dialog.setPositiveButton("Awesome!", (d, which) -> d.dismiss());
                }

                dialog.setCancelable(true);
                dialog.create().show();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(
                badgeReceiver, new IntentFilter("badge-collected"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (badgeReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(badgeReceiver);
        }
    }

    private String getEmojiForIcon(String icon) {
        if (icon == null) return "\u2B50";
        switch (icon) {
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

    private void loadBadgeCount() {
        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String url = justhelper.BASE_URL + "/navigation/get_user_badges.php?device_id=" + thisdevice + "&cid=" + cid;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("badge", "Failed to fetch badge count");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                try {
                    JSONObject json = new JSONObject(body);
                    int collected = json.optInt("collected_count", 0) + 1;
                    int total = json.optInt("total_badges", 0) + 1;
                    if (total > 1) {
                        runOnUiThread(() -> {
                            if (collected >= total) {
                                // All collected — gold warrior style
                                badgeCountText.setText("Island Warrior!");
                                badgeCountText.setTextColor(Color.WHITE);
                                badgeCountIcon.setImageResource(R.drawable.warrior_star);
                                badgeCounter.setBackground(ContextCompat.getDrawable(
                                        Myactivity.this, R.drawable.badge_counter_complete_bg));
                            } else {
                                badgeCountText.setText(collected + " of " + total);
                            }
                            badgeCounter.setVisibility(View.VISIBLE);
                        });
                    }
                } catch (JSONException e) {
                    Log.i("badge", "Error parsing badge count: " + e.getMessage());
                }
            }
        });
    }

    // --- Helper to create modern menu card ---

    private View addMenuCard(String title, String subtitle, int iconRes, int iconTint,
                             View.OnClickListener listener) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_menu, menuContainer, false);

        TextView titleView = card.findViewById(R.id.menu_title);
        TextView subtitleView = card.findViewById(R.id.menu_subtitle);
        ImageView iconView = card.findViewById(R.id.menu_icon);
        View iconBg = card.findViewById(R.id.menu_icon_bg);

        titleView.setText(title);
        iconView.setImageResource(iconRes);

        if (subtitle != null && !subtitle.isEmpty()) {
            subtitleView.setText(subtitle);
            subtitleView.setVisibility(View.VISIBLE);
        }

        // Tint the icon circle background
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(iconTint & 0x20FFFFFF); // 12% alpha of the tint color
        bg.setSize(dpToPx(48), dpToPx(48));
        iconBg.setBackground(bg);

        // Tint the icon
        iconView.setColorFilter(iconTint, PorterDuff.Mode.SRC_IN);

        // Margins
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dpToPx(10));
        card.setLayoutParams(lp);

        card.setOnClickListener(listener);
        menuContainer.addView(card);
        return card;
    }

    private View addHighlightCard(String title, String subtitle, int iconRes,
                                  int bgDrawable, int textColor,
                                  View.OnClickListener listener) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_menu, menuContainer, false);

        TextView titleView = card.findViewById(R.id.menu_title);
        TextView subtitleView = card.findViewById(R.id.menu_subtitle);
        ImageView iconView = card.findViewById(R.id.menu_icon);
        View iconBg = card.findViewById(R.id.menu_icon_bg);

        titleView.setText(title);
        titleView.setTextColor(textColor);

        if (subtitle != null && !subtitle.isEmpty()) {
            subtitleView.setText(subtitle);
            subtitleView.setVisibility(View.VISIBLE);
            subtitleView.setTextColor(textColor & 0xBBFFFFFF);
        }

        iconView.setImageResource(iconRes);
        iconView.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        // White circle for icon on dark bg
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(0x33FFFFFF);
        bg.setSize(dpToPx(48), dpToPx(48));
        iconBg.setBackground(bg);

        // Set the card background
        card.setBackground(ContextCompat.getDrawable(this, bgDrawable));
        card.setElevation(dpToPx(4));

        // Hide chevron or tint it white
        ImageView chevron = (ImageView) ((LinearLayout) card).getChildAt(2);
        if (chevron != null) {
            chevron.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            chevron.setAlpha(0.5f);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dpToPx(10));
        card.setLayoutParams(lp);

        card.setOnClickListener(listener);
        menuContainer.addView(card);
        return card;
    }

    private void addSectionLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(12);
        label.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        label.setAllCaps(true);
        label.setLetterSpacing(0.08f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dpToPx(4), dpToPx(14), 0, dpToPx(8));
        label.setLayoutParams(lp);
        menuContainer.addView(label);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // --- Category icon colors ---

    private int getIconColor(String fid) {
        switch (fid) {
            case "1": return 0xFF0EA5E9;  // Beaches - sky blue
            case "2": return 0xFFEF4444;  // Restaurants - red
            case "3": return 0xFF8B5CF6;  // Interests - purple
            case "5": return 0xFF6366F1;  // Ports - indigo
            case "6": return 0xFF14B8A6;  // Accommodations - teal
            case "7": return 0xFFF97316;  // Petrol - orange
            case "10": return 0xFF22C55E; // Groceries - green
            case "11": return 0xFF64748B; // Car Rentals - slate
            case "12": return 0xFFF59E0B; // Badges - amber
            default: return 0xFF6366F1;   // Default - indigo
        }
    }

    private int getIconRes(String fid) {
        switch (fid) {
            case "1": return R.drawable.beach;
            case "2": return R.drawable.pineat;
            case "3": return R.drawable.mmpin;
            case "5": return R.drawable.luggage;
            case "6": return R.drawable.villa;
            case "7": return R.drawable.petrol;
            case "10": return R.drawable.retail;
            case "11": return R.drawable.crental;
            case "12": return R.drawable.trophy;
            default: return R.drawable.mmpin;
        }
    }

    // --- Build menu ---

    private void logactivity(String item, String activity, String page) {
        Log.i("action url", item + "/" + activity + "/" + page);
    }

    private void checkAndRequestPermissions(String tag) {
        if (tag.equals("9")) {
            Intent intent = new Intent(getApplicationContext(), Loadevents.class);
            intent.putExtra("list", tag);
            startActivity(intent);
        } else if (tag.equals("8")) {
            Intent intent = new Intent(getApplicationContext(), Loaddiscounts.class);
            intent.putExtra("list", tag);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), Loaditems.class);
            intent.putExtra("list", tag);
            startActivity(intent);
        }
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void initnav(String json) {
        try {
            JSONObject jsonObject = new JSONObject(somebits);
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                final String fid = keys.next();
                final String label = jsonObject.getString(fid);

                // Skip Badges and Interests here — added later under Experiences
                if (fid.equals("12") || fid.equals("3")) continue;

                addMenuCard(label, null, getIconRes(fid), getIconColor(fid), v -> {
                    logactivity(fid, label, "android");
                    checkAndRequestPermissions(fid);
                });

                // Insert Entertainment right after Restaurants
                if (fid.equals("2")) {
                    ent();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
    }

    // --- Special menu items ---

    private void ent() {
        addHighlightCard("Entertainment", "Live music, events & nightlife",
                R.drawable.livemusic, R.drawable.menu_card_highlight, Color.WHITE,
                v -> checkAndRequestPermissions("9"));
    }

    private void esims() {
        addHighlightCard("Activate your " + thiscountry + " eSim", "Stay connected on the island",
                R.drawable.wifi, R.drawable.menu_card_esim, Color.WHITE,
                v -> {
                    Intent activity = new Intent(getApplicationContext(), Esims.class);
                    startActivity(activity);
                });
    }

    private void islandtour() {
        addSectionLabel("Experiences");
        addMenuCard("Island Tour", "Guided audio tour of the island",
                R.drawable.maptour, 0xFF0EA5E9,
                v -> {
                    Intent activity = new Intent(getApplicationContext(), LoadTours.class);
                    startActivity(activity);
                });
    }

    private void collectBadges() {
        addMenuCard("Collect Badges", "Explore & collect island badges",
                R.drawable.trophy, 0xFFF59E0B,
                v -> checkAndRequestPermissions("12"));
    }

    private void thingsOfInterest() {
        addMenuCard("Things of Interest", "Points of interest around the island",
                R.drawable.mmpin, 0xFF8B5CF6,
                v -> checkAndRequestPermissions("3"));
    }

    private void discounts() {
        addMenuCard("Discounts", "Deals & offers near you",
                R.drawable.couponoff, 0xFFEC4899,
                v -> checkAndRequestPermissions("8"));
    }

    private void needhelp() {
        addMenuCard("Emergencies", "Police, hospital & emergency services",
                R.drawable.help, 0xFFDC2626,
                v -> checkAndRequestPermissions("4"));
    }

    private void contact() {
        addMenuCard("Contact", "Get in touch with us",
                R.drawable.feedback, 0xFF64748B,
                v -> {
                    Intent activity = new Intent(getApplicationContext(), Contactus.class);
                    startActivity(activity);
                });
    }

    private void feedback() {
        addMenuCard("Feedback", "Tell us how we can improve",
                R.drawable.feedback, 0xFF8B5CF6,
                v -> {
                    Intent activity = new Intent(getApplicationContext(), LoadExitInterview.class);
                    startActivity(activity);
                });
    }

    private void changecountry() {
        addMenuCard("Change Country", null,
                R.drawable.citizenship, 0xFF6366F1,
                v -> {
                    Intent activity = new Intent(getApplicationContext(), SelectCountry.class);
                    activity.putExtra("change", "dochange");
                    startActivity(activity);
                });
    }

    private void gohome() {
        addMenuCard("Return Home", "Navigate back to your starting point",
                R.drawable.locationhome, 0xFF22C55E,
                v -> {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Myactivity.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("Let's get going");
                    dialog.setMessage("Press yes to start your journey home");
                    dialog.setPositiveButton("Yes", (d, id) -> {
                        String locationnow = readFile();
                        String gohome = gethomeloc();
                        locationnow = locationnow.replace(',', '/');
                        gohome = gohome.replace(',', '/');
                        String routenow = locationnow + "~" + gohome;
                        Log.i("route", routenow);
                        Intent activity = new Intent(getApplicationContext(), Renturnhome.class);
                        activity.putExtra("theroute", routenow);
                        startActivity(activity);
                        d.dismiss();
                    });
                    dialog.setNegativeButton("No", (d, which) -> d.dismiss());
                    dialog.create().show();
                });
    }

    // --- First badge ---

    private void showFirstBadge() {
        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);
        boolean hasShown = shared.getBoolean("has_shown_first_badge", false);
        if (hasShown) return;

        String url = justhelper.BASE_URL + "/navigation/load_badges.php?cid=" + cid;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("badge", "Failed to fetch badges");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                try {
                    org.json.JSONArray arr = new org.json.JSONArray(body);
                    int totalBadges = arr.length();
                    if (totalBadges == 0) return;

                    runOnUiThread(() -> {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(Myactivity.this);
                        dialog.setTitle("\uD83C\uDFC6 Congratulations!");
                        dialog.setMessage("You've collected 1 out of " + (totalBadges + 1) + " badges!\n\nExplore the island to discover and collect more badges.");
                        dialog.setPositiveButton("Let's go!", (d, which) -> d.dismiss());
                        dialog.setCancelable(true);
                        dialog.create().show();

                        shared.edit().putBoolean("has_shown_first_badge", true).apply();
                    });
                } catch (Exception e) {
                    Log.i("badge", "Error parsing badges: " + e.getMessage());
                }
            }
        });
    }

    // --- Utilities ---

    public String gethomeloc() {
        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String url = justhelper.BASE_URL + "/navigation/gethomeloc.php?device=" + thisdevice;
        Log.i("action url", url);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("loc", "loc")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.i("SUCC", "" + response.message());
            }
            responseLocation = response.body().string().trim();
            Log.i("respBody:main", responseLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseLocation;
    }

    public String readFile() {
        String fileName = "navi.txt";
        StringBuilder stringBuilder = new StringBuilder();

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            fis = openFileInput(fileName);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
            locationnow = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { if (br != null) br.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if (isr != null) isr.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if (fis != null) fis.close(); } catch (IOException e) { e.printStackTrace(); }
        }
        return locationnow;
    }

    private void fetchAppSettings() {
        Request request = new Request.Builder()
                .url(justhelper.BASE_URL + "/navigation/load_app_settings.php")
                .build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("AppSettings", "Failed to load: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    esimEnabled = json.optString("esim_enabled", "1").equals("1");
                } catch (Exception e) {
                    Log.e("AppSettings", "Parse error: " + e.getMessage());
                }
            }
        });
    }

    void doGetRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.i("ddevice", url);
        OkHttpClient client = new OkHttpClient();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        Log.i("ddevice", "error");
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        somebits = response.body().string();
                        Log.i("ddevice", somebits);

                        handler2.post(() -> {
                            if (esimEnabled) esims();
                            initnav(somebits);
                            islandtour();
                            collectBadges();
                            thingsOfInterest();
                            discounts();
                            //gohome();
                            addSectionLabel("More");
                            needhelp();
                            feedback();
                            contact();
                            changecountry();
                            showFirstBadge();
                        });
                    }
                });
    }
}
