
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


<b>关键词：</b>Interface定义Controller；feign服务端；feign interface；Http RPC；cat-client；cat-server；catface；

<b>概&nbsp;&nbsp;&nbsp;要：</b>`catface`，使用类似`FeignClient`的Interface作为客户端发起Http请求，然后在服务端使用实现了这些Interface的类作为`Controller`角色，将客户端、服务端通过Interface耦合在一起，实现无感知调用的轻量级组件。其底层通讯协议依旧是Http，支持Http的所有特性：证书、负载均衡、熔断、路由转发、报文日志、swagger等；

如果使用过`dubbo`就比较好理解，和`dubbo`调用非常类似。只不过`catface`只支持Http协议，并且目前只有spring项目可以使用。

<br>

`catface`的灵感来于`FeignClient`，想必其他同学在看见feignClient的Interface的时候，应该都有一个想法：feign interface使用的注解，和Controller有很是多共用，那么能不能直接用使用这些Interface来定义Controller呢？

<br>

**举个例子**
先粗略看一下feign的接口`IDemoService`： <br>
<i>为了后续避免歧义，此处我们将类似于IDemoService的Interface，称呼为<b>`feign-interface`</b></i>
```java
public interface IDemoService {

    @RequestMapping(value = "/server/demo41", method = RequestMethod.POST)
    Demo demo1(@RequestBody Demo req);
    
    @RequestMapping(value = "/server/demo43", method = RequestMethod.GET)
    List<Demo> demo3(@ModelAttribute Demo req);
    
    @RequestMapping(value = "/server/demo44", method = RequestMethod.POST)
    ResponseEntity<Demo> demo4(@RequestParam("userName") String name, @RequestParam("userMark") String mark);
    
    @RequestMapping(value = "/server/demo46/{uid}", method = RequestMethod.GET)
    Void demo6(@PathVariable("uid") Long userId, @RequestParam("userName") String name);
    
    @RequestMapping(value = "/server/demo47", method = RequestMethod.PUT)
    Demo demo7(@RequestHeader("token") String token);
}
```

<u>FeignClient客户端</u>，在spring项目中，只需要把`IDemoService`作为一个普通的Service类使用，自动注入到其他Bean中，就可以直接执行*IDemoService*类中的方法，FeignClient便会自动根据方法上的注解信息，发起Http请求；

<s>FeignClient服务端</s>：没有严格意义上的FeignClient服务端！在spring项目中一般是`Controller`提供的接口，返回相应的报文；



<br><br>

如果是公司内部系统相互调用，例如：**clientA**调用**serverB**：
在clientA中定义若干feign-interface作为客户端，并定义接口方法的入参、响应类；
在serverB中定义Controller、以及对应DTO、VO模型；

可以发现： <br>
:: serverB中的DTO、VO，其数据结构，和feign-interface方法上入参、响应类是一致的； <br>
:: clientA与serverB，在代码层面上没有任何联系。无论是feign-interface的方法入参、响应类增减字段，都不会影响到DTO、VO模型；反之也是一样；

<br>

如果存在feign-interface可以直接定义Controller这个功能，那么上述一般流程，可以修改成如下模式：
新增一个接口层：**serverB-facede**，表示由serverB模块对外提供服务；
其中： <br>
:: serverB-facede中包含：feign-interface、方法的入参、响应类； <br>
:: clientA依赖serverB-facede模块，在clientA中依旧可以使用feign-interface客户端； <br>
:: serverB同样依赖serverB-facede模块，在serverB中实现feign-interface接口，将<u>实现类</u><b>注册</b>成Controller； <br>

如此以来，clientA和serverB，便通过serverB-facede耦合在一起。看上去就像是在clientA模块中，自动注入了一个feign-interface类，执行其中的方法，就可以得到serverB模块中实现类返回的数据！Http调用对于消费者、提供者，都是无感知的；

<br>

```

  clientA                   调用                     serverB	                   
 服务消费者   ===================================>   服务提供者           
	 │                                             │ 
	 │  注入                                        │         
	 │                                             │ 
	 └──────────────   feign-interface             │                                 
       	                       │                       │   
                               └───────────────────────┤   
                                                       │
	                                          实现  │
                                                       │
	                                        feign-interface 实现类

```

<br><br>

如果了解spring扫描Bean的原理，在服务端根据feign-interface上的注解，手动注册Controller不难；而且在熟悉动态代理的情况下，自己根据feign-interface写一个http RPC也不是不可能；

于是在技术实现都不难的情况下，`catface`就出现辣~：


<br><br>

`catface`基于spring框架开发的<i> <s>(不会吧不会吧，在如今spring大有一统江湖的趋势，还会有新项目没有使用spring全家桶的吧？)</s></i>，通过Interface、以及其方法的输入返回模型，耦合客户端与服务端，实现无感知Http调用。
对于开发者而言，眼前没有客户端、服务端，只有Interface调用者和Interface的实现类：

1. 精细化到每个接口的输入输出报文记录方案；
   比如某些API是核心流程，需要记录详细的输入输出报文，以便如果后续出现问题，可以查阅日志内容；而有些API不是那么重要，但调用又非常频繁，此时我们不希望打印它们，以免浪费系统性能、浪费存储空间；对于一般的API，平时的时候就记录一下调用记录，如果发生Http异常、或返回了业务异常，就记录详细的输入输出报文；


2. 精细化到每个接口的Http等待时间：
   对于中台而言，调用后台接口，不可能无限等待后台接口响应；和日志一样的逻辑，有的API等待时间可以长点，有的又不行；


3. 自动加、拆<u>响应包装器类</u>：
   当服务端的所有响应，为统一的一个数据模型、具体的业务数据，是该模型`通过泛型确认`的属性时，例如：<i>HttpEntity\<User\></i>、<i>ResponesDTO\<User\></i>，称这种似于<i>HttpEntity</i>、<i>ResponesDTO</i>的数据结构为`响应包装器类`；
   ```java
   public class ResponesDTO<T> {
       private String code; 	// code为1表示成功，非1表示失败
       private String message;	//异常原因说明
       private T data;		//业务数据
   }
   ```
   当**客户端**获取到响应对象<i>ResponesDTO&lt;User&gt;</i>之后：
   1. 需要先判断对象是否为null；
   2. 再判断code是否为1，不为1需要进入异常流程；
   3. 最后才能获取到业务数据User；

   `catface`支持自动拆[响应包装器类](#AbstractResponesWrapper)。当启用后，客户端feign-interface的方法返回数据类型： <br>
   &nbsp; &nbsp; &nbsp; :: 可以是<i>ResponesDTO&lt;User&gt;</i>，保持原流程不变； <br>
   &nbsp; &nbsp; &nbsp; :: 直接为<i>User</i>，而服务端无需做任何修改。当feign-interface的方法成功执行完并返回了<i>User</i>对象，表示Http调用成功，并且响应的code一定是1；如果调用失败、或者code不为1，会自动进入预先设置好的异常流程；<br>

   加[响应包装器类](#AbstractResponesWrapper)，针对**服务端**而言，是拆包装器类的逆操作。当服务端启用之后，feign-interface和与之对应的Controller方法响应的数据类型，可以直接是<i>User</i>，`catface`会自动在最外层加上<i>ResponesDTO</i>；

   另外，对于使用**继承**实现公共属性code、message和业务数据并列的情况，也同样适用于此功能；


4. 自定义标记功能：
   在feign-interface上添加自定义标记、还可以为单个API接口添加标记。结合拦截器、springEL动态解析入参，便可以灵活实现各种各样的业务逻辑；


5. 在服务端，通过继承实现API接口升级：
   在Java中，继承本身就可以增强父类功能。此特性也适用于`catface`，通过继承特性对服务端API接口升级增强，而客户端无需任何修改；


6. 通过feign-interface生成的Controller，依旧支持swagger；


7. 其他比较一般的功能：拦截器、修改http Jar包、负载均衡、熔断、异常重试、Mock测试等等；



<br><br>

------

<br><br>

## cat-client 模块

此模块可以单独使用，使用方式和`feignclient`非常类似；以feign-interface为模板动态生成Http调用实现类，在应用层自动注入feign-interface对象，执行feign-interface中的方法，即可发起http请求，并最终将Http响应结果，转换成<u>方法返回对象</u>数据类型，返回给应用层；

<br>

<a id="EnableCatClient"></a>
### @EnableCatClient

这个注解表示**启用**CatClient客户端。该注解依赖于`spring容器`，置于任何spring bean之上，例如：springboot项目启动类上、或者任意包含`@Component`注解的类上；

+ <u>value</u>: 扫描的包路径，处于这个目录中的feign-interface都会被注册成客户端。默认值是被标记类的同级目录；
+ <u>classes</u>: 指定客户端class进行注册，优先度高于包路径。class值分为2大类： <br>
  :: 普通的class: 当类上包含[@CatClient](#CatClient)注解时，将该class注册成客户端； <br>
  :: [CatClientProvider](#CatClientProvider)的子类: 解析该子类中包含@CatClient注解的方法，将方法返回对象的class注册成客户端； <br>
+ <u>configuration</u>: 生成客户端的一些[配置项和默认值](#CatClientConfiguration)；

<br>

<a id="CatClient"></a>
### @CatClient

该注解用于**定义**某个feign-interface为客户端，包含：客户端别名、远程服务端host、拦截器、异常回调、http等待时间、默认的API日志记录方案；定义CatClient客户端有2种方式：

**方式1:** 在feign-interface上添加`@CatClient`；系统启动时，会根据[@EnableCatClient](#EnableCatClient)的配置，自动扫描并注册成客户端：
```java
@CatClient(host = "${userService.remoteApi}", connect = 3000, logs = RequestLogs.All2)
public interface IUserService {

    ResponseEntity<PageInfo<UserInfo>> userPage(@RequestHeader("token") String token, @ModelAttribute UserPageVi vi);
    
    UserInfo userInfo(@PathVariable("uid") String uid, @RequestParam("type") String type);
    
    ResponseEntity<Void> userSave(@RequestBody UserSaveVi vi);
}
```
<br>

**方式2:** 通过[CatClientProvider](#CatClientProvider)的子类，集中批量定义。将包含`@CatClient`注解的方法其返回对象的class注册成客户端；

```java
public interface RemoteProvider extends CatClientProvider {

    @CatClient(host = "${userService.remoteApi}", connect = 3000, socket = 3000)
    IUserService userService(); //实际上将IUserService注册成客户端
    
    @CatClient(host = "${orderService.remoteApi}")
    IOrderService orderService(); //将IOrderService注册成客户端
}
```

+ <u>value</u>: 客户端组件的别名，默认首字母小写。最终客户端会注册到spring容器中，可以通过`@Autowired`实现自动注入；
+ <u>host</u>: http请求的主机地址。可以是 IP+端口，也可以是域名、cloud服务名： <br>
  :: 字面量: <i>https://www.bugcat.cc </i> <br>
  :: 配置文件值: <i>${xxx.xxx}</i> <br>
  :: 服务名，配合注册中心: <i>http://myserver-name/ctx </i> <br>
+ <u>interceptor</u>: http请求[拦截器](#CatSendInterceptor)；用来修改入参、请求url、修改参数签名、添加token等处理；默认值受[CatClientConfiguration](#CatClientConfiguration)控制；
+ <u>factory</u>: 负责创建发送Http请求相关对象的[工厂类](#CatClientFactory)；一般如果仅需修改入参、或者添加签名等，可以使用拦截器修改。如果有比较大的Http流程调整，才考虑修改[CatClientFactory](#CatClientFactory)，例如：添加负载均衡；默认值受[CatClientConfiguration](#CatClientConfiguration)控制；
+ <u>fallback</u>: 异常处理类；当接口发生http异常（40x、50x），执行的回调方法。如果在回调方法中，继续抛出异常，或者关闭回调模式，则会执行[CatResultProcessor](#CatResultProcessor)#onHttpError进行最终**兜底**处理；其值可以取以下类型： <br>
  :: Object.class: 尝试使用feign-interface默认方法，如果feign-interface没有默认实现，再执行兜底方法； <br>
  :: Void.class: **关闭回调**模式，直接执行兜底方法； <br>
  :: 其他class值: 必须实现该feign-interface。当发生异常之后，执行实现类的对应方法； <br>
+ <u>socket</u>: http读值超时毫秒，-1 代表不限制；默认值受[CatClientConfiguration](#CatClientConfiguration)控制；
+ <u>connect</u>: http链接超时毫秒，-1 代表不限制；默认值受[CatClientConfiguration](#CatClientConfiguration)控制；
+ <u>logsMod</u>: 记录日志方案；默认值受[CatClientConfiguration](#CatClientConfiguration)控制；
+ <u>tags</u>: 分组标记，给客户端添加自定义标记；详细使用见[CatNote](#CatNote)；

<br>

<a id="CatMethod"></a>
### @CatMethod

定义客户端feign-interface中的方法，为API接口；

+ <u>value</u>: 具体的url； <br>
  :: 字面量: <i>/qq/972245132</i> <br>
  :: 配置文件值: <i>${xxx.xxx}</i> <br>
  :: uri类型参数: <i>/qq/{pathVariable}</i>
+ <u>method</u>: Http请求方式，默认使用POST发送表单；
+ <u>notes</u>: 自定义参数、或标记；当方法上还存在[@CatNotes](#CatNotes)注解时，会**忽略**这个属性值！详细使用见[CatNotes](#CatNotes)；
+ <u>socket</u>: http读值超时毫秒；-1 不限；0 同当前feign-interface配置；其他数值，超时的毫秒数；
+ <u>connect</u>: http链接超时毫秒；-1 不限；0 同当前feign-interface配置；其他数值，超时的毫秒数；
+ <u>logsMod</u>: 日志记录方案；Def 同当前feign-interface配置；

对于常用的GET、POST请求方式，还有`@CatGet`、`@CatPost`2个便捷组合注解；

<br>

<a id="CatNote"></a>
### @CatNote

自定义标记注解；有2大类使用场景：

1. 为feign-interface、方法、参数，添加标记；
  + <i>@CatNote(key="name", value="bugcat")</i>: 字面量；创建了一个标记，标记名=name，标记值=bugcat；
  + <i>@CatNote("bugcat")</i>: 字面量；省略key属性，最终key与value值相同，即标记名=标记值=bugcat；
  + <i>@CatNote(key="host", value="${orderhost}")</i>: 取配置文件值；创建了一个标记，标记名=host，标记值从配置中获取<u>orderhost</u>对应的值；
  + <i>@CatNote(key="userId", value="#{req.userId}")</i>: 取方法入参值；使用springEL表达式，从方法入参对象中获取标记值。此种方法，必须要为入参取别名；

<br>

2. 为方法入参取别名；一般配合`@RequestBody`使用，可以实现 <i>#{参数别名.属性}</i> 动态获取入参的属性值；可为入参取别名注解有：
  - <i>@ModelAttribute("paramName")</i>: 为GET、POST表单对象取别名；
  - <i>@PathVariable("paramName")</i>: PathVariable类型参数别名；
  - <i>@RequestParam("paramName")</i>: 键值对参数别名；
  - <i>@RequestHeader("paramName")</i>: 请求头参数别名；
  - <i>@CatNote("paramName")</i>: 通用类型参数别名，一般结合`@RequestBody`使用；

<br>

<a id="CatResponesWrapper"></a>

### @CatResponesWrapper

为当前feign-interface，配置加、拆<u>响应包装器类</u>；由于这个注解是标记在feign-interface接口上，因此如果feign-interface是作为客户端，那么`@CatResponesWrapper`便是启用拆<u>响应包装器类</u>功能；如果是作为服务端Controller，则是加<u>响应包装器类</u>；

+ <u>value</u>: Http<u>响应包装器类</u>[处理类](#AbstractResponesWrapper)；

<br>

<a id="CatClientConfiguration"></a>
### CatClientConfiguration
生成客户端的一些配置项和默认值。配合<i>@EnableCatClient</i>使用，可用于修改`@CatClient`、`@CatResponesWrapper`注解的实际默认值；

例如：<i>@CatClient#socket</i>的默认值为1000。如果需要统一修改成3000，而不想在每个feign-interface客户端上修改<i>socket=3000</i>，可以**重写**<i>CatClientConfiguration#getSocket</i>方法，使其返回3000即可；

+ <u>getSocket</u>: http读值超时，默认1000毫秒；对应<i>@CatClient#socket</i>；
+ <u>getConnect</u>: http链接超时，默认1000毫秒；对应<i>@CatClient#connect</i>；
+ <u>getLogsMod</u>: API接口输入输出报文记录方案，默认是当发生Http异常时记录；对应<i>@CatClient#logsMod</i>；
+ <u>getWrapper</u>: [拆包装器类](#AbstractResponesWrapper)处理类，对应<i>@CatResponesWrapper#value</i>；
+ <u>getMethodInterceptor</u>: http请求[拦截器](#CatSendInterceptor)，对应<i>@CatClient#interceptor</i>；
+ <u>getClientFactory</u>: 创建客户端发送Http请求相关对象的[工厂类](#CatClientFactory)，对应<i>@CatClient#factory</i>；
+ <u>getCatHttp</u>: [Http请求发送工具类](#CatHttp)，默认使用`RestTemplate`；
+ <u>getPayloadResolver</u>: Http请求输入输出对象，[序列化与反序列化处理类](#CatPayloadResolver)；默认使用`Jackson`框架；
+ <u>getLoggerProcessor</u>: 打印Http[日志类](#CatLoggerProcessor)；默认使用`logback`框架。如果需要修改日志打印格式，可以实现`CatLoggerProcessor`接口；


<br>

<a id="CatClientProvider"></a>
### CatClientProvider

批量注册客户端类；

```java
public interface RemoteProvider extends CatClientProvider {

    @CatClient(host = "${userService.remoteApi}", connect = 3000, socket = 3000)
    IUserService userService(); //实际上将IUserService注册成客户端
    
    @CatClient(host = "${orderService.remoteApi}")
    IOrderService orderService(); //将IOrderService注册成客户端
}
```

采用此方法定义客户端，将<i>@CatClient</i>置于<u>CatClientProvider子类</u>的<b>方法之上，</b>使得<i>@CatClient</i>注解与feign-interface类在物理上分隔开，避免注解**污染**feign-interface，以便可以多次复用feign-interface；

配合<i>@EnableCatClient#classes</i>使用，可以集中处理，按需加载使用。特别适用于多模块、多客户端feign-interface的场景；

例如：在serverB-facede模块中，与非常多的feign-interface，其中若干个feign-interface实现一个完整的业务流程。
为了避免多个消费端，需要多次手动注册多个feign-interface客户端，可以在serverB-facede模块中创建一个CatClientProvider子类，将相关feign-interface在其子类中预先定义好。
消费端在<i>@EnableCatClient#classes</i>中指定该子类，即可实现批量注册feign-interface客户端；


<br>

<a id="CatClientFactory"></a>
### CatClientFactory

创建客户端发送Http请求相关对象的工厂类。`CatClientConfiguration`中的参数适用于全局的默认配置。如果存在部分feign-interface客户端，需要特别的个性化配置，就需要使用自定义`CatClientFactory`，返回个性化的配置项；可以实现`CatClientFactory`接口，或者继承`SimpleClientFactory`，再修改<i>@CatClient#factory</i>参数值；

> @CatClient(host = "${orderhost}", factory = TokenFactory.class)

+ <u>getCatHttp</u>: 自定义Http发送类；
+ <u>getPayloadResolver</u>: 自定义入参响应序列化与反序列类；
+ <u>getLoggerProcessor</u>: 自定义日志格式打印类；
+ <u>getResultHandler</u>: [自定义http响应处理类](#CatResultProcessor)；
+ <u>newSendHandler</u>: [自定义http发送流程类](#CatSendProcessor)；返回对象**必须是多例**！


<br>

<a id="CatSendInterceptor"></a>
### CatSendInterceptor
Http发送请求流程中的拦截器；可以重写<i>[CatClientConfiguration](#CatClientConfiguration)#getMethodInterceptor</i>方法修改全局默认的拦截器；也可以通过<i>[@CatClient](#CatClient)#interceptor</i>为指定的feign-interface修改；

<i><u>全局拦截器，和自定义拦截器只能生效一个！</u>~~(支持多个拦截器下下下个版本再加)~~</i>

<br>

拦截器有4个切入点，对应[CatSendProcessor](#CatSendProcessor)的四个方法：

1. <u>executeConfigurationResolver</u>: 处理http配置项前后；对应<i>CatSendProcessor#doConfigurationResolver</i>方法，处理Http请求相关配置：host、url、读取超时、请求方式、请求头参数、解析自定义标记；
2. <u>executeVariableResolver</u>: 处理入参数据前后；对应<i>CatSendProcessor#doVariableResolver</i>、<i>CatSendProcessor#postVariableResolver</i>2个方法：如果在调用远程API，需要额外处理参数、或添加签名等，可以在[此处添加](#example-CatSendInterceptor)； <br>
   :: doVariableResolver，入参转字符串、或表单对象； <br>
   :: postVariableResolver，入参转换之后处理，这是给子类提供重写的方法； <br>
3. <u>executeHttpSend</u>: 发送Http请求前后；对应<i>CatSendProcessor#postHttpSend</i>方法；如果启用重连，那么该方法会执行多次！
4. <u>postComplete</u>: 在成功、异常回调方法之后、拆响应包装器之前执行；此处可以再次对响应对象进行修改；对于异常流程，可以继续抛出异常；

<br>

**调用流程示意**：

```
CatMethodAopInterceptor#intercept
			│
	CatClientContextHolder#executeConfigurationResolver
			│
		CatSendProcessor#doConfigurationResolver  <-------  CatSendInterceptor#executeConfigurationResolver
			│
	CatClientContextHolder#executeVariableResolver
			│
		CatSendProcessor#doVariableResolver  <----┬----  CatSendInterceptor#executeVariableResolver
			│                                 :
		CatSendProcessor#postVariableResolver  <--┘
			│
	CatClientContextHolder#executeRequest
			│
		CatSendProcessor#postHttpSend  <-------  CatSendInterceptor#executeHttpSend
			│
	[[CatResultProcessor#onHttpError]]
			│
	CatResultProcessor#resultToBean
			│
	CatClientContextHolder#postComplete  <------- CatSendInterceptor#postComplete
			│
	CatResultProcessor#onFinally
			│
return  <───────────────┘

```


<br>

<a id="CatHttp"></a>

### CatHttp
Http发送请求工具类，默认实现类`CatRestHttp`，底层使用`RestTemplate`发送请求。优先是从spring容器中获取RestTemplate，如果spring容器中没有，才会自动创建。

因此，如果spring容器中的RestTemplate配置了负载均衡，那么对应的CatRestHttp同样也有负载均衡特性！

如果需要修改成其他Http框架，可以如下操作：首先需要实现`CatHttp`接口；再将实现类编织到客户端Http发送流程中： <br>
方案1：将CatHttp实现类对象，注册到spring容器中；适用于全局； <br>
方案2：重写<i>[CatClientConfiguration](#CatClientConfiguration)#getCatHttp</i>方法，使其返回指定CatHttp对象；适用于全局； <br>
方案3：对于特定的API接口，可以利用<i>[CatClientFactory](#CatClientFactory)</i>。在定义feign-interface客户端时，修改<i>@CatClient#factory</i>值为指定<u>CatClientFactory子类</u>，再重写<i>CatClientFactory#getCatHttp</i>方法，返回自定义CatHttp实现类对象； <br>


<br>

<a id="CatPayloadResolver"></a>

### CatPayloadResolver

针对于使用POST方式发送IO流情况，将输入输出对象，序列化与反序列化的处理类。<i>如果是GET、POST发送表单数据，应该在CatHttp层进行统一的`uri编码`。</i>

默认使用`Jackson`框架，`catface`中还内置了`Fastjson`框架处理类。如果需要使用其他框架、或者使用xml，可以自行实现`CatPayloadResolver`接口，实现类编织到<u>Http发送流程中</u>方式，和自定义[CatHttp](#CatHttp)一致；


<br>

<a id="CatObjectResolver"></a>
### CatObjectResolver

将feign-interface方法上的**复杂数据类型**入参，转成表单对象；

`catface`不建议使用POST、GET发送太过于复杂的表单对象，推荐使用POST + Json这种一般方式。

虽然内置了入参数据转表单对象的处理类，但是对于<u>怪异的场景</u>如果出现不支持情况，就需要自行编写转换类。实现`CatObjectResolver`接口，再执行<i>[CatSendProcessor](#CatSendProcessor)#setObjectResolverSupplier</i>方法手动赋值；<i>CatSendProcessor对象可以在[拦截器](#CatSendInterceptor)中获取得到；</i>


<br>

<a id="CatLoggerProcessor"></a>
### CatLoggerProcessor
调用API接口的日志记录处理类。可以自行控制日志打印级别，以及日志格式；


<br>

<a id="CatResultProcessor"></a>
### CatResultProcessor

Http响应处理类：

+ <u>onHttpError</u>: 当发生Http异常后默认执行流程，最终进行兜底的异常处理；
+ <u>canRetry</u>: 异常是否需要重连，如果开启重连，并且发送Http异常，判断是否需要重新请求。一般结合熔断器或者注册中心使用，可以重新选择一个健康的服务端实例；
+ <u>resultToBean</u>: 响应报文转结果对象，默认使用`Jackson`框架。如果是通过xml传输信息，需要自行实现[CatPayloadResolver](#CatPayloadResolver)接口；
+ <u>onFinally</u>: 自动拆响应包装器类，结合[响应包装器类](#AbstractResponesWrapper)使用，可以使API接口方法，直接返回业务对象；


只能通过自定义<i>CatClientFactory#getResultHandler</i>修改，为单例；

<br>

<a id="CatSendProcessor"></a>
### CatSendProcessor

Http发送请求核心处理类；该对象可以通过<i>CatClientFactory#newSendHandler</i>自动创建，也支持手动创建后，作为feign-interface方法的入参传入。

`CatSendProcessor`类虽然提供了扩展的入口，但一般情况下无需修改，如果对Http请求整体流程有比较大的修改，才考虑覆盖重写。例如：搭配注册中心、负载均衡器使用、或者换成Socket协议等。<i>(负载均衡也可以使用[RestTemplate](#CatHttp)实现)</i>

若仅仅是修改入参、添加token、签名，可以直接使用更轻量级的[拦截器](#CatSendInterceptor)实现。

+ <u>doConfigurationResolver</u>: 初始化http相关配置。可修改项包括： <br>
  :: Http连接读取超时: <br>
  :: Http请求方式: 虽然在<i>@CatClient</i>中声明是使用POST发送Json字符串，但是此处也可以修改成POST发送表单方式； <br>
  :: 远程服务端host、API调用的url: 可以使用<i>@PathVariable</i>、取环境配置参数<i>\${xxx}</i>等方式动态给url赋值，也可以在此方法中修改host、url； <br>
  :: 自定义的标记数据: 根据<i>@CatNote</i>标记，获取到环境配置参数、动态取入参的属性值，可以对Host、url、入参、请求方式等，执行更自由修改；例如，集成负载均衡、自定义路由规则； <br>
+ <u>doVariableResolver</u>: 请求入参的默认处理。如果是POST、GET表单方式，将方法入参转成表单对象；如果POST发送IO流，则将入参对象序列化成字符串；此方法可以将入参模型，修改成表单对象；转成Json字符串；添加公共参数；添加Token；计算签名；记录请求信息；
+ <u>doVariableResolver</u>: 处理入参后处理。默认是个空方法，提供给子类重写。
+ <u>postHttpSend</u>: 全部数据准备充分之后，发起Http请求；如果需要换成其他协议，如Socket，重写此方法，将最终响应结果存储到<i>CatClientContextHolder#setResponseObject</i>中；

<br>

<a id="AbstractResponesWrapper"></a>
### AbstractResponesWrapper

<u>响应包装器类</u>处理类；
+ <u>getWrapperClass</u>: 获取响应包装器类的class；用于判断API接口方法的响应对象，是包装器类、还是直接业务数据类；
+ <u>getWrapperType</u>: 将业务数据类型Type，组装到响应包装器类中，返回标准响应Type的引用；例如，包装器类是<i>ResponseDTO\<T\></i>，业务数据类型为<i>User</i>，API接口返回的原始数据类型应该为<i>ResponseDTO\<User\></i>。对于使用拆包装器类的API接口方法，需要将方法的响应类<i>(即业务数据类型)</i>，组装到包装器类中。

```java
  public <T> CatTypeReference getWrapperType(Type type){
      //type 为业务数据的类型，可以是User、List<Order>、Long、String[]等
      //ResponseDTO是响应包装器类，type最终会替换掉T的位置，最终结果是ResponseDTO<Type>
      //注意后面的一对花括号不能少！
      return new CatTypeReference<ResponseDTO<T>>(type){};  
  }
```
+ <u>checkValid</u>: 校验返回的业务数据是否正确。需要注意异常分为两大类： <br>
  :: **Http异常** 由http请求造成的异常，例如：403 404 500 503，读取超时等，此类异常可以重新连接，或者换远程服务实例调用； <br>
  :: **业务异常** 此类为服务端接收到请求，并且通过业务逻辑判断，得出不处理该请求，并将消息成功返回给Http调用者。例如：调用取消订单，服务端判断该订单已经使用，不可以取消。API接口调用成功，但是响应为逻辑处理失败。  	
  checkValid方法主要是针对于业务异常场景，当发生业务异常时，程序该如何处理？可以选择继续抛出，由最顶层的<i>@ControllerAdvice</i>统一处理异常；也可以自行解析业务异常编码，再修改返回默认业务对象； <br>
+ <u>getValue</u>: 从响应包装器类中获取业务数据；
+ <u>createEntryOnSuccess</u>: 服务器端加响应包装器类，当成功执行；
+ <u>createEntryOnException</u>: 服务器端加响应包装器类，当异常执行；


<br>

无论是<i>CatResultProcessor#onHttpError</i>继续抛出、还是<i>AbstractResponesWrapper#checkValid</i>校验失败抛出，都会造成<u>应用层</u>调用feign-interface方法发生异常。

但是在定义feign-interface方法时，方法可以不显示抛出异常，因此在调用时，应当清楚feign-interface方法会<b>隐式抛出异常</b>，需要注意<b>如果发生异常</b>该如何处理。

当<u>应用层</u>执行feign-interface方法后，希望无论是成功还是失败，都要有结果返回，然后应用层再根据执行结果，自行处理异常，那么不应该使用自动拆包装器类！
仅当<u>应用层</u>执行方法后，对于异常流程没有严格要求时，才会建议使用！

<br>

<a id="CatClientBuilders"></a>

### CatClientBuilders

静态方法创建CatClient客户端。可以在非spring环境中使用，也可以在运行过程中手动创建，或者单元测试时期使用；


<br>


<a id="CatHttpRetryConfigurer"></a>

### CatHttpRetryConfigurer

当发生Http异常时，重新连接策略：

+ <u>enable</u>: 是否开启重连；默认false；
+ <u>retries</u>: 重连次数，不包含第一次调用！默认2，实际上最多会调用3次；
+ <u>status</u>: 重连的状态码：多个用逗号隔开；可以为<i>500,501,401</i>或<i>400-410,500-519,419</i>或<i>`*`</i>或<i>any</i>，默认<i>500-520</i>；
+ <u>method</u>: 需要重连的请求方式，多个用逗号隔开；可以为<i>post,get</i> 或<i>`*`</i> 或<i>any</i>，默认<i>any</i>；
+ <u>exception</u>: 需要重连的异常、或其子类；多个用逗号隔开；可以为<i>java.io.IOException</i> 或<i>`*`</i> 或<i>any</i>，默认空；
+ <u>tags</u>: 需要重连的客户端分组，在<i>@CatClient#tags</i>中配置；多个用逗号隔开，默认空；
+ <u>note</u>: 需要重连的API方法标记；多个用逗号隔开；会匹配<i>@CatMethod#notes</i>中的值；当配置的note值，在<i>@CatNote#value</i>中存在时，触发重连；
  例如：<u>note=bugcat</u>匹配<i>@CatNote(key="name", value="bugcat")、@CatNote("bugcat")</i>，不会匹配<i>@CatNote(key="bugcat", value="972245132")</i>
+ <u>noteMatch</u>: 需要重连的API方法标记键值对；在配置文件中，使用单引号包裹的Json字符串，默认值`'{}'`；当noteMatch设置的键值对，在<i>@CatMethod#notes</i>的键值对中完全匹配时，触发重连：
  <u>note-match='{"name":"bugcat","age":"17"}'</u>，会匹配<i>notes={@CatNote(key="name", value="bugcat"), @CatNote(key="age", value="17")}</i>；

> 如果<i>@CatNote</i>采用springEL表达式形式，可以实现运行时，根据入参决定是否需要重连！
> 例如：当设置<u>note=save</u>，其中<i>@CatMethod(notes = @CatNote("#{req.methodName}"))</i>，或者<u>note-match='{"method":"save"}'</u>、对应<i>@CatMethod(notes = @CatNote(key="method", value="#{req.methodName}"))</i>时，如果请求入参req的methodName值为<b>save</b>，会触发重连，其他则不会；


<br>


### 其他说明

#### 1. 方法入参注解

<i>@ModelAttribute、@RequestBody、@RequestParam</i>在同一个方法中，只能三选一，可以和<i>@RequestHeader、@PathVariable</i>共存；

+ <u>@ModelAttribute</u>: 用于标记复杂对象，只能存在一个；表示使用表单方式发送参数；
+ <u>@RequestBody</u>: 用于标记复杂对象，只能存在一个；表示使用POST IO流方式发送参数；可以使用[@CatNote](#CatNote)为参数取别名；
+ <u>@RequestParam</u>: 用于标记基础数据类型、字符串、日期对象，可以有多组；表示使用表单方式发送参数；
+ <u>@RequestHeader</u>: 表示请求头参数，可以有多组；
+ <u>@PathVariable</u>: 表示uri参数，可以有多组；

默认情况下<i>@ModelAttribute、@RequestBody</i>代表把对应的对象转换成表单、或者字符串。但是具体数据格式，需要参考[拦截器](#CatSendInterceptor)、[CatSendProcessor子类](#CatSendProcessor)中的自定义逻辑；

<br>

#### 2. 方法响应类
+ 如果响应是**Object**类型，那么会返回Http的原始响应，无论是否启用了拆<u>响应包装器类</u>；
+ 如果响应是**Date**类型，默认日期格式为`yyyy-mm-dd HH:mi:ss.SSS`，可以使用`@JsonFormat#pattern`、`JSONField#format`修改格式；


<br>

#### 3. 示例
```java
/**
 * 定义一个客户端；
 * 远程服务器地址为：${core-server.remoteApi}，需要从环境变量中获取；
 * 该客户端定义了一个拦截器：TokenInterceptor；
 * 单独配置了http链接、读取超时：3000ms；
 * 其他配置为默认值，参考CatClientConfiguration；
 * 并且该客户端，配置了自动拆包装器ResponseEntityWrapper，实际API接口返回数据类型为ResponseEntity<T>；
 * 	  如果方法的返回类型不是ResponseEntity(除Object类型以外)，一律推定需要使用自动拆包装器！
 * */
@CatResponesWrapper(ResponseEntityWrapper.class)
@CatClient(host = "${core-server.remoteApi}", interceptor = TokenInterceptor.class, connect = 3000, socket = 3000)
public interface TokenRemote {

    /**
     * CatSendProcessor手动创建并且作为方法入参传入；
     * 定义了2个标记：username、pwd，其标记值从环境配置中获取demo.username、demo.pwd对应的参数值；
     * 方法有默认实现，当发生Http异常后，会自动执行，并将结果作为Http请求的结果返回；
     * 虽然添加了自动拆响应包装器类，但是该方法返回数据类型仍然是ResponseEntity，
     * 	  所以依旧按正常流程解析，将原始响应，转成ResponseEntity<String>对象后，再返回；
     * */
    @CatMethod(value = "/cat/getToken", method = RequestMethod.POST,  notes = {@CatNote(key = "username", value = "${demo.username}"), @CatNote(key = "pwd", value = "${demo.pwd}")})
    default ResponseEntity<String> getToken(CatSendProcessor sender) {
        return ResponseEntity.fail("-1", "当前网络异常！");
    }

    /**
     * 定义了1个标记，标记的key和value都是'needToken'这个字符串；
     * 方法返回数据类型String，和配置的包装器类型不一致，因此推定需要自动拆包装器，实际返回数据类型应该为ResponseEntity<String>；
     *    先将原始数据转换ResponseEntity<String>，再获取泛型属性对应值返回；
     * */
    @CatMethod(value = "/cat/sendDemo", method = RequestMethod.POST, notes = @CatNote("needToken"))
    String sendDemo1(@RequestBody Demo demo);

    /**
     * 将token参数，作为请求头参数传输；
     * 该方法返回类型为Object，为内定的特定数据类型，直接返回最原始的响应字符串；
     * */
    @CatMethod(value = "/cat/sendDemo", method = RequestMethod.POST)
    Object sendDemo2(@RequestBody Demo demo, @RequestHeader("token") String token);

    /**
     * 动态url，具体访问地址，由方法入参url确定；
     * */
    @CatMethod(value = "{sendurl}", method = RequestMethod.POST)
    default ResponseEntity<Void> sendDemo3(@PathVariable("sendurl") String url, @RequestHeader("token") String token, @RequestBody String req) {
        return ResponseEntity.fail("-1", "默认异常！");
    }

    /**
     * 给入参OrderInfo取了别名：'order'；
     * 自定义了一个标记，标记的key='routeId'，其value为入参OrderInfo的oid属性值；
     * 实际返回数据类型应该是ResponseEntity<Void>；
     *    如果返回的是基础数据类型，对应的ResponseEntity<基础数据类型包装类>；
     * */
    @CatMethod(value = "/order/edit", notes = @CatNote(key = "routeId", value = "#{order.oid}"), method = RequestMethod.POST)
    void sendDemo4(@CatNote("order") @RequestBody OrderInfo orderInfo);
}
```

<a id="example-CatSendInterceptor"></a>

```java
/**
 * 拦截器
 * */
@Component
public class TokenInterceptor implements CatSendInterceptor {

    /**
     * 使用拦截器修改参数
     * */
    @Override
    public void executeVariableResolver(CatClientContextHolder context, Intercepting intercepting) throws Exception {
        CatSendProcessor sendHandler = context.getSendHandler();
        sendHandler.setTracerId(String.valueOf(System.currentTimeMillis())); //设置日志id，可以通过日志id查询本次请求所有内容。如果不指定，自动使用uuid
        JSONObject notes = sendHandler.getNotes(); //所有的自定义标记都存放在这里
        CatHttpPoint httpPoint = sendHandler.getHttpPoint();
        String need = notes.getString("needToken");//使用note标记是否需要添加签名
        if( CatToosUtil.isNotBlank(need)){
            String token = TokenInfo.getToken();
            httpPoint.getHeaderMap().put("token", token);//将token存入到请求头中
            System.out.println(token);
        }
        intercepting.executeInternal(); // 执行默认参数处理
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
                TokenSend sender = new TokenSend(); // 获取token的时候，显示使用指定CatSendProcessor实例
                ResponseEntity<String> bean = tokenRemote.getToken(sender);
                keepTime = System.currentTimeMillis() + 3600;
                value = bean.getData();
                return value;
            } else {
                return value;
            }
        }
    }

    /**
     * 获取token的时候单独处理器；
     * 一般情况使用拦截器即可，此处演示作用，使用继承CatSendProcessor形式修改参数
     * */
    private static class TokenSend extends CatSendProcessor {
        @Override
        public void postVariableResolver(CatClientContextHolder context){
            String pwd = notes.getString("pwd"); //notes 已经在postConfigurationResolver方法中解析完毕
            String username = notes.getString("username");
            MultiValueMap<String, Object> keyValueParam = this.getHttpPoint().getKeyValueParam();
            keyValueParam.add("username", username);
            keyValueParam.add("pwd", pwd);
            //注意feign-interface中的getToken方法，
            //原getToken方法没有“有效的”入参，但是实际发送Http请求的时候，却有2组请求参数！
            //此特性可以非常灵活调整feign-interface的入参数量、请求方式等
        }
    }
}
```


```java
/**
 * http响应包装器类处理。包装器类为：ResponseEntity<T>；
 * 如果在客户端，则为拆包装器；
 * 如果在服务端，则为加包装器；
 *
 * @see AbstractResponesWrapper
 * @author bugcat
 * */
public class ResponseEntityWrapper extends AbstractResponesWrapper<ResponseEntity>{

    /**
     * 返回包装器类class
     * */
    @Override
    public Class<ResponseEntity> getWrapperClass() {
        return ResponseEntity.class;
    }

    /**
     * 组装包装器类中的实际泛型
     * */
    @Override
    public <T> CatTypeReference getWrapperType(Type type){
        return new CatTypeReference<ResponseEntity<T>>(type){};
    }

    /**
     * 拆包装器，并且自动校验业务是否成功？
     * 本示例直接继续抛出异常；
     * */
    @Override
    public void checkValid(ResponseEntity wrapper) throws Exception {
        if( ResponseEntity.succ.equals(wrapper.getErrCode())){
            //正常
        } else {
            //业务异常记录日志
            CatClientContextHolder contextHolder = CatClientContextHolder.getContextHolder(); //CatClientContextHolder可以获取到本次http请求相关上下文对象，里面包含请求相关的各种参数
            CatClientLogger lastCatLog = contextHolder.getSendHandler().getHttpPoint().getLastCatLog();
            lastCatLog.setErrorMessge("[" + wrapper.getErrCode() + "]" + wrapper.getErrMsg());
            //业务异常，可以直接继续抛出，在公共的异常处理类中，统一处理
            throw new RuntimeException(lastCatLog.getErrorMessge());
        }
    }

    /**
     * 拆包装器，获取包装器类中的业务对象
     * */
    @Override
    public Object getValue(ResponseEntity wrapper) {
        return wrapper.getData();
    }

    /**
     * 服务端成功之后加包装器类
     * */
    @Override
    public ResponseEntity createEntryOnSuccess(Object value, Class methodReturnClass) {
        return ResponseEntity.ok(value);
    }

    /**
     * 服务端当发生异常时加包装器
     * */
    @Override
    public ResponseEntity createEntryOnException(Throwable throwable, Class methodReturnClass) {
        throwable.printStackTrace();
        return ResponseEntity.fail("-1", throwable.getMessage() == null ? "NullPointerException" : throwable.getMessage());
    }
}

```


<br><br>

------

<br><br>

## cat-server 模块

此模块也可以单独使用，但是更多是搭配feign-interface类型的客户端使用；具体的业务类实现feign-interface，业务类的方法便可以通过Http模式调用；

`cat-server`为了支持<u>自动加响应包装器类</u>，以及整合自家的`cat-client`客户端，因此做了看似很复杂的逻辑，但是如果看到文档最后[`catface`](#catface)部分，就会发现很合理了。

<br>

<a id="flow"></a>

```
  1.  feign-interface -----------------------┐
            ↑                                : 
            │                                : 3. asm增强interface
            │                                :
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
                                 6. cglib-controller <══════════  7. http调用
```
1. <u>feign-interface</u>: 包含@PostMapping、@GetMapping、@RequestBody、@RequestParam等注解的Interface接口；
2. <u>CatServer</u>: 具体的业务类，实现了feign-interface，其类上加有`@CatServer`注解；
3. 使用asm对feign-interface增强处理：如果配置了加<u>响应包装器类</u>功能，修改interface的方法返回对象，统一为响应包装器类；如果使用了catface模式，将方法入参列表转成虚拟入参对象；并将feign-interface的、方法级、入参级注解，转移到新Interface上；
4. <u>Enhancer-Interface</u>: 增强后的Interface，与原feign-interface没有任何结构上的关系；
5. 使用cglib对Enhancer-Interface动态代理，生成Controller角色的类，并注册到spring容器；
6. <u>cglib-controller</u>: 动态代理生成的Controller对象，持有<b>CatServer</b>实现类的引用<i>(对象适配模式)</i>；
7. http访问cglib-controller对象方法，cglib-controller预处理入参之后，再执行CatServer对应的方法；

**Enhancer-Interface**、**cglib-controller** 是自动成的类；Http请求指向cglib-controller，cglib-controller做预处理之后，再执行CatServer业务类的方法，看上去就好像Http请求是直接调用到CatServer业务类。待业务处理完毕之后，cglib-controller再判断是否需要添加响应包装器类，最终返回结果；


<br>

<a id="EnableCatServer"></a>
### @EnableCatServer

这个注解表示**启用**CatServer服务端。该注解依赖于`spring容器`，置于任何spring bean之上，例如：springboot项目启动类上、或者任意包含`@Component`注解的类上；

+ <u>value</u>: 扫描的包路径，处于这个目录中的feign-interface都会被注册成服务端。默认值是被标记类的同级目录；
+ <u>classes</u>: 指定服务端class进行注册，优先度高于包路径；
+ <u>configuration</u>: 生成服务端的一些[配置项和默认值](#CatServerConfiguration)；


<br>

<a id="CatServer"></a>
### @CatServer

该注解用于**定义**某个feign-interface为服务端接口。包含：服务端别名、拦截器
、自定义标记、响应处理类；

```java
//feign-interface，@CatMethod也可以换成@RequestMapping
@Api(tags = "Catface - 用户操作api")
@CatResponesWrapper(ResponseEntityWrapper.class)
public interface UserService {

    @ApiOperation("分页查询用户")
    @CatMethod(value = "/user/userPage")
    ResponseEntity<PageInfo<UserInfo>> userPage(@ModelAttribute("vi") UserPageVi vi);

    @ApiOperation("根据用户id查询用户信息")
    @CatMethod(value = "/user/get/{uid}", method = RequestMethod.GET, notes = @CatNote("user"))
    UserInfo userInfo(@PathVariable("uid") String uid);

    @ApiOperation("编辑用户")
    @CatMethod(value = "/user/save", method = RequestMethod.POST, notes = @CatNote(key = "name", value = "#{vi.name}"))
    ResponseEntity<Void> userSave(@RequestBody @CatNote("vi") UserSaveVi vi) throws Exception;

    @ApiOperation("设置用户状态")
    @CatMethod(value = "/user/status", method = RequestMethod.GET)
    void status(@RequestParam("uid") String userId, @RequestParam("status") String status);

}
```

```java
//服务端具体实现类
//此处可以添加事务注解，或其他AOP注解
@CatServer(interceptors = {UserInterceptor2.class, CatServerInterceptor.class, UserInterceptor.class, CatServerInterceptor.GroupOff.class}) //自定义拦截器 + 全局拦截器，无拦截器组
public class UserServiceImpl implements UserService{

    @Override
    public ResponseEntity<PageInfo<UserInfo>> userPage(UserPageVi vi) {
		//具体实现
        return ResponseEntity.ok(page);
    }

    @Override
    public UserInfo userInfo(String uid) {
        //具体实现
        return info;
    }

    @Override
    public ResponseEntity<Void> userSave(UserSaveVi vi) {
        //具体实现
        return ResponseEntity.ok(null);
    }

    @Override
    public void status(String userId, String status) {
        System.out.println("userSave >>> userId=" + userId + ", status=" + status);
    }
}
```


+ <u>value</u>: 服务端组件的别名，默认首字母小写；
+ <u>resultHandler</u>: [结果处理类](#CatResultHandler)，如果配置了加响应包装器类，在此处添加；默认值受[CatServerConfiguration](#CatServerConfiguration)控制；
+ <u>tags</u>: 自定义[标记](#CatNote)；用于[拦截器组](#CatInterceptorGroup)匹配；
+ <u>interceptors</u>: [拦截器](#CatServerInterceptor)；<i>cglib-controller</i>调用<i>CatServer</i>过程中的拦截器链。其值有4种类型：
  1. <u>为空</u>: 表示启用拦截器组，和全局默认拦截器；默认拦截器会被<i>[CatServerConfiguration](#(#CatServerConfiguration))#getServerInterceptor</i>的返回对象替换；
  1. <u>CatServerInterceptor.GroupOff.class</u>: **关闭**拦截器组；
  1. <u>CatServerInterceptor.NoOp.class</u>: **关闭**自定义拦截器和全局默认拦截器；
  1. <u>其他值</u>: 表示启用自定义拦截器；

<a id="CatServerInterceptor-info"></a>

&nbsp; &nbsp; &nbsp;  **拦截器规则：** <br>
&nbsp; &nbsp; &nbsp; 1. `拦截器组`，是在运行中动态匹配，除非在<i>interceptors</i>中配置了`CatServerInterceptor.GroupOff.class`，否则总是生效； <br>
&nbsp; &nbsp; &nbsp; 2. interceptors值如果为空、或者没有`自定义拦截器`类型，则全局默认拦截器生效，可以使用`CatServerInterceptor.NoOp.class`关闭这一功能；若存在任一一个`自定义拦截器`类型，则会忽略全局默认拦截器； <br>
&nbsp; &nbsp; &nbsp; 3. 多个自定义拦截器，按配置的先后顺序执行；如果需要执行全局默认拦截器，可以使用`CatServerInterceptor.class`占位； <br>

&nbsp; &nbsp; &nbsp;  :: <i>@CatServer()</i>: 启用拦截器组，和全局默认拦截器； <br>
&nbsp; &nbsp; &nbsp;  :: <i>@CatServer(interceptors = {A.class, CatServerInterceptor.class})</i>: 启用拦截器组、A拦截器、和全局默认拦截器。此处<i>CatServerInterceptor.class</i>表示全局拦截器的占位符，故A拦截器先于全局执行； <br>
&nbsp; &nbsp; &nbsp;  :: <i>@CatServer(interceptors = {A.class})</i>: 启用拦截器组，和自定义拦截器； <br>
&nbsp; &nbsp; &nbsp;  :: <i>@CatServer(interceptors = {UserInterceptor.GroupOff.class})</i>: 关闭拦截器组；仅全局默认拦截器有效； <br>
&nbsp; &nbsp; &nbsp;  :: <i>@CatServer(interceptors = {UserInterceptor.NoOp.class, A.class})</i>: 仅拦截器组有效，关闭全局拦截器和自定义拦截器； <br>
&nbsp; &nbsp; &nbsp;  :: <i>@CatServer(interceptors = {CatServerInterceptor.class, A.class, UserInterceptor.GroupOff.class})</i>: 关闭拦截器组；全局拦截器、A拦截器有效； <br>
&nbsp; &nbsp; &nbsp;  :: <i>@CatServer(interceptors = {UserInterceptor.NoOp.class, UserInterceptor.GroupOff.class, A.class, B.class})</i>: 关闭所有拦截器； <br>

<br>

<a id="#CatBefore"></a>

### @CatBefore

配置[入参处理器类](#CatParameterResolver)；在执行业务类方法之前执行，用于验证、修改、打印方法入参；


<br>

<a id="CatServerConfiguration"></a>

### CatServerConfiguration

生成服务端的一些配置项和默认值。配合<i>@EnableCatServer</i>使用，可用于修改`@CatServer`、`@CatResponesWrapper`注解的实际默认值；

+ <u>getWrapper</u>: [加包装器类](#AbstractResponesWrapper)处理类，对应<i>@CatResponesWrapper#value</i>；
+ <u>getResultHandler</u>: 默认的[结果处理类](#CatResultHandler)；
+ <u>getServerInterceptor</u>: 全局的默认[拦截器](#CatServerInterceptor)；用于替换<i>CatServer#interceptors</i>中<i>CatServerInterceptor.class</i>位置；
+ <u>getInterceptorGroup</u>: 拦截器组；在运行过程中动态匹配；

<br>

<a id="CatParameterResolver"></a>

### CatParameterResolver

参入预处理类，通过<i>@CatBefore</i>配置。在执行业务类方法之前执行，用于验证、修改、打印方法入参等；

<br>

<a id="CatResultHandler"></a>

### CatResultHandler

feign-interface实现类的返回值处理类。配合响应包装器使用，可以将返回对象、异常转换成统一风格的响应；


<br>

<a id="CatServerInterceptor"></a>

### CatServerInterceptor

在<i>cglib-controller</i>调用<i>CatServer</i>过程中的[自定义拦截器](#CatServerInterceptor-info)；可以用于验证调用权限、必要缓存注入、记录输入输出入参日志等；

+ <u>CatServerInterceptor.NoOp.class</u>: 特殊枚举类，关闭自定义拦截器和全局默认拦截器；
+ <u>CatServerInterceptor.GroupOff.class</u>: 特殊枚举类，关闭拦截器组；
+ <u>preHandle</u>: 某个拦截器可以被多个服务端引用，在执行拦截器内容之前执行，用于判断是否满足前置要求： <br>
  :: false 表示不满足，不执行拦截器内容； <br>
  :: true 表示满足，需要执行拦截器； <br>
  :: 抛出异常，默认情况下表示立刻结束拦截器链，不执行<i>CatServer</i>的对应方法；以上2种布尔返回值，仍然会执行<i>CatServer</i>方法！ <br>
+ <u>postHandle</u>: 拦截器内容；


<br>

<a id="CatInterceptorGroup"></a>

### CatInterceptorGroup

拦截器组，在运行过程中动态匹配，优先于自定义拦截器执行；如果服务端没有配置<i>CatServerInterceptor.GroupOff.class</i>，则总是执行；一般用于记录日志、验证权限使用；

+ <u>matcher</u>: 匹配方法；在运行过程中，根据入参和调用的上下文进行匹配校验；
+ <u>getInterceptorFactory</u>: 当匹配方法返回true后执行，返回满足该分组要求的[拦截器](#CatServerInterceptor)集合；
+ <u>getOrder</u>: 执行顺序，越小越先执行；


<br>

### 其他说明

cglib-controller在调用CatServer业务类[过程](#flow)中，以及CatServer业务类内部发生的异常，均可以通过<i>[CatResultHandler](#CatResultHandler)#onError</i>统一处理。

但是对于Http请求cglib-controller过程中的异常（403、404、500、入参验证不通过）等，只能通过`@ControllerAdvice`处理！
CatServer组件中内置了`CatControllerAssist`异常处理类，可以通过`cat-server.controller-assist.enable=false`关闭；

<br>

如果feign-interface的实现类，被其他类继承了，并且该子类上也存在`@CatServer`注解，那么Http请求会指向子类的方法！


<br><br>

------

<br><br>

## cat-face 模块

把cat-client和cat-server结合使用，好像就可以实现最开始提出的「客户端、服务端用过Interface耦合实现无感知调用」？答案是，也不完全是！<s><i>是「如是」.jpg</i></s>

客户端与服务端共享feign-interface、入参和返回对象的数据类型。其中客户端发起Http请求的url，是通过feign-interface方法上的@CatClient注解获取，服务端注册Controller的url，也是通过@CatClient注解获取，也就是说<i>@CatClient#value()</i>无论返回什么值，客户端总能找到对应的服务端！同样的，Http请求方式也是如此。

既然如此，何不固定请求方式为POST、url通过feign-interface的特征值自动生成，那岂不是可以省下@CatClient注解不用写了？

> 例如url生成规则：自定义命名空间 + feign-interface组件别名 + 方法名；

<br>

至于feign-interface方法入参，转Http请求报文这部分比较麻烦。POST请求只能传输form表单对象、或IO流，考虑到方法入参的复杂性，因此有2种转换方案：
1. 在客户端，将方法的每个入参，都统一转成字符串，然后使用<i>“入参名 = 字符串”</i> 组成form表单对象。在服务端生成cglib-controller时，Http入参全部使用字符串接收，然后再逐个转成实际数据类型，并验证入参有效性；

2. 在客户端，将方法的入参，组合成一个<i>“入参名: 入参对象”</i> 的Map，再将Map序列化成一个Json字符串；在服务端生成cglib-controller时，将原入参列表，转成一个虚拟的入参对象，入参对象的属性，就是原入参名；这样Http请求的Json，可以直接转成虚拟入参对象，并自动执行入参验证框架；

但是由于Interface在编译成class字节码之后，参数名会被擦除<i>(可以使用@CatNote为参数取别名)</i>，实际上的参数名应该是：arg0、arg1、...、argX；故：

&nbsp; &nbsp; &nbsp;  **方案1示意**：

```java
    UserInfo param8(@ApiParam("参数map") Map<String, Object> map,
                    @ApiParam("参数vi1") @Valid UserPageVi vi1,
                    @ApiParam("参数vi2") UserPageVi vi2,
                    @ApiParam("参数status") @NotNull(message = "status 不能为空") @CatNote("status") Boolean status,
                    @ApiParam("参数vi3") @Valid ResponseEntity<PageInfo<UserPageVi>> vi3);
	
	/**
     * 最后Http请求格式为
	 * url：/feign-interface别名/param8
	 * query：
	 * 	 arg0="{\"mapKey\":\"mapValue\"}"
	 *   arg1="{\"name\":\"vi1's name\"}"
	 *   arg2="{\"label\":\"vi2's label\"}"
	 *   arg3="false"
	 *   arg4={\"errCode\":\"1", \"data\":"{\"total\": \"12\", \"list\":"[{\"qname\":\"vi3\"}]"}"}"
	 * */
```

<br>

&nbsp; &nbsp; &nbsp;  **方案2示例：**

```java
    UserInfo param8(@ApiParam("参数map") Map<String, Object> map,
                    @ApiParam("参数vi1") @Valid UserPageVi vi1,
                    @ApiParam("参数vi2") UserPageVi vi2,
                    @ApiParam("参数status") @NotNull(message = "status 不能为空") @CatNote("status") Boolean status,
                    @ApiParam("参数vi3") @Valid ResponseEntity<PageInfo<UserPageVi>> vi3);
	
	/**
     * 最后服务端cglib-controller
	 * url：/feign-interface别名/param8
	 * class Virtual {
	 * 	 private Map<String, Object> arg0;
	 *   @Valid private UserPageVi vi1;
	 *   private UserPageVi vi2;
	 *   private Boolean status;
	 *   @Valid private ResponseEntity<PageInfo<UserPageVi>> arg4;
	 * }
	 * UserInfo param8(@Valid @RequestBody Virtual virtual);
	 * */
```

<br>


第1种方案，实现起来比较容易。缺点是：记录入参日志时，入参全部是字符串，在打印的时候会出现引号转义；服务端Controller的入参都是字符串，swagger生成的接口文档没有详细的字段说明，不够友好； <br>
第2种方案，实现起来比较麻烦。不存在方案1的缺点，但是由于在服务端生成一个虚拟的入参对象，因此在feign-interface中不能出现方法重载！

catface中主要使用<b>`方案2`</b>转换参数；

<br>

### @Catface

标记feign-interface为catface模式；表示将feign-interface中的所有方法都注册成客户端API，方法上、方法入参上可以没有任何注解！

在客户端，方法上的入参列表，会先转换成Map，Map键为`arg0`~`argX`按顺序自动生成，值为入参对象；然后再转换成Json字符串，POST + Json方式发起Http请求。请求的url为：配置的命名空间 + feign-interface别名 + 方法名，因此，这需要feign-interface中的方法名不能相同，即不能存在重载方法！

在服务端，会为每个方法自动生成一个虚拟入参对象，方法入参会转换成虚拟入参对象的属性；这样Http入参Json字符串，可以直接转换成方法入参对应的数据类型；

+ <u>value</u>: feign-interface别名；默认是首字母小写。
+ <u>namespace</u>: 命名空间；统一的url前缀，默认空；

> 最终生成的url为：[/命名空间]/feign-interface别名/方法名

<br>

### @CatNotes

在catface模式下，为feign-interface方法添加自定义标记；

+ <u>value</u>: [自定义标记](#CatNote)；
+ <u>scope</u>: 自定义标记适用范围： <br>
  :: All: 适用于客户端、服务端； <br>
  :: Cilent: 标记仅在作为客户端使用时生效； <br>
  :: Server: 标记仅在作为服务端使用时生效； <br>


<br>

最后feign-interface可以简化成如下形式：

```java
//@Api、@ApiOperation、@ApiParam是swagger框架的注解，如果没有这方面需求，可以删除；
//@NotBlank、@NotNull、@Valid、@Validated是springMVC验证框架注解；

@Api(tags = "Catface - 精简模式")
@Catface
@CatResponesWrapper(ResponseEntityWrapper.class)
public interface FaceDemoService{

    UserInfo queryById(@NotBlank(message = "userId不能为空") String userId);

    @ApiOperation("api - param2")
    ResponseEntity<UserInfo> enable(String userId, Integer status);

    @CatNotes(value = {@CatNote(key = "uname", value = "#{user.name}")}, scope = CatNotes.Scope.Cilent)
    @CatNotes(value = {@CatNote(key = "uid", value = "#{user.id}")}, scope = CatNotes.Scope.Server)
    UserPageVi query(@CatNote("user") UserInfo vi);
    
    PageInfo<UserPageVi> queryByBean(String userId, UserInfo  vi, @CatNote("isStatus")  Boolean status);

    int param8(@ApiParam("参数map") Map<String, Object> map,
                    @ApiParam("参数vi1") @Valid UserPageVi vi1,
                    @ApiParam("参数vi2") UserPageVi vi2,
                    @ApiParam("参数status") @NotNull(message = "status 不能为空") @CatNote("status") Boolean status,
                    @ApiParam("参数vi3") @Valid ResponseEntity<PageInfo<UserPageVi>> vi3);

    default void dosomething(@ApiParam("参数map") Map<String, Object> map, 
                    @ApiParam("参数vi1") @Validated UserPageVi vi1,
                    @ApiParam("参数date") Date date,
                    @ApiParam("参数status") Integer status,
                    @ApiParam("参数decimal") BigDecimal decimal,
                    @ApiParam("参数vi3") @Valid ResponseEntity<PageInfo<UserPageVi>> vi3) {
        CatClientContextHolder holder = CatClientContextHolder.getContextHolder();
        Throwable exception = holder.getException();
        System.out.println("异常：" + exception.getMessage());
        return null;
    }
}
```

<br>

### 其他说明

除了feign-interface中方法不能重载，还要注意一点的是：如果在生产环境上迭代升级feign-interface，假设将<i>FaceDemoService#dosomething</i>方法入参有增减，无论是先更新客户端、还是先更新服务端，都会造成该API接口参数接收会错位！

一般这种情况，可以事先给入参取别名，这样在接收入参时，会根据参数名匹配，而不是参数顺序；或者采用面向对象开发，保持方法入参上只有一个入参对象，增减参数数量，转换成增减对象属性多少的问题。


<br>



<br><br><br>

---

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










