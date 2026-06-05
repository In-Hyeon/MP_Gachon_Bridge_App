package com.example.gachonbridge;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GachonMealCrawler {

    private static final String EMPTY_MENU = "-";
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}\\.\\d{2}\\.\\d{2}");
    private static final String NO_MENU_TEXT = "등록된 식단내용이(가) 없습니다.";
    private static final String USER_AGENT = "Mozilla/5.0 (Android) GachonBridge";

    private final RestaurantSource[] restaurantSources = {
            new RestaurantSource(
                    "교육대학원",
                    "https://www.gachon.ac.kr/kor/7349/subview.do",
                    null,
                    "점심",
                    "저녁"
            ),
            new RestaurantSource(
                    "비전타워",
                    "https://www.gachon.ac.kr/kor/7347/subview.do",
                    "천원의아침밥",
                    "점심 A메뉴(정식)",
                    null
            ),
            new RestaurantSource(
                    "학생생활관",
                    "https://www.gachon.ac.kr/kor/7350/subview.do",
                    "천원의아침밥",
                    "점심",
                    "저녁"
            )
    };

    public List<RestaurantMenu> fetchTodayMenus(Date date) throws IOException {
        String dateKey = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(date);
        List<RestaurantMenu> menus = new ArrayList<>();

        for (RestaurantSource source : restaurantSources) {
            Document document = Jsoup.connect(source.url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();
            menus.add(parseTodayMenu(document, dateKey, source));
        }

        return menus;
    }

    private RestaurantMenu parseTodayMenu(Document document, String dateKey, RestaurantSource source) {
        RestaurantMenu menu = new RestaurantMenu(source.name);
        Elements rows = document.select("table tbody tr");
        String currentDate = "";

        for (Element row : rows) {
            Element dateCell = row.selectFirst("th");
            if (dateCell != null) {
                currentDate = extractDate(dateCell.text());
            }

            if (!dateKey.equals(currentDate)) {
                continue;
            }

            Elements cells = row.select("td");
            if (cells.size() < 2) {
                continue;
            }

            String mealType = normalizeSpaces(cells.get(0).text());
            String mealText = normalizeMenu(cellToText(cells.get(1)));

            if (matchesMealType(mealType, source.breakfastKey)) {
                menu.breakfast = mealText;
            } else if (matchesMealType(mealType, source.lunchKey)) {
                menu.lunch = mealText;
            } else if (matchesMealType(mealType, source.dinnerKey)) {
                menu.dinner = mealText;
            }
        }

        menu.fillEmptyMenus();
        return menu;
    }

    private boolean matchesMealType(String mealType, String key) {
        return key != null && mealType.startsWith(key);
    }

    private String extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private String cellToText(Element cell) {
        StringBuilder builder = new StringBuilder();
        for (Node node : cell.childNodes()) {
            appendNodeText(node, builder);
        }
        return builder.toString();
    }

    private void appendNodeText(Node node, StringBuilder builder) {
        if (node instanceof TextNode) {
            builder.append(((TextNode) node).text());
            return;
        }

        if ("br".equalsIgnoreCase(node.nodeName())) {
            builder.append('\n');
            return;
        }

        for (Node child : node.childNodes()) {
            appendNodeText(child, builder);
        }
    }

    private String normalizeMenu(String text) {
        String normalized = text
                .replace('\u00A0', ' ')
                .replace("\r", "\n");
        String[] lines = normalized.split("\\n");
        StringBuilder builder = new StringBuilder();

        for (String line : lines) {
            String cleaned = normalizeSpaces(line);
            if (cleaned.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(cleaned);
        }

        String result = builder.toString();
        if (result.isEmpty() || NO_MENU_TEXT.equals(result)) {
            return EMPTY_MENU;
        }
        return result;
    }

    private String normalizeSpaces(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    private static class RestaurantSource {
        private final String name;
        private final String url;
        private final String breakfastKey;
        private final String lunchKey;
        private final String dinnerKey;

        private RestaurantSource(
                String name,
                String url,
                String breakfastKey,
                String lunchKey,
                String dinnerKey
        ) {
            this.name = name;
            this.url = url;
            this.breakfastKey = breakfastKey;
            this.lunchKey = lunchKey;
            this.dinnerKey = dinnerKey;
        }
    }

    public static class RestaurantMenu {
        public final String restaurantName;
        public String breakfast = EMPTY_MENU;
        public String lunch = EMPTY_MENU;
        public String dinner = EMPTY_MENU;

        private RestaurantMenu(String restaurantName) {
            this.restaurantName = restaurantName;
        }

        private void fillEmptyMenus() {
            if (breakfast == null || breakfast.trim().isEmpty()) {
                breakfast = EMPTY_MENU;
            }
            if (lunch == null || lunch.trim().isEmpty()) {
                lunch = EMPTY_MENU;
            }
            if (dinner == null || dinner.trim().isEmpty()) {
                dinner = EMPTY_MENU;
            }
        }
    }
}
