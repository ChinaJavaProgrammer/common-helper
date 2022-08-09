package com.base.util.feign;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: FeignConfiguration
 * @Description:
 * @author: dh
 * @date: 2020/11/24
 * @version: v1
 */
public class FeignConfiguration implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware, ApplicationContextAware {



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
        scannerFeignClient(importingClassMetadata,registry);
    }

    private void scannerFeignClient(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry){
        System.out.println("---------------------------------");
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        Set<String> basePackages;
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(
                FeignClient.class);
        scanner.addIncludeFilter(annotationTypeFilter);
        basePackages = getBasePackages(importingClassMetadata);
        System.out.println("package:");
        //自定义的包扫描器
        FindFeignClientPathScanHandle scanHandle = new  FindFeignClientPathScanHandle(registry,false);
        scanHandle.setResourceLoader(this.resourceLoader);
        scanHandle.addIncludeFilter(new AnnotationTypeFilter(FeignClient.class));
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner
                    .findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    // verify annotated class is an interface
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    Assert.isTrue(annotationMetadata.isInterface(),
                            "@FeignClient can only be specified on an interface");

                    Map<String, Object> attributes = annotationMetadata
                            .getAnnotationAttributes(
                                    FeignClient.class.getCanonicalName());

//                    registerFeignClient(registry, annotationMetadata, attributes);
                }
            }
        }
        if(resourceLoader != null){
            scanHandle.setResourceLoader(resourceLoader);
        }
        //这里实现的是根据名称来注入
        scanHandle.setBeanNameGenerator(new FeignClienBeanNameGenerator());
//
//        for (String basePackage : basePackages) {
//            Set<BeanDefinition> candidateComponents = scanHandle
//                    .findCandidateComponents(basePackage);
//            for (BeanDefinition candidateComponent : candidateComponents) {
//                if (candidateComponent instanceof AnnotatedBeanDefinition) {
//                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
//                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
////                    registerLogService(registry, annotationMetadata);
//                }
//            }
//        }

        //扫描指定路径下的接口
//        Set<BeanDefinitionHolder> beanDefinitionHolders = scanHandle.doScan(basePackages);
//        System.out.println("扫描出来的Bean："+beanDefinitionHolders.size());
//        if(beanDefinitionHolders!=null && beanDefinitionHolders.size()>0){
//            Iterator<BeanDefinitionHolder> iterator = beanDefinitionHolders.iterator();
//            while(iterator.hasNext()){
//                BeanDefinitionHolder beanDefinitionHolder = iterator.next();
//                BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
//                if(registry.containsBeanDefinition(beanDefinitionHolder.getBeanName())){
//                    System.out.println(beanDefinitionHolder.getBeanName());
//                    iterator.remove();
//                }else{
//                    System.out.println("注册bean："+beanDefinitionHolder.getBeanName());
//                    Enhancer enhancer = new Enhancer();
//                    enhancer.setSuperclass(BeanDefinition.class);
//                    BeanDefinition finalBeanDefinition = beanDefinition;
//                    enhancer.setCallback((InvocationHandler) (o, method, objects) -> {
//                        if(method.getName().equals("getSource")){
//                            return proxyFeignClient(finalBeanDefinition.getBeanClassName());
//                        }
//                        return method.invoke(finalBeanDefinition,objects);
//                    });
//                    beanDefinition = (BeanDefinition) enhancer.create();
//                    registry.registerBeanDefinition(beanDefinitionHolder.getBeanName(),beanDefinition);
//                }
//            }
//        }
    }


    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }

    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Set<String> basePackages = new HashSet<>();
        if(importingClassMetadata.hasAnnotation(ComponentScan.class.getName())){
            Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(ComponentScan.class.getName());

            for(String pkg :(String[]) annotationAttributes.get("value")){
                if(StringUtils.hasText(pkg)){
                    basePackages.add(pkg);
                }
            }
            for(String pkg :(String[]) annotationAttributes.get("basePackages")){
                if(StringUtils.hasText(pkg)){
                    basePackages.add(pkg);
                }
            }


                annotationAttributes = importingClassMetadata.getAnnotationAttributes(SpringBootApplication.class.getName());
            for(String pkg :(String[]) annotationAttributes.get("scanBasePackages")){
                if(StringUtils.hasText(pkg)){
                    basePackages.add(pkg);
                }
            }

        }else{
            Map<String, Object>   annotationAttributes = importingClassMetadata.getAnnotationAttributes(SpringBootApplication.class.getName());
            for(String pkg :(String[]) annotationAttributes.get("scanBasePackages")){
                if(StringUtils.hasText(pkg)){
                    basePackages.add(pkg);
                }
            }
        }


        if (basePackages.isEmpty()) {
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

    private Object proxyFeignClient(String className){

        return new FeignClientProxy(applicationContext,environment,className);
    }

//    private void registerFeignClient(BeanDefinitionRegistry registry,
//                                     AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
//        String className = annotationMetadata.getClassName();
//        BeanDefinitionBuilder definition = BeanDefinitionBuilder
//                .genericBeanDefinition(FeignClientFactoryBean.class);
//        validate(attributes);
//        definition.addPropertyValue("url", getUrl(attributes));
//        definition.addPropertyValue("path", getPath(attributes));
//        String name = getName(attributes);
//        definition.addPropertyValue("name", name);
//        definition.addPropertyValue("type", className);
//        definition.addPropertyValue("decode404", attributes.get("decode404"));
//        definition.addPropertyValue("fallback", attributes.get("fallback"));
//        definition.addPropertyValue("fallbackFactory", attributes.get("fallbackFactory"));
//        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
//
//        String alias = name + "FeignClient";
//        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
//
//        boolean primary = (Boolean)attributes.get("primary"); // has a default, won't be null
//
//        beanDefinition.setPrimary(primary);
//
//        String qualifier = getQualifier(attributes);
//        if (StringUtils.hasText(qualifier)) {
//            alias = qualifier;
//        }
//
//        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className,
//                new String[] { alias });
//        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
//    }
}
