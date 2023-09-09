package com.hzh.rpc.util;

import com.hzh.rpc.exception.RpcException;
import com.hzh.rpc.exception.errorcode.ErrorCode;
import com.hzh.rpc.exception.errorcode.SystemErrorCode;

/**
 * @ClassName Checker
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/9/9 13:45
 * @Version 0.0.1
 **/
public class Checker {
    public static void CheckNotNull(Object point, ErrorCode errorCode,String msg){
        if (point==null){
            throw new RpcException(errorCode,msg);
        }
    }

    public static void CheckNotNull(Object point){
        if (point==null){
            throw new RpcException(SystemErrorCode.PARAM_NULL_ERROR,"the param must not be null");
        }
    }
}
