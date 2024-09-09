package org.emf.grammargenerator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.util.XtextVersion;
import org.eclipse.xtext.xtext.wizard.EPackageInfo;
import org.eclipse.xtext.xtext.wizard.LanguageDescriptor;
import org.eclipse.xtext.xtext.wizard.LanguageDescriptor.FileExtensions;
import org.eclipse.xtext.xtext.wizard.ecore2xtext.Ecore2XtextGrammarCreator;
import org.eclipse.xtext.xtext.ui.wizard.project.XtextProjectInfo;
import com.google.common.collect.Lists;
import org.eclipse.xtext.xtext.wizard.Ecore2XtextConfiguration;
import org.eclipse.xtext.xtext.ui.wizard.project.XtextProjectCreator;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * HOW TO EXECUTE XSD TO XTEXT TRANSFORMATION:
 * (1) 		Run "runGeneratorPart1" as JUnit Test
 * (2)		Run "runGeneratorPart2" as JUnit Plugin (!) Test
 * (3.1) 	Import generated Xtext projects to current Eclipse workspace
 * (3.2) 	Select this project and refresh it
 * (3.3) 	Run "runGeneratorPart3" as JUnit Test
 * 
 * @author Patrick Neubauer - Initial contribution
 *
 */
@SuppressWarnings("restriction")
public class XsdToXtextGenerator {	
	/**
	 * ECLIPSE_WORKSPACE_LOCATION has to exactly match the workspace folder in
	 * which this projects resides
	 */
	public static String WORKSPACE_LOCATION;
	/**
	 * Location of the Xtext main project
	 */
	private static boolean useRuntimeProjectLocationForXText = true;
	//TODO: Leider geht es nicht anders
	private static String XTEXT_DSL_PROJECT_LOCATION;
	private static String TARGET_WORKSPACE_LOCATION;
	private static String PROJECT_DIRECTORY;
	/**
	 * Name of this project
	 */
	private static String PROJECT_NAME;
	
	public static String getWorkspaceLocation() {
        // 获取用户目录
        String userHome = System.getProperty("user.home");
        // Windows 下的默认工作区位置
        String windowsWorkspacePath = userHome + "\\eclipse-workspace";
        // macOS 下的默认工作区位置
        String macWorkspacePath = userHome + "/workspace";
        // Linux 下的默认工作区位置
        String linuxWorkspacePath = userHome + "/workspace";

        // 根据操作系统选择相应的工作区位置
        String workspaceLocation;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            workspaceLocation = windowsWorkspacePath;
        } else if (osName.contains("mac")) {
            workspaceLocation = macWorkspacePath;
        } else {
            workspaceLocation = linuxWorkspacePath;
        }

        // 检查工作区是否存在
        File workspaceDirectory = new File(workspaceLocation);
        if (workspaceDirectory.exists() && workspaceDirectory.isDirectory()) {
            return workspaceLocation;
        } else {
            return null;
        }
    }

	public static void XsdToXtextGenerator() {		
		String projectDirectory = XsdToXtextGenerator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		
		if (projectDirectory.endsWith("bin/")) {
			PROJECT_DIRECTORY = projectDirectory.substring(1, projectDirectory.length()-5);
		} 
		else {
		    PROJECT_DIRECTORY = projectDirectory.substring(1, projectDirectory.length()-1);
		}
		
	    PROJECT_NAME = PROJECT_DIRECTORY.substring(
	    		PROJECT_DIRECTORY.lastIndexOf("/")+1, 
	    		PROJECT_DIRECTORY.length());
		WORKSPACE_LOCATION = getWorkspaceLocation();
		
		TARGET_WORKSPACE_LOCATION = WORKSPACE_LOCATION;
		
		if (useRuntimeProjectLocationForXText) {
			XTEXT_DSL_PROJECT_LOCATION = TARGET_WORKSPACE_LOCATION;
		} 
		else {
			XTEXT_DSL_PROJECT_LOCATION = WORKSPACE_LOCATION;			
		}
		
		System.out.println("Xtext DSL Project Location: "+XTEXT_DSL_PROJECT_LOCATION);
	}
	
	public static void createXtextGrammar(Resource ecoreResource) {
		if (ecoreResource != null && !ecoreResource.getContents().isEmpty()) {
            // Assuming there's only one root element
			EPackage ePackage = (EPackage) ecoreResource.getContents().get(0);
			
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
	            EClassifier rootClassifier = GrammarGenerator.findClassifierByName(eClassifiers, userInput);

	            if (rootClassifier != null) {
	            	EClass ecoreResourceRoot = (EClass) rootClassifier;
	            	
	            	// Create a file chooser
	                JFileChooser fileChooser = new JFileChooser();
	                
	                // Set the file filter to show only .ecore files
	                FileNameExtensionFilter filter = new FileNameExtensionFilter("Genmodel Files", "genmodel");
	                fileChooser.setFileFilter(filter);

	                // Show the file chooser dialog
	                int result = fileChooser.showOpenDialog(null);
	                
	                // Check if the user selected a file
	                if (result == JFileChooser.APPROVE_OPTION) {
	                    // Get the absolute path of the selected file
	                	String genModelLocation = fileChooser.getSelectedFile().getAbsolutePath();
	                	
	                	// generate an Xtext grammar from the genmodel and ecore
	                	generateXtextGrammar(genModelLocation, ecoreResource, ecoreResourceRoot);
	                }
	            	
	            	break;
	            } else {
	                System.out.println("Failed to find the matched member.");
	                return;
	            }
	        }

	        scanner.close();
		}        
	}
	
	protected static List<EPackageInfo> createEPackageInfosFromGenModel(URI genModelURI, Resource genModelResource) {
		List<EPackageInfo> ePackageInfos = Lists.newArrayList();
		for (TreeIterator<EObject> i = genModelResource.getAllContents(); i.hasNext();) {
			EObject next = i.next();
			if (next instanceof GenPackage) {
				GenPackage genPackage = (GenPackage) next;
				EPackage ePackage = genPackage.getEcorePackage();
				URI importURI;
				if(ePackage.eResource() == null) {
					importURI = URI.createURI(ePackage.getNsURI());
				} else {
					importURI = ePackage.eResource().getURI();
				}
				EPackageInfo ePackageInfo = new EPackageInfo(ePackage, importURI, genModelURI, genPackage
						.getQualifiedPackageInterfaceName(), genPackage.getGenModel().getModelPluginID());
				ePackageInfos.add(ePackageInfo);
			} else if (!(next instanceof GenModel)) {
				i.prune();
			}
		}
		return ePackageInfos;
	}

	@SuppressWarnings("restriction")
	private static void generateXtextGrammar(String genModelLocation, Resource ecoreResource, EClass ecoreResourceRoot) {
		XsdToXtextGenerator();
		
		XtextProjectInfo ecore2XtextConfig = null;
		
		URI genModelURI = URI.createFileURI(genModelLocation);

		ResourceSet resourceSet = new ResourceSetImpl();
		EcoreResourceFactoryImpl ecoreFactory = new EcoreResourceFactoryImpl();
		Resource.Factory.Registry registry = resourceSet.getResourceFactoryRegistry();
		Map<String, Object> map = registry.getExtensionToFactoryMap();
		map.put("ecore", ecoreFactory);
		map.put("genmodel", ecoreFactory);

		GenModelPackage.eINSTANCE.eClass();

		Resource genModelResource = resourceSet.getResource(genModelURI, true);
		try
		{
			genModelResource.load(new HashMap());

			if (genModelResource.getContents().size() != 1) {
				System.out.println("Resource has " + genModelResource.getContents().size() + " loaded objects");
			} else {
				List<EPackageInfo> ePackageInfos = Lists.newArrayList();
				ePackageInfos.addAll(createEPackageInfosFromGenModel(genModelURI, genModelResource));

				ecore2XtextConfig = buildXtextProjectInfo(new XtextProjectInfo(), ePackageInfos, ecoreResourceRoot);

				// Generate Xtext Grammar
				Ecore2XtextGrammarCreator ecore2XtextGrammarCreator = new Ecore2XtextGrammarCreator();
				
				CharSequence grammar = ecore2XtextGrammarCreator.grammar(ecore2XtextConfig);

				FileHelper.saveToFile(grammar.toString());
			}
			
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
		}
	}// generateXtextProjects
	
	@SuppressWarnings("restriction")
	private static XtextProjectInfo buildXtextProjectInfo(XtextProjectInfo info, List<EPackageInfo> ePackageInfos, EClass rootClass) {
		Scanner scanner = new Scanner(System.in);

        System.out.println("Enter language project name:");
        String languagePrjName = scanner.nextLine();

        System.out.println("Enter language name:");
        String languageName = scanner.nextLine();

        System.out.println("Enter language file extension:");
        String languageFileExtension = scanner.nextLine();
        
        scanner.close();

		info.setEncoding(Charset.defaultCharset());
		info.setJavaVersion(JavaVersion.JAVA11);
		info.setXtextVersion(XtextVersion.getCurrent());
		info.setBaseName(languagePrjName + "." + languageName);
		info.setProjectName(languagePrjName);
		Ecore2XtextConfiguration config = info.getEcore2Xtext();
		config.getEPackageInfos().addAll(ePackageInfos);
		EPackageInfo defaultEPackageInfo = ePackageInfos.iterator().next();
		config.setDefaultEPackageInfo(defaultEPackageInfo);
		info.setRootLocation(XTEXT_DSL_PROJECT_LOCATION);
		System.out.println("Set Root location to "+XTEXT_DSL_PROJECT_LOCATION);
		LanguageDescriptor ld = info.getLanguage();
		
		FileExtensions fe = new FileExtensions(Arrays.asList(languageFileExtension));
		ld.setFileExtensions(fe);
		ld.setName(languagePrjName + "." + languageName);
		
		List<IWorkingSet> workingSets = new ArrayList<IWorkingSet>();
		info.setWorkingSets(workingSets);

		config.setRootElementClass(rootClass);
		
		return info;
	}
}