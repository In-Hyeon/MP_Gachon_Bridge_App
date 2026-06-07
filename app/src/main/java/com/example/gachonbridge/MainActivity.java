package com.example.gachonbridge;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity {

    private TextView[] noticeTabs;
    private TextView[] noticeItems;
    private TextView[] mealTabs;
    private TextView[] mealItems;
    private TextView[] scheduleDates;
    private TextView[] scheduleTitles;
    private View[] homeWindIndicators;
    private PagerSnapHelper homeWindSnapHelper;
    private TextView[] scheduleDdays;
    private RecyclerView homeWindRecyclerView;
    private HomeWindAdapter homeWindAdapter;
    private final ArrayList<GachonWindCrawler.WindProgram> homeWindPrograms = new ArrayList<>();
    private int selectedMealIndex;
    private static final int HOME_NOTICE_PREVIEW_COUNT = 5;
    private static final int HOME_WIND_PREVIEW_COUNT = 3;
    private static final String[] MEAL_RESTAURANT_URLS = {
            "https://www.gachon.ac.kr/kor/7349/subview.do",
            "https://www.gachon.ac.kr/kor/7347/subview.do",
            "https://www.gachon.ac.kr/kor/7350/subview.do"
    };
    private final ExecutorService contentExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable mealHighlightUpdater = new Runnable() {
        @Override
        public void run() {
            applyMealTimeHighlight();
            mainHandler.postDelayed(this, getMealHighlightRefreshDelayMillis());
        }
    };

    private String[][] noticePreviewData = new String[3][HOME_NOTICE_PREVIEW_COUNT];

    private String[][] mealPreviewData = {
            {
                    "\uC544\uCE68(\uCC9C\uC6D0\uC758\uC544\uCE68\uBC25)\n\uBD88\uB7EC\uC624\uB294 \uC911...",
                    "\uC810\uC2EC\n\uBD88\uB7EC\uC624\uB294 \uC911...",
                    "\uC800\uB141\n\uBD88\uB7EC\uC624\uB294 \uC911..."
            },
            {
                    "\uC544\uCE68(\uCC9C\uC6D0\uC758\uC544\uCE68\uBC25)\n\uBD88\uB7EC\uC624\uB294 \uC911...",
                    "\uC810\uC2EC\n\uBD88\uB7EC\uC624\uB294 \uC911...",
                    "\uC800\uB141\n\uBD88\uB7EC\uC624\uB294 \uC911..."
            },
            {
                    "\uC544\uCE68(\uCC9C\uC6D0\uC758\uC544\uCE68\uBC25)\n\uBD88\uB7EC\uC624\uB294 \uC911...",
                    "\uC810\uC2EC\n\uBD88\uB7EC\uC624\uB294 \uC911...",
                    "\uC800\uB141\n\uBD88\uB7EC\uC624\uB294 \uC911..."
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < HOME_NOTICE_PREVIEW_COUNT; j++) {
                noticePreviewData[i][j] = "\uBD88\uB7EC\uC624\uB294 \uC911...";
            }
        }

        bindBottomNavigation(R.id.navHome);
        bindSchedulePreviewCards();
        bindHomePreviewTabs();
        startMealHighlightUpdates();
        bindNoticeLinks();
        bindHomeWindCarousel();
        bindMapLink();
        bindClubCategoryLinks();
        loadAcademicSchedules();
        loadTodayMeals();
        loadHomeWindTopPrograms();
        loadHomeNotices();
    }

    private void bindHomeWindCarousel() {
        homeWindRecyclerView = findViewById(R.id.homeWindRecyclerView);
        if (homeWindRecyclerView == null) return;

        homeWindIndicators = new View[]{
                findViewById(R.id.homeWindIndicator1),
                findViewById(R.id.homeWindIndicator2),
                findViewById(R.id.homeWindIndicator3)
        };

        homeWindPrograms.clear();
        for (int i = 0; i < HOME_WIND_PREVIEW_COUNT; i++) {
            homeWindPrograms.add(null);
        }

        homeWindAdapter = new HomeWindAdapter(homeWindPrograms);
        homeWindRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        homeWindRecyclerView.setAdapter(homeWindAdapter);

        homeWindSnapHelper = new PagerSnapHelper();
        homeWindSnapHelper.attachToRecyclerView(homeWindRecyclerView);

        homeWindRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateHomeWindIndicator();
                }
            }
        });

        updateHomeWindIndicator();
    }
    private void updateHomeWindIndicator() {
        if (homeWindRecyclerView == null || homeWindIndicators == null) return;

        RecyclerView.LayoutManager layoutManager = homeWindRecyclerView.getLayoutManager();
        View snapView = homeWindSnapHelper.findSnapView(layoutManager);
        int position = snapView == null ? 0 : homeWindRecyclerView.getChildAdapterPosition(snapView);

        for (int i = 0; i < homeWindIndicators.length; i++) {
            homeWindIndicators[i].setBackground(makeIndicatorDrawable(i == position));
        }
    }

    private GradientDrawable makeIndicatorDrawable(boolean active) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(ContextCompat.getColor(
                this,
                active ? R.color.gb_primary : R.color.gb_outline_variant
        ));
        return drawable;
    }

    private void bindSchedulePreviewCards() {
        scheduleDates = new TextView[]{
                findViewById(R.id.scheduleDate1),
                findViewById(R.id.scheduleDate2),
                findViewById(R.id.scheduleDate3)
        };
        scheduleTitles = new TextView[]{
                findViewById(R.id.scheduleTitle1),
                findViewById(R.id.scheduleTitle2),
                findViewById(R.id.scheduleTitle3)
        };
        scheduleDdays = new TextView[]{
                findViewById(R.id.scheduleDday1),
                findViewById(R.id.scheduleDday2),
                findViewById(R.id.scheduleDday3)
        };

        List<GachonAcademicScheduleCrawler.DailySchedule> loadingSchedules =
                new GachonAcademicScheduleCrawler().buildLoadingSchedules(new Date(), 3);
        updateScheduleCards(loadingSchedules);
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
                findViewById(R.id.noticePreview3),
                findViewById(R.id.noticePreview4),
                findViewById(R.id.noticePreview5)
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
        bindMealTabs();
        bindMealLinks();
        updatePreviewGroup(noticeTabs, noticeItems, noticePreviewData, 0);
        updateMealPreviewGroup(0);
    }

    private void bindMealTabs() {
        for (int i = 0; i < mealTabs.length; i++) {
            final int selectedIndex = i;
            mealTabs[i].setOnClickListener(v -> {
                selectedMealIndex = selectedIndex;
                updateMealPreviewGroup(selectedMealIndex);
            });
        }
    }

    private void bindMealLinks() {
        for (TextView mealItem : mealItems) {
            mealItem.setOnClickListener(v -> openSelectedMealRestaurant());
        }
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

    private void updateMealPreviewGroup(int selectedIndex) {
        updatePreviewGroup(mealTabs, mealItems, mealPreviewData, selectedIndex);
        applyMealTimeHighlight();
    }

    private void startMealHighlightUpdates() {
        mainHandler.removeCallbacks(mealHighlightUpdater);
        mainHandler.post(mealHighlightUpdater);
    }

    private void applyMealTimeHighlight() {
        if (mealItems == null) return;
        int highlightedIndex = getCurrentMealIndex();
        for (int i = 0; i < mealItems.length; i++) {
            boolean highlighted = i == highlightedIndex;
            mealItems[i].setTextColor(ContextCompat.getColor(
                    this,
                    highlighted ? R.color.gb_on_surface : R.color.gb_on_surface_variant
            ));
            mealItems[i].setTypeface(null, highlighted ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private int getCurrentMealIndex() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 10) return 0;
        if (hour < 14) return 1;
        return 2;
    }

    private long getMealHighlightRefreshDelayMillis() {
        Calendar now = Calendar.getInstance();
        Calendar nextBoundary = (Calendar) now.clone();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) {
            nextBoundary.set(Calendar.HOUR_OF_DAY, 10);
        } else if (hour < 14) {
            nextBoundary.set(Calendar.HOUR_OF_DAY, 14);
        } else {
            nextBoundary.add(Calendar.DAY_OF_MONTH, 1);
            nextBoundary.set(Calendar.HOUR_OF_DAY, 0);
        }
        nextBoundary.set(Calendar.MINUTE, 0);
        nextBoundary.set(Calendar.SECOND, 0);
        nextBoundary.set(Calendar.MILLISECOND, 0);
        return Math.max(1000L, nextBoundary.getTimeInMillis() - now.getTimeInMillis() + 1000L);
    }

    private void loadTodayMeals() {
        contentExecutor.execute(() -> {
            String[][] nextMealData;
            try {
                List<GachonMealCrawler.RestaurantMenu> menus =
                        new GachonMealCrawler().fetchTodayMenus(new Date());
                nextMealData = buildMealPreviewData(menus);
            } catch (Exception e) {
                nextMealData = buildEmptyMealPreviewData();
            }
            String[][] finalNextMealData = nextMealData;
            mainHandler.post(() -> {
                mealPreviewData = finalNextMealData;
                updateMealPreviewGroup(selectedMealIndex);
            });
        });
    }

    private void loadAcademicSchedules() {
        contentExecutor.execute(() -> {
            List<GachonAcademicScheduleCrawler.DailySchedule> nextSchedules;
            try {
                nextSchedules = new GachonAcademicScheduleCrawler().fetchSchedulesFrom(new Date(), 3);
            } catch (Exception e) {
                nextSchedules = new GachonAcademicScheduleCrawler().buildEmptySchedules(new Date(), 3);
            }
            List<GachonAcademicScheduleCrawler.DailySchedule> finalNextSchedules = nextSchedules;
            mainHandler.post(() -> updateScheduleCards(finalNextSchedules));
        });
    }

    private void updateScheduleCards(List<GachonAcademicScheduleCrawler.DailySchedule> schedules) {
        int count = Math.min(scheduleDates.length, schedules.size());
        for (int i = 0; i < count; i++) {
            GachonAcademicScheduleCrawler.DailySchedule schedule = schedules.get(i);
            scheduleDates[i].setText(schedule.dateLabel);
            scheduleTitles[i].setText(schedule.title);
            scheduleDdays[i].setText(schedule.ddayLabel);
        }
    }

    private String[][] buildMealPreviewData(List<GachonMealCrawler.RestaurantMenu> menus) {
        String[][] data = buildEmptyMealPreviewData();
        int count = Math.min(data.length, menus.size());
        for (int i = 0; i < count; i++) {
            GachonMealCrawler.RestaurantMenu menu = menus.get(i);
            data[i][0] = formatMealItem("\uC544\uCE68(\uCC9C\uC6D0\uC758\uC544\uCE68\uBC25)", menu.breakfast);
            data[i][1] = formatMealItem("\uC810\uC2EC", menu.lunch);
            data[i][2] = formatMealItem("\uC800\uB141", menu.dinner);
        }
        return data;
    }

    private String[][] buildEmptyMealPreviewData() {
        return new String[][]{
                {
                        formatMealItem("\uC544\uCE68(\uCC9C\uC6D0\uC758\uC544\uCE68\uBC25)", "-"),
                        formatMealItem("\uC810\uC2EC", "-"),
                        formatMealItem("\uC800\uB141", "-")
                },
                {
                        formatMealItem("\uC544\uCE68(\uCC9C\uC6D0\uC758\uC544\uCE68\uBC25)", "-"),
                        formatMealItem("\uC810\uC2EC", "-"),
                        formatMealItem("\uC800\uB141", "-")
                },
                {
                        formatMealItem("\uC544\uCE68(\uCC9C\uC6D0\uC758\uC544\uCE68\uBC25)", "-"),
                        formatMealItem("\uC810\uC2EC", "-"),
                        formatMealItem("\uC800\uB141", "-")
                }
        };
    }

    private String formatMealItem(String label, String menu) {
        return label + "\n " + formatMealPreviewMenu(menu);
    }

    private String formatMealPreviewMenu(String menu) {
        if (menu == null || menu.trim().isEmpty()) return "-";
        String normalized = menu
                .replace('\u00A0', ' ')
                .replace("\r", "\n")
                .replaceAll("\\s*\\n+\\s*", ", ")
                .replaceAll("\\s*,\\s*", ", ")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized.isEmpty() ? "-" : normalized;
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

    private void loadHomeWindTopPrograms() {
        contentExecutor.execute(() -> {
            try {
                List<GachonWindCrawler.WindProgram> topPrograms =
                        new GachonWindCrawler().fetchTopPrograms(HOME_WIND_PREVIEW_COUNT);
                mainHandler.post(() -> updateHomeWindCards(topPrograms));
            } catch (Exception e) {
                mainHandler.post(this::showEmptyHomeWindCards);
            }
        });
    }

    private void updateHomeWindCards(List<GachonWindCrawler.WindProgram> programs) {
        homeWindPrograms.clear();
        int count = Math.min(HOME_WIND_PREVIEW_COUNT, programs.size());
        for (int i = 0; i < count; i++) {
            homeWindPrograms.add(programs.get(i));
        }
        for (int i = count; i < HOME_WIND_PREVIEW_COUNT; i++) {
            homeWindPrograms.add(null);
        }
        if (homeWindAdapter != null) {
            homeWindAdapter.notifyDataSetChanged();
        }
    }

    private void showEmptyHomeWindCards() {
        homeWindPrograms.clear();
        for (int i = 0; i < HOME_WIND_PREVIEW_COUNT; i++) {
            homeWindPrograms.add(null);
        }
        if (homeWindAdapter != null) {
            homeWindAdapter.notifyDataSetChanged();
        }
    }

    private String formatWindHomePeriod(GachonWindCrawler.WindProgram program) {
        return "\uC2E0\uCCAD  " + orDash(program.applyPeriod) + "\n\uC6B4\uC601  " + orDash(program.period);
    }

    private String orDash(String text) {
        return text == null || text.trim().isEmpty() ? "-" : text.trim();
    }

    private void bindMapLink() {
        View mapSection = findViewById(R.id.sectionMap);
        mapSection.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
    }

    private void openHomeWindDetail(String detailUrl) {
        if (detailUrl == null || detailUrl.trim().isEmpty()) {
            openWindPage();
            return;
        }

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(detailUrl)));
        } catch (Exception e) {
            openWindPage();
        }
    }

    private void openWindPage() {
        Intent intent = new Intent(this, WindActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void bindNoticeLinks() {
        int[] ids = {
                R.id.noticePreview1, R.id.noticePreview2, R.id.noticePreview3,
                R.id.noticePreview4, R.id.noticePreview5
        };
        for (int id : ids) {
            TextView v = findViewById(id);
            if (v != null) v.setOnClickListener(view -> openNoticePage());
        }
    }

    private void bindNoticeLink(int viewId) {
        TextView noticeView = findViewById(viewId);
        noticeView.setOnClickListener(v -> openNoticePage());
    }

    private void openNoticePage() {
        startActivity(new Intent(this, NoticeActivity.class));
    }

    private void loadHomeNotices() {
        contentExecutor.execute(() -> {
            try {
                List<Notice> all = GachonScraper.fetchNotices(GachonScraper.Category.ALL, HOME_NOTICE_PREVIEW_COUNT);
                List<Notice> academic = GachonScraper.fetchNotices(GachonScraper.Category.ACADEMIC, HOME_NOTICE_PREVIEW_COUNT);
                List<Notice> scholarship = GachonScraper.fetchNotices(GachonScraper.Category.SCHOLARSHIP, HOME_NOTICE_PREVIEW_COUNT);
                mainHandler.post(() -> {
                    fillNoticeData(0, all);
                    fillNoticeData(1, academic);
                    fillNoticeData(2, scholarship);
                    updatePreviewGroup(noticeTabs, noticeItems, noticePreviewData, 0);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void fillNoticeData(int catIndex, List<Notice> notices) {
        for (int i = 0; i < HOME_NOTICE_PREVIEW_COUNT; i++) {
            if (i < notices.size()) {
                noticePreviewData[catIndex][i] = notices.get(i).getTitle();
            } else {
                noticePreviewData[catIndex][i] = "-";
            }
        }
    }

    private void openSelectedMealRestaurant() {
        if (selectedMealIndex < 0 || selectedMealIndex >= MEAL_RESTAURANT_URLS.length) return;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MEAL_RESTAURANT_URLS[selectedMealIndex])));
    }

    private class HomeWindAdapter extends RecyclerView.Adapter<HomeWindAdapter.HomeWindViewHolder> {
        private final List<GachonWindCrawler.WindProgram> programs;

        HomeWindAdapter(List<GachonWindCrawler.WindProgram> programs) {
            this.programs = programs;
        }

        @NonNull
        @Override
        public HomeWindViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_home_wind_card, parent, false);
            view.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            return new HomeWindViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeWindViewHolder holder, int position) {
            GachonWindCrawler.WindProgram program = programs.get(position);
            holder.rank.setText("TOP\n" + (position + 1));

            if (program == null) {
                holder.image.setImageDrawable(null);
                holder.image.setVisibility(View.GONE);
                holder.hits.setText("\uBD88\uB7EC\uC624\uB294 \uC911...");
                holder.institution.setText("WIND");
                holder.title.setText("\uD504\uB85C\uADF8\uB7A8 \uBD88\uB7EC\uC624\uB294 \uC911...");
                holder.period.setText("-");
                holder.itemView.setOnClickListener(v -> openWindPage());
                return;
            }

            holder.hits.setText(NumberFormat.getNumberInstance(Locale.KOREA).format(program.hits) + " HITS");
            holder.institution.setText(orDash(program.institution));
            holder.title.setText(orDash(program.title));
            holder.period.setText(formatWindHomePeriod(program));

            if (program.coverUrl != null && !program.coverUrl.isEmpty()) {
                holder.image.setVisibility(View.VISIBLE);
                Glide.with(MainActivity.this)
                        .load(program.coverUrl)
                        .centerCrop()
                        .into(holder.image);
            } else {
                holder.image.setImageDrawable(null);
                holder.image.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> openHomeWindDetail(program.detailUrl));
        }

        @Override
        public int getItemCount() {
            return programs.size();
        }

        class HomeWindViewHolder extends RecyclerView.ViewHolder {
            final ImageView image;
            final TextView rank;
            final TextView hits;
            final TextView institution;
            final TextView title;
            final TextView period;

            HomeWindViewHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.windHomeImage);
                rank = itemView.findViewById(R.id.windHomeRank);
                hits = itemView.findViewById(R.id.windHomeHits);
                institution = itemView.findViewById(R.id.windHomeInstitution);
                title = itemView.findViewById(R.id.windHomeTitle);
                period = itemView.findViewById(R.id.windHomePeriod);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacks(mealHighlightUpdater);
        contentExecutor.shutdownNow();
    }
}
