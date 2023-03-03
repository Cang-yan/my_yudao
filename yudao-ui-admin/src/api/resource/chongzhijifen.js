import request from '@/utils/request'

export function chongzhijifen(query) {
  // console.log(query)
  return request({
    url: '/identity/mall/extends/buy',
    method: 'get',
    params: query
  })
}