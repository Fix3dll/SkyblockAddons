package com.fix3dll.skyblockaddons.core;

import lombok.Getter;
import net.minecraft.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// https://wiki.hypixel.net/Mayors
@Getter
public enum SkyblockMayor {
    // Regular Candidates
    Aatrox("Slayer XP Buff", "Pathfinder", "SLASHED Pricing"),
    Cole("Mining Fiesta", "Mining XP Buff", "Molten Forge", "Prospection"),
    Diana("Pet XP Buff", "Lucky!", "Mythological Ritual", "Sharing is Caring"),
    Diaz("Long Term Investment", "Shopping Spree", "Stock Exchange", "Volume Trading: Double"),
    Finnegan("Blooming Business", "GOATed", "Pelt-pocalypse", "Pest Eradicator"),
    Foxy("A Time for Giving", "Chivalrous Carnival", "Extra Event", "Sweet Benevolence"),
    Marina("Double Trouble", "Fishing XP Buff", "Fishing Festival", "Luck of the Sea 2.0"),
    Paul("Benediction", "Marauder", "EZPZ"),
    // Special Candidates
    Jerry("Perkpocalypse", "Statspocalypse", "Jerrypocalypse"),
    Derpy("QUAD TAXES!!!", "TURBO MINIONS!!!", "DOUBLE MOBS HP!!!", "MOAR SKILLZ!!!"),
    Scorpius("Bribe", "Darker Auctions");

    private final List<String> perks;

    SkyblockMayor(String... perks) {
        this.perks = Collections.unmodifiableList(Arrays.asList(perks));
    }

    public static SkyblockMayor getByPerkName(String name) {
        if (StringUtil.isNullOrEmpty(name)) return null; // return silently

        for (SkyblockMayor skyblockMayor : SkyblockMayor.values()) {
            for (String perk : skyblockMayor.perks) {
                if (perk.equalsIgnoreCase(name)) {
                    return skyblockMayor;
                }
            }
        }
        return null;
    }
}
