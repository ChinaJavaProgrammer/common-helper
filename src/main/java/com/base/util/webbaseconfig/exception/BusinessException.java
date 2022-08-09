package com.base.util.webbaseconfig.exception;

/**
 * @ClassName: BusinessException
 * @Description:    业务错误异常，用于全局异常捕获
 * @author: dh
 * @date: 2020/11/30
 * @version: v1
 */
public class BusinessException extends RuntimeException {



    private int code;

    private String message;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
        code = 500;
        message= message;

    }

    public BusinessException(String format, Object... args) {
        super(String.format(format,args));
    }

    public BusinessException(int errorCode,String message){
        this(String.format("{\"code\":%s,\"message\":\"%s\"}",errorCode,message));
        code = errorCode;
        message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    protected BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
