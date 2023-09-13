package com.hzh.rpc.spi.sp_2;

import java.io.IOException;

/**
 * @ClassName Wrapper
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/9/14 1:47
 * @Version 0.0.1
 **/
public interface Wrapper <T>{
    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;

    byte getType();
}
