package ru.rsreu;

public enum AnalyzerMode {
    LEX,
    SYN,
    SEM;

    public static AnalyzerMode from(String value) {
        if (value == null) {
            throw new ValidationException("Режим работы не указан");
        }
        return switch (value.toUpperCase()) {
            case "LEX" -> LEX;
            case "SYN" -> SYN;
            case "SEM" -> SEM;
            default -> throw new ValidationException("Неизвестный режим работы: " + value);
        };
    }
}
