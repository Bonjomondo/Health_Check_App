# Smart Health Guard - 健康监测App

一个完整的健康监测Android应用，通过MQTT（阿里云）与单片机通信，实时监测心率、血氧、体温等健康数据。

## 功能特性

### 页面一：实时监测首页 (Dashboard - Monitor)
- ✅ 顶部状态栏显示设备名称、连接状态、电量
- ✅ 心率卡片（大尺寸）- 显示BPM值，动态心形图标，状态提示（正常/过快/过慢）
- ✅ 血氧卡片（中尺寸）- 显示百分比，水滴图标
- ✅ 体温卡片（中尺寸）- 显示摄氏度，温度计图标
- ✅ 环境卡片 - 显示环境温度和湿度
- ✅ 运动状态卡片 - 将MPU6050数据转化为状态（静止/行走/跌倒检测）和步数
- ✅ 悬浮按钮 - "开始测量"功能
- ✅ 底部导航栏 - 快速切换三个主页面

### 页面二：历史趋势 (History & Analysis)
- ✅ 标签页切换 - 心率、血氧、体温
- ✅ 折线图展示 - 使用MPAndroidChart显示数据趋势
- ✅ 时间范围选择 - 1小时或24小时
- ✅ 异常记录列表 - 显示所有触发报警的记录

### 页面三：设置与连接 (Settings & Connection)
- ✅ 连接管理 - 蓝牙设备扫描、Wi-Fi配置
- ✅ 阈值设置 - 自定义心率上限（默认100）、体温上限（默认37.3）
- ✅ 功能开关 - 久坐提醒、震动反馈
- ✅ 设置持久化 - 使用SharedPreferences保存用户偏好

### 交互逻辑
- ✅ 连接反馈 - 蓝牙/MQTT连接成功时状态变绿，显示Toast提示
- ✅ 数据同步 - 500ms-1s刷新UI，避免卡顿
- ✅ 报警弹窗 - 检测到跌倒或高烧时弹出对话框
- ✅ 手机震动 - 异常情况触发震动反馈
- ✅ 双向通信 - App可下发指令给单片机（开始测量、触发蜂鸣器）

## 技术架构

### 传感器数据来源
- **MAX30102**: 心率、血氧
- **DS18B20**: 体温
- **DHT传感器**: 环境温度、湿度
- **MPU6050**: 运动状态、步数、跌倒检测

### 通信协议
- **MQTT** - 通过阿里云IoT平台与单片机通信
- **主题设计**:
  - `sensor/data` - 接收传感器数据
  - `device/command` - 下发控制指令
  - `device/status` - 设备状态（电量等）

### 数据格式（JSON）
```json
{
  "heartRate": 75,
  "bloodOxygen": 98,
  "bodyTemperature": 36.5,
  "environmentTemperature": 25.0,
  "humidity": 60,
  "motionStatus": "SEDENTARY",
  "steps": 1234,
  "battery": 85,
  "timestamp": 1637500000000
}
```

### 控制指令
- `START_MEASURE` - 开始测量
- `ALARM_FALL` - 跌倒报警（触发蜂鸣器）
- `ALARM_FEVER` - 高烧报警（触发蜂鸣器）
- `ALARM_HEART_RATE` - 心率异常报警（触发蜂鸣器）

## 项目结构

```
app/src/main/java/com/example/health_check_app/
├── MainActivity.java              # 主页面 - 实时监测
├── HistoryActivity.java           # 历史趋势页面
├── SettingsActivity.java          # 设置页面
├── AlertLogAdapter.java           # 异常记录适配器
├── models/
│   ├── SensorData.java           # 传感器数据模型
│   └── AlertRecord.java          # 异常记录模型
└── mqtt/
    └── MqttManager.java          # MQTT通信管理器

app/src/main/res/
├── layout/
│   ├── activity_main.xml         # 主页面布局
│   ├── activity_history.xml      # 历史页面布局
│   ├── activity_settings.xml     # 设置页面布局
│   └── item_alert_log.xml        # 异常记录项布局
├── menu/
│   └── bottom_navigation_menu.xml # 底部导航菜单
├── values/
│   ├── strings.xml               # 字符串资源
│   └── colors.xml                # 颜色资源
└── ...
```

## 依赖库

- **AndroidX** - 现代Android开发库
- **Material Design Components** - Google Material Design组件
- **MPAndroidChart** - 图表库
- **Eclipse Paho MQTT** - MQTT客户端
- **ViewPager2** - 页面滑动组件

## 配置MQTT连接

在实际部署时，需要在 `MqttManager.java` 中配置阿里云IoT的连接信息：

```java
// MQTT Broker configuration for Alibaba Cloud IoT
private static final String MQTT_BROKER = "tcp://your-instance.aliyuncs.com:1883";
private static final String CLIENT_ID = "your_client_id";

// 在MainActivity中调用
mqttManager.connect("your_username", "your_password");
```

## 权限说明

应用需要以下权限：
- `INTERNET` - 网络通信
- `ACCESS_NETWORK_STATE` - 检测网络状态
- `BLUETOOTH` / `BLUETOOTH_ADMIN` - 蓝牙连接
- `BLUETOOTH_CONNECT` / `BLUETOOTH_SCAN` - 蓝牙扫描（Android 12+）
- `ACCESS_FINE_LOCATION` - 蓝牙扫描需要位置权限
- `VIBRATE` - 震动反馈
- `WAKE_LOCK` - MQTT保持连接

## 构建和运行

1. 克隆仓库
2. 使用Android Studio打开项目
3. 同步Gradle依赖
4. 配置MQTT连接信息
5. 连接Android设备或启动模拟器
6. 运行应用

## 最低要求

- Android SDK 24 (Android 7.0)
- 目标SDK 36 (Android 14)
- Java 11

## 开发者

该应用遵循Material Design设计规范，提供直观的用户界面和流畅的交互体验。

## License

MIT License
