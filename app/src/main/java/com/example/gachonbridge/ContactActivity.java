package com.example.gachonbridge;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends BaseActivity {

    static class Contact {
        String section;
        String name;
        String location;
        String phone;

        Contact(String section, String name, String location, String phone) {
            this.section  = section;
            this.name     = name;
            this.location = location;
            this.phone    = phone;
        }
    }

    private static final List<Contact> ALL_CONTACTS = new ArrayList<Contact>() {{

        // ── 교무처 ──
        add(new Contact("교무처", "계절학기",      null, "031-750-5727"));
        add(new Contact("교무처", "졸업/융합전공",  null, "031-750-5048"));
        add(new Contact("교무처", "이수구분",       null, "031-750-4790"));
        add(new Contact("교무처", "휴학/복학/자퇴", null, "031-750-5726"));
        add(new Contact("교무처", "전과/유급/편입", null, "031-750-5581"));
        add(new Contact("교무처", "제적/재입학",    null, "031-750-5046"));

        // ── 기획처 ──
        add(new Contact("기획처", "P-학기제 지원", null, "031-750-5213"));
        add(new Contact("기획처", "P-학기제 운영", null, "031-750-5212"));

        // ── 학생복지처 ──
        add(new Contact("학생복지처", "학생회/중앙동아리", null, "031-750-5055"));
        add(new Contact("학생복지처", "학생생활관",         null, "031-750-5054"));
        add(new Contact("학생복지처", "한마음원정대",        null, "031-750-5052"));
        add(new Contact("학생복지처", "총학생회/간부장학금", null, "031-750-5053"));
        add(new Contact("학생복지처", "학군단",              null, "031-750-5437"));
        add(new Contact("학생복지처", "교내장학금",          null, "031-750-5056"));
        add(new Contact("학생복지처", "교내장학금",          null, "031-750-5058"));
        add(new Contact("학생복지처", "국가장학금",          null, "031-750-5059"));

        // ── 취업진로처 ──
        add(new Contact("취업진로처", "진로취업 비교과",      null, "031-750-5833"));
        add(new Contact("취업진로처", "대학일자리플러스센터", null, "031-750-5990"));
        add(new Contact("취업진로처", "대학일자리플러스센터", null, "031-750-5836"));
        add(new Contact("취업진로처", "진로/취업 컨설팅",     null, "031-750-4787"));
        add(new Contact("취업진로처", "취업동아리",           null, "031-750-4788"));
        add(new Contact("취업진로처", "취업동아리/현장실습",  null, "031-750-4551"));
        add(new Contact("취업진로처", "현장실습 운영",        null, "031-750-5992"));

        // ── 총무처 ──
        add(new Contact("총무처", "복지매장 관리", null, "031-750-5065"));
        add(new Contact("총무처", "코로나 대응",   null, "031-750-5064"));

        // ── 국제교류처 ──
        add(new Contact("국제교류처", "하와이 엘리트코스",  null, "031-750-5558"));
        add(new Contact("국제교류처", "졸업인증/토익캠프",  null, "031-750-5624"));
        add(new Contact("국제교류처", "하와이 프리미엄코스", null, "031-750-2682"));
        add(new Contact("국제교류처", "해외파견/교환학생",  null, "031-750-2686"));
        add(new Contact("국제교류처", "외국어능력 졸업인증", null, "031-750-5642"));
        add(new Contact("국제교류처", "그룹스터디",         null, "031-750-5646"));

        // ── 교육혁신원 ──
        add(new Contact("교육혁신원", "학습지원",             null, "031-750-5741"));
        add(new Contact("교육혁신원", "학습지원",             null, "031-750-5754"));
        add(new Contact("교육혁신원", "아르테크네 창업지원",  null, "031-750-8952"));
        add(new Contact("교육혁신원", "아르테크네 서포터즈",  null, "031-750-6934"));
        add(new Contact("교육혁신원", "아르테크네 Bottom Up", null, "031-750-6961"));
        add(new Contact("교육혁신원", "아르테크네 경진대회",  null, "031-750-6936"));
        add(new Contact("교육혁신원", "인권침해 고충상담",    null, "031-750-8965"));

        // ── 중앙도서관 ──
        add(new Contact("중앙도서관", "창업대학/코코네스쿨", null, "031-750-4541"));
        add(new Contact("중앙도서관", "상호대차",            null, "031-750-4707"));
        add(new Contact("중앙도서관", "도서 배달",           null, "031-750-5313"));

        // ── 산학협력단 ──
        add(new Contact("산학협력단", "학생인건비", null, "031-750-5432"));

        // ── 연대본부 ──
        add(new Contact("연대본부", "예비군/민방위", null, "031-750-5075"));
        add(new Contact("연대본부", "ROTC",          null, "031-750-5436"));

        // ── 공학교육혁신센터 ──
        add(new Contact("공학교육혁신센터", "공학교육인증", null, "031-750-8931"));

        // ── 학생생활관 ──
        add(new Contact("학생생활관", "생활관 관리",    null, "031-724-4577"));
        add(new Contact("학생생활관", "2기숙 야간 사감", null, "031-724-4561"));
        add(new Contact("학생생활관", "가천고시관",     null, "031-724-4569"));

        // ── 총학생회 ──
        add(new Contact("총학생회", "총학생회", null, "031-750-5445"));

        // ── 대외협력처 ──
        add(new Contact("대외협력처", "마스코트", null, "031-750-5827"));

        // ── 메디컬 캠퍼스 ──
        add(new Contact("메디컬 캠퍼스", "강좌개설/수강신청",   null, "032-820-4052"));
        add(new Contact("메디컬 캠퍼스", "의과대학 행정", null, "032-458-2520"));
        add(new Contact("메디컬 캠퍼스", "의과대학 연구/국제",  null, "032-458-2518"));
        add(new Contact("메디컬 캠퍼스", "의학교육 과정운영",   null, "032-458-2521"));
        add(new Contact("메디컬 캠퍼스", "ASK센터",             null, "032-458-2538"));
        add(new Contact("메디컬 캠퍼스", "간호대학 행정",        null, "032-820-4097"));
        add(new Contact("메디컬 캠퍼스", "약학대학 행정",        null, "032-820-4820"));
        add(new Contact("메디컬 캠퍼스", "평생교육원",           null, "032-820-4111"));
    }};

    private String currentFilter = "전체";
    private String currentQuery  = "";
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        bindBottomNavigation(R.id.navContact);

        RecyclerView recyclerView = findViewById(R.id.contactRecyclerView);
        EditText      etSearch    = findViewById(R.id.contactEtSearch);
        LinearLayout  chipGroup   = findViewById(R.id.contactChipGroup);

        adapter = new ContactAdapter(getFilteredList());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buildChips(chipGroup);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                currentQuery = s.toString().trim();
                adapter.updateData(getFilteredList());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void buildChips(LinearLayout chipGroup) {
        List<String> sections = new ArrayList<>();
        sections.add("전체");
        for (Contact c : ALL_CONTACTS) {
            if (!sections.contains(c.section)) sections.add(c.section);
        }

        for (String sec : sections) {
            TextView chip = new TextView(this);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    dp(42)
            );
            lp.setMarginEnd(dp(14));
            chip.setLayoutParams(lp);

            chip.setText(sec);
            applyContactChipStyle(chip, sec.equals(currentFilter));

            chip.setOnClickListener(v -> {
                currentFilter = sec;
                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                    View child = chipGroup.getChildAt(i);
                    if (child instanceof TextView) {
                        String label = ((TextView) child).getText().toString();
                        applyContactChipStyle((TextView) child, label.equals(sec));
                    }
                }
                adapter.updateData(getFilteredList());
            });

            chipGroup.addView(chip);
        }
    }

    private void applyContactChipStyle(TextView chip, boolean active) {
        chip.setMinWidth(dp(78));
        chip.setGravity(android.view.Gravity.CENTER);
        chip.setTextSize(14f);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setSingleLine(true);
        chip.setPadding(dp(20), 0, dp(20), 0);
        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setBackgroundResource(active ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
        chip.setTextColor(ContextCompat.getColor(
                this,
                active ? R.color.gb_on_primary : R.color.gb_on_surface
        ));
    }
//    private void setChipState(TextView chip, boolean active) {
//        chip.setBackgroundResource(active ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
//        chip.setTextColor(ContextCompat.getColor(
//                this,
//                active ? R.color.gb_on_primary : R.color.gb_on_surface));
//    }

    private List<Contact> getFilteredList() {
        List<Contact> result = new ArrayList<>();
        for (Contact c : ALL_CONTACTS) {
            boolean sectionMatch = currentFilter.equals("전체") || c.section.equals(currentFilter);
            boolean queryMatch   = currentQuery.isEmpty()
                    || c.name.contains(currentQuery)
                    || c.section.contains(currentQuery)
                    || c.phone.contains(currentQuery);
            if (sectionMatch && queryMatch) result.add(c);
        }
        return result;
    }

    class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.VH> {

        private List<Contact> list;

        ContactAdapter(List<Contact> list) { this.list = list; }

        void updateData(List<Contact> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_contact_card, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Contact contact = list.get(pos);

            if (pos == 0 || !list.get(pos - 1).section.equals(contact.section)) {
                h.tvSectionHeader.setVisibility(View.VISIBLE);
                h.tvSectionHeader.setText(contact.section);
            } else {
                h.tvSectionHeader.setVisibility(View.GONE);
            }

            h.tvName.setText(contact.name);
            h.tvPhone.setText("전화  " + contact.phone);

            h.btnCall.setOnClickListener(v -> {
                String uri = "tel:" + contact.phone.replaceAll("[^0-9+]", "");
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(uri));
                startActivity(intent);
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvSectionHeader, tvName, tvPhone;
            ImageView btnCall;

            VH(View v) {
                super(v);
                tvSectionHeader = v.findViewById(R.id.tvContactSectionHeader);
                tvName          = v.findViewById(R.id.tvContactName);
                tvPhone         = v.findViewById(R.id.tvContactPhone);
                btnCall         = v.findViewById(R.id.btnContactCall);
            }
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
