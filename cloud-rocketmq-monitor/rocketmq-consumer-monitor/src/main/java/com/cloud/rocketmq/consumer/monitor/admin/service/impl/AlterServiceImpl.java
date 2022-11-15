package com.cloud.rocketmq.consumer.monitor.admin.service.impl;

import cn.hutool.json.JSONUtil;
import com.cloud.dingtalk.dinger.DingerSender;
import com.cloud.dingtalk.dinger.core.entity.DingerRequest;
import com.cloud.rocketmq.consumer.monitor.admin.dto.PushAlterDTO;
import com.cloud.rocketmq.consumer.monitor.admin.service.AlterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author 马腾飞
 * @Date 2020/2/26
 * @Time 19:03
 * @Description
 */
@Service
@Slf4j
public class AlterServiceImpl implements AlterService {

    @Autowired
    private DingerSender dingerSender;


    @Override
    public void alterPost(PushAlterDTO pushAlterDTO) {
        dingerSender.send(DingerRequest.request(JSONUtil.toJsonStr(pushAlterDTO)));
    }

}
