# 前端开发 Agent 提示词

你负责 `Front/` 下的 Vue 3 + TypeScript 前端。目标是把模板工程改造成变电站监测工作台，展示设备状态、趋势曲线、告警和维保工单。

## 技术要求

- 使用 Vue 3。
- 单文件组件使用 `<script setup lang="ts">`。
- 使用 Element Plus 构建布局、表格、表单、抽屉、弹窗和状态标签。
- 使用 ECharts 绘制趋势曲线和高频波形。
- API 请求封装到 `src/api/`。
- 类型定义放到 `src/types/`。
- 不保留 Vite 默认首页、默认计数器和模板说明。

## 推荐目录

```text
src/
├── api/
│   ├── http.ts
│   ├── metadata.ts
│   ├── monitoring.ts
│   ├── alarm.ts
│   └── maintenance.ts
├── components/
│   ├── charts/
│   │   ├── RealtimeTrendChart.vue
│   │   └── ReplayWaveChart.vue
│   ├── DeviceStatusCard.vue
│   └── AlarmLevelTag.vue
├── layouts/
│   └── MainLayout.vue
├── views/
│   ├── DashboardView.vue
│   ├── DeviceMonitorView.vue
│   ├── AlarmReplayView.vue
│   └── MaintenanceTaskView.vue
├── types/
│   ├── metadata.ts
│   ├── monitoring.ts
│   ├── alarm.ts
│   └── maintenance.ts
└── utils/
    └── format.ts
```

## 页面结构

首屏应为监测工作台：

- 左侧：站点、间隔、设备导航。
- 顶部：系统名称、当前连接状态、最后刷新时间、告警数量。
- 主区上方：设备关键指标卡片，例如电流、油温、绕组温度、负载率。
- 主区中部：分钟级趋势曲线。
- 主区下方：告警列表和维保任务列表。
- 右侧或抽屉：点击告警后展示事故追忆高频波形。

## ECharts 组件要求

趋势图组件需要暴露 `updateData`：

```ts
export interface TrendPoint {
  sampleTime: string
  value: number
  freqFlag: 0 | 1
}

export interface RealtimeTrendChartExpose {
  updateData: (points: TrendPoint[]) => void
}
```

组件内部建议：

- `onMounted` 初始化图表。
- `onBeforeUnmount` 销毁图表。
- 监听容器尺寸变化并 resize。
- 使用 `defineExpose({ updateData })` 暴露方法。
- 高频数据和分钟级数据用不同颜色或线型区分。

## 交互要求

告警下钻：

- 用户点击曲线上的告警点或告警表格行。
- 前端调用 `/api/alarms/{alarmId}/replay`。
- 在抽屉或页面区域展示告警前 5 分钟到后 5 分钟的 1 秒波形。
- 展示告警类型、开始时间、结束时间、设备名称、处理状态。

工单流转：

- 待办、处理中、已完成使用 Element Plus Tag 显示。
- 支持状态更新。
- 支持工程师反馈文本。
- 完成后列表状态立即刷新。

## API 类型示例

```ts
export interface DeviceLatestValue {
  deviceId: number
  deviceName: string
  sampleTime: string
  currentValue: number
  oilTemperature: number
  windingTemperature: number
  samplingMode: 'NORMAL' | 'BURST'
}

export interface AlarmRecord {
  id: number
  deviceId: number
  deviceName: string
  alarmType: string
  level: 'WARN' | 'SERIOUS'
  startTime: string
  endTime?: string
  status: 'ACTIVE' | 'CLOSED'
}

export interface MaintenanceTask {
  taskId: number
  alarmId: number
  deviceName: string
  status: 0 | 1 | 2
  assignee?: string
  feedback?: string
  createdAt: string
}
```

## 视觉风格

- 面向电力监控和运维场景，界面应清晰、克制、稳定。
- 避免营销页、超大 hero、装饰性渐变背景。
- 颜色建议以浅色工作台为主，使用红、橙、蓝、绿表达状态。
- 表格、曲线和状态信息优先，装饰图形从简。
- 组件边距和字号保持紧凑，适合长时间监控。

## 验收标准

- `npm run build` 可通过。
- 没有 TypeScript 类型错误。
- 首屏不再显示 Vite/Vue 模板内容。
- 能展示设备状态、分钟级曲线、告警列表、工单列表。
- ECharts 组件具备 `updateData` 暴露接口。
- 告警点击后能展示事故追忆区域或抽屉。

