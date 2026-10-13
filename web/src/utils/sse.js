import { useUserStore } from '@/stores/user'

/**
 * 以 POST + fetch 读取 SSE 流（EventSource 不支持自定义请求头与 POST）。
 *
 * 后端每帧下发一个 JSON：{ type: 'delta' | 'done' | 'error', content }
 *
 * @param {string} url
 * @param {Object} body
 * @param {Object} handlers { onDelta, onDone, onError }
 * @param {AbortSignal} signal
 */
export async function postStream(url, body, handlers = {}, signal) {
  const userStore = useUserStore()
  const headers = { 'Content-Type': 'application/json' }
  if (userStore.token) {
    headers.Authorization = `Bearer ${userStore.token}`
  }

  let response
  try {
    response = await fetch(url, {
      method: 'POST',
      headers,
      body: JSON.stringify(body),
      signal
    })
  } catch (error) {
    if (error.name === 'AbortError') {
      handlers.onDone?.({ aborted: true })
      return
    }
    handlers.onError?.(error)
    return
  }

  if (!response.ok || !response.body) {
    handlers.onError?.(new Error(`服务返回异常状态码 ${response.status}`))
    return
  }

  // 未登录、参数校验失败等场景后端返回的是统一响应体 JSON 而非 SSE 帧，
  // 若不识别会出现「点了发送但毫无反应」的静默失败。
  const contentType = response.headers.get('content-type') || ''
  if (!contentType.includes('text/event-stream')) {
    let message = `服务返回异常状态码 ${response.status}`
    try {
      const body = JSON.parse(await response.text())
      if (body && body.msg) {
        message = body.msg
      }
    } catch {
      // 非 JSON 响应时保留默认提示
    }
    handlers.onError?.(new Error(message))
    return
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      // 逐行解析：兼容 CRLF / LF 以及以空行分隔的 SSE 帧
      let newlineIndex = buffer.indexOf('\n')
      while (newlineIndex !== -1) {
        const rawLine = buffer.slice(0, newlineIndex)
        buffer = buffer.slice(newlineIndex + 1)
        newlineIndex = buffer.indexOf('\n')

        const line = rawLine.trim()
        if (!line.startsWith('data:')) continue

        const payload = line.slice(5).trim()
        if (!payload) continue

        try {
          const event = JSON.parse(payload)
          if (event.type === 'delta') {
            handlers.onDelta?.(event.content || '')
          } else if (event.type === 'done') {
            handlers.onDone?.({ aborted: false })
          } else if (event.type === 'error') {
            handlers.onError?.(new Error(event.content || 'AI 服务返回错误'))
          }
        } catch {
          // 忽略非法帧，继续读取后续内容
        }
      }
    }
    handlers.onDone?.({ aborted: false })
  } catch (error) {
    if (error.name === 'AbortError') {
      handlers.onDone?.({ aborted: true })
      return
    }
    handlers.onError?.(error)
  }
}
