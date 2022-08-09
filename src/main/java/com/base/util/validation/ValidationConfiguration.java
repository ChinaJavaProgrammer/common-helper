package com.base.util.validation;

import com.alibaba.fastjson.JSONObject;
import com.base.util.log.annotation.Logger;
import com.base.util.validation.annotation.Validate;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @ClassName: ValidationConfiguration
 * @Description:
 * @author: dh
 * @date: 2020/11/24
 * @version: v1
 */
@Configuration
@Aspect
public class ValidationConfiguration {


    @Before("@annotation(validate)")
    public void  pageAspect(final JoinPoint joinPoint, Validate validate) throws IllegalAccessException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        Parameter[] parameters = targetMethod.getParameters();
        Object [] args = joinPoint.getArgs();
    }
}
