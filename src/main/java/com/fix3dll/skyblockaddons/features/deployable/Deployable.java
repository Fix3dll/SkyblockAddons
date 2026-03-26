package com.fix3dll.skyblockaddons.features.deployable;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import lombok.Builder;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Deployables introduced with the Slayer Update and unlocked through the Wolf slayer and Blaze slayer quests.
 */
@Getter
public enum Deployable {

    // Orbs
    RADIANT(Properties.builder()
            .display("Radiant ")
            .healthRegen(0.01)
            .rangeSquared(18 * 18)
            .resourcePath("radiant")
            .build()),

    MANA_FLUX(Properties.builder()
            .display("Mana Flux ")
            .healthRegen(0.02).manaRegen(0.5).strength(10)
            .rangeSquared(18 * 18)
            .resourcePath("manaflux")
            .build()),

    OVERFLUX(Properties.builder()
            .display("Overflux ")
            .healthRegen(0.025).manaRegen(1.0).strength(25).vitality(5.0).mending(5.0)
            .rangeSquared(18 * 18)
            .resourcePath("overflux")
            .build()),

    PLASMAFLUX(Properties.builder()
            .display("Plasmaflux ")
            .healthRegen(0.03).manaRegen(1.25).strength(35).vitality(7.5).mending(7.5)
            .rangeSquared(20 * 20)
            .resourcePath("plasmaflux")
            .build()),

    // Flares
    WARNING_FLARE(Properties.builder()
            .display("Warning")
            .manaRegen(0.0).vitality(10.0).trueDefense(10)
            .textureId("22e2bf6c1ec330247927ba63479e5872ac66b06903c86c82b52dac9f1c971458")
            .rangeSquared(40 * 40)
            .resourcePath("warning")
            .build()),

    ALERT_FLARE(Properties.builder()
            .display("Alert")
            .manaRegen(0.5).vitality(20.0).trueDefense(20).ferocity(10)
            .textureId("9d2bf9864720d87fd06b84efa80b795c48ed539b16523c3b1f1990b40c003f6b")
            .rangeSquared(40 * 40)
            .resourcePath("alert")
            .build()),

    SOS_FLARE(Properties.builder()
            .display("SOS")
            .manaRegen(1.25).vitality(30.0).trueDefense(25).ferocity(10).bonusAttackSpeed(5)
            .textureId("c0062cc98ebda72a6a4b89783adcef2815b483a01d73ea87b3df76072a89d13b")
            .rangeSquared(40 * 40)
            .resourcePath("sos")
            .build()),

    // Umberella
    UMBERELLA(Properties.builder()
            .display("Umberella ")
            .trophyFishChance(5)
            .rangeSquared(30 * 30)
            .resourcePath("umberella")
            .build()),

    // Totem of Corruption
    TOTEM_OF_CORRUPTION(Properties.builder()
            .display("Totem of Corruption")
            .rangeSquared(30 * 30)
            .resourcePath("totem_of_corruption")
            .build()),

    // Mining Lanterns
    DWARVEN_LANTERN(Properties.builder()
            .display("Dwarven Lantern ")
            .miningSpeed(20)
            .rangeSquared(30 * 30)
            .resourcePath("dwarven")
            .build()),

    MITHRIL_LANTERN(Properties.builder()
            .display("Mithril Lantern ")
            .miningSpeed(40).miningFortune(10)
            .rangeSquared(30 * 30)
            .resourcePath("mithril")
            .build()),

    TITANIUM_LANTERN(Properties.builder()
            .display("Titanium Lantern ")
            .miningSpeed(60).miningFortune(15).heatResistance(5)
            .rangeSquared(30 * 30)
            .resourcePath("titanium")
            .build()),

    GLACITE_LANTERN(Properties.builder()
            .display("Glacite Lantern ")
            .miningSpeed(80).miningFortune(20).heatResistance(10).coldResistance(5)
            .rangeSquared(30 * 30)
            .resourcePath("glacite")
            .build()),

    WILL_O_WISP(Properties.builder()
            .display("Will-o'-wisp ")
            .miningSpeed(100).miningFortune(25).heatResistance(20).coldResistance(10).gemstoneSpread(2.5)
            .rangeSquared(30 * 30)
            .resourcePath("will_o_wisp")
            .build());

    /**
     * Start of the display name of the actual floating deployable entity.
     */
    private final String display;

    /**
     * Percentage of max health that's regenerated every second
     */
    private final double healthRegen;

    /**
     * Percentage of mana regeneration increase given by the deployable
     */
    private final double manaRegen;

    /**
     * Amount of strength given by the deployable
     */
    private final int strength;

    /**
     * Amount of vitality given by the deployable
     */
    private final double vitality;

    /**
     * Amount of mending given by the deployable
     */
    private final double mending;

    /**
     * Amount of vitality given by the deployable
     */
    private final int trueDefense;

    /**
     * Amount of ferocity given by the deployable
     */
    private final int ferocity;

    /**
     * Amount of strength given by the deployable
     */
    private final int bonusAttackSpeed;

    /**
     * Amount of Trophy Fish Chance given by the deployable
     */
    private final int trophyFishChance;

    /**
     * Amount of mining speed given by the deployable
     */
    private final int miningSpeed;

    /**
     * Amount of mining fortune given by the deployable
     */
    private final int miningFortune;

    /**
     * Amount of heat resistance given by the deployable
     */
    private final int heatResistance;

    /**
     * Amount of cold resistance given by the deployable
     */
    private final int coldResistance;

    /**
     * Amount of gemstone spread given by the deployable
     */
    private final double gemstoneSpread;

    /**
     * The squared range of the deployable effects
     */
    private final int rangeSquared;

    /**
     * Resource location to the icon used when displaying the deployable
     */
    private final Identifier identifier;

    /**
     * Entity textureId for detect Flares
     */
    private final String textureId;

    /**
     * Pre-calculated and formatted list of static stats.
     * Cached during initialization to achieve zero-allocation rendering in the draw method.
     */
    @Getter private final List<Component> staticDisplayLines;

    private String compactDisplay;

    /**
     * Internal configuration class used exclusively for cleanly initializing Enum constants.
     */
    @Builder
    private static class Properties {
        @Builder.Default String display = "";
        @Builder.Default double healthRegen = 0.0D;
        @Builder.Default double manaRegen = 0.0;
        @Builder.Default int strength = 0;
        @Builder.Default double vitality = 0.0D;
        @Builder.Default double mending = 0.0;
        @Builder.Default int trueDefense = 0;
        @Builder.Default int ferocity = 0;
        @Builder.Default int bonusAttackSpeed = 0;
        @Builder.Default int trophyFishChance = 0;
        @Builder.Default int miningSpeed = 0;
        @Builder.Default int miningFortune = 0;
        @Builder.Default int heatResistance = 0;
        @Builder.Default int coldResistance = 0;
        @Builder.Default double gemstoneSpread = 0.0D;
        @Builder.Default String textureId = "";

        // Mandatory fields for the builder
        int rangeSquared;
        String resourcePath;
    }

    Deployable(Properties props) {
        this.display = props.display;
        this.healthRegen = props.healthRegen;
        this.manaRegen = props.manaRegen;
        this.strength = props.strength;
        this.vitality = props.vitality;
        this.mending = props.mending;
        this.trueDefense = props.trueDefense;
        this.ferocity = props.ferocity;
        this.bonusAttackSpeed = props.bonusAttackSpeed;
        this.trophyFishChance = props.trophyFishChance;
        this.miningSpeed = props.miningSpeed;
        this.miningFortune = props.miningFortune;
        this.heatResistance = props.heatResistance;
        this.coldResistance = props.coldResistance;
        this.gemstoneSpread = props.gemstoneSpread;
        this.rangeSquared = props.rangeSquared;
        this.textureId = props.textureId;
        this.identifier = SkyblockAddons.identifier("deployables/" + props.resourcePath + ".png");

        ArrayList<Component> staticLines = new ArrayList<>();
        if (this.strength > 0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%s ❁ ".formatted(this.strength)),
                ColorCode.RED.getColor()
        ));
        if (this.vitality > 0.0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%s ♨ ".formatted(TextUtils.formatNumber(this.vitality))),
                ColorCode.DARK_RED.getColor()
        ));
        if (this.mending > 0.0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%s ☄ ".formatted(TextUtils.formatNumber(this.mending))),
                ColorCode.GREEN.getColor()
        ));
        if (this.trueDefense > 0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%d ❂ ".formatted(this.trueDefense)),
                ColorCode.WHITE.getColor()
        ));
        if (this.ferocity > 0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%d ⫽ ".formatted(this.ferocity)),
                ColorCode.RED.getColor()
        ));
        if (this.bonusAttackSpeed > 0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%d%% ⚔ ".formatted(this.bonusAttackSpeed)),
                ColorCode.YELLOW.getColor()
        ));
        if (this.trophyFishChance > 0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%d ♔ ".formatted(this.trophyFishChance)),
                ColorCode.GOLD.getColor()
        ));
        if (this.miningSpeed > 0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%d ⸕ ".formatted(this.miningSpeed)),
                ColorCode.GOLD.getColor()
        ));
        if (this.miningFortune > 0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%d ☘ ".formatted(this.miningFortune)),
                ColorCode.GOLD.getColor()
        ));
        if (this.heatResistance > 0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%d ♨ ".formatted(this.heatResistance)),
                ColorCode.RED.getColor()
        ));
        if (this.coldResistance > 0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%d ❄ ".formatted(this.coldResistance)),
                ColorCode.AQUA.getColor()
        ));
        if (this.gemstoneSpread > 0) staticLines.add(TextUtils.withFixedColor(
                Component.literal("+%s ▚ ".formatted(TextUtils.formatNumber(this.gemstoneSpread))),
                ColorCode.YELLOW.getColor()
        ));

        // Convert to an immutable list to ensure thread-safety
        this.staticDisplayLines = List.copyOf(staticLines);
    }

    /**
     * Check if a distance is within this deployables radius.
     * @param distanceSquared Squared distance from deployable entity to player
     * @return Whether that distance is within radius
     */
    public boolean isInRadius(double distanceSquared) {
        return distanceSquared <= rangeSquared;
    }

    /**
     * Retrieves the lazily initialized, compact version of the display.
     * <p>
     * The compact name consists of the first word of the original {@code display} string
     * (everything before the first space character). If no space is present, the full
     * display string is used. The result is cached upon first invocation to prevent
     * string allocation overhead during high-frequency render loops.
     * @return the compact display, or an empty string ({@code ""}) if the original display string is {@code null}
     */
    public String getCompactDisplay() {
        if (this.display == null) return "";
        if (this.compactDisplay == null) {
            this.compactDisplay = this.display.indexOf(' ') == -1
                    ? this.display
                    : this.display.substring(0, this.display.indexOf(' '));
        }
        return this.compactDisplay;
    }

    /**
     * Match an entity display name against Orbs entity names to get the corresponding type.
     * @param displayName Entity display name
     * @return The matching type or null if none was found
     */
    public static Deployable getByDisplayName(String displayName) {
        for (Deployable orb : values()) {
            if (!orb.display.isEmpty() && displayName.startsWith(orb.display)) {
                return orb;
            }
        }
        return null;
    }

    /**
     * Match an entity skull id against Flares skull ids to get the corresponding type.
     * @param textureId Entity skull id
     * @return The matching type or null if none was found
     */
    public static Deployable getByTextureId(String textureId) {
        for (Deployable flare : values()) {
            if (!flare.textureId.isEmpty() && textureId.equals(flare.textureId)) {
                return flare;
            }
        }
        return null;
    }

}