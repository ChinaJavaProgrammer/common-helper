package com.base.util.ftp;

import java.io.*;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


import org.apache.commons.net.ftp.*;


public class FtpUtil {


	public static String ftpUpload(InputStream in,String fileType,String host,String user,String password,String path,int port) {

		try {
			//创建一个FtpClient对象
					FTPClient ftpClient = new FTPClient();
					//创建ftp连接，默认是：21端口
					ftpClient.connect(host,port);
					//登录ftp服务器，使用用户名 和密码
					ftpClient.login(user, password);
					//上传文件
					//设置上传的路径
					ftpClient.changeWorkingDirectory(path);
					//修改上传文件的格式
					ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
					//第二个参数：上传文档的inputStream
					String fileName = generateCode()+"."+fileType;
					ftpClient.storeFile(fileName, in);
					//关闭连接
					ftpClient.logout();
					return fileName;
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	public InputStream  ftpDownLoadFile(String host,String user,String password,String path,int port) {

		try {
			//创建一个FtpClient对象
			FTPClient ftpClient = new FTPClient();
			//创建ftp连接，默认是：21端口
			ftpClient.connect(host,port);
			//登录ftp服务器，使用用户名 和密码
			ftpClient.login(user, password);
			//上传文件
			//设置上传的路径
			ftpClient.changeWorkingDirectory(path);
			//修改上传文件的格式
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			MyFTPFileFilters ftpFileFilter = new MyFTPFileFilters();
			ftpFileFilter.addFileName("");
			FTPFile[] ftpFiles = ftpClient.listFiles("", ftpFileFilter);
			InputStream inputStream=null;
			if(ftpFiles.length>0){
				inputStream = ftpClient.retrieveFileStream(path);
			}

			//关闭连接
			ftpClient.logout();
			return inputStream;
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	synchronized private static String generateCode() {
		synchronized (FtpUtil.class) {
			String uuid=UUID.randomUUID()+"";
			uuid+=System.currentTimeMillis();
			return uuid;
		}


	}

	public static class MyFTPFileFilters implements  FTPFileFilter{


		public Set<String> fileNames = new HashSet<>();

		public  void addFileName(String fileName){
			this.fileNames.add(fileName);
		}

		@Override
		public boolean accept(FTPFile file) {
			return fileNames.size()==0 ? true : fileNames.contains(file.getName());
		}
	}

	public static void main(String args[]) throws IOException {



	}

	public static byte[] change(byte [] a){
		byte [] b = new byte[a.length];
		for(int i=0;i<b.length;i++){
			b[i]= a[b.length-i-1];
		}
		return b;
	}
	public static int byteArr2Int(byte[] arr){
		return (arr[0] & 0xff) << 24 | (arr[1] & 0xff) << 16 | (arr[2] & 0xff) << 8 | (arr[3] & 0xff) << 0;
	}
}
