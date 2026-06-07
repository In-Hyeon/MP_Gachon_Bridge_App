package com.example.gachonbridge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LabActivity extends BaseActivity {

    private static final int PAGE_SIZE = 4;
    private static final String PREF_NAME = "lab_favorite_pref";
    private static final String PREF_FAVORITES = "favorite_professors";

    private Button btnTabAi, btnTabBusiness, btnTabChem;
    private Button btnSearchProfessor, btnViewAll, btnViewFavorites;
    private View btnMoreLabs;
    private EditText editSearchProfessor;
    private TextView txtLabSubtitle;
    private LinearLayout labListLayout;

    private List<Lab> currentLabs = new ArrayList<>();
    private int visibleCount = PAGE_SIZE;
    private boolean isSearchMode = false;
    private boolean favoritesOnly = false;

    private SharedPreferences sharedPreferences;
    private Set<String> favoriteProfessorNames = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab);
        bindBottomNavigation(R.id.navLab);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        Set<String> savedFavorites = sharedPreferences.getStringSet(PREF_FAVORITES, null);
        if (savedFavorites != null) favoriteProfessorNames = new HashSet<>(savedFavorites);

        btnTabAi        = findViewById(R.id.btnTabAi);
        btnTabBusiness  = findViewById(R.id.btnTabBusiness);
        btnTabChem      = findViewById(R.id.btnTabChem);
        btnSearchProfessor = findViewById(R.id.btnSearchProfessor);
        btnMoreLabs     = findViewById(R.id.btnMoreLabs);
        btnViewAll      = findViewById(R.id.btnViewAll);
        btnViewFavorites= findViewById(R.id.btnViewFavorites);
        editSearchProfessor = findViewById(R.id.editSearchProfessor);
        txtLabSubtitle  = findViewById(R.id.txtLabSubtitle);
        labListLayout   = findViewById(R.id.labListLayout);

        setActionButtonStyle(btnSearchProfessor);

        btnTabAi.setOnClickListener(v -> showDepartment("AI"));
        btnTabBusiness.setOnClickListener(v -> showDepartment("BUSINESS"));
        btnTabChem.setOnClickListener(v -> showDepartment("CHEM"));
        btnSearchProfessor.setOnClickListener(v -> searchProfessor());
        btnMoreLabs.setOnClickListener(v -> showMoreLabs());

        btnViewAll.setOnClickListener(v -> {
            favoritesOnly = false; isSearchMode = false;
            visibleCount = PAGE_SIZE;
            editSearchProfessor.setText("");
            updateFavoriteModeButtons();
            renderLabs(getLabsByFavoriteMode());
        });

        btnViewFavorites.setOnClickListener(v -> {
            favoritesOnly = true; isSearchMode = false;
            visibleCount = PAGE_SIZE;
            editSearchProfessor.setText("");
            updateFavoriteModeButtons();
            renderLabs(getLabsByFavoriteMode());
        });

        updateFavoriteModeButtons();
        showDepartment("AI");
    }

    private void showDepartment(String department) {
        isSearchMode = false; favoritesOnly = false;
        visibleCount = PAGE_SIZE;
        editSearchProfessor.setText("");

        if ("AI".equals(department)) {
            txtLabSubtitle.setText("가천대학교 AI/SW학부 연구실을 탐색해보세요.");
            setSelectedTab(btnTabAi);
            currentLabs = getAiLabs();
        } else if ("BUSINESS".equals(department)) {
            txtLabSubtitle.setText("가천대학교 경영학과 연구실을 탐색해보세요.");
            setSelectedTab(btnTabBusiness);
            currentLabs = getBusinessLabs();
        } else if ("CHEM".equals(department)) {
            txtLabSubtitle.setText("가천대학교 화학과 연구실을 탐색해보세요.");
            setSelectedTab(btnTabChem);
            currentLabs = getChemLabs();
        }

        updateFavoriteModeButtons();
        renderLabs(getLabsByFavoriteMode());
    }

    private void searchProfessor() {
        String keyword = editSearchProfessor.getText().toString().trim();
        if (keyword.isEmpty()) {
            isSearchMode = false; visibleCount = PAGE_SIZE;
            renderLabs(getLabsByFavoriteMode()); return;
        }
        String nk = keyword.replace(" ", "");
        List<Lab> result = new ArrayList<>();
        for (Lab lab : getLabsByFavoriteMode()) {
            if (lab.professor.contains(keyword) || lab.professor.replace(" ","").contains(nk))
                result.add(lab);
        }
        isSearchMode = true;
        renderLabs(result);
        if (result.isEmpty()) Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
    }

    private void showMoreLabs() {
        List<Lab> target = getLabsByFavoriteMode();
        if (target == null || target.isEmpty()) return;
        isSearchMode = false;
        visibleCount += PAGE_SIZE;
        renderLabs(target);
    }

    private List<Lab> getLabsByFavoriteMode() {
        if (!favoritesOnly) return new ArrayList<>(currentLabs);
        List<Lab> favs = new ArrayList<>();
        for (Lab lab : currentLabs)
            if (favoriteProfessorNames.contains(lab.professor)) favs.add(lab);
        return favs;
    }

    private void toggleFavorite(Lab lab) {
        if (favoriteProfessorNames.contains(lab.professor)) {
            favoriteProfessorNames.remove(lab.professor);
            Toast.makeText(this, lab.professor + " 교수님 즐겨찾기 해제", Toast.LENGTH_SHORT).show();
        } else {
            favoriteProfessorNames.add(lab.professor);
            Toast.makeText(this, lab.professor + " 교수님 즐겨찾기 추가", Toast.LENGTH_SHORT).show();
        }
        saveFavorites();
        String kw = editSearchProfessor.getText().toString().trim();
        if (!kw.isEmpty()) searchProfessor();
        else { isSearchMode = false; renderLabs(getLabsByFavoriteMode()); }
    }

    private void saveFavorites() {
        sharedPreferences.edit().putStringSet(PREF_FAVORITES, new HashSet<>(favoriteProfessorNames)).apply();
    }

    private void updateFavoriteModeButtons() {
        setFavoriteModeButtonStyle(btnViewAll, !favoritesOnly);
        setFavoriteModeButtonStyle(btnViewFavorites, favoritesOnly);
    }

    private void setFavoriteModeButtonStyle(Button button, boolean selected) {
        button.setAllCaps(false); button.setMinWidth(0); button.setMinHeight(0);
        button.setBackgroundTintList(null);
        if (selected) {
            button.setTextColor(Color.parseColor("#111318"));
            button.setBackground(makeRoundRect("#7CADFF", 16));
        } else {
            button.setTextColor(Color.WHITE);
            button.setBackground(makeRoundRectWithStroke("#00000000", "#FFFFFF", 16, 1));
        }
    }

    private void setSelectedTab(Button sel) {
        setTabStyle(btnTabAi, false); setTabStyle(btnTabBusiness, false); setTabStyle(btnTabChem, false);
        setTabStyle(sel, true);
    }

    private void setTabStyle(Button button, boolean selected) {
        button.setAllCaps(false); button.setMinWidth(0); button.setMinHeight(0);
        button.setBackgroundTintList(null);
        if (selected) {
            button.setTextColor(Color.parseColor("#111318"));
            button.setBackground(makeRoundRect("#7CADFF", 18));
        } else {
            button.setTextColor(Color.WHITE);
            button.setBackground(makeRoundRectWithStroke("#00000000", "#FFFFFF", 18, 1));
        }
    }

    private void setActionButtonStyle(Button button) {
        button.setAllCaps(false); button.setMinWidth(0); button.setMinHeight(0);
        button.setBackgroundTintList(null);
        button.setTextColor(Color.parseColor("#111318"));
        button.setBackground(makeRoundRect("#7CADFF", 4));
    }

    private void renderLabs(List<Lab> labs) {
        labListLayout.removeAllViews();
        if (labs == null || labs.isEmpty()) {
            String msg = (favoritesOnly && !isSearchMode) ? "즐겨찾기한 연구실이 없습니다." : "검색 결과가 없습니다.";
            TextView empty = makeText(msg, "#A8A8A8", 14, false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(24), 0, dp(24));
            labListLayout.addView(empty, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            btnMoreLabs.setVisibility(View.GONE); return;
        }
        int count = isSearchMode ? labs.size() : Math.min(visibleCount, labs.size());
        for (int i = 0; i < count; i++) labListLayout.addView(createLabCard(labs.get(i)));
        btnMoreLabs.setVisibility((!isSearchMode && visibleCount < labs.size()) ? View.VISIBLE : View.GONE);
    }

    private View createLabCard(Lab lab) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(makeRoundRect("#1E2029", 12));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0,0,0,dp(14)); card.setLayoutParams(cp);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL); topRow.setGravity(Gravity.TOP);

        TextView avatar = new TextView(this);
        avatar.setText(lab.badge); avatar.setGravity(Gravity.CENTER);
        avatar.setTextColor(Color.parseColor("#7CADFF")); avatar.setTextSize(15);
        avatar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        avatar.setBackground(makeRoundRect("#24262E", 32));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(dp(48),dp(48));
        ap.setMargins(0,0,dp(14),0); topRow.addView(avatar, ap);

        LinearLayout infoBox = new LinearLayout(this);
        infoBox.setOrientation(LinearLayout.VERTICAL);
        infoBox.addView(makeText(lab.labName, "#FFFFFF", 16, true));
        TextView prof = makeText(lab.professor + " / " + lab.position, "#A0A8C0", 13, false);
        prof.setPadding(0,dp(4),0,0); infoBox.addView(prof);
        TextView res = makeText(lab.research, "#6E7A9A", 12, false);
        res.setPadding(0,dp(4),0,0); infoBox.addView(res);
        topRow.addView(infoBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView star = new TextView(this);
        star.setText(favoriteProfessorNames.contains(lab.professor) ? "★" : "☆");
        star.setTextColor(favoriteProfessorNames.contains(lab.professor) ? Color.parseColor("#F6C344") : Color.parseColor("#6E7A9A"));
        star.setTextSize(24); star.setGravity(Gravity.CENTER);
        star.setOnClickListener(v -> toggleFavorite(lab));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(36),dp(36));
        sp.setMargins(dp(6),0,0,0); topRow.addView(star, sp);

        card.addView(topRow);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#2E3040"));
        LinearLayout.LayoutParams dp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        dp2.setMargins(0,dp(12),0,dp(10)); card.addView(divider, dp2);

        addInfoRow(card, "연락처", lab.phone);
        addInfoRow(card, "이메일", lab.email);
        addInfoRow(card, "홈페이지", lab.homepage);
        addInfoRow(card, "위치", lab.location);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams brp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        brp.setMargins(0,dp(12),0,0);

        Button detailBtn = new Button(this);
        detailBtn.setText("상세보기"); detailBtn.setTextSize(12);
        detailBtn.setTextColor(Color.parseColor("#111318")); detailBtn.setAllCaps(false);
        detailBtn.setMinWidth(0); detailBtn.setMinHeight(0);
        detailBtn.setBackground(makeRoundRect("#7CADFF", 6));
        detailBtn.setOnClickListener(v -> openHomepage(lab.homepage));
        btnRow.addView(detailBtn, new LinearLayout.LayoutParams(dp(100), dp(38)));
        card.addView(btnRow, brp);

        return card;
    }

    private void addInfoRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rp.setMargins(0,dp(5),0,0);
        TextView lv = makeText(label, "#6E7A9A", 12, true);
        lv.setMinWidth(dp(60));
        TextView vv = makeText(value, "#A0A8C0", 12, false);
        vv.setSingleLine(false);
        row.addView(lv, new LinearLayout.LayoutParams(dp(66), LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(vv, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        parent.addView(row, rp);
    }

    private TextView makeText(String text, String color, int sizeSp, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text); tv.setTextColor(Color.parseColor(color)); tv.setTextSize(sizeSp);
        tv.setLineSpacing(dp(2), 1.0f);
        if (bold) tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return tv;
    }

    private void openHomepage(String homepage) {
        if (homepage == null || homepage.trim().isEmpty() || "-".equals(homepage.trim())) {
            Toast.makeText(this, "등록된 홈페이지가 없습니다.", Toast.LENGTH_SHORT).show(); return;
        }
        String url = homepage.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://" + url;
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(this, "홈페이지를 열 수 없습니다.", Toast.LENGTH_SHORT).show(); }
    }

    private GradientDrawable makeRoundRect(String color, int r) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE); d.setColor(Color.parseColor(color)); d.setCornerRadius(dp(r)); return d;
    }

    private GradientDrawable makeRoundRectWithStroke(String color, String stroke, int r, int sw) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE); d.setColor(Color.parseColor(color));
        d.setCornerRadius(dp(r)); d.setStroke(dp(sw), Color.parseColor(stroke)); return d;
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private List<Lab> getAiLabs() {
        return Arrays.asList(
                new Lab("강","강상우 교수 연구실","강상우","부교수","자연어처리, LLM, 대화형 QA 시스템","031-750-8669","swkang@gachon.ac.kr","https://sites.google.com/view/kangsangwoo","AI관 419호"),
                new Lab("구","구자경 교수 연구실","구자경","조교수","계산 이미징","031-750-6967","jakeoung@gachon.ac.kr","https://sites.google.com/view/jakeoung","AI관 433호"),
                new Lab("노","노웅기 교수 연구실","노웅기","교수","대용량 데이터 마이닝, 유전자 데이터베이스","031-750-5349","wkloh2@gachon.ac.kr","http://professor.gachon.ac.kr/main/main.jsp?Forum_seq=88524","AI관 422호"),
                new Lab("민","민홍 교수 연구실","민홍","교수","시스템소프트웨어, 운영체제, 사물인터넷","031-750-5828","hmin@gachon.ac.kr","https://sites.google.com/view/hmin","AI관 431호"),
                new Lab("안","안종현 교수 연구실","안종현","조교수 / 대학원주임교수","자율주행, 인공지능, 기계학습, 센서퓨전","031-750-5332","jhonghyun@gachon.ac.kr","https://sites.google.com/view/vip-lab","AI관 740호"),
                new Lab("엄","엄광현 교수 연구실","엄광현","조교수","의료 인공지능, 의료 영상 분석, 정밀의료","031-750-8850","khuhm@gachon.ac.kr","https://sites.google.com/view/aimhi-lab","AI관 737호"),
                new Lab("오","오현영 교수 연구실","오현영","조교수","시스템 보안, 하드웨어 기반 보안, AI 보안","031-750-5504","hyoh@gachon.ac.kr","https://sites.google.com/view/hyoh","AI관 741호"),
                new Lab("유","유준 교수 연구실","유준","교수","자율주행자동차 통신, 클라우드 데이터센터 통신","031-750-5832","joon.yoo@gachon.ac.kr","https://sites.google.com/view/winl","AI관 423호"),
                new Lab("이","이상웅 교수 연구실","이상웅","교수","패턴인식, 컴퓨터비전, 뇌과학","031-750-6918","slee@gachon.ac.kr","http://pr.gachon.ac.kr","AI관 416호"),
                new Lab("이","이주형 교수 연구실","이주형","부교수","사물인터넷, 엣지/클라우드 컴퓨팅, 머신러닝","031-750-6968","j17.lee@gachon.ac.kr","https://sites.google.com/view/imeslab/main","AI관 424호"),
                new Lab("정","정옥란 교수 연구실","정옥란","교수","빅데이터, 소셜미디어마이닝, 머신러닝","031-750-5831","orjeong@gachon.ac.kr","http://ida.gachon.ac.kr","AI관 425호"),
                new Lab("정","정용주 교수 연구실","정용주","부교수","컴퓨터비전, 영상처리","031-750-8658","yjung@gachon.ac.kr","https://sites.google.com/site/gachoncvip/","AI관 430호"),
                new Lab("정","정윤현 교수 연구실","정윤현","부교수","컴퓨터 그래픽스, 혼합현실, 머신러닝, 의료 영상","031-750-4771","younhyun.jung@gachon.ac.kr","https://gcu-vomlab.github.io/VOM/index.html","AI관 421호"),
                new Lab("조","조정찬 교수 연구실","조정찬","부교수 / 인공지능,소프트웨어전공 학과장","컴퓨터 비전, 딥러닝, 머신러닝","031-750-5328","thinkai@gachon.ac.kr","https://sites.google.com/view/visual-ai/","AI관 429호"),
                new Lab("조","조풍진 교수 연구실","조풍진","조교수","데이터 사이언스, 금융공학","031-750-5353","pjcho@gachon.ac.kr","https://sites.google.com/view/fdslab","AI관 417호"),
                new Lab("조","조해민 교수 연구실","조해민","조교수","로보틱스, 로봇 지능 제어, 자율주행 기술","031-750-5320","hmcho9@gachon.ac.kr","https://hxxmin.github.io/rai-lab/","AI관 736호"),
                new Lab("최","최아영 교수 연구실","최아영","부교수","인간 컴퓨터 상호작용, 헬스케어, 감성인식","031-750-8656","aychoi@gachon.ac.kr","https://sites.google.com/site/ahyoungchoi00/home","AI관 434호"),
                new Lab("최","최재영 교수 연구실","최재영","부교수","통계적 추론, 데이터마이닝, 강화학습","031-750-5829","jychoi19@gachon.ac.kr","https://sites.google.com/view/jaeyoungchoi/home","AI관 420호"),
                new Lab("최","최재용 교수 연구실","최재용","조교수","지능형 로봇, 무인 시스템, 멀티모달 AI","031-750-8667","andrewjchoi@gachon.ac.kr","https://sites.google.com/view/irasc","AI관 428호"),
                new Lab("최","최재혁 교수 연구실","최재혁","교수","차세대 통신/네트워크, 지능형시스템, AIoT","031-750-8657","jchoi@gachon.ac.kr","https://sites.google.com/gachon.ac.kr/inclab","AI관 435호")
        );
    }

    private List<Lab> getBusinessLabs() {
        return Arrays.asList(
                new Lab("강","강승완 교수 연구실","강승완","교수","인사조직","031-750-5219","global7@gachon.ac.kr","https://www.gachon.ac.kr/biz/4446/subview.do","가천관 806호"),
                new Lab("곽","곽찬희 교수 연구실","곽찬희","부교수","경영공학, 데이터 분석, 정보시스템","031-750-5538","chkwak@gachon.ac.kr","https://www.gachon.ac.kr/biz/4446/subview.do","가천관 506호"),
                new Lab("김","김동엽 교수 연구실","김동엽","부교수","경영공학","031-750-5216","학과 페이지 참조","https://www.gachon.ac.kr/biz/4446/subview.do","가천관 704호"),
                new Lab("김","김산월 교수 연구실","김산월","교수","생성형 AI, 디지털 전환, ESG, 기업재무","031-750-5757","jsyrena0923@gachon.ac.kr","https://sites.google.com/gachon.ac.kr/jsy","가천관 609호"),
                new Lab("김","김수 교수 연구실","김수","조교수","인사조직","031-750-5231","학과 페이지 참조","https://www.gachon.ac.kr/biz/4446/subview.do","중앙도서관 407호"),
                new Lab("김","김한얼 교수 연구실","김한얼","교수","경영관리","031-750-5221","학과 페이지 참조","https://www.gachon.ac.kr/biz/4446/subview.do","가천관 808호"),
                new Lab("김","김홍범 교수 연구실","김홍범","조교수","기술혁신, 운영관리, IT 전략, 산업정책","031-750-5176","hkim@gachon.ac.kr","https://sites.google.com/view/hongbumkim/home","가천관 707호"),
                new Lab("김","김희진 교수 연구실","김희진","부교수","마케팅","031-750-5201","heejinkim@gachon.ac.kr","https://www.gachon.ac.kr/biz/4446/subview.do","가천관 932호"),
                new Lab("서","서정대 교수 연구실","서정대","교수","생산운영관리","031-750-5369","jdsuh@gachon.ac.kr","https://www.gachon.ac.kr/biz/4446/subview.do","가천관 809호"),
                new Lab("심","심형섭 교수 연구실","심형섭","부교수","경영과학","031-750-5530","학과 페이지 참조","https://www.gachon.ac.kr/biz/4446/subview.do","가천관 805호"),
                new Lab("엄","엄금철 교수 연구실","엄금철","부교수","생성형 AI, Human-Machine Interaction, 추천시스템","031-750-5181","yanjz@gachon.ac.kr","https://sites.google.com/gachon.ac.kr/yanjz/home","가천관 711호"),
                new Lab("왕","왕맹맹 교수 연구실","왕맹맹","조교수","빅데이터 분석, 디지털 비즈니스 전환","031-750-8744","bizmwang@gachon.ac.kr","https://sites.google.com/view/bizmwang","중앙도서관 432호")
        );
    }

    private List<Lab> getChemLabs() {
        return Arrays.asList(
                new Lab("강","강종백 교수 연구실","강종백","교수","생화학","031-750-5409","학과 페이지 참조","https://www.gachon.ac.kr/chemistry/6378/subview.do","반도체대학 6-7호"),
                new Lab("김","김명종 교수 연구실","김명종","교수","나노사이언스, BNNT, 에너지 저장/촉매 소재","031-750-8721","myungjongkim@gachon.ac.kr","https://snclweb.github.io/gachon/index.html","바이오나노연구원 514호"),
                new Lab("김","김연호 교수 연구실","김연호","부교수","광촉매, CO2 전환, 차세대 이차전지 전극 소재","031-750-8558","yeonho@gachon.ac.kr","https://sites.google.com/view/acemlab","예술체육대학1 4-10"),
                new Lab("김","김효나 교수 연구실","김효나","조교수","화학","031-750-8553","학과 페이지 참조","https://www.gachon.ac.kr/chemistry/6378/subview.do","반도체대학 5-23호"),
                new Lab("손","손상준 교수 연구실","손상준","교수","생물유기","031-750-8756","학과 페이지 참조","https://www.gachon.ac.kr/chemistry/6378/subview.do","미래1관 210호"),
                new Lab("송","송하영 교수 연구실","송하영","조교수","합성 무기화학, 유기금속, 초분자","031-750-5410","학과 페이지 참조","https://sites.google.com/view/hayoungsong/","바이오나노연구원 211호"),
                new Lab("안","안태양 교수 연구실","안태양","조교수","화학생물학, 바이오소재","031-750-8559","suns2000@gachon.ac.kr","https://sites.google.com/view/anlab/","예술체육대학1 3-9"),
                new Lab("이","이상훈 교수 연구실","이상훈","교수","고분자화학, 에너지 재료화학","031-750-8836","학과 페이지 참조","https://www.gachon.ac.kr/chemistry/6378/subview.do","바이오나노연구원 미래1관 306호"),
                new Lab("이","이충환 교수 연구실","이충환","조교수","유기합성, 유기광촉매, 유기전자소재","031-750-8826","clee@gachon.ac.kr","https://sites.google.com/view/cwleegroup/","반도체대학 5-9"),
                new Lab("장","장창현 교수 연구실","장창현","교수","나노화학, 액정 기반 바이오센서, 표면화학","031-750-8555","학과 페이지 참조","https://www.gachon.ac.kr/chemistry/6378/subview.do","바이오나노연구원 511호"),
                new Lab("한","한상윤 교수 연구실","한상윤","교수","질량분석 및 이온화학","031-750-8720","학과 페이지 참조","https://www.gachon.ac.kr/chemistry/6378/subview.do","바이오나노연구원 213호"),
                new Lab("홍","홍완표 교수 연구실","홍완표","부교수","유기합성","031-750-8529","wphongw@gachon.ac.kr","https://www.gachon.ac.kr/chemistry/6378/subview.do","반도체대학 5-10")
        );
    }

    static class Lab {
        String badge, labName, professor, position, research, phone, email, homepage, location;
        Lab(String badge, String labName, String professor, String position, String research,
            String phone, String email, String homepage, String location) {
            this.badge=badge; this.labName=labName; this.professor=professor; this.position=position;
            this.research=research; this.phone=phone; this.email=email; this.homepage=homepage; this.location=location;
        }
    }
}