package com.base.util.pagehelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: Sort
 * @Description: 排序
 * @author: dh
 * @date: 2020/10/12
 * @version: v1
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sort {


    /**
     * 默认为升序
     * @return
     */
    int defaultValue() default 0;


}
