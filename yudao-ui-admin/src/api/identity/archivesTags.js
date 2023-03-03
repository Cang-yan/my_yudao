import request from '@/utils/request'

// 创建标注 档案;
export function createArchivesTags(data) {
  return request({
    url: '/identity/archives-tags/create',
    method: 'post',
    data: data
  })
}

// 更新标注 档案;
export function updateArchivesTags(data) {
  return request({
    url: '/identity/archives-tags/update',
    method: 'put',
    data: data
  })
}

// 删除标注 档案;
export function deleteArchivesTags(id) {
  return request({
    url: '/identity/archives-tags/delete?id=' + id,
    method: 'delete'
  })
}

// 获得标注 档案;
export function getArchivesTags(id) {
  return request({
    url: '/identity/archives-tags/get?id=' + id,
    method: 'get'
  })
}

// 获得标注 档案;分页
export function getArchivesTagsPage(query) {
  return request({
    url: '/identity/archives-tags/page',
    method: 'get',
    params: query
  })
}

// 导出标注 档案; Excel
export function exportArchivesTagsExcel(query) {
  return request({
    url: '/identity/archives-tags/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
