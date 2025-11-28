package ru.rsreu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.List;

public class OutputWriter {

    public void writeTokens(Path tokensFile, List<Token> tokens) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            sb.append(t).append(System.lineSeparator());
        }
        Files.writeString(tokensFile, sb.toString());
    }

    public void writeSymbols(Path symbolsFile, SymbolTable table) throws IOException {
        StringBuilder sb = new StringBuilder();
        List<TableFields> identifiers = table.getAll();
        for (TableFields entry : identifiers) {
            sb.append(String.format("%d - %s [%s]%n",
                    entry.id(),
                    entry.name(),
                    entry.type().symbolDescription()));
        }
        Files.writeString(symbolsFile, sb.toString());
    }

    public void writePortableCode(Path output, List<ThreeAddressInstruction> instructions) throws IOException {
        String content = instructions.stream()
                .map(ThreeAddressInstruction::toString)
                .collect(Collectors.joining(System.lineSeparator()));
        Files.writeString(output, content);
    }

    public void writePostfix(Path output, List<String> postfix) throws IOException {
        String content = String.join(" ", postfix);
        Files.writeString(output, content);
    }

    public void writeCodeSymbols(Path symbolsFile, SymbolTable table) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (TableFields entry : table.getAll()) {
            sb.append(String.format("<id,%d> - %s, %s%n",
                    entry.id(),
                    entry.name(),
                    entry.type().codeName()));
        }
        Files.writeString(symbolsFile, sb.toString());
    }
}
