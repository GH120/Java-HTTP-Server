package com.example.parser;

import java.util.ArrayList;

interface Node {

}

class Token implements Node{

    String string;
    String type;
    int index;

    public Token(String string, String type, int index) {
        this.string = string;
        this.type = type;
        this.index = index;
    }

    @Override
    public String toString() {
        return "Token(" + string + ", " + type + ", " + index + ")";
    }
}


class TreeNode implements Node{

    String name;
    ArrayList<Node> children;


    public TreeNode(String name){
        this.name = name;
        this.children = new ArrayList<Node>();
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int level) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < level; i++) {
            sb.append("  "); // dois espaços por nível
        }

        sb.append(name).append("\n");


        for (Node child : children) {

            if(child instanceof Token) continue;

            sb.append(((TreeNode)child).toString(level + 1));
        }

        return sb.toString();
    }
}
