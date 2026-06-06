package com.example.gachonbridge;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.gb_surface_low));
        window.setNavigationBarColor(getColor(R.color.gb_surface_low));
        bindBackToHome();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        applySystemBarInsets();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        applySystemBarInsets();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        applySystemBarInsets();
    }

    private void applySystemBarInsets() {
        ViewGroup content = findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) {
            return;
        }

        View root = content.getChildAt(0);
        int initialLeft = root.getPaddingLeft();
        int initialTop = root.getPaddingTop();
        int initialRight = root.getPaddingRight();
        int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    initialLeft,
                    initialTop + systemBars.top,
                    initialRight,
                    initialBottom + systemBars.bottom
            );
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void bindBackToHome() {
        if (!shouldBindBackToHome()) {
            return;
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent, ActivityOptions.makeCustomAnimation(BaseActivity.this, 0, 0).toBundle());
                overridePendingTransition(0, 0);
            }
        });
    }

    protected boolean shouldBindBackToHome() {
        return !(this instanceof MainActivity);
    }

    protected void bindBottomNavigation(int activeNavId) {
        bindNav(R.id.navHome, MainActivity.class, activeNavId);
        bindNav(R.id.navWind, WindActivity.class, activeNavId);
        bindNav(R.id.navContact, ContactActivity.class, activeNavId);
        bindNav(R.id.navClub, ClubActivity.class, activeNavId);
        bindNav(R.id.navNotice, NoticeActivity.class, activeNavId);
        bindNav(R.id.navLab, LabActivity.class, activeNavId);
    }

    private void bindNav(int navId, Class<?> target, int activeNavId) {
        View item = findViewById(navId);
        if (item == null) {
            return;
        }

        boolean active = navId == activeNavId;
        int color = ContextCompat.getColor(
                this,
                active ? R.color.gb_primary_container : R.color.gb_on_surface_variant
        );
        applyBottomNavColor(item, color);
        item.setBackgroundResource(active ? R.drawable.bg_nav_active : 0);
        item.setOnClickListener(v -> {
            if (active) {
                return;
            }
            Intent intent = new Intent(this, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent, ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle());
            overridePendingTransition(0, 0);
        });
    }

    private void applyBottomNavColor(View view, int color) {
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
            return;
        }
        if (view instanceof ImageView) {
            ((ImageView) view).setImageTintList(ColorStateList.valueOf(color));
            return;
        }
        if (!(view instanceof ViewGroup)) {
            return;
        }

        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            applyBottomNavColor(group.getChildAt(i), color);
        }
    }
}
