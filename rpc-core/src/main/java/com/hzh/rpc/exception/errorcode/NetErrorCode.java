package com.hzh.rpc.exception.errorcode;

/**
 * @ClassName NetErrorCode
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/9/9 13:33
 * @Version 0.0.1
 **/
public enum NetErrorCode implements ErrorCode{


    TIME_OUT_ERROR("RPC10001","Network link timeout"),

    HOST_PORT_ERROR("RPC10002","Host port format error"),

    CONNECT_ERROR("RPC10003","The connection failed, please check that various parameters are configured correctly.")

    ;


    private String code;

    private String message;

    NetErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
