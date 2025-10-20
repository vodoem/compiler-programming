package ru.rsreu;

public record Token(TokenType type, Object lexeme) {

    @Override
    public String toString() {
        if (type == TokenType.IDENTIFIER && lexeme instanceof Identifier id) {
            return String.format("<id,%d> - идентификатор с именем %s", id.id(), id.name());
        }
        return switch (type) {
            case IDENTIFIER -> {
                try {
                    throw new LexicalException("Тип должен быть Identifier", -1);
                } catch (LexicalException e) {
                    throw new RuntimeException(e);
                }
            }
            case INTEGER_CONST -> String.format("<%s> - константа целого типа", lexeme);
            case REAL_CONST -> String.format("<%s> - константа вещественного типа", lexeme);
            case PLUS -> "<+> - операция сложения";
            case MINUS -> "<-> - операция вычитания";
            case MULTIPLY -> "<*> - операция умножения";
            case DIVIDE -> "</> - операция деления";
            case LPAREN -> "<(> - открывающая скобка";
            case RPAREN -> "<)> - закрывающая скобка";
        };
    }
}