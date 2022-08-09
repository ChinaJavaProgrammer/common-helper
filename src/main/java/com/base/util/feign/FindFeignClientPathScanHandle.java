package com.base.util.feign;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * @ClassName: FindFeignClientPathScanHandle
 * @Description:
 * @author: dh
 * @date: 2020/11/25
 * @version: v1
 */
public class FindFeignClientPathScanHandle extends ClassPathBeanDefinitionScanner {


    public FindFeignClientPathScanHandle(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public FindFeignClientPathScanHandle(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    public FindFeignClientPathScanHandle(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment) {
        super(registry, useDefaultFilters, environment);
    }

    public FindFeignClientPathScanHandle(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment, ResourceLoader resourceLoader) {
        super(registry, useDefaultFilters, environment, resourceLoader);
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        //添加过滤条件，这里是只添加了@NRpcServer的注解才会被扫描到
        addIncludeFilter(new AnnotationTypeFilter(FeignClient.class));
        //调用spring的扫描
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        return beanDefinitionHolders;
    }
}
