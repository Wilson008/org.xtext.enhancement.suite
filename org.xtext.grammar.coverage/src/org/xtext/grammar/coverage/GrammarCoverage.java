package org.xtext.grammar.coverage;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.XtextPackage;
import java.io.IOException;
import org.eclipse.xtext.resource.XtextResource;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.AbstractRule;
import java.io.File;

public class GrammarCoverage {
	public static String targetFileName = null;
	
	public static void analyzeGrammarCoverage(Resource resource) throws IOException {
		if (appendAllGrammarRules(resource)) {
			// 将实例所用到的语法规则名找到并写入同名文件（补充在新行）
            appendUsedGrammarRules(targetFileName, resource);
		}		
	}
	
	public static boolean appendAllGrammarRules(Resource resource) throws IOException {
		boolean bSuccess = false;
		
		// 获取语法的对象
		if (resource instanceof XtextResource) {
	        XtextResource xtextResource = (XtextResource) resource;
	        
	        // 确保资源已经被解析
	        IParseResult parseResult = xtextResource.getParseResult();
	        if (parseResult == null) {
	            System.err.println("Failed to path the resource.");
	            return false;
	        }

	        // 通过解析结果获取根对象
	        EObject rootASTElement = parseResult.getRootASTElement();
	        if (rootASTElement == null) {
	            System.err.println("No root AST element found in the resource.");
	            return false;
	        }

	        // 获取 grammar rules
	        Grammar grammar = (Grammar) XtextPackage.eINSTANCE.getEFactoryInstance().create(rootASTElement.eClass());
	        
	        if (grammar != null) {
	        	// 获取语法文件的绝对路径
	        	String grammarFilePath = getGrammarPath(grammar);
	        	String grammarFileName = getFileNameWithoutExtension(grammarFilePath);
	        	
	        	// 打印一句话，语法文件XX被找到
	        	System.out.println("Found grammar file: " + grammarFilePath);
	        	
	        	// 我们将语法规则名字写入与语法文件同名的txt文件中（此处为拼凑文件名）
	        	targetFileName = grammarFileName + ".txt";
	        	
	        	// 文件的第一行放语法文件的绝对路径名
	        	WriteToFile.appendToFile(targetFileName, grammarFilePath);
	        	
	        	if (grammar.getRules().size() > 0) {
	        		// 将所有的语法规则写入与语法文件同名的txt文件中（此处为写操作）
		            for (AbstractRule rule : grammar.getRules()) {
		            	WriteToFile.appendToFile(targetFileName, rule.getName());
		            }
		            
		            // 打印一句话，XX个语法规则被写入YY文件
		            System.out.printf("d% grammar rules have been written into file %s\n", 
		            		grammar.getRules().size(), targetFileName);
		            
		            bSuccess = true;
	        	}
	        	else {
	        		System.err.println("No grammar rules is found");
	        	}
	        } else {
	            System.err.println("No grammar is found for the instance。");
	        } 
		}
		
		return bSuccess;
	}
	
	public static void appendUsedGrammarRules(String fileName, Resource resource) throws IOException {
		if (resource == null || fileName == null)
			return;
		
		// 获取实例文件的绝对路径
		String instanceFilePath = getInstancePath(resource);
		
		// 将实例文件路径名写入文件中
		WriteToFile.appendToFile(fileName, instanceFilePath);
		
		// 获取根对象
		EObject eObject = (EObject) resource.getContents();
		
		// 递归遍历对象并打印其类型
		traverseEObject(fileName, eObject);
	}
	
	// 递归遍历资源中的 EObject 并打印其类型
	public static void traverseEObject(String fileName, EObject eObject) throws IOException {
		EClass eClass = eObject.eClass();
		WriteToFile.appendToFile(fileName, eClass.getName());
		
		// 遍历子对象（如果有）
		for (EObject child : eObject.eContents()) {
			traverseEObject(fileName, child);
		}
	}
	
	public static String getInstancePath(Resource resource) {
		String path = null;
		
		// 获取资源的 URI
        URI resourceURI = resource.getURI();
        
        // 如果是文件 URI，转换为绝对路径
        if (resourceURI.isFile()) {
        	path = resourceURI.toFileString();
        } else {
            System.out.println("The resource is not a file.");
        }
        
		return path;
	}
	
	public static String getGrammarPath(Grammar grammar) {
		String path = null;
		
		// 获取 Grammar 的 Resource
        Resource grammarResource = grammar.eResource();

        if (grammarResource != null) {
            URI grammarURI = grammarResource.getURI();
            
            // 获取绝对路径
            path = grammarURI.toFileString();
            System.out.println("Grammar absolute path: " + path);
        }
        
		return path;
	}
	
	public static String getFileNameWithoutExtension(String filePath) {
        // 创建文件对象
        File file = new File(filePath);
        
        // 获取文件名（包含扩展名）
        String fileName = file.getName();
        
        // 去掉扩展名并返回
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        
        return fileName; // 如果没有扩展名，直接返回文件名
    }
	
//	public static void analyzeGrammarCoverage(String instanceFilePath, String grammarFilePath) {
//        // Step 1: Parse the DSL instance file and generate the AST
//		EObject ast = parseDslInstance(instanceFilePath);
//
//        // Step 2: Collect used grammar rules from the AST
//        Set<String> usedRules = new HashSet<>();
//        collectUsedRules(ast, usedRules);
//
//        // Step 3: Get all grammar rules from the DSL grammar file
//        Set<String> allRules = getAllGrammarRules(grammarFilePath);
//
//        // Step 4: Compute the difference between all rules and used rules
////        Set<String> unusedRules = new HashSet<>(allRules);
////        unusedRules.removeAll(usedRules);
//        
//        WriteToFile.writeSetToFile(allRules, "allRules.txt");
//        WriteToFile.writeSetToFile(usedRules, "usedRules.txt");
//        
////        int iTotalCount = allRules.size();
////        int iUsedCount = usedRules.size();
////        
////        double usedRatio = (double) iUsedCount / iTotalCount;
////        
////        // 格式化为保留两位小数
////        DecimalFormat df = new DecimalFormat("0.00");
////        String formattedRatio = df.format(usedRatio * 100) + "%";
////        System.out.println("占比: " + formattedRatio);
//
////        // Step 5: Print or save the list of unused rules
////        for (String rule : unusedRules) {
////            System.out.println("Unused rule: " + rule);
////        }
//    }
	
	public static EObject parseDslInstance(String instanceFilePath) {
        // 创建ResourceSet
        ResourceSet resourceSet = new XtextResourceSet();
        // 加载资源（DSL实例文件）
        Resource resource = resourceSet.getResource(URI.createFileURI(instanceFilePath), true);
        // 解析模型
        EObject model = (EObject) resource.getContents().get(0);
        return model;
    }
	
	public static String getGrammarRuleName(EObject node) {
        if (node == null) {
            return null;
        }
        EStructuralFeature feature = node.eClass().getEStructuralFeature("grammarElement");
        if (feature != null) {
            Object grammarElement = node.eGet(feature);
            if (grammarElement != null) {
                return grammarElement.toString();
            }
        }
        return node.eClass().getName();
    }
	
	public static void collectUsedRules(EObject node, Set<String> usedRules) {
	    String ruleName = getGrammarRuleName(node); // 获取对应的语法规则
	    usedRules.add(ruleName);
	    
	    // 递归遍历子节点
	    for (EObject child : node.eContents()) {
	        collectUsedRules(child, usedRules);
	    }
	}
	
	public static Set<String> getAllGrammarRules(String grammarFilePath) {
        Set<String> ruleNames = new HashSet<>();

        try {
            // Initialize the resource set
            ResourceSet resourceSet = new XtextResourceSet();

            // Create a URI from the file path
            URI grammarURI = URI.createFileURI(grammarFilePath);

            // Load the grammar resource
            Resource resource = resourceSet.getResource(grammarURI, true);

            // Ensure the resource is loaded
            resource.load(null);

            // Access the grammar
            if (resource instanceof XtextResource) {
                XtextResource xtextResource = (XtextResource) resource;
                Grammar grammar = xtextResource.getContents().get(0) instanceof Grammar
                        ? (Grammar) xtextResource.getContents().get(0)
                        : null;

                if (grammar != null) {
                    // Iterate over all grammar rules and collect their names
                    grammar.getRules().forEach(rule -> ruleNames.add(rule.getName()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ruleNames;
    }
}
