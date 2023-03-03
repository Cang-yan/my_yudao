import request from '@/utils/request'

// 创建关键词;
export function createKeywords(data) {
  return request({
    url: '/identity/keywords/create',
    method: 'post',
    data: data
  })
}

// 更新关键词;
export function updateKeywords(data) {
  return request({
    url: '/identity/keywords/update',
    method: 'put',
    data: data
  })
}

// 删除关键词;
export function deleteKeywords(id) {
  return request({
    url: '/identity/keywords/delete?id=' + id,
    method: 'delete'
  })
}

// 获得关键词;
export function getKeywords(id) {
  return request({
    url: '/identity/keywords/get?id=' + id,
    method: 'get'
  })
}

// 获得关键词;分页
export function getKeywordsPage(query) {
  return request({
    url: '/identity/keywords/page',
    method: 'get',
    params: query
  })
}

// 导出关键词; Excel
export function exportKeywordsExcel(query) {
  return request({
    url: '/identity/keywords/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
