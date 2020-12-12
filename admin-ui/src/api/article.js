import request from '@/utils/request'

export function listArticle(query) {
  return request({
    url: '/article/list',
    method: 'get',
    params: query
  })
}

export function deleteArticle(data) {
  return request({
    url: '/article/delete',
    method: 'post',
    data
  })
}

export function publishArticle(data) {
  return request({
    url: '/article/create',
    method: 'post',
    data
  })
}

export function detailArticle(id) {
  return request({
    url: '/article/detail',
    method: 'get',
    params: { id }
  })
}

export function editArticle(data) {
  return request({
    url: '/article/update',
    method: 'post',
    data
  })
}

