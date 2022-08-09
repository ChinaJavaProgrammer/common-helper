package com.base.util.webbaseconfig.annotation;

import com.base.util.webbaseconfig.controller.ErrorController;
import com.base.util.webbaseconfig.globalexception.GlobalExceptionConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @ClassName: EnabledGlobalExceptions
 * @Description:    开启全局异常处理
 * @author: dh
 * @date: 2020/12/2
 * @version: v1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ErrorController.class, GlobalExceptionConfiguration.class})
public @interface EnableGlobalExceptions {


    /**
     * 哪些异常需要被拦截然后通过获取Throwable.getMessage()返回提示信息
     * @return
     */
    Class [] exceptions() default {};

    /**
     * 不需要拦截的异常返回的提示信息默认为
     * @return
     */
    String message()default "服务器错误，请联系管理员";

    /**
     * 对于全局异常的捕获是通过继承关系捕获还是直接通过Class对象捕获
     * @return
     */
    boolean cascadeFindException() default true;
}
