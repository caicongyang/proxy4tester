package com.caicongyang.proxy4tester.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caicongyang.proxy4tester.entity.RouteDefinition;
import com.caicongyang.proxy4tester.mapper.RouteDefinitionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteDefinitionService extends ServiceImpl<RouteDefinitionMapper, RouteDefinition> {
    
    public List<RouteDefinition> getAllEnabledRoutes() {
        return baseMapper.selectAllEnabledRoutes();
    }

    public RouteDefinition getRouteByRouteId(String routeId) {
        return lambdaQuery().eq(RouteDefinition::getRouteId, routeId).one();
    }

    public boolean updateRouteByRouteId(String routeId, RouteDefinition updatedRoute) {
        RouteDefinition existingRoute = getRouteByRouteId(routeId);
        if (existingRoute == null) {
            return false;
        }
        updatedRoute.setId(existingRoute.getId());
        return updateById(updatedRoute);
    }

    public boolean deleteRouteByRouteId(String routeId) {
        return remove(lambdaQuery().eq(RouteDefinition::getRouteId, routeId));
    }
}
