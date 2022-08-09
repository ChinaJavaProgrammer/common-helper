package com.base.util.webbaseconfig.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dhu
 *
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessRequestBody {

    /**
     * body默认是否需要穿参数
     * @return
     */
    boolean required() default true;

    /**
     * 将参数以驼峰命名的方式封装
     * @return
     */
    boolean needCamel() default false;

    /**
     * 只有一个参数的json数组是否需要封装成List对象
     * @return
     */
    boolean needOneParameterToList() default false;

    /**
     * 取值的字段
     * @return
     */
    String fetchField() default "";

    boolean printMetaData() default true;
}
