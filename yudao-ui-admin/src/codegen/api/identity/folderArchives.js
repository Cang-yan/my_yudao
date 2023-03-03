import request from '@/utils/request'

// 创建文件夹和档案的关系;
export function createFolderArchives(data) {
  return request({
    url: '/identity/folder-archives/create',
    method: 'post',
    data: data
  })
}

// 更新文件夹和档案的关系;
export function updateFolderArchives(data) {
  return request({
    url: '/identity/folder-archives/update',
    method: 'put',
    data: data
  })
}

// 删除文件夹和档案的关系;
export function deleteFolderArchives(id) {
  return request({
    url: '/identity/folder-archives/delete?id=' + id,
    method: 'delete'
  })
}

// 获得文件夹和档案的关系;
export function getFolderArchives(id) {
  return request({
    url: '/identity/folder-archives/get?id=' + id,
    method: 'get'
  })
}

// 获得文件夹和档案的关系;分页
export function getFolderArchivesPage(query) {
  return request({
    url: '/identity/folder-archives/page',
    method: 'get',
    params: query
  })
}

// 导出文件夹和档案的关系; Excel
export function exportFolderArchivesExcel(query) {
  return request({
    url: '/identity/folder-archives/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
