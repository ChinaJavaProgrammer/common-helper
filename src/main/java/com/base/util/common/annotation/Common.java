package com.base.util.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @ClassName: Common
 * @Description:
 * @author: dh
 * @date: 2020/11/25
 * @version: v1
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Common {


    String value();
}
