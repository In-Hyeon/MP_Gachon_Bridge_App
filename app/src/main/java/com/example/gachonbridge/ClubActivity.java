package com.example.gachonbridge;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class ClubActivity extends BaseActivity {

    public static final String EXTRA_SELECTED_CAMPUS = "selected_campus";
    public static final String EXTRA_SELECTED_CATEGORY = "selected_category";
    public static final int CAMPUS_GLOBAL = 0;
    public static final int CAMPUS_MEDICAL = 1;

    private static final String[] CATEGORIES = {
            "\uC804\uCCB4",
            "\uC74C\uC545",
            "\uACF5\uC5F0",
            "\uC0AC\uD68C\u00B7\uD559\uC220",
            "\uC885\uAD50",
            "\uCCB4\uC721",
            "\uBD09\uC0AC",
            "\uC804\uC2DC\u00B7\uCDE8\uBBF8"
    };

    private final Club[] globalClubs = {
            new Club("\uD558\uB2AC\uBC14\uB78C", "\uC74C\uC545", "1985\uB144\uBD80\uD130 \uD604\uC7AC\uAE4C\uC9C0 \uD3ED \uB113\uC740 \uC7A5\uB974\uC758 \uC74C\uC545\uC744 \uD568\uAED8 \uC5F0\uC8FC\uD569\uB2C8\uB2E4.", "\uC74C\uC545", "\uBAA8\uC9D1 \uC911", "\u266A", true),
            new Club("\uD30C\uB791\uC0C8", "\uC74C\uC545", "\uB2E4\uC591\uD55C \uC7A5\uB974\uC758 \uC74C\uC545\uC73C\uB85C \uAD00\uAC1D\uACFC \uC18C\uD1B5\uD558\uB294 \uBC34\uB4DC \uB3D9\uC544\uB9AC\uC785\uB2C8\uB2E4.", "\uC74C\uC545", "\uBAA8\uC9D1 \uC911", "\u266B", false),
            new Club("\uD604\uC74C", "\uC74C\uC545", "\uD604\uC74C\uC740 \uB2E4\uC591\uD55C \uC7A5\uB974\uC758 \uC74C\uC545\uC744 \uC5F0\uC8FC\uD558\uBA70 \uD569\uC8FC\uD558\uB294 \uB3D9\uC544\uB9AC\uC785\uB2C8\uB2E4.", "\uC74C\uC545", "\uB9C8\uAC10", "\u25A3", false),
            new Club("\uACE0\uC6B4\uC18C\uB9AC", "\uC74C\uC545", "\uC5B4\uCFE0\uC2A4\uD2F1 \uBD84\uC704\uAE30\uB97C \uAE30\uBC18\uC73C\uB85C \uD654\uC74C\uC744 \uB9CC\uB4E4\uACE0 \uACF5\uC5F0\uD569\uB2C8\uB2E4.", "\uC74C\uC545", "\uBAA8\uC9D1 \uC911", "\u266B", false),
            new Club("\uCC9C\uD558\uB300\uC7A5\uAD70", "\uC74C\uC545", "1985\uB144\uBD80\uD130 \uC774\uC5B4\uC838 \uC628 \uC911\uC559 \uB77D\uBC34\uB4DC \uB3D9\uC544\uB9AC\uB85C \uD569\uC8FC\uC640 \uACF5\uC5F0\uC744 \uD569\uB2C8\uB2E4.", "\uC74C\uC545", "\uBAA8\uC9D1 \uC911", "\u266A", false),
            new Club("\uCF54\uB4DC\uD06C\uB798\uD504\uD2B8 \uAC1C\uBC1C", "\uC0AC\uD68C\u00B7\uD559\uC220", "\uC18C\uD504\uD2B8\uC6E8\uC5B4 \uC5D4\uC9C0\uB2C8\uC5B4\uB9C1 \uC6CC\uD06C\uC0F5\uACFC \uD504\uB85C\uC81D\uD2B8\uB97C \uD568\uAED8 \uC9C4\uD589\uD569\uB2C8\uB2E4.", "\uD559\uC220", "\uBAA8\uC9D1 \uC911", "</>", false),
            new Club("\uAC00\uCC9C \uC774\uC2A4\uD3EC\uCE20", "\uCCB4\uC721", "\uACBD\uC7C1\uC801\uC778 \uAC8C\uC784 \uB9AC\uADF8\uC640 \uCE90\uC8FC\uC5BC \uD1A0\uB108\uBA3C\uD2B8\uB97C \uC6B4\uC601\uD569\uB2C8\uB2E4.", "\uC0AC\uD68C", "\uB9C8\uAC10", "\u25B6", false),
            new Club("\uAC00\uCC9C \uB18D\uAD6C\uBD80", "\uCCB4\uC721", "\uB9E4\uC8FC \uC815\uAE30 \uC6B4\uB3D9\uACFC \uD0C0 \uB300\uD559 \uAD50\uB958\uC804\uC744 \uC900\uBE44\uD569\uB2C8\uB2E4.", "\uCCB4\uC721", "\uBAA8\uC9D1 \uC911", "\u25CE", false),
            new Club("\uC2A4\uD29C\uB514\uC624 G", "\uC804\uC2DC\u00B7\uCDE8\uBBF8", "\uB2E4\uC591\uD55C \uBBF8\uC220 \uB9E4\uCCB4\uB85C \uCC3D\uC791 \uD65C\uB3D9\uACFC \uC804\uC2DC\uB97C \uAE30\uD68D\uD569\uB2C8\uB2E4.", "\uC804\uC2DC\u00B7\uCDE8\uBBF8", "\uB9C8\uAC10", "\u25CC", false),
            new Club("\uADF8\uB8E8\uBE0C \uC564 \uB304\uC2A4", "\uACF5\uC5F0", "\uC2A4\uD2B8\uB9BF \uB304\uC2A4\uC640 \uCF54\uB808\uC624\uADF8\uB798\uD53C \uC6CC\uD06C\uC0F5\uC744 \uC9C4\uD589\uD569\uB2C8\uB2E4.", "\uACF5\uC5F0", "\uBAA8\uC9D1 \uC911", "\u25E2", false),
            new Club("\uAC00\uCC9C \uB098\uB214\uD68C", "\uBD09\uC0AC", "\uC9C0\uC5ED \uC544\uB3D9 \uBA58\uD1A0\uB9C1\uACFC \uCEA0\uD37C\uC2A4 \uD658\uACBD \uCEA0\uD398\uC778\uC744 \uD568\uAED8 \uC900\uBE44\uD569\uB2C8\uB2E4.", "\uBD09\uC0AC", "\uBAA8\uC9D1 \uC911", "\u2665", false),
            new Club("\uBBFF\uC74C\uC758 \uAE38", "\uC885\uAD50", "\uC608\uBC30, \uBD09\uC0AC, \uC18C\uADF8\uB8F9 \uBAA8\uC784\uC73C\uB85C \uCEA0\uD37C\uC2A4 \uCEE4\uBBA4\uB2C8\uD2F0\uB97C \uB9CC\uB4ED\uB2C8\uB2E4.", "\uC885\uAD50", "\uBAA8\uC9D1 \uC911", "+", false)
    };

    private final Club[] medicalClubs = {
            new Club("\uBA54\uB514\uCEEC \uCF54\uB7EC\uC2A4", "\uC74C\uC545", "\uD559\uC5C5 \uC774\uD6C4 \uD569\uCC3D\uACFC \uC18C\uADDC\uBAA8 \uACF5\uC5F0\uC73C\uB85C \uC7AC\uCDA9\uC804\uD558\uB294 \uB3D9\uC544\uB9AC\uC785\uB2C8\uB2E4.", "\uCD94\uCC9C", "\uBAA8\uC9D1 \uC911", "\u266B", true),
            new Club("\uD5EC\uC2A4\uCF00\uC5B4 \uB9AC\uC11C\uCE58", "\uC0AC\uD68C\u00B7\uD559\uC220", "\uC758\uB8CC \uB370\uC774\uD130\uC640 \uACF5\uACF5\uBCF4\uAC74 \uC8FC\uC81C\uB97C \uD568\uAED8 \uC5F0\uAD6C\uD569\uB2C8\uB2E4.", "\uD559\uC220", "\uBAA8\uC9D1 \uC911", "H", false),
            new Club("\uC751\uAE09\uAD6C\uC870 \uC2A4\uD130\uB514", "\uC0AC\uD68C\u00B7\uD559\uC220", "\uC751\uAE09\uCC98\uCE58 \uC2E4\uC2B5\uACFC \uC9C0\uC5ED \uC548\uC804 \uCEA0\uD398\uC778\uC744 \uC900\uBE44\uD569\uB2C8\uB2E4.", "\uD559\uC220", "\uB9C8\uAC10", "ER", false),
            new Club("\uBA54\uB514 \uB7EC\uB2DD \uB7F0", "\uCCB4\uC721", "\uB7EC\uB2DD\uACFC \uD53C\uD2B8\uB2C8\uC2A4 \uD65C\uB3D9\uC73C\uB85C \uCCB4\uB825\uC744 \uAD00\uB9AC\uD569\uB2C8\uB2E4.", "\uCCB4\uC721", "\uBAA8\uC9D1 \uC911", "RUN", false),
            new Club("\uC544\uD2B8 \uD14C\uB77C\uD53C \uD074\uB7FD", "\uC804\uC2DC\u00B7\uCDE8\uBBF8", "\uBBF8\uC220 \uD65C\uB3D9\uACFC \uD790\uB9C1 \uD504\uB85C\uADF8\uB7A8\uC744 \uC9C0\uC5ED\uACFC \uC5F0\uACB0\uD569\uB2C8\uB2E4.", "\uC804\uC2DC\u00B7\uCDE8\uBBF8", "\uBAA8\uC9D1 \uC911", "\u25CC", false),
            new Club("\uBA54\uB514\uCEEC \uD50C\uB808\uC774", "\uACF5\uC5F0", "\uC758\uD559 \uCEA0\uD37C\uC2A4 \uCD95\uC81C\uC640 \uC18C\uADDC\uBAA8 \uBB34\uB300\uB97C \uD568\uAED8 \uB9CC\uB4ED\uB2C8\uB2E4.", "\uACF5\uC5F0", "\uB9C8\uAC10", "\u25B2", false),
            new Club("\uB9C8\uC74C \uC5F0\uACB0", "\uBD09\uC0AC", "\uBCD1\uC6D0 \uC548\uB0B4 \uBD09\uC0AC\uC640 \uC9C0\uC5ED \uAC74\uAC15 \uCEA0\uD398\uC778\uC744 \uD568\uAED8 \uC9C4\uD589\uD569\uB2C8\uB2E4.", "\uBD09\uC0AC", "\uBAA8\uC9D1 \uC911", "\u2665", false),
            new Club("\uB098\uB214 \uAE30\uB3C4\uD68C", "\uC885\uAD50", "\uC815\uAE30 \uBAA8\uC784\uACFC \uC9C0\uC5ED \uBD09\uC0AC\uB97C \uC911\uC2EC\uC73C\uB85C \uD65C\uB3D9\uD569\uB2C8\uB2E4.", "\uC885\uAD50", "\uBAA8\uC9D1 \uC911", "+", false)
    };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club);
        bindBottomNavigation(R.id.navClub);
        readInitialSelection();
        bindViews();
        bindCampusTabs();
        createCategoryTabs();
        renderClubScreen();
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
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equals(category)) {
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
            renderClubScreen();
        });
        campusMedical.setOnClickListener(v -> {
            selectedCampus = CAMPUS_MEDICAL;
            selectedCategory = 0;
            renderClubScreen();
        });
    }

    private void createCategoryTabs() {
        categoryTabs = new TextView[CATEGORIES.length];
        categoryContainer.removeAllViews();
        for (int i = 0; i < CATEGORIES.length; i++) {
            final int categoryIndex = i;
            TextView tab = new TextView(this);
            tab.setText(CATEGORIES[i]);
            tab.setGravity(Gravity.CENTER);
            tab.setTextSize(15);
            tab.setTypeface(null, Typeface.BOLD);
            tab.setSingleLine(true);
            tab.setPadding(dp(18), 0, dp(18), 0);
            tab.setOnClickListener(v -> {
                selectedCategory = categoryIndex;
                renderClubScreen();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(54)
            );
            params.setMarginEnd(dp(12));
            categoryContainer.addView(tab, params);
            categoryTabs[i] = tab;
        }
    }

    private void renderClubScreen() {
        Club[] clubs = selectedCampus == CAMPUS_GLOBAL ? globalClubs : medicalClubs;
        updateCampusTabs();
        updateCategoryTabs();
        updateFeaturedClub(clubs);
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
            boolean selected = i == selectedCategory;
            categoryTabs[i].setBackgroundResource(selected ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
            categoryTabs[i].setTextColor(ContextCompat.getColor(
                    this,
                    selected ? R.color.gb_on_primary : R.color.gb_on_surface
            ));
        }
    }

    private void updateFeaturedClub(Club[] clubs) {
        if (selectedCategory != 0) {
            featuredClubCard.setVisibility(View.GONE);
            return;
        }

        featuredClubCard.setVisibility(View.VISIBLE);
        Club featured = findFirstVisibleClub(clubs);
        featuredBadge.setText(featured.badge);
        featuredCategory.setText(featured.category);
        featuredTitle.setText(featured.name);
        featuredBody.setText(featured.description);
        featuredClubCard.setOnClickListener(v -> openClubDetail(featured));
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
        return selectedCategory == 0 || club.category.equals(CATEGORIES[selectedCategory]);
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
        description.setText(club.description);
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
                    "\uBAA8\uC9D1 \uC911".equals(club.status) ? R.color.gb_primary : R.color.gb_on_surface_variant
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
        intent.putExtra(ClubDetailActivity.EXTRA_DESCRIPTION, club.description);
        intent.putExtra(ClubDetailActivity.EXTRA_BADGE, club.badge);
        intent.putExtra(ClubDetailActivity.EXTRA_ICON, club.icon);
        startActivity(intent);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class Club {
        final String name;
        final String category;
        final String description;
        final String badge;
        final String status;
        final String icon;
        final boolean featured;

        Club(String name, String category, String description, String badge, String status, String icon, boolean featured) {
            this.name = name;
            this.category = category;
            this.description = description;
            this.badge = badge;
            this.status = status;
            this.icon = icon;
            this.featured = featured;
        }
    }
}
