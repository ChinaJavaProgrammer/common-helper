package com.base.util.pagehelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 排序字段
 * @ClassName: OrderField
 * @Description:
 * @author: dh
 * @date: 2020/10/12
 * @version: v1
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OrderField {


    /**
     * 默认排序字段值为空
     * @return
     */
    String defaultValue() default "";

    boolean needCame() default false;
}
