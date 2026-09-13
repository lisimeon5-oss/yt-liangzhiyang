package com.zbkj.service.dao.groupby;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbkj.common.model.groupbuy.GroupBuyActivity;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 拼团活动表 Mapper 接口
 * </p>
 *
 * @author dazongzi
 * @since 2024-08-13
 */
public interface GroupBuyActivityDao extends BaseMapper<GroupBuyActivity> {

    /**
     * 新开团时锁定活动行，使同一活动的容量校验与团记录写入串行执行。
     */
    GroupBuyActivity selectByIdForUpdate(@Param("id") Integer id);
}
