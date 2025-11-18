package ru.rsreu.ast;

import java.util.List;

public class ConversionNode extends AstNode {
    private final AstNode child;

    public ConversionNode(AstNode child) {
        this.child = child;
    }

    public AstNode child() {
        return child;
    }

    @Override
    public String label() {
        return "Int2Float";
    }

    @Override
    public List<AstNode> children() {
        return List.of(child);
    }
}
