// Use API epoch timestamps so activity times do not depend on browser timezone.
export function bargainActivityStatus(activity, now = Date.now()) {
  if (Number(activity.auditStatus) !== 1) return 'unavailable';
  if (!activity.enabled) return 'offShelf';
  if (activity.endTimeMs != null && now >= Number(activity.endTimeMs)) return 'ended';
  if (activity.startTimeMs != null && now < Number(activity.startTimeMs)) return 'upcoming';
  if (Number(activity.stock) <= 0) return 'soldOutStatus';
  return 'running';
}
