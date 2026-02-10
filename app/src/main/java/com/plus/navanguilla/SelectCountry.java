package com.plus.navanguilla;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SelectCountry extends AppCompatActivity {
    String somebits;
    Handler handler2;
    String responseLocation;
    String cid;
    SharedPreferences sharedpreferences;
    int autoSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_country);



        final LinearLayout layout = findViewById(R.id.scnf);
        justhelper.setBrightness(this, 75); // Sets brightness to 75%
        handler2 = new Handler(Looper.getMainLooper());

        sharedpreferences = getSharedPreferences("autoLogin", Context.MODE_PRIVATE);
        int j = sharedpreferences.getInt("key", 0);

        Bundle extras = getIntent().getExtras();
        String changecountry;

// Check if the extras Bundle is null
        if (extras != null) {
            // If extras exist, retrieve the "change" string, using "defaultKey" as a fallback
            changecountry = extras.getString("change", "defaultKey");
        } else {
            // If there are no extras, set "changecountry" to "defaultKey" or any default value as needed
            changecountry = "";
        }


        // Check if "changecountry" is an empty string
        if (changecountry.equals("")) {
            // Assuming 'j' is defined elsewhere and its condition is to be checked
            if (j > 0) {
                // If 'j' is greater than 0, start "Myactivity"
                Intent activity = new Intent(getApplicationContext(), Myactivity.class);
                startActivity(activity);
            }
        } // En



        try {
            doGetRequest(justhelper.BASE_URL + "/navigation/selectcountry.php");




        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    public void countrylist(String json) {

        int totalWidth = getResources().getDisplayMetrics().widthPixels;
        int margin = (int) (totalWidth * 0.10);  // 30% of screen width


        try {
            JSONArray jsonArray = new JSONArray(json);

            // Loop through the JSON object and create buttons

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                cid = jsonObject.getString("cid");

                String country = jsonObject.getString("country").trim();
                String code = jsonObject.getString("code").trim();
                // Create a button with the index as a tag
                Button button = new Button(this);
                button.setTag(cid);
                button.setTag(R.id.tag_first, country);
                button.setText(country);

                // Add an OnClickListener to handle button clicks
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Handle button click here
                        String  tag = (String) view.getTag();
                        String thiscountry = (String) button.getTag(R.id.tag_first);

                        Log.i("tagg url",tag + "/");


                        autoSave = 1;
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt("key", autoSave);
                        editor.putString("cid", tag);
                        editor.putString("country", thiscountry);
                        editor.apply();


                        Intent activity = new Intent(getApplicationContext(), Myactivity.class);
                        startActivity(activity);



                    }
                });


                // Setting button height
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                buttonParams.height = 80;  // adjust this value to your liking
                button.setTextSize(14);  // adjust this value to your liking
                int padding = 20;  // adjust this value to your liking
                button.setPadding(padding, padding, padding, padding);

                // Aligning text to the left and adding an image
                button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);  // This aligns the text to the left
                int drawableLeft;
                drawableLeft = R.drawable.beach;  // Replace with your drawable resource ID
                // Log.i("side",tag);


                button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                button.setCompoundDrawablePadding(10); // Optional, if you want padding between text and image
                button.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_button_background_beach));

// Setting margins
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(margin, 0, margin, 55);
                button.setLayoutParams(layoutParams);

// Add the button to your layout
                LinearLayout linearLayout = findViewById(R.id.scnf); // Replace with your layout ID
                linearLayout.addView(button);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }








    }










    void doGetRequest(String url) throws IOException {
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

                                countrylist(somebits);

                            }
                        });


                    }//end if




                });

    }




}