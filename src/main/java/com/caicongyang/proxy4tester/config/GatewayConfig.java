package com.caicongyang.proxy4tester.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    // 移除了之前的 customRouteLocator 方法
    // 所有的路由配置现在都由 DynamicRouteConfig 处理
}
