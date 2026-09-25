# 砍价记录与逐次明细

参考单商户 `admin/src/views/marketing/bargain/bargainList/index.vue` 及
`StoreBargainUserResponse`、`StoreBargainUserHelpResponse`，补齐多商户平台端和商户端。

## 管理页面

- 记录显示商品图、商品名称、规格、活动、发起用户、起始价、底价和当前价。
- 累计已砍 = 起始价 - 当前价；还需砍价 = 当前价 - 底价，使用 BigDecimal 计算。
- 已砍次数含发起时自动首刀；显示总次数、剩余次数。
- 发起次数按同一用户、同一活动及记录 ID 排序计算，表示本条是第几次发起，不是全站记录 ID。
- 明细按助力记录 ID 升序稳定排序，分页后序号连续，区分发起人自动首刀和好友助力。
- 明细包含用户昵称、头像、UID、砍前价格、本刀金额、砍后价格、时间。
- 保留关键字、活动 ID、用户 ID、状态、订单号、发起时间区间筛选；平台端另可按商户名称筛选。
- 无订单号显示破折号，不再显示 `bargain.undefined`。

## 接口与权限

`GET /api/admin/{merchant|platform}/bargain/records/{id}/helps?page=1&limit=20`

沿用相应 `bargain:list` 权限。商户 ID 从登录身份取，必须与发起记录所属商户一致；不能通过传参查看其他商户的助力用户。
只读事务读取历史记录，不要求活动仍然上架；单页最多 50 条。
查询基于现有 `eb_bargain_help`，无需修改表结构。

## 验证与部署

- `node --test mer_mer_admin/tests/bargain-records.test.cjs`：列表查询、四语文案、弹窗切换、翻页、失败及关闭后的请求竞态。
- `BargainServiceTest`：商户隔离、平台读取、首刀身份、跨页序号和金额精度，以及既有下单/砍价约束。
- `mer_mer_admin/tests/bargain-browser.cjs` 是真实组件的本地合成数据预览，无线上请求。

需要同步发布 Java `crmeb-admin` 服务、商户后台与平台后台前端。前端发布目录分别为两个项目的 `dist-bargain-records`。
H5 商城无需因本次改动重发。后台包基于当前工作区构建，包含此前已有的后台改动，平台包也包含菜单切换修复。
此次没有对线上商品、订单、砍价金额或活动配置做写入。
