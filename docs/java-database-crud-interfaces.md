# Java 数据库 CRUD 接口说明

## 文档用途

本文档用于梳理 `Core/src/main/java` 中与数据库相关的主要 Java 接口，帮助阅读代码结构时快速定位新增、查询、修改、删除对应的入口。

每一项按照以下链路组织：

`HTTP 接口 -> controller -> service -> mapper/JdbcTemplate`

说明：

- 标准 CRUD 主要集中在 `metadata` 模块。
- 有些接口同时包含查询和修改行为，例如 `login`。
- 部分写操作不是通过 MyBatis 的 `@Insert/@Update/@Delete` 实现，而是通过 Oracle 存储过程完成。

## 新增

### 注册用户

- HTTP：`POST /api/auth/register`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/auth/controller/AuthController.java:29`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/auth/service/AuthService.java:71`
- Mapper：`Core/src/main/java/pers/luoluo/databasekeshe/auth/mapper/AuthMapper.java:51`
- 数据库动作：`insertUser(...)`，向 `SYS_USER` 表新增用户

### 新增变压器

- HTTP：`POST /api/metadata/transformers`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/controller/MetadataController.java:59`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/service/MetadataAdminService.java:37`
- 数据库动作：调用 Oracle 存储过程 `PKG_PSM_ASSET.CREATE_TRANSFORMER`

### 新增回路

- HTTP：`POST /api/metadata/transformers/{transformerId}/circuits`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/controller/MetadataController.java:96`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/service/MetadataAdminService.java:81`
- 数据库动作：调用 Oracle 存储过程 `PKG_PSM_ASSET.CREATE_CIRCUIT`

### 新增测点

- HTTP：`POST /api/metadata/transformers/{transformerId}/points`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/controller/MetadataController.java:134`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/service/MetadataAdminService.java:112`
- 数据库动作：调用 Oracle 存储过程 `PKG_PSM_ASSET.CREATE_POINT`

## 查询

### 登录时查询用户

- HTTP：`POST /api/auth/login`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/auth/controller/AuthController.java:24`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/auth/service/AuthService.java:34`
- Mapper：`Core/src/main/java/pers/luoluo/databasekeshe/auth/mapper/AuthMapper.java:18`
- 数据库动作：`findByUsername(...)`，从 `SYS_USER` 表读取用户信息

### 查询元数据树

- HTTP：`GET /api/metadata/transformers`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/controller/MetadataController.java:50`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/service/MetadataService.java:23`
- Mapper：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/mapper/MetadataMapper.java:94`
- 数据库动作：`findTransformerPointRows()`，一次读取变压器、回路、测点的树形数据

### 查询消息

- HTTP：`GET /api/messages`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/query/controller/QueryController.java:30`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/query/service/QueryService.java:35`
- Mapper：
- `Core/src/main/java/pers/luoluo/databasekeshe/query/mapper/QueryMapper.java:65` `findSampleMessages(...)`
- `Core/src/main/java/pers/luoluo/databasekeshe/query/mapper/QueryMapper.java:127` `findAlarmMessages(...)`
- `Core/src/main/java/pers/luoluo/databasekeshe/query/mapper/QueryMapper.java:190` `findTaskMessages(...)`
- 数据库动作：分别从采样、告警、任务相关表中查询消息数据

### 查询历史数据

- HTTP：`GET /api/history`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/query/controller/QueryController.java:54`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/query/service/QueryService.java:80`
- Mapper：`Core/src/main/java/pers/luoluo/databasekeshe/query/mapper/QueryMapper.java:296`
- 数据库动作：`findHistory(...)`，从 `TS_RAW_DATA` 和 `TS_RAW_DATA_DAY_ARCHIVE` 中查询历史数据

### 查询维护任务

- HTTP：`GET /api/tasks`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/maintenance/controller/MaintenanceTaskController.java:31`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/maintenance/service/MaintenanceTaskService.java:38`
- Mapper：`Core/src/main/java/pers/luoluo/databasekeshe/maintenance/mapper/MaintenanceTaskMapper.java:71`
- 数据库动作：`findTasks(...)`，查询 `MAINT_TASK`、`ALARM_LOG`、`BOX_TRANSFORMER`、`POWER_CIRCUIT`、`MEASURE_POINT`

### 查询运行日志

- HTTP：`GET /api/runtime-logs`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/logging/controller/RuntimeLogController.java:26`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/logging/service/DatabaseRuntimeLogService.java:20`
- Mapper：`Core/src/main/java/pers/luoluo/databasekeshe/logging/mapper/DatabaseRuntimeLogMapper.java:35`
- 数据库动作：`findLogs(...)`，从 `DB_RUNTIME_LOG` 表中查询日志

## 修改

### 登录时更新最后登录时间

- HTTP：`POST /api/auth/login`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/auth/controller/AuthController.java:24`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/auth/service/AuthService.java:34`
- Mapper：`Core/src/main/java/pers/luoluo/databasekeshe/auth/mapper/AuthMapper.java:64`
- 数据库动作：`updateLastLoginAt(...)`，更新 `SYS_USER` 表

### 修改变压器

- HTTP：`PUT /api/metadata/transformers/{transformerId}`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/controller/MetadataController.java:71`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/service/MetadataAdminService.java:57`
- 数据库动作：调用 Oracle 存储过程 `PKG_PSM_ASSET.UPDATE_TRANSFORMER`

### 修改回路

- HTTP：`PUT /api/metadata/circuits/{circuitId}`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/controller/MetadataController.java:109`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/service/MetadataAdminService.java:95`
- 数据库动作：调用 Oracle 存储过程 `PKG_PSM_ASSET.UPDATE_CIRCUIT`

### 修改测点

- HTTP：`PUT /api/metadata/points/{pointId}`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/controller/MetadataController.java:147`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/service/MetadataAdminService.java:135`
- 数据库动作：调用 Oracle 存储过程 `PKG_PSM_ASSET.UPDATE_POINT`

### 修改维护任务

- HTTP：`PUT /api/tasks/{taskId}`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/maintenance/controller/MaintenanceTaskController.java:53`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/maintenance/service/MaintenanceTaskService.java:57`
- 辅助查询：
- `Core/src/main/java/pers/luoluo/databasekeshe/maintenance/mapper/MaintenanceTaskMapper.java:86` `existsById(...)`
- `Core/src/main/java/pers/luoluo/databasekeshe/maintenance/mapper/MaintenanceTaskMapper.java:117` `findById(...)`
- 数据库动作：调用 Oracle 存储过程 `PKG_PSM_TASK.UPDATE_TASK`

## 删除

### 删除变压器

- HTTP：`DELETE /api/metadata/transformers/{transformerId}`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/controller/MetadataController.java:84`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/service/MetadataAdminService.java:77`
- 数据库动作：调用 Oracle 存储过程 `PKG_PSM_ASSET.DELETE_TRANSFORMER`

### 删除回路

- HTTP：`DELETE /api/metadata/circuits/{circuitId}`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/controller/MetadataController.java:122`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/service/MetadataAdminService.java:108`
- 数据库动作：调用 Oracle 存储过程 `PKG_PSM_ASSET.DELETE_CIRCUIT`

### 删除测点

- HTTP：`DELETE /api/metadata/points/{pointId}`
- Controller：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/controller/MetadataController.java:160`
- Service：`Core/src/main/java/pers/luoluo/databasekeshe/metadata/service/MetadataAdminService.java:157`
- 数据库动作：调用 Oracle 存储过程 `PKG_PSM_ASSET.DELETE_POINT`

## 补充说明

- 当前项目中，对外暴露的标准删除接口只出现在 `MetadataController` 中。
- 元数据查询在 `MetadataMapper` 中使用了 `IS_DELETED = 0` 条件，因此这些删除过程大概率是逻辑删除。
- 具体删除逻辑不在 Java 的 Mapper 注解里，而是在 Oracle 包过程里实现。