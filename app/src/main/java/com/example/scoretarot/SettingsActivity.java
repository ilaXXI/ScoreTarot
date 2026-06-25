package com.example.scoretarot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView infoText = findViewById(R.id.text_settings_info);
        infoText.setText(R.string.settings_about);

        Button langButton = findViewById(R.id.button_switch_lang);
        langButton.setOnClickListener(v -> showLanguageDialog());

        Button syncButton = findViewById(R.id.button_test_sync);
        syncButton.setOnClickListener(v -> {
            String msg = ExternalDatabaseManager.sync(this);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });

        Button clearButton = findViewById(R.id.button_clear_data);
        clearButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.clear_data_title)
                    .setMessage(R.string.clear_data_message)
                    .setPositiveButton(R.string.clear_data_positive, (dialog, which) -> {
                        GameDatabase.getInstance(this).clearDatabase();
                        Toast.makeText(this, R.string.clear_data_success, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    private void showLanguageDialog() {
        String[] languages = {"Français", "English"};
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_lang_dialog_title)
                .setItems(languages, (dialog, which) -> {
                    String langCode = (which == 0) ? "fr" : "en";
                    setLocale(langCode);
                })
                .show();
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        android.content.res.Resources res = getResources();
        android.content.res.Configuration config = res.getConfiguration();
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());

        // Redémarrer l'application pour appliquer les changements partout
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
