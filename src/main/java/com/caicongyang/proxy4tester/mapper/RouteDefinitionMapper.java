package com.caicongyang.proxy4tester.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caicongyang.proxy4tester.entity.RouteDefinition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RouteDefinitionMapper extends BaseMapper<RouteDefinition> {
    List<RouteDefinition> selectAllEnabledRoutes();
}
