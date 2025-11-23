package com.example.health_check_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.health_check_app.bluetooth.BluetoothManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
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
    
    private BluetoothManager bluetoothManager;

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
        
        // Initialize Bluetooth manager
        bluetoothManager = new BluetoothManager(this);
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
            scanBluetoothDevices();
        });
        
        wifiConfigButton.setOnClickListener(v -> {
            // WiFi configuration is not needed for Bluetooth-only implementation
            Toast.makeText(this, "使用蓝牙连接，无需WiFi配置", Toast.LENGTH_SHORT).show();
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
    
    private void scanBluetoothDevices() {
        // Check if Bluetooth is available
        if (!bluetoothManager.isBluetoothAvailable()) {
            Toast.makeText(this, "此设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if Bluetooth is enabled
        if (!bluetoothManager.isBluetoothEnabled()) {
            // Request to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (hasBluetoothPermissions()) {
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                requestBluetoothPermissions();
            }
            return;
        }
        
        // Check for permissions
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions();
            return;
        }
        
        // Get paired devices
        showPairedDevices();
    }
    
    private boolean hasBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                   == PackageManager.PERMISSION_GRANTED &&
                   ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                   == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) 
                   == PackageManager.PERMISSION_GRANTED &&
                   ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                   == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                },
                REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }
    
    @SuppressLint("MissingPermission")
    private void showPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevices();
        
        if (pairedDevices == null || pairedDevices.isEmpty()) {
            Toast.makeText(this, "未找到已配对的蓝牙设备\n请先在系统设置中配对设备", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Create list of device names and addresses
        ArrayList<String> deviceList = new ArrayList<>();
        final ArrayList<BluetoothDevice> devices = new ArrayList<>();
        
        for (BluetoothDevice device : pairedDevices) {
            String deviceName = device.getName();
            String deviceAddress = device.getAddress();
            deviceList.add((deviceName != null ? deviceName : "Unknown") + "\n" + deviceAddress);
            devices.add(device);
        }
        
        // Show dialog with device list
        new AlertDialog.Builder(this)
            .setTitle("选择蓝牙设备")
            .setItems(deviceList.toArray(new String[0]), (dialog, which) -> {
                BluetoothDevice selectedDevice = devices.get(which);
                connectToDevice(selectedDevice);
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        Toast.makeText(this, "正在连接到 " + device.getName() + "...", Toast.LENGTH_SHORT).show();
        
        // Save the device address for auto-reconnect
        android.content.SharedPreferences prefs = getSharedPreferences("HealthCheckSettings", MODE_PRIVATE);
        prefs.edit().putString("connectedDeviceAddress", device.getAddress()).apply();
        
        // Connect to the device
        bluetoothManager.setConnectionListener(new BluetoothManager.BluetoothConnectionListener() {
            @Override
            public void onConnected() {
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "已连接到设备", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onDisconnected() {
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "设备已断开", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onConnectionFailed(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "连接失败: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
        
        bluetoothManager.connect(device);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                scanBluetoothDevices();
            } else {
                Toast.makeText(this, "需要蓝牙权限才能扫描设备", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                scanBluetoothDevices();
            } else {
                Toast.makeText(this, "需要启用蓝牙才能扫描设备", Toast.LENGTH_SHORT).show();
            }
        }
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
