package com.zachary.magpie.mqtt.handle;

import com.zachary.magpie.mqtt.utils.IdWorker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务器不应依赖客户端在收到 DISCONNECT 后关闭 TCP/IP 连接。
 * <p>
 * {@link MqttMessageType#CONNECT} 连接中的 Clean Session flag 清理会话标志为 true 时会发起。
 *
 * @description: 客户端断开连接，通知服务端清除 Session
 * @author: cuiweiman
 * @date: 2024/3/4 15:09
 */
@Slf4j
public class MqttDisconnectHandler implements IMqttInboundHandler {

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
        if (ctx.channel().isActive()) {
            ctx.channel().close().syncUninterruptibly();
            log.debug("Mqtt channel closed {}", ctx.channel().id());
        }
        System.out.printf("%s 断开MQTT连接%n", ctx.channel().remoteAddress().toString());
    }
}
