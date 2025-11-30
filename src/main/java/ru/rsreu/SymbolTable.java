package ru.rsreu;

import java.util.*;

public class SymbolTable {
    private final List<TableFields> identifiers = new ArrayList<>();
    private final Map<String, TableFields> entriesByName = new HashMap<>();
    private int tempCounter = 0;

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

    public Identifier registerTemporary(VariableType type) {
        tempCounter++;
        String name = "#T" + tempCounter;
        int newId = identifiers.size() + 1;
        TableFields entry = new TableFields(newId, name, type);
        identifiers.add(entry);
        entriesByName.put(name, entry);
        return new Identifier(entry.id(), entry.name(), entry.type());
    }

    public List<TableFields> getAll() {
        return Collections.unmodifiableList(identifiers);
    }

    public Identifier findIdentifierByReference(String reference) {
        Integer id = parseId(reference);
        if (id == null) {
            return null;
        }
        TableFields entry = getById(id);
        if (entry == null) {
            return null;
        }
        return new Identifier(entry.id(), entry.name(), entry.type());
    }

    private TableFields getById(int id) {
        if (id <= 0 || id > identifiers.size()) {
            return null;
        }
        return identifiers.get(id - 1);
    }

    private Integer parseId(String reference) {
        if (reference == null || !reference.startsWith("<id,") || !reference.endsWith(">") || reference.length() < 6) {
            return null;
        }
        try {
            String numeric = reference.substring(4, reference.length() - 1);
            return Integer.parseInt(numeric);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
