

**cat-client**：猫脸客户端

使用方式类似于`FeignClient`。如果要切换成`cat-client`，仅需要修改启动类注解，和interface上的注解，其他不变；
  
+ 轻量级，只要是Spring项目，都可以使用。仅额外需要引入fastjson；
+ 支持高度的自由扩展：
    - 支持定制日志记录方案，精确到具体的某个API方法的输入输出
    - 支持定制Http工具类，可以为每个API方法配置超时
+ http过程调用完全透明，可进行优化、定制开发
    - 添加签名、添加token
    - 去掉响应对象，统一的外层包裹类


<br>
<br>

**cat-client-http**：猫脸客户端http插件

按需求引用。目前只有`HttpClient`实现，其他几种http方式，有时间再补上；


<br>
<br>

**cat-server**：猫脸服务端

可以通过类似被`@FeignClient`标记的interface，和它的`实现类`，直接构成服务端

搭配`FeignClient`、或者`cat-client`，可以实现如同`dubbo`框架 *(客户端与服务器通过interface耦合：客户端注入interface，服务端实现interface)* 风格



<br>
<br>


**client-example**：客户端示例

使用方式，和注意事项，参见栗子

<br>
<br>

**server-example**：服务端示例

使用方式，和注意事项，参见栗子



<br>
<br>

**server-example-api**：示例代码



<br>
<br>
<br>
<br>









