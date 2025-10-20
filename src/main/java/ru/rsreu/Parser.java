package ru.rsreu;

import ru.rsreu.ast.AstNode;
import ru.rsreu.ast.BinaryAstNode;
import ru.rsreu.ast.OperandNode;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public AstNode parse() throws SyntaxException {
        if (tokens == null || tokens.isEmpty()) {
            throw new SyntaxException("Синтаксическая ошибка! В выражении отсутствуют токены.");
        }
        AstNode expression = parseExpression(null);
        if (!isAtEnd()) {
            Token unexpected = peek();
            Token prev = previous();
            if (isOperandToken(unexpected) || unexpected.type() == TokenType.LPAREN) {
                throw new SyntaxException(String.format(
                        "Синтаксическая ошибка! После %s на позиции %d отсутствует операция.",
                        prev.compactRepresentation(), prev.position()));
            }
            if (unexpected.type() == TokenType.RPAREN) {
                throw new SyntaxException(String.format(
                        "Синтаксическая ошибка! У закрывающей скобки <)> на позиции %d отсутствует открывающая скобка.",
                        unexpected.position()));
            }
            throw new SyntaxException(String.format(
                    "Синтаксическая ошибка! Неожиданный токен %s на позиции %d.",
                    unexpected.compactRepresentation(), unexpected.position()));
        }
        return expression;
    }

    private AstNode parseExpression(Token operatorContext) throws SyntaxException {
        AstNode node = parseTerm(operatorContext);
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            AstNode right = parseTerm(operator);
            node = new BinaryAstNode(operator, node, right);
        }
        return node;
    }

    private AstNode parseTerm(Token operatorContext) throws SyntaxException {
        AstNode node = parseFactor(operatorContext);
        while (match(TokenType.MULTIPLY, TokenType.DIVIDE)) {
            Token operator = previous();
            AstNode right = parseFactor(operator);
            node = new BinaryAstNode(operator, node, right);
        }
        return node;
    }

    private AstNode parseFactor(Token operatorContext) throws SyntaxException {
        if (isAtEnd()) {
            if (operatorContext != null) {
                throw missingOperand(operatorContext);
            }
            throw new SyntaxException("Синтаксическая ошибка! Неожиданный конец выражения.");
        }

        Token token = peek();

        if (isOperandToken(token)) {
            advance();
            return new OperandNode(token);
        }

        if (token.type() == TokenType.LPAREN) {
            Token open = advance();
            if (check(TokenType.RPAREN)) {
                throw new SyntaxException(String.format(
                        "Синтаксическая ошибка! После открывающей скобки <(> на позиции %d отсутствует операнд.",
                        open.position()));
            }
            AstNode inner = parseExpression(null);
            if (!match(TokenType.RPAREN)) {
                throw new SyntaxException(String.format(
                        "Синтаксическая ошибка! У открывающей скобки <(> на позиции %d отсутствует закрывающая скобка.",
                        open.position()));
            }
            return inner;
        }

        if (operatorContext != null) {
            throw missingOperand(operatorContext);
        }

        if (token.type() == TokenType.RPAREN) {
            Token closing = advance();
            throw new SyntaxException(String.format(
                    "Синтаксическая ошибка! У закрывающей скобки <)> на позиции %d отсутствует открывающая скобка.",
                    closing.position()));
        }

        if (isOperatorToken(token)) {
            Token op = advance();
            throw missingOperand(op);
        }

        Token unexpected = advance();
        throw new SyntaxException(String.format(
                "Синтаксическая ошибка! Неожиданный токен %s на позиции %d.",
                unexpected.compactRepresentation(), unexpected.position()));
    }

    private SyntaxException missingOperand(Token operator) {
        return new SyntaxException(String.format(
                "Синтаксическая ошибка! У операции %s на позиции %d отсутствует операнд",
                operator.compactRepresentation(), operator.position()));
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isOperandToken(Token token) {
        return token.type() == TokenType.IDENTIFIER
                || token.type() == TokenType.INTEGER_CONST
                || token.type() == TokenType.REAL_CONST;
    }

    private boolean isOperatorToken(Token token) {
        return token.type() == TokenType.PLUS
                || token.type() == TokenType.MINUS
                || token.type() == TokenType.MULTIPLY
                || token.type() == TokenType.DIVIDE;
    }
}
