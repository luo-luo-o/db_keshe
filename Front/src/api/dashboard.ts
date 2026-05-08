import { apiGet, apiPost } from './http'
import type { AuthSession } from '../types/auth'
import type {
  DeviceOptionResponse,
  HistoryDataRow,
  HistoryQuery,
  MessageQuery,
  MessageResponse,
  SimulationStatusResponse,
} from '../types/dashboard'

export function fetchDevices(session: AuthSession) {
  return apiGet<DeviceOptionResponse[]>('/api/metadata/devices', session)
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

export function startSimulation(session: AuthSession) {
  return apiPost<SimulationStatusResponse>('/api/simulation/start', session)
}

export function stopSimulation(session: AuthSession) {
  return apiPost<SimulationStatusResponse>('/api/simulation/stop', session)
}

export function setSimulationAnomaly(session: AuthSession, enabled: boolean) {
  return apiPost<SimulationStatusResponse>('/api/simulation/anomaly', session, { enabled }, 'PUT')
}
