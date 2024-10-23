package com.caicongyang.proxy4tester.controller;

import com.caicongyang.proxy4tester.config.RouteRefresher;
import com.caicongyang.proxy4tester.entity.RouteDefinition;
import com.caicongyang.proxy4tester.service.RouteDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/http-routes")
public class HttpRouteController {

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
        boolean result = routeDefinitionService.save(routeDefinition);
        if (result) {
            routeRefresher.refreshHttpRoutes();
        }
        return result;
    }

    @PutMapping("/{id}")
    public boolean updateRoute(@PathVariable Long id, @RequestBody RouteDefinition routeDefinition) {
        routeDefinition.setId(id);
        boolean result = routeDefinitionService.updateById(routeDefinition);
        if (result) {
            routeRefresher.refreshHttpRoutes();
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public boolean deleteRoute(@PathVariable Long id) {
        boolean result = routeDefinitionService.removeById(id);
        if (result) {
            routeRefresher.refreshHttpRoutes();
        }
        return result;
    }

    @PostMapping("/refresh")
    public String refreshRoutes() {
        routeRefresher.refreshHttpRoutes();
        return "HTTP routes refreshed";
    }

    @PutMapping("/update/{routeId}")
    public String updateRouteByRouteId(@PathVariable String routeId, @RequestBody RouteDefinition routeDefinition) {
        RouteDefinition existingRoute = routeDefinitionService.getRouteByRouteId(routeId);
        if (existingRoute == null) {
            return "Route not found";
        }
        
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
            routeRefresher.refreshHttpRoutes();
            return "HTTP route updated and refreshed";
        }
        return "Failed to update HTTP route";
    }
}
