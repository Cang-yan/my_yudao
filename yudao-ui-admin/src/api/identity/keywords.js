import request from '@/utils/request'

// 创建关键词;
export function createKeywords(data) {
  return request({
    url: '/identity/keywords/create',
    method: 'post',
    data: data
  })
}

// 更新关键词;
export function updateKeywords(data) {
  return request({
    url: '/identity/keywords/update',
    method: 'put',
    data: data
  })
}

// 删除关键词;
export function deleteKeywords(id) {
  return request({
    url: '/identity/keywords/delete?id=' + id,
    method: 'delete'
  })
}

// 获得关键词;
export function getKeywords(id) {
  return request({
    url: '/identity/keywords/get?id=' + id,
    method: 'get'
  })
}

// 获得关键词;分页
export function getKeywordsPage(query) {
  return request({
    url: '/identity/keywords/page',
    method: 'get',
    params: query
  })
}

// 导出关键词; Excel
export function exportKeywordsExcel(query) {
  return request({
    url: '/identity/keywords/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

export function searchDetail(id) {
  return request({
    url: '/identity/archives/extends/get/keywords/detail?keywordsId=' + id,
    method: 'get'
  })
}

export function giveTag(data) {
  return request({
    url: '/identity/archives/extends/create/keywords/tags',
    method: 'post',
    data: data
  })
}

export function getFolderAPI(data = {}) {
  return request({
    url: '/identity/archives/extends/folder/keywords',
    method: 'post',
    data: data
  })
}

export function addFileToFolder(data) {
  return request({
    url: '/identity/folder-keywords-relation/create',
    method: 'post',
    data: data
  })
}

export function addNewFolderAPI(data) {
  return request({
    url: '/identity/folder-keywords/create',
    method: 'post',
    data
  })
}

export function moveFolder(query, data) {
  return request({
    url: '/identity/archives/extends/move/keywords',
    method: 'post',
    params: query,
    data
  })
}