<template>
  <el-dialog :title="$t('bargain.helpDetails')" :visible.sync="visible" width="90%" append-to-body @close="close">
    <template v-if="selected">
      <div class="product"><el-image v-if="selected.image" :src="selected.image" fit="cover"/><div><b>{{ selected.productName || selected.activityName }}</b><p>{{ selected.sku }} · ID: {{ selected.id }}</p><p>{{ selected.nickname || '—' }} · UID: {{ selected.uid }}</p></div></div>
      <div class="summary"><span>{{ $t('bargain.original') }}: <b>{{ money(selected.price) }}</b></span><span>{{ $t('bargain.current') }}: <b>{{ money(selected.currentPrice) }}</b></span><span>{{ $t('bargain.cutTotal') }}: <b>{{ money(selected.cutTotal) }}</b></span><span>{{ $t('bargain.cutCount') }}: <b>{{ selected.helpCount || 0 }} / {{ selected.peopleNum || '—' }}</b></span></div>
      <el-alert :title="$t('bargain.firstCutNote')" type="info" :closable="false" show-icon/>
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon/>
      <el-table :data="rows" v-loading="loading" border stripe :empty-text="$t('bargain.noRecords')" class="details">
        <el-table-column prop="sequence" :label="$t('bargain.cutSequence')" width="90"/>
        <el-table-column :label="$t('bargain.helper')" min-width="190"><template slot-scope="s"><div class="helper"><el-avatar :src="s.row.avatar" :size="32"/><div>{{ s.row.nickname || '—' }}<small>UID: {{ s.row.uid }}</small></div></div></template></el-table-column>
        <el-table-column :label="$t('bargain.cutType')" min-width="150"><template slot-scope="s"><el-tag :type="s.row.selfCut ? 'info' : 'success'">{{ $t('bargain.'+(s.row.selfCut ? 'selfCut' : 'friendCut')) }}</el-tag></template></el-table-column>
        <el-table-column :label="$t('bargain.beforePrice')" min-width="125"><template slot-scope="s">{{ money(s.row.beforePrice) }}</template></el-table-column>
        <el-table-column :label="$t('bargain.cutAmount')" min-width="125"><template slot-scope="s"><strong>{{ money(s.row.cutPrice) }}</strong></template></el-table-column>
        <el-table-column :label="$t('bargain.afterPrice')" min-width="125"><template slot-scope="s">{{ money(s.row.afterPrice) }}</template></el-table-column>
        <el-table-column prop="createTime" :label="$t('bargain.cutTime')" min-width="180"/>
      </el-table>
      <el-pagination :current-page.sync="page" :page-size="20" :total="total" layout="total, prev, pager, next" @current-change="load"/>
    </template>
  </el-dialog>
</template>
<script>
import request from '@/utils/request';
export default {
  props:{platform:Boolean},
  data:()=>({visible:false,selected:null,rows:[],page:1,total:0,loading:false,error:'',version:0}),
  beforeDestroy(){this.version++;},
  methods:{
    money(value){return value == null ? '—' : Number(value).toFixed(2);},
    open(row){this.selected={...row};this.rows=[];this.total=0;this.page=1;this.visible=true;this.load();},
    close(){this.version++;this.loading=false;},
    async load(){
      const version=++this.version;this.loading=true;this.error='';this.rows=[];
      try {
        const res=await request({url:'/admin/'+(this.platform?'platform':'merchant')+'/bargain/records/'+this.selected.id+'/helps',method:'get',params:{page:this.page,limit:20}});
        if(version!==this.version)return;
        this.rows=res.list||[];this.total=res.total||0;
        if(res.record)this.selected={...this.selected,...res.record,price:res.price,minPrice:res.minPrice,cutTotal:(Math.round(Number(res.price)*100)-Math.round(Number(res.record.currentPrice)*100))/100};
      } catch(e){if(version===this.version)this.error=this.$t('bargain.recordsLoadFailed');}
      finally{if(version===this.version)this.loading=false;}
    }
  }
};
</script>
<style scoped>
.product,.helper{display:flex;align-items:center;gap:12px}.product .el-image{width:64px;height:64px;border-radius:6px;flex-shrink:0}.product p{margin:5px 0;color:#909399}.summary{display:flex;flex-wrap:wrap;gap:24px;padding:20px 0}.summary b,strong{color:#e65b36;font-variant-numeric:tabular-nums}small{display:block;color:#909399;margin-top:4px}.details{margin-top:16px}.el-pagination{margin-top:16px;text-align:right}
</style>
