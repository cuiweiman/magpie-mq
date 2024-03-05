package com.zachary.magpie.mqtt.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 * @description: inbound message handler
 * @author: cuiweiman
 * @date: 2024/3/1 18:39
 */
public interface IMqttInboundHandler {

    /**
     * 服务端需要处理的消息类别共10种：
     * <ol>
     *     <li>{@link MqttMessageType#CONNECT}</li>
     *     <li>{@link MqttMessageType#PUBLISH}</li>
     *     <li>{@link MqttMessageType#PUBACK}</li>
     *     <li>{@link MqttMessageType#PUBREC}</li>
     *     <li>{@link MqttMessageType#PUBREL}</li>
     *     <li>{@link MqttMessageType#PUBCOMP}</li>
     *     <li>{@link MqttMessageType#SUBSCRIBE}</li>
     *     <li>{@link MqttMessageType#UNSUBSCRIBE}</li>
     *     <li>{@link MqttMessageType#PINGREQ}</li>
     *     <li>{@link MqttMessageType#DISCONNECT}</li>
     * </ol>
     */
    void process(ChannelHandlerContext ctx, MqttMessage mqttMessage);

}
