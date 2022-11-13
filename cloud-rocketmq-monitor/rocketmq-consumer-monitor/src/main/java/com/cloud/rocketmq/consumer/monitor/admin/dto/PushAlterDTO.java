package com.cloud.rocketmq.consumer.monitor.admin.dto;

import com.cloud.rocketmq.consumer.monitor.admin.common.utils.CommonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 推送请求DTO
 */
@Data
public class PushAlterDTO {
    /**
     * labels 标签
     */
    private LabelsDTO labels;
    /***
     * 推送信息
     */
    private AnnotationsDTO annotations;
    /**
     * 告警出现时间
     */
    private String startsAt;

    private String endsAt;

    public PushAlterDTO() {
        labels = new LabelsDTO();
        annotations = new AnnotationsDTO();
    }

    //设置标签
    public void addLables(String jobType, String consumerGroup, String extendedField) {
        labels.setJobType(jobType);
        labels.setConsumerGroup(consumerGroup);
        labels.setExtendedField(extendedField);
    }

    //获取当前对象的缓存hash
    public String cacheHashKey() {
        return CommonUtils.hashByKey(labels.getJobType() + labels.getConsumerGroup() + labels.getExtendedField());
    }

    @Data
    public class LabelsDTO {
        private String alarmType;

        private String alertname;
        //任务名（分类表签 prd-rocketmq-monitor）
        private String job;
        //任务类型（分类标签）
        private String jobType;
        //消费组（分类标签）
        private String consumerGroup;
        //拓展字段	可以表示client 或者broker名字或者其他字段
        private String extendedField;
    }

    @Data
    public class AnnotationsDTO {
        //告警内容
        private String alarmContent;
        //告警钉钉机器人
        @JsonProperty(value = "channel_7_mqm")
        private String channelMqm;
    }
}
