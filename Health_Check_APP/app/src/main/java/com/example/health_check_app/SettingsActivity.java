package com.example.health_check_app;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {
    
    private Button scanDevicesButton;
    private Button wifiConfigButton;
    private SeekBar heartRateMaxSeekBar;
    private SeekBar temperatureMaxSeekBar;
    private TextView heartRateMaxValue;
    private TextView temperatureMaxValue;
    private SwitchMaterial sedentaryReminderSwitch;
    private SwitchMaterial vibrationFeedbackSwitch;
    
    private int heartRateMax = 100;
    private float temperatureMax = 37.3f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initializeViews();
        setupListeners();
        loadSettings();
    }
    
    private void initializeViews() {
        scanDevicesButton = findViewById(R.id.scanDevicesButton);
        wifiConfigButton = findViewById(R.id.wifiConfigButton);
        heartRateMaxSeekBar = findViewById(R.id.heartRateMaxSeekBar);
        temperatureMaxSeekBar = findViewById(R.id.temperatureMaxSeekBar);
        heartRateMaxValue = findViewById(R.id.heartRateMaxValue);
        temperatureMaxValue = findViewById(R.id.temperatureMaxValue);
        sedentaryReminderSwitch = findViewById(R.id.sedentaryReminderSwitch);
        vibrationFeedbackSwitch = findViewById(R.id.vibrationFeedbackSwitch);
    }
    
    private void setupListeners() {
        scanDevicesButton.setOnClickListener(v -> {
            // In real implementation, this would scan for Bluetooth devices
            Toast.makeText(this, "正在扫描蓝牙设备...", Toast.LENGTH_SHORT).show();
            // scanBluetoothDevices();
        });
        
        wifiConfigButton.setOnClickListener(v -> {
            // In real implementation, this would open WiFi configuration
            Toast.makeText(this, "WiFi配置功能", Toast.LENGTH_SHORT).show();
            // openWifiConfig();
        });
        
        heartRateMaxSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                heartRateMax = progress + 60; // Min value 60
                heartRateMaxValue.setText(String.valueOf(heartRateMax));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveSettings();
            }
        });
        
        temperatureMaxSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                temperatureMax = 35.0f + (progress / 10.0f); // Min 35.0, increments of 0.1
                temperatureMaxValue.setText(String.format("%.1f", temperatureMax));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveSettings();
            }
        });
        
        sedentaryReminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings();
            Toast.makeText(this, 
                isChecked ? "久坐提醒已开启" : "久坐提醒已关闭", 
                Toast.LENGTH_SHORT).show();
        });
        
        vibrationFeedbackSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSettings();
            Toast.makeText(this, 
                isChecked ? "震动反馈已开启" : "震动反馈已关闭", 
                Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadSettings() {
        // Load from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("HealthCheckSettings", MODE_PRIVATE);
        
        heartRateMax = prefs.getInt("heartRateMax", 100);
        temperatureMax = prefs.getFloat("temperatureMax", 37.3f);
        
        heartRateMaxSeekBar.setProgress(heartRateMax - 60);
        temperatureMaxSeekBar.setProgress((int)((temperatureMax - 35.0f) * 10));
        
        heartRateMaxValue.setText(String.valueOf(heartRateMax));
        temperatureMaxValue.setText(String.format("%.1f", temperatureMax));
        
        sedentaryReminderSwitch.setChecked(prefs.getBoolean("sedentaryReminder", false));
        vibrationFeedbackSwitch.setChecked(prefs.getBoolean("vibrationFeedback", true));
    }
    
    private void saveSettings() {
        android.content.SharedPreferences prefs = getSharedPreferences("HealthCheckSettings", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        
        editor.putInt("heartRateMax", heartRateMax);
        editor.putFloat("temperatureMax", temperatureMax);
        editor.putBoolean("sedentaryReminder", sedentaryReminderSwitch.isChecked());
        editor.putBoolean("vibrationFeedback", vibrationFeedbackSwitch.isChecked());
        
        editor.apply();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
