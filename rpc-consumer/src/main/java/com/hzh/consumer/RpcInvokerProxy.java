/*
 * Aloudata.com Inc.
 * Copyright (c) 2021-2023 All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hzh.consumer;

import com.hzh.provider.registry.RegistryService;
import com.hzh.rpc.common.MiniRpcRequest;
import com.hzh.rpc.common.MiniRpcResponse;
import com.hzh.rpc.protocol.MiniRpcProtocol;
import com.hzh.rpc.protocol.MsgHeader;
import com.hzh.rpc.serialization.SerializationTypeEnum;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author dahuang
 * @version : RpcInvokerProxy.java, v 0.1 2023-08-11 16:32 dahuang
 */
@AllArgsConstructor
public class RpcInvokerProxy implements InvocationHandler {

    private final String serviceVersion;
    private final long timeout;
    private final RegistryService registryService;
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MiniRpcProtocol<MiniRpcRequest> protocol = new MiniRpcProtocol<>();
        MsgHeader header = new MsgHeader();
        long requestId = MiniRpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
        header.setMsgType((byte) MsgType.REQUEST.getType());
        header.setStatus((byte) 0x1);
        protocol.setHeader(header);

        MiniRpcRequest request = new MiniRpcRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParams(args);
        protocol.setBody(request);

        RpcConsumer rpcConsumer = new RpcConsumer();
        MiniRpcFuture<MiniRpcResponse> future = new MiniRpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
        MiniRpcRequestHolder.REQUEST_MAP.put(requestId, future);
        rpcConsumer.sendRequest(protocol, this.registryService);

        // TODO hold request by ThreadLocal


        return future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS).getData();
    }
}