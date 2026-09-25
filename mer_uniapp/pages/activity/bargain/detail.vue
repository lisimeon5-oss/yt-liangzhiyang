<template>
 <view class="bargain">
  <view v-if="error" class="error" @click="load">{{ error }} · {{ $t('bargain.retry') }}</view>
  <view v-if="activity">
   <view class="header"><view class="back" @click="all">‹ {{ $t('bargain.back') }}</view></view>
   <view class="pad30">
    <view class="wrapper">
     <view class="pictxt"><image class="product-image" :src="activity.image" mode="aspectFill"/><view class="product-info"><view class="name">{{ activity.name }}</view><view class="floor-chip">{{ $t('bargain.floor') }} ฿{{ activity.minPrice }}</view><view class="surplus">{{ $t('bargain.original') }} <text class="original-price">฿{{ activity.price }}</text></view></view></view>
     <view class="price-row"><text class="price-label">{{ $t('bargain.current') }}</text><view class="money"><text class="currency">฿</text>{{ record ? record.currentPrice : activity.price }}</view></view>
     <view class="countdown" v-if="!expired"><text class="count-label">{{ $t('bargain.countdownLabel') }}</text><view class="time-parts"><view v-for="(part,i) in countdown.split(' : ')" :key="i" class="time-part"><text class="time-number">{{ part }}</text><text class="time-unit">{{ $t('bargain.timeUnits.'+i) }}</text></view></view></view><view class="surplus" v-else>{{ $t('bargain.expired') }}</view>
    </view>
    <view class="content">
     <view class="progress-money" v-if="record"><view>{{ $t('bargain.cut',{amount:'฿'+cutAmount}) }}</view><view>{{ $t('bargain.leftAmount') }} <text>฿{{ leftAmount }}</text></view></view>
     <view class="cu-progress" v-if="record"><view class="bar" :style="{width:percent+'%'}"/></view>
     <view class="tip" v-if="record">{{ $t('bargain.remaining',{count:Math.max(0,activity.peopleNum-record.helpCount)}) }}</view>
           <view class="footer">
        <button v-if="record && record.mine && record.orderNo" @click="viewOrder">{{ $t('bargain.viewOrder') }}</button>
        <view v-else-if="expired">{{ $t('bargain.expired') }}</view>
        <button v-else-if="!record" :disabled="busy" @click="start">{{ $t('bargain.start') }}</button>
        <template v-else-if="record.mine">
          <button v-if="record.status === 2" :disabled="busy" @click="buy">{{ $t('bargain.buy') }}</button>
          <button v-if="record.status === 1" open-type="share" @click="share">{{ $t('bargain.share') }}</button>
        </template>
        <template v-else><button v-if="record.status === 1" :disabled="busy" @click="help">{{ $t('bargain.help') }}</button><button :disabled="busy" @click="start">{{ $t('bargain.start') }}</button></template>

      </view>
     <view class="stock-notice">{{ $t('bargain.stockNotice') }}</view>
    </view>
    <view class="panel" v-if="activity.description"><view class="section-title">{{ $t('bargain.rules') }}</view><text class="description">{{ activity.description }}</text></view>
    <view class="panel" v-if="helpList.length"><view class="section-title">{{ $t('bargain.helpHistory') }}</view><view v-for="(item,i) in helpList" :key="i" class="cut"><view class="cut-mark">✓</view><view class="cut-info"><view>{{ $t('bargain.cut',{amount:'฿'+item.cutPrice}) }}</view><text>{{ item.createTime }}</text></view></view></view>
   </view>
  </view>
 </view>
</template>
<script>
import { bargainDetail, bargainProgress, bargainStart, bargainHelp } from '@/api/bargain';
import { preOrderApi } from '@/api/order';
import { telegramBargainUrl, telegramSharePickerUrl } from '@/utils/bargainShare';
import { TELEGRAM_MINI_APP_URL } from '@/config/telegram';
export default {
  data: () => ({ id: null, recordId: null, activity: null, record: null, helpList: [], busy: false, error: '', now: Date.now(), clock: null }),
  computed: {
    cutAmount(){return this.record ? Math.max(0,Number(this.activity.price)-Number(this.record.currentPrice)).toFixed(2) : '0.00';},
    leftAmount(){return this.record ? Math.max(0,Number(this.record.currentPrice)-Number(this.activity.minPrice)).toFixed(2) : '0.00';},
    countdown(){const seconds=Math.max(0,Math.floor((Number(this.activity.endTimeMs)-this.now)/1000));const pad=n=>String(n).padStart(2,'0');return [pad(Math.floor(seconds/86400)),pad(Math.floor(seconds/3600)%24),pad(Math.floor(seconds/60)%60),pad(seconds%60)].join(' : ');},
    percent() { return this.record ? Math.min(100, Math.round((this.activity.price - this.record.currentPrice) / (this.activity.price - this.activity.minPrice) * 100)) : 0; },
    expired() { return !this.activity.enabled || this.now >= Number(this.activity.endTimeMs) || this.now < Number(this.activity.startTimeMs); }
  },
  onLoad(options) { this.id = Number(options.id) || null; this.recordId = Number(options.recordId) || null; },
  onShow() { this.load(); this.clock = setInterval(() => { this.now = Date.now(); }, 1000); },
  onHide() { clearInterval(this.clock); }, onUnload() { clearInterval(this.clock); },
  onShareAppMessage() { return { title: this.activity ? this.activity.name : this.$t('bargain.title'), path: '/pages/activity/bargain/detail?recordId=' + this.recordId }; },
  methods: {
    async load() {
      this.error = ''; uni.setNavigationBarTitle({ title: this.$t('bargain.title') });
      try {
        if (this.recordId) { const res = await bargainProgress(this.recordId); this.activity = res.data.activity; this.record = res.data.record; this.helpList = res.data.helpList; this.id = this.activity.id; }
        else { this.activity = (await bargainDetail(this.id)).data; }
      } catch (e) { this.error = e.msg || String(e); }
    },
    async action(fn) { if (this.busy) return; this.busy = true; try { await fn(); } catch (e) { uni.showToast({ title: e.msg || String(e), icon: 'none' }); } finally { this.busy = false; } },
    start() { this.action(async () => { const res = await bargainStart(this.id); this.recordId = res.data.id; uni.redirectTo({ url: '/pages/activity/bargain/detail?recordId=' + this.recordId }); }); },
    help() { this.action(async () => { const res = await bargainHelp(this.recordId); uni.showToast({ title: this.$t('bargain.cut', { amount: res.data.cutPrice }), icon: 'none' }); await this.load(); }); },
    buy() { this.action(async () => { const res = await preOrderApi({ preOrderType: 'bargain', orderDetails: [{ productId: this.activity.productId, attrValueId: this.activity.attrValueId, productNum: 1, bargainRecordId: this.recordId }] }); uni.navigateTo({ url: '/pages/goods/order_confirm/index?orderNo=' + res.data.orderNo }); }); },
    share() {
      // #ifdef H5
      const url = telegramBargainUrl(TELEGRAM_MINI_APP_URL, this.recordId);
      if (!url) { uni.showToast({ title: this.$t('bargain.telegramUnavailable'), icon: 'none' }); return; }
      const tg = window.Telegram && window.Telegram.WebApp;
      if (tg && tg.initData && typeof tg.openTelegramLink === 'function') {
        tg.openTelegramLink(telegramSharePickerUrl(url, this.$t('bargain.share') + ' · ' + this.activity.name));
      } else {
        uni.setClipboardData({ data: url, success: () => uni.showToast({ title: this.$t('bargain.copied'), icon: 'none' }) });
      }
      // #endif
      // #ifndef H5
      uni.showShareMenu({ withShareTicket: true });
      // #endif
    },
    viewOrder() { uni.navigateTo({ url: '/pages/goods/order_details/index?orderNo=' + this.record.orderNo }); },
    all() { uni.navigateTo({ url: '/pages/activity/bargain/index' }); }
  }
};
</script>
<style scoped>
/* Layout and artwork ported from single-merchant Java goods_bargain_details. */
.bargain{background:#f52e17;min-height:100vh;padding-bottom:40rpx;color:#333}.header{height:340rpx;background:url('/static/bargain/detail-header.png') no-repeat;background-size:100% 100%;padding-top:20rpx;box-sizing:border-box}.back{display:inline-block;max-width:90%;padding:12rpx 26rpx;border-radius:0 40rpx 40rpx 0;background:linear-gradient(233deg,#ffd169,#fe960f);font-size:26rpx;color:#6e3f00}.pad30{padding:0 30rpx}.wrapper{position:relative;margin-top:-100rpx;background:#fff;padding:0 24rpx 10rpx;border-radius:14rpx 14rpx 0 0}.pictxt{display:flex;gap:20rpx;background:#fff5e6;padding:20rpx;border-radius:14rpx;align-items:center}.product-image{width:220rpx;height:220rpx;border-radius:6rpx;flex-shrink:0}.product-info{flex:1;min-width:0}.name{font-size:28rpx;line-height:38rpx;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}.surplus{font-size:22rpx;color:#999;margin-top:8rpx}.money{color:#e93323;font-size:24rpx;font-weight:600;margin-top:10rpx}.money text{font-size:36rpx}.countdown{font-size:20rpx;margin-top:10rpx;line-height:34rpx}.countdown text{background:#333;color:#fff;padding:4rpx 8rpx;border-radius:4rpx}.content{background:#fff url('/static/bargain/zhuangshi.png') no-repeat;background-size:100% 100%;border-radius:0 0 14rpx 14rpx;padding:36rpx 24rpx}.progress-money{display:flex;justify-content:center;gap:28rpx;font-size:28rpx;flex-wrap:wrap}.progress-money text{color:#e93323}.cu-progress{height:20rpx;background:#fdeae8;border-radius:20rpx;overflow:hidden;margin:24rpx 20rpx}.bar{height:100%;background:linear-gradient(90deg,#ffa363,#e93323);border-radius:20rpx;transition:width .6s}.tip{font-size:24rpx;color:#999;text-align:center;margin:16rpx 0 28rpx}.footer{text-align:center}.footer button{width:100%;min-height:80rpx;line-height:40rpx;padding:20rpx 24rpx;background:linear-gradient(90deg,#ff7931,#e93323);color:#fff;border-radius:60rpx;font-size:30rpx;font-weight:600;margin:16rpx 0;box-sizing:border-box}.footer button::after{border:0}.footer button[disabled]{opacity:.55}.all{color:#b17735;font-size:24rpx;padding:16rpx}.stock-notice{font-size:22rpx;color:#aaa;text-align:center;line-height:34rpx;margin-top:12rpx}.panel{background:#fff;border-radius:14rpx;padding:28rpx;margin-top:24rpx}.section-title{text-align:center;font-size:30rpx;font-weight:600;margin-bottom:24rpx}.description{font-size:26rpx;line-height:42rpx;white-space:pre-wrap;overflow-wrap:break-word}.cut{display:flex;align-items:center;gap:18rpx;padding:20rpx 0;border-bottom:1rpx solid #f2f2f2}.cut-mark{background:#fff0e5;color:#e93323;border-radius:50%;width:60rpx;height:60rpx;line-height:60rpx;text-align:center}.cut-info{font-size:26rpx;color:#e93323}.cut-info text{color:#999;font-size:22rpx}.error{padding:30rpx;background:#fff;color:#e93323}

/* Match the original mobile type scale; keep labels separate from amounts. */
.bargain{font-family:-apple-system,BlinkMacSystemFont,"Segoe UI","PingFang SC","Microsoft YaHei",sans-serif;-webkit-font-smoothing:antialiased}.pad30{padding:0 28rpx}.wrapper{padding:24rpx 24rpx 0;border-radius:20rpx 20rpx 0 0}.pictxt{background:#fff8ee;padding:20rpx;gap:24rpx;align-items:flex-start}.product-image{width:200rpx;height:200rpx;border-radius:12rpx}.product-info{padding:6rpx 0}.name{font-size:30rpx;font-weight:600;line-height:44rpx;letter-spacing:.3rpx}.floor-chip{display:inline-block;background:#ffeadb;color:#d84b22;font-size:24rpx;line-height:34rpx;padding:4rpx 12rpx;margin-top:18rpx;border-radius:6rpx}.surplus{font-size:24rpx;line-height:36rpx;margin-top:14rpx;color:#8d8d8d}.original-price{text-decoration:line-through;margin-left:8rpx}.price-row{display:flex;align-items:baseline;gap:20rpx;padding:28rpx 6rpx 22rpx;flex-wrap:wrap}.price-label{font-size:26rpx;color:#555}.money{font-family:Arial,sans-serif;font-size:52rpx;font-weight:700;line-height:1.15;margin:0;letter-spacing:-1rpx;color:#ee4024}.money .currency{font-size:30rpx;font-weight:500;margin-right:6rpx}.countdown{border-top:1rpx solid #f2efec;padding:20rpx 6rpx 12rpx;margin:0;display:flex;align-items:center;justify-content:space-between;gap:16rpx;flex-wrap:wrap}.countdown text{background:none;color:inherit;padding:0;border-radius:0}.count-label{font-size:24rpx;color:#888!important}.time-parts{display:flex;gap:10rpx;align-items:center;flex-wrap:wrap}.time-part{display:flex;align-items:center;gap:6rpx}.countdown .time-number{display:inline-block;min-width:40rpx;padding:4rpx 6rpx;background:#fff0e7;border-radius:6rpx;color:#d9502d;font:600 24rpx/32rpx Arial,sans-serif;text-align:center}.countdown .time-unit{font-size:20rpx;color:#999}.content{padding:20rpx 24rpx 28rpx;background-image:none;border-radius:0 0 20rpx 20rpx}.footer button{min-height:88rpx;padding:24rpx;line-height:40rpx;font-size:30rpx;font-weight:600;margin:10rpx 0 0;box-shadow:0 8rpx 20rpx rgba(233,51,35,.13);letter-spacing:1rpx}.stock-notice{margin-top:22rpx;color:#999;font-size:23rpx;line-height:36rpx}.panel{padding:30rpx;border-radius:20rpx;margin-top:24rpx}.section-title{text-align:left;font-size:30rpx;line-height:44rpx;font-weight:600;margin:0 0 20rpx;display:flex;align-items:center;gap:12rpx}.section-title::before{content:"";width:6rpx;height:28rpx;background:#f55a2d;border-radius:4rpx;display:block}.description{font-size:27rpx;line-height:46rpx;color:#666}.back{font-size:24rpx;line-height:36rpx;padding:10rpx 24rpx}
</style>
