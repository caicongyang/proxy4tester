package com.caicongyang.proxy4tester.filter;

import com.caicongyang.proxy4tester.entity.RouteDefinition;
import com.caicongyang.proxy4tester.service.RouteDefinitionService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
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

@Component
public class CustomResponseFilter extends AbstractGatewayFilterFactory<CustomResponseFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(CustomResponseFilter.class);
    private final RouteDefinitionService routeDefinitionService;

    public CustomResponseFilter(RouteDefinitionService routeDefinitionService) {
        super(Config.class);
        this.routeDefinitionService = routeDefinitionService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            if (route != null) {
                String routeId = route.getId();
                logger.info("Processing request for route: {}", routeId);

                RouteDefinition routeDef = routeDefinitionService.getRouteByRouteId(routeId);
                if (routeDef != null) {
                    logger.info("Route definition found: {}", routeDef);
                    if (Boolean.TRUE.equals(routeDef.getDirectResponse())) {
                        logger.info("Direct response for route: {}", routeId);
                        ServerHttpResponse response = exchange.getResponse();
                        response.getHeaders().setContentType(org.springframework.http.MediaType.valueOf(routeDef.getResponseContentType()));
                        response.setStatusCode(org.springframework.http.HttpStatus.valueOf(routeDef.getResponseStatusCode()));
                        byte[] bytes = routeDef.getResponseBody().getBytes();
                        DataBuffer buffer = response.bufferFactory().wrap(bytes);
                        return response.writeWith(Mono.just(buffer));
                    } else {
                        logger.info("Forwarding request for route: {}", routeId);
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
                } else {
                    logger.warn("Route definition not found for routeId: {}", routeId);
                }
            } else {
                logger.warn("No route found in exchange attributes");
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
        // 配置属性（如果需要）
    }
}
