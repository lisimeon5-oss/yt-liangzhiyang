package com.zbkj.service.service;

import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.bargain.*;
import com.zbkj.common.model.product.*;
import com.zbkj.common.model.merchant.Merchant;
import com.zbkj.common.request.PreOrderDetailRequest;
import com.zbkj.common.vo.PreOrderInfoVo;
import com.zbkj.common.vo.PreMerchantOrderVo;
import com.zbkj.common.vo.PreOrderInfoDetailVo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class BargainServiceTest {
    @Test public void helpLedgerCannotBeReadByAnotherMerchant() {
        fails("notFound",()->service.adminHelps(2,99,1,20));
        verify(jdbc,never()).queryForList(anyString(),anyInt(),anyInt(),anyInt());
    }
    @Test public void helpLedgerShowsStableSequenceAndExactMoneyAcrossPages() {
        Map<String,Object> cut=new HashMap<>();cut.put("uid",41);cut.put("cutPrice",new BigDecimal("0.37"));cut.put("previousCut",new BigDecimal("8.63"));
        when(jdbc.queryForList(contains("FROM eb_bargain_help h"),eq(2),eq(2),eq(2))).thenReturn(Collections.singletonList(cut));
        com.alibaba.fastjson.JSONObject result=service.adminHelps(2,10,2,2);
        assertEquals(3,cut.get("sequence"));assertEquals(false,cut.get("selfCut"));
        assertEquals(new BigDecimal("1.37"),cut.get("beforePrice"));assertEquals(new BigDecimal("1.00"),cut.get("afterPrice"));
        assertFalse(cut.containsKey("previousCut"));assertNotNull(result.get("record"));
    }
    @Test public void helpLedgerLabelsAutomaticFirstCutAndAllowsPlatformRead() {
        Map<String,Object> cut=new HashMap<>();cut.put("uid",40);cut.put("cutPrice",new BigDecimal("2.00"));cut.put("previousCut",BigDecimal.ZERO);
        when(jdbc.queryForList(contains("FROM eb_bargain_help h"),eq(2),eq(50),eq(0))).thenReturn(Collections.singletonList(cut));
        service.adminHelps(2,null,0,500);
        assertEquals(1,cut.get("sequence"));assertEquals(true,cut.get("selfCut"));
        assertEquals(new BigDecimal("8.00"),cut.get("afterPrice"));
    }
    @Test public void adminRecordMoneyUsesExactDecimals() {
        com.zbkj.common.response.BargainRecordAdminResponse r=new com.zbkj.common.response.BargainRecordAdminResponse();
        r.setPrice(new BigDecimal("200.00"));r.setMinPrice(new BigDecimal("100.00"));r.setCurrentPrice(new BigDecimal("141.46"));
        assertEquals(new BigDecimal("58.54"),r.getCutTotal());assertEquals(new BigDecimal("41.46"),r.getRemainingCut());
    }
    @Test public void adminRecordSearchScopesMerchantAndBindsFilters() {
        com.zbkj.common.request.BargainRecordSearchRequest q=new com.zbkj.common.request.BargainRecordSearchRequest();
        q.setKeyword("apple' OR 1=1 --");q.setUid(40);q.setActivityId(1);q.setStatus(4);q.setOrderNo("ORDER1");
        q.setStartTime(new Date(1000));q.setEndTime(new Date(2000));
        service.adminRecords(10,q,2,20);
        org.mockito.ArgumentCaptor<String> sql=org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<Object[]> params=org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).query(sql.capture(),params.capture(),any(RowMapper.class));
        assertTrue(sql.getValue().contains("r.mer_id=?"));
        assertTrue(sql.getValue().contains("AS product_name"));
        assertTrue(sql.getValue().contains("CASE WHEN r.status<3"));
        assertFalse(sql.getValue().contains(q.getKeyword()));
        assertEquals(10,params.getValue()[0]);
        assertTrue(Arrays.asList(params.getValue()).contains("%"+q.getKeyword()+"%"));
        assertEquals(20,params.getValue()[params.getValue().length-1]);
    }
    @Test public void adminRecordSearchRejectsReversedDates() {
        com.zbkj.common.request.BargainRecordSearchRequest q=new com.zbkj.common.request.BargainRecordSearchRequest();
        q.setStartTime(new Date(2000));q.setEndTime(new Date(1000));
        fails("invalidRules",()->service.adminRecords(10,q,1,20));
    }
    private BargainService service;
    private JdbcTemplate jdbc;
    private BargainActivity activity;
    private BargainRecord record;
    @Before @SuppressWarnings("unchecked") public void setup() {
        service=new BargainService();jdbc=mock(JdbcTemplate.class);
        ProductService products=mock(ProductService.class);ProductAttrValueService skus=mock(ProductAttrValueService.class);MerchantService merchants=mock(MerchantService.class);
        ReflectionTestUtils.setField(service,"jdbc",jdbc);ReflectionTestUtils.setField(service,"products",products);ReflectionTestUtils.setField(service,"skus",skus);ReflectionTestUtils.setField(service,"merchants",merchants);
        activity=new BargainActivity();activity.setId(1);activity.setMerId(10);activity.setProductId(20);activity.setAttrValueId(30);activity.setAuditStatus(1);activity.setEnabled(true);activity.setStock(5);activity.setPrice(new BigDecimal("10.00"));activity.setMinPrice(new BigDecimal("1.00"));activity.setPeopleNum(3);activity.setHelpLimit(3);activity.setStartTime(new Date(System.currentTimeMillis()-60000));activity.setEndTime(new Date(System.currentTimeMillis()+60000));
        record=new BargainRecord();record.setId(2);record.setActivityId(1);record.setMerId(10);record.setUid(40);record.setStatus(2);record.setHelpCount(3);record.setCurrentPrice(new BigDecimal("1.00"));
        when(jdbc.query(anyString(),any(RowMapper.class),anyInt())).thenAnswer(i -> Collections.singletonList(i.<String>getArgument(0).contains("eb_bargain_activity")?activity:record));
        Product p=new Product();p.setId(20);p.setMerId(10);p.setIsDel(false);p.setIsShow(true);p.setStock(10);when(products.getById(20)).thenReturn(p);
        ProductAttrValue sku=new ProductAttrValue();sku.setProductId(20);sku.setIsShow(true);sku.setStock(10);when(skus.getById(30)).thenReturn(sku);
        Merchant m=new Merchant();m.setIsSwitch(true);when(merchants.getByIdException(10)).thenReturn(m);
        when(jdbc.queryForObject(anyString(),any(Object[].class),eq(Integer.class))).thenReturn(0);
    }
    private PreOrderDetailRequest request(){return new PreOrderDetailRequest().setProductId(20).setAttrValueId(30).setProductNum(1);}
    private void fails(String key,Runnable fn){try{fn.run();fail("Expected rejection");}catch(CrmebException e){assertEquals("bargain."+key,e.getMessage());}}
    @Test public void anotherMerchantCannotReadOrEnableActivity(){fails("notFound",()->service.detail(1,99,false));fails("notFound",()->service.enabled(1,99,true));}
    @Test public void anotherUserCannotSpendSuccessfulBargain(){fails("notReady",()->service.validateOrder(2,99,request()));}
    @Test public void substitutedSkuAndMultipleQuantityAreRejected(){fails("invalidProduct",()->service.validateOrder(2,40,request().setAttrValueId(31)));fails("invalidProduct",()->service.validateOrder(2,40,request().setProductNum(2)));}
    @Test public void unfinishedAndAlreadyOrderedRecordsCannotBuy(){record.setStatus(1);fails("notReady",()->service.validateOrder(2,40,request()));record.setStatus(2);record.setOrderNo("PT-other");fails("notReady",()->service.validateOrder(2,40,request()));}
    @Test public void expiredOrDisabledActivitiesCannotBuy(){activity.setEndTime(new Date(0));fails("inactive",()->service.validateOrder(2,40,request()));activity.setEndTime(new Date(System.currentTimeMillis()+60000));activity.setEnabled(false);fails("inactive",()->service.validateOrder(2,40,request()));}
    @Test public void ownerCannotTakeAnExtraSelfCut(){record.setStatus(1);fails("selfHelp",()->service.help(2,40));}
    @Test public void repeatedFriendCutIsRejected(){record.setStatus(1);when(jdbc.queryForObject(contains("record_id=? AND uid=?"),any(Object[].class),eq(Integer.class))).thenReturn(1);fails("alreadyHelped",()->service.help(2,41));}
    @Test public void successfulOwnerCanCheckout(){assertEquals(Integer.valueOf(1),service.validateOrder(2,40,request()).getId());}
    @Test public void finalFriendReceivesExactlyTheRemainder(){
        record.setStatus(1);record.setHelpCount(2);record.setCurrentPrice(new BigDecimal("1.37"));
        assertEquals(new BigDecimal("0.37"),service.help(2,41).getBigDecimal("cutPrice"));
        verify(jdbc).update(contains("current_price=?"),eq(new BigDecimal("1.00")),eq(2),eq(2));
    }
    @Test public void secondOrderCannotSpendAnAlreadyReservedRecord(){
        PreOrderInfoDetailVo d=new PreOrderInfoDetailVo();d.setBargainRecordId(2);d.setProductId(20);d.setAttrValueId(30);d.setPayNum(1);d.setPrice(new BigDecimal("1.00"));
        PreMerchantOrderVo m=new PreMerchantOrderVo();m.setMerId(10);m.setOrderInfoList(Collections.singletonList(d));
        PreOrderInfoVo vo=new PreOrderInfoVo();vo.setMerchantOrderVoList(Collections.singletonList(m));
        when(jdbc.update(contains("stock=stock-1"),eq(1))).thenReturn(1);
        when(jdbc.update(contains("SET status=3"),eq("PT-one"),eq(2))).thenAnswer(i->{record.setStatus(3);record.setOrderNo("PT-one");return 1;});
        service.reserve(vo,40,"PT-one");fails("notReady",()->service.reserve(vo,40,"PT-two"));
        verify(jdbc,times(1)).update(contains("stock=stock-1"),eq(1));
    }
    @Test @SuppressWarnings("unchecked") public void cancellationReleasesTheReservationOnlyOnce(){
        when(jdbc.query(contains("WHERE order_no=?"),any(RowMapper.class),eq("PT-one"))).thenReturn(Collections.singletonList(record));
        when(jdbc.update(contains("SET status=2,order_no=NULL"),eq(2),eq("PT-one"))).thenReturn(1,0);
        assertTrue(service.cancel("PT-one"));assertFalse(service.cancel("PT-one"));
        verify(jdbc,times(1)).update(contains("stock=stock+1"),eq(1));
    }
}
