package com.example.parser;

import java.util.ArrayList;
import java.util.List;

public class Token extends AbstractSyntaxNode {

    String expression;
    int index;

    public Token(String expression, String type, int index) {
        this.expression = expression;
        this.type = type;
        this.index = index;
    }

    @Override
    public String toString() {
        if ("CRLF".equals(type)) return type + "()" + " i: " + index;
        return type + "(" + expression + ")" + " i: " + index;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public List<AbstractSyntaxNode> getNodeByType(String type) {
        List<AbstractSyntaxNode> result = new ArrayList<>();
        if (this.type.equals(type)) {
            result.add(this);
        }
        return result;
    }
}