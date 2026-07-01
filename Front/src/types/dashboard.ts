import type { RoleCode } from './auth'

export type MessageCategory = 'SAMPLE' | 'ALARM' | 'TASK'
export type DeviceStatus = 0 | 1 | 2
export type MeasurePointStatus = 0 | 1
export type CircuitDirection = 'INCOMING' | 'OUTGOING'
export type PointGroup = 'ELECTRIC_IN' | 'ELECTRIC_OUT' | 'TRANSFORMER' | 'CABINET'
export type PhaseCode = 'A' | 'B' | 'C' | 'AB' | 'BC' | 'CA' | 'TOTAL' | 'NONE'

export interface MeasurePointOptionResponse {
  id: number
  pointCode: string
  pointName: string
  pointGroup: PointGroup
  measureType: string
  phaseCode: PhaseCode
  unit?: string
  minLimit?: number
  maxLimit?: number
  rateLimit?: number
  status: MeasurePointStatus
}

export interface CircuitOptionResponse {
  circuitId: number
  circuitCode: string
  circuitName: string
  direction: CircuitDirection
  ratedVoltageKv?: number
  ratedCurrentA?: number
  status: DeviceStatus
  points: MeasurePointOptionResponse[]
}

export interface TransformerOptionResponse {
  transformerId: number
  transformerCode: string
  transformerName: string
  transformerType: string
  ratedCapacityKva?: number
  ratedVoltageRatio?: string
  commissionDate?: string
  manufacturer?: string
  oilLevel?: number
  location?: string
  status: DeviceStatus
  circuits: CircuitOptionResponse[]
  points: MeasurePointOptionResponse[]
}

export interface CreateTransformerPayload {
  transformerCode: string
  transformerName: string
  ratedCapacityKva?: number
  ratedVoltageRatio?: string
  commissionDate?: string
  manufacturer?: string
  oilLevel?: number
  location?: string
  status: DeviceStatus
}

export interface UpdateTransformerPayload extends CreateTransformerPayload {}

export interface CreateCircuitPayload {
  circuitCode: string
  circuitName: string
  direction: CircuitDirection
  ratedVoltageKv?: number
  ratedCurrentA?: number
  status: DeviceStatus
}

export interface UpdateCircuitPayload extends CreateCircuitPayload {}

export interface CreateMeasurePointPayload {
  circuitId?: number
  pointCode: string
  pointName: string
  pointGroup: PointGroup
  measureType: string
  phaseCode: PhaseCode
  unit?: string
  minLimit?: number
  maxLimit?: number
  rateLimit?: number
  status: MeasurePointStatus
}

export interface UpdateMeasurePointPayload extends CreateMeasurePointPayload {}

export interface MessageResponse {
  category: MessageCategory
  id: number
  transformerId: number
  transformerName: string
  circuitId?: number
  circuitName?: string
  pointId?: number
  pointName?: string
  pointCode?: string
  eventTime: string
  value?: number
  unit?: string
  qualityFlag?: number
  alarmType?: string
  alarmLevel?: string
  status?: number
  assignee?: string
  feedback?: string
}

export interface MessageQuery {
  category?: MessageCategory
  transformerId?: number
  circuitId?: number
  pointId?: number
  startTime?: string
  endTime?: string
  keyword?: string
}

export interface HistoryDataRow {
  id: number
  transformerId: number
  transformerName: string
  circuitId?: number
  circuitName?: string
  pointId?: number
  pointName?: string
  pointCode?: string
  unit?: string
  sampleTime: string
  rangeEndTime?: string
  value: number
  avgValue?: number
  minValue?: number
  maxValue?: number
  sampleCount?: number
  qualityFlag: number
  granularity?: string
  createdAt: string
}

export interface HistoryQuery {
  transformerId?: number
  circuitId?: number
  pointId?: number
  startTime?: string
  endTime?: string
}

export interface MaintenanceTaskResponse {
  taskId: number
  alarmId: number
  transformerId: number
  transformerName: string
  circuitId?: number
  circuitName?: string
  pointId?: number
  pointName?: string
  pointCode?: string
  unit?: string
  alarmType: string
  alarmLevel: string
  alarmValue?: number
  alarmTime: string
  status: number
  assignee?: string
  feedback?: string
  createdAt: string
  updatedAt?: string
  finishedAt?: string
}

export interface TaskQuery {
  status?: number
  transformerId?: number
  circuitId?: number
  startTime?: string
  endTime?: string
  keyword?: string
}

export interface TaskUpdatePayload {
  status: number
  assignee?: string
  feedback?: string
}

export interface SimulationStatusResponse {
  running: boolean
  anomalyEnabled: boolean
  startedAt?: string
  lastWriteAt?: string
  writeCount: number
  alarmCount: number
  taskCount: number
  normalIntervalMs: number
  anomalyIntervalMs: number
  currentIntervalMs: number
}

export interface SimulationDataRow {
  id: number
  transformerId: number
  transformerName: string
  circuitId?: number
  circuitName?: string
  pointId: number
  pointName: string
  pointCode: string
  unit?: string
  sampleTime: string
  value: number
  qualityFlag: number
  createdAt: string
}

export interface SimulationDataPage {
  rows: SimulationDataRow[]
  total: number
  page: number
  size: number
}

export interface BackupSnapshotResponse {
  id: number
  snapshotName: string
  note?: string
  transformerCount: number
  circuitCount: number
  pointCount: number
  rawDataCount: number
  alarmCount: number
  taskCount: number
  createdBy?: string
  createdAt: string
  restoredAt?: string
  restoredBy?: string
}

export interface CreateBackupPayload {
  snapshotName?: string
  note?: string
}

export type RuntimeLogLevel = 'DEBUG' | 'INFO' | 'WARN' | 'ERROR'
export type RuntimeLogSource = 'DATABASE'

export interface RuntimeLogResponse {
  id: number
  source: RuntimeLogSource
  level: RuntimeLogLevel
  message: string
  context?: string
  createdAt: string
}

export const categoryLabels: Record<MessageCategory, string> = {
  SAMPLE: '采样数据',
  ALARM: '告警记录',
  TASK: '维护工单',
}

export const runtimeLogLevelLabels: Record<RuntimeLogLevel, string> = {
  DEBUG: '调试',
  INFO: '信息',
  WARN: '警告',
  ERROR: '错误',
}

export const runtimeLogLevelOptions: RuntimeLogLevel[] = ['INFO', 'WARN', 'ERROR', 'DEBUG']

export const pointGroupLabels: Record<PointGroup, string> = {
  ELECTRIC_IN: '进线测点',
  ELECTRIC_OUT: '出线测点',
  TRANSFORMER: '箱变本体',
  CABINET: '柜体测点',
}

export const phaseCodeOptions: PhaseCode[] = ['A', 'B', 'C', 'AB', 'BC', 'CA', 'TOTAL', 'NONE']

export function allowedCategories(roleCode: RoleCode): MessageCategory[] {
  if (roleCode === 'ENGINEER') {
    return ['ALARM', 'TASK']
  }

  return ['SAMPLE', 'ALARM', 'TASK']
}
