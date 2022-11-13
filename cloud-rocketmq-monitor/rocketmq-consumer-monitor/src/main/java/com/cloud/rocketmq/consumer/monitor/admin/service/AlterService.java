package com.cloud.rocketmq.consumer.monitor.admin.service;


import com.cloud.rocketmq.consumer.monitor.admin.dto.PushAlterDTO;

/**
 * @Author 马腾飞
 * @Date 2020/2/26
 * @Time 19:03
 * @Description
 */
public interface AlterService {

    void alterPost(PushAlterDTO pushAlterDTO);
}
