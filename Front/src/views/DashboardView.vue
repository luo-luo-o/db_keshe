<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import * as echarts from 'echarts'
import {
  fetchDevices,
  fetchHistory,
  fetchMessages,
  fetchSimulationStatus,
  setSimulationAnomaly,
  startSimulation,
  stopSimulation,
} from '../api/dashboard'
import { roleLabels } from '../types/auth'
import {
  allowedCategories,
  categoryLabels,
  type DeviceOptionResponse,
  type HistoryDataRow,
  type MessageCategory,
  type MessageResponse,
  type SimulationStatusResponse,
  type TagOptionResponse,
} from '../types/dashboard'
import type { AuthSession } from '../types/auth'

const props = defineProps<{
  session: AuthSession
}>()

const emit = defineEmits<{
  logout: []
}>()

type TabName = 'messages' | 'history' | 'simulation'

const activeTab = ref<TabName>('messages')
const devices = ref<DeviceOptionResponse[]>([])
const messages = ref<MessageResponse[]>([])
const historyRows = ref<HistoryDataRow[]>([])
const simulation = ref<SimulationStatusResponse | null>(null)
const isLoadingMessages = ref(false)
const isLoadingHistory = ref(false)
const isLoadingMetadata = ref(false)
const isSimulationBusy = ref(false)
const errorMessage = ref('')
const chartEl = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const messageForm = reactive({
  category: '' as '' | MessageCategory,
  deviceId: undefined as number | undefined,
  tagId: undefined as number | undefined,
  startTime: '' as string,
  endTime: '' as string,
  keyword: '',
})

const historyForm = reactive({
  deviceId: undefined as number | undefined,
  tagId: undefined as number | undefined,
  startTime: toLocalInputValue(new Date(Date.now() - 60 * 60 * 1000)),
  endTime: toLocalInputValue(new Date()),
  freqFlag: '' as '' | 0 | 1,
})

const categoryOptions = computed(() => allowedCategories(props.session.roleCode))
const isAdmin = computed(() => props.session.roleCode === 'ADMIN')

const messageTagOptions = computed(() => tagsForDevice(messageForm.deviceId))
const historyTagOptions = computed(() => tagsForDevice(historyForm.deviceId))

const deviceStatusCounts = computed(() => {
  const normal = devices.value.filter((device) => device.status === 0).length
  const warning = devices.value.filter((device) => device.status === 1).length
  const offline = devices.value.filter((device) => device.status === 2).length
  return { normal, warning, offline, total: devices.value.length }
})

onMounted(async () => {
  await loadMetadata()
  await Promise.all([queryMessages(), queryHistory(), loadSimulationStatus()])
})

watch(
  () => messageForm.deviceId,
  () => {
    messageForm.tagId = undefined
  },
)

watch(
  () => historyForm.deviceId,
  () => {
    historyForm.tagId = undefined
  },
)

watch(historyRows, () => {
  void nextTick(renderChart)
})

function tagsForDevice(deviceId?: number): TagOptionResponse[] {
  if (!deviceId) {
    return devices.value.flatMap((device) => device.tags)
  }

  return devices.value.find((device) => device.deviceId === deviceId)?.tags ?? []
}

async function loadMetadata() {
  isLoadingMetadata.value = true
  errorMessage.value = ''
  try {
    devices.value = await fetchDevices(props.session)
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    isLoadingMetadata.value = false
  }
}

async function queryMessages() {
  isLoadingMessages.value = true
  errorMessage.value = ''
  try {
    messages.value = await fetchMessages(props.session, {
      category: messageForm.category || undefined,
      deviceId: messageForm.deviceId,
      tagId: messageForm.tagId,
      startTime: toIsoValue(messageForm.startTime),
      endTime: toIsoValue(messageForm.endTime),
      keyword: messageForm.keyword.trim() || undefined,
    })
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    isLoadingMessages.value = false
  }
}

async function queryHistory() {
  isLoadingHistory.value = true
  errorMessage.value = ''
  try {
    historyRows.value = await fetchHistory(props.session, {
      deviceId: historyForm.deviceId,
      tagId: historyForm.tagId,
      startTime: toIsoValue(historyForm.startTime),
      endTime: toIsoValue(historyForm.endTime),
      freqFlag: historyForm.freqFlag === '' ? undefined : historyForm.freqFlag,
    })
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    isLoadingHistory.value = false
  }
}

async function loadSimulationStatus() {
  if (!isAdmin.value) {
    return
  }

  try {
    simulation.value = await fetchSimulationStatus(props.session)
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  }
}

async function handleStartSimulation() {
  isSimulationBusy.value = true
  try {
    simulation.value = await startSimulation(props.session)
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    isSimulationBusy.value = false
  }
}

async function handleStopSimulation() {
  isSimulationBusy.value = true
  try {
    simulation.value = await stopSimulation(props.session)
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    isSimulationBusy.value = false
  }
}

async function handleAnomalyChange(value: string | number | boolean) {
  isSimulationBusy.value = true
  try {
    simulation.value = await setSimulationAnomaly(props.session, Boolean(value))
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  } finally {
    isSimulationBusy.value = false
  }
}

function renderChart() {
  if (!chartEl.value) {
    return
  }

  chart ??= echarts.init(chartEl.value)
  const orderedRows = [...historyRows.value].reverse()
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 20, top: 24, bottom: 40 },
    xAxis: {
      type: 'category',
      data: orderedRows.map((row) => formatTime(row.sampleTime)),
      axisLabel: { color: '#64748b' },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#64748b' },
      splitLine: { lineStyle: { color: '#e2e8f0' } },
    },
    series: [
      {
        name: '采样值',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        data: orderedRows.map((row) => row.value),
        lineStyle: { color: '#2563eb', width: 2 },
        itemStyle: { color: '#2563eb' },
        areaStyle: { color: 'rgba(37, 99, 235, 0.10)' },
      },
    ],
  })
}

function resetHistoryRange() {
  historyForm.startTime = toLocalInputValue(new Date(Date.now() - 60 * 60 * 1000))
  historyForm.endTime = toLocalInputValue(new Date())
}

function messageStatusLabel(row: MessageResponse) {
  if (row.category === 'ALARM') {
    return row.status === 0 ? '活跃' : '已关闭'
  }

  if (row.category === 'TASK') {
    return row.status === 0 ? '待办' : row.status === 1 ? '处理中' : '已完成'
  }

  return row.qualityFlag === 0 ? '正常' : row.qualityFlag === 1 ? '可疑' : '无效'
}

function messageStatusType(row: MessageResponse) {
  if (row.category === 'ALARM') {
    return row.status === 0 ? 'danger' : 'success'
  }

  if (row.category === 'TASK') {
    return row.status === 2 ? 'success' : 'warning'
  }

  return row.qualityFlag === 0 ? 'success' : 'warning'
}

function freqLabel(freqFlag?: number) {
  if (freqFlag === 1) {
    return '秒级'
  }

  if (freqFlag === 0) {
    return '分钟级'
  }

  return '-'
}

function formatTime(value?: string) {
  if (!value) {
    return '-'
  }

  return value.replace('T', ' ').slice(0, 19)
}

function toLocalInputValue(date: Date) {
  const offset = date.getTimezoneOffset()
  const local = new Date(date.getTime() - offset * 60 * 1000)
  return local.toISOString().slice(0, 16)
}

function toIsoValue(value: string) {
  if (!value) {
    return undefined
  }

  return value.length === 16 ? `${value}:00` : value
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : '操作失败'
}

function handleMenuSelect(key: string) {
  activeTab.value = key as TabName
}
</script>

<template>
  <div class="dashboard-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">PSM</span>
        <div>
          <strong>变电站监测</strong>
          <small>Smart Operation Console</small>
        </div>
      </div>

      <el-menu :default-active="activeTab" class="nav-menu" @select="handleMenuSelect">
        <el-menu-item index="messages">消息查询</el-menu-item>
        <el-menu-item index="history">历史数据</el-menu-item>
        <el-menu-item v-if="isAdmin" index="simulation">模拟测试</el-menu-item>
      </el-menu>
    </aside>

    <main class="workspace">
      <header class="topbar">
        <div>
          <h1>{{ activeTab === 'messages' ? '消息查询' : activeTab === 'history' ? '历史数据' : '模拟测试' }}</h1>
          <p>{{ props.session.displayName }} · {{ roleLabels[props.session.roleCode] }}</p>
        </div>

        <el-button @click="emit('logout')">退出登录</el-button>
      </header>

      <el-alert v-if="errorMessage" class="error-alert" type="error" :title="errorMessage" show-icon />

      <section class="overview-grid" v-loading="isLoadingMetadata">
        <div class="metric">
          <span>设备总数</span>
          <strong>{{ deviceStatusCounts.total }}</strong>
        </div>
        <div class="metric">
          <span>正常设备</span>
          <strong>{{ deviceStatusCounts.normal }}</strong>
        </div>
        <div class="metric">
          <span>告警设备</span>
          <strong>{{ deviceStatusCounts.warning }}</strong>
        </div>
        <div class="metric">
          <span>停运设备</span>
          <strong>{{ deviceStatusCounts.offline }}</strong>
        </div>
      </section>

      <section v-if="activeTab === 'messages'" class="panel">
        <el-form class="query-form" :model="messageForm" label-position="top">
          <el-form-item label="消息类型">
            <el-select v-model="messageForm.category" clearable placeholder="全部可查询类型">
              <el-option
                v-for="category in categoryOptions"
                :key="category"
                :label="categoryLabels[category]"
                :value="category"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="设备">
            <el-select v-model="messageForm.deviceId" clearable filterable placeholder="全部设备">
              <el-option
                v-for="device in devices"
                :key="device.deviceId"
                :label="`${device.stationName} / ${device.deviceName}`"
                :value="device.deviceId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="测点">
            <el-select v-model="messageForm.tagId" clearable filterable placeholder="全部测点">
              <el-option
                v-for="tag in messageTagOptions"
                :key="tag.id"
                :label="`${tag.tagName} (${tag.tagCode})`"
                :value="tag.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="开始时间">
            <el-input v-model="messageForm.startTime" type="datetime-local" />
          </el-form-item>

          <el-form-item label="结束时间">
            <el-input v-model="messageForm.endTime" type="datetime-local" />
          </el-form-item>

          <el-form-item label="关键词">
            <el-input v-model="messageForm.keyword" clearable placeholder="设备、测点、告警类型" />
          </el-form-item>

          <el-form-item class="form-actions">
            <el-button type="primary" :loading="isLoadingMessages" @click="queryMessages">查询</el-button>
          </el-form-item>
        </el-form>

        <el-table :data="messages" border stripe height="520" v-loading="isLoadingMessages">
          <el-table-column label="类型" width="110">
            <template #default="{ row }">
              <el-tag>{{ categoryLabels[row.category as MessageCategory] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="eventTime" label="时间" min-width="160">
            <template #default="{ row }">{{ formatTime(row.eventTime) }}</template>
          </el-table-column>
          <el-table-column prop="deviceName" label="设备" min-width="150" />
          <el-table-column prop="tagName" label="测点" min-width="150" />
          <el-table-column label="数值" width="120">
            <template #default="{ row }">
              {{ row.value ?? '-' }}{{ row.unit ? ` ${row.unit}` : '' }}
            </template>
          </el-table-column>
          <el-table-column label="告警/工单" min-width="150">
            <template #default="{ row }">
              {{ row.alarmType || row.assignee || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="messageStatusType(row)">{{ messageStatusLabel(row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="feedback" label="反馈" min-width="180" show-overflow-tooltip />
        </el-table>
      </section>

      <section v-else-if="activeTab === 'history'" class="panel">
        <el-form class="query-form history-form" :model="historyForm" label-position="top">
          <el-form-item label="设备">
            <el-select v-model="historyForm.deviceId" clearable filterable placeholder="全部设备">
              <el-option
                v-for="device in devices"
                :key="device.deviceId"
                :label="`${device.stationName} / ${device.deviceName}`"
                :value="device.deviceId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="测点">
            <el-select v-model="historyForm.tagId" clearable filterable placeholder="全部测点">
              <el-option
                v-for="tag in historyTagOptions"
                :key="tag.id"
                :label="`${tag.tagName} (${tag.tagCode})`"
                :value="tag.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="采样频率">
            <el-select v-model="historyForm.freqFlag" clearable placeholder="全部">
              <el-option label="分钟级" :value="0" />
              <el-option label="秒级" :value="1" />
            </el-select>
          </el-form-item>

          <el-form-item label="开始时间">
            <el-input v-model="historyForm.startTime" type="datetime-local" />
          </el-form-item>

          <el-form-item label="结束时间">
            <el-input v-model="historyForm.endTime" type="datetime-local" />
          </el-form-item>

          <el-form-item class="form-actions">
            <el-button @click="resetHistoryRange">最近一小时</el-button>
            <el-button type="primary" :loading="isLoadingHistory" @click="queryHistory">查询</el-button>
          </el-form-item>
        </el-form>

        <div ref="chartEl" class="history-chart"></div>

        <el-table :data="historyRows" border stripe height="420" v-loading="isLoadingHistory">
          <el-table-column prop="sampleTime" label="采样时间" min-width="170">
            <template #default="{ row }">{{ formatTime(row.sampleTime) }}</template>
          </el-table-column>
          <el-table-column prop="deviceName" label="设备" min-width="150" />
          <el-table-column prop="tagName" label="测点" min-width="150" />
          <el-table-column label="数值" width="130">
            <template #default="{ row }">{{ row.value }}{{ row.unit ? ` ${row.unit}` : '' }}</template>
          </el-table-column>
          <el-table-column label="频率" width="100">
            <template #default="{ row }">
              <el-tag :type="row.freqFlag === 1 ? 'warning' : 'success'">{{ freqLabel(row.freqFlag) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="质量" width="100">
            <template #default="{ row }">{{ row.qualityFlag === 0 ? '正常' : row.qualityFlag === 1 ? '可疑' : '无效' }}</template>
          </el-table-column>
        </el-table>
      </section>

      <section v-else class="panel simulation-panel">
        <div class="simulation-header">
          <div>
            <h2>模拟数据写入</h2>
            <p>仅系统管理员可用。正常模式写入分钟级采样，异常开关打开后写入高频异常采样并生成告警和工单。</p>
          </div>
          <el-tag :type="simulation?.running ? 'success' : 'info'">
            {{ simulation?.running ? '运行中' : '已停止' }}
          </el-tag>
        </div>

        <div class="simulation-actions">
          <el-button type="primary" :disabled="simulation?.running" :loading="isSimulationBusy" @click="handleStartSimulation">
            开始模拟数据
          </el-button>
          <el-button type="danger" :disabled="!simulation?.running" :loading="isSimulationBusy" @click="handleStopSimulation">
            停止
          </el-button>
          <el-switch
            :model-value="simulation?.anomalyEnabled ?? false"
            active-text="模拟异常数据"
            :disabled="!simulation?.running || isSimulationBusy"
            @change="handleAnomalyChange"
          />
        </div>

        <div class="overview-grid simulation-metrics">
          <div class="metric">
            <span>写入采样</span>
            <strong>{{ simulation?.writeCount ?? 0 }}</strong>
          </div>
          <div class="metric">
            <span>生成告警</span>
            <strong>{{ simulation?.alarmCount ?? 0 }}</strong>
          </div>
          <div class="metric">
            <span>生成工单</span>
            <strong>{{ simulation?.taskCount ?? 0 }}</strong>
          </div>
          <div class="metric">
            <span>最近写入</span>
            <strong class="metric-time">{{ formatTime(simulation?.lastWriteAt) }}</strong>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
.dashboard-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  background: #edf2f7;
  color: #0f172a;
}

.sidebar {
  border-right: 1px solid #d8dee8;
  background: #101827;
  color: #e2e8f0;
}

.brand {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 22px 18px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.16);
}

.brand-mark {
  display: inline-flex;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: #2563eb;
  color: #fff;
  font-weight: 800;
}

.brand strong,
.brand small {
  display: block;
}

.brand small {
  color: #94a3b8;
  font-size: 12px;
}

.nav-menu {
  border-right: none;
}

.workspace {
  min-width: 0;
  padding: 24px;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.topbar h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 750;
  letter-spacing: 0;
}

.topbar p {
  margin-top: 4px;
  color: #64748b;
}

.error-alert {
  margin-bottom: 16px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.metric {
  min-height: 78px;
  padding: 14px 16px;
  border: 1px solid #d8dee8;
  border-radius: 8px;
  background: #fff;
  box-sizing: border-box;
}

.metric span {
  display: block;
  color: #64748b;
  font-size: 13px;
}

.metric strong {
  display: block;
  margin-top: 8px;
  font-size: 24px;
}

.metric-time {
  font-size: 15px !important;
}

.panel {
  padding: 16px;
  border: 1px solid #d8dee8;
  border-radius: 8px;
  background: #fff;
}

.query-form {
  display: grid;
  grid-template-columns: repeat(6, minmax(140px, 1fr));
  gap: 12px;
  align-items: end;
  margin-bottom: 16px;
}

.history-form {
  grid-template-columns: repeat(6, minmax(140px, 1fr));
}

.form-actions {
  align-self: end;
}

.history-chart {
  width: 100%;
  height: 260px;
  margin-bottom: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.simulation-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.simulation-header h2 {
  margin: 0 0 6px;
  font-size: 20px;
  letter-spacing: 0;
}

.simulation-header p {
  color: #64748b;
}

.simulation-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.simulation-metrics {
  margin-bottom: 0;
}

@media (max-width: 980px) {
  .dashboard-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    border-right: none;
  }

  .query-form,
  .overview-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .workspace {
    padding: 16px;
  }

  .query-form,
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .topbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }
}
</style>
