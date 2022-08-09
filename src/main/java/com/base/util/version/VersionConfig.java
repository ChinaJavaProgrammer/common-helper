package com.base.util.version;

import com.base.util.version.annotation.EnabledVersionInfo;
import com.sun.javafx.binding.StringFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Map;

/**
 * @ClassName: VersionConfig
 * @Description: TODO
 * @Author: dhu
 * @Date: 2021/6/8 10:02
 * @Version: v1
 **/
@Slf4j
public class VersionConfig implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware, ApplicationContextAware {




    private ClassLoader classLoader;

    private Environment environment;

    private ResourceLoader resourceLoader;

    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext =applicationContext;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader=classLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment=environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader=resourceLoader;
    }


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnabledVersionInfo.class.getName(), false);
        String versionFile = (String) annotationAttributes.get("versionFile");
        if(!StringUtils.isEmpty(versionFile)){
            Resource resource = resourceLoader.getResource(versionFile);
            if(resource != null){
                try {
                    InputStream inputStream = resource.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String version = null;
                    String versionMessage=null;
                    while((version = bufferedReader.readLine())!=null){
                        versionMessage = version;
                    }
                    if(StringUtils.isEmpty(versionMessage)){
                        log.info("当前项目无版本号信息");
                    }else{
                                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("version.txt");
                        String printMessage = "";
                        if(resourceAsStream!=null){
                            int available = resourceAsStream.available();
                            byte [] b = new byte[available];
                            IOUtils.readFully(resourceAsStream,b);
                            printMessage = String.format("\n %s",new String(b));
                        }
                        printMessage=String.format("\n %s \n %s \n",printMessage,versionMessage);
                        log.info("项目版本信息：{}",printMessage);
                    }
                } catch (IOException e) {
                    log.info("版本号文件读取失败：{}",versionFile);
                }
            }else{
                log.info("版本号文件读取失败：{}",versionFile);
            }
        }

    }
}
