package com.example.health_check_app.mqtt;

import android.content.Context;
import android.util.Log;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import com.example.health_check_app.models.SensorData;
import org.json.JSONException;
import org.json.JSONObject;

public class MqttManager {
    private static final String TAG = "MqttManager";
    
    // MQTT Broker configuration for Alibaba Cloud IoT
    // These should be replaced with actual credentials
    private static final String MQTT_BROKER = "tcp://iot-instance.aliyuncs.com:1883";
    private static final String CLIENT_ID = "health_check_app_";
    
    // Topics
    private static final String TOPIC_SENSOR_DATA = "sensor/data";
    private static final String TOPIC_DEVICE_COMMAND = "device/command";
    private static final String TOPIC_DEVICE_STATUS = "device/status";
    
    private MqttAndroidClient mqttClient;
    private Context context;
    private MqttConnectionListener connectionListener;
    private MqttDataListener dataListener;
    
    public interface MqttConnectionListener {
        void onConnected();
        void onDisconnected();
        void onConnectionFailed(String error);
    }
    
    public interface MqttDataListener {
        void onSensorDataReceived(SensorData data);
        void onBatteryLevelReceived(int level);
    }
    
    public MqttManager(Context context) {
        this.context = context;
        String clientId = CLIENT_ID + System.currentTimeMillis();
        mqttClient = new MqttAndroidClient(context, MQTT_BROKER, clientId);
        setupCallbacks();
    }
    
    public void setConnectionListener(MqttConnectionListener listener) {
        this.connectionListener = listener;
    }
    
    public void setDataListener(MqttDataListener listener) {
        this.dataListener = listener;
    }
    
    private void setupCallbacks() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "Connection lost: " + cause.getMessage());
                if (connectionListener != null) {
                    connectionListener.onDisconnected();
                }
            }
            
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.d(TAG, "Message arrived from topic: " + topic);
                handleMessage(topic, new String(message.getPayload()));
            }
            
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "Message delivery complete");
            }
        });
    }
    
    public void connect(String username, String password) {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);
            
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Connected to MQTT broker");
                    subscribeToTopics();
                    if (connectionListener != null) {
                        connectionListener.onConnected();
                    }
                }
                
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to connect: " + exception.getMessage());
                    if (connectionListener != null) {
                        connectionListener.onConnectionFailed(exception.getMessage());
                    }
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Exception during connection: " + e.getMessage());
            if (connectionListener != null) {
                connectionListener.onConnectionFailed(e.getMessage());
            }
        }
    }
    
    private void subscribeToTopics() {
        try {
            mqttClient.subscribe(TOPIC_SENSOR_DATA, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscribed to sensor data topic");
                }
                
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to subscribe: " + exception.getMessage());
                }
            });
            
            mqttClient.subscribe(TOPIC_DEVICE_STATUS, 1);
        } catch (MqttException e) {
            Log.e(TAG, "Exception during subscription: " + e.getMessage());
        }
    }
    
    private void handleMessage(String topic, String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            
            if (topic.equals(TOPIC_SENSOR_DATA)) {
                SensorData data = parseSensorData(json);
                if (dataListener != null) {
                    dataListener.onSensorDataReceived(data);
                }
            } else if (topic.equals(TOPIC_DEVICE_STATUS)) {
                if (json.has("battery")) {
                    int batteryLevel = json.getInt("battery");
                    if (dataListener != null) {
                        dataListener.onBatteryLevelReceived(batteryLevel);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
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
    
    public void publishCommand(String command) {
        if (!mqttClient.isConnected()) {
            Log.w(TAG, "Cannot publish: not connected");
            return;
        }
        
        try {
            JSONObject json = new JSONObject();
            json.put("command", command);
            json.put("timestamp", System.currentTimeMillis());
            
            MqttMessage message = new MqttMessage(json.toString().getBytes());
            message.setQos(1);
            message.setRetained(false);
            
            mqttClient.publish(TOPIC_DEVICE_COMMAND, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Command published: " + command);
                }
                
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to publish command: " + exception.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error publishing command: " + e.getMessage());
        }
    }
    
    public void disconnect() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                Log.d(TAG, "Disconnected from MQTT broker");
            } catch (MqttException e) {
                Log.e(TAG, "Error disconnecting: " + e.getMessage());
            }
        }
    }
    
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }
}
