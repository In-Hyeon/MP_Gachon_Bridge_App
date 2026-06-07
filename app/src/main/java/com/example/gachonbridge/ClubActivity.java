package com.example.gachonbridge;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClubActivity extends BaseActivity {

    public static final String EXTRA_SELECTED_CAMPUS = "selected_campus";
    public static final String EXTRA_SELECTED_CATEGORY = "selected_category";
    public static final int CAMPUS_GLOBAL = 0;
    public static final int CAMPUS_MEDICAL = 1;

    private static final String[] CATEGORIES_GLOBAL = { "전체", "음악", "공연", "사회·학술", "종교", "체육", "전시·취미" };
    private static final String[] CATEGORIES_MEDICAL = { "전체", "공연", "봉사", "운동·레저", "종교" };

    private String[] getCurrentCategories() { return selectedCampus == CAMPUS_GLOBAL ? CATEGORIES_GLOBAL : CATEGORIES_MEDICAL; }

    private final List<Club> globalClubs = new ArrayList<>();
    private final List<Club> medicalClubs = new ArrayList<>();

    private TextView campusGlobal, campusMedical;
    private LinearLayout categoryContainer;
    private TextView[] categoryTabs;
    private View featuredClubCard;
    private ImageView featuredClubImage;
    private TextView featuredBadge, featuredCategory, featuredTitle, featuredBody;
    private LinearLayout clubList;
    private int selectedCampus = CAMPUS_GLOBAL;
    private int selectedCategory = 0;

    private Club currentlyFeaturedClub = null;
    private android.os.Handler carouselHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable carouselRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club);
        bindBottomNavigation(R.id.navClub);
        loadClubsFromJson();
        readInitialSelection();
        bindViews();
        bindCampusTabs();
        createCategoryTabs();
        renderClubScreen();
    }

    private void loadClubsFromJson() {
        try {
            globalClubs.clear(); medicalClubs.clear();
            loadCampusClubs("clubs_global.json", globalClubs);
            loadCampusClubs("clubs_medical.json", medicalClubs);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadCampusClubs(String filename, List<Club> targetList) throws Exception {
        InputStream is = getAssets().open(filename);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer); is.close();
        String json = new String(buffer, StandardCharsets.UTF_8);
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            targetList.add(new Club(
                    obj.optString("name", ""), obj.optString("division", ""),
                    obj.optString("short_description", ""), obj.optString("long_description", ""),
                    obj.optString("activity", ""), obj.optString("location", ""), 
                    obj.optString("image_url", "")
            ));
        }
    }

    private void readInitialSelection() {
        selectedCampus = getIntent().getIntExtra(EXTRA_SELECTED_CAMPUS, CAMPUS_GLOBAL);
        selectedCategory = findCategoryIndex(getIntent().getStringExtra(EXTRA_SELECTED_CATEGORY));
    }

    private int findCategoryIndex(String category) {
        if (category == null) return 0;
        String[] cats = getCurrentCategories();
        for (int i = 0; i < cats.length; i++) if (cats[i].equals(category)) return i;
        return 0;
    }

    private void bindViews() {
        campusGlobal = findViewById(R.id.tabCampusGlobal);
        campusMedical = findViewById(R.id.tabCampusMedical);
        categoryContainer = findViewById(R.id.clubCategoryContainer);
        featuredClubCard = findViewById(R.id.featuredClubCard);
        featuredClubImage = findViewById(R.id.featuredClubImage);
        featuredBadge = findViewById(R.id.featuredClubBadge);
        featuredCategory = findViewById(R.id.featuredClubCategory);
        featuredTitle = findViewById(R.id.featuredClubTitle);
        featuredBody = findViewById(R.id.featuredClubBody);
        clubList = findViewById(R.id.clubList);
    }

    private void bindCampusTabs() {
        campusGlobal.setOnClickListener(v -> { selectedCampus = CAMPUS_GLOBAL; selectedCategory = 0; createCategoryTabs(); renderClubScreen(); });
        campusMedical.setOnClickListener(v -> { selectedCampus = CAMPUS_MEDICAL; selectedCategory = 0; createCategoryTabs(); renderClubScreen(); });
    }

    private void createCategoryTabs() {
        String[] cats = getCurrentCategories();
        categoryTabs = new TextView[cats.length];
        categoryContainer.removeAllViews();
        for (int i = 0; i < cats.length; i++) {
            final int index = i;
            TextView tab = new TextView(this); tab.setText(cats[i]);
            tab.setOnClickListener(v -> { selectedCategory = index; renderClubScreen(); });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(42));
            params.setMarginEnd(dp(14)); categoryContainer.addView(tab, params);
            categoryTabs[i] = tab; applyCategoryTabStyle(tab, index == selectedCategory);
        }
    }

    private void renderClubScreen() {
        final List<Club> allClubs = selectedCampus == CAMPUS_GLOBAL ? globalClubs : medicalClubs;
        updateCampusTabs(); updateCategoryTabs();
        if (carouselRunnable != null) carouselHandler.removeCallbacks(carouselRunnable);

        final List<Club> visibleClubs = new ArrayList<>();
        for (Club c : allClubs) if (isVisible(c)) visibleClubs.add(c);

        if (selectedCategory == 0 && !visibleClubs.isEmpty()) {
            featuredClubCard.setVisibility(View.VISIBLE);
            Random random = new Random();
            currentlyFeaturedClub = visibleClubs.get(random.nextInt(visibleClubs.size()));
            updateFeaturedClubUI(currentlyFeaturedClub);
            carouselRunnable = new Runnable() {
                @Override
                public void run() {
                    if (visibleClubs.size() > 1) {
                        Club next;
                        do { next = visibleClubs.get(random.nextInt(visibleClubs.size())); } while (next == currentlyFeaturedClub);
                        currentlyFeaturedClub = next;
                    }
                    updateFeaturedClubUI(currentlyFeaturedClub);
                    // Removed updateClubList call to prevent flickering, as the list is now constant
                    carouselHandler.postDelayed(this, 6000);
                }
            };
            carouselHandler.postDelayed(carouselRunnable, 6000);
        } else {
            featuredClubCard.setVisibility(View.GONE);
            currentlyFeaturedClub = null;
        }
        updateClubList(allClubs);
    }

    private void updateCampusTabs() {
        updateSegmentTab(campusGlobal, selectedCampus == CAMPUS_GLOBAL);
        updateSegmentTab(campusMedical, selectedCampus == CAMPUS_MEDICAL);
    }

    private void updateSegmentTab(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected ? R.drawable.bg_chip_active : 0);
        tab.setTextColor(ContextCompat.getColor(this, selected ? R.color.gb_on_primary : R.color.gb_on_surface_variant));
        tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void updateCategoryTabs() { for (int i = 0; i < categoryTabs.length; i++) applyCategoryTabStyle(categoryTabs[i], i == selectedCategory); }

    private void applyCategoryTabStyle(TextView tab, boolean selected) {
        tab.setMinWidth(dp(78)); tab.setGravity(Gravity.CENTER); tab.setTextSize(14);
        tab.setTypeface(null, Typeface.BOLD); tab.setSingleLine(true); tab.setPadding(dp(20), 0, dp(20), 0);
        tab.setBackgroundResource(selected ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        tab.setTextColor(ContextCompat.getColor(this, selected ? R.color.gb_on_primary : R.color.gb_on_surface));
    }

    private void updateFeaturedClubUI(Club featured) {
        if (featured == null) return;
        featuredBadge.setText(featured.category); featuredCategory.setText(featured.category);
        featuredTitle.setText(featured.name); featuredBody.setText(featured.shortDescription);
        featuredClubCard.setOnClickListener(v -> openClubDetail(featured));
        featuredClubImage.setImageDrawable(null);
        if (featured.imageUrl != null && !featured.imageUrl.isEmpty()) {
            int resId = getResources().getIdentifier(featured.imageUrl, "drawable", getPackageName());
            if (resId != 0) Glide.with(this).load(resId).into(featuredClubImage);
            else if (featured.imageUrl.startsWith("http")) Glide.with(this).load(featured.imageUrl).into(featuredClubImage);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (carouselHandler != null && carouselRunnable != null) carouselHandler.removeCallbacks(carouselRunnable);
    }

    private void updateClubList(List<Club> clubs) {
        clubList.removeAllViews();
        for (Club club : clubs) {
            if (!isVisible(club)) continue;
            clubList.addView(createClubCard(club));
        }
    }

    private boolean isVisible(Club club) {
        if (selectedCategory == 0) return true;
        String tabName = getCurrentCategories()[selectedCategory];
        String clubDiv = club.category;
        if (tabName.equals("사회·학술") && clubDiv.equals("사회학술")) return true;
        if (tabName.equals("전시·취미") && clubDiv.equals("전시취미")) return true;
        if (tabName.equals("운동·레저") && clubDiv.equals("운동레저")) return true;
        if (tabName.equals("체육") && clubDiv.equals("운동레저")) return true;
        return clubDiv.equals(tabName);
    }

    private View createClubCard(Club club) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackgroundResource(R.drawable.bg_card); card.setClickable(true); card.setFocusable(true);
        card.setOnClickListener(v -> openClubDetail(club));
        card.setLayoutParams(new LinearLayout.LayoutParams(-1, -2) {{ setMargins(0, 0, 0, dp(16)); }});

        FrameLayout imageArea = new FrameLayout(this);
        LinearLayout.LayoutParams iaParams = new LinearLayout.LayoutParams(dp(86), dp(86));
        iaParams.setMarginEnd(dp(18)); card.addView(imageArea, iaParams);

        // Fallback Letter Area
        TextView fallback = new TextView(this); fallback.setText(club.icon); fallback.setGravity(Gravity.CENTER);
        fallback.setTextSize(26); fallback.setTypeface(null, Typeface.BOLD);
        fallback.setTextColor(Color.WHITE);
        
        // Generate unique colored background for fallback based on name
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(dp(16));
        int hashColor = Color.HSVToColor(new float[]{ Math.abs(club.name.hashCode() % 360), 0.6f, 0.8f });
        gd.setColor(hashColor);
        fallback.setBackground(gd);
        imageArea.addView(fallback, new FrameLayout.LayoutParams(-1, -1));

        // Actual Image (Only visible if loaded)
        ImageView icon = new ImageView(this); icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        icon.setBackgroundResource(R.drawable.bg_club_photo); icon.setClipToOutline(true);
        icon.setVisibility(View.GONE);
        
        if (club.imageUrl != null && !club.imageUrl.isEmpty()) {
            int resId = getResources().getIdentifier(club.imageUrl, "drawable", getPackageName());
            if (resId != 0 || club.imageUrl.startsWith("http")) {
                icon.setVisibility(View.VISIBLE);
                Glide.with(this).load(resId != 0 ? resId : club.imageUrl).into(icon);
            }
        }
        imageArea.addView(icon, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL);
        card.addView(body, new LinearLayout.LayoutParams(0, -2, 1));

        TextView category = new TextView(this); category.setText(club.category); category.setGravity(Gravity.CENTER);
        category.setTextSize(13); category.setPadding(dp(12), 0, dp(12), 0);
        category.setTextColor(ContextCompat.getColor(this, R.color.gb_on_surface_variant));
        category.setBackgroundResource(R.drawable.bg_chip_inactive);
        body.addView(category, new LinearLayout.LayoutParams(-2, dp(32)));

        TextView title = new TextView(this); title.setText(club.name);
        title.setTextColor(ContextCompat.getColor(this, R.color.gb_on_surface));
        title.setTextSize(20); title.setTypeface(null, Typeface.BOLD); title.setSingleLine(true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2); tp.setMargins(0, dp(8), 0, 0);
        body.addView(title, tp);

        TextView desc = new TextView(this); desc.setText(club.shortDescription);
        desc.setTextColor(ContextCompat.getColor(this, R.color.gb_on_surface_variant));
        desc.setTextSize(14); desc.setSingleLine(true);
        LinearLayout.LayoutParams dp1 = new LinearLayout.LayoutParams(-1, -2); dp1.setMargins(0, dp(6), 0, 0);
        body.addView(desc, dp1);

        return card;
    }

    private void openClubDetail(Club club) {
        Intent intent = new Intent(this, ClubDetailActivity.class);
        intent.putExtra(ClubDetailActivity.EXTRA_NAME, club.name);
        intent.putExtra(ClubDetailActivity.EXTRA_CATEGORY, club.category);
        intent.putExtra("club_short_description", club.shortDescription);
        intent.putExtra(ClubDetailActivity.EXTRA_DESCRIPTION, club.longDescription);
        intent.putExtra(ClubDetailActivity.EXTRA_IMAGE_URL, club.imageUrl);
        intent.putExtra("club_activity", club.activity);
        intent.putExtra("club_location", club.location);
        startActivity(intent);
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    public static class Club {
        public final String name, category, shortDescription, longDescription, activity, location, imageUrl, icon;
        public Club(String name, String category, String shortDescription, String longDescription, String activity, String location, String imageUrl) {
            this.name = name; this.category = category; this.shortDescription = shortDescription;
            this.longDescription = longDescription; this.activity = activity; this.location = location;
            this.imageUrl = imageUrl;
            this.icon = (name != null && !name.isEmpty()) ? name.substring(0, 1) : "G";
        }
    }
}
