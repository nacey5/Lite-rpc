# rpc-调用框架
1. 概述
   Mini-RPC是一个为toB服务端设计的RPC通信框架。当前端发起HTTP调用时，会被Consumer端捕获并通过Netty框架使用RPC协议发送到Provider端。
2. 主要功能
   RPC调用：方便地发起RPC远程调用。
   负载均衡：在服务注册时实现负载均衡。
   服务缓存：提供高效的服务缓存功能。
   一致性哈希：确保均匀的请求分布。
   超时重试：在请求超时时重新发起调用。
   异步调用：支持非阻塞的异步调用。
   泛化调用：提供泛化的调用方式。
   心跳检测：确保服务的可用性。
3. 特点
   异步调用：Mini-RPC框架支持高效的异步远程调用，提高服务响应速度。
   服务缓存：使用高效的缓存机制，减少不必要的网络请求和延迟。
   泛化调用
4. 依赖
   要使用Mini-RPC，您需要以下依赖：
~~~xml
<!-- Mini-RPC 核心 -->
<dependency>
    <groupId>com.hzh</groupId>
    <artifactId>rpc-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
<!-- Curator相关依赖 -->
<dependency>
    <groupId>org.apache.curator</groupId>
    <artifactId>curator-framework</artifactId>
</dependency>
...
~~~
完整的依赖列表请参见项目的pom.xml。

5. 快速启动
   启动外部的Zookeeper注册中心。
   启动Provider。
   启动Consumer。

架构图：  
  ![img_1.png](img_1.png)  
关系图:  
  ![img_2.png](img_2.png)  
调用图:  
![img_3.png](img_3.png)  

领域模型图:
~~~mermaid
graph TD
  A[客户端 Client] -->|发起调用| B[服务端 Server]
  B -->|返回结果| A
  C[服务注册中心 Service Registry] -->|提供服务地址| A
  D[服务提供者 Provider] -->|注册服务| C
  A -->|查询服务| C
  E[通信协议 Protocol] -->|定义通信规则| A
  E -->|定义通信规则| B
  F[序列化 Serialization] -->|转换数据| A
  G[反序列化 Deserialization] -->|转换数据| B
  H[负载均衡 Load Balancer] -->|分配请求| B
  I[网络传输 Transport] -->|传输数据| A
  I -->|传输数据| B
  J[服务发现 Service Discovery] -->|发现新服务| A
~~~


目前已是实现功能:
1. 服务注册,其主要通过zk进行注册，eureka的暂时不支持
2. 自定义协议和解析
3. 代理支持：JDK和javassist，可在consumer中选择代理放肆
4. 负载均衡：目前通过一致性哈希环算法保证负载均衡，并且支持两种哈希，建议使用MurmurHash
5. 连接池支持：在之前版本中consumer会频繁的创建，所以在最新的版本，已使用单一对象进行consumer生成，多个rpc调用通过连接池进行资源释放
6. 资源钩子：在之前版本中，consumer和provider的资源释放都是通过jvm的钩子进行释放，但是在最新版本中，已经通过钩子进行资源回收，后续将考虑是否使用spring的@PreDestroy进行资源释放
7. 超时重试：目前已完成超时重试功能，并且失败之后会等待一段时间进行重试
8. 支持泛化调用：目前已完成泛化调用，但是在调用时需要传入接口的全限定名
9. 完成限流改造：已完成使用令牌桶进行限流，但是在使用时需要在consumer中进行配置
10. 添加了服务缓存，保证无状态调用之后添加了服务缓存
11. 新增SPI模块，目前完成了对代理类型和序列化的SPI扩展
12. 新增上下文，目前客户端和服务端的上下文统一，但是简易有关header和body的部分直接获取
13. 新增了服务端的心跳检测，目前心跳检测是通过netty异步线程进行检测
14. 新增服务直连，目前支持客户端指定服务端ip和端口直接发起调用，但是在生产中不建议使用这种方式

