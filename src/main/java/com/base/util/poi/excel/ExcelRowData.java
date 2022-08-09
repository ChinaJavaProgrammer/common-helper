package com.base.util.poi.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Map;

public interface ExcelRowData<T> {

    /**
     * 获取当前行对象
     * @return
     */
    Row getRow();

    /**
     * 获取当前列对象
     * @return
     */
    Cell getCell();

    /**
     * 获取当前行的原始数据
     * @return
     */
    T getRowData();


    <U> U getRowData(Class<U> uClass);

    /**
     * 当前列的数据
     * @return
     */
    Object currentValue();

    /**
     * 当前列数
     * @return
     */
    int  currentCellNum();

    /**
     * 当前行数
     * @return
     */
    int currentRowNum();


    /**
     * 获取excel中最原始的数据
     * @param uClass
     * @param <U>
     * @return
     */
    <U> U getOriginalCellData(Class<U> uClass);

    /**
     * 获取excel中最原始的数据
     * @return
     */
    Map<String,Object> getOriginalCellData();
}
