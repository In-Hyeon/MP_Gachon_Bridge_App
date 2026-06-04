package com.example.gachonbridge;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class MainActivity extends BaseActivity {

    private TextView[] noticeTabs;
    private TextView[] noticeItems;
    private TextView[] mealTabs;
    private TextView[] mealItems;

    private final String[][] noticePreviewData = {
            {
                    "[\uC77C\uBC18] 2026\uD559\uB144\uB3C4 1\uD559\uAE30 \uC218\uAC15\uC2E0\uCCAD \uBCC0\uACBD \uC548\uB0B4        05.16",
                    "[\uC7A5\uD559] \uAD6D\uAC00\uC7A5\uD559\uAE08 2\uCC28 \uC2E0\uCCAD \uBC0F \uC11C\uB958 \uC81C\uCD9C \uC548\uB0B4        05.13",
                    "[\uD559\uC0AC] \uC878\uC5C5\uC778\uC99D \uC81C\uCD9C \uC77C\uC815 \uBC0F \uC720\uC758\uC0AC\uD56D \uC548\uB0B4        05.10"
            },
            {
                    "[\uD559\uC0AC] 2026\uD559\uB144\uB3C4 \uD558\uACC4 \uACC4\uC808\uD559\uAE30 \uC218\uAC15\uC2E0\uCCAD \uC548\uB0B4        05.21",
                    "[\uD559\uC0AC] \uAE30\uB9D0\uACE0\uC0AC \uC2DC\uAC04\uD45C \uC5F4\uB78C \uBC0F \uAC15\uC758\uC2E4 \uD655\uC778        05.18",
                    "[\uD559\uC0AC] \uBCF5\uC218\uC804\uACF5 \uBC0F \uBD80\uC804\uACF5 \uC2E0\uCCAD \uC77C\uC815 \uC548\uB0B4        05.14"
            },
            {
                    "[\uC7A5\uD559] \uAD50\uB0B4 \uADFC\uB85C\uC7A5\uD559 \uCD94\uAC00 \uBAA8\uC9D1 \uC548\uB0B4        05.22",
                    "[\uC7A5\uD559] \uAC00\uCC9C \uC6B0\uC218\uC7A5\uD559\uAE08 \uC120\uBC1C \uACB0\uACFC \uBC1C\uD45C        05.19",
                    "[\uC7A5\uD559] \uD559\uC790\uAE08 \uC9C0\uC6D0\uAD6C\uAC04 \uD655\uC778 \uBC0F \uC11C\uB958 \uC81C\uCD9C        05.15"
            }
    };

    private final String[][] mealPreviewData = {
            {
                    "\uC870\uC2DD 08:00 - 09:30\n\uC18C\uACE0\uAE30\uBBF8\uC5ED\uAD6D, \uACC4\uB780\uB9D0\uC774, \uBC30\uCD94\uAE40\uCE58",
                    "\uC911\uC2DD 11:30 - 13:30\n\uC81C\uC721\uBCF6\uC74C, \uB41C\uC7A5\uCC0C\uAC1C, \uC0D0\uB7EC\uB4DC, \uC300\uBC25",
                    "\uC11D\uC2DD 17:30 - 19:00\n\uC624\uBBC0\uB77C\uC774\uC2A4, \uD06C\uB9BC\uC2A4\uD504, \uD53C\uD074"
            },
            {
                    "\uC870\uC2DD 08:10 - 09:20\n\uC2DC\uB798\uAE30\uAD6D, \uBCA0\uC774\uCEE8\uAC10\uC790\uBCF6\uC74C, \uAE40\uAD6C\uC774",
                    "\uC911\uC2DD 11:20 - 13:40\n\uB3C8\uAE4C\uC2A4\uCEE4\uB9AC, \uC6B0\uB3D9\uAD6D\uBB3C, \uCF54\uC6B8\uC2AC\uB85C",
                    "\uC11D\uC2DD 17:20 - 18:50\n\uB2ED\uAC08\uBE44\uB36E\uBC25, \uBBF8\uC18C\uC7A5\uAD6D, \uBB34\uC0DD\uCC44"
            },
            {
                    "\uC870\uC2DD 08:00 - 09:30\n\uD1A0\uC2A4\uD2B8, \uC2A4\uD06C\uB7A8\uBE14\uC5D0\uADF8, \uC2DC\uB9AC\uC5BC",
                    "\uC911\uC2DD 11:30 - 13:30\n\uBD88\uACE0\uAE30\uBE44\uBE54\uBC25, \uCF69\uB098\uBB3C\uAD6D, \uC5F0\uB450\uBD80",
                    "\uC11D\uC2DD 17:30 - 19:00\n\uAE40\uCE58\uBCF6\uC74C\uBC25, \uACC4\uB780\uD6C4\uB77C\uC774, \uC5B4\uBB35\uAD6D"
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TODO: Replace XML dummy sections with API-backed views or RecyclerViews.
        bindBottomNavigation(R.id.navHome);
        bindHomePreviewTabs();
        bindNoticeLinks();
        bindHomeWindLinks();
        bindClubCategoryLinks();
    }

    private void bindHomePreviewTabs() {
        noticeTabs = new TextView[]{
                findViewById(R.id.tabNoticeAll),
                findViewById(R.id.tabNoticeAcademic),
                findViewById(R.id.tabNoticeScholarship)
        };
        noticeItems = new TextView[]{
                findViewById(R.id.noticePreview1),
                findViewById(R.id.noticePreview2),
                findViewById(R.id.noticePreview3)
        };
        mealTabs = new TextView[]{
                findViewById(R.id.tabMealGraduate),
                findViewById(R.id.tabMealVision),
                findViewById(R.id.tabMealStudent)
        };
        mealItems = new TextView[]{
                findViewById(R.id.mealPreview1),
                findViewById(R.id.mealPreview2),
                findViewById(R.id.mealPreview3)
        };

        bindTabGroup(noticeTabs, noticeItems, noticePreviewData);
        bindTabGroup(mealTabs, mealItems, mealPreviewData);
        updatePreviewGroup(noticeTabs, noticeItems, noticePreviewData, 0);
        updatePreviewGroup(mealTabs, mealItems, mealPreviewData, 0);
    }

    private void bindTabGroup(TextView[] tabs, TextView[] items, String[][] data) {
        for (int i = 0; i < tabs.length; i++) {
            final int selectedIndex = i;
            tabs[i].setOnClickListener(v -> updatePreviewGroup(tabs, items, data, selectedIndex));
        }
    }

    private void updatePreviewGroup(TextView[] tabs, TextView[] items, String[][] data, int selectedIndex) {
        for (int i = 0; i < tabs.length; i++) {
            boolean selected = i == selectedIndex;
            tabs[i].setTextColor(ContextCompat.getColor(
                    this,
                    selected ? R.color.gb_primary : R.color.gb_on_surface_variant
            ));
            tabs[i].setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        }

        for (int i = 0; i < items.length; i++) {
            items[i].setText(data[selectedIndex][i]);
        }
    }

    private void bindClubCategoryLinks() {
        bindClubCategoryLink(R.id.clubCategoryMusic, "\uC74C\uC545");
        bindClubCategoryLink(R.id.clubCategoryShow, "\uACF5\uC5F0");
        bindClubCategoryLink(R.id.clubCategoryStudy, "\uC0AC\uD68C\u00B7\uD559\uC220");
        bindClubCategoryLink(R.id.clubCategorySports, "\uCCB4\uC721");
        bindClubCategoryLink(R.id.clubCategoryService, "\uBD09\uC0AC");
        bindClubCategoryLink(R.id.clubCategoryReligion, "\uC885\uAD50");
    }

    private void bindClubCategoryLink(int viewId, String category) {
        TextView categoryView = findViewById(viewId);
        categoryView.setOnClickListener(v -> openClubCategory(category));
    }

    private void openClubCategory(String category) {
        Intent intent = new Intent(this, ClubActivity.class);
        intent.putExtra(ClubActivity.EXTRA_SELECTED_CAMPUS, ClubActivity.CAMPUS_GLOBAL);
        intent.putExtra(ClubActivity.EXTRA_SELECTED_CATEGORY, category);
        startActivity(intent);
    }

    private void bindHomeWindLinks() {
        bindHomeWindLink(R.id.cardWindDummyProgram1);
        bindHomeWindLink(R.id.cardWindDummyProgram2);
        bindHomeWindLink(R.id.cardWindDummyProgram3);
    }

    private void bindHomeWindLink(int viewId) {
        View view = findViewById(viewId);
        view.setOnClickListener(v -> openWindPage());
    }

    private void openWindPage() {
        Intent intent = new Intent(this, WindActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void bindNoticeLinks() {
        bindNoticeLink(R.id.noticePreview1);
        bindNoticeLink(R.id.noticePreview2);
        bindNoticeLink(R.id.noticePreview3);
    }

    private void bindNoticeLink(int viewId) {
        TextView noticeView = findViewById(viewId);
        noticeView.setOnClickListener(v -> openNoticePage());
    }

    private void openNoticePage() {
        startActivity(new Intent(this, NoticeActivity.class));
    }
}
