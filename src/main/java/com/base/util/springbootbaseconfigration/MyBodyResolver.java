package com.base.util.springbootbaseconfigration;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.base.util.common.CommonUtil;
import com.base.util.webbaseconfig.annotation.BusinessRequestBody;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author dhu
 * @date 2020-12-02 21:35
 * @version 1.0
 */

@SuppressWarnings({"rawtypes","unchecked"})
public class MyBodyResolver extends AbstractMessageConverterMethodArgumentResolver {




    public MyBodyResolver(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //绑定注解
        return parameter.hasParameterAnnotation(BusinessRequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Object object = convertToBean(parameter,webRequest);
        validation(object,parameter);
        return object;
    }


    /**
     * 参数校验
     * @param bean    java对象
     * @param parameter controller参数列表
     */
    private void validation(Object bean,MethodParameter parameter){
        Valid valid = getAnnotation(parameter);
        if(valid == null)
            return;
    }

    /**
     * 参数封装
     * @param parameter     方法参数对象
     * @param webRequest    原生Request对象
     * @return              返回java对象
     */
    private Object convertToBean(MethodParameter parameter, NativeWebRequest webRequest){
        BusinessRequestBody businessRequestBody = getAnnotaion(parameter);
        String json = getJsonStr(webRequest,businessRequestBody);
        return parseJsonToBean(json,businessRequestBody,parameter);
    }

    /**
     * 获取参数注解
     * @param parameter 参数
     * @return 返回自定义注解
     */
    private BusinessRequestBody getAnnotaion(MethodParameter parameter){
        return parameter.getParameterAnnotation(BusinessRequestBody.class);
    }

    /**
     * 获取参数校验注解
     * @param parameter
     * @return
     */
    private Valid getAnnotation(MethodParameter parameter){
        return parameter.getParameterAnnotation(Valid.class);
    }



    private ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024*1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取前端传入的json字符串
     * @param webRequest            原生request对象
     * @param businessRequestBody   自定义注解
     * @return                      json字符串
     */
    private String getJsonStr( NativeWebRequest webRequest,BusinessRequestBody businessRequestBody){
       boolean required =  businessRequestBody.required();
       HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        String str =null;
        try {
            InputStream inputStream = request.getInputStream();
            if(required && (inputStream==null || StringUtils.isEmpty(str =getStrFromInputStream(inputStream)))){
                throw new RuntimeException("流获取失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(required){
                throw new RuntimeException("流获取失败");
            }
        }
        return str;
    }

    /**
     * 通过输入流获取字符串
     * @param inputStream   输入流
     * @return              字符串
     * @throws IOException  获取流失败抛出异常
     */
    private String getStrFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder json = new StringBuilder();
        String s;
        while((s=bufferedReader.readLine())!=null){
            json.append(s);
        }
        return json.toString().trim();
    }





    /**
     * 将json数据转换为java bean
     * @param str                   json字符串
     * @param businessRequestBody   自定义注解
     * @param parameter             方法参数
     * @return                      java bean对象
     */
    private Object parseJsonToBean(String str,BusinessRequestBody businessRequestBody,MethodParameter parameter){

        Class beanClass;
        Object object=null;
        Class genericType;
        try {
            beanClass = parameter.getParameterType();
            genericType = beanClass.getComponentType();
            if(genericType==null){
                genericType = getGenericType(parameter);
            }
            if(String.class.isAssignableFrom(beanClass)){
                object = str;
                return object;
            }
            if(!StringUtils.isEmpty(str)){
                if(str.startsWith("{")){
                    object = jsonObjectHandler(str,beanClass,parameter,businessRequestBody,genericType);
                }else{
                    object = jsonArrayHandler(str,businessRequestBody,beanClass,genericType);
                }
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
           throw new IllegalArgumentException(e.getMessage());
        }
        return object;
    }


    /**
     * json数组对象处理
     * @param str
     * @param businessRequestBody
     * @param beanClass
     * @param genericType
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private Object jsonArrayHandler(String str,BusinessRequestBody businessRequestBody,Class beanClass,Class genericType) throws IllegalAccessException, InstantiationException {
        boolean needCamel = businessRequestBody.needCamel();
        boolean needOneParameterToList = businessRequestBody.needOneParameterToList();
        String fetchField = businessRequestBody.fetchField();
        Object object = null;
        JSONArray array = JSONObject.parseArray(str);
        boolean flag=isSingleJsonParam(array);
        Field field;
        if(flag && needOneParameterToList ){
            if(!StringUtils.isEmpty(fetchField) && (field = ReflectionUtils.findField(beanClass,fetchField))!=null){
                object = beanClass.newInstance();
                Type type;
                ParameterizedType listGenericType = (ParameterizedType) field.getGenericType();
                Type[] listActualTypeArguments = listGenericType.getActualTypeArguments();
                type = listActualTypeArguments[0];
                Class cType = (Class) type;
                if(List.class.isAssignableFrom(field.getType())){
                    addList(object, array, field, cType);
                }else if(isArray(field.getType())){
                    cType = getArrayGenericType(field.getType());
                    Object child = new ArrayList<>();
                    addList(child, array, field, cType);
                    object =((List) child).toArray();
                }
            }else if(List.class.isAssignableFrom(beanClass)  || isArray(beanClass)){
                if(isArray(beanClass) || beanClass.isInterface() ){
                    object = new ArrayList<>();
                }else{
                    object = beanClass.newInstance();
                }
                if(isBaseType(genericType)){
                    for(int i=0;i< array.size();i++){
                        JSONObject jsonObject = array.getJSONObject(i);
                        Class finalGenericType = genericType;
                        Object finalObject1 = object;
                        jsonObject.forEach( (key , value) ->{
                            ((List) finalObject1).add(getValue(finalGenericType,jsonObject,key));
                        });
                    }
                }
                return isArray(beanClass) ? ((List)object).toArray((Object[]) Array.newInstance(genericType,0)) : object;
            }


        }else{
            object = getObject(beanClass,needCamel, needOneParameterToList, array,genericType);
        }
        return  object;
    }
    /**
     * 单个json对象处理
     * @param str
     * @param beanClass
     * @param parameter
     * @param businessRequestBody
     * @param genericType
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    private Object jsonObjectHandler(String str,Class beanClass,MethodParameter parameter,BusinessRequestBody businessRequestBody,Class genericType) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        boolean needCamel = businessRequestBody.needCamel();
        boolean needOneParameterToList = businessRequestBody.needOneParameterToList();
        String fetchField = businessRequestBody.fetchField();
        Object object = null;
        JSONObject jsonObject = JSONObject.parseObject(str);
        if(Map.class==beanClass || (Map.class.isAssignableFrom(beanClass) && beanClass.isInterface())){
            object = jsonObject.toJavaObject(Map.class);
            return object;
        }else if(Map.class.isAssignableFrom(beanClass)){
            object = beanClass.newInstance();
            Class[]  mapType = getMapGenericType(parameter);
            Method method = ReflectionUtils.findMethod(beanClass,"put",mapType);
            Object finalObject = object;
            jsonObject.forEach((key, value) -> ReflectionUtils.invokeMethod(method, finalObject, key, value));
            return finalObject;
        }
        if(fetchField.trim().length()>0){
            if(jsonObject.containsKey(fetchField) && jsonObject.get(fetchField) instanceof JSONObject){
                object = newInstance(beanClass);

                object = parseJsonToObject(jsonObject.getJSONObject(fetchField),needCamel,needOneParameterToList,object);
            }else if(jsonObject.containsKey(fetchField) && jsonObject.get(fetchField) instanceof JSONArray){
                JSONArray array = jsonObject.getJSONArray(fetchField);
                boolean flag=isSingleJsonParam(array);
                if(flag && needOneParameterToList){
                    if(beanClass.isInterface()){
                        if(List.class.isAssignableFrom(beanClass)){
                            object = new ArrayList<>();
                            final Object [] values = new Object[array.size()];
                            for(int i=0;i<array.size();i++){
                                JSONObject jsonObject1 = array.getJSONObject(i);
                                int finalI = i;
                                jsonObject1.values().forEach(c -> values[finalI]=c);
                            }
                            ((List) object).addAll(Arrays.asList(values));
                        }else if(Map.class.isAssignableFrom(beanClass)){
                            object = new HashMap<>();
                        }else if(Set.class.isAssignableFrom(beanClass)){
                            object = new HashSet<>();
                            final Object [] values = new Object[array.size()];
                            for(int i=0;i<array.size();i++){
                                JSONObject jsonObject1 = array.getJSONObject(i);
                                int finalI = i;
                                jsonObject1.values().forEach(c -> values[finalI]=c);
                            }
                            ((Set) object).addAll(Arrays.asList(values));
                        }else{
                            throw new IllegalArgumentException("当前class无法创建实例："+beanClass);
                        }

                    }else{
                        if(List.class.isAssignableFrom(beanClass)){
                            object = beanClass.newInstance();
                            final Object [] values = new Object[array.size()];
                            for(int i=0;i<array.size();i++){
                                JSONObject jsonObject1 = array.getJSONObject(i);
                                int finalI = i;
                                jsonObject1.values().forEach(c -> values[finalI]=c);
                            }
                            ((List) object).addAll(Arrays.asList(values));
                        }

                    }
                    if(needCamel){
                        fetchField = changeKeyToCamel(fetchField);
                    }
                    Field field = ReflectionUtils.findField(object.getClass(),fetchField);
                    if(field==null){
                        return  object;
                    }
                    field.setAccessible(true);
                    Class aClass;
                    if(isArray(field.getType())){
                        aClass = getArrayGenericType(field.getType());
                    }else{
                        ParameterizedType listGenericType = (ParameterizedType) field.getGenericType();
                        Type[] listActualTypeArguments = listGenericType.getActualTypeArguments();
                        Type type = listActualTypeArguments[0];
                        aClass = Class.forName(type.getTypeName());
                    }
                    addList(object, array, field, aClass);
                }else{
                    object = getObject(beanClass,needCamel, needOneParameterToList, array,genericType);
                }

            }
        }else{
            object = newInstance(beanClass);
            object = parseJsonToObject(jsonObject,needCamel,needOneParameterToList,object);
        }
        return object;
    }

    /**
     * 实例化对象
     * @param beanClass 需要实例化的类
     * @return  返回实例化的第一个对象
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Object newInstance(Class beanClass) throws InstantiationException, IllegalAccessException {
        Object object;
        if(beanClass.isInterface()){
            if(List.class.isAssignableFrom(beanClass)){
                object = new ArrayList<>();
            }else if(Map.class.isAssignableFrom(beanClass)){
                object = new HashMap<>();
            }else if(Set.class.isAssignableFrom(beanClass)){
                object = new HashSet<>();
            }else{
                throw new IllegalArgumentException("当前class无法创建实例："+beanClass);
            }

        }else{
            object = beanClass.newInstance();
        }
        return object;
    }

    /**
     * 往list里面添加参数
     * @param object    java对象
     * @param array     Json数组
     * @param field     类属性字段
     * @param cType     字段类型
     */
    private void addList(Object object, JSONArray array, Field field, Class cType) {
        List list = new ArrayList();
        field.setAccessible(true);
        for(int i=0;i<array.size();i++){
            JSONObject jsonObjects = array.getJSONObject(i);
            jsonObjects.forEach((key, value1) -> {
                Object value = getValue(cType, jsonObjects, key);
                list.add(value);
            });

        }
        if(isArray(field.getType())){
            ReflectionUtils.setField(field,object,list.toArray((Object[]) Array.newInstance(cType,0)));
        }else{
            ReflectionUtils.setField(field,object,list);
        }

    }

    /**
     * 是否满足只有单一对象
     * @param array     json数组
     * @return          java对象
     */
    private boolean isSingleJsonParam(JSONArray array){
    boolean flag=true;
    for(int i=0;i<array.size();i++){
        if(!(array.get(i) instanceof JSONObject) || array.getJSONObject(i).size()!=1){
            flag=false;
            break;
        }
    }
    return flag;
}

    /**
     * 获取java对象
     * @param needCamel                 是否需要以小驼峰命名的方式封装参数
     * @param needOneParameterToList    是否需要将json数组中所有只有一个键值对的json转换为java的list对象，比如[{"id":"aa"},{"id":"bb"}] => List<String> list = new ArrayList({"aa","bb"});
     * @param array                     参数的json数组
     * @return                          java对象
     */
    private Object getObject(Class sourceType,boolean needCamel, boolean needOneParameterToList, JSONArray array,Class clazz) throws IllegalAccessException, InstantiationException {
        System.out.println(sourceType.getComponentType());
        Object object;
        if((List.class.isAssignableFrom(sourceType) && sourceType.isInterface()) || sourceType.getComponentType()!=null){
            object = new ArrayList<>();
        }else{
            object = sourceType.newInstance();
        }

        for(int i=0;i<array.size();i++){
            if(array.get(i) instanceof JSONObject && clazz!=null){
                Object obj = clazz.newInstance();
                JSONObject jsonObject = array.getJSONObject(i);
                Object  childObject = parseJsonToObject(jsonObject,needCamel,needOneParameterToList,obj);
                ((List)object).add(childObject);
            }else if(array.get(i) instanceof JSONObject && clazz == null){
                JSONObject jsonObject = array.getJSONObject(i);
                ((List)object).add(jsonObject.toJavaObject(Map.class));
            }else if(!(array.get(i) instanceof JSONObject) && clazz != null){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("temp",array.get(i));
               Object value =  getValue(clazz,jsonObject,"temp");
                ((List)object).add(value);
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("temp",array.get(i));
                Object value =  getValue(String.class,jsonObject,"temp");
                ((List)object).add(value);
            }

        }
        return sourceType.getComponentType()!=null ? ((List)object).toArray((Object[]) Array.newInstance(clazz,0)) : object;
    }

    /**
     * 将json对象转换为 java bean
     * @param jsonObject                json数据串
     * @param needCamel                 是否需要小驼峰命名封装，比如 org_id 在封装的时候会将下划线去掉变成 orgId
     * @param needOneParameterToList    是否需要将json数组中所有只有一个键值对的json转换为java的list对象，比如[{"id":"aa"},{"id":"bb"}] => List<String> list = new ArrayList({"aa","bb"});
     * @param object                    java bean 对象
     * @return                          返回java对象
     */
    public Object parseJsonToObject(JSONObject jsonObject,boolean needCamel,boolean needOneParameterToList,Object object){
        jsonObject.forEach((key, value) -> {
            String oldKey = key;
            if (needCamel) {
                key = changeKeyToCamel(key);
            }
            sealAndPackage(jsonObject, oldKey, key, object, needCamel, needOneParameterToList);
        });
        return object;
    }

    /**
     * 将json的key转换为驼峰命名
     * @param key   原来的key
     * @return      转换之后的key
     */
    private String changeKeyToCamel(String key){
        while(key.contains("_") && (!key.startsWith("_") || !key.endsWith("_"))){
            String prev = key.substring(0,key.indexOf("_"));
            String sub = key.substring(key.indexOf("_")+1);
            String first = sub.substring(0,1).toUpperCase();
            key = prev+first+sub.substring(1);
        }
        return key;
    }

    /**
     * 对java bean 封装的核心逻辑
     * @param jsonObject                json串
     * @param oldKey                    原始的键key
     * @param key                       驼峰命名的键key
     * @param object                    java对象
     * @param needCamel                 是否需要驼峰命名
     * @param needOneParameterToList    是否需要将json数组中所有只有一个键值对的json转换为java的list对象，比如[{"id":"aa"},{"id":"bb"}] => List<String> list = new ArrayList({"aa","bb"});
     */
    private void sealAndPackage(JSONObject jsonObject, String oldKey, String key, Object object, boolean needCamel, boolean needOneParameterToList){
        Field field = ReflectionUtils.findField(object.getClass(),key);
        if(field==null){
            field = ReflectionUtils.findField(object.getClass(),oldKey);
        }
        if(field!=null){
            field.setAccessible(true);
           Class fieldType = field.getType();
           Object jsonValue =jsonObject.get(oldKey);
           if(jsonValue instanceof JSONArray && (List.class.isAssignableFrom(field.getType()) || isArray(field.getType()))){

               Class c;
               if(isArray(field.getType())){
                   c = getArrayGenericType(field.getType());
               }else{
                   ParameterizedType listGenericType = (ParameterizedType) field.getGenericType();
                   Type[] listActualTypeArguments = listGenericType.getActualTypeArguments();
                  c = (Class) listActualTypeArguments[0];
               }
               List list;
               Object child;
               try {
                   if(isArray(field.getType()) || field.getType().isInterface()){
                       list = new ArrayList();
                   }else {
                       list = (List) field.getType().newInstance();
                   }
               } catch (IllegalAccessException | InstantiationException e) {
                   throw new IllegalArgumentException(e.getMessage());
               }
               JSONArray array = jsonObject.getJSONArray(oldKey);
                for(int i=0;i< array.size();i++){
                   JSONObject json = array.getJSONObject(i);
                    child = createBeanAndSetValue(c,json,needCamel,needOneParameterToList);
                    if(child!=null){
                        list.add(child);
                    }
                }
                if(isArray(field.getType())){
                    ReflectionUtils.setField(field,object,list.toArray((Object[]) Array.newInstance(getArrayGenericType(field.getType()),0)));
                }else{
                    ReflectionUtils.setField(field,object,list);
                }

           }else if(jsonValue instanceof  JSONObject){
               Object child;
               JSONObject json = (JSONObject) jsonValue;
                 child = createBeanAndSetValue(field.getType(),json,needCamel,needOneParameterToList);
                 if(child!=null){
                     ReflectionUtils.setField(field,object,child);
                 }
           }else{
              Object value =  getValue(fieldType,jsonObject,oldKey);
               ReflectionUtils.setField(field,object,value);
           }
        }else{
            Class[] methodType;
            Method method;
            if(String.class.isAssignableFrom(object.getClass())){
                methodType = new Class[]{Object.class};
            }else{
                methodType = new Class[]{object.getClass()};
            }
            if(needOneParameterToList && jsonObject.size()==1){

                method = ReflectionUtils.findMethod(object.getClass(),"valueOf",methodType);
                if(method!=null){
                    jsonObject.forEach((key1,value) ->{
                        Object objValue = getValue(object.getClass(),jsonObject,key);
                        ReflectionUtils.invokeMethod(method,object,objValue);
                    });
                }
            }
        }
    }


    /**
     *  创建bean对象并且赋值
     * @param c         属性类型
     * @param json      json数据
     * @param needCamel 是否需要驼峰赋值
     * @param needOneParameterToList    是否需要将json数组中所有只有一个键值对的json转换为java的list对象，比如[{"id":"aa"},{"id":"bb"}] => List<String> list = new ArrayList({"aa","bb"});
     * @return      java对象
     */
    public Object createBeanAndSetValue(Class c, JSONObject json, boolean needCamel, boolean needOneParameterToList){
        Object child = baseTypeHandler(c,json,needOneParameterToList);
        if(!(child instanceof Boolean)){
            return child;
        }else{
            try {
                child = c.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalArgumentException( e.getMessage());
            }
            Object finalChild = child;
            json.forEach((key1, value) -> {
                String oldKey1 = key1;
                if (needCamel) {
                    key1 = changeKeyToCamel(key1);
                }
                sealAndPackage(json, oldKey1, key1, finalChild, needCamel, needOneParameterToList);
            });
        }
        return child;
    }


    private boolean isBaseType( Class c){
        boolean flag = false;
        if(String.class.isAssignableFrom(c)){
            flag = true;
        }else if(Integer.class.isAssignableFrom(c) || "int".equals(c.getName())){
            flag = true;
        }else if(Short.class.isAssignableFrom(c) || "short".equals(c.getName())){
            flag = true;
        }else if(Double.class.isAssignableFrom(c) || "double".equals(c.getName())){
            flag = true;
        }else if(Float.class.isAssignableFrom(c) || "float".equals(c.getName())){
            flag = true;
        }else if(Long.class.isAssignableFrom(c) || "long".equals(c.getName())){
            flag = true;
        }else if(Boolean.class.isAssignableFrom(c) || "boolean".equals(c.getName())){
            flag = true;
        }else if(Byte.class.isAssignableFrom(c) || "byte".equals(c.getName())){
            flag = true;
        }else if(Date.class.isAssignableFrom(c) ){
            flag = true;
        }
        return flag;
    }

    /**
     * 基本类型的处理
     * @param c 需要处理的类型
     * @param  json json数据
     * @param  needOneParameterToList 是否需要将一列转换成一个List对象
     * @return 返回处理之后的类型或者是boolean为false表示不是基本类型
     */
    private Object baseTypeHandler(Class c, JSONObject json, boolean needOneParameterToList){
       Object value=null;
       boolean flag = isBaseType(c);
        if(!flag){
            return false;
        }
        if(needOneParameterToList && json.size()==1){
            String key = (String) json.keySet().toArray()[0];
            value = getValue(c,json,key);
        }
        return value;
    }
    /**
     *      通过java bean的参数类型获取对应类型的值
     * @param fieldType     字段类型
     * @param jsonObject    json对象
     * @param oldKey        原始的键 key
     * @return              返回java对象
     */
    private Object getValue(Class fieldType,JSONObject jsonObject,String oldKey){
        Object object = null;
       switch (fieldType.getName()){
           case "java.lang.String":
               object = jsonObject.getString(oldKey);
               break;
           case "java.lang.Integer":
               object = jsonObject.getInteger(oldKey);
               break;
           case "int":
               object = jsonObject.getIntValue(oldKey);
               break;
           case "java.lang.Long":
               object = jsonObject.getLong(oldKey);
               break;
           case "long":
               object = jsonObject.getLongValue(oldKey);
               break;
           case "java.lang.Float":
               object = jsonObject.getFloat(oldKey);
               break;
           case "float":
               object = jsonObject.getFloatValue(oldKey);
               break;
           case "java.lang.Double":
               object = jsonObject.getDouble(oldKey);
               break;
           case "double":
               object = jsonObject.getDoubleValue(oldKey);
               break;
           case "java.lang.Short":
               object = jsonObject.getShort(oldKey);
               break;
           case "short":
               object = jsonObject.getShortValue(oldKey);
               break;
           case "java.lang.Boolean":
               object = jsonObject.getBoolean(oldKey);
               break;
           case "boolean":
               object = jsonObject.getBooleanValue(oldKey);
               break;
           case "java.util.Date":
               object = jsonObject.getDate(oldKey);
               break;
           case "java.sql.Date":
               object = jsonObject.getSqlDate(oldKey);
               break;
           case "java.sql.Timestamp":
               object = jsonObject.getTimestamp(oldKey);
               break;
           case "java.math.BigDecimal":
               object = jsonObject.getBigDecimal(oldKey);
               break;
           case "java.math.BigInteger":
               object = jsonObject.getBigInteger(oldKey);
               break;
           default:
               break;

       }
       return object;
    }


    /**
     * 获取参数的泛型类型
     * @param parameter 参数对象
     * @param index     泛型的下标
     * @return          泛型类型
     */
    private  Class  getParameterGenericType(Parameter parameter, int index){
        Type type = parameter.getParameterizedType();
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class) actualTypeArguments[index];
    }

    /**
     * 获取参数的泛型类型
     * @param parameter 参数对象
     * @return          泛型类型
     */
    private Class[]  getParameterGenericType(Parameter parameter){
        Type type = parameter.getParameterizedType();
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return (Class[]) actualTypeArguments;
    }


    /**
     * 获取属性的泛型类型
     * @param field 类属性
     * @return      泛型类型
     */
    private Class getListFieldGenericType(Field field){
        Type type=null;
        ParameterizedType listGenericType = (ParameterizedType) field.getGenericType();
        Type[] listActualTypeArguments = listGenericType.getActualTypeArguments();
        type = listActualTypeArguments[0];
        return (Class) type;
    }

    /**
     * 获取属性的泛型类型
     * @param field 类属性
     * @return      泛型类型
     */
    private Class[] getMapFieldGenericType(Field field){
        Type type=null;
        ParameterizedType listGenericType = (ParameterizedType) field.getGenericType();
        Type[] listActualTypeArguments = listGenericType.getActualTypeArguments();
        return (Class[]) listActualTypeArguments;
    }

    /**
     * 获取类的泛型
     * @param parameter MethodParameter对象
     * @return 类的泛型
     */
    private Class getGenericType(MethodParameter parameter){
        Type type =parameter.getGenericParameterType();
        if(type instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class) parameterizedType.getActualTypeArguments()[0];
        }
        return null;
    }

    /**
     * 获取Map的泛型类型
     * @param parameter  MethodParameter对象
     * @return map的key value的对象
     */
    private Class[] getMapGenericType(MethodParameter parameter){
        Type type =parameter.getGenericParameterType();
        if(type instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class[]) parameterizedType.getActualTypeArguments();
        }
        return null;
    }

    /**
     * 是否为数组类型
     * @param clazz 目标类
     * @return 是否为数组类型
     */
    private boolean isArray(Class clazz){
        return clazz.getComponentType()!=null;
    }

    /**
     * 获取数组类型
     * @param clazz 目标类
     * @return  数组的类型
     */
    private Class getArrayGenericType( Class clazz){
        if(isArray(clazz)){
            return clazz.getComponentType();
        }
        return null;
    }


}

