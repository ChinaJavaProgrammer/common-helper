package com.base.util.pagehelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: PageSize
 * @Description:    每页大小
 * @author: dh
 * @date: 2020/10/12
 * @version: v1
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PageSize {


    /**
     *  默认每页大小为-1 不分页
     * @return
     */
    int defaultValue() default -1;


}
