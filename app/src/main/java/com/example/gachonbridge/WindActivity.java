package com.example.gachonbridge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WindActivity extends BaseActivity {

    public static final String WIND_URL = "https://wind.gachon.ac.kr/";
    private static final String WIND_BASE_URL = "https://wind.gachon.ac.kr";
    private static final String WIND_LIST_URL = "https://wind.gachon.ac.kr/ko/program/all";

    private TextView tvLoading;
    private RecyclerView rvTop3;
    private RecyclerView rvLatest;
    private View sectionLatest;

    static class WindProgram {
        String title;
        String institution;
        String applyPeriod;
        String period;
        int    hits;
        long   startTimestamp;
        String detailUrl;
        String coverUrl;

        WindProgram(String title, String institution, String applyPeriod,
                    String period, int hits, long startTimestamp, String detailUrl, String coverUrl) {
            this.title          = title;
            this.institution    = institution;
            this.applyPeriod    = applyPeriod;
            this.period         = period;
            this.hits           = hits;
            this.startTimestamp = startTimestamp;
            this.detailUrl      = detailUrl;
            this.coverUrl       = coverUrl;
        }
    }

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
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WIND_LIST_URL))));
        }

        loadWindData();
    }

    private void loadWindData() {
        tvLoading.setVisibility(View.VISIBLE);
        rvTop3.setVisibility(View.GONE);
        sectionLatest.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                Document doc = Jsoup.connect(WIND_LIST_URL)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();

                Elements items = doc.select("div[data-role=item]");
                List<WindProgram> all = new ArrayList<>();

                for (Element item : items) {
                    Element titleEl = item.selectFirst("b.title");
                    String title = titleEl != null ? titleEl.text().trim() : "";

                    Element hitEl = item.selectFirst("span.hit");
                    int hits = 0;
                    if (hitEl != null) {
                        String hitText = hitEl.text().replaceAll("[^0-9]", "");
                        if (!hitText.isEmpty()) hits = Integer.parseInt(hitText);
                    }

                    Element instEl = item.selectFirst("span.institution");
                    String institution = instEl != null ? instEl.text().trim() : "";

                    Elements dateLayerEls = item.select("small.date_layer");
                    String applyPeriod = "";
                    String period      = "";
                    long   startTimestamp = 0;

                    for (Element dl : dateLayerEls) {
                        String dateTitle = dl.select(".date_title").text();
                        Elements times   = dl.select("time");
                        if (times.size() >= 2) {
                            String range = times.get(0).text() + " ~ " + times.get(1).text();
                            if (dateTitle.contains("신청")) {
                                applyPeriod = range;
                            } else if (dateTitle.contains("운영")) {
                                period = range;
                                String dt = times.get(0).attr("data-time");
                                if (!dt.isEmpty()) {
                                    try { startTimestamp = Long.parseLong(dt); } catch (Exception ignored) {}
                                }
                            }
                        }
                    }

                    Element aEl = item.selectFirst("a[href]");
                    String href = aEl != null ? aEl.attr("href") : "";
                    String detailUrl = href.startsWith("http") ? href : WIND_BASE_URL + href;
                    if (detailUrl.isEmpty()) detailUrl = WIND_LIST_URL;

                    String coverUrl = "";
                    Element coverEl = item.selectFirst("div.cover");
                    if (coverEl != null) {
                        String bgStyle = coverEl.attr("style");
                        int urlStart = bgStyle.indexOf("url(");
                        if (urlStart >= 0) {
                            String after = bgStyle.substring(urlStart + 4);
                            after = after.replace("&quot;", "").replace("\"", "").replace("'", "");
                            int urlEnd = after.indexOf(")");
                            if (urlEnd > 0) {
                                String path = after.substring(0, urlEnd).trim();
                                coverUrl = path.startsWith("http") ? path : WIND_BASE_URL + path;
                            }
                        }
                    }

                    if (!title.isEmpty()) {
                        boolean isDuplicate = false;
                        for (WindProgram existing : all) {
                            if (existing.title.equals(title)) {
                                isDuplicate = true;
                                break;
                            }
                        }
                        if (!isDuplicate) {
                            all.add(new WindProgram(title, institution, applyPeriod,
                                    period, hits, startTimestamp, detailUrl, coverUrl));
                        }
                    }
                }

                List<WindProgram> top3 = new ArrayList<>(all);
                Collections.sort(top3, (a, b) -> b.hits - a.hits);
                final List<WindProgram> top3Final = top3.subList(0, Math.min(3, top3.size()));

                List<WindProgram> latest = new ArrayList<>(all);
                Collections.sort(latest, (a, b) -> Long.compare(b.startTimestamp, a.startTimestamp));
                final List<WindProgram> latestFinal = latest.subList(0, Math.min(4, latest.size()));

                runOnUiThread(() -> {
                    tvLoading.setVisibility(View.GONE);
                    rvTop3.setVisibility(View.VISIBLE);
                    sectionLatest.setVisibility(View.VISIBLE);

                    rvTop3.setAdapter(new WindAdapter(new ArrayList<>(top3Final), true));
                    rvTop3.scrollToPosition(0);
                    rvLatest.setAdapter(new WindAdapter(new ArrayList<>(latestFinal), false));
                });

            } catch (IOException e) {
                runOnUiThread(() ->
                        tvLoading.setText("데이터를 불러오지 못했습니다.\n잠시 후 다시 시도해주세요."));
            }
        }).start();
    }

    class WindAdapter extends RecyclerView.Adapter<WindAdapter.VH> {

        private List<WindProgram> list;
        private boolean showRank;

        WindAdapter(List<WindProgram> list, boolean showRank) {
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
            WindProgram p = list.get(pos);

            if (showRank) {
                h.tvRank.setVisibility(View.VISIBLE);
                h.tvRank.setText(String.valueOf(pos + 1));
            } else {
                h.tvRank.setVisibility(View.GONE);
            }

            if (h.ivCover != null) {
                if (p.coverUrl != null && !p.coverUrl.isEmpty()) {
                    h.ivCover.setVisibility(View.VISIBLE);
                    Glide.with(h.itemView.getContext())
                            .load(p.coverUrl)
                            .centerCrop()
                            .into(h.ivCover);
                } else {
                    h.ivCover.setVisibility(View.GONE);
                }
            }
            h.tvTitle.setText(p.title);
            h.tvInstitution.setText(p.institution);
            h.tvHits.setText(p.hits + " HITS");
            h.tvApplyPeriod.setText("신청  " + p.applyPeriod);
            h.tvPeriod.setText("운영  " + p.period);

            h.cardLayout.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(p.detailUrl)));
                } catch (Exception e) {
                    Toast.makeText(WindActivity.this, "브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivCover;
            TextView tvRank, tvTitle, tvInstitution, tvHits, tvApplyPeriod, tvPeriod;
            LinearLayout cardLayout;

            VH(View v) {
                super(v);
                cardLayout    = (LinearLayout) ((LinearLayout) v).getChildAt(0);
                ivCover       = v.findViewById(R.id.ivWindCover);
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