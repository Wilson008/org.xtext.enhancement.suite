package org.xtext.grammar.grammarrule;

import java.util.ArrayList;
import java.util.List;

public class Grammar {
    private List<GrammarRule> rules;  // 规则列表

    // Constructor: 初始化为空的列表
    public Grammar() {
        this.rules = new ArrayList<>();  // 初始化为可变的空列表
    }

    // 获取 GrammarRule 列表
    public List<GrammarRule> getRules() {
        return rules;
    }

    // 添加 GrammarRule 到列表中
    public void addRule(GrammarRule rule) {
        rules.add(rule);
    }

    // 从列表中删除 GrammarRule
    public void removeRule(GrammarRule rule) {
        rules.remove(rule);
    }
}
