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

import com.cloud.rocketmq.consumer.monitor.admin.admin.DefaultMQAdminExt;
import com.cloud.rocketmq.consumer.monitor.admin.common.enums.JobTypeEnum;
import com.cloud.rocketmq.consumer.monitor.admin.dto.BrokerStatusDTO;
import com.cloud.rocketmq.consumer.monitor.admin.dto.PushAlterDTO;
import com.cloud.rocketmq.consumer.monitor.admin.factory.MessageModelFactory;
import com.cloud.rocketmq.consumer.monitor.admin.service.AlterService;
import com.google.common.collect.Maps;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.*;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.topic.OffsetMovedEvent;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class MonitorService {
    private final Logger log = LoggerFactory.getLogger("monitor");
    private final ScheduledExecutorService scheduledExecutorService = Executors
            .newSingleThreadScheduledExecutor(new ThreadFactoryImpl("MonitorService"));

    private DefaultMQAdminExt defaultMQAdminExt;
    private final DefaultMQPullConsumer defaultMQPullConsumer = new DefaultMQPullConsumer(
            MixAll.TOOLS_CONSUMER_GROUP);
    private final DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer(
            MixAll.MONITOR_CONSUMER_GROUP);

    @Autowired
    private MonitorConfig monitorConfig;
    @Autowired
    private AlterService alterService;
    @Autowired
    private  MonitorListener monitorListener;
    @Autowired
    private AclClientRPCHook aclRPCHook;

    private void initMqAdmin(AclClientRPCHook aclRPCHook) {
        this.defaultMQAdminExt = new DefaultMQAdminExt(aclRPCHook);
        this.defaultMQAdminExt.setInstanceName(instanceName());
        this.defaultMQAdminExt.setNamesrvAddr(monitorConfig.getNamesrvAddr());

        this.defaultMQPullConsumer.setInstanceName(instanceName());
        this.defaultMQPullConsumer.setNamesrvAddr(monitorConfig.getNamesrvAddr());

        this.defaultMQPushConsumer.setInstanceName(instanceName());
        this.defaultMQPushConsumer.setNamesrvAddr(monitorConfig.getNamesrvAddr());
        try {
            this.defaultMQPushConsumer.setConsumeThreadMin(1);
            this.defaultMQPushConsumer.setConsumeThreadMax(1);
            //this.defaultMQPushConsumer.subscribe(MixAll.OFFSET_MOVED_EVENT, "*");
            this.defaultMQPushConsumer.subscribe("OFFSET_MOVED_EVENT", "*");
            this.defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {

                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                                ConsumeConcurrentlyContext context) {
                    try {
                        OffsetMovedEvent ome =
                                OffsetMovedEvent.decode(msgs.get(0).getBody(), OffsetMovedEvent.class);
                        if (ome.getOffsetNew() == 0) {
                            log.info("may no message in queue or offset overflow minOffset is 0");
                            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                        }
                        if (ome.getOffsetRequest() == 0 && ome.getMessageQueue().getTopic().contains("_BROADCAST")) {
                            log.info("ignore broadcast message warn when OffsetMovedEvent,because docker can't persistence data");
                            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                        }
                        DeleteMsgsEvent deleteMsgsEvent = new DeleteMsgsEvent();
                        deleteMsgsEvent.setOffsetMovedEvent(ome);
                        deleteMsgsEvent.setEventTimestamp(msgs.get(0).getStoreTimestamp());

                        monitorListener.reportDeleteMsgsEvent(deleteMsgsEvent);
                    } catch (Exception e) {
                    }

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
        } catch (MQClientException e) {
        }
    }

    @PostConstruct
    public void postConstruct() throws MQClientException {
        this.initMqAdmin(aclRPCHook);

        this.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            private volatile boolean hasShutdown = false;

            @Override
            public void run() {
                synchronized (this) {
                    if (!this.hasShutdown) {
                        this.hasShutdown = true;
                        shutdown();
                    }
                }
            }
        }, "ShutdownHook"));
    }


    private String instanceName() {
        String name =
                System.currentTimeMillis() + new Random().nextInt() + this.monitorConfig.getNamesrvAddr();

        return "MonitorService_" + name.hashCode();
    }

    public void start() throws MQClientException {
        this.defaultMQPullConsumer.start();
        this.defaultMQAdminExt.start();
        this.defaultMQPushConsumer.start();
        this.startScheduleTask();

        log.info(String.format("mq monitor %s","start"));
    }

    public void shutdown() {
        this.defaultMQPullConsumer.shutdown();
        this.defaultMQAdminExt.shutdown();
        this.defaultMQPushConsumer.shutdown();

        log.info(String.format("mq monitor %s","shutdown"));
        try {
            TimeUnit.SECONDS.sleep(15L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public AclClientRPCHook aclRPCHook() {
        return new AclClientRPCHook(new SessionCredentials("zhoushuai", "zs19921119"));
    }

    private void startScheduleTask() {
        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    doConsumerMonitorWork();
                } catch (Exception e) {
                    PushAlterDTO pushAlterDTO = MessageModelFactory.createPushAlmDTO();
                    pushAlterDTO.addLables(JobTypeEnum.MONITOR_WORK_EXCEPTION.getCode(), "", "doConsumerMonitorWork");
                    pushAlterDTO.getAnnotations().setAlarmContent(String.format("doConsumerMonitorWork \n%s", e));
                    alterService.alterPost(pushAlterDTO);
                    log.error("doConsumerMonitorWork",e);
                }
                try {
                    doBrokerMonitorWork();
                } catch (Exception e) {
                    PushAlterDTO pushAlterDTO = MessageModelFactory.createPushAlmDTO();
                    pushAlterDTO.addLables(JobTypeEnum.MONITOR_WORK_EXCEPTION.getCode(), "", "doBrokerMonitorWork");
                    pushAlterDTO.getAnnotations().setAlarmContent(String.format("doBrokerMonitorWork \n%s", e));
                    alterService.alterPost(pushAlterDTO);
                    log.error("doBrokerMonitorWork",e);
                }
            }
        }, 1000 * 20, this.monitorConfig.getRoundInterval(), TimeUnit.MILLISECONDS);
    }


    public void doBrokerMonitorWork() throws Exception {
        Map<String, Object> resultMap = Maps.newHashMap();
        ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
        Map<String/*brokerName*/, Map<Long/* brokerId */, Object/* brokerDetail */>> brokerServer = Maps.newHashMap();
        Set<String> runningBrokerNames = new HashSet<>();

        List<BrokerStatusDTO> brokerStatusList = new ArrayList<>();

        for (BrokerData brokerData : clusterInfo.getBrokerAddrTable().values()) {
            Map<Long, Object> brokerMasterSlaveMap = Maps.newHashMap();
            for (Entry<Long/* brokerid */, String/* broker address */> brokerAddr : brokerData.getBrokerAddrs().entrySet()) {
                KVTable kvTable = defaultMQAdminExt.fetchBrokerRuntimeStats(brokerAddr.getValue());
                brokerMasterSlaveMap.put(brokerAddr.getKey(), kvTable.getTable());
                brokerStatusList.add(new BrokerStatusDTO(brokerData.getBrokerName(), brokerAddr.getKey(), kvTable));
            }
            runningBrokerNames.add(brokerData.getBrokerName());
            brokerServer.put(brokerData.getBrokerName(), brokerMasterSlaveMap);
        }

        //监控是否有停止运行的broker
        List<String> stopdBrokers = new ArrayList<>();
        for (String brokerName : monitorConfig.getBrokerNames()) {
            if (!runningBrokerNames.contains(brokerName)) {
                stopdBrokers.add(brokerName);
            }
        }
        if (!stopdBrokers.isEmpty()) {
            monitorListener.reportStopedBroker(stopdBrokers);
        }
        //判断是否需要输出
        if (!brokerStatusList.isEmpty()) {
            boolean needNotify = false;
            String TT_BROKER_NAME = "broker";
            String TT_DISK = "磁盘";
            String TT_PUTTPS = "生产TPS";
            String TT_GETTPS = "消费TPS";
            Map<String, Map<String, String>> mdTable = new LinkedHashMap<>();
            for (BrokerStatusDTO brokerStatus : brokerStatusList) {
                //过滤salve 节点，#0 表示 Master，>0 表示 Slave
                if (brokerStatus.getBrokerId() > 0) {
                    continue;
                }

                HashMap<String, String> prop = new LinkedHashMap<>();
                mdTable.put(brokerStatus.getBrokerName(), prop);
                if (Double.parseDouble(brokerStatus.getDiskRatio()) > monitorConfig.getDiskRatioThreshold()) {
                    needNotify = true;
                    prop.put(TT_DISK, "**" + brokerStatus.getDiskRatio() + ", " + brokerStatus.getDiskInfo() + "**");
                } else {
                    prop.put(TT_DISK, brokerStatus.getDiskRatio() + ", " + brokerStatus.getDiskInfo());
                }
                if (Double.parseDouble(brokerStatus.getPutTps()) > monitorConfig.getTpsThreshold()) {
                    needNotify = true;
                    prop.put(TT_PUTTPS, "**" + brokerStatus.getPutTps() + "**");
                } else {
                    prop.put(TT_PUTTPS, brokerStatus.getPutTps());
                }
                prop.put(TT_GETTPS, brokerStatus.getGetTps());
            }
            if (needNotify) {
                monitorListener.reportRiskedBroker(mdTable);
            }
        }
    }

    public void doConsumerMonitorWork() throws RemotingException, MQClientException, InterruptedException {
        long beginTime = System.currentTimeMillis();
        this.monitorListener.beginRound();

        TopicList topicList = defaultMQAdminExt.fetchAllTopicList();
        for (String topic : topicList.getTopicList()) {
            if (topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                String consumerGroup = topic.substring(MixAll.RETRY_GROUP_TOPIC_PREFIX.length());

                try {
                    this.reportUndoneMsgs(consumerGroup);
                } catch (Exception e) {
                    // log.error("reportUndoneMsgs Exception", e);
                }

                try {
                    this.reportConsumerRunningInfo(consumerGroup);
                } catch (Exception e) {
                    //log.error("reportConsumerRunningInfo Exception", e);
                }
            }
        }
        this.monitorListener.endRound();
        long spentTimeMills = System.currentTimeMillis() - beginTime;
        //log.info("Execute one round monitor work, spent timemills: {}", spentTimeMills);
    }

    private void reportUndoneMsgs(final String consumerGroup) {
        ConsumeStats cs = null;
        try {
            cs = defaultMQAdminExt.examineConsumeStats(consumerGroup);
        } catch (Exception e) {
            return;
        }

        ConsumerConnection cc = null;
        try {
            cc = defaultMQAdminExt.examineConsumerConnectionInfo(consumerGroup);
        } catch (Exception e) {
            return;
        }

        if (cs != null) {

            HashMap<String/* Topic */, ConsumeStats> csByTopic = new HashMap<String, ConsumeStats>();
            {
                Iterator<Entry<MessageQueue, OffsetWrapper>> it = cs.getOffsetTable().entrySet().iterator();
                while (it.hasNext()) {
                    Entry<MessageQueue, OffsetWrapper> next = it.next();
                    MessageQueue mq = next.getKey();
                    OffsetWrapper ow = next.getValue();
                    ConsumeStats csTmp = csByTopic.get(mq.getTopic());
                    if (null == csTmp) {
                        csTmp = new ConsumeStats();
                        csByTopic.put(mq.getTopic(), csTmp);
                    }

                    csTmp.getOffsetTable().put(mq, ow);
                }
            }

            {
                Iterator<Entry<String, ConsumeStats>> it = csByTopic.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, ConsumeStats> next = it.next();
                    UndoneMsgs undoneMsgs = new UndoneMsgs();
                    undoneMsgs.setConsumerGroup(consumerGroup);
                    undoneMsgs.setTopic(next.getKey());
                    this.computeUndoneMsgs(undoneMsgs, next.getValue());
                    //积压告警
                    if (hasUndone(undoneMsgs)) {
                        monitorListener.reportUndoneMsgs(undoneMsgs);
                    }

                    this.reportFailedMsgs(consumerGroup, next.getKey());
                }
            }
        }
    }
    //是否积压告警
    private boolean hasUndone(UndoneMsgs undoneMsgs) {
        //是否自定义消费者的 告警
        if (monitorConfig.hasCustomConsumer(undoneMsgs.getConsumerGroup())) {
            //是否有个性化积压配置,eg monitor.config.undoneMsgConsumer.c_bi_flink_cabinet_store_heartbeat.TP_IOT_DAG_CAB_HB = 50000
            Map<String, Integer> topicUndoMap = monitorConfig.getUndoneMsgConsumer().get(undoneMsgs.getConsumerGroup());

            if (null != topicUndoMap.get(undoneMsgs.getTopic())
                    && undoneMsgs.getUndoneMsgsTotal() >= topicUndoMap.get(undoneMsgs.getTopic())) {
                log.info("自定义告警积压：{}={}", undoneMsgs.getTopic(), topicUndoMap.get(undoneMsgs.getTopic()));
                return true;
            }
        }

        //默认积压配置
        return undoneMsgs.getUndoneMsgsTotal() >= monitorConfig.getUndoneMessageMax()
                //超过特定毫秒数
                && undoneMsgs.getUndoneMsgsDelayTimeMills() >= monitorConfig.getUndoneMessageMaxTimeMs();
    }


    public void reportConsumerRunningInfo(final String consumerGroup) throws InterruptedException,
            MQBrokerException, RemotingException, MQClientException {
        ConsumerConnection cc = null;

        try {
            cc = defaultMQAdminExt.examineConsumerConnectionInfo(consumerGroup);
        } catch (MQBrokerException e) {
            if (e.getResponseCode() == 206) {
                //CODE: 206  DESC: the consumer group[c_example] not online
                monitorListener.reportConsumerNotOnline(consumerGroup);
            }
            throw e;
        }
        TreeMap<String, ConsumerRunningInfo> infoMap = new TreeMap<String, ConsumerRunningInfo>();
        for (Connection c : cc.getConnectionSet()) {
            String clientId = c.getClientId();

            if (c.getVersion() < MQVersion.Version.V3_1_8_SNAPSHOT.ordinal()) {
                continue;
            }

            try {
                ConsumerRunningInfo info =
                        defaultMQAdminExt.getConsumerRunningInfo(consumerGroup, clientId, false);
                infoMap.put(clientId, info);
            } catch (Exception e) {
            }
        }

        if (!infoMap.isEmpty()) {
            monitorListener.reportConsumerRunningInfo(consumerGroup, infoMap, monitorConfig);
        }
    }

    private void computeUndoneMsgs(final UndoneMsgs undoneMsgs, final ConsumeStats consumeStats) {
        long total = 0;
        long singleMax = 0;
        long delayMax = 0;
        Iterator<Entry<MessageQueue, OffsetWrapper>> it = consumeStats.getOffsetTable().entrySet().iterator();
        while (it.hasNext()) {
            Entry<MessageQueue, OffsetWrapper> next = it.next();
            MessageQueue mq = next.getKey();
            OffsetWrapper ow = next.getValue();
            long diff = ow.getBrokerOffset() - ow.getConsumerOffset();

            if (diff > singleMax) {
                singleMax = diff;
            }

            if (diff > 0) {
                total += diff;
            }

            // Delay
            if (ow.getLastTimestamp() > 0) {
                try {
                    long maxOffset = this.defaultMQPullConsumer.maxOffset(mq);
                    if (maxOffset > 0) {
                        PullResult pull = this.defaultMQPullConsumer.pull(mq, "*", maxOffset - 1, 1);
                        switch (pull.getPullStatus()) {
                            case FOUND:
                                long delay =
                                        pull.getMsgFoundList().get(0).getStoreTimestamp() - ow.getLastTimestamp();
                                if (delay > delayMax) {
                                    delayMax = delay;
                                }
                                break;
                            case NO_MATCHED_MSG:
                            case NO_NEW_MSG:
                            case OFFSET_ILLEGAL:
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        undoneMsgs.setUndoneMsgsTotal(total);
        undoneMsgs.setUndoneMsgsSingleMQ(singleMax);
        undoneMsgs.setUndoneMsgsDelayTimeMills(delayMax);
    }

    private void reportFailedMsgs(final String consumerGroup, final String topic) {
        FailedMsgs failedMsgs = new FailedMsgs();
        failedMsgs.setConsumerGroup(consumerGroup);
        failedMsgs.setTopic(topic);
        monitorListener.reportFailedMsgs(failedMsgs);
    }
}
