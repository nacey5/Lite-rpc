package com.hzh.consumer.pool;


import io.netty.bootstrap.Bootstrap;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
/**
 * @ClassName RpcConnectionFactory
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/17 14:27
 * @Version 0.0.1
 **/
public class RpcConnectionFactory implements PooledObjectFactory<RpcConnection> {
    private final Bootstrap bootstrap;
    private final String host;
    private final int port;

    public RpcConnectionFactory(Bootstrap bootstrap, String host, int port) {
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
    }


    // ... 实现其他方法，如destroy、validate等 ...
    @Override
    public void activateObject(PooledObject<RpcConnection> pooledObject) throws Exception {
        // 可以添加重新初始化连接的代码，如果需要的话，用户重新激活对象
    }

    @Override
    public void destroyObject(PooledObject<RpcConnection> pooledObject) throws Exception {
        RpcConnection connection = pooledObject.getObject();
        if (connection != null) {
            connection.close(); // 假设RpcConnection有一个close方法来关闭连接
        }
    }

    @Override
    public void destroyObject(PooledObject<RpcConnection> p, DestroyMode destroyMode) throws Exception {
        destroyObject(p); // 使用上面的destroyObject方法
    }

    //core
    @Override
    public PooledObject<RpcConnection> makeObject() throws Exception {
        RpcConnection connection = new RpcConnection(bootstrap, host, port);
        return new DefaultPooledObject<>(connection);
    }

    @Override
    public void passivateObject(PooledObject<RpcConnection> pooledObject) throws Exception {

    }

    @Override
    public boolean validateObject(PooledObject<RpcConnection> pooledObject) {
        RpcConnection connection = pooledObject.getObject();
        // RpcConnection有一个isConnected方法来检查连接是否仍然有效
        return connection != null && connection.isConnected();
    }
}

