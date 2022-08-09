package com.base.util.adapter;

import com.base.util.adapter.annotation.PoJoName;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * @ClassName: AdapterTool
 * @Description:
 * @author: dh
 * @date: 2020/10/12
 * @version: v1
 */
public class AdapterTool {



    public static <T>  List<T> adapter(List<?> source , Class<T> target){
        List<T> results = new ArrayList<>(12);
        source.forEach(c->{
           T t =adapter(c,target);
            results.add(t);
        });
        return results;
    }

    public static <T>  T adapter(Object source, Class<T> target){
        T  result = null;
        try {
            Constructor constructor =  target.getConstructor();
             result= (T) constructor.newInstance();
            Class cl =source.getClass();
            List<Field> fieldList = new ArrayList<>();
            while(cl!=null){
                fieldList.addAll(Arrays.asList(cl.getDeclaredFields()));
                cl = cl.getSuperclass();
            }
            Field[] fields  = fieldList.toArray(new Field[0]);
            for(Field field : fields){
                PoJoName poJoName = field.getAnnotation(PoJoName.class);
                if(poJoName!=null){
                    String fieldName = field.getName();
                    String targetName;
                    String value = poJoName.value();
                    FieldType type = poJoName.type();
                    if(value.equals(""))
                        targetName=fieldName;
                    else
                        targetName=value;
                    String targetSetName="set"+upperCase(targetName);
                    String sourceGetName="get"+upperCase(fieldName);
                    Method sourceMethod = ReflectionUtils.findMethod(source.getClass(),sourceGetName);
                    Object sourceValue = sourceMethod.invoke(source);
                    try{
                        Class targetType = getType(type,field.getType());
                        Method targetMethod = ReflectionUtils.findMethod(result.getClass(),targetSetName,targetType);
                        Object targetValue = changeToFieldType(type,sourceValue);
                        if(targetValue!=null){
                            targetMethod.invoke(result,targetValue);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }


    private static String upperCase(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }
    private static Class getType(FieldType fieldType,Class type){
        Class result=null;
        try{
            switch (fieldType){
                case INTEGER:
                    result = Integer.class;
                    break;
                case DOUBLE:
                    result = Double.class;
                    break;
                case FLOAT:
                    result = Float.class;
                    break;
                case STRING:
                    result=String.class;
                    break;
                case CHAR:
                    result = char.class;
                    break;
                case Character:
                    break;
                case SHORT:
                    result = Short.class;
                    break;
                case LONG:
                    result = Long.class;
                    break;
                case BOOLEAN:
                    result = Boolean.class;
                    break;
                case baseInt:
                    result=int.class;
                    break;
                case BaseDouble:
                    result =double.class;
                    break;
                case BaseFloat:
                    result =float.class;
                    break;
                case BaseShort:
                    result =short.class;
                    break;
                case BaseLong:
                    result =long.class;
                    break;
                case BaseBoolean:
                    result =boolean.class;
                    break;
                case NONE:
                    result =type;
                    break;
            }
        }catch (Exception e){
            throw new RuntimeException("类型转换失败",e);
        }
        return result;
    }
    private static Object changeToFieldType(FieldType fieldType,Object value){
        Object result;
        if(value==null){
            return null;
        }
        try{
        switch (fieldType){
            case INTEGER:
            case baseInt:
                result = Integer.valueOf(value+"");
                break;
            case DOUBLE:
            case BaseDouble:
                result = Double.valueOf(value+"");
                break;
            case FLOAT:
            case BaseFloat:
                result = Float.valueOf(value+"");
                break;
            case STRING:
                result=value+"";
                break;
            case CHAR:
                result = (value+"").charAt(0);
                break;
            case SHORT:
            case BaseShort:
                result = Short.valueOf(value+"");
                break;
            case LONG:
            case BaseLong:
                result = Long.valueOf(value+"");
                break;
            case BOOLEAN:
            case BaseBoolean:
                result = Boolean.valueOf(value+"");
                break;
            default:
                result = value;
        }
        }catch (Exception e){
            throw new RuntimeException("类型转换失败",e);
        }
        return result;
    }

    private static String lowerCase(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'A' && ch[0] <= 'Z') {
            ch[0] = (char) (ch[0] + 32);
        }
        return new String(ch);
    }

    public  static  <T> T  camelCase(Object source,Class<T> target){
        T t = null;
        try {
        if(Map.class.isAssignableFrom(target)){
          t = (T) HashMap.class.newInstance();
        }else{
                t = target.newInstance();
        }
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        List<ObjectModel> models = new ArrayList<>();
        if(source instanceof Map){
            Map map = (Map)source;
            map.forEach( (key,value) ->{
                ObjectModel objectModel = new ObjectModel();
                objectModel.fieldName = (String) key;
                objectModel.camelName = stringCaselCase(objectModel.fieldName);
                objectModel.value=value;
                models.add(objectModel);
            });
        }else{
            Method[] allDeclaredMethods = ReflectionUtils.getAllDeclaredMethods(source.getClass());
            for(Method method : allDeclaredMethods){
                String methodName = method.getName();
                if(methodName.startsWith("get") && methodName.length()>3){
                    String fieldName = methodName.substring(3);
                    fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                    Object value = ReflectionUtils.invokeMethod(method, source);
                    Field field = ReflectionUtils.findField(source.getClass(),fieldName);
                    if(value!=null && field!=null){
                        ObjectModel objectModel = new ObjectModel();
                        objectModel.fieldName = fieldName;
                        objectModel.camelName = stringCaselCase(objectModel.fieldName);
                        objectModel.value=value;
                        objectModel.field=field;
                        models.add(objectModel);
                    }

                }
            }
        }

        T finalT = t;
        models.forEach(c->{
            if(Map.class.isAssignableFrom(target)){
                Method method = ReflectionUtils.findMethod(target, "put");
                if(method!=null){
                    ReflectionUtils.invokeMethod(method, finalT,c.camelName,c.value);
                }
            }else{
                String fieldName = c.fieldName;
                Field field =ReflectionUtils.findField(target,c.fieldName);
                if(field==null){
                    field =ReflectionUtils.findField(target,c.camelName);
                    if(field!=null){
                        field.setAccessible(true);
                        ReflectionUtils.setField(field,finalT,caseObject(c.value,field.getType()));
                    }
                }else{
                    field.setAccessible(true);
                    ReflectionUtils.setField(field,finalT,caseObject(c.value,field.getType()));
                }
            }
        });
        return finalT;
    }


    public static Object caseObject(Object value, Class type) {
        Object result = null;
        if (value == null )
            return type.cast(null);
        if (type != null) {
            String className = type.getCanonicalName();
            switch (className) {
                case "java.lang.String":
                    result = value + "";
                    break;
                case "java.lang.Integer":
                case "int":
                    if ((value + "").indexOf(".") != -1) {
                        value = (value + "").subSequence(0, (value + "").indexOf("."));
                    }
                    result = Integer.valueOf(value.toString());
                    break;
                case "java.lang.Double":
                case "double":
                    result = Double.valueOf(value.toString());
                    break;
                case "java.lang.Long":
                case "long":
                    result = Long.valueOf(value.toString());
                    break;
                case "java.lang.Boolean":
                case "boolean":
                    result = Boolean.valueOf(value.toString());
                    break;
                case "java.lang.Float":
                case "float":
                    result = Float.valueOf(value.toString());
                    break;
                case "java.lang.Short":
                case "short":
                    result = Short.valueOf(value.toString());
                    break;
                case "java.util.Date":
                    DateFormat format = DateFormat.getDateInstance();
                    try {
                        result = format.parse(value.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case "java.sql.Timestamp":
                    format = DateFormat.getDateInstance();
                    try {
                        Date temp = format.parse(value.toString());
                        result = new Timestamp(temp.getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    break;
                case "java.sql.Date":
                    format = DateFormat.getDateInstance();
                    try {
                        Date temp = format.parse(value.toString());
                        result = new java.sql.Date(temp.getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        return result;
    }
    public static String stringCaselCase(String str){
        if(str==null || str.trim().length()==0){
            return str;
        }
        str = str.trim();
        while(str.startsWith("_") || str.endsWith("_")){
            if(str.startsWith("_")){
                str = str.substring(1);
            }
            if(str.endsWith("_")){
                str = str.substring(0,str.length()-1);
            }
        }

        while (str.contains("_")){
            int _index = str.indexOf("_");
            String first = str.substring(0,_index);
            String last = str.substring(_index+1);
            char c = last.charAt(0);
            c = Character.toUpperCase(c);
            last = c+last.substring(1);
            str = first+last;
        }
        if(str.length()>0){
            char c = str.charAt(0);
            c = Character.toLowerCase(c);
            str = c+str.substring(1);
        }

        return str;
    }


    public  static  <T> List<T>  camelCase(List source,Class<T> target){
        List<T> result = new ArrayList<>();
        if(source==null || source.size()==0){
            return result;
        }
        source.forEach( c -> result.add(camelCase(c,target)));
        return result;
    }

    private static class ObjectModel{
        public String fieldName ;

        public String camelName ;

        public  Field field;

        public Object value;

    }

    public static void main(String[] args) {

    }
}
