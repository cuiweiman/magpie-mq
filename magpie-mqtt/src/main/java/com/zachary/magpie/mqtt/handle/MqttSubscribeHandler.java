package com.zachary.magpie.mqtt.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户端发起 {@link MqttMessageType#SUBSCRIBE}，服务端响应 {@link MqttMessageType#SUBACK} 进行确认
 * SUBSCRIBE 包含了一系列的要订阅的主题以及QOS。
 *
 * @description: 客户端订阅主题处理器。
 * @author: cuiweiman
 * @date: 2024/3/4 11:04
 */
public class MqttSubscribeHandler implements IMqttInboundHandler {

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttSubscribeMessage subscribeMessage = (MqttSubscribeMessage) mqttMessage;
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        MqttQoS mqttQoS = subscribeMessage.fixedHeader().qosLevel();
        MqttFixedHeader ackFixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, mqttQoS, false, 0);
        MqttMessageIdVariableHeader ackVariableHeader = MqttMessageIdVariableHeader.from(variableHeader.messageId());
        List<Integer> grantedQosLevel = subscribeMessage.payload().topicSubscriptions().stream().map(topicSub -> topicSub.qualityOfService().value()).collect(Collectors.toList());
        MqttSubAckPayload ackSubscribePayload = new MqttSubAckPayload(grantedQosLevel);
        MqttSubAckMessage subAckMessage = new MqttSubAckMessage(ackFixedHeader, ackVariableHeader, ackSubscribePayload);
        ctx.writeAndFlush(subAckMessage);
        System.out.printf("%s 订阅了Topic %s%n",
                ctx.channel().remoteAddress().toString(),
                subscribeMessage.payload().topicSubscriptions().toString());
    }
}
