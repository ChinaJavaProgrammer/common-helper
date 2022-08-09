package com.base.util.file;

import java.io.File;

/**
 * @ClassName: FileTool
 * @Author: dhu
 * @Date: 2021/3/3 14:51
 * @Description: 文件操作工具类
 * @version: 0.0.1
 **/
public class FileTool {




    public  static String getFileType(File file){
        if(file.exists()){
            String fileName = file.getName();
            fileName = fileName.substring(fileName.lastIndexOf("."));
            return fileName;
        }
        return null;
    }
}
