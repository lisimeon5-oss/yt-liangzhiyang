-- Run ONCE before deploying the region-i18n backend. This script has NOT been applied.
-- Replaces the unexecuted fixed-language-column draft. No IDs/hierarchy/snapshots change.
ALTER TABLE eb_city_region
  ADD COLUMN region_names TEXT NULL COMMENT 'Display names JSON keyed by language code';
-- Example: {"en":"...","th":"..."}; missing/blank entries fall back to region_name.
-- Keep region_name as the canonical name. Manage translations in /operation/regions.
