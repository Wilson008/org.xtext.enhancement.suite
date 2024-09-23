package org.xtext.complementary.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {
	/**
     * 递归遍历文件夹，并查找符合扩展名的文件
     * @param dir 要遍历的文件夹
     * @param extensions 文件扩展名数组
     * @param files 存储符合条件的文件列表
     */
    public static void listFilesWithExtensions(File dir, String[] extensions, List<File> files) {
        if (!dir.isDirectory()) {
            return;
        }
        
        // 列出文件夹中的所有文件和子文件夹
        File[] fileList = dir.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    // 如果是文件夹，递归查找
                    listFilesWithExtensions(file, extensions, files);
                } else {
                    // 检查文件的扩展名
                    for (String ext : extensions) {
                        if (file.getName().toLowerCase().endsWith(ext.toLowerCase())) {
                            files.add(file);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 递归遍历文件夹，并查找符合扩展名的文件
     * @param dir 要遍历的文件夹
     * @param extensions 文件扩展名数组
     * @param files 存储符合条件的文件列表
     */
	public static List<String> listFileNamesWithExtensions(String dir, String[] extensions) {
    	List<String> listFileNames = new ArrayList<>();  // 初始化为空的列表
    	if (dir == null || dir.isBlank())
    		return listFileNames;
    	
    	List<File> files = new ArrayList<>();
        listFilesWithExtensions(new File(dir), extensions, files);
        
        System.out.println("Found files: ");
        // 输出所有符合条件的文件的绝对路径
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
            listFileNames.add(file.getAbsolutePath());
        }
        
        return listFileNames;
    }
}
