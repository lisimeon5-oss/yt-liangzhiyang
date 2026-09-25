import request from '@/utils/request';
export const bargainList = params => request.get('bargain/list', params, { noAuth: true });
export const bargainDetail = id => request.get(`bargain/detail/${id}`, {}, { noAuth: true });
export const bargainProgress = id => request.get(`bargain/progress/${id}`, {}, { noAuth: true });
export const bargainStart = id => request.post(`bargain/start/${id}`);
export const bargainHelp = id => request.post(`bargain/help/${id}`);
export const bargainRecords = params => request.get('bargain/records', params);
