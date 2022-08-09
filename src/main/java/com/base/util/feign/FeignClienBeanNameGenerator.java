package com.base.util.feign;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

/**
 * @ClassName: FeignClienBeanNameGenerator
 * @Description:
 * @author: dh
 * @date: 2020/11/25
 * @version: v1
 */
public class FeignClienBeanNameGenerator  extends AnnotationBeanNameGenerator {

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        //从自定义注解中拿name
        String name = getNameByServiceFindAnntation(definition,registry);
        if(name != null && !"".equals(name)){
            return name;
        }
        //走父类的方法
        return super.generateBeanName(definition, registry);
    }

    private String getNameByServiceFindAnntation(BeanDefinition definition, BeanDefinitionRegistry registry) {
        String beanClassName = definition.getBeanClassName();
        try {
            Class<?> aClass = Class.forName(beanClassName);
            String name = aClass.getSimpleName();
            name = name.substring(0,1).toLowerCase()+name.substring(1);
            return name;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
