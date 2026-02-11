package com.plus.navanguilla;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Loaditems extends AppCompatActivity implements VenueAdapter.OnVenueClickListener {

    private Handler handler;
    private String locationnow;
    private String itemid;
    private String thistag;
    private TextView loading;
    private ProgressBar progressBar;
    private View loadingContainer;
    private String cid;
    private boolean sortByDistance = true;
    private boolean filterLocal = false;

    private RecyclerView recyclerView;
    private VenueAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        justhelper.setBrightness(this, 75);

        setContentView(R.layout.activity_loaditems);
        handler = new Handler(Looper.getMainLooper());

        Button goback = findViewById(R.id.backmain);
        loading = findViewById(R.id.loadingtext);
        progressBar = findViewById(R.id.spin_kit);
        loadingContainer = findViewById(R.id.loading_container);

        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);
        cid = shared.getString("cid", "");

        goback.setOnClickListener(v -> {
            Intent intent = new Intent(Loaditems.this, Myactivity.class);
            startActivity(intent);
        });

        itemid = getIntent().getExtras().getString("list", "defaultKey");
        Log.i("side", itemid);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.venue_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        float density = getResources().getDisplayMetrics().density;
        int sideMargin = (int) (16 * density);
        int topPadding = (int) (20 * density);
        recyclerView.setPadding(sideMargin, topPadding, sideMargin, 0);
        adapter = new VenueAdapter(itemid, this);
        recyclerView.setAdapter(adapter);

        // Search bar for restaurants and accommodations
        EditText searchBar = findViewById(R.id.search_bar);
        if (itemid.equals("2") || itemid.equals("6")) {
            searchBar.setVisibility(View.VISIBLE);
            searchBar.setHint(itemid.equals("2") ? "Search restaurants..." : "Search accommodations...");
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filter(s.toString());
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Sort toggle for restaurants only
        Button sortButton = findViewById(R.id.sort_button);
        Button localFilterButton = findViewById(R.id.local_filter_button);
        if (itemid.equals("2")) {
            sortButton.setVisibility(View.VISIBLE);
            localFilterButton.setVisibility(View.VISIBLE);
            sortButton.setText("Sort List A - Z");
            sortButton.setOnClickListener(v -> {
                sortByDistance = !sortByDistance;
                if (sortByDistance) {
                    sortButton.setText("Sort List A - Z");
                    loadlist("distance");
                    loading.setText("Sorting by distance .. loading");
                } else {
                    sortButton.setText("Sort by Distance");
                    loadlist("venue");
                    loading.setText("Sorting A to Z .. loading");
                }
                searchBar.setText("");
            });

            localFilterButton.setOnClickListener(v -> {
                filterLocal = !filterLocal;
                adapter.setLocalOnly(filterLocal);
                if (filterLocal) {
                    localFilterButton.setText("Show All");
                    localFilterButton.setTextColor(0xFFFFFFFF);
                    localFilterButton.setBackgroundResource(R.drawable.local_filter_button_active_background);
                } else {
                    localFilterButton.setText("Show Local");
                    localFilterButton.setTextColor(0xFF2E7D32);
                    localFilterButton.setBackgroundResource(R.drawable.local_filter_button_background);
                }
            });
        }

        // Badge counter for badges list
        if (itemid.equals("12")) {
            loadBadgeCount();
        }

        // Initial load
        if (itemid.equals("2") || itemid.equals("1") || itemid.equals("3") ||
                itemid.equals("7") || itemid.equals("5") || itemid.equals("4") ||
                itemid.equals("10") || itemid.equals("11") || itemid.equals("12")) {
            loadlist("distance");
            loading.setText("Sorting by distance .. loading");
        } else {
            sortByDistance = false;
            loadlist("venue");
            loading.setText("Sorting A to Z .. loading");
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
                            LinearLayout badgeCounter = findViewById(R.id.badge_counter);
                            TextView badgeCountText = findViewById(R.id.badge_count_text);
                            ImageView badgeCountIcon = findViewById(R.id.badge_count_icon);
                            if (collected >= total) {
                                badgeCountText.setText("Island Warrior!");
                                badgeCountText.setTextColor(android.graphics.Color.WHITE);
                                badgeCountIcon.setImageResource(R.drawable.warrior_star);
                                badgeCounter.setBackground(androidx.core.content.ContextCompat.getDrawable(
                                        Loaditems.this, R.drawable.badge_counter_complete_bg));
                            } else {
                                badgeCountText.setText(collected + " of " + total);
                            }
                            badgeCounter.setVisibility(View.VISIBLE);
                        });
                    }
                } catch (Exception e) {
                    Log.i("badge", "Error parsing badge count: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Loaditems.this, Myactivity.class);
        startActivity(intent);
    }

    public void loadlist(String sortorder) {
        loadingContainer.setVisibility(View.VISIBLE);
        adapter.setVenues(Collections.emptyList());

        try {
            String getlocation = readFile();
            String url;
            if (itemid.equals("12")) {
                url = justhelper.BASE_URL + "/navigation/load_badge_list.php?location=" + getlocation
                        + "&sortorder=" + sortorder
                        + "&cid=" + cid;
            } else {
                url = justhelper.BASE_URL + "/navigation/loadlist.php?id=" + itemid
                        + "&location=" + getlocation
                        + "&sortorder=" + sortorder
                        + "&cid=" + cid;
            }
            doLoadlist(url);
        } catch (IOException e) {
            e.printStackTrace();
            loadingContainer.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
        }
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
            if (br != null) {
                try { br.close(); } catch (IOException e) { e.printStackTrace(); }
            }
            if (isr != null) {
                try { isr.close(); } catch (IOException e) { e.printStackTrace(); }
            }
            if (fis != null) {
                try { fis.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }

        return locationnow;
    }

    void doLoadlist(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.i("ddevice", url);
        OkHttpClient client = new OkHttpClient();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        Log.i("ddevice", "error " + e);
                        runOnUiThread(() -> {
                            loadingContainer.setVisibility(View.GONE);
                            Toast.makeText(Loaditems.this, "Failed to load venues", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        final String json = response.body().string();
                        Log.i("ddevice", json);

                        handler.post(() -> {
                            parseAndDisplay(json);
                            loadingContainer.setVisibility(View.GONE);
                        });
                    }
                });
    }

    private void parseAndDisplay(String json) {
        List<Venue> venues = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                venues.add(Venue.fromJson(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse venue data", Toast.LENGTH_SHORT).show();
        }
        // Featured listings first, then by current sort order
        Collections.sort(venues, (a, b) -> {
            int aAd = a.isAdvertiser.equals("1") ? 0 : 1;
            int bAd = b.isAdvertiser.equals("1") ? 0 : 1;
            if (aAd != bAd) return aAd - bAd;
            if (sortByDistance) {
                return Double.compare(a.distance, b.distance);
            } else {
                return a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
            }
        });
        adapter.setVenues(venues);

        // Fetch weather for beaches
        if (itemid.equals("1")) {
            fetchBeachWeather();
        }
    }

    private void fetchBeachWeather() {
        String url = justhelper.BASE_URL + "/navigation/load_beach_weather.php?cid=" + cid;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("weather", "Failed to fetch beach weather: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                try {
                    JSONArray arr = new JSONArray(body);
                    Map<String, JSONObject> weatherMap = new HashMap<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        weatherMap.put(obj.optString("lid", ""), obj);
                    }
                    runOnUiThread(() -> {
                        adapter.applyWeather(weatherMap);
                    });
                } catch (Exception e) {
                    Log.i("weather", "Error parsing weather: " + e.getMessage());
                }
            }
        });
    }

    // --- VenueAdapter.OnVenueClickListener ---

    @Override
    public void onNavigateClick(Venue venue) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);

        String thisaction;
        String thiscancel;

        if (itemid.equals("2") || venue.isInterest.equals("1")) {
            String msgtop;
            String msgbtm;

            if (venue.isInterest.equals("1")) {
                msgtop = "Book Activity or start navigation";
                msgbtm = "Book Activity";
            } else {
                msgtop = "Reserve a table or start navigation";
                msgbtm = "Reserve table";
            }

            dialog.setTitle(venue.name);
            dialog.setMessage(msgtop);
            thisaction = "Start";
            thiscancel = "Cancel";

            dialog.setNeutralButton(msgbtm, (d, which) -> {
                Uri number = Uri.parse("tel:" + venue.phone);
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                startActivity(callIntent);
            });
        } else {
            dialog.setTitle("Start Navigation");
            dialog.setMessage("Are you sure you want to start this route?");
            thisaction = "Yes";
            thiscancel = "No";
        }

        dialog.setPositiveButton(thisaction, (d, id) -> {
            d.dismiss();
            if (!isLocationPermissionGranted() || !isGpsEnabled()) {
                Intent noperm = new Intent(getApplicationContext(), Nopermission.class);
                startActivity(noperm);
                return;
            }
            thistag = venue.placeId;
            gettheroutes(thistag);
        });

        dialog.setNegativeButton(thiscancel, (d, which) -> d.dismiss());

        dialog.create().show();
    }

    @Override
    public void onCallClick(Venue venue) {
        Uri number = Uri.parse("tel:" + venue.phone);
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(callIntent);
    }

    @Override
    public void onRateClick(Venue venue, int position) {
        showRatingDialog(venue.site.isEmpty() ? venue.name : venue.site,
                venue.placeId, "loadlist", position);
    }

    private void showRatingDialog(String name, String placeId, String placeType, int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate " + name);

        // Build star selector layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(0, 32, 0, 16);

        LinearLayout starsRow = new LinearLayout(this);
        starsRow.setOrientation(LinearLayout.HORIZONTAL);
        starsRow.setGravity(android.view.Gravity.CENTER);

        final int[] selectedRating = {0};
        final TextView[] starViews = new TextView[5];

        for (int i = 0; i < 5; i++) {
            final int starIndex = i;
            TextView star = new TextView(this);
            star.setText("\u2605");
            star.setTextSize(36);
            star.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.star_empty));
            star.setPadding(8, 0, 8, 0);
            starViews[i] = star;
            star.setOnClickListener(v -> {
                selectedRating[0] = starIndex + 1;
                for (int j = 0; j < 5; j++) {
                    starViews[j].setTextColor(androidx.core.content.ContextCompat.getColor(this,
                            j <= starIndex ? R.color.star_filled : R.color.star_empty));
                }
            });
            starsRow.addView(star);
        }
        layout.addView(starsRow);
        builder.setView(layout);

        builder.setPositiveButton("Submit", (d, which) -> {
            if (selectedRating[0] == 0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }
            submitRating(placeId, placeType, selectedRating[0], adapterPosition);
        });
        builder.setNegativeButton("Cancel", (d, which) -> d.dismiss());
        builder.create().show();
    }

    private void submitRating(String placeId, String placeType, int rating, int adapterPosition) {
        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String url = justhelper.BASE_URL + "/navigation/submit_rating.php?device_id=" + thisdevice
                + "&place_id=" + placeId
                + "&place_type=" + placeType
                + "&rating=" + rating;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(Loaditems.this, "Failed to submit rating", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                try {
                    JSONObject json = new JSONObject(body);
                    if (json.optString("status").equals("ok")) {
                        double avg = json.optDouble("avg_rating", 0);
                        int total = json.optInt("total_ratings", 0);
                        runOnUiThread(() -> {
                            adapter.updateRating(adapterPosition, avg, total);
                            Toast.makeText(Loaditems.this, "Thanks for rating!", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(Loaditems.this, "Failed to submit rating", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // --- Navigation launch flow (unchanged) ---

    public void gettheroutes(String thisplace) {
        try {
            System.out.println(justhelper.BASE_URL + "/navigation/getroute.php?&id=" + thisplace);
            returnroute(justhelper.BASE_URL + "/navigation/getroute.php?&id=" + thisplace);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void returnroute(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        runOnUiThread(() ->
                                Toast.makeText(Loaditems.this, "Failed to load route", Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        String resulting = response.body().string().trim();
                        String modifiednow = locationnow.replace(',', '/');
                        String routenow = modifiednow + "~" + resulting;
                        Log.d("routex", ": " + routenow);

                        Intent activity = new Intent(getApplicationContext(), Pickup.class);
                        activity.putExtra("itemid", itemid);
                        activity.putExtra("placeid", thistag);
                        activity.putExtra("theroute", routenow);
                        startActivity(activity);
                    }
                });
    }

    // --- Fullscreen helpers (unchanged) ---

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private boolean isGpsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
