package com.sixi.gateway.marketservice.repository;

import com.alibaba.fastjson.JSON;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.data.redis.core.StringRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.synchronizedMap;

/**
 * @Author: ZY
 * @Date: 2019/8/12 18:17
 * @Version 1.0
 * @Description: 持久化路由存储信息
 */
public class UnifiedRouteRepository implements RouteDefinitionRepository {

    public static final String GATEWAY_ROUTES = "gateway_routes";

    @Resource
    private StringRedisTemplate redisTemplate;

    private final Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<>());

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        redisTemplate.opsForHash().values(GATEWAY_ROUTES).stream()
                .forEach(routeDefinition -> routeDefinitions.add(JSON.parseObject(routeDefinition.toString(), RouteDefinition.class)));
        return Flux.fromIterable(routeDefinitions);
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            routes.put(r.getId(), r);
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            if (routes.containsKey(id)) {
                routes.remove(id);
                return Mono.empty();
            }
            return Mono.defer(() -> Mono.error(
                    new NotFoundException("RouteDefinition not found: " + routeId)));
        });
    }

}
