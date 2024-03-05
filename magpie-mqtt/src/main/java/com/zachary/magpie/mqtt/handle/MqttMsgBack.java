package com.zachary.magpie.mqtt.handle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: cuiweiman
 * @date: 2024/2/29 14:39
 */
public class MqttMsgBack {

    public static final MqttMsgBack INSTANCE = new MqttMsgBack();

    private static final AtomicInteger PUB_MSG_ID = new AtomicInteger(1000);

    private MqttMsgBack() {
    }

    /**
     * 客户端连接 服务端响应
     */
    public void connectionAck(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttConnectMessage mqttConnectMessage = (MqttConnectMessage) mqttMessage;
        MqttFixedHeader mqttFixedHeader = mqttConnectMessage.fixedHeader();
        MqttConnectVariableHeader mqttVariableHeader = mqttConnectMessage.variableHeader();
        // 构建返回报文，可变报头
        MqttConnAckVariableHeader mqttVariableHeaderBack = new MqttConnAckVariableHeader(
                MqttConnectReturnCode.CONNECTION_ACCEPTED, mqttVariableHeader.isCleanSession());
        // 构建返回报文，固定报头
        MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.CONNACK, mqttFixedHeader.isDup(),
                MqttQoS.AT_MOST_ONCE, mqttFixedHeader.isRetain(), 0x02);
        // 构建连接回复消息体，并响应 ack
        MqttConnAckMessage connAckMessage = new MqttConnAckMessage(mqttFixedHeaderBack, mqttVariableHeaderBack);
        ctx.writeAndFlush(connAckMessage);

        /*MqttConnAckMessage ackMessage = MqttMessageBuilders.connAck().returnCode(MqttConnectReturnCode.CONNECTION_ACCEPTED).build();
        ctx.channel().writeAndFlush(ackMessage);*/

        // 获取连接的 ClientID，判重
        // String clientIdentifier = mqttConnectMessage.payload().clientIdentifier();
    }

    /**
     * 客户端消息发布 服务端响应
     */
    public void publishAck(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttPublishMessage mqttPublishMessage = (MqttPublishMessage) mqttMessage;
        MqttFixedHeader fixedHeader = mqttPublishMessage.fixedHeader();
        MqttPublishVariableHeader variableHeader = mqttPublishMessage.variableHeader();
        // 得到 QOS
        MqttQoS qos = fixedHeader.qosLevel();
        // 得到 Topic
        String topic = variableHeader.topicName();
        // 得到 PacketId
        int packetId = variableHeader.packetId();
        boolean dup = fixedHeader.isDup();
        boolean retain = fixedHeader.isRetain();
        // 得到消息体
        ByteBuf payload = mqttPublishMessage.payload();
        String payloadStr = ByteBufUtil.hexDump(payload);
        System.out.printf(">>> topic %s, payloadHex %s%n>>> payloadStr %s%n", topic, payloadStr, payload.toString(StandardCharsets.UTF_8));

        // 构建返回报文，可变报头
        MqttMessageIdVariableHeader variableHeaderBack;
        // 构建返回报文，固定报头
        MqttFixedHeader fixedHeaderBack;
        // 构建返回报文，载荷
        MqttPubAckMessage mqttPubAckMessage;
        switch (qos) {
            // 最多一次
            case AT_MOST_ONCE:
                // 构建返回报文，可变报头
                variableHeaderBack = MqttMessageIdVariableHeader.from(1);
                // 构建返回报文，固定报头
                fixedHeaderBack = new MqttFixedHeader(MqttMessageType.PUBACK, dup, MqttQoS.AT_MOST_ONCE, retain, 0);
                // 构建返回报文，载荷
                mqttPubAckMessage = new MqttPubAckMessage(fixedHeaderBack, variableHeaderBack);
                ctx.writeAndFlush(mqttPubAckMessage);
                break;
            // 最少一次
            case AT_LEAST_ONCE:
                variableHeaderBack = MqttMessageIdVariableHeader.from(PUB_MSG_ID.getAndIncrement());
                fixedHeaderBack = new MqttFixedHeader(MqttMessageType.PUBACK, dup, MqttQoS.AT_MOST_ONCE, retain, 0x02);
                mqttPubAckMessage = new MqttPubAckMessage(fixedHeaderBack, variableHeaderBack);
                ctx.writeAndFlush(mqttPubAckMessage);
                break;
            // 正好一次
            case EXACTLY_ONCE:
                variableHeaderBack = MqttMessageIdVariableHeader.from(PUB_MSG_ID.getAndIncrement());
                fixedHeaderBack = new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_LEAST_ONCE, false, 0x02);
                mqttPubAckMessage = new MqttPubAckMessage(fixedHeaderBack, variableHeaderBack);
                ctx.writeAndFlush(mqttPubAckMessage);
                break;
            default:
                break;
        }
    }

    /**
     * 客户端消息发布完成 服务端响应
     */
    public void publishComplete(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        // 构建返回报文，可变报头
        MqttMessageIdVariableHeader variableHeaderBack = MqttMessageIdVariableHeader.from(mqttMessageIdVariableHeader.messageId());
        // 构建返回报文，固定报头
        MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.PUBCOMP, false,
                MqttQoS.AT_MOST_ONCE, false, 0x02);
        // 构建连接回复消息体，并响应 ack
        MqttMessage mqttMessageBack = new MqttMessage(mqttFixedHeaderBack, variableHeaderBack);
        ctx.writeAndFlush(mqttMessageBack);
    }

    /**
     * 客户端订阅 服务端响应
     */
    public void subscribeAck(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttSubscribeMessage mqttSubscribeMessage = (MqttSubscribeMessage) mqttMessage.variableHeader();
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = mqttSubscribeMessage.variableHeader();
        // 构建返回报文，可变报头
        MqttMessageIdVariableHeader variableHeaderBack = MqttMessageIdVariableHeader.from(mqttMessageIdVariableHeader.messageId());
        int size = mqttSubscribeMessage.payload().topicSubscriptions().size();
        List<Integer> grantedQosLevel = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            grantedQosLevel.add(mqttSubscribeMessage.payload().topicSubscriptions().get(i).qualityOfService().value());
        }
        // 构建返回报文 载荷
        MqttSubAckPayload mqttSubAckPayload = new MqttSubAckPayload(grantedQosLevel);
        // 构建返回报文，固定报头
        MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.SUBACK, false,
                MqttQoS.AT_MOST_ONCE, false, 2 * size);
        // 构建连接回复消息体，并响应 ack
        MqttSubAckMessage subscribeAckMessage = new MqttSubAckMessage(mqttFixedHeaderBack, variableHeaderBack, mqttSubAckPayload);
        ctx.writeAndFlush(subscribeAckMessage);
    }

    /**
     * 客户端取消订阅 服务端响应
     */
    public void unsubscribeAck(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        // 构建返回报文，可变报头
        MqttMessageIdVariableHeader variableHeaderBack = MqttMessageIdVariableHeader.from(mqttMessageIdVariableHeader.messageId());
        // 构建返回报文，固定报头
        MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.UNSUBACK, false,
                MqttQoS.AT_MOST_ONCE, false, 0x02);
        // 构建连接回复消息体，并响应 ack
        MqttUnsubAckMessage unsubscribeAckMessage = new MqttUnsubAckMessage(mqttFixedHeaderBack, variableHeaderBack);
        ctx.writeAndFlush(unsubscribeAckMessage);
    }

    /**
     * 客户端发送心跳 服务端响应
     * 客户端配置的每隔 keep-alive 个秒，发送一次心跳请求，服务端需要及时响应
     */
    public void pingResp(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        // 构建返回报文，固定报头
        MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.PINGRESP, false,
                MqttQoS.AT_MOST_ONCE, false, 0);
        // 构建连接回复消息体，并响应 ack
        MqttMessage pingResp = new MqttMessage(mqttFixedHeaderBack);
        ctx.writeAndFlush(pingResp);
    }

}
