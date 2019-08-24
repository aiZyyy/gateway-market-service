# gateway-market-service
    目前运用全局过滤器进行请求拦截,所有的请求都需要验证有没有添加的"ignore"(自定义的一个键值对)的请求头,如果检验有的话,就直接跳过验签,直接进入路由环节,如果没有的话,就需要验签,当验签通过后就会重新封装请求,将请求传递,再次进入全局,直接进行转发
    目前动态路由功能在这个项目里就不能使用了,因为这个端口相当于只能访问gateway.do唯一一个接口,但是如果需要这个功能只需要讲项目的RouteController,GatewayFilterDefinition,GatewayPredicateDefinition,GatewayRouteDefinition,DynamicRouteService,RouteAddForm,RouteDelForm移出到另外一个项目即可
