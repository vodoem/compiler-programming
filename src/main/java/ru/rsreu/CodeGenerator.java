package ru.rsreu;

import ru.rsreu.ast.AstNode;
import ru.rsreu.ast.BinaryAstNode;
import ru.rsreu.ast.ConversionNode;
import ru.rsreu.ast.OperandNode;

import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {
    private final SymbolTable symbolTable;
    private final List<String> instructions = new ArrayList<>();

    public CodeGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public List<String> generate(AstNode root) throws SemanticException {
        process(root);
        return instructions;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    private ExpressionResult process(AstNode node) throws SemanticException {
        if (node instanceof OperandNode operandNode) {
            return buildOperandResult(operandNode);
        }

        if (node instanceof ConversionNode conversionNode) {
            ExpressionResult child = process(conversionNode.child());
            Identifier temp = symbolTable.registerTemporary(VariableType.REAL);
            String resultRef = formatIdentifier(temp.id());
            instructions.add(String.format("i2f %s %s", resultRef, child.reference()));
            return new ExpressionResult(resultRef, VariableType.REAL);
        }

        if (node instanceof BinaryAstNode binaryNode) {
            ExpressionResult left = process(binaryNode.left());
            ExpressionResult right = process(binaryNode.right());

            VariableType resultType = (left.type() == VariableType.REAL || right.type() == VariableType.REAL)
                    ? VariableType.REAL
                    : VariableType.INTEGER;

            Identifier temp = symbolTable.registerTemporary(resultType);
            String resultRef = formatIdentifier(temp.id());
            String opcode = toOpcode(binaryNode.operator().type());
            instructions.add(String.format("%s %s %s %s", opcode, resultRef, left.reference(), right.reference()));
            return new ExpressionResult(resultRef, resultType);
        }

        throw new SemanticException("Семантическая ошибка: неизвестный тип узла дерева");
    }

    private ExpressionResult buildOperandResult(OperandNode operandNode) throws SemanticException {
        Token token = operandNode.token();
        return switch (token.type()) {
            case IDENTIFIER -> {
                Identifier id = (Identifier) token.lexeme();
                yield new ExpressionResult(formatIdentifier(id.id()), id.type());
            }
            case INTEGER_CONST -> new ExpressionResult(token.lexeme().toString(), VariableType.INTEGER);
            case REAL_CONST -> new ExpressionResult(token.lexeme().toString(), VariableType.REAL);
            default -> throw new SemanticException("Семантическая ошибка: неизвестный тип операнда");
        };
    }

    private String toOpcode(TokenType type) throws SemanticException {
        return switch (type) {
            case PLUS -> "add";
            case MINUS -> "sub";
            case MULTIPLY -> "mul";
            case DIVIDE -> "div";
            default -> throw new SemanticException("Семантическая ошибка: неизвестный оператор для генерации кода");
        };
    }

    private String formatIdentifier(int id) {
        return String.format("<id,%d>", id);
    }

    private record ExpressionResult(String reference, VariableType type) {
    }
}
