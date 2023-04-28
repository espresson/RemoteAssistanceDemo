package com.assistance.client.data.bean;

/**
 * Author: HuangYuGuang
 * Date: 2022/12/7
 */
public class DeviceHost {
    private String hostName;
    private String ip;

    public DeviceHost() {
    }

    public DeviceHost(String hostName, String ip) {
        this.hostName = hostName;
        this.ip = ip;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
