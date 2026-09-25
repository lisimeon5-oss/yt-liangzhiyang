-- Place bargain immediately after group buying (sort DESC, id ASC).
-- Run after 20260924_merchant_bargain.sql; safe to run again.
START TRANSACTION;
DROP TEMPORARY TABLE IF EXISTS bargain_menu_position;
CREATE TEMPORARY TABLE bargain_menu_position AS
SELECT b.id AS bargain_id, g.id AS group_id, g.pid, g.type, g.sort AS group_sort
FROM eb_system_menu b
JOIN eb_system_menu g ON g.pid=b.pid AND g.type=b.type
WHERE b.component='/marketing/bargain' AND g.component='/marketing/group'
  AND b.type IN (3,4) AND b.is_delte=0 AND g.is_delte=0
  AND b.sort<>g.sort-1;

-- Raise the group and preceding siblings, leaving room without negative sorts.
UPDATE eb_system_menu m
JOIN bargain_menu_position p ON m.pid=p.pid AND m.type=p.type
SET m.sort=m.sort+2
WHERE m.id<>p.bargain_id AND m.is_delte=0
  AND (m.sort>p.group_sort OR (m.sort=p.group_sort AND m.id<=p.group_id));

UPDATE eb_system_menu m
JOIN bargain_menu_position p ON m.id=p.bargain_id
SET m.sort=p.group_sort+1;
DROP TEMPORARY TABLE bargain_menu_position;
COMMIT;
