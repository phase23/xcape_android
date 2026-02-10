package com.plus.navanguilla;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Esims extends AppCompatActivity {
    Button goback;
    Button buysim;
    String thiscountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esims);

        justhelper.setBrightness(this, 75); // Sets brightness to 75%
        goback = (Button)findViewById(R.id.backmain);
        buysim = (Button)findViewById(R.id.buy_plan_button);

        SharedPreferences shared = getSharedPreferences("autoLogin", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = shared.edit();

        thiscountry = shared.getString("country", "");

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Esims.this, Myactivity.class);
                startActivity(intent);

            }
        });


        buysim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = justhelper.BASE_URL + "/esims/?os=android&country="+thiscountry; // Replace with your actual URL
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent); // Launch the browser
                // Verify that there is an app available to handle the Intent
                if (intent.resolveActivity(getPackageManager()) != null) {
                  //  startActivity(intent); // Launch the browser
                } else {
                   // Toast.makeText(getApplicationContext(), "No browser app found to open the link", Toast.LENGTH_LONG).show();
                }




            }
        });








    }
}