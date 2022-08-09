package com.base.util.image;

/**
 * @ClassName: SameFileNameException
 * @Description:
 * @author: dh
 * @date: 2020/11/20
 * @version: v1
 */
public class SameFileNameException extends IllegalStateException {


    public SameFileNameException() {
    }

    public SameFileNameException(String s) {
        super(s);
    }

    public SameFileNameException(String message, Throwable cause) {
        super(message, cause);
    }

    public SameFileNameException(Throwable cause) {
        super(cause);
    }
}
