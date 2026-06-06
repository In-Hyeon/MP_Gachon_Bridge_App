package com.example.gachonbridge;

import android.util.Base64;
import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GachonScraper {
    private static final String BASE_URL = "https://www.gachon.ac.kr";

    public enum Category {
        ALL("7986"),
        ACADEMIC("3104"),
        SCHOLARSHIP("1146");

        public final String id;
        Category(String id) { this.id = id; }
    }

    public static List<Notice> fetchNotices(Category category, int minCount) throws IOException {
        List<Notice> allNotices = new ArrayList<>();
        
        // Always try to fetch first 2 pages to be safe
        for (int page = 1; page <= 2; page++) {
            String targetUrl = BASE_URL + "/kor/" + category.id + "/subview.do?page=" + page;
            Document doc = Jsoup.connect(targetUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            // Find all tables and pick the one with notice list
            Elements tables = doc.select("table");
            for (Element table : tables) {
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    // Skip header rows (th)
                    if (row.selectFirst("th") != null) continue;
                    
                    // Regular notice rows usually have multiple cells
                    Elements cells = row.select("td");
                    if (cells.size() < 3) continue;

                    Element link = row.selectFirst("a");
                    if (link == null) continue;

                    String title = link.text().trim();
                    if (title.length() < 2 || title.contains("게시판 목록") || title.contains("다음") || title.contains("이전")) continue;

                    String rawHref = link.attr("href");
                    String url = targetUrl;

                    if (rawHref.contains("jf_viewArtcl")) {
                        Pattern p = Pattern.compile("'([^']*)'");
                        Matcher m = p.matcher(rawHref);
                        List<String> args = new ArrayList<>();
                        while (m.find()) args.add(m.group(1));

                        if (args.size() >= 2) {
                            String lang = args.get(0);
                            String id = args.get(1);
                            String rawParams = "fnct1|@@|/commonNotice/" + lang + "/" + id + "/artclView.do?page=1&srchColumn=&srchWord=&";
                            String enc = Base64.encodeToString(rawParams.getBytes(), Base64.NO_WRAP | Base64.URL_SAFE);
                            url = BASE_URL + "/kor/" + category.id + "/subview.do?enc=" + enc;
                        }
                    } else if (rawHref.startsWith("/")) {
                        url = BASE_URL + rawHref;
                    } else if (rawHref.startsWith("http")) {
                        url = rawHref;
                    }

                    // Simple date search
                    String date = "";
                    for (Element cell : cells) {
                        String txt = cell.text().trim();
                        if (txt.matches(".*\\d{2}\\.\\d{2}.*")) { // Match any string containing MM.DD
                            date = txt;
                        }
                    }

                    boolean isPinned = row.hasClass("notice") || row.selectFirst(".notice") != null || row.text().contains("공지");
                    
                    // Duplicate check by title
                    boolean isDup = false;
                    for (Notice n : allNotices) {
                        if (n.getTitle().equals(title)) { isDup = true; break; }
                    }
                    
                    if (!isDup) {
                        allNotices.add(new Notice(title, date, url, category.name(), isPinned));
                    }
                }
            }
        }
        return allNotices;
    }
}
