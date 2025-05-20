package com.example.parser;

import java.util.ArrayList;

interface Node {

    public String getExpression();
}

class Token implements Node {

    String expression;
    String type;
    int index;

    public Token(String expression, String type, int index) {
        this.expression = expression;
        this.type = type;
        this.index = index;
    }

    @Override
    public String toString() {

        if(type == "CRLF") return type + "()" + " i: " + index;

        return type + "(" + expression + ")" + " i: " + index;
    }

    public String getExpression(){
        return expression;
    }
}

class TreeNode implements Node {

    String type;
    ArrayList<Node> children;

    public TreeNode(String type) {
        this.type = type;
        this.children = new ArrayList<Node>();
    }

    public String getExpression(){
        return children.stream().map(c -> c.getExpression()).reduce("", String::concat);
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int level) {
        StringBuilder sb = new StringBuilder();

        // Indentação do nó atual
        sb.append("  ".repeat(level)).append(type).append("\n");

        for (Node child : children) {
            if (child instanceof Token) {
                // Indenta tokens
                sb.append("  ".repeat(level + 1))
                  .append(child.toString())
                  .append("\n");
            } else if (child instanceof TreeNode) {
                // Recursão para subnós
                sb.append(((TreeNode) child).toString(level + 1));
            }
        }

        return sb.toString();
    }
}
