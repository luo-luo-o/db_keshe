import { apiDelete, apiGet, apiPost } from './http'
import type { AuthSession } from '../types/auth'
import type {
  CreateCircuitPayload,
  CreateMeasurePointPayload,
  CreateTransformerPayload,
  HistoryDataRow,
  HistoryQuery,
  MaintenanceTaskResponse,
  TransformerOptionResponse,
  MessageQuery,
  MessageResponse,
  RuntimeLogLevel,
  RuntimeLogResponse,
  SimulationStatusResponse,
  TaskQuery,
  TaskUpdatePayload,
  UpdateCircuitPayload,
  UpdateMeasurePointPayload,
  UpdateTransformerPayload,
} from '../types/dashboard'

export function fetchTransformers(session: AuthSession) {
  return apiGet<TransformerOptionResponse[]>('/api/metadata/transformers', session)
}

export function createTransformer(session: AuthSession, payload: CreateTransformerPayload) {
  return apiPost<TransformerOptionResponse>('/api/metadata/transformers', session, payload)
}

export function updateTransformer(session: AuthSession, transformerId: number, payload: UpdateTransformerPayload) {
  return apiPost<TransformerOptionResponse>(`/api/metadata/transformers/${transformerId}`, session, payload, 'PUT')
}

export function deleteTransformer(session: AuthSession, transformerId: number) {
  return apiDelete(`/api/metadata/transformers/${transformerId}`, session)
}

export function createCircuit(session: AuthSession, transformerId: number, payload: CreateCircuitPayload) {
  return apiPost(`/api/metadata/transformers/${transformerId}/circuits`, session, payload)
}

export function updateCircuit(session: AuthSession, circuitId: number, payload: UpdateCircuitPayload) {
  return apiPost(`/api/metadata/circuits/${circuitId}`, session, payload, 'PUT')
}

export function deleteCircuit(session: AuthSession, circuitId: number) {
  return apiDelete(`/api/metadata/circuits/${circuitId}`, session)
}

export function createMeasurePoint(session: AuthSession, transformerId: number, payload: CreateMeasurePointPayload) {
  return apiPost(`/api/metadata/transformers/${transformerId}/points`, session, payload)
}

export function updateMeasurePoint(session: AuthSession, pointId: number, payload: UpdateMeasurePointPayload) {
  return apiPost(`/api/metadata/points/${pointId}`, session, payload, 'PUT')
}

export function deleteMeasurePoint(session: AuthSession, pointId: number) {
  return apiDelete(`/api/metadata/points/${pointId}`, session)
}

export function fetchMessages(session: AuthSession, query: MessageQuery) {
  return apiGet<MessageResponse[]>('/api/messages', session, query)
}

export function fetchHistory(session: AuthSession, query: HistoryQuery) {
  return apiGet<HistoryDataRow[]>('/api/history', session, query)
}

export function fetchSimulationStatus(session: AuthSession) {
  return apiGet<SimulationStatusResponse>('/api/simulation/status', session)
}

export function fetchRuntimeLogs(session: AuthSession, level: RuntimeLogLevel = 'INFO') {
  return apiGet<RuntimeLogResponse[]>('/api/runtime-logs', session, { level })
}

export function fetchTasks(session: AuthSession, query: TaskQuery) {
  return apiGet<MaintenanceTaskResponse[]>('/api/tasks', session, query)
}

export function updateTask(session: AuthSession, taskId: number, payload: TaskUpdatePayload) {
  return apiPost<MaintenanceTaskResponse>(`/api/tasks/${taskId}`, session, payload, 'PUT')
}

export function startSimulation(session: AuthSession) {
  return apiPost<SimulationStatusResponse>('/api/simulation/start', session)
}

export function stopSimulation(session: AuthSession) {
  return apiPost<SimulationStatusResponse>('/api/simulation/stop', session)
}

export function setSimulationAnomaly(session: AuthSession, enabled: boolean) {
  return apiPost<SimulationStatusResponse>('/api/simulation/anomaly', session, { enabled }, 'PUT')
}
