package com.caicongyang.proxy4tester.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tcp_route_definition")
public class TcpRouteDefinition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String routeId;
    private Integer localPort;
    private String remoteHost;
    private Integer remotePort;
    private Boolean directResponse;
    private String mockResponse;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
