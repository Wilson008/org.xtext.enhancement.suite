package org.xtext.metamodel.coverage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class WriteToFile {
	/**
     * 将单个字符串值追加到已存在的文件中
     *
     * @param filePath 文件路径
     * @param content  要追加的内容
     * @throws IOException 处理文件时发生的异常
     */
    public static void appendToFile(String filePath, String content) throws IOException {
        // 使用 true 参数以追加模式打开文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(content);
            writer.newLine(); // 添加换行符，如果需要每个内容后面换行
        }
    }
    
    // 将Set中的唯一类名追加到文件中
    public static void appendUniqueNamesToFile(Set<String> classNames, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) { // true 表示追加写入
            for (String className : classNames) {
                writer.write(className);
                writer.newLine(); // 写入换行符
            }
        } catch (IOException e) {
            e.printStackTrace(); // 处理可能的IO异常
        }
    }
}
