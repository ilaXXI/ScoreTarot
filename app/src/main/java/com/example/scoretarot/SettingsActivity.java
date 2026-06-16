package com.example.scoretarot;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView infoText = findViewById(R.id.text_settings_info);
        infoText.setText(R.string.settings_about);

        Button syncButton = findViewById(R.id.button_test_sync);
        syncButton.setOnClickListener(v -> Toast.makeText(this, ExternalDatabaseStub.sync(this), Toast.LENGTH_LONG).show());
    }
}