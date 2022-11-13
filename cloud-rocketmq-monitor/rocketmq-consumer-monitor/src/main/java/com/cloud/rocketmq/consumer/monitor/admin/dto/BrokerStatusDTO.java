package com.cloud.rocketmq.consumer.monitor.admin.dto;

import org.apache.rocketmq.common.protocol.body.KVTable;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BrokerStatusDTO {
    public BrokerStatusDTO(String brokerName, Long id, KVTable kvTable) {
        String putTps = kvTable.getTable().get("putTps");
        String getTransferedTps = kvTable.getTable().get("getTransferedTps");
        String diskRatio = kvTable.getTable().get("commitLogDiskRatio");
        String diskRatioDesc = kvTable.getTable().get("commitLogDirCapacity");
        double in = 0, out = 0;
        {
            String[] tpss = putTps.split(" ");
            if (tpss.length > 0) {
                in = Double.parseDouble(tpss[0]);
            }
        }
        {
            String[] tpss = getTransferedTps.split(" ");
            if (tpss.length > 0) {
                out = Double.parseDouble(tpss[0]);
            }
        }
        String inTps = BigDecimal.valueOf(in).setScale(2, RoundingMode.UP).toString();
        String outTps = BigDecimal.valueOf(out).setScale(2, RoundingMode.UP).toString();
        diskRatio = BigDecimal.valueOf(Double.parseDouble(diskRatio)).setScale(2, RoundingMode.UP).toString();

        this.brokerId = id;
        this.brokerName = brokerName;
        this.diskRatio = diskRatio;
        this.diskInfo = diskRatioDesc;
        this.putTps = inTps;
        this.getTps = outTps;
    }

    private String brokerName;
    private Long brokerId;
    private String diskRatio;
    private String diskInfo;
    private String putTps;
    private String getTps;

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public Long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Long brokerId) {
        this.brokerId = brokerId;
    }

    public String getDiskRatio() {
        return diskRatio;
    }

    public void setDiskRatio(String diskRatio) {
        this.diskRatio = diskRatio;
    }

    public String getDiskInfo() {
        return diskInfo;
    }

    public void setDiskInfo(String diskInfo) {
        this.diskInfo = diskInfo;
    }

    public String getPutTps() {
        return putTps;
    }

    public void setPutTps(String putTps) {
        this.putTps = putTps;
    }

    public String getGetTps() {
        return getTps;
    }

    public void setGetTps(String getTps) {
        this.getTps = getTps;
    }
}
