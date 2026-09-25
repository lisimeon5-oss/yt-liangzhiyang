package com.zbkj.common.model.bargain;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/** Merchant-owned bargain configuration. Translations use extensible language keys. */
@Data
public class BargainActivity {
    private Integer id;
    private Integer merId;
    private String merName;
    private Integer productId;
    private Integer attrValueId;
    private String name;
    private String nameJson;
    private String description;
    private String descriptionJson;
    private String image;
    private BigDecimal price;
    private BigDecimal minPrice;
    private Integer peopleNum;
    private Integer startLimit;
    private Integer helpLimit;
    private Integer stock;
    private Integer auditStatus;
    private String auditRemark;
    private Boolean enabled;
    private Integer sort;
    private Date startTime;
    private Date endTime;
    private Date createTime;
    public Long getEndTimeMs() { return endTime == null ? null : endTime.getTime(); }
    public Long getStartTimeMs() { return startTime == null ? null : startTime.getTime(); }
}
