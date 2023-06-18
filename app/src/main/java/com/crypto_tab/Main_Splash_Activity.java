package com.crypto_tab;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class Main_Splash_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_splash);

        getWindow().setNavigationBarColor(Color.BLACK);


        Intent intent = new Intent(this, FullscreenActivity.class);
        startActivity(intent);
        finish();


    }
}
