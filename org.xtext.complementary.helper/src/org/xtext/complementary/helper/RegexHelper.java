package org.xtext.complementary.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import org.xtext.grammar.grammarrule.*;

public class RegexHelper {
	/**
	 * Get the second matching group of the regex in a specific string
	 */
	public static String getTargetString(String strOrigin, Pattern pattern) {
		if (null == strOrigin || null == pattern)
			return null;

		Matcher matcher = pattern.matcher(strOrigin);

		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}
	
	public static List<String> getTargetStringArray(String strOrigin, Pattern pattern) {
		if (null == strOrigin || null == pattern)
			return null;

		Matcher matcher = pattern.matcher(strOrigin);
		List<String> output= new ArrayList<String>();
		
		while (matcher.find()) {
			output.add(matcher.group(1));
		}
		
		return output;
	}
	
	public static boolean doesStringExist(String strInput, String strRegex) {
		boolean isExist = false;

		Pattern pattern = Pattern.compile(strRegex);
		Matcher matcher = pattern.matcher(strInput);

		if (matcher.find()) {
			isExist = true;
		}

		return isExist;
	}
	
	public static String getKeywordFromAttr(String input) {
		String output = null;
		
		Pattern pattern = Pattern.compile("\'(\\w*)\'");
		Matcher matcher = pattern.matcher(input);
		
		if (matcher.find()) {
			output = matcher.group(1);
		}
		
		return output;
	}
	
	public static boolean hasContainerBrackets(GrammarRule rule) {
		boolean hasOpenBracket = false;
		boolean hasCloseBracket = false;
		
		for (LineEntry lineEntry : rule.getLines()) {
			if (doesStringExist(lineEntry.getLineContent(), "\'\\{\'") && !doesStringExist(lineEntry.getLineContent(), "\\w"))
				hasOpenBracket = true;
			if (doesStringExist(lineEntry.getLineContent(), "\'\\}\'") && !doesStringExist(lineEntry.getLineContent(), "\\w"))
				hasCloseBracket = true;
		}
		
		if (hasOpenBracket && hasCloseBracket) 
			return true;
		else
			return false;
	}
	
	public static boolean areAttributesInOrRelationship(GrammarRule rule) {
		boolean bRet = false;
		int bFirstAttrIndex = -1;
		int bLastAttrIndex = -1;
		
		for (int i = 0; i < rule.getLines().size(); i++) {
			if (RegexHelper.doesStringExist(rule.getLines().get(i).getLineContent(), "\\w+\\+*=")) {
				bFirstAttrIndex = i;
				break;
			}
		}
		
		for (int j = rule.getLines().size() - 1; j > 0; j--) {
			if (RegexHelper.doesStringExist(rule.getLines().get(j).getLineContent(), "\\w+\\+*=")) {
				bLastAttrIndex = j;
				break;
			}
		}
		
		
		
		if (bFirstAttrIndex >= 0 && bLastAttrIndex >= 0) {
			boolean bAllHaveOrSymbol = true;
			
			for (int k = bFirstAttrIndex; k < bLastAttrIndex; k++) {
				if (!RegexHelper.doesStringExist(rule.getLines().get(k).getLineContent(), "\\w+\\+*=.*\\|")) {
					bAllHaveOrSymbol = false;
					break;
				}
			}
			
			if (bAllHaveOrSymbol) {
				if (RegexHelper.doesStringExist(rule.getLines().get(bLastAttrIndex).getLineContent(), "\\w+\\+*=") && 
						!RegexHelper.doesStringExist(rule.getLines().get(bLastAttrIndex).getLineContent(), "\\w+\\+*=.*\\|")) {
					bRet = true;
				}
				else
					bRet = false;
			}
			else {
				bRet = false;
			}
		}
		
		return bRet;
	}
	
	public static boolean areAttributesOptional(GrammarRule rule) {
		boolean bReturn = false;
		int count = 0;
		
		if (hasContainerBrackets(rule)) {
			boolean isInContainer = false;
			
			for (LineEntry lineEntry : rule.getLines()) {
				if (doesStringExist(lineEntry.getLineContent(), "\'\\{\'") && !doesStringExist(lineEntry.getLineContent(), "\\w"))
					isInContainer = true;
				if (doesStringExist(lineEntry.getLineContent(), "\'\\}\'") && !doesStringExist(lineEntry.getLineContent(), "\\w"))
					isInContainer = false;
				
				if (!isInContainer)
					continue;
				
				// no "=" or no "+=" means it is not an attribute
				if (!doesStringExist(lineEntry.getLineContent(), "="))
					continue;
				
				// if exists attribute having no "?", means it is a mandatory attribute
				if (!doesStringExist(lineEntry.getLineContent(), "\\?")) {
					bReturn = false;
					break;
				}
				
				bReturn = true;
				count++;
			}
			
			// At the moment, this method are only used to support change all the optional attributes
			// into be OR relationship with each other, so when there is only one optional attribute,
			// it's not necessary to take any action.
//			if (count ==1)
//				bReturn = false;
		}
		else {
			bReturn = true;
			
			for (LineEntry lineEntry : rule.getLines()) {
				if (doesStringExist(lineEntry.getLineContent(), "\\=") && !doesStringExist(lineEntry.getLineContent(), "\\?")) {
					bReturn = false;
					break;
				}
			}
		}
				
		return bReturn;
	}
	
	public static void addAndSymbolBetweenAttrs(GrammarRule rule) {
		int lineCount = rule.getLines().size();
		
		if (lineCount <= 1)
			return;
		
		for (int i = 0; i < lineCount - 1; i++) {
			if (doesStringExist(rule.getLines().get(i).getLineContent(), "\\)\\?") && doesStringExist(rule.getLines().get(i).getLineContent(), "\\=") &&
					doesStringExist(rule.getLines().get(i+1).getLineContent(), "\\)\\?") && doesStringExist(rule.getLines().get(i+1).getLineContent(), "\\=")) {
				String lineContent = rule.getLines().get(i).getLineContent();
				lineContent += " " + "&";
				rule.getLines().get(i).setLineContent(lineContent);
			}
		}
	}
	
	public static void addOperatorSymbolBetweenAttrs(GrammarRule rule, String symbol) {
		if (hasContainerBrackets(rule)) {
			boolean isInContainer = false;
			
			for (int i = 0; i < rule.getLines().size(); i++) {
				if (doesStringExist(rule.getLines().get(i).getLineContent(), "\'\\{\'") && !doesStringExist(rule.getLines().get(i).getLineContent(), "\\w"))
					isInContainer = true;
				
				if (!isInContainer)
					continue;
				
				if (!doesStringExist(rule.getLines().get(i).getLineContent(), "="))
					continue;
				
				String nextLineContent = rule.getLines().get(i+1).getLineContent();
				
				if (doesStringExist(nextLineContent, "\'\\}\'") && !doesStringExist(nextLineContent, "\\w")) {
					break;
				}
				
				String lineContent = rule.getLines().get(i).getLineContent();
				lineContent += " " + symbol;
				rule.getLines().get(i).setLineContent(lineContent);
			}
		}
		else {
			int count = 0;
			
			for (int i = rule.getLines().size() - 1; i > 0; i--) {
				if (!doesStringExist(rule.getLines().get(i).getLineContent(), "="))
					continue;
				
				// if we add the "|" at the end of an attribute
				// we don't need add a "|" to the last attribute
				if (count++ == 0)
					continue;
				
				String lineContent = rule.getLines().get(i).getLineContent();
				lineContent += " |";
				rule.getLines().get(i).setLineContent(lineContent);
			}
		}
	}
	
	public static boolean doesAttributeExist(GrammarRule rule, String attr) {
		for (LineEntry lineEntry : rule.getLines()) {
			String lineContent = lineEntry.getLineContent();
			
			if (lineContent == null || lineContent.isEmpty())
				continue;
			
			if (doesStringExist(lineContent, attr))
				return true;
		}
		return false;
	}
	
	public static boolean doesActionExist(GrammarRule rule) {
		if (rule == null)
			return false;
		if (rule.getLines().size() < 3)
			return false;
		
		String ruleName = rule.getName();
		String regexAction = "\\{" + ruleName + "\\}";
		
		if (doesStringExist(rule.getLines().get(1).getLineContent(), regexAction)) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isSingleNonLetter(String str) {
        String regex = "^[^a-zA-Z]$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
