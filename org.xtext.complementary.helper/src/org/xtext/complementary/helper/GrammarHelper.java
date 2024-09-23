package org.xtext.complementary.helper;

import java.util.regex.Pattern;
import org.xtext.grammar.grammarrule.*;

import java.util.HashSet;
import java.util.Set;

public class GrammarHelper {
	public static Set<String> getAllGrammarRuleNames(String strRaw) {
		Set<String> uniqueTypeNames = new HashSet<>();
		
		Grammar grammar = new Grammar();
		processManualGrammar(grammar, strRaw);
		
		if (grammar.getRules().size() > 0)
			System.out.println("Succesfully get all grammar rules!");
		
		for (int i = 0; i < grammar.getRules().size(); i++) {
			if (grammar.getRules().get(i).getName() != null && !grammar.getRules().get(i).getName().isEmpty()) {
				uniqueTypeNames.add(grammar.getRules().get(i).getName());
			}
		}
		
		return uniqueTypeNames;
	}
	
	public static Grammar processGrammar(Grammar grammar, String strRaw) {
		// Split the contents of Xtext file (i.e. string) into lines
		String lines[] = strRaw.split("\r\n|\r|\n");

		// split the text into different GrammarRule instances
		GrammarRule grammarRule = new GrammarRule();

		for (int i = 0; i < lines.length; i++) {			
			//if (!lines[i].isEmpty() && !lines[i].equals("\t") && !(lines[i].trim().length() == 0)) {
			if (!lines[i].isEmpty() && !lines[i].equals("\t") && !lines[i].equals("\\s+")) {
				LineEntry lineEntry = new LineEntry();

				lineEntry.setLineContent(lines[i]);

				String regex = "(\\w*)\\s*\\+*\\?*\\=.*\\w*";
				Pattern pattern = Pattern.compile(regex);
				String attrName = RegexHelper.getTargetString(lines[i], pattern);
				lineEntry.setAttrName(attrName);

				// collect the lines from a same rule
				// grammarRule.lines.add(lineEntry);
				grammarRule.getLines().add(lineEntry);
			} else {
				if (grammarRule.getLines() == null || grammarRule.getLines().size() == 0)
					continue;

				// when encounter empty line, means a grammar rule finishes
				grammar.getRules().add(grammarRule);

				// then clear grammar rule object
				grammarRule = new GrammarRule();
			}
		}

		grammar.getRules().add(grammarRule);

		return processFirstLine(grammar);
	}
	
	public static Grammar processManualGrammar(Grammar grammar, String strRaw) {
		// Split the contents of Xtext file (i.e. string) into lines
		String lines[] = strRaw.split("\r\n|\r|\n");

		// split the text into different GrammarRule instances
		GrammarRule grammarRule = new GrammarRule();
		boolean bStartFromBeginning = true;
		
		for (int i = 0; i < lines.length; i++) {
			if (bStartFromBeginning) {
				if (lines[i].trim().length() == 0)
					continue;
			}
			
			if (isFirstLineOfGrammarRule(lines[i]) && !RegexHelper.doesStringExist(lines[i], "import.*http")) {
				if (grammarRule.getLines() != null) {
					if (grammarRule.getLines().size() != 0) {
						// when encounter empty line, means a grammar rule finishes
						grammar.getRules().add(grammarRule);

						// then clear grammar rule object
						grammarRule = new GrammarRule();
					}
				}

				
				storeTempLine(lines[i], grammarRule);
				bStartFromBeginning = true;
				
				continue;
			}
			else if (RegexHelper.doesStringExist(lines[i], "import.*http")) {
				if ((i + 1) < lines.length) {
					if (!RegexHelper.doesStringExist(lines[i + 1], "import")) {
						storeTempLine(lines[i], grammarRule);
						
						// when encounter empty line, means a grammar rule finishes
						grammar.getRules().add(grammarRule);

						// then clear grammar rule object
						grammarRule = new GrammarRule();
						bStartFromBeginning = true;
						
						continue;
					}
				}
				
				if ((i - 1) >= 0) {
					if (RegexHelper.doesStringExist(lines[i - 1], "grammar")) {
						// when encounter empty line, means a grammar rule finishes
						grammar.getRules().add(grammarRule);

						// then clear grammar rule object
						grammarRule = new GrammarRule();
						
						storeTempLine(lines[i], grammarRule);
						bStartFromBeginning = true;
						
						continue;
					}
				}
			}
			
			storeTempLine(lines[i], grammarRule);
			bStartFromBeginning = false;
		}
		
		grammar.getRules().add(grammarRule);
		
		return processFirstLine(grammar);
	}
	
	public static void storeTempLine(String lineContent, GrammarRule grammarRule) {
		LineEntry lineEntry = new LineEntry();

		lineEntry.setLineContent(lineContent);

		String regex = "(\\w*)\\s*\\+*\\?*\\=.*\\w*";
		Pattern pattern = Pattern.compile(regex);
		String attrName = RegexHelper.getTargetString(lineContent, pattern);
		lineEntry.setAttrName(attrName);

		// collect the lines from a same rule
		// grammarRule.lines.add(lineEntry);
		grammarRule.getLines().add(lineEntry);
	}
	
	public static boolean isFirstLineOfGrammarRule(String str) {
		boolean bRet = false;
		
		if (RegexHelper.doesStringExist(str, "\\w+\\s*\\:") && !RegexHelper.doesStringExist(str, "\\w+\\s*\\+*=")) {
			bRet = true;
		}
		
		return bRet;
	}
	
	private static Grammar processFirstLine(Grammar grammar) {
		for (int i = 0; i < grammar.getRules().size(); i++) {
			if (grammar.getRules().get(i).getLines() == null || grammar.getRules().get(i).getLines().isEmpty()) {
				continue;
			}
			String firstLine = grammar.getRules().get(i).getLines().get(0).getLineContent();

			if (RegexHelper.doesStringExist(firstLine, "package") || RegexHelper.doesStringExist(firstLine, "import")
					|| RegexHelper.doesStringExist(firstLine, "//.*") 
					|| RegexHelper.doesStringExist(firstLine, "grammar")) {
				grammar.getRules().get(i).setName(null);

				continue;
			}
			
			if (RegexHelper.doesStringExist(firstLine, "returns")) {

				
				String[] arr = firstLine.split("\\s+");

				if (RegexHelper.doesStringExist(firstLine, "enum")) {
					if (arr.length >= 4) {
						// in an enum rule, the second string is the rule name
						grammar.getRules().get(i).setName(arr[1]);
					} else {
						// just in case if a rule doesn't reference anything
						grammar.getRules().get(i).setName(arr[1]);
					}				
				} else {
					if (arr.length >= 3) {
						// in a common rule, the first string is the rule name
						grammar.getRules().get(i).setName(arr[0]);
					} else {
						// just in case if a rule doesn't reference anything
						grammar.getRules().get(i).setName(arr[0]);
					}
				}
			} 
			else if (RegexHelper.doesStringExist(firstLine, "@Override")) {
				// rule name is in second line
				firstLine = grammar.getRules().get(i).getLines().get(1).getLineContent();
				String[] arr = firstLine.split("\\s+");
				if(RegexHelper.doesStringExist(firstLine, "terminal")) {
					String ruleName = arr[1];
					if(ruleName.endsWith(":")) {
						ruleName = ruleName.substring(0, ruleName.indexOf(":")); 
					} 
					grammar.getRules().get(i).setName(ruleName);
				} else {
					if (RegexHelper.doesStringExist(arr[0], "\\w+\\:")) {
						Pattern pattern = Pattern.compile("(\\w+)\\:.*");
						arr[0] = RegexHelper.getTargetString(arr[0], pattern);
					}
					grammar.getRules().get(i).setName(arr[0]);
				}
			}
			else {			
				String[] arr = firstLine.split("\\s+");
				String ruleName = null;
				if (RegexHelper.doesStringExist(firstLine, "enum") || 
						RegexHelper.doesStringExist(firstLine, "terminal")) {
					if (RegexHelper.doesStringExist(arr[1], "\\w+"))
						ruleName = arr[1];
				}
				else {
					if (RegexHelper.doesStringExist(arr[0], "\\w+\\:*"))
						ruleName = arr[0];
				}
				
				if (ruleName != null) {
					if(ruleName.endsWith(":")) {
						ruleName = ruleName.substring(0, ruleName.indexOf(":")); 
					} 
					grammar.getRules().get(i).setName(ruleName);
				}
			}
		}

		return grammar;
	}
}
