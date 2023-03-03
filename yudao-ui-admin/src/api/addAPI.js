import request from '@/utils/request'

// 获得管理员主页数据
export const getAdminData = (role) => {
  // console.log(role)
  return request({
    url: '/identity/archives/extends/index/' + role,
    method: 'get'
  })
}