import request from '@/utils/request'

// 创建关键词文件夹;
export function createFolderKeywords(data) {
  return request({
    url: '/identity/folder-keywords/create',
    method: 'post',
    data: data
  })
}

// 更新关键词文件夹;
export function updateFolderKeywords(data) {
  return request({
    url: '/identity/folder-keywords/update',
    method: 'put',
    data: data
  })
}

// 删除关键词文件夹;
export function deleteFolderKeywords(id) {
  return request({
    url: '/identity/folder-keywords/delete?id=' + id,
    method: 'delete'
  })
}

// 获得关键词文件夹;
export function getFolderKeywords(id) {
  return request({
    url: '/identity/folder-keywords/get?id=' + id,
    method: 'get'
  })
}

// 获得关键词文件夹;分页
export function getFolderKeywordsPage(query) {
  return request({
    url: '/identity/folder-keywords/page',
    method: 'get',
    params: query
  })
}

// 导出关键词文件夹; Excel
export function exportFolderKeywordsExcel(query) {
  return request({
    url: '/identity/folder-keywords/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
