package com.caicongyang.proxy4tester.config;

import com.caicongyang.proxy4tester.service.TcpRouteDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RouteRefresher {

    private final TcpRouteDefinitionService tcpRouteService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public RouteRefresher(TcpRouteDefinitionService tcpRouteService) {
        this.tcpRouteService = tcpRouteService;
    }

    public void refreshHttpRoutes() {
        eventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }

    public void refreshTcpRoutes() {
        tcpRouteService.refreshRouteCache();
    }

    public void refreshAllRoutes() {
        refreshHttpRoutes();
        refreshTcpRoutes();
    }
}
