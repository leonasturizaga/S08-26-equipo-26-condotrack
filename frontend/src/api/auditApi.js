import { apiRequest } from './apiClient.js'

export function getAudit(params = {}) {
  const search = new URLSearchParams()
  search.set('page', params.page ?? 0)
  search.set('size', params.size ?? 25)
  if (params.entityType) search.set('entityType', params.entityType)
  if (params.action) search.set('action', params.action)
  if (params.buildingId) search.set('buildingId', params.buildingId)
  if (params.actorEmail) search.set('actorEmail', params.actorEmail)
  if (params.from) search.set('from', new Date(params.from).toISOString())
  if (params.to) search.set('to', new Date(params.to).toISOString())
  return apiRequest(`/api/audit?${search.toString()}`)
}
