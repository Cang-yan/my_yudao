import request from '@/utils/request'

// 创建api积分;
export function createScores(data) {
  return request({
    url: '/resource/scores/create',
    method: 'post',
    data: data
  })
}

// 更新api积分;
export function updateScores(data) {
  return request({
    url: '/resource/scores/update',
    method: 'put',
    data: data
  })
}

// 删除api积分;
export function deleteScores(id) {
  return request({
    url: '/resource/scores/delete?id=' + id,
    method: 'delete'
  })
}

// 获得api积分;
export function getScores(id) {
  return request({
    url: '/resource/scores/get?id=' + id,
    method: 'get'
  })
}

// 获得api积分;分页
export function getScoresPage(query) {
  return request({
    url: '/resource/scores/page',
    method: 'get',
    params: query
  })
}

// 导出api积分; Excel
export function exportScoresExcel(query) {
  return request({
    url: '/resource/scores/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
