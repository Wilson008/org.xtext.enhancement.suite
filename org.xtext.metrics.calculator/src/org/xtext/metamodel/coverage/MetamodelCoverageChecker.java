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
import es.udima.cesarlaso.tfm.*;
import es.udima.tfm.cesarlaso.IotDslStandaloneSetup;
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
		String repoPath = "E:\\xtext_repos_clone_new\\cesarlaso_tfm-udima-arquitectura-del-software-dsl-ecore-xtext-emf";
		String[] ecoreExtensions = {"ecore"};
		List<String> listEcoreFiles = FileHelper.listFileNamesWithExtensions(repoPath, ecoreExtensions);
		
		String[] xtextExtensions = {"xtext"};
		List<String> listXtextFiles = FileHelper.listFileNamesWithExtensions(repoPath, xtextExtensions);
		
		String[] insExtensions = {"iotproyect","iotproyect2"};
		List<String> listInstances = FileHelper.listFileNamesWithExtensions(repoPath, insExtensions);

		int iTotalCntClasses = 0;
        // get class names from the metamodels
        for (int i = 0; i < listEcoreFiles.size(); i++) {       	
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
        	String fileName = "grammar_rules_in_" 
        			+ FileHelper.getFileNameWithExtension(listXtextFiles.get(i))
        			+ "_" 
        			+ String.valueOf(i) 
        			+ ".txt";
        	iTotalCntRules += getRuleNamesFromSingleGrammar(listXtextFiles.get(i), fileName);
        }
        
        System.out.printf("Total count of grammar rules in the xtext files is: %d\n", iTotalCntRules);
        
        // get object types from the instances
        for (int i = 0; i < listInstances.size(); i++) {
        	String fileName = "types_in_" 
        			+ FileHelper.getFileNameWithExtension(listInstances.get(i))
        			+ "_" 
        			+ String.valueOf(i) 
        			+ ".txt";
        	//getTypesFromSingleIns(listInstances.get(i), fileName);
        }
    }
	
	public static void getTypesFromSingleIns(String dslFilePath, String saveFileName) {
		// 1. 使用Xtext生成的Injector解析DSL文件
        IotDslStandaloneSetup.doSetup();
        XtextResourceSet resourceSet = new XtextResourceSet();
        
		// 2. 加载DSL文件
        Resource dslResource = resourceSet.getResource(URI.createFileURI(dslFilePath), true);
        EObject dslModel = dslResource.getContents().get(0);
        
//		System.out.println("Instance types");
        // 存储唯一的EClass名
        Set<String> uniqueTypeNames = new HashSet<>();
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
	}
	
	public static int getRuleNamesFromSingleGrammar(String xtextFilePath, String saveFileName) {
		String strRaw = FileHelper.readFileContent(xtextFilePath);
		Set<String> uniqueTypeNames = GrammarHelper.getAllGrammarRuleNames(strRaw);
//		System.out.printf("Count of found grammar rules: %d\n", uniqueTypeNames.size());
		WriteToFile.appendUniqueNamesToFile(uniqueTypeNames, saveFileName);
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
