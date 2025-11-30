package ru.rsreu;

import ru.rsreu.ast.AstNode;
import ru.rsreu.ast.BinaryAstNode;
import ru.rsreu.ast.ConversionNode;
import ru.rsreu.ast.OperandNode;

import java.util.*;

public class CodeGenerator {
    private final SymbolTable symbolTable;
    private final boolean optimize;
    private final List<ThreeAddressInstruction> instructions = new ArrayList<>();
    private final Map<VariableType, Deque<Identifier>> tempPool = new EnumMap<>(VariableType.class);

    public CodeGenerator(SymbolTable symbolTable, boolean optimize) {
        this.symbolTable = symbolTable;
        this.optimize = optimize;
    }

    public List<ThreeAddressInstruction> generate(AstNode root) throws SemanticException {
        process(root);
        return instructions;
    }

    public List<ThreeAddressInstruction> getInstructions() {
        return instructions;
    }

    private ExpressionResult process(AstNode node) throws SemanticException {
        if (node instanceof OperandNode operandNode) {
            return buildOperandResult(operandNode);
        }

        if (node instanceof ConversionNode conversionNode) {
            ExpressionResult child = process(conversionNode.child());
            Identifier temp = acquireTemporary(VariableType.REAL);
            String resultRef = formatIdentifier(temp.id());
            instructions.add(new ThreeAddressInstruction("i2f", resultRef, child.reference(), null));
            releaseTemporary(child, resultRef);
            return new ExpressionResult(resultRef, VariableType.REAL);
        }

        if (node instanceof BinaryAstNode binaryNode) {
            ExpressionResult left = process(binaryNode.left());
            ExpressionResult right = process(binaryNode.right());

            VariableType resultType = (left.type() == VariableType.REAL || right.type() == VariableType.REAL)
                    ? VariableType.REAL
                    : VariableType.INTEGER;

            Identifier temp = acquireTemporary(resultType);
            String resultRef = formatIdentifier(temp.id());
            String opcode = toOpcode(binaryNode.operator().type());
            instructions.add(new ThreeAddressInstruction(opcode, resultRef, left.reference(), right.reference()));
            releaseTemporary(left, resultRef);
            releaseTemporary(right, resultRef);
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

    private Identifier acquireTemporary(VariableType type) {
        if (!optimize) {
            return symbolTable.registerTemporary(type);
        }

        Deque<Identifier> pool = tempPool.computeIfAbsent(type, t -> new ArrayDeque<>());
        Identifier existing = pool.poll();
        if (existing != null) {
            return existing;
        }
        return symbolTable.registerTemporary(type);
    }

    private void releaseTemporary(ExpressionResult result, String preservedReference) {
        if (!optimize) {
            return;
        }
        String reference = result.reference();
        if (reference.equals(preservedReference)) {
            return;
        }

        Identifier identifier = symbolTable.findIdentifierByReference(reference);
        if (identifier != null && identifier.name().startsWith("#T")) {
            Deque<Identifier> pool = tempPool.computeIfAbsent(identifier.type(), t -> new ArrayDeque<>());
            pool.offer(identifier);
        }
    }

    private record ExpressionResult(String reference, VariableType type) {
    }
}
