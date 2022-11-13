package com.cloud.rocketmq.consumer.monitor.admin.common.enums;

import com.cloud.rocketmq.consumer.monitor.admin.dto.ValueTextDTO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 任务类型
 */
@Getter
public enum JobTypeEnum {
    REPORT_UNDONE_MSGS("reportUndoneMsgs", "未消费消息"),
    REPORT_CONSUMER_NOT_ONLINE("reportConsumerNotOnline", "消费组不在线"),
    REPORT_FAILED_MSGS("reportFailedMsgs", "消费失败"),
    REPORT_DELETE_MSGS_EVENT("reportDeleteMsgsEvent", "消费消息已被删除或请求消息不存在"),
    SUBSCRIPTION_DIFFERENT("reportConsumerSubscriptionDifferent", "消费者订阅信息不一致"),
    CONSUMER_GROUP_BLOCK("consumerGroupBlock", "消费者阻塞告警"),
    REPORT_STOPED_BROKER("reportStopedBroker", "broker未启动"),
    REPORT_RISKED_BROKER("reportRiskedBroker", "broker运行状态cpu、磁盘、TPS等"),
    MONITOR_WORK_EXCEPTION("MonitorWorkException", "mqm运行异常"),


    ;

    private String code;
    private String codeDesc;

    JobTypeEnum(String code, String codeDesc) {
        this.code = code;
        this.codeDesc = codeDesc;
    }

    /**
     * 获取所有枚举list
     *
     * @return
     */
    public static List<ValueTextDTO> getList() {
        List<ValueTextDTO> list = new ArrayList<ValueTextDTO>();
        for (JobTypeEnum type : JobTypeEnum.values()) {
            ValueTextDTO valueTextVO = new ValueTextDTO();
            valueTextVO.setValue(type.getCode());
            valueTextVO.setText(type.getCodeDesc());
            list.add(valueTextVO);
        }
        return list;
    }

    private static final HashMap<String, JobTypeEnum> map = new HashMap<>();

    static {
        for (JobTypeEnum status : JobTypeEnum.values()) {
            map.put(status.getCode(), status);
        }
    }

    public static JobTypeEnum of(String code) {
        return map.get(code);
    }
}
