package org.xtext.complementary.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHelper {
	/**
     * 递归遍历文件夹，并查找符合扩展名的文件
     * @param dir 要遍历的文件夹
     * @param extensions 文件扩展名数组
     * @param files 存储符合条件的文件列表
     */
    public static void listFilesWithExtension(File dir, String extension, List<File> files) {
        if (!dir.isDirectory()) {
            return;
        }
        
        // 列出文件夹中的所有文件和子文件夹
        File[] fileList = dir.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    // 如果是文件夹，递归查找
                    listFilesWithExtension(file, extension, files);
                } else {
                    // 检查文件的扩展名
                	if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                        files.add(file);
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
	public static List<String> listFileNamesWithExtension(String dir, String extension) {
    	List<String> listFileNames = new ArrayList<>();  // 初始化为空的列表
    	if (dir == null || dir.isBlank())
    		return listFileNames;
    	
    	List<File> files = new ArrayList<>();
        listFilesWithExtension(new File(dir), extension, files);
        
        System.out.println("Found files: ");
        // 输出所有符合条件的文件的绝对路径
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
            listFileNames.add(file.getAbsolutePath());
        }
        
        return listFileNames;
    }
	
	public static String getFileNameWithoutExtension(String fileName) {
		// 去掉扩展名（找到最后一个"."的位置并截取）
        int dotIndex = fileName.lastIndexOf('.');
        String fileNameWithoutExtension = 
        		(dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        return fileNameWithoutExtension;
	}
	
	public static String getFileNameWithExtension(String fileName) {
		// 获取文件名（带扩展名）
        File file = new File(fileName);
        return file.getName(); 
	}
	
	/**
     * 读取给定文件的内容，并将其存储在一个字符串中
     * @param filePath 文本文件的绝对路径
     * @return 返回文件内容的字符串形式
     */
    public static String readFileContent(String filePath) {
        String strRaw = "";
        try {
            // 读取文件的所有内容并存储到字符串中
            strRaw = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            // 捕获异常，打印错误信息
            System.err.println("Error reading file: " + e.getMessage());
        }
        return strRaw;
    }
    
    // 定义函数，根据传入的单个路径进行检查，并在 paths 列表中搜索同名文件
    public static boolean checkForSameFileWithoutBin(String path, List<String> paths) {
        // 检查路径是否包含文件夹 "bin"
        if (path.contains("/bin/") || path.contains("\\bin\\")) {
            // 提取文件名（含扩展名）
            String fileName = getFileNameFromPath(path);

            // 遍历 paths 列表，检查是否存在同名文件且路径不含 "bin"
            for (String otherPath : paths) {
                if (!otherPath.contains("/bin/") && !otherPath.contains("\\bin\\") 
                    && getFileNameFromPath(otherPath).equals(fileName)) {
                    return true;  // 找到符合条件的同名文件，返回 true
                }
            }
        }
        return false;  // 如果没有找到符合条件的文件，返回 false
    }

    // 辅助函数，用于从路径中提取文件名
    private static String getFileNameFromPath(String path) {
        // 根据路径分隔符提取最后一个部分，即文件名
        if (path.contains("/")) {
            return path.substring(path.lastIndexOf('/') + 1);
        } else {
            return path.substring(path.lastIndexOf('\\') + 1);
        }
    }
    
    public static boolean isItBinFile(String fileName) {
		if (fileName == null)
			return false;
		
		// 检查路径中是否包含 'bin' 文件夹
        if (fileName.contains("\\bin\\")) {
        	return true;
        } else {
        	return false;
        }
	}
    
    public static boolean isItSrcGenFile(String fileName) {
    	if (fileName == null)
			return false;
		
		// 检查路径中是否包含 'bin' 文件夹
        if (fileName.contains("\\src-gen\\")) {
        	return true;
        } else {
        	return false;
        }
    }
}
