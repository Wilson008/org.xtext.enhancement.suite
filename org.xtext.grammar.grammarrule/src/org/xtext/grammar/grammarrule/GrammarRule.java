package org.xtext.grammar.grammarrule;

import java.util.ArrayList;
import java.util.List;

public class GrammarRule {
    private String name;            // 默认为 null
    private List<LineEntry> lines;  // 初始化为空的列表

    // Constructor: 不需要参数，name 和 lines 均有默认值
    public GrammarRule() {
        this.name = null;           // 默认值为 null
        this.lines = new ArrayList<>();  // 初始化为空的列表，确保可以自由添加和删除
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter for lines
    public List<LineEntry> getLines() {
        return lines;
    }

    // Method to add a LineEntry to the list
    public void addLine(LineEntry lineEntry) {
        lines.add(lineEntry);
    }

    // Method to remove a LineEntry from the list
    public void removeLine(LineEntry lineEntry) {
        lines.remove(lineEntry);
    }
}

