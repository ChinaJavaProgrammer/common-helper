package com.base.util.zip;

import com.base.util.webbaseconfig.exception.BusinessException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @ClassName: ZipUtil
 * @Description: 压缩与解压
 * @Author: dhu
 * @Date: 2022/8/5 14:16
 * @Version: v1
 **/
public class ZipUtil {

    private  static final Logger logger = LoggerFactory.getLogger(ZipUtil.class);

    /**
     * 解压
     * @param zipFilepath 压缩文件目录
     * @param destDir  解压目录
     * @throws BuildException
     * @throws RuntimeException
     */
    public static void unzip(String zipFilepath, String destDir) throws BuildException, RuntimeException {
        if (!new File(zipFilepath).exists()) {
            throw new RuntimeException("zip file " + zipFilepath + " does not exist.");
        }

        Project proj = new Project();
        Expand expand = new Expand();
        expand.setProject(proj);
        expand.setTaskType("unzip");
        expand.setTaskName("unzip");
        expand.setEncoding("utf8");

        expand.setSrc(new File(zipFilepath));
        expand.setDest(new File(destDir));
        expand.execute();

        logger.debug("uncompress success");
    }

    /**
     * 压缩
     * @param srcPathname   需要压缩的目录
     * @param zipFilepath   压缩后的存放路径
     * @throws BuildException
     * @throws RuntimeException
     */
    public static void zip(String srcPathname, String zipFilepath) throws BuildException, RuntimeException {
        File file = new File(srcPathname);
        if (!file.exists()) {
            throw new BusinessException("source file or directory " + srcPathname + " does not exist.");
        }

        Project proj = new Project();
        FileSet fileSet = new FileSet();
        fileSet.setProject(proj);
        // 判断是目录还是文件
        if (file.isDirectory()) {
            fileSet.setDir(file);
        } else {
            fileSet.setFile(file);
        }
        Zip zip = new Zip();
        zip.setProject(proj);
        zip.setDestFile(new File(zipFilepath));
        zip.addFileset(fileSet);
        zip.setEncoding("utf8");
        zip.execute();

        logger.debug("compress success");
    }

}
