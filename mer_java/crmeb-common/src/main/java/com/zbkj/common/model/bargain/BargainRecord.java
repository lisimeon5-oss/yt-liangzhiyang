package com.zbkj.common.model.bargain;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class BargainRecord {
    private Integer id;
    private Integer activityId;
    private Integer merId;
    private String merName;
    private Integer uid;
    private BigDecimal currentPrice;
    private Integer helpCount;
    /** 1 cutting, 2 reached floor, 3 order placed. Expiry is derived from activity end. */
    private Integer status;
    private String orderNo;
    private Date createTime;
    private Date endTime;
    public Integer getDisplayStatus() {
        return status != null && status < 3 && endTime != null && !new Date().before(endTime) ? 4 : status;
    }
}
