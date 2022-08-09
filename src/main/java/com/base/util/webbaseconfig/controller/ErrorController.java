package com.base.util.webbaseconfig.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

import com.alibaba.fastjson.JSONObject;
import com.base.util.common.CommonUtil;
import com.base.util.webbaseconfig.exception.BusinessException;
import com.base.util.webbaseconfig.globalexception.GlobalExceptionConfiguration;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 *
 * <p>Title: ErrorController</p>
 * <p>Description:在Spring Boot中，Controller中抛出的异常默认交给了/error来处理，
 * 					应用程序可以将error映射到一个特定的Controller中处理来代替Spring Boot的默认实现，
 * 					应用可以继承AbstractErrorController来统一处理系统的各种异常 </p>
 * @author daihu
 * @date 2019年4月29日
 */
@Controller
public class ErrorController extends AbstractErrorController{


    private  static   Set<Class> exceptions = Collections.synchronizedSet(new HashSet<Class>());

    private  static  boolean  cascadeFindException=false;

    private  static  boolean  initCascadeFinish=false;
    private  static  boolean  initFinish=false;

    private  static String message;



    Log log = LogFactory.getLog(ErrorController.class);

    static {
        exceptions.add(BusinessException.class);
        exceptions.add(ValidationException.class);
    }



    @Autowired
    ObjectMapper mapper;
    public ErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @Override
    public String getErrorPath() {
        return null;
    }
    /**
     * 替代系统的处理方式
     * @param modelAndView
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/error")
    public ModelAndView getErrorPath(ModelAndView modelAndView,HttpServletRequest request,HttpServletResponse response) {

        //获取错误信息{timestamp=Sun May 05 20:15:58 CST 2019, status=500, error=Internal Server Error, message=11, path=/testError.json}
        Map<String, Object> errorAttributes=getErrorAttributes(request,false);
        //获取异常
        Throwable error = getCause(request);
        //获取status
        int status = (int)errorAttributes.get("status");
        //获取错误信息
        String message = (String)errorAttributes.get("message");
        //友好提示
        String errorMessage = getErrorMessage(error);

        //后台打印日志信息方便查错
        log.info(status+","+message,error);
        response.setStatus(status);
        JSONObject json = new JSONObject();
        json.put("code",status);
        json.put("message",errorMessage);
        json.put("timestamp", CommonUtil.getCurrentTime(null));
        writeJson(response,json);

        return modelAndView;
    }

    /**
     * 用于获取应用系统的异常，定义如下：
     * @param request
     * @return
     */
    protected Throwable getCause(HttpServletRequest request) {
        //从request里面拿到异常对象
        Throwable error = (Throwable)request.getAttribute("javax.servlet.error.exception");
        if(error !=null) {
            //MVC有可能会封装异常为ServletException，需要调用getCause获取真正的异常
            while(error instanceof ServletException && error.getCause()!=null) {
                error=((ServletException)error).getCause();
            }
        }
        return error;
    }

    /**
     * 输出报错信息
     * @param ex
     * @return
     */
    protected String getErrorMessage(Throwable ex) {
        if(ex==null){
            return message;
        }
        Iterator<Class> iterator = exceptions.iterator();
        while(iterator.hasNext()){
            Class c = iterator.next();
                if((c.isAssignableFrom(ex.getClass()) && cascadeFindException) || (!cascadeFindException && c==ex.getClass())){
                    return ex.getMessage();
                }
        }
        return String.format("%s : %s",message,ex.getMessage());
    }

    /**
     * 区分客户端发起的是页面渲染请求还是JSON请求
     * @param request
     * @return
     */
    protected boolean isJsonRequest(HttpServletRequest request) {
        String requestUri =(String)request.getAttribute("javax.servlet.error.request_uri");
        if(requestUri!=null && requestUri.endsWith(".json")) {
            return true;
        }else if(request.getHeader("Accept").contains("application/json")) {
            return true;
        }else {
            return false;
        }
    }
    /**
     * 输出json
     * @param response
     * @param object
     */
    protected void writeJson(HttpServletResponse response,Object object) {
        try {
            response.setHeader("CharSet", "UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/x-json;charset=UTF-8");
            response.getWriter().print(object);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    public static void setGlobalExceptionType(boolean cascadeFindException){
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for(StackTraceElement stackTraceElement :stackTrace ){
            if(stackTraceElement.getClassName().equals(GlobalExceptionConfiguration.class.getName()) && stackTraceElement.getMethodName().equals("registerBeanDefinitions")){
                ErrorController.cascadeFindException =cascadeFindException;
                return;
            }
        }
    }

    public static void setMessage(String message){
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for(StackTraceElement stackTraceElement :stackTrace ){
            if(stackTraceElement.getClassName().equals(GlobalExceptionConfiguration.class.getName()) && stackTraceElement.getMethodName().equals("registerBeanDefinitions")){
                ErrorController.message=message;
                return;
            }
        }
    }


    public static void setGlobalException(Class[] globalException){
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for(StackTraceElement stackTraceElement :stackTrace ){
            if(stackTraceElement.getClassName().equals(GlobalExceptionConfiguration.class.getName()) && stackTraceElement.getMethodName().equals("registerBeanDefinitions")){
                for(Class clazz : globalException){
                    if(!Throwable.class.isAssignableFrom(clazz)){
                        throw new BusinessException(clazz+" is not isAssignableFrom "+Throwable.class);
                    }
                    if(exceptions.size()==0){
                        exceptions.add(clazz);
                        return;
                    }
                    if(exceptions.contains(clazz)){
                        return;
                    }else if(!cascadeFindException){
                        exceptions.add(clazz);
                    }else{
                        //包含1、如果这个class是某一个已存在的父类那么
                        Iterator<Class> iterator = exceptions.iterator();
                        Set<Class> newException = new HashSet<>();
                        while(iterator.hasNext()){
                            Class cla = iterator.next();
                            if(clazz.isAssignableFrom(cla)){
                                iterator.remove();
                                newException.add(clazz);
                            }
                            }
                        exceptions.addAll(newException);
                        }
                    }
            }
        }
    }


}
