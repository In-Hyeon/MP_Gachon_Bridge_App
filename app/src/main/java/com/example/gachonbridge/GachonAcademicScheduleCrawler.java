package com.example.gachonbridge;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GachonAcademicScheduleCrawler {

    private static final String SCHEDULE_URL = "https://www.gachon.ac.kr/dbm/6482/subview.do";
    private static final String EMPTY_SCHEDULE = "-";
    private static final String USER_AGENT = "Mozilla/5.0 (Android) GachonBridge";
    private static final Pattern DATE_RANGE_PATTERN =
            Pattern.compile("(\\d{2})\\.(\\d{2})(?:\\s*~\\s*(\\d{2})\\.(\\d{2}))?");

    public List<DailySchedule> fetchSchedulesFrom(Date startDate, int days) throws IOException {
        List<DailySchedule> dailySchedules = buildEmptyDailySchedules(startDate, days);
        Document document = Jsoup.connect(SCHEDULE_URL)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get();

        Elements rows = document.select(".sche-comt table tbody tr");
        for (Element row : rows) {
            Element dateCell = row.selectFirst("th");
            Element titleCell = row.selectFirst("td");
            if (dateCell == null || titleCell == null) {
                continue;
            }

            String rangeText = normalizeSpaces(dateCell.text());
            String title = normalizeSpaces(titleCell.text());
            if (title.isEmpty()) {
                continue;
            }

            Matcher matcher = DATE_RANGE_PATTERN.matcher(rangeText);
            if (!matcher.find()) {
                continue;
            }

            MonthDay start = new MonthDay(matcher.group(1), matcher.group(2));
            MonthDay end = matcher.group(3) == null
                    ? start
                    : new MonthDay(matcher.group(3), matcher.group(4));

            for (DailySchedule dailySchedule : dailySchedules) {
                if (containsDate(start, end, dailySchedule.date)) {
                    dailySchedule.addTitle(title);
                }
            }
        }

        for (DailySchedule dailySchedule : dailySchedules) {
            dailySchedule.fillEmptyTitle();
        }
        return dailySchedules;
    }

    public List<DailySchedule> buildLoadingSchedules(Date startDate, int days) {
        List<DailySchedule> schedules = buildEmptyDailySchedules(startDate, days);
        for (DailySchedule schedule : schedules) {
            schedule.title = "불러오는 중...";
        }
        return schedules;
    }

    public List<DailySchedule> buildEmptySchedules(Date startDate, int days) {
        List<DailySchedule> schedules = buildEmptyDailySchedules(startDate, days);
        for (DailySchedule schedule : schedules) {
            schedule.fillEmptyTitle();
        }
        return schedules;
    }

    private List<DailySchedule> buildEmptyDailySchedules(Date startDate, int days) {
        List<DailySchedule> schedules = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.setTime(startDate);

        for (int i = 0; i < days; i++) {
            schedules.add(new DailySchedule(calendar.getTime(), i));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return schedules;
    }

    private boolean containsDate(MonthDay start, MonthDay end, Date date) {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.setTime(date);
        MonthDay target = new MonthDay(
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        if (start.compareTo(end) <= 0) {
            return target.compareTo(start) >= 0 && target.compareTo(end) <= 0;
        }

        return target.compareTo(start) >= 0 || target.compareTo(end) <= 0;
    }

    private String normalizeSpaces(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    private static class MonthDay implements Comparable<MonthDay> {
        private final int month;
        private final int day;

        private MonthDay(String month, String day) {
            this(Integer.parseInt(month), Integer.parseInt(day));
        }

        private MonthDay(int month, int day) {
            this.month = month;
            this.day = day;
        }

        @Override
        public int compareTo(MonthDay other) {
            if (month != other.month) {
                return month - other.month;
            }
            return day - other.day;
        }
    }

    public static class DailySchedule {
        public final Date date;
        public final String dateLabel;
        public final String dayLabel;
        public final String ddayLabel;
        public String title = "";

        private DailySchedule(Date date, int dayOffset) {
            this.date = date;
            this.dateLabel = new SimpleDateFormat("M.d", Locale.KOREA).format(date);
            this.dayLabel = new SimpleDateFormat("E", Locale.KOREA).format(date);
            if (dayOffset == 0) {
                this.ddayLabel = "TODAY";
            } else {
                this.ddayLabel = "D+" + dayOffset;
            }
        }

        private void addTitle(String nextTitle) {
            if (title.isEmpty()) {
                title = nextTitle;
            } else if (!title.contains(nextTitle)) {
                title += "\n" + nextTitle;
            }
        }

        private void fillEmptyTitle() {
            if (title.trim().isEmpty()) {
                title = EMPTY_SCHEDULE;
            }
        }
    }
}
