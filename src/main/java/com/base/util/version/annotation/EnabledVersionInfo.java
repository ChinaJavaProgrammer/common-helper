package com.base.util.version.annotation;


import com.base.util.version.VersionConfig;
import com.base.util.webbaseconfig.controller.ErrorController;
import com.base.util.webbaseconfig.globalexception.GlobalExceptionConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({VersionConfig.class})
public @interface EnabledVersionInfo {

    String versionFile() default "version.txt";
}
