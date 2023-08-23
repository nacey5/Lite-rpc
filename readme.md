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


目前已是实现功能:
1. 服务注册,其主要通过zk进行注册，eureka的暂时不支持
2. 自定义协议和解析
3. 代理支持：JDK和javassist，可在consumer中选择代理放肆
4. 负载均衡：目前通过一致性哈希环算法保证负载均衡，并且支持两种哈希，建议使用MurmurHash
5. 连接池支持：在之前版本中consumer会频繁的创建，所以在最新的版本，已使用单一对象进行consumer生成，多个rpc调用通过连接池进行资源释放
6. 资源钩子：在之前版本中，consumer和provider的资源释放都是通过jvm的钩子进行释放，但是在最新版本中，已经通过钩子进行资源回收，后续将考虑是否使用spring的@PreDestroy进行资源释放
7. 超时重试：目前已完成超时重试功能，并且失败之后会等待一段时间进行重试

