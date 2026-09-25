package com.zbkj.service.service;

import com.alibaba.fastjson.JSONObject;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.bargain.*;
import com.zbkj.common.model.product.Product;
import com.zbkj.common.model.product.ProductAttrValue;
import com.zbkj.common.request.BargainSaveRequest;
import com.zbkj.common.request.PreOrderDetailRequest;
import com.zbkj.common.utils.BargainCalculator;
import com.zbkj.common.utils.I18nJsonUtil;
import com.zbkj.common.utils.RequestUtil;
import com.zbkj.common.vo.PreMerchantOrderVo;
import com.zbkj.common.vo.PreOrderInfoDetailVo;
import com.zbkj.common.vo.PreOrderInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

/** Activity-row locks serialize starts, cuts and reservations across application instances. */
@Service
public class BargainService {
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ProductService products;
    @Autowired private ProductAttrValueService skus;
    @Autowired private MerchantService merchants;

    private CrmebException error(String key) { return new CrmebException("bargain." + key); }
    private int count(String sql, Object... args) { return jdbc.queryForObject(sql, args, Integer.class); }
    public BargainActivity activity(Integer id, boolean lock) {
        List<BargainActivity> rows = jdbc.query("SELECT * FROM eb_bargain_activity WHERE id=?" + (lock ? " FOR UPDATE" : ""),
                new BeanPropertyRowMapper<>(BargainActivity.class), id);
        if (rows.isEmpty()) throw error("notFound");
        return rows.get(0);
    }
    private BargainActivity lockRecordActivity(Integer recordId) {
        List<BargainActivity> rows = jdbc.query("SELECT * FROM eb_bargain_activity WHERE id=(SELECT activity_id FROM eb_bargain_record WHERE id=?) FOR UPDATE",
                new BeanPropertyRowMapper<>(BargainActivity.class), recordId);
        if(rows.isEmpty()) throw error("notFound");
        return rows.get(0);
    }
    private BargainRecord record(Integer id) {
        List<BargainRecord> rows = jdbc.query("SELECT * FROM eb_bargain_record WHERE id=? FOR UPDATE",
                new BeanPropertyRowMapper<>(BargainRecord.class), id);
        if (rows.isEmpty()) throw error("notFound");
        return rows.get(0);
    }
    private void owner(BargainActivity a, Integer merId) {
        if (merId != null && !Objects.equals(a.getMerId(), merId)) throw error("notFound");
    }
    private void active(BargainActivity a) {
        Date now = new Date();
        if (!Integer.valueOf(1).equals(a.getAuditStatus()) || !Boolean.TRUE.equals(a.getEnabled())
                || now.before(a.getStartTime()) || !now.before(a.getEndTime())) throw error("inactive");
        Product p = products.getById(a.getProductId());
        ProductAttrValue sku = skus.getById(a.getAttrValueId());
        if (p == null || Boolean.TRUE.equals(p.getIsDel()) || !Boolean.TRUE.equals(p.getIsShow())
                || !Objects.equals(p.getMerId(), a.getMerId()) || sku == null
                || !Objects.equals(sku.getProductId(), p.getId()) || !Boolean.TRUE.equals(sku.getIsShow())
                || !Boolean.TRUE.equals(merchants.getByIdException(a.getMerId()).getIsSwitch())) throw error("inactive");
        if (a.getStock() < 1 || p.getStock() < 1 || sku.getStock() < 1) throw error("soldOut");
    }
    private void json(String value) {
        if (value == null || value.trim().isEmpty()) return;
        try {
            JSONObject object = JSONObject.parseObject(value);
            if (object == null || object.size() > 100) throw new IllegalArgumentException();
            for (Object text : object.values()) if (!(text instanceof String)) throw new IllegalArgumentException();
        } catch (Exception e) { throw error("invalidTranslations"); }
    }
    @Transactional(rollbackFor=Exception.class)
    public Integer save(Integer merId, BargainSaveRequest r) {
        Product p = products.getById(r.getProductId());
        ProductAttrValue sku = skus.getById(r.getAttrValueId());
        if (p == null || !Objects.equals(p.getMerId(), merId) || Boolean.TRUE.equals(p.getIsDel())
                || !Integer.valueOf(0).equals(p.getMarketingType()) || !Integer.valueOf(0).equals(p.getType())
                || sku == null || !Objects.equals(sku.getProductId(), p.getId())
                || !Integer.valueOf(0).equals(sku.getMarketingType())) throw error("invalidProduct");
        if (!r.getEndTime().after(r.getStartTime()) || !r.getEndTime().after(new Date())
                || r.getPrice().compareTo(sku.getPrice()) > 0
                || r.getPrice().subtract(r.getMinPrice()).compareTo(BigDecimal.valueOf(r.getPeopleNum(), 2)) < 0
                || r.getStock() > sku.getStock()) throw error("invalidRules");
        json(r.getNameJson()); json(r.getDescriptionJson());
        Integer id = r.getId();
        if (id != null) {
            BargainActivity old = activity(id, true); owner(old, merId);
            if (count("SELECT COUNT(*) FROM eb_bargain_record WHERE activity_id=?", id) > 0) throw error("alreadyStarted");
        } else {
            jdbc.update("INSERT INTO eb_bargain_activity (mer_id,product_id,attr_value_id,name,price,min_price,people_num,start_limit,help_limit,stock,start_time,end_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    merId,r.getProductId(),r.getAttrValueId(),r.getName(),r.getPrice(),r.getMinPrice(),r.getPeopleNum(),r.getStartLimit(),r.getHelpLimit(),r.getStock(),r.getStartTime(),r.getEndTime());
            id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        }
        jdbc.update("UPDATE eb_bargain_activity SET product_id=?,attr_value_id=?,name=?,name_json=?,description=?,description_json=?,image=?,price=?,min_price=?,people_num=?,start_limit=?,help_limit=?,stock=?,start_time=?,end_time=?,sort=?,audit_status=0,enabled=0,audit_remark=NULL WHERE id=?",
                r.getProductId(),r.getAttrValueId(),r.getName(),r.getNameJson(),r.getDescription(),r.getDescriptionJson(),p.getImage(),r.getPrice(),r.getMinPrice(),r.getPeopleNum(),r.getStartLimit(),r.getHelpLimit(),r.getStock(),r.getStartTime(),r.getEndTime(),r.getSort(),id);
        return id;
    }
    @Transactional(rollbackFor=Exception.class)
    public void audit(Integer id, boolean approved, String remark) {
        activity(id,true);
        if (remark != null && remark.length() > 500) throw error("invalidRules");
        jdbc.update("UPDATE eb_bargain_activity SET audit_status=?,audit_remark=?,enabled=? WHERE id=?", approved?1:2,remark,approved?1:0,id);
    }
    @Transactional(rollbackFor=Exception.class)
    public void enabled(Integer id, Integer merId, boolean enabled) {
        BargainActivity a=activity(id,true); owner(a,merId);
        if (enabled && a.getAuditStatus()!=1) throw error("inactive");
        jdbc.update("UPDATE eb_bargain_activity SET enabled=? WHERE id=?",enabled?1:0,id);
    }
    public JSONObject list(Integer merId, int page, int limit, boolean front) {
        page=Math.max(1,page); limit=Math.min(50,Math.max(1,limit));
        String where=" WHERE 1=1";
        List<Object> args=new ArrayList<>();
        if(merId!=null){where+=" AND a.mer_id=?";args.add(merId);}
        if(front) where+=" AND a.audit_status=1 AND a.enabled=1 AND a.start_time<=NOW() AND a.end_time>NOW() AND EXISTS (SELECT 1 FROM eb_product p WHERE p.id=a.product_id AND p.is_del=0 AND p.is_show=1) AND EXISTS (SELECT 1 FROM eb_merchant m WHERE m.id=a.mer_id AND m.is_switch=1)";
        int total=count("SELECT COUNT(*) FROM eb_bargain_activity a"+where,args.toArray());
        args.add(limit);args.add((page-1)*limit);
        List<BargainActivity> rows=jdbc.query("SELECT a.*,m.name AS mer_name FROM eb_bargain_activity a LEFT JOIN eb_merchant m ON m.id=a.mer_id"+where+" ORDER BY a.sort DESC,a.id DESC LIMIT ? OFFSET ?",args.toArray(),new BeanPropertyRowMapper<>(BargainActivity.class));
        if(front) rows.forEach(this::localize);
        JSONObject out=new JSONObject();out.put("list",rows);out.put("total",total);return out;
    }
    private void localize(BargainActivity a){
        a.setName(I18nJsonUtil.resolveLocalized(a.getName(),a.getNameJson(),RequestUtil.getLang()));
        a.setDescription(I18nJsonUtil.resolveLocalized(a.getDescription(),a.getDescriptionJson(),RequestUtil.getLang()));
    }
    public BargainActivity detail(Integer id,Integer merId,boolean front){
        BargainActivity a=activity(id,false);owner(a,merId);
        if(front){if(a.getAuditStatus()!=1)throw error("notFound");localize(a);}return a;
    }
    @Transactional(rollbackFor=Exception.class)
    public BargainRecord start(Integer activityId,Integer uid){
        BargainActivity a=activity(activityId,true);active(a);
        List<BargainRecord> existing=jdbc.query("SELECT * FROM eb_bargain_record WHERE activity_id=? AND uid=? AND status IN (1,2) ORDER BY id DESC LIMIT 1",new BeanPropertyRowMapper<>(BargainRecord.class),activityId,uid);
        if(!existing.isEmpty())return existing.get(0);
        if(count("SELECT COUNT(*) FROM eb_bargain_record WHERE activity_id=? AND uid=?",activityId,uid)>=a.getStartLimit())throw error("startLimit");
        BigDecimal cut=BargainCalculator.cut(a.getPrice(),a.getMinPrice(),a.getPeopleNum());
        jdbc.update("INSERT INTO eb_bargain_record(activity_id,mer_id,uid,current_price,help_count,status) VALUES(?,?,?,?,1,1)",activityId,a.getMerId(),uid,a.getPrice().subtract(cut));
        Integer id=jdbc.queryForObject("SELECT LAST_INSERT_ID()",Integer.class);
        jdbc.update("INSERT INTO eb_bargain_help(activity_id,record_id,uid,cut_price) VALUES(?,?,?,?)",activityId,id,uid,cut);
        return record(id);
    }
    @Transactional(rollbackFor=Exception.class)
    public JSONObject help(Integer id,Integer uid){
        BargainActivity a=lockRecordActivity(id);active(a);
        BargainRecord r=record(id);
        if(Objects.equals(r.getUid(),uid))throw error("selfHelp");
        if(r.getStatus()!=1)throw error("completed");
        if(count("SELECT COUNT(*) FROM eb_bargain_help WHERE record_id=? AND uid=?",id,uid)>0)throw error("alreadyHelped");
        // The automatic first cut does not consume the quota for helping friends.
        if(count("SELECT COUNT(*) FROM eb_bargain_help h JOIN eb_bargain_record r ON r.id=h.record_id WHERE h.activity_id=? AND h.uid=? AND r.uid<>?",a.getId(),uid,uid)>=a.getHelpLimit())throw error("helpLimit");
        BigDecimal cut=BargainCalculator.cut(r.getCurrentPrice(),a.getMinPrice(),a.getPeopleNum()-r.getHelpCount());
        BigDecimal next=r.getCurrentPrice().subtract(cut);
        jdbc.update("UPDATE eb_bargain_record SET current_price=?,help_count=help_count+1,status=? WHERE id=?",next,next.compareTo(a.getMinPrice())==0?2:1,id);
        jdbc.update("INSERT INTO eb_bargain_help(activity_id,record_id,uid,cut_price) VALUES(?,?,?,?)",a.getId(),id,uid,cut);
        JSONObject out=new JSONObject();out.put("cutPrice",cut);return out;
    }
    public JSONObject progress(Integer id,Integer uid){
        BargainRecord r=record(id);BargainActivity a=detail(r.getActivityId(),null,true);
        JSONObject out=new JSONObject();out.put("activity",a);
        // Sharing exposes progress, not the initiator's user ID or order number.
        JSONObject progress=new JSONObject();progress.put("id",r.getId());progress.put("currentPrice",r.getCurrentPrice());
        progress.put("helpCount",r.getHelpCount());progress.put("status",r.getStatus());progress.put("mine",Objects.equals(r.getUid(),uid));
        if(Objects.equals(r.getUid(),uid)) {
            String orderNo=r.getOrderNo();
            if(orderNo!=null) {
                List<String> children=jdbc.queryForList("SELECT order_no FROM eb_order WHERE plat_order_no=? AND uid=? AND mer_id=? ORDER BY id LIMIT 1",String.class,orderNo,uid,r.getMerId());
                if(!children.isEmpty())orderNo=children.get(0);
            }
            progress.put("orderNo",orderNo);
        }
        out.put("record",progress);
        out.put("helpList",jdbc.queryForList("SELECT cut_price AS cutPrice,create_time AS createTime FROM eb_bargain_help WHERE record_id=? ORDER BY id DESC LIMIT 1000",id));
        return out;
    }
    public JSONObject adminRecords(Integer merId, com.zbkj.common.request.BargainRecordSearchRequest q, int page, int limit) {
        page=Math.max(1,page); limit=Math.min(50,Math.max(1,limit));
        if(q.getStartTime()!=null && q.getEndTime()!=null && q.getStartTime().after(q.getEndTime())) throw error("invalidRules");
        String joins=" FROM eb_bargain_record r LEFT JOIN eb_bargain_activity a ON a.id=r.activity_id LEFT JOIN eb_merchant m ON m.id=r.mer_id LEFT JOIN eb_product p ON p.id=a.product_id LEFT JOIN eb_product_attr_value s ON s.id=a.attr_value_id LEFT JOIN eb_user u ON u.id=r.uid";
        StringBuilder where=new StringBuilder(" WHERE 1=1"); List<Object> args=new ArrayList<>();
        if(merId!=null){where.append(" AND r.mer_id=?");args.add(merId);}
        if(q.getUid()!=null){where.append(" AND r.uid=?");args.add(q.getUid());}
        if(q.getActivityId()!=null){where.append(" AND r.activity_id=?");args.add(q.getActivityId());}
        if(q.getKeyword()!=null && !q.getKeyword().trim().isEmpty()) {
            where.append(" AND (a.name LIKE ? OR p.name LIKE ? OR a.name_json LIKE ? OR p.name_json LIKE ? OR u.nickname LIKE ?)");
            for(int i=0;i<5;i++)args.add("%"+q.getKeyword().trim()+"%");
        }
        if(q.getMerchantName()!=null && !q.getMerchantName().trim().isEmpty()){where.append(" AND m.name LIKE ?");args.add("%"+q.getMerchantName().trim()+"%");}
        if(q.getOrderNo()!=null && !q.getOrderNo().trim().isEmpty()){where.append(" AND r.order_no=?");args.add(q.getOrderNo().trim());}
        if(q.getStartTime()!=null){where.append(" AND r.create_time>=?");args.add(q.getStartTime());}
        if(q.getEndTime()!=null){where.append(" AND r.create_time<=?");args.add(q.getEndTime());}
        if(q.getStatus()!=null){
            where.append(" AND (CASE WHEN r.status<3 AND a.end_time<=? THEN 4 ELSE r.status END)=?");
            args.add(new Date());args.add(q.getStatus());
        }
        int total=count("SELECT COUNT(*)"+joins+where,args.toArray());
        args.add(limit);args.add((page-1)*limit);
        String columns="SELECT r.*,a.end_time,m.name AS mer_name,a.name AS activity_name,a.product_id,p.name AS product_name,a.image,s.sku,u.nickname,a.price,a.min_price,a.people_num,(SELECT COUNT(*) FROM eb_bargain_record previous WHERE previous.activity_id=r.activity_id AND previous.uid=r.uid AND previous.id<=r.id) AS attempt_no";
        JSONObject out=new JSONObject();out.put("total",total);
        out.put("list",jdbc.query(columns+joins+where+" ORDER BY r.id DESC LIMIT ? OFFSET ?",args.toArray(),new BeanPropertyRowMapper<>(com.zbkj.common.response.BargainRecordAdminResponse.class)));
        return out;
    }
    /** Historical help ledger: scoped before returning any helper identity. */
    @Transactional(readOnly=true)
    public JSONObject adminHelps(Integer recordId,Integer merId,int page,int limit) {
        page=Math.max(1,page);limit=Math.min(50,Math.max(1,limit));
        List<BargainRecord> records=jdbc.query("SELECT * FROM eb_bargain_record WHERE id=?",new BeanPropertyRowMapper<>(BargainRecord.class),recordId);
        if(records.isEmpty() || (merId!=null && !Objects.equals(merId,records.get(0).getMerId())))throw error("notFound");
        BargainRecord record=records.get(0);
        BargainActivity activity=activity(record.getActivityId(),false);
        int total=count("SELECT COUNT(*) FROM eb_bargain_help WHERE record_id=?",recordId);
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT h.id,h.uid,u.nickname,u.avatar,h.cut_price AS cutPrice,DATE_FORMAT(h.create_time,'%Y-%m-%d %H:%i:%s') AS createTime,"+
                "(SELECT COALESCE(SUM(prior.cut_price),0) FROM eb_bargain_help prior WHERE prior.record_id=h.record_id AND prior.id<h.id) AS previousCut "+
                "FROM eb_bargain_help h LEFT JOIN eb_user u ON u.id=h.uid WHERE h.record_id=? ORDER BY h.id ASC LIMIT ? OFFSET ?",recordId,limit,(page-1)*limit);
        int sequence=(page-1)*limit;
        for(Map<String,Object> row:rows) {
            BigDecimal before=activity.getPrice().subtract(new BigDecimal(row.remove("previousCut").toString()));
            row.put("sequence",++sequence);
            row.put("selfCut",Objects.equals(record.getUid(),((Number)row.get("uid")).intValue()));
            row.put("beforePrice",before);
            row.put("afterPrice",before.subtract(new BigDecimal(row.get("cutPrice").toString())));
        }
        JSONObject out=new JSONObject();out.put("list",rows);out.put("total",total);out.put("record",record);
        out.put("price",activity.getPrice());out.put("minPrice",activity.getMinPrice());return out;
    }
    public JSONObject records(Integer uid,Integer merId,int page,int limit){
        page=Math.max(1,page);limit=Math.min(50,Math.max(1,limit));
        String where=" WHERE 1=1";List<Object> args=new ArrayList<>();
        if(uid!=null){where+=" AND r.uid=?";args.add(uid);}if(merId!=null){where+=" AND r.mer_id=?";args.add(merId);}
        int total=count("SELECT COUNT(*) FROM eb_bargain_record r"+where,args.toArray());args.add(limit);args.add((page-1)*limit);
        JSONObject out=new JSONObject();out.put("total",total);out.put("list",jdbc.query("SELECT r.*,a.end_time,m.name AS mer_name FROM eb_bargain_record r JOIN eb_bargain_activity a ON a.id=r.activity_id LEFT JOIN eb_merchant m ON m.id=r.mer_id"+where+" ORDER BY r.id DESC LIMIT ? OFFSET ?",args.toArray(),new BeanPropertyRowMapper<>(BargainRecord.class)));return out;
    }
    public BargainActivity validateOrder(Integer recordId,Integer uid,PreOrderDetailRequest d){
        if(recordId==null)throw error("notFound");BargainRecord r=record(recordId);BargainActivity a=activity(r.getActivityId(),false);active(a);
        if(!Objects.equals(r.getUid(),uid)||r.getStatus()!=2||r.getCurrentPrice().compareTo(a.getMinPrice())!=0||r.getOrderNo()!=null)throw error("notReady");
        if(!Objects.equals(d.getProductId(),a.getProductId())||!Objects.equals(d.getAttrValueId(),a.getAttrValueId())||!Integer.valueOf(1).equals(d.getProductNum()))throw error("invalidProduct");
        return a;
    }
    /** Called inside the existing order transaction: rollback also restores the reservation. */
    @Transactional(rollbackFor=Exception.class)
    public void reserve(PreOrderInfoVo vo,Integer uid,String orderNo){
        if(vo.getMerchantOrderVoList().size()!=1)throw error("invalidProduct");
        PreMerchantOrderVo m=vo.getMerchantOrderVoList().get(0);
        if(m.getOrderInfoList().size()!=1)throw error("invalidProduct");
        PreOrderInfoDetailVo d=m.getOrderInfoList().get(0);
        lockRecordActivity(d.getBargainRecordId());
        BargainRecord r=record(d.getBargainRecordId());
        PreOrderDetailRequest request=new PreOrderDetailRequest().setProductId(d.getProductId()).setAttrValueId(d.getAttrValueId()).setProductNum(d.getPayNum());
        BargainActivity a=validateOrder(r.getId(),uid,request);
        if(!Objects.equals(a.getMerId(),m.getMerId())||d.getPrice().compareTo(a.getMinPrice())!=0)throw error("invalidProduct");
        if(jdbc.update("UPDATE eb_bargain_activity SET stock=stock-1 WHERE id=? AND stock>0",a.getId())!=1)throw error("soldOut");
        if(jdbc.update("UPDATE eb_bargain_record SET status=3,order_no=? WHERE id=? AND status=2 AND order_no IS NULL",orderNo,r.getId())!=1)throw error("notReady");
    }
    @Transactional(rollbackFor=Exception.class)
    public boolean cancel(String orderNo){
        List<BargainRecord> rows=jdbc.query("SELECT * FROM eb_bargain_record WHERE order_no=?",new BeanPropertyRowMapper<>(BargainRecord.class),orderNo);
        if(rows.isEmpty())return false;BargainRecord r=rows.get(0);activity(r.getActivityId(),true);
        if(jdbc.update("UPDATE eb_bargain_record SET status=2,order_no=NULL WHERE id=? AND order_no=? AND status=3",r.getId(),orderNo)==1) {
            jdbc.update("UPDATE eb_bargain_activity SET stock=stock+1 WHERE id=?",r.getActivityId());
            return true;
        }
        return false;
    }
}
