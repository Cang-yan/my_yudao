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
export function updateApicount(query) {
  // console.log(data)
  return request({
    url: '/identity/apiresource/extends/create?' + 'applicationName=' + query,
    method: 'get'
  })
}

//查看API 密钥
export function secretkey(query) {
  return request({
    url: '/identity/apiresource/extends/get/secret' + '?appId=' + query,
    method: 'get'
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
    url: '/identity/apiresource/extends/get/appdetail' + id,
    method: 'get'
  })
}

// 获得调用次数;分页
export function getApicountPage(query) {
  return request({
    url: '/identity/apiresource/extends/get/appdetail',
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

// 购买积分的接口
export function bugScoreAPI(data) {
  return request({
    url: '/identity/apiresource/extends/exchange',
    method: 'get',
    params: data
  })
}