package com.base.util.poi.excel;

import com.base.util.poi.excel.annotation.ExcelInfo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: ExcelAnnotationProperty
 * @Description: Excel注解信息接口
 * @Author: dhu
 * @Date: 2021/4/15 16:19
 * @Version: v1
 **/
public interface ExcelAnnotationProperty {



    String getTitle();

    String [] getHeader();

    List<ExcelModel> getExcelModels();

    ExcelInfo getExcelInfo();

    Object getExcelData();

    Map<Integer,String> getMergeInfo();

    Map<Integer,Integer> getColumnWidthInfo();
}
