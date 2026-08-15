package com.zbkj.common.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_telegram_info")
@ApiModel(value="Telegram_info对象", description="用户小飞机信息表")
public class UserTelegramInfo implements Serializable {


    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "账户")
    private String account;

    @ApiModelProperty(value = "小飞机唯一ID")
    private Long tgId;

    @ApiModelProperty(value = "小飞机用户名如：@xiaowang")
    private String tgUsername;

    @ApiModelProperty(value = "小飞机昵称")
    private String tgNickname;

}