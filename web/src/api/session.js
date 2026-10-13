import request from './request'

export function listSessions(keyword) {
  return request.get('/session', { params: keyword ? { keyword } : {} })
}

export function createSession(payload) {
  return request.post('/session', payload || {})
}

export function listMessages(sessionId) {
  return request.get(`/session/${sessionId}/messages`)
}

export function renameSession(sessionId, title) {
  return request.patch(`/session/${sessionId}/title`, { title })
}

export function removeSession(sessionId) {
  return request.delete(`/session/${sessionId}`)
}
