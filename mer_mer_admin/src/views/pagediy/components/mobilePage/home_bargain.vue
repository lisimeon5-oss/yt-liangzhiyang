<template>
  <div v-if="configObj">
    <div class="seckill" :style="boxStyle">
      <div class="bg_box" :style="boxBgStyle"></div>
      <div class="title acea-row row-between-wrapper">
        <div class="acea-row row-middle">
          <div class="pictrue skeleton-rect" v-if="logoSrc">
            <el-image :src="logoSrc" class="image"></el-image>
          </div>
          <div class="lines" v-if="logoSrc"></div>
          <div class="point skeleton-rect" :style="titleColor">{{ titleDisplay }}</div>
        </div>
        <div class="more acea-row row-center-wrapper skeleton-rect">GO<span class="iconfont icon-xuanze"></span></div>
      </div>
      <div class="conter" v-show="listStyle === 0">
        <div style="white-space: nowrap; vertical-align: middle">
          <div class="itemCon" :style="contentConfig" v-for="(item, index) in previewItems" :key="index">
            <div class="item">
              <div class="pictrue skeleton-rect">
                <!--<el-image :src="item.image" :style="contentStyle" mode="aspectFill"></el-image>-->
                <div class="empty-box" :style="contentStyle"><span class="iconfont icon-tu"></span></div>
              </div>
              <div class="bargain-action">{{ $t('bargain.start') }} →</div><div v-show="nameShow" class="name line1 skeleton-rect" :style="nameColor">{{ diyUiText(item.name) }}</div>
              <div v-show="priceShow" class="x_money line1 skeleton-rect semiBold" :style="priceColor">
                {{ $t('bargain.floor') }} ฿<span class="num">{{ item.price }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="conter_y" :style="gridGap" v-show="listStyle === 1">
        <div class="item" :style="contentConfig" v-for="(item, index) in previewItems" :key="index">
          <div class="pictrue">
            <div class="empty-box" :style="contentStyle"><span class="iconfont icon-tu"></span></div>
            <!--<el-image :src="item.image" :style="contentStyle" mode="aspectFill"></el-image>-->
          </div>
          <div class="text-info text-add acea-row row-column row-between">
            <div class="bargain-action">{{ $t('bargain.start') }} →</div><div v-show="nameShow">
              <div class="title line2" :style="nameColor">{{ diyUiText(item.name) }}</div>
            </div>
            <div v-show="priceShow" class="price semiBold" :style="priceColor">
              <div>
                {{ $t('bargain.floor') }} <span>฿</span>{{ item.price }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------
import bargainBackground from '@/assets/imgs/bargain/kjbj.png';
import { mapState, mapGetters } from 'vuex';
import { diyCname, diyUiText } from '@/utils/diyCname';
import { getFormLocalizedText, getUiLocale } from '@/utils/localizedName';
export default {
  name: 'home_bargain',
  ...diyCname('bargain.title'),
  icon: 't-icon-zujian-miaosha',
  configName: 'c_home_bargain',
  type: 1, // 0 基础组件 1 营销组件 2工具组件
  defaultName: 'bargain', // 外面匹配名称
  props: {
    index: {
      type: null,
    },
    num: {
      type: null,
    },
  },
  computed: {
    previewItems(){const count=this.configObj && this.configObj.countConfig ? this.configObj.countConfig.val : 6;return Array.from({length:Math.max(1,Math.min(30,Number(count)||6))},(_,i)=>this.spikeList[i%this.spikeList.length]);},
    ...mapState('mobildConfig', ['defaultArray']),
    ...mapGetters(['mediaDomain', 'mobileTheme']),
    //最外层盒子的样式
    boxStyle() {
      return [
        { 'border-radius': this.configObj.bgStyle.val ? this.configObj.bgStyle.val + 'px' : '0' },
        { margin: this.configObj.mbConfig.val + 'px' + ' ' + this.configObj.lrConfig.val + 'px' + ' ' + 0 },
        { padding: this.configObj.upConfig.val + 'px' + ' ' + '10px' + ' ' + this.configObj.downConfig.val + 'px' },
      ];
    },
    //图片背景
    boxBgStyle() {
      return {
        backgroundImage: `url(${bargainBackground})`,
      };
    },
    //图片样式
    contentStyle() {
      return {
        'border-radius': this.configObj.contentStyle.val + 'px',
      };
    },
    //内容边距
    contentConfig() {
      return [{ 'margin-right': this.configObj.contentConfig.val ? this.configObj.contentConfig.val + 'px' : '0' }];
    },
    //标题颜色
    titleColor() {
      return { color: '#333333' };
    },
    //名称颜色
    nameColor() {
      return { color: this.configObj.nameColor.color[0].item };
    },
    //价格颜色
    priceColor() {
      return { color: this.themeStyle ? this.configObj.priceColor.color[0].item : this.themeColor };
    },
    //内容间距
    gridGap() {
      return { 'grid-gap': this.configObj.contentConfig.val + 'px' };
    },
    //商品名称
    nameShow() {
      if (this.configObj.typeConfig.activeValue.indexOf(0) !== -1) {
        return true;
      } else {
        return false;
      }
    },
    //商品价格
    priceShow() {
      if (this.configObj.typeConfig.activeValue.indexOf(1) !== -1) {
        return true;
      } else {
        return false;
      }
    },
    previewLang() {
      return (this.configObj && this.configObj.diyMediaLang) || getUiLocale(this);
    },
    logoSrc() { return getFormLocalizedText(this.configObj.logoConfig.url, this.configObj.logoConfig.urlJson, this.previewLang) || ''; },
    titleDisplay() {
      if (!this.configObj || !this.configObj.titleConfig) return '';
      return getFormLocalizedText(this.configObj.titleConfig.val, this.configObj.titleConfig.valJson, this.previewLang) || this.$t('bargain.title');
    },
  },
  watch: {
    pageData: {
      handler(nVal, oVal) {
        this.setConfig(nVal);
      },
      deep: true,
    },
    num: {
      handler(nVal, oVal) {
        let data = this.$store.state.mobildConfig.defaultArray[nVal];
        this.setConfig(data);
      },
      deep: true,
    },
    defaultArray: {
      handler(nVal, oVal) {
        let data = this.$store.state.mobildConfig.defaultArray[this.num];
        this.setConfig(data);
      },
      deep: true,
    },
    themeStyle: {
      handler(nVal, oVal) {
        this.configObj.priceColor.isShow = this.configObj.themeStyleConfig.tabVal;
      },
      deep: true,
    },
  },
  data() {
    return {
      defaultConfig: {
        name: 'bargain',
        timestamp: this.num,
        setUp: {
          tabVal: 0,
          cname: this.$t('bargain.title'),
        },
        tabConfig: {
          title: this.$t('pagediy.displayStyle'),
          tabTitle: this.$t('pagediy.layoutSettings'),
          tabVal: 0,
          isShow: 1,
          list: [
            {
              val: this.$t('marketing.style1'),
              icon: 'icon-yangshiyi',
            },
            {
              val: this.$t('marketing.style2'),
              icon: 'icon-yangshier',
            },
          ],
        },
        countConfig: { title: this.$t('marketing.productCount'), val: 6, min: 1, max: 30, isShow: 1 },
        logoConfig: {
          isShow: 1,
          tabTitle: this.$t('pagediy.iconSettings'),
          title: this.$t('pagediy.uploadIcon'),
          tips: this.$t('pagediy.suggestIconSize124'),
          url: '',
          urlJson: '',
        },
        titleConfig: {
          tabTitle: this.$t('pagediy.titleSettings'),
          title: this.$t('pagediy.titleContent'),
          val: '',
          valJson: '',
          place: this.$t('pagediy.pleaseEnterTitle'),
          isShow: 1,
          max: 15,
        },
        linkConfig: {
          title: this.$t('pagediy.moreLink'),
          val: '/pages/activity/bargain/index',
          place: this.$t('application.pleaseSelectLink'),
          isShow: 1,
          max: 100,
        },
        //显示内容
        typeConfig: {
          tabTitle: this.$t('pagediy.displayContent'),
          name: 'rowsNum',
          title: this.$t('pagediy.displayInfo'),
          activeValue: [0, 1],
          list: [
            {
              val: this.$t('product.productName'),
            },
            {
              val: this.$t('pagediy.productPriceLabel'),
            },
          ],
        },
        // 背景颜色
        bgColor: {
          tabTitle: this.$t('pagediy.colorSettings'),
          title: this.$t('pagediy.backgroundColor'),
          color: [
            {
              item: '#FFFFFF',
            },
            {
              item: '#FFFFFF',
            },
          ],
          default: [
            {
              item: '#FFFFFF',
            },
            {
              item: '#FFFFFF',
            },
          ],
        },
        nameColor: {
          title: this.$t('pagediy.productTitleColor'),
          color: [
            {
              item: '#282828',
            },
          ],
          default: [
            {
              item: '#282828',
            },
          ],
        },
        titleColor: {
          title: this.$t('pagediy.titleColor'),
          color: [
            {
              item: '#333333',
            },
          ],
          default: [
            {
              item: '#333333',
            },
          ],
        },
        //色调
        themeStyleConfig: {
          title: this.$t('pagediy.colorTone'),
          tabVal: 0,
          isShow: 1,
          list: [
            {
              val: this.$t('pagediy.followTheme'),
            },
            {
              val: this.$t('pagediy.customStyle'),
            },
          ],
        },
        priceColor: {
          isShow: 0,
          title: this.$t('pagediy.priceColor'),
          color: [
            {
              item: '#E93323',
            },
          ],
          default: [
            {
              item: '#E93323',
            },
          ],
        },
        bgStyle: {
          tabTitle: this.$t('pagediy.radiusSettings'),
          title: this.$t('pagediy.backgroundCircle'),
          name: 'bgStyle',
          val: 7,
          min: 0,
          max: 30,
        },
        contentStyle: {
          title: this.$t('pagediy.contentRadius'),
          name: 'contentStyle',
          val: 5,
          min: 0,
          max: 30,
        },
        // 上间距
        upConfig: {
          tabTitle: this.$t('pagediy.marginSettings'),
          title: this.$t('pagediy.topMargin'),
          val: 10,
          min: 0,
          max: 100,
        },
        // 下间距
        downConfig: {
          title: this.$t('pagediy.bottomMargin'),
          val: 10,
          min: 0,
        },
        // 左右间距
        lrConfig: {
          title: this.$t('pagediy.leftRightMargin'),
          val: 12,
          min: 0,
          max: 25,
        },
        mbConfig: {
          title: this.$t('pagediy.pageSpacing'),
          val: 10,
          min: 0,
        },
        contentConfig: {
          title: this.$t('pagediy.contentSpacing'),
          val: 10,
          min: 0,
          max: 30,
        },
      },
      configObj: null,
      listStyle: 0,
      spikeList: [
        {
          img: '',
          name: this.$t('pagediy.demoShopName'),
          price: '234',
        },
        {
          img: '',
          name: this.$t('pagediy.demoShopName'),
          price: '177',
          ot_price: '300',
        },
        {
          img: '',
          name: this.$t('pagediy.demoShopName'),
          price: '177',
          ot_price: '300',
        },
        {
          img: '',
          name: this.$t('pagediy.demoShopName'),
          price: '234',
          ot_price: '300',
        },
      ],
      themeStyle: 0,
      themeColor: '',
    };
  },
  mounted() {
    this.$nextTick(() => {
      if (this.num !== undefined && this.num !== null) {
        let pageData = this.$store.state.mobildConfig.defaultArray[this.num];
        this.setConfig(pageData);
      }
    });
  },
  methods: {
    diyUiText,
    setConfig(data) {
      if (!data) return;
      if (data) {
        this.configObj = data;
        this.listStyle = this.configObj.tabConfig.tabVal;
        this.themeStyle = data.themeStyleConfig.tabVal;
        this.themeColor = this.$options.filters.filterTheme(this.mobileTheme - 1);
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.bargain-action{position:relative;display:inline-block;color:#fff;background:linear-gradient(90deg,#ff7931,#e93323);border-radius:20px;font-size:12px;padding:4px 12px;margin:6px 0;}
.more {
  position: relative;
}
.seckill {
  width: auto;
  background: #fff;
  border-radius: 7px;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
  .bg_box {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 128px;
    background-repeat: no-repeat;
    background-size: cover;
  }
  .title {
    .pictrue {
      width: 62px;
      height: 16px;

      el-image,
      .image,
      ::v-deep.el-image {
        width: 100%;
        height: 100%;
      }
    }

    .lines {
      width: 1px;
      height: 12px;
      background-color: #fff;
      opacity: 0.6;
      margin-left: 8px;
    }

    .point {
      font-size: 13px;
      color: #fff;
      margin-left: 10px;
      margin-right: 2px;
      z-index: 9;
    }
    .styleAll {
      width: 17px;
      height: 17px;
      background-color: #2f2f2f;
      border-radius: 3px;
      color: #fff;
      text-align: center;
    }

    .more {
      width: 43px;
      height: 20px;
      background: linear-gradient(142deg, #ffe9ce 0%, #ffd6a7 100%);
      opacity: 1;
      border-radius: 18px;
      font-size: 11px;
      color: #fe960f;
      padding-left: 4px;
      font-weight: 800;
      .iconfont {
        font-size: 10px;
      }
    }
  }

  .conter {
    border-radius: 12px;
    margin-top: 15px;

    .itemCon {
      display: inline-block;
      width: 93px;

      .item {
        width: 100%;

        .pictrue {
          width: 100%;
          height: 93px;
          overflow: hidden;

          image {
            width: 100%;
            height: 100%;
            border-radius: 3px;
          }
        }

        .name {
          font-size: 13px;
          color: #333;
          margin-top: 4px;
        }
        .y_money {
          font-size: 12px;
          color: #999999;
          text-decoration: line-through;
        }

        .x_money {
          font-size: 14px;
          height: 100%;
          font-weight: bold;
          margin-top: 2px;
          .num {
            font-size: 14px;
          }
        }

        .money {
          margin-top: 7px;
        }
      }
    }
  }
}
.conter_y {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  grid-template-rows: auto;
  grid-gap: 10px;
  width: 100%;
  margin-top: 15px;
  .item {
    display: flex;
    width: 100%;

    .pictrue {
      width: 108px;
      height: 108px;

      image {
        width: 100%;
        height: 100%;
      }
    }
    .text-info {
      margin-bottom: 5px;
      z-index: 9;
      flex: 1;
      .title {
        width: 100%;
        height: 40px;
        line-height: 20px;
        color: #333;
      }

      .old-price {
        font-weight: normal;
        font-size: 12px;
        color: #999;
      }

      .price {
        font-size: 18px;
        font-weight: 600;

        text {
          padding-bottom: 2px;
          font-size: 13px;
          font-weight: normal;
        }
      }
    }
  }
}
</style>
