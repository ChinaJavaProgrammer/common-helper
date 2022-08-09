package com.base.util.poi.excel;

/**
 * excel转换对象出去接口
 */
public interface ExcelTranslateHandler {



    default boolean needHandle(){return false;}

    default Object handler(ExcelRowData rowData){throw new UnsupportedOperationException();};
}
