package com.example.gachonbridge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NoticeActivity extends BaseActivity {

    private TextView filterAll;
    private TextView filterAcademic;
    private TextView filterScholarship;
    private TextView tvLoading;
    private RecyclerView recyclerView;
    private NoticeAdapter adapter;
    private final List<Notice> noticeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        bindBottomNavigation(R.id.navNotice);

        filterAll = findViewById(R.id.buttonNoticeFilterAll);
        filterAcademic = findViewById(R.id.buttonNoticeFilterAcademic);
        filterScholarship = findViewById(R.id.buttonNoticeFilterScholarship);
        tvLoading = findViewById(R.id.tvNoticeLoading);
        recyclerView = findViewById(R.id.noticeRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeAdapter(noticeList);
        recyclerView.setAdapter(adapter);

        filterAll.setOnClickListener(v -> loadNotices(GachonScraper.Category.ALL));
        filterAcademic.setOnClickListener(v -> loadNotices(GachonScraper.Category.ACADEMIC));
        filterScholarship.setOnClickListener(v -> loadNotices(GachonScraper.Category.SCHOLARSHIP));

        findViewById(R.id.buttonNoticeMore).setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gachon.ac.kr/kor/7986/subview.do")));
        });

        loadNotices(GachonScraper.Category.ALL);
    }

    private void loadNotices(GachonScraper.Category category) {
        setActiveFilter(category);
        noticeList.clear();
        adapter.notifyDataSetChanged();
        tvLoading.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                // Fetch up to 12 notices as requested
                List<Notice> fetched = GachonScraper.fetchNotices(category, 12);
                runOnUiThread(() -> {
                    if (fetched.size() > 12) {
                        noticeList.addAll(fetched.subList(0, 12));
                    } else {
                        noticeList.addAll(fetched);
                    }
                    adapter.notifyDataSetChanged();
                    tvLoading.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    tvLoading.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void setActiveFilter(GachonScraper.Category category) {
        setFilterState(filterAll, category == GachonScraper.Category.ALL);
        setFilterState(filterAcademic, category == GachonScraper.Category.ACADEMIC);
        setFilterState(filterScholarship, category == GachonScraper.Category.SCHOLARSHIP);
    }

    private void setFilterState(TextView filter, boolean active) {
        filter.setBackgroundResource(active ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        filter.setTextColor(ContextCompat.getColor(
                this,
                active ? R.color.gb_on_primary : R.color.gb_on_surface
        ));
    }

    class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.VH> {
        private final List<Notice> items;

        NoticeAdapter(List<Notice> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice_card, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Notice notice = items.get(position);
            holder.tvTitle.setText(notice.getTitle());
            holder.tvDate.setText(notice.getDate());
            holder.tvBadge.setText(notice.getCategory().equals("ACADEMIC") ? "학사" : 
                                 notice.getCategory().equals("SCHOLARSHIP") ? "장학" : "전체");

            if (notice.isPinned()) {
                setBackgroundPreservingPadding(holder.container, R.drawable.bg_notice_featured);
                holder.tvScrap.setVisibility(View.VISIBLE);
            } else {
                setBackgroundPreservingPadding(holder.container, R.drawable.bg_card);
                holder.tvScrap.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(notice.getUrl())));
            });
        }

        private void setBackgroundPreservingPadding(View view, int resId) {
            int pL = view.getPaddingLeft();
            int pT = view.getPaddingTop();
            int pR = view.getPaddingRight();
            int pB = view.getPaddingBottom();
            view.setBackgroundResource(resId);
            view.setPadding(pL, pT, pR, pB);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            View container;
            TextView tvBadge, tvTitle, tvDate, tvScrap;

            VH(View v) {
                super(v);
                container = v.findViewById(R.id.noticeItemContainer);
                tvBadge = v.findViewById(R.id.tvNoticeBadge);
                tvTitle = v.findViewById(R.id.tvNoticeTitle);
                tvDate = v.findViewById(R.id.tvNoticeDate);
                tvScrap = v.findViewById(R.id.tvNoticeScrap);
            }
        }
    }
}
