package com.zbkj.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 手机密码登录请求对象
 *  +----------------------------------------------------------------------
 *  | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 *  +----------------------------------------------------------------------
 *  | Author: CRMEB Team <admin@crmeb.com>
 *  +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="LoginTelegramRequest", description="小飞机自动登录请求对象")
public class LoginTelegramRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "会员账号", required = true, example = "sdjfaljcadsfas")
    @NotBlank(message = "会员账号不能为空")
    @JsonProperty(value = "account")
    private String account;

    @ApiModelProperty(value = "username", required = true)
   // @NotBlank(message = "username不能为空")
    @JsonProperty(value = "username")
    private String username;

    @ApiModelProperty(value = "昵称", required = true)
    //@NotBlank(message = "昵称不能为空")
    @JsonProperty(value = "nickname")
    private String nickname;

    @ApiModelProperty(value = "推广人id")
    private Integer spreadPid = 0;
}
