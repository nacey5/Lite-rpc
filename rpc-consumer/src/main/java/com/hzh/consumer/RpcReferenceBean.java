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

import com.hzh.provider.registry.RegistryFactory;
import com.hzh.provider.registry.RegistryService;
import lombok.Data;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * @author dahuang
 * @version : RpcReferenceBean.java, v 0.1 2023-08-10 11:28 dahuang
 */
@Data
public class RpcReferenceBean implements FactoryBean<Object> {

    private Class<?> interfaceClass;

    private String serviceVersion;

    private String registryType;

    private String registryAddr;

    private long timeout;

    private Object object;
    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    public void init() throws Exception {

        // 生成动态代理对象并赋值给 object
        RegistryService registryService = RegistryFactory.getInstance(this.registryAddr, RegistryType.valueOf(this.registryType));

        this.object = Proxy.newProxyInstance(

            interfaceClass.getClassLoader(),

            new Class<?>[]{interfaceClass},

            new RpcInvokerProxy(serviceVersion, timeout, registryService));
    }
}