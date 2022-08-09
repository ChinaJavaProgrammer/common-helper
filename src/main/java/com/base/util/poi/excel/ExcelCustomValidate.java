package com.base.util.poi.excel;

/**
 * 对导入的excel文件做自定义校验
 * @param <T>
 */
public interface ExcelCustomValidate<T> {


    boolean  validate (ExcelRowData<T> excelRowData);


    String   errorMessage();
}
