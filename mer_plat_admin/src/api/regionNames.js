import request from '@/utils/request';
export function regionTree() {
  return request({ url: '/admin/platform/city/region/list/tree', method: 'get' });
}
export function saveRegionNames(params) {
  return request({ url: '/admin/platform/city/region/edit', method: 'post', params });
}
