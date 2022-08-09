package com.base.util.poi.excel.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * excel的时间格式化注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelDateFormat {

    /**
     * 时间格式
     * @return String
     */
    String pattern () default "yyyy-MM-dd HH:mm:ss";
}
