package com.caicongyang.proxy4tester.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("route_definitions")
public class RouteDefinition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String routeId;
    private String path;
    private String uri;
    private String filters;
    private Integer filterOrder;
    private Boolean enabled;
    private Boolean directResponse;
    private String responseBody;
    private String responseContentType;
    private Integer responseStatusCode;
}
