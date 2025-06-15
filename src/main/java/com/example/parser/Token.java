package com.example.parser;

import java.util.ArrayList;
import java.util.List;

public class Token extends Node {

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
    public List<Node> getNodeByType(String type) {
        List<Node> result = new ArrayList<>();
        if (this.type.equals(type)) {
            result.add(this);
        }
        return result;
    }
}