package com.base.util.springbootbaseconfigration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;


/**
 * @ClassName: MyRequestBodyAdd
 * @Description: TODO
 * @Author: dhu
 * @Date: 2021/4/15 11:06
 * @Version: v1
 **/
@ConditionalOnBean(WebMvcConfigurer.class)
public class MyRequestBodyAdd {


    @Autowired
    WebMvcConfigurer webMvcConfigurer;

    @Autowired
    public void init(List<HandlerMethodArgumentResolver> resolvers){
        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
        //添加消息转换器
        converters.add(new MappingJackson2HttpMessageConverter());
        //消息转换器与Resolver绑定
        resolvers.add(new MyBodyResolver(converters));
    }
}
