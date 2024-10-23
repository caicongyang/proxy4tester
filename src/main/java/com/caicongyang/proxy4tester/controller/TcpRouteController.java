package com.caicongyang.proxy4tester.controller;

import com.caicongyang.proxy4tester.config.RouteRefresher;
import com.caicongyang.proxy4tester.entity.TcpRouteDefinition;
import com.caicongyang.proxy4tester.service.TcpRouteDefinitionService;
import com.caicongyang.proxy4tester.tcp.TcpProxyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tcp-routes")
public class TcpRouteController {

    @Autowired
    private TcpRouteDefinitionService tcpRouteDefinitionService;

    @Autowired
    private RouteRefresher routeRefresher;

    @Autowired
    private TcpProxyServer tcpProxyServer;

    @GetMapping
    public List<TcpRouteDefinition> getAllTcpRoutes() {
        return tcpRouteDefinitionService.getAllRoutes();
    }

    @PostMapping
    public ResponseEntity<String> addTcpRoute(@RequestBody TcpRouteDefinition tcpRouteDefinition) {
        boolean result = tcpRouteDefinitionService.save(tcpRouteDefinition);
        if (result) {
            routeRefresher.refreshTcpRoutes();
            return ResponseEntity.ok("TCP route added and refreshed");
        }
        return ResponseEntity.badRequest().body("Failed to add TCP route");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateTcpRoute(@PathVariable Long id, @RequestBody TcpRouteDefinition tcpRouteDefinition) {
        tcpRouteDefinition.setId(id);
        boolean result = tcpRouteDefinitionService.updateById(tcpRouteDefinition);
        if (result) {
            routeRefresher.refreshTcpRoutes();
            return ResponseEntity.ok("TCP route updated and refreshed");
        }
        return ResponseEntity.badRequest().body("Failed to update TCP route");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTcpRoute(@PathVariable Long id) {
        boolean result = tcpRouteDefinitionService.removeById(id);
        if (result) {
            routeRefresher.refreshTcpRoutes();
            return ResponseEntity.ok("TCP route deleted and refreshed");
        }
        return ResponseEntity.badRequest().body("Failed to delete TCP route");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshTcpRoutes() {
        try {
            routeRefresher.refreshTcpRoutes();
            tcpProxyServer.restart();
            return ResponseEntity.ok("TCP routes refreshed and Netty server restarted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to refresh TCP routes: " + e.getMessage());
        }
    }

    @PutMapping("/update/{routeId}")
    public ResponseEntity<String> updateTcpRouteByRouteId(@PathVariable String routeId, @RequestBody TcpRouteDefinition tcpRouteDefinition) {
        TcpRouteDefinition existingRoute = tcpRouteDefinitionService.getRouteByRouteId(routeId);
        if (existingRoute == null) {
            return ResponseEntity.badRequest().body("TCP route not found");
        }
        
        existingRoute.setLocalPort(tcpRouteDefinition.getLocalPort());
        existingRoute.setRemoteHost(tcpRouteDefinition.getRemoteHost());
        existingRoute.setRemotePort(tcpRouteDefinition.getRemotePort());
        existingRoute.setDirectResponse(tcpRouteDefinition.getDirectResponse());
        existingRoute.setMockResponse(tcpRouteDefinition.getMockResponse());
        existingRoute.setEnabled(tcpRouteDefinition.getEnabled());
        
        boolean updated = tcpRouteDefinitionService.updateById(existingRoute);
        if (updated) {
            routeRefresher.refreshTcpRoutes();
            return ResponseEntity.ok("TCP route updated and refreshed");
        }
        return ResponseEntity.badRequest().body("Failed to update TCP route");
    }
}
