package com.zachary.magpie.mqtt.initializer;

import com.zachary.magpie.mqtt.dispatcher.MqttMsgDispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;

import java.util.Optional;

/**
 * @description: 客户端接入事件钩子，消息回调
 * @author: cuiweiman
 * @date: 2024/2/29 14:18
 */
public class MqttBootCallback extends SimpleChannelInboundHandler<MqttMessage> {

    public static final String HANDLER_NAME = "客户端钩子回调器";

    public MqttBootCallback() {
    }

    /**
     * 客户端 TCP 连接完成，通道可用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.printf("客户端 %s 成功建立TCP连接%n", ctx.channel().remoteAddress().toString());
    }


    /**
     * 客户端断开连接时触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.printf("客户端 %s 断开连接%n", ctx.channel().remoteAddress().toString());
    }

    /**
     * 客户端有消息时触发
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) {
        if (msg.decoderResult().isFailure()) {
            System.out.printf("MqttMessage 解码错误 %s", msg.decoderResult().cause());
            return;
        }
        MqttMessageType mqttMessageType = msg.fixedHeader().messageType();
        Optional.ofNullable(MqttMsgDispatcher.getHandler(mqttMessageType)).ifPresent(handler -> handler.process(ctx, msg));
    }


    /**
     * 客户端连接异常时触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.out.printf("客户端 %s 连接异常: %s%n", ctx.channel().remoteAddress().toString(), cause.toString());
    }

    /**
     * IdleStateHandler 超时触发
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        System.out.printf("服务端 %s 读超时: %s%n", ctx.channel().remoteAddress().toString(), evt.toString());
    }


}
