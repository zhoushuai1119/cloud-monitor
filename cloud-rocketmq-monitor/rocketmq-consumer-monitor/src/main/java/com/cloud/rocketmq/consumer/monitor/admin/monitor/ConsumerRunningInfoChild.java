package com.cloud.rocketmq.consumer.monitor.admin.monitor;

import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.common.protocol.body.ProcessQueueInfo;
import org.apache.rocketmq.common.protocol.heartbeat.ConsumeType;

import java.util.Iterator;
import java.util.Map;

/**
 * @Author 马腾飞
 * @Date 2019/9/11
 * @Time 16:58
 * @Description
 */
public class ConsumerRunningInfoChild extends ConsumerRunningInfo {

    public static String analyzeProcessQueue(final String clientId, ConsumerRunningInfo info, MonitorConfig monitorConfig) {
        StringBuilder sb = new StringBuilder();
        boolean push = false;
        {
            String property = info.getProperties().getProperty(ConsumerRunningInfo.PROP_CONSUME_TYPE);

            if (property == null) {
                property = ((ConsumeType) info.getProperties().get(ConsumerRunningInfo.PROP_CONSUME_TYPE)).name();
            }
            push = ConsumeType.valueOf(property) == ConsumeType.CONSUME_PASSIVELY;
        }

        boolean orderMsg = false;
        {
            String property = info.getProperties().getProperty(ConsumerRunningInfo.PROP_CONSUME_ORDERLY);
            orderMsg = Boolean.parseBoolean(property);
        }

        if (push) {
            Iterator<Map.Entry<MessageQueue, ProcessQueueInfo>> it = info.getMqTable().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<MessageQueue, ProcessQueueInfo> next = it.next();
                MessageQueue mq = next.getKey();
                ProcessQueueInfo pq = next.getValue();

                if (orderMsg) {
                    //未获得锁 && 本地未消费的消息大于零
                    if (!pq.isLocked() && pq.getCachedMsgCount() > 0) {
                        sb.append(String.format("%s %s can't lock for a while, %dms%n  ******* info： %s",
                                clientId,
                                mq,
                                System.currentTimeMillis() - pq.getLastLockTimestamp(),
                                pq
                        ));
                    } else {
                        if (pq.isDroped() && (pq.getTryUnlockTimes() > 0)) {
                            sb.append(String.format("%s %s unlock %d times, still failed%n ******* info： %s",
                                    clientId,
                                    mq,
                                    pq.getTryUnlockTimes(),
                                    pq
                            ));
                        }
                    }

                } else {
                    long diff = System.currentTimeMillis() - pq.getLastConsumeTimestamp();
                    //最后消费时间 1分钟前，积压数据60000 阻塞告警
                    if (diff > monitorConfig.getBlockedMessageMaxTimeMs()
                            && pq.getCachedMsgCount() > monitorConfig.getBlockedMessageMaxTimeMs()) {
                        sb.append(String.format("%s %s can't consume for a while, maybe blocked, %dms%n",
                                clientId,
                                mq,
                                diff));
                    }
                }
            }
        }

        return sb.toString();
    }
}
