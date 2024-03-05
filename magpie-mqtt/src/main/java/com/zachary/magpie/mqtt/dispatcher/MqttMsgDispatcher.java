package com.zachary.magpie.mqtt.dispatcher;

import com.zachary.magpie.mqtt.handle.IMqttInboundHandler;
import com.zachary.magpie.mqtt.handle.MqttConnectHandler;
import com.zachary.magpie.mqtt.handle.MqttDisconnectHandler;
import com.zachary.magpie.mqtt.handle.MqttPingReqHandler;
import com.zachary.magpie.mqtt.handle.MqttPubCompHandler;
import com.zachary.magpie.mqtt.handle.MqttPubRecHandler;
import com.zachary.magpie.mqtt.handle.MqttPubRelHandler;
import com.zachary.magpie.mqtt.handle.MqttPublishHandler;
import com.zachary.magpie.mqtt.handle.MqttSubscribeHandler;
import com.zachary.magpie.mqtt.handle.MqttUnsubscribeHandler;
import io.netty.handler.codec.mqtt.MqttMessageType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: MQTT 事件分发器
 * @author: cuiweiman
 * @date: 2024/3/1 18:24
 */
public class MqttMsgDispatcher {

    private static final Map<MqttMessageType, IMqttInboundHandler> HANDLER_MAP = new ConcurrentHashMap<>();

    static {
        // Mqtt 消息类型注册
        // 静态代码块的位置会影响类的初始化执行顺序
        register(MqttMessageType.CONNECT, new MqttConnectHandler());
        register(MqttMessageType.DISCONNECT, new MqttDisconnectHandler());
        register(MqttMessageType.PINGREQ, new MqttPingReqHandler());
        register(MqttMessageType.PUBLISH, new MqttPublishHandler());
        register(MqttMessageType.PUBREC, new MqttPubRecHandler());
        register(MqttMessageType.PUBREL, new MqttPubRelHandler());
        register(MqttMessageType.PUBCOMP, new MqttPubCompHandler());
        register(MqttMessageType.SUBSCRIBE, new MqttSubscribeHandler());
        register(MqttMessageType.UNSUBSCRIBE, new MqttUnsubscribeHandler());
    }

    private static void register(MqttMessageType mqttMessageType, IMqttInboundHandler iMqttInboundHandler) {
        HANDLER_MAP.put(mqttMessageType, iMqttInboundHandler);
    }

    public static IMqttInboundHandler getHandler(MqttMessageType mqttMessageType) {
        return HANDLER_MAP.get(mqttMessageType);
    }

    private MqttMsgDispatcher() {
    }
}
