package com.example.gachonbridge;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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

    private static final String[] CATEGORIES_GLOBAL = {
            "전체",
            "음악",
            "공연",
            "사회·학술",
            "종교",
            "체육",
            "전시·취미"
    };

    private static final String[] CATEGORIES_MEDICAL = {
            "전체",
            "공연",
            "봉사",
            "운동·레저",
            "종교"
    };

    private String[] getCurrentCategories() {
        return selectedCampus == CAMPUS_GLOBAL ? CATEGORIES_GLOBAL : CATEGORIES_MEDICAL;
    }

    private final List<Club> globalClubs = new ArrayList<>();
    private final List<Club> medicalClubs = new ArrayList<>();

    private TextView campusGlobal;
    private TextView campusMedical;
    private LinearLayout categoryContainer;
    private TextView[] categoryTabs;
    private LinearLayout featuredClubCard;
    private TextView featuredBadge;
    private TextView featuredCategory;
    private TextView featuredTitle;
    private TextView featuredBody;
    private LinearLayout clubList;
    private int selectedCampus = CAMPUS_GLOBAL;
    private int selectedCategory = 0;

    private android.os.Handler carouselHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private int currentFeaturedIndex = 0;
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
            loadCampusClubs("clubs_global.json", globalClubs);
            loadCampusClubs("clubs_medical.json", medicalClubs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCampusClubs(String filename, List<Club> targetList) throws Exception {
        InputStream is = getAssets().open(filename);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, StandardCharsets.UTF_8);

        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            String name = obj.optString("name", "");
            String division = obj.optString("division", "");
            String shortDesc = obj.optString("short_description", "");
            String longDesc = obj.optString("long_description", "");
            String activity = obj.optString("activity", "");
            String location = obj.optString("location", "");
            String imageUrl = obj.optString("image_url", "");

            targetList.add(new Club(name, division, shortDesc, longDesc, activity, location, imageUrl, i == 0));
        }
    }

    private void readInitialSelection() {
        selectedCampus = getIntent().getIntExtra(EXTRA_SELECTED_CAMPUS, CAMPUS_GLOBAL);
        String category = getIntent().getStringExtra(EXTRA_SELECTED_CATEGORY);
        selectedCategory = findCategoryIndex(category);
    }

    private int findCategoryIndex(String category) {
        if (category == null) {
            return 0;
        }
        String[] currentCategories = getCurrentCategories();
        for (int i = 0; i < currentCategories.length; i++) {
            if (currentCategories[i].equals(category)) {
                return i;
            }
        }
        return 0;
    }

    private void bindViews() {
        campusGlobal = findViewById(R.id.tabCampusGlobal);
        campusMedical = findViewById(R.id.tabCampusMedical);
        categoryContainer = findViewById(R.id.clubCategoryContainer);
        featuredClubCard = findViewById(R.id.featuredClubCard);
        featuredBadge = findViewById(R.id.featuredClubBadge);
        featuredCategory = findViewById(R.id.featuredClubCategory);
        featuredTitle = findViewById(R.id.featuredClubTitle);
        featuredBody = findViewById(R.id.featuredClubBody);
        clubList = findViewById(R.id.clubList);
    }

    private void bindCampusTabs() {
        campusGlobal.setOnClickListener(v -> {
            selectedCampus = CAMPUS_GLOBAL;
            selectedCategory = 0;
            createCategoryTabs();
            renderClubScreen();
        });
        campusMedical.setOnClickListener(v -> {
            selectedCampus = CAMPUS_MEDICAL;
            selectedCategory = 0;
            createCategoryTabs();
            renderClubScreen();
        });
    }

    private void createCategoryTabs() {
        String[] currentCategories = getCurrentCategories();
        categoryTabs = new TextView[currentCategories.length];
        categoryContainer.removeAllViews();

        for (int i = 0; i < currentCategories.length; i++) {
            final int categoryIndex = i;

            TextView tab = new TextView(this);
            tab.setText(currentCategories[i]);
            tab.setOnClickListener(v -> {
                selectedCategory = categoryIndex;
                renderClubScreen();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(42)
            );
            params.setMarginEnd(dp(14));

            categoryContainer.addView(tab, params);
            categoryTabs[i] = tab;

            applyCategoryTabStyle(tab, categoryIndex == selectedCategory);
        }
    }

    private void renderClubScreen() {
        List<Club> clubListRef = selectedCampus == CAMPUS_GLOBAL ? globalClubs : medicalClubs;
        Club[] clubs = clubListRef.toArray(new Club[0]);
        updateCampusTabs();
        updateCategoryTabs();
        
        // Setup Carousel
        if (carouselRunnable != null) {
            carouselHandler.removeCallbacks(carouselRunnable);
        }
        
        List<Club> visibleClubs = new ArrayList<>();
        for (Club c : clubs) {
            if (isVisible(c)) visibleClubs.add(c);
        }
        
        if (selectedCategory == 0 && !visibleClubs.isEmpty()) {
            featuredClubCard.setVisibility(View.VISIBLE);
            
            // Initial random selection
            Random random = new Random();
            currentFeaturedIndex = random.nextInt(visibleClubs.size());
            updateFeaturedClubUI(visibleClubs.get(currentFeaturedIndex));
            
            carouselRunnable = new Runnable() {
                @Override
                public void run() {
                    if (visibleClubs.size() > 1) {
                        int nextIndex;
                        do {
                            nextIndex = random.nextInt(visibleClubs.size());
                        } while (nextIndex == currentFeaturedIndex); // Prevent showing the same club twice in a row
                        currentFeaturedIndex = nextIndex;
                    }
                    updateFeaturedClubUI(visibleClubs.get(currentFeaturedIndex));
                    carouselHandler.postDelayed(this, 6000); // 6 seconds per rotation
                }
            };
            carouselHandler.postDelayed(carouselRunnable, 6000);
        } else {
            featuredClubCard.setVisibility(View.GONE);
        }
        
        updateClubList(clubs);
    }

    private void updateCampusTabs() {
        updateSegmentTab(campusGlobal, selectedCampus == CAMPUS_GLOBAL);
        updateSegmentTab(campusMedical, selectedCampus == CAMPUS_MEDICAL);
    }

    private void updateSegmentTab(TextView tab, boolean selected) {
        tab.setBackgroundResource(selected ? R.drawable.bg_chip_active : 0);
        tab.setTextColor(ContextCompat.getColor(
                this,
                selected ? R.color.gb_on_primary : R.color.gb_on_surface_variant
        ));
        tab.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void updateCategoryTabs() {
        for (int i = 0; i < categoryTabs.length; i++) {
            applyCategoryTabStyle(categoryTabs[i], i == selectedCategory);
        }
    }

    private void applyCategoryTabStyle(TextView tab, boolean selected) {
        tab.setMinWidth(dp(78));
        tab.setGravity(Gravity.CENTER);
        tab.setTextSize(14);
        tab.setTypeface(null, Typeface.BOLD);
        tab.setSingleLine(true);
        tab.setPadding(dp(20), 0, dp(20), 0);
        tab.setBackgroundResource(selected ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        tab.setTextColor(ContextCompat.getColor(
                this,
                selected ? R.color.gb_on_primary : R.color.gb_on_surface
        ));
    }

    private void updateFeaturedClubUI(Club featured) {
        featuredBadge.setText(featured.badge);
        featuredCategory.setText(featured.category);
        featuredTitle.setText(featured.name);
        featuredBody.setText(featured.shortDescription);
        featuredClubCard.setOnClickListener(v -> openClubDetail(featured));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (carouselHandler != null && carouselRunnable != null) {
            carouselHandler.removeCallbacks(carouselRunnable);
        }
    }

    private void updateFeaturedClub(Club[] clubs) {
        // Obsolete, replaced by carousel logic in renderClubScreen
    }

    private void updateClubList(Club[] clubs) {
        clubList.removeAllViews();
        for (Club club : clubs) {
            if (!isVisible(club)) {
                continue;
            }
            if (selectedCategory == 0 && club.featured) {
                continue;
            }
            clubList.addView(createClubCard(club));
        }
    }

    private Club findFirstVisibleClub(Club[] clubs) {
        if (clubs.length == 0) return null;
        for (Club club : clubs) {
            if (isVisible(club) && club.featured) {
                return club;
            }
        }
        for (Club club : clubs) {
            if (isVisible(club)) {
                return club;
            }
        }
        return clubs[0];
    }

    private boolean isVisible(Club club) {
        if (selectedCategory == 0) return true; // "전체" 탭
        
        String[] currentCategories = getCurrentCategories();
        if (selectedCategory >= currentCategories.length) return true; // 안전장치
        
        String tabName = currentCategories[selectedCategory];
        String clubDiv = club.category;
        
        // UI 탭 이름과 JSON 데이터의 분과명 매칭
        if (tabName.equals("사회·학술") && clubDiv.equals("사회학술")) return true;
        if (tabName.equals("전시·취미") && clubDiv.equals("전시취미")) return true;
        if (tabName.equals("운동·레저") && clubDiv.equals("운동레저")) return true;
        if (tabName.equals("체육") && clubDiv.equals("운동레저")) return true;
        
        return clubDiv.equals(tabName);
    }

    private LinearLayout createClubCard(Club club) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackgroundResource(R.drawable.bg_card);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> openClubDetail(club));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(16));
        card.setLayoutParams(cardParams);

        TextView icon = new TextView(this);
        icon.setText(club.icon);
        icon.setGravity(Gravity.CENTER);
        icon.setTextSize(24);
        icon.setTypeface(null, Typeface.BOLD);
        icon.setTextColor(ContextCompat.getColor(this, R.color.gb_on_primary));
        icon.setBackgroundResource(R.drawable.bg_chip_active);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(86), dp(86));
        iconParams.setMarginEnd(dp(18));
        card.addView(icon, iconParams);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        card.addView(body, bodyParams);

        TextView category = new TextView(this);
        category.setText(club.badge);
        category.setGravity(Gravity.CENTER);
        category.setTextSize(13);
        category.setPadding(dp(12), 0, dp(12), 0);
        category.setTextColor(ContextCompat.getColor(this, R.color.gb_on_surface_variant));
        category.setBackgroundResource(R.drawable.bg_chip_inactive);
        LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(32)
        );
        body.addView(category, categoryParams);

        TextView title = new TextView(this);
        title.setText(club.name);
        title.setTextColor(ContextCompat.getColor(this, R.color.gb_on_surface));
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setSingleLine(true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, dp(8), 0, 0);
        body.addView(title, titleParams);

        TextView description = new TextView(this);
        description.setText(club.shortDescription);
        description.setTextColor(ContextCompat.getColor(this, R.color.gb_on_surface_variant));
        description.setTextSize(15);
        description.setSingleLine(true);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descriptionParams.setMargins(0, dp(6), 0, 0);
        body.addView(description, descriptionParams);

        if (selectedCategory == 0) {
            TextView status = new TextView(this);
            status.setText(club.status);
            status.setTextSize(14);
            status.setTextColor(ContextCompat.getColor(
                    this,
                    "모집 중".equals(club.status) ? R.color.gb_primary : R.color.gb_on_surface_variant
            ));
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            statusParams.setMarginStart(dp(12));
            card.addView(status, statusParams);
        }

        return card;
    }

    private void openClubDetail(Club club) {
        Intent intent = new Intent(this, ClubDetailActivity.class);
        intent.putExtra(ClubDetailActivity.EXTRA_NAME, club.name);
        intent.putExtra(ClubDetailActivity.EXTRA_CATEGORY, club.category);
        intent.putExtra("club_short_description", club.shortDescription);
        intent.putExtra(ClubDetailActivity.EXTRA_DESCRIPTION, club.longDescription);
        intent.putExtra(ClubDetailActivity.EXTRA_BADGE, club.badge);
        intent.putExtra(ClubDetailActivity.EXTRA_ICON, club.icon);
        intent.putExtra("club_activity", club.activity);
        intent.putExtra("club_location", club.location);
        startActivity(intent);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    public static class Club {
        public final String name;
        public final String category;
        public final String shortDescription;
        public final String longDescription;
        public final String activity;
        public final String location;
        public final String imageUrl;
        public final String badge;
        public final String status;
        public final String icon;
        public final boolean featured;

        public Club(String name, String category, String shortDescription, String longDescription, String activity, String location, String imageUrl, boolean featured) {
            this.name = name;
            this.category = category;
            this.shortDescription = shortDescription;
            this.longDescription = longDescription;
            this.activity = activity;
            this.location = location;
            this.imageUrl = imageUrl;
            this.badge = category;
            this.status = "모집 중";
            this.icon = category.isEmpty() ? "G" : category.substring(0, 1);
            this.featured = featured;
        }
    }
}
