package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.zbkj.common.model.seckill.SeckillProduct;
import com.zbkj.service.dao.SeckillProductDao;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SeckillHomeProductsTest {
    private SeckillProductServiceImpl service;
    private final List<SeckillProduct> rows = new ArrayList<>();
    private LambdaQueryWrapper<SeckillProduct> query;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"),
                SeckillProduct.class);
        service = new SeckillProductServiceImpl();
        SeckillProductDao dao = (SeckillProductDao) Proxy.newProxyInstance(
                SeckillProductDao.class.getClassLoader(), new Class<?>[]{SeckillProductDao.class},
                (proxy, method, args) -> {
                    if ("toString".equals(method.getName())) return "SeckillProductDao test double";
                    if (!"selectList".equals(method.getName())) throw new AssertionError(method.getName());
                    query = (LambdaQueryWrapper<SeckillProduct>) args[0];
                    return rows;
                });
        ReflectionTestUtils.setField(service, "dao", dao);
    }

    @Test
    public void returnsAllFortyProductsWithoutSqlLimit() {
        for (int id = 1; id <= 40; id++) rows.add(new SeckillProduct().setId(id).setProductId(id));
        List<SeckillProduct> result = service.getIndexList(Collections.singletonList(64));
        assertEquals(40, result.size());
        assertEquals(Integer.valueOf(40), result.get(39).getProductId());
        String sql = query.getSqlSegment().toLowerCase();
        assertFalse(sql.contains("limit"));
        assertTrue(sql.contains("activity_id in"));
        assertTrue(sql.contains("is_show ="));
        assertTrue(sql.contains("is_del ="));
        assertTrue(sql.contains("audit_status ="));
        assertTrue(sql.contains("order by sort desc,seckill_price asc,id desc"));
    }

    @Test
    public void keepsFirstRankedRecordWhenActivitiesShareAProduct() {
        rows.add(new SeckillProduct().setId(101).setProductId(7));
        rows.add(new SeckillProduct().setId(102).setProductId(8));
        rows.add(new SeckillProduct().setId(103).setProductId(7));
        List<SeckillProduct> result = service.getIndexList(Arrays.asList(64, 65));
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(101), result.get(0).getId());
        assertEquals(Integer.valueOf(102), result.get(1).getId());
    }

    @Test
    public void noActiveActivitiesDoesNotQueryAllProducts() {
        assertTrue(service.getIndexList(Collections.emptyList()).isEmpty());
        assertTrue(service.getIndexList(null).isEmpty());
        assertNull(query);
    }
}
