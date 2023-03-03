import request from '@/utils/request'

// 创建用户标注库;
export function createTagsStore(data) {
  return request({
    url: '/identity/tags-store/create',
    method: 'post',
    data: data
  })
}

// 更新用户标注库;
export function updateTagsStore(data) {
  return request({
    url: '/identity/tags-store/update',
    method: 'put',
    data: data
  })
}

// 删除用户标注库;
export function deleteTagsStore(id) {
  return request({
    url: '/identity/tags-store/delete?id=' + id,
    method: 'delete'
  })
}

// 获得用户标注库;
export function getTagsStore(id) {
  return request({
    url: '/identity/tags-store/get?id=' + id,
    method: 'get'
  })
}

// 获得用户标注库;分页
export function getTagsStorePage(query) {
  return request({
    url: '/identity/tags-store/page',
    method: 'get',
    params: query
  })
}

// 导出用户标注库; Excel
export function exportTagsStoreExcel(query) {
  return request({
    url: '/identity/tags-store/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
