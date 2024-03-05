package com.zachary.magpie.mqtt.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * 客户端发起 {@link MqttMessageType#CONNECT} 服务端响应 {@link MqttMessageType#CONNACK}
 * 建立客户端的TCP连接通道，
 * <p>
 * CONNECTION_ACCEPTED((byte) 0x00), 连接已接受
 * //MQTT 3 codes
 * CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION((byte) 0X01), 不接受协议版本
 * CONNECTION_REFUSED_IDENTIFIER_REJECTED((byte) 0x02), 标识符被拒绝
 * CONNECTION_REFUSED_SERVER_UNAVAILABLE((byte) 0x03), 服务器不可用
 * CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD((byte) 0x04), 用户名或密码错误
 * CONNECTION_REFUSED_NOT_AUTHORIZED((byte) 0x05), 未授权
 *
 * <p>
 * 主要是客户机的ClientID，订阅的Topic和Message以及用户名和密码，其于变长头部中的 will 是对应的
 * 客户端发起 {@link MqttMessageType#CONNECT} 服务端响应 {@link MqttMessageType#CONNACK}
 *
 * @description: 客户端连接处理器
 * @author: cuiweiman
 * @date: 2024/3/4 11:00
 */
public class MqttConnectHandler implements IMqttInboundHandler {

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttConnectMessage connectMessage = (MqttConnectMessage) mqttMessage;
        // 构建返回报文，固定报头
        MqttFixedHeader ackFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, connectMessage.fixedHeader().isDup(), MqttQoS.AT_LEAST_ONCE, connectMessage.fixedHeader().isRetain(), 0);
        // 构建返回报文，可变报头
        MqttConnAckVariableHeader ackVariableHeader = new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, connectMessage.variableHeader().isCleanSession());
        // 构建连接回复消息体，并响应 ack
        MqttConnAckMessage connAckMessage = new MqttConnAckMessage(ackFixedHeader, ackVariableHeader);
        ctx.writeAndFlush(connAckMessage);
        System.out.printf("%s 完成MQTT连接%n", ctx.channel().remoteAddress().toString());
    }
}
