package com.hzh.provider.registry;


import com.hzh.rpc.common.ServiceMeta;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.config.ConfigurationManager;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import org.apache.commons.configuration.AbstractConfiguration;

public class EurekaRegistryService implements RegistryService {


    private final EurekaClient eurekaClient;

    private final ApplicationInfoManager applicationInfoManager;

    public EurekaRegistryService(String registryAddr) {
        // Eureka 注冊
        AbstractConfiguration configInstance = ConfigurationManager.getConfigInstance();
        configInstance.setProperty("eureka.serviceUrl.default", registryAddr + "/eureka/");
        EurekaInstanceConfig instanceConfig = new MyDataCenterInstanceConfig();
        InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
        applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);
        eurekaClient = new DiscoveryClient(applicationInfoManager, new DefaultEurekaClientConfig());
    }

    @Override
    public void register(ServiceMeta serviceMeta) {
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);
    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) {
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
    }

    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode) {
        InstanceInfo instanceInfo = eurekaClient.getNextServerFromEureka(serviceName, false);
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setServiceAddr(instanceInfo.getHostName());
        serviceMeta.setServicePort(instanceInfo.getPort());
        return serviceMeta;
    }

    @Override
    public void destroy() {
        eurekaClient.shutdown();
    }
}
