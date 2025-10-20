package ru.rsreu;

public record Identifier(int id, String name) {
    @Override
    public String toString() {
        return name;
    }
}
