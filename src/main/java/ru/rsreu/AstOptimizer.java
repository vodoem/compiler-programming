package ru.rsreu;

import ru.rsreu.ast.AstNode;
import ru.rsreu.ast.BinaryAstNode;
import ru.rsreu.ast.ConversionNode;
import ru.rsreu.ast.OperandNode;

public class AstOptimizer {

    public AstNode optimize(AstNode root) throws SemanticException {
        if (root == null) {
            throw new SemanticException("Семантическая ошибка: отсутствует синтаксическое дерево для оптимизации");
        }
        return optimizeNode(root).node();
    }

    private OptimizationResult optimizeNode(AstNode node) throws SemanticException {
        if (node instanceof OperandNode operandNode) {
            return optimizeOperand(operandNode);
        }

        if (node instanceof ConversionNode conversionNode) {
            OptimizationResult child = optimizeNode(conversionNode.child());

            if (child.type() == VariableType.REAL) {
                return child;
            }

            if (child.isIntegerConstant()) {
                double value = ((Integer) child.constant()).doubleValue();
                return new OptimizationResult(createConstantNode(TokenType.REAL_CONST, value), VariableType.REAL, value);
            }

            if (child.isConstant()) {
                double value = toDouble(child.constant());
                return new OptimizationResult(createConstantNode(TokenType.REAL_CONST, value), VariableType.REAL, value);
            }

            return new OptimizationResult(new ConversionNode(child.node()), VariableType.REAL, null);
        }

        if (node instanceof BinaryAstNode binaryNode) {
            OptimizationResult left = optimizeNode(binaryNode.left());
            OptimizationResult right = optimizeNode(binaryNode.right());

            VariableType resultType = left.type() == VariableType.REAL || right.type() == VariableType.REAL
                    ? VariableType.REAL
                    : VariableType.INTEGER;

            if (left.isConstant() && right.isConstant()) {
                return foldConstants(binaryNode, left, right, resultType);
            }

            OptimizationResult simplified = simplifyBinary(binaryNode, left, right, resultType);
            if (simplified != null) {
                return simplified;
            }

            return new OptimizationResult(new BinaryAstNode(binaryNode.operator(), left.node(), right.node()), resultType, null);
        }

        throw new SemanticException("Семантическая ошибка: неизвестный тип узла дерева для оптимизации");
    }

    private OptimizationResult optimizeOperand(OperandNode operandNode) throws SemanticException {
        Token token = operandNode.token();
        return switch (token.type()) {
            case IDENTIFIER -> new OptimizationResult(operandNode, ((Identifier) token.lexeme()).type(), null);
            case INTEGER_CONST -> new OptimizationResult(operandNode, VariableType.INTEGER, token.lexeme());
            case REAL_CONST -> new OptimizationResult(operandNode, VariableType.REAL, token.lexeme());
            default -> throw new SemanticException("Семантическая ошибка: неизвестный тип операнда при оптимизации");
        };
    }

    private OptimizationResult foldConstants(BinaryAstNode node, OptimizationResult left, OptimizationResult right, VariableType resultType) throws SemanticException {
        Object leftVal = left.constant();
        Object rightVal = right.constant();

        Object foldedValue = switch (node.operator().type()) {
            case PLUS -> resultType == VariableType.REAL
                    ? toDouble(leftVal) + toDouble(rightVal)
                    : toInt(leftVal) + toInt(rightVal);
            case MINUS -> resultType == VariableType.REAL
                    ? toDouble(leftVal) - toDouble(rightVal)
                    : toInt(leftVal) - toInt(rightVal);
            case MULTIPLY -> resultType == VariableType.REAL
                    ? toDouble(leftVal) * toDouble(rightVal)
                    : toInt(leftVal) * toInt(rightVal);
            case DIVIDE -> {
                if (isZero(right)) {
                    throw new SemanticException("Семантическая ошибка: обнаружено деление на константу 0");
                }
                yield resultType == VariableType.REAL
                        ? toDouble(leftVal) / toDouble(rightVal)
                        : toInt(leftVal) / toInt(rightVal);
            }
            default -> throw new SemanticException("Семантическая ошибка: неизвестный оператор при оптимизации");
        };

        if (resultType == VariableType.INTEGER) {
            return new OptimizationResult(createConstantNode(TokenType.INTEGER_CONST, foldedValue), VariableType.INTEGER, foldedValue);
        }
        double realValue = foldedValue instanceof Integer ? ((Integer) foldedValue).doubleValue() : (Double) foldedValue;
        return new OptimizationResult(createConstantNode(TokenType.REAL_CONST, realValue), VariableType.REAL, realValue);
    }

    private OptimizationResult simplifyBinary(BinaryAstNode node, OptimizationResult left, OptimizationResult right, VariableType resultType) {
        if (node.operator().type() == TokenType.PLUS) {
            if (isZero(left)) return right;
            if (isZero(right)) return left;
        }

        if (node.operator().type() == TokenType.MINUS) {
            if (isZero(right)) return left;
        }

        if (node.operator().type() == TokenType.MULTIPLY) {
            if (isZero(left) || isZero(right)) {
                Object zero = resultType == VariableType.REAL ? 0.0 : 0;
                TokenType type = resultType == VariableType.REAL ? TokenType.REAL_CONST : TokenType.INTEGER_CONST;
                return new OptimizationResult(createConstantNode(type, zero), resultType, zero);
            }
            if (isOne(left)) return right;
            if (isOne(right)) return left;
        }

        if (node.operator().type() == TokenType.DIVIDE) {
            if (isZero(left)) {
                Object zero = resultType == VariableType.REAL ? 0.0 : 0;
                TokenType type = resultType == VariableType.REAL ? TokenType.REAL_CONST : TokenType.INTEGER_CONST;
                return new OptimizationResult(createConstantNode(type, zero), resultType, zero);
            }
            if (isOne(right)) {
                return left;
            }
        }

        return null;
    }

    private boolean isZero(OptimizationResult value) {
        return value.isConstant() && isZero(value.constant());
    }

    private boolean isOne(OptimizationResult value) {
        if (!value.isConstant()) {
            return false;
        }
        Object constant = value.constant();
        if (constant instanceof Integer integer) {
            return integer == 1;
        }
        if (constant instanceof Double dbl) {
            return Double.compare(dbl, 1.0) == 0;
        }
        return false;
    }

    private boolean isZero(Object constant) {
        if (constant instanceof Integer integer) {
            return integer == 0;
        }
        if (constant instanceof Double dbl) {
            return Double.compare(dbl, 0.0) == 0;
        }
        return false;
    }

    private double toDouble(Object value) {
        return value instanceof Integer integer ? integer.doubleValue() : (Double) value;
    }

    private int toInt(Object value) {
        return ((Number) value).intValue();
    }

    private AstNode createConstantNode(TokenType type, Object value) {
        return new OperandNode(new Token(type, value, 0));
    }

    private record OptimizationResult(AstNode node, VariableType type, Object constant) {
        boolean isConstant() {
            return constant != null;
        }

        boolean isIntegerConstant() {
            return isConstant() && constant instanceof Integer;
        }
    }
}
