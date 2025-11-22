# Project Structure Overview

## File Organization

```
Health_Check_App/
├── README.md                          # Main documentation
├── ARCHITECTURE.md                    # System architecture
├── INTEGRATION_GUIDE.md              # Integration guide
├── Demands                           # Original requirements (Chinese)
│
└── Health_Check_APP/                 # Android project root
    ├── app/
    │   ├── build.gradle.kts          # App build configuration
    │   ├── src/
    │   │   ├── main/
    │   │   │   ├── AndroidManifest.xml              # App manifest & permissions
    │   │   │   ├── java/com/example/health_check_app/
    │   │   │   │   ├── MainActivity.java            # 页面一：实时监测
    │   │   │   │   ├── HistoryActivity.java         # 页面二：历史趋势
    │   │   │   │   ├── SettingsActivity.java        # 页面三：设置连接
    │   │   │   │   ├── AlertLogAdapter.java         # 异常记录适配器
    │   │   │   │   ├── AppConfig.java               # 配置管理器
    │   │   │   │   ├── models/
    │   │   │   │   │   ├── SensorData.java          # 传感器数据模型
    │   │   │   │   │   └── AlertRecord.java         # 异常记录模型
    │   │   │   │   └── mqtt/
    │   │   │   │       └── MqttManager.java          # MQTT通信管理器
    │   │   │   └── res/
    │   │   │       ├── layout/
    │   │   │       │   ├── activity_main.xml        # 主页面布局
    │   │   │       │   ├── activity_history.xml     # 历史页面布局
    │   │   │       │   ├── activity_settings.xml    # 设置页面布局
    │   │   │       │   └── item_alert_log.xml       # 异常记录项
    │   │   │       ├── menu/
    │   │   │       │   └── bottom_navigation_menu.xml # 底部导航
    │   │   │       ├── drawable/
    │   │   │       │   ├── ic_heart.xml             # 心形图标
    │   │   │       │   └── ic_connection.xml        # 连接图标
    │   │   │       └── values/
    │   │   │           ├── strings.xml              # 字符串资源
    │   │   │           └── colors.xml               # 颜色资源
    │   │   ├── test/
    │   │   │   └── java/...                         # 单元测试
    │   │   └── androidTest/
    │   │       └── java/...                         # 集成测试
    │   └── proguard-rules.pro
    ├── gradle/
    │   └── libs.versions.toml                       # 依赖版本管理
    ├── build.gradle.kts                             # 项目级构建配置
    ├── settings.gradle.kts                          # Gradle设置
    └── gradlew                                      # Gradle wrapper
```

## Component Overview

### Activities (3)

| Activity | Purpose | Key Features |
|----------|---------|--------------|
| MainActivity | 实时监测首页 | - 卡片式布局<br>- 实时数据显示<br>- 连接状态<br>- 报警监控 |
| HistoryActivity | 历史趋势分析 | - 折线图<br>- Tab切换<br>- 异常记录列表 |
| SettingsActivity | 设置与连接 | - 设备扫描<br>- 阈值设置<br>- 功能开关 |

### Models (2)

| Model | Fields | Description |
|-------|--------|-------------|
| SensorData | heartRate, bloodOxygen, bodyTemperature, environmentTemperature, humidity, motionStatus, steps, batteryLevel, timestamp | 传感器数据完整模型 |
| AlertRecord | time, type, message, timestamp | 异常记录模型 |

### Utilities (3)

| Utility | Purpose |
|---------|---------|
| MqttManager | MQTT连接、订阅、发布、消息处理 |
| AppConfig | SharedPreferences封装，统一配置管理 |
| AlertLogAdapter | RecyclerView适配器，显示异常记录 |

### Layouts (4)

| Layout | Type | Description |
|--------|------|-------------|
| activity_main.xml | CoordinatorLayout | 主页面：卡片 + FAB + 底部导航 |
| activity_history.xml | LinearLayout | 历史页面：Tab + 图表 + 列表 |
| activity_settings.xml | ScrollView | 设置页面：卡片式设置项 |
| item_alert_log.xml | CardView | 异常记录单项布局 |

### Resources

- **Strings**: 60+ 中文字符串资源
- **Colors**: 15+ Material Design配色
- **Menu**: 底部导航菜单
- **Drawables**: 矢量图标

## Dependencies

```gradle
dependencies {
    // Android核心库
    implementation 'androidx.appcompat:appcompat:1.7.1'
    implementation 'com.google.android.material:material:1.13.0'
    implementation 'androidx.activity:activity:1.12.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.2.1'
    implementation 'androidx.viewpager2:viewpager2:1.1.0'
    
    // 图表库
    implementation 'com.github.PhilJay:MPAndroidChart:3.1.0'
    
    // MQTT客户端
    implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5'
    implementation 'org.eclipse.paho:org.eclipse.paho.android.service:1.2.5'
    
    // 测试库
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.3.0'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.7.0'
}
```

## Key Features Implemented

### ✅ UI/UX
- [x] Material Design卡片式布局
- [x] 响应式UI设计
- [x] 中文本地化
- [x] 底部导航栏
- [x] 悬浮操作按钮
- [x] 折线图数据可视化
- [x] 动态状态指示器

### ✅ Data Management
- [x] MQTT实时数据接收
- [x] JSON数据解析
- [x] SharedPreferences持久化
- [x] 数据模型封装
- [x] 异常记录管理

### ✅ Communication
- [x] MQTT连接管理
- [x] 自动重连机制
- [x] Topic订阅/发布
- [x] 双向通信
- [x] 命令下发

### ✅ Alerts & Notifications
- [x] 阈值监控
- [x] Dialog弹窗提醒
- [x] 手机震动反馈
- [x] 状态颜色变化
- [x] Toast提示

### ✅ Settings & Configuration
- [x] 心率阈值设置
- [x] 体温阈值设置
- [x] 久坐提醒开关
- [x] 震动反馈开关
- [x] 设置持久化

## Permissions Required

```xml
<!-- 网络通信 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 蓝牙 -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />

<!-- 位置 (蓝牙扫描需要) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- 震动 -->
<uses-permission android:name="android.permission.VIBRATE" />

<!-- MQTT保持连接 -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## Build Configuration

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)
- **Compile SDK**: 36
- **Java Version**: 11
- **Gradle**: 8.13
- **AGP**: 8.3.0

## Next Steps for Production

1. ✅ Code implementation complete
2. ⏳ Configure Alibaba Cloud IoT credentials
3. ⏳ Test with actual hardware
4. ⏳ Security review
5. ⏳ Performance optimization
6. ⏳ User acceptance testing
7. ⏳ Play Store deployment

## Maintenance

- Regular dependency updates
- Security patches
- Performance monitoring
- User feedback integration
- Bug fixes
- Feature enhancements
