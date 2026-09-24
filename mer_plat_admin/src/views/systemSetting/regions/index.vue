<template>
  <div class="app-container">
    <el-card v-loading="loading">
      <div slot="header">配送地区 · 多语言名称</div>
      <el-alert title="译名跟随平台已启用语言显示。留空使用原名称；停用语言保留译名，重新启用后可继续编辑。" type="info" :closable="false" />
      <div class="tools">
        <el-input v-model="search" placeholder="搜索地区名称或 ID" clearable style="width: 300px" />
        <el-button @click="load">刷新地区和语言</el-button>
      </div>
      <el-table :data="filtered" row-key="regionId">
        <el-table-column prop="regionId" label="地区 ID" width="100" />
        <el-table-column prop="path" label="地区路径（原名称）" min-width="260" />
        <el-table-column v-for="lang in languages" :key="lang.code" :label="lang.name" min-width="160">
          <template slot-scope="scope">{{ translated(scope.row, lang.code) || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="checkPermi(['platform:city:region:edit'])" type="text" @click="edit(scope.row)">编辑译名</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog title="编辑地区译名" :visible.sync="visible" width="600px" :close-on-click-modal="false">
      <el-form v-if="selected" label-width="140px" @submit.native.prevent>
        <el-form-item label="地区 ID">{{ selected.regionId }}</el-form-item>
        <el-form-item label="原名称">{{ selected.path }}</el-form-item>
        <el-form-item v-for="lang in languages" :key="lang.code" :label="lang.name + ' (' + lang.code + ')'">
          <el-input v-model="draft[lang.code]" maxlength="128" show-word-limit placeholder="留空使用原名称" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="!languages.length" @click="save">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import { regionTree, saveRegionNames } from '@/api/regionNames';
import { systemLanguageList } from '@/api/systemLanguage';
import { checkPermi } from '@/utils/permission';
const normalize = code => String(code || '').trim().toLowerCase().replace(/_/g, '-');
function names(row) {
  try { return JSON.parse(row.regionNames || '{}') || {}; } catch (_) { return {}; }
}
export default {
  name: 'DeliveryRegionNames',
  data() { return { loading: false, saving: false, rows: [], languages: [], search: '', visible: false, selected: null, draft: {} }; },
  computed: {
    filtered() {
      const query = this.search.trim().toLowerCase();
      return this.rows.filter(row => (row.path + ' ' + row.regionId + ' ' + Object.values(names(row)).join(' ')).toLowerCase().includes(query));
    }
  },
  created() { this.load(); },
  methods: {
    checkPermi,
    async load() {
      if (!checkPermi(['platform:city:region:list:tree'])) return;
      this.loading = true;
      try {
        const [tree, langs] = await Promise.all([regionTree(), systemLanguageList()]);
        const rows = [];
        const visit = (nodes, path) => (nodes || []).forEach(node => {
          const full = path.concat(node.regionName);
          rows.push({ ...node, path: full.join(' / ') });
          visit(node.child, full);
        });
        visit(tree, []);
        this.rows = rows;
        this.languages = Array.isArray(langs) ? langs : [];
      } finally { this.loading = false; }
    },
    translated(row, code) {
      const values = names(row);
      const key = Object.keys(values).find(key => normalize(key) === normalize(code));
      return key ? values[key] : '';
    },
    edit(row) {
      this.selected = row;
      this.draft = {};
      this.languages.forEach(lang => this.$set(this.draft, lang.code, this.translated(row, lang.code)));
      this.visible = true;
    },
    async save() {
      if (this.saving || !this.selected) return;
      this.saving = true;
      try {
        await saveRegionNames({ oldRegionId: this.selected.regionId, regionId: this.selected.regionId,
          regionName: this.selected.regionName, regionNames: JSON.stringify(this.draft) });
        this.$message.success('译名已保存');
        this.visible = false;
        await this.load();
      } finally { this.saving = false; }
    }
  }
};
</script>
<style scoped>
.tools { display: flex; gap: 12px; margin: 20px 0; }
</style>
