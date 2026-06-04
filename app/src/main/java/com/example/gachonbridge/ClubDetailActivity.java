package com.example.gachonbridge;

import android.os.Bundle;
import android.widget.TextView;

public class ClubDetailActivity extends BaseActivity {

    public static final String EXTRA_NAME = "club_name";
    public static final String EXTRA_CATEGORY = "club_category";
    public static final String EXTRA_DESCRIPTION = "club_description";
    public static final String EXTRA_BADGE = "club_badge";
    public static final String EXTRA_ICON = "club_icon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_detail);
        bindBottomNavigation(R.id.navClub);
        bindBackButton();
        bindClubDetail();
    }

    @Override
    protected boolean shouldBindBackToHome() {
        return false;
    }

    private void bindBackButton() {
        TextView backButton = findViewById(R.id.buttonClubDetailBack);
        backButton.setOnClickListener(v -> finish());
    }

    private void bindClubDetail() {
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        String description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        String badge = getIntent().getStringExtra(EXTRA_BADGE);
        String icon = getIntent().getStringExtra(EXTRA_ICON);

        if (name == null) {
            name = getString(R.string.page_club_title);
        }
        if (category == null) {
            category = "";
        }
        if (description == null) {
            description = "";
        }
        if (badge == null || badge.isEmpty()) {
            badge = category;
        }
        if (icon == null || icon.isEmpty()) {
            icon = "GB";
        }

        setText(R.id.clubDetailHeroBadge, detailBadgeText(category));
        setText(R.id.clubDetailHeroTitle, name);
        setText(R.id.clubDetailHeroSubtitle, detailSubtitle(category));
        setText(R.id.clubDetailIntroTitle, "\uB3D9\uC544\uB9AC \uC18C\uAC1C");
        setText(R.id.clubDetailIntroBody, detailIntro(name, description, category));
        setText(R.id.clubDetailPhotoTitle, "\uD65C\uB3D9 \uC0AC\uC9C4");
        setText(R.id.clubDetailHeroIcon, icon);
        setText(R.id.clubDetailPhotoMain, icon);
        setText(R.id.clubDetailPhotoSide, badge);
    }

    private void setText(int viewId, String text) {
        TextView textView = findViewById(viewId);
        textView.setText(text);
    }

    private String detailBadgeText(String category) {
        if ("\uC74C\uC545".equals(category)) {
            return "\uC5B4\uCFE0\uC2A4\uD2F1 \uBC34\uB4DC";
        }
        if ("\uACF5\uC5F0".equals(category)) {
            return "\uACF5\uC5F0 \uD06C\uB8E8";
        }
        if ("\uC0AC\uD68C\u00B7\uD559\uC220".equals(category)) {
            return "\uD559\uC220 \uCEE4\uBBA4\uB2C8\uD2F0";
        }
        return category;
    }

    private String detailSubtitle(String category) {
        if ("\uC74C\uC545".equals(category)) {
            return "\uC5B4\uCFE0\uC2A4\uD2F1\uC758 \uB530\uB73B\uD55C \uC6B8\uB9BC, \uC6B0\uB9AC\uC758 \uCCAD\uCD98\uC744 \uB178\uB798\uD558\uB2E4.";
        }
        if ("\uCCB4\uC721".equals(category)) {
            return "\uD568\uAED8 \uC6C0\uC9C1\uC774\uBA70 \uD300\uC6CC\uD06C\uC640 \uC5D0\uB108\uC9C0\uB97C \uB9CC\uB4E4\uB2E4.";
        }
        return "\uC88B\uC544\uD558\uB294 \uBD84\uC57C\uC5D0\uC11C \uC0AC\uB78C\uB4E4\uACFC \uC5F0\uACB0\uB418\uACE0 \uACBD\uD5D8\uC744 \uB113\uD788\uB2E4.";
    }

    private String detailIntro(String name, String description, String category) {
        if ("\uC74C\uC545".equals(category)) {
            return "1985\uB144\uC5D0 \uCC3D\uC124\uB41C '" + name + "'\uC740 \uAC00\uCC9C\uB300\uD559\uAD50 \uAE00\uB85C\uBC8C\uCEA0\uD37C\uC2A4 \uC911\uC559 \uC74C\uC545 \uB3D9\uC544\uB9AC\uC785\uB2C8\uB2E4. "
                    + description
                    + " \uB9E4 \uD559\uAE30 \uC815\uAE30 \uACF5\uC5F0\uACFC \uBC84\uC2A4\uD0B9\uC744 \uD1B5\uD574 \uD559\uC6B0\uB4E4\uACFC \uC74C\uC545\uC73C\uB85C \uC18C\uD1B5\uD558\uBA70, "
                    + "\uC74C\uC545\uC744 \uC0AC\uB791\uD558\uB294 \uC0AC\uB78C\uB4E4\uC774 \uBAA8\uC5EC \uB530\uB73B\uD55C \uCD94\uC5B5\uC744 \uB9CC\uB4E4\uC5B4\uAC00\uACE0 \uC788\uC2B5\uB2C8\uB2E4.";
        }
        return "'" + name + "'\uC740 " + category + " \uBD84\uC57C\uC5D0 \uAD00\uC2EC \uC788\uB294 \uD559\uC0DD\uB4E4\uC774 \uBAA8\uC5EC \uD65C\uB3D9\uD558\uB294 \uB3D9\uC544\uB9AC\uC785\uB2C8\uB2E4. "
                + description
                + " \uC815\uAE30 \uBAA8\uC784\uACFC \uD504\uB85C\uADF8\uB7A8\uC744 \uD1B5\uD574 \uC0C8\uB85C\uC6B4 \uACBD\uD5D8\uC744 \uB9CC\uB4E4\uACE0 \uC11C\uB85C\uC758 \uAD00\uC2EC\uC0AC\uB97C \uD655\uC7A5\uD569\uB2C8\uB2E4.";
    }
}
