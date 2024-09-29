package org.xtext.complementary.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xtext.grammar.grammarrule.*;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class GrammarHelper {
	public static List<String> getAllGrammarRuleNames(String strRaw) {
		List<String> uniqueTypeNames = new ArrayList<>();
		
		Grammar grammar = new Grammar();
		processManualGrammar(grammar, strRaw);
		
		if (grammar.getRules().size() > 0) {
//			System.out.println("Succesfully get all grammar rules!");
//			System.out.printf("Found rules: %d\n", grammar.getRules().size());
			for (int i = 0; i < grammar.getRules().size(); i++) {
//				System.out.println(grammar.getRules().get(i).getName());
				if (grammar.getRules().get(i).getName() != null && !grammar.getRules().get(i).getName().isEmpty()) {
					
					uniqueTypeNames.add(grammar.getRules().get(i).getName());
				}
//				else {
//					System.out.println("No name rule");
//				}
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
	
	public static void resetAllLines(String lines[]) {
		boolean justEndaRule = false;
		
		for (int i = 0; i < lines.length; i++) {
//			String updatedRule = rule.replaceAll("(^\\w+\\s*:\\s*'[^']+'\\s*)(\\w+=)", "$1\n$2");
//			lines[i].replaceAll("(\\w+\\s*:\\s*('\\w+')*)\\s(\\w+\\+*=\\w+)", "$1\n$2");
			if (RegexHelper.doesStringExist(lines[i], "(\\w+\\s*:\\s*)(('\\w+')?\\s+\\w+?=\\w+.*)")) {
				lines[i] = splitAndReformat(lines[i]);
			}
			
		}
	}
	
	// 函数：拆分并重组字符串，使用正则表达式匹配规则名和后续部分
    public static String splitAndReformat(String line) {
        // 定义正则表达式
        // \\w+: 匹配规则名 (以字母开头)
        // ('\\w+')?: 匹配可能存在的关键字，单引号中的关键字，可能不存在
        // [\\+\\w]+=\\w+: 匹配类似 name=ID 的格式，考虑 + 号的情况
        String regex = "(\\w+\\s*:\\s*)(('\\w+')?\\s+\\w+?=\\w+.*)";
//    	String regex = "(\\w+\\s*:\\s*)(({\\w+})?\\s*('\\w+')?\\s+\\w+?=\\w+.*)";
        
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);

        String substring1 = "";
        String substring2 = "";

        // 如果正则匹配成功，提取两部分
        if (matcher.find()) {
            substring1 = matcher.group(1).trim();  // 规则名和冒号
            substring2 = matcher.group(2).trim();  // 后续部分，包括可能存在的关键字和 name=ID
        }

        // 拼接，插入换行符
        String substring3 = substring1 + "\n" + substring2;
        return substring3;
    }
	
	public static Grammar processManualGrammar(Grammar grammar, String strRaw) {
		// Split the contents of Xtext file (i.e. string) into lines
		String lines[] = strRaw.split("\r\n|\r|\n");
		
		resetAllLines(lines);

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
		
		if (RegexHelper.doesStringExist(str, "\\w+\\s*\\:") && 
				(!RegexHelper.doesStringExist(str, "\\w+\\s*\\+*=") || 
				(RegexHelper.doesStringExist(str, "\\w+\\s*\\+*=") && RegexHelper.doesStringExist(str, "\\n")))) {
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
	
	public static boolean isContainerNormal(GrammarRule rule) {
		boolean bRet = true;
		
		// find '{' and '}'
		boolean bOpeningExist = false;
		boolean bClosingExist = false;
		
		for (int i = 0; i < rule.getLines().size(); i++) {
			String lineContent = rule.getLines().get(i).getLineContent();
			if (lineContent.strip().equals("'{'")) {
				bOpeningExist = true;
			}
			
			if (lineContent.strip().equals("'}'") || lineContent.strip().equals("'}';")) {
				bClosingExist = true;
			}
		}
		
		if ((bOpeningExist && bClosingExist) || (!bOpeningExist && !bClosingExist)) {
			bRet = true;
		}
		else {
			return false;
		}
		
		// find ('{' and '}')?
		bOpeningExist = false;
		bClosingExist = false;
		
		for (int i = 0; i < rule.getLines().size(); i++) {
			String lineContent = rule.getLines().get(i).getLineContent();
			if (lineContent.strip().equals("('{'")) {
				bOpeningExist = true;
			}
			
			if (lineContent.strip().equals("'}')?") || lineContent.strip().equals("'}')?;")) {
				bClosingExist = true;
			}
		}
		
		if ((bOpeningExist && bClosingExist) || (!bOpeningExist && !bClosingExist)) {
			bRet = true;
		}
		else {
			return false;
		}
		
		// find '(' and ')'
		bOpeningExist = false;
		bClosingExist = false;
		
		for (int i = 0; i < rule.getLines().size(); i++) {
			String lineContent = rule.getLines().get(i).getLineContent();
			if (lineContent.strip().equals("'('")) {
				bOpeningExist = true;
			}
			
			if (lineContent.strip().equals("')'") || lineContent.strip().equals("')';")) {
				bClosingExist = true;
			}
		}
		
		if ((bOpeningExist && bClosingExist) || (!bOpeningExist && !bClosingExist)) {
			bRet = true;
		}
		else {
			return false;
		}
		
		// find '[' and ']'
		bOpeningExist = false;
		bClosingExist = false;
		
		for (int i = 0; i < rule.getLines().size(); i++) {
			String lineContent = rule.getLines().get(i).getLineContent();
			if (lineContent.strip().equals("'['")) {
				bOpeningExist = true;
			}
			
			if (lineContent.strip().equals("']'") || lineContent.strip().equals("']';")) {
				bClosingExist = true;
			}
		}
		
		if ((bOpeningExist && bClosingExist) || (!bOpeningExist && !bClosingExist)) {
			bRet = true;
		}
		else {
			return false;
		}
		
		return bRet;
	}
	
	public static boolean doesItNeedNormalize(GrammarRule rule) {
		boolean bRet = false;
		
		for (LineEntry line: rule.getLines()) {
			if (RegexHelper.doesStringExist(line.getLineContent(), "\\w+\\+*=")) {
				List<String> attrNames = new ArrayList<String>();
				Pattern pattern = Pattern.compile("(\\w+)\\+*=");
				Matcher matcher = pattern.matcher(line.getLineContent());
				
				while (matcher.find()) {
					attrNames.add(matcher.group());
				}
				
				for (int i = 0; i < attrNames.size(); i++) {
					for (int j = 0; j < attrNames.size(); j++) {
						if (i != j && attrNames.get(i).equals(attrNames.get(j))) {
							attrNames.remove(attrNames.get(j));
						}
					}
				}
				
				if (attrNames.size() > 1) {
					bRet = true;
					break;
				}
			}
			
			if (RegexHelper.doesStringExist(line.getLineContent(), "\\{\\w+\\}\\s\\w+") || 
					RegexHelper.doesStringExist(line.getLineContent(), "\\{\\w+\\}\\s\\W+")) {
				bRet = true;
				break;
			}
			
			if (RegexHelper.doesStringExist(line.getLineContent(), "'\\W+'") && !RegexHelper.doesStringExist(line.getLineContent(), "\\w+\\s*\\+*=")) {
				String[] subLines = line.getLineContent().strip().split("\\s");
				
				if (subLines.length > 1)
					bRet = true;
				else if (subLines.length < 1)
					bRet = false;
				else {
					if (isBracket(subLines[0]))
						bRet = false;
					else
						bRet = true;
				}
			}
		}
		
		return bRet;
	}
	
	public static boolean isBracket(String line) {
		boolean bRet = false;
		
		line = line.strip();

		if (line.equals("'{'") || line.equals("'}'") || line.equals("'}';") || 
				line.equals("('{'") || line.equals("'}')?") || line.equals("'}')?;") || 
				line.equals("'('") || line.equals("')'") || line.equals("')';") || 
				line.equals("'['") || line.equals("']'") || line.equals("']';")) {
			bRet = true;
		}		
		
		return bRet;
	}
	
	/**
	 * This method is for combining all the lines except first line to a same one line
	 * */
	public void resetGrammarRule(GrammarRule input) {
		if (input == null || input.getName() == null)
			return;
		
		if (input.getLines() == null || input.getLines().size() == 1)
			return;
		
		if (!RegexHelper.doesStringExist(input.getLines().get(0).getLineContent(), input.getName()))
			return;
		
		List<LineEntry> preList = new ArrayList<LineEntry>();
		preList.add(input.getLines().get(0));
		String combinedLineStr = "";
		
		for (int m = 1; m < input.getLines().size(); m++) {
			combinedLineStr += input.getLines().get(m).getLineContent().strip();
			
			if (m != input.getLines().size() - 1) {
				combinedLineStr += " ";
			}
		}
		LineEntry lineEntry = new LineEntry();
		lineEntry.setLineContent(combinedLineStr);
		lineEntry.setAttrName(null);
		preList.add(lineEntry);
		
		if (preList.size() > 0) {
			input.getLines().clear();
			input.getLines().addAll(preList);
		}
	}
	
	public GrammarRule normalizeGrammarRule(GrammarRule input) {
		GrammarRule output = null;
		boolean doesItNeedNormalize = false;
		
		if (input.getName().equals("WildcardParameterExpression"))
			System.out.println("WildcardParameterExpression");
		
		if (isContainerNormal(input)) {
			if (doesItNeedNormalize(input)) {
				doesItNeedNormalize = true;
			}
		}
		else {
			doesItNeedNormalize = true;
		}
		
		if (doesItNeedNormalize) {
			resetGrammarRule(input);
			
			System.out.printf("Start normalizing grammar rule %s\n", input.getName());
			
			List<LineEntry> tempList = new ArrayList<LineEntry>();
			for (int i = 0; i < input.getLines().size(); i++) {	
				String stripLine = input.getLines().get(i).getLineContent().strip();
				String[] lines = stripLine.split("\\s");

				// with {Action} and something
				if (RegexHelper.doesStringExist(input.getLines().get(i).getLineContent(), "\\{\\w+\\}")) {
					if (lines.length == 1 && RegexHelper.doesStringExist(lines[0], "\\{\\w+\\}")) {
						if (RegexHelper.doesStringExist(lines[0], "\\{\\w+\\}\\.\\w+")) {
							// there should be changes happened, while this can not be handled by current GrammarOptimizer
							// so we default to not processing it for the time being
							tempList.add(input.getLines().get(i));
						}
						else {
							// with {Action} only, no need to do anything in this case
							tempList.add(input.getLines().get(i));
						}
					}
					// there is something else after an {Action}, divide it
					else {
						divideLine(i, lines, tempList);
					}
				}
				else {
					// without {Action}, but with multiple/single attribute
					if (RegexHelper.doesStringExist(input.getLines().get(i).getLineContent(), "\\w+\\+*=")) {
						divideLine(i, lines, tempList);
					}
					else {
						// without {Action} and attribute, no need to do anything in this case
						tempList.add(input.getLines().get(i));
					}
				}
			}
			
			if (tempList.size() > 0) {
				input.getLines().clear();
				input.getLines().addAll(tempList);
				output = input;
			}
			
			System.out.printf("Complete normalizing grammar rule %s\n", input.getName());
		}
		else {
			output = input;
		}
		
		return output;
	}
	
	/**
	 * In an hand-craft grammar, it's possible to see that different attributes are
	 * put in the same line or something put after an {Action}. This creates a barrier
	 * for grammar comparison, as the comparison is based on line-by-line mapping. So
	 * in this method, we divide such lines.
	 * */
	public void divideLine(int index, String[] subStrings, List<LineEntry> dividedLines) {
		String newLine = "";
		boolean bAlreadyAttr = false;
		String attrName = null;
		
		for (int i = 0; i < subStrings.length; i++) {
			newLine += subStrings[i] + " ";
			
			// encounter an {Action}, start a new line
			if (RegexHelper.doesStringExist(subStrings[i], "\\{\\w+\\}")) {
				LineEntry lineEntry = new LineEntry();
				lineEntry.setLineContent(newLine);
				lineEntry.setAttrName(null);
				dividedLines.add(lineEntry);
				newLine = "";
				attrName = null;
				bAlreadyAttr = false;
				continue;
			}
			else if (RegexHelper.doesStringExist(subStrings[i], "\\w+\\+*=")) {
				bAlreadyAttr = true;
				Pattern pattern = Pattern.compile("(\\w+)\\+*=");
				attrName = RegexHelper.getTargetString(subStrings[i], pattern);
				
				// when encounter an attribute, check the next line whether it's also an attribute
				if ((i + 1) < subStrings.length) {
					if (RegexHelper.doesStringExist(subStrings[i + 1], "\\w+\\+*=")) {
						Pattern pattern1 = Pattern.compile("(\\w+)\\+*=");
						String nextAttrName = RegexHelper.getTargetString(subStrings[i + 1], pattern1);
						
						// if the attribute in next line is different, then start a new line
						if (!attrName.equals(nextAttrName)) {
							LineEntry lineEntry = new LineEntry();
							lineEntry.setLineContent(newLine);
							lineEntry.setAttrName(attrName);
							dividedLines.add(lineEntry);
							bAlreadyAttr = false;
							newLine = "";
							attrName = null;
							continue;
						}

					}
					else if (RegexHelper.doesStringExist(subStrings[i + 1], "'(\\(|\\[|\\{)'")) {
						LineEntry lineEntry = new LineEntry();
						lineEntry.setLineContent(newLine);
						lineEntry.setAttrName(attrName);
						dividedLines.add(lineEntry);
						bAlreadyAttr = false;
						newLine = "";
						attrName = null;
						continue;
					}
					else {
						continue;
					}
				}
			}
			else {
				if (bAlreadyAttr) {
					if ((i + 1) < subStrings.length) {
						if (RegexHelper.doesStringExist(subStrings[i + 1], "\\w+\\+*=")) {
							Pattern pattern1 = Pattern.compile("(\\w+)\\+*=");
							String nextAttrName = RegexHelper.getTargetString(subStrings[i + 1], pattern1);
							
							if (!attrName.equals(nextAttrName)) {
								LineEntry lineEntry = new LineEntry();
								lineEntry.setLineContent(newLine);
								lineEntry.setAttrName(attrName);
								dividedLines.add(lineEntry);
								bAlreadyAttr = false;
								newLine = "";
								attrName = null;
								continue;
							}
						}
						else if (RegexHelper.doesStringExist(subStrings[i + 1], "'(\\(|\\[|\\{)'")) {
							LineEntry lineEntry = new LineEntry();
							lineEntry.setLineContent(newLine);
							lineEntry.setAttrName(attrName);
							dividedLines.add(lineEntry);
							bAlreadyAttr = false;
							newLine = "";
							attrName = null;
							continue;
						}
						else {
							continue;
						}
					}
				}
				else 
					continue;
			}
		}
		
		// In the above code, when face the last line, we didn't do anything
		// so here, this is the code for end the last line
		if (!newLine.equals("")) {
			LineEntry lineEntry = new LineEntry();
			lineEntry.setLineContent(newLine);
			if (RegexHelper.doesStringExist(newLine, "\\w+\\+*=")) {
				Pattern pattern = Pattern.compile("(\\w+)\\+*=");
				attrName = RegexHelper.getTargetString(newLine, pattern);
				lineEntry.setAttrName(attrName);
				attrName = null;
			}
			dividedLines.add(lineEntry);
		}
	}
}
