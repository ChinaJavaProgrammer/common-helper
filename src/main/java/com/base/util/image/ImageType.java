package com.base.util.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: ImageType
 * @Description:
 * @author: dh
 * @date: 2020/11/20
 * @version: v1
 */
public enum ImageType {

    JPEG(0),
    PNG(1) ,
    GIF(2),
    BMP(3),
    WEBP(4),
    TIF(5);

    private final static  Map<String,ImageType> map = new ConcurrentHashMap<>();


    static{
        map.put("FFD8FF",JPEG);
        map.put("89504E47",PNG);
        map.put("47494638",GIF);
        map.put("424D",BMP);
        map.put("52494646",WEBP);
        map.put("49492A00",TIF);
    }
    private int i;
    ImageType(int i) {
    this.i=i;
    }

    public String getName(){
        return this.name();
    }




  public   static ImageType getInstance(byte [] b){
     String type =  bytesToHexString(b);
     return map.getOrDefault(type,JPEG);
  }

    private static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


}
