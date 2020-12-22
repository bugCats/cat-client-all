package com.bugcat.catclient;


/**
 *
 * @author bugcat
 * 
 * 
 *
 * 
 * 仿FeignClient http客户端
 * 
 * 
 * 
 * 1、轻量级，只要是Spring项目，都可以使用。仅额外需要引入fastjson；
 * 
 * 2、支持高度的自由扩展：
 *      
 *      支持定制日志记录方案，精确到具体的某个API方法的输入输出
 *      
 *      支持定制Http工具类，可以为每个API方法配置超时
 *      
 * 3、http过程调用完全透明，可进行优化、定制开发（添加签名、添加token、去掉响应外层包裹对象）     
 * 
 * 
 * 
 * 
 * 
 * 
 *
 * 断点调试入口
 * com.bugcat.catclient.beanInfos.CatMethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], org.springframework.cglib.proxy.MethodProxy)
 *
 * 
 * 更多用法，参见 client-example 项目
 * 
 * 
 * */