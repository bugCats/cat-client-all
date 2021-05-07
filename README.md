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

**`FeignClient`** 作为客户端，其实上是使用 **`http`** 请求服务端的接口。一般使用*restful*风格的*Controller*作为服务端代码。服务端和客户端，分别有自己的输入输出数据模型，通过*Json*字符串耦合在一起。

<br>

反观 **`dubbo`** 类的框架，服务端会有一个*xml*配置文件，暴露提供的*Service*层信息（高版本中直接可以使用注解申明）。然后在 **`服务端`** ，直接用一个类 **实现** 这个*interface*接口，实现*interface*中方法，即可被远程客户端调用。看上去就好像是 **`客户端`** ，**注入**一个*Service*类的*interface*，就完成了调用远程服务端的*Service*具体实现类。


<br>


```markdown
  客户端                    调用                      服务端	                   
 服务消费者   ───────────────────────────────────>   服务提供者           
     │                                                 │ 
     │                                                 │         
     │                                                 │ 
     └──────────────  Service interface                │                                 
	                       │                       │
	                       │                       │   
	                       └───────────────────────┤   
	                                               │
                                                       │
	                                          Service 实现类
```


> 服务端和客户端，直接通过Service类的*interface*耦合在一起，如果服务端输入模型、响应模型中，新增了一个字段，那么同版本的客户端可以直接使用这个字段了。


<br><br>

如果`FeignClient`+`Controller`也想和`dubbo`一样，用一个*interface*将客户端与服务端耦合在一起，改如何处理？

仔细观察*feign-interface*，方法上已经包含了 *@PostMapping*、*@GetMapping*、*@RequestBody*、*@RequestParam* 等注解，作为*Controller*的一些必要元素已经包含了。这使得通过*feign-interface*类定义*Controller*变为可能！


<br>

---

<br>


## cat-server：猫脸服务端

#### 1. 定义一个*feign-interface*

```
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

#### 2. 普通类实现这个*feign-interface*，并且在类上添加`@CatServer`

```
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

<br>

#### 3. 启动类上添加`@EnableCatServer`注解，启用*catserver*服务端
```
@EnableCatServer("com.bugcat")
@SpringBootApplication
public class CatServerApplication {
    public static void main(String[] args) {
        	SpringApplication app = new SpringApplication(CatServerApplication.class);
        	app.run(args);
    }
}
```

至此，便可以通过 *http://ip:port/ctx* + */user/userPage*|*/user/get/xxx*|*/user/status*访问API

<br>

### 其他说明
被`@CatServer`标记的类，最后仍然充当Controller角色。但是又可以像普通*Service*一样，可以被其他组件注入调用！

1. 生成的Controller类，支持swagger框架：  
在interface的方法、输入模型、输出模型上，使用swagger注解，同样可以生成API文档，并且能正常调用

2. 可以为每个生成的Controller单独配置拦截器：   
仅当通过API调用，作为Controller角色时，拦截器生效；而一般情况，作为组件注入调用时，不拦截！

3. 可以为Controller的响应，自动加上包装器类：  
很多情况下，API接口的响应是同一个类，具体的业务数据，是响应类的一个泛型属性。CatServer组件，可以让开发人员专注业务数据，程序将业务对象自动封装到公共的响应类中

4. 通过类的继承特性，实现对API接口升级：   
例如有个新类`UserServiceExtImpl`继承`UserServiceImpl`，并且重写了父类方法，那么这个API便升级成了子类重新的方法！

5. 可搭配`FeignClient`使用：    
可以实现如同`dubbo`框架风格，客户端与服务器通过*interface*耦合。*客户端注入interface，服务端实现interface*

6. 可以像普通Service类一样，支持`@Transactional`事务配置。

<br>
<br>

---

## cat-client：猫脸客户端 
和`cat-server`搭配使用的客户端，也可以单独使用的轻量级*仿FeignClient*组件。  

### 使用方式类似于`FeignClient`   

#### 1. 定义客户端    

可以在*interface*上直接添加 **@CatClient** ，通过注解扫描加载：   
```java
@CatClient(host = "${user-server}", connect = 3000, logs = RequestLogs.All2)
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

#### 2. 通过 CatClients 子类定义 
通过**CatClients**子类，集中批量定义：  

```java
public interface Remotes extends CatClients {


    @CatClient(host = "${core-server.remoteApi}", connect = 3000, socket = 3000)
    UserService userService();

}

```
> 这样的好处在于，不用在*interface*类上添加额外注解，保持*interface*类干净，提高复用性。


<br>

#### 3. 启动类上添加`@EnableCatClient`注解，启用*catclient*客户端

```java
@EnableCatClient(value = "com.bugcat",  classes = Remotes.class)
@SpringBootApplication
public class CatClientApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CatClientApplication.class);
        app.run(args);
    }
}

```

#### 4. 调用方式
通过自动注入，和普通Service一样使用。或者直接通过静态方法`CatClientUtil.proxy`生成
```java
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    
    @PostConstruct
    public void init(){
        UserInfo info = userService.userInfo(666L, "1");
        UserService userService2 = CatClientUtil.proxy(UserService.class);
    }
}
```

<br>


### 其他说明

为什么在已经有了`FeignClient`的情况下，还要开发`cat-client`？
bugcat在第一次使用FeignClient时，被他的设计理念惊艳到。一边读源码学习，一边决心按照自己的想法写点什么。等回过神的时候，cat-client已经成型。
不要嘲讽bugcat重复造轮子，你在嘲笑我时候，我已经把相关技术熟记于心。同时也借此机会，诞生了cat-server！

1. 轻量级    
整个项目，核心代码不到3000行。只要是springboot项目，都可以使用，仅额外需要引入fastjson。（Springmvc项目，也仅需要改一下自定义扫描地方）

2. 支持扩展    
http模块、对象序列化与反序列化模块、负载均衡模块，全部是预留接口、使用插件方式引入，可以自由搭配、按需使用；

3. 精细化控制每个API方法   
精确到具体的某个API方法，设置http链接超时，是否打印输入、输出日志；调用前添加签名、添加token；http失败回调、http失败重连机制等。

4. 自动去包装器类   
这是`cat-server`自动添加包装器的逆操作。开发人员应该关注具体的业务代码，重复的事、公共的事，交给程序做。


<br><br>

---

## 项目介绍

#### cat-client-cloud
cat-client 负载均衡服务组件。需要自行实现*ServerChoose*接口；nacos有时间补上；
> 有需要搭载负载均衡的可以引入。

<br>


#### cat-client-http
cat-client http组件 -- HttpClient，其他几种http方式，有时间再补上；
> 项目恰好支持HttpClient的可以引入，否则自行实现*CatHttp*接口；

<br>


#### cat-client-jackson
cat-client 对象序列化组件 -- jackson；cat-client项目中内置fastjson序列化。
> 当项目使用jackson作为序列化工具，需要引入该组件。如果不是fastjson、jackson，需要自行实现*CatJsonResolver*接口

<br>


#### cat-client
cat-client客户端核心模块；   
> 有需要使用cat-client客户端的必须引入

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

#### 更多示例
https://blog.csdn.net/qq_41399429/article/details/93488645

----

<br><br>

~~如果觉得还不错，点个赞再走呗~~

<br><br>










