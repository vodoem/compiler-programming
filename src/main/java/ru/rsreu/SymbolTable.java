package ru.rsreu;

import java.util.*;

public class SymbolTable {
    private final List<TableFields> identifiers = new ArrayList<>();
    private final Map<String, TableFields> entriesByName = new HashMap<>();

    public Identifier register(String name, VariableType type, boolean explicitType, int position) throws LexicalException {
        TableFields existing = entriesByName.get(name);
        if (existing != null) {
            if (explicitType && existing.type() != type) {
                throw new LexicalException(String.format("Идентификатор '%s' уже объявлен с типом %s", name, existing.type().symbolDescription()), position);
            }
            if (!explicitType && existing.type() != VariableType.INTEGER) {
                throw new LexicalException(String.format("Идентификатор '%s' уже объявлен с типом %s", name, existing.type().symbolDescription()), position);
            }
            return new Identifier(existing.id(), existing.name(), existing.type());
        }

        VariableType finalType = explicitType ? type : VariableType.INTEGER;
        int newId = identifiers.size() + 1;
        TableFields entry = new TableFields(newId, name, finalType);
        identifiers.add(entry);
        entriesByName.put(name, entry);
        return new Identifier(entry.id(), entry.name(), entry.type());
    }

    public List<TableFields> getAll() {
        return Collections.unmodifiableList(identifiers);
    }
}
