package com.plus.navanguilla;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Myservice extends Service {
    private LocationManager locationManager;
    private LocationListener locationListener;
    Context mContext;
    String somebits;

    private static final String BADGE_CHANNEL_ID = "badge_notifications";
    private List<JSONObject> badgeList = new ArrayList<>();
    private boolean badgesLoaded = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext=this;


      //  Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationManager=(LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                2000,
                1, locationListenerGPS);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
              //  Toast.makeText(Myservice.this, "Location changed: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                //Toast.makeText(this, "Location listener is enabled", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(this, "GPS is disabled", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
           // Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
           // Toast.makeText(this, "GPS provider does not exist on this device", Toast.LENGTH_SHORT).show();
        }

        // Create badge notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    BADGE_CHANNEL_ID, "Badge Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications when you collect a badge");
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }

        // Fetch badges for proximity checking
        fetchBadges();
    }

    private void fetchBadges() {
        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);
        String cid = shared.getString("cid", "1");
        String url = justhelper.BASE_URL + "/navigation/load_badges.php?cid=" + cid;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("badge", "Failed to fetch badges for proximity");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                try {
                    JSONArray arr = new JSONArray(body);
                    List<JSONObject> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        list.add(arr.getJSONObject(i));
                    }
                    badgeList = list;
                    badgesLoaded = true;
                    Log.i("badge", "Loaded " + list.size() + " badges for proximity");
                } catch (Exception e) {
                    Log.i("badge", "Error parsing badges: " + e.getMessage());
                }
            }
        });
    }




    @Override
    public void onStart(Intent intent, int startid) {
       // Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Your start command handling
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // Anguilla island center
    private static final double ISLAND_LAT = 18.2206;
    private static final double ISLAND_LNG = -63.0686;
    private static final double ISLAND_RADIUS_KM = 40.0;

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    LocationListener locationListenerGPS=new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
            Float bearing = location.getBearing();
            String msg= latitude + ","+ longitude;
            Log.d("Changed", ": " + msg);

            // Check if user is within Anguilla boundary
            boolean onIsland = haversineKm(latitude, longitude, ISLAND_LAT, ISLAND_LNG) <= ISLAND_RADIUS_KM;

            // Write island center to navi.txt if off-island, real coords if on-island
            if (onIsland) {
                createAndWriteToFile(msg);
            } else {
                createAndWriteToFile(ISLAND_LAT + "," + ISLAND_LNG);
            }

            // Always broadcast real coordinates for live navigation
            sendnewlocationtomaps( latitude, longitude, bearing);

            // Only check badge proximity when on-island
            if (onIsland) {
                checkBadgeProximity(latitude, longitude);
            }
            //Toast.makeText(getApplicationContext(), "Location changed", Toast.LENGTH_LONG).show();

            String thisdevice = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            try {
                updatedevicelocation(justhelper.BASE_URL + "/navigation/updatedevicelocation.php?lat="+latitude + "&long="+longitude + "&device="+ thisdevice);

            } catch (IOException e) {
                e.printStackTrace();
            }



           /*
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://axcessdrivers-default-rtdb.firebaseio.com/");

            DatabaseReference newdriver = database.getReference(thisdevice);
            newdriver.child("latitude").setValue(latitude);
            newdriver.child("longitude").setValue(longitude);

           */

        }


        void updatedevicelocation(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.i("ddevice",url);
            OkHttpClient client = new OkHttpClient();
            client.newCall(request)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(final Call call, IOException e) {
                            Log.i("ddevice","errot"); // Error


                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {


                            somebits = response.body().string();



                        }//end if




                    });

        }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }





    };


    private void sendnewlocationtomaps(Double lat, Double lon, Float Bearing){
        Intent intent = new Intent("my-location");
        // Adding some data
        String mylatconvert = String.valueOf(lat);
        String mylongconvert = String.valueOf(lon);
        String mybearingconvert = String.valueOf(Bearing);

        intent.putExtra("mylat", mylatconvert);
        intent.putExtra("mylon", mylongconvert);
        intent.putExtra("mybearing", mybearingconvert);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }


    public void createAndWriteToFile(String data) {
        String fileName = "navi.txt";
        System.out.println("ss: " + data);

        FileOutputStream fos = null;
        try {
            fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // --- Badge proximity checking ---

    private void checkBadgeProximity(double lat, double lng) {
        if (!badgesLoaded || badgeList.isEmpty()) return;

        SharedPreferences prefs = getSharedPreferences("badge_collected", MODE_PRIVATE);
        Location current = new Location("");
        current.setLatitude(lat);
        current.setLongitude(lng);

        for (JSONObject badge : badgeList) {
            try {
                String bid = badge.getString("bid");

                // Already collected locally?
                if (prefs.getBoolean("badge_" + bid, false)) continue;

                String latlng = badge.getString("latlng");
                String[] parts = latlng.split(",");
                double bLat = Double.parseDouble(parts[0].trim());
                double bLng = Double.parseDouble(parts[1].trim());
                double range = badge.optDouble("triggerrange", 50);

                Location badgeLoc = new Location("");
                badgeLoc.setLatitude(bLat);
                badgeLoc.setLongitude(bLng);

                float distance = current.distanceTo(badgeLoc);

                if (distance <= range) {
                    // Mark collected locally immediately to prevent repeat triggers
                    prefs.edit().putBoolean("badge_" + bid, true).apply();

                    String name = badge.optString("badge_name", "Badge");
                    String desc = badge.optString("badge_desc", "");
                    String icon = badge.optString("badge_icon", "star");

                    // Collect on server
                    collectBadgeOnServer(bid, lat, lng, name, desc, icon);
                }
            } catch (Exception e) {
                Log.i("badge", "Error checking badge: " + e.getMessage());
            }
        }
    }

    private void collectBadgeOnServer(String badgeId, double lat, double lng,
                                       String badgeName, String badgeDesc, String badgeIcon) {
        String thisdevice = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String url = justhelper.BASE_URL + "/navigation/collect_badge.php";

        RequestBody body = new FormBody.Builder()
                .add("device_id", thisdevice)
                .add("badge_id", badgeId)
                .add("lat", String.valueOf(lat))
                .add("lng", String.valueOf(lng))
                .build();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("badge", "Failed to collect badge on server");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(respBody);
                    String status = json.optString("status", "");
                    if (status.equals("ok")) {
                        int collected = json.optInt("collected", 0) + 1;  // +1 welcome badge
                        int total = json.optInt("total", 0) + 1;         // +1 welcome badge
                        boolean allCollected = (collected >= total);

                        if (allCollected) {
                            // Special "all collected" notification
                            showAllBadgesNotification(total);
                        } else {
                            // Normal badge notification
                            showBadgeNotification(badgeName, badgeDesc, badgeIcon, collected, total);
                        }

                        // Broadcast to any active activity to show dialog
                        Intent intent = new Intent("badge-collected");
                        intent.putExtra("badge_name", badgeName);
                        intent.putExtra("badge_desc", badgeDesc);
                        intent.putExtra("badge_icon", badgeIcon);
                        intent.putExtra("collected", collected);
                        intent.putExtra("total", total);
                        intent.putExtra("all_collected", allCollected);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    }
                } catch (Exception e) {
                    Log.i("badge", "Error parsing collect response: " + e.getMessage());
                }
            }
        });
    }

    private void showBadgeNotification(String badgeName, String badgeDesc, String badgeIcon,
                                        int collected, int total) {
        String emoji = getEmojiForIcon(badgeIcon);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, Myactivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, BADGE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(emoji + " Badge Collected!")
                .setContentText(badgeName + " — " + collected + " of " + total + " badges collected")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(badgeName + "\n" + badgeDesc + "\n\n" + collected + " of " + total + " badges collected"))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        nm.notify(Integer.parseInt(String.valueOf(System.currentTimeMillis() % 100000)), builder.build());
    }

    private void showAllBadgesNotification(int total) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, Myactivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, BADGE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("\uD83C\uDFC6 Island Warrior!")
                .setContentText("You've collected all " + total + " badges!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Congratulations! You've collected every badge on the island!\n\n"
                                + "All " + total + " badges earned. You are a true Island Warrior!"))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        nm.notify(Integer.parseInt(String.valueOf(System.currentTimeMillis() % 100000)), builder.build());
    }

    private String getEmojiForIcon(String icon) {
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
            default: return "\u2B50"; // star
        }
    }
}
