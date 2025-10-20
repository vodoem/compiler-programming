package ru.rsreu;

import java.util.*;
import java.util.stream.Collectors;


public class SymbolTable {
    private final List<TableFields> identifiers = new ArrayList<>();
    private final Map<String, Integer> indexCache = new HashMap<>();

    public int getOrAdd(String name) {
        Integer existingId = indexCache.get(name);
        if (existingId != null) return existingId;

        int newId = identifiers.size() + 1;
        identifiers.add(new TableFields(name));
        indexCache.put(name, newId);
        return newId;
    }

    public List<String> getAll() {
        return Collections.unmodifiableList(identifiers).stream().map(TableFields::name).collect(Collectors.toList());
    }
}