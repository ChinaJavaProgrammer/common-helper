package com.base.util.webbaseconfig.globalexception;

import com.base.util.feign.FeignClienBeanNameGenerator;
import com.base.util.feign.FeignClientProxy;
import com.base.util.feign.FindFeignClientPathScanHandle;
import com.base.util.webbaseconfig.annotation.EnableGlobalExceptions;
import com.base.util.webbaseconfig.controller.ErrorController;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: FeignConfiguration
 * @Description:
 * @author: dh
 * @date: 2020/11/24
 * @version: v1
 */
public class GlobalExceptionConfiguration implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware, ApplicationContextAware {



    private ClassLoader classLoader;

    private Environment environment;

    private ResourceLoader resourceLoader;

    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext =applicationContext;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader=classLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment=environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader=resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> defaultAttrs = importingClassMetadata
                .getAnnotationAttributes(EnableGlobalExceptions.class.getName(), false);
        Class [] exceptions = (Class[]) defaultAttrs.get("exceptions");
        String message = (String) defaultAttrs.get("message");
        boolean cascadeFindException = (boolean) defaultAttrs.get("cascadeFindException");
        ErrorController.setGlobalExceptionType(cascadeFindException);
        ErrorController.setMessage(message);
        if(exceptions.length!=0){
            ErrorController.setGlobalException(exceptions);
        }
    }

}
