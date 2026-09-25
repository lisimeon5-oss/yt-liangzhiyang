<template>
  <div class="bargain-page">
    <el-card shadow="never">
      <div class="toolbar"><h2>{{ $t('bargain.title') }}</h2><el-button type="primary" @click="create">{{ $t('bargain.create') }}</el-button></div>
      <el-tabs v-model="tab" @tab-click="reset"><el-tab-pane :label="$t('bargain.title')" name="list"/><el-tab-pane :label="$t('bargain.records')" name="records"/></el-tabs>
      <bargain-records v-if="tab === 'records'" />
    <el-table v-if="tab === 'list'" key="activities" :data="rows" v-loading="loading">
        <el-table-column prop="id" label="ID" width="75"/>
        <template v-if="tab === 'list'">
          <el-table-column prop="name" :label="$t('bargain.name')" min-width="180"/>
          <el-table-column prop="price" :label="$t('bargain.original')"/>
          <el-table-column prop="minPrice" :label="$t('bargain.floor')"/>
          <el-table-column prop="stock" :label="$t('bargain.stock')"/>
          <el-table-column :label="$t('bargain.auditState')"><template slot-scope="s">{{ $t('bargain.' + ['pending','approved','rejected'][s.row.auditStatus]) }}<div>{{ s.row.auditRemark }}</div></template></el-table-column><el-table-column :label="$t('bargain.activityState')" min-width="110"><template slot-scope="s"><el-tag size="small" :type="activityState(s.row) === 'running' ? 'success' : 'info'">{{ $t('bargain.' + activityState(s.row)) }}</el-tag></template></el-table-column>
          <el-table-column prop="endTime" :label="$t('bargain.ends')" min-width="160"/>
          <el-table-column width="170"><template slot-scope="s"><el-button type="text" @click="edit(s.row)">{{ $t('bargain.edit') }}</el-button><el-button type="text" v-if="Number(s.row.auditStatus) === 1" @click="toggle(s.row)">{{ $t(s.row.enabled ? 'bargain.off' : 'bargain.on') }}</el-button></template></el-table-column>
        </template>
        
      </el-table>
      <el-pagination v-if="tab === 'list'" :current-page.sync="page" :page-size="20" :total="total" layout="prev, pager, next, total" @current-change="load"/>
    </el-card>
    <el-dialog :visible.sync="visible" :title="$t('bargain.create')" width="760px" :close-on-click-modal="false">
      <el-form label-width="190px" :model="form">
        <el-form-item :label="$t('bargain.product')">
          <div v-if="selectedProduct" class="selected-product" v-loading="productLoading">
            <el-image v-if="selectedProduct.image" :src="selectedProduct.image" :preview-src-list="[selectedProduct.image]" fit="cover" class="selected-product-image"/>
            <div class="selected-product-info"><div>{{ selectedProduct.name }}</div><small>ID: {{ selectedProduct.id }}</small><div class="product-actions"><el-button type="text" @click="changeProduct">{{ $t('bargain.changeProduct') }}</el-button><el-button type="text" @click="clearProduct">{{ $t('bargain.clearProduct') }}</el-button></div></div>
          </div>
          <el-button v-else type="primary" icon="el-icon-plus" @click="changeProduct">{{ $t('marketing.addActivityProduct') }}</el-button>
        </el-form-item>
        <el-form-item :label="$t('bargain.sku')"><el-select v-model="form.attrValueId" :disabled="!form.productId || productLoading" v-loading="productLoading" style="width:100%"><el-option v-for="s in skus" :key="s.id" :value="s.id" :label="s.sku + ' · ' + $t('bargain.skuPrice') + ' ' + s.price + ' · ' + $t('bargain.skuStock') + ' ' + s.stock"/></el-select><div class="from-tips">{{ $t('bargain.formTips.sku') }}</div></el-form-item>
        <el-form-item :label="$t('bargain.name')"><el-input v-model="form.name" maxlength="255"/></el-form-item>
        <el-form-item :label="$t('bargain.rules')"><el-input v-model="form.description" type="textarea" maxlength="5000"/></el-form-item>
        <el-tabs v-model="lang"><el-tab-pane v-for="l in languages" :key="l.code" :name="l.code" :label="l.name"/></el-tabs>
        <el-form-item :label="$t('bargain.name') + ' · ' + lang"><el-input :value="names[lang]" @input="$set(names,lang,$event)" maxlength="255"/></el-form-item>
        <el-form-item :label="$t('bargain.rules') + ' · ' + lang"><el-input type="textarea" :value="descriptions[lang]" @input="$set(descriptions,lang,$event)" maxlength="5000"/><div class="from-tips">{{ $t('bargain.formTips.translation') }}</div></el-form-item>
        <el-form-item v-for="n in numericFields" :key="n.key" :label="$t('bargain.' + n.label)"><el-input-number v-model="form[n.key]" :min="n.min" :max="n.max" :precision="n.decimal ? 2 : 0"/><span v-if="n.key === 'price' && selectedSku" class="sku-price">{{ $t('bargain.skuPrice') }}：<strong>{{ selectedSku.price }}</strong></span><div class="from-tips">{{ $t('bargain.formTips.' + n.key) }}</div></el-form-item>
        <el-form-item :label="$t('bargain.startTime')"><el-date-picker v-model="form.startTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"/></el-form-item>
        <el-form-item :label="$t('bargain.ends')"><el-date-picker v-model="form.endTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss"/><div class="from-tips">{{ $t('bargain.formTips.time') }}</div></el-form-item>
      </el-form>
      <span slot="footer"><el-button type="primary" :loading="saving" @click="save">{{ $t('bargain.save') }}</el-button></span>
    </el-dialog>
  </div>
</template>
<script>
import request from '@/utils/request';
import BargainRecords from '@/components/BargainRecords';
import { bargainActivityStatus } from '@/utils/bargainActivityStatus';
import { productDetailApi } from '@/api/product';
import { systemLanguageList } from '@/api/systemLanguage';
import { defaultLangList } from '@/i18n/defaultLangList';
const base = '/admin/merchant/bargain';
const fresh = () => ({ productId: null, attrValueId: null, name: '', description: '', price: 1, minPrice: 0.01, stock: 1, peopleNum: 3, startLimit: 1, helpLimit: 3, sort: 0, startTime: '', endTime: '' });
export default {
 components: { BargainRecords },
  data: () => ({ now:Date.now(),statusTimer:null,tab: 'list', rows: [], total: 0, page: 1, loading: false, visible: false, saving: false, form: fresh(), productLoading: false, productRequest: 0, searchRequest: 0, autoProductName: '', selectedProduct: null, products: [], skus: [], languages: [], lang: 'zh-cn', names: {}, descriptions: {},
    numericFields: [{key:'price',label:'original',min:0.01,max:99999999,decimal:true},{key:'minPrice',label:'floor',min:0.01,max:99999999,decimal:true},{key:'stock',label:'stock',min:1,max:99999999},{key:'peopleNum',label:'people',min:2,max:1000},{key:'startLimit',label:'startLimitLabel',min:1,max:1000},{key:'helpLimit',label:'helpLimitLabel',min:1,max:1000},{key:'sort',label:'sort',min:0,max:99999}] }),
  computed: {
    selectedSku() { return this.skus.find(s => String(s.id) === String(this.form.attrValueId)); }
  },
  created() { this.load(); systemLanguageList().then(list => { this.languages = list || []; if(this.languages.length) this.lang = this.languages[0].code; }).catch(() => { this.languages = defaultLangList.map(l => ({code:l.value,name:l.label})); }); },
  mounted(){this.statusTimer=setInterval(()=>{this.now=Date.now();},30000);},
  beforeDestroy(){clearInterval(this.statusTimer);},
  methods: {
    activityState(row){return bargainActivityStatus(row,this.now);},
    reset() { this.page = 1; this.load(); },
    async load() { if(this.tab !== 'list')return; this.loading = true; try { const res = await request({url:base+'/'+this.tab,method:'get',params:{page:this.page,limit:20}}); this.rows=res.list;this.total=res.total; } finally {this.loading=false;} },
    changeProduct() {
      this.$modalActivityProduct(row => {
        if (!row || !row.id || !this.visible) return;
        if (String(row.id) === String(this.form.productId)) return;
        this.form.productId = row.id;
        this.selectedProduct = row;
        this.selectProduct(row.id);
      }, 'one', this.selectedProduct ? [this.selectedProduct] : [], 0, 0);
    },
    clearProduct() { this.selectedProduct = null; this.form.productId = null; this.selectProduct(null); },
    async selectProduct(id) {
      const version = ++this.productRequest;
      const previousName = this.autoProductName;
      if (this.form.name === previousName) this.form.name = '';
      this.autoProductName = '';
      this.skus = [];
      this.form.attrValueId = null;
      const defaults = fresh();
      ['price', 'minPrice', 'stock'].forEach(key => { this.form[key] = defaults[key]; });
      this.productLoading = false;
      if (!id) return;
      this.productLoading = true;
      try {
        const res = await productDetailApi(id);
        if (version !== this.productRequest || this.form.productId !== id) return;
        this.selectedProduct = { ...res, id };
        this.skus = res.attrValueList || [];
        if (!this.form.name) this.form.name = res.name;
        this.autoProductName = res.name;
      } finally {
        if (version === this.productRequest) this.productLoading = false;
      }
    },
    create() {++this.productRequest;this.productLoading=false;this.autoProductName='';this.products=[];this.selectedProduct=null;this.form=fresh();this.names={};this.descriptions={};this.skus=[];this.visible=true;},
    async edit(row) {++this.productRequest;this.productLoading=false;this.autoProductName='';const res=await request({url:base+'/detail/'+row.id,method:'get'});this.form=Object.assign(fresh(),res);this.names=JSON.parse(res.nameJson||'{}');this.descriptions=JSON.parse(res.descriptionJson||'{}');const product=await productDetailApi(res.productId);this.selectedProduct={...product,id:res.productId};this.skus=product.attrValueList||[];this.autoProductName=product.name;this.visible=true;},
    async save() {if(this.saving)return;if(this.productLoading||!this.form.productId||!this.form.name.trim()||!this.form.attrValueId||!this.form.startTime||!this.form.endTime){this.$message.error(this.$t('bargain.invalidRules'));return;}this.saving=true;try {await request({url:base+'/save',method:'post',data:{...this.form,nameJson:JSON.stringify(this.names),descriptionJson:JSON.stringify(this.descriptions)}});this.visible=false;this.$message.success(this.$t('bargain.saved'));this.load();}finally{this.saving=false;}},
    async toggle(row) {await request({url:base+'/enabled/'+row.id,method:'post',params:{enabled:!row.enabled}});this.load();}
  }
};
</script>
<style scoped>.bargain-page{padding:20px}.toolbar{display:flex;align-items:center;justify-content:space-between}.toolbar h2{font-size:20px}.el-pagination{margin-top:24px}.el-tabs{margin:16px 0}.from-tips{margin-top:6px;color:#909399;font-size:12px;line-height:1.6;word-break:normal;overflow-wrap:break-word}.sku-price{display:inline-block;margin-left:14px;color:#606266;font-size:13px}.sku-price strong{color:#e6a23c;font-weight:600}</style>

<style scoped>.selected-product{display:flex;gap:16px;padding:14px;border:1px solid #ebeef5;border-radius:6px;background:#fafafa}.selected-product-image{width:80px;height:80px;flex-shrink:0;border-radius:4px}.selected-product-info{min-width:0;line-height:1.6;overflow-wrap:anywhere}.selected-product-info small{color:#909399}.product-actions{line-height:1.4}</style>
