// Display-only helpers: never write translated names into delivery payloads.
export function regionDisplayName(region, locale = 'zh-cn') {
  if (!region) return '';
  const normalize = code => String(code || '').trim().toLowerCase().replace(/_/g, '-');
  const lang = normalize(locale);
  let names = region.regionNames || {};
  if (typeof names === 'string') {
    try { names = JSON.parse(names); } catch (_) { names = {}; }
  }
  if (!names || typeof names !== 'object' || Array.isArray(names)) names = {};
  const key = Object.keys(names).find(code => normalize(code) === lang);
  const text = key && typeof names[key] === 'string' ? names[key].trim() : '';
  return text || region.regionName || '';
}

export function indexRegions(tree) {
  const result = {};
  const visit = nodes => (nodes || []).forEach(node => {
    result[String(node.regionId)] = node;
    visit(node.child);
  });
  visit(tree);
  return result;
}

export function savedRegion(index, id, originalName, parentId) {
  if (id != null && index[String(id)]) return index[String(id)];
  // Legacy street addresses have no street ID: match only within their own district.
  if (parentId != null && originalName) {
    const matches = Object.keys(index).map(key => index[key]).filter(node =>
      String(node.parentId) === String(parentId) && node.regionName === originalName);
    if (matches.length === 1) return matches[0];
  }
  return { regionName: originalName || '' };
}

export function addressDisplayText(address, index, locale) {
  if (!address) return '';
  const parts = [['province', 'provinceId'], ['city', 'cityId'], ['district', 'districtId'], ['street', 'streetId']]
    .map(([name, id]) => regionDisplayName(savedRegion(index, address[id], address[name],
      name === 'street' ? address.districtId : null), locale)).filter(Boolean);
  return parts.concat(address.detail ? [address.detail] : []).join(' / ');
}
