import type { AuthResponse, AuthSession } from './types/auth'

const SESSION_KEY = 'psm-smart-session'

export function toSession(response: AuthResponse): AuthSession {
  if (!response.id || !response.username || !response.displayName || !response.roleCode) {
    throw new Error('登录响应缺少用户信息')
  }

  return {
    id: response.id,
    username: response.username,
    displayName: response.displayName,
    roleCode: response.roleCode,
  }
}

export function loadSession(): AuthSession | null {
  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) {
    return null
  }

  try {
    const parsed = JSON.parse(raw) as AuthSession
    if (!parsed.id || !parsed.username || !parsed.displayName || !parsed.roleCode) {
      return null
    }
    return parsed
  } catch {
    return null
  }
}

export function saveSession(session: AuthSession) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY)
}
