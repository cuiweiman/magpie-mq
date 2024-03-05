package com.zachary.magpie.mqtt.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * QoS=2 消息发送完成通知
 * 忽略鉴权和消息转发
 *
 * @description: QoS = 2时,收到 客户端 发送的 {@link MqttMessageType#PUBREL}, 响应  {@link MqttMessageType#PUBCOMP}
 * @author: cuiweiman
 * @date: 2024/3/4 19:58
 */
public class MqttPubCompHandler implements IMqttInboundHandler {

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttMessageIdVariableHeader replyVariableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.PUBCOMP, false,
                MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader variableHeaderBack = MqttMessageIdVariableHeader.from(replyVariableHeader.messageId());
        MqttMessage mqttMessageBack = new MqttMessage(mqttFixedHeaderBack, variableHeaderBack);
        ctx.writeAndFlush(mqttMessageBack);
        System.out.printf("%s 完成 %s -> PUBCOMP 响应%n", ctx.channel().remoteAddress().toString(), mqttMessage.fixedHeader().messageType());
    }

}
