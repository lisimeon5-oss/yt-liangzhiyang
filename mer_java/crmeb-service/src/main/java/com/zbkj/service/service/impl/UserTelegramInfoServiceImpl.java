package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbkj.common.model.user.UserTelegramInfo;
import com.zbkj.service.dao.UserTelegramInfoDao;
import com.zbkj.service.service.UserTelegramInfoService;
import org.springframework.stereotype.Service;

@Service
public class UserTelegramInfoServiceImpl extends ServiceImpl<UserTelegramInfoDao, UserTelegramInfo> implements UserTelegramInfoService {
}
