import request from '@/utils/request'

// 创建文件夹和关键词的关系;
export function createFolderKeywordsRelation(data) {
  return request({
    url: '/identity/folder-keywords-relation/create',
    method: 'post',
    data: data
  })
}

// 更新文件夹和关键词的关系;
export function updateFolderKeywordsRelation(data) {
  return request({
    url: '/identity/folder-keywords-relation/update',
    method: 'put',
    data: data
  })
}

// 删除文件夹和关键词的关系;
export function deleteFolderKeywordsRelation(id) {
  return request({
    url: '/identity/folder-keywords-relation/delete?id=' + id,
    method: 'delete'
  })
}

// 获得文件夹和关键词的关系;
export function getFolderKeywordsRelation(id) {
  return request({
    url: '/identity/folder-keywords-relation/get?id=' + id,
    method: 'get'
  })
}

// 获得文件夹和关键词的关系;分页
export function getFolderKeywordsRelationPage(query) {
  return request({
    url: '/identity/folder-keywords-relation/page',
    method: 'get',
    params: query
  })
}

// 导出文件夹和关键词的关系; Excel
export function exportFolderKeywordsRelationExcel(query) {
  return request({
    url: '/identity/folder-keywords-relation/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
