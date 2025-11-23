package com.example.health_check_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.health_check_app.models.SensorData;
import com.example.health_check_app.bluetooth.BluetoothManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    
    private TextView heartRateValue;
    private TextView heartRateStatus;
    private TextView bloodOxygenValue;
    private TextView bodyTemperatureValue;
    private TextView environmentTemperature;
    private TextView environmentHumidity;
    private TextView motionStatusValue;
    private TextView stepsValue;
    private TextView connectionStatus;
    private ImageView connectionIcon;
    private TextView batteryLevel;
    private TextView heartRateIcon;
    
    private FloatingActionButton startMeasureFab;
    private BottomNavigationView bottomNavigation;
    
    private SensorData currentData;
    private boolean isConnected = false;
    private Handler uiUpdateHandler;
    private Runnable uiUpdateRunnable;
    
    private BluetoothManager bluetoothManager;
    
    // Thresholds (will be loaded from preferences in SettingsActivity)
    private int heartRateMaxThreshold = 100;
    private float temperatureMaxThreshold = 37.3f;
    private boolean vibrationEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupListeners();
        setupUIUpdater();
        setupBluetooth();
        
        // Initialize with default data
        currentData = new SensorData();
        updateUI(currentData);
    }
    
    private void initializeViews() {
        heartRateValue = findViewById(R.id.heartRateValue);
        heartRateStatus = findViewById(R.id.heartRateStatus);
        bloodOxygenValue = findViewById(R.id.bloodOxygenValue);
        bodyTemperatureValue = findViewById(R.id.bodyTemperatureValue);
        environmentTemperature = findViewById(R.id.environmentTemperature);
        environmentHumidity = findViewById(R.id.environmentHumidity);
        motionStatusValue = findViewById(R.id.motionStatusValue);
        stepsValue = findViewById(R.id.stepsValue);
        connectionStatus = findViewById(R.id.connectionStatus);
        connectionIcon = findViewById(R.id.connectionIcon);
        batteryLevel = findViewById(R.id.batteryLevel);
        heartRateIcon = findViewById(R.id.heartRateIcon);
        
        startMeasureFab = findViewById(R.id.startMeasureFab);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }
    
    private void setupListeners() {
        startMeasureFab.setOnClickListener(v -> {
            // Send command to microcontroller to start measurement
            startMeasurement();
        });
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_monitor) {
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
    
    private void setupUIUpdater() {
        uiUpdateHandler = new Handler(Looper.getMainLooper());
        uiUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                // UI updates are now handled by MQTT callbacks
                // This handler is kept for potential periodic updates
                
                // Schedule next update
                uiUpdateHandler.postDelayed(this, 1000);
            }
        };
    }
    
    private void setupBluetooth() {
        bluetoothManager = new BluetoothManager(this);
        
        bluetoothManager.setConnectionListener(new BluetoothManager.BluetoothConnectionListener() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> {
                    updateConnectionStatus(true);
                });
            }
            
            @Override
            public void onDisconnected() {
                runOnUiThread(() -> {
                    updateConnectionStatus(false);
                });
            }
            
            @Override
            public void onConnectionFailed(String error) {
                runOnUiThread(() -> {
                    updateConnectionStatus(false);
                    Toast.makeText(MainActivity.this, "连接失败: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
        
        bluetoothManager.setDataListener(new BluetoothManager.BluetoothDataListener() {
            @Override
            public void onSensorDataReceived(SensorData data) {
                runOnUiThread(() -> {
                    currentData = data;
                    updateUI(data);
                    checkThresholds(data);
                });
            }
            
            @Override
            public void onBatteryLevelReceived(int level) {
                runOnUiThread(() -> {
                    currentData.setBatteryLevel(level);
                    batteryLevel.setText(String.format("%d%%", level));
                });
            }
        });
        
        // Auto-connect to saved device if available
        loadAndConnectDevice();
    }
    
    private void startMeasurement() {
        if (!isConnected) {
            Toast.makeText(this, "设备未连接", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "开始测量...", Toast.LENGTH_SHORT).show();
        // Send Bluetooth command to microcontroller
        bluetoothManager.sendCommand("START_MEASURE");
    }
    
    private void updateUI(SensorData data) {
        // Heart Rate
        if (data.getHeartRate() > 0) {
            heartRateValue.setText(String.valueOf(data.getHeartRate()));
            updateHeartRateStatus(data.getHeartRate());
        } else {
            heartRateValue.setText("--");
        }
        
        // Blood Oxygen
        if (data.getBloodOxygen() > 0) {
            bloodOxygenValue.setText(String.valueOf(data.getBloodOxygen()));
        } else {
            bloodOxygenValue.setText("--");
        }
        
        // Body Temperature
        if (data.getBodyTemperature() > 0) {
            bodyTemperatureValue.setText(String.format("%.1f", data.getBodyTemperature()));
        } else {
            bodyTemperatureValue.setText("--");
        }
        
        // Environment
        if (data.getEnvironmentTemperature() > 0) {
            environmentTemperature.setText(String.format("%.1f°C", data.getEnvironmentTemperature()));
        }
        if (data.getHumidity() > 0) {
            environmentHumidity.setText(String.format("%d%%", data.getHumidity()));
        }
        
        // Motion Status
        if (data.getMotionStatus() != null) {
            switch (data.getMotionStatus()) {
                case SEDENTARY:
                    motionStatusValue.setText(R.string.sedentary);
                    motionStatusValue.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    break;
                case WALKING:
                    motionStatusValue.setText(R.string.walking);
                    motionStatusValue.setTextColor(ContextCompat.getColor(this, R.color.status_normal));
                    break;
                case FALL_DETECTED:
                    motionStatusValue.setText(R.string.fall_detected);
                    motionStatusValue.setTextColor(ContextCompat.getColor(this, R.color.status_danger));
                    break;
            }
        }
        
        // Steps
        stepsValue.setText(String.format("%d 步数", data.getSteps()));
        
        // Battery
        if (data.getBatteryLevel() > 0) {
            batteryLevel.setText(String.format("%d%%", data.getBatteryLevel()));
        }
    }
    
    private void updateHeartRateStatus(int heartRate) {
        if (heartRate < 60) {
            heartRateStatus.setText(R.string.too_slow);
            heartRateStatus.setTextColor(ContextCompat.getColor(this, R.color.status_warning));
        } else if (heartRate > heartRateMaxThreshold) {
            heartRateStatus.setText(R.string.too_fast);
            heartRateStatus.setTextColor(ContextCompat.getColor(this, R.color.status_danger));
        } else {
            heartRateStatus.setText(R.string.normal);
            heartRateStatus.setTextColor(ContextCompat.getColor(this, R.color.status_normal));
        }
    }
    
    private void checkThresholds(SensorData data) {
        // Check for fall detection
        if (data.getMotionStatus() == SensorData.MotionStatus.FALL_DETECTED) {
            showAlertDialog(getString(R.string.alert_title), getString(R.string.alert_fall));
            if (vibrationEnabled) {
                vibratePhone();
            }
            // Send command to microcontroller to trigger buzzer
            bluetoothManager.sendCommand("ALARM_FALL");
        }
        
        // Check for high temperature
        if (data.getBodyTemperature() > temperatureMaxThreshold) {
            showAlertDialog(getString(R.string.alert_title), getString(R.string.alert_fever));
            if (vibrationEnabled) {
                vibratePhone();
            }
            // Send command to microcontroller to trigger buzzer
            bluetoothManager.sendCommand("ALARM_FEVER");
        }
        
        // Check for high heart rate
        if (data.getHeartRate() > heartRateMaxThreshold) {
            showAlertDialog(getString(R.string.alert_title), getString(R.string.alert_high_heart_rate));
            if (vibrationEnabled) {
                vibratePhone();
            }
            // Send command to microcontroller to trigger buzzer
            bluetoothManager.sendCommand("ALARM_HEART_RATE");
        }
    }
    
    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show();
    }
    
    private void vibratePhone() {
        android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(500);
        }
    }
    
    private void updateConnectionStatus(boolean connected) {
        isConnected = connected;
        if (connected) {
            connectionStatus.setText(R.string.connected);
            connectionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_connected));
            connectionIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_connected));
            Toast.makeText(this, R.string.device_ready, Toast.LENGTH_SHORT).show();
        } else {
            connectionStatus.setText(R.string.disconnected);
            connectionStatus.setTextColor(ContextCompat.getColor(this, R.color.status_disconnected));
            connectionIcon.setColorFilter(ContextCompat.getColor(this, R.color.status_disconnected));
        }
    }
    
    private void loadSettings() {
        android.content.SharedPreferences prefs = getSharedPreferences("HealthCheckSettings", MODE_PRIVATE);
        heartRateMaxThreshold = prefs.getInt("heartRateMax", 100);
        temperatureMaxThreshold = prefs.getFloat("temperatureMax", 37.3f);
        vibrationEnabled = prefs.getBoolean("vibrationFeedback", true);
    }
    
    private void loadAndConnectDevice() {
        android.content.SharedPreferences prefs = getSharedPreferences("HealthCheckSettings", MODE_PRIVATE);
        String savedDeviceAddress = prefs.getString("connectedDeviceAddress", null);
        
        if (savedDeviceAddress != null && bluetoothManager.isBluetoothEnabled()) {
            // Auto-connect to the previously connected device
            bluetoothManager.connect(savedDeviceAddress);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_monitor);
        loadSettings();
        // Start UI updates
        uiUpdateHandler.post(uiUpdateRunnable);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop UI updates
        uiUpdateHandler.removeCallbacks(uiUpdateRunnable);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothManager != null) {
            bluetoothManager.disconnect();
        }
    }
}
