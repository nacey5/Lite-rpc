package com.hzh.rpc.exception;

import com.hzh.rpc.exception.errorcode.ErrorCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * @ClassName RpcException
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/9/9 13:24
 * @Version 0.0.1
 **/
public class RpcException extends RuntimeException{
    private ErrorCode errorCode;

    private String message;

    private Object[] args;

    public RpcException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(null, cause);
        Assert.notNull(errorCode, "errorCode can not be null");
        this.errorCode = errorCode;
        this.args = args;
        message = String.format(errorCode.getMessage(), args);

    }

    public RpcException(ErrorCode errorCode, Object... args) {
        super(StringUtils.EMPTY);
        Assert.notNull(errorCode, "errorCode can not be null");
        this.errorCode = errorCode;
        this.args = args;
        message = String.format(errorCode.getMessage(), args);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Object[] getArgs() {
        return args;
    }
}
