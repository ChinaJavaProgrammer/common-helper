package com.base.util.image;

import org.springframework.util.StringUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @ClassName: ImageUtils
 * @Description:
 * @author: dh
 * @date: 2020/11/19
 * @version: v1
 */
public class ImageUtils {


    private static final String DATA="data";

    private static final String HTTP="http";


    /**
     *  将图片转化为Base64编码
     * @param image         图片对象
     * @param needPrefix    是否加上前缀比如 data:image/jpg;base64
     * @return  返回通过base64编码之后的字符串
     */
    public static  String base64EncoderImage(BufferedImage image,boolean needPrefix) throws IOException {
        return base64EncoderImage(image,needPrefix,null);
    }

    public static  String base64EncoderImage(BufferedImage image,boolean needPrefix,String formatName) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, StringUtils.isEmpty(formatName) ? "JPEG" : formatName ,byteArrayOutputStream);
        byte [] b = byteArrayOutputStream.toByteArray();
        Base64.Encoder encoder = Base64.getEncoder();
        return needPrefix ? "data:image/"+(StringUtils.isEmpty(formatName) ? "JPEG" : formatName)+";base64,"+encoder.encodeToString(b) : encoder.encodeToString(b);
    }

    /**
     * 将base64字符串解码成图片
     * @param base64Str      通过base64编码之后的串
     * @return  返回一个图片对象
     */
    public static BufferedImage base64DecoderImage(String base64Str){
        Base64.Decoder base64Decoder = Base64.getDecoder();
        BufferedImage image=null;
        if(base64Str==null){
            return null;
        }

        if(base64Str.startsWith(DATA)){
            base64Str=base64Str.substring(base64Str.indexOf(",")+1);
        }

        try {
            byte[] b = base64Decoder.decode(base64Str);
            for (int i = 0; i < b.length; ++i) {
                //修正异常字节
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(b);
            image = ImageIO.read(byteArrayInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    /**
     * 下载图片
     * @param url           图片的访问地址
     * @param fileSavePath      图片存放的物理磁盘位置
     * @param needCoverSameFile 如果物理磁盘下有同名的文件是否需要覆盖,如果不需要覆盖则遇到相同文件是会抛出异常
     */
    public static void downLoadImage(String url,String fileSavePath,boolean needCoverSameFile) throws SameFileNameException{
        url = url.replaceAll("\\\\","/");
       fileSavePath = fileSavePath.replaceAll("\\\\","/");
       String suffix = fileSavePath.substring(fileSavePath.lastIndexOf(".")+1);
        try {
            BufferedImage image =  ImageIO.read(new URL(url));
            String prefix = fileSavePath.substring(0,fileSavePath.lastIndexOf("/"));
            File file = new File(prefix);
            if(!file.exists()){
                file.mkdirs();
            }
            file = new File(fileSavePath);
            if(file.exists() && !needCoverSameFile){
                throw new SameFileNameException(fileSavePath+" has the same file ");
            }
            ImageIO.write(image,suffix,new FileOutputStream(fileSavePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取图片
     * @param imageUrl  图片访问地址，可以是当前系统的物理磁盘路径可以是http协议路径
     * @return          缓冲图片流
     */
    public static  BufferedImage  getImage(String  imageUrl){
        return getImage(imageUrl,0);
    }

    /**
     * 获取图片
     * @param imageUrl  图片访问地址，可以是当前系统的物理磁盘路径可以是http协议路径
     * @timeOut   读取图片超时时间毫秒值，如果超时则返回
     * @return      缓冲图片流
     */
    public static BufferedImage getImage(String  imageUrl,int timeOut){
        BufferedImage image = null;
        try {
            if(imageUrl==null || (imageUrl=imageUrl.trim()).length()==0){
                return null;
            }else if(imageUrl.startsWith(HTTP)){
                URL url = new URL(imageUrl);
                URLConnection urlConnection = url.openConnection();
                if(timeOut>0){
                    urlConnection.setConnectTimeout(timeOut);
                }
                InputStream inputStream = urlConnection.getInputStream();
                image =  ImageIO.read(ImageIO.createImageInputStream(inputStream));
            }else{
                image = ImageIO.read(new FileInputStream(imageUrl));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static String urlEncoder(String url){
        return url;
    }

    public static void main(String[] args) throws IOException {
        BufferedImage read = ImageIO.read(new File("D://fb5eb76bfad671a169013c54fb79e5d5 (1).jpg"));
        String s = base64EncoderImage(read, true);
        System.out.println(s);
    }
}
