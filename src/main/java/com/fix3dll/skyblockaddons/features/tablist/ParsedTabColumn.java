package com.fix3dll.skyblockaddons.features.tablist;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class ParsedTabColumn {

    private final String title;
    private final Int2ObjectLinkedOpenHashMap<String> lines = new Int2ObjectLinkedOpenHashMap<>();
    private final List<ParsedTabSection> sections = new LinkedList<>();

    public ParsedTabColumn(String title) {
        this.title = title;
    }

    public void addLine(int vanillaIndex, String line) {
        this.lines.put(vanillaIndex, line);
    }

    public void addSection(ParsedTabSection section) {
        this.sections.add(section);
    }

    public int size() {
        return lines.size() + 1;
    }

}