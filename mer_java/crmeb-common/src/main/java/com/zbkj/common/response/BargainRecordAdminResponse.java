package com.zbkj.common.response;

import com.zbkj.common.model.bargain.BargainRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper=true)
public class BargainRecordAdminResponse extends BargainRecord {
    private String activityName;
    private Integer productId;
    private String productName;
    private String image;
    private String sku;
    private String nickname;
    private BigDecimal price;
    private BigDecimal minPrice;
    private Integer peopleNum;
    /** Number of times this user has started this activity, including this record. */
    private Integer attemptNo;
    public BigDecimal getCutTotal() { return price == null || getCurrentPrice() == null ? null : price.subtract(getCurrentPrice()).max(BigDecimal.ZERO); }
    public BigDecimal getRemainingCut() { return minPrice == null || getCurrentPrice() == null ? null : getCurrentPrice().subtract(minPrice).max(BigDecimal.ZERO); }
}
