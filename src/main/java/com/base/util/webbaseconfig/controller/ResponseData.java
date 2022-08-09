package com.base.util.webbaseconfig.controller;

/**
 * @author dh
 * @param <T>
 */
public class ResponseData<T> extends BaseRespData {

    protected T data;

    public ResponseData() {
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
