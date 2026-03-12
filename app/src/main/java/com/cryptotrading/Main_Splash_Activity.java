package com.crypto_tab;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_splash);

        getWindow().setNavigationBarColor(Color.BLACK);


        Intent intent = new Intent(this, MarketOverviewActivity.class);
        startActivity(intent);
        finish();


    }
}
