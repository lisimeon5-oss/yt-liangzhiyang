package com.zbkj.common.request;

import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class BargainSaveRequest {
    private Integer id;
    @NotNull @Min(1) private Integer productId;
    @NotNull @Min(1) private Integer attrValueId;
    @NotBlank @Size(max=255) private String name;
    private String nameJson;
    @Size(max=5000) private String description;
    private String descriptionJson;
    @NotNull @DecimalMin("0.01") @Digits(integer=8,fraction=2) private BigDecimal price;
    @NotNull @DecimalMin("0.01") @Digits(integer=8,fraction=2) private BigDecimal minPrice;
    @NotNull @Min(2) @Max(1000) private Integer peopleNum;
    @NotNull @Min(1) @Max(1000) private Integer startLimit;
    @NotNull @Min(1) @Max(1000) private Integer helpLimit;
    @NotNull @Min(1) private Integer stock;
    @NotNull private Date startTime;
    @NotNull private Date endTime;
    private Integer sort = 0;
}
