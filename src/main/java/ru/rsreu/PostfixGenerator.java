package ru.rsreu;

import ru.rsreu.ast.AstNode;
import ru.rsreu.ast.BinaryAstNode;
import ru.rsreu.ast.ConversionNode;
import ru.rsreu.ast.OperandNode;

import java.util.ArrayList;
import java.util.List;

public class PostfixGenerator {

    public List<String> generate(AstNode root) throws SemanticException {
        List<String> output = new ArrayList<>();
        traverse(root, output);
        return output;
    }

    private void traverse(AstNode node, List<String> output) throws SemanticException {
        if (node instanceof OperandNode operandNode) {
            output.add(operandNode.token().compactRepresentation());
            return;
        }

        if (node instanceof ConversionNode conversionNode) {
            traverse(conversionNode.child(), output);
            output.add("<i2f>");
            return;
        }

        if (node instanceof BinaryAstNode binaryNode) {
            traverse(binaryNode.left(), output);
            traverse(binaryNode.right(), output);
            output.add(binaryNode.operator().compactRepresentation());
            return;
        }

        throw new SemanticException("Семантическая ошибка: неизвестный тип узла дерева");
    }
}
