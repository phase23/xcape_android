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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;

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

public class Loaddiscounts extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private Handler handler;
    private String locationnow;
    private String itemid;
    private String thistag;
    private View loadingContainer;
    private String cid;

    private RecyclerView recyclerView;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        justhelper.setBrightness(this, 75);
        setContentView(R.layout.activity_loaddiscounts);
        handler = new Handler(Looper.getMainLooper());

        Button goback = findViewById(R.id.backmain);
        loadingContainer = findViewById(R.id.loading_container);

        goback.setOnClickListener(v -> {
            Intent intent = new Intent(Loaddiscounts.this, Myactivity.class);
            startActivity(intent);
        });

        itemid = "";

        // Set up RecyclerView
        recyclerView = findViewById(R.id.discount_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        float density = getResources().getDisplayMetrics().density;
        int sideMargin = (int) (16 * density);
        int topPadding = (int) (20 * density);
        recyclerView.setPadding(sideMargin, topPadding, sideMargin, 0);
        adapter = new EventAdapter(this, R.drawable.discount);
        recyclerView.setAdapter(adapter);

        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);
        cid = shared.getString("cid", "");

        try {
            String getlocation = readFile();
            doLoadlist(justhelper.BASE_URL + "/navigation/loaddiscounts.php?location=" + getlocation + "&cid=" + cid);
        } catch (IOException e) {
            e.printStackTrace();
            loadingContainer.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load discounts", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Loaddiscounts.this, Myactivity.class);
        startActivity(intent);
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
                            Toast.makeText(Loaddiscounts.this, "Failed to load discounts", Toast.LENGTH_SHORT).show();
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
        List<Event> events = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                events.add(Event.fromJson(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse discount data", Toast.LENGTH_SHORT).show();
        }
        adapter.setEvents(events);
    }

    // --- EventAdapter.OnEventClickListener ---

    @Override
    public void onEventClick(Event event) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("Start Navigation");
        dialog.setMessage("Navigate to " + event.venue + "?");

        dialog.setPositiveButton("Yes", (d, id) -> {
            thistag = event.placeId;
            checkAndRequestPermissions();
            d.dismiss();
        });

        dialog.setNegativeButton("No", (d, which) -> d.dismiss());

        dialog.create().show();
    }

    // --- Permissions + navigation ---

    private void checkAndRequestPermissions() {
        if (!isLocationPermissionGranted() || !isGpsEnabled()) {
            Intent activity = new Intent(getApplicationContext(), Nopermission.class);
            startActivity(activity);
        } else {
            gettheroutes(thistag);
        }
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void gettheroutes(String placeid) {
        try {
            returnroute(justhelper.BASE_URL + "/navigation/getroute.php?&id=" + placeid);
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
                                Toast.makeText(Loaddiscounts.this, "Failed to load route", Toast.LENGTH_SHORT).show()
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
                        activity.putExtra("theroute", routenow);
                        activity.putExtra("placeid", thistag);
                        activity.putExtra("preclass", "1");
                        startActivity(activity);
                    }
                });
    }

    // --- Fullscreen helpers ---

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
}
