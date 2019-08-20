package com.sixi.gateway.marketservice.controller;

import com.sixi.gateway.marketservice.domain.form.RouteAddForm;
import com.sixi.gateway.marketservice.domain.form.RouteDelForm;
import com.sixi.gateway.marketservice.route.DynamicRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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

    @PostMapping("/notify")
    public String notifyChanged() {
        dynamicRouteService.notifyChanged();
        return "notify_success";
    }

    /**
     * 增加路由
     *
     * @param routeAddForm
     * @return
     */
    @PostMapping("/add")
    public String add(@Valid @RequestBody RouteAddForm routeAddForm) {
        try {
            return this.dynamicRouteService.add(routeAddForm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    @PostMapping("/update")
    public String update(@RequestBody RouteAddForm routeAddForm) {
        return this.dynamicRouteService.update(routeAddForm);
    }

    @PostMapping("/delete")
    public String delete(@Valid @RequestBody RouteDelForm routeDelForm) {
        return this.dynamicRouteService.delete(routeDelForm);
    }
}
