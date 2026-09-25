<template>
  <div class="bargain-records">
    <el-form :inline="true" size="small" @submit.native.prevent="search">
      <el-form-item :label="$t('bargain.recordKeyword')"><el-input v-model.trim="filters.keyword" clearable maxlength="100" :placeholder="$t('bargain.recordKeyword')" @keyup.enter.native="search"/></el-form-item>
      <el-form-item v-if="platform" :label="$t('bargain.merchantName')"><el-input v-model.trim="filters.merchantName" clearable maxlength="100" @keyup.enter.native="search"/></el-form-item>
      <el-form-item :label="$t('bargain.activityId')"><el-input v-model.trim="filters.activityId" clearable @keyup.enter.native="search"/></el-form-item>
      <el-form-item :label="$t('bargain.user')"><el-input v-model.trim="filters.uid" clearable @keyup.enter.native="search"/></el-form-item>
      <el-form-item :label="$t('bargain.status')"><el-select v-model="filters.status" clearable :placeholder="$t('bargain.allStatuses')"><el-option v-for="(name, id) in statuses" :key="id" :value="Number(id)" :label="$t('bargain.'+name)"/></el-select></el-form-item>
      <el-form-item :label="$t('bargain.order')"><el-input v-model.trim="filters.orderNo" clearable maxlength="64" @keyup.enter.native="search"/></el-form-item>
      <el-form-item :label="$t('bargain.recordCreated')"><el-date-picker v-model="dates" type="datetimerange" value-format="yyyy-MM-dd HH:mm:ss" :default-time="['00:00:00','23:59:59']" :start-placeholder="$t('bargain.startTime')" :end-placeholder="$t('bargain.ends')"/></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" @click="search">{{ $t('bargain.queryRecords') }}</el-button><el-button @click="reset">{{ $t('bargain.resetRecords') }}</el-button></el-form-item>
    </el-form>
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="record-error"/>
    <el-table :data="rows" v-loading="loading" border stripe :empty-text="$t('bargain.noRecords')">
      <el-table-column prop="id" label="ID" width="75"/>
      <el-table-column v-if="platform" :label="$t('bargain.merchantName')" min-width="160"><template slot-scope="s"><div>{{ s.row.merName || '—' }}</div><small>ID: {{ s.row.merId }}</small></template></el-table-column>
      <el-table-column :label="$t('bargain.product')" min-width="300"><template slot-scope="s"><div class="record-product"><el-image v-if="s.row.image" :src="s.row.image" :preview-src-list="[s.row.image]" fit="cover"/><div><div class="record-name">{{ s.row.productName || s.row.activityName || '—' }}</div><small>{{ s.row.sku || '—' }}</small><small>{{ $t('bargain.product') }} ID: {{ s.row.productId || '—' }}</small></div></div></template></el-table-column>
      <el-table-column :label="$t('bargain.name')" min-width="170"><template slot-scope="s">{{ s.row.activityName || '—' }}<small>ID: {{ s.row.activityId }}</small></template></el-table-column>
      <el-table-column :label="$t('bargain.recordUser')" min-width="140"><template slot-scope="s">{{ s.row.nickname || '—' }}<small>ID: {{ s.row.uid }}</small></template></el-table-column>
      <el-table-column :label="$t('bargain.recordPrices')" min-width="155"><template slot-scope="s"><strong>{{ $t('bargain.current') }}: {{ money(s.row.currentPrice) }}</strong><small>{{ $t('bargain.original') }}: {{ money(s.row.price) }}</small><small>{{ $t('bargain.floor') }}: {{ money(s.row.minPrice) }}</small></template></el-table-column>
      <el-table-column :label="$t('bargain.cutTotal')" min-width="160"><template slot-scope="s"><strong>{{ money(s.row.cutTotal) }}</strong><small>{{ $t('bargain.remainingCut') }}: {{ money(s.row.remainingCut) }}</small></template></el-table-column>
      <el-table-column :label="$t('bargain.recordProgress')" min-width="220"><template slot-scope="s"><el-progress :percentage="progress(s.row)" :stroke-width="6"/><small>{{ $t('bargain.cutCount') }}: {{ s.row.helpCount || 0 }} / {{ s.row.peopleNum || '—' }}</small><small>{{ $t('bargain.remainingCount') }}: {{ s.row.peopleNum == null ? '—' : Math.max(0,s.row.peopleNum-(s.row.helpCount||0)) }}</small><small>{{ $t('bargain.attemptNo') }}: {{ s.row.attemptNo || '—' }}</small></template></el-table-column>
      <el-table-column :label="$t('bargain.status')" width="115"><template slot-scope="s"><el-tag size="small" :type="tagType(s.row)">{{ $t('bargain.' + statusName(s.row)) }}</el-tag></template></el-table-column>
      <el-table-column :label="$t('bargain.recordCreated')" min-width="175"><template slot-scope="s">{{ s.row.createTime || '—' }}<small>{{ $t('bargain.ends') }}: {{ s.row.endTime || '—' }}</small></template></el-table-column>
      <el-table-column :label="$t('bargain.order')" min-width="200"><template slot-scope="s"><span class="order-number">{{ s.row.orderNo || '—' }}</span></template></el-table-column>
      <el-table-column :label="$t('bargain.helpDetails')" fixed="right" width="130"><template slot-scope="s"><el-button type="text" @click="$refs.details.open(s.row)">{{ $t('bargain.helpDetails') }}</el-button></template></el-table-column>
    </el-table>
    <help-details ref="details" :platform="platform"/>
    <el-pagination :current-page.sync="page" :page-size="limit" :page-sizes="[10,20,50]" :total="total" layout="total, sizes, prev, pager, next, jumper" @current-change="load" @size-change="resize"/>
  </div>
</template>
<script>
import request from '@/utils/request';
import HelpDetails from './HelpDetails.vue';
const empty = () => ({ keyword:'',merchantName:'',activityId:'',uid:'',status:'',orderNo:'' });
export default {
  components:{HelpDetails},
  props: { platform: Boolean },
  data: () => ({filters:empty(),applied:{},dates:[],rows:[],total:0,page:1,limit:20,loading:false,error:'',requestVersion:0,statuses:{1:'inProgress',2:'success',3:'ordered',4:'expired'}}),
  created(){this.load();},
  beforeDestroy(){this.requestVersion++;},
  methods: {
    money(value){return value == null ? '—' : Number(value).toFixed(2);},
    progress(row){return row.peopleNum ? Math.min(100,Math.max(0,Math.round((row.helpCount||0)/row.peopleNum*100))) : 0;},
    statusName(row){return this.statuses[row.displayStatus || row.status] || 'unknownRecordStatus';},
    tagType(row){return ({inProgress:'warning',success:'success',ordered:'',expired:'info'})[this.statusName(row)] || 'info';},
    search(){
      for(const key of ['activityId','uid']) if(this.filters[key] && !/^[1-9]\d{0,9}$/.test(this.filters[key])) {this.$message.error(this.$t('bargain.invalidRecordId'));return;}
      this.applied={...this.filters,startTime:this.dates && this.dates[0],endTime:this.dates && this.dates[1]};this.page=1;this.load();
    },
    reset(){this.filters=empty();this.dates=[];this.applied={};this.page=1;this.load();},
    resize(size){this.limit=size;this.page=1;this.load();},
    async load(){
      const version=++this.requestVersion;this.loading=true;this.error='';
      const params={page:this.page,limit:this.limit};
      Object.keys(this.applied).forEach(key=>{if(this.applied[key]!=='' && this.applied[key]!=null)params[key]=this.applied[key];});
      try{const res=await request({url:'/admin/'+(this.platform?'platform':'merchant')+'/bargain/records',method:'get',params});if(version!==this.requestVersion)return;this.rows=res.list||[];this.total=res.total||0;}
      catch(e){if(version!==this.requestVersion)return;this.rows=[];this.total=0;this.error=this.$t('bargain.recordsLoadFailed');}
      finally{if(version===this.requestVersion)this.loading=false;}
    }
  }
};
</script>
<style scoped>
.bargain-records{padding-top:8px}.bargain-records .el-input,.bargain-records .el-select{width:200px}.record-product{display:flex;gap:12px;align-items:center}.record-product .el-image{width:58px;height:58px;flex-shrink:0;border-radius:6px;background:#f5f7fa}.record-name{font-weight:500;line-height:1.6;overflow-wrap:anywhere}.bargain-records small{display:block;color:#909399;line-height:1.7;margin-top:4px}.bargain-records strong{color:#e65b36;font-weight:600}.order-number{overflow-wrap:anywhere}.el-pagination{margin-top:20px;text-align:right}.record-error{margin-bottom:16px}
</style>
