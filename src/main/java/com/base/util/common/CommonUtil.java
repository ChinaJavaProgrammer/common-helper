package com.base.util.common;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

public class CommonUtil {


    public static String getCurrentTime(String pattern){
        if(isEmpty(pattern))
            pattern="yyyy-MM-dd HH:mm:ss";
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * 	判断字符串是否为空
     * @Title: isEmpty
     * @Description: TODO
     * @param object
     * @return  
     * @return boolean
     */
    public static boolean isEmpty(String object){
        return object==null || object.trim().length()==0;
    }

    /**
     * 	参数必须为空，否则抛出异常
     * @Title: isNull
     * @Description: TODO
     * @param object
     * @param message  
     * @return void
     */
    public  static void isNull(Object object,String message) {
    	if(object!=null)
    		throw new NullPointerException(message);
    }

    /**
     * 	参数不能为空，如果为空则抛出异常
     * @Title: notNull
     * @Description: TODO
     * @param object
     * @param message  
     * @return void
     */
    public static void notNull(Object object,String message) {
    	if(object==null) 
    		throw new IllegalArgumentException(message);
    }

    /**
     * 	获取网卡所对应的所有IP列表
     * @Title: getLocalIP
     * @Description: TODO
     * @return  
     * @return List<String>
     */
    public static List<String> getLocalIP() {
        List<String> ipList = new ArrayList<String>();
        InetAddress ip;
        try {
            Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                // 遍历所有ip
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    ip = (InetAddress) ips.nextElement();
                    if (null == ip || "".equals(ip)) {
                        continue;
                    }
                    String sIP = ip.getHostAddress();
                    if(sIP == null || sIP.contains(":")) {
                        continue;
                    }
                    ipList.add(ni.getName()+":"+sIP);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ipList;
    }
    
    public static String formatDate(Object object,String pattern) {
        if(isEmpty(pattern))
            pattern="yyyy-MM-dd HH:mm:ss";
    	SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(object);
    }

    /**
     * 	通过网卡名称获取网卡
     * @Title: getLocalIP
     * @Description: TODO
     * @param networkName
     * @return  
     * @return String
     */
    public static String getLocalIP(String networkName){
        if(isEmpty(networkName))
        	throw new IllegalArgumentException("网卡名称不能为空");
        InetAddress ip;
        String localIp = null;
        try {
            Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                // 遍历所有ip
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    ip = ips.nextElement();
                    if (null == ip || "".equals(ip)) {
                        continue;
                    }
                    String sIP = ip.getHostAddress();
                    if(sIP == null || sIP.contains(":")) {
                        continue;
                    }
                   if(ni.getName().contains(networkName) || ni.getDisplayName().contains(networkName)){
                       localIp = sIP;
                       break;
                   }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localIp;
    }
    
    /**
     * 	原型模式必须实现cloneable接口
     * @Title: clone
     * @Description: TODO
     * @param t
     * @param protoType
     * @return  
     * @return T
     */
    public static <T> T clone(T t,String protoType) {
    	
    	return t;
    }
    
    /**
     * 	取最小值
     * @Title: min
     * @Description: TODO
     * @param num
     * @return  
     * @return int
     */
    public static int min(int ...num) {
        if(num==null || num.length==0)
            return Integer.MIN_VALUE;
        Arrays.sort(num);
    	return num[0];
    }

    public static double min(double ...num) {
        if(num==null || num.length==0)
            return Double.MIN_VALUE;
        Arrays.sort(num);
        return num[0];
    }

    public static float min(float ...num) {
        if(num==null || num.length==0)
            return Float.MIN_VALUE;
        Arrays.sort(num);
        return num[0];
    }

    public static long min(long ...num) {
        if(num==null || num.length==0)
            return Long.MIN_VALUE;
        Arrays.sort(num);
        return num[0];
    }
    
    /**
     * 	取最大值
     * @Title: max
     * @Description: TODO
     * @param num
     * @return  
     * @return int
     */
    public static int max(int ...num) {
        if(num==null || num.length==0)
            return Integer.MAX_VALUE;
        Arrays.sort(num);
        return num[num.length-1];
    }

    public static double max(double ...num) {
        if(num==null || num.length==0)
            return Double.MAX_VALUE;
        Arrays.sort(num);
        return num[num.length-1];
    }

    public static float max(float ...num) {
        if(num==null || num.length==0)
            return Float.MAX_VALUE;
        Arrays.sort(num);
        return num[num.length-1];
    }

    public static long max(long ...num) {
        if(num==null || num.length==0)
            return Long.MAX_VALUE;
        Arrays.sort(num);
        return num[num.length-1];
    }
    
    /**
     * 	下载文件
     * @Title: downLoadFile
     * @Description: TODO
     * @param url
     * @param response  
     * @return void
     */
    public static void downLoadFile(String url ,HttpServletResponse response) {
    	try {
    		notNull(url, "下载链接不能为空");
    		String httpProtocal = "http:";
    		if(url.startsWith("https"))
    			httpProtocal="https:";
    		url=url.substring(url.indexOf(":")+1);
    		String secondStr = url.substring(0,url.indexOf(":")+1);
    		String last = url.substring(url.indexOf(":")+1);
    		httpProtocal=httpProtocal+secondStr;
    		String [] array = last.split("/");
    		if(array.length==0) {
    			array=last.split("\\\\");
    		}
    		for(String s : array) {
    			httpProtocal+=URLEncoder.encode(s,"UTF-8")+"/";
    		}
    		httpProtocal=httpProtocal.substring(0,httpProtocal.length()-1);
    		System.out.println(httpProtocal);
			URL urlC = new URL(httpProtocal);
			URLConnection connetion = urlC.openConnection();
			connetion.connect();
			InputStream input = connetion.getInputStream();
		   	int v ;
	    	while((v=input.read())!=-1) {
	    		response.getOutputStream().write(v);
	    	}
	    	response.getOutputStream().flush();
	    	response.getOutputStream().close();
	    	input.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    public static  void downLoadFile(HttpServletResponse response, InputStream inputStream,String fileName) throws IOException {
		String outFileName = URLEncoder.encode(fileName, "UTF-8");
		response.setHeader("Content-Disposition", "attachment;fileName=" + outFileName);
    	int v ;
    	while((v=inputStream.read())!=-1) {
    		response.getOutputStream().write(v);
    	}
    	response.getOutputStream().flush();
    	response.getOutputStream().close();
    	inputStream.close();
    }
 
    public static String genetar() {
    	Random random = new Random();
    	String num  =(System.currentTimeMillis()+random.nextInt(1000))+"";
    	num = num.substring(0,10);
    	return num;
    }
    
    public static void main(String[] args) {
        System.out.println(genetar());
//        max();
//		System.out.println(getLocalIP());
		Integer s =133;
		Integer v=133;
        System.out.println(s==v.intValue());

	}
}
