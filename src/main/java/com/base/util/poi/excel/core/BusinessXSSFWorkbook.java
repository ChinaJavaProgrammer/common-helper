package com.base.util.poi.excel.core;

import org.apache.poi.POIXMLException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFactory;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @ClassName: BusinessXSSFWorkbook
 * @Description: TODO
 * @Author: dhu
 * @Date: 2022/8/5 15:43
 * @Version: v1
 **/
public class BusinessXSSFWorkbook extends XSSFWorkbook {




    public   void addPicture(int index){
        XSSFPictureData img = (XSSFPictureData)createRelationship(XSSFRelation.IMAGE_JPEG, XSSFFactory.getInstance(), index, true);
        if(index == 1){
            getAllPictures();
        }else {
            getAllPictures().add(img);
        }
    }
}
