package ru.rsreu;

public enum AnalyzerMode {
    LEX,
    SYN,
    SEM,
    GEN1,
    GEN2,
    GEN3;

    public static AnalyzerMode from(String value) {
        if (value == null) {
            throw new ValidationException("Режим работы не указан");
        }
        return switch (value.toUpperCase()) {
            case "LEX" -> LEX;
            case "SYN" -> SYN;
            case "SEM" -> SEM;
            case "GEN1" -> GEN1;
            case "GEN2" -> GEN2;
            case "GEN3" -> GEN3;
            default -> throw new ValidationException("Неизвестный режим работы: " + value);
        };
    }
}
