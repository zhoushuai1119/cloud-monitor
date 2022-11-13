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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.rocketmq.exporter.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.protocol.body.KVTable;
import org.apache.rocketmq.exporter.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class BrokerRuntimeStats {
    private long msgPutTotalTodayNow;
    private long msgGetTotalTodayNow;
    private long msgPutTotalTodayMorning;
    private long msgGetTotalTodayMorning;
    private long msgPutTotalYesterdayMorning;
    private long msgGetTotalYesterdayMorning;
    private List<ScheduleMessageOffsetTable> scheduleMessageOffsetTables = new ArrayList<>();
    private long sendThreadPoolQueueHeadWaitTimeMills;
    private long queryThreadPoolQueueHeadWaitTimeMills;
    private long pullThreadPoolQueueHeadWaitTimeMills;
    private long queryThreadPoolQueueSize;
    private long pullThreadPoolQueueSize;
    private long sendThreadPoolQueueCapacity;
    private long pullThreadPoolQueueCapacity;
    private Map<String, Integer> putMessageDistributeTimeMap = new HashMap<>();
    private double remainHowManyDataToFlush;
    private long commitLogMinOffset;
    private long commitLogMaxOffset;
    private String runtime;
    private long bootTimestamp;
    private double commitLogDirCapacityTotal;
    private double commitLogDirCapacityFree;
    private int brokerVersion;
    private long dispatchMaxBuffer;

    private PutTps putTps = new PutTps();
    private GetMissTps getMissTps = new GetMissTps();
    private GetTransferedTps getTransferedTps = new GetTransferedTps();
    private GetTotalTps getTotalTps = new GetTotalTps();
    private GetFoundTps getFoundTps = new GetFoundTps();

    private double consumeQueueDiskRatio;
    private double commitLogDiskRatio;

    private long pageCacheLockTimeMills;

    private long getMessageEntireTimeMax;

    private long putMessageTimesTotal;

    private String brokerVersionDesc;
    private long sendThreadPoolQueueSize;
    private long startAcceptSendRequestTimeStamp;
    private long putMessageEntireTimeMax;
    private long earliestMessageTimeStamp;

    private long remainTransientStoreBufferNumbs;
    private long queryThreadPoolQueueCapacity;
    private double putMessageAverageSize;
    private long putMessageSizeTotal;
    private long dispatchBehindBytes;
    private double putLatency99;
    private double putLatency999;


    private final static Logger log = LoggerFactory.getLogger(BrokerRuntimeStats.class);
    public BrokerRuntimeStats(KVTable kvTable) {
        this.msgPutTotalTodayNow = Long.parseLong(kvTable.getTable().get("msgPutTotalTodayNow"));

        loadScheduleMessageOffsets(kvTable);
        loadPutMessageDistributeTime(kvTable.getTable().get("putMessageDistributeTime"));

        loadTps(this.putTps, kvTable.getTable().get("putTps"));
        loadTps(this.getMissTps, kvTable.getTable().get("getMissTps"));
        loadTps(this.getTransferedTps, kvTable.getTable().get("getTransferredTps"));
        loadTps(this.getTotalTps, kvTable.getTable().get("getTotalTps"));
        loadTps(this.getFoundTps, kvTable.getTable().get("getFoundTps"));

        loadCommitLogDirCapacity(kvTable.getTable().get("commitLogDirCapacity"));

        this.sendThreadPoolQueueHeadWaitTimeMills = Long.parseLong(kvTable.getTable().get("sendThreadPoolQueueHeadWaitTimeMills"));
        this.queryThreadPoolQueueHeadWaitTimeMills = Long.parseLong(kvTable.getTable().get("queryThreadPoolQueueHeadWaitTimeMills"));

        this.remainHowManyDataToFlush = Double.parseDouble(kvTable.getTable().get("remainHowManyDataToFlush").split(" ")[0]);//byte
        this.msgGetTotalTodayNow = Long.parseLong(kvTable.getTable().get("msgGetTotalTodayNow"));
        this.queryThreadPoolQueueSize = Long.parseLong(kvTable.getTable().get("queryThreadPoolQueueSize"));
        this.bootTimestamp = Long.parseLong(kvTable.getTable().get("bootTimestamp"));
        this.msgPutTotalYesterdayMorning = Long.parseLong(kvTable.getTable().get("msgPutTotalYesterdayMorning"));
        this.msgGetTotalYesterdayMorning = Long.parseLong(kvTable.getTable().get("msgGetTotalYesterdayMorning"));
        this.pullThreadPoolQueueSize = Long.parseLong(kvTable.getTable().get("pullThreadPoolQueueSize"));
        this.commitLogMinOffset = Long.parseLong(kvTable.getTable().get("commitLogMinOffset"));
        this.pullThreadPoolQueueHeadWaitTimeMills = Long.parseLong(kvTable.getTable().get("pullThreadPoolQueueHeadWaitTimeMills"));
        this.runtime = kvTable.getTable().get("runtime");
        this.dispatchMaxBuffer = Long.parseLong(kvTable.getTable().get("dispatchMaxBuffer"));
        this.brokerVersion = Integer.parseInt(kvTable.getTable().get("brokerVersion"));
        this.consumeQueueDiskRatio = Double.parseDouble(kvTable.getTable().get("consumeQueueDiskRatio"));
        this.pageCacheLockTimeMills = Long.parseLong(kvTable.getTable().get("pageCacheLockTimeMills"));
        this.commitLogDiskRatio = Double.parseDouble(kvTable.getTable().get("commitLogDiskRatio"));
        this.commitLogMaxOffset = Long.parseLong(kvTable.getTable().get("commitLogMaxOffset"));
        this.getMessageEntireTimeMax = Long.parseLong(kvTable.getTable().get("getMessageEntireTimeMax"));
        this.msgPutTotalTodayMorning = Long.parseLong(kvTable.getTable().get("msgPutTotalTodayMorning"));
        this.putMessageTimesTotal = Long.parseLong(kvTable.getTable().get("putMessageTimesTotal"));
        this.msgGetTotalTodayMorning = Long.parseLong(kvTable.getTable().get("msgGetTotalTodayMorning"));
        this.brokerVersionDesc = kvTable.getTable().get("brokerVersionDesc");
        this.sendThreadPoolQueueSize = Long.parseLong(kvTable.getTable().get("sendThreadPoolQueueSize"));
        this.startAcceptSendRequestTimeStamp = Long.parseLong(kvTable.getTable().get("startAcceptSendRequestTimeStamp"));
        this.putMessageEntireTimeMax = Long.parseLong(kvTable.getTable().get("putMessageEntireTimeMax"));
        this.earliestMessageTimeStamp = Long.parseLong(kvTable.getTable().get("earliestMessageTimeStamp"));
        this.remainTransientStoreBufferNumbs = Long.parseLong(kvTable.getTable().get("remainTransientStoreBufferNumbs"));
        this.queryThreadPoolQueueCapacity = Long.parseLong(kvTable.getTable().get("queryThreadPoolQueueCapacity"));
        this.putMessageAverageSize = Double.parseDouble(kvTable.getTable().get("putMessageAverageSize"));
        this.dispatchBehindBytes = Long.parseLong(kvTable.getTable().get("dispatchBehindBytes"));
        this.putMessageSizeTotal = Long.parseLong(kvTable.getTable().get("putMessageSizeTotal"));
        this.sendThreadPoolQueueCapacity = Long.parseLong(kvTable.getTable().get("sendThreadPoolQueueCapacity"));
        this.pullThreadPoolQueueCapacity = Long.parseLong(kvTable.getTable().get("pullThreadPoolQueueCapacity"));
        this.putLatency99 = Double.parseDouble(kvTable.getTable().getOrDefault("putLatency99", "-1"));
        this.putLatency999 = Double.parseDouble(kvTable.getTable().getOrDefault("putLatency999", "-1"));

    }

    private void loadCommitLogDirCapacity(String commitLogDirCapacity) {
        String[] arr = commitLogDirCapacity.split(" ");
        String total = String.format("%s %s", arr[2], arr[3].substring(0, arr[3].length() - 1));
        String free = String.format("%s %s", arr[6], arr[7].substring(0, arr[7].length() - 1));
        this.commitLogDirCapacityTotal = Utils.machineReadableByteCount(total);
        this.commitLogDirCapacityFree = Utils.machineReadableByteCount(free);
    }

    private void loadTps(PutTps putTps, String value) {
        if (StringUtils.isNotBlank(value)) {
            String[] arr = value.split(" ");
            if (arr.length >= 1) {
                putTps.ten = Double.parseDouble(arr[0]);
            }
            if (arr.length >= 2) {
                putTps.sixty = Double.parseDouble(arr[1]);
            }
            if (arr.length >= 3) {
                putTps.sixHundred = Double.parseDouble(arr[2]);
            }
        }
    }

    private void loadPutMessageDistributeTime(String str) {
        if ("null".equalsIgnoreCase(str)) {
            log.warn("loadPutMessageDistributeTime WARN, value is null");
            return;
        }
        String[] arr = str.split(" ");
        String key = "", value = "";
        for (String ar : arr) {
            String[] tarr = ar.split(":");
            if (tarr.length < 2) {
                log.warn("loadPutMessageDistributeTime WARN, wrong value is {}, {}", ar, str);
                continue;
            }
            key = tarr[0].replace("[", "").replace("]", "");
            value = tarr[1];
            this.putMessageDistributeTimeMap.put(key, Integer.parseInt(value));
        }
    }

    public void loadScheduleMessageOffsets(KVTable kvTable) {
        for (String key : kvTable.getTable().keySet()) {
            if (key.startsWith("scheduleMessageOffset")) {
                String[] arr = kvTable.getTable().get(key).split(",");
                ScheduleMessageOffsetTable table = new ScheduleMessageOffsetTable(
                        Long.parseLong(arr[0]),
                        Long.parseLong(arr[1])
                );
                this.scheduleMessageOffsetTables.add(table);
            }
        }
    }

    @Data
    public static class ScheduleMessageOffsetTable {
        private long delayOffset;
        private long maxOffset;

        public ScheduleMessageOffsetTable(long first, long second) {
            this.delayOffset = first;
            this.maxOffset = second;
        }

    }

    @Data
    public class PutTps {
        private double ten;
        private double sixty;
        private double sixHundred;

    }

    public class GetMissTps extends PutTps {
    }

    public class GetTransferedTps extends PutTps {
    }

    public class GetTotalTps extends PutTps {
    }

    public class GetFoundTps extends PutTps {
    }

}
