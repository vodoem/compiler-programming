package ru.rsreu;

import java.nio.file.Path;

public record AnalyzerConfig(
        AnalyzerMode mode,
        Path input,
        Path tokens,
        Path symbols,
        Path syntaxTree,
        Path portableCode,
        Path postfix,
        Path postCodeBinary,
        boolean optimize
) {
    public boolean isLexMode() {
        return mode == AnalyzerMode.LEX;
    }

    public boolean isSynMode() {
        return mode == AnalyzerMode.SYN;
    }

    public boolean isSemMode() {
        return mode == AnalyzerMode.SEM;
    }

    public boolean isGen1Mode() {
        return mode == AnalyzerMode.GEN1;
    }

    public boolean isGen2Mode() {
        return mode == AnalyzerMode.GEN2;
    }

    public boolean isGen3Mode() {
        return mode == AnalyzerMode.GEN3;
    }

    public boolean optimize() {
        return optimize;
    }
}
