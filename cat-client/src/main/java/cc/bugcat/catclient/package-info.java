package cc.bugcat.catclient;


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
 * 3、http过程调用完全透明，可进行优化、定制开发（添加签名、添加token）     
 * 
 * 4、可以自动去掉响应外层包装器类，直接返回业务对象
 * 
 * 
 * 
 * 
 *
 * 
 * 更多用法，参见 client-example 项目
 * 
 * 
 * */