package com.zachary.magpie.mqtt.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;

/**
 * 客户端发起 {@link MqttMessageType#UNSUBSCRIBE}，服务端响应 {@link MqttMessageType#UNSUBACK} 进行确认
 *
 * @description: 客户端 取消订阅 处理器。
 * @author: cuiweiman
 * @date: 2024/3/4 11:04
 */
public class MqttUnsubscribeHandler implements IMqttInboundHandler {

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        MqttFixedHeader ackFixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false,
                MqttQoS.AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader ackVariableHeader = MqttMessageIdVariableHeader.from(variableHeader.messageId());
        MqttUnsubAckMessage unsubscribeAckMessage = new MqttUnsubAckMessage(ackFixedHeader, ackVariableHeader);
        ctx.writeAndFlush(unsubscribeAckMessage);
        MqttUnsubscribeMessage unsubscribeMessage = (MqttUnsubscribeMessage) mqttMessage;
        System.out.printf("%s 取消订阅Topic %s%n",
                ctx.channel().remoteAddress().toString(),
                unsubscribeMessage.payload().topics().toString());
    }
}
