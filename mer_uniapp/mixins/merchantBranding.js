import { getMerDiyInfoApi } from '@/api/merchant';
import { getLocalizedText } from '@/utils/localizedName';

const pending = new Map();
function loadBranding(id) {
  const key = String(id);
  if (!pending.has(key)) {
    const request = getMerDiyInfoApi(id, 0).then(res => {
      let value = res.data && res.data.value;
      if (typeof value === 'string') value = JSON.parse(value);
      const header = Object.values(value || {}).find(item => item &&
        item.merchantBranding && String(item.merchantBranding.merId) === key);
      return header ? header.merchantBranding : {};
    }).catch(() => {
      pending.delete(key);
      return {};
    });
    pending.set(key, request);
  }
  return pending.get(key);
}

export default {
  data() {
    return { merchantBrandingById: {} };
  },
  methods: {
    loadMerchantBranding(items) {
      return Promise.all((items || []).map(item => {
        const id = item && item.id;
        if (!id) return Promise.resolve();
        return loadBranding(id).then(branding => {
          this.$set(this.merchantBrandingById, String(id), branding);
        });
      }));
    },
    merchantMedia(item, field) {
      const branding = this.merchantBrandingById[String(item.id)] || {};
      const media = branding.media || {};
      return getLocalizedText(item[field], media[field] || item[field + 'Json'], this.i18nLocale);
    },
    merchantTypeLabel(item) {
      const branding = this.merchantBrandingById[String(item.id)] || {};
      const fallback = this.$options.filters.merchantTypeFilter(item.typeId);
      const json = String(branding.typeId) === String(item.typeId) ? branding.typeNameJson : null;
      return getLocalizedText(fallback, json, this.i18nLocale);
    }
  }
};
