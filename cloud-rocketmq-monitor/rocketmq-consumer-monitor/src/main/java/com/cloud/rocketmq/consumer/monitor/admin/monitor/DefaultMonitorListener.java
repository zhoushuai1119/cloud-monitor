/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloud.rocketmq.consumer.monitor.admin.monitor;

import com.cloud.rocketmq.consumer.monitor.admin.common.enums.JobTypeEnum;
import com.cloud.rocketmq.consumer.monitor.admin.common.utils.MarkdownCreaterUtil;
import com.cloud.rocketmq.consumer.monitor.admin.dto.PushAlterDTO;
import com.cloud.rocketmq.consumer.monitor.admin.factory.MessageModelFactory;
import com.cloud.rocketmq.consumer.monitor.admin.service.AlterService;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
@Component
public class DefaultMonitorListener implements MonitorListener {
    private final static String LOG_PREFIX = "";
    private final static String LOG_NOTIFY = LOG_PREFIX + "";
    @Autowired
    private AlterService alterService;

    public DefaultMonitorListener() {
    }

    @Override
    public void beginRound() {
        //log.info(LOG_PREFIX + "=========================================beginRound");
    }

    @Override
    public void reportUndoneMsgs(UndoneMsgs undoneMsgs) {
        PushAlterDTO pushAlterDTO = MessageModelFactory.createPushAlmDTO();
        String jobType = JobTypeEnum.REPORT_UNDONE_MSGS.getCode();
        pushAlterDTO.addLables(jobType, undoneMsgs.getConsumerGroup(), undoneMsgs.getTopic());
        pushAlterDTO.getAnnotations()
                .setAlarmContent(String.format(LOG_PREFIX + "reportUndoneMsgs: %s", undoneMsgs));
        alterService.alterPost(pushAlterDTO);
    }

    @Override
    public void reportConsumerNotOnline(String consumerGroup) {
        PushAlterDTO pushAlterDTO = MessageModelFactory.createPushAlmDTO();
        pushAlterDTO.addLables(JobTypeEnum.REPORT_CONSUMER_NOT_ONLINE.getCode(), consumerGroup, "");
        pushAlterDTO.getAnnotations()
                .setAlarmContent(String.format("消费组不在线: %s", consumerGroup));
        alterService.alterPost(pushAlterDTO);
    }

    @Override
    public void reportFailedMsgs(FailedMsgs failedMsgs) {
        PushAlterDTO pushAlterDTO = MessageModelFactory.createPushAlmDTO();
        pushAlterDTO.addLables(JobTypeEnum.REPORT_FAILED_MSGS.getCode(), failedMsgs.getConsumerGroup(), failedMsgs.getTopic());
        pushAlterDTO.getAnnotations()
                .setAlarmContent(String.format(LOG_PREFIX + "reportFailedMsgs: %s", failedMsgs));
        alterService.alterPost(pushAlterDTO);
    }

    @Override
    public void reportDeleteMsgsEvent(DeleteMsgsEvent deleteMsgsEvent) {
        PushAlterDTO pushAlterDTO = MessageModelFactory.createPushAlmDTO();
        String consumerGroup = Optional.ofNullable(deleteMsgsEvent.getOffsetMovedEvent().getConsumerGroup())
                .orElse(deleteMsgsEvent.toString());
        String extendedField = Optional.ofNullable(deleteMsgsEvent.getOffsetMovedEvent().getMessageQueue().toString())
                .orElse(deleteMsgsEvent.toString());
        pushAlterDTO.addLables(JobTypeEnum.REPORT_DELETE_MSGS_EVENT.getCode(), consumerGroup, extendedField);
        pushAlterDTO.getAnnotations()
                .setAlarmContent(String.format(LOG_PREFIX + "reportDeleteMsgsEvent: %s", deleteMsgsEvent));
        alterService.alterPost(pushAlterDTO);
    }

    @Override
    public void reportConsumerRunningInfo(String consumerGroup, TreeMap<String, ConsumerRunningInfo> criTable, MonitorConfig monitorConfig) {

        {
            boolean result = ConsumerRunningInfo.analyzeSubscription(criTable);
            if (!result) {
                Map<String, Map<String, String>> details = new LinkedHashMap<>();

                Entry<String, ConsumerRunningInfo> prev = criTable.firstEntry();
                Iterator<Entry<String, ConsumerRunningInfo>> it = criTable.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, ConsumerRunningInfo> next = it.next();
                    boolean equals = next.getValue().getSubscriptionSet().equals(prev.getValue().getSubscriptionSet());
                    if (!equals) {
                        details.put(prev.getKey(), getSubsciptionInfo(prev.getValue().getSubscriptionSet()));
                        details.put(next.getKey(), getSubsciptionInfo(next.getValue().getSubscriptionSet()));
                        break;
                    }
                }

                PushAlterDTO pushAlterDTO = MessageModelFactory.createPushAlmDTO();
                pushAlterDTO.addLables(JobTypeEnum.SUBSCRIPTION_DIFFERENT.getCode(), consumerGroup, "");
                pushAlterDTO.getAnnotations()
                        .setAlarmContent(String.format(LOG_NOTIFY
                                        + "同一消费组订阅信息不一致告警: ConsumerGroup: %s, Subscription different \n%s",
                                consumerGroup, MarkdownCreaterUtil.listMarkdown(details)));
                alterService.alterPost(pushAlterDTO);
            }
        }

        {
            Iterator<Entry<String, ConsumerRunningInfo>> it = criTable.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, ConsumerRunningInfo> next = it.next();
                String result = ConsumerRunningInfoChild.analyzeProcessQueue(next.getKey(), next.getValue(), monitorConfig);
                if (!result.isEmpty()) {
                    String clientId = next.getKey();

                    PushAlterDTO pushAlterDTO = MessageModelFactory.createPushAlmDTO();
                    pushAlterDTO.addLables(JobTypeEnum.CONSUMER_GROUP_BLOCK.getCode(), consumerGroup, next.getKey());
                    pushAlterDTO.getAnnotations()
                            .setAlarmContent(String.format(LOG_NOTIFY + "消费者阻塞告警: ConsumerGroup: %s, ClientId: %s, %s",
                                    consumerGroup, clientId, result));
                    alterService.alterPost(pushAlterDTO);
                }
            }
        }
    }

    @Override
    public void reportStopedBroker(List<String> brokerNames) {
        PushAlterDTO pushAlterDTO = MessageModelFactory.createPushAlmDTO();
        pushAlterDTO.addLables(JobTypeEnum.REPORT_STOPED_BROKER.getCode(), brokerNames.toString(), "");
        pushAlterDTO.getAnnotations()
                .setAlarmContent(String.format(LOG_PREFIX + "broker未启动: %s", brokerNames));
        alterService.alterPost(pushAlterDTO);
    }

    @Override
    public void reportRiskedBroker(Map<String, Map<String, String>> notifyTable) {
        PushAlterDTO pushAlterDTO = MessageModelFactory.createPushAlmDTO();
        pushAlterDTO.addLables(JobTypeEnum.REPORT_RISKED_BROKER.getCode(), "", "");
        pushAlterDTO.getAnnotations()
                .setAlarmContent(String.format(LOG_PREFIX + "broker运行状态: \n%s", MarkdownCreaterUtil.listMarkdown(notifyTable)));
        alterService.alterPost(pushAlterDTO);
    }

    @Override
    public void endRound() {
        //log.info(LOG_PREFIX + "=========================================endRound");
    }

    private Map<String, String> getSubsciptionInfo(TreeSet<SubscriptionData> subscriptionData) {
        Map<String, String> map = new HashMap<>();
        for (SubscriptionData d : subscriptionData) {
            map.put(d.getTopic(), d.getTagsSet().toString());
        }
        return map;
    }
}
