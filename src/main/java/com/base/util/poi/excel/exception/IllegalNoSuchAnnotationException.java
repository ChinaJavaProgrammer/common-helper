package com.base.util.poi.excel.exception;

/**
 * @ClassName: IllegalNoSuchAnnotationException
 * @Description: TODO
 * @Author: dhu
 * @Date: 2021/4/15 18:10
 * @Version: v1
 **/
public class IllegalNoSuchAnnotationException extends RuntimeException {


    public IllegalNoSuchAnnotationException() {
    }

    public IllegalNoSuchAnnotationException(String message) {
        super(message);
    }

    public IllegalNoSuchAnnotationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalNoSuchAnnotationException(Throwable cause) {
        super(cause);
    }

    public IllegalNoSuchAnnotationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
