package ru.rsreu;

import ru.rsreu.ast.AstNode;
import ru.rsreu.ast.BinaryAstNode;
import ru.rsreu.ast.ConversionNode;
import ru.rsreu.ast.OperandNode;

public class SemanticAnalyzer {

    public AstNode analyze(AstNode root) throws SemanticException {
        if (root == null) {
            throw new SemanticException("Семантическая ошибка: отсутствует синтаксическое дерево для анализа");
        }
        return transform(root).node();
    }

    private SemanticResult transform(AstNode node) throws SemanticException {
        if (node instanceof OperandNode operandNode) {
            return new SemanticResult(operandNode, determineOperandType(operandNode));
        }

        if (node instanceof BinaryAstNode binaryNode) {
            AstNode leftOriginal = binaryNode.left();
            AstNode rightOriginal = binaryNode.right();

            if (binaryNode.operator().type() == TokenType.DIVIDE && isZeroConstant(rightOriginal)) {
                throw new SemanticException("Семантическая ошибка: обнаружено деление на константу 0");
            }

            SemanticResult left = transform(leftOriginal);
            SemanticResult right = transform(rightOriginal);

            AstNode newLeft = left.node();
            AstNode newRight = right.node();

            boolean leftNeedsConversion = left.type() == VariableType.INTEGER && right.type() == VariableType.REAL;
            boolean rightNeedsConversion = right.type() == VariableType.INTEGER && left.type() == VariableType.REAL;

            if (leftNeedsConversion) {
                newLeft = new ConversionNode(newLeft);
                left = new SemanticResult(newLeft, VariableType.REAL);
            }
            if (rightNeedsConversion) {
                newRight = new ConversionNode(newRight);
                right = new SemanticResult(newRight, VariableType.REAL);
            }

            VariableType resultType = left.type() == VariableType.REAL || right.type() == VariableType.REAL
                    ? VariableType.REAL
                    : VariableType.INTEGER;

            return new SemanticResult(new BinaryAstNode(binaryNode.operator(), newLeft, newRight), resultType);
        }

        if (node instanceof ConversionNode conversionNode) {
            SemanticResult inner = transform(conversionNode.child());
            return new SemanticResult(new ConversionNode(inner.node()), VariableType.REAL);
        }

        throw new SemanticException("Семантическая ошибка: неизвестный тип узла дерева");
    }

    private boolean isZeroConstant(AstNode node) {
        if (node instanceof OperandNode operandNode) {
            Token token = operandNode.token();
            if (token.type() == TokenType.INTEGER_CONST) {
                Integer value = (Integer) token.lexeme();
                return value == 0;
            }
            if (token.type() == TokenType.REAL_CONST) {
                Double value = (Double) token.lexeme();
                return Double.compare(value, 0.0) == 0;
            }
        }
        return false;
    }

    private VariableType determineOperandType(OperandNode operandNode) throws SemanticException {
        Token token = operandNode.token();
        return switch (token.type()) {
            case IDENTIFIER -> ((Identifier) token.lexeme()).type();
            case INTEGER_CONST -> VariableType.INTEGER;
            case REAL_CONST -> VariableType.REAL;
            default -> throw new SemanticException("Семантическая ошибка: неизвестный тип операнда");
        };
    }

    private record SemanticResult(AstNode node, VariableType type) {
    }
}
