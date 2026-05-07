# 后端开发 Agent 提示词

你负责 `Core/` 下的 Spring Boot 后端。目标是实现 PSM-Smart 的资产台账、动态采样、告警、事故追忆和维保工单闭环。

## 技术要求

- 使用 Java 17。
- 使用 Spring Boot 3.x。
- 使用 MyBatis 访问 Oracle 21c XE。
- 优先采用构造器注入。
- Controller、Service、Mapper、DTO/VO 分层清晰。
- 不在 Controller 中写业务逻辑。
- 不在 Mapper SQL 中写复杂业务状态机。

## 推荐包结构

```text
pers.luoluo.databasekeshe
├── common
│   ├── ApiResponse.java
│   ├── GlobalExceptionHandler.java
│   └── PageResult.java
├── config
│   ├── MyBatisBatchConfig.java
│   └── SchedulingConfig.java
├── metadata
│   ├── controller
│   ├── domain
│   ├── dto
│   ├── mapper
│   └── service
├── acquisition
│   ├── domain
│   ├── mapper
│   ├── scheduler
│   └── service
├── alarm
│   ├── controller
│   ├── domain
│   ├── mapper
│   └── service
├── maintenance
│   ├── controller
│   ├── domain
│   ├── mapper
│   └── service
└── visualization
    ├── controller
    ├── dto
    └── service
```

## 必须实现的核心类

动态采样建议类：

- `SamplingMode`：枚举，包含 `NORMAL`、`BURST`。
- `SamplingConfig`：采样周期、阈值、Burst 保持时间。
- `SamplePoint`：设备 ID、测点 ID、采样时间、值、频率标识。
- `DeviceSamplingState`：保存某设备当前采样模式、上一次值、进入 Burst 的时间、最近稳定时间。
- `AcquisitionScheduler`：定时模拟采集入口。
- `AdaptiveSamplingService`：负责判断是否切换采样模式。
- `SampleBufferService`：维护 `BlockingQueue<SamplePoint>`。
- `BatchRawDataWriter`：使用 MyBatis batch 写入 Oracle。

告警与工单建议类：

- `AlarmService`：创建告警、结束告警、查询告警。
- `MaintenanceTaskService`：严重告警触发时自动生成工单。
- `ReplayService`：按告警时间查询前后 5 分钟高频数据。

## 调度与批处理要求

模拟采集：

- 使用 `@Scheduled`。
- Normal 采集可每分钟执行。
- Burst 采集可每秒执行，内部只处理处于 Burst 的设备。
- 调度开关必须可配置，例如 `psm.acquisition.enabled=true`。

队列写入：

- 采集线程只负责生成数据并放入 `BlockingQueue`。
- 批量写入线程按数量或时间窗口 flush。
- 建议参数：
  - `batch-size: 200`
  - `flush-interval-ms: 1000`
  - `queue-capacity: 10000`

MyBatis batch：

- 可使用独立 `SqlSessionTemplate` 或在写入服务中通过 `SqlSessionFactory.openSession(ExecutorType.BATCH)`。
- batch flush 后需要提交事务。
- 失败时记录日志，并避免吞掉异常导致数据静默丢失。

## application.yml 要求

使用 `application.yml` 管理公共配置，并使用 profile 区分环境：

```yaml
spring:
  application:
    name: database-keshe
  profiles:
    active: dev-docker

mybatis:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: pers.luoluo.databasekeshe
  configuration:
    map-underscore-to-camel-case: true

psm:
  acquisition:
    enabled: true
    normal-interval-ms: 60000
    burst-interval-ms: 1000
    burst-hold-minutes: 5
    batch-size: 200
    queue-capacity: 10000
```

`dev-docker` 使用 Oracle 21c XE PDB Service Name：

```yaml
spring:
  config:
    activate:
      on-profile: dev-docker
  datasource:
    driver-class-name: oracle.jdbc.OracleDriver
    url: jdbc:oracle:thin:@//localhost:1521/XEPDB1
    username: ${DB_USERNAME:psm_app}
    password: ${DB_PASSWORD:psm_app_123}
```

`dev-windows` 可使用本地 service name：

```yaml
spring:
  config:
    activate:
      on-profile: dev-windows
  datasource:
    driver-class-name: oracle.jdbc.OracleDriver
    url: ${DB_URL:jdbc:oracle:thin:@//localhost:1521/XEPDB1}
    username: ${DB_USERNAME:psm_app}
    password: ${DB_PASSWORD:psm_app_123}
```

## API 输出约定

统一返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "timestamp": "2026-05-06T21:30:00"
}
```

建议接口：

```text
GET    /api/stations
GET    /api/bays?stationId=
GET    /api/devices?stationId=&bayId=
GET    /api/devices/{deviceId}/latest
GET    /api/devices/{deviceId}/curve?startTime=&endTime=&freqFlag=
GET    /api/alarms?deviceId=&status=
GET    /api/alarms/{alarmId}/replay
GET    /api/tasks?status=
PUT    /api/tasks/{taskId}/status
POST   /api/tasks/{taskId}/feedback
```

## 验收标准

- `mvn test` 或至少 `mvn package` 可通过。
- 应用能使用 `dev-docker` profile 连接 Oracle 21c XE。
- 模拟采集能向 `TS_RAW_DATA` 写入 `FREQ_FLAG = 0` 与 `FREQ_FLAG = 1` 两类数据。
- 严重告警能生成 `ALARM_LOG` 和 `MAINT_TASK`。
- `/api/alarms/{alarmId}/replay` 能返回告警前后 5 分钟数据。
