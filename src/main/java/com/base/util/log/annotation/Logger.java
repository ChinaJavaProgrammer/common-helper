package com.base.util.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: Logger
 * @Description:    用于打印方法参数的注解
 * @author: dh
 * @date: 2020/11/24
 * @version: v1
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Logger {


 String des() default "";
}
