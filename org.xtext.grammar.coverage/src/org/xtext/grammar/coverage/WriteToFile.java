package org.xtext.grammar.coverage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class WriteToFile {

    public static void writeSetToFile(Set<String> set, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String member : set) {
                writer.write(member);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
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
//
//    public static void main(String[] args) {
//        // 示例用法
//        Set<String> exampleSet = Set.of("Apple", "Banana", "Cherry");
//        writeSetToFile(exampleSet, "output.txt");
//    }
}

