import request from '@/utils/request'

// 创建元标注;
export function createTagsMeta(data) {
  return request({
    url: '/identity/tags-meta/create',
    method: 'post',
    data: data
  })
}

// 更新元标注;
export function updateTagsMeta(data) {
  return request({
    url: '/identity/tags-meta/update',
    method: 'put',
    data: data
  })
}

// 删除元标注;
export function deleteTagsMeta(id) {
  return request({
    url: '/identity/tags-meta/delete?id=' + id,
    method: 'delete'
  })
}

// 获得元标注;
export function getTagsMeta(id) {
  return request({
    url: '/identity/tags-meta/get?id=' + id,
    method: 'get'
  })
}

// 获得元标注;分页
export function getTagsMetaPage(query) {
  return request({
    url: '/identity/tags-meta/page',
    method: 'get',
    params: query
  })
}

// 导出元标注; Excel
export function exportTagsMetaExcel(query) {
  return request({
    url: '/identity/tags-meta/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
