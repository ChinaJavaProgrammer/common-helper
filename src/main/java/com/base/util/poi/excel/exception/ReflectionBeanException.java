package com.base.util.poi.excel.exception;

/**
 * @ClassName: ReflectionBeanException
 * @Description: TODO
 * @Author: dhu
 * @Date: 2022/7/27 9:16
 * @Version: v1
 **/
public class ReflectionBeanException extends RuntimeException{


    public ReflectionBeanException() {
        super();
    }

    public ReflectionBeanException(String message) {
        super(message);
    }

    public ReflectionBeanException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionBeanException(Throwable cause) {
        super(cause);
    }

    protected ReflectionBeanException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
