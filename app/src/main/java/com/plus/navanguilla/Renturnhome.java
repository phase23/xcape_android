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
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class Renturnhome extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnPolylineClickListener, TextToSpeech.OnInitListener {

    String dmylat;
    String dmylon ;
    String somebits;
    Handler handler2;
    private GoogleMap mMap;
    private ActivityPickupBinding binding;
    String responseBody;
    String responseCheck;
    String fname;
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

    private Marker mUserMarker;
    private Polyline mRoute;
    private Marker mStartMarker;
    private Marker drivermaker;
    String thephone;
    Button getback;
    Button prvieworders;
    Button startroutex;
    String itemid;
    private boolean isTTSInitialized = false;
    private TextToSpeech tts;

    String theroute;

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



        ll = (LinearLayout) findViewById(R.id.topbar);
        ll.setAlpha(0.5f);

        itemid = getIntent().getExtras().getString("itemid","defaultKey");


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
        // String theroute = getroute(cunq, thisorderid);

        getback = (Button)findViewById(R.id.dialcustomer);
        //startroutex = (Button)findViewById(R.id.startjourney);
        //prvieworders = (Button)findViewById(R.id.prvieworders);
















        getback.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {




                AlertDialog.Builder builder = new AlertDialog.Builder(Renturnhome.this);
                builder.setTitle("Go Back");

                builder.setMessage(Html.fromHtml("<b>Return to list ?</b>"));

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        String tag = (String) view.getTag();
                        Intent intent = new Intent(getApplicationContext(), Myactivity.class);
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
            tts.speak("Proceed to the route, please remember to keep left", TextToSpeech.QUEUE_FLUSH, null, null);
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

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                float currentZoom = mMap.getCameraPosition().zoom;
                if (currentZoom != 18.0f) {
                    // The zoom level is not what we expected, try zooming again
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
                }
                // Else, the zoom level is as expected
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


            CameraPosition position = CameraPosition.builder()
                    .bearing(thebearing)
                    .target(new LatLng(mydoublelat, mydoublelon))
                    .zoom(mMap.getCameraPosition().zoom)
                    .tilt(30.0f) // Set tilt to 30 degrees
                    .build();

            //mMap.clear();
            drivermaker.remove();
            MarkerOptions mp = new MarkerOptions();
            mp.position(new LatLng(mydoublelat, mydoublelon));
            mp.title("my position");
            mp.icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarker));
            drivermaker = mMap.addMarker(mp);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));


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