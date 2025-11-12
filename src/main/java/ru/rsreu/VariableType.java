package ru.rsreu;

public enum VariableType {
    INTEGER("целого", "целый"),
    REAL("вещественного", "вещественный");

    private final String tokenDescription;
    private final String symbolDescription;

    VariableType(String tokenDescription, String symbolDescription) {
        this.tokenDescription = tokenDescription;
        this.symbolDescription = symbolDescription;
    }

    public String tokenDescription() {
        return tokenDescription;
    }

    public String symbolDescription() {
        return symbolDescription;
    }

    public static VariableType fromSpecifier(char specifier) {
        return switch (Character.toLowerCase(specifier)) {
            case 'i' -> INTEGER;
            case 'f' -> REAL;
            default -> throw new IllegalArgumentException("Неизвестный тип переменной: " + specifier);
        };
    }
}
