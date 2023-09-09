package com.hzh.consumer.config;

import com.hzh.consumer.hook.ShutdownHook;
import com.hzh.consumer.hook.ShutdownHookManager;
import com.hzh.consumer.hook.annotations.HookShutdown;
import com.hzh.consumer.proxy.RpcReferenceBean;
import com.hzh.consumer.annotation.RpcReference;
import com.hzh.rpc.common.RpcConstants;
import com.hzh.rpc.exception.errorcode.ReflectionErrorCode;
import com.hzh.rpc.local.annotations.RpcStub;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.hzh.rpc.util.Checker.checkNotNull;

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
            if (StringUtils.isNotBlank(beanClassName)) {
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);
                processFields(clazz);
            }
        }
        parseHook();
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
     * @param clazz
     */
    @Deprecated
    private Boolean parseLocalAnnotations(Class clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            //先解析RpcStub
            RpcStub rpcStub  = AnnotationUtils.getAnnotation(field, RpcStub.class);
            if (rpcStub != null) {
                // 创建并注册 @RpcStub 对应的bean定义
                Class<?> rpcClass = rpcStub.value();
                GenericBeanDefinition rpcBeanDefinition = new GenericBeanDefinition();
                rpcBeanDefinition.setBeanClass(rpcClass);
                // 这里可以设置其他属性，例如作用域、构造参数等
                rpcRefBeanDefinitions.put("",rpcBeanDefinition);
            }
        }
        return true;
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

    private void parseHook(){
        // hook基础包名，后续将更改为配置，但是如果脱离了框架去识别配置文件，会导致启动比较慢的问题
        Reflections reflections = new Reflections("com.hzh.consumer.hook.instance");
        checkNotNull(reflections, ReflectionErrorCode.REFLECTION_ERROR_CODE,"Please check whether your basic package name is <com.hzh.consumer.hook.instance>");
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(HookShutdown.class);
        for (Class<?> clazz : annotatedClasses) {
            if (ShutdownHook.class.isAssignableFrom(clazz)) { // 确保类实现了ShutdownHook接口
                try {
                    ShutdownHook hookInstance = (ShutdownHook) clazz.getDeclaredConstructor().newInstance();
                    HookShutdown annotation = clazz.getAnnotation(HookShutdown.class);
                    ShutdownHookManager.getInstance().addShutdownHook(hookInstance, annotation.priority());
                } catch (Exception e) {
                    // 处理异常，例如日志记录
                    e.printStackTrace();
                }
            }
        }
    }

}
