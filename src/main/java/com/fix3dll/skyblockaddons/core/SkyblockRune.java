package com.fix3dll.skyblockaddons.core;

import lombok.Getter;

import java.util.Map;

@Getter
public class SkyblockRune {

    private String type;
    private int level;

    public SkyblockRune(Map<String, Integer> runeData) {
        // There should only be 1 rune type
        for (Map.Entry<String, Integer> rune : runeData.entrySet()) {
            type = rune.getKey();
            level = rune.getValue();
            break;
        }
    }

}
