package com.example.parser;

import java.util.ArrayList;

public class TreeNode {

    String name;
    ArrayList<TreeNode> children;


    public TreeNode(String name){
        this.name = name;
        this.children = new ArrayList<TreeNode>();
    }

    @Override
    public String toString() {
        return toString(0);
    }

    private String toString(int level) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < level; i++) {
            sb.append("  "); // dois espaços por nível
        }

        sb.append(name).append("\n");

        for (TreeNode child : children) {
            sb.append(child.toString(level + 1));
        }

        return sb.toString();
    }
}
