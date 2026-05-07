# PSM-Smart 验收清单

开发完成后按本清单检查。未满足项应记录原因和后续处理计划。

## 环境

- [ ] Windows 11 下能进入项目根目录。
- [ ] `docker compose up -d` 可启动 Oracle 21c XE。
- [ ] `.env` 不包含需要提交到仓库的真实敏感密码。
- [ ] 后端可通过 profile 切换 `dev-docker` 与 `dev-windows`。
- [ ] README 中说明 SID 连接和 Service Name 连接差异。

## 数据库

- [ ] `STATION_BASE` 已创建。
- [ ] `BAY_BASE` 已创建。
- [ ] `DEVICE_BASE` 已创建。
- [ ] `TAG_BASE` 已创建。
- [ ] `TS_RAW_DATA` 已创建。
- [ ] `ALARM_LOG` 已创建。
- [ ] `MAINT_TASK` 已创建。
- [ ] 所有核心表有对应 `SEQUENCE`。
- [ ] `TS_RAW_DATA` 有 `(DEVICE_ID, SAMPLE_TIME)` 复合索引。
- [ ] SQL 可在 Oracle 21c XE 执行。
- [ ] 提供分区策略说明，或提供分区版可选脚本。

## 后端

- [ ] 使用 Java 17 编译。
- [ ] Spring Boot 应用可启动。
- [ ] MyBatis mapper 能正常加载。
- [ ] 统一响应结构已实现。
- [ ] 资产台账查询接口可用。
- [ ] 模拟采集任务可配置启停。
- [ ] Normal 模式写入 `FREQ_FLAG = 0`。
- [ ] Burst 模式写入 `FREQ_FLAG = 1`。
- [ ] 阈值越限可触发告警。
- [ ] 变化率异常可触发告警。
- [ ] 严重告警可自动生成 `MAINT_TASK`。
- [ ] 事故追忆接口返回告警前后 5 分钟数据。
- [ ] 高频批量写入使用队列和 batch 机制。

## 前端

- [ ] Vite 默认模板内容已移除。
- [ ] Element Plus 已接入。
- [ ] ECharts 已接入。
- [ ] 使用 `<script setup lang="ts">`。
- [ ] 首页是监测工作台。
- [ ] 有站点、间隔、设备导航。
- [ ] 有设备最新状态展示。
- [ ] 有分钟级趋势曲线。
- [ ] 有告警列表。
- [ ] 有维保工单列表。
- [ ] 图表组件暴露 `updateData`。
- [ ] 点击告警可打开事故追忆曲线。
- [ ] `npm run build` 可通过。

## 业务闭环

- [ ] 初始化台账数据。
- [ ] 采集任务产生正常数据。
- [ ] 模拟异常触发 Burst。
- [ ] Burst 数据落库。
- [ ] 告警记录生成。
- [ ] 维保工单生成。
- [ ] 前端展示告警。
- [ ] 前端下钻高频波形。
- [ ] 工程师反馈可保存。
- [ ] 工单可归档。

## 演示口径

- [ ] 能解释为什么不是全量 1 秒存储。
- [ ] 能解释 `FREQ_FLAG` 的作用。
- [ ] 能解释 `BlockingQueue + MyBatis BATCH` 的意义。
- [ ] 能解释当前项目继续使用 Sequence 的原因。
- [ ] 能展示 Normal 到 Burst 的状态切换。
- [ ] 能展示告警点前后 5 分钟事故追忆。
