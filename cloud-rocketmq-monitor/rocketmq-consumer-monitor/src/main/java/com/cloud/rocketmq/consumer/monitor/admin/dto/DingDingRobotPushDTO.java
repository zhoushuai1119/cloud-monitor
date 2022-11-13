package com.cloud.rocketmq.consumer.monitor.admin.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author 张云和
 * @Date 2018/8/16
 * @Time 11:44
 */
@Data
public class DingDingRobotPushDTO implements Serializable{

    private List<String> atEmployeeNumbers;

    private String isAtAll = "false";
}
