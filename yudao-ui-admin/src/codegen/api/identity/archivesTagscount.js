import request from '@/utils/request'

// 创建档案标注统计;
export function createArchivesTagscount(data) {
  return request({
    url: '/identity/archives-tagscount/create',
    method: 'post',
    data: data
  })
}

// 更新档案标注统计;
export function updateArchivesTagscount(data) {
  return request({
    url: '/identity/archives-tagscount/update',
    method: 'put',
    data: data
  })
}

// 删除档案标注统计;
export function deleteArchivesTagscount(id) {
  return request({
    url: '/identity/archives-tagscount/delete?id=' + id,
    method: 'delete'
  })
}

// 获得档案标注统计;
export function getArchivesTagscount(id) {
  return request({
    url: '/identity/archives-tagscount/get?id=' + id,
    method: 'get'
  })
}

// 获得档案标注统计;分页
export function getArchivesTagscountPage(query) {
  return request({
    url: '/identity/archives-tagscount/page',
    method: 'get',
    params: query
  })
}

// 导出档案标注统计; Excel
export function exportArchivesTagscountExcel(query) {
  return request({
    url: '/identity/archives-tagscount/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
