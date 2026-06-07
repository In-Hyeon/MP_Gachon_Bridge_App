package com.example.gachonbridge;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NoticeWorker extends Worker {

    private static final String TAG = "NoticeWorker";
    private static final String PREF_NAME = "keyword_prefs";
    private static final String KEY_KEYWORDS = "user_keywords";
    private static final String KEY_NOTIFIED_URLS = "notified_urls";
    private static final String CHANNEL_ID = "notice_keyword_channel";

    public NoticeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Background notice check started...");
        
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> keywords = prefs.getStringSet(KEY_KEYWORDS, new HashSet<>());
        Set<String> notifiedUrls = prefs.getStringSet(KEY_NOTIFIED_URLS, new HashSet<>());

        if (keywords.isEmpty()) {
            return Result.success();
        }

        try {
            // Check all categories
            checkCategory(context, GachonScraper.Category.ALL, keywords, notifiedUrls, prefs);
            checkCategory(context, GachonScraper.Category.ACADEMIC, keywords, notifiedUrls, prefs);
            checkCategory(context, GachonScraper.Category.SCHOLARSHIP, keywords, notifiedUrls, prefs);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }

        return Result.success();
    }

    private void checkCategory(Context context, GachonScraper.Category category, Set<String> keywords, Set<String> notifiedUrls, SharedPreferences prefs) throws Exception {
        List<Notice> notices = GachonScraper.fetchNotices(category, 10);
        
        for (Notice notice : notices) {
            if (notifiedUrls.contains(notice.getUrl())) continue;

            for (String keyword : keywords) {
                if (notice.getTitle().contains(keyword)) {
                    sendNotification(context, notice, keyword);
                    
                    // Mark as notified
                    Set<String> updatedNotified = new HashSet<>(prefs.getStringSet(KEY_NOTIFIED_URLS, new HashSet<>()));
                    updatedNotified.add(notice.getUrl());
                    prefs.edit().putStringSet(KEY_NOTIFIED_URLS, updatedNotified).apply();
                    break; 
                }
            }
        }
    }

    private void sendNotification(Context context, Notice notice, String keyword) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "공지사항 키워드 알림", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(notice.getUrl()));
        PendingIntent pi = PendingIntent.getActivity(context, notice.getUrl().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bg_icon_circle) // Placeholder, ideally a bell icon
                .setContentTitle("키워드 공지 알림: [" + keyword + "]")
                .setContentText(notice.getTitle())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        nm.notify(notice.getUrl().hashCode(), builder.build());
    }

    public static void schedulePeriodicWork(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(NoticeWorker.class, 2, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "NoticeCheckWork",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    public static void runImmediateCheck(Context context) {
        androidx.work.OneTimeWorkRequest request = new androidx.work.OneTimeWorkRequest.Builder(NoticeWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build();
        WorkManager.getInstance(context).enqueue(request);
    }
}
