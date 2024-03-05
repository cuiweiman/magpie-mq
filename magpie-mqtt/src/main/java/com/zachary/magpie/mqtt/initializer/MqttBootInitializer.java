package com.zachary.magpie.mqtt.initializer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @description: 处理器配置
 * @author: cuiweiman
 * @date: 2024/3/1 16:25
 */
public class MqttBootInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        /*
            readerIdleTimeSeconds：读超时,触发 IdleStateEvent#READER_IDLE 事件
            writerIdleTimeSeconds：写超时,触发 IdleStateEvent#WRITER_IDLE 事件
            allIdleTimeSeconds：读/写超时,触发 IdleStateEvent#ALL_IDLE 事件
        */
        socketChannel.pipeline().addLast(new IdleStateHandler(300, 300, 600, TimeUnit.SECONDS));
        // 编解码器
        socketChannel.pipeline().addLast("MqttEncoder", MqttEncoder.INSTANCE);
        socketChannel.pipeline().addLast("MqttDecoder", new MqttDecoder());
        // MQTT 协议 业务处理器
        socketChannel.pipeline().addLast(MqttBootCallback.HANDLER_NAME, new MqttBootCallback());
    }

}
