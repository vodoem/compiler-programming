package ru.rsreu;

import ru.rsreu.ast.AstNode;

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
            AnalyzerConfig config = Validator.validateArgs(args);

            String content = readInputFile(config.input());

            Lexer lexer = new Lexer(content);
            lexer.tokenize();

            if (config.isLexMode()) {
                writeLexicalOutput(lexer, config.tokens(), config.symbols());
                printLexicalSummary(lexer);
            } else if (config.isSynMode()) {
                Parser parser = new Parser(lexer.getTokens());
                AstNode syntaxTree = parser.parse();
                SyntaxTreeWriter writer = new SyntaxTreeWriter();
                writer.write(config.syntaxTree(), syntaxTree);
                System.out.println("Синтаксический анализ завершён успешно.");
            } else if (config.isSemMode()) {
                Parser parser = new Parser(lexer.getTokens());
                AstNode syntaxTree = parser.parse();
                SemanticAnalyzer analyzer = new SemanticAnalyzer();
                AstNode modifiedTree = analyzer.analyze(syntaxTree);
                SyntaxTreeWriter writer = new SyntaxTreeWriter();
                writer.write(config.syntaxTree(), modifiedTree);
                System.out.println("Семантический анализ завершён успешно.");
            } else if (config.isGen1Mode()) {
                Parser parser = new Parser(lexer.getTokens());
                AstNode syntaxTree = parser.parse();
                SemanticAnalyzer analyzer = new SemanticAnalyzer();
                AstNode modifiedTree = analyzer.analyze(syntaxTree);
                AstNode optimizedTree = config.optimize() ? new AstOptimizer().optimize(modifiedTree) : modifiedTree;
                CodeGenerator generator = new CodeGenerator(lexer.getSymbolTable(), config.optimize());
                generator.generate(optimizedTree);
                OutputWriter writer = new OutputWriter();
                writer.writePortableCode(config.portableCode(), generator.getInstructions());
                writer.writeCodeSymbols(config.symbols(), lexer.getSymbolTable());
                System.out.println("Генерация трехадресного кода завершена успешно.");
            } else if (config.isGen2Mode()) {
                Parser parser = new Parser(lexer.getTokens());
                AstNode syntaxTree = parser.parse();
                SemanticAnalyzer analyzer = new SemanticAnalyzer();
                AstNode modifiedTree = analyzer.analyze(syntaxTree);
                AstNode optimizedTree = config.optimize() ? new AstOptimizer().optimize(modifiedTree) : modifiedTree;
                PostfixGenerator generator = new PostfixGenerator();
                OutputWriter writer = new OutputWriter();
                writer.writePostfix(config.postfix(), generator.generate(optimizedTree));
                writer.writeCodeSymbols(config.symbols(), lexer.getSymbolTable());
                System.out.println("Генерация постфиксной записи завершена успешно.");
            }
            return 0;

        } catch (ValidationException e) {
            System.err.println("Ошибка валидации: " + e.getMessage());
            return 2;
        } catch (LexicalException le) {
            System.err.printf("Лексическая ошибка: %s (позиция: %d)%n", le.getMessage(), le.getPosition());
            return 4;
        } catch (SyntaxException se) {
            System.err.println(se.getMessage());
            return 7;
        } catch (SemanticException se) {
            System.err.println(se.getMessage());
            return 8;
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

    private void writeLexicalOutput(Lexer lexer, Path tokensPath, Path symbolsPath) throws IOException {
        OutputWriter writer = new OutputWriter();
        writer.writeTokens(tokensPath, lexer.getTokens());
        writer.writeSymbols(symbolsPath, lexer.getSymbolTable());
    }

    private void printLexicalSummary(Lexer lexer) {
        System.out.println("Лексический анализ завершён успешно.");
        System.out.printf("Токенов: %d; Идентификаторов в таблице: %d%n",
                lexer.getTokens().size(),
                lexer.getSymbolTable().getAll().size());
    }
}