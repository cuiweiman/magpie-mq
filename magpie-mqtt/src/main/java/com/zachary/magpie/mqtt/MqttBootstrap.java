package com.zachary.magpie.mqtt;

import com.zachary.magpie.mqtt.initializer.MqttBootInitializer;
import com.zachary.magpie.mqtt.utils.SystemUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: Mqtt Broker 启动器
 * @author: cuiweiman
 * @date: 2024/2/29 10:54
 */
@Slf4j
public class MqttBootstrap {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final Class<? extends ServerSocketChannel> serverSocketChannel;
    private final Integer port;

    private ChannelFuture tcpChannelFuture;

    public MqttBootstrap() {
        if (Epoll.isAvailable()) {
            this.bossGroup = new EpollEventLoopGroup(1);
            this.workerGroup = new EpollEventLoopGroup();
            this.serverSocketChannel = EpollServerSocketChannel.class;
        } else {
            this.bossGroup = new NioEventLoopGroup(1);
            this.workerGroup = new NioEventLoopGroup();
            this.serverSocketChannel = NioServerSocketChannel.class;
        }
        this.port = 8883;
    }

    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(this.bossGroup, this.workerGroup)
                // Netty Channel 通道 配置
                .channel(this.serverSocketChannel)
                // TCP 连接个数: 半连接(三次握手尚未完成)的数量 + 全连接(完成三次握手)的数量
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 监控TCP客户端连接状态，连接空闲超过2小时后，服务端会发送探测包，若12分钟内得不到响应，主动剔除客户端连接
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // 关闭Nagle算法，Nagle算法会延迟小数据包的发送，直到小数据包组装成更大的数据帧后才发送。
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new MqttBootInitializer());

        tcpChannelFuture = serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                log.info("MqttBootstrap 启动成功 PID {}, 端口 {}", SystemUtil.getPid(), port);
            } else {
                log.error("MqttBootstrap 启动失败", future.cause());
                System.exit(-1);
            }
        });
    }

    public void shutdown() {
        tcpChannelFuture.channel().close().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
        bossGroup.shutdownGracefully().syncUninterruptibly();
    }

}
