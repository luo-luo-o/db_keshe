import type { AuthSession } from '../types/auth'

interface RequestOptions {
  method?: string
  params?: object
  body?: unknown
}

export async function apiGet<TResponse>(
  url: string,
  session: AuthSession,
  params?: object,
): Promise<TResponse> {
  return request<TResponse>(url, session, { params })
}

export async function apiPost<TResponse>(
  url: string,
  session: AuthSession,
  body?: unknown,
  method = 'POST',
): Promise<TResponse> {
  return request<TResponse>(url, session, { method, body })
}

export async function apiDelete(url: string, session: AuthSession): Promise<void> {
  await request<void>(url, session, { method: 'DELETE' })
}

async function request<TResponse>(
  url: string,
  session: AuthSession,
  options: RequestOptions,
): Promise<TResponse> {
  const target = withQuery(url, options.params)
  const hasBody = options.body !== undefined
  let response: Response

  try {
    response = await fetch(target, {
      method: options.method ?? 'GET',
      headers: {
        ...authHeaders(session),
        ...(hasBody ? { 'Content-Type': 'application/json' } : {}),
      },
      body: hasBody ? JSON.stringify(options.body) : undefined,
    })
  } catch {
    throw new Error('服务端连接失败，请检查后端服务或网络')
  }

  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }

  if (response.status === 204) {
    return undefined as TResponse
  }

  const contentType = response.headers.get('Content-Type') ?? ''
  if (contentType.includes('application/json')) {
    return response.json() as Promise<TResponse>
  }

  const text = await response.text()
  return text as TResponse
}

function withQuery(url: string, params?: object) {
  const query = new URLSearchParams()
  Object.entries(params ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.set(key, String(value))
    }
  })

  return query.size > 0 ? `${url}?${query.toString()}` : url
}

function authHeaders(session: AuthSession) {
  return {
    'X-User-Id': String(session.id),
    'X-Role-Code': session.roleCode,
  }
}

async function readErrorMessage(response: Response) {
  const contentType = response.headers.get('Content-Type') ?? ''
  if (contentType.includes('application/json')) {
    const body = (await response.json().catch(() => null)) as { message?: string } | null
    return body?.message ?? '请求失败'
  }

  return response.text().catch(() => '请求失败')
}
