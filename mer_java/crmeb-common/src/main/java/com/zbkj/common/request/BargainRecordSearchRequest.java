package com.zbkj.common.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import javax.validation.constraints.*;
import java.util.Date;

@Data
public class BargainRecordSearchRequest {
    @Size(max=100) private String keyword;
    @Size(max=100) private String merchantName;
    @Size(max=64) private String orderNo;
    @Min(1) private Integer uid;
    @Min(1) private Integer activityId;
    @Min(1) @Max(4) private Integer status;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") private Date startTime;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") private Date endTime;
}
