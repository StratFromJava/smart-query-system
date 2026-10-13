import request from './request'

export function listModels() {
  return request.get('/models')
}

export function listFeatures() {
  return request.get('/features')
}
