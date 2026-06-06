package com.example.gachonbridge;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ClubDetailActivity extends BaseActivity {

    public static final String EXTRA_NAME = "club_name";
    public static final String EXTRA_CATEGORY = "club_category";
    public static final String EXTRA_DESCRIPTION = "club_description";
    public static final String EXTRA_BADGE = "club_badge";
    public static final String EXTRA_ICON = "club_icon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_detail);
        bindBottomNavigation(R.id.navClub);
        bindBackButton();
        bindClubDetail();
    }

    @Override
    protected boolean shouldBindBackToHome() {
        return false;
    }

    private void bindBackButton() {
        View backButton = findViewById(R.id.buttonClubDetailBack);
        backButton.setOnClickListener(v -> finish());
    }

    private void bindClubDetail() {
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        String shortDesc = getIntent().getStringExtra("club_short_description");
        String longDesc = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        String badge = getIntent().getStringExtra(EXTRA_BADGE);
        String icon = getIntent().getStringExtra(EXTRA_ICON);

        if (name == null) {
            name = getString(R.string.page_club_title);
        }
        if (category == null) {
            category = "";
        }
        if (shortDesc == null) {
            shortDesc = "";
        }
        if (longDesc == null) {
            longDesc = "";
        }
        if (badge == null || badge.isEmpty()) {
            badge = category;
        }
        if (icon == null || icon.isEmpty()) {
            icon = "GB";
        }

        setText(R.id.clubDetailHeroBadge, badge);
        setText(R.id.clubDetailHeroTitle, name);
        setText(R.id.clubDetailHeroSubtitle, shortDesc);
        setText(R.id.clubDetailIntroTitle, "동아리 소개");
        setText(R.id.clubDetailIntroBody, longDesc);
        setText(R.id.clubDetailPhotoTitle, "활동 사진");
        setText(R.id.clubDetailHeroIcon, icon);
        setText(R.id.clubDetailPhotoMain, icon);
        setText(R.id.clubDetailPhotoSide, badge);
    }

    private void setText(int viewId, String text) {
        TextView textView = findViewById(viewId);
        if (textView != null) {
            textView.setText(text);
        }
    }
}
