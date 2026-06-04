package com.example.gachonbridge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class WindActivity extends BaseActivity {

    public static final String WIND_URL = "https://wind.gachon.ac.kr/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wind);
        bindBottomNavigation(R.id.navWind);

        bindWindLink(R.id.cardWindRecommended1);
        bindWindLink(R.id.cardWindRecommended2);
        bindWindLink(R.id.cardWindRecommended3);
        bindWindLink(R.id.cardWindLatest1);
        bindWindLink(R.id.cardWindLatest2);
        bindWindLink(R.id.cardWindLatest3);
        bindWindLink(R.id.buttonWindMorePrograms);
    }

    private void bindWindLink(int viewId) {
        View view = findViewById(viewId);
        if (view == null) {
            return;
        }
        view.setOnClickListener(v -> openWindSite());
    }

    private void openWindSite() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WIND_URL)));
    }
}
