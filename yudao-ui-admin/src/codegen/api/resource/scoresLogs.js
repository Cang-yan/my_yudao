import request from '@/utils/request'

// 创建积分记录日志;
export function createScoresLogs(data) {
  return request({
    url: '/resource/scores-logs/create',
    method: 'post',
    data: data
  })
}

// 更新积分记录日志;
export function updateScoresLogs(data) {
  return request({
    url: '/resource/scores-logs/update',
    method: 'put',
    data: data
  })
}

// 删除积分记录日志;
export function deleteScoresLogs(id) {
  return request({
    url: '/resource/scores-logs/delete?id=' + id,
    method: 'delete'
  })
}

// 获得积分记录日志;
export function getScoresLogs(id) {
  return request({
    url: '/resource/scores-logs/get?id=' + id,
    method: 'get'
  })
}

// 获得积分记录日志;分页
export function getScoresLogsPage(query) {
  return request({
    url: '/resource/scores-logs/page',
    method: 'get',
    params: query
  })
}

// 导出积分记录日志; Excel
export function exportScoresLogsExcel(query) {
  return request({
    url: '/resource/scores-logs/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
