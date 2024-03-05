package com.zachary.magpie.mqtt.handle;

import com.zachary.magpie.mqtt.utils.IdWorker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * 客户端发送消息 {@link MqttMessageType#PUBLISH} 服务端根据QOS响应不同动作
 * <p>
 * Fixed header: QoS level, DUP flag（0 表示该消息是第一次发送）, RETAIN flag 0表示不保留, Remaining Length field 可变标头的长度加上有效负载的长度
 * Variable header: Topic name, Message ID,
 * <p>
 * QoS level:
 * 0 最多一次,即发即忘,可以不响应, {@link io.netty.handler.codec.mqtt.MqttQoS#AT_MOST_ONCE}
 * 1 最少一次,确认交付,响应{@link MqttMessageType#PUBACK}, {@link io.netty.handler.codec.mqtt.MqttQoS#AT_LEAST_ONCE}
 * 2 正好一次,放心交付,响应{@link MqttMessageType#PUBREC}, {@link io.netty.handler.codec.mqtt.MqttQoS#EXACTLY_ONCE}
 * ...qos=2 时, C/S两端正反方向交互四次
 * 客户端发送: client-PUBLISH->server, server-PUBREC->client,client-PUBREL->server,server-PUBCOMP->client
 * 服务端发送: server-PUBLISH->client, client-PUBREC->client,server-PUBREL->client,client-PUBCOMP->client
 * <p>
 * (B为服务端需要返回的包类型)
 * ...Qos = 0 的客户端 C0：B 将该消息转发给 C0（不加确认是否收到）
 * ...Qos = 1 的客户端 C1：B 将该消息转发给 C1，C1 收到后向 B 发送确认 PUBACK，B 收到确认则将消息从队列中删除，否则确认超时重发；
 * C1 只要收到消息，就会将其转发给 C1 的应用，所以 C1 的应用可能收到重复的消息。
 * ...Qos = 2 的客户端 C2：B 将该消息转发给 C2，C2 收到后向 B 发送确认 PUBREC，B 收到确认后向 C2 发送确认收到确认 PUBREL；C2收到PUBREL后向 B 发送PUBCOMP发布完成包
 * C2 只有收到消息并发出 PUBREC 且收到对应的 PUBREL，才会将消息转发给 C2 的应用，所以 C2 的应用不会收到重复的消息
 * <p>
 * DUP flag 即 	Duplicate delivery 重复投递: 0 表示首次发送消息，没有重复发送
 * <p>
 * RETAIN flag:
 * 0 消息发送后忘记（不需要保存）
 * 1 服务端应保留该消息，当有其它客户端订阅时，服务端会把该消息转发给其它订阅者。
 *
 * @description: 客户端信息发布处理器, 忽略权限和消息转发
 * @author: cuiweiman
 * @date: 2024/3/4 1 1:03
 */
public class MqttPublishHandler implements IMqttInboundHandler {

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        MqttPublishMessage publishMessage = (MqttPublishMessage) mqttMessage;
        MqttQoS mqttQoS = publishMessage.fixedHeader().qosLevel();
        switch (mqttQoS) {
            case AT_MOST_ONCE -> this.atMostOnce(ctx, publishMessage);
            case AT_LEAST_ONCE -> this.atLeastOnce(ctx, publishMessage);
            case EXACTLY_ONCE -> this.exactlyOnce(ctx, publishMessage);
        }
    }

    private void atMostOnce(ChannelHandlerContext ctx, MqttPublishMessage publishMessage) {
        // 将消息发送给所有订阅者
        MqttFixedHeader ackFixedHeader = new MqttFixedHeader(
                MqttMessageType.PUBACK,
                publishMessage.fixedHeader().isDup(),
                MqttQoS.AT_MOST_ONCE,
                publishMessage.fixedHeader().isRetain(),
                0);
        MqttMessageIdVariableHeader ackVariableHeader = MqttMessageIdVariableHeader.from(IdWorker.nextIntId());
        MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(ackFixedHeader, ackVariableHeader);
        ctx.writeAndFlush(mqttPubAckMessage);
        System.out.printf("%s 完成 %s -> PUBACK 响应%n", ctx.channel().remoteAddress().toString(), publishMessage.fixedHeader().messageType());
    }

    private void atLeastOnce(ChannelHandlerContext ctx, MqttPublishMessage publishMessage) {
        // 将消息记录到持久存储中，转发给所有订阅者，并向发送者返回 PUBACK 消息
        MqttFixedHeader ackFixedHeader = new MqttFixedHeader(
                MqttMessageType.PUBACK,
                publishMessage.fixedHeader().isDup(),
                MqttQoS.AT_LEAST_ONCE,
                publishMessage.fixedHeader().isRetain(),
                0);
        MqttMessageIdVariableHeader ackVariableHeader = MqttMessageIdVariableHeader.from(IdWorker.nextIntId());
        MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(ackFixedHeader, ackVariableHeader);
        ctx.writeAndFlush(mqttPubAckMessage);
        System.out.printf("%s 完成 %s -> PUBACK 响应%n", ctx.channel().remoteAddress().toString(), publishMessage.fixedHeader().messageType());
    }

    private void exactlyOnce(ChannelHandlerContext ctx, MqttPublishMessage publishMessage) {
        MqttFixedHeader ackFixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC,
                publishMessage.fixedHeader().isDup(),
                MqttQoS.EXACTLY_ONCE,
                publishMessage.fixedHeader().isRetain(),
                0);
        MqttMessageIdVariableHeader ackVariableHeader = MqttMessageIdVariableHeader.from(IdWorker.nextIntId());
        MqttPubAckMessage mqttPubAckMessage = new MqttPubAckMessage(ackFixedHeader, ackVariableHeader);
        ctx.writeAndFlush(mqttPubAckMessage);
        System.out.printf("%s 完成 %s -> PUBREC 响应%n", ctx.channel().remoteAddress().toString(), publishMessage.fixedHeader().messageType());
    }
}
