package com.base.util.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.base.util.mybatisplus.annotation.QueryWrapperField;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @ClassName: WrapperTool
 * @Description:
 * @author: dh
 * @date: 2020/12/1
 * @version: v1
 */
public class WrapperTool {


    /**
     * 获取mybatisplus查询对象
     * @param object    封装查询条件的实体
     * @return  QueryWrapper
     */
    public static  QueryWrapper getQueryWrapper(Object object){
        return getQueryWrapper(object,Object.class);
    }

    /**
     * 获取类的所有字段
     * @param clazz 类
     * @return  Field []字段数组
     */
    public static Field [] findALlDeclaredFields(Class clazz){
        List<Field> fields = new ArrayList<>();
        while(clazz!=null){
            Field[] declaredFields = clazz.getDeclaredFields();
            if(declaredFields!=null && declaredFields.length>0){
                fields.addAll(Arrays.asList(declaredFields))  ;
            }
            clazz=clazz.getSuperclass();
        }
        return fields.toArray(new Field[]{});
    }

    /**
     * 获取mybatisplus的查询对象QueryWrapper
     * @param object   参数封装对象
     * @param tClass    泛型类型
     * @param <T>       泛型类型
     * @return  QueryWrapper
     */
    public static <T> QueryWrapper<T> getQueryWrapper(Object object,Class<T> tClass){
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        Field [] fields = findALlDeclaredFields(object.getClass());
        List<Field> fieldList = new ArrayList<>();
        for(Field field : fields){
            field.setAccessible(true);
            QueryWrapperField queryWrapperField=null;
            if((queryWrapperField=field.getAnnotation(QueryWrapperField.class))!=null){
                String condition = queryWrapperField.condition().trim();
                String dataBaseColumn = queryWrapperField.dataBaseColumn();
                QueryType queryType = queryWrapperField.queryType();
                if(StringUtils.isEmpty(dataBaseColumn)){
                    dataBaseColumn = field.getName();
                }
                addQueryField(queryType,queryWrapper,dataBaseColumn,condition,ReflectionUtils.getField(field,object));
                if(queryWrapperField.queryType()==QueryType.BETWEEN){
                    fieldList.add(field);
                }
            }
        }
        handlerBetween(fieldList,queryWrapper,object);
        return queryWrapper;
    }

    /**
     * 处理between and的情况
     * @param fieldList 字段列表
     * @param queryWrapper  mybatisplus查询对象
     * @param object    值
     */
    private static void handlerBetween(List<Field> fieldList,QueryWrapper queryWrapper,Object object){
        if(fieldList.size()==2){
            String columnName =fieldList.get(0).getAnnotation(QueryWrapperField.class).dataBaseColumn();
            if(StringUtils.isEmpty(columnName)){
                fieldList.get(1).getAnnotation(QueryWrapperField.class).dataBaseColumn();
            }
            if(!StringUtils.isEmpty(columnName)){
                Field field1 = fieldList.get(0);
                Object value1 = ReflectionUtils.getField(field1,object);
                QueryWrapperField queryWrapperField1 = field1.getAnnotation(QueryWrapperField.class);
                int index1 = queryWrapperField1.paramIndex();
                String condition1 = queryWrapperField1.condition();
                if(!paramValidate(condition1,value1)){
                    return;
                }
                Field field2 = fieldList.get(1);
                Object value2 = ReflectionUtils.getField(field2,object);
                QueryWrapperField queryWrapperField2 = field1.getAnnotation(QueryWrapperField.class);
                String condition2 = queryWrapperField2.condition();
                int index2 = queryWrapperField1.paramIndex();
                if(!paramValidate(condition2,value2)){
                    return;
                }
                if (index1 == index2) {
                    queryWrapper.between(columnName, value1, value2);
                } else {
                    if (index1 > index2) {
                        queryWrapper.between(columnName, value2, value1);
                    } else {
                        queryWrapper.between(columnName, value1, value2);
                    }
                }
            }
        }
    }

    /**
     * 向queryWrapper里添加相应的参数
     * @param queryType 插叙类型
     * @param queryWrapper  mybatisplus查询对象
     * @param dataBaseColumn    查询字段名称
     * @param condition 条件
     * @param value 值
     * @return  mybatisplus查询对象
     */
    private static QueryWrapper addQueryField(QueryType queryType,QueryWrapper queryWrapper,String dataBaseColumn,String condition,Object value){
        boolean flag = paramValidate(condition,value);
        if(flag){
            initQueryWrapper(queryType,queryWrapper,dataBaseColumn,value);
        }
        return queryWrapper;
    }

    /**
     * 验证参数的合法性
     * @param condition 条件
     * @param value     值
     * @return  是否合法
     */
    private  static boolean paramValidate(String condition,Object value){
        boolean flag= false;
        if(condition.equals("0xffff") && !StringUtils.isEmpty(value)){
                flag=true;

        }else if(!condition.equals("0xffff") && !StringUtils.isEmpty(value)){
                String [] conditionArray = condition.split("&");
            if(!StringUtils.isEmpty(value) ){
                if(condition.startsWith("!=")){
                    condition = condition.replace("!=","");
                    if(!value.toString().equals(condition)){
                        flag=true;
                    }
                }else if(condition.startsWith("==")){
                    condition = condition.replace("==","");
                    if(value.toString().equals(condition)){
                        flag=true;
                    }
                }else if(condition.startsWith(">=")){
                    condition = condition.replace(">=","");
                    if(value.toString().equals(condition)){
                        flag=true;
                    }
                }else if(condition.startsWith("<=")){

                }else if(condition.startsWith(">")){

                }else if(condition.startsWith("<")){

                }else{
                    flag=true;
                }
            }
        }
        return flag;
    }

    /**
     * 给queryWrapper封装查询参数
     * @param queryType 查询类型
     * @param queryWrapper  mybatisplus的查询对象
     * @param dataBaseColumn    查询字段名称
     * @param value 值
     */
        private static void initQueryWrapper(QueryType queryType,QueryWrapper queryWrapper,String dataBaseColumn,Object value){
        switch (queryType){
            case IN:
                queryWrapper.in(dataBaseColumn,value);
                break;
            case EQUAL:
                queryWrapper.eq(dataBaseColumn,value);
                break;
            case LIKE:
                queryWrapper.like(dataBaseColumn,value);
                break;
            case LEFT_LIKE:
                queryWrapper.likeLeft(dataBaseColumn,value);
                break;
            case RIGHT_LIKE:
                queryWrapper.likeRight(dataBaseColumn,value);
                break;
            case NOTEQUAL:
                queryWrapper.ne(dataBaseColumn,value);
            default:
                break;
        }
        }


    /**
     * 批量新增
     * @param data       新增的数据
     * @param object     新增操作对象
     * @param methodName 新增操作对象方法
     * @param bathNum    每次批量数量
     * @param <T>        需要新增的数据的对象泛型
     */
        public static <T>   void  batchExecute(List<T> data,Object object,String methodName,int bathNum){
            if(object == null || !StringUtils.hasText(methodName))
                throw new IllegalArgumentException("执行对象和方法名称不能为空");
            Method method = ReflectionUtils.findMethod(object.getClass(), methodName, List.class);
            if(method == null)
                throw new IllegalArgumentException(" 方法 "+ methodName+" 未找到");
            if( bathNum == 0 )
                bathNum = 500;
            int pointsDataLimit = bathNum;
            int listSize = data.size();
            int maxSize = listSize - 1;
            List<T> newList = new ArrayList<>();
            for (int i = 0; i < listSize; i++) {
                newList.add(data.get(i));
                if (pointsDataLimit == newList.size() || i == maxSize) {
                    ReflectionUtils.invokeMethod(method,object,newList);
                    newList.clear();
                }
            }
        }
}
