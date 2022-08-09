package com.base.util.adapter.annotation;

import com.base.util.adapter.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: PoJoName
 * @Description:
 * @author: dh
 * @date: 2020/10/12
 * @version: v1
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PoJoName {


    /**
     * 要映射的字段名称
     * @return
     */
    String value() default "";

    /**
     * 要映射的字段类型
     * @return
     */
    FieldType type() default FieldType.NONE;

}
