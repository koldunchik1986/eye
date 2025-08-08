package com.koldunchik1986.eye.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.koldunchik1986.eye.util.AppConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingDialogFragment extends DialogFragment {
    public interface OnMappedListener {
        void onMapped(Map<String, Integer> mapping);
        void onCancelled();
    }

    private static final String ARG_HEADERS = "arg_headers";

    private OnMappedListener listener;

    public static MappingDialogFragment newInstance(String[] headers, OnMappedListener listener) {
        MappingDialogFragment fragment = new MappingDialogFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_HEADERS, headers);
        fragment.setArguments(args);
        fragment.setOnMappedListener(listener);
        return fragment;
    }

    public void setOnMappedListener(OnMappedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String[] headers = getArguments() != null ? getArguments().getStringArray(ARG_HEADERS) : new String[0];

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(
                AppConstants.DIALOG_PADDING_LR_PX,
                AppConstants.DIALOG_PADDING_TB_PX,
                AppConstants.DIALOG_PADDING_LR_PX,
                AppConstants.DIALOG_PADDING_TB_PX
        );

        List<EditText> editTexts = new ArrayList<>();
        if (headers != null) {
            for (String header : headers) {
                TextView tv = new TextView(requireContext());
                tv.setText(getString(com.koldunchik1986.eye.R.string.mapping_field_prefix) + header);
                layout.addView(tv);

                EditText et = new EditText(requireContext());
                et.setHint(com.koldunchik1986.eye.R.string.mapping_hint);
                et.setText(inferKey(header));
                layout.addView(et);
                editTexts.add(et);
            }
        }

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.addView(layout);

        return new AlertDialog.Builder(requireContext())
                .setTitle(com.koldunchik1986.eye.R.string.mapping_title)
                .setView(scrollView)
                .setPositiveButton(com.koldunchik1986.eye.R.string.btn_ok, (d, w) -> {
                    Map<String, Integer> mapping = new HashMap<>();
                    if (headers != null) {
                        for (int i = 0; i < headers.length; i++) {
                            String key = editTexts.get(i).getText().toString().trim().toLowerCase();
                            if (!key.isEmpty()) {
                                mapping.put(key, i);
                            }
                        }
                    }
                    if (listener != null) listener.onMapped(mapping);
                })
                .setNegativeButton(com.koldunchik1986.eye.R.string.btn_cancel, (d, w) -> {
                    if (listener != null) listener.onCancelled();
                })
                .create();
    }

    private String inferKey(String header) {
        String h = header == null ? "" : header.toLowerCase();
        if (h.contains("tel") || h.contains("phone")) return "tel";
        if (h.contains("mail") || h.contains("email")) return "email";
        if (h.contains("name") || h.contains("fio") || h.contains("фио")) return "name";
        if (h.contains("tg") || h.contains("telegram")) return "tg_id";
        return h;
    }
}