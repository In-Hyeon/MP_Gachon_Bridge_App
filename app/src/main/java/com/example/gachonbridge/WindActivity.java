package com.example.gachonbridge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WindActivity extends BaseActivity {

    private TextView tvLoading;
    private RecyclerView rvTop3;
    private RecyclerView rvLatest;
    private View sectionLatest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wind);
        bindBottomNavigation(R.id.navWind);

        tvLoading    = findViewById(R.id.tvWindLoading);
        rvTop3       = findViewById(R.id.windRecyclerView);
        rvLatest     = findViewById(R.id.windLatestRecyclerView);
        sectionLatest= findViewById(R.id.windSectionLatest);

        rvTop3.setLayoutManager(new LinearLayoutManager(this));
        rvLatest.setLayoutManager(new LinearLayoutManager(this));

        View btnMore = findViewById(R.id.buttonWindMorePrograms);
        if (btnMore != null) {
            btnMore.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GachonWindCrawler.WIND_LIST_URL))));
        }

        loadWindData();
    }

    private void loadWindData() {
        tvLoading.setVisibility(View.VISIBLE);
        rvTop3.setVisibility(View.GONE);
        sectionLatest.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                GachonWindCrawler crawler = new GachonWindCrawler();
                final List<GachonWindCrawler.WindProgram> top3Final = crawler.fetchTopPrograms(3);
                final List<GachonWindCrawler.WindProgram> latestFinal = crawler.fetchLatestPrograms(4);

                runOnUiThread(() -> {
                    tvLoading.setVisibility(View.GONE);
                    rvTop3.setVisibility(View.VISIBLE);
                    sectionLatest.setVisibility(View.VISIBLE);

                    rvTop3.setAdapter(new WindAdapter(new ArrayList<>(top3Final), true));
                    rvLatest.setAdapter(new WindAdapter(new ArrayList<>(latestFinal), false));
                });

            } catch (IOException e) {
                runOnUiThread(() ->
                    tvLoading.setText("데이터를 불러오지 못했습니다.\n잠시 후 다시 시도해주세요."));
            }
        }).start();
    }

    // ── Adapter ──
    class WindAdapter extends RecyclerView.Adapter<WindAdapter.VH> {

        private List<GachonWindCrawler.WindProgram> list;
        private boolean showRank;

        WindAdapter(List<GachonWindCrawler.WindProgram> list, boolean showRank) {
            this.list     = list;
            this.showRank = showRank;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_wind_card, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            GachonWindCrawler.WindProgram p = list.get(pos);

            if (showRank) {
                h.tvRank.setVisibility(View.VISIBLE);
                h.tvRank.setText(String.valueOf(pos + 1));
            } else {
                h.tvRank.setVisibility(View.GONE);
            }

            h.tvTitle.setText(p.title);
            h.tvInstitution.setText(p.institution);
            h.tvHits.setText(p.hits + " HITS");
            h.tvApplyPeriod.setText("신청  " + p.applyPeriod);
            h.tvPeriod.setText("운영  " + p.period);

            h.itemView.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(p.detailUrl)));
                } catch (Exception e) {
                    Toast.makeText(WindActivity.this, "브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvRank, tvTitle, tvInstitution, tvHits, tvApplyPeriod, tvPeriod;
            VH(View v) {
                super(v);
                tvRank        = v.findViewById(R.id.tvWindRank);
                tvTitle       = v.findViewById(R.id.tvWindTitle);
                tvInstitution = v.findViewById(R.id.tvWindInstitution);
                tvHits        = v.findViewById(R.id.tvWindHits);
                tvApplyPeriod = v.findViewById(R.id.tvWindApplyPeriod);
                tvPeriod      = v.findViewById(R.id.tvWindPeriod);
            }
        }
    }
}
