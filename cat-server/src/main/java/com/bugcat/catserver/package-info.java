package com.bugcat.catserver;

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
 * 3、生成的Controller，支持事务，可以直接当作成一个普通的Service实现类
 * 
 * 
 *
 * 
 * 更多用法，参见 server-example 项目
 * 
 * 
 * */