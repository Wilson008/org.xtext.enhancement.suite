package org.xtext.metamodel.coverage;

import com.google.inject.Injector;
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
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import uniandes.automat.sql.*;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.Enumeration;
import java.util.HashSet;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.emf.ecore.EEnum;

public class MetamodelCoverageChecker {
	public static void main(String[] args) {
        // 绝对路径设置
        String dslFilePath = "E:\\xtext_repos_modified\\MISO4202_xtext-egl-sql2java\\SegundaInstancia\\generador.sql";
        String ecoreFilePath = "E:\\xtext_repos_modified\\MISO4202_xtext-egl-sql2java\\Gramatica\\uniandes.automat.sql\\model\\generated\\Sql.ecore";

        // 1. 使用Xtext生成的Injector解析DSL文件
        SqlStandaloneSetup.doSetup();
        XtextResourceSet resourceSet = new XtextResourceSet();

        // 2. 加载DSL文件
        Resource dslResource = resourceSet.getResource(URI.createFileURI(dslFilePath), true);
        EObject dslModel = dslResource.getContents().get(0);
        

        // 3. 加载Ecore元模型
        ResourceSet ecoreResourceSet = new ResourceSetImpl();
        ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
        Resource ecoreResource = ecoreResourceSet.getResource(URI.createFileURI(ecoreFilePath), true);
        EPackage ePackage = (EPackage) ecoreResource.getContents().get(0);

        // 4. 遍历DSL实例，获取使用的元模型类
        System.out.println("Instance types");
        // 存储唯一的EClass名
        Set<String> uniqueTypeNames = new HashSet<>();
        for (Iterator<EObject> it = dslModel.eAllContents(); it.hasNext(); ) {
            EObject eObject = it.next();
            EClass eClass = eObject.eClass();
            // 将非重复的名字加入Set
            uniqueTypeNames.add(eClass.getName());
            
            System.out.println(eClass.getName());
        }
        
        WriteToFile.appendUniqueNamesToFile(uniqueTypeNames, "types_used_in_instances.txt");

        // 5. 获取Ecore模型中的所有类
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
        System.out.println("Ecore classes:");
        Set<String> uniqueClassNames = new HashSet<>();
        for (EClass eClass : ecoreClasses) {
        	uniqueClassNames.add(eClass.getName());
            System.out.println(eClass.getName());
        }
        
        System.out.println("Ecore enums:");
        for (EEnum eEnum : ecoreEnums) {
        	uniqueClassNames.add(eEnum.getName());
        	System.out.println(eEnum.getName());
        }
        
        WriteToFile.appendUniqueNamesToFile(uniqueClassNames, "classes_in_metamodel.txt");
    }
}
