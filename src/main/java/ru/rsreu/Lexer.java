package ru.rsreu;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private final int length;
    private int pos = 0;
    private final SymbolTable symbolTable = new SymbolTable();
    private final List<Token> tokens = new ArrayList<>();

    public Lexer(String input) {
        this.input = input != null ? input : "";
        this.length = this.input.length();
    }

    public void tokenize() throws LexicalException {
        while (!isAtEnd()) {
            char c = peek();
            if (isWhitespace(c)) {
                advance();
                continue;
            }
            if (isLetter(c) || c == '_') {
                lexIdentifier();
                continue;
            }
            if (isDigit(c)) {
                lexNumberOrErrorIfFollowedByLetters();
                continue;
            }
            switch (c) {
                case '+':
                    tokens.add(new Token(TokenType.PLUS, "+", pos + 1));
                    advance();
                    break;
                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-", pos + 1));
                    advance();
                    break;
                case '*':
                    tokens.add(new Token(TokenType.MULTIPLY, "*", pos + 1));
                    advance();
                    break;
                case '/':
                    tokens.add(new Token(TokenType.DIVIDE, "/", pos + 1));
                    advance();
                    break;
                case '(':
                    tokens.add(new Token(TokenType.LPAREN, "(", pos + 1));
                    advance();
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RPAREN, ")", pos + 1));
                    advance();
                    break;
                default:
                    int errPos = pos + 1;
                    throw new LexicalException(String.format("Лексическая ошибка! Недопустимый символ '%c' на позиции %d", c, errPos), errPos);
            }
        }
    }

    private void lexIdentifier() throws LexicalException {
        int start = pos;
        StringBuilder sb = new StringBuilder();
        sb.append(advance());
        while (!isAtEnd() && (isLetter(peek()) || isDigit(peek()) || peek() == '_')) {
            sb.append(advance());
        }
        String name = sb.toString();
        VariableType type = VariableType.INTEGER;
        boolean explicit = false;
        if (!isAtEnd() && peek() == '[') {
            explicit = true;
            advance();
            if (isAtEnd()) {
                throw new LexicalException(String.format("Лексическая ошибка! Не указан тип для идентификатора '%s'", name), pos + 1);
            }
            char typeChar = advance();
            try {
                type = VariableType.fromSpecifier(typeChar);
            } catch (IllegalArgumentException e) {
                throw new LexicalException(String.format("Лексическая ошибка! Неизвестный тип '%c' для идентификатора '%s'", typeChar, name), pos);
            }
            if (isAtEnd() || peek() != ']') {
                throw new LexicalException(String.format("Лексическая ошибка! Отсутствует закрывающая скобка типа у идентификатора '%s'", name), pos + 1);
            }
            advance();
        }
        Identifier identifier = symbolTable.register(name, type, explicit, start + 1);
        tokens.add(new Token(TokenType.IDENTIFIER, identifier, start + 1));
    }

    private void lexNumberOrErrorIfFollowedByLetters() throws LexicalException {
        int start = pos;
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && isDigit(peek())) {
            sb.append(advance());
        }
        boolean isReal = false;
        if (!isAtEnd() && peek() == '.') {
            isReal = true;
            sb.append(advance());
            if (isAtEnd() || !isDigit(peek())) {
                int errPos = pos + 1;
                throw new LexicalException(String.format("Лексическая ошибка! Неправильно задана константа «%s» на позиции %d", buildPreview(start), errPos), errPos);
            }
            while (!isAtEnd() && isDigit(peek())) {
                sb.append(advance());
            }
        }
        if (!isAtEnd() && (isLetter(peek()) || peek() == '_')) {
            int errPos = start + 1;
            throw new LexicalException(String.format("Лексическая ошибка! Идентификатор '%s' не может начинаться с цифры на позиции %d", sb.toString(), errPos), errPos);
        }
        String numLexeme = sb.toString();
        if (isReal) {
            if (!isNumericRealWellFormed(numLexeme)) {
                int errPos = start + 1;
                throw new LexicalException(String.format("Лексическая ошибка! Неправильно задана константа «%s» на позиции %d", numLexeme, errPos), errPos);
            }
            tokens.add(new Token(TokenType.REAL_CONST, Double.parseDouble(numLexeme), start + 1));
        } else {
            tokens.add(new Token(TokenType.INTEGER_CONST, Integer.parseInt(numLexeme), start + 1));
        }
    }

    private boolean isNumericRealWellFormed(String s) {
        int dots = 0;
        for (char c : s.toCharArray()) {
            if (c == '.') dots++;
            else if (!Character.isDigit(c)) return false;
        }
        return dots == 1;
    }

    private char advance() {
        return input.charAt(pos++);
    }

    private char peek() {
        return input.charAt(pos);
    }

    private boolean isAtEnd() {
        return pos >= length;
    }

    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    private boolean isLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private String buildPreview(int start) {
        int end = Math.min(length, start + 10);
        return input.substring(start, end);
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}
