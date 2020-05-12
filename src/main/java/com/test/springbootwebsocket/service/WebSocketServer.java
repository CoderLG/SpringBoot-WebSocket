package com.test.springbootwebsocket.service;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.stereotype.Component;


import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/websocket/{uid}")
@Component
public class WebSocketServer {
    static Log log = LogFactory.getLog(WebSocketServer.class);
    /**
     * 记录当前在线连接数
     */
    private static int onlineCount = 0;
    /**
     * 存放每个客户端对应WebSocket对象
     */
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     **/
    private Session session;
    private String uid = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uid") String uid) {
        this.session = session;
        webSocketSet.add(this);
        addOnlineCount();
        log.info("有新窗口开始监听" + uid + ",当前人数为" + getOnlineCount());
        this.uid = uid;
        try {
            sendMessage("连接成功");
        } catch (Exception e) {
            log.error("WebSocket IO异常");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        subOnlineCount();
        log.info("连接关闭！,当前人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到来自窗口" + uid + "的消息：" + message);
        //群发消息
        for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 服务器推动消息
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message, @PathParam("uid") String uid) throws IOException {
        log.info("推送消息到窗口" + uid + "，推送内容为" + message);
        for (WebSocketServer item : webSocketSet) {
            try {
                //uid==null，则对所有窗口进行推送
                if (uid.equals(" ")) {
                    item.sendMessage(message);
                } else if (item.uid.equals(uid)) {
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}
