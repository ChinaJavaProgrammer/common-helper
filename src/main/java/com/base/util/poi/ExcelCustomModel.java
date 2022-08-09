package com.base.util.poi;

import com.base.util.poi.excel.ExcelModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Map;

/**
 * @ClassName: ExcelCustomModel
 * @Description: TODO
 * @Author: dhu
 * @Date: 2021/6/28 14:34
 * @Version: v1
 **/
public class ExcelCustomModel {

    /**
     * 当前列号
     */
    private int currentCellNum;

    /**
     * 当前行号
     */
    private int currentRowNum;

    /**
     * 当前行对象
     */
    private Row row;

    /**
     * 当前列对象
     */
    private Cell cell;

    /**
     * 当前值
     */
    private Object currentValue;


    public Object getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Object currentValue) {
        this.currentValue = currentValue;
    }

    public Row getRow() {
        return row;
    }

    public void setRow(Row row) {
        this.row = row;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    /**
     * 当前行校验模型
     */
    private ExcelModel excelModel;


    public int getCurrentCellNum() {
        return currentCellNum;
    }

    public void setCurrentCellNum(int currentCellNum) {
        this.currentCellNum = currentCellNum;
    }

    public int getCurrentRowNum() {
        return currentRowNum;
    }

    public void setCurrentRowNum(int currentRowNum) {
        this.currentRowNum = currentRowNum;
    }


    public ExcelModel getExcelModel() {
        return excelModel;
    }

    public void setExcelModel(ExcelModel excelModel) {
        this.excelModel = excelModel;
    }
}
