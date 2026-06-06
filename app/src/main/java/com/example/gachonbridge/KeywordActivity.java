package com.example.gachonbridge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeywordActivity extends BaseActivity {

    private EditText editKeywordInput;
    private RecyclerView recyclerView;
    private TextView textNoKeywords;
    private KeywordAdapter adapter;
    private List<String> keywordList = new ArrayList<>();
    
    private static final String PREF_NAME = "keyword_prefs";
    private static final String KEY_KEYWORDS = "user_keywords";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyword);
        bindBottomNavigation(R.id.navNotice);
        
        editKeywordInput = findViewById(R.id.editKeywordInput);
        recyclerView = findViewById(R.id.keywordRecyclerView);
        textNoKeywords = findViewById(R.id.textNoKeywords);
        
        findViewById(R.id.buttonKeywordBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.buttonAddKeyword).setOnClickListener(v -> addKeyword());

        loadKeywords();
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new KeywordAdapter();
        recyclerView.setAdapter(adapter);
        
        updateEmptyView();
    }

    private void loadKeywords() {
        Set<String> saved = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getStringSet(KEY_KEYWORDS, new HashSet<>());
        keywordList = new ArrayList<>(saved);
    }

    private void saveKeywords() {
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_KEYWORDS, new HashSet<>(keywordList))
                .apply();
        
        NoticeWorker.schedulePeriodicWork(this);
    }

    private void addKeyword() {
        String keyword = editKeywordInput.getText().toString().trim();
        if (keyword.isEmpty()) return;
        
        if (keywordList.contains(keyword)) {
            Toast.makeText(this, "이미 등록된 키워드입니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        keywordList.add(0, keyword);
        saveKeywords();
        NoticeWorker.runImmediateCheck(this); // Trigger immediate check
        adapter.notifyItemInserted(0);
        editKeywordInput.setText("");
        updateEmptyView();
        Toast.makeText(this, "키워드가 등록되었습니다. 백그라운드에서 감지를 시작합니다.", Toast.LENGTH_SHORT).show();
    }

    private void deleteKeyword(int position) {
        keywordList.remove(position);
        saveKeywords();
        adapter.notifyItemRemoved(position);
        updateEmptyView();
    }

    private void updateEmptyView() {
        textNoKeywords.setVisibility(keywordList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    class KeywordAdapter extends RecyclerView.Adapter<KeywordAdapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_keyword, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            String keyword = keywordList.get(position);
            h.textName.setText(keyword);
            h.btnDelete.setOnClickListener(v -> deleteKeyword(position));
        }

        @Override
        public int getItemCount() {
            return keywordList.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView textName;
            ImageView btnDelete;
            VH(View v) {
                super(v);
                textName = v.findViewById(R.id.textKeywordName);
                btnDelete = v.findViewById(R.id.buttonDeleteKeyword);
            }
        }
    }
}
