package com.base.util.feign;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;

/**
 * @ClassName: FeignClientProxy
 * @Description:
 * @author: dh
 * @date: 2020/11/23
 * @version: v1
 */
@Slf4j
public class FeignClientProxy {


    private RestTemplate restTemplate;


    private Environment environment;

    private ApplicationContext applicationContext;

    private String httpUrl;

    private String className;

    public FeignClientProxy(ApplicationContext applicationContext, Environment environment,String className){
        this.applicationContext = applicationContext;
        this.environment=environment;
        restTemplate = applicationContext.getBean(RestTemplate.class)==null ? applicationContext.getBean(RestTemplateBuilder.class).build() : applicationContext.getBean(RestTemplate.class);
    }


    public String getAnalysisAddr(Class clientClass){
        FeignClient feignClient = (FeignClient) clientClass.getAnnotation(FeignClient.class);
        String value = feignClient.value();
        if(!value.equals("")){
            return environment.getProperty(value);
        }
        throw new IllegalArgumentException(clientClass+" 无法找到请求地址 ");
    }

    class Invocation implements InvocationHandler{
        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            String url = getUrl(method);
            return sendHttpRequest(method,url,objects);
        }
    }
    
    private String getUrl(Method method){
        String url=null;
        String methodType;
        RequestMapping requestMapping;
        PostMapping postMapping;
        GetMapping getMapping;
        DeleteMapping deleteMapping;
        PutMapping putMapping;
        
        if((requestMapping=method.getAnnotation(RequestMapping.class))!=null){
            if(requestMapping.value().length==0){
                throw new IllegalArgumentException("url 不能为空");
            }

            url = "requestMapping "+requestMapping.value()[0];
        }else if((postMapping=method.getAnnotation(PostMapping.class))!=null){
            if(postMapping.value().length==0){
                throw new IllegalArgumentException("url 不能为空");
            }
            url =  "postMapping "+postMapping.value()[0];
        }else if((getMapping=method.getAnnotation(GetMapping.class))!=null){
            if(getMapping.value().length==0){
                throw new IllegalArgumentException("url 不能为空");
            }
            url ="getMapping "+ getMapping.value()[0];
        }else if((deleteMapping=method.getAnnotation(DeleteMapping.class))!=null){
            if(deleteMapping.value().length==0){
                throw new IllegalArgumentException("url 不能为空");
            }
            url ="deleteMapping "+ deleteMapping.value()[0];
        }else if((putMapping=method.getAnnotation(PutMapping.class))!=null){
            if(putMapping.value().length==0){
                throw new IllegalArgumentException("url 不能为空");
            }
            url ="putMapping "+ putMapping.value()[0];
        }
        return url;
    }

    public Object sendHttpRequest(Method method,String url,Object[] objects){
         Class returenType = null;
         if(method.getReturnType().getName().equals("void")){
             returenType = String.class;
         }else{
             returenType = method.getReturnType();
        }
         String methodType = url.split(" ")[0];
         url = url.split(" ")[1];
        Class returnType = method.getReturnType();
        log.info(method.getName()+"--send http request url:{},param:{}",httpUrl+url,objects[0]);
        switch (methodType){
            case "requestMapping":
//                restTemplate.postForObject(analysisAddr+url,,);
                break;
            case "postMapping":
                restTemplate.postForObject(httpUrl+url,objects[0],returenType);
                break;
            case "getMapping":
                break;
            case "deleteMapping":

                break;
            case "putMapping":
                break;
        }
         return null;
    }

    private String getRequestMapping(RequestMapping requestMapping){
        if(requestMapping.method().length==0){
            return "requestMapping ";
        }else {
            RequestMethod requestMethod = requestMapping.method()[0];
            switch (requestMethod.name()){
                case "GET":
                    return "getMapping ";
                case "POST":
                    return "postMapping ";
                case "DELETE":
                    return "deleteMapping ";
                case "PUT":
                    return "putMapping ";
                default:
                    return "";
            }
        }
    }

    public Object getBean(){
        try {
            Class clazz = Class.forName(className);
            httpUrl =  getAnalysisAddr(clazz);
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(clazz);
            enhancer.setCallback(new Invocation());
            return enhancer.create();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    return null;
    }
}
