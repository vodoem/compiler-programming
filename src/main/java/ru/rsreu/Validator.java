package ru.rsreu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Validator {
    public static void validateArgs(String[] args) {
        if (args.length != 3) {
            throw new ValidationException("Ожидалось 3 аргумента: inputFile tokensFile symbolsFile");
        }

        String inputFile = args[0];
        String tokensFile = args[1];
        String symbolsFile = args[2];

        validateFileName(inputFile);
        validateFileName(tokensFile);
        validateFileName(symbolsFile);

        if (!Files.exists(Path.of(inputFile))) {
            throw new ValidationException("Входной файл не найден: " + inputFile);
        }

        try {
            if (Files.size(Path.of(inputFile)) == 0) {
                throw new ValidationException("Входной файл пуст: " + inputFile);
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