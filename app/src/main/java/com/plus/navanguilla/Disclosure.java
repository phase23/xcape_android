package com.plus.navanguilla;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Disclosure extends AppCompatActivity {
    Button turnon;
    TextView nothanks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclosure);

        turnon = (Button)findViewById(R.id.turnon);
        nothanks = (TextView) findViewById(R.id.nothanks);

        turnon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Disclosure.this, Locationyes.class);
                startActivity(intent);

            }
        });

        nothanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Disclosure.this, SelectCountry.class);
                startActivity(intent);

            }
        });






    }
}