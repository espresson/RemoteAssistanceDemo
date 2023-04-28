package com.assistance.client.websocket;

/**
 * Author: HuangYuGuang
 * Date: 2022/12/19
 */
public interface OnSocketMessage {

    void onMessage(String message);

    void onMessage(byte[] bytes);
}
