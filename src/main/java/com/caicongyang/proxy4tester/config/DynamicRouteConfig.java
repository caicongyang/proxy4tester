package com.caicongyang.proxy4tester.config;

import com.caicongyang.proxy4tester.entity.RouteDefinition;
import com.caicongyang.proxy4tester.service.RouteDefinitionService;
import com.caicongyang.proxy4tester.filter.CustomResponseFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * DynamicRouteConfig 类负责动态配置 Spring Cloud Gateway 的路由。
 * 它从数据库中读取路由定义，并根据这些定义创建相应的路由。
 */
@Configuration
public class DynamicRouteConfig {
    private static final Logger logger = LoggerFactory.getLogger(DynamicRouteConfig.class);

    @Autowired
    private RouteDefinitionService routeDefinitionService;

    @Autowired
    private CustomResponseFilter customResponseFilter;

    /**
     * 创建自定义的 RouteLocator Bean。
     * 这个方法会被 Spring 调用来创建路由配置。
     *
     * @param builder RouteLocatorBuilder，用于构建路由
     * @return 配置好的 RouteLocator
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();
        
        // 获取所有启用的路由定义
        List<RouteDefinition> enabledRoutes = routeDefinitionService.getAllEnabledRoutes();
        logger.info("Loading {} enabled routes", enabledRoutes.size());
        
        // 遍历所有启用的路由定义，为每个定义创建一个路由
        for (RouteDefinition routeDef : enabledRoutes) {
            logger.info("Configuring route: id={}, path={}, directResponse={}, responseBody={}", 
                        routeDef.getRouteId(), routeDef.getPath(), routeDef.getDirectResponse(), routeDef.getResponseBody());
            
            // 使用路由定义的信息创建路由
            routes.route(routeDef.getRouteId(),
                r -> r.path(routeDef.getPath())
                    .filters(f -> f.filter(customResponseFilter.apply(new CustomResponseFilter.Config())))
                    .uri(routeDef.getDirectResponse() ? "no://op" : routeDef.getUri())
            );
        }
        
        // 构建并返回 RouteLocator
        return routes.build();
    }
}
