package com.zachary.magpie.mqtt.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubReplyMessageVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * @description: QoS = 2时,收到 客户端 发送的 {@link MqttMessageType#PUBREL}, 响应  {@link MqttMessageType#PUBCOMP}
 * @author: cuiweiman
 * @date: 2024/3/4 11:04
 * @see MqttPublishHandler 介绍
 */
public class MqttPubRelHandler implements IMqttInboundHandler {

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttPubReplyMessageVariableHeader variableHeader = (MqttPubReplyMessageVariableHeader) mqttMessage.variableHeader();
        MqttFixedHeader ackFixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.EXACTLY_ONCE, false, 0);
        MqttMessageIdVariableHeader ackVariableHeader = MqttMessageIdVariableHeader.from(variableHeader.messageId());
        MqttMessage pubComp = new MqttMessage(ackFixedHeader, ackVariableHeader);
        ctx.writeAndFlush(pubComp);
        System.out.printf("%s 完成 %s -> PUBCOMP 响应%n", ctx.channel().remoteAddress().toString(), mqttMessage.fixedHeader().messageType());
    }
}

