package com.zachary.magpie.mqtt.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * 客户端发起心跳 {@link MqttMessageType#PINGREQ} 服务端需要即时响应 {@link MqttMessageType#PINGRESP}
 * <p>
 * {@link MqttMessageType#CONNECT} 连接中 需要配置 Keep Alive timer
 *
 * @description: 客户端心跳处理器
 * @author: cuiweiman
 * @date: 2024/3/4 11:04
 */
public class MqttPingReqHandler implements IMqttInboundHandler {

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        // 构建返回报文，固定报头
        MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.PINGRESP, false,
                MqttQoS.AT_LEAST_ONCE, false, 0);
        // 构建连接回复消息体，并响应 ack
        MqttMessage pingResp = new MqttMessage(mqttFixedHeaderBack);
        ctx.writeAndFlush(pingResp);
        System.out.printf("%s 完成心跳%n", ctx.channel().remoteAddress().toString());
    }
}
