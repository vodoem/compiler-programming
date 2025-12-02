package ru.rsreu;

import ru.rsreu.ast.AstNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
                List<ThreeAddressInstruction> instructions = generator.getInstructions();
                Iterable<Integer> usedIds = collectIdsFromInstructions(instructions, lexer.getSymbolTable());
                Map<Integer, Integer> idMapping = buildIdMapping(usedIds);
                List<ThreeAddressInstruction> remappedInstructions = remapInstructions(instructions, idMapping, lexer.getSymbolTable());
                writer.writePortableCode(config.portableCode(), remappedInstructions);
                writer.writeCodeSymbols(config.symbols(), lexer.getSymbolTable().remapEntries(idMapping));
                System.out.println("Генерация трехадресного кода завершена успешно.");
            } else if (config.isGen2Mode()) {
                Parser parser = new Parser(lexer.getTokens());
                AstNode syntaxTree = parser.parse();
                SemanticAnalyzer analyzer = new SemanticAnalyzer();
                AstNode modifiedTree = analyzer.analyze(syntaxTree);
                AstNode optimizedTree = config.optimize() ? new AstOptimizer().optimize(modifiedTree) : modifiedTree;
                PostfixGenerator generator = new PostfixGenerator();
                OutputWriter writer = new OutputWriter();
                List<String> postfix = generator.generate(optimizedTree);
                Iterable<Integer> usedIds = collectIdsFromPostfix(postfix, lexer.getSymbolTable());
                Map<Integer, Integer> idMapping = buildIdMapping(usedIds);
                List<String> remappedPostfix = remapPostfix(postfix, idMapping, lexer.getSymbolTable());
                writer.writePostfix(config.postfix(), remappedPostfix);
                writer.writeCodeSymbols(config.symbols(), lexer.getSymbolTable().remapEntries(idMapping));
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

    private Iterable<Integer> collectIdsFromInstructions(List<ThreeAddressInstruction> instructions, SymbolTable symbolTable) {
        Set<Integer> ids = new LinkedHashSet<>();
        for (ThreeAddressInstruction instruction : instructions) {
            addReference(instruction.result(), symbolTable, ids);
            addReference(instruction.operand1(), symbolTable, ids);
            if (instruction.operand2() != null && !instruction.operand2().isBlank()) {
                addReference(instruction.operand2(), symbolTable, ids);
            }
        }
        return ids;
    }

    private Iterable<Integer> collectIdsFromPostfix(List<String> postfix, SymbolTable symbolTable) {
        Set<Integer> ids = new LinkedHashSet<>();
        for (String token : postfix) {
            addReference(token, symbolTable, ids);
        }
        return ids;
    }

    private Map<Integer, Integer> buildIdMapping(Iterable<Integer> usedIds) {
        Map<Integer, Integer> mapping = new LinkedHashMap<>();
        TreeSet<Integer> sorted = new TreeSet<>();
        for (Integer id : usedIds) {
            if (id != null) {
                sorted.add(id);
            }
        }
        int nextId = 1;
        for (Integer oldId : sorted) {
            mapping.put(oldId, nextId++);
        }
        return mapping;
    }

    private List<ThreeAddressInstruction> remapInstructions(List<ThreeAddressInstruction> instructions, Map<Integer, Integer> idMapping, SymbolTable symbolTable) {
        return instructions.stream()
                .map(instr -> new ThreeAddressInstruction(
                        instr.opcode(),
                        remapReference(instr.result(), idMapping, symbolTable),
                        remapReference(instr.operand1(), idMapping, symbolTable),
                        instr.operand2() == null ? null : remapReference(instr.operand2(), idMapping, symbolTable)))
                .toList();
    }

    private List<String> remapPostfix(List<String> postfix, Map<Integer, Integer> idMapping, SymbolTable symbolTable) {
        return postfix.stream()
                .map(token -> remapReference(token, idMapping, symbolTable))
                .toList();
    }

    private void addReference(String reference, SymbolTable symbolTable, Set<Integer> ids) {
        Integer id = symbolTable.extractId(reference);
        if (id != null) {
            ids.add(id);
        }
    }

    private String remapReference(String reference, Map<Integer, Integer> idMapping, SymbolTable symbolTable) {
        Integer id = symbolTable.extractId(reference);
        if (id == null) {
            return reference;
        }
        Integer newId = idMapping.get(id);
        if (newId == null) {
            return reference;
        }
        return String.format("<id,%d>", newId);
    }
}