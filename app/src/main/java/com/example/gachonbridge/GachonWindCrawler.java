package com.example.gachonbridge;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GachonWindCrawler {

    public static final String WIND_URL = "https://wind.gachon.ac.kr/";
    public static final String WIND_LIST_URL = "https://wind.gachon.ac.kr/ko/program/all";
    private static final String WIND_BASE_URL = "https://wind.gachon.ac.kr";
    private static final String USER_AGENT = "Mozilla/5.0 (Android) GachonBridge";

    public List<WindProgram> fetchTopPrograms(int count) throws IOException {
        List<WindProgram> all = fetchAllPrograms();
        Collections.sort(all, (a, b) -> b.hits - a.hits);
        return new ArrayList<>(all.subList(0, Math.min(count, all.size())));
    }

    public List<WindProgram> fetchLatestPrograms(int count) throws IOException {
        List<WindProgram> all = fetchAllPrograms();
        Collections.sort(all, (a, b) -> Long.compare(b.startTimestamp, a.startTimestamp));
        return new ArrayList<>(all.subList(0, Math.min(count, all.size())));
    }

    private List<WindProgram> fetchAllPrograms() throws IOException {
        Document document = Jsoup.connect(WIND_LIST_URL)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get();

        Elements items = document.select("div[data-role=item]");
        List<WindProgram> programs = new ArrayList<>();

        for (Element item : items) {
            WindProgram program = parseProgram(item);
            if (program.title.isEmpty() || containsTitle(programs, program.title)) {
                continue;
            }
            programs.add(program);
        }

        return programs;
    }

    private WindProgram parseProgram(Element item) {
        Element titleEl = item.selectFirst("b.title");
        String title = titleEl != null ? titleEl.text().trim() : "";

        Element hitEl = item.selectFirst("span.hit");
        int hits = 0;
        if (hitEl != null) {
            String hitText = hitEl.text().replaceAll("[^0-9]", "");
            if (!hitText.isEmpty()) {
                hits = Integer.parseInt(hitText);
            }
        }

        Element institutionEl = item.selectFirst("span.institution");
        String institution = institutionEl != null ? institutionEl.text().trim() : "";

        String applyPeriod = "";
        String period = "";
        long startTimestamp = 0;

        Elements dateLayerEls = item.select("small.date_layer");
        for (Element dateLayer : dateLayerEls) {
            String dateTitle = dateLayer.select(".date_title").text();
            Elements times = dateLayer.select("time");
            if (times.size() < 2) {
                continue;
            }

            String range = times.get(0).text() + " ~ " + times.get(1).text();
            if (dateTitle.contains("신청")) {
                applyPeriod = range;
            } else if (dateTitle.contains("운영")) {
                period = range;
                String timestamp = times.get(0).attr("data-time");
                if (!timestamp.isEmpty()) {
                    try {
                        startTimestamp = Long.parseLong(timestamp);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        Element linkEl = item.selectFirst("a[href]");
        String detailUrl = linkEl != null ? WIND_BASE_URL + linkEl.attr("href") : WIND_LIST_URL;

        // 커버 이미지 URL 파싱
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

        return new WindProgram(title, institution, applyPeriod, period, hits, startTimestamp, detailUrl, coverUrl);
    }

    private boolean containsTitle(List<WindProgram> programs, String title) {
        for (WindProgram program : programs) {
            if (program.title.equals(title)) {
                return true;
            }
        }
        return false;
    }

    public static class WindProgram {
        public final String title;
        public final String institution;
        public final String applyPeriod;
        public final String period;
        public final int hits;
        public final long startTimestamp;
        public final String detailUrl;
        public final String coverUrl;

        private WindProgram(
                String title,
                String institution,
                String applyPeriod,
                String period,
                int hits,
                long startTimestamp,
                String detailUrl,
                String coverUrl
        ) {
            this.title = title;
            this.institution = institution;
            this.applyPeriod = applyPeriod;
            this.period = period;
            this.hits = hits;
            this.startTimestamp = startTimestamp;
            this.detailUrl = detailUrl;
            this.coverUrl = coverUrl;
        }
    }
}