import request from '@/utils/request'

// 创建标注关键词;
export function createKeywordsTags(data) {
  return request({
    url: '/identity/keywords-tags/create',
    method: 'post',
    data: data
  })
}

// 更新标注关键词;
export function updateKeywordsTags(data) {
  return request({
    url: '/identity/keywords-tags/update',
    method: 'put',
    data: data
  })
}

// 删除标注关键词;
export function deleteKeywordsTags(id) {
  return request({
    url: '/identity/keywords-tags/delete?id=' + id,
    method: 'delete'
  })
}

// 获得标注关键词;
export function getKeywordsTags(id) {
  return request({
    url: '/identity/keywords-tags/get?id=' + id,
    method: 'get'
  })
}

// 获得标注关键词;分页
export function getKeywordsTagsPage(query) {
  return request({
    url: '/identity/keywords-tags/page',
    method: 'get',
    params: query
  })
}

// 导出标注关键词; Excel
export function exportKeywordsTagsExcel(query) {
  return request({
    url: '/identity/keywords-tags/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
