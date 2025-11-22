# 集成指南 - Integration Guide

## 快速开始

### 1. 阿里云IoT配置

#### 创建产品和设备
1. 登录阿里云IoT平台控制台
2. 创建产品（产品类型：直连设备）
3. 创建设备，获取三元组信息：
   - ProductKey
   - DeviceName  
   - DeviceSecret

#### 生成MQTT连接参数
```
Broker: ${ProductKey}.iot-as-mqtt.cn-shanghai.aliyuncs.com:1883
ClientID: ${ClientId}|securemode=3,signmethod=hmacsha1|
Username: ${DeviceName}&${ProductKey}
Password: sign_hmac(DeviceSecret, content)
```

详细算法参考：https://help.aliyun.com/document_detail/73742.html

### 2. 配置App

#### 方法一：代码配置（快速测试）
编辑 `app/src/main/java/com/example/health_check_app/mqtt/MqttManager.java`

```java
// 替换为你的MQTT Broker地址
private static final String MQTT_BROKER = "tcp://YOUR_PRODUCT_KEY.iot-as-mqtt.cn-shanghai.aliyuncs.com:1883";
```

在 `MainActivity.java` 的 `setupMqtt()` 方法中启用连接：
```java
// 取消注释并填入你的认证信息
mqttManager.connect("YOUR_USERNAME", "YOUR_PASSWORD");
```

#### 方法二：通过设置页面配置（推荐）
未来可以在SettingsActivity中添加MQTT配置界面，让用户输入：
- Broker地址
- 用户名
- 密码

### 3. 单片机端配置

#### ESP8266/ESP32 MQTT客户端配置

```cpp
#include <PubSubClient.h>
#include <ESP8266WiFi.h>

// WiFi配置
const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

// MQTT配置
const char* mqtt_server = "YOUR_PRODUCT_KEY.iot-as-mqtt.cn-shanghai.aliyuncs.com";
const int mqtt_port = 1883;
const char* mqtt_user = "YOUR_USERNAME";
const char* mqtt_password = "YOUR_PASSWORD";

WiFiClient espClient;
PubSubClient client(espClient);

void setup() {
  Serial.begin(115200);
  
  // 连接WiFi
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  // 配置MQTT
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();
  
  // 定期发送传感器数据
  publishSensorData();
  delay(500);
}

void reconnect() {
  while (!client.connected()) {
    if (client.connect("ESP8266Client", mqtt_user, mqtt_password)) {
      Serial.println("Connected to MQTT");
      client.subscribe("device/command");
    } else {
      delay(5000);
    }
  }
}

void callback(char* topic, byte* payload, unsigned int length) {
  String message = "";
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];
  }
  
  // 解析JSON命令
  if (message.indexOf("START_MEASURE") >= 0) {
    startMeasurement();
  } else if (message.indexOf("ALARM") >= 0) {
    triggerBuzzer();
  }
}

void publishSensorData() {
  // 读取传感器数据
  int heartRate = readHeartRate();
  int bloodOxygen = readBloodOxygen();
  float bodyTemp = readBodyTemperature();
  float envTemp = readEnvironmentTemperature();
  int humidity = readHumidity();
  String motionStatus = getMotionStatus();
  int steps = getSteps();
  int battery = getBatteryLevel();
  
  // 构建JSON
  String json = "{";
  json += "\"heartRate\":" + String(heartRate) + ",";
  json += "\"bloodOxygen\":" + String(bloodOxygen) + ",";
  json += "\"bodyTemperature\":" + String(bodyTemp) + ",";
  json += "\"environmentTemperature\":" + String(envTemp) + ",";
  json += "\"humidity\":" + String(humidity) + ",";
  json += "\"motionStatus\":\"" + motionStatus + "\",";
  json += "\"steps\":" + String(steps) + ",";
  json += "\"battery\":" + String(battery) + ",";
  json += "\"timestamp\":" + String(millis());
  json += "}";
  
  // 发布到MQTT
  client.publish("sensor/data", json.c_str());
}
```

### 4. 传感器接线参考

#### MAX30102 (心率/血氧)
```
MAX30102  →  ESP8266/ESP32
VIN       →  3.3V
GND       →  GND
SDA       →  D2 (GPIO4)
SCL       →  D1 (GPIO5)
INT       →  D3 (GPIO0)
```

#### DS18B20 (体温)
```
DS18B20   →  ESP8266/ESP32
VDD       →  3.3V
GND       →  GND
DQ        →  D4 (GPIO2) + 4.7kΩ上拉电阻到3.3V
```

#### DHT11/DHT22 (温湿度)
```
DHT       →  ESP8266/ESP32
VCC       →  3.3V
GND       →  GND
DATA      →  D5 (GPIO14) + 10kΩ上拉电阻到3.3V
```

#### MPU6050 (运动检测)
```
MPU6050   →  ESP8266/ESP32
VCC       →  3.3V
GND       →  GND
SDA       →  D2 (GPIO4)
SCL       →  D1 (GPIO5)
INT       →  D6 (GPIO12)
```

#### 蜂鸣器
```
蜂鸣器     →  ESP8266/ESP32
+         →  D7 (GPIO13) 通过三极管
-         →  GND
```

### 5. 测试步骤

#### 单元测试
1. **测试WiFi连接**
   ```cpp
   Serial.println(WiFi.localIP());
   ```

2. **测试MQTT连接**
   ```cpp
   if (client.connected()) {
     Serial.println("MQTT Connected");
   }
   ```

3. **测试传感器读取**
   - MAX30102: 读取心率和血氧
   - DS18B20: 读取体温
   - DHT: 读取温湿度
   - MPU6050: 读取加速度

4. **测试MQTT发布**
   ```cpp
   client.publish("sensor/data", "{\"test\":\"data\"}");
   ```

5. **测试MQTT订阅**
   - 发送命令测试蜂鸣器是否响应

#### App测试
1. 安装APK到Android设备
2. 查看日志确认MQTT连接状态
   ```bash
   adb logcat | grep MqttManager
   ```
3. 确认UI能接收并显示传感器数据
4. 测试阈值报警功能
5. 测试命令下发（开始测量、报警）

### 6. 故障排查

#### MQTT连接失败
- 检查网络连接
- 验证Broker地址和端口
- 确认用户名和密码正确
- 查看阿里云IoT设备状态

#### 数据不更新
- 检查单片机MQTT发布是否成功
- 确认Topic名称一致
- 验证JSON格式正确
- 查看App日志是否有解析错误

#### 传感器读数异常
- 检查传感器接线
- 验证I2C地址（MAX30102: 0x57, MPU6050: 0x68）
- 确认上拉电阻配置正确
- 使用I2C扫描程序检测设备

#### 报警不触发
- 检查阈值设置
- 确认震动权限已授予
- 验证MQTT命令发送成功
- 检查单片机端命令解析逻辑

### 7. 生产部署建议

1. **安全性**
   - 使用SSL/TLS加密MQTT连接
   - 定期更换MQTT密码
   - 启用阿里云IoT的设备认证

2. **稳定性**
   - 实现MQTT自动重连
   - 添加数据缓存机制
   - 异常情况下的降级策略

3. **性能**
   - 优化传感器采样频率
   - 减少MQTT消息发送频率
   - 启用消息压缩

4. **用户体验**
   - 添加网络状态检测
   - 提供离线模式
   - 数据本地缓存

## 常见问题 FAQ

**Q: 为什么连接一直失败？**
A: 请检查阿里云IoT控制台设备状态，确认三元组信息正确，并检查签名算法。

**Q: 数据延迟很大怎么办？**
A: 检查网络质量，考虑使用更近的Region，或优化JSON数据大小。

**Q: 如何实现多设备连接？**
A: 每个设备使用唯一的ClientID，App端可以订阅多个设备的Topic。

**Q: 可以使用其他MQTT Broker吗？**
A: 可以，修改MqttManager中的MQTT_BROKER地址即可，支持标准MQTT协议的Broker都可以。

## 联系支持

- 项目Issues: https://github.com/Bonjomondo/Health_Check_App/issues
- 阿里云IoT文档: https://help.aliyun.com/product/30520.html
