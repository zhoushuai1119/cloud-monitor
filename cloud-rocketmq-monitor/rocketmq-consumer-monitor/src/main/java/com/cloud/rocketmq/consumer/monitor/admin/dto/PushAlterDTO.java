package com.cloud.rocketmq.consumer.monitor.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 推送请求DTO
 * @author shuai.zhou
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushAlterDTO implements Serializable {

    private static final long serialVersionUID = 569550145453830718L;

    /**
     * 告警类型
     */
    private String alarmType;
    /**
     * 告警内容
     */
    private String alarmContent;
    /**
     * 消费者组
     */
    private String consumerGroup;
    /**
     * 拓展字段(可以表示client 或者broker名字或者其他字段)
     */
    private String extendedField;

}
