import request from '@/utils/request'

// 创建商城订单;
export function createTrade(data) {
  return request({
    url: '/mall/trade/create',
    method: 'post',
    data: data
  })
}

// 更新商城订单;
export function updateTrade(data) {
  return request({
    url: '/mall/trade/update',
    method: 'put',
    data: data
  })
}

// 删除商城订单;
export function deleteTrade(id) {
  return request({
    url: '/mall/trade/delete?id=' + id,
    method: 'delete'
  })
}

// 获得商城订单;
export function getTrade(id) {
  return request({
    url: '/mall/trade/get?id=' + id,
    method: 'get'
  })
}

// 获得商城订单;分页
export function getTradePage(query) {
  return request({
    url: '/mall/trade/page',
    method: 'get',
    params: query
  })
}

// 导出商城订单; Excel
export function exportTradeExcel(query) {
  return request({
    url: '/mall/trade/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
