package ru.rsreu.ast;

import ru.rsreu.Token;

import java.util.Collections;
import java.util.List;

public class OperandNode extends AstNode {
    private final Token token;

    public OperandNode(Token token) {
        this.token = token;
    }

    @Override
    public String label() {
        return token.compactRepresentation();
    }

    @Override
    public List<AstNode> children() {
        return Collections.emptyList();
    }
}
