package com.hzh.consumer.config;

import com.hzh.consumer.proxy.RpcReferenceBean;
import com.hzh.consumer.annotation.RpcReference;
import com.hzh.rpc.common.RpcConstants;
import com.hzh.rpc.local.annotations.RpcMock;
import com.hzh.rpc.local.annotations.RpcStub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class RpcConsumerPostProcessor implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryPostProcessor {

    private ApplicationContext context;
    private ClassLoader classLoader;
    private final Map<String, BeanDefinition> rpcRefBeanDefinitions = new LinkedHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);
                processFields(clazz);
            }
        }
        registerBeans((BeanDefinitionRegistry) beanFactory);
    }


    private void parseRpcReference(Field field) {
        RpcReference annotation = AnnotationUtils.getAnnotation(field, RpcReference.class);
        if (annotation != null) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcReferenceBean.class);
            builder.setInitMethodName(RpcConstants.INIT_METHOD_NAME);
            builder.addPropertyValue("interfaceClass", field.getType());
            builder.addPropertyValue("serviceVersion", annotation.serviceVersion());
            builder.addPropertyValue("registryType", annotation.registryType());
            builder.addPropertyValue("registryAddr", annotation.registryAddress());
            builder.addPropertyValue("timeout", annotation.timeout());
            builder.addPropertyValue("proxyType", annotation.proxyType()); // 设置代理类型
            builder.addPropertyValue("directAddress", annotation.directAddress());
            BeanDefinition beanDefinition = builder.getBeanDefinition();
            rpcRefBeanDefinitions.put(field.getName(), beanDefinition);
        }
    }

    /**
     * 这部分不算过期，但是本地存根功能目前暂未完成，代码暂留
     *
     * @param field
     */
    @Deprecated
    private void parseLocalAnnotations(Field field) {
        RpcMock mockAnnotation = AnnotationUtils.getAnnotation(field, RpcMock.class);
        if (mockAnnotation != null) {
            // 创建并注册 @RpcMock 对应的bean定义
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(mockAnnotation.value());
            BeanDefinition beanDefinition = builder.getBeanDefinition();
            rpcRefBeanDefinitions.put(field.getName() + "Mock", beanDefinition);
        }

        RpcStub stubAnnotation = AnnotationUtils.getAnnotation(field, RpcStub.class);
        if (stubAnnotation != null) {
            // 创建并注册 @RpcStub 对应的bean定义
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(stubAnnotation.value());
            BeanDefinition beanDefinition = builder.getBeanDefinition();
            rpcRefBeanDefinitions.put(field.getName() + "Stub", beanDefinition);
        }
    }

    private void registerBeans(BeanDefinitionRegistry beanFactory) {
        BeanDefinitionRegistry registry = beanFactory;
        this.rpcRefBeanDefinitions.forEach((beanName, beanDefinition) -> {
            if (context.containsBean(beanName)) {
                throw new IllegalArgumentException("spring context already has a bean named " + beanName);
            }
            registry.registerBeanDefinition(beanName, rpcRefBeanDefinitions.get(beanName));
            log.info("registered RpcReferenceBean {} success.", beanName);
        });
    }

    private void processFields(Class<?> clazz) {
        ReflectionUtils.doWithFields(clazz, this::parseRpcReference);
        //本地存根似乎这种方案不太好，因为这样会导致本地存根的beanDefinition被注册到spring容器中，而本地存根的beanDefinition是不需要注册到spring容器中的
//                ReflectionUtils.doWithFields(clazz, this::parseLocalAnnotations);
    }

}
