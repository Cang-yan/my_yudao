import request from '@/utils/request'

export function passApplyAPI(query) {
  return request({
    url: '/identity/mall/extends/check',
    method: 'get',
    params: query
  })
}

export function rejectApplyAPI(query) {
  return request({
    url: '/mall/trade/get?id=',
    method: 'get',
    params: query
  })
}