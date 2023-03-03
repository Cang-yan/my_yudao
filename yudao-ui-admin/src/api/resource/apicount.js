import request from '@/utils/request'

// 创建调用次数;
export function createApicount(data) {
  return request({
    url: '/resource/apicount/create',
    method: 'post',
    data: data
  })
}

// 更新调用次数;
export function updateApicount(data) {
  return request({
    url: '/resource/apicount/update',
    method: 'put',
    data: data
  })
}

// 删除调用次数;
export function deleteApicount(id) {
  return request({
    url: '/resource/apicount/delete?id=' + id,
    method: 'delete'
  })
}

// 获得调用次数;
export function getApicount(id) {
  return request({
    url: '/resource/apicount/get?id=' + id,
    method: 'get'
  })
}

// 获得调用次数;分页
export function getApicountPage(query) {
  return request({
    url: '/resource/apicount/page',
    method: 'get',
    params: query
  })
}

// 导出调用次数; Excel
export function exportApicountExcel(query) {
  return request({
    url: '/resource/apicount/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
