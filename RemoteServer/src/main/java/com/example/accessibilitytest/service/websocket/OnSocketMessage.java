package com.example.accessibilitytest.service.websocket;

import org.java_websocket.WebSocket;

/**
 * Author: HuangYuGuang
 * Date: 2022/12/19
 */
public abstract class OnSocketMessage {

    public void onMessage(WebSocket conn, String message){};

    public void onMessage(WebSocket conn, byte[] bytes){};
}
