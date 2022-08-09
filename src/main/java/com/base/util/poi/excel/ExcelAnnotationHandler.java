package com.base.util.poi.excel;

import com.base.util.poi.excel.annotation.*;
import com.base.util.poi.excel.exception.IllegalNoSuchAnnotationException;
import com.base.util.poi.excel.exception.IllegalNoSuchPropertyException;
import com.base.util.poi.excel.exception.IllegalReturnTypeException;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName: ExcelAnnotationHandler
 * @Description: excel注解类配置处理
 * @Author: dhu
 * @Date: 2021/4/15 15:52
 * @Version: v1
 **/
public class ExcelAnnotationHandler {


    private Object excelInfo;

    private  Map<Object,Object> annotations = new HashMap<>();

    private ExcelAnnotationProperty excelAnnotationProperty;

    public  ExcelAnnotationHandler(Object excelInfo){
        this.excelInfo = excelInfo;
        handleExcelInfoAnnotation();
        handleExcelInfo();
    }

    public ExcelAnnotationProperty getExcelAnnotationProperty(){
        return  excelAnnotationProperty;
    }




    /**
     * 处理所有注解信息
     */
    private void handleExcelInfoAnnotation(){
        ExcelInfo excelInfoAnnotation = excelInfo.getClass().getAnnotation(ExcelInfo.class);
        annotations.put(ExcelInfo.class,excelInfoAnnotation);
        List<Field> excelColumnField = new ArrayList<>();
        List<Field> listBoxField = new ArrayList<>();
        List<Field> imageColumnField = new ArrayList<>();
        List<Field> dateColumnField = new ArrayList<>();
        List<Field> infoChildField = new ArrayList<>();
        List<Method> translateMethod = new ArrayList<>();
        List<Method> listBoxFieldMethod = new ArrayList<>();
        List<Method> customValidateMethod = new ArrayList<>();
        Class clazz = excelInfo.getClass();
        while(clazz!=null){
            Field[] declaredFields = clazz.getDeclaredFields();
            Method[] methods = clazz.getDeclaredMethods();
            if(declaredFields!=null){
                for(Field field : declaredFields){
                    if(field.getAnnotation(ExcelColumn.class)!=null){
                        excelColumnField.add(field);
                    }
                    if(field.getAnnotation(ExcelImage.class)!=null){
                        imageColumnField.add(field);
                    }
                    if(field.getAnnotation(ExcelDateFormat.class)!=null){
                        dateColumnField.add(field);
                    }
                    if(field.getAnnotation(ExcelInfoChild.class)!=null){
                        infoChildField.add(field);
                    }
                    if(field.getAnnotation(ExcelListBox.class)!=null){
                        listBoxField.add(field);
                    }
                    if(field.getAnnotation(ExcelTitle.class)!=null && !annotations.containsKey(ExcelTitle.class)){
                        annotations.put(ExcelTitle.class,field);
                    }
                    if(field.getAnnotation(ExcelData.class)!=null && !annotations.containsKey(ExcelData.class)){
                        annotations.put(ExcelData.class,field);
                    }
                }
            }

            if(methods!=null){
                for(Method method : methods){
                    if(method.getAnnotation(ExcelTranslateMethod.class)!=null){
                        translateMethod.add(method);
                    }
                    if(method.getAnnotation(ExcelListBox.class)!=null){
                        listBoxFieldMethod.add(method);
                    }
                    if(method.getAnnotation(ExcelCustomValidateMethod.class)!=null){
                        customValidateMethod.add(method);
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }
        annotations.put(ExcelColumn.class,excelColumnField);
        annotations.put(ExcelImage.class,imageColumnField);
        annotations.put(ExcelDateFormat.class,dateColumnField);
        annotations.put(ExcelTranslateMethod.class,translateMethod);
        annotations.put(ExcelListBox.class,listBoxField);
        annotations.put("ExcelListBoxMethod",listBoxFieldMethod);
        annotations.put(ExcelCustomValidateMethod.class,customValidateMethod);
        excelAnnotationProperty = new ExcelAnnotationPropertyInfo();
    }

    /**
     * 处理注解相关信息
     */
    private void handleExcelInfo(){
        ExcelInfo excelInfo = null;
        if((excelInfo=this.excelInfo.getClass().getAnnotation(ExcelInfo.class)) == null)
            throw  new IllegalNoSuchAnnotationException("缺少@ExcelInfo注解");
        ((ExcelAnnotationPropertyInfo)excelAnnotationProperty).excelInfo = excelInfo;
        handleExcelTitle();
    }

    /**
     * 处理标题信息
     */
    private void handleExcelTitle(){
        Field titleField = (Field) annotations.get(ExcelTitle.class);
        if(titleField != null ) {
            if(!String.class.isAssignableFrom(titleField.getType()))
                throw new IllegalReturnTypeException("被@ExcelTitle注解标识的字段类型必须是String");
            titleField.setAccessible(true);
            Object fieldValue = ReflectionUtils.getField(titleField, excelInfo);
            ((ExcelAnnotationPropertyInfo)excelAnnotationProperty).title = (String) fieldValue;
        }
        handleExcelColumn();
    }

    /**
     * 处理列信息
     */
    private void handleExcelColumn(){
        LinkedList<ExcelModel> excelModels = new LinkedList<>();
        List<Field> columns= (List<Field>) annotations.get(ExcelColumn.class);
        List<Field> excelListBoxFields= (List<Field>) annotations.get(ExcelListBox.class);
        List<Method> excelListBoxMethods= (List<Method>) annotations.get("ExcelListBoxMethod");
        List<Method> translateMethod= (List<Method>) annotations.get(ExcelTranslateMethod.class);
        List<Method> customValidateMethod= (List<Method>) annotations.get(ExcelCustomValidate.class);
        Map<Integer,Integer> columnWidth = new HashMap<>();
        Map<Integer,String> columnMergeInfo = new HashMap<>();
        if(columns!=null && columns.size()>0){
            columns.sort((o1, o2) -> {
                ExcelColumn annotation1 = o1.getAnnotation(ExcelColumn.class);
                ExcelColumn annotation2 = o2.getAnnotation(ExcelColumn.class);
                return Integer.compare(annotation1.index(),annotation2.index());
            });
            String [] header = new String[columns.size()];
            AtomicInteger count= new AtomicInteger();
            columns.forEach( column ->{
                column.setAccessible(true);
                String fieldName = column.getName();
                ExcelModel excelModel  = new ExcelModel(fieldName);
                ExcelColumn annotation = column.getAnnotation(ExcelColumn.class);
                ExcelImage image;
                ExcelDateFormat excelDateFormat;
                ExcelListBox excelListBox;
                String columnName = annotation.columnName();
                columnWidth.put(count.get(),annotation.columnWidth());
                if(annotation.needMergeCell()){
                    columnMergeInfo .put(count.get(),column.getName());
                }
                excelModel.setNullAble(annotation.nullable());

                if( (image=column.getAnnotation(ExcelImage.class))!=null){
                    String imageDownPath = image.imageDownPath();
                    String imageVisitPrex = image.imageVisitPrev();
                    excelModel.setPicture(true);
                    excelModel.setImageDownPath(imageDownPath);
                    excelModel.setImageVisitPrex(imageVisitPrex);
                }
                if(excelListBoxFields!=null){
                    List<Field> collect = excelListBoxFields.stream().filter(f -> f.getName().equals(fieldName)).collect(Collectors.toList());
                    if(collect.size() == 1 ){
                        excelListBox =  collect.get(0).getAnnotation(ExcelListBox.class);
                        String [] listArray = excelListBox.listTextBox();
                        if(listArray != null && listArray.length > 0){
                            Set<String> listSet = Arrays.stream(listArray).collect(Collectors.toSet());
                            excelModel.setListTextBox(listSet);
                        }

                    }
                }
                if(excelListBoxMethods!=null && excelListBoxMethods.size() > 0){
                    List<String> errorMethod = excelListBoxMethods.stream().filter(s -> !StringUtils.hasText(s.getAnnotation(ExcelListBox.class).columnName())).map(s -> s.getName()).collect(Collectors.toList());
                    if(errorMethod.size() > 0 ){
                        throw  new IllegalNoSuchPropertyException("方法'"+errorMethod+"'@ExcelListBox中的columnName不能为空");
                    }
                    List<Method> collect = excelListBoxMethods.stream().filter(s -> s.getAnnotation(ExcelListBox.class).columnName().equals(fieldName)).collect(Collectors.toList());
                    if(collect.size() == 1){
                        Method method = collect.get(0);
                        Class<?> returnType = method.getReturnType();
                        if(List.class.isAssignableFrom(returnType)){
                            try {
                                List<String> list = (List<String>) method.invoke(excelInfo);
                                if(list !=null && list.size() > 0 ){
                                    Set<String> collect1 = list.stream().collect(Collectors.toSet());
                                    excelModel.setListTextBox(collect1);
                                    excelModel.setListBox(true);
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }catch (ClassCastException e){
                                throw  new IllegalReturnTypeException("方法："+method.getName()+"通过@ExcelListBox注解标记之后返回类型只能为List<String>,Set<String>,String[]");
                            }
                        }else if(Set.class.isAssignableFrom(returnType)){
                            try {
                                Set<String> set = (Set<String>) method.invoke(excelInfo);
                                if(set != null && set.size() > 0){
                                    excelModel.setListTextBox(set);
                                    excelModel.setListBox(true);
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }catch (ClassCastException e){
                                throw  new IllegalReturnTypeException("方法："+method.getName()+"通过@ExcelListBox注解标记之后返回类型只能为List<String>,Set<String>,String[]");
                            }
                        }else if(returnType.getComponentType()!=null){
                            try {
                                String [] str = (String []) method.invoke(excelInfo);
                                if(str != null && str.length > 0){
                                    Set<String> collect1 = Arrays.stream(str).collect(Collectors.toSet());
                                    excelModel.setListTextBox(collect1);
                                    excelModel.setListBox(true);
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }catch (ClassCastException e){
                                throw  new IllegalReturnTypeException("方法："+method.getName()+"通过@ExcelListBox注解标记之后返回类型只能为List<String>,Set<String>,String[]");
                            }
                        }else {
                            throw  new IllegalReturnTypeException("方法："+method.getName()+"通过@ExcelListBox注解标记之后返回类型只能为List<String>,Set<String>,String[]");
                        }
                    }
                }
                if( (excelDateFormat=column.getAnnotation(ExcelDateFormat.class))!=null){
                    String pattern = excelDateFormat.pattern();
                    excelModel.setDate(true);
                    excelModel.setPattern(pattern);
                }
                if(translateMethod!=null){
                    Method m = null;
                    for(Method method : translateMethod){
                        ExcelTranslateMethod excelTranslateMethod = method.getAnnotation(ExcelTranslateMethod.class);
                        if(excelTranslateMethod.columnName().equals(fieldName)){
                            Class returnType  = method.getReturnType();
                            if(!Function.class.isAssignableFrom(returnType)){
                                throw new IllegalReturnTypeException("被@ExcelTranslateMethod标记的方法返回类型必须为Function<ExcelRowData,Object>");
                            }else{
                                try{
                                    excelModel.setBiFunction((Function<ExcelRowData, Object>) ReflectionUtils.invokeMethod(method,excelInfo));
                                }catch (ClassCastException e){
                                    throw new IllegalReturnTypeException("被@ExcelTranslateMethod标记的方法返回类型必须为Function<ExcelRowData,Object>");
                                }
                            }
                            m = method;
                            break;
                        }
                    }
                    if(m!=null)
                        translateMethod.remove(m);
                }

                if(column.getAnnotation(ExcelData.class) !=null){
                    ((ExcelAnnotationPropertyInfo)excelAnnotationProperty).excelData = ReflectionUtils.getField(column,excelInfo);
                }
                annotation.index();
                header[count.getAndIncrement()] = columnName;
                excelModels.add(excelModel);
            });

            ((ExcelAnnotationPropertyInfo)excelAnnotationProperty).header = header;
            ((ExcelAnnotationPropertyInfo)excelAnnotationProperty).excelModels = excelModels;
            ((ExcelAnnotationPropertyInfo)excelAnnotationProperty).mergeInfo = columnMergeInfo;
            ((ExcelAnnotationPropertyInfo)excelAnnotationProperty).columnWidthInfo = columnWidth;
            if(annotations.get(ExcelData.class)!=null){
                Field field =  (Field) annotations.get(ExcelData.class);
                field.setAccessible(true);
                ((ExcelAnnotationPropertyInfo)excelAnnotationProperty).excelData = ReflectionUtils.getField(field,this.excelInfo);
            }
        }
    }

    /**
     * Excel注解属性实现类
     */
    public static class ExcelAnnotationPropertyInfo implements ExcelAnnotationProperty{

        private String title;

        private String [] header;

        private List<ExcelModel> excelModels;

        private ExcelInfo excelInfo;

        private Object excelData;

        private Map<Integer,String> mergeInfo;

        private Map<Integer,Integer> columnWidthInfo;

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String[] getHeader() {
            return header;
        }

        @Override
        public List<ExcelModel> getExcelModels() {
            return excelModels;
        }

        @Override
        public ExcelInfo getExcelInfo() {
            return excelInfo;
        }

        @Override
        public Object getExcelData() {
            return excelData;
        }

        @Override
        public Map<Integer, String> getMergeInfo() {
            return mergeInfo;
        }

        @Override
        public Map<Integer, Integer> getColumnWidthInfo() {
            return columnWidthInfo;
        }
    }

}
