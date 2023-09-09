package com.hzh.rpc.exception.errorcode;

/**
 * @ClassName SystemErrorCode
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/9/9 13:54
 * @Version 0.0.1
 **/
public enum SystemErrorCode implements ErrorCode{

    PARAM_NULL_ERROR("SY00001","Parameter cannot be null")

    ;
    private String code;

    private String message;

    SystemErrorCode(String code, String message) {
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
