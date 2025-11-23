package com.example.health_check_app.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import com.example.health_check_app.models.SensorData;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothManager {
    private static final String TAG = "BluetoothManager";
    
    // Standard Serial Port Profile UUID
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Thread workerThread;
    private volatile boolean isConnected = false;
    private volatile boolean stopWorker = false;
    
    private BluetoothConnectionListener connectionListener;
    private BluetoothDataListener dataListener;
    private Handler mainHandler;
    
    public interface BluetoothConnectionListener {
        void onConnected();
        void onDisconnected();
        void onConnectionFailed(String error);
    }
    
    public interface BluetoothDataListener {
        void onSensorDataReceived(SensorData data);
        void onBatteryLevelReceived(int level);
    }
    
    public BluetoothManager(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Log if Bluetooth is not available
        if (this.bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth is not available on this device");
        }
    }
    
    public void setConnectionListener(BluetoothConnectionListener listener) {
        this.connectionListener = listener;
    }
    
    public void setDataListener(BluetoothDataListener listener) {
        this.dataListener = listener;
    }
    
    public boolean isBluetoothAvailable() {
        return bluetoothAdapter != null;
    }
    
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
    
    @SuppressLint("MissingPermission")
    public Set<BluetoothDevice> getPairedDevices() {
        if (!hasBluetoothPermissions()) {
            Log.e(TAG, "Missing Bluetooth permissions");
            return null;
        }
        
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.getBondedDevices();
        }
        return null;
    }
    
    private boolean hasBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) 
                   == PackageManager.PERMISSION_GRANTED;
        } else {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) 
                   == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    @SuppressLint("MissingPermission")
    public void connect(BluetoothDevice device) {
        if (!hasBluetoothPermissions()) {
            notifyConnectionFailed("缺少蓝牙权限");
            return;
        }
        
        // Disconnect if already connected
        if (isConnected) {
            disconnect();
        }
        
        new Thread(() -> {
            try {
                Log.d(TAG, "Attempting to connect to device: " + device.getName());
                
                // Create a socket connection
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                
                // Cancel discovery to improve connection speed
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                
                // Connect to the device
                bluetoothSocket.connect();
                
                // Get input and output streams
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
                
                isConnected = true;
                notifyConnected();
                
                // Start listening for incoming data
                startListening();
                
            } catch (IOException e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
                notifyConnectionFailed("连接失败: " + e.getMessage());
                cleanup();
            }
        }).start();
    }
    
    @SuppressLint("MissingPermission")
    public void connect(String deviceAddress) {
        if (!hasBluetoothPermissions()) {
            notifyConnectionFailed("缺少蓝牙权限");
            return;
        }
        
        if (bluetoothAdapter == null) {
            notifyConnectionFailed("蓝牙不可用");
            return;
        }
        
        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connect(device);
        } catch (IllegalArgumentException e) {
            notifyConnectionFailed("无效的设备地址");
        }
    }
    
    private void startListening() {
        stopWorker = false;
        workerThread = new Thread(() -> {
            StringBuilder messageBuffer = new StringBuilder();
            int braceDepth = 0;
            boolean inJson = false;
            
            while (!Thread.currentThread().isInterrupted() && !stopWorker && isConnected) {
                try {
                    // Read character by character until we get a complete JSON message
                    int data = inputStream.read();
                    if (data == -1) {
                        // End of stream
                        break;
                    }
                    
                    char character = (char) data;
                    
                    // Check for JSON object boundaries with brace depth tracking
                    if (character == '{') {
                        if (!inJson) {
                            messageBuffer = new StringBuilder();
                            inJson = true;
                            braceDepth = 0;
                        }
                        braceDepth++;
                        messageBuffer.append(character);
                    } else if (character == '}') {
                        messageBuffer.append(character);
                        braceDepth--;
                        
                        if (braceDepth == 0 && inJson) {
                            String message = messageBuffer.toString();
                            
                            // Process the complete JSON message
                            if (message.startsWith("{")) {
                                handleReceivedData(message);
                            }
                            
                            messageBuffer = new StringBuilder();
                            inJson = false;
                        }
                    } else if (inJson) {
                        messageBuffer.append(character);
                    }
                    
                } catch (IOException e) {
                    if (isConnected && !stopWorker) {
                        Log.e(TAG, "Error reading data: " + e.getMessage());
                        notifyDisconnected();
                        isConnected = false;
                    }
                    break;
                }
            }
        });
        workerThread.start();
    }
    
    private void handleReceivedData(String data) {
        try {
            JSONObject json = new JSONObject(data);
            
            // Check if this is sensor data or status data
            if (json.has("heartRate") || json.has("bloodOxygen") || json.has("bodyTemperature")) {
                SensorData sensorData = parseSensorData(json);
                notifyDataReceived(sensorData);
            } else if (json.has("battery") && !json.has("heartRate")) {
                int batteryLevel = json.getInt("battery");
                notifyBatteryReceived(batteryLevel);
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage() + ", Data: " + data);
        }
    }
    
    private SensorData parseSensorData(JSONObject json) throws JSONException {
        SensorData data = new SensorData();
        
        if (json.has("heartRate")) {
            data.setHeartRate(json.getInt("heartRate"));
        }
        if (json.has("bloodOxygen")) {
            data.setBloodOxygen(json.getInt("bloodOxygen"));
        }
        if (json.has("bodyTemperature")) {
            data.setBodyTemperature((float) json.getDouble("bodyTemperature"));
        }
        if (json.has("environmentTemperature")) {
            data.setEnvironmentTemperature((float) json.getDouble("environmentTemperature"));
        }
        if (json.has("humidity")) {
            data.setHumidity(json.getInt("humidity"));
        }
        if (json.has("motionStatus")) {
            String status = json.getString("motionStatus");
            data.setMotionStatus(parseMotionStatus(status));
        }
        if (json.has("steps")) {
            data.setSteps(json.getInt("steps"));
        }
        if (json.has("battery")) {
            data.setBatteryLevel(json.getInt("battery"));
        }
        
        return data;
    }
    
    private SensorData.MotionStatus parseMotionStatus(String status) {
        switch (status.toUpperCase()) {
            case "WALKING":
                return SensorData.MotionStatus.WALKING;
            case "FALL":
            case "FALL_DETECTED":
                return SensorData.MotionStatus.FALL_DETECTED;
            case "SEDENTARY":
            default:
                return SensorData.MotionStatus.SEDENTARY;
        }
    }
    
    public void sendCommand(String command) {
        if (!isConnected || outputStream == null) {
            Log.w(TAG, "Cannot send command: not connected");
            return;
        }
        
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("command", command);
                json.put("timestamp", System.currentTimeMillis());
                
                String message = json.toString() + "\n";
                outputStream.write(message.getBytes());
                outputStream.flush();
                
                Log.d(TAG, "Command sent: " + command);
                
            } catch (Exception e) {
                Log.e(TAG, "Error sending command: " + e.getMessage());
            }
        }).start();
    }
    
    public void disconnect() {
        stopWorker = true;
        isConnected = false;
        
        cleanup();
        notifyDisconnected();
    }
    
    private void cleanup() {
        try {
            if (workerThread != null) {
                workerThread.interrupt();
                workerThread = null;
            }
            
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error during cleanup: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return isConnected && bluetoothSocket != null && bluetoothSocket.isConnected();
    }
    
    private void notifyConnected() {
        mainHandler.post(() -> {
            if (connectionListener != null) {
                connectionListener.onConnected();
            }
        });
    }
    
    private void notifyDisconnected() {
        mainHandler.post(() -> {
            if (connectionListener != null) {
                connectionListener.onDisconnected();
            }
        });
    }
    
    private void notifyConnectionFailed(String error) {
        mainHandler.post(() -> {
            if (connectionListener != null) {
                connectionListener.onConnectionFailed(error);
            }
        });
    }
    
    private void notifyDataReceived(SensorData data) {
        mainHandler.post(() -> {
            if (dataListener != null) {
                dataListener.onSensorDataReceived(data);
            }
        });
    }
    
    private void notifyBatteryReceived(int level) {
        mainHandler.post(() -> {
            if (dataListener != null) {
                dataListener.onBatteryLevelReceived(level);
            }
        });
    }
}
