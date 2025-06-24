package com.fix3dll.skyblockaddons.features.deployable;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents the Deployables introduced with the Slayer Update and unlocked through the Wolf slayer and Blaze slayer quests.
 */
@Getter
public enum Deployable {
    // Orbs
    RADIANT("Radiant ", 0.01, 0.0, 0, 0, 0.0, 18*18, "radiant"),
    MANA_FLUX("Mana Flux ", 0.02, 0.5, 10, 0, 0.0, 18*18, "manaflux"),
    OVERFLUX("Overflux ", 0.025, 1, 25, 5, 5.0, 18*18, "overflux"),
    PLASMAFLUX("Plasmaflux ", 0.03, 1.25, 35, 7.5, 7.5, 20*20, "plasmaflux"),

    // Flares
    WARNING_FLARE(0.0, 10, 10, 0, 0,  "22e2bf6c1ec330247927ba63479e5872ac66b06903c86c82b52dac9f1c971458", 40*40, "warning"),
    ALERT_FLARE(0.5, 20, 20, 10, 0,  "9d2bf9864720d87fd06b84efa80b795c48ed539b16523c3b1f1990b40c003f6b", 40*40, "alert"),
    SOS_FLARE(1.25, 30, 25, 10, 5,  "c0062cc98ebda72a6a4b89783adcef2815b483a01d73ea87b3df76072a89d13b", 40*40, "sos"),

    // Umberella
    UMBERELLA("Umberella ", 5, 30*30, "umberella");

    /**
     * Start of the display name of the actual floating deployable entity.
     */
    private String display = "";
    /**
     * Percentage of max health that's regenerated every second
     */
    private double healthRegen = 0.0D;
    /**
     * Percentage of mana regeneration increase given by the deployable
     */
    private double manaRegen = 0.0;
    /**
     * Amount of strength given by the deployable
     */
    private int strength = 0;
    /**
     * Amount of vitality given by the deployable
     */
    private double vitality = 0.0D;
    /**
     * Amount of mending given by the deployable
     */
    private double mending = 0;
    /**
     * Amount of vitality given by the deployable
     */
    private int trueDefense = 0;
    /**
     * Amount of ferocity given by the deployable
     */
    private int ferocity = 0;
    /**
     * Amount of strength given by the deployable
     */
    private int bonusAttackSpeed;
    /**
     * The squared range of the deployable effects
     */
    private final int rangeSquared;
    /**
     * Resource location to the icon used when displaying the deployable
     */
    private ResourceLocation resourceLocation = null;
    /**
     * Entity textureId for detect Flares
     */
    private String textureId = "";

    /**
     * Amount of Trophy Fish Chance given by the deployable
     */
    private int trophyFishChance = 0;

    // Orbs
    Deployable(String display, double healthRegen, double manaRegen, int strength, double vitality, double mending, int rangeSquared, String resourcePath) {
        this.display = display;
        this.healthRegen = healthRegen;
        this.manaRegen = manaRegen;
        this.strength = strength;
        this.vitality = vitality;
        this.mending = mending;
        this.rangeSquared = rangeSquared;
        this.resourceLocation = ResourceLocation.fromNamespaceAndPath("skyblockaddons", "deployables/"+resourcePath+".png");
    }

    // Flares
    Deployable(double manaRegen, double vitality, int trueDefense, int ferocity, int bonusAttackSpeed, String textureId, int rangeSquared, String resourcePath) {
        this.manaRegen = manaRegen;
        this.vitality = vitality;
        this.trueDefense = trueDefense;
        this.ferocity = ferocity;
        this.bonusAttackSpeed = bonusAttackSpeed;
        this.rangeSquared = rangeSquared;
        this.resourceLocation = ResourceLocation.fromNamespaceAndPath("skyblockaddons", "deployables/"+resourcePath+".png");
        this.textureId = textureId;
    }

    // Umberella
    Deployable(String display, int trophyFishChance, int rangeSquared, String resourcePath) {
        this.display = display;
        this.trophyFishChance = trophyFishChance;
        this.rangeSquared = rangeSquared;
        this.resourceLocation = ResourceLocation.fromNamespaceAndPath("skyblockaddons", "deployables/"+resourcePath+".png");
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
     * Match an entity display name against Orbs entity names to get the corresponding type.
     * @param displayName Entity display name
     * @return The matching type or null if none was found
     */
    public static Deployable getByDisplayName(String displayName) {
        for (Deployable orb : values()) {
            if(!orb.display.isEmpty() && displayName.startsWith(orb.display)) {
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
            if(!flare.textureId.isEmpty() && textureId.equals(flare.textureId)) {
                return flare;
            }
        }
        return null;
    }
}
