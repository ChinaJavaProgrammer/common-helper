package com.base.util.mybatisplus.annotation;

import com.base.util.mybatisplus.QueryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: QueryWrapperField
 * @Description:
 * @author: dh
 * @date: 2020/12/1
 * @version: v1
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryWrapperField {

    /**
     * 对应的数据库字段
     * @return
     */
    String dataBaseColumn() default "";

    /**
     * 查询方式
     * @return
     */
    QueryType queryType() default QueryType.EQUAL;


    /**
     * 其他条件
     * @return
     */
    String condition() default "0xffff";

    /**
     * between and 的情况 的参数先后顺序
     * @return
     */
    int paramIndex() default -1;

}
