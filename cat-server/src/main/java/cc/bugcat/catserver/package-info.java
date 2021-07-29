package cc.bugcat.catserver;

/**
 *
 * @author bugcat
 * 
 * 
 * 
 * 可以通过类似与被@FeignClient标记的interface，直接生成服务端
 * 
 * 结合FeignClient使用，简化服务端开发，并且能使客户端和服务端通过interface耦合在一起
 * 
 *
 * 
 * 1、生成的Controller，支持swagger
 * 
 * 2、可以为每个生成的Controller，单独配置拦截器链
 * 
 * 3、生成的Controller，可以当作成一个普通的Service组件
 * 
 * 4、可以自动为Controller响应，加上包装类（效果与作用和CatClient的拆包相反）
 *
 *
 * 
 * 更多用法，参见 server-example 项目
 * 
 * 
 * */