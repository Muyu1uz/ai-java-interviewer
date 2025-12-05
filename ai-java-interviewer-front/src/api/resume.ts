import request from './request'

export const uploadResume = (formData: FormData) => {
  return request({
    url: '/resume/create', // Updated to match user provided doc URL
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 120000 // 2 minutes timeout for AI analysis
  })
}

export const checkResume = () => {
  return request({
    url: '/resume/check',
    method: 'get'
  })
}
