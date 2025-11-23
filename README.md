# Smart Health Guard - 健康监测App

一个完整的健康监测Android应用，通过蓝牙与单片机通信，实时监测心率、血氧、体温等健康数据。

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
- ✅ 连接管理 - 蓝牙设备扫描与配对
- ✅ 阈值设置 - 自定义心率上限（默认100）、体温上限（默认37.3）
- ✅ 功能开关 - 久坐提醒、震动反馈
- ✅ 设置持久化 - 使用SharedPreferences保存用户偏好

### 交互逻辑
- ✅ 连接反馈 - 蓝牙连接成功时状态变绿，显示Toast提示
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
- **蓝牙 (Bluetooth SPP)** - 通过经典蓝牙与单片机通信
- **数据格式**: JSON
- **连接方式**: 串口配置文件 (Serial Port Profile)

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
└── bluetooth/
    └── BluetoothManager.java     # 蓝牙通信管理器

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
- **ViewPager2** - 页面滑动组件

## 配置蓝牙连接

### 单片机端配置
1. 使用HC-05或HC-06蓝牙模块
2. 配置波特率（建议9600或115200）
3. 设置蓝牙名称便于识别
4. 实现JSON格式数据发送

### Android端使用
1. 在设置页面点击"扫描设备"
2. 选择要连接的蓝牙设备
3. 等待连接成功提示
4. 开始接收传感器数据

## 权限说明

应用需要以下权限：
- `BLUETOOTH` / `BLUETOOTH_ADMIN` - 蓝牙连接（Android 11及以下）
- `BLUETOOTH_CONNECT` / `BLUETOOTH_SCAN` - 蓝牙扫描（Android 12+）
- `ACCESS_FINE_LOCATION` - 蓝牙扫描需要位置权限
- `ACCESS_COARSE_LOCATION` - 粗略位置权限
- `VIBRATE` - 震动反馈
- `INTERNET` - 未来功能扩展预留
- `ACCESS_NETWORK_STATE` - 网络状态检测

## 构建和运行

1. 克隆仓库
2. 使用Android Studio打开项目
3. 同步Gradle依赖
4. 连接Android设备或启动模拟器
5. 运行应用
6. 在设置页面扫描并连接蓝牙设备

## 最低要求

- Android SDK 24 (Android 7.0)
- 目标SDK 36 (Android 14)
- Java 11
- 支持蓝牙的Android设备

## 开发者

该应用遵循Material Design设计规范，提供直观的用户界面和流畅的交互体验。

## License

MIT License
