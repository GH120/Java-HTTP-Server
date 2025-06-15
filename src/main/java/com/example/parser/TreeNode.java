package com.example.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

abstract class Node {

    public String type;

    abstract public String getExpression();

    abstract public List<Node> getNodeByType(String type);
}

public class TreeNode extends Node {

    ArrayList<Node> children;

    public TreeNode(String type) {
        this.type = type;
        this.children = new ArrayList<>();
    }

    @Override
    public String getExpression() {
        return children.stream()
                .map(Node::getExpression)
                .reduce("", String::concat);
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int level) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(level)).append(type).append("\n");

        for (Node child : children) {
            if (child instanceof Token) {
                sb.append("  ".repeat(level + 2))
                  .append(child.toString())
                  .append("\n");
            } else if (child instanceof TreeNode) {
                sb.append(((TreeNode) child).toString(level + 2));
            }
        }

        return sb.toString();
    }

    @Override
    public List<Node> getNodeByType(String type) {
        List<Node> result = new ArrayList<>();

        if (this.type.equals(type)) {
            result.add(this);
        }

        for (Node child : children) {
            result.addAll(child.getNodeByType(type));
        }

        return result;
    }
}
