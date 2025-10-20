package ru.rsreu;

public record Token(TokenType type, Object lexeme, int position) {

    @Override
    public String toString() {
        return switch (type) {
            case IDENTIFIER -> {
                Identifier id = (Identifier) lexeme;
                yield String.format("<id,%d> - идентификатор с именем %s", id.id(), id.name());
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

    public String compactRepresentation() {
        return switch (type) {
            case IDENTIFIER -> {
                Identifier id = (Identifier) lexeme;
                yield String.format("<id,%d>", id.id());
            }
            case INTEGER_CONST, REAL_CONST -> String.format("<%s>", lexeme);
            case PLUS -> "<+>";
            case MINUS -> "<->";
            case MULTIPLY -> "<*>";
            case DIVIDE -> "</>";
            case LPAREN -> "<(>";
            case RPAREN -> "<)>";
        };
    }
}