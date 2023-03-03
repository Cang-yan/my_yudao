import request from '@/utils/request'

// 创建文件夹;
export function createFolder(data) {
  return request({
    url: '/identity/folder/create',
    method: 'post',
    data: data
  })
}

// 更新文件夹;
export function updateFolder(data) {
  return request({
    url: '/identity/folder/update',
    method: 'put',
    data: data
  })
}

// 删除文件夹;
export function deleteFolder(id) {
  return request({
    url: '/identity/folder/delete?id=' + id,
    method: 'delete'
  })
}

// 获得文件夹;
export function getFolder(id) {
  return request({
    url: '/identity/folder/get?id=' + id,
    method: 'get'
  })
}

// 获得文件夹;分页
export function getFolderPage(query) {
  return request({
    url: '/identity/folder/page',
    method: 'get',
    params: query
  })
}

// 导出文件夹; Excel
export function exportFolderExcel(query) {
  return request({
    url: '/identity/folder/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
