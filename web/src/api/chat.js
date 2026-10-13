import { postStream } from '@/utils/sse'

/**
 * 流式对话。
 *
 * @param {Object} payload   { sessionId, content, model }
 * @param {Object} handlers  { onDelta, onDone, onError }
 * @param {AbortSignal} signal 用于「停止生成」
 */
export function streamChat(payload, handlers, signal) {
  return postStream('/api/chat/stream', payload, handlers, signal)
}

/** 非流式对话（预留）。 */
export function chat(payload) {
  return fetch('/api/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${localStorage.getItem('sqs_token') || ''}`
    },
    body: JSON.stringify(payload)
  }).then((res) => res.json())
}
