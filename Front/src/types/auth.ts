export type RoleCode = 'ADMIN' | 'OPERATOR' | 'ENGINEER' | 'MANAGER'

export const roleLabels: Record<RoleCode, string> = {
  ADMIN: '系统管理员',
  OPERATOR: '运行监控员',
  ENGINEER: '维保工程师',
  MANAGER: '管理人员',
}

export const roleOptions: Array<{ label: string; value: RoleCode }> = [
  { label: roleLabels.ADMIN, value: 'ADMIN' },
  { label: roleLabels.OPERATOR, value: 'OPERATOR' },
  { label: roleLabels.ENGINEER, value: 'ENGINEER' },
  { label: roleLabels.MANAGER, value: 'MANAGER' },
]

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  displayName: string
  roleCode: RoleCode
}

export interface AuthResponse {
  id?: number
  username?: string
  displayName?: string
  roleCode?: RoleCode
}

export interface AuthSession {
  id: number
  username: string
  displayName: string
  roleCode: RoleCode
}
