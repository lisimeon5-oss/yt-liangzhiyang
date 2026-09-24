import { getRegionTree } from '@/api/api.js';
import { getLocale } from '@/i18n';
import { indexRegions, regionDisplayName, savedRegion, addressDisplayText } from '@/utils/regionNames.js';

export default {
  data() { return { regionNameIndex: {} }; },
  computed: {
    regionLocale() { return this.$store.getters.locale || getLocale(); }
  },
  onShow() { this.refreshRegionNames(); },
  methods: {
    refreshRegionNames() {
      // Refresh on entry so edits in the platform take effect without clearing device storage.
      return getRegionTree().then(res => {
        this.regionNameIndex = indexRegions(res.data || []);
      }).catch(() => { /* Keep original address readable when the network is unavailable. */ });
    },
    displayRegion(region) {
      const entry = savedRegion(this.regionNameIndex, region.regionId, region.regionName, region.parentId);
      return regionDisplayName(Object.assign({}, region, entry), this.regionLocale);
    },
    displayAddress(address) {
      return addressDisplayText(address, this.regionNameIndex, this.regionLocale);
    }
  }
};
