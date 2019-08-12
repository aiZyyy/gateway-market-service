package com.sixi.gateway.marketservice.controller;

import com.sixi.gateway.marketservice.model.GatewayFilterDefinition;
import com.sixi.gateway.marketservice.model.GatewayPredicateDefinition;
import com.sixi.gateway.marketservice.model.GatewayRouteDefinition;
import com.sixi.gateway.marketservice.route.DynamicRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: ZY
 * @Date: 2019/8/12 18:15
 * @Version 1.0
 * @Description:
 */
@RestController
@RequestMapping("/route")
public class RouteController {
    @Autowired
    private DynamicRouteService dynamicRouteService;

    /**
     * 增加路由
     * @param gatewayRouteDefinition
     * @return
     */
    @PostMapping("/add")
    public String add(@RequestBody GatewayRouteDefinition gatewayRouteDefinition) {
        try {
            RouteDefinition definition = assembleRouteDefinition(gatewayRouteDefinition);
            return this.dynamicRouteService.add(definition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "succss";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable String id) {
        return this.dynamicRouteService.delete(id);
    }

    @PostMapping("/update")
    public String update(@RequestBody GatewayRouteDefinition gwdefinition) {
        RouteDefinition definition = assembleRouteDefinition(gwdefinition);
        return this.dynamicRouteService.update(definition);
    }

    private RouteDefinition assembleRouteDefinition(GatewayRouteDefinition gwdefinition) {

        RouteDefinition definition = new RouteDefinition();

        // ID
        definition.setId(gwdefinition.getId());

        // Predicates
        List<PredicateDefinition> pdList = new ArrayList<>();
        for (GatewayPredicateDefinition gpDefinition: gwdefinition.getPredicates()) {
            PredicateDefinition predicate = new PredicateDefinition();
            predicate.setArgs(gpDefinition.getArgs());
            predicate.setName(gpDefinition.getName());
            pdList.add(predicate);
        }
        definition.setPredicates(pdList);

        // Filters
        List<FilterDefinition> fdList = new ArrayList<>();
        for (GatewayFilterDefinition gfDefinition: gwdefinition.getFilters()) {
            FilterDefinition filter = new FilterDefinition();
            filter.setArgs(gfDefinition.getArgs());
            filter.setName(gfDefinition.getName());
            fdList.add(filter);
        }
        definition.setFilters(fdList);

        // URI
        URI uri = UriComponentsBuilder.fromUriString(gwdefinition.getUri()).build().toUri();
        definition.setUri(uri);

        return definition;
    }
}
