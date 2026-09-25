<template>
  <div class="page"><el-card shadow="never"><h2>{{ $t('bargain.title') }}</h2>
    <el-input v-if="tab === 'list'" v-model="merId" :placeholder="$t('bargain.merchant')" clearable style="width:200px" @change="reset"/>
    <el-tabs v-model="tab" @tab-click="reset"><el-tab-pane :label="$t('bargain.title')" name="list"/><el-tab-pane :label="$t('bargain.records')" name="records"/></el-tabs>
    <bargain-records v-if="tab === 'records'" platform/>
    <el-table v-if="tab === 'list'" key="activities" :data="rows" v-loading="loading"><el-table-column prop="id" label="ID" width="75"/><el-table-column :label="$t('bargain.merchantName')" min-width="160"><template slot-scope="s"><div>{{ s.row.merName || $t('bargain.merchantUnavailable') }}</div><div class="merchant-id">ID: {{ s.row.merId }}</div></template></el-table-column>
      <template v-if="tab === 'list'"><el-table-column prop="name" :label="$t('bargain.name')"/><el-table-column prop="price" :label="$t('bargain.original')"/><el-table-column prop="minPrice" :label="$t('bargain.floor')"/><el-table-column prop="stock" :label="$t('bargain.stock')"/>
      <el-table-column :label="$t('bargain.auditState')"><template slot-scope="s">{{ $t('bargain.'+['pending','approved','rejected'][s.row.auditStatus]) }}</template></el-table-column><el-table-column :label="$t('bargain.activityState')" min-width="110"><template slot-scope="s"><el-tag size="small" :type="activityState(s.row) === 'running' ? 'success' : 'info'">{{ $t('bargain.' + activityState(s.row)) }}</el-tag></template></el-table-column>
      <el-table-column width="280"><template slot-scope="s"><el-button type="text" @click="inspect(s.row)">{{ $t('bargain.detailTitle') }}</el-button><el-button type="text" v-if="Number(s.row.auditStatus) === 0" @click="audit(s.row,true)">{{ $t('bargain.approve') }}</el-button><el-button type="text" v-if="Number(s.row.auditStatus) === 0" @click="audit(s.row,false)">{{ $t('bargain.reject') }}</el-button><el-button type="text" v-if="Number(s.row.auditStatus) === 1" @click="toggle(s.row)">{{ $t(s.row.enabled?'bargain.off':'bargain.on') }}</el-button></template></el-table-column></template>
      
    </el-table><el-pagination v-if="tab === 'list'" :current-page.sync="page" :page-size="20" :total="total" layout="prev, pager, next, total" @current-change="load"/>
    </el-card>
    <el-dialog :visible.sync="visible" :title="$t('bargain.detailTitle')" width="800px" custom-class="bargain-detail-dialog" top="6vh">
      <div v-if="detail" class="activity-detail">
        <div class="detail-hero">
          <el-image v-if="detail.image" class="detail-image" :src="detail.image" fit="cover" :preview-src-list="[detail.image]"/>
          <div class="detail-heading"><h3>{{ detail.name }}</h3><p>{{ $t('bargain.merchantName') }}：{{ detail.merName || $t('bargain.merchantUnavailable') }} <span class="merchant-id">(ID: {{ detail.merId }})</span></p><el-tag size="small" :type="['warning','success','danger'][detail.auditStatus]">{{ $t('bargain.'+['pending','approved','rejected'][detail.auditStatus]) }}</el-tag><el-tag size="small" style="margin-left:8px" :type="activityState(detail) === 'running' ? 'success' : 'info'">{{ $t('bargain.activityState') }}：{{ $t('bargain.' + activityState(detail)) }}</el-tag></div>
        </div>
        <div class="price-summary"><div v-for="field in priceFields" :key="field.key"><span>{{ $t('bargain.'+field.label) }}</span><strong>{{ detail[field.key] }}</strong></div></div>
        <section class="detail-section"><h4>{{ $t('bargain.ruleSettings') }}</h4><dl class="detail-grid"><div v-for="field in ruleFields" :key="field.key"><dt>{{ $t('bargain.'+field.label) }}</dt><dd>{{ detail[field.key] }}</dd></div></dl></section>
        <section class="detail-section"><h4>{{ $t('bargain.rules') }}</h4><p class="detail-copy">{{ detail.description || $t('bargain.notProvided') }}</p></section>
        <section class="detail-section" v-if="translationCodes.length"><h4>{{ $t('bargain.translationsTitle') }}</h4><el-tabs v-model="detailLang"><el-tab-pane v-for="code in translationCodes" :key="code" :name="code" :label="languageName(code)"><dl class="translation-fields"><dt>{{ $t('bargain.name') }}</dt><dd>{{ translations[code] || detail.name }}</dd><dt>{{ $t('bargain.rules') }}</dt><dd class="detail-copy">{{ descriptions[code] || detail.description || $t('bargain.notProvided') }}</dd></dl></el-tab-pane></el-tabs></section>
        <section v-if="detail.auditRemark" class="detail-section"><h4>{{ $t('bargain.reason') }}</h4><p class="detail-copy">{{ detail.auditRemark }}</p></section>
      </div>
      <span slot="footer"><el-button @click="visible=false">{{ $t('bargain.closeDetail') }}</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import request from '@/utils/request';
import BargainRecords from '@/components/BargainRecords';
import { bargainActivityStatus } from '@/utils/bargainActivityStatus';
import { systemLanguageList } from '@/api/systemLanguage';
import { defaultLangList } from '@/i18n/defaultLangList';
const base='/admin/platform/bargain';
export default {
 components: { BargainRecords },
 data:()=>({now:Date.now(),statusTimer:null,tab:'list',rows:[],page:1,total:0,merId:'',loading:false,visible:false,detail:null,translations:{},descriptions:{},detailLang:'',languages:[],priceFields:[{key:'price',label:'original'},{key:'minPrice',label:'floor'},{key:'stock',label:'stock'}],ruleFields:[{key:'peopleNum',label:'people'},{key:'startLimit',label:'startLimitLabel'},{key:'helpLimit',label:'helpLimitLabel'},{key:'sort',label:'sort'},{key:'startTime',label:'startTime'},{key:'endTime',label:'ends'}]}),
 computed:{translationCodes(){return [...new Set([...Object.keys(this.translations),...Object.keys(this.descriptions)])];}},
 created(){this.load();systemLanguageList().then(list=>{this.languages=list||[];}).catch(()=>{this.languages=defaultLangList.map(l=>({code:l.value,name:l.label}));});},
 mounted(){this.statusTimer=setInterval(()=>{this.now=Date.now();},30000);},
 beforeDestroy(){clearInterval(this.statusTimer);},
 methods:{
  activityState(row){return bargainActivityStatus(row,this.now);},
  languageName(code){const lang=this.languages.find(l=>l.code===code);return lang ? lang.name : code;},
  reset(){this.page=1;this.load();},
  async load(){if(this.tab !== 'list')return;this.loading=true;try{const res=await request({url:base+'/'+this.tab,method:'get',params:{page:this.page,limit:20,merId:this.merId||undefined}});this.rows=res.list;this.total=res.total;}finally{this.loading=false;}},
  async inspect(row){this.detail={...row,...await request({url:base+'/detail/'+row.id,method:'get'})};this.detail.merName=this.detail.merName||row.merName;this.translations=JSON.parse(this.detail.nameJson||'{}');this.descriptions=JSON.parse(this.detail.descriptionJson||'{}');this.detailLang=this.translationCodes[0]||'';this.visible=true;},
  async audit(row,approved){try{const {value}=await this.$prompt(this.$t('bargain.reason'),this.$t(approved?'bargain.approve':'bargain.reject'),{inputValue:'',inputValidator:v=>(!v||v.length<=500)});await request({url:base+'/audit/'+row.id,method:'post',params:{approved,remark:value||''}});this.load();}catch(e){if(e!=='cancel'&&e!=='close')throw e;}},
  async toggle(row){await request({url:base+'/enabled/'+row.id,method:'post',params:{enabled:!row.enabled}});this.load();}
 }
};
</script>
<style scoped>.page{padding:20px}.el-pagination{margin-top:24px}.merchant-id{font-size:12px;color:#909399;line-height:20px}
.activity-detail{max-height:65vh;overflow-y:auto;padding-right:8px;color:#303133;line-height:1.6}.detail-hero{display:flex;gap:18px;align-items:flex-start}.detail-image{width:88px;height:88px;flex-shrink:0;border-radius:8px;background:#f5f7fa}.detail-heading{min-width:0}.detail-heading h3{margin:0 0 8px;font-size:18px;overflow-wrap:anywhere}.detail-heading p{margin:0 0 8px}.price-summary{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px;background:#f5f7fa;padding:18px;border-radius:8px;margin:22px 0}.price-summary span{display:block;color:#909399;font-size:13px}.price-summary strong{display:block;margin-top:6px;font-size:24px;font-weight:600}.detail-section{margin-top:22px}.detail-section h4{margin:0 0 12px;padding-bottom:10px;border-bottom:1px solid #ebeef5;font-size:15px}.detail-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px 24px;margin:0}.detail-grid dt,.translation-fields dt{color:#909399;font-size:13px;margin-bottom:4px}.detail-grid dd,.translation-fields dd{margin:0;overflow-wrap:anywhere}.translation-fields{margin:0}.translation-fields dt:not(:first-child){margin-top:14px}.detail-copy{white-space:pre-wrap;overflow-wrap:anywhere;margin:0;font-size:14px}
</style>
<style>.bargain-detail-dialog{max-width:calc(100vw - 32px)}.bargain-detail-dialog .el-dialog__body{padding:22px 26px}.bargain-detail-dialog .el-dialog__footer{border-top:1px solid #ebeef5}</style>
