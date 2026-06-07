package com.example.gachonbridge;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class ClubDetailActivity extends BaseActivity {

    public static final String EXTRA_NAME = "club_name";
    public static final String EXTRA_CATEGORY = "club_category";
    public static final String EXTRA_DESCRIPTION = "club_description";
    public static final String EXTRA_BADGE = "club_badge";
    public static final String EXTRA_ICON = "club_icon";
    public static final String EXTRA_IMAGE_URL = "club_image_url";

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
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);

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

        setText(R.id.clubDetailHeroBadge, badge);
        setText(R.id.clubDetailHeroTitle, name);
        setText(R.id.clubDetailHeroSubtitle, shortDesc);
        setText(R.id.clubDetailIntroTitle, "동아리 소개");
        setText(R.id.clubDetailIntroBody, longDesc);
        setText(R.id.clubDetailPhotoTitle, "활동 사진");

        String activity = getIntent().getStringExtra("club_activity");
        String location = getIntent().getStringExtra("club_location");

        bindTextOrHide(R.id.clubDetailActivitySection, R.id.clubDetailActivityBody, activity);
        bindTextOrHide(R.id.clubDetailLocationSection, R.id.clubDetailLocationBody, location);

        ImageView heroImage = findViewById(R.id.clubDetailHeroImage);
        ImageView photoMain = findViewById(R.id.clubDetailPhotoMain);
        ImageView photoSide = findViewById(R.id.clubDetailPhotoSide);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Load from drawable if it's just a filename
            int resId = getResources().getIdentifier(imageUrl, "drawable", getPackageName());
            if (resId != 0) {
                Glide.with(this).load(resId).into(heroImage);
                Glide.with(this).load(resId).into(photoMain);
                Glide.with(this).load(resId).into(photoSide);
            } else if (imageUrl.startsWith("http")) {
                Glide.with(this).load(imageUrl).into(heroImage);
                Glide.with(this).load(imageUrl).into(photoMain);
                Glide.with(this).load(imageUrl).into(photoSide);
            }
        }
    }

    private void setText(int viewId, String text) {
        TextView textView = findViewById(viewId);
        if (textView != null) {
            textView.setText(text);
        }
    }

    private void bindTextOrHide(int sectionId, int bodyId, String text) {
        View section = findViewById(sectionId);
        TextView body = findViewById(bodyId);
        if (text == null || text.trim().isEmpty()) {
            if (section != null) section.setVisibility(View.GONE);
        } else {
            if (section != null) section.setVisibility(View.VISIBLE);
            if (body != null) body.setText(text);
        }
    }
}
