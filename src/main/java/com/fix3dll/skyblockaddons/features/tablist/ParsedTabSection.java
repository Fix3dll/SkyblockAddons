package com.fix3dll.skyblockaddons.features.tablist;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import lombok.Getter;

@Getter
public class ParsedTabSection {

    private final ParsedTabColumn column;
    private final Int2ObjectLinkedOpenHashMap<String> lines = new Int2ObjectLinkedOpenHashMap<>();

    public ParsedTabSection(ParsedTabColumn column) {
        this.column = column;
    }

    public void addLine(int vanillaIndex, String line) {
        this.lines.put(vanillaIndex, line);
    }

    public int size() {
        return lines.size();
    }

}