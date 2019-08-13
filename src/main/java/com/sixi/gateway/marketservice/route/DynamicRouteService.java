package com.sixi.gateway.marketservice.route;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.sixi.gateway.marketservice.repository.UnifiedRouteRepository.GATEWAY_ROUTES;

/**
 * @Author: ZY
 * @Date: 2019/8/12 17:20
 * @Version 1.0
 * @Description:
 */

@Service
public class DynamicRouteService implements ApplicationEventPublisherAware {

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    private ApplicationEventPublisher publisher;

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 刷新路由信息
     */
    private void notifyChanged() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    /**
     * 增加路由
     *
     * @param definition
     * @return
     */
    public String add(RouteDefinition definition) {
        //放入内存
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        //存入redis
        redisTemplate.opsForHash().put(GATEWAY_ROUTES, definition.getId(), JSON.toJSONString(definition));
        notifyChanged();
        return "success";
    }

    /**
     * 更新路由
     *
     * @param definition
     * @return
     */
    public String update(RouteDefinition definition) {
        try {
            this.routeDefinitionWriter.delete(Mono.just(definition.getId()));
            //删除redis信息
            redisTemplate.opsForHash().delete(definition.getId());
        } catch (Exception e) {
            return "update fail,not find route  routeId: " + definition.getId();
        }
        try {
            add(definition);
        } catch (Exception e) {
            return "update route fail";
        }
        return null;
    }

    /**
     * 删除路由
     *
     * @param id
     * @return
     */
    public String delete(String id) {
        try {
            this.routeDefinitionWriter.delete(Mono.just(id));
            //删除redis信息
            redisTemplate.opsForHash().delete(GATEWAY_ROUTES,id);
            notifyChanged();
            return "delete success";
        } catch (Exception e) {
            e.printStackTrace();
            return "delete fail";
        }

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }


}