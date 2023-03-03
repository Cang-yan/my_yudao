import request from '@/utils/request'

// 创建档案正文;
export function createArchivesText(data) {
  return request({
    url: '/identity/archives-text/create',
    method: 'post',
    data: data
  })
}

// 更新档案正文;
export function updateArchivesText(data) {
  return request({
    url: '/identity/archives-text/update',
    method: 'put',
    data: data
  })
}

// 删除档案正文;
export function deleteArchivesText(id) {
  return request({
    url: '/identity/archives-text/delete?id=' + id,
    method: 'delete'
  })
}

// 获得档案正文;
export function getArchivesText(id) {
  return request({
    url: '/identity/archives-text/get?id=' + id,
    method: 'get'
  })
}

// 获得档案正文;分页
export function getArchivesTextPage(query) {
  return request({
    url: '/identity/archives-text/page',
    method: 'get',
    params: query
  })
}

// 导出档案正文; Excel
export function exportArchivesTextExcel(query) {
  return request({
    url: '/identity/archives-text/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
