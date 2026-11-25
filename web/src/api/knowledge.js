import request from './request'

export function listDocuments() {
  return request.get('/knowledge/documents')
}

export function uploadDocument(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/knowledge/documents', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

export function removeDocument(id) {
  return request.delete(`/knowledge/documents/${id}`)
}
