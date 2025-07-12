package com.example.parser;

import java.util.List;

public abstract class AbstractSyntaxNode {

    public String type;

    abstract public String getExpression();

    abstract public List<AbstractSyntaxNode> getNodeByType(String type);
}