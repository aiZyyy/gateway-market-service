package com.sixi.gateway.marketservice.domain.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Author: ZY
 * @Date: 2019/8/14 13:51
 * @Version 1.0
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteAddForm {

    /**
     * 路由id
     */
    @NotBlank(message = "路由id不能为空")
    private String routeId;

    /**
     * 对外路径
     */
    @NotBlank(message = "path不能为空")
    private String path;

    /**
     * 真实映射路径
     */
    @NotBlank(message = "映射路径不能为空")
    private String uri;
}
