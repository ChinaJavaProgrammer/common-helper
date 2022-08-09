package com.base.util.poi.excel;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName: ImageDownLoadTask
 * @Description: TODO
 * @Author: dhu
 * @Date: 2022/7/28 10:38
 * @Version: v1
 **/
@Slf4j
public class ImageDownLoadTask implements Runnable{





    private Map<Integer,String> pictureIndexMapping;

    private String path;

    private int imageReadTimeOut;

    private CountDownLatch count;

    public ImageDownLoadTask(Map<Integer,String> pictureIndexMapping,String path,int imageReadTimeOut,CountDownLatch count){
        this.pictureIndexMapping = pictureIndexMapping;
        this.path = path;
        this.imageReadTimeOut = imageReadTimeOut;
        this.count =  count;
    }

    @Override
    public void run() {
        pictureIndexMapping.forEach( ( index,url) ->{
            try (FileOutputStream fileOutputStream = new FileOutputStream(path + File.separator + "image" + index + ".jpeg")){
                BufferedImage bufferImg;
                if (url.startsWith("http")) {
                    URL imgUrl = new URL(url);
                    URLConnection urlConnection = imgUrl.openConnection();
                    urlConnection.setConnectTimeout(imageReadTimeOut);
                    bufferImg = ImageIO.read(ImageIO.createImageInputStream(urlConnection.getInputStream()));
                } else {
                    bufferImg = ImageIO.read(new File(url));
                }
                if (bufferImg != null) {
                    ImageIO.write(bufferImg, "jpg", fileOutputStream);
                }
            }catch (Exception e){
                log.error("图片下载失败",e);
            }
        });
        count.countDown();
        log.debug("finish down picture task :"+count.getCount());
    }
}
