package com.base.util.encryption;

import sun.misc.CRC16;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

/**
 * @ClassName: CRCUtils
 * @Description: TODO
 * @Author: dhu
 * @Date: 2021/10/18 14:53
 * @Version: v1
 **/
public class CRCUtils {

    public static long getCRC32(String filePath) {
        long crc32Value = 0L;
        try {
            CRC32 crc32 = new CRC32();
            File file = new File(filePath);
            int fileLen = (int) file.length();
            InputStream in = new FileInputStream(file);
            //分段进行crc校验
            int let = 10 * 1024 * 1024;
            int sum = fileLen / let + 1;
            for (int i = 0; i < sum; i++) {
                if (i == sum - 1) {
                    let = fileLen - (let * (sum - 1));
                }
                byte[] b = new byte[let];
                in.read(b, 0, let);
                crc32.update(b);
            }
            crc32Value = crc32.getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return crc32Value;
    }

    public  static long getCRC(String s){
        Crc32c crc32c = new Crc32c();
        crc32c.update(s.getBytes(StandardCharsets.UTF_8),0,s.getBytes(StandardCharsets.UTF_8).length);
        return crc32c.getValue();
    }

    public static void main(String[] args) {
        System.out.println(getCRC("admin123"));
    }
}
