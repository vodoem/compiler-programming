package ru.rsreu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        List<String> identifiers = table.getAll();
        for (int i = 0; i < identifiers.size(); i++) {
            sb.append(String.format("%d - %s%n", i+1, identifiers.get(i)));
        }
        Files.writeString(symbolsFile, sb.toString());
    }
}
