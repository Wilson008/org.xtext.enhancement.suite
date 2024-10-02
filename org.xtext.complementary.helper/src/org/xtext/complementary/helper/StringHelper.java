package org.xtext.complementary.helper;

import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

public class StringHelper {
	// Function to remove comments from a string (this string may have multiple lines)
    public static String removeComments(String content) {
    	// Regular expression to match block comments (/* ... */) and line comments (// ...)
        // Block comments: /* ... */
        String blockCommentPattern = "/\\*.*?\\*/";

        // Line comments: // ..., but not inside quotes (ignores 'http://' and similar cases)
        String lineCommentPattern = "(?<!:)//.*?(\\r?\\n|$)"; // (?<!:) is a negative lookbehind for ':'
        
        // This pattern ensures that the '//' after 'http:' is not considered a comment
        // And we'll also handle strings to prevent comments being stripped within quotes
        String stringPattern = "\"(\\\\\"|[^\"])*\""; // Matches anything between " " including escaped quotes
        
        // Combine patterns, giving priority to strings so comments inside them are ignored
        String combinedPattern = stringPattern + "|" + blockCommentPattern + "|" + lineCommentPattern;
        
        // Compile the combined pattern
        Pattern pattern = Pattern.compile(combinedPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        // Use a StringBuffer to store the result while handling each match
        StringBuffer result = new StringBuffer();

        // Iterate through matches and preserve strings while removing comments
        while (matcher.find()) {
            if (matcher.group().startsWith("\"")) {
                // If the match is a string (starts with "), keep it as is
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
            } else {
                // Otherwise, it's a comment, so we replace it with an empty string
                matcher.appendReplacement(result, "");
            }
        }
        matcher.appendTail(result); // Append the rest of the content

        return result.toString();
    }
}
