# 蓝牙通信集成指南

本文档提供了如何将单片机系统与Android应用通过蓝牙进行集成的详细说明。

## 硬件要求

### Android端
- Android 7.0 (API 24) 或更高版本
- 支持蓝牙的设备
- 建议使用Android 12+以获得最佳兼容性

### 单片机端
- Arduino/ESP32或其他支持串口通信的MCU
- HC-05或HC-06蓝牙模块
- 传感器模块：
  - MAX30102 (心率/血氧)
  - DS18B20 (体温)
  - DHT11/DHT22 (温湿度)
  - MPU6050 (运动检测)
- 蜂鸣器（用于报警）

## 蓝牙模块配置

### HC-05/HC-06 初始配置

1. **连接AT模式**（仅HC-05需要）
   ```
   VCC → 3.3V
   GND → GND
   TXD → RX (MCU)
   RXD → TX (MCU)
   KEY → 3.3V (进入AT模式)
   ```

2. **配置波特率**
   ```
   AT+UART=9600,0,0    # 设置波特率为9600
   或
   AT+UART=115200,0,0  # 设置波特率为115200
   ```

3. **设置设备名称**
   ```
   AT+NAME=HealthGuard  # 设置蓝牙名称
   ```

4. **设置配对密码**（可选）
   ```
   AT+PSWD=1234  # 设置密码为1234
   ```

## 单片机端代码实现

### 1. 基础串口配置

```cpp
// Arduino示例
void setup() {
  // 初始化串口（与蓝牙模块通信）
  Serial.begin(9600);  // 或115200，与蓝牙模块配置一致
  
  // 初始化传感器
  initSensors();
}
```

### 2. JSON数据发送

```cpp
#include <ArduinoJson.h>

void sendSensorData() {
  StaticJsonDocument<256> doc;
  
  // 读取传感器数据
  doc["heartRate"] = readHeartRate();
  doc["bloodOxygen"] = readBloodOxygen();
  doc["bodyTemperature"] = readBodyTemp();
  doc["environmentTemperature"] = readEnvTemp();
  doc["humidity"] = readHumidity();
  doc["motionStatus"] = getMotionStatus();
  doc["steps"] = getStepCount();
  doc["battery"] = getBatteryLevel();
  doc["timestamp"] = millis();
  
  // 序列化并发送
  serializeJson(doc, Serial);
  Serial.println();  // 重要：发送换行符
}
```

### 3. 接收命令

```cpp
void loop() {
  // 检查是否有命令
  if (Serial.available() > 0) {
    String command = Serial.readStringUntil('\n');
    processCommand(command);
  }
  
  // 定期发送数据（每秒一次）
  static unsigned long lastSend = 0;
  if (millis() - lastSend >= 1000) {
    sendSensorData();
    lastSend = millis();
  }
}

void processCommand(String jsonStr) {
  StaticJsonDocument<128> doc;
  DeserializationError error = deserializeJson(doc, jsonStr);
  
  if (error) {
    return;  // 解析失败
  }
  
  String cmd = doc["command"];
  
  if (cmd == "START_MEASURE") {
    startMeasurement();
  } else if (cmd == "ALARM_FALL") {
    triggerBuzzer(1000);  // 响1秒
  } else if (cmd == "ALARM_FEVER") {
    triggerBuzzer(2000);  // 响2秒
  } else if (cmd == "ALARM_HEART_RATE") {
    triggerBuzzer(1500);  // 响1.5秒
  }
}
```

### 4. 完整示例

```cpp
#include <ArduinoJson.h>
#include <Wire.h>

// 蜂鸣器引脚
#define BUZZER_PIN 9

// 传感器变量
int heartRate = 0;
int bloodOxygen = 0;
float bodyTemp = 0.0;
float envTemp = 0.0;
int humidity = 0;
String motionStatus = "SEDENTARY";
int steps = 0;
int battery = 100;

void setup() {
  Serial.begin(9600);
  
  // 初始化I2C
  Wire.begin();
  
  // 初始化传感器
  // initMAX30102();
  // initDS18B20();
  // initDHT();
  // initMPU6050();
  
  // 蜂鸣器引脚
  pinMode(BUZZER_PIN, OUTPUT);
}

void loop() {
  // 处理接收的命令
  handleCommands();
  
  // 定期发送传感器数据
  static unsigned long lastSend = 0;
  if (millis() - lastSend >= 1000) {
    sendSensorData();
    lastSend = millis();
  }
  
  // 读取传感器（根据实际情况调整频率）
  updateSensorData();
}

void handleCommands() {
  if (Serial.available() > 0) {
    String jsonStr = Serial.readStringUntil('\n');
    
    StaticJsonDocument<128> doc;
    DeserializationError error = deserializeJson(doc, jsonStr);
    
    if (!error) {
      String cmd = doc["command"];
      processCommand(cmd);
    }
  }
}

void sendSensorData() {
  StaticJsonDocument<256> doc;
  
  doc["heartRate"] = heartRate;
  doc["bloodOxygen"] = bloodOxygen;
  doc["bodyTemperature"] = bodyTemp;
  doc["environmentTemperature"] = envTemp;
  doc["humidity"] = humidity;
  doc["motionStatus"] = motionStatus;
  doc["steps"] = steps;
  doc["battery"] = battery;
  doc["timestamp"] = millis();
  
  serializeJson(doc, Serial);
  Serial.println();
}

void processCommand(String cmd) {
  if (cmd == "START_MEASURE") {
    // 开始测量逻辑
  } else if (cmd == "ALARM_FALL") {
    triggerBuzzer(1000);
  } else if (cmd == "ALARM_FEVER") {
    triggerBuzzer(2000);
  } else if (cmd == "ALARM_HEART_RATE") {
    triggerBuzzer(1500);
  }
}

void triggerBuzzer(int duration) {
  digitalWrite(BUZZER_PIN, HIGH);
  delay(duration);
  digitalWrite(BUZZER_PIN, LOW);
}

void updateSensorData() {
  // 实际实现中，这里应该读取真实的传感器数据
  // heartRate = readMAX30102_HeartRate();
  // bloodOxygen = readMAX30102_SpO2();
  // bodyTemp = readDS18B20();
  // envTemp = readDHT_Temperature();
  // humidity = readDHT_Humidity();
  // motionStatus = analyzeMotion();
  // steps = getStepCount();
  // battery = readBatteryLevel();
}
```

## Android端使用指南

### 1. 权限配置

在 `AndroidManifest.xml` 中已配置必要的权限：
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### 2. 配对蓝牙设备

在使用应用之前：
1. 打开手机蓝牙设置
2. 搜索附近的蓝牙设备
3. 找到 "HealthGuard" 或您设置的设备名称
4. 点击配对（如果设置了密码，输入密码）

### 3. 在应用中连接

1. 打开应用
2. 进入"设置"页面
3. 点击"扫描设备"按钮
4. 从列表中选择已配对的设备
5. 等待连接成功提示

### 4. 测试连接

连接成功后：
- 顶部状态栏应显示"已连接"（绿色）
- 开始接收传感器数据
- 数值会实时更新

## 故障排除

### 问题1: 无法找到蓝牙设备

**解决方案:**
- 确保蓝牙模块已通电
- 检查蓝牙模块LED是否闪烁
- 确保设备未被其他应用连接
- 在系统蓝牙设置中先配对设备

### 问题2: 连接失败

**解决方案:**
- 检查波特率是否匹配
- 确认蓝牙模块工作正常
- 重启蓝牙模块和应用
- 在系统设置中取消配对后重新配对

### 问题3: 接收不到数据

**解决方案:**
- 检查单片机串口输出
- 使用串口调试工具验证数据格式
- 确认JSON格式正确（包含换行符）
- 检查数据发送频率（建议1Hz）

### 问题4: 数据解析错误

**解决方案:**
- 确保JSON格式正确
- 检查字段名称是否匹配
- 验证数据类型（整数、浮点数）
- 查看Android日志输出

## 数据格式规范

### 传感器数据 (MCU → App)
```json
{
  "heartRate": 75,           // 整数，单位BPM
  "bloodOxygen": 98,          // 整数，百分比
  "bodyTemperature": 36.5,    // 浮点数，单位℃
  "environmentTemperature": 25.0,  // 浮点数，单位℃
  "humidity": 60,             // 整数，百分比
  "motionStatus": "SEDENTARY", // 字符串: SEDENTARY/WALKING/FALL_DETECTED
  "steps": 1234,              // 整数
  "battery": 85,              // 整数，百分比
  "timestamp": 1637500000000  // 长整数，毫秒时间戳
}
```

### 控制命令 (App → MCU)
```json
{
  "command": "START_MEASURE",  // 字符串
  "timestamp": 1637500000000   // 长整数
}
```

## 性能建议

1. **数据发送频率**: 建议1Hz (每秒一次)，避免过于频繁导致卡顿
2. **JSON大小**: 保持在256字节以内，确保传输效率
3. **缓冲区管理**: 及时清空串口缓冲区，避免数据积压
4. **错误处理**: 实现超时和重传机制
5. **电源管理**: 合理使用睡眠模式节省电量

## 参考资源

- [HC-05 蓝牙模块文档](https://www.electronicwings.com/sensors-modules/hc-05-bluetooth-module)
- [ArduinoJson 库文档](https://arduinojson.org/)
- [Android Bluetooth 官方文档](https://developer.android.com/guide/topics/connectivity/bluetooth)

## 技术支持

如遇到问题，请检查：
1. 硬件连接是否正确
2. 蓝牙模块配置是否正确
3. 代码中的波特率设置
4. JSON数据格式
5. Android日志输出
