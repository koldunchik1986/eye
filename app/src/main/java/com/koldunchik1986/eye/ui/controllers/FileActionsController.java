package com.koldunchik1986.eye.ui.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileActionsController {
    private final Context context;
    private final CsvFileRepository repository;
    private final File csvDir;

    private final Button buttonShowFiles;
    private final ListView listViewFiles;
    private final LinearLayout layoutFileActions;
    private final Button buttonDeleteFile;
    private final Button buttonRenameFile;
    private final Button buttonShareFile;

    private File selectedFile;

    public FileActionsController(Context context,
                                 CsvFileRepository repository,
                                 File csvDir,
                                 Button buttonShowFiles,
                                 ListView listViewFiles,
                                 LinearLayout layoutFileActions,
                                 Button buttonDeleteFile,
                                 Button buttonRenameFile,
                                 Button buttonShareFile) {
        this.context = context;
        this.repository = repository;
        this.csvDir = csvDir;
        this.buttonShowFiles = buttonShowFiles;
        this.listViewFiles = listViewFiles;
        this.layoutFileActions = layoutFileActions;
        this.buttonDeleteFile = buttonDeleteFile;
        this.buttonRenameFile = buttonRenameFile;
        this.buttonShareFile = buttonShareFile;
    }

    public void bind() {
        buttonShowFiles.setOnClickListener(v -> showFilesList());
        buttonDeleteFile.setOnClickListener(v -> onDelete());
        buttonRenameFile.setOnClickListener(v -> onRename());
        buttonShareFile.setOnClickListener(v -> onShare());
    }

    public void refreshList() {
        showFilesList();
    }

    private void onDelete() {
        if (selectedFile == null) return;
        new AlertDialog.Builder(context)
                .setTitle(com.koldunchik1986.eye.R.string.delete_title)
                .setMessage(context.getString(com.koldunchik1986.eye.R.string.delete_message, selectedFile.getName()))
                .setPositiveButton(com.koldunchik1986.eye.R.string.btn_yes, (d, w) -> {
                    boolean deleted = repository.delete(selectedFile);
                    if (deleted) {
                        Toast.makeText(context, com.koldunchik1986.eye.R.string.file_deleted, Toast.LENGTH_SHORT).show();
                        selectedFile = null;
                        layoutFileActions.setVisibility(View.GONE);
                        showFilesList();
                    } else {
                        Toast.makeText(context, com.koldunchik1986.eye.R.string.delete_error, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(com.koldunchik1986.eye.R.string.btn_no, null)
                .show();
    }

    private void onRename() {
        if (selectedFile == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(com.koldunchik1986.eye.R.string.rename_title);
        final EditText input = new EditText(context);
        input.setText(selectedFile.getName());
        builder.setView(input);
        builder.setPositiveButton("OK", (d, w) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty() || !newName.toLowerCase().endsWith(com.koldunchik1986.eye.util.AppConstants.EXT_CSV)) {
                Toast.makeText(context, com.koldunchik1986.eye.R.string.rename_hint_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            File newFile = new File(csvDir, newName);
            if (newFile.exists()) {
                Toast.makeText(context, com.koldunchik1986.eye.R.string.file_exists, Toast.LENGTH_SHORT).show();
                return;
            }
            boolean renamed = repository.rename(selectedFile, newFile);
            if (renamed) {
                Toast.makeText(context, com.koldunchik1986.eye.R.string.renamed_ok, Toast.LENGTH_SHORT).show();
                selectedFile = newFile;
                showFilesList();
            } else {
                Toast.makeText(context, com.koldunchik1986.eye.R.string.rename_error, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(com.koldunchik1986.eye.R.string.btn_cancel, null);
        builder.show();
    }

    private void onShare() {
        if (selectedFile == null) return;
        Uri fileUri = CsvFileRepository.getShareUri(context, selectedFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, context.getString(com.koldunchik1986.eye.R.string.share_title)));
    }

    private void showFilesList() {
        List<File> filesList = repository.listCsvFiles();
        if (filesList.isEmpty()) {
            Toast.makeText(context, "Нет файлов", Toast.LENGTH_SHORT).show();
            listViewFiles.setAdapter(null);
            listViewFiles.setVisibility(View.GONE);
            layoutFileActions.setVisibility(View.GONE);
            selectedFile = null;
            return;
        }

        List<String> fileNames = new ArrayList<>();
        for (File f : filesList) fileNames.add(f.getName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, fileNames);
        listViewFiles.setAdapter(adapter);
        listViewFiles.setVisibility(View.VISIBLE);
        layoutFileActions.setVisibility(View.GONE);
        selectedFile = null;

        listViewFiles.setOnItemClickListener((parent, view, position, id) -> {
            selectedFile = filesList.get(position);
            layoutFileActions.setVisibility(View.VISIBLE);
        });
    }
}