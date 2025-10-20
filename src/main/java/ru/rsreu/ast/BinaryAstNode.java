package ru.rsreu.ast;

import ru.rsreu.Token;

import java.util.List;

public class BinaryAstNode extends AstNode {
    private final Token operator;
    private final AstNode left;
    private final AstNode right;

    public BinaryAstNode(Token operator, AstNode left, AstNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public String label() {
        return operator.compactRepresentation();
    }

    @Override
    public List<AstNode> children() {
        return List.of(left, right);
    }
}
