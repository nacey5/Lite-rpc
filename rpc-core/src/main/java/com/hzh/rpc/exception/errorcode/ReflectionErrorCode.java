package com.hzh.rpc.exception.errorcode;

/**
 * @ClassName ReflectionErrorCode
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/9/9 13:39
 * @Version 0.0.1
 **/
public enum ReflectionErrorCode implements ErrorCode {


    REFLECTION_ERROR_CODE("RPC20001","Reflection failed, check full class name or packageName"),
    METHOD_ERROR_CODE("RPC20002","Method reflection failed, check whether the method exists in the class"),

    FIELD_ERROR_CODE("RPC20003","Field reflection failed, check whether the field exists in the class")

    ;
    private String code;

    private String message;

    ReflectionErrorCode(String code, String message) {
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
