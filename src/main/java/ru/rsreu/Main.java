package ru.rsreu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        int rc = new Main().execute(args);
        if (rc != 0) System.exit(rc);
    }

    public int execute(String[] args) {
        try {
            Validator.validateArgs(args);

            Path inputPath = Path.of(args[0]);
            Path tokensPath = Path.of(args[1]);
            Path symbolsPath = Path.of(args[2]);

            String content = readInputFile(inputPath);

            Lexer lexer = new Lexer(content);
            lexer.tokenize();

            writeOutput(lexer, tokensPath, symbolsPath);

            printSummary(lexer);
            return 0;

        } catch (ValidationException e) {
            System.err.println("Ошибка валидации: " + e.getMessage());
            return 2;
        } catch (LexicalException le) {
            System.err.printf("Лексическая ошибка: %s (позиция: %d)%n", le.getMessage(), le.getPosition());
            return 4;
        } catch (IOException e) {
            System.err.println("Ошибка ввода/вывода: " + e.getMessage());
            return 5;
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка: " + e.getMessage());
            e.printStackTrace();
            return 6;
        }
    }

    private String readInputFile(Path inputPath) throws IOException {
        return Files.readString(inputPath);
    }

    private void writeOutput(Lexer lexer, Path tokensPath, Path symbolsPath) throws IOException {
        OutputWriter writer = new OutputWriter();
        writer.writeTokens(tokensPath, lexer.getTokens());
        writer.writeSymbols(symbolsPath, lexer.getSymbolTable());
    }

    private void printSummary(Lexer lexer) {
        System.out.println("Лексический анализ завершён успешно.");
        System.out.printf("Токенов: %d; Идентификаторов в таблице: %d%n",
                lexer.getTokens().size(),
                lexer.getSymbolTable().getAll().size());
    }
}