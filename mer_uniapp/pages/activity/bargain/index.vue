<template>
 <view class="bargain-list">
  <view class="header" :class="{'translated-header':i18nLocale !== 'zh-cn'}"><view v-if="i18nLocale !== 'zh-cn'" class="headline">{{ $t('bargain.title') }}</view></view>
  <view class="list-wrap">
   <view class="tabs"><view :class="{active:!mine}" @click="change(false)">{{ $t('bargain.back') }}</view><view :class="{active:mine}" @click="change(true)">{{ $t('bargain.mine') }}</view></view>
   <view v-for="item in items" :key="item.id" class="item" @click="open(item)">
    <image v-if="!mine" class="pictrue" :src="item.image" mode="aspectFill"/>
    <view class="text"><view class="name">{{ mine ? '#'+item.id : item.name }}</view>
     <view class="meta" v-if="!mine">{{ $t('bargain.ends') }} {{ String(item.endTime || '').slice(5,16) }}</view>
     <view class="meta" v-else>{{ $t('bargain.' + ({1:'inProgress',2:'success',3:'ordered'}[item.displayStatus || item.status] || 'expired')) }}</view>
     <view class="bottom"><view class="money">{{ $t(mine?'bargain.current':'bargain.floor') }}<view>฿<text class="price">{{ mine ? item.currentPrice : item.minPrice }}</text></view></view><view class="cutBnt">{{ $t(mine?'bargain.detailLink':'bargain.join') }} ›</view></view>
    </view>
   </view>
   <view class="empty" v-if="!items.length && !loading && !error">{{ $t('bargain.empty') }}</view>
   <view v-if="error" class="empty" @click="load">{{ error }} · {{ $t('bargain.retry') }}</view>
   <view v-if="hasMore && !error" class="load" @click="load">{{ $t(loading ? 'bargain.loading' : 'bargain.more') }}</view>
  </view>
 </view>
</template>
<script>
import { bargainList, bargainRecords } from '@/api/bargain';
export default {
  data: () => ({ items: [], page: 1, hasMore: true, loading: false, error: '', mine: false, merId: null, generation: 0 }),
  onLoad(options) { this.merId = Number(options.merId) || null; this.mine = options.mine === '1'; },
  onShow() { uni.setNavigationBarTitle({ title: this.$t('bargain.title') }); this.reset(); },
  onReachBottom() { this.load(); },
  methods: {
    reset() { this.generation++; this.items = []; this.page = 1; this.hasMore = true; this.loading = false; this.load(); },
    change(mine) { this.mine = mine; this.reset(); },
    async load() {
      if (this.loading || !this.hasMore) return;
      const generation = this.generation; this.loading = true; this.error = '';
      try {
        const res = await (this.mine ? bargainRecords : bargainList)({ page: this.page, limit: 20, merId: this.merId });
        if (generation !== this.generation) return;
        const ids = new Set(this.items.map(i => i.id));
        this.items.push(...res.data.list.filter(i => !ids.has(i.id)));
        this.hasMore = res.data.list.length > 0 && this.items.length < res.data.total; this.page++;
      } catch (e) { if (generation === this.generation) this.error = e.msg || String(e); }
      finally { if (generation === this.generation) this.loading = false; }
    },
    open(item) { uni.navigateTo({ url: '/pages/activity/bargain/detail?' + (this.mine ? 'recordId=' : 'id=') + item.id }); }
  }
};
</script>
<style scoped>
/* Layout and artwork ported from single-merchant Java goods_bargain. */
.bargain-list{min-height:100vh;background:#f62f19;padding-bottom:40rpx}.header{width:100%;height:420rpx;background:url('/static/bargain/list-header.png') no-repeat center top;background-size:100% 100%}.translated-header{background-image:url('/static/bargain/detail-header.png');height:340rpx}.headline{text-align:center;font-size:48rpx;font-weight:800;color:#fff8d8;padding:46rpx 28rpx;text-shadow:0 4rpx 0 #e75418}.list-wrap{padding:0 30rpx;margin-top:-24rpx;position:relative}.tabs{display:flex;background:#fff;border-radius:14rpx;margin-bottom:20rpx;overflow:hidden}.tabs view{flex:1;text-align:center;font-size:28rpx;padding:24rpx 12rpx;color:#666}.tabs .active{color:#e93323;font-weight:600;box-shadow:inset 0 -5rpx #e93323}.item{display:flex;gap:22rpx;background:#fff;border-radius:14rpx;margin-bottom:20rpx;padding:25rpx;align-items:center}.pictrue{width:190rpx;height:190rpx;flex-shrink:0;border-radius:14rpx}.text{flex:1;min-width:0}.name{font-size:28rpx;line-height:38rpx;color:#333;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden}.meta{font-size:22rpx;color:#999;line-height:32rpx;margin:10rpx 0}.bottom{display:flex;align-items:flex-end;justify-content:space-between;gap:12rpx;flex-wrap:wrap}.money{color:#e93323;font-size:22rpx;font-weight:600}.money view{font-size:24rpx}.price{font-size:38rpx;margin-left:4rpx}.cutBnt{border-radius:50rpx;font-size:24rpx;color:#fff;text-align:center;padding:12rpx 22rpx;background:linear-gradient(90deg,#ff7931,#e93323);line-height:32rpx}.empty,.load{font-size:26rpx;text-align:center;padding:30rpx 12rpx;color:#fff}.empty{background:#fff;color:#999;border-radius:14rpx}

.bargain-list{font-family:-apple-system,BlinkMacSystemFont,"Segoe UI","PingFang SC","Microsoft YaHei",sans-serif;-webkit-font-smoothing:antialiased}.list-wrap{padding:0 28rpx}.tabs{border-radius:16rpx;margin-bottom:22rpx}.tabs view{font-size:28rpx;line-height:40rpx;padding:22rpx 12rpx;position:relative}.tabs .active{box-shadow:none}.tabs .active::after{content:"";position:absolute;bottom:0;left:35%;width:30%;height:5rpx;background:#ed4328;border-radius:5rpx}.item{padding:24rpx;gap:24rpx;border-radius:18rpx;align-items:flex-start}.pictrue{width:204rpx;height:224rpx;border-radius:12rpx}.text{display:flex;flex-direction:column;min-height:224rpx}.name{font-size:30rpx;font-weight:600;line-height:42rpx;margin-top:4rpx}.meta{font-size:23rpx;line-height:34rpx;margin:12rpx 0 18rpx;color:#999}.bottom{margin-top:auto;gap:12rpx}.money{font-size:22rpx;line-height:30rpx;font-weight:400}.money view{font-family:Arial,sans-serif;line-height:44rpx;margin-top:4rpx}.price{font-size:42rpx;font-weight:700;letter-spacing:-.5rpx}.cutBnt{font-size:24rpx;line-height:34rpx;padding:12rpx 20rpx;min-width:120rpx;box-sizing:border-box;font-weight:500;box-shadow:0 4rpx 10rpx rgba(233,51,35,.12)}
</style>
