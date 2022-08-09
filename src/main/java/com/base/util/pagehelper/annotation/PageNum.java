package com.base.util.pagehelper.annotation;

import com.base.util.adapter.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: PageNum
 * @Description:  分页参数的开始页
 * @author: dh
 * @date: 2020/10/12
 * @version: v1
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PageNum {


    /**
     * 默认开始页为-1不分页
     * @return
     */
    int defaultValue() default -1;



}
