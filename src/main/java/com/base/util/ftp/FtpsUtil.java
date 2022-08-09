//package com.base.util.ftp;
//
//import it.sauronsoftware.ftp4j.FTPClient;
//import it.sauronsoftware.ftp4j.FTPFile;
//
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSocketFactory;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//import java.io.*;
//import java.security.SecureRandom;
//import java.security.cert.X509Certificate;
//
//public class FtpsUtil {
//
//
//    public static  InputStream ftpsDownLoadFileInputStream(String ip, int port, String path,String fileName,String user,String password) {
//
//        OutputStream outputStream = ftpsDownLoadFileOutputStream(ip, port, path, fileName, user, password);
//
//        return outputStream == null ? null : new ByteArrayInputStream(((ByteArrayOutputStream)outputStream).toByteArray());
//    }
//
//    public static OutputStream ftpsDownLoadFileOutputStream(String ip, int port, String path,String fileName,String user,String password) {
//        ByteArrayOutputStream outputStream=null;
//
//        try {
//            TrustManager[] trustManager = new TrustManager[]{new X509TrustManager() {
//                public X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[1];
//                }
//
//                public void checkClientTrusted(X509Certificate[] certs,
//                                               String authType) {
//                }
//
//                public void checkServerTrusted(X509Certificate[] certs,
//                                               String authType) {
//                }
//            }};
//            SSLContext sslContext = null;
//            sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, trustManager, new SecureRandom());
//            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//            FTPClient client = new FTPClient();
//            client.setSSLSocketFactory(sslSocketFactory);
//            client.setSecurity(FTPClient.SECURITY_FTPS);
//            client.connect(ip, port);
//            client.login(user, password);
//            FTPFile[] list = client.list(path);
//            for (FTPFile file : list) {
//                if (file.getName().equals(fileName)) {
//                    outputStream = new ByteArrayOutputStream();
//                    client.download(path+"/"+file.getName(), outputStream, 0, null);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//        return outputStream;
//
//    }
//}
