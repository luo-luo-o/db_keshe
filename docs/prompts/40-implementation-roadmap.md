# 落地开发路线提示词

本文件用于拆分开发任务，帮助后续 Agent 按可运行闭环逐步推进，而不是一次性堆叠大量未验证代码。

## 第一阶段：数据库与基础启动

目标：

- Oracle 21c 容器能启动。
- 后端能连接数据库。
- 建表脚本可执行。

任务：

1. 检查 `docker-compose.yml` 和 `.env`。
2. 编写或维护 `oracle21c-schema.sql`，并让 `oracle21c-init.sql` 作为入口调用。
3. 编写 `oracle21c-base-data.sql`，包含部署基础账号、维保人员、站点、间隔、设备、测点和阈值。
4. 编写 `oracle21c-mock-data.sql`，仅放当前占位演示数据，后续由真实模拟采集逻辑替换。
4. 将 `application.properties` 调整为 `application.yml`。
5. 配置 `dev-docker` 与 `dev-windows` profile。
6. 启动 Spring Boot 验证数据源连接。

验收：

- Docker Oracle 端口可用。
- 后端启动无数据源错误。
- 核心表和序列存在。

## 第二阶段：资产台账 API

目标：

- 前端能获取站点、间隔、设备、测点层级。

任务：

1. 编写 metadata domain/dto/mapper/service/controller。
2. 实现站点列表、间隔列表、设备列表、测点列表接口。
3. 返回统一 `ApiResponse`。
4. 添加基础查询测试或启动验证。

验收：

- `/api/stations` 可返回基础数据。
- `/api/devices` 可按站点和间隔过滤。

## 第三阶段：模拟采集与动态采样

目标：

- 系统能自动生成采样数据。
- 异常波动能触发 Burst。

任务：

1. 建立采样状态模型。
2. 编写 `AcquisitionScheduler`。
3. 编写模拟数据生成器，让正常数据平稳波动，并周期性制造异常。
4. 编写 `AdaptiveSamplingService` 判定阈值和变化率。
5. 使用 `BlockingQueue` 缓冲采样点。
6. 使用 MyBatis batch 写入 `TS_RAW_DATA`。

验收：

- 正常数据写入 `FREQ_FLAG = 0`。
- 异常后写入 `FREQ_FLAG = 1`。
- Burst 至少持续 5 分钟或可通过演示配置缩短。

## 第四阶段：告警与工单闭环

目标：

- 严重告警自动生成工单。

任务：

1. 编写 `AlarmService`。
2. 异常触发时插入 `ALARM_LOG`。
3. 严重级别告警插入 `MAINT_TASK`。
4. 编写告警查询、工单查询、工单状态更新接口。
5. 处理告警关闭和工单归档。

验收：

- 告警表有记录。
- 工单表有记录。
- 工单状态可从待办改为处理中和已完成。

## 第五阶段：前端工作台

目标：

- 用户打开前端即可看到监测工作台。

任务：

1. 安装 Element Plus、ECharts、axios。
2. 移除 Vite 模板内容。
3. 编写 `MainLayout.vue`。
4. 编写设备状态卡片、趋势图、告警表、工单表。
5. 封装 API 请求。
6. 对接后端基础接口。

验收：

- `npm run build` 通过。
- 页面无模板内容。
- 能展示真实或模拟接口数据。

## 第六阶段：事故追忆可视化

目标：

- 点击告警后能下钻查看前后 5 分钟高频波形。

任务：

1. 后端实现 `/api/alarms/{alarmId}/replay`。
2. 前端实现 `ReplayWaveChart.vue`。
3. 告警表行点击打开抽屉。
4. 抽屉内展示告警信息和高频曲线。
5. 高频点与分钟点视觉区分。

验收：

- 告警点点击后能看到 1 秒级曲线。
- 数据范围覆盖告警前后 5 分钟。

## 第七阶段：演示优化

目标：

- 项目适合课程设计展示和答辩说明。

任务：

1. README 补充启动步骤。
2. 添加截图或演示数据说明。
3. 添加常见问题，例如 Oracle SID 与 Service Name。
4. 添加接口清单。
5. 检查 `.gitignore`，避免提交敏感数据和构建产物。

验收：

- 新环境可按 README 启动。
- 业务闭环可完整演示。
