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

import uniandes.automat.sql.*;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MetamodelCoverageChecker {
	public static void main(String[] args) {
        // 绝对路径设置
        String dslFilePath = "E:\\xtext_repos_modified\\MISO4202_xtext-egl-sql2java\\examples\\example.dsl";
        String ecoreFilePath = "E:\\xtext_repos_modified\\MISO4202_xtext-egl-sql2java\\Gramatica\\uniandes.automat.sql\\model\\generated\\Metamodel.ecore";

        // 1. 使用Xtext生成的Injector解析DSL文件
        Injector injector = new SqlStandaloneSetup().createInjectorAndDoEMFRegistration();
        XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);

        // 2. 加载DSL文件
        Resource dslResource = resourceSet.getResource(URI.createFileURI(dslFilePath), true);
        EObject dslModel = dslResource.getContents().get(0);

        // 3. 加载Ecore元模型
        ResourceSet ecoreResourceSet = new ResourceSetImpl();
        ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
        Resource ecoreResource = ecoreResourceSet.getResource(URI.createFileURI(ecoreFilePath), true);
        EPackage ePackage = (EPackage) ecoreResource.getContents().get(0);

        // 4. 遍历DSL实例，获取使用的元模型类
        for (Iterator<EObject> it = dslModel.eAllContents(); it.hasNext(); ) {
            EObject eObject = it.next();
            EClass eClass = eObject.eClass();
            System.out.println("Instance uses EClass: " + eClass.getName());
        }

     // 5. 获取Ecore模型中的所有类
        List<EClassifier> ecoreClassifiers = ePackage.getEClassifiers();
        List<EClass> ecoreClasses = ecoreClassifiers.stream()
                .filter(EClass.class::isInstance) // 过滤EClassifier中的EClass
                .map(EClass.class::cast) // 转换为EClass
                .collect(Collectors.toList());

        // 比较和统计覆盖率可以在这里完成
        System.out.println("Ecore classes:");
        for (EClass eClass : ecoreClasses) {
            System.out.println(eClass.getName());
        }
    }
}
