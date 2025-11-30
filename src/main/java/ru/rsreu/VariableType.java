package ru.rsreu;

public enum VariableType {
    INTEGER("целого", "целый", "integer"),
    REAL("вещественного", "вещественный", "float");

    private final String tokenDescription;
    private final String symbolDescription;
    private final String codeName;

    VariableType(String tokenDescription, String symbolDescription, String codeName) {
        this.tokenDescription = tokenDescription;
        this.symbolDescription = symbolDescription;
        this.codeName = codeName;
    }

    public String tokenDescription() {
        return tokenDescription;
    }

    public String symbolDescription() {
        return symbolDescription;
    }

    public String codeName() {
        return codeName;
    }

    public static VariableType fromSpecifier(char specifier) {
        return switch (Character.toLowerCase(specifier)) {
            case 'i' -> INTEGER;
            case 'f' -> REAL;
            default -> throw new IllegalArgumentException("Неизвестный тип переменной: " + specifier);
        };
    }
}
