package com.example.gachonbridge;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class NoticeActivity extends BaseActivity {

    private TextView filterAll;
    private TextView filterAcademic;
    private TextView filterScholarship;
    private View generalNotice1;
    private View academicNotice1;
    private View scholarshipNotice1;
    private View generalNotice2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        // TODO: Replace dummy notice cards with backend notice list data.
        bindBottomNavigation(R.id.navNotice);

        filterAll = findViewById(R.id.buttonNoticeFilterAll);
        filterAcademic = findViewById(R.id.buttonNoticeFilterAcademic);
        filterScholarship = findViewById(R.id.buttonNoticeFilterScholarship);
        generalNotice1 = findViewById(R.id.cardNoticeDummyGeneral1);
        academicNotice1 = findViewById(R.id.cardNoticeDummyAcademic1);
        scholarshipNotice1 = findViewById(R.id.cardNoticeDummyScholarship1);
        generalNotice2 = findViewById(R.id.cardNoticeDummyGeneral2);

        filterAll.setOnClickListener(v -> showAllNotices());
        filterAcademic.setOnClickListener(v -> showAcademicNotices());
        filterScholarship.setOnClickListener(v -> showScholarshipNotices());

        // TODO: Connect these dummy notice cards to the notice detail website or API-linked page.
        generalNotice1.setOnClickListener(v -> { });
        academicNotice1.setOnClickListener(v -> { });
        scholarshipNotice1.setOnClickListener(v -> { });
        generalNotice2.setOnClickListener(v -> { });
        findViewById(R.id.buttonNoticeMore).setOnClickListener(v -> {
            // TODO: Connect to full academic notice website.
        });

        showAllNotices();
    }

    private void showAllNotices() {
        generalNotice1.setVisibility(View.VISIBLE);
        academicNotice1.setVisibility(View.VISIBLE);
        scholarshipNotice1.setVisibility(View.VISIBLE);
        generalNotice2.setVisibility(View.VISIBLE);
        setActiveFilter(filterAll);
    }

    private void showAcademicNotices() {
        generalNotice1.setVisibility(View.GONE);
        academicNotice1.setVisibility(View.VISIBLE);
        scholarshipNotice1.setVisibility(View.GONE);
        generalNotice2.setVisibility(View.GONE);
        setActiveFilter(filterAcademic);
    }

    private void showScholarshipNotices() {
        generalNotice1.setVisibility(View.GONE);
        academicNotice1.setVisibility(View.GONE);
        scholarshipNotice1.setVisibility(View.VISIBLE);
        generalNotice2.setVisibility(View.GONE);
        setActiveFilter(filterScholarship);
    }

    private void setActiveFilter(TextView activeFilter) {
        setFilterState(filterAll, activeFilter == filterAll);
        setFilterState(filterAcademic, activeFilter == filterAcademic);
        setFilterState(filterScholarship, activeFilter == filterScholarship);
    }

    private void setFilterState(TextView filter, boolean active) {
        filter.setBackgroundResource(active ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filter.setTextColor(ContextCompat.getColor(
                this,
                active ? R.color.gb_on_primary : R.color.gb_on_surface
        ));
    }
}
