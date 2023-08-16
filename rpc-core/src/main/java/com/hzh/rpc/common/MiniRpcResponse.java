package com.hzh.rpc.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class MiniRpcResponse implements Serializable {
    private Object data;
    private String message;
}
