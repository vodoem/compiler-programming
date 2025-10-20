package ru.rsreu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Validator {
    public static AnalyzerConfig validateArgs(String[] args) {
        if (args.length < 2) {
            throw new ValidationException("Ожидалось минимум 2 аргумента: режим и путь к входному файлу");
        }

        AnalyzerMode mode = AnalyzerMode.from(args[0]);

        return switch (mode) {
            case LEX -> validateLexArgs(args);
            case SYN -> validateSynArgs(args);
        };
    }

    private static AnalyzerConfig validateLexArgs(String[] args) {
        if (args.length != 4) {
            throw new ValidationException("Для режима LEX ожидается 4 аргумента: режим, входной файл, tokens, symbols");
        }
        String inputFile = args[1];
        String tokensFile = args[2];
        String symbolsFile = args[3];

        validateFileName(inputFile);
        validateFileName(tokensFile);
        validateFileName(symbolsFile);

        Path inputPath = Path.of(inputFile);
        ensureInputFileValid(inputPath);

        return new AnalyzerConfig(AnalyzerMode.LEX, inputPath, Path.of(tokensFile), Path.of(symbolsFile), null);
    }

    private static AnalyzerConfig validateSynArgs(String[] args) {
        if (args.length != 3) {
            throw new ValidationException("Для режима SYN ожидается 3 аргумента: режим, входной файл, syntax_tree");
        }
        String inputFile = args[1];
        String treeFile = args[2];

        validateFileName(inputFile);
        validateFileName(treeFile);

        Path inputPath = Path.of(inputFile);
        ensureInputFileValid(inputPath);

        return new AnalyzerConfig(AnalyzerMode.SYN, inputPath, null, null, Path.of(treeFile));
    }

    private static void ensureInputFileValid(Path inputPath) {
        if (!Files.exists(inputPath)) {
            throw new ValidationException("Входной файл не найден: " + inputPath);
        }

        try {
            if (Files.size(inputPath) == 0) {
                throw new ValidationException("Входной файл пуст: " + inputPath);
            }
        } catch (IOException e) {
            throw new ValidationException("Ошибка при чтении входного файла: " + e.getMessage());
        }
    }

    private static void validateFileName(String fileName) {
        File file = new File(fileName);
        try {
            file.getCanonicalPath();
        } catch (Exception e) {
            throw new ValidationException("Недопустимое имя файла: " + fileName);
        }
    }
}