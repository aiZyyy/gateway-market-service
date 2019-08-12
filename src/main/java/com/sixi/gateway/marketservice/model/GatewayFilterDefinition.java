package com.sixi.gateway.marketservice.model;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: ZY
 * @Date: 2019/8/12 18:18
 * @Version 1.0
 * @Description:过滤器定义模型
 */

public class GatewayFilterDefinition {

    /**
     * Filter Name
     */
    private String name;

    /**
     * 对应的路由规则
     */
    private Map<String, String> args = new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args;
    }
}