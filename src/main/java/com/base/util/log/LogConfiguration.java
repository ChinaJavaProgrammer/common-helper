package com.base.util.log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.base.util.log.annotation.Logger;
import com.base.util.pagehelper.annotation.*;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * @ClassName: LogConfiguration
 * @Description:
 * @author: dh
 * @date: 2020/11/24
 * @version: v1
 */
@Configuration
@Aspect
@Slf4j
public class LogConfiguration {

    @Before("@annotation(logger)")
    public void  pageAspect(final JoinPoint joinPoint, Logger logger) throws IllegalAccessException {
        String des = logger.des();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        Parameter[] parameters = targetMethod.getParameters();
        Object [] args = joinPoint.getArgs();
       String value="";
        for(Object o : args){
            if(value.length()==0){
                value = JSONObject.toJSON(o)+"";
            }else {
                value += ","+JSONObject.toJSON(o);
            }
        }
      log.info("------{}:------{}---->{}",targetMethod.getName(),des,value);
    }
}
