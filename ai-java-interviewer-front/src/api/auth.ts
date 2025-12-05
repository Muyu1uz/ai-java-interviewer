import request from './request'

export const login = (data: any) => {
  return request({
    url: '/user/login', // Updated to /user based on user feedback
    method: 'post',
    data
  })
}

export const register = (data: any) => {
  return request({
    url: '/user/register', // Updated to /user based on user feedback
    method: 'post',
    data
  })
}
