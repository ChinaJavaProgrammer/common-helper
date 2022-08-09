package com.base.util.webbaseconfig.controller;

import java.util.Calendar;
import java.util.Date;

/**
 * @author dh
 */
public class BaseRespData {

    protected int code;
    protected String message;
    protected Date timestamp = Calendar.getInstance().getTime();

    public BaseRespData() {
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BaseRespData [code=" + this.code + ", message=" + this.message + ", timestamp=" + this.timestamp + "]";
    }
}
