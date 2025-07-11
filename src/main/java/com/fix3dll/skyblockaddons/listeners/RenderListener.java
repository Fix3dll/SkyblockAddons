package com.fix3dll.skyblockaddons.listeners;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.CrimsonArmorAbilityStack;
import com.fix3dll.skyblockaddons.core.EssenceType;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureGuiData;
import com.fix3dll.skyblockaddons.core.InventoryType;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.ItemDiff;
import com.fix3dll.skyblockaddons.core.PlayerStat;
import com.fix3dll.skyblockaddons.core.SkillType;
import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.fix3dll.skyblockaddons.core.SlayerArmorProgress;
import com.fix3dll.skyblockaddons.core.ThunderBottle;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.core.scheduler.ScheduledTask;
import com.fix3dll.skyblockaddons.events.RenderEvents;
import com.fix3dll.skyblockaddons.features.BaitManager;
import com.fix3dll.skyblockaddons.features.EndstoneProtectorManager;
import com.fix3dll.skyblockaddons.features.FetchurManager;
import com.fix3dll.skyblockaddons.features.PetManager;
import com.fix3dll.skyblockaddons.features.deployable.Deployable;
import com.fix3dll.skyblockaddons.features.deployable.DeployableManager;
import com.fix3dll.skyblockaddons.features.dragontracker.DragonTracker;
import com.fix3dll.skyblockaddons.features.dragontracker.DragonType;
import com.fix3dll.skyblockaddons.features.dragontracker.DragonsSince;
import com.fix3dll.skyblockaddons.features.dungeonmap.DungeonMapManager;
import com.fix3dll.skyblockaddons.features.dungeons.DungeonClass;
import com.fix3dll.skyblockaddons.features.dungeons.DungeonMilestone;
import com.fix3dll.skyblockaddons.features.TrevorTrapperTracker;
import com.fix3dll.skyblockaddons.features.healingcircle.HealingCircleManager;
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerBoss;
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerDrop;
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerTracker;
import com.fix3dll.skyblockaddons.features.spooky.CandyType;
import com.fix3dll.skyblockaddons.features.spooky.SpookyEventManager;
import com.fix3dll.skyblockaddons.features.tablist.TabListParser;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonLocation;
import com.fix3dll.skyblockaddons.gui.screens.EnchantmentSettingsGui;
import com.fix3dll.skyblockaddons.gui.screens.IslandWarpGui;
import com.fix3dll.skyblockaddons.gui.screens.LocationEditGui;
import com.fix3dll.skyblockaddons.gui.screens.SettingsGui;
import com.fix3dll.skyblockaddons.gui.screens.SkyblockAddonsGui;
import com.fix3dll.skyblockaddons.core.updater.Updater;
import com.fix3dll.skyblockaddons.mixin.hooks.FontHook;
import com.fix3dll.skyblockaddons.utils.ActionBarParser;
import com.fix3dll.skyblockaddons.utils.ColorUtils;
import com.fix3dll.skyblockaddons.utils.DrawUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils.AutoUpdateMode;
import com.fix3dll.skyblockaddons.utils.EnumUtils.DeployableDisplayStyle;
import com.fix3dll.skyblockaddons.utils.EnumUtils.PetItemStyle;
import com.fix3dll.skyblockaddons.utils.ItemUtils;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.SkyblockColor;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class RenderListener {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();
    public static final ResourceLocation SBA_RENDER_LAYER = SkyblockAddons.resourceLocation("hud_layer");

    private static final ItemStack BONE_ITEM = new ItemStack(Items.BONE);
    private static final ResourceLocation ARMOR = ResourceLocation.withDefaultNamespace("hud/armor_full");
    private static final ResourceLocation BARS = SkyblockAddons.resourceLocation("bars_v2.png");
    private static final ResourceLocation DEFENCE_VANILLA = SkyblockAddons.resourceLocation("defence.png");
    private static final ResourceLocation TICKER_SYMBOL = SkyblockAddons.resourceLocation("ticker.png");
    private static final ResourceLocation ENDERMAN_ICON = SkyblockAddons.resourceLocation("icons/enderman.png");
    private static final ResourceLocation ENDERMAN_GROUP_ICON = SkyblockAddons.resourceLocation("icons/endermangroup.png");
    private static final ResourceLocation SIRIUS_ICON = SkyblockAddons.resourceLocation("icons/sirius.png");
    private static final ResourceLocation SUMMONING_EYE_ICON = SkyblockAddons.resourceLocation("icons/summoningeye.png");
    private static final ResourceLocation ZEALOTS_PER_EYE_ICON = SkyblockAddons.resourceLocation("icons/zealotspereye.png");
    private static final ResourceLocation SLASH_ICON = SkyblockAddons.resourceLocation("icons/slash.png");
    private static final ResourceLocation IRON_GOLEM_ICON = SkyblockAddons.resourceLocation("icons/irongolem.png");
    private static final ResourceLocation FARM_ICON = SkyblockAddons.resourceLocation("icons/farm.png");
    private static final ResourceLocation RIFTSTALKER_BLOODFIEND = SkyblockAddons.resourceLocation("vampire.png");
    private static final ResourceLocation MORT_ICON = SkyblockAddons.resourceLocation("icons/mort.png");

    private static final ItemStack WATER_BUCKET = Items.WATER_BUCKET.getDefaultInstance();
    private static final ItemStack CHEST = Blocks.CHEST.asItem().getDefaultInstance();
    private static final SlayerArmorProgress[] DUMMY_PROGRESSES = new SlayerArmorProgress[] {
            new SlayerArmorProgress(new ItemStack(Items.DIAMOND_BOOTS)),
            new SlayerArmorProgress(new ItemStack(Items.CHAINMAIL_LEGGINGS)),
            new SlayerArmorProgress(new ItemStack(Items.DIAMOND_CHESTPLATE)),
            new SlayerArmorProgress(new ItemStack(Items.LEATHER_HELMET))
    };

    private static final ObjectArrayList<ItemDiff> DUMMY_PICKUP_LOG = ObjectArrayList.of(
            new ItemDiff(ColorCode.DARK_PURPLE + "Forceful Ember Chestplate", 1, new ItemStack(Items.CHAINMAIL_CHESTPLATE)),
            new ItemDiff("Oak Boat", -1, new ItemStack(Items.OAK_BOAT)),
            new ItemDiff(ColorCode.BLUE + "Aspect of the End", 1, new ItemStack(Items.DIAMOND_SWORD))
    );

    private static final Pattern DUNGEON_STAR_PATTERN = Pattern.compile("(?i)(?:(?:§[a-f0-9])?✪)+(?:§r)?(?:§[a-f0-9]?[➊-➒])?");

    private static Zombie revenant;
    private static Spider tarantula;
    private static CaveSpider caveSpider;
    private static Wolf sven;
    private static EnderMan enderman;
    private static Blaze inferno;
    private static RemotePlayer riftstalker;

    @Getter @Setter private boolean predictHealth;
    @Getter @Setter private boolean predictMana;

    @Setter private boolean updateMessageDisplayed;
    private ScheduledTask updateMessageDisplayTask;

    private Feature subtitleFeature;
    @Getter private Feature titleFeature;
    @Getter private ScheduledTask subtitleResetTask;
    @Getter private ScheduledTask titleResetTask;

    @Setter private int arrowsLeft = -1;
    @Setter private String arrowsType;

    @Setter private String cannotReachMobName;

    @Setter private long skillFadeOutTime = -1;
    @Setter private SkillType skill;
    @Setter private String skillText;

    @Setter private EnumUtils.GUIType guiToOpen;
    private int guiPageToOpen = 1;
    private EnumUtils.GuiTab guiTabToOpen = EnumUtils.GuiTab.MAIN;
    private Feature guiFeatureToOpen;

    @Setter private float maxRiftHealth = 0.0F;

    // caching
    private PetManager.Pet pet = null;
    private ItemStack petSkull = null;

    public RenderListener() {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerAfter(
                IdentifiedLayer.HOTBAR_AND_BARS,
                SBA_RENDER_LAYER,
                this::onRenderHud
        ));
        RenderEvents.LIVING_NAME.register(this::shouldRenderLivingName);
        WorldRenderEvents.LAST.register(this::onRenderWorld);
    }

    /**
     * Render overlays and warnings for clients.
     */
    public void onRenderHud(GuiGraphics graphics, DeltaTracker renderTickCounter) {
        if (main.getUtils().isOnSkyblock()) {
            renderOverlays(graphics);
            renderWarnings(graphics);
        } else {
            renderTimersOnly(graphics);
        }
        drawUpdateMessage(graphics);
        onRender();
    }

    private void shouldRenderLivingName(LivingEntity entity, double d, CallbackInfoReturnable<Boolean> cir) {
        Component customName = entity.getCustomName();
        if (customName != null) {
            String formattedText = TextUtils.getFormattedText(customName);
            if (Feature.MINION_DISABLE_LOCATION_WARNING.isEnabled()) {
                if (formattedText.startsWith("§cThis location isn't perfect! :(")) {
                    cir.cancel();
                    return;
                }
                if (customName.getString().startsWith("§c/!\\") && MC.level != null) {
                    for (Entity listEntity : MC.level.entitiesForRendering()) {
                        if (listEntity.hasCustomName()
                                && formattedText.startsWith("§cThis location isn't perfect! :(")
                                && listEntity.getX() == entity.getX()
                                && listEntity.getZ() == entity.getZ()
                                && listEntity.getY() + 0.375 == entity.getY()) {
                            cir.cancel();
                            break;
                        }
                    }
                }
            }

            if (Feature.HIDE_SVEN_PUP_NAMETAGS.isEnabled() && entity instanceof ArmorStand) {
                if (customName.getString().contains("Sven Pup")) {
                    cir.cancel();
                    return;
                }
            }
        }
    }

    /**
     * I have an option so you can see dark auction timer and farm event timer in other games so that's why.
     */
    private void renderTimersOnly(GuiGraphics graphics) {
        if (!(MC.screen instanceof LocationEditGui) /*&& !(MC.screen instanceof GuiNotification)*/) {
            if (Feature.DARK_AUCTION_TIMER.isEnabled(FeatureSetting.DARK_AUCTION_TIMER_IN_OTHER_GAMES)) {
                float scale = Feature.DARK_AUCTION_TIMER.getGuiScale();
                PoseStack poseStack = graphics.pose();
                poseStack.pushPose();
                poseStack.scale(scale, scale, 1);
                drawText(graphics, Feature.DARK_AUCTION_TIMER, scale, null);
                poseStack.popPose();
            }
            if (Feature.FARM_EVENT_TIMER.isEnabled(FeatureSetting.FARM_EVENT_TIMER_IN_OTHER_GAMES)) {
                float scale = Feature.FARM_EVENT_TIMER.getGuiScale();
                PoseStack poseStack = graphics.pose();
                poseStack.pushPose();
                poseStack.scale(scale, scale, 1);
                drawText(graphics, Feature.FARM_EVENT_TIMER, scale, null);
                poseStack.popPose();
            }
        }
    }

    /**
     * This renders all the title/subtitle warnings from features.
     */
    // TODO refactor
    private void renderWarnings(GuiGraphics graphics) {
        if (MC.level == null || MC.player == null || !main.getUtils().isOnSkyblock()) {
            return;
        }

        int scaledWidth = MC.getWindow().getGuiScaledWidth();
        int scaledHeight = MC.getWindow().getGuiScaledHeight();
        if (titleFeature != null) {
            String translationKey = switch (titleFeature) {
                case FULL_INVENTORY_WARNING -> "messages.fullInventory";
                case SUMMONING_EYE_ALERT -> "messages.summoningEyeFound";
                case SPECIAL_ZEALOT_ALERT -> "messages.specialZealotFound";
                case LEGENDARY_SEA_CREATURE_WARNING -> "messages.legendarySeaCreatureWarning";
                case BOSS_APPROACH_ALERT -> "messages.bossApproaching";
                case FETCHUR_TODAY -> "messages.fetchurWarning";
                case BROOD_MOTHER_ALERT -> "messages.broodMotherWarning";
                case BAL_BOSS_ALERT -> "messages.balBossWarning";
                default -> null;
            };
            if (translationKey != null) {
                String text = Translations.getMessage(translationKey);
                int stringWidth = MC.font.width(text);

                float scale = 4; // Scale is normally 4, but if it's larger than the screen, scale it down...
                if (stringWidth * scale > (scaledWidth * 0.9F)) {
                    scale = (scaledWidth * 0.9F) / (float) stringWidth;
                }

                PoseStack poseStack = graphics.pose();
                poseStack.pushPose();
                poseStack.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), 0.0F);
                poseStack.pushPose();
                poseStack.scale(scale, scale, scale); // TODO Check if changing this scale breaks anything...

//                //FontRendererHook.setupFeatureFont(titleFeature);
                DrawUtils.drawText(
                        graphics,
                        text,
                        -MC.font.width(text) / 2F,
                        -20.0F,
                        titleFeature.getColor()
                );

                poseStack.popPose();
                poseStack.popPose();
            }
        }
        if (subtitleFeature != null) {
            String text = switch (subtitleFeature) {
                case MINION_STOP_WARNING -> Translations.getMessage("messages.minionCannotReach", cannotReachMobName);
                case MINION_FULL_WARNING -> Translations.getMessage("messages.minionIsFull");
                case NO_ARROWS_LEFT_ALERT -> {
                    if (arrowsType != null) {
                        if (arrowsLeft != -1) {
                            yield Translations.getMessage("messages.onlyFewArrowsLeft", arrowsLeft, arrowsType);
                        } else {
                            yield Translations.getMessage("messages.noArrowsLeft", arrowsType);
                        }
                    }
                    yield null;
                }
                default -> null;
            };

            if (text != null) {

                int stringWidth = MC.font.width(text);

                float scale = 2; // Scale is normally 2, but if it's larger than the screen, scale it down...
                if (stringWidth * scale > (scaledWidth * 0.9F)) {
                    scale = (scaledWidth * 0.9F) / (float) stringWidth;
                }

                PoseStack poseStack = graphics.pose();
                poseStack.pushPose();
                poseStack.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), 0.0F);
                poseStack.pushPose();
                poseStack.scale(scale, scale, scale);  // TODO Check if changing this scale breaks anything...

//                //FontRendererHook.setupFeatureFont(subtitleFeature);
                DrawUtils.drawText(
                        graphics,
                        text,
                        -MC.font.width(text) / 2F,
                        -23.0F,
                        subtitleFeature.getColor()
                );

                poseStack.popPose();
                poseStack.popPose();
            }
        }
    }

    /**
     * This renders all the gui elements (bars, icons, texts, skeleton bar, etc.).
     */
    private void renderOverlays(GuiGraphics graphics) {
        if (!(MC.screen instanceof LocationEditGui) /*&& !(MC.screen instanceof GuiNotification)*/) {
            for (Feature feature : Feature.getGuiFeatures()) {
                if (feature.isEnabled()) {
                    if (feature == Feature.SKELETON_BAR && !main.getInventoryUtils().isWearingSkeletonHelmet())
                        continue;
                    if (feature == Feature.HEALTH_UPDATES && main.getPlayerListener().getActionBarParser().getHealthUpdate() == null)
                        continue;

                    drawFeature(graphics, feature, feature.getGuiScale(), null);
                }
            }
        }
    }

    public void drawFeature(GuiGraphics graphics, Feature feature, float scale, ButtonLocation buttonLocation) {
        FeatureGuiData guiFeatureData = feature.getFeatureGuiData();
        if (guiFeatureData != null && guiFeatureData.getDrawType() != null) {
            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            poseStack.scale(scale, scale, 1);
            switch (guiFeatureData.getDrawType()) {
                case SKELETON_BAR -> main.getRenderListener().drawSkeletonBar(graphics, scale, buttonLocation);
                case BAR -> main.getRenderListener().drawBar(graphics, feature, scale, buttonLocation);
                case TEXT -> main.getRenderListener().drawText(graphics, feature, scale, buttonLocation);
                case PICKUP_LOG -> main.getRenderListener().drawItemPickupLog(graphics, scale, buttonLocation);
                case DEFENCE_ICON -> main.getRenderListener().drawIcon(graphics, scale, buttonLocation);
                case SLAYER_ARMOR_PROGRESS -> main.getRenderListener().drawSlayerArmorProgress(graphics, scale, buttonLocation);
                case DEPLOYABLE_DISPLAY -> main.getRenderListener().drawDeployableStatus(graphics, scale, buttonLocation);
                case TICKER -> main.getRenderListener().drawScorpionFoilTicker(graphics, scale, buttonLocation);
                case BAIT_LIST_DISPLAY -> main.getRenderListener().drawBaitList(graphics, scale, buttonLocation);
                case DUNGEONS_MAP -> DungeonMapManager.drawDungeonsMap(graphics, scale, buttonLocation);
                case SLAYER_TRACKERS -> main.getRenderListener().drawSlayerTrackers(graphics, feature, scale, buttonLocation);
                case DRAGON_STATS_TRACKER -> main.getRenderListener().drawDragonTrackers(graphics, scale, buttonLocation);
                case PROXIMITY_INDICATOR -> TrevorTrapperTracker.drawTrackerLocationIndicator(graphics, scale, buttonLocation);
                case PET_DISPLAY -> drawPetDisplay(graphics, scale, buttonLocation);
            }
            poseStack.popPose();
        }
    }

    /**
     * This draws all Skyblock Addons Bars, including the Health, Mana, Drill, and Skill XP bars
     *
     * @param feature        for which to render the bars
     * @param scale          the scale of the feature
     * @param buttonLocation the resizing gui, if present
     */
    public void drawBar(GuiGraphics graphics, Feature feature, float scale, ButtonLocation buttonLocation) {
        // The fill of the bar from 0 to 1
        float fill;
        // Whether the player has absorption hearts
        boolean hasAbsorption = false;
        // Float value to scale width
        float widthScale = 1.0F;

        switch (feature) {
            case MANA_BAR:
                fill = PlayerStat.MANA.getValue() / PlayerStat.MAX_MANA.getValue();
                break;
            case DRILL_FUEL_BAR:
                fill = PlayerStat.FUEL.getValue() / PlayerStat.MAX_FUEL.getValue();
                break;
            case SKILL_PROGRESS_BAR:
                if (buttonLocation == null) {
                    ActionBarParser parser = main.getPlayerListener().getActionBarParser();
                    if (parser.getPercent() == 0 || parser.getPercent() == 100) {
                        return;
                    } else {
                        fill = parser.getPercent() / 100;
                    }
                } else {
                    fill = 0.40F;
                }
                break;
            case HEALTH_BAR:
                if (Feature.HEALTH_BAR.isEnabled(FeatureSetting.HIDE_HEALTH_BAR_ON_RIFT) && main.getUtils().isOnRift())
                    return;
                fill = PlayerStat.HEALTH.getValue() / PlayerStat.MAX_HEALTH.getValue();
                break;
            case PRESSURE_BAR:
                float pressure = buttonLocation != null ? 50 : PlayerStat.PRESSURE.getValue();
                if (pressure == -1) return;
                fill = pressure / 100.0F;
                break;
            default:
                return;
        }

        if (fill > 1) fill = 1;

        float x = feature.getActualX();
        float y = feature.getActualY();
        float scaleX = feature.getFeatureData().getSizesX();
        float scaleY = feature.getFeatureData().getSizesY();
        graphics.pose().scale(scaleX, scaleY, 1);

        x = transformX(x, 71, scale * scaleX, false);
        y = transformY(y, 5, scale * scaleY);

        // Render the button resize box if necessary
        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + 71, y, y + 5, scale, scaleX, scaleY);
        }

        SkyblockColor color = ColorUtils.getDummySkyblockColor(feature.getColor(), feature.isChroma());

        switch (feature) {
            case SKILL_PROGRESS_BAR:
                if (buttonLocation != null) break;

                int remainingTime = (int) (skillFadeOutTime - System.currentTimeMillis());

                if (remainingTime < 0) {
                    if (remainingTime < -2000) {
                        return; // Will be invisible, no need to render.
                    }

                    int textAlpha = Math.round(255 - (-remainingTime / 2000F * 255F));
                    color = ColorUtils.getDummySkyblockColor(
                            feature.getColor(textAlpha),
                            feature.isChroma()
                    ); // so it fades out, 0.016 is the minimum alpha
                }
                break;
            case DRILL_FUEL_BAR:
                if (MC.player == null) return;
                if (buttonLocation == null && !ItemUtils.isDrill(MC.player.getMainHandItem())) return;
                break;
            case HEALTH_BAR:
                if (feature.isEnabled(FeatureSetting.CHANGE_BAR_COLOR_WITH_POTIONS) && MC.player != null) {
                    if (MC.player.hasEffect(MobEffects.POISON)) {
                        color = ColorUtils.getDummySkyblockColor(ColorCode.DARK_GREEN.getColor(), feature.isChroma());
                    } else if (MC.player.hasEffect(MobEffects.WITHER)) {
                        color = ColorUtils.getDummySkyblockColor(ColorCode.DARK_GRAY.getColor(), feature.isChroma());
                    } else if (MC.player.hasEffect(MobEffects.ABSORPTION)) {
                        if (PlayerStat.HEALTH.getValue() > PlayerStat.MAX_HEALTH.getValue()) {
                            fill = PlayerStat.MAX_HEALTH.getValue() / PlayerStat.HEALTH.getValue();
                            hasAbsorption = true;
                        }
                    }
                }

                if (main.getUtils().isOnRift()) {
                    float maxCurrentHealth = PlayerStat.MAX_RIFT_HEALTH.getValue();
                    fill = PlayerStat.HEALTH.getValue() / maxCurrentHealth;

                    if (maxCurrentHealth > maxRiftHealth) {
                        maxRiftHealth = maxCurrentHealth;
                    } else {
                        widthScale = maxCurrentHealth / maxRiftHealth;
                    }

                    if (Float.isNaN(widthScale)) {
                        widthScale = 1.0F;
                    }
                }
                break;
            case PRESSURE_BAR:
                if (Feature.PRESSURE_BAR.isEnabled(FeatureSetting.PRESSURE_BAR_ALERT)) {
                    float pressure = PlayerStat.PRESSURE.getValue();
                    if (pressure >= 90.0F && main.getScheduler().getTotalTicks() % 40 >= 20) {
                        color = ColorUtils.getDummySkyblockColor(ColorCode.RED.getColor(), feature.isChroma());
                    }
                }
                break;
        }

        // Draw the actual bar
        drawMultiLayeredBar(graphics, color, x, y, fill, hasAbsorption, widthScale);
    }

    /**
     * Draws a multitextured bar:
     * Begins by coloring and rendering the empty bar.
     * Then, colors and renders the full bar up to the fraction {@param fill}.
     * Then, overlays the absorption portion of the bar in gold if the player has absorption hearts
     * Then, overlays (and does not skyblockColor) an additional texture centered on the current progress of the bar.
     * Then, overlays (and does not skyblockColor) a final style texture over the bar
     * @param skyblockColor the skyblockColor with which to render the bar
     * @param x the x position of the bar
     * @param y the y position of the bar
     * @param fill the fraction (from 0 to 1) of the bar that's full
     * @param hasAbsorption {@code true} if the player has absorption hearts
     */
    private void drawMultiLayeredBar(GuiGraphics graphics, SkyblockColor skyblockColor, float x, float y, float fill, boolean hasAbsorption, float widthScale) {
        int barHeight = 5;
        float barWidth = 71 * widthScale;
        float barFill = barWidth * fill;
        int color;
        if (skyblockColor.getColor() == ColorCode.BLACK.getColor()) { // too dark normally
            color = ARGB.colorFromFloat(ARGB.alpha(skyblockColor.getColor()) / 255F, 0.25F, 0.25F, 0.25F);
        } else { // A little darker for contrast...
            color = ARGB.color(230 , skyblockColor.getColor());
        }
        RenderType renderType;
        // If chroma, draw the empty bar much darker than the filled bar
        if (skyblockColor.drawMulticolorUsingShader()) {
            color = ARGB.colorFromFloat(1F, 0.5F, 0.5F, 0.5F);
            renderType = FontHook.getChromaTextured(BARS);
        } else {
            renderType = RenderType.guiTextured(BARS);
        }

        // Empty bar first
        int emptyBarColor = color;
        graphics.drawSpecial(source -> DrawUtils.blitAbsolute(graphics.pose(), source, renderType, x, y, 1, 1, barWidth, barHeight, 80, 50, emptyBarColor));

        if (skyblockColor.drawMulticolorUsingShader()) {
            color = ARGB.white(1F);
        }

        int finalColor = color;

        // Filled bar next
        if (fill != 0) {
            graphics.drawSpecial(source -> DrawUtils.blitAbsolute(graphics.pose(), source, renderType, x, y, 1, 7, barFill, barHeight, 80, 50, finalColor));
        }

        // Overlay absorption health if needed
        if (hasAbsorption) {
            graphics.drawSpecial(source -> DrawUtils.blitAbsolute(graphics.pose(), source, BARS, x + barFill, y, 1 + barFill, 7, barWidth - barFill, barHeight, 80, 50, ColorCode.GOLD.getColor()));
        }

        // Overlay uncolored progress indicator next (texture packs can use this to overlay their own static bar colors)
        if (fill > 0 && fill < 1) {
            // Make sure that the overlay doesn't go outside the bounds of the bar.
            // It's 4 pixels wide, so ensure we only render the texture between 0 <= x <= barWidth
            // Start rendering at x => 0 (for small fill values, also don't render before the bar starts)
            // Adding padding ensures that no green bar gets rendered from the texture...?
            float padding = .01F;
            float oneSide = 2 - padding;
            float startX = Math.max(0, barFill - oneSide);
            // Start texture at x >= 0 (for small fill values, also start the texture so indicator is always centered)
            float startTexX = Math.max(padding, oneSide - barFill);
            // End texture at x <= barWidth and 4 <= startTexX + endTexX (total width of overlay texture). Cut off for large fill values.
            float endTexX = Math.min(2 * oneSide - startTexX, barWidth - barFill + oneSide);
            graphics.drawSpecial(source -> DrawUtils.blitAbsolute(graphics.pose(), source, BARS, x + startX, y, 1 + startTexX, 24, endTexX, barHeight, 80, 50, finalColor));
        }
        // Overlay uncolored bar display next (texture packs can use this to overlay their own static bar colors)
        graphics.drawSpecial(source -> DrawUtils.blitAbsolute(graphics.pose(), source, BARS, x, y, 1, 13, barWidth, barHeight, 80, 50, finalColor));
    }

    /**
     * Renders the messages from the SkyblockAddons Updater
     */
    private void drawUpdateMessage(GuiGraphics graphics) {
        Updater updater = main.getUpdater();

        if (updater.hasUpdate() && !updateMessageDisplayed) {
            String message = updater.getMessageToRender();

            if (message != null && Feature.AUTO_UPDATE.getValue() == AutoUpdateMode.UPDATE_OFF) {
                String[] textList = main.getUtils().wrapSplitText(message, 36);

                int halfWidth = MC.getWindow().getGuiScaledWidth() / 2;
                graphics.fill(
                        halfWidth - 110,
                        20,
                        halfWidth + 110,
                        53 + textList.length * 10,
                        ColorUtils.getDefaultBlue(140)
                );
                String title = SkyblockAddons.METADATA.getName();
                PoseStack poseStack = graphics.pose();
                poseStack.pushPose();
                float scale = 1.5F;
                poseStack.scale(scale, scale, 1);
                graphics.drawCenteredString(MC.font, Component.literal(title), (int) (halfWidth / scale), (int) (30 / scale), ColorCode.WHITE.getColor());
                poseStack.popPose();
                int y = 45;
                for (String line : textList) {
                    graphics.drawCenteredString(MC.font, Component.literal(line), halfWidth, y, ColorCode.WHITE.getColor());
                    y += 10;
                }
            }

            if (!main.getUpdater().hasSentUpdateMessage()) {
                main.getUpdater().sendUpdateMessage();
            }

            if (updateMessageDisplayTask == null) {
                updateMessageDisplayTask = main.getScheduler().scheduleTask(scheduledTask -> {
                    main.getRenderListener().setUpdateMessageDisplayed(true);
                    updateMessageDisplayTask = null;
                }, 10 * 20);
            }
        }
    }

    /**
     * This renders a bar for the skeleton hat bones bar.
     */
    public void drawSkeletonBar(GuiGraphics graphics, float scale, ButtonLocation buttonLocation) {
        float x = Feature.SKELETON_BAR.getActualX();
        float y = Feature.SKELETON_BAR.getActualY();
        int bones;
        if (!(MC.screen instanceof LocationEditGui) && MC.level != null && MC.player != null) {
             List<ItemEntity> bonesEntityList = MC.level.getEntitiesOfClass(
                    ItemEntity.class,
                    MC.player.getBoundingBox().inflate(8),
                    itemEntity -> itemEntity.isPassenger()
                            && itemEntity.getVehicle() instanceof ArmorStand vehicle
                            && vehicle.isInvisible()
             );
             bones = bonesEntityList.size();
        } else {
            bones = 3;
        }
        if (bones > 3) bones = 3;

        int height = 16;
        int width = 3 * 16;

        x = transformX(x, width, scale, false);
        y = transformY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + height, scale);
        }

        for (int boneCounter = 0; boneCounter < bones; boneCounter++) {
            renderItem(graphics, BONE_ITEM, x + boneCounter * 16, y);
        }

    }

    /**
     * This renders the skeleton bar.
     */
    public void drawScorpionFoilTicker(GuiGraphics graphics, float scale, ButtonLocation buttonLocation) {
        if (buttonLocation != null || main.getPlayerListener().getTickers() != -1) {
            float x = Feature.TICKER_CHARGES_DISPLAY.getActualX();
            float y = Feature.TICKER_CHARGES_DISPLAY.getActualY();

            int height = 9;
            int width = 3 * 11 + 9;

            x = transformX(x, width, scale, false);
            y = transformY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + height, scale);
            }

            int maxTickers = (buttonLocation == null) ? main.getPlayerListener().getMaxTickers() : 4;
            for (int tickers = 0; tickers < maxTickers; tickers++) {
//                MC.getTextureManager().bindTexture(TICKER_SYMBOL);
//                GlStateManager.enableAlpha();
                float finalX = x + tickers * 11;
                float finalY = y;

                if (tickers < (buttonLocation == null ? main.getPlayerListener().getTickers() : 3)) {
                    graphics.drawSpecial(source -> DrawUtils.blitAbsolute(graphics.pose(), source, TICKER_SYMBOL, finalX, finalY, 0, 0, 9, 9, 18, 9, -1));
                } else {
                    graphics.drawSpecial(source -> DrawUtils.blitAbsolute(graphics.pose(), source, TICKER_SYMBOL, finalX, finalY, 9, 0, 9, 9, 18, 9, -1));
                }
            }
        }
    }

    /**
     * This renders the defence icon.
     */
    public void drawIcon(GuiGraphics graphics, float scale, ButtonLocation buttonLocation) {
        // There is no defense stat on Rift Dimension
        if (main.getUtils().isOnRift()) return;

        // The height and width of this element (box not included)
        float x = Feature.DEFENCE_ICON.getActualX();
        float y = Feature.DEFENCE_ICON.getActualY();
        x = transformX(x, 9, scale, false);
        y = transformY(y, 9, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + 9, y, y + 9, scale);
        }
        if (Feature.DEFENCE_ICON.isEnabled(FeatureSetting.USE_VANILLA_TEXTURE)) {
            final float finalX = x, finalY = y;
            graphics.drawSpecial(source -> DrawUtils.blitAbsolute(graphics.pose(), source, DEFENCE_VANILLA, finalX, finalY, 0, 0, 9, 9, 9, 9, -1));
        } else {
            graphics.blitSprite(RenderType::guiTextured, ARMOR, (int) x, (int) y, 9, 9);
        }
    }

    /**
     * This renders all the different types gui text elements.
     */
    @SuppressWarnings("RedundantLabeledSwitchRuleCodeBlock")
    public void drawText(GuiGraphics graphics, Feature feature, float scale, ButtonLocation buttonLocation) {
        String text;
        boolean onRift = main.getUtils().isOnRift();
        int color = feature.getColor();

        switch (feature) {
            case MANA_TEXT -> {
                text = TextUtils.formatNumber(PlayerStat.MANA.getValue()) + "/"
                        + TextUtils.formatNumber(PlayerStat.MAX_MANA.getValue())
                        + (feature.isEnabled(FeatureSetting.MANA_TEXT_ICON) ? "✎" : "");
            }
            case OVERFLOW_MANA -> {
                if (PlayerStat.OVERFLOW_MANA.getValue() == 0 && buttonLocation == null) return;
                text = TextUtils.formatNumber(PlayerStat.OVERFLOW_MANA.getValue()) + "ʬ";
            }
            case HEALTH_TEXT -> {
                if (feature.isEnabled(FeatureSetting.HIDE_HEALTH_TEXT_ON_RIFT) && onRift) return;

                // Dividing with 2 for show heart value instead of health value. 1 heart == 2 health
                boolean shouldHeart = feature.isEnabled(FeatureSetting.HEART_INSTEAD_HEALTH_ON_RIFT) && onRift;

                text = TextUtils.formatNumber(PlayerStat.HEALTH.getValue() / (shouldHeart ? 2F : 1F)) + "/";
                if (main.getUtils().isOnRift()) {
                    text += TextUtils.formatNumber(PlayerStat.MAX_RIFT_HEALTH.getValue() / (shouldHeart ? 2F : 1F));
                } else {
                    text += TextUtils.formatNumber(PlayerStat.MAX_HEALTH.getValue());
                }
                if (feature.isEnabled(FeatureSetting.HEALTH_TEXT_ICON)) {
                    text += "❤";
                }

            }
            case CRIMSON_ARMOR_ABILITY_STACKS -> {
                if (buttonLocation != null) {
                    text = "Hydra Strike ⁑ 1";
                } else {
                    text = getCrimsonArmorAbilityStacks();
                }
                if (text == null) return;
            }
            case DEFENCE_TEXT -> {
                if (onRift) return;
                text = TextUtils.formatNumber(PlayerStat.DEFENCE.getValue())
                        + (feature.isEnabled(FeatureSetting.DEFENCE_TEXT_ICON) ? "❈" : "");
            }
            case OTHER_DEFENCE_STATS -> {
                text = main.getPlayerListener().getActionBarParser().getOtherDefense();
                if (text == null || text.isEmpty()) {
                    if (buttonLocation != null) {
                        text = "|||  T3!";
                    } else {
                        return;
                    }
                }
            }
            case EFFECTIVE_HEALTH_TEXT -> {
                if (onRift) return;
                text = TextUtils.formatNumber(
                        Math.round(PlayerStat.HEALTH.getValue() * (1 + PlayerStat.DEFENCE.getValue() / 100F))
                ) + (feature.isEnabled(FeatureSetting.EFFECTIVE_HEALTH_TEXT_ICON) ? "❤" : "");
            }
            case DRILL_FUEL_TEXT -> {
                boolean heldDrill = MC.player != null && ItemUtils.isDrill(MC.player.getMainHandItem());

                if (heldDrill || buttonLocation != null) {
                    float fuel = heldDrill ? PlayerStat.FUEL.getValue() : 3000;
                    float maxFuel = heldDrill ? PlayerStat.MAX_FUEL.getValue(): 3000;
                    text = TextUtils.formatNumber(fuel) + "/";
                    if (feature.isEnabled(FeatureSetting.ABBREVIATE_DRILL_FUEL_DENOMINATOR)) {
                        text += TextUtils.abbreviate(maxFuel);
                    } else {
                        text += TextUtils.formatNumber(maxFuel);
                    }
                } else {
                    return;
                }
            }
            case DEFENCE_PERCENTAGE -> {
                if (onRift) return;
                double doubleDefence = PlayerStat.DEFENCE.getValue();
                double percentage = doubleDefence / (doubleDefence + 100) * 100; //Taken from https://wiki.hypixel.net/Defense
                BigDecimal bigDecimal = new BigDecimal(percentage).setScale(1, RoundingMode.HALF_UP);
                text = bigDecimal + "%";
            }
            case SPEED_PERCENTAGE -> {
                if (MC.player != null) {
                    // 0.3xyz -> 3xy.z -> 3xy
                    int walkSpeed = (int) (MC.player.getAbilities().getWalkingSpeed() * 1000);
                    text = walkSpeed + "%";
                } else /* Dummy */ {
                    text = "123%";
                }
            }
            case HEALTH_UPDATES -> {
                if (feature.isEnabled(FeatureSetting.HIDE_HEALTH_UPDATES_ON_RIFT) && main.getUtils().isOnRift())
                    return;
                Float healthUpdate = main.getPlayerListener().getActionBarParser().getHealthUpdate();
                if (buttonLocation == null) {
                    if (healthUpdate != null) {
                        color = healthUpdate > 0 ? ColorCode.GREEN.getColor() : ColorCode.RED.getColor();
                        text = (healthUpdate > 0 ? "+" : "-") + TextUtils.formatNumber(Math.abs(healthUpdate));
                    } else {
                        return;
                    }
                } else {
                    text = "+123";
                    color = ColorCode.GREEN.getColor();
                }
            }
            case DARK_AUCTION_TIMER -> {
                // The timezone of the server, to avoid problems with like timezones that are 30 minutes ahead or whatnot.
                ZonedDateTime nowDA = SkyblockAddons.getHypixelZonedDateTime();
                ZonedDateTime nextDarkAuction = nowDA.withMinute(55).withSecond(0);
                if (nowDA.getMinute() >= 55) {
                    nextDarkAuction = nextDarkAuction.plusHours(1);
                }
                Duration diffDA = Duration.between(nowDA, nextDarkAuction);
                text = String.format("%02d:%02d", diffDA.toMinutes(), diffDA.getSeconds() % 60);
            }
            case FARM_EVENT_TIMER -> {
                // The timezone of the server, to avoid problems with like timezones that are 30 minutes ahead or whatnot.
                ZonedDateTime nowFE = SkyblockAddons.getHypixelZonedDateTime();
                ZonedDateTime nextFarmEvent = nowFE.withMinute(15).withSecond(0);
                if (nowFE.getMinute() >= 15) {
                    nextFarmEvent = nextFarmEvent.plusHours(1);
                }
                Duration diffFE = Duration.between(nowFE, nextFarmEvent);
                long minutesFE = diffFE.toMinutes();
                if (minutesFE < 40) {
                    text = String.format("%02d:%02d", minutesFE, diffFE.getSeconds() % 60);
                } else {
                    text = String.format("Active: %02d:%02d", minutesFE - 40, diffFE.getSeconds() % 60);
                }
            }
            case SKILL_DISPLAY -> {
                if (buttonLocation == null) {
                    text = skillText;
                    if (text == null) return;
                } else {
                    StringBuilder previewBuilder = new StringBuilder();
                    if (feature.isEnabled(FeatureSetting.SHOW_SKILL_XP_GAINED)) {
                        previewBuilder.append("+123 ");
                    }
                    if (feature.isEnabled(FeatureSetting.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP)) {
                        previewBuilder.append("40% ");
                    } else {
                        previewBuilder.append("(2000/5000) ");
                    }
                    if (feature.isEnabled(FeatureSetting.SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL)) {
                        previewBuilder.append(" - ")
                                .append(Translations.getMessage("messages.actionsLeft", 3000))
                                .append(" ");
                    }
                    previewBuilder.setLength(previewBuilder.length() - 1);
                    text = previewBuilder.toString();
                }
                if (buttonLocation == null) {
                    int remainingTime = (int) (skillFadeOutTime - System.currentTimeMillis());

                    if (remainingTime < 0) {
                        if (remainingTime < -1968) {
                            return; // Will be invisible, no need to render.
                        }

                        int textAlpha = Math.round(255 - (-remainingTime / 2000F * 255F));
                        color = feature.getColor(textAlpha); // so it fades out, 0.016 is the minimum alpha
                    }
                }
            }
            case ZEALOT_COUNTER -> {
                if (feature.isEnabled(FeatureSetting.COUNTER_ZEALOT_SPAWN_AREAS_ONLY) &&
                        !LocationUtils.isOnZealotSpawnLocation() && buttonLocation == null) {
                    return;
                }
                text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getKills());
            }
            case SHOW_TOTAL_ZEALOT_COUNT -> {
                if (feature.isEnabled(FeatureSetting.TOTAL_ZEALOT_SPAWN_AREAS_ONLY) &&
                        !LocationUtils.isOnZealotSpawnLocation() && buttonLocation == null) {
                    return;
                }
                if (main.getPersistentValuesManager().getPersistentValues().getTotalKills() <= 0) {
                    text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getKills());
                } else {
                    text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getTotalKills()
                            + main.getPersistentValuesManager().getPersistentValues().getKills());
                }
            }
            case SHOW_SUMMONING_EYE_COUNT -> {
                if (feature.isEnabled(FeatureSetting.EYE_ZEALOT_SPAWN_AREAS_ONLY) &&
                        !LocationUtils.isOnZealotSpawnLocation() && buttonLocation == null) {
                    return;
                }
                text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount());
            }
            case SHOW_AVERAGE_ZEALOTS_PER_EYE -> {
                if (feature.isEnabled(FeatureSetting.AVERAGE_ZEALOT_SPAWN_AREAS_ONLY) &&
                        !LocationUtils.isOnZealotSpawnLocation() && buttonLocation == null) {
                    return;
                }
                int summoningEyeCount = main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount();

                if (summoningEyeCount > 0) {
                    text = TextUtils.formatNumber(Math.round(main.getPersistentValuesManager().getPersistentValues().getTotalKills()
                            / (double) main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount()));
                } else {
                    text = "0"; // Avoid zero division.
                }
            }
            case BIRCH_PARK_RAINMAKER_TIMER -> {
                long rainmakerTime = main.getPlayerListener().getRainmakerTimeEnd();

                if (!LocationUtils.isOn("Birch Park") && buttonLocation == null) return;

                String parsedRainTime = TabListParser.getParsedRainTime();

                if (parsedRainTime != null) {
                    text = parsedRainTime;
                } else if (rainmakerTime != -1) {
                    int totalSeconds = (int) (rainmakerTime - System.currentTimeMillis()) / 1000;
                    if (totalSeconds <= 0) return;

                    StringBuilder timerBuilder = new StringBuilder();

                    int hours = totalSeconds / 3600;
                    int minutes = totalSeconds / 60 % 60;
                    int seconds = totalSeconds % 60;

                    if (hours > 0) {
                        timerBuilder.append(hours).append(":");
                    }
                    if (minutes < 10 && hours > 0) {
                        timerBuilder.append("0");
                    }
                    timerBuilder.append(minutes).append(":");
                    if (seconds < 10) {
                        timerBuilder.append("0");
                    }
                    timerBuilder.append(seconds);

                    text = timerBuilder.toString();
                } else {
                    if (buttonLocation == null) return;
                    text = "1:23";
                }
            }
            case ENDSTONE_PROTECTOR_DISPLAY -> {
                if ((!LocationUtils.isOn(Island.THE_END)
                        || EndstoneProtectorManager.getMinibossStage() == null
                        || !EndstoneProtectorManager.isCanDetectSkull()
                ) && buttonLocation == null) return;

                EndstoneProtectorManager.Stage stage = EndstoneProtectorManager.getMinibossStage();

                if (buttonLocation != null && stage == null)
                    stage = EndstoneProtectorManager.Stage.STAGE_3;

                int stageNum = Math.min(stage.ordinal(), 5);
                text = Translations.getMessage("messages.stage", String.valueOf(stageNum));
            }
            case SHOW_DUNGEON_MILESTONE -> {
                if (buttonLocation == null && !main.getUtils().isInDungeon()) return;

                DungeonMilestone dungeonMilestone = main.getDungeonManager().getDungeonMilestone();
                if (dungeonMilestone == null) {
                    if (buttonLocation != null) dungeonMilestone = new DungeonMilestone(DungeonClass.HEALER);
                    else return;
                }
                text = "Milestone " + dungeonMilestone.getLevel();
            }
            case DUNGEONS_COLLECTED_ESSENCES_DISPLAY -> {
                if (buttonLocation == null && !main.getUtils().isInDungeon()) return;
                text = "";
            }
            case DUNGEON_DEATH_COUNTER -> {
                if (buttonLocation == null && !main.getUtils().isInDungeon()) return;
                text = Integer.toString(main.getDungeonManager().getDeathCount());
            }
            case ROCK_PET_TRACKER -> {
                if (buttonLocation == null && feature.isEnabled(FeatureSetting.SHOW_ONLY_HOLDING_MINING_TOOL)
                        && !main.getPlayerListener().isHoldingMiningTool()) {
                    return;
                }
                text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getOresMined());
            }
            case DOLPHIN_PET_TRACKER -> {
                if (buttonLocation == null && feature.isEnabled(FeatureSetting.SHOW_ONLY_HOLDING_FISHING_ROD)
                        && !main.getPlayerListener().isHoldingRod()) {
                    return;
                }
                text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getSeaCreaturesKilled());
            }
            case DUNGEONS_SECRETS_DISPLAY -> {
                if (buttonLocation == null && !main.getUtils().isInDungeon()) return;
                text = "Secrets";
            }
            case SPIRIT_SCEPTRE_DISPLAY -> {
                if (MC.player == null) return;
                if (buttonLocation != null) {
                    text = "§6Hyperion";
                    break;
                }

                ItemStack holdingItem = MC.player.getMainHandItem();
                String skyblockItemID = ItemUtils.getSkyblockItemID(holdingItem);

                if (holdingItem == ItemStack.EMPTY || skyblockItemID == null) {
                    return;
                } else if (DamageDisplayItem.getByID(skyblockItemID) != null) {
                    text = DUNGEON_STAR_PATTERN.matcher(
                            TextUtils.getFormattedText(holdingItem.getCustomName())
                    ).replaceFirst("");
                } else {
                    return;
                }
            }
            case CANDY_POINTS_COUNTER -> {
                if (buttonLocation == null && !SpookyEventManager.isActive()) return;
                text = "Test";
            }
            case FETCHUR_TODAY -> {
                FetchurManager.FetchurItem fetchurItem = FetchurManager.getInstance().getCurrentFetchurItem();
                if (!FetchurManager.getInstance().hasFetchedToday() || buttonLocation != null) {
                    if (feature.isEnabled(FeatureSetting.SHOW_FETCHUR_ITEM_NAME)) {
                        text = Translations.getMessage(
                                "messages.fetchurItem",
                                fetchurItem.itemStack().getCount() + "x " + fetchurItem.itemText()
                        );
                    } else {
                        text = Translations.getMessage("messages.fetchurItem", "");
                    }
                } else {
                    text = ""; // If it has made fetchur, then no need for text
                }
            }
            case FIRE_FREEZE_TIMER -> {
                if (buttonLocation == null && !main.getUtils().isInDungeon()) return;

                if (buttonLocation != null) {
                    text = "Fire Freeze in 5,00";
                } else {
                    if (feature.isEnabled(FeatureSetting.FIRE_FREEZE_WHEN_HOLDING) && !main.getPlayerListener().isHoldingFireFreeze())
                        return;

                    long fireFreezeTimer = main.getPlayerListener().getFireFreezeTimer();
                    if (fireFreezeTimer == 0) return;

                    double countdown = (fireFreezeTimer - System.currentTimeMillis()) / 1000D;

                    if (countdown > 0) {
                        text = String.format("Fire Freeze in %.2f", countdown);
                    } else {
                        if (feature.isEnabled(FeatureSetting.FIRE_FREEZE_SOUND)) {
                            main.getUtils().playLoudSound(SoundEvents.WITHER_SPAWN, 1);
                        }
                        main.getPlayerListener().setFireFreezeTimer(0);
                        return;
                    }
                }
            }
            case THUNDER_BOTTLE_DISPLAY -> {
                ThunderBottle displayBottle = ThunderBottle.getDisplayBottle();

                if (buttonLocation == null && displayBottle == null) {
                    return;
                }

                if (displayBottle != null) {
                    if (displayBottle.isFull()) {
                        text = "§aFull!";
                    } else {
                        final String capacity = feature.isEnabled(FeatureSetting.ABBREVIATE_THUNDER_DISPLAYS_DENOMINATOR)
                                ? TextUtils.abbreviate(displayBottle.getCapacity())
                                : TextUtils.formatNumber(displayBottle.getCapacity());
                        text = TextUtils.formatNumber(displayBottle.getCharge()) + "/" + capacity;
                    }
                } else /*buttonLocation != null*/ {
                    final String capacity = feature.isEnabled(FeatureSetting.ABBREVIATE_THUNDER_DISPLAYS_DENOMINATOR)
                            ? TextUtils.abbreviate(50000)
                            : TextUtils.formatNumber(50000);
                    text = TextUtils.formatNumber(42440) + "/" + capacity;
                }
            }
            case PRESSURE_TEXT -> {
                float pressure = buttonLocation != null ? 50.0F : PlayerStat.PRESSURE.getValue();
                if (pressure == -1) return;
                boolean icon = feature.isEnabled(FeatureSetting.PRESSURE_TEXT_ICON);
                boolean lastRemembered = main.getPlayerListener().getActionBarParser().isUseLastRememberedPressure();
                text = (icon ? "❍" : "") + (lastRemembered ? "~" : "") + TextUtils.formatNumber(pressure) + "%";
            }
            default -> {
                return;
            }
        }

        float x = feature.getActualX();
        float y = feature.getActualY();

        int height = 7;
        int width = MC.font.width(text);

        switch (feature) {
            case ZEALOT_COUNTER:
            case SHOW_AVERAGE_ZEALOTS_PER_EYE:
            case SHOW_TOTAL_ZEALOT_COUNT:
            case SHOW_SUMMONING_EYE_COUNT:
                width = MC.font.width(text) + 18;
                height += 9;
                break;

            case ENDSTONE_PROTECTOR_DISPLAY:
                width += 18 + 2 + 16 + 2 + MC.font.width(
                        String.valueOf(EndstoneProtectorManager.getZealotCount())
                );
                height += 9;
                break;

            case SHOW_DUNGEON_MILESTONE:
                width += 16 + 2;
                height += 10;
                break;

            case DUNGEONS_SECRETS_DISPLAY:
                width += 16 + 2;
                height += 12;
                break;

            case FETCHUR_TODAY:
                if (feature.isDisabled(FeatureSetting.SHOW_FETCHUR_ITEM_NAME)) {
                    width += 18;
                    height += 9;
                }
                break;

            case DUNGEONS_COLLECTED_ESSENCES_DISPLAY:
                int maxNumberWidth = MC.font.width("99");
                width = 18 + 2 + maxNumberWidth + 5 + 18 + 2 + maxNumberWidth;
                height = 18 * (int) Math.ceil(EssenceType.values().length / 2F);
                break;

            case SPIRIT_SCEPTRE_DISPLAY:
                width += 18 + MC.font.width("12345");
                height += 20;
                break;

            case CANDY_POINTS_COUNTER:
                width = 0;

                Map<CandyType, Integer> candyCounts = SpookyEventManager.getCandyCounts();
                if (!SpookyEventManager.isActive()) {
                    if (buttonLocation == null) return;
                    else candyCounts = SpookyEventManager.getDummyCandyCounts();
                }

                int green = candyCounts.get(CandyType.GREEN);
                int purple = candyCounts.get(CandyType.PURPLE);
                if (buttonLocation != null || green > 0) {
                    width += 16 + 1 + MC.font.width(TextUtils.formatNumber(green));
                }
                if (buttonLocation != null || purple > 0) {
                    if (green > 0) width += 1;
                    width += 16 + 1 + MC.font.width(TextUtils.formatNumber(purple)) + 1;
                }
                height = 16 + 8;
                break;

            case DARK_AUCTION_TIMER:
            case FARM_EVENT_TIMER:
            case SKILL_DISPLAY:
            case BIRCH_PARK_RAINMAKER_TIMER:
            case DUNGEON_DEATH_COUNTER:
            case DOLPHIN_PET_TRACKER:
            case FIRE_FREEZE_TIMER:
            case THUNDER_BOTTLE_DISPLAY:
            case ROCK_PET_TRACKER:
                width += 18;
                height += 9;
                break;
        }

        x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
        y = transformY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + height, scale);
        }

        PoseStack poseStack = graphics.pose();
        final float fX = x, fY = y;
        switch (feature) {
            case DARK_AUCTION_TIMER -> {
                graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, SIRIUS_ICON, fX, fY, 0, 0, 16, 16, 16, 16, -1));

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

            }
            case FARM_EVENT_TIMER -> {
                graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, FARM_ICON, fX, fY, 0, 0, 16, 16, 16, 16, -1));

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

            }
            case ZEALOT_COUNTER -> {
                graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, ENDERMAN_ICON, fX, fY, 0, 0, 16, 16, 16, 16, -1));

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

            }
            case SHOW_TOTAL_ZEALOT_COUNT -> {
                graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, ENDERMAN_GROUP_ICON, fX, fY, 0, 0, 16, 16, 16, 16, -1));

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

            }
            case SHOW_SUMMONING_EYE_COUNT -> {
                graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, SUMMONING_EYE_ICON, fX, fY, 0, 0, 16, 16, 16, 16, -1));

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

            }
            case SHOW_AVERAGE_ZEALOTS_PER_EYE -> {
                final int finalColor = color;
                graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, ZEALOTS_PER_EYE_ICON, fX, fY, 0, 0, 16, 16, 16, 16, -1));
                graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, SLASH_ICON, fX, fY, 0, 0, 16, 16, 16, 16, finalColor));
                // TODO true?
//                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16, true);

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

            }
            case SKILL_DISPLAY -> {
                if ((skill == null || skill.getItem() == null) && buttonLocation == null) return;
                renderItem(graphics, buttonLocation == null ? skill.getItem() : SkillType.FARMING.getItem(), x, y);

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

            }
            case BIRCH_PARK_RAINMAKER_TIMER -> {
                renderItem(graphics, WATER_BUCKET, x, y);

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

            }
            case ENDSTONE_PROTECTOR_DISPLAY -> {
                graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, IRON_GOLEM_ICON, fX, fY, 0, 0, 16, 16, 16, 16, -1));

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

                x += 16 + 2 + MC.font.width(text) + 2;

                final float fX2 = x;
                graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, ENDERMAN_GROUP_ICON, fX2, fY, 0, 0, 16, 16, 16, 16, -1));

                int count = EndstoneProtectorManager.getZealotCount();

                DrawUtils.drawText(graphics, TextUtils.formatNumber(count), x + 16 + 2, y + 4, color);

            }
            case SHOW_DUNGEON_MILESTONE -> {
                DungeonMilestone dungeonMilestone = main.getDungeonManager().getDungeonMilestone();
                if (buttonLocation != null) {
                    dungeonMilestone = new DungeonMilestone(DungeonClass.HEALER);
                }

                renderItem(graphics, dungeonMilestone.getDungeonClass().getItem(), x, y);

                DrawUtils.drawText(graphics, text, x + 18, y, color);
                Number amount;
                try {
                    amount = TextUtils.NUMBER_FORMAT.parse(dungeonMilestone.getValue());
                } catch (ParseException e) {
                    amount = -1;
                }
                String formattedAmount = TextUtils.formatNumber(amount);
                DrawUtils.drawText(
                        graphics,
                        formattedAmount,
                        x + 18 + MC.font.width(text) / 2F
                                - MC.font.width(formattedAmount) / 2F,
                        y + 9,
                        color
                );

            }
            case DUNGEONS_COLLECTED_ESSENCES_DISPLAY -> {
                this.drawCollectedEssences(graphics, x, y, buttonLocation != null, true);
            }
            case DUNGEON_DEATH_COUNTER -> {
                graphics.drawSpecial(source -> DrawUtils.blitAbsolute(poseStack, source, MORT_ICON, fX, fY, 0, 0, 16, 16, 16, 16, -1));

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

            }
            case ROCK_PET_TRACKER -> {
                renderItem(graphics, ItemUtils.getTexturedHead("DUMMY_ROCK"), x, y);

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

            }
            case DOLPHIN_PET_TRACKER -> {
                renderItem(graphics, ItemUtils.getTexturedHead("DUMMY_DOLPHIN"), x, y);

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);

            }
            case DUNGEONS_SECRETS_DISPLAY -> {
                int secrets = main.getDungeonManager().getSecrets();
                int maxSecrets = main.getDungeonManager().getMaxSecrets();

                DrawUtils.drawText(graphics, text, x + 16 + 2, y, color);

                if (secrets == -1 && buttonLocation != null) {
                    secrets = 5;
                    maxSecrets = 10;
                }

                if (secrets == -1 | maxSecrets == 0) {

                    String none = Translations.getMessage("messages.none");
                    DrawUtils.drawText(
                            graphics,
                            none,
                            x + 16 + 2 + MC.font.width(text) / 2F
                                    - MC.font.width(none) / 2F,
                            y + 10,
                            color
                    );

                } else {
                    if (secrets > maxSecrets) {
                        // Assume the max secrets equals to found secrets
                        maxSecrets = secrets;
                    }

                    float percent = secrets / (float) maxSecrets;
                    if (percent < 0) {
                        percent = 0;
                    } else if (percent > 1) {
                        percent = 1;
                    }

                    float r;
                    float g;
                    if (percent <= 0.5) { // Fade from red -> yellow
                        r = 1;
                        g = (percent * 2) * 0.66F + 0.33F;
                    } else { // Fade from yellow -> green
                        r = (1 - percent) * 0.66F + 0.33F;
                        g = 1;
                    }
                    int secretsColor = new Color(Math.min(1, r), g, 0.33F).getRGB();

                    float secretsWidth = MC.font.width(String.valueOf(secrets));
                    float slashWidth = MC.font.width("/");
                    float maxSecretsWidth = MC.font.width(String.valueOf(maxSecrets));

                    float totalWidth = secretsWidth + slashWidth + maxSecretsWidth;

                    DrawUtils.drawText(
                            graphics,
                            "/",
                            x + 16 + 2 + MC.font.width(text) / 2F - totalWidth / 2F + secretsWidth,
                            y + 11,
                            color
                    );

                    DrawUtils.drawText(
                            graphics,
                            String.valueOf(secrets),
                            x + 16 + 2 + MC.font.width(text) / 2F - totalWidth / 2F,
                            y + 11,
                            secretsColor
                    );
                    DrawUtils.drawText(
                            graphics,
                            String.valueOf(maxSecrets),
                            x + 16 + 2 + MC.font.width(text) / 2F - totalWidth / 2F + secretsWidth + slashWidth,
                            y + 11,
                            secretsColor
                    );
                }

                renderItem(graphics, CHEST, x, y);
            }
            case SPIRIT_SCEPTRE_DISPLAY -> {
                int hitEnemies = main.getPlayerListener().getSpiritSceptreHitEnemies();
                float dealtDamage = main.getPlayerListener().getSpiritSceptreDealtDamage();

                DrawUtils.drawText(graphics, text, x + 16 + 2, y, color);

                if (hitEnemies == 1) {
                    DrawUtils.drawText(graphics, String.format("%d enemy hit", hitEnemies), x + 16 + 2, y + 9, color);
                } else {
                    DrawUtils.drawText(graphics, String.format("%d enemies hit", hitEnemies), x + 16 + 2, y + 9, color);
                }

                DrawUtils.drawText(graphics, String.format("%,d damage dealt", Math.round(dealtDamage)), x + 16 + 2, y + 18, color);

                if (buttonLocation != null) {
                    renderItem(graphics, DamageDisplayItem.HYPERION.itemStack, x, y);
                    break;
                }

                ItemStack displayItem = DamageDisplayItem.getByID(ItemUtils.getSkyblockItemID(MC.player.getMainHandItem()));
                if (displayItem != null) {
                    renderItem(graphics, displayItem, x, y);
                }
            }
            case CANDY_POINTS_COUNTER -> {
                Map<CandyType, Integer> candyCounts = SpookyEventManager.getCandyCounts();
                if (!SpookyEventManager.isActive()) {
                    candyCounts = SpookyEventManager.getDummyCandyCounts();
                }
                int green = candyCounts.get(CandyType.GREEN);
                int purple = candyCounts.get(CandyType.PURPLE);

                int points = SpookyEventManager.getPoints();
                if (!SpookyEventManager.isActive()) {
                    points = 5678;
                }

                float currentX = x;
                if (buttonLocation != null || green > 0) {
                    renderItem(graphics, ItemUtils.getTexturedHead("GREEN_CANDY"), currentX, y);

                    currentX += 16 + 1;

                    DrawUtils.drawText(graphics, TextUtils.formatNumber(green), currentX, y + 4, color);

                }
                if (buttonLocation != null || purple > 0) {
                    if (buttonLocation != null || green > 0) {
                        currentX += MC.font.width(TextUtils.formatNumber(green)) + 1;
                    }

                    renderItem(graphics, ItemUtils.getTexturedHead("PURPLE_CANDY"), currentX, y);

                    currentX += 16 + 1;

                    DrawUtils.drawText(graphics, TextUtils.formatNumber(purple), currentX, y + 4, color);

                }

                text = TextUtils.formatNumber(points) + " Points";
                DrawUtils.drawText(graphics, text, x + width / 2F - MC.font.width(text) / 2F, y + 16, color);

            }
            case FETCHUR_TODAY -> {
                boolean showDwarven = feature.isDisabled(FeatureSetting.SHOW_FETCHUR_ONLY_IN_DWARVENS) || LocationUtils.isOn(Island.DWARVEN_MINES);
                boolean showInventory = feature.isDisabled(FeatureSetting.SHOW_FETCHUR_INVENTORY_OPEN_ONLY) || MC.screen != null;
                FetchurManager.FetchurItem fetchurItem = FetchurManager.getInstance().getCurrentFetchurItem();

                // Show if it's the gui button position, or the player hasn't given Fetchur,
                // and it shouldn't be hidden b/c of dwarven mines or inventory
                if (fetchurItem != null && (buttonLocation != null ||
                        (!FetchurManager.getInstance().hasFetchedToday() && showDwarven && showInventory))) {

                    if (feature.isDisabled(FeatureSetting.SHOW_FETCHUR_ITEM_NAME)) {
                        DrawUtils.drawText(graphics, text, x + 1, y + 4, color); // Line related to the "Fetchur wants" text
                        float offsetX = MC.font.width(text);
                        renderItemAndOverlay(
                                graphics,
                                fetchurItem.itemStack(),
                                String.valueOf(fetchurItem.itemStack().getCount()),
                                x + offsetX,
                                y
                        );
                    } else {
                        DrawUtils.drawText(graphics, text, x, y, color); // Line related to the "Fetchur wants" text
                    }

                }
            }
            case HEALTH_TEXT -> {
                // 22 -> Absorption
                if (MC.player != null && MC.player.hasEffect(MobEffects.ABSORPTION)
                        && PlayerStat.HEALTH.getValue() > PlayerStat.MAX_HEALTH.getValue()) {
                    String formattedHealth = TextUtils.formatNumber(PlayerStat.HEALTH.getValue());
                    int formattedHealthWidth = MC.font.width(formattedHealth);

                    color = ColorUtils.getDummySkyblockColor(ColorCode.GOLD.getColor(), feature.isChroma()).getColor();

                    DrawUtils.drawText(graphics, formattedHealth, x, y, color);

                    color = feature.getColor();
                    DrawUtils.drawText(
                            graphics,
                            "/" + TextUtils.formatNumber(PlayerStat.MAX_HEALTH.getValue())
                                    + (feature.isEnabled(FeatureSetting.HEALTH_TEXT_ICON) ? "❤" : ""),
                            x + formattedHealthWidth,
                            y,
                            color
                    );

                } else {

                    DrawUtils.drawText(graphics, text, x, y, color);

                }
            }
            case FIRE_FREEZE_TIMER -> {
                renderItem(graphics, Blocks.DANDELION.asItem().getDefaultInstance(), x, y - 3);

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);
            }
            case THUNDER_BOTTLE_DISPLAY -> {
                ThunderBottle displayBottle = ThunderBottle.getDisplayBottle();

                if (displayBottle != null) {
                    renderItem(graphics, displayBottle.getItemStack(), x, y);
                } else /*buttonLocation != null*/ {
                    renderItem(graphics, ItemUtils.getTexturedHead("DUMMY_THUNDER_BOTTLE"), x, y);
                }

                DrawUtils.drawText(graphics, text, x + 18, y + 4, color);
            }
            case PRESSURE_TEXT -> {
                if (Feature.PRESSURE_TEXT.isEnabled(FeatureSetting.PRESSURE_TEXT_ALERT)) {
                    float pressure = PlayerStat.PRESSURE.getValue();
                    if (pressure >= 90.0F && main.getScheduler().getTotalTicks() % 40 >= 20) {
                        color = ColorCode.RED.getColor();
                    }
                }

                DrawUtils.drawText(graphics, text, x, y, color);
            }
            default -> {
                DrawUtils.drawText(graphics, text, x, y, color);

            }
        }

    }

    private String getCrimsonArmorAbilityStacks() {
        LocalPlayer player = MC.player;
        if (player == null) return null;

        StringBuilder builder = new StringBuilder();
        out:
        for (CrimsonArmorAbilityStack crimsonArmorAbilityStack : CrimsonArmorAbilityStack.values()) {
            for (EquipmentSlot equipmentSlot : Inventory.EQUIPMENT_SLOT_MAPPING.values()) { // 1.21.5
                ItemStack itemStack = player.getItemBySlot(equipmentSlot);
                if (itemStack == ItemStack.EMPTY) continue;
                for (String line : ItemUtils.getItemLore(itemStack)) {
                    if (line.contains("§6Tiered Bonus: ")) {
                        String abilityName = crimsonArmorAbilityStack.getAbilityName();
                        if (line.contains(abilityName)) {
                            String symbol = crimsonArmorAbilityStack.getSymbol();
                            int stack = crimsonArmorAbilityStack.getCurrentValue();
                            builder.append(abilityName).append(" ").append(symbol).append(" ").append(stack);
                            continue out;
                        }
                    }
                }
            }
        }
        return builder.isEmpty() ? null : builder.toString();
    }

public void drawCollectedEssences(GuiGraphics graphics, float x, float y, boolean usePlaceholders, boolean hideZeroes) {
    InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

    float currentX = x;
    float currentY;

    int maxNumberWidth;
    if (inventoryType == InventoryType.SALVAGING) {
        Set<Map.Entry<EssenceType, Integer>> entrySet = main.getDungeonManager().getSalvagedEssences().entrySet();
        if (entrySet.isEmpty()) return;

        String highestAmountStr = Collections.max(entrySet, Map.Entry.comparingByValue()).getValue().toString();
        maxNumberWidth = MC.font.width(highestAmountStr);
    } else {
        maxNumberWidth = MC.font.width("99");
    }

    int color = Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY.getColor();

    int count = 0;
    for (EssenceType essenceType : EssenceType.values()) {
        int value;

        if (inventoryType == InventoryType.SALVAGING) {
            value = main.getDungeonManager().getSalvagedEssences().getOrDefault(essenceType, 0);
        } else {
            value = main.getDungeonManager().getCollectedEssences().getOrDefault(essenceType, 0);
        }

        if (usePlaceholders) {
            value = 99;
        } else if (value <= 0 && hideZeroes) {
            continue;
        }

        int column = count % 2;
        int row = count / 2;

        if (column == 0) {
            currentX = x;
        } else if (column == 1) {
            currentX = x + 18 + 2 + maxNumberWidth + 5;
        }
        currentY = y + row * 18;

        final float fX = currentX, fY = currentY;
        graphics.drawSpecial(source -> DrawUtils.blitAbsolute(graphics.pose(), source, essenceType.getResourceLocation(), fX, fY, 0, 0, 16, 16, 16, 16, -1));

//        FontRendererHook.setupFeatureFont(Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY);
        DrawUtils.drawText(graphics, TextUtils.formatNumber(value), currentX + 18 + 2, currentY + 5, color);
//        FontRendererHook.endFeatureFont();

        count++;
    }
}

    /**
     * Displays the bait list. Only shows bait with count > 0.
     */
    public void drawBaitList(GuiGraphics graphics, float scale, ButtonLocation buttonLocation) {
        if (!main.getPlayerListener().isHoldingRod() && buttonLocation == null) return;

        Map<ItemStack, Integer> baits = BaitManager.getInstance().getBaitsInInventory();
        if (buttonLocation != null) {
            baits = BaitManager.DUMMY_BAITS;
        }

        int longestLineWidth = 0;
        for (Map.Entry<ItemStack, Integer> entry : baits.entrySet()) {
            longestLineWidth = Math.max(
                    longestLineWidth,
                    MC.font.width(TextUtils.formatNumber(entry.getValue()))
            );
        }

        Feature feature = Feature.BAIT_LIST;
        float x = feature.getActualX();
        float y = feature.getActualY();

        int spacing = 1;
        int iconSize = 16;
        int width = iconSize + spacing + longestLineWidth;
        int height = iconSize * baits.size();

        x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
        y = transformY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + height, scale);
        }

        for (Map.Entry<ItemStack, Integer> entry : baits.entrySet()) {
            if (entry.getValue() == 0) continue;

            renderItem(graphics, entry.getKey(), x, y);

            int color = feature.getColor();
//            //FontRendererHook.setupFeatureFont(Feature.BAIT_LIST);
            DrawUtils.drawText(
                    graphics,
                    TextUtils.formatNumber(entry.getValue()),
                    x + iconSize + spacing,
                    y + (iconSize / 2F) - (8 / 2F),
                    color
            );

            y += iconSize;
        }
    }

    public void drawSlayerTrackers(GuiGraphics graphics, Feature feature, float scale, ButtonLocation buttonLocation) {
        boolean colorByRarity;
        boolean textMode;
        SlayerBoss slayerBoss;
        EnumUtils.SlayerQuest quest = main.getUtils().getSlayerQuest();

        switch (feature) {
            case REVENANT_SLAYER_TRACKER:
                if (buttonLocation == null && feature.isEnabled(FeatureSetting.HIDE_WHEN_NOT_IN_CRYPTS)
                        && (quest != EnumUtils.SlayerQuest.REVENANT_HORROR
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.REVENANT_HORROR))) {
                    return;
                }
                colorByRarity = feature.isEnabled(FeatureSetting.REVENANT_TRACKER_COLOR_BY_RARITY);
                textMode = feature.isEnabled(FeatureSetting.REVENANT_TRACKER_TEXT_MODE);
                slayerBoss = SlayerBoss.REVENANT;
                break;

            case TARANTULA_SLAYER_TRACKER:
                if (buttonLocation == null && feature.isEnabled(FeatureSetting.HIDE_WHEN_NOT_IN_SPIDERS_DEN)
                        && (quest != EnumUtils.SlayerQuest.TARANTULA_BROODFATHER
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.TARANTULA_BROODFATHER))) {
                    return;
                }
                colorByRarity = feature.isEnabled(FeatureSetting.TARANTULA_TRACKER_COLOR_BY_RARITY);
                textMode = feature.isEnabled(FeatureSetting.TARANTULA_TRACKER_TEXT_MODE);
                slayerBoss = SlayerBoss.TARANTULA;
                break;

            case SVEN_SLAYER_TRACKER:
                if (buttonLocation == null && feature.isEnabled(FeatureSetting.HIDE_WHEN_NOT_IN_CASTLE)
                        && (quest != EnumUtils.SlayerQuest.SVEN_PACKMASTER
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.SVEN_PACKMASTER))) {
                    return;
                }
                colorByRarity = feature.isEnabled(FeatureSetting.SVEN_TRACKER_COLOR_BY_RARITY);
                textMode = feature.isEnabled(FeatureSetting.SVEN_TRACKER_TEXT_MODE);
                slayerBoss = SlayerBoss.SVEN;
                break;

            case VOIDGLOOM_SLAYER_TRACKER:
                if (buttonLocation == null && feature.isEnabled(FeatureSetting.HIDE_WHEN_NOT_IN_END)
                        && (quest != EnumUtils.SlayerQuest.VOIDGLOOM_SERAPH
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.VOIDGLOOM_SERAPH))) {
                    return;
                }
                colorByRarity = feature.isEnabled(FeatureSetting.VOIDGLOOM_TRACKER_COLOR_BY_RARITY);
                textMode = feature.isEnabled(FeatureSetting.VOIDGLOOM_TRACKER_TEXT_MODE);
                slayerBoss = SlayerBoss.VOIDGLOOM;
                break;

            case INFERNO_SLAYER_TRACKER:
                if (buttonLocation == null && feature.isEnabled(FeatureSetting.HIDE_WHEN_NOT_IN_CRIMSON)
                        && (quest != EnumUtils.SlayerQuest.INFERNO_DEMONLORD
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.INFERNO_DEMONLORD))) {
                    return;
                }
                colorByRarity = feature.isEnabled(FeatureSetting.INFERNO_TRACKER_COLOR_BY_RARITY);
                textMode = feature.isEnabled(FeatureSetting.INFERNO_TRACKER_TEXT_MODE);
                slayerBoss = SlayerBoss.INFERNO;
                break;

            case RIFTSTALKER_SLAYER_TRACKER:
                if (buttonLocation == null && feature.isEnabled(FeatureSetting.HIDE_WHEN_NOT_IN_RIFT)
                        && (quest != EnumUtils.SlayerQuest.RIFTSTALKER_BLOODFIEND
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.RIFTSTALKER_BLOODFIEND))) {
                    return;
                }
                colorByRarity = feature.isEnabled(FeatureSetting.RIFTSTALKER_TRACKER_COLOR_BY_RARITY);
                textMode = feature.isEnabled(FeatureSetting.RIFTSTALKER_TRACKER_TEXT_MODE);
                slayerBoss = SlayerBoss.RIFTSTALKER;
                break;

            default:
                return;
        }

        float x = feature.getActualX();
        float y = feature.getActualY();
        int color = feature.getColor();

        if (textMode) {
            int lineHeight = 8;
            int spacer = 3;

            int lines = 0;
            int spacers = 0;

            int longestLineWidth = MC.font.width(slayerBoss.getDisplayName());
            lines++;
            spacers++;

            int longestSlayerDropLineWidth = MC.font.width(
                    Translations.getMessage("slayerTracker.bossesKilled")
            );
            int longestCount = MC.font.width(
                    String.valueOf(SlayerTracker.getInstance().getSlayerKills(slayerBoss))
            );
            lines++;
            spacers++;

            for (SlayerDrop drop : slayerBoss.getDrops()) {
                longestSlayerDropLineWidth = Math.max(
                        longestSlayerDropLineWidth,
                        MC.font.width(drop.getDisplayName())
                );
                longestCount = Math.max(
                        longestCount,
                        MC.font.width(String.valueOf(SlayerTracker.getInstance().getDropCount(drop)))
                );
                lines++;
            }

            int width = Math.max(longestLineWidth, longestSlayerDropLineWidth + 8 + longestCount);
            int height = lines * 8 + spacer * spacers;

            x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
            y = transformY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + height, scale);
            }

            DrawUtils.drawText(graphics, slayerBoss.getDisplayName(), x, y, color);
            y += lineHeight + spacer;
            DrawUtils.drawText(graphics, Translations.getMessage("slayerTracker.bossesKilled"), x, y, color);
            String text = String.valueOf(SlayerTracker.getInstance().getSlayerKills(slayerBoss));
            DrawUtils.drawText(graphics, text, x + width - MC.font.width(text), y, color);
            y += lineHeight + spacer;

            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {

                int currentColor = colorByRarity ? slayerDrop.getRarity().getColorCode().getColor() : color;

                DrawUtils.drawText(graphics, slayerDrop.getDisplayName(), x, y, currentColor, colorByRarity);

                text = String.valueOf(SlayerTracker.getInstance().getDropCount(slayerDrop));
                DrawUtils.drawText(graphics, text, x + width - MC.font.width(text), y, currentColor);

                y += lineHeight;
            }

        } else {
            int entityRenderY;
            int textCenterX = switch (feature) {
                case REVENANT_SLAYER_TRACKER -> {
                    entityRenderY = 30;
                    yield 15;
                }
                case TARANTULA_SLAYER_TRACKER -> {
                    entityRenderY = 36;
                    yield 28;
                }
                case SVEN_SLAYER_TRACKER -> {
                    entityRenderY = 25;
                    yield 20;
                }
                case VOIDGLOOM_SLAYER_TRACKER -> {
                    entityRenderY = 24;
                    yield 20;
                }
                case INFERNO_SLAYER_TRACKER -> {
                    entityRenderY = 35;
                    yield 20;
                }
                case RIFTSTALKER_SLAYER_TRACKER -> {
                    entityRenderY = 40;
                    yield 15;
                }
                default -> {
                    entityRenderY = 36;
                    yield 15;
                }
            };

            int iconWidth = 16;

            int entityWidth = textCenterX * 2;
            int entityIconSpacingHorizontal = 2;
            int iconTextOffset = -2;
            int row = 0;
            int column = 0;
            int maxItemsPerRow = (int) Math.ceil(slayerBoss.getDrops().size() / 3.0);
            int[] maxTextWidths = new int[maxItemsPerRow];
            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
                int width = MC.font.width(
                        TextUtils.abbreviate(SlayerTracker.getInstance().getDropCount(slayerDrop))
                );

                maxTextWidths[column] = Math.max(maxTextWidths[column], width);

                column++;
                if (column == maxItemsPerRow) {
                    column = 0;
                    row++;
                }
            }

            int totalColumnWidth = 0;
            for (int i : maxTextWidths) {
                totalColumnWidth += i;
            }
            int iconSpacingVertical = 4;

            int width = entityWidth + entityIconSpacingHorizontal + maxItemsPerRow * iconWidth + totalColumnWidth + iconTextOffset;
            int height = (iconWidth + iconSpacingVertical) * 3 - iconSpacingVertical;

            x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
            y = transformY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + height, scale);
            }

            switch (feature) {
                case REVENANT_SLAYER_TRACKER:
                    if (revenant == null) {
                        revenant = new Zombie(EntityType.ZOMBIE, MC.level /*Utils.getDummyWorld()*/);

                        revenant.setItemSlot(EquipmentSlot.MAINHAND, ItemUtils.createItemStack(Items.DIAMOND_HOE, true));
                        revenant.setItemSlot(EquipmentSlot.FEET, ItemUtils.createItemStack(Items.DIAMOND_BOOTS, false));
                        revenant.setItemSlot(EquipmentSlot.LEGS, ItemUtils.createItemStack(Items.CHAINMAIL_LEGGINGS, true));
                        revenant.setItemSlot(EquipmentSlot.CHEST, ItemUtils.createItemStack(Items.DIAMOND_CHESTPLATE, true));
                        revenant.setItemSlot(EquipmentSlot.HEAD, ItemUtils.getTexturedHead("REAPER_MASK"));
                    }
                    revenant.tickCount = (int) main.getScheduler().getTotalTicks();
                    drawEntity(graphics, revenant, x + 15, y + 53, -15); // left is 35
                    break;

                case TARANTULA_SLAYER_TRACKER:
                    if (tarantula == null) {
                        tarantula = new Spider(EntityType.SPIDER, MC.level /*Utils.getDummyWorld()*/);
                        caveSpider = new CaveSpider(EntityType.CAVE_SPIDER, MC.level /*Utils.getDummyWorld()*/);
                        caveSpider.startRiding(tarantula, true);
                    }
                    drawEntity(graphics, tarantula, x + 28, y + 38, -30);
                    drawEntity(graphics, caveSpider, x + 25, y + 23, -30);
                    break;

                case SVEN_SLAYER_TRACKER:
                    if (sven == null) {
                        sven = new Wolf(EntityType.WOLF, MC.level /*Utils.getDummyWorld()*/);
                        sven.setRemainingPersistentAngerTime(Integer.MAX_VALUE);
                    }
                    drawEntity(graphics, sven, x + 17, y + 38, -35);
                    break;

                case VOIDGLOOM_SLAYER_TRACKER:
                    if (enderman == null) {
                        enderman = new EnderMan(EntityType.ENDERMAN, MC.level /*Utils.getDummyWorld()*/);
                        enderman.setCarriedBlock(Blocks.BEACON.defaultBlockState());
                    }
//                    GlStateManager.color(1, 1, 1, 1);
                    enderman.tickCount = (int) main.getScheduler().getTotalTicks();
                    graphics.pose().scale(0.7F, 0.7F, 1F);
                    drawEntity(graphics, enderman, (x + 15.0F) / 0.7F, (y + 51.0F) / 0.7F, -30.0F);
                    graphics.pose().scale(1.0F / 0.7F, 1.0F / 0.7F, 1.0F);
                    break;

                case INFERNO_SLAYER_TRACKER:
                    if (inferno == null) {
                        inferno = new Blaze(EntityType.BLAZE, MC.level /*Utils.getDummyWorld()*/);
                        inferno.setCharged(true);
                    }
                    inferno.tickCount = (int) main.getScheduler().getTotalTicks();
                    drawEntity(graphics, inferno, x + 15, y + 53, -15);
                    break;

                case RIFTSTALKER_SLAYER_TRACKER:
                    if (riftstalker == null) {
                        riftstalker = new RemotePlayer(MC.level /*Utils.getDummyWorld()*/, new GameProfile(UUID.randomUUID(), "Riftstalker")) {
                            @Override
                            public PlayerSkin getSkin() {
                                return new PlayerSkin(RIFTSTALKER_BLOODFIEND, null, null, null, PlayerSkin.Model.WIDE, true);
                            }
                        };
                        riftstalker.setCustomNameVisible(false);
                    }
                    drawEntity(graphics, riftstalker, x + 15, y + 53, -15);
                    break;
            }

//            GlStateManager.disableDepth();
            row = 0;
            column = 0;
            float currentX = x + entityIconSpacingHorizontal + entityWidth;
            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
                if (column > 0) {
                    currentX += iconWidth + maxTextWidths[column - 1];
                }

                float currentY = y + row * (iconWidth + iconSpacingVertical);

                renderItem(graphics, slayerDrop.getItemStack(), currentX, currentY);

                int currentColor = colorByRarity ? slayerDrop.getRarity().getColorCode().getColor() : color;

                DrawUtils.drawText(
                        graphics,
                        TextUtils.abbreviate(SlayerTracker.getInstance().getDropCount(slayerDrop)),
                        currentX + iconWidth + iconTextOffset,
                        currentY + 8,
                        currentColor,
                        colorByRarity
                );

                column++;
                if (column == maxItemsPerRow) {
                    currentX = x + entityIconSpacingHorizontal + entityWidth;
                    column = 0;
                    row++;
                }
            }

            String text = TextUtils.abbreviate(SlayerTracker.getInstance().getSlayerKills(slayerBoss)) + " Kills";
            DrawUtils.drawText(
                    graphics,
                    text,
                    x + textCenterX - MC.font.width(text) / 2F,
                    y + entityRenderY,
                    color
            );

//            GlStateManager.enableDepth();
        }
    }

    public void drawDragonTrackers(GuiGraphics graphics, float scale, ButtonLocation buttonLocation) {
        Feature feature = Feature.DRAGON_STATS_TRACKER;
        if (feature.isEnabled(FeatureSetting.DRAGONS_NEST_ONLY)
                && !LocationUtils.isOn("Dragon's Nest") && buttonLocation == null) {
            return;
        }

        List<DragonType> recentDragons = DragonTracker.getInstance().getRecentDragons();
        if (recentDragons.isEmpty() && buttonLocation != null) {
            recentDragons = DragonTracker.getDummyDragons();
        }

        boolean colorByRarity = feature.isEnabled(FeatureSetting.DRAGON_TRACKER_COLOR_BY_RARITY);
        boolean textMode = feature.isEnabled(FeatureSetting.DRAGON_TRACKER_TEXT_MODE);

        int spacerHeight = 3;
        String never = Translations.getMessage("dragonTracker.never");
        int width;
        int height;
        if (textMode) {
            int lines = 0;
            int spacers = 0;

            int longestLineWidth = MC.font.width(
                    Translations.getMessage("dragonTracker.recentDragons")
            );
            lines++;
            spacers++;

            spacers++;
            longestLineWidth = Math.max(
                    longestLineWidth,
                    MC.font.width(Translations.getMessage("dragonTracker.dragonsSince"))
            );
            lines++;
            spacers++;

            for (DragonType dragon : recentDragons) {
                longestLineWidth = Math.max(longestLineWidth, MC.font.width(dragon.getDisplayName()));
                lines++;
            }

            int longestCount = 0;
            int longestDragonsSinceLineWidth = 0;
            for (DragonsSince dragonsSince : DragonsSince.values()) {
                longestDragonsSinceLineWidth = Math.max(
                        longestDragonsSinceLineWidth,
                        MC.font.width(dragonsSince.getDisplayName())
                );
                int dragonsSinceValue = DragonTracker.getInstance().getDragsSince(dragonsSince);
                longestCount = Math.max(
                        longestCount,
                        MC.font.width(dragonsSinceValue == 0 ? never : String.valueOf(dragonsSinceValue))
                );
                lines++;
            }
            width = Math.max(longestLineWidth, longestDragonsSinceLineWidth + 8 + longestCount);

            height = lines * 8 + spacerHeight * spacers;
        } else {
            width = 100;
            height = 100;
        }

        float x = Feature.DRAGON_STATS_TRACKER.getActualX();
        float y = Feature.DRAGON_STATS_TRACKER.getActualY();
        x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
        y = transformY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + height, scale);
        }

        int color = feature.getColor();

        if (textMode) {
//            //FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
            DrawUtils.drawText(graphics, Translations.getMessage("dragonTracker.recentDragons"), x, y, color);
            y += 8 + spacerHeight;

            for (DragonType dragon : recentDragons) {
                int currentColor = colorByRarity ? dragon.getColor().getColor() : color;

                DrawUtils.drawText(graphics, dragon.getDisplayName(), x, y, currentColor, colorByRarity);

                y += 8;
            }
            y += spacerHeight;

            color = feature.getColor();
            DrawUtils.drawText(graphics, Translations.getMessage("dragonTracker.dragonsSince"), x, y, color);
            y += 8 + spacerHeight;

            for (DragonsSince dragonsSince : DragonsSince.values()) {

                int currentColor = colorByRarity ? dragonsSince.getItemRarity().getColorCode().getColor() : color;

                DrawUtils.drawText(graphics, dragonsSince.getDisplayName(), x, y, currentColor, colorByRarity);

//                //FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
                int dragonsSinceValue = DragonTracker.getInstance().getDragsSince(dragonsSince);
                String text = dragonsSinceValue == 0 ? never : String.valueOf(dragonsSinceValue);
                DrawUtils.drawText(graphics, text, x + width - MC.font.width(text), y, color);
                y += 8;

            }
        }
    }

    public void drawSlayerArmorProgress(GuiGraphics graphics, float scale, ButtonLocation buttonLocation) {
        float x = Feature.SLAYER_ARMOR_PROGRESS.getActualX();
        float y = Feature.SLAYER_ARMOR_PROGRESS.getActualY();
        Feature feature = Feature.SLAYER_ARMOR_PROGRESS;

        int longest = -1;
        SlayerArmorProgress[] progresses = main.getInventoryUtils().getSlayerArmorProgresses();
        if (buttonLocation != null) progresses = DUMMY_PROGRESSES;
        for (SlayerArmorProgress progress : progresses) {
            if (progress == null) continue;

            int textWidth = MC.font.width(progress.getPercent() + "% (" + progress.getDefence() + ")");
            if (textWidth > longest) {
                longest = textWidth;
            }
        }
        if (longest == -1) return;

        int height = 15 * 4;
        int width = 16 + 2 + longest;

        x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
        y = transformY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + height, scale);
        }

        boolean downwards = Feature.SLAYER_ARMOR_PROGRESS.getAnchorPoint().isOnTop();

        int color = feature.getColor();

        int drawnCount = 0;
        for (int armorPiece = 3; armorPiece >= 0; armorPiece--) {
            SlayerArmorProgress progress = progresses[downwards ? armorPiece : 3 - armorPiece];
            if (progress == null) continue;

            float fixedY;
            if (downwards) {
                fixedY = y + drawnCount * 15;
            } else {
                fixedY = (y + 45) - drawnCount * 15;
            }
            renderItem(graphics, progress.getItemStack(), x, fixedY);

            float currentX = x + 19;
//            //FontRendererHook.setupFeatureFont(Feature.SLAYER_ARMOR_PROGRESS);
            DrawUtils.drawText(graphics, progress.getPercent() + "% (", currentX, fixedY + 5, color);

            currentX += MC.font.width(progress.getPercent() + "% (");
            DrawUtils.drawText(graphics, progress.getDefence(), currentX, fixedY + 5, 0xFFFFFFFF);

            currentX += MC.font.width(progress.getDefence());
//            //FontRendererHook.setupFeatureFont(Feature.SLAYER_ARMOR_PROGRESS);
            DrawUtils.drawText(graphics, ")", currentX, fixedY + 5, color);

            drawnCount++;
        }
    }

    private void drawPetDisplay(GuiGraphics graphics, float scale, ButtonLocation buttonLocation) {
        if (main.getUtils().isOnRift()) return;

        Feature feature = Feature.PET_DISPLAY;
        PetManager.Pet newPet = main.getPetCacheManager().getCurrentPet();
        if (newPet == null) {
            return;
        } else if (pet != newPet) {
            pet = newPet;
            petSkull = newPet.getItemStack();
        }

        String text = pet.getDisplayName();

        float x = feature.getActualX();
        float y = feature.getActualY();

        int height = 7 + MC.font.lineHeight;
        int width = MC.font.width(text) + 18; // + ItemStack width

        PetItemStyle style = (PetItemStyle) feature.getAsEnum(FeatureSetting.PET_ITEM_STYLE);
        int line = 1; // maybe new lines can be added in the future?
        // Second line
        if (style != PetItemStyle.NONE && pet.getPetInfo().getHeldItemId() != null) {
            height *= 2;
            width += 18;
            line++;
        }

        x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
        y = transformY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + height, scale);
        }

//        //FontRendererHook.setupFeatureFont(Feature.PET_DISPLAY);
        int color = feature.getColor();
        DrawUtils.drawText(graphics, text, x + (18 * line), y + 4, color);

        switch (style) {
            case DISPLAY_NAME:
                if (pet.getPetInfo().getHeldItemId() == null) break;

                String petDisplayName = PetManager.getInstance().getPetItemDisplayNameFromId(
                        pet.getPetInfo().getHeldItemId()
                );
                DrawUtils.drawText(graphics, "Held Item: " + petDisplayName, x + (18 * line), y + 16, color);
                break;

            case SHOW_ITEM:
                if (pet.getPetInfo().getHeldItemId() == null) break;

                PetManager petManager = PetManager.getInstance();
                String petHeldItemId = pet.getPetInfo().getHeldItemId();

                ItemStack petItemStack = petManager.getPetItemFromId(petHeldItemId);
                SkyblockRarity petItemRarity = petManager.getPetItemRarityFromId(petHeldItemId);

                String displayText = "Held Item:";
                if (petHeldItemId.endsWith(petItemRarity.getLoreName())) {
                    // To recognize those with the same Item but different Rarity
                    displayText += " " + petItemRarity.getColorCode().toString() + petItemRarity.getLoreName();
                }
                DrawUtils.drawText(graphics, displayText, x + (18 * line), y + 16, color);

                renderItem(graphics, petItemStack, x + (18 * line) + MC.font.width(displayText), y + 10);
                break;
        }

        // render pet
        renderItem(graphics, petSkull, x, y, line);
    }

    private void renderItem(GuiGraphics graphics, ItemStack item, float x, float y) {
        renderItem(graphics, item, x, y, 1);
    }

    /**
     * The main purpose is scale the item for make it compatible for add new lines e.g. scale with two for add 2nd line
     */
    private void renderItem(GuiGraphics graphics, ItemStack item, float x, float y, float scale) {
        if (item == null || item.isEmpty()) return;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        if (scale != 1) {
            poseStack.scale(scale, scale, 1F);
        }
        poseStack.translate(x / scale, y / scale, 0);
        graphics.renderItem(item, 0, 0);
        poseStack.popPose();
    }

    public static void renderItemAndOverlay(GuiGraphics graphics, ItemStack item, String name, float x, float y) {
        renderItemAndOverlay(graphics, item, name, x, y, 0);
    }

    public static void renderItemAndOverlay(GuiGraphics graphics, ItemStack item, String name, float x, float y, float z) {
        if (item == null || item.isEmpty()) return;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        graphics.renderItem(item, 0, 0);
        graphics.renderItemDecorations(MC.font, item, 0, 0, name);
        poseStack.popPose();
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public void drawItemPickupLog(GuiGraphics graphics, float scale, ButtonLocation buttonLocation) {
        Feature feature = Feature.ITEM_PICKUP_LOG;
        float x = feature.getActualX();
        float y = feature.getActualY();

        boolean downwards = feature.getAnchorPoint().isOnTop();
        boolean renderItemStack = feature.isEnabled(FeatureSetting.RENDER_ITEM_ON_LOG);

        int heightSpacer = renderItemStack ? 6 : 1;
        int lineHeight = MC.font.lineHeight + heightSpacer; // + pixel spacer
        int height = lineHeight * DUMMY_PICKUP_LOG.size();
        int width = MC.font.width("+ 1x Forceful Ember Chestplate");

        if (renderItemStack) {
            width += 18;
        }

        x = transformX(x, width, scale, false);
        y = transformY(y, height, scale);
        // X/Y Alignment
        if (renderItemStack) {
            x += 9;
            y += (heightSpacer / 2) * DUMMY_PICKUP_LOG.size();
        }

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + height, scale);
        }

        int i = 0;
        Collection<ItemDiff> log = main.getInventoryUtils().getItemPickupLog();
        if (buttonLocation != null) {
            log = DUMMY_PICKUP_LOG;
        }
        for (ItemDiff itemDiff : log) {
            float stringY;
            if (downwards) {
                stringY = y + (i * lineHeight) + heightSpacer / 2;
            } else {
                stringY = y + height - (i * lineHeight) - 9 - heightSpacer / 2;
            }

            String countText = String.format(
                    "%s %sx",
                    itemDiff.getAmount() > 0 ? "§a+" : "§c-",
                    Math.abs(itemDiff.getAmount())
            );
            DrawUtils.drawText(graphics, countText, x, stringY, 0xFFFFFFFF);
            DrawUtils.drawText(
                    graphics,
                    "§r" + itemDiff.getDisplayName(),
                    x + MC.font.width(countText) + (renderItemStack ? 20 : 4),
                    stringY,
                    0xFFFFFFFF
            );
            if (renderItemStack) {
                renderItem(
                        graphics,
                        itemDiff.getItemStack(),
                        x + MC.font.width(countText) + 2,
                        stringY - heightSpacer / 2 - 1
                );
            }
            i++;
        }
    }

    public void drawDeployableStatus(GuiGraphics graphics, float scale, ButtonLocation buttonLocation) {
        DeployableManager.DeployableEntry activeDeployable = DeployableManager.getInstance().getActiveDeployable();
        if (buttonLocation != null && activeDeployable == null) {
            activeDeployable = DeployableManager.DUMMY_DEPLOYABLE_ENTRY;
        }
        if (activeDeployable != null) {
            Deployable deployable = activeDeployable.getDeployable();
            int seconds = activeDeployable.getSeconds();

            DeployableDisplayStyle displayStyle = (DeployableDisplayStyle) Feature.DEPLOYABLE_STATUS_DISPLAY.getAsEnum(FeatureSetting.DEPLOYABLE_DISPLAY_STYLE);
            if (displayStyle == DeployableDisplayStyle.DETAILED) {
                drawDetailedDeployableStatus(graphics, scale, buttonLocation, deployable, seconds);
            } else {
                drawCompactDeployableStatus(graphics, scale, buttonLocation, deployable, seconds);
            }
        }
    }

    /**
     * Displays the deployable display in a compact way with only the amount of seconds to the right of the icon.
     * <p>
     * ----
     * |  | XXs
     * ----
     */
    private void drawCompactDeployableStatus(GuiGraphics graphics, float scale, ButtonLocation buttonLocation, Deployable deployable, int seconds) {
        Feature feature = Feature.DEPLOYABLE_STATUS_DISPLAY;
        float x = feature.getActualX();
        float y = feature.getActualY();

        String secondsString = String.format("§e%ss", seconds);
        int spacing = 1;
        int iconSize = MC.font.lineHeight * 3; // 3 because it looked the best
        int width = iconSize + spacing + MC.font.width(secondsString);

        x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
        y = transformY(y, iconSize, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + iconSize, scale);
        }

        Entity entity = null;
        DeployableManager.DeployableEntry activeDeployable = DeployableManager.getInstance().getActiveDeployable();
        if (activeDeployable != null) {
            UUID uuidOfActiveDep = activeDeployable.getUuid();
            if (uuidOfActiveDep != null) {
                entity = Utils.getEntityFromUUID(uuidOfActiveDep);
            }
        } else if (buttonLocation != null) {
            entity = DeployableManager.DUMMY_ARMOR_STAND;
        }

        if (entity instanceof ArmorStand armorStand) {
            drawDeployableArmorStand(graphics, armorStand, x + 1, y + 4);
        } else {
            graphics.blit(RenderType::guiTextured, deployable.getResourceLocation(), (int) x, (int) y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        DrawUtils.drawText(
                graphics,
                secondsString,
                x + spacing + iconSize,
                y + (iconSize / 2F) - (8 / 2F),
                ColorCode.WHITE.getColor(255)
        );
    }

    /**
     * Displays the deployable with detailed stats about the boost you're receiving.
     * <p>
     * ---- +X ❤/s
     * |  | +X ✎/s
     * ---- +X ❁
     * XXs
     */
    private void drawDetailedDeployableStatus(GuiGraphics graphics, float scale, ButtonLocation buttonLocation, Deployable deployable, int seconds) {
        Feature feature = Feature.DEPLOYABLE_STATUS_DISPLAY;
        float x = feature.getActualX();
        float y = feature.getActualY();

        List<String> display = new LinkedList<>();
        // Counts already long strings
        int passIndex = 0;

        if (deployable.getHealthRegen() > 0.0) {
            float maxHealth = PlayerStat.MAX_HEALTH.getValue();
            float healthRegen = (float) (maxHealth * deployable.getHealthRegen());
            if (main.getUtils().getSlayerQuest() == EnumUtils.SlayerQuest.TARANTULA_BROODFATHER
                    && main.getUtils().getSlayerQuestLevel() >= 2) {
                healthRegen *= 0.5F; // Tarantula boss 2+ reduces healing by 50%.
            }
            display.add(String.format("§c+%s ❤/s", TextUtils.formatNumber(healthRegen)));
            passIndex++;
        }

        if (deployable.getManaRegen() > 0.0) {
            float maxMana = PlayerStat.MAX_MANA.getValue();
            float manaRegen = (float) (maxMana * deployable.getManaRegen() / 50);
            display.add(String.format("§b+%s ✎/s", TextUtils.formatNumber(manaRegen)));
            passIndex++;
        }

        int strength = deployable.getStrength();
        if (strength > 0) display.add("§c+" + strength + " ❁ ");

        double vitality = deployable.getVitality();
        if (vitality > 0.0) display.add("§4+" + TextUtils.formatNumber(vitality) + " ♨ ");

        double mending = deployable.getMending();
        if (mending > 0.0) display.add("§a+" + TextUtils.formatNumber(mending) + " ☄ ");

        int trueDefense = deployable.getTrueDefense();
        if (trueDefense > 0) display.add("§f+" + trueDefense + " ❂ ");

        int ferocity = deployable.getFerocity();
        if (ferocity > 0) display.add("§c+" + ferocity + " ⫽ ");

        int bonusAttackSpeed = deployable.getBonusAttackSpeed();
        if (bonusAttackSpeed > 0) display.add("§e+" + bonusAttackSpeed + "% ⚔ ");

        int trophyFishChance = deployable.getTrophyFishChance();
        if (trophyFishChance > 0) display.add("§6+" + trophyFishChance + " ♔ ");

        // For better visual (maybe?)
        if (feature.isEnabled(FeatureSetting.EXPAND_DEPLOYABLE_STATUS) && display.size() > 3) {
            List<String> displayCopy = new LinkedList<>(display);
            display.clear();

            // Firstly add mana and health strings which already long
            for (int i = 0; i < passIndex; i++)
                display.add(displayCopy.get(i));

            // Concatenate the remaining strings in pairs
            for (int i = passIndex; i < displayCopy.size() - 1; i += 2) {
                if (i - 1 < displayCopy.size()) {
                    display.add(displayCopy.get(i) + displayCopy.get(i + 1));
                }
            }

            // Last touch
            if ((displayCopy.size() - passIndex) % 2 != 0) {
                display.add(displayCopy.getLast());
            }
        }

        Optional<String> longestLine = display.stream().max(Comparator.comparingInt(String::length));

        int spacingBetweenLines = 1;
        int iconSize = MC.font.lineHeight * 3; // 3 because it looked the best
        int iconAndSecondsHeight = iconSize + MC.font.lineHeight;

        int effectsHeight = (MC.font.lineHeight + spacingBetweenLines) * display.size();
        int width = iconSize + 2 + longestLine.map(MC.font::width).orElseGet(() ->
                MC.font.width(display.getFirst())
        );
        int height = Math.max(effectsHeight, iconAndSecondsHeight);

        x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
        y = transformY(y, height, scale);

        float startY = Math.round(y + (iconAndSecondsHeight / 2F) - (effectsHeight / 2F));
        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y - spacingBetweenLines, y + height, scale);
        }

        // move the overflowing part to the buttonLocation boxAdd commentMore actions
        if (effectsHeight > iconAndSecondsHeight) {
            int add = Math.abs(effectsHeight - iconAndSecondsHeight) / 2;
            y += add;
            startY += add;
        }

        Entity entity = null;
        DeployableManager.DeployableEntry activeDeployable = DeployableManager.getInstance().getActiveDeployable();
        if (activeDeployable != null) {
            UUID uuidOfActiveDep = activeDeployable.getUuid();
            if (uuidOfActiveDep != null) {
                entity = Utils.getEntityFromUUID(uuidOfActiveDep);
            }
        } else if (buttonLocation != null) {
            entity = DeployableManager.DUMMY_ARMOR_STAND;
        }

        if (entity instanceof ArmorStand armorStand) {
            drawDeployableArmorStand(graphics, armorStand, x + 1, y + 4);
        } else {
            graphics.blit(RenderType::guiTextured, deployable.getResourceLocation(), (int) x, (int) y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        String secondsString = String.format("§e%ss", seconds);
        DrawUtils.drawText(
                graphics,
                secondsString,
                Math.round(x + (iconSize / 2F) - (MC.font.width(secondsString) / 2F)),
                y + iconSize,
                ColorCode.WHITE.getColor(255)
        );

        for (int i = 0; i < display.size(); i++) {
            DrawUtils.drawText(
                    graphics,
                    display.get(i),
                    x + iconSize + 2,
                    startY + (i * (MC.font.lineHeight + spacingBetweenLines)),
                    ColorCode.WHITE.getColor(255)
            );
        }
    }

    public void onRender() {
        if (guiToOpen == EnumUtils.GUIType.MAIN) {
            MC.setScreen(new SkyblockAddonsGui(guiPageToOpen, guiTabToOpen));
        } else if (guiToOpen == EnumUtils.GUIType.EDIT_LOCATIONS) {
            MC.setScreen(new LocationEditGui(guiPageToOpen, guiTabToOpen));
        } else if (guiToOpen == EnumUtils.GUIType.SETTINGS) {
            if (guiFeatureToOpen == Feature.ENCHANTMENT_LORE_PARSING) {
                MC.setScreen(new EnchantmentSettingsGui(1, guiPageToOpen, guiTabToOpen));
            } else {
                MC.setScreen(new SettingsGui(guiFeatureToOpen, 1, guiPageToOpen, guiTabToOpen, null));
            }
        } else if (guiToOpen == EnumUtils.GUIType.WARP) {
            MC.setScreen(new IslandWarpGui());
        }
        guiToOpen = null;
    }

    public void setGuiToOpen(EnumUtils.GUIType guiToOpen, int page, EnumUtils.GuiTab tab) {
        this.guiToOpen = guiToOpen;
        guiPageToOpen = page;
        guiTabToOpen = tab;
    }

    public void setGuiToOpen(EnumUtils.GUIType guiToOpen, int page, EnumUtils.GuiTab tab, Feature feature) {
        setGuiToOpen(guiToOpen, page, tab);
        guiFeatureToOpen = feature;
    }

    // TODO improve xAllignment
    public float transformX(float x, int width, float scale, boolean xAllignment) {
        double minecraftScale = MC.getWindow().getGuiScale();
        if (!xAllignment) {
            x -= width / 2F * scale;
        } else {
            // TODO x -= dummyWidth / 2 * scale (Feature refactor)
            // TODO allignment to right edge of screen
            // x -= width * scale;
        }
        x = (float) (Math.round(x * minecraftScale) / minecraftScale);
        return x / scale;
    }

    public float transformY(float y, int height, float scale) {
        double minecraftScale = MC.getWindow().getGuiScale();
        y -= height / 2F * scale;
        y = (float) (Math.round(y * minecraftScale) / minecraftScale);
        return y / scale;
    }

    public void onRenderWorld(WorldRenderContext worldRenderContext) {
        HealingCircleManager.renderHealingCircleOverlays(worldRenderContext);
    }

    // FIXME
    private void drawDeployableArmorStand(GuiGraphics graphics, ArmorStand deployableArmorStand, float x, float y) {
        float prevRenderYawOffset = deployableArmorStand.yBodyRot;
        float prevPrevRenderYawOffset = deployableArmorStand.yBodyRotO;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x + 12.5F, y + 50F, 50F);
        poseStack.scale(-25F, 25F, 25F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(135.0F));
        Lighting.setupFor3DItems();
        poseStack.mulPose(Axis.YP.rotationDegrees(-135.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(22.0F));

        EntityRenderDispatcher renderDispatcher = MC.getEntityRenderDispatcher();
        renderDispatcher.setRenderShadow(false);

        deployableArmorStand.setInvisible(true);
        float yaw = System.currentTimeMillis() % 1750 / 1750F * 360F;
        deployableArmorStand.yBodyRot = yaw;
        deployableArmorStand.yBodyRotO = yaw;

        graphics.drawSpecial(source -> renderDispatcher.render(deployableArmorStand, 0.0D, 0.0D, 0.0D, 1.0F, poseStack, source, LightTexture.FULL_BRIGHT));
        renderDispatcher.setRenderShadow(true);

        Lighting.setupForFlatItems();
        poseStack.popPose();

        deployableArmorStand.yBodyRot = prevRenderYawOffset;
        deployableArmorStand.yBodyRotO = prevPrevRenderYawOffset;
    }

    private void drawEntity(GuiGraphics graphics, LivingEntity entity, float x, float y, float yaw) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 50.0F);
        poseStack.scale(-25.0F, 25.0F, 25.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(15.0F));
        Lighting.setupForEntityInInventory();

        entity.setYRot(yaw);
        entity.yRotO = yaw;
        entity.yHeadRot = yaw;
        entity.yHeadRotO = yaw;

        EntityRenderDispatcher renderDispatcher = MC.getEntityRenderDispatcher();
        renderDispatcher.overrideCameraOrientation(Axis.YN.rotationDegrees(-180.0F));
        renderDispatcher.setRenderShadow(false);
        graphics.drawSpecial(source -> renderDispatcher.render(entity, 0.0D, 0.0D, 0.0D, 1.0F, poseStack, source, LightTexture.FULL_BRIGHT));
        renderDispatcher.setRenderShadow(true);

        poseStack.popPose();
    }

    public void setTitleFeature(Feature feature) {
        titleFeature = feature;
        if (feature != null) {
            if (titleResetTask != null) {
                titleResetTask.cancel();
            }

            titleResetTask = main.getScheduler().scheduleTask(
                    scheduledTask -> main.getRenderListener().setTitleFeature(null),
                    Feature.WARNING_TIME.numberValue().intValue() * 20,
                    0,
                    true,
                    false
            );
        }
    }

    public void setSubtitleFeature(Feature feature) {
        subtitleFeature = feature;
        if (feature != null) {
            if (subtitleResetTask != null) {
                subtitleResetTask.cancel();
            }

            subtitleResetTask = main.getScheduler().scheduleTask(
                    scheduledTask -> main.getRenderListener().setSubtitleFeature(null),
                    Feature.WARNING_TIME.numberValue().intValue() * 20,
                    0,
                    true,
                    false
            );
        }
    }

    private enum DamageDisplayItem {
        HYPERION(ItemUtils.createItemStack(Items.IRON_SWORD, "§6Hyperion", "HYPERION", false)),
        VALKYRIE(ItemUtils.createItemStack(Items.IRON_SWORD, "§6Valkyrie", "VALKYRIE", false)),
        ASTRAEA(ItemUtils.createItemStack(Items.IRON_SWORD, "§6Astraea", "ASTRAEA", false)),
        SCYLLA(ItemUtils.createItemStack(Items.IRON_SWORD, "§6Scylla", "SCYLLA", false)),
        BAT_WAND(new ItemStack(Blocks.ALLIUM, 1)),
        STARRED_BAT_WAND(new ItemStack(Blocks.ALLIUM, 1)),
        MIDAS_STAFF(ItemUtils.createItemStack(Items.GOLDEN_SHOVEL, "§6Midas Staff", "MIDAS_STAFF", false));

        private final ItemStack itemStack;

        DamageDisplayItem(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public static ItemStack getByID(String id) {
            for (DamageDisplayItem displayItem : DamageDisplayItem.values()) {
                if (displayItem.name().equals(id))
                    return displayItem.itemStack;
            }
            return null;
        }
    }

}