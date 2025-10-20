package ru.rsreu;

import java.nio.file.Path;

public record AnalyzerConfig(
        AnalyzerMode mode,
        Path input,
        Path tokens,
        Path symbols,
        Path syntaxTree
) {
    public boolean isLexMode() {
        return mode == AnalyzerMode.LEX;
    }

    public boolean isSynMode() {
        return mode == AnalyzerMode.SYN;
    }
}
