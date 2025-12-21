package ru.rsreu.io;

import ru.rsreu.TableFields;
import ru.rsreu.ThreeAddressInstruction;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class PostCodeBinaryWriter {
    private static final int MAGIC = 0x50433033; // "PC03"
    private static final int VERSION = 1;

    public void write(Path output, List<ThreeAddressInstruction> instructions, List<TableFields> symbols) throws IOException {
        List<TableFields> sortedSymbols = symbols.stream()
                .sorted(Comparator.comparingInt(TableFields::id))
                .toList();

        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(output))) {
            dos.writeInt(MAGIC);
            dos.writeInt(VERSION);
            writeSymbols(dos, sortedSymbols);
            writeInstructions(dos, instructions);
        }
    }

    private void writeSymbols(DataOutputStream dos, List<TableFields> symbols) throws IOException {
        dos.writeInt(symbols.size());
        for (TableFields entry : symbols) {
            dos.writeInt(entry.id());
            dos.writeUTF(entry.name());
            dos.writeUTF(entry.type().name());
        }
    }

    private void writeInstructions(DataOutputStream dos, List<ThreeAddressInstruction> instructions) throws IOException {
        dos.writeInt(instructions.size());
        for (ThreeAddressInstruction instruction : instructions) {
            dos.writeUTF(instruction.opcode());
            dos.writeUTF(instruction.result());
            dos.writeUTF(instruction.operand1());
            boolean hasOperand2 = instruction.operand2() != null && !instruction.operand2().isBlank();
            dos.writeBoolean(hasOperand2);
            if (hasOperand2) {
                dos.writeUTF(instruction.operand2());
            }
        }
    }
}
