package org.emf.grammargenerator;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import java.util.Map;
import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
//import org.eclipse.xtext.resource.XtextResource;
//import org.eclipse.xtext.resource.XtextResourceSet;

import org.eclipse.emf.ecore.EGenericType;
import com.google.inject.Injector;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.common.util.EList;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.xtext.xtext.wizard.Ecore2XtextConfiguration;
import org.eclipse.xtext.xtext.wizard.ecore2xtext.Ecore2XtextExtensions;
import java.util.Collection;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class GrammarGenerator extends AbstractHandler {

	public static void main(String[] args) {
		GrammarGenerator gg = new GrammarGenerator();
		gg.MainProc();
    }
	
	public void MainProc() {
		// Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        
        // Set the file filter to show only .ecore files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Ecore Files", "ecore");
        fileChooser.setFileFilter(filter);

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(null);

        // Check if the user selected a file
        if (result == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            java.io.File selectedFile = fileChooser.getSelectedFile();
            
            // Load the selected Ecore metamodel
            Resource resource = loadEcoreMetamodel(selectedFile.getAbsolutePath());
            
            //GenerateGrammar(resource);
            XsdToXtextGenerator.createXtextGrammar(resource);
        }
	}

	private static Resource loadEcoreMetamodel(String filePath) {
        try {
        	// Create a resource set.
            ResourceSet resourceSet = new ResourceSetImpl();
            
            // Register the default resource factory -- only needed for stand-alone!
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
            
            // Register the package -- only needed for stand-alone!
            EcorePackage ecorePackage = EcorePackage.eINSTANCE;

            URI fileURI = URI.createFileURI(filePath);

            // Demand load the resource for this file.
            Resource resource = resourceSet.getResource(fileURI, true);

            System.out.println("Successfully load Ecore metamodel!");
            
            return resource;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading Ecore metamodel from: " + filePath);
            return null;
        }
    }

	private static void GenerateGrammar(Resource resource) {
        if (resource != null && !resource.getContents().isEmpty()) {
            // Assuming there's only one root element
            EObject rootEObject = resource.getContents().get(0);

            // Process the root EObject and its children
            processEPackage((EPackage) rootEObject);
            //GenerateXtextGrammar.processEPackageNew((EPackage) rootEObject, 0);
        }
    }

	private static void processEPackage(EPackage ePackage) {
        System.out.println("EPackage: " + ePackage.getName());
        
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Please input the name of root class (or exit):");
            String userInput = scanner.nextLine();

            if ("exit".equalsIgnoreCase(userInput)) {
                System.out.println("Application exits!");
                break;
            }
            
            // 一个元模型中可能包含很多元类
            EList<EClassifier> eClassifiers = ePackage.getEClassifiers();

            // 在用户输入根元类之后，要先为根元类创建语法规则
            EClassifier rootClassifier = findClassifierByName(eClassifiers, userInput);

            if (rootClassifier != null) {
            	String rootGrammarRule = generateCommonRule(rootClassifier, false);
            	System.out.println("The root grammar rule is: \n" + rootGrammarRule);
            	
            	String fatherRules = generateCommonRules(eClassifiers, rootClassifier.getName());
            	System.out.println("The father rules are: \n" + fatherRules);
            	
            	String wholeGrammarText = rootGrammarRule + fatherRules;
            	FileHelper.saveToFile(wholeGrammarText);
            	
            	break;
            } else {
                System.out.println("Failed to find the matched member.");
                return;
            }
        }

        scanner.close();
    }
	
	public static String generateCommonRules(EList<EClassifier> eClassifiers, String rootRule) {
		String ret = "";
		
		for (EClassifier eClassifier : eClassifiers) {
			if (eClassifier instanceof EClass) {
	            EClass eClass = (EClass) eClassifier;
	            
	            if (eClass.getName().equals(rootRule))
	            	continue;
	            
	            List<String> sons = findEClassifiersWithSuperType(eClassifiers, eClass.getName());
	            
	            if (sons == null || sons.size() == 0) {
	            	ret += generateCommonRule(eClassifier, false);
	            }
	            else {
	            	ret += generateFatherRules(sons, eClass);
	            	ret += generateCommonRule(eClassifier, true);
	            }
			}
			else if (eClassifier instanceof EEnum) {
				EEnum eEnum = (EEnum) eClassifier;
				
				ret += generateRuleForEEnum(eEnum);
			}
		}
		
		return ret;
	}
	
	private static String generateRuleForEEnum(EEnum eEnum) {		
		StringBuilder ruleBuilder = new StringBuilder();
		
		// first line
		ruleBuilder.append("enum " + eEnum.getName()).append(" return ").append(eEnum.getName() + ":\n");
		
		// second line
		int literalNum = eEnum.getELiterals().size();
		
		if (literalNum == 1)
			ruleBuilder.append("    " + eEnum.getELiterals().get(0).getName() + " = '").append(eEnum.getELiterals().get(0).getName() + "';\n");
		else {
			for (int i = 0; i < literalNum; i++) {
				if (i == 0) {
					ruleBuilder.append("    " + eEnum.getELiterals().get(i).getName() + " = '").append(eEnum.getELiterals().get(i).getName() + "'");
				}
				else {
					ruleBuilder.append(" | " + eEnum.getELiterals().get(i).getName() + " = '").append(eEnum.getELiterals().get(i).getName() + "'");
					
					if (i == (literalNum - 1))
						ruleBuilder.append(";\n");
				}
			}
		}
		
		return ruleBuilder.toString();
	}
	
	private static String generateFatherRules(List<String> sons, EClass eClass) {
		String ret = "";
		
		ret += eClass.getName() + " return " + eClass.getName() + ":\n";
		
		if (!eClass.isAbstract()) {
			ret += "    " + eClass.getName() + "_Impl | ";
		}
		else 
			ret += "    ";
        
        if (sons.size() == 1) {
        	ret += sons.get(0) + ";\n";
        }
        else if (sons.size() > 1) {
        	for (int j = 0; j < sons.size(); j++) {
        		if (j == 0)
        			ret += sons.get(j);
        		else if (j == (sons.size() - 1))
        			ret += " | " + sons.get(j) + ";\n";
        		else 
        			ret += " | " + sons.get(j);
        	}
        }
        
        ret += "\n";
        
		return ret;
	}
    
    public static EClassifier findClassifierByName(EList<EClassifier> inputList, String name) {
        if (inputList == null || name == null) {
            return null;
        }

        for (EClassifier eClassifier : inputList) {
            if (name.equals(eClassifier.getName())) {
                return eClassifier;
            }
        }

        return null;
    }
    
    public static EList<EClassifier> filterEClassifiersByReferenceType(EList<EClassifier> inputList, String referenceTypeName) {
        if (inputList == null || referenceTypeName == null) {
            // 输入参数为空时返回 null
            return null;
        }

        EList<EClassifier> filteredList = new org.eclipse.emf.common.util.BasicEList<>();

        for (EClassifier eClassifier : inputList) {
            if (eClassifier instanceof EReference) {
                // 如果是 EReference，则检查其类型的名字是否与传入的字符串相同
                EReference eReference = (EReference) eClassifier;
                if (eReference.getEReferenceType() != null && referenceTypeName.equals(eReference.getEReferenceType().getName())) {
                    // 如果名字相同，则加入筛选后的列表
                    filteredList.add(eClassifier);
                }
            }
        }

        if (filteredList.isEmpty()) {
            // 如果没有找到匹配的项，则返回 null
            return null;
        }

        return filteredList;
    }
   
    public static String generateCommonRule(EClassifier eClassifier, Boolean impl) {
        if (eClassifier instanceof EClass) {
            EClass eClass = (EClass) eClassifier;

            StringBuilder ruleBuilder = new StringBuilder();

            if (impl)
            	// 第一行
                ruleBuilder.append(eClass.getName()+"_Impl").append(" return ").append(eClass.getName()).append(":").append(System.lineSeparator());
            else
            	// 第一行
                ruleBuilder.append(eClass.getName()).append(" return ").append(eClass.getName()).append(":").append(System.lineSeparator());
            
            
            // 第二行
            ruleBuilder.append("    '" + eClass.getName() + "'\n");

            // 第三行
            ruleBuilder.append("    '{'").append(System.lineSeparator());
            
            List<EStructuralFeature> attributes = collectAttributes(eClass);

            // 处理每个属性
            //for (EStructuralFeature feature : eClass.getEStructuralFeatures()) {
            for (EStructuralFeature feature : attributes) {
            	if (feature instanceof EAttribute) {
            		ruleBuilder.append("        ").append(processAttribute(feature)).append(System.lineSeparator());
            	}
            	else if (feature instanceof EReference) {
            		ruleBuilder.append("        ").append(processReference(feature)).append(System.lineSeparator());
            	}
            }

            // 最后一行
            ruleBuilder.append("    '}';\n").append(System.lineSeparator());

            return ruleBuilder.toString();
        }

        return null; // 如果不是 EClass 类型，返回 null 或者适当的错误处理
    }
    
    public static List<EStructuralFeature> collectAttributes(EClass eClass) {
        List<EStructuralFeature> allAttributes = new ArrayList<>();
        collectAttributesRecursively(eClass, allAttributes);
        return allAttributes;
    }
    
    private static void collectAttributesRecursively(EClass eClass, List<EStructuralFeature> allAttributes) {
        // 处理父类
        for (EClass superClass : eClass.getESuperTypes()) {
            collectAttributesRecursively(superClass, allAttributes);
        }

        // 处理当前类的属性
        for (EStructuralFeature feature : eClass.getEStructuralFeatures()) {
            allAttributes.add(feature);
        }
    }
    
    public static String processAttribute(EStructuralFeature feature) {
        if (feature instanceof EAttribute) {
            EAttribute eAttribute = (EAttribute) feature;

            // 核心文本
            String coreText;
            if (eAttribute.getUpperBound() == -1) {
                // upperbound 为 -1 的情况
                coreText = "'{' " + feature.getName() + "+=" + eAttribute.getEAttributeType().getName() +
                        " (',' " + feature.getName() + "+=" + eAttribute.getEAttributeType().getName() + ")* '}'";
            } else {
                // upperbound 不为 -1 的情况
                coreText = feature.getName() + "=" + eAttribute.getEAttributeType().getName();
            }

            // 判断 lowerbound 是否为 0
            if (feature.getLowerBound() == 0) {
                // 在核心文本两侧用 "()?" 包裹
                return "(" + coreText + ")?";
            } else {
                return coreText;
            }
        }

        return null; // 如果不是 EAttribute 类型，返回 null 或适当的错误处理
    }
    
    private static String processReference(EStructuralFeature feature) {
    	if (feature instanceof EReference) {
    		EReference eReference = (EReference) feature;

            // 核心文本
            String coreText;
            if (eReference.getUpperBound() == -1) {
            	if (eReference.isContainment()) {
            		// upperbound 为 -1 的情况
                    coreText = "'{' " + feature.getName() + "+=" + eReference.getEReferenceType().getName() +
                            " (',' " + feature.getName() + "+=" + eReference.getEReferenceType().getName() + ")* '}'";
            	}
            	else {
            		// upperbound 为 -1 的情况
                    coreText = "'{' " + feature.getName() + "+=[" + eReference.getEReferenceType().getName() + "|EString]" +
                            " (',' " + feature.getName() + "+=[" + eReference.getEReferenceType().getName() + "|EString]" + ")* '}'";
            	}
            } else {
            	if (eReference.isContainment()) {
            		// upperbound 不为 -1 的情况
                    coreText = feature.getName() + "=" + eReference.getEReferenceType().getName();
            	}
            	else {
            		// upperbound 不为 -1 的情况
                    coreText = feature.getName() + "=[" + eReference.getEReferenceType().getName() + "|EString]";
            	}
            }

            // 判断 lowerbound 是否为 0
            if (feature.getLowerBound() == 0) {
                // 在核心文本两侧用 "()?" 包裹
                return "('" + feature.getName() + "' " + coreText + ")?";
            } else {
                return coreText;
            }
        }

        return null; // 如果不是 EAttribute 类型，返回 null 或适当的错误处理
    }
    
    public static List<String> findEClassifiersWithSuperType(EList<EClassifier> eClassifiers, String superTypeName) {
        List<String> matchingClassifiers = new ArrayList<>();

        for (EClassifier eClassifier : eClassifiers) {
            if (eClassifier instanceof EClass) {
                EClass eClass = (EClass) eClassifier;

                // 检查是否有超类型
                if (hasSuperType(eClass, superTypeName)) {
                    matchingClassifiers.add(eClass.getName());
                }
            }
        }

        return matchingClassifiers;
    }

    private static boolean hasSuperType(EClass eClass, String superTypeName) {
        for (EGenericType genericType : eClass.getEGenericSuperTypes()) {
            if (superTypeName.equals(genericType.getERawType().getName())) {
                return true;
            }
        }

        return false;
    }

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		MainProc();
		return null;
	}
}