package com.caicongyang.proxy4tester.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caicongyang.proxy4tester.entity.TcpRouteDefinition;
import com.caicongyang.proxy4tester.mapper.TcpRouteDefinitionMapper;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TcpRouteDefinitionService extends ServiceImpl<TcpRouteDefinitionMapper, TcpRouteDefinition> {

    private final Map<Integer, TcpRouteDefinition> routeCache = new ConcurrentHashMap<>();

    public List<TcpRouteDefinition> getAllRoutes() {
        return baseMapper.selectAllEnabledRoutes();
    }

    public TcpRouteDefinition getRouteByRouteId(String routeId) {
        return lambdaQuery().eq(TcpRouteDefinition::getRouteId, routeId).one();
    }

    public TcpRouteDefinition getRouteByPort(int localPort) {
        return routeCache.get(localPort);
    }

    public boolean updateRouteByRouteId(String routeId, TcpRouteDefinition updatedRoute) {
        TcpRouteDefinition existingRoute = getRouteByRouteId(routeId);
        if (existingRoute == null) {
            return false;
        }
        updatedRoute.setId(existingRoute.getId());
        boolean updated = updateById(updatedRoute);
        if (updated) {
            refreshRouteCache();
        }
        return updated;
    }

    public boolean deleteRouteByRouteId(String routeId) {
        boolean removed = remove(lambdaQuery().eq(TcpRouteDefinition::getRouteId, routeId));
        if (removed) {
            refreshRouteCache();
        }
        return removed;
    }

    public List<TcpRouteDefinition> loadRoutesFromDatabase() {
        return baseMapper.selectAllEnabledRoutes();
    }

    public void refreshRouteCache() {
        List<TcpRouteDefinition> routes = loadRoutesFromDatabase();
        Map<Integer, TcpRouteDefinition> newCache = routes.stream()
            .collect(Collectors.toMap(TcpRouteDefinition::getLocalPort, route -> route, (r1, r2) -> r1));
        routeCache.clear();
        routeCache.putAll(newCache);
    }

    public List<Integer> getAllEnabledPorts() {
        return routeCache.keySet().stream().collect(Collectors.toList());
    }

    @PostConstruct
    public void init() {
        refreshRouteCache();
    }
}
