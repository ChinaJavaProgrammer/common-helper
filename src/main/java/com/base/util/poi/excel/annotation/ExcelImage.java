package com.base.util.poi.excel.annotation;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * excel的图片处理
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelImage {

    /**
     * 如果是excel导入需要下载图片那么需要指定图片的访问前缀可以是${vvvv.ffff}
     * @return String
     */
    String imageVisitPrev() default "";

    /**
     * 如果是excel导入并且需要下载那么需要指定将文件下载到哪个位置，可以是Spring的占位符号${vvvv.ffff}
     * @return String
     */
    String imageDownPath() default "";

}
