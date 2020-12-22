**使用interface定义Controller**

<br>

`FeignClient`作为客户端，其实上是使用`http`请求服务端的接口。

一般使用restful风格的Controller作为服务端代码。

服务端和客户端，分别有自己的输入输出数据模型，通过Json字符串耦合在一起。

<br><br>

反观`dubbo`类的框架，服务端会有一个xml配置文件，暴露提供的Service层信息（高版本中直接可以使用注解申明）
                      
然后在服务端，直接用一个类**实现**这个interface接口，实现接口中方法，即可被远程客户端调用。
                      
看上去就好像是客户端，**注入**一个Service类的interface，就完成了调用远程服务端的Service实现类。


<br>


```markdown
  客户端                    调用                      服务端	                   
 服务消费者   ───────────────────────────────────>   服务提供者           
     │                                                 │ 
     │                                                 │         
     │                                                 │ 
     └──────────────  Service interface                │                                 
	                             │                         │
	                             │                         │   
	                             └─────────────────────────┤   
	                                                       │
                                                       │
	                                                 Service 实现类
```


服务端和客户端，直接通过Service类的**interface**耦合在一起，

如果服务端输入模型、响应模型中，新增了一个字段，那么同版本的客户端可以直接使用这个字段了。


<br><br>

如果`FeignClient`+`Controller`也想和`dubbo`一样，需要解决如何通过interface来定义一个Controller，

于是，cat-server来了...



<br><br>




**cat-client**：猫脸客户端

使用方式类似于`FeignClient`。如果要切换成`cat-client`，仅需要修改启动类注解，和interface上的注解，其他不变；
  
+ 轻量级，只要是Spring项目，都可以使用。仅额外需要引入fastjson；
+ 支持高度的自由扩展：
    - 支持定制日志记录方案，精确到具体的某个API方法的输入输出
    - 支持定制Http工具类，可以为每个API方法配置超时
+ http过程调用完全透明，可进行优化、定制开发
    - 添加签名、添加token
    - 去掉响应对象，统一的外层包裹类


<br><br>


**cat-client-http**：猫脸客户端http插件

按需求引用。目前只有`HttpClient`实现，其他几种http方式，有时间再补上；


<br><br>


**cat-server**：猫脸服务端

使用interface定义Controller *(可以通过类似被`@FeignClient`标记的interface，和它的`实现类`，直接构成服务端)*

搭配`FeignClient`、或者`cat-client`，可以实现如同`dubbo`框架 *(客户端与服务器通过interface耦合：客户端注入interface，服务端实现interface)* 风格



<br><br>



**client-example**：客户端示例

使用方式，和注意事项，参见栗子


<br><br>


**server-example**：服务端示例

使用方式，和注意事项，参见栗子


<br><br>


**server-example-api**：示例代码




<br><br>
<br><br>










