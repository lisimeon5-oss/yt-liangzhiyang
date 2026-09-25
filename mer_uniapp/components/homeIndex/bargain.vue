<template>
  <view v-if="items.length" class="home-bargain" :style="boxStyle">
    <view class="bargain-heading" :style="{color:'#333333'}" @click="more">
      <image v-if="logo" :src="logo" mode="aspectFit" class="bargain-logo"/>
      <text class="bargain-title">{{ title }}</text><text class="bargain-more">{{ $t('bargain.seeMore') }} ›</text>
    </view>
    <scroll-view :scroll-x="horizontal && items.length > 1" :show-scrollbar="false">
      <view :class="['bargain-products', horizontal && items.length > 1 ? 'horizontal' : 'vertical']">
        <view v-for="item in items" :key="item.id" class="bargain-product" :style="{borderRadius: val('contentStyle',5)*2+'rpx',marginRight:horizontal?val('contentConfig',10)*2+'rpx':'0',marginBottom:horizontal?'0':val('contentConfig',10)*2+'rpx'}" @click="open(item)">
          <image :src="item.image" mode="aspectFill" class="bargain-image" :style="{borderRadius:val('contentStyle',5)*2+'rpx'}"/>
          <view class="bargain-info">
            <view v-if="show(0)" class="bargain-name" :style="{color:color('nameColor','#282828')}">{{ item.name }}</view>
            <view v-if="show(1)" class="bargain-price" :style="{color:priceColor}">{{ $t('bargain.floor') }} ฿{{ item.minPrice }}</view>
            <view v-if="show(1)" class="bargain-original">{{ $t('bargain.original') }} ฿{{ item.price }}</view>
            <view class="bargain-button" :style="{backgroundColor:priceColor}">{{ $t('bargain.join') }} ›</view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>
<script>
import { bargainList } from '@/api/bargain';
import { getLocalizedDiyVal, getLocalizedDiyUrl } from '@/utils/localizedName';
export default {
  name:'homeBargain',
  props:{dataConfig:{type:Object,default:()=>({})},merId:{type:Number,default:0}},
  data:()=>({items:[],generation:0}),
  computed:{
    title(){return getLocalizedDiyVal(this.dataConfig.titleConfig,this.i18nLocale)||this.$t('bargain.title');},
    logo(){return getLocalizedDiyUrl(this.dataConfig.logoConfig,this.i18nLocale)||'';},
    horizontal(){return !this.dataConfig.tabConfig || Number(this.dataConfig.tabConfig.tabVal)===0;},
    count(){return Math.max(1,Math.min(30,Number(this.val('countConfig',6))||6));},
    priceColor(){return this.dataConfig.themeStyleConfig && this.dataConfig.themeStyleConfig.tabVal ? this.color('priceColor','#e93323') : (this.$options.filters.filterTheme(getApp().globalData.theme) || '#e93323');},
    boxStyle(){const colors=this.dataConfig.bgColor && this.dataConfig.bgColor.color;const legacy=colors && colors[0].item.toUpperCase()==='#FF2200' && colors[1] && colors[1].item.toUpperCase()==='#FFFFFF';return {backgroundColor:!colors||legacy?'#fff':colors[0].item,backgroundImage:"url('/static/bargain/kjbj.png')",borderRadius:this.val('bgStyle',7)*2+'rpx',margin:this.val('mbConfig',10)*2+'rpx '+this.val('lrConfig',12)*2+'rpx 0',padding:this.val('upConfig',10)*2+'rpx 20rpx '+this.val('downConfig',10)*2+'rpx'};}
  },
  watch:{merId(){this.load();},i18nLocale(){this.load();},count(){this.load();}},
  created(){this.load();},
  beforeDestroy(){this.generation++;},
  methods:{
    val(key,fallback){const config=this.dataConfig[key];return config && config.val!=null?config.val:fallback;},
    color(key,fallback){const config=this.dataConfig[key];return config&&config.color&&config.color[0]?config.color[0].item:fallback;},
    show(index){return !this.dataConfig.typeConfig || this.dataConfig.typeConfig.activeValue.indexOf(index)!==-1;},
    async load(){const generation=++this.generation;this.items=[];try{const res=await bargainList({page:1,limit:this.count,merId:this.merId||undefined});if(generation!==this.generation)return;const ids=new Set();this.items=((res.data&&res.data.list)||[]).filter(item=>{if(ids.has(item.id))return false;ids.add(item.id);return true;});}catch(e){if(generation===this.generation)this.items=[];}},
    open(item){uni.navigateTo({url:'/pages/activity/bargain/detail?id='+item.id});},
    more(){const path=this.merId?'/pages/activity/bargain/index?merId='+this.merId:(this.val('linkConfig','')||'/pages/activity/bargain/index');this.$util.navigateTo(path);}
  }
};
</script>
<style scoped>
/* Ported from single-merchant Java pages/index/components/c_bargain.vue. */
.home-bargain{overflow:hidden;background-repeat:no-repeat;background-size:100% auto}.bargain-heading{display:flex;align-items:center;gap:12rpx;margin-bottom:28rpx;padding:8rpx 0}.bargain-title{font-size:32rpx;font-weight:700;flex:1;min-width:0}.bargain-logo{width:130rpx;height:34rpx}.bargain-more{font-size:22rpx;color:#fe960f;background:linear-gradient(142deg,#ffe9ce,#ffd6a7);border-radius:28rpx;padding:8rpx 16rpx}.horizontal{display:flex;flex-wrap:nowrap}.bargain-product{overflow:hidden;box-sizing:border-box}.horizontal .bargain-product{width:220rpx;flex-shrink:0}.bargain-image{width:220rpx;height:220rpx;display:block}.bargain-info{padding-top:14rpx;min-width:0}.bargain-name{font-size:26rpx;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:#333}.bargain-price{font-size:30rpx;font-weight:700;margin-top:12rpx;white-space:normal;color:#e93323}.bargain-original{color:#aaa;font-size:22rpx;margin-top:6rpx;text-decoration:line-through}.bargain-button{margin-top:16rpx;color:white;border-radius:28rpx;font-size:24rpx;padding:10rpx 16rpx;text-align:center;background:linear-gradient(90deg,#ff7931,#e93323);white-space:normal;line-height:30rpx}.vertical .bargain-product{display:flex;gap:24rpx}.vertical .bargain-info{flex:1;padding-top:0}.vertical .bargain-name{white-space:normal;line-height:38rpx}.vertical .bargain-button{display:inline-block;min-width:140rpx}
</style>
