package com.cloud.rocketmq.consumer.monitor.admin.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.cloud.rocketmq.consumer.monitor.admin.dto.DingDingRobotPushDTO;
import com.cloud.rocketmq.consumer.monitor.admin.dto.PushAlterDTO;
import com.cloud.rocketmq.consumer.monitor.admin.dto.PushReceiverDTO;
import com.cloud.rocketmq.consumer.monitor.admin.dto.PushRequestDTO;
import com.cloud.rocketmq.consumer.monitor.admin.monitor.MonitorConfig;
import com.cloud.rocketmq.consumer.monitor.admin.service.AlterService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @Author 马腾飞
 * @Date 2020/2/26
 * @Time 19:03
 * @Description
 */
@Service
@Slf4j
public class AlterServiceImpl implements AlterService {

    @Value("${spring.profiles.active}")
    String profile;

    @Autowired
    private MonitorConfig monitorConfig;


    @Override
    public void alterPost(PushAlterDTO pushAlterDTO) {
        //发送给MSG
        HttpUtil.post(monitorConfig.getMsgUri(), formatMessage2MSG(pushAlterDTO));
        //httpPost(formatMessage2MSG(pushAlterDTO), monitorConfig.getMsgUri());
        //直接推送给alterManager
        String message = formatMessage2AlterManager(pushAlterDTO);
        //httpPost(message, monitorConfig.getAlterUri());
        HttpUtil.post(monitorConfig.getAlterUri(), message);
        log.info(message);
    }

    //发送消息给AlterManager
    private String formatMessage2AlterManager(PushAlterDTO pushAlterDTO) {
        String content = pushAlterDTO.getAnnotations().getAlarmContent().replace('"', ' ').replace('\'', ' ');
//                .replace("\n", "\\/n");//钉钉推送消息处理
        pushAlterDTO.getAnnotations().setAlarmContent(content);
        pushAlterDTO.getAnnotations().setChannelMqm(monitorConfig.getAlterManagerToken());

        return JSONUtil.toJsonStr(Lists.newArrayList(pushAlterDTO));
    }

    //发送消息给MSG
    private String formatMessage2MSG(PushAlterDTO pushAlterDTO) {
        String split = " ";
        StringBuilder buffer = new StringBuilder();
        buffer.append(profile + "-rocketmq-monitor-old");
        buffer.append(split);
        buffer.append(LocalDateTime.now());
        buffer.append(split);
        buffer.append("\n");
        buffer.append(pushAlterDTO.getAnnotations().getAlarmContent());

        PushRequestDTO pushRequest = new PushRequestDTO();
        pushRequest.setTemplateCode("MSG_MQ_MONITOR");
        pushRequest.setSystemCode("MQM");
        pushRequest.setPushType(7);
        pushRequest.setKeywords(Maps.newHashMap());
        pushRequest.setReceivers(Lists.newArrayList());
        pushRequest.setDingPush(new DingDingRobotPushDTO());

        PushReceiverDTO receiver = new PushReceiverDTO();
        receiver.setAccessToken(monitorConfig.getMSGToken());

        pushRequest.getReceivers().add(receiver);
        String content = buffer.toString().replace('"', ' ').replace('\'', ' ');
//                .replace("\n", "\\/n");
        pushRequest.getKeywords().put("content", content);
        return JSONUtil.toJsonStr(pushRequest);
    }


}
