## Define Controller through feign-interface


<br>

*feign-interface:*

```java
public interface UserService {
    
    @PostMapping("/user/userPage")
    ResponseEntity<PageInfo<UserInfo>> userPage(@RequestHeader("token") String token, @ModelAttribute UserPageVi vi);

    @GetMapping("/user/get/{uid}")
    UserInfo userInfo(@PathVariable("uid") String uid, @RequestParam("type") String type);

    @PostMapping("/user/userSave")
    ResponseEntity<Void> userSave(@RequestBody UserSaveVi vi);
    
}
```

<br>

*example：*

```java
@CatServer(handers = UserInterceptor.class)
public class UserServiceImpl implements UserService {

    @Override
    public ResponseEntity<PageInfo<UserInfo>> userPage(String token, UserPageVi vi) {
        return null;
    }

    @Override
    public UserInfo userInfo(String uid, String type) {
    	System.out.println("uid=" + uid + " type=" + type);
        return null;
    }

    @Override
    public ResponseEntity<Void> userSave(UserSaveVi vi) {
        return null;
    }
}
```

> UserServiceImpl => Controller

> curl 'http://ip:port/user/get/666?type=cat', console print 'uid=666 type=cat'


<br>

---



<s>散装英语尽力了</s>

<br>

<br>


## 使用interface定义Controller

<br>

`FeignClient`作为客户端，其实上是使用`http`请求服务端的接口。一般使用*restful*风格的*Controller*作为服务端代码。<br>
服务端和客户端，分别有自己的输入输出数据模型，通过*Json*字符串耦合在一起。

<br>

反观`dubbo`类的框架，服务端会有一个*xml*配置文件，暴露提供的*Service*层信息（高版本中直接可以使用注解申明）。<br>
然后在`服务端`，直接用一个类实现这个*interface*接口，实现*interface*中方法，即可被远程客户端调用。<br>
看上去就好像是`客户端`，注入一个*Service*类的*interface*，就完成了调用远程服务端的*Service*具体实现类。


<br>


```markdown
  客户端                    调用                      服务端	                   
 服务消费者   ══════════════════════════════════>   服务提供者           
     │                                                 ↑ 
     │                                                 │         
     │                                                 │ 
     └──────────────> Service interface                │                                 
	                       │                       │
	                       │                       │   
	                       └───────────────────────┤   
	                                               │
                                                       │
	                                          Service 实现类
```


> 服务端和客户端，直接通过Service类的*interface*耦合在一起，<br>
如果服务端输入模型、响应模型中，新增了一个字段，那么同版本的客户端可以直接使用这个字段了。


<br><br>

如果`FeignClient`+`Controller`也想和`dubbo`一样，用一个*interface*将客户端与服务端耦合在一起，改如何处理？

仔细观察*feign-interface*，方法上已经包含了 *@PostMapping*、*@GetMapping*、*@RequestBody*、*@RequestParam* 等注解，作为*Controller*的一些必要元素已经包含了。<br>
这使得通过*feign-interface*类定义*Controller*变为可能！

<br><br><br>

---

## 开始使用

环境：JDK1.8+

依赖：spring 4.3 + 

<br><br>

### cat-client：客户端 

使用方式类似于`FeignClient`。CatClient客户端一定是个**interface**。

<br><br>


#### 启用CatClient客户端

SpringBoot项目，可以在启动类上添加`@EnableCatClient`

```java
@EnableCatClient
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
}
```

<br>

**@EnableCatClient**，依赖spring容器：
+ `value`：扫描的包路径，默认是启动类所在包。如果把客户端放在了其他包目录下，需要指定其包路径。会自动将包含`@CatClient`注解的类，注册成客户端；
+ `configuration`：一些全局的默认配置项。http链接超时、日志记录方案、对象序列化与反序列化类、http请求类、自动拆包装器类等；
+ `classes`：指定客户端class进行注册，优先度高于包路径。class值分为2大类：
    1. 是普通的class，并且类上包含`@CatClient`注解，将该class注册成客户端；
    2. 是**CatClientProvider**的子类，解析该子类中包含`@CatClient`注解的方法，将方法返回对象的class注册成客户端；
    
 

<br><br>

##### 定义客户端    

方式一，在interface上添加`@CatClient`。系统启动时，会自动扫描注册成客户端：   
```java
@CatClient(host = "${userService.remoteApi}", connect = 3000, logs = RequestLogs.All2)
public interface UserService {
    
    @CatMethod( value = "/user/userPage", method = RequestMethod.POST)
    ResponseEntity<PageInfo<UserInfo>> userPage(@RequestHeader("token") String token, @ModelAttribute UserPageVi vi);

    @CatMethod( value = "/user/get/{uid}", method = RequestMethod.GET)
    UserInfo userInfo(@PathVariable("uid") String uid, @RequestParam("type") String type);

    @CatMethod( value = "/user/userSave", method = RequestMethod.POST)
    ResponseEntity<Void> userSave(@RequestBody UserSaveVi vi);
    
}
```

<br>

方式二，通过**CatClientProvider**子类集中批量定义：  

```java
public interface RemoteApis extends CatClientProvider {

    @CatClient(host = "${userService.remoteApi}", connect = 3000, socket = 3000)
    UserService userService();
    
    @CatClient(host = "${orderService.remoteApi}")
    OrderService orderService();

}
```

使用这种方式注册，可以避免在interface类上修改，保持interface干净。并且能提高interface复用性，也可以减少耦合。


<br><br>


##### 定义API方法

在客户端的方法上，添加`@CatMethod`。

方法上的入参，最多只能存在一个`@ModelAttribute`、或`@RequestBody`、或多组`@RequestParam`注解；但是可以与多组`@RequestHeader`、`@PathVariable`注解共存：

+ `@ModelAttribute`：用于标记复杂对象，只能存在一个；表示使用表单方式发送参数，不代表将该对象转成表单。
+ `@RequestBody`：用于标记复杂对象，只能存在一个；表示使用post流方式发送参数，不代表将该对象转成字符串；可以使用`@CatNote`为参数取别名；
+ `@RequestParam`：用于标记基础数据类型、字符串、日期对象，可以有多组，表示使用表单方式发送参数；
+ `@RequestHeader`：表示请求头参数，可以有多组；
+ `@PathVariable`：表示uri参数，可以有多组；

> @ModelAttribute、@RequestBody、@RequestParam 三者，在一个方法中，只能三选一。<br>
前2者不代表把对应的对象转换成表单、或者字符串。具体参数格式，需要参考CatSendProcessor类


<br><br>


#### 调用方式

1. 方式一、方式二必须依赖spring容器。在`@EnableCatClient`启用后，通过spring容器自动注入，和普通Service一样使用：
```java
@RestController
public class UserController {

    @Autowired // 自动注入客户端
    private UserService userService;
    
    @PostConstruct
    public void init(){
        UserInfo info = userService.userInfo(666L, "1");
    }
}
```

<br>

2. 如果是非spring环境、或者没有使用`@EnableCatClient`注解，可以使用静态方法**CatClientBuilders**创建实例： 

```
// 创建实例
Properties prop = new Properties(); //环境变量，用于填充和解析注解中${xxx}
prop.put("userService.remoteApi", "http://127.0.0.1:8012");
UserService remote = CatClientBuilders.builder(UserService.class)
                .environment(prop)
                .build();

// 使用
UserService remote = CatClientUtil.getBean(UserService.class);
remote.userInfo(666L, "1");
```


<br><br>

#### 核心注解

##### @CatClient

定义一个interface为CatClient客户端：

+ `value`：客户端组件的别名，默认首字母小写。最终客户端会注册到spring容器中，可以通过@Autowired实现自动注入；
+ `host`：http请求的主机地址。可以是 ip+端口，也可以是域名、cloud服务名：
    1. 字面量：`https://www.bugcat.cc`
    2. 读取配置文件值：`${xxx.xxx}`
    3. 服务名，配合注册中心：`http://myservername/ctx`
+ `factory`：创建http发送类对象、响应解析类对象的工厂类。如果服务端有特殊的入参格式，可以继承CatClientFactory实现扩展，然后再指定factory为扩展类；默认值受`CatClientConfiguration`控制；
+ `interceptor`：http发送拦截器，可以用来修改输入输出日志、修改参数签名、添加token等处理；默认值受`CatClientConfiguration`控制；
+ `fallback`：异常处理类，当接口发生http异常（40x、50x），执行的回调方法。类似FeignClient的fallback：
    1. `Object.class`：尝试执行interface的默认方法，如果没有默认实现，再执行全局默认异常处理；
    2. `Void.class`：关闭回调模式，执行全局默认异常处理；
    3. `其他class值`：必须实现该interface。当发生异常后，执行实现类的对应方法；
+ `socket`：http读值超时毫秒；-1 代表不限制；默认值受`CatClientConfiguration`控制；
+ `connect`：http链接超时毫秒；-1 代表不限制；默认值受`CatClientConfiguration`控制；
+ `logsMod`：记录日志方案。默认值为当发生异常时，记录输入输出参数；默认值受`CatClientConfiguration`控制；
+ `tags`：分组标记。用于配置重连分组、日志、自定义标签等，详细参见`@CatNote`：




<br><br>

##### @CatMethod

定义一个方法为http请求：

+ `value`：具体的url，与CatClient.host组合成完整的请求路径；
    1. 字面量：/qq/972245132
    2. 读取配置文件值：${xxx.xxx}
    3. 支持uri类型参数：/qq/{pathVariable}
+ `method`：请求方式，POST、GET等；默认POST；
+ `notes`：追加的其他自定义参数、或标记，详细参见`@CatNote`。用于自定义标记，可以用于http重连配置、或者标记分组等：
+ `socket`：http读值超时毫秒；-1 代表不限制；0 代表与CatClient.socket相同；优先度高于CatClient；
+ `connect`：http链接超时毫秒；-1 代表不限制；0 代表与CatClient.connect相同；优先度高于CatClient；
+ `logsMod`：记录日志方案；默认值为CatClient.logsMod；优先度高于CatClient；


<br><br>


##### @CatNote

作用一：为方法、参数、添加标记；参见`CatMethod.notes`：

1. `@CatNote(key="name", value="bugcat")` 直接字符串；
2. `@CatNote(key="host", value="${host}")` 从环境变量中获取；
3. `@CatNote(key="clazz", value="#{req.type}")` 从方法入参上获取`参数名.属性`。此种方法，必须要为入参取别名；
4. `@CatNote("bugcat")` 省略key，最终key与value值相同；

<br>

作用二：为入参取别名，一般配合`@RequestBody`使用，可以实现`#{参数名.属性}`动态获取入参属性；

> @CatMethod(value = "/user/save", notes = @CatNote(key = "uid", value = "#{req.userId}"), method = RequestMethod.POST) <br>
Void saveUser(@CatNote("req") @RequestBody UserInfo user);


<br><br>


#### 配置项

位于`cc.bugcat.catclient.spi`包中所有类，都可以通过继承与实现，达到增强。

<br>

##### CatClientConfiguration

全局默认配置。在启动注解`@EnableCatClient`中配置。

影响所有的`@CatClient`注解配置默认值。

如果`@CatClient`属性没有手动单独指定，其真实的默认值由`CatClientConfiguration`确定。


<br><br>

##### CatClientFactory

发起http请求的相关辅助类工厂，可以在`CatClientConfiguration#getClientFactory()`指定为全局。<br>也可以在`CatClient#factory()`为某个客户端单独指定。


通过实现该接口，或者继承`DefaultCatClientFactory`，可以修改：

+ `CatHttp`：http请求工具类，默认使用RestTemplate；
+ `CatPayloadResolver`：对象序列化与反序列化类，默认使用jackson；
+ `CatLoggerProcessor`：打印日志的控制类；默认使用apache slf4j；
+ `CatResultProcessor`：http响应处理类；
+ `CatSendProcessor`：http参数处理类；


<br><br>


##### CatHttp

http请求工具类，默认使用RestTemplate；优选从Srping容器中获取RestTemplate；

也可以自定义其他http组件：需要实现`cc.bugcat.catclient.spi.CatHttp`接口，在`CatClientConfiguration#getCatHttp()`指定为全局。



<br><br>

##### CatPayloadResolver

对象转字符串、字符串转对象处理类，默认使用Jackson框架；可以在`CatClientConfiguration#getJsonResolver()`指定为全局。

实现`cc.bugcat.catclient.spi.CatPayloadResolver`接口。`toSendString`方法也可以用于对象转XML。

注意：默认情况下，如果入参实现了`Stringable`，会优先使用入参自带的序列化方法。


<br><br>

##### CatLoggerProcessor

打印输入、输入参数，已其他一些与http相关的日志；修改日志的打印格式与方法。可以在`CatClientConfiguration#getLoggerProcessor()`指定为全局。

实现`cc.bugcat.catclient.spi.CatLoggerProcessor`接口。

可以控制到具体方法的输入、输出参数是否打印。


<br><br>

##### CatSendInterceptors

http请求拦截器，可以用于修改http请求配置、http请求入参、已经http响应。

实现`cc.bugcat.catclient.spi.CatSendInterceptor`接口。

`executeConfigurationResolver`方法，可以对`CatSendProcessor#postConfigurationResolver()`进行环绕增强。<br>
可以修改与http环境相关的配置，例如：请求地址、请求头、读取超时、日志方案等；

`executeVariableResolver`方法，可以对`CatSendProcessor#preVariableResolver()`、`CatSendProcessor#postVariableResolver()`进行环绕增强。<br>
<b>用于修改参数、计算签名、添加token、添加其他自定义标记</b>

`executeHttpSend`方法，可以对`CatSendProcessor#postHttpSend()`进行环绕增强。


<br><br>

##### CatSendProcessor

http请求参数处理器，必须为多例。主要控制和存储http相关数据：请求地址、请求头、请求入参、原始响应字符串、原始异常信息等。

可以通过`CatClientFactory#newSendHandler()`自动创建。也可以在调用客户端方法时，作为参数显示传入。

一般修改参数、签名、token等，优先使用`CatSendInterceptors`。<br>
如果需要对流程有非常大修改，才考虑扩展`CatSendProcessor`，如：需要接入注册中心等。

<br><br>

##### CatHttpRetryConfigurer

重连策略：

+ `enable`：是否开启重连；默认"false"；
+ `retries`：重连次数，不包含第一次调用！默认"2"，实际上最多会调用3次；
+ `status`：重连的状态码：多个用逗号隔开；可以为 "500,501,401" 或 "400-410,500-519,419" 或 "*" 或 "any"，默认"500-520"；
+ `method`：需要重连的请求方式：多个用逗号隔开；可以为 "post,get" 或 "*" 或 "any"，默认"any"；
+ `exception`：需要重连的异常、或其子类；多个用逗号隔开；可以为 "java.io.IOException" 或 "*" 或 "any"，默认""；
+ `tags`：需要重连的客户端分组，在`@CatClient#tags()`中配置；多个用逗号隔开；默认""；
+ `note`：需要重连的其他特殊标记；多个用逗号隔开；会匹配`@CatMethod#notes()`中标记；<br>
当note设置的值，在`@CatNote#value()`中存在时，触发重连；默认""；<br>
`note=bugcat`匹配`@CatNote(key="name", value="bugcat")`、`@CatNote("bugcat")`，不会匹配`@CatNote(key="bugcat", value="972245132")`
+ `noteMatch`：需要重连的其他特殊标记键值对；会匹配`@CatMethod#notes()`中标记；在配置文件中，使用单引号包裹的Json字符串，默认值`'{}'`；<br>
当noteMatch设置键值对，在notes的键值对中完全匹配时，触发重连：<br>
`note-match='{"name":"bugcat","age":"17"}'`，会匹配`notes={@CatNote(key="name", value="bugcat"), @CatNote(key="age", value="17")}`<br>
如果`@CatNote`采用`#{req.userId}`形式，可以实现运行时，根据入参决定是否需要重连：<br>
当设置`note=save`、`notes={@CatNote("#{req.methodName}"}`，或者`note-match='{"method":"save"}'`、`notes={@CatNote(key="method", value="#{req.methodName}")}`时，如果请求入参req的methodName=save，会触发重连；


<br><br>

##### AbstractResponesWrapper

自动拆包装器类，配合`@CatResponesWrapper`使用。

部分框架，服务端的响应，为统一对象，具体的业务数据，是统一对象通过泛型确认的属性。<br>
比喻ResponseEntity&lt;User&gt;、HttpEntity&lt;User&gt;，ResponesDTO&lt;User&gt;，具体响应是通过泛型确认的一个属性。可以称这类ResponseEntity、HttpEntity、ResponesDTO为响应包装器类。<br>
为了避免每次都要判断 `ResponesDTO != null && ResponesDTO.getCode()`，可以采用自动拆包装器类：如果ResponesDTO为null，或者code不为成功，则直接抛出异常；否则就直接返回User。

```
/**
 * 如果配置了拆包装器，demo1方法与demo2方法是等价。
 * 区别在于demo1方法，可以直接获取业务数据Demo，如果发生异常、或code等于失败，则抛出异常；
 * demo2方法，需要手动判断code，然后再决定是否需要抛出、或忽略异常执行后续流程。
 * 二者可以同时存在一个客户端内。
 * */
 
@CatMethod(value = "/cat/demo1", method = RequestMethod.POST)
Demo demo1(@RequestBody Demo req);

@CatMethod(value = "/cat/demo1", method = RequestMethod.POST)
ResponseEntity<Demo> demo2(@RequestBody Demo req);
```

> demo1、demo2方法，在实际http网络交互过程中，仍然有外层的ResponseEntity，但是对于demo1的业务层而言，ResponseEntity却是无感知的。






<br><br>

#### 综合示例

```
/**
 * 定义一个客户端
 * 远程服务器地址为：${core-server.remoteApi}，需要从环境变量中获取
 * 该客户端定义了一个拦截器：TokenInterceptor
 * 单独配置了http链接、读取超时：3000ms
 * 其他配置为默认值，参考cc.bugcat.catclient.config.CatClientConfiguration
 * 并且改客户端，配置了自动拆包装器，实际API接口，返回数据类型为ResponseEntity<T>
 * */
@CatResponesWrapper(ResponseEntityWrapper.class)
@CatClient(host = "${core-server.remoteApi}", interceptor = TokenInterceptor.class, connect = 3000, socket = 3000)
public interface TokenRemote {

    /**
     * 通过账户密码，获取token
     * 其中 username、pwd从环境配置中获取
     * */
    @CatMethod(value = "/cat/getToken", method = RequestMethod.POST,
            notes = {@CatNote(key = "username", value = "${demo.username}"), @CatNote(key = "pwd", value = "${demo.pwd}")})
    default ResponseEntity<String> getToken(CatSendProcessor sender, @RequestParam("username") String username, @RequestParam("pwd") String pwd) {
        return ResponseEntity.fail("-1", "当前网络异常！");
    }

    /**
     * {@code @CatNote}标记这个接口，需要token
     * */
    @CatMethod(value = "/cat/sendDemo", method = RequestMethod.POST, notes = @CatNote("needToken"))
    ResponseEntity<String> sendDemo1(@RequestBody Demo demo);

    /**
     * 将token作为请求头参数
     * */
    @CatMethod(value = "/cat/sendDemo", method = RequestMethod.POST)
    ResponseEntity<String> sendDemo2(@RequestBody Demo demo, @RequestHeader("token") String token);

    /**
     * 动态url，具体访问地址，有参数url确定
     * */
    @CatMethod(value = "{url}", method = RequestMethod.POST)
    default ResponseEntity<String> sendDemo3(@PathVariable("url") String url, @RequestHeader("token") String token, @RequestBody String req) {
        return ResponseEntity.fail("-1", "默认异常！");
    }
}
```


```
/**
 * http拦截器
 * */
@Component
public class TokenInterceptor implements CatSendInterceptors {

    /**
     * 使用拦截器修改参数，可以添加token、计算签名等
     * */
    @Override
    public void executeVariableResolver(CatClientContextHolder context, Intercepting intercepting) {
        CatSendProcessor sendHandler = context.getSendHandler();
        sendHandler.setTracerId(String.valueOf(System.currentTimeMillis()));
        JSONObject notes = sendHandler.getNotes();
        CatHttpPoint httpPoint = sendHandler.getHttpPoint();
        //使用note，标记是否需要添加签名
        String need = notes.getString("needToken");
        if( CatToosUtil.isNotBlank(need)){
            String token = TokenInfo.getToken();
            httpPoint.getHeaderMap().put("token", token);
            System.out.println(token);
        }
        // 执行默认参数处理
        intercepting.executeInternal();
    }

    /**
     * token管理
     * */
    private static class TokenInfo {

        private static TokenInfo info = new TokenInfo();
        public static String getToken(){
            return info.getToken(System.currentTimeMillis());
        }

        private TokenRemote tokenRemote = CatClientUtil.getBean(TokenRemote.class);
        private long keepTime;
        private String value;
        private String getToken(long now){
            if( now > keepTime ){
                TokenSend sender = new TokenSend(); // 获取token的时候，显示指定CatSendProcessor实例
                ResponseEntity<String> bean = tokenRemote.getToken(sender, "", "");
                keepTime = System.currentTimeMillis() + 3600;
                value = bean.getData();
                return value;
            } else {
                return value;
            }
        }
    }

    /**
     * 获取token的时候单独处理器
     * */
    private static class TokenSend extends CatSendProcessor {
        /**
         * 使用继承CatSendProcessor形式修改参数
         * */
        @Override
        public void postVariableResolver(CatClientContextHolder context){
            /**
             * notes 已经在postConfigurationResolver方法中解析完毕
             * 此处可以直接使用
             * */
            String pwd = notes.getString("pwd");
            String username = notes.getString("username");
            MultiValueMap<String, Object> keyValueParam = this.getHttpPoint().getKeyValueParam();
            keyValueParam.add("username", username);
            keyValueParam.add("pwd", pwd);
        }
    }
}
```

```
/**
 * 单元测试类
 * */
public class TokenRemoteTest {

    private static TokenRemote remote;
    static {
        /**
         * 静态方法调用
         * 如果使用Spring容器启动，则不需要这些
         * */
        // 用来填充客户端中使用的${xxx}
        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "");
        prop.put("demo.username", "bugcat");
        prop.put("demo.pwd", "[密码]");

        CatClientDepend clientDepend = CatClientDepend.builder()
                .defaultSendInterceptor(new TokenInterceptor())
                .build();
                
        remote = CatClientBuilders.builder(TokenRemote.class)
                .clientDepend(clientDepend)
                .catClient(new CatClients(){
                    @Override
                    public String host() { // 在运行时，动态修改
                        return "http://127.0.0.1:8012";
                    }
                })
                .environment(prop)
                .build();
    }
    
    @Test
    public void token() {
        Demo demo = new Demo();
        demo.setName("bugcat");
        demo.setMark("猫脸");
        String sendDemo1 = remote.sendDemo1(demo);
        System.out.println("remote.sendDemo1=" + sendDemo1);

        String token = "TokenRemoteTest-token2";
        String sendDemo2 = remote.sendDemo2(demo, token);
        System.out.println("remote.sendDemo2=" + sendDemo2);

        String url = "/cat/sendDemo";
        ResponseEntity<String> sendDemo3 = remote.sendDemo3(url, "TokenRemoteTest-token3", JSONObject.toJSONString(demo));
        System.out.println("remote.sendDemo3=" + sendDemo3.getData());

    }
}
```

<br><br><br><br>

---

<br><br><br><br>


### cat-server：服务端

此处`FeignClient-Interface`，指类似于`FeignClient`的interface，里面包含 *@PostMapping*、*@GetMapping*、*@RequestBody*、*@RequestParam* 等注解；

另外服务端，是指使用`FeignClient-Interface`，定义的类似于Controller的类。

<br>

```markdown
  1. FeignClient-Interface ┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┐
            ↑                                ┆ 
            │                                ┆ 3. asm增强interface
            │                                ┆
            │                                ↓
            │                        4. Enhancer-Interface
            │                                ↑
            │                                │
            │                                │
            │                                │ 5. 使用cglib
            │                                │
     2. CatServer <═════════════════════╗    │ 
                                        ║    │
                                        ║    │
                               6. cglib-controller   <══════════  7. http调用
```

1. feign-interface，包含@PostMapping、@GetMapping、@RequestBody、@RequestParam等注解的interface类；
2. feign-interface的具体实现类，其类上加有`@CatServer`注解；
3. 使用asm对feign-interface增强处理；
4. 增强后的Interface；
5. 使用cglib对增强后的Interface动态代理，生成controller角色的类；
6. 动态代理生成的controller对象，其中持有`2. CatServer`实现类的引用；
7. http访问controller对象方法，controller执行`2. CatServer`对应的方法；

<br><br>

#### 启用CatServer服务端

SpringBoot项目，可以在启动类上添加`@EnableCatServer`

```java
@EnableCatServer
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
}
```
<br>

**@EnableCatServer**，依赖spring容器：
+ `value`：扫描的包路径，默认是启动类所在包。如果把服务端放在了其他包目录下，需要指定其包路径。会自动将包含`@CatServer`注解的类，注册成服务端；
+ `configuration`：一些全局的默认配置项。访问服务端的拦截等；
+ `classes`：指定服务端class进行注册，优先度高于包路径。
    
 
<br><br>

#### 定义服务端

1. 定义一个`feign-interface`：

```java
@Api(tags = "用户操作api")
public interface UserService {
    
    @ApiOperation("分页查询用户")
    @RequestMapping(value = "/user/userPage")
    ResponseEntity<PageInfo<UserInfo>> userPage(@ModelAttribute("vi") UserPageVi vi);

    @ApiOperation("根据用户id查询用户信息")
    @RequestMapping(value = "/user/get/{uid}", method = RequestMethod.GET)
    UserInfo userInfo(@PathVariable("uid") String uid);

    @ApiOperation("设置用户状态")
    @RequestMapping(value = "/user/status", method = RequestMethod.GET)
    Void status(@RequestParam("uid") String userId, @RequestParam("status") String status);
    
}
```

<br>

2. 普通类实现这个*feign-interface*，并且在类上添加`@CatServer`：

```java
@CatServer
public class UserServiceImpl implements UserService {

    @Override
    public ResponseEntity<PageInfo<UserInfo>> userPage(UserPageVi vi) {
        System.out.println("PageInfo >>> 这是调用服务端userPage接口返回");
        return ResponseEntity.ok(null);
    }

    @Override
    public UserInfo userInfo(String uid) {
        System.out.println("userInfo >>> 这是调用服务端userInfo接口返回");
        return null;
    }
    
    @Override
    public Void status(String userId, String status) {
        System.out.println("status >>> userId=" + userId + ", status=" + status);
        return null;
    }
    
}
```

> 启动项目后，便可以通过`http://localhost:{port}/{ctx}/user/xxx`访问UserServiceImpl对象对应的方法了

<br><br>

#### 核心注解

##### @CatServer

定义一个改实现类为CatServer服务端：

+ `value`：服务端组件的别名，默认首字母小写。最终生成的服务器组件会充当controller角色注入到spring容器中；
+ `tags`：分组标记。用于配置连接器组、日志、自定义标签等，详细参见`@CatNote`：
+ `resultHandler`：结果处理类。针对不同服务端，可以有不同的包装器类、异常处理流程；
+ `interceptors`：拦截器。用于控制CatServer类的访问权限、打印日志等；
  1. CatServerInterceptor.class 表示启用全局的默认拦截器配置，最终该位置会被全局配置的拦截器替换；
  3. CatServerInterceptor.Off.class 表示关闭所有拦截器。

> `@CatServer`：启用默认拦截器，默认拦截器在CatServerConfiguration#getDefaultInterceptor()指定；<br>
`@CatServer(interceptors = CatServerInterceptor.class)`：启用默认拦截器，具体是哪个类，由CatServerConfiguration#getDefaultInterceptor()指定；<br>
`@CatServer(interceptors = {CatServerInterceptor.class, UserInterceptor.class})`：启用默认拦截器和自定义拦截器；<br>
`@CatServer(interceptors = UserInterceptor.class)`：仅启用自定义拦截器；<br>


<br><br>

##### @CatBefore

CatServer类方法上自定义的入参处理器：可以用于注入session、requestModelAttribute等；


<br><br>


#### 配置项

位于`cc.bugcat.catserver.spi`包中所有类，都可以通过继承与实现，达到增强。

<br>

##### CatServerConfiguration

全局默认配置。在启动注解`@EnableCatServer`中配置。

影响所有的`@CatServer`注解配置默认值。

如果`@CatServer`属性没有手动单独指定，其真实的默认值由`CatServerConfiguration`确定。


<br><br>

##### CatParameterResolver

CatServer类方法上自定义的入参处理器。配合`@CatBefore`使用；

<br><br>

##### CatServerInterceptor

拦截器配置：用于控制访问权限、记录日志打印。默认拦截器在CatServerConfiguration#getDefaultInterceptor()指定。也可以为单个服务端配置、或者关闭。

1. CatServerInterceptor.class 表示启用全局的默认拦截器配置，最终该位置会被全局配置的拦截器替换；
3. CatServerInterceptor.Off.class 表示关闭所有拦截器。


<br><br>

##### CatInterceptorGroup

拦截器组：在运行时动态匹配。不会被自定义拦截器覆盖，只能被CatServerInterceptor.Off.class关闭


<br><br>


##### AbstractResponesWrapper

自动加包装器类，配合`@CatResponesWrapper`使用。和客户端的自动拆包装器类似，服务端可以实现自动加包装器类。

部分框架，服务端的响应，为统一对象，具体的业务数据，是统一对象通过泛型确认的属性。<br>
比喻ResponseEntity&lt;User&gt;、HttpEntity&lt;User&gt;，ResponesDTO&lt;User&gt;，具体响应是通过泛型确认的一个属性。可以称这类ResponseEntity、HttpEntity、ResponesDTO为响应包装器类。<br>
为了统一响应风格，可以采用自动加包装器类。


<br><br>

#### 精简版

cat-server服务端可以使用标准版的`feign-interface`，也可以使用cat-client的客户端interface。



当与cat-client配合使用时，可在interface上添加`@Catface`注解，启用精简模式。<br>
此模式下，默认把interface中所有的方法都注册成API接口，并且默认用post发送字符流方式发送Http请求。

可以省略 *@PostMapping*、*@GetMapping*、*@RequestBody*、*@RequestParam* 等所有的注解，此时interface可以是一个普通的接口类；

<br>

`@Catface`：在interface上添加，表示启用精简模式，将interface中所有方法视为API方法，采用post + 字符流模式；
+ `value`：interface类别名，默认是首字母小写。
+ `namespace`：命名空间。


> 最终API接口的url为：/命名空间/interface别名/方法名 <br>
精简模式下，不容许出行方法重载，即：同一个interface中，不能出现多个相同名称的方法！


<br>


```java
@Api(tags = "精简模式") // swagger注解，使用精简模式，swagger仍然可以使用
@Catface               // 标记为精简模式
@CatResponesWrapper(ResponseEntityWrapper.class) // 自动拆、加包装器类。如果是客户端，表示自动拆包装器；如果是服务端，则正好相反，表示自动加保证器类
public interface FaceDemoService {

    // 方法名可任意，此处只做示例
    UserInfo param0(); 

    // 可以使用注解验证参数必填
    UserInfo param1(@NotBlank(message = "userId不能为空") String userId);
    
    // 可以添加swagger描述
    @ApiOperation("api - param2")
    UserInfo param2(String userId, Integer status);

    UserInfo param3(UserPageVi vi);

    UserInfo param4(String userId, UserPageVi vi);

    UserInfo param5(String userId, UserPageVi vi, Integer status);

    UserInfo param6(UserPageVi vi1, UserPageVi vi2, Integer status);

    UserInfo param7(UserPageVi vi1, UserPageVi vi2, Integer status, Map<String, Object> map);

    // swagger参数描述
    UserInfo param8(@ApiParam("参数map") Map<String, Object> map,
                    @ApiParam("参数vi1") @Valid UserPageVi vi1,
                    @ApiParam("参数vi2") UserPageVi vi2,
                    @ApiParam("参数status") @NotNull(message = "status 不能为空") Integer status,
                    @ApiParam("参数vi3") @Valid ResponseEntity<PageInfo<UserPageVi>> vi3);

    // 默认方法，提供给客户端使用。当客户端调用失败之后，执行
    default UserInfo param9(@ApiParam("参数map") Map<String, Object> map,
                          @ApiParam("参数vi1") @Validated UserPageVi vi1,
                          @ApiParam("参数date") Date date,
                          @ApiParam("参数status") Integer status,
                          @ApiParam("参数decimal") BigDecimal decimal,
                          @ApiParam("参数vi3") @Valid ResponseEntity<PageInfo<UserPageVi>> vi3) {
        Throwable exception = CatToosUtil.getException();
        System.out.println("异常：" + exception.getMessage());
        return null;
    }  
    
}
```

<br>

```java
/**
 * 使用CatClientProvider方式定义客户端，避免污染FaceDemoService类
 * */
public interface FaceConfig extends CatClientProvider {
    
    @CatClient(host = "${core-server.remoteApi}", connect = -1, socket = -1)
    FaceDemoService faceDemoService();

}

```

```java
public class FaceDemoServiceTest {
    
    private static FaceDemoService faceDemoService;
    
    static {
        /** 使用静态方法调用。如果是spring容器启动，可以直接自动注入 **/
        Properties prop = new Properties();
        prop.put("core-server.remoteApi", "http://127.0.0.1:8012");

        Map<Class, Object> configMap = CatClientBuilders.builder(FaceConfig.class, FaceDemoService.class)
                .environment(prop)
                .build();
        
        faceDemoService = (FaceDemoService) configMap.get(FaceDemoService.class);
    }

    @Test
    public void param0() throws Exception {
        faceDemoService.param0();
    }
    
    @Test
    public void param7() throws Exception {
        UserPageVi vi1 = new UserPageVi();
        vi1.setUid("param61");
        vi1.setName("入参61");
        UserPageVi vi2 = new UserPageVi();
        vi2.setUid("param62");
        vi2.setName("入参62");
        Map<String, Object> map = new HashMap<>();
        map.put("mapKey1", "value1");
        map.put("mapKey2", "value2");
        faceDemoService.param7(vi1, vi2, 1, map);
    }

    @Test
    public void param8() throws Exception {
        UserPageVi vi1 = new UserPageVi();
        vi1.setUid("param61");
        vi1.setName("入参61");
        UserPageVi vi2 = new UserPageVi();
        vi2.setUid("param62");
        vi2.setName("入参62");

        UserPageVi vi31 = new UserPageVi();
        vi31.setUid("param63");
        vi31.setName("入参63");
        PageInfo<UserPageVi> vi32 = new PageInfo<>(1, 10, 1);
        vi32.setList(Collections.singletonList(vi31));
        ResponseEntity<PageInfo<UserPageVi>> vi3 = ResponseEntity.ok(vi32);

        Map<String, Object> map = new HashMap<>();
        map.put("mapKey1", "value1");
        map.put("mapKey2", "value2");

        faceDemoService.param8(map, vi1, vi2, 1, vi3);
    }
}
```

> 定义客户端，执行客户端方法

<br>

```java
@CatServer(interceptors = UserInterceptor.class) // 添加了自定义拦截器
public class FaceDemoServiceImpl implements FaceDemoService{

    @Override
    public UserInfo param0() {
        System.out.println("param0:");
        return info();
    }

    @Override
    public UserInfo param1(String userId) {
        System.out.println("param1: userId=" + userId);
        return info();
    }

    @Override
    public UserInfo param2(String userId, Integer status) {
        System.out.println("param2: userId=" + userId + "; status=" + status);
        return info();
    }

    @Override
    public UserInfo param3(UserPageVi vi) {
        System.out.println("param3: vi=" + JSONObject.toJSONString(vi));
        return info();
    }

    @Override
    public UserInfo param4(String userId, UserPageVi vi) {
        System.out.println("param4: userId=" + userId + ";vi=" + JSONObject.toJSONString(vi));
        return info();
    }

    @Override
    public UserInfo param5(String userId, UserPageVi vi, Integer status) {
        System.out.println("param5: userId=" + userId + ";vi=" + JSONObject.toJSONString(vi) + ";status=" + status);
        return info();
    }

    @Override
    public UserInfo param6(UserPageVi vi1, UserPageVi vi2, Integer status) {
        System.out.println("param6: vi1=" + JSONObject.toJSONString(vi1) + ";vi2=" + JSONObject.toJSONString(vi2)
                        + ";status=" + status );
        return info();
    }

    @Override
    public UserInfo param7(UserPageVi vi1, UserPageVi vi2, Integer status, Map<String, Object> map) {
        System.out.println("param7: vi1=" + JSONObject.toJSONString(vi1) + ";vi2=" + JSONObject.toJSONString(vi2)
                        + ";status=" + status + ";map=" + JSONObject.toJSONString(map));
        return info();
    }
    
    @Override
    public UserInfo param8(Map<String, Object> map, UserPageVi vi1, UserPageVi vi2, Integer status, ResponseEntity<PageInfo<UserPageVi>> vi3) {
        System.out.println("param8: vi1=" + JSONObject.toJSONString(vi1) + ";map=" + JSONObject.toJSONString(map) + ";vi2=" + JSONObject.toJSONString(vi2)
                + ";vi3=" + JSONObject.toJSONString(vi3) + ";status=" + status );
        return info();
    }
    
    @Override
    public UserInfo param9(Map<String, Object> map, UserPageVi vi1, Date date, Integer status, BigDecimal decimal, ResponseEntity<PageInfo<UserPageVi>> vi3) {
        System.out.println("param9: vi1=" + JSONObject.toJSONString(vi1) + ";map=" + JSONObject.toJSONString(map)
                + ";vi2=" + JSONObject.toJSONString(date)
                + ";vi3=" + JSONObject.toJSONString(vi3)
                + ";decimal=" + decimal
                + ";status=" + status );
        return info();
    }

    private UserInfo info(){
        UserInfo info = new UserInfo();
        info.setUid("face");
        info.setName("ok");
        return info;
    }
}
```

> 定义服务端


<br>

可以发现，精简模式下，与`dubbo`已经非常相似了！


<br><br>


#### 其他说明

被`@CatServer`标记的类，可以充当Controller角色。但是又可以像普通*Service*一样，可以被其他组件注入调用！

1. 生成的Controller类，支持swagger框架：  
在interface的方法、输入模型、输出模型上，使用swagger注解，同样可以生成API文档，并且能正常调用

2. 可以为每个生成的Controller单独配置拦截器：   
仅当通过API调用，作为Controller角色时，拦截器生效；而一般情况，作为组件注入调用时，不拦截！

3. 可以为Controller的响应，自动加上包装器类：  
很多情况下，API接口的响应是同一个类，具体的业务数据，是响应类的一个泛型属性。CatServer组件，可以让开发人员专注业务数据，程序将业务对象自动封装到公共的响应类中。<br>
对应的，如果某处发生异常没有被捕获，在最终也可以通过配置生成不同的异常响应对象

5. 通过类的继承特性，实现对API接口升级：   
例如有个新类`UserServiceExtImpl`继承`UserServiceImpl`，并且重写了父类方法，那么这个API便升级成了子类重新的方法！

6. 可搭配`FeignClient`使用：    
可以实现如同`dubbo`框架风格，客户端与服务器通过*interface*耦合。*客户端注入interface，服务端实现interface*

7. 可以像普通Service类一样，支持`@Transactional`事务配置。


<br><br><br><br>

---

<br><br><br><br>


## 项目介绍

#### cat-client 客户端

<br>

#### cat-client-cloud
cat-client 负载均衡服务组件。需要自行实现*ServerChoose*接口；nacos有时间补上；
> 有需要搭载负载均衡的可以引入。

<br>

#### cat-common
cat-client、cat-server都需要使用的工具类

<br>

#### cat-server-swagger
cat-server swagger组件，修正服务端将url映射到interface上，swagger不生效问题。
> 需要生成swagger文档的可以引入

<br>

#### cat-server
cat-server服务端核心模块
> 有需要使用cat-server服务端的必须引入

<br>

#### examples/example-client-xxxx
客户端调用示例

<br>

#### examples/example-server-xxx
服务端示例

<br>

#### examples-csdn
博客内容示例


<br>


----

<br><br>



<br><br>










