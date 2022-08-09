package com.base.util.poi.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: ExcelCustomValidateMethod
 * @Description: TODO
 * @Author: dhu
 * @Date: 2021/12/3 9:59
 * @Version: v1
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelCustomValidateMethod {

    String columnName();
}
