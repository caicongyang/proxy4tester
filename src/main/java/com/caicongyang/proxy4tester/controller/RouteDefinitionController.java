package com.caicongyang.proxy4tester.controller;

import com.caicongyang.proxy4tester.config.RouteRefresher;
import com.caicongyang.proxy4tester.entity.RouteDefinition;
import com.caicongyang.proxy4tester.service.RouteDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteDefinitionController {

    @Autowired
    private RouteDefinitionService routeDefinitionService;

    @Autowired
    private RouteRefresher routeRefresher;

    @GetMapping
    public List<RouteDefinition> getAllRoutes() {
        return routeDefinitionService.list();
    }

    @PostMapping
    public boolean addRoute(@RequestBody RouteDefinition routeDefinition) {
        return routeDefinitionService.save(routeDefinition);
    }

    @PutMapping("/{id}")
    public boolean updateRoute(@PathVariable Long id, @RequestBody RouteDefinition routeDefinition) {
        routeDefinition.setId(id);
        return routeDefinitionService.updateById(routeDefinition);
    }

    @DeleteMapping("/{id}")
    public boolean deleteRoute(@PathVariable Long id) {
        return routeDefinitionService.removeById(id);
    }

    @PostMapping("/refresh")
    public String refreshRoutes() {
        routeRefresher.refreshRoutes();
        return "Routes refreshed";
    }

    @PutMapping("/update/{routeId}")
    public String updateRouteByRouteId(@PathVariable String routeId, @RequestBody RouteDefinition routeDefinition) {
        RouteDefinition existingRoute = routeDefinitionService.getRouteByRouteId(routeId);
        if (existingRoute == null) {
            return "Route not found";
        }
        
        // 确保更新所有相关字段，包括 directResponse
        existingRoute.setPath(routeDefinition.getPath());
        existingRoute.setUri(routeDefinition.getUri());
        existingRoute.setFilters(routeDefinition.getFilters());
        existingRoute.setFilterOrder(routeDefinition.getFilterOrder());
        existingRoute.setEnabled(routeDefinition.getEnabled());
        existingRoute.setDirectResponse(routeDefinition.getDirectResponse());
        existingRoute.setResponseBody(routeDefinition.getResponseBody());
        existingRoute.setResponseContentType(routeDefinition.getResponseContentType());
        existingRoute.setResponseStatusCode(routeDefinition.getResponseStatusCode());
        
        boolean updated = routeDefinitionService.updateById(existingRoute);
        if (updated) {
            routeRefresher.refreshRoutes();
            return "Route updated and refreshed";
        }
        return "Failed to update route";
    }
}
