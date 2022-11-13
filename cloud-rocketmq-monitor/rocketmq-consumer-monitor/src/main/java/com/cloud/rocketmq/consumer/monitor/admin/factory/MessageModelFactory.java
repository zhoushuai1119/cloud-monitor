package com.cloud.rocketmq.consumer.monitor.admin.factory;

import com.cloud.rocketmq.consumer.monitor.admin.dto.PushAlterDTO;
import com.ctrip.framework.apollo.ConfigService;

import java.time.Instant;


/**
 * @Author 马腾飞
 * @Date 2020/2/20
 * @Time 16:24
 * @Description
 */
public class MessageModelFactory {

    protected static String profile = ConfigService.getAppConfig().getProperty("spring.profiles.active", "none");

    public static PushAlterDTO createPushAlmDTO() {
        PushAlterDTO pushAlterDTO = new PushAlterDTO();
        pushAlterDTO.setStartsAt(Instant.now().toString());
        pushAlterDTO.setEndsAt("0001-01-01T00:00:00Z");

        pushAlterDTO.getLabels().setAlarmType("rocketmq告警");
        pushAlterDTO.getLabels().setAlertname("RocketMq#mqm");
        pushAlterDTO.getLabels().setJob(profile + "-rocketmq-monitor");

        return pushAlterDTO;
    }

}
