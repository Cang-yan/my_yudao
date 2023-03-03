import request from '@/utils/request'

// 创建关键词标注统计;
export function createKeywordsTagscount(data) {
  return request({
    url: '/identity/keywords-tagscount/create',
    method: 'post',
    data: data
  })
}

// 更新关键词标注统计;
export function updateKeywordsTagscount(data) {
  return request({
    url: '/identity/keywords-tagscount/update',
    method: 'put',
    data: data
  })
}

// 删除关键词标注统计;
export function deleteKeywordsTagscount(id) {
  return request({
    url: '/identity/keywords-tagscount/delete?id=' + id,
    method: 'delete'
  })
}

// 获得关键词标注统计;
export function getKeywordsTagscount(id) {
  return request({
    url: '/identity/keywords-tagscount/get?id=' + id,
    method: 'get'
  })
}

// 获得关键词标注统计;分页
export function getKeywordsTagscountPage(query) {
  return request({
    url: '/identity/archives/extends/get/records/keywords',
    method: 'post',
    data: query
  })
}

// 导出关键词标注统计; Excel
export function exportKeywordsTagscountExcel(query) {
  return request({
    url: '/identity/keywords-tagscount/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
