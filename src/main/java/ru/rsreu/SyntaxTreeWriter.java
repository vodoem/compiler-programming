package ru.rsreu;

import ru.rsreu.ast.AstNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SyntaxTreeWriter {

    public void write(Path output, AstNode root) throws IOException {
        StringBuilder sb = new StringBuilder();
        build(sb, root, true, "");
        Files.writeString(output, sb.toString());
    }

    private void build(StringBuilder sb, AstNode node, boolean isRoot, String indent) {
        if (isRoot) {
            sb.append(node.label()).append(System.lineSeparator());
        } else {
            sb.append(indent).append("|---").append(node.label()).append(System.lineSeparator());
        }

        String childIndent = isRoot ? "" : indent + "    ";
        for (AstNode child : node.children()) {
            build(sb, child, false, childIndent);
        }
    }
}
