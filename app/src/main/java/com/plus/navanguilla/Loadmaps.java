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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
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
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.plus.navanguilla.databinding.ActivityPickupBinding;
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

public class Loadmaps extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener  {


    String locationnow;
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


    String thelist;

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
        thelist = getIntent().getExtras().getString("list");

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }



        distancetoplace = (TextView)findViewById(R.id.distancetoplace);
        if (itemid.equals("1")){
            distancetoplace.setText("Beaches");
        }else if (itemid.equals("2")){
            distancetoplace.setText("Restaurants");
        }


        // String theroute = getroute(cunq, thisorderid);

        getback = (Button)findViewById(R.id.dialcustomer);
        //startroutex = (Button)findViewById(R.id.startjourney);
        //prvieworders = (Button)findViewById(R.id.prvieworders);
















        getback.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {




                AlertDialog.Builder builder = new AlertDialog.Builder(Loadmaps.this);
                builder.setTitle("Go Back");

                builder.setMessage(Html.fromHtml("<b>Return to list ?</b>"));

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        String tag = (String) view.getTag();
                        Intent intent = new Intent(getApplicationContext(), Loaditems.class);
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




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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



        String getlocation = readFile();
        String[] parts = getlocation.split(",");
        double mydoublelat = Double.parseDouble(parts[0]);
        double mydoublelon = Double.parseDouble(parts[1]);

        try {
            goloadmap(justhelper.BASE_URL + "/navigation/loadmaps.php?list="+thelist + "&location="+getlocation);

        } catch (IOException e) {
            e.printStackTrace();
        }


     LatLng source  = new LatLng(mydoublelat, mydoublelon);


        String API_KEY = getResources().getString(R.string.google_maps_key);

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





        //LatLng location = new LatLng(myddoublelat, myddoublelon);
        //Marker marker =  mMap.addMarker(new MarkerOptions().position(location).title("Destination"));
        //marker.showInfoWindow();
/*
        drivermaker = mMap.addMarker(new MarkerOptions().position(anguilla).title("My Location")
                .icon(BitmapFromVector(getApplicationContext(), R.drawable.ic_baseline_directions_car_24)));


 */
        //mMap.addMarker(new MarkerOptions().position(source).title("SOURCE"));
        //mMap.addMarker(new MarkerOptions().position(destination).title("DEST"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(anguilla));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mydoublelat,mydoublelon), 12.0f), 4000, null);


        // mMap.animateCamera( CameraUpdateFactory.zoomTo( 12.0f ) );
        //  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mydoublelat,mydoublelon), 16.0f), 4000, null);
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
            // Use the file contents as needed
            // Uncomment the line below to display a toast message with the content
            // Toast.makeText(getApplicationContext(), "Serlat: " + locationnow, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Error reading file
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return  locationnow;
    }


    void goloadmap(String url) throws IOException {
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


                               loadpointers(somebits);

                            }
                        });


                    }//end if




                });

    }


    public void loadpointers(String json) {

        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Extracting data from JSON object
                String placeId = jsonObject.getString("placeid");
                String whichSite = jsonObject.getString("whichsite");
                double distance = jsonObject.getDouble("distance");
                String formattedDistance = String.format("%.2f", distance);
                double dlat = jsonObject.getDouble("dlat");
                double dlon = jsonObject.getDouble("dlon");
                double mins = jsonObject.getDouble("mins");
                String cord = dlat + "/"+dlon;

if(itemid.equals("1")) {
    LatLng location = new LatLng(dlat, dlon);
    Marker marker = mMap.addMarker(new MarkerOptions()
            .position(location)
            .title(whichSite)
            .icon(createCustomMarker(whichSite + "\n" + mins + " mins", Color.BLUE, Color.WHITE)));
             marker.setTag(cord);
             mMap.setOnMarkerClickListener(this);

}else if (itemid.equals("2")) {

    LatLng location = new LatLng(dlat, dlon);
    Marker marker = mMap.addMarker(new MarkerOptions()
            .position(location)
            .title(whichSite)
            .icon(createCustomMarker(whichSite + "\n" + mins + " mins", R.color.myorange, Color.WHITE)));
            marker.setTag(cord);
            mMap.setOnMarkerClickListener(this);


}


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }




    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        // Handle the marker click event
        //Toast.makeText(this, "Marker clicked: " + marker.getTag(), Toast.LENGTH_SHORT).show();


        AlertDialog.Builder dialog = new AlertDialog.Builder(Loadmaps.this);
        dialog.setCancelable(false);
        dialog.setTitle("Let's get going");
        dialog.setMessage("Press yes to start route to " + marker.getTitle());
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {



                String modifiednow = locationnow.replace(',', '/');
                String routenow = modifiednow+"~"+marker.getTag();

                Log.i("ddevice",routenow + "whay:" + marker.getTag()); // Error
                Intent activity = new Intent(getApplicationContext(), Pickup.class);
                activity.putExtra("itemid",itemid);
                activity.putExtra("theroute",routenow);
                startActivity(activity);


                //gettheroutes(marker.getTag());


                dialog.dismiss();
            }
        })
                .setNegativeButton("No ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Action for "Cancel".
                        dialog.dismiss();
                    }
                });

        final AlertDialog alert = dialog.create();
        alert.show();




        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }




    private BitmapDescriptor createCustomMarker(String text, int bgColor, int textColor) {
        Paint textPaint = new Paint();
        textPaint.setTextSize(15); // Text size
        textPaint.setColor(textColor); // Text color

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(bgColor); // Background color

        // Calculate the width and height of the text
        float baseline = -textPaint.ascent(); // ascent() is negative
        int width = (int) (textPaint.measureText(text) + 20f); // Add some padding
        int height = (int) (baseline + textPaint.descent() + 20f);

        // Define pointer size
        int pointerWidth = 20;
        int pointerHeight = 10;

        // Increase height to accommodate the pointer
        height += pointerHeight;

        // Create a bitmap and draw background and text on it
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawRect(0, 0, width, height - pointerHeight, backgroundPaint); // Draw background
        canvas.drawText(text, 10, baseline + 10, textPaint); // Draw text

        // Draw the pointer
        Path path = new Path();
        path.moveTo((width - pointerWidth) / 2, height - pointerHeight); // Left point
        path.lineTo(width / 2, height); // Bottom point
        path.lineTo((width + pointerWidth) / 2, height - pointerHeight); // Right point
        path.close();

        canvas.drawPath(path, backgroundPaint); // Draw the pointer with background paint

        return BitmapDescriptorFactory.fromBitmap(image);
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



            String nextroute = mydoublelat + "," + mydoublelon + "," + dmylat + "," + dmylon ;

           /*
            try {
                sendforroute("https://axfull.com/nav/fetchroutedetails.php?location="+nextroute);

            } catch (IOException e) {
                e.printStackTrace();
            }
*/
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