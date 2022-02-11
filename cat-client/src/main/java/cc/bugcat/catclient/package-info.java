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
 * 更多用法，参见 client-example 项目
 *
 *
 *
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * 启动流程：
 *
 *
 *  0、Spring容器启动
 *
 *  ↓
 *
 *  1、EnableCatClient 启用，@Import -> CatClientScannerRegistrar
 *
 *  ↓
 *
 *  2、CatClientScannerRegistrar 扫描包含@CatClient的interface，向Spring容器注册对应的FactoryBean
 *
 *  ↓
 *
 *  3、CatClientInfoFactoryBean 解析interface上的@CatClient注解 => CatClientInfo
 *
 *  ↓
 *
 *  4、通过cglib，使用interface创建动态代理类
 *
 *  ↓
 *
 *  5、解析@CatMethod注解 => CatMethodInfo
 *
 *  ↓
 *
 *  6、为interface的每个方法，添加拦截器 => CatMethodAopInterceptor
 *
 *  ↓
 *
 *  7、CatClientInfoFactoryBean 返回cglib动态代理后的对象，实现自动注入
 *
 *
 *
 * 调用流程：
 *
 *  0、执行interface的方法
 *
 *  1、进入CatMethodAopInterceptor#intercept(..)方法
 *
 *  2、从方法参数列表获取CatSendProcessor对象。
 *      如果没有，使用CatClientFactory创建一个
 *
 *  3、使用CatMethodInfo#parseArgs(..)处理参数列表
 *
 *  4、调用CatSendProcessor#sendConfigurationResolver(..)设置http相关参数
 *
 *  5、调用CatSendProcessor#postVariableResolver()处理http入参
 *
 *  6、执行CatSendProcessor#httpSend()方法发送http请求
 *
 *  7、使用DefaultResultHandler#resultToBean(..)解析响应字符串
 *
 *  8、DefaultResultHandler#doFinally(..)做最后结果处理
 *
 *  9、如果发生http异常，可通过MethodProxy#invokeSuper(..)执行回调类方法，返回默认异常值；或者通过DefaultResultHandler#onHttpError(..)统一处理
 *
 *
 *
 * */