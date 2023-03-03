import request from '@/utils/request'

// 创建档案;
export function createArchives(data) {
  return request({
    url: '/identity/archives/extends/create',
    method: 'post',
    data: data
  })
}

// 查询档案信息
export function searchDetail(id) {
  return request({
    url: '/identity/archives/extends/get/detail?archivesId=' + id,
    method: 'get'
  })
}

// 更新档案;
export function updateArchives(data) {
  return request({
    url: '/identity/archives/update',
    method: 'put',
    data: data
  })
}

// 删除档案;
export function deleteArchives(id) {
  return request({
    url: '/identity/archives/delete?id=' + id,
    method: 'delete'
  })
}

// 获得档案;
export function getArchives(id) {
  return request({
    url: '/identity/archives/get?id=' + id,
    method: 'get'
  })
}

// 获得档案;分页
export function getArchivesPage(query) {
  return request({
    url: '/identity/archives/page',
    method: 'get',
    params: query
  })
}

// 导出档案; Excel
export function exportArchivesExcel(query) {
  return request({
    url: '/identity/archives/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

export function getAllInfo() {
  return request({
    url: '/identity/archives/extends/get/tagsdetail',
    method: 'get'
  })
}

export function giveTag(data) {
  return request({
    url: '/identity/archives/extends/create/tags',
    method: 'post',
    data: data
  })
}

export function getFolderAPI(data = {}) {
  return request({
    url: '/identity/archives/extends/folder/archive',
    method: 'post',
    data: data
  })
}

export function addFileToFolder(data) {
  return request({
    url: '/identity/folder-archives/create',
    method: 'post',
    data: data
  })
}

export function addNewFolderAPI(data) {
  return request({
    url: '/identity/folder/create',
    method: 'post',
    data
  })
}

export function moveFolder(query, data) {
  return request({
    url: '/identity/archives/extends/move/archives',
    method: 'post',
    params: query,
    data
  })
}