package com.example.parser;

import java.util.ArrayList;
import java.util.List;

public class TreeNode extends AbstractSyntaxNode {

    ArrayList<AbstractSyntaxNode> children;

    public TreeNode(String type) {
        this.type = type;
        this.children = new ArrayList<>();
    }

    @Override
    public String getExpression() {
        return children.stream()
                .map(AbstractSyntaxNode::getExpression)
                .reduce("", String::concat);
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int level) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(level)).append(type).append("\n");

        for (AbstractSyntaxNode child : children) {
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
    public List<AbstractSyntaxNode> getNodeByType(String type) {
        List<AbstractSyntaxNode> result = new ArrayList<>();

        if (this.type.equals(type)) {
            result.add(this);
        }

        for (AbstractSyntaxNode child : children) {
            result.addAll(child.getNodeByType(type));
        }

        return result;
    }
}
