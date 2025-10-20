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
                    tokens.add(new Token(TokenType.PLUS, "+"));
                    advance();
                    break;
                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-"));
                    advance();
                    break;
                case '*':
                    tokens.add(new Token(TokenType.MULTIPLY, "*"));
                    advance();
                    break;
                case '/':
                    tokens.add(new Token(TokenType.DIVIDE, "/"));
                    advance();
                    break;
                case '(':
                    tokens.add(new Token(TokenType.LPAREN, "("));
                    advance();
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RPAREN, ")"));
                    advance();
                    break;
                default:
                    int errPos = pos + 1;
                    throw new LexicalException(String.format("Лексическая ошибка! Недопустимый символ '%c' на позиции %d", c, errPos), errPos);
            }
        }
    }

    private void lexIdentifier() {
        int start = pos;
        StringBuilder sb = new StringBuilder();
        sb.append(advance());
        while (!isAtEnd() && (isLetter(peek()) || isDigit(peek()) || peek() == '_')) {
            sb.append(advance());
        }
        String name = sb.toString();
        int id = symbolTable.getOrAdd(name);
        tokens.add(new Token(TokenType.IDENTIFIER, new Identifier(id, name)));
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
            tokens.add(new Token(TokenType.REAL_CONST, Double.parseDouble(numLexeme)));
        } else {
            tokens.add(new Token(TokenType.INTEGER_CONST, Integer.parseInt(numLexeme)));
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
