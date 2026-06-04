package com.example.gachonbridge;

import android.os.Bundle;
import android.widget.TextView;

public abstract class SimplePageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_page);
        bindBottomNavigation(activeNavId());
        // TODO: Replace dummy string resources with API response data for each page.
        setText(R.id.pageTitle, titleResId());
        setText(R.id.pageHeading, titleResId());
        setText(R.id.pageDescription, subtitleResId());
        setText(R.id.pageCardTitle, cardTitleResId());
        setText(R.id.pageCardBody, cardBodyResId());
    }

    protected abstract int activeNavId();

    protected abstract int titleResId();

    protected abstract int subtitleResId();

    protected abstract int cardTitleResId();

    protected abstract int cardBodyResId();

    private void setText(int viewId, int textResId) {
        TextView textView = findViewById(viewId);
        if (textView != null) {
            textView.setText(textResId);
        }
    }
}
