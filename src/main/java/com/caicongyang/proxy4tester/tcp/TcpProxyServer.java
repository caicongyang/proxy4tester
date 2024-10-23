package com.caicongyang.proxy4tester.tcp;

import com.caicongyang.proxy4tester.service.TcpRouteDefinitionService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TcpProxyServer 类负责启动和管理 TCP 代理服务器。
 * 它使用 Netty 框架来处理 TCP 连接。
 */
@Component
public class TcpProxyServer {

    // 用于获取路由定义的服务
    private final TcpRouteDefinitionService routeDefinitionService;
    // 用于接受新连接的事件循环组
    private final EventLoopGroup bossGroup;
    // 用于处理已接受连接的 I/O 操作的事件循环组
    private final EventLoopGroup workerGroup;
    // 服务器套接字接受队列的最大长度
    private final int backlog;
    // 是否保持长连接
    private final boolean keepAlive;
    // 存储所有服务器通道的线程安全列表
    private final List<Channel> serverChannels = new CopyOnWriteArrayList<>();

    /**
     * 构造函数
     * @param routeDefinitionService 路由定义服务
     * @param bossThreads boss 线程数
     * @param workerThreads worker 线程数
     * @param backlog 接受队列长度
     * @param keepAlive 是否保持长连接
     */
    public TcpProxyServer(TcpRouteDefinitionService routeDefinitionService,
                          @Value("${tcp.proxy.boss-threads:1}") int bossThreads,
                          @Value("${tcp.proxy.worker-threads:4}") int workerThreads,
                          @Value("${tcp.proxy.backlog:128}") int backlog,
                          @Value("${tcp.proxy.keep-alive:true}") boolean keepAlive) {
        this.routeDefinitionService = routeDefinitionService;
        this.bossGroup = new NioEventLoopGroup(bossThreads);
        this.workerGroup = new NioEventLoopGroup(workerThreads);
        this.backlog = backlog;
        this.keepAlive = keepAlive;
    }

    /**
     * 在 bean 初始化完成后自动调用此方法启动服务器
     */
    @PostConstruct
    public void start() {
        startServer();
    }

    /**
     * 启动服务器的具体实现
     */
    private void startServer() {
        // 获取所有启用的端口
        List<Integer> ports = routeDefinitionService.getAllEnabledPorts();
        // 创建服务器引导程序
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    // 获取本地端口并为每个连接创建一个新的 TcpProxyHandler
                    int localPort = ch.localAddress().getPort();
                    ch.pipeline().addLast(new TcpProxyHandler(routeDefinitionService, localPort));
                }
            })
            .option(ChannelOption.SO_BACKLOG, backlog)
            .childOption(ChannelOption.SO_KEEPALIVE, keepAlive);

        // 为每个端口绑定服务器
        for (int port : ports) {
            try {
                ChannelFuture f = b.bind(port).sync();
                serverChannels.add(f.channel());
                f.channel().closeFuture().addListener(future -> {
                    // 当服务器关闭时，可以在这里添加清理逻辑
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 重启服务器
     * @throws Exception 如果重启过程中发生错误
     */
    public void restart() throws Exception {
        stop();
        start();
    }

    /**
     * 在 bean 销毁前自动调用此方法停止服务器
     * @throws Exception 如果停止过程中发生错误
     */
    @PreDestroy
    public void stop() throws Exception {
        // 关闭所有服务器通道
        for (Channel ch : serverChannels) {
            ch.close().sync();
        }
        serverChannels.clear();
        // 优雅地关闭事件循环组
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
