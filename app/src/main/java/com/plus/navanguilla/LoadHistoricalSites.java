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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoadHistoricalSites extends AppCompatActivity implements HistoricalSiteAdapter.OnSiteClickListener {

    private Handler handler;
    private String locationnow;
    private View loadingContainer;
    private View emptyContainer;
    private RecyclerView recyclerView;
    private HistoricalSiteAdapter adapter;
    private String cid;

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
        setContentView(R.layout.activity_loadhistoricalsites);
        handler = new Handler(Looper.getMainLooper());

        Button goback = findViewById(R.id.backmain);
        loadingContainer = findViewById(R.id.loading_container);
        emptyContainer = findViewById(R.id.empty_container);

        goback.setOnClickListener(v -> {
            Intent intent = new Intent(LoadHistoricalSites.this, Myactivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.historical_site_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        float density = getResources().getDisplayMetrics().density;
        int sideMargin = (int) (16 * density);
        int topPadding = (int) (20 * density);
        recyclerView.setPadding(sideMargin, topPadding, sideMargin, 0);
        adapter = new HistoricalSiteAdapter(this);
        recyclerView.setAdapter(adapter);

        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);
        cid = shared.getString("cid", "");

        try {
            readFile();
            loadSites(justhelper.BASE_URL + "/navigation/load_historical_sites.php?cid=" + cid);
        } catch (IOException e) {
            e.printStackTrace();
            loadingContainer.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load sites", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LoadHistoricalSites.this, Myactivity.class);
        startActivity(intent);
    }

    public String readFile() {
        String fileName = "navi.txt";
        StringBuilder sb = new StringBuilder();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = openFileInput(fileName);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            locationnow = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { if (br != null) br.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if (isr != null) isr.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if (fis != null) fis.close(); } catch (IOException e) { e.printStackTrace(); }
        }
        return locationnow;
    }

    void loadSites(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Log.i("historical", url);
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                Log.i("historical", "error " + e);
                runOnUiThread(() -> {
                    loadingContainer.setVisibility(View.GONE);
                    Toast.makeText(LoadHistoricalSites.this, "Failed to load sites", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String json = response.body().string();
                Log.i("historical", json);
                handler.post(() -> {
                    parseAndDisplay(json);
                    loadingContainer.setVisibility(View.GONE);
                });
            }
        });
    }

    private void parseAndDisplay(String json) {
        List<HistoricalSite> sites = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                sites.add(HistoricalSite.fromJson(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse site data", Toast.LENGTH_SHORT).show();
        }

        if (sites.isEmpty()) {
            emptyContainer.setVisibility(View.VISIBLE);
        } else {
            adapter.setSites(sites);
        }
    }

    // --- OnSiteClickListener ---

    @Override
    public void onSiteClick(HistoricalSite site) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("Start Navigation");
        dialog.setMessage("Navigate to " + site.siteTitle + "?");

        dialog.setPositiveButton("Yes", (d, id) -> {
            d.dismiss();
            navigateToSite(site);
        });

        dialog.setNegativeButton("No", (d, which) -> d.dismiss());
        dialog.create().show();
    }

    @Override
    public void onRateClick(HistoricalSite site, int position) {
        showRatingDialog(site.siteTitle, site.hid, "historical_site", position);
    }

    private void showRatingDialog(String name, String placeId, String placeType, int adapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate " + name);

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
                runOnUiThread(() -> Toast.makeText(LoadHistoricalSites.this, "Failed to submit rating", Toast.LENGTH_SHORT).show());
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
                            Toast.makeText(LoadHistoricalSites.this, "Thanks for rating!", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(LoadHistoricalSites.this, "Failed to submit rating", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void navigateToSite(HistoricalSite site) {
        if (!isLocationPermissionGranted() || !isGpsEnabled()) {
            Intent activity = new Intent(getApplicationContext(), Nopermission.class);
            startActivity(activity);
            return;
        }

        if (locationnow == null || locationnow.isEmpty()) {
            Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build route: currentLat/currentLng~destLat/destLng
        String modifiednow = locationnow.replace(',', '/');
        String destCoords = site.lat + "/" + site.longi;
        String routenow = modifiednow + "~" + destCoords;
        Log.d("historical_route", routenow);

        Intent activity = new Intent(getApplicationContext(), Pickup.class);
        activity.putExtra("itemid", "");
        activity.putExtra("theroute", routenow);
        activity.putExtra("placeid", site.hid);
        activity.putExtra("placename", site.siteTitle);
        activity.putExtra("preclass", "2");
        startActivity(activity);
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // --- Fullscreen ---

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
}
