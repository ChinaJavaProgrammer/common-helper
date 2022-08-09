package com.base.util.springbootbaseconfigration;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;


/**
 * @author dhu
 * @date 2020-12-02 21:35
 * @version 1.0
 */
@ConditionalOnMissingBean(WebMvcConfigurer.class)
@Configuration
public class MyRequestBodyConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
        //添加消息转换器
        converters.add(new MappingJackson2HttpMessageConverter());
        //消息转换器与Resolver绑定
        resolvers.add(new MyBodyResolver(converters));
    }
}

