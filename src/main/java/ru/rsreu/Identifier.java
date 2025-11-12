package ru.rsreu;

public record Identifier(int id, String name, VariableType type) {
    @Override
    public String toString() {
        return name;
    }
}
