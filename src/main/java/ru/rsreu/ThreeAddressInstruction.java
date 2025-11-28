package ru.rsreu;

public record ThreeAddressInstruction(String opcode, String result, String operand1, String operand2) {
    public ThreeAddressInstruction {
        if (opcode == null || result == null || operand1 == null) {
            throw new IllegalArgumentException("opcode, result и operand1 не могут быть null");
        }
    }

    @Override
    public String toString() {
        if (operand2 == null || operand2.isBlank()) {
            return String.format("%s %s %s", opcode, result, operand1);
        }
        return String.format("%s %s %s %s", opcode, result, operand1, operand2);
    }
}
