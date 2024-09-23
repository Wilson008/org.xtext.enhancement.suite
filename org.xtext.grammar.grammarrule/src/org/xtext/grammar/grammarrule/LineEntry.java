package org.xtext.grammar.grammarrule;

public class LineEntry {
    private String lineContent;
    private String attrName;

    // 无参构造函数
    public LineEntry() {
        // 初始化为默认值，如果需要的话
        this.lineContent = null;
        this.attrName = null;
    }
    
    // Constructor
    public LineEntry(String lineContent, String attrName) {
        this.lineContent = lineContent;
        this.attrName = attrName;
    }

    // Getter and Setter for lineContent
    public String getLineContent() {
        return lineContent;
    }

    public void setLineContent(String lineContent) {
        this.lineContent = lineContent;
    }

    // Getter and Setter for attrName
    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }
}
