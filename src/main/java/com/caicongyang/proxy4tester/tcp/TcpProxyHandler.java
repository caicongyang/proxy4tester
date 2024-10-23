package com.caicongyang.proxy4tester.tcp;

import com.caicongyang.proxy4tester.entity.TcpRouteDefinition;
import com.caicongyang.proxy4tester.service.TcpRouteDefinitionService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * TcpProxyHandler 类负责处理 TCP 连接的代理逻辑。
 * 它可以根据配置直接响应或转发请求到远程服务器。
 */
public class TcpProxyHandler extends ChannelInboundHandlerAdapter {

    // 创建日志记录器
    private static final Logger logger = LoggerFactory.getLogger(TcpProxyHandler.class);

    // 用于获取路由定义的服务
    private final TcpRouteDefinitionService routeDefinitionService;
    // 本地监听端口
    private final int localPort;
    // 用于与远程服务器通信的通道
    private Channel outboundChannel;

    /**
     * 构造函数
     * @param routeDefinitionService 路由定义服务
     * @param localPort 本地监听端口
     */
    public TcpProxyHandler(TcpRouteDefinitionService routeDefinitionService, int localPort) {
        this.routeDefinitionService = routeDefinitionService;
        this.localPort = localPort;
    }

    /**
     * 当通道接收到数据时调用此方法
     * @param ctx 通道处理上下文
     * @param msg 接收到的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 将消息转换为 ByteBuf
        ByteBuf inBuffer = (ByteBuf) msg;
        
        // 根据本地端口获取路由定义
        TcpRouteDefinition route = routeDefinitionService.getRouteByPort(localPort);

        // 如果没有找到对应的路由定义，关闭连接
        if (route == null) {
            logger.error("No route found for port {}", localPort);
            ctx.close();
            return;
        }

        // 根据路由定义决定是直接响应还是转发请求
        if (route.getDirectResponse()) {
            handleDirectResponse(ctx, inBuffer, route);
        } else {
            forwardToRemoteServer(ctx, inBuffer, route);
        }
    }

    /**
     * 处理直接响应的情况
     * @param ctx 通道处理上下文
     * @param inBuffer 输入缓冲区
     * @param route 路由定义
     */
    private void handleDirectResponse(ChannelHandlerContext ctx, ByteBuf inBuffer, TcpRouteDefinition route) {
        // 获取模拟响应
        String response = route.getMockResponse();
        // 将响应写入通道并关闭连接
        ctx.writeAndFlush(Unpooled.copiedBuffer(response, Charset.defaultCharset()))
           .addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 将请求转发到远程服务器
     * @param ctx 通道处理上下文
     * @param msg 要转发的消息
     * @param route 路由定义
     */
    private void forwardToRemoteServer(ChannelHandlerContext ctx, ByteBuf msg, TcpRouteDefinition route) {
        // 如果出站通道不存在或不活跃，创建新的连接
        if (outboundChannel == null || !outboundChannel.isActive()) {
            // 创建新的 Bootstrap 用于客户端连接
            Bootstrap b = new Bootstrap();
            b.group(ctx.channel().eventLoop())
             .channel(ctx.channel().getClass())
             .handler(new ChannelInboundHandlerAdapter() {
                 @Override
                 public void channelRead(ChannelHandlerContext ctx, Object msg) {
                     // 将从远程服务器接收到的数据写回客户端
                     ctx.writeAndFlush(msg);
                 }
             });

            // 连接到远程服务器
            ChannelFuture f = b.connect(route.getRemoteHost(), route.getRemotePort());
            outboundChannel = f.channel();
            f.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // 连接成功，将数据发送到远程服务器
                    outboundChannel.writeAndFlush(msg);
                } else {
                    // 连接失败，关闭客户端连接
                    ctx.close();
                }
            });
        } else {
            // 如果出站通道已存在且活跃，直接发送数据
            outboundChannel.writeAndFlush(msg);
        }
    }

    /**
     * 当通道变为非活跃状态时调用此方法
     * @param ctx 通道处理上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    /**
     * 当发生异常时调用此方法
     * @param ctx 通道处理上下文
     * @param cause 异常原因
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught in TcpProxyHandler", cause);
        closeOnFlush(ctx.channel());
    }

    /**
     * 刷新并关闭通道
     * @param ch 要关闭的通道
     */
    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
