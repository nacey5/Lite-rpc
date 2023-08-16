package com.hzh.provider.registry.loadbalancer;

import java.util.List;

public interface ServiceLoadBalancer<T> {
    T select(List<T> servers, int hashCode);
}