package com.caicongyang.proxy4tester.filter;

import com.caicongyang.proxy4tester.entity.RouteDefinition;
import com.caicongyang.proxy4tester.service.RouteDefinitionService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CustomResponseFilter 类是一个自定义的 Spring Cloud Gateway 过滤器。
 * 它继承自 AbstractGatewayFilterFactory，用于处理进入网关的请求。
 * 这个过滤器的主要功能是根据路由定义来决定是直接返回响应还是转发请求。
 */
@Component
public class CustomResponseFilter extends AbstractGatewayFilterFactory<CustomResponseFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(CustomResponseFilter.class);
    private final RouteDefinitionService routeDefinitionService;

    /**
     * 构造函数，注入 RouteDefinitionService。
     * @param routeDefinitionService 用于获取路由定义的服务
     */
    public CustomResponseFilter(RouteDefinitionService routeDefinitionService) {
        super(Config.class);
        this.routeDefinitionService = routeDefinitionService;
    }

    /**
     * 应用过滤器的主要方法。
     * @param config 过滤器配置
     * @return GatewayFilter 实例
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 从 exchange 中获取当前路由
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            if (route != null) {
                String routeId = route.getId();
                logger.info("Processing request for route: {}", routeId);

                // 根据路由 ID 获取路由定义
                RouteDefinition routeDef = routeDefinitionService.getRouteByRouteId(routeId);
                if (routeDef != null) {
                    logger.info("Route definition found: {}", routeDef);
                    if (Boolean.TRUE.equals(routeDef.getDirectResponse())) {
                        // 如果配置为直接响应，则构造并返回响应
                        logger.info("Direct response for route: {}", routeId);
                        return handleDirectResponse(exchange, routeDef);
                    } else {
                        // 否则，转发请求并修改请求头
                        logger.info("Forwarding request for route: {}", routeId);
                        return handleForwardRequest(exchange, chain, routeDef);
                    }
                } else {
                    logger.warn("Route definition not found for routeId: {}", routeId);
                }
            } else {
                logger.warn("No route found in exchange attributes");
            }

            // 如果没有匹配的路由定义，继续过滤器链
            return chain.filter(exchange);
        };
    }

    /**
     * 处理直接响应的情况。
     * @param exchange ServerWebExchange 实例
     * @param routeDef 路由定义
     * @return Mono<Void> 表示响应的完成
     */
    private Mono<Void> handleDirectResponse(ServerWebExchange exchange, RouteDefinition routeDef) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(org.springframework.http.MediaType.valueOf(routeDef.getResponseContentType()));
        response.setStatusCode(org.springframework.http.HttpStatus.valueOf(routeDef.getResponseStatusCode()));
        byte[] bytes = routeDef.getResponseBody().getBytes();
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 处理转发请求的情况。
     * @param exchange ServerWebExchange 实例
     * @param chain GatewayFilterChain 实例
     * @param routeDef 路由定义
     * @return Mono<Void> 表示请求处理的完成
     */
    private Mono<Void> handleForwardRequest(ServerWebExchange exchange, GatewayFilterChain chain, RouteDefinition routeDef) {
        // 重写路径
        String path = exchange.getRequest().getURI().getRawPath();
        String newPath = path.replaceFirst(routeDef.getPath().replace("/**", ""), "");
        ServerHttpRequest request = exchange.getRequest().mutate()
            .path(newPath)
            .header("X-Proxy-By", "proxy4tester")
            .build();
        
        ServerWebExchange finalExchange = exchange.mutate().request(request).build();

        return chain.filter(finalExchange)
            .then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = finalExchange.getResponse();
                response.getHeaders().add("X-Proxied-By", "proxy4tester");
            }));
    }

    /**
     * 配置类，用于存储过滤器的配置信息。
     * 目前为空，但可以在将来添加配置属性。
     */
    public static class Config {
        // 配置属性（如果需要）
    }
}
