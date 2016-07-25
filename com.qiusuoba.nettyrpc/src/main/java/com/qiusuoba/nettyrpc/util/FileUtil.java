package com.qiusuoba.nettyrpc.util;  

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.qiusuoba.nettyrpc.common.Constants;

/**
 *文件util类 
 *@Author:caimin 
 *@Since:2016年7月25日  
 *@Version:
 */
public class FileUtil {

	
	/**
	 * create file
	 * @param filePath
	 * @param content
	 * @throws IOException
	 */
	public static void createFile(String filePath, String content) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(filePath);
			writer.write(content);
		} catch(IOException ex) {
			throw ex;
		} finally {
			if(writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * create multilevel folder
	 * @param path
	 */
	public static void createFolder(String path){
		File file = new File(path);
		file.mkdirs();
	}
	
	/**
	 * move file
	 * @param oldPath
	 * @param newPath
	 */
	public static void moveFile(String oldPath, String newPath) {
		File fileOld = new File(oldPath);
		if(fileOld.exists()) {
			File fileNew = new File(newPath);
			fileOld.renameTo(fileNew);
		}
	}
	
	/**
	 * delete file
	 * @param path
	 */
	public static void deleteFile(String path) {
		File file = new File(path);
		file.deleteOnExit();
	}
	
	/**
	 * get all file which in dir
	 * @param dir
	 * @param extension
	 * @return
	 */
	public static List<File> getFiles(String dir, String... extension) {
		File f = new File(dir);
		if (!f.isDirectory()) {
			return null;
		}

		List<File> fileList = new ArrayList<File>();
		getFiles(f, fileList, extension);
		
		return fileList;
	}
	
	/**
	 * 得到第一层目录结构中的jar包，得到所有的class文件
	 * @param dirList
	 * @return
	 * @throws IOException 
	 */
	public static List<String> getFirstPath(String... dirs) throws IOException{

		List<String> jarList = new ArrayList<String>();
		List<String> fileNameList = new ArrayList<String>();
		
		for(String dir : dirs) {
			List<File> fileList = FileUtil.getFiles(dir, "rar", "jar", "war", "ear","class");
			if(fileList != null) {
				for(File file : fileList) {
					if(!fileNameList.contains(file.getName()) && file.getCanonicalPath().startsWith(Constants.ROOT_PATH)) {
						if(file.getName().endsWith(".class"))
						{
							jarList.add(file.getCanonicalPath().substring(dir.length()+1));
							fileNameList.add(file.getName());
						}
						else
						{
							jarList.add(file.getCanonicalPath());
							fileNameList.add(file.getName());
						}
					}
				}
			}
		}
		return jarList;
	}
	
	
	/**
	 * get all jar/war/ear which in dir
	 * @param dirList
	 * @return
	 * @throws IOException 
	 */
	public static List<String> getUniqueLibPath(String... dirs) throws IOException{

		List<String> jarList = new ArrayList<String>();
		List<String> fileNameList = new ArrayList<String>();
		
		for(String dir : dirs) {
			List<File> fileList = FileUtil.getFiles(dir, "rar", "jar", "war", "ear","class");
			if(fileList != null) {
				for(File file : fileList) {
					if(!fileNameList.contains(file.getName())) {
						jarList.add(file.getCanonicalPath());
						fileNameList.add(file.getName());
					}
				}
			}
		}

		return jarList;
	}
	
	/**
	 * 
	 * @param file
	 * @param fileList
	 * @param extension
	 */
	private static void getFiles(File f, List<File> fileList, String... extension) {
		File[] files = f.listFiles();
		if(files==null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				getFiles(files[i], fileList, extension);
			} else if (files[i].isFile()) {
				
				String fileName = files[i].getName().toLowerCase();
				boolean isAdd = false;
				if(extension != null) {
					for(String ext : extension) {
						if (fileName.lastIndexOf(ext) == fileName.length() - ext.length()){
							isAdd = true;
							break;
						}
					}
				}
				
				if(isAdd) {
					fileList.add(files[i]);
				}
			}
		}
	}
	
	/**
	 * 按行读取文件
	 * @param path
	 * @throws IOException 
	 */
	public static String getContentByLines(String path) throws IOException {
        File file = new File(path);
        if(!file.exists()) {
        	throw new IOException("file not exist:" + path);
        }
        BufferedReader reader = null;
        StringBuilder sbContent = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
            	sbContent.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
            	try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
        
        return sbContent.toString();
	}


}
