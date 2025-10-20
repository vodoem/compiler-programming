package ru.rsreu.ast;

import java.util.List;

public abstract class AstNode {
    public abstract String label();

    public abstract List<AstNode> children();
}
