package com.sixi.gateway.marketservice.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: ZY
 * @Date: 2019/8/12 18:19
 * @Version 1.0
 * @Description: 路由断言定义模型
 */
public class GatewayPredicateDefinition {

    /**
     * 断言对应的Name
     */
    private String name;

    /**
     * 配置的断言规则
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
