package com.example.accessibilitytest.data.event;

/**
 * Author: HuangYuGuang
 * Date: 2022/12/2
 */
public class NettyInitEvent {

    private String msg;

    public NettyInitEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
