import request from '@/utils/request'

// 创建商城商品;
export function createProduct(data) {
  return request({
    url: '/mall/product/create',
    method: 'post',
    data: data
  })
}

// 更新商城商品;
export function updateProduct(data) {
  return request({
    url: '/mall/product/update',
    method: 'put',
    data: data
  })
}

// 删除商城商品;
export function deleteProduct(id) {
  return request({
    url: '/mall/product/delete?id=' + id,
    method: 'delete'
  })
}

// 获得商城商品;
export function getProduct(id) {
  return request({
    url: '/mall/product/get?id=' + id,
    method: 'get'
  })
}

// 获得商城商品;分页
export function getProductPage(query) {
  return request({
    url: '/mall/product/page',
    method: 'get',
    params: query
  })
}

// 导出商城商品; Excel
export function exportProductExcel(query) {
  return request({
    url: '/mall/product/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
