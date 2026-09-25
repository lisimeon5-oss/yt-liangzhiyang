/** Keep both history and hash deployments shareable; never forward account/login parameters. */
export function bargainShareUrl(location, recordId) {
  const route = '/pages/activity/bargain/detail?recordId=' + encodeURIComponent(recordId);
  if ((location.hash || '').startsWith('#/')) return location.origin + location.pathname + '#' + route;
  const marker = location.pathname.indexOf('/pages/');
  const base = marker >= 0 ? location.pathname.slice(0, marker) : '';
  return location.origin + base + route;
}

// Launch parameters identify a record only; they are never login credentials.
export function telegramBargainUrl(entry, recordId) {
  if (!/^https:\/\/t\.me\/[A-Za-z0-9_]+(?:\/[A-Za-z0-9_]+)?\/?$/.test(entry || '')) return '';
  if (!/^[1-9]\d*$/.test(String(recordId))) return '';
  return entry.replace(/\/$/, '') + '?startapp=bargain_' + recordId;
}

export function telegramBargainRoute(tg, search = '', hash = '') {
  const payload = telegramLaunchParam(tg, search, hash);
  const match = /^bargain_([1-9]\d*)$/.exec(payload);
  return match ? '/pages/activity/bargain/detail?recordId=' + match[1] : '';
}

export function telegramSharePickerUrl(url, title) {
  return 'https://t.me/share/url?url=' + encodeURIComponent(url) + '&text=' + encodeURIComponent(title || '');
}

export function telegramGroupUrl(entry, record) {
  if (!/^https:\/\/t\.me\/[A-Za-z0-9_]+(?:\/[A-Za-z0-9_]+)?\/?$/.test(entry || '') || !record) return '';
  const ids = [record.groupActivityId, record.productId, record.groupRecordId];
  if (!ids.every(id => /^[1-9]\d{0,9}$/.test(String(id)))) return '';
  return entry.replace(/\/$/, '') + '?startapp=group_' + ids.join('_');
}

export function telegramGroupRoute(tg, search = '', hash = '') {
  const payload = telegramLaunchParam(tg, search, hash);
  const match = /^group_([1-9]\d{0,9})_([1-9]\d{0,9})_([1-9]\d{0,9})$/.exec(payload);
  return match ? '/pages/activity/status/index?gt=' + match[1] + '&pt=' + match[2] + '&rd=' + match[3] : '';
}

// Launch data is used only for routing; authentication still goes through login.
export function telegramLaunchParam(tg, search = '', hash = '') {
  const read = value => {
    const text = String(value || '').replace(/^[?#]/, '');
    const params = new URLSearchParams(text.startsWith('/') ? text.slice(text.indexOf('?') + 1) : text);
    return params.get('tgWebAppStartParam') || new URLSearchParams(params.get('tgWebAppData') || '').get('start_param') || '';
  };
  return read(search) || read(hash) || (tg && tg.initDataUnsafe && tg.initDataUnsafe.start_param) ||
    new URLSearchParams((tg && tg.initData) || '').get('start_param') || '';
}
