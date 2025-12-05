import request from './request'

/**
 * 获取已提问技术点列表
 */
export const getAskedTopics = () => {
  return request({
    url: '/interview-topics/list',
    method: 'get'
  })
}

/**
 * 清除已提问技术点（重新开始面试）
 */
export const clearAskedTopics = () => {
  return request({
    url: '/interview-topics/clear',
    method: 'delete'
  })
}

/**
 * 添加已提问技术点
 */
export const addAskedTopics = (topics: string) => {
  return request({
    url: '/interview-topics/add',
    method: 'post',
    params: { topics }
  })
}

/**
 * 获取技术点统计信息
 */
export const getTopicsStats = () => {
  return request({
    url: '/interview-topics/stats',
    method: 'get'
  })
}
