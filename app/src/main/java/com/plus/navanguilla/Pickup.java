package com.plus.navanguilla;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.ahmadrosid.lib.drawroutemap.DrawMarker;
//import com.ahmadrosid.lib.drawroutemap.DrawRouteMaps;
//import com.directions.route.Route;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.plus.navanguilla.databinding.ActivityPickupBinding;
import com.plus.navanguilla.util.DirectionPointListener;
import com.plus.navanguilla.util.GetPathFromLocation;
import com.plus.navanguilla.util.Routes;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class Pickup extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnPolylineClickListener, TextToSpeech.OnInitListener {

    String dmylat;
    String dmylon ;
    String somebits;
    Handler handler2;
    private GoogleMap mMap;
    private ActivityPickupBinding binding;
    String responseBody;
    String responseCheck;
    String fname;
    String prec;
    String cunq;
    String thisorderid;
    String whataction;
    TextView distancetoplace;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    LinearLayout ll;
    LinearLayout lb;
    private List<Routes> routes = new ArrayList<>();
    private Marker infoMarker = null;
    List<Waypoint> waypoints = new ArrayList<>();
    private Marker mUserMarker;
    private Polyline mRoute;
    private Marker mStartMarker;
    private Marker drivermaker;
    String thephone;
    Button getback;
    Button prvieworders;
    Button startroutex;
    String itemid;
    String placeid;
    String placename;
    private boolean isTTSInitialized = false;
    private TextToSpeech tts;
    private static final float BEARING_TOLERANCE = 20;
    String theroute;
    Boolean playedalready = false;
    String cid;
    String thiscountry;
    private boolean isFollowingUser = true;
    private ImageView recenterBtn;
    private double lastLat;
    private double lastLon;
    private float lastBearing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        binding = ActivityPickupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = shared.edit();
        handler2 = new Handler(Looper.getMainLooper());
        justhelper.setBrightness(this, 75); // Sets brightness to 75%


        ll = (LinearLayout) findViewById(R.id.topbar);
        ll.setAlpha(0.9f);

        itemid = getIntent().getExtras().getString("itemid","defaultKey");
        prec = getIntent().getExtras().getString("preclass","defaultKey");

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }



        distancetoplace = (TextView)findViewById(R.id.distancetoplace);


        theroute = getIntent().getExtras().getString("theroute");
        placeid = getIntent().getExtras().getString("placeid");
        placename = getIntent().getExtras().getString("placename", "");
        // String theroute = getroute(cunq, thisorderid);

        getback = (Button)findViewById(R.id.dialcustomer);
        //startroutex = (Button)findViewById(R.id.startjourney);
        //prvieworders = (Button)findViewById(R.id.prvieworders);

        tts = new TextToSpeech(this, this);
        String waymarkers = getwaymarkers();
        try {

            JSONArray jsonArray = new JSONArray(waymarkers);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String[] latLngParts = jsonObject.getString("latlng").split(", ");
                LatLng latLng = new LatLng(Double.parseDouble(latLngParts[0]), Double.parseDouble(latLngParts[1]));
                String speak = jsonObject.getString("speak");
                int bearing = jsonObject.getInt("bearing");
                int triggerRange = jsonObject.getInt("triggerrange");

                waypoints.add(new Waypoint(latLng, speak, bearing, triggerRange));
            }


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Initalising.. Tour loaded", Toast.LENGTH_SHORT).show();
            waypoints.add(new Waypoint(new LatLng(18.19238153341906, -63.09833374426017), "In 100 Meters Turn Left", 90, 50)); // Assuming 90 degrees is the bearing to turn right
            waypoints.add(new Waypoint(new LatLng(18.192557595008015, -63.09784515360257), "Turn Left", 90, 50)); // Assuming 90 degrees is the bearing to turn right

            waypoints.add(new Waypoint(new LatLng(18.195796254992818, -63.087607100510525), "At the end of the road, go straight ahead.", 90, 50)); // Assuming 0 degrees is the bearing to go straight
            waypoints.add(new Waypoint(new LatLng(18.195840567531334, -63.087199934507524), "Go straight ahead, watch for traffic on your right", 90, 20)); // Assuming 0 degrees is the bearing to go straight

            waypoints.add(new Waypoint(new LatLng(18.20000114849078, -63.076765863797235), "In 100 Metres at the roundabout take the 1st Exit", 45, 50)); // comnig down southhil
            waypoints.add(new Waypoint(new LatLng(18.200600442446454, -63.076055893507714), "Take the 1st Exit", 28, 20)); // southhill roundabout go sandyground

            waypoints.add(new Waypoint(new LatLng(18.20090333137871, -63.07654571085362), "Take the 1st Exit", 60, 30)); // coming up sandy grouond
            waypoints.add(new Waypoint(new LatLng(18.201198016627863, -63.07522519421326), "Take the 1st Exit", 157, 50)); // sandy ground going west
            waypoints.add(new Waypoint(new LatLng(18.19591537175044, -63.08612359637553), "Turn Left and follow the route", 270, 20)); //turn left going west after tastys
            waypoints.add(new Waypoint(new LatLng(18.192524989949273, -63.096556513609066), "Continue Straight", 260, 50)); // go staright down west not back street
            waypoints.add(new Waypoint(new LatLng(18.200650765521036, -63.0805041410244), "Continue Straight", 270, 50)); // go straight down sandyground
            waypoints.add(new Waypoint(new LatLng(18.201027307206783, -63.08150587105852), "At the end of the road Turn Left", 157, 50)); // turn left sandyground to go up

            waypoints.add(new Waypoint(new LatLng(18.223519108319238, -63.01142245845117), "In 100 Metres at the rounadbout take the 1st Exit", 215, 50)); // Round about Sandyhill
            waypoints.add(new Waypoint(new LatLng(18.223070878783407, -63.01177806290838), "take the 1st Exit", 215, 15)); // Round about Sandyhill

            waypoints.add(new Waypoint(new LatLng(18.207419610314545, -63.05634026686826), "In 100 Metres at the roundabout take the 1st Exit", 63, 50)); // valley roundabout going east
            waypoints.add(new Waypoint(new LatLng(18.20793427697478, -63.055391330772196), "Take the 1st Exit", 63, 50)); // valley roundabout going east



            // places names
            waypoints.add(new Waypoint(new LatLng(18.213889908109298, -63.048053439634735), "welcome to the Valley ", 115, 15)); // turn left sandyground to go up
            waypoints.add(new Waypoint(new LatLng(18.198495834657088, -63.08581933524174), "Welcome to Sandy Ground", 270, 15)); // go straight down sandyground
            waypoints.add(new Waypoint(new LatLng(18.193761532512006, -63.087741648177825), "Welcome to South Hill", 254, 15)); // go straight down sandyground
            waypoints.add(new Waypoint(new LatLng(18.19376301911901, -63.095331278322156), "Sandy Ground Lookout", 60, 15)); // Assuming 0 degrees is the bearing to go straight
            waypoints.add(new Waypoint(new LatLng(18.21394472114308, -63.04827859987668), "Welcome to The Valley", 20, 15)); // Assuming 0 degrees is the bearing to go straight
            waypoints.add(new Waypoint(new LatLng(18.210227731497774, -63.05394220165699), "Follow the route", 30, 15)); // Assuming 0 degrees is the bearing to go straight


        }














        getback.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {




                AlertDialog.Builder builder = new AlertDialog.Builder(Pickup.this);
                builder.setTitle("Go Back");

                builder.setMessage(Html.fromHtml("<b>Return to list ?</b>"));

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        String tag = (String) view.getTag();
                        Intent intent;
                        if(prec.equals("3")){
                            intent   = new Intent(getApplicationContext(), LoadCalendar.class);
                        }else if(prec.equals("2")){
                            intent   = new Intent(getApplicationContext(), LoadHistoricalSites.class);
                        }else if(prec.equals("1")){
                            intent   = new Intent(getApplicationContext(), Loadevents.class);
                        }else {
                             intent = new Intent(getApplicationContext(), Loaditems.class);
                        }
                        intent.putExtra("list",itemid);
                        startActivity(intent);

                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();



            }
        });

        tts = new TextToSpeech(this, this);

        recenterBtn = (ImageView) findViewById(R.id.recenterBtn);
        recenterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFollowingUser = true;
                recenterBtn.setVisibility(View.GONE);
                if (mMap != null) {
                    CameraPosition position = CameraPosition.builder()
                            .bearing(lastBearing)
                            .target(new LatLng(lastLat, lastLon))
                            .zoom(mMap.getCameraPosition().zoom)
                            .tilt(30.0f)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
                }
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            isTTSInitialized = true;
            // Optionally set language, pitch, etc.
            float speechRate = 0.7f; // 50% of the normal speech rate
            //tts.setSpeechRate(speechRate);
            String thislocation = (placename != null && !placename.isEmpty()) ? placename : getendlocation(placeid);
            tts.speak("Proceed to the route, please remember to keep left, navigating to " + thislocation, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            // Initialization failed
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mRoute = mMap.addPolyline(new PolylineOptions());


        boolean walkLine = true;
        //draw alternative routes if possible
        boolean alternatives = true;





        //String theroute = getroute(cunq, thisorderid, whataction);

        theroute = theroute.trim();
        String[] havles = theroute.split(Pattern.quote("~"));
        String mylocation;
        String mydestination;
        mylocation = havles[0];
        mydestination = havles[1];


        String[] latng = mylocation.split("/");
        String mylat = latng[0];
        String mylon = latng[1];


        String[] dlatng = mydestination.split("/");
         dmylat = dlatng[0];
         dmylon = dlatng[1];

        String sendroute = mylat +"," + mylon + ","+ dmylat + ","+dmylon;

        try {
            sendforroute(justhelper.BASE_URL + "/navigation/fetchroutedetails.php?location="+sendroute);

        } catch (IOException e) {
            e.printStackTrace();
        }




        Double mydoublelat = 0.0;
        try {
            mydoublelat = Double.parseDouble(mylat);
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }

        Double mydoublelon = 0.0;
        try {
            mydoublelon = Double.parseDouble(mylon);
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }


        Double myddoublelat = 0.0;
        try {
            myddoublelat = Double.parseDouble(dmylat);
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }

        Double myddoublelon = 0.0;
        try {
            myddoublelon = Double.parseDouble(dmylon);
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }



        LatLng source  = new LatLng(mydoublelat, mydoublelon);
        LatLng destination  = new LatLng(myddoublelat, myddoublelon);
        String waypoints = "";
        String API_KEY = getResources().getString(R.string.google_maps_key);
        new GetPathFromLocation(source, waypoints, destination, alternatives, walkLine, API_KEY, new DirectionPointListener() {
            @Override
            public void onPath(List<Routes> allRoutes) {
                routes = allRoutes;
                drawRoutes();
                drawDuration(0);
            }
        }).execute();

        mMap.setOnPolylineClickListener(this);

        //mMap.setOnPolylineClickListener(this);
        // Add a marker in Sydney and move the camera
        LatLng anguilla = new LatLng(mydoublelat, mydoublelon);
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setTrafficEnabled(true);
        drivermaker = mMap.addMarker(new MarkerOptions()
                .position(anguilla)
                .title("My Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker))
                );

        System.out.println("Where " + mydoublelat + " " + mydoublelon);

       // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(anguilla, 16.0f);
        //mMap.animateCamera(cameraUpdate);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(anguilla));
       // mMap.animateCamera( CameraUpdateFactory.zoomTo( 16.0f ) );

        LatLng location = new LatLng(myddoublelat, myddoublelon);
        Marker marker =  mMap.addMarker(new MarkerOptions().position(location).title("Destination"));
        marker.showInfoWindow();
/*
        drivermaker = mMap.addMarker(new MarkerOptions().position(anguilla).title("My Location")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_baseline_directions_car_24)));


 */
        //mMap.addMarker(new MarkerOptions().position(source).title("SOURCE"));
        //mMap.addMarker(new MarkerOptions().position(destination).title("DEST"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(anguilla));
       // mMap.animateCamera( CameraUpdateFactory.zoomTo( 18.0f ) );
       mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mydoublelat,mydoublelon), 16.0f), 4000, null);

        CameraPosition position = CameraPosition.builder()

                .target(new LatLng(mydoublelat, mydoublelon))
                .zoom(mMap.getCameraPosition().zoom)
                .tilt(30.0f)
                .build();

        // One-time zoom-in: animate to 18 then remove the listener
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                float currentZoom = mMap.getCameraPosition().zoom;
                if (currentZoom < 18.0f) {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
                } else {
                    mMap.setOnCameraIdleListener(null);
                }
            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    isFollowingUser = false;
                    recenterBtn.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public String   getwaymarkers(){

        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = shared.edit();

        cid = shared.getString("cid", "");
        thiscountry = shared.getString("country", "");

        String url = justhelper.BASE_URL + "/navigation/loadwaymarkers.php?type=general&cid="+cid;
        Log.i("action url",url);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)

                .addFormDataPart("what","this" )

                .build();
        Request request = new Request.Builder()
                .url(url)//your webservice url
                .post(requestBody)
                .build();
        try {
            //String responseBody;
            okhttp3.Response response = client.newCall(request).execute();
            // Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                Log.i("SUCC",""+response.message());
            }
            String resp = response.message();
            responseBody =  response.body().string();
            Log.i("respBody:main",responseBody);
            Log.i("MSG",resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBody;


    }


    public String getendlocation(String itemid){

        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String url = justhelper.BASE_URL + "/navigation/getendlocation.php?id=" + itemid;
        Log.i("action url",url);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)

                .addFormDataPart("what","this" )

                .build();
        Request request = new Request.Builder()
                .url(url)//your webservice url
                .post(requestBody)
                .build();
        try {
            //String responseBody;
            okhttp3.Response response = client.newCall(request).execute();
            // Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                Log.i("SUCC",""+response.message());
            }
            String resp = response.message();
            responseBody =  response.body().string();
            Log.i("respBody:main",responseBody);
            Log.i("MSG",resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBody;


    }

    private void recordVisit(String placeId, String placeName, double lat, double lng) {
        String deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String url = justhelper.BASE_URL + "/navigation/record_visit.php";

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("device_id", deviceId)
                .addFormDataPart("place_id", placeId)
                .addFormDataPart("place_name", placeName)
                .addFormDataPart("lat", String.valueOf(lat))
                .addFormDataPart("lng", String.valueOf(lng))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("RecordVisit", "Failed to record visit", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("RecordVisit", "Visit recorded: " + response.body().string());
                response.close();
            }
        });
    }



    //a dotted pattern for the walk line
    final List<PatternItem> pattern = Arrays.asList(new Dot(), new Gap(20));
    // color for different routes
    final int routeColors[] = {Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW};

    private void drawRoutes() {
        Routes route = null;
        int color = routeColors[0];
        //iterate over all routes
        for (int i = 0; i < routes.size(); i++) {
            route = routes.get(i);
            color = routeColors[i >= routes.size() ? 0 : i];
            //draw the driving route
            PolylineOptions options = new PolylineOptions()
                    .addAll(route.drivingRoute)
                    .width(10)
                    .color(color)
                    .clickable(true);
            //add the route to the map
            Polyline drivingRoute = mMap.addPolyline(options);
            //add tag to the route to be accessible
            drivingRoute.setTag(route.route_id);
        }
        //here we draw the dotted walk line once
        if (route != null && route.destWalk != null) {
            //the dotted line between source->near driving route
            PolylineOptions destWalk = new PolylineOptions()
                    .addAll(route.destWalk)
                    .width(10)
                    .color(color)
                    .pattern(pattern);
            //the dotted line between dest->last driving route
            PolylineOptions srcWalk = new PolylineOptions()
                    .addAll(route.sourceWalk)
                    .width(10)
                    .color(color)
                    .pattern(pattern);
            //add both routes to the map
            mMap.addPolyline(destWalk);
            mMap.addPolyline(srcWalk);
        }
    }

    private void drawDuration(int route_id) {
        //select route by id from multiple routes
        Routes route = null;
        for (Routes r : this.routes) {
            if (r.route_id == route_id) {
                route = r;
                break;
            }
        }
        if (route == null) return;

        /*get route duration*/
        //text value ex '8 mins'
        String text_duration = route.text_duration;
        //value in seconds ex '469'
        double duration = route.duration;

        /*get route distance*/
        //text value ex '12 km'
        String text_distance = route.text_distance;
        //value in meter ex '12000'
        double distance = route.distance;
        //select the middle point on the marker
        LatLng middlePoint = route.drivingRoute.get(route.drivingRoute.size() / 2);
        //draw window info to show the distance and duration
        if (infoMarker != null) infoMarker.remove();


        //distancetoplace.setText(text_duration + " - " + text_distance);

        /*
        infoMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(middlePoint)
                        .title(text_duration)
                        .snippet(text_distance)
        );

        infoMarker.showInfoWindow();
           */
    }


    void sendforroute(String url) throws IOException {
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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // For the example, you can show an error dialog or a toast
                                // on the main UI thread
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {


                        somebits = response.body().string();
                        Log.i("ddevice",somebits);

                        handler2.post(new Runnable() {
                            @Override
                            public void run() {

                                distancetoplace.setText(somebits);


                            }
                        });


                    }//end if




                });

    }

    @Override
    public void onPolylineClick(Polyline route) {
        //set the clicked route at the top
        route.setZIndex(route.getZIndex() + 1);
        //do something with the selected route..
        drawDuration((int) route.getTag());
    }







    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {

        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onResume() {
        super.onResume();
        // This registers messageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter("my-location"));
    }




    // Handling the received Intents for the "my-integer" event
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String mylat = intent.getStringExtra("mylat"); // -1 is going to be used as the default value
            String mylon = intent.getStringExtra("mylon");
            String mybearing = intent.getStringExtra("mybearing");

            System.out.println("degres  lat :" + mylat + " long : " +  mylon);
            Double mydoublelat = 0.0;
            try {
                mydoublelat = Double.parseDouble(mylat);
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            Double mydoublelon = 0.0;
            try {
                mydoublelon = Double.parseDouble(mylon);
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            float thebearing = 0;
            try {
                thebearing = Float.parseFloat(mybearing);
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }


            // destination
            Double myddoublelat = 0.0;
            try {
                myddoublelat = Double.parseDouble(dmylat);
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            Double myddoublelon = 0.0;
            try {
                myddoublelon = Double.parseDouble(dmylon);
            } catch(NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }



            boolean isClose = isWithin20MetersOfDestination(mydoublelat, mydoublelon, myddoublelat, myddoublelon);
            if(isClose) {
                // You are within 20 meters of the destination
                if(!playedalready){
                    String endlocation = (placename != null && !placename.isEmpty()) ? placename : getendlocation(placeid);
                    speak("You have arrived at you destination. " +endlocation);
                    recordVisit(placeid, endlocation, mydoublelat, mydoublelon);
                 playedalready = true;
                }
            } else {
                // You are more than 20 meters away from the destination
            }


            //float tilt = 30; // This tilts the camera by 30 degrees
            LatLng currentlocation = new LatLng(mydoublelat, mydoublelon);
            checkProximityToWaypoints(currentlocation, thebearing,waypoints);

            // Always store latest position for re-center
            lastLat = mydoublelat;
            lastLon = mydoublelon;
            lastBearing = thebearing;

            // Always update the driver marker
            drivermaker.remove();
            MarkerOptions mp = new MarkerOptions();
            mp.position(new LatLng(mydoublelat, mydoublelon));
            mp.title("my position");
            mp.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker));
            drivermaker = mMap.addMarker(mp);

            // Only move camera when following user
            if (isFollowingUser) {
                CameraPosition position = CameraPosition.builder()
                        .bearing(thebearing)
                        .target(new LatLng(mydoublelat, mydoublelon))
                        .zoom(mMap.getCameraPosition().zoom)
                        .tilt(30.0f)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            }


            String nextroute = mydoublelat + "," + mydoublelon + "," + dmylat + "," + dmylon ;






            try {
                sendforroute(justhelper.BASE_URL + "/navigation/fetchroutedetails.php?location="+nextroute);

            } catch (IOException e) {
                e.printStackTrace();
            }

/*
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mydoublelat, mydoublelon), 16));
*/

        }
    };


    public boolean isWithin20MetersOfDestination(double mydoublelat, double mydoublelon, double myddoublelat, double myddoublelon) {
        // Create Location objects for the current location and the destination
        Location currentLocation = new Location("currentLocation");
        currentLocation.setLatitude(mydoublelat);
        currentLocation.setLongitude(mydoublelon);

        Location destinationLocation = new Location("destinationLocation");
        destinationLocation.setLatitude(myddoublelat);
        destinationLocation.setLongitude(myddoublelon);

        // Calculate the distance between the current location and the destination
        float distance = currentLocation.distanceTo(destinationLocation);

        // Check if the distance is less than or equal to 20 meters
        return distance <= 50;
    }


    // Method to speak text
    private void speak(String text) {
        float speechRate = 0.8f; // 50% of the normal speech rate

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }



    private void checkProximityToWaypoints(LatLng currentLocationLatLng, float currentBearing, List<Waypoint> waypoints) {
        float[] results = new float[1];
        Iterator<Waypoint> iterator = waypoints.iterator();

        while (iterator.hasNext()) {
            Waypoint waypoint = iterator.next();
            Location.distanceBetween(currentLocationLatLng.latitude, currentLocationLatLng.longitude,
                    waypoint.getLocation().latitude, waypoint.getLocation().longitude, results);

            if (results[0] <= waypoint.getActionRange()) {
                if (Math.abs(waypoint.getApproachBearing() - currentBearing) <= BEARING_TOLERANCE) {
                    speak(waypoint.getAction()); // Speak the message
                    iterator.remove(); // Remove the waypoint from the list
                    break; // Stop checking further waypoints
                }
            }
        }
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        super.onPause();
    }

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

    /*
    @Override
    protected void onStart() {
        super.onStart();

        // Store our shared preference- NOT IN USE - TO GET
        SharedPreferences sp = getSharedPreferences("OURINFO", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", true);
        ed.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Store our shared preference
        SharedPreferences sp = getSharedPreferences("OURINFO", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.commit();

    }


     */








    @Override
    public void onBackPressed() {

    }


    public String getroute(String drvierid, String theorder, String action){

        String thisdevice = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String url = "https://axcess.ai/barapp/driver_route.php?&action=" + action + "&driverid="+drvierid + "&orderid=" + theorder;
        Log.i("action url",url);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)

                .addFormDataPart("what","this" )

                .build();
        Request request = new Request.Builder()
                .url(url)//your webservice url
                .post(requestBody)
                .build();
        try {
            //String responseBody;
            okhttp3.Response response = client.newCall(request).execute();
            // Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                Log.i("SUCC",""+response.message());
            }
            String resp = response.message();
            responseBody =  response.body().string();
            Log.i("respBody:main",responseBody);
            Log.i("MSG",resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBody;


    }





}