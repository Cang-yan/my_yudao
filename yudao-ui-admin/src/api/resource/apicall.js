import request from '@/utils/request'

// 创建api调用资源;
export function createApicall(data) {
  return request({
    url: '/resource/apicall/create',
    method: 'post',
    data: data
  })
}

// 更新api调用资源;
export function updateApicall(data) {
  return request({
    url: '/resource/apicall/update',
    method: 'put',
    data: data
  })
}

// 删除api调用资源;
export function deleteApicall(id) {
  return request({
    url: '/resource/apicall/delete?id=' + id,
    method: 'delete'
  })
}

// 获得api调用资源;
export function getApicall(id) {
  return request({
    url: '/resource/apicall/get?id=' + id,
    method: 'get'
  })
}

// 获得api调用资源;分页
export function getApicallPage(query) {
  return request({
    url: '/resource/apicall/page',
    method: 'get',
    params: query
  })
}

// 导出api调用资源; Excel
export function exportApicallExcel(query) {
  return request({
    url: '/resource/apicall/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
