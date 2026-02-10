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

public class LoadCalendar extends AppCompatActivity implements CalendarEventAdapter.OnCalendarEventClickListener {

    private Handler handler;
    private View loadingContainer;
    private View emptyContainer;
    private RecyclerView recyclerView;
    private CalendarEventAdapter adapter;
    private String cid;
    private String locationnow;
    private String pendingEventTitle;

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
        setContentView(R.layout.activity_loadcalendar);
        handler = new Handler(Looper.getMainLooper());

        Button goback = findViewById(R.id.backmain);
        loadingContainer = findViewById(R.id.loading_container);
        emptyContainer = findViewById(R.id.empty_container);

        goback.setOnClickListener(v -> {
            Intent intent = new Intent(LoadCalendar.this, Myactivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.calendar_event_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        float density = getResources().getDisplayMetrics().density;
        int sideMargin = (int) (16 * density);
        int topPadding = (int) (20 * density);
        recyclerView.setPadding(sideMargin, topPadding, sideMargin, 0);
        adapter = new CalendarEventAdapter(this);
        recyclerView.setAdapter(adapter);

        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);
        cid = shared.getString("cid", "");

        try {
            String getlocation = readFile();
            loadEvents(justhelper.BASE_URL + "/navigation/load_calendar.php?cid=" + cid + "&location=" + getlocation);
        } catch (IOException e) {
            e.printStackTrace();
            loadingContainer.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LoadCalendar.this, Myactivity.class);
        startActivity(intent);
    }

    void loadEvents(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Log.i("calendar", url);
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                Log.i("calendar", "error " + e);
                runOnUiThread(() -> {
                    loadingContainer.setVisibility(View.GONE);
                    Toast.makeText(LoadCalendar.this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String json = response.body().string();
                Log.i("calendar", json);
                handler.post(() -> {
                    parseAndDisplay(json);
                    loadingContainer.setVisibility(View.GONE);
                });
            }
        });
    }

    private void parseAndDisplay(String json) {
        List<CalendarEvent> events = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                events.add(CalendarEvent.fromJson(jsonArray.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to parse event data", Toast.LENGTH_SHORT).show();
        }

        if (events.isEmpty()) {
            emptyContainer.setVisibility(View.VISIBLE);
        } else {
            adapter.setEvents(events);
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

    @Override
    public void onCalendarEventClick(CalendarEvent event) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(event.eventTitle);

        String message = event.getFormattedDate();
        if (event.showtime != null && !event.showtime.isEmpty()) {
            message += "\n" + event.showtime;
        }
        if (event.eventDesc != null && !event.eventDesc.isEmpty()) {
            message += "\n\n" + event.eventDesc;
        }

        dialog.setMessage(message);
        dialog.setPositiveButton("OK", (d, which) -> d.dismiss());
        dialog.setCancelable(true);
        dialog.create().show();
    }

    @Override
    public void onCalendarEventNavigate(CalendarEvent event) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("Start Navigation");
        dialog.setMessage("Navigate to " + event.eventTitle + "?");

        dialog.setPositiveButton("Yes", (d, id) -> {
            d.dismiss();
            pendingEventTitle = event.eventTitle;
            navigateToCoordinates(event.eventLat, event.eventLng);
        });

        dialog.setNegativeButton("No", (d, which) -> d.dismiss());
        dialog.create().show();
    }

    private void navigateToCoordinates(String lat, String lng) {
        if (!isLocationPermissionGranted() || !isGpsEnabled()) {
            Intent activity = new Intent(getApplicationContext(), Nopermission.class);
            startActivity(activity);
            return;
        }

        String modifiednow = locationnow.replace(',', '/');
        String routenow = modifiednow + "~" + lat + "/" + lng;
        Log.d("routex", "calendar nav: " + routenow);

        Intent activity = new Intent(getApplicationContext(), Pickup.class);
        activity.putExtra("itemid", "");
        activity.putExtra("theroute", routenow);
        activity.putExtra("placeid", "");
        activity.putExtra("placename", pendingEventTitle);
        activity.putExtra("preclass", "3");
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
