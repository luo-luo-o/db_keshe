# Agent Memory: Dashboard Query Simulation

本记忆文档用于后续 agent 继续维护登录后主界面、权限查询、历史数据和模拟测试功能。

## 固定约定

- 前端显示中文角色名，后端和数据库只保存英文角色编码。
- 当前角色编码固定为 `ADMIN`、`OPERATOR`、`ENGINEER`、`MANAGER`。
- `ADMIN` 拥有所有权限。
- 业务接口采用轻量请求头校验：`X-User-Id`、`X-Role-Code`。
- 不引入 Spring Security；本项目当前以课设演示闭环为目标。

## 业务范围

消息查询覆盖三类数据：

- `TS_RAW_DATA`：采样监测数据。
- `ALARM_LOG`：告警记录。
- `MAINT_TASK`：维保工单。

历史数据查询默认时间范围为当前时间前 1 小时到当前时间。前端可自定义开始和结束时间。

ADMIN 模拟测试包括：

- 启动模拟数据写入。
- 停止模拟数据写入。
- 模拟运行期间切换异常数据。
- 异常开启时写入越限/高频采样数据，生成告警和严重告警工单。

## 前后端接口

- `GET /api/metadata/devices`
- `GET /api/messages?category=&deviceId=&tagId=&startTime=&endTime=&keyword=`
- `GET /api/history?deviceId=&tagId=&startTime=&endTime=&freqFlag=`
- `POST /api/simulation/start`
- `POST /api/simulation/stop`
- `PUT /api/simulation/anomaly`
- `GET /api/simulation/status`

时间参数使用 ISO 字符串，后端转换为 `LocalDateTime`。

## 权限矩阵

| 功能 | ADMIN | OPERATOR | ENGINEER | MANAGER |
| :--- | :---: | :---: | :---: | :---: |
| 采样数据查询 | 是 | 是 | 关联查询 | 是 |
| 告警查询 | 是 | 是 | 是 | 是 |
| 工单查询 | 是 | 否 | 是 | 是 |
| 历史数据 | 是 | 是 | 关联查询 | 是 |
| 模拟测试 | 是 | 否 | 否 | 否 |

## 验收命令

本仓库构建验收统一使用根目录的一键脚本，不要绕过它单独跑前端或后端构建：

```powershell
.\build\build.bat
```

该脚本负责安装前端依赖、构建前端、复制静态资源、打包后端和复制 SQL。
