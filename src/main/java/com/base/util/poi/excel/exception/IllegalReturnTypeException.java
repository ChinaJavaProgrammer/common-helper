package com.base.util.poi.excel.exception;

/**
 * @ClassName: IllegalReturnTypeException
 * @Description: TODO
 * @Author: dhu
 * @Date: 2021/4/15 17:41
 * @Version: v1
 **/
public class IllegalReturnTypeException extends RuntimeException{

    public IllegalReturnTypeException(){

    }

    public IllegalReturnTypeException(String message){
        super(message);
    }

    public IllegalReturnTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalReturnTypeException(Throwable cause) {
        super(cause);
    }

    public IllegalReturnTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
