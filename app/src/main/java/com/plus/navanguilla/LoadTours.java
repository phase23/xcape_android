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

public class LoadTours extends AppCompatActivity implements TourAdapter.OnTourClickListener {

    private Handler handler;
    private View loadingContainer;
    private View emptyContainer;
    private RecyclerView recyclerView;
    private TourAdapter adapter;
    private String cid;
    private String locationnow;

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
        setContentView(R.layout.activity_loadtours);
        handler = new Handler(Looper.getMainLooper());

        Button goback = findViewById(R.id.backmain);
        loadingContainer = findViewById(R.id.loading_container);
        emptyContainer = findViewById(R.id.empty_container);

        goback.setOnClickListener(v -> {
            Intent intent = new Intent(LoadTours.this, Myactivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.tour_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        float density = getResources().getDisplayMetrics().density;
        int sideMargin = (int) (16 * density);
        int topPadding = (int) (20 * density);
        recyclerView.setPadding(sideMargin, topPadding, sideMargin, 0);
        adapter = new TourAdapter(this);
        recyclerView.setAdapter(adapter);

        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);
        cid = shared.getString("cid", "");

        try {
            loadTours(justhelper.BASE_URL + "/navigation/load_tours.php?cid=" + cid);
        } catch (IOException e) {
            e.printStackTrace();
            loadingContainer.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load tours", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LoadTours.this, Myactivity.class);
        startActivity(intent);
    }

    void loadTours(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Log.i("tours", url);
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                Log.i("tours", "error " + e);
                runOnUiThread(() -> {
                    loadingContainer.setVisibility(View.GONE);
                    Toast.makeText(LoadTours.this, "Failed to load tours", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String json = response.body().string();
                Log.i("tours", json);
                handler.post(() -> {
                    parseAndDisplay(json);
                    loadingContainer.setVisibility(View.GONE);
                });
            }
        });
    }

    private void parseAndDisplay(String json) {
        List<Tour> tours = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                tours.add(Tour.fromJson(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse tour data", Toast.LENGTH_SHORT).show();
        }

        if (tours.isEmpty()) {
            emptyContainer.setVisibility(View.VISIBLE);
        } else {
            adapter.setTours(tours);
        }
    }

    @Override
    public void onTourClick(Tour tour) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("Start Tour");
        String message = "Start " + tour.tourName + "?";
        if (tour.tourDuration != null && !tour.tourDuration.isEmpty()) {
            message += "\nDuration: " + tour.tourDuration;
        }
        dialog.setMessage(message);

        dialog.setPositiveButton("Yes", (d, id) -> {
            d.dismiss();
            if (!isLocationPermissionGranted() || !isGpsEnabled()) {
                Intent noperm = new Intent(getApplicationContext(), Nopermission.class);
                startActivity(noperm);
                return;
            }
            locationnow = readFile();
            String modifiednow = locationnow.replace(',', '/');
            String routenow = modifiednow + "~" + tour.destLat + "/" + tour.destLng;
            Log.i("route", routenow);

            Intent activity = new Intent(getApplicationContext(), Islandtour.class);
            activity.putExtra("theroute", routenow);
            activity.putExtra("tour_id", tour.tid);
            activity.putExtra("tour_name", tour.tourName);
            activity.putExtra("tour_duration", tour.tourDuration);
            startActivity(activity);
        });

        dialog.setNegativeButton("No", (d, which) -> d.dismiss());
        dialog.create().show();
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

    private boolean isGpsEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
