package com.cloud.rocketmq.consumer.monitor.admin.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 推送请求DTO
 *
 * @Author Wang Lin(王霖)
 * @Date 2017/11/1
 * @Time 10:47
 */
@Data
public class PushRequestDTO {


    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 调用系统三字码
     */
    private String systemCode;

    /**
     * 推送类型
     */
    private Integer pushType;

    /**
     * 关键字
     */
    private Map<String, String> keywords;

    /**
     * 接收人
     */
    private List<PushReceiverDTO> receivers;

    /**
     * 钉钉群机器人push
     */
    private DingDingRobotPushDTO dingPush;

    /**
     * 邮件抄送人
     */
    private Set<String> ccList;
}
