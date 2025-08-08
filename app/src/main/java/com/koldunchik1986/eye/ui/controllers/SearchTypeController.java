package com.koldunchik1986.eye.ui.controllers;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.koldunchik1986.eye.util.AppConstants;

public class SearchTypeController {
    public interface TypeDetector {
        String detect(String query);
        String formatDisplay(String type);
    }

    private final EditText editTextQuery;
    private final TextView textViewType;
    private final TypeDetector detector;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pending;

    public SearchTypeController(EditText editTextQuery, TextView textViewType, TypeDetector detector) {
        this.editTextQuery = editTextQuery;
        this.textViewType = textViewType;
        this.detector = detector;
    }

    public void bind() {
        editTextQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (pending != null) handler.removeCallbacks(pending);

                if (query.isEmpty()) {
                    textViewType.setText("Тип запроса не определён");
                    textViewType.setVisibility(View.GONE);
                    return;
                }

                pending = () -> {
                    String type = detector.detect(query);
                    String display = detector.formatDisplay(type);
                    textViewType.setText(display);
                    textViewType.setVisibility(View.VISIBLE);
                };
                handler.postDelayed(pending, AppConstants.SEARCH_TYPE_HINT_DELAY_MS);
            }
        });
    }
}