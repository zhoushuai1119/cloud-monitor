package com.cloud.rocketmq.consumer.monitor.admin.dto;

import lombok.Data;

/**
 * 推送接收者DTO
 *
 * @Author Wang Lin(王霖)
 * @Date 2017/11/1
 * @Time 10:47
 */
@Data
public class PushReceiverDTO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 员工工号
     */
    private String employeeNumber;

    /**
     * 钉钉群机器人push token
     */
    private String accessToken;
}
