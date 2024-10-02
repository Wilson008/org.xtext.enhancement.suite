package org.xtext.metamodel.coverage;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.EPackage;
//import ck2xtext.common.Ck2TerminalsStandaloneSetup;
//import ck2xtext.generic.Ck2StandaloneSetup;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import org.eclipse.emf.ecore.EEnum;
import org.xtext.complementary.helper.*;


public class MetamodelCoverageChecker {
	public static void main(String[] args) {
		String repoPath = "E:\\xtext_repos_clone_new\\PAMunb_MetaCrySL";

		List<String> listEcoreFiles = FileHelper.listFileNamesWithExtension(repoPath, "ecore");
		List<String> listXtextFiles = FileHelper.listFileNamesWithExtension(repoPath, "xtext");
		
		String[] insExtensions = {"gfx"};

		int iTotalCntClasses = 0;
        // get class names from the metamodels
        for (int i = 0; i < listEcoreFiles.size(); i++) {
        	if (FileHelper.isItBinFile(listEcoreFiles.get(i)) || FileHelper.isItSrcGenFile(listEcoreFiles.get(i)))
        		continue;
        	
        	String fileName = "classes_in_" 
        			+ FileHelper.getFileNameWithExtension(listEcoreFiles.get(i))
        			+ "_" 
        			+ String.valueOf(i) 
        			+ ".txt";
        	iTotalCntClasses += getClassesFromSingleMM(listEcoreFiles.get(i), fileName);
        }
        
        System.out.printf("Total count of classes in the metamodels is: %d\n", iTotalCntClasses);
        
        int iTotalCntRules = 0;
        // get grammar rule names from the grammars
        for (int i = 0; i < listXtextFiles.size(); i++) {

        	if (FileHelper.checkForSameFileWithoutBin(listXtextFiles.get(i), listXtextFiles))
        		continue;
        	
        	String fileName = "grammar_rules_in_" 
        			+ FileHelper.getFileNameWithExtension(listXtextFiles.get(i))
        			+ "_" 
        			+ String.valueOf(i) 
        			+ ".txt";
        	iTotalCntRules += getRuleNamesFromSingleGrammar(listXtextFiles.get(i), fileName);
        }
        
        System.out.printf("Total count of grammar rules in the xtext files is: %d\n", iTotalCntRules);
        
        // get types of objects in instances
//        getTypesFromInstances(repoPath, insExtensions);
    }
	
	public static void getTypesFromInstances(String repoPath, String[] insExtensions) {
		for (int j = 0; j < insExtensions.length; j++) {
			List<String> listInstances = FileHelper.listFileNamesWithExtension(repoPath, insExtensions[j]);
			
//	        int iTotalCntTypes = 0;
	        Set<String> uniqueTypeNames = new HashSet<>();
	        // get object types from the instances
	        for (int i = 0; i < listInstances.size(); i++) {
	        	String fileName = "types_in_" 
	        			+ FileHelper.getFileNameWithExtension(listInstances.get(i))
	        			+ "_" 
	        			+ String.valueOf(i) 
	        			+ ".txt";
	        	getTypesFromSingleIns(listInstances.get(i), fileName, uniqueTypeNames);
	        }
	        
	        System.out.printf("Total count of types in instances with extension %s is: %d.\n", insExtensions[j], uniqueTypeNames.size());
		}
	}
	
	public static void getTypesFromSingleIns(String dslFilePath, String saveFileName, Set<String> uniqueTypeNames) {
		// 1. 使用Xtext生成的Injector解析DSL文件
//		Ck2StandaloneSetup.doSetup();
        XtextResourceSet resourceSet = new XtextResourceSet();
        
		// 2. 加载DSL文件
        Resource dslResource = resourceSet.getResource(URI.createFileURI(dslFilePath), true);
        EObject dslModel = dslResource.getContents().get(0);
        
//		System.out.println("Instance types");
        // 存储唯一的EClass名
//        Set<String> uniqueTypeNames = new HashSet<>();
        EClass rootEClass = dslModel.eClass();
        uniqueTypeNames.add(rootEClass.getName());
        for (Iterator<EObject> it = dslModel.eAllContents(); it.hasNext(); ) {
            EObject eObject = it.next();
            EClass eClass = eObject.eClass();
            // 将非重复的名字加入Set
            uniqueTypeNames.add(eClass.getName());
            
//            System.out.println(eClass.getName());
            
            // 检查 EClass 中的 EStructuralFeatures，寻找 EEnum 类型
            eClass.getEAllStructuralFeatures().forEach(feature -> {
                if (feature.getEType() instanceof EEnum) {
                    EEnum eEnum = (EEnum) feature.getEType();
                    uniqueTypeNames.add(eEnum.getName());
//                    System.out.println("EEnum type: " + eEnum.getName());
                }
            });
        }
        
        WriteToFile.appendUniqueNamesToFile(uniqueTypeNames, saveFileName);
        
//        return uniqueTypeNames;
	}
	
	public static int getRuleNamesFromSingleGrammar(String xtextFilePath, String saveFileName) {
		String strRaw = FileHelper.readFileContent(xtextFilePath);
		strRaw = StringHelper.removeComments(strRaw);
		List<String> uniqueTypeNames = GrammarHelper.getAllGrammarRuleNames(strRaw);
//		System.out.printf("Count of found grammar rules: %d\n", uniqueTypeNames.size());
		WriteToFile.appendListToFile(uniqueTypeNames, saveFileName);
		return uniqueTypeNames.size();
	}
	
	public static int getClassesFromSingleMM(String ecoreFilePath, String saveFileName) {
		// 3. 加载Ecore元模型
        ResourceSet ecoreResourceSet = new ResourceSetImpl();
        ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
        Resource ecoreResource = ecoreResourceSet.getResource(URI.createFileURI(ecoreFilePath), true);
        EPackage ePackage = (EPackage) ecoreResource.getContents().get(0);
        
		List<EClassifier> ecoreClassifiers = ePackage.getEClassifiers();
        
        List<EClass> ecoreClasses = ecoreClassifiers.stream()
                .filter(EClass.class::isInstance) // 过滤EClassifier中的EClass
                .map(EClass.class::cast) // 转换为EClass
                .collect(Collectors.toList());
        
        List<EEnum> ecoreEnums = ecoreClassifiers.stream()
                .filter(EEnum.class::isInstance) // 过滤EClassifier中的EEnum
                .map(EEnum.class::cast) // 转换为EEnum
                .collect(Collectors.toList());

        // 比较和统计覆盖率可以在这里完成
//        System.out.println("Ecore classes:");
        Set<String> uniqueClassNames = new HashSet<>();
        for (EClass eClass : ecoreClasses) {
        	uniqueClassNames.add(eClass.getName());
//            System.out.println(eClass.getName());
        }
        
//        System.out.println("Ecore enums:");
        for (EEnum eEnum : ecoreEnums) {
        	uniqueClassNames.add(eEnum.getName());
//        	System.out.println(eEnum.getName());
        }
        
        WriteToFile.appendUniqueNamesToFile(uniqueClassNames, saveFileName);
        
        return uniqueClassNames.size();
	}
}
