-- Run before deploying admin/front. No existing products or orders are rewritten.
CREATE TABLE IF NOT EXISTS eb_bargain_activity (
 id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 mer_id INT NOT NULL, product_id INT NOT NULL, attr_value_id INT NOT NULL,
 name VARCHAR(255) NOT NULL, name_json TEXT, description TEXT, description_json TEXT,
 image VARCHAR(1024), price DECIMAL(10,2) NOT NULL, min_price DECIMAL(10,2) NOT NULL,
 people_num INT NOT NULL, start_limit INT NOT NULL, help_limit INT NOT NULL,
 stock INT NOT NULL, audit_status TINYINT NOT NULL DEFAULT 0, audit_remark VARCHAR(500),
 enabled TINYINT NOT NULL DEFAULT 0, sort INT NOT NULL DEFAULT 0,
 start_time DATETIME NOT NULL, end_time DATETIME NOT NULL,
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 KEY idx_mer (mer_id,id), KEY idx_listing (audit_status,enabled,end_time,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS eb_bargain_record (
 id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 activity_id INT NOT NULL, mer_id INT NOT NULL, uid INT NOT NULL,
 current_price DECIMAL(10,2) NOT NULL, help_count INT NOT NULL DEFAULT 0,
 status TINYINT NOT NULL DEFAULT 1, order_no VARCHAR(64) NULL,
 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 KEY idx_activity_user (activity_id,uid,id), KEY idx_mer (mer_id,id),
 UNIQUE KEY uq_order (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS eb_bargain_help (
 id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 activity_id INT NOT NULL, record_id INT NOT NULL, uid INT NOT NULL,
 cut_price DECIMAL(10,2) NOT NULL, create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uq_record_helper (record_id,uid), KEY idx_activity_helper (activity_id,uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO eb_system_menu(pid,name,icon,perms,component,menu_type,sort,is_show,is_delte,type)
SELECT id,'砍价活动','','','/marketing/bargain','C',10,1,0,4 FROM eb_system_menu
WHERE type=4 AND component='/marketing' AND is_delte=0
AND NOT EXISTS(SELECT 1 FROM (SELECT id FROM eb_system_menu WHERE type=4 AND component='/marketing/bargain') existing_menu) LIMIT 1;
INSERT INTO eb_system_menu(pid,name,icon,perms,component,menu_type,sort,is_show,is_delte,type)
SELECT id,'砍价列表','','merchant:bargain:list','','A',1,1,0,4 FROM eb_system_menu
WHERE type=4 AND component='/marketing/bargain'
AND NOT EXISTS(SELECT 1 FROM (SELECT id FROM eb_system_menu WHERE perms='merchant:bargain:list') existing_perm) LIMIT 1;
INSERT INTO eb_system_menu(pid,name,icon,perms,component,menu_type,sort,is_show,is_delte,type)
SELECT id,'保存砍价活动','','merchant:bargain:save','','A',1,1,0,4 FROM eb_system_menu
WHERE type=4 AND component='/marketing/bargain'
AND NOT EXISTS(SELECT 1 FROM (SELECT id FROM eb_system_menu WHERE perms='merchant:bargain:save') existing_perm) LIMIT 1;

INSERT INTO eb_system_menu(pid,name,icon,perms,component,menu_type,sort,is_show,is_delte,type)
SELECT id,'砍价活动','','','/marketing/bargain','C',10,1,0,3 FROM eb_system_menu
WHERE type=3 AND component='/marketing' AND is_delte=0
AND NOT EXISTS(SELECT 1 FROM (SELECT id FROM eb_system_menu WHERE type=3 AND component='/marketing/bargain') existing_menu) LIMIT 1;
INSERT INTO eb_system_menu(pid,name,icon,perms,component,menu_type,sort,is_show,is_delte,type)
SELECT id,'砍价列表','','platform:bargain:list','','A',1,1,0,3 FROM eb_system_menu
WHERE type=3 AND component='/marketing/bargain'
AND NOT EXISTS(SELECT 1 FROM (SELECT id FROM eb_system_menu WHERE perms='platform:bargain:list') existing_perm) LIMIT 1;
INSERT INTO eb_system_menu(pid,name,icon,perms,component,menu_type,sort,is_show,is_delte,type)
SELECT id,'审核砍价活动','','platform:bargain:audit','','A',1,1,0,3 FROM eb_system_menu
WHERE type=3 AND component='/marketing/bargain'
AND NOT EXISTS(SELECT 1 FROM (SELECT id FROM eb_system_menu WHERE perms='platform:bargain:audit') existing_perm) LIMIT 1;
