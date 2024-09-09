package org.emf.grammargenerator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.ecore.EObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import javax.swing.JFileChooser;

public class FileHelper {
	
	public static String readFile(String fileName) {
		String content = null;
		
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line;

            // Read each line and append it to the StringBuilder
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }

            // Get the final content as a string
            content = sb.toString();

            // Print or use the content as needed
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return content;
	}
	
	// 将文本写入文件，覆盖原有内容
    public static void writeTextToFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }
    
    public static void saveToFile(String wholeGrammarText) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Please choose a location to save the file:");
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String fileName = fileChooser.getSelectedFile().getAbsolutePath();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(wholeGrammarText);
                System.out.println("Saved file: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to save the file.");
            }
        } else {
            System.out.println("The user has cancelled the saving operation.");
        }
    }
}
