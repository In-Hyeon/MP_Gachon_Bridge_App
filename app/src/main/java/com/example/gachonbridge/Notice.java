package com.example.gachonbridge;

public class Notice {
    private String title;
    private String date;
    private String url;
    private String category;
    private boolean isPinned;

    public Notice(String title, String date, String url, String category, boolean isPinned) {
        this.title = title;
        this.date = date;
        this.url = url;
        this.category = category;
        this.isPinned = isPinned;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getUrl() { return url; }
    public String getCategory() { return category; }
    public boolean isPinned() { return isPinned; }
}
