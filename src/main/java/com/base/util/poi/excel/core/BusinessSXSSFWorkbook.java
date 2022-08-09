package com.base.util.poi.excel.core;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFactory;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFRelation;

/**
 * @ClassName: BusinessHSSFWorkbook
 * @Description: TODO
 * @Author: dhu
 * @Date: 2022/8/5 15:44
 * @Version: v1
 **/
public class BusinessSXSSFWorkbook extends SXSSFWorkbook {

    public BusinessSXSSFWorkbook(BusinessXSSFWorkbook xssfWorkbook) {
        super(xssfWorkbook);
    }

    public   void addPicture(int index){
        ((BusinessXSSFWorkbook)getXSSFWorkbook()).addPicture(index);
    }
}
