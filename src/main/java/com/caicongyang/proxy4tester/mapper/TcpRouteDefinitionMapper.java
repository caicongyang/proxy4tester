package com.caicongyang.proxy4tester.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caicongyang.proxy4tester.entity.TcpRouteDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TcpRouteDefinitionMapper extends BaseMapper<TcpRouteDefinition> {
    @Select("SELECT * FROM tcp_route_definition WHERE enabled = true")
    List<TcpRouteDefinition> selectAllEnabledRoutes();
}
