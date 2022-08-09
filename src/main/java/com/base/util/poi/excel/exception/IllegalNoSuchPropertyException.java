package com.base.util.poi.excel.exception;

/**
 * @ClassName: IllegalNoSuchAnnotationException
 * @Description: TODO
 * @Author: dhu
 * @Date: 2021/4/15 18:10
 * @Version: v1
 **/
public class IllegalNoSuchPropertyException extends RuntimeException {


    public IllegalNoSuchPropertyException() {
    }

    public IllegalNoSuchPropertyException(String message) {
        super(message);
    }

    public IllegalNoSuchPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalNoSuchPropertyException(Throwable cause) {
        super(cause);
    }

    public IllegalNoSuchPropertyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
