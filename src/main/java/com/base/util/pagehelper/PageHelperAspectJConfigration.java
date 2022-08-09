package com.base.util.pagehelper;

import com.base.util.pagehelper.annotation.*;
import com.github.pagehelper.PageHelper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: PageHelperConfigration
 * @Description:
 * @author: dh
 * @date: 2020/11/20
 * @version: v1
 */
@Configuration
@Aspect
public class PageHelperAspectJConfigration {



    @Before("@annotation(page)")
    public void  pageAspectBefore(final JoinPoint joinPoint, Page page) throws IllegalAccessException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        Parameter [] parameters = targetMethod.getParameters();
        Object [] args = joinPoint.getArgs();
        int pageNum=-1;
        int pageSize=-1;
        String order="";
        int sort =-1;
        PageModel pageModel;
        PageNum pageNum1;
        PageSize pageSize1;
        OrderField orderField;
        Sort sort1;
        if(parameters!=null){
            for(int i=0;i<parameters.length;i++) {
                Parameter parameter = parameters[i];

                Object currentObject = null;
                currentObject=args[i];
                Object value=null;
                if ((pageModel = parameter.getAnnotation(PageModel.class)) != null) {
                    Class c = currentObject.getClass();
                    List<Field> fields = new ArrayList<>();
                    while(c!=null){
                        Field[] declaredFields = c.getDeclaredFields();
                        if(declaredFields!=null){
                            for(Field field : declaredFields){
                                fields.add(field);
                            }
                        }
                        c = c.getSuperclass();
                    }
                    for (Field field : fields) {
                        field.setAccessible(true);

                        if ((pageNum1 = field.getAnnotation(PageNum.class)) != null) {
                            pageNum = (value = field.get(currentObject))==null ? pageNum1.defaultValue() : Integer.parseInt(value + "");
                        } else if ((pageSize1 = field.getAnnotation(PageSize.class)) != null) {
                            pageSize = (value = field.get(currentObject)) == null ? pageSize1.defaultValue() : Integer.parseInt(value + "");
                        } else if ((orderField = field.getAnnotation(OrderField.class)) != null) {
                            order = (value = field.get(currentObject)) == null ? orderField.defaultValue() : value + "";
                            if(orderField.needCame()){
                                order = getCame(order);
                            }
                        } else if ((sort1 = field.getAnnotation(Sort.class)) != null) {
                            sort = (value = field.get(currentObject)) == null ? sort1.defaultValue() : Integer.parseInt(value + "");
                        }else if((pageModel = field.getAnnotation(PageModel.class)) != null){
                            Object    chilObject = field.get(currentObject);
                            if(chilObject==null){
                                continue;
                            }
                            for(Field cfield :chilObject.getClass().getDeclaredFields()){
                                cfield.setAccessible(true);

                                if ((pageNum1 = cfield.getAnnotation(PageNum.class)) != null) {
                                    pageNum = (value = cfield.get(chilObject))==null ? pageNum1.defaultValue() : Integer.parseInt(value + "");
                                } else if ((pageSize1 = cfield.getAnnotation(PageSize.class)) != null) {
                                    pageSize = (value = cfield.get(chilObject)) == null ? pageSize1.defaultValue() : Integer.parseInt(value + "");
                                } else if ((orderField = cfield.getAnnotation(OrderField.class)) != null) {
                                    order = (value = cfield.get(chilObject)) == null ? orderField.defaultValue() : value + "";
                                    if(orderField.needCame()){
                                        order = getCame(order);
                                    }
                                } else if ((sort1 = cfield.getAnnotation(Sort.class)) != null) {
                                    sort = (value = cfield.get(chilObject)) == null ? sort1.defaultValue() : Integer.parseInt(value + "");
                                }
                            }
                        }
                    }
                }else if((pageNum1 = parameter.getAnnotation(PageNum.class)) != null){
                    pageNum = (value=currentObject) == null ? pageNum1.defaultValue() : Integer.parseInt(value + "");
                }else if((pageSize1 = parameter.getAnnotation(PageSize.class)) != null){
                    pageSize = (value=currentObject) == null ? pageSize1.defaultValue() : Integer.parseInt(value + "");
                }else if((orderField = parameter.getAnnotation(OrderField.class)) != null){
                    order = (value=currentObject) == null ? orderField.defaultValue() : value + "";
                    if(orderField.needCame()){
                        order = getCame(order);
                    }
                }else if ((sort1 = parameter.getAnnotation(Sort.class)) != null) {
                    sort = (value=currentObject) == null ? sort1.defaultValue() : Integer.parseInt(value + "");
                }
            }
            if(pageNum!=-1 && pageSize!=-1 && pageSize!=0 && pageNum!=0){
                PageHelper.startPage(pageNum,pageSize);
            }
            sort = sort<0 ? 0: sort>1 ? 1 : sort;
            if(order!=null && order.trim().length()>0){
                PageHelper.orderBy( order + " "+ (sort==1 ? "desc":"asc"));
            }
        }
    }

    @After("@annotation(page)")
    public void  pageAspectAfter(final JoinPoint joinPoint, Page page) throws IllegalAccessException {

    }


    public static String getCame(String source){
        String newSource="";
        char[] chars = source.toCharArray();
        for (char c : chars){
            if(c >= 65 && c<=90){
                newSource+= "_"+(char)(c+32);
            }else{
                newSource+=c+"";
            }
        }
        return newSource;
    }


}
