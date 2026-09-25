import { telegramGroupUrl, telegramSharePickerUrl } from '@/utils/bargainShare';
import { TELEGRAM_MINI_APP_URL } from '@/config/telegram';

export function shareTelegramGroup(record, productName, t) {
  const url = telegramGroupUrl(TELEGRAM_MINI_APP_URL, record);
  if (!url) {
    uni.showToast({ title: t('活动已失效！'), icon: 'none' });
    return;
  }
  const tg = window.Telegram && window.Telegram.WebApp;
  if (tg && tg.initData && typeof tg.openTelegramLink === 'function') {
    tg.openTelegramLink(telegramSharePickerUrl(url, t('邀请好友参团') + (productName ? ' · ' + productName : '')));
  } else {
    uni.setClipboardData({ data: url, success: () => uni.showToast({ title: t('Telegram邀请链接已复制'), icon: 'none' }) });
  }
}
