package com.base.util.poi.excel;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @ClassName: ImageAddTask
 * @Description: TODO
 * @Author: dhu
 * @Date: 2022/8/9 9:46
 * @Version: v1
 **/
@Slf4j
public class ImageAddTask implements Runnable{


    private List<File> files;

    private CountDownLatch count;

    private String tempPath;

    public ImageAddTask(List<File> files, CountDownLatch count, String tempPath) {
        this.files = files;
        this.count = count;
        this.tempPath = tempPath;
    }

    @Override
    public void run() {
        File tempFile = null;
        long id = IdWorker.getId();
        log.debug("pic in :"+id);
        try{
            for(File file : files){
                String name = file.getName();
                tempFile = new File(tempPath+"/xl/media"+"/"+name);
                FileUtils.copyFile(file,tempFile);
                file.delete();
            }

        }catch (Exception e){
            log.error("picture add fail "+(tempFile!=null ? ":"+tempFile.getName() : ""),e);
        }
        log.debug("pic out :"+id);
        count.countDown();
        log.debug("picture add success:"+count.getCount());
    }
}
