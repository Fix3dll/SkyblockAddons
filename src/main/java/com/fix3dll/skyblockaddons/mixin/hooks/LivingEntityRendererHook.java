package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.time.ZonedDateTime;
import java.util.Set;

public class LivingEntityRendererHook {

    private static final ObjectOpenHashSet<String> COOL_PEOPLE = new ObjectOpenHashSet<>(Set.of(
            "Fix3dll"
    ));

    public static boolean isCoolPerson;

    public static boolean isCoolPerson(String name) {
        ZonedDateTime zdt = SkyblockAddons.getHypixelZonedDateTime();
        isCoolPerson = (zdt.getMonth().getValue() == 4 && zdt.getDayOfMonth() == 1) != COOL_PEOPLE.contains(name);
        return isCoolPerson;
    }

}