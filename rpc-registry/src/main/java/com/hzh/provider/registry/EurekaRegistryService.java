package com.hzh.provider.registry;


import com.hzh.rpc.common.ServiceMeta;


/**
 * Eureka 注册中心服务
 * 目前这个类已经不用了，因为我不知道他为什么会引发错误
 * 并且需要引入 com.netflix.eureka:eureka-client
 * 所以目前先暂时放弃使用eureka服务
 */
@Deprecated
public class EurekaRegistryService implements RegistryService {


//    private final EurekaClient eurekaClient;
//
//    private final ApplicationInfoManager applicationInfoManager;

    public EurekaRegistryService(String registryAddr) {
        // Eureka 注冊
//        AbstractConfiguration configInstance = ConfigurationManager.getConfigInstance();
//        configInstance.setProperty("eureka.serviceUrl.default", registryAddr + "/eureka/");
//        EurekaInstanceConfig instanceConfig = new MyDataCenterInstanceConfig();
//        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
//        applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);
//        eurekaClient = new DiscoveryClient(applicationInfoManager, new DefaultEurekaClientConfig());
    }

    @Override
    public void register(ServiceMeta serviceMeta) {
//        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);
    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) {
//        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
    }

    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode) {
//        InstanceInfo instanceInfo = eurekaClient.getNextServerFromEureka(serviceName, false);
//        ServiceMeta serviceMeta = new ServiceMeta();
//        serviceMeta.setServiceAddr(instanceInfo.getHostName());
//        serviceMeta.setServicePort(instanceInfo.getPort());
//        return serviceMeta;
        return null;
    }

    @Override
    public void destroy() {
//        eurekaClient.shutdown();
    }
}
