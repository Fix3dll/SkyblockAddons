package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.*;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.feature.FeatureGuiData;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.features.dungeon.DungeonClass;
import codes.biscuit.skyblockaddons.features.dungeon.DungeonMilestone;
import codes.biscuit.skyblockaddons.features.*;
import codes.biscuit.skyblockaddons.features.TrevorTrapperTracker;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonTracker;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonType;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonsSince;
import codes.biscuit.skyblockaddons.features.dungeonmap.DungeonMapManager;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleManager;
import codes.biscuit.skyblockaddons.features.deployables.Deployable;
import codes.biscuit.skyblockaddons.features.deployables.DeployableManager;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerBoss;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerDrop;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.features.spooky.CandyType;
import codes.biscuit.skyblockaddons.features.spooky.SpookyEventManager;
import codes.biscuit.skyblockaddons.features.tablist.TabListParser;
import codes.biscuit.skyblockaddons.features.tablist.TabListRenderer;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonLocation;
import codes.biscuit.skyblockaddons.gui.screens.EnchantmentSettingsGui;
import codes.biscuit.skyblockaddons.gui.screens.IslandWarpGui;
import codes.biscuit.skyblockaddons.gui.screens.LocationEditGui;
import codes.biscuit.skyblockaddons.gui.screens.SettingsGui;
import codes.biscuit.skyblockaddons.gui.screens.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.core.updater.Updater;
import codes.biscuit.skyblockaddons.core.scheduler.ScheduledTask;
import codes.biscuit.skyblockaddons.mixins.hooks.FontRendererHook;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenTexturedShader;
import codes.biscuit.skyblockaddons.utils.*;
import codes.biscuit.skyblockaddons.utils.EnumUtils.AutoUpdateMode;
import codes.biscuit.skyblockaddons.utils.EnumUtils.DeployableDisplayStyle;
import codes.biscuit.skyblockaddons.utils.EnumUtils.PetItemStyle;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.GuiNotification;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

import static net.minecraft.client.gui.Gui.icons;

public class RenderListener {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getMinecraft();
    public static final ICamera CAMERA = new Frustum();

    private static final ItemStack BONE_ITEM = new ItemStack(Items.bone);
    private static final ResourceLocation BARS = new ResourceLocation("skyblockaddons", "barsV2.png");
    private static final ResourceLocation DEFENCE_VANILLA = new ResourceLocation("skyblockaddons", "defence.png");
    private static final ResourceLocation TICKER_SYMBOL = new ResourceLocation("skyblockaddons", "ticker.png");
    private static final ResourceLocation ENDERMAN_ICON = new ResourceLocation("skyblockaddons", "icons/enderman.png");
    private static final ResourceLocation ENDERMAN_GROUP_ICON = new ResourceLocation("skyblockaddons", "icons/endermangroup.png");
    private static final ResourceLocation SIRIUS_ICON = new ResourceLocation("skyblockaddons", "icons/sirius.png");
    private static final ResourceLocation SUMMONING_EYE_ICON = new ResourceLocation("skyblockaddons", "icons/summoningeye.png");
    private static final ResourceLocation ZEALOTS_PER_EYE_ICON = new ResourceLocation("skyblockaddons", "icons/zealotspereye.png");
    private static final ResourceLocation SLASH_ICON = new ResourceLocation("skyblockaddons", "icons/slash.png");
    private static final ResourceLocation IRON_GOLEM_ICON = new ResourceLocation("skyblockaddons", "icons/irongolem.png");
    private static final ResourceLocation FARM_ICON = new ResourceLocation("skyblockaddons", "icons/farm.png");
    private static final ResourceLocation RIFTSTALKER_BLOODFIEND = new ResourceLocation("skyblockaddons", "vampire.png");

    private static final ItemStack WATER_BUCKET = new ItemStack(Items.water_bucket);
    private static final ItemStack ROCK_PET = ItemUtils.createSkullItemStack(
            "§7[Lvl 100] §6Rock",
            null,
            "988354a0-b787-3ca5-b782-d0db5e7b876a",
            "7df8aab57136df2296c7c6f969ff25d58116fe2ec59b96a85ba4927e1f6779e6"
    );
    private static final ItemStack DOLPHIN_PET = ItemUtils.createSkullItemStack(
            "§7[Lvl 100] §6Dolphin",
            null,
            "9001c25b-f0ff-3748-82c5-7bd117935ce2",
            "1415d2c543e34bb88ede94d79b9427691fc9be72daad8831a9ef297180546e18"
    );
    private static final ItemStack CHEST = new ItemStack(Item.getItemFromBlock(Blocks.chest));
    private static final ItemStack SKULL = ItemUtils.createSkullItemStack(
            "Skull",
            null,
            "c659cdd4-e436-4977-a6a7-d5518ebecfbb",
            "1ae3855f952cd4a03c148a946e3f812a5955ad35cbcb52627ea4acd47d3081"
    );
    private static final ItemStack GREEN_CANDY = ItemUtils.createSkullItemStack(
            "Green Candy",
            "GREEN_CANDY",
            "e5190c90-5144-3e4e-a545-8499ea3503ca",
            "e31c0bd76a655d5d8fea5b06daaf1fb8d8060bf0823ebbc6eb6f99c8ee5a35aa"
    );
    private static final ItemStack PURPLE_CANDY = ItemUtils.createSkullItemStack(
            "Purple Candy",
            "PURPLE_CANDY",
            "60a5c7bc-a65b-3772-889f-8831d4329fc4",
            "91611d874e874e322a1199b3b7b9e934bbb0dbed587ee8fcd6ccc1b07e281651"
    );
    private static final ItemStack DUMMY_THUNDER_BOTTLE = ItemUtils.createSkullItemStack(
            "§5Empty Thunder Bottle",
            Collections.emptyList(),
            "THUNDER_IN_A_BOTTLE_EMPTY",
            "552fdcec-5679-3b0e-a48e-84dc83b6dc6e",
            "ab3616f523bf5a00bf2b3e9fb8314c47390b90a5ca68c5db3684acd567430cd3"
    );

    private static final SlayerArmorProgress[] DUMMY_PROGRESSES = new SlayerArmorProgress[] {
            new SlayerArmorProgress(new ItemStack(Items.diamond_boots)),
            new SlayerArmorProgress(new ItemStack(Items.chainmail_leggings)),
            new SlayerArmorProgress(new ItemStack(Items.diamond_chestplate)),
            new SlayerArmorProgress(new ItemStack(Items.leather_helmet))
    };

    private static final List<ItemDiff> DUMMY_PICKUP_LOG = Collections.unmodifiableList(Arrays.asList(
            new ItemDiff(ColorCode.DARK_PURPLE + "Forceful Ember Chestplate", 1, new ItemStack(Items.chainmail_chestplate)),
            new ItemDiff("Boat", -1, new ItemStack(Items.boat)),
            new ItemDiff(ColorCode.BLUE + "Aspect of the End", 1, new ItemStack(Items.diamond_sword))
    ));

    private static final Pattern DUNGEON_STAR_PATTERN = Pattern.compile("(?:(?:§[a-f0-9])?✪)+(?:§[a-f0-9]?[➊-➒])?");

    private static EntityZombie revenant;
    private static EntitySpider tarantula;
    private static EntityCaveSpider caveSpider;
    private static EntityWolf sven;
    private static EntityEnderman enderman;
    private static EntityBlaze inferno;
    private static EntityOtherPlayerMP riftstalker;

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

    /**
     * Render overlays and warnings for clients without labymod.
     */
    @SubscribeEvent()
    public void onRenderRegular(RenderGameOverlayEvent.Post e) {
        if ((!main.isUsingLabymod() || MC.ingameGUI instanceof GuiIngameForge)) {
            if (e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE
                    || e.type == RenderGameOverlayEvent.ElementType.JUMPBAR) {
                if (main.getUtils().isOnSkyblock()) {
                    renderOverlays();
                    renderWarnings(e.resolution);
                } else {
                    renderTimersOnly();
                }
                drawUpdateMessage();
            }
        }
    }

    /**
     * Render overlays and warnings for clients with labymod.
     * Labymod creates its own ingame gui and replaces the forge one, and changes the events that are called.
     * This is why the above method can't work for both.
     */
    @SubscribeEvent()
    public void onRenderLabyMod(RenderGameOverlayEvent e) {
        if (e.type == null && main.isUsingLabymod()) {
            if (main.getUtils().isOnSkyblock()) {
                renderOverlays();
                renderWarnings(e.resolution);
            } else {
                renderTimersOnly();
            }
            drawUpdateMessage();
        }
    }

    @SubscribeEvent()
    public void onRenderLivingName(RenderLivingEvent.Specials.Pre<EntityLivingBase> e) {
        Entity entity = e.entity;
        if (entity.hasCustomName()) {
            if (Feature.MINION_DISABLE_LOCATION_WARNING.isEnabled()) {
                if (entity.getCustomNameTag().startsWith("§cThis location isn't perfect! :(")) {
                    e.setCanceled(true);
                }
                if (entity.getCustomNameTag().startsWith("§c/!\\")) {
                    for (Entity listEntity : MC.theWorld.loadedEntityList) {
                        if (listEntity.hasCustomName()
                                && listEntity.getCustomNameTag().startsWith("§cThis location isn't perfect! :(")
                                && listEntity.posX == entity.posX
                                && listEntity.posZ == entity.posZ
                                && listEntity.posY + 0.375 == entity.posY) {
                            e.setCanceled(true);
                            break;
                        }
                    }
                }
            }

            if (Feature.HIDE_SVEN_PUP_NAMETAGS.isEnabled()) {
                if (entity instanceof EntityArmorStand
                        && entity.hasCustomName()
                        && entity.getCustomNameTag().contains("Sven Pup")) {
                    e.setCanceled(true);
                }
            }
        }
    }

    /**
     * I have an option so you can see dark auction timer and farm event timer in other games so that's why.
     */
    private void renderTimersOnly() {
        if (!(MC.currentScreen instanceof LocationEditGui) && !(MC.currentScreen instanceof GuiNotification)) {
            GlStateManager.disableBlend();
            if (Feature.DARK_AUCTION_TIMER.isEnabled(FeatureSetting.DARK_AUCTION_TIMER_IN_OTHER_GAMES)) {
                float scale = Feature.DARK_AUCTION_TIMER.getGuiScale();
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                drawText(Feature.DARK_AUCTION_TIMER, scale,null);
                GlStateManager.popMatrix();
            }
            if (Feature.FARM_EVENT_TIMER.isEnabled(FeatureSetting.FARM_EVENT_TIMER_IN_OTHER_GAMES)) {
                float scale = Feature.FARM_EVENT_TIMER.getGuiScale();
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                drawText(Feature.FARM_EVENT_TIMER, scale,null);
                GlStateManager.popMatrix();
            }
        }
    }

    /**
     * This renders all the title/subtitle warnings from features.
     */
    private void renderWarnings(ScaledResolution scaledResolution) {
        if (MC.theWorld == null || MC.thePlayer == null || !main.getUtils().isOnSkyblock()) {
            return;
        }

        int scaledWidth = scaledResolution.getScaledWidth();
        int scaledHeight = scaledResolution.getScaledHeight();
        if (titleFeature != null) {
            String translationKey = null;
            switch (titleFeature) {
                case FULL_INVENTORY_WARNING:
                    translationKey = "messages.fullInventory";
                    break;
                case SUMMONING_EYE_ALERT:
                    translationKey = "messages.summoningEyeFound";
                    break;
                case SPECIAL_ZEALOT_ALERT:
                    translationKey = "messages.specialZealotFound";
                    break;
                case LEGENDARY_SEA_CREATURE_WARNING:
                    translationKey = "messages.legendarySeaCreatureWarning";
                    break;
                case BOSS_APPROACH_ALERT:
                    translationKey = "messages.bossApproaching";
                    break;
                case FETCHUR_TODAY:
                    translationKey = "messages.fetchurWarning";
                    break;
                case BROOD_MOTHER_ALERT:
                    translationKey = "messages.broodMotherWarning";
                    break;
                case BAL_BOSS_ALERT:
                    translationKey = "messages.balBossWarning";
                    break;
            }
            if (translationKey != null) {
                String text = Translations.getMessage(translationKey);
                int stringWidth = MC.fontRendererObj.getStringWidth(text);

                float scale = 4; // Scale is normally 4, but if its larger than the screen, scale it down...
                if (stringWidth * scale > (scaledWidth * 0.9F)) {
                    scale = (scaledWidth * 0.9F) / (float) stringWidth;
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale); // TODO Check if changing this scale breaks anything...

                FontRendererHook.setupFeatureFont(titleFeature);
                DrawUtils.drawText(
                        text,
                        (float) (-MC.fontRendererObj.getStringWidth(text) / 2),
                        -20.0F,
                        titleFeature.getColor()
                );
                FontRendererHook.endFeatureFont();

                GlStateManager.popMatrix();
                GlStateManager.popMatrix();
            }
        }
        if (subtitleFeature != null) {
            String text = null;
            switch (subtitleFeature) {
                case MINION_STOP_WARNING:
                    text = Translations.getMessage("messages.minionCannotReach", cannotReachMobName);
                    break;
                case MINION_FULL_WARNING:
                    text = Translations.getMessage("messages.minionIsFull");
                    break;
                case NO_ARROWS_LEFT_ALERT:
                    if (arrowsType != null) {
                        if (arrowsLeft != -1) {
                            text = Translations.getMessage("messages.onlyFewArrowsLeft", arrowsLeft, arrowsType);
                        } else {
                            text = Translations.getMessage("messages.noArrowsLeft", arrowsType);
                        }
                    }
                    break;
            }

            if (text != null) {

                int stringWidth = MC.fontRendererObj.getStringWidth(text);

                float scale = 2; // Scale is normally 2, but if it's larger than the screen, scale it down...
                if (stringWidth * scale > (scaledWidth * 0.9F)) {
                    scale = (scaledWidth * 0.9F) / (float) stringWidth;
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale);  // TODO Check if changing this scale breaks anything...

                FontRendererHook.setupFeatureFont(subtitleFeature);
                DrawUtils.drawText(
                        text,
                        -MC.fontRendererObj.getStringWidth(text) / 2F,
                        -23.0F,
                        subtitleFeature.getColor()
                );
                FontRendererHook.endFeatureFont();

                GlStateManager.popMatrix();
                GlStateManager.popMatrix();
            }
        }
    }

    /**
     * This renders all the gui elements (bars, icons, texts, skeleton bar, etc.).
     */
    private void renderOverlays() {
        if (!(MC.currentScreen instanceof LocationEditGui) && !(MC.currentScreen instanceof GuiNotification)) {
            GlStateManager.disableBlend();

            for (Feature feature : Feature.getGuiFeatures()) {
                if (feature.isEnabled()) {
                    if (feature == Feature.SKELETON_BAR && !main.getInventoryUtils().isWearingSkeletonHelmet()) {
                        continue;
                    }
                    if (feature == Feature.HEALTH_UPDATES && main.getPlayerListener().getActionBarParser().getHealthUpdate() == null) {
                        continue;
                    }

                    float scale = feature.getGuiScale();
                    drawFeature(feature, scale, null);
                }
            }
        }
    }

    public void drawFeature(Feature feature, float scale, ButtonLocation buttonLocation) {
        FeatureGuiData guiFeatureData = feature.getFeatureGuiData();
        if (guiFeatureData != null && guiFeatureData.getDrawType() != null) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            switch (guiFeatureData.getDrawType()) {
                case SKELETON_BAR:
                    drawSkeletonBar(scale, buttonLocation);
                    break;
                case BAR:
                    drawBar(feature, scale, buttonLocation);
                    break;
                case TEXT:
                    drawText(feature, scale, buttonLocation);
                    break;
                case PICKUP_LOG:
                    drawItemPickupLog(scale, buttonLocation);
                    break;
                case DEFENCE_ICON:
                    drawIcon(scale, buttonLocation);
                    break;
                case SLAYER_ARMOR_PROGRESS:
                    drawSlayerArmorProgress(scale, buttonLocation);
                    break;
                case DEPLOYABLE_DISPLAY:
                    drawDeployableStatus(scale, buttonLocation);
                    break;
                case TICKER:
                    drawScorpionFoilTicker(scale, buttonLocation);
                    break;
                case BAIT_LIST_DISPLAY:
                    drawBaitList(scale, buttonLocation);
                    break;
                case DUNGEONS_MAP:
                    DungeonMapManager.drawDungeonsMap(MC, scale, buttonLocation);
                    break;
                case SLAYER_TRACKERS:
                    drawSlayerTrackers(feature, scale, buttonLocation);
                    break;
                case DRAGON_STATS_TRACKER:
                    drawDragonTrackers(scale, buttonLocation);
                    break;
                case PROXIMITY_INDICATOR:
                    TrevorTrapperTracker.drawTrackerLocationIndicator(scale, buttonLocation);
                    break;
                case PET_DISPLAY:
                    drawPetDisplay(scale, buttonLocation);
                    break;
            }
            GlStateManager.popMatrix();
        }
    }

    /**
     * This draws all Skyblock Addons Bars, including the Health, Mana, Drill, and Skill XP bars
     *
     * @param feature        for which to render the bars
     * @param scale          the scale of the feature
     * @param buttonLocation the resizing gui, if present
     */
    public void drawBar(Feature feature, float scale, ButtonLocation buttonLocation) {
        // The fill of the bar from 0 to 1
        float fill;
        // Whether the player has absorption hearts
        boolean hasAbsorption = false;
        // Float value to scale width
        float widthScale = 1.0F;

        switch (feature) {
            case MANA_BAR:
                fill = PlayerStats.MANA.getValue() / PlayerStats.MAX_MANA.getValue();
                break;
            case DRILL_FUEL_BAR:
                fill = PlayerStats.FUEL.getValue() / PlayerStats.MAX_FUEL.getValue();
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
                fill = PlayerStats.HEALTH.getValue() / PlayerStats.MAX_HEALTH.getValue();
                break;
            default:
                return;
        }

        if (fill > 1) fill = 1;

        float x = feature.getActualX();
        float y = feature.getActualY();
        float scaleX = feature.getFeatureData().getSizesX();
        float scaleY = feature.getFeatureData().getSizesY();
        GlStateManager.scale(scaleX, scaleY, 1);

        x = transformX(x, 71, scale * scaleX, false);
        y = transformY(y, 5, scale * scaleY);

        // Render the button resize box if necessary
        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + 71, y, y + 5, scale, scaleX, scaleY);
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
                if (buttonLocation == null && !ItemUtils.isDrill(MC.thePlayer.getHeldItem())) return;
                break;
            case HEALTH_BAR:
                if (feature.isEnabled(FeatureSetting.CHANGE_BAR_COLOR_WITH_POTIONS) && MC.thePlayer != null) {
                    if (MC.thePlayer.isPotionActive(19/* Poison */)) {
                        color = ColorUtils.getDummySkyblockColor(
                                ColorCode.DARK_GREEN.getColor(),
                                feature.isChroma()
                        );
                    } else if (MC.thePlayer.isPotionActive(20/* Wither */)) {
                        color = ColorUtils.getDummySkyblockColor(
                                ColorCode.DARK_GRAY.getColor(),
                                feature.isChroma()
                        );
                    } else if (MC.thePlayer.isPotionActive(22) /* Absorption */) {
                        if (PlayerStats.HEALTH.getValue() > PlayerStats.MAX_HEALTH.getValue()) {
                            fill = PlayerStats.MAX_HEALTH.getValue() / PlayerStats.HEALTH.getValue();
                            hasAbsorption = true;
                        }
                    }
                }

                if (main.getUtils().isOnRift()) {
                    float maxCurrentHealth = PlayerStats.MAX_RIFT_HEALTH.getValue();
                    fill = PlayerStats.HEALTH.getValue() / maxCurrentHealth;

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
        }

        main.getUtils().enableStandardGLOptions();
        // Draw the actual bar
        drawMultiLayeredBar(color, x, y, fill, hasAbsorption, widthScale);

        main.getUtils().restoreGLOptions();
    }

    /**
     * Draws a multitextured bar:
     * Begins by coloring and rendering the empty bar.
     * Then, colors and renders the full bar up to the fraction {@param fill}.
     * Then, overlays the absorption portion of the bar in gold if the player has absorption hearts
     * Then, overlays (and does not color) an additional texture centered on the current progress of the bar.
     * Then, overlays (and does not color) a final style texture over the bar
     * @param color the color with which to render the bar
     * @param x the x position of the bar
     * @param y the y position of the bar
     * @param fill the fraction (from 0 to 1) of the bar that's full
     * @param hasAbsorption {@code true} if the player has absorption hearts
     */
    private void drawMultiLayeredBar(SkyblockColor color, float x, float y, float fill, boolean hasAbsorption, float widthScale) {
        int barHeight = 5;
        float barWidth = 71 * widthScale;
        float barFill = barWidth * fill;
        MC.getTextureManager().bindTexture(BARS);
        if (color.getColor() == ColorCode.BLACK.getColor()) { // too dark normally
            GlStateManager.color(0.25F, 0.25F, 0.25F, ColorUtils.getAlpha(color.getColor()) / 255F);
        } else { // A little darker for contrast...
            ColorUtils.bindColor(color.getColor(), 0.9F);
        }
        // If chroma, draw the empty bar much darker than the filled bar
        if (color.drawMulticolorUsingShader()) {
            GlStateManager.color(.5F, .5F, .5F);
            ShaderManager.getInstance().enableShader(ChromaScreenTexturedShader.class);
        }
        // Empty bar first
        DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 1, 1, barWidth, barHeight, 80, 50);

        if (color.drawMulticolorUsingShader()) {
            ColorUtils.bindWhite();
            ShaderManager.getInstance().enableShader(ChromaScreenTexturedShader.class);
        }

        // Filled bar next
        if (fill != 0) {
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 1, 7, barFill, barHeight, 80, 50);
        }
        // Disable coloring
        if (color.drawMulticolorUsingShader()) {
            ShaderManager.getInstance().disableShader();
        }

        // Overlay absorption health if needed
        if (hasAbsorption) {
            ColorUtils.bindColor(ColorCode.GOLD.getColor());
            DrawUtils.drawModalRectWithCustomSizedTexture(x + barFill, y, barFill + 1, 7, barWidth - barFill, barHeight, 80, 50);
        }
        ColorUtils.bindWhite();

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
            DrawUtils.drawModalRectWithCustomSizedTexture(x + startX, y, 1 + startTexX, 24, endTexX, barHeight, 80, 50);
        }
        // Overlay uncolored bar display next (texture packs can use this to overlay their own static bar colors)
        DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 1, 13, barWidth, barHeight, 80, 50);
    }

    /**
     * Renders the messages from the SkyblockAddons Updater
     */
    private void drawUpdateMessage() {
        Updater updater = main.getUpdater();

        if (updater.hasUpdate() && !updateMessageDisplayed) {
            String message = updater.getMessageToRender();

            if (message != null && Feature.AUTO_UPDATE.getValue() == AutoUpdateMode.UPDATE_OFF) {
                String[] textList = main.getUtils().wrapSplitText(message, 36);

                int halfWidth = new ScaledResolution(MC).getScaledWidth() / 2;
                Gui.drawRect(
                        halfWidth - 110,
                        20,
                        halfWidth + 110,
                        53 + textList.length * 10,
                        ColorUtils.getDefaultBlue(140)
                );
                String title = SkyblockAddons.MOD_NAME;
                GlStateManager.pushMatrix();
                float scale = 1.5F;
                GlStateManager.scale(scale, scale, 1);
                DrawUtils.drawCenteredText(title, (int) (halfWidth / scale), (int) (30 / scale), ColorCode.WHITE.getColor());
                GlStateManager.popMatrix();
                int y = 45;
                for (String line : textList) {
                    DrawUtils.drawCenteredText(line, halfWidth, y, ColorCode.WHITE.getColor());
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
    public void drawSkeletonBar(float scale, ButtonLocation buttonLocation) {
        float x = Feature.SKELETON_BAR.getActualX();
        float y = Feature.SKELETON_BAR.getActualY();
        int bones = 0;
        if (!(MC.currentScreen instanceof LocationEditGui)) {
            for (Entity listEntity : MC.theWorld.loadedEntityList) {
                if (listEntity instanceof EntityItem
                        && listEntity.ridingEntity instanceof EntityArmorStand
                        && listEntity.ridingEntity.isInvisible()
                        && listEntity.getDistanceToEntity(MC.thePlayer) <= 8) {
                    bones++;
                }
            }
        } else {
            bones = 3;
        }
        if (bones > 3) bones = 3;

        int height = 16;
        int width = 3 * 16;

        x = transformX(x, width, scale, false);
        y = transformY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

        for (int boneCounter = 0; boneCounter < bones; boneCounter++) {
            renderItem(BONE_ITEM, x + boneCounter * 16, y);
        }

        main.getUtils().restoreGLOptions();
    }

    /**
     * This renders the skeleton bar.
     */
    public void drawScorpionFoilTicker(float scale, ButtonLocation buttonLocation) {
        if (buttonLocation != null || main.getPlayerListener().getTickers() != -1) {
            float x = Feature.TICKER_CHARGES_DISPLAY.getActualX();
            float y = Feature.TICKER_CHARGES_DISPLAY.getActualY();

            int height = 9;
            int width = 3 * 11 + 9;

            x = transformX(x, width, scale, false);
            y = transformY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            }

            main.getUtils().enableStandardGLOptions();

            int maxTickers = (buttonLocation == null) ? main.getPlayerListener().getMaxTickers() : 4;
            for (int tickers = 0; tickers < maxTickers; tickers++) {
                MC.getTextureManager().bindTexture(TICKER_SYMBOL);
                GlStateManager.enableAlpha();
                if (tickers < (buttonLocation == null ? main.getPlayerListener().getTickers() : 3)) {
                    DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 0, 0, 9, 9, 18, 9, false);
                } else {
                    DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 9, 0, 9, 9, 18, 9, false);
                }
            }

            main.getUtils().restoreGLOptions();
        }
    }

    /**
     * This renders the defence icon.
     */
    public void drawIcon(float scale, ButtonLocation buttonLocation) {
        // There is no defense stat on Rift Dimension
        if (main.getUtils().isOnRift())
            return;

        if (Feature.DEFENCE_ICON.isDisabled(FeatureSetting.USE_VANILLA_TEXTURE)) {
            MC.getTextureManager().bindTexture(icons);
        } else {
            MC.getTextureManager().bindTexture(DEFENCE_VANILLA);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // The height and width of this element (box not included)
        int height = 9;
        int width = 9;
        float x = Feature.DEFENCE_ICON.getActualX();
        float y = Feature.DEFENCE_ICON.getActualY();
        x = transformX(x, width, scale, false);
        y = transformY(y, height, scale);

        main.getUtils().enableStandardGLOptions();

        if (buttonLocation == null) {
            MC.ingameGUI.drawTexturedModalRect(x, y, 34, 9, width, height);
        } else {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            buttonLocation.drawTexturedModalRect(x, y, 34, 9, width, height);
        }

        main.getUtils().restoreGLOptions();
    }

    /**
     * This renders all the different types gui text elements.
     */
    public void drawText(Feature feature, float scale, ButtonLocation buttonLocation) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        String text;
        boolean onRift = main.getUtils().isOnRift();
        int color = feature.getColor();

        switch (feature) {
            case MANA_TEXT:
                text = TextUtils.formatNumber(PlayerStats.MANA.getValue()) + "/"
                        + TextUtils.formatNumber(PlayerStats.MAX_MANA.getValue())
                        + (feature.isEnabled(FeatureSetting.MANA_TEXT_ICON) ? "✎" : "");
                break;

            case OVERFLOW_MANA:
                if (PlayerStats.OVERFLOW_MANA.getValue() == 0 && buttonLocation == null) return;
                text = TextUtils.formatNumber(PlayerStats.OVERFLOW_MANA.getValue()) + "ʬ";
                break;

            case HEALTH_TEXT:
                if (feature.isEnabled(FeatureSetting.HIDE_HEALTH_TEXT_ON_RIFT) && onRift) return;

                // Dividing with 2 for show heart value instead of health value. 1 heart == 2 health
                boolean shouldHeart = feature.isEnabled(FeatureSetting.HEART_INSTEAD_HEALTH_ON_RIFT) && onRift;

                text = TextUtils.formatNumber(PlayerStats.HEALTH.getValue() / (shouldHeart ? 2F : 1F)) + "/";
                if (main.getUtils().isOnRift()) {
                    text += TextUtils.formatNumber(PlayerStats.MAX_RIFT_HEALTH.getValue() / (shouldHeart ? 2F : 1F));
                } else {
                    text += TextUtils.formatNumber(PlayerStats.MAX_HEALTH.getValue());
                }
                if (feature.isEnabled(FeatureSetting.HEALTH_TEXT_ICON)) {
                    text +=  "❤";
                }

                break;

            case CRIMSON_ARMOR_ABILITY_STACKS:
                if (buttonLocation != null) {
                    text = "Hydra Strike ⁑ 1";
                } else {
                    text = getCrimsonArmorAbilityStacks();
                }
                if (text == null) return;
                break;

            case DEFENCE_TEXT:
                if (onRift) return;
                text = TextUtils.formatNumber(PlayerStats.DEFENCE.getValue())
                        + (feature.isDisabled(FeatureSetting.DEFENCE_TEXT_ICON) ? "❈" : "");
                break;

            case OTHER_DEFENCE_STATS:
                text = main.getPlayerListener().getActionBarParser().getOtherDefense();
                if (text == null || text.isEmpty()) {
                    if (buttonLocation != null) {
                        text = "|||  T3!";
                    } else {
                        return;
                    }
                }
                break;

            case EFFECTIVE_HEALTH_TEXT:
                if (onRift) return;
                text = TextUtils.formatNumber(
                        Math.round(PlayerStats.HEALTH.getValue() * (1 + PlayerStats.DEFENCE.getValue() / 100F))
                ) + (feature.isEnabled(FeatureSetting.EFFECTIVE_HEALTH_TEXT_ICON) ? "❤" : "");
                break;

            case DRILL_FUEL_TEXT:
                boolean heldDrill = MC.thePlayer != null && ItemUtils.isDrill(MC.thePlayer.getHeldItem());

                if (heldDrill) {
                    text = TextUtils.formatNumber(PlayerStats.FUEL.getValue()) + "/";
                    if (feature.isEnabled(FeatureSetting.ABBREVIATE_DRILL_FUEL_DENOMINATOR)) {
                        text += TextUtils.abbreviate((int) PlayerStats.MAX_FUEL.getValue());
                    } else {
                        text += TextUtils.formatNumber(PlayerStats.MAX_FUEL.getValue());
                    }
                } else if (buttonLocation != null){
                    text = TextUtils.formatNumber(3000) + "/" + TextUtils.formatNumber(3000);
                } else {
                    return;
                }
                break;

            case DEFENCE_PERCENTAGE:
                if (onRift) return;
                double doubleDefence = PlayerStats.DEFENCE.getValue();
                double percentage = doubleDefence / (doubleDefence + 100) * 100; //Taken from https://wiki.hypixel.net/Defense
                BigDecimal bigDecimal = new BigDecimal(percentage).setScale(1, RoundingMode.HALF_UP);
                text = bigDecimal + "%";
                break;

            case SPEED_PERCENTAGE:
                if (MC.thePlayer != null) {
                    // 0.3xyz -> 3xy.z -> 3xy
                    int walkSpeed = (int) (MC.thePlayer.capabilities.getWalkSpeed() * 1000);
                    text = walkSpeed + "%";
                } else /* Dummy */ {
                    text = "123%";
                }
                break;

            case HEALTH_UPDATES:
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
                break;

            case DARK_AUCTION_TIMER:
                // The timezone of the server, to avoid problems with like timezones that are 30 minutes ahead or whatnot.
                ZonedDateTime nowDA = SkyblockAddons.getHypixelZonedDateTime();
                ZonedDateTime nextDarkAuction = nowDA.withMinute(55).withSecond(0);
                if (nowDA.getMinute() >= 55) {
                    nextDarkAuction = nextDarkAuction.plusHours(1);
                }
                Duration diffDA = Duration.between(nowDA, nextDarkAuction);
                text = String.format("%02d:%02d", diffDA.toMinutes(), diffDA.getSeconds() % 60);
                break;

            case FARM_EVENT_TIMER:
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
                break;

            case SKILL_DISPLAY:
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
                break;

            case ZEALOT_COUNTER:
                if (feature.isEnabled(FeatureSetting.COUNTER_ZEALOT_SPAWN_AREAS_ONLY)
                        && !LocationUtils.isOnZealotSpawnLocation() && buttonLocation == null) {
                    return;
                }
                text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getKills());
                break;

            case SHOW_TOTAL_ZEALOT_COUNT:
                if (feature.isEnabled(FeatureSetting.TOTAL_ZEALOT_SPAWN_AREAS_ONLY)
                        && !LocationUtils.isOnZealotSpawnLocation() && buttonLocation == null) {
                    return;
                }
                if (main.getPersistentValuesManager().getPersistentValues().getTotalKills() <= 0) {
                    text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getKills());
                } else {
                    text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getTotalKills()
                            + main.getPersistentValuesManager().getPersistentValues().getKills());
                }
                break;

            case SHOW_SUMMONING_EYE_COUNT:
                if (feature.isEnabled(FeatureSetting.EYE_ZEALOT_SPAWN_AREAS_ONLY) &&
                        !LocationUtils.isOnZealotSpawnLocation() && buttonLocation == null) {
                    return;
                }
                text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount());
                break;

            case SHOW_AVERAGE_ZEALOTS_PER_EYE:
                if (feature.isEnabled(FeatureSetting.AVERAGE_ZEALOT_SPAWN_AREAS_ONLY)
                        && !LocationUtils.isOnZealotSpawnLocation() && buttonLocation == null) {
                    return;
                }
                int summoningEyeCount = main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount();

                if (summoningEyeCount > 0) {
                    text = TextUtils.formatNumber(Math.round(main.getPersistentValuesManager().getPersistentValues().getTotalKills()
                            / (double) main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount()));
                } else {
                    text = "0"; // Avoid zero division.
                }
                break;

            case BIRCH_PARK_RAINMAKER_TIMER:
                long rainmakerTime = main.getPlayerListener().getRainmakerTimeEnd();

                if (!LocationUtils.isOn("Birch Park") && buttonLocation == null)
                    return;

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
                break;

            case ENDSTONE_PROTECTOR_DISPLAY:
                if ((!LocationUtils.isOn(Island.THE_END)
                        || EndstoneProtectorManager.getMinibossStage() == null
                        || !EndstoneProtectorManager.isCanDetectSkull()
                    ) && buttonLocation == null) return;

                EndstoneProtectorManager.Stage stage = EndstoneProtectorManager.getMinibossStage();

                if (buttonLocation != null && stage == null)
                    stage = EndstoneProtectorManager.Stage.STAGE_3;

                int stageNum = Math.min(stage.ordinal(), 5);
                text = Translations.getMessage("messages.stage", String.valueOf(stageNum));
                break;

            case SHOW_DUNGEON_MILESTONE:
                if (buttonLocation == null && !main.getUtils().isInDungeon()) return;

                DungeonMilestone dungeonMilestone = main.getDungeonManager().getDungeonMilestone();
                if (dungeonMilestone == null) {
                    if (buttonLocation != null) dungeonMilestone = new DungeonMilestone(DungeonClass.HEALER);
                    else return;
                }
                text = "Milestone " + dungeonMilestone.getLevel();
                break;

            case DUNGEONS_COLLECTED_ESSENCES_DISPLAY:
                if (buttonLocation == null && !main.getUtils().isInDungeon()) return;
                text = "";
                break;

            case DUNGEON_DEATH_COUNTER:
                if (buttonLocation == null && !main.getUtils().isInDungeon()) return;
                text = Integer.toString(main.getDungeonManager().getDeathCount());
                break;

            case ROCK_PET_TRACKER:
                if (buttonLocation == null && feature.isEnabled(FeatureSetting.SHOW_ONLY_HOLDING_MINING_TOOL)
                        && !main.getPlayerListener().isHoldingMiningTool()) {
                    return;
                }
                text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getOresMined());
                break;

            case DOLPHIN_PET_TRACKER:
                if (buttonLocation == null && feature.isEnabled(FeatureSetting.SHOW_ONLY_HOLDING_FISHING_ROD)
                        && !main.getPlayerListener().isHoldingRod()) {
                    return;
                }
                text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getSeaCreaturesKilled());
                break;

            case DUNGEONS_SECRETS_DISPLAY:
                if (buttonLocation == null && !main.getUtils().isInDungeon()) return;
                text = "Secrets";
                break;

            case SPIRIT_SCEPTRE_DISPLAY:
                if (buttonLocation != null) {
                    text = "§6Hyperion";
                    break;
                }

                ItemStack holdingItem = MC.thePlayer.getCurrentEquippedItem();
                String skyblockItemID = ItemUtils.getSkyblockItemID(holdingItem);

                if (holdingItem == null || skyblockItemID == null) {
                    return;
                } else if (DamageDisplayItem.getByID(skyblockItemID) != null) {
                    text = DUNGEON_STAR_PATTERN.matcher(holdingItem.getDisplayName()).replaceFirst("");
                } else {
                    return;
                }
                break;
            case CANDY_POINTS_COUNTER:
                if (buttonLocation == null && !SpookyEventManager.isActive()) return;
                text = "Test";
                break;

            case FETCHUR_TODAY:
                FetchurManager.FetchurItem fetchurItem = FetchurManager.getInstance().getCurrentFetchurItem();
                if (!FetchurManager.getInstance().hasFetchedToday() || buttonLocation != null) {
                    if (feature.isEnabled(FeatureSetting.SHOW_FETCHUR_ITEM_NAME)) {
                        text = Translations.getMessage(
                                "messages.fetchurItem",
                                fetchurItem.getItemStack().stackSize + "x " + fetchurItem.getItemText()
                        );
                    } else {
                        text = Translations.getMessage("messages.fetchurItem", "");
                    }
                } else {
                    text = ""; // If it has made fetchur, then no need for text
                }
                break;

            case FIRE_FREEZE_TIMER:
                if (buttonLocation == null && !main.getUtils().isInDungeon()) return;

                if (buttonLocation != null) {
                    text = "Fire Freeze in 5,00";
                } else {
                    if (feature.isEnabled(FeatureSetting.FIRE_FREEZE_WHEN_HOLDING) && !main.getPlayerListener().isHoldingFireFreeze()) {
                        return;
                    }

                    long fireFreezeTimer = main.getPlayerListener().getFireFreezeTimer();
                    if (fireFreezeTimer == 0) return;

                    double countdown = (fireFreezeTimer - System.currentTimeMillis()) / 1000D;

                    if (countdown > 0) {
                        text = String.format("Fire Freeze in %.2f", countdown);
                    } else {
                        if (feature.isEnabled(FeatureSetting.FIRE_FREEZE_SOUND)) {
                            main.getUtils().playLoudSound("mob.wither.spawn", 1);
                        }
                        main.getPlayerListener().setFireFreezeTimer(0);
                        return;
                    }
                }
                break;

            case THUNDER_BOTTLE_DISPLAY:
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
                break;

            default:
                return;
        }

        float x = feature.getActualX();
        float y = feature.getActualY();

        int height = 7;
        int width = MC.fontRendererObj.getStringWidth(text);

        switch (feature) {
            case ZEALOT_COUNTER:
            case SHOW_AVERAGE_ZEALOTS_PER_EYE:
            case SHOW_TOTAL_ZEALOT_COUNT:
            case SHOW_SUMMONING_EYE_COUNT:
                width = MC.fontRendererObj.getStringWidth(text) + 18;
                height += 9;
                break;

            case ENDSTONE_PROTECTOR_DISPLAY:
                width += 18 + 2 + 16 + 2 + MC.fontRendererObj.getStringWidth(
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
                int maxNumberWidth = MC.fontRendererObj.getStringWidth("99");
                width = 18 + 2 + maxNumberWidth + 5 + 18 + 2 + maxNumberWidth;
                height = 18 * (int) Math.ceil(EssenceType.values().length / 2F);
                break;

            case SPIRIT_SCEPTRE_DISPLAY:
                width += 18 + MC.fontRendererObj.getStringWidth("12345");
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
                    width += 16 + 1 + MC.fontRendererObj.getStringWidth(TextUtils.formatNumber(green));
                }
                if (buttonLocation != null || purple > 0) {
                    if (green > 0) width += 1;
                    width += 16 + 1 + MC.fontRendererObj.getStringWidth(TextUtils.formatNumber(purple)) + 1;
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
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

        switch (feature) {
            case DARK_AUCTION_TIMER:
                MC.getTextureManager().bindTexture(SIRIUS_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case FARM_EVENT_TIMER:
                MC.getTextureManager().bindTexture(FARM_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case ZEALOT_COUNTER:
                MC.getTextureManager().bindTexture(ENDERMAN_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case SHOW_TOTAL_ZEALOT_COUNT:
                MC.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case SHOW_SUMMONING_EYE_COUNT:
                MC.getTextureManager().bindTexture(SUMMONING_EYE_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case SHOW_AVERAGE_ZEALOTS_PER_EYE:
                MC.getTextureManager().bindTexture(ZEALOTS_PER_EYE_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);
                MC.getTextureManager().bindTexture(SLASH_ICON);
                ColorUtils.bindColor(color);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16, true);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case SKILL_DISPLAY:
                if ((skill == null || skill.getItem() == null) && buttonLocation == null) return;
                renderItem(buttonLocation == null ? skill.getItem() : SkillType.FARMING.getItem(), x, y);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case BIRCH_PARK_RAINMAKER_TIMER:
                renderItem(WATER_BUCKET, x, y);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case ENDSTONE_PROTECTOR_DISPLAY:
                MC.getTextureManager().bindTexture(IRON_GOLEM_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();

                x += 16 + 2 + MC.fontRendererObj.getStringWidth(text) + 2;

                GlStateManager.color(1, 1, 1, 1);
                MC.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                int count = EndstoneProtectorManager.getZealotCount();

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(TextUtils.formatNumber(count), x + 16 + 2, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case SHOW_DUNGEON_MILESTONE:
                DungeonMilestone dungeonMilestone = main.getDungeonManager().getDungeonMilestone();
                if (buttonLocation != null) {
                    dungeonMilestone = new DungeonMilestone(DungeonClass.HEALER);
                }

                renderItem(dungeonMilestone.getDungeonClass().getItem(), x, y);
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y, color);
                Number amount;
                try {
                    amount = TextUtils.NUMBER_FORMAT.parse(dungeonMilestone.getValue());
                } catch (ParseException e) {
                    amount = -1;
                }
                String formattedAmount = TextUtils.formatNumber(amount);
                DrawUtils.drawText(
                        formattedAmount,
                        x + 18 + MC.fontRendererObj.getStringWidth(text) / 2F
                                - MC.fontRendererObj.getStringWidth(formattedAmount) / 2F,
                        y + 9,
                        color
                );
                FontRendererHook.endFeatureFont();
                break;

            case DUNGEONS_COLLECTED_ESSENCES_DISPLAY:
                this.drawCollectedEssences(x, y, buttonLocation != null, true);
                break;

            case DUNGEON_DEATH_COUNTER:
                renderItem(SKULL, x, y);
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case ROCK_PET_TRACKER:
                renderItem(ROCK_PET, x, y);
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case DOLPHIN_PET_TRACKER:
                renderItem(DOLPHIN_PET, x, y);
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case DUNGEONS_SECRETS_DISPLAY:
                int secrets = main.getDungeonManager().getSecrets();
                int maxSecrets = main.getDungeonManager().getMaxSecrets();

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 16 + 2, y, color);
                FontRendererHook.endFeatureFont();

                if (secrets == -1 && buttonLocation != null) {
                    secrets = 5;
                    maxSecrets = 10;
                }

                if (secrets == -1 | maxSecrets == 0) {
                    FontRendererHook.setupFeatureFont(feature);
                    String none = Translations.getMessage("messages.none");
                    DrawUtils.drawText(
                            none,
                            x + 16 + 2 + MC.fontRendererObj.getStringWidth(text) / 2F
                                    - MC.fontRendererObj.getStringWidth(none) / 2F,
                            y + 10,
                            color
                    );
                    FontRendererHook.endFeatureFont();
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

                    float secretsWidth = MC.fontRendererObj.getStringWidth(String.valueOf(secrets));
                    float slashWidth = MC.fontRendererObj.getStringWidth("/");
                    float maxSecretsWidth = MC.fontRendererObj.getStringWidth(String.valueOf(maxSecrets));

                    float totalWidth = secretsWidth + slashWidth + maxSecretsWidth;

                    FontRendererHook.setupFeatureFont(feature);
                    DrawUtils.drawText(
                            "/",
                            x + 16 + 2 + MC.fontRendererObj.getStringWidth(text) / 2F - totalWidth / 2F + secretsWidth,
                            y + 11,
                            color
                    );
                    FontRendererHook.endFeatureFont();

                    DrawUtils.drawText(
                            String.valueOf(secrets),
                            x + 16 + 2 + MC.fontRendererObj.getStringWidth(text) / 2F - totalWidth / 2F,
                            y + 11,
                            secretsColor
                    );
                    DrawUtils.drawText(
                            String.valueOf(maxSecrets),
                            x + 16 + 2 + MC.fontRendererObj.getStringWidth(text) / 2F - totalWidth / 2F + secretsWidth + slashWidth,
                            y + 11,
                            secretsColor
                    );
                }

                GlStateManager.color(1, 1, 1, 1);
                renderItem(CHEST, x, y);
                break;

            case SPIRIT_SCEPTRE_DISPLAY:
                int hitEnemies = main.getPlayerListener().getSpiritSceptreHitEnemies();
                float dealtDamage = main.getPlayerListener().getSpiritSceptreDealtDamage();
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 16 + 2, y, color);

                if (hitEnemies == 1) {
                    DrawUtils.drawText(String.format("%d enemy hit", hitEnemies), x + 16 + 2, y + 9, color);
                } else {
                    DrawUtils.drawText(String.format("%d enemies hit", hitEnemies), x + 16 + 2, y + 9, color);
                }

                DrawUtils.drawText(String.format("%,d damage dealt", Math.round(dealtDamage)), x + 16 + 2, y + 18, color);
                FontRendererHook.endFeatureFont();

                if (buttonLocation != null) {
                    renderItem(DamageDisplayItem.HYPERION.itemStack, x, y);
                    break;
                }

                ItemStack displayItem = DamageDisplayItem.getByID(ItemUtils.getSkyblockItemID(MC.thePlayer.getHeldItem()));
                if (displayItem != null) {
                    renderItem(displayItem, x, y);
                }
                break;

            case CANDY_POINTS_COUNTER:
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
                    renderItem(GREEN_CANDY, currentX, y);

                    currentX += 16 + 1;
                    FontRendererHook.setupFeatureFont(feature);
                    DrawUtils.drawText(TextUtils.formatNumber(green), currentX, y + 4, color);
                    FontRendererHook.endFeatureFont();
                }
                if (buttonLocation != null || purple > 0) {
                    if (buttonLocation != null || green > 0) {
                        currentX += MC.fontRendererObj.getStringWidth(TextUtils.formatNumber(green)) + 1;
                    }

                    renderItem(PURPLE_CANDY, currentX, y);

                    currentX += 16 + 1;
                    FontRendererHook.setupFeatureFont(feature);
                    DrawUtils.drawText(TextUtils.formatNumber(purple), currentX, y + 4, color);
                    FontRendererHook.endFeatureFont();
                }

                FontRendererHook.setupFeatureFont(feature);
                text = TextUtils.formatNumber(points) + " Points";
                DrawUtils.drawText(text, x + width / 2F - MC.fontRendererObj.getStringWidth(text) / 2F, y + 16, color);
                FontRendererHook.endFeatureFont();
                break;

            case FETCHUR_TODAY:
                boolean showDwarven = feature.isDisabled(FeatureSetting.SHOW_FETCHUR_ONLY_IN_DWARVENS)
                        || LocationUtils.isOn(Island.DWARVEN_MINES);
                boolean showInventory = feature.isDisabled(FeatureSetting.SHOW_FETCHUR_INVENTORY_OPEN_ONLY)
                        || MC.currentScreen != null;
                FetchurManager.FetchurItem fetchurItem = FetchurManager.getInstance().getCurrentFetchurItem();

                // Show if it's the gui button position, or the player hasn't given Fetchur,
                // and it shouldn't be hidden b/c of dwarven mines or inventory
                if (fetchurItem != null && (buttonLocation != null ||
                        (!FetchurManager.getInstance().hasFetchedToday() && showDwarven && showInventory))) {

                    FontRendererHook.setupFeatureFont(feature);

                    if (feature.isDisabled(FeatureSetting.SHOW_FETCHUR_ITEM_NAME)) {
                        DrawUtils.drawText(text, x + 1, y + 4, color); // Line related to the "Fetchur wants" text
                        float offsetX = MC.fontRendererObj.getStringWidth(text);
                        renderItemAndOverlay(
                                fetchurItem.getItemStack(),
                                String.valueOf(fetchurItem.getItemStack().stackSize),
                                x + offsetX,
                                y
                        );
                    } else {
                        DrawUtils.drawText(text, x, y, color); // Line related to the "Fetchur wants" text
                    }
                    FontRendererHook.endFeatureFont();
                }
                break;

            case HEALTH_TEXT:
                // 22 -> Absorption
                if (MC.thePlayer != null && MC.thePlayer.isPotionActive(22)
                        && PlayerStats.HEALTH.getValue() > PlayerStats.MAX_HEALTH.getValue()) {
                    String formattedHealth = TextUtils.formatNumber(PlayerStats.HEALTH.getValue());
                    int formattedHealthWidth = MC.fontRendererObj.getStringWidth(formattedHealth);

                    color = ColorUtils.getDummySkyblockColor(
                            ColorCode.GOLD.getColor(),
                            feature.isChroma()
                    ).getColor();
                    FontRendererHook.setupFeatureFont(feature);
                    DrawUtils.drawText(formattedHealth, x, y, color);

                    color = feature.getColor();
                    DrawUtils.drawText(
                            "/" + TextUtils.formatNumber(PlayerStats.MAX_HEALTH.getValue())
                                    + (feature.isEnabled(FeatureSetting.HEALTH_TEXT_ICON) ? "❤" : ""),
                            x + formattedHealthWidth,
                            y,
                            color
                    );
                    FontRendererHook.endFeatureFont();
                } else {
                    FontRendererHook.setupFeatureFont(feature);
                    DrawUtils.drawText(text, x, y, color);
                    FontRendererHook.endFeatureFont();
                }
                break;

            case FIRE_FREEZE_TIMER:
                renderItem(new ItemStack(Blocks.yellow_flower, 1), x, y - 3);
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case THUNDER_BOTTLE_DISPLAY:
                ThunderBottle displayBottle = ThunderBottle.getDisplayBottle();

                if (displayBottle != null) {
                    renderItem(displayBottle.getItemStack(), x, y);
                } else /*buttonLocation != null*/ {
                    renderItem(DUMMY_THUNDER_BOTTLE, x, y);
                }
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            default:
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x, y, color);
                FontRendererHook.endFeatureFont();
                break;
        }

        main.getUtils().restoreGLOptions();
    }

    private String getCrimsonArmorAbilityStacks() {
        EntityPlayerSP player = MC.thePlayer;
        ItemStack[] itemStacks = player.inventory.armorInventory;

        StringBuilder builder = new StringBuilder();
        out:
        for (CrimsonArmorAbilityStack crimsonArmorAbilityStack : CrimsonArmorAbilityStack.values()) {
            for (ItemStack itemStack : itemStacks) {
                if (itemStack == null) continue;
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
        return builder.length() == 0 ? null : builder.toString();
    }

    public void drawCollectedEssences(float x, float y, boolean usePlaceholders, boolean hideZeroes) {
        InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

        float currentX = x;
        float currentY;

        int maxNumberWidth;
        if (inventoryType == InventoryType.SALVAGING) {
            Set<Map.Entry<EssenceType, Integer>> entrySet = main.getDungeonManager().getSalvagedEssences().entrySet();
            if (entrySet.isEmpty()) return;

            String highestAmountStr = Collections.max(entrySet, Map.Entry.comparingByValue()).getValue().toString();
            maxNumberWidth = MC.fontRendererObj.getStringWidth(highestAmountStr);
        } else {
            maxNumberWidth = MC.fontRendererObj.getStringWidth("99");
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

            GlStateManager.color(1, 1, 1, 1);
            MC.getTextureManager().bindTexture(essenceType.getResourceLocation());
            DrawUtils.drawModalRectWithCustomSizedTexture(currentX, currentY, 0, 0, 16, 16, 16, 16);

            FontRendererHook.setupFeatureFont(Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY);
            DrawUtils.drawText(TextUtils.formatNumber(value), currentX + 18 + 2, currentY + 5, color);
            FontRendererHook.endFeatureFont();

            count++;
        }
    }

    /**
     * Displays the bait list. Only shows bait with count > 0.
     */
    public void drawBaitList(float scale, ButtonLocation buttonLocation) {
        if (!main.getPlayerListener().isHoldingRod() && buttonLocation == null) return;

        Map<BaitManager.BaitType, Integer> baits = BaitManager.getInstance().getBaitsInInventory();
        if (buttonLocation != null) {
            baits = BaitManager.DUMMY_BAITS;
        }

        int longestLineWidth = 0;
        for (Map.Entry<BaitManager.BaitType, Integer> entry : baits.entrySet()) {
            longestLineWidth = Math.max(
                    longestLineWidth,
                    MC.fontRendererObj.getStringWidth(TextUtils.formatNumber(entry.getValue()))
            );
        }

        float x = Feature.BAIT_LIST.getActualX();
        float y = Feature.BAIT_LIST.getActualY();

        int spacing = 1;
        int iconSize = 16;
        int width = iconSize + spacing + longestLineWidth;
        int height = iconSize * baits.size();

        x = transformX(x, width, scale, Feature.BAIT_LIST.isEnabled(FeatureSetting.X_ALLIGNMENT));
        y = transformY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
        }

        main.getUtils().enableStandardGLOptions();

        for (Map.Entry<BaitManager.BaitType, Integer> entry : baits.entrySet()) {
            if (entry.getValue() == 0) continue;

            GlStateManager.color(1, 1, 1, 1F);
            renderItem(entry.getKey().getItemStack(), x, y);

            int color = Feature.BAIT_LIST.getColor();
            FontRendererHook.setupFeatureFont(Feature.BAIT_LIST);
            DrawUtils.drawText(
                    TextUtils.formatNumber(entry.getValue()),
                    x + iconSize + spacing,
                    y + (iconSize / 2F) - (8 / 2F),
                    color
            );
            FontRendererHook.endFeatureFont();

            y += iconSize;
        }

        main.getUtils().restoreGLOptions();
    }

    public void drawSlayerTrackers(Feature feature, float scale, ButtonLocation buttonLocation) {
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

            int longestLineWidth = MC.fontRendererObj.getStringWidth(slayerBoss.getDisplayName());
            lines++;
            spacers++;

            int longestSlayerDropLineWidth = MC.fontRendererObj.getStringWidth(
                    Translations.getMessage("slayerTracker.bossesKilled")
            );
            int longestCount = MC.fontRendererObj.getStringWidth(
                    String.valueOf(SlayerTracker.getInstance().getSlayerKills(slayerBoss))
            );
            lines++;
            spacers++;

            for (SlayerDrop drop : slayerBoss.getDrops()) {
                longestSlayerDropLineWidth = Math.max(
                        longestSlayerDropLineWidth,
                        MC.fontRendererObj.getStringWidth(drop.getDisplayName())
                );
                longestCount = Math.max(
                        longestCount,
                        MC.fontRendererObj.getStringWidth(
                                String.valueOf(SlayerTracker.getInstance().getDropCount(drop))
                        )
                );
                lines++;
            }

            int width = Math.max(longestLineWidth, longestSlayerDropLineWidth + 8 + longestCount);
            int height = lines * 8 + spacer * spacers;

            x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
            y = transformY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            }

            FontRendererHook.setupFeatureFont(feature);

            DrawUtils.drawText(slayerBoss.getDisplayName(), x, y, color);
            y += lineHeight + spacer;
            DrawUtils.drawText(Translations.getMessage("slayerTracker.bossesKilled"), x, y, color);
            String text = String.valueOf(SlayerTracker.getInstance().getSlayerKills(slayerBoss));
            DrawUtils.drawText(text, x + width - MC.fontRendererObj.getStringWidth(text), y, color);
            y += lineHeight + spacer;

            FontRendererHook.endFeatureFont();

            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {

                int currentColor = color;
                if (colorByRarity) {
                    currentColor = slayerDrop.getRarity().getColorCode().getColor();
                } else {
                    FontRendererHook.setupFeatureFont(feature);
                }

                DrawUtils.drawText(slayerDrop.getDisplayName(), x, y, currentColor);

                if (!colorByRarity) {
                    FontRendererHook.endFeatureFont();
                }

                FontRendererHook.setupFeatureFont(feature);
                text = String.valueOf(SlayerTracker.getInstance().getDropCount(slayerDrop));
                DrawUtils.drawText(text, x + width - MC.fontRendererObj.getStringWidth(text), y, currentColor);
                FontRendererHook.endFeatureFont();

                y += lineHeight;
            }

        } else {
            int entityRenderY;
            int textCenterX;

            switch (feature) {
                case REVENANT_SLAYER_TRACKER:
                    entityRenderY = 30;
                    textCenterX = 15;
                    break;
                case TARANTULA_SLAYER_TRACKER:
                    entityRenderY = 36;
                    textCenterX = 28;
                    break;
                case SVEN_SLAYER_TRACKER:
                    entityRenderY = 25;
                    textCenterX = 20;
                    break;
                case VOIDGLOOM_SLAYER_TRACKER:
                    entityRenderY = 24;
                    textCenterX = 20;
                    break;
                case INFERNO_SLAYER_TRACKER:
                    entityRenderY = 35;
                    textCenterX = 20;
                    break;
                case RIFTSTALKER_SLAYER_TRACKER:
                    entityRenderY = 40;
                    textCenterX = 15;
                    break;
                default:
                    entityRenderY = 36;
                    textCenterX = 15;
                    break;
            }

            int iconWidth = 16;

            int entityWidth = textCenterX * 2;
            int entityIconSpacingHorizontal = 2;
            int iconTextOffset = -2;
            int row = 0;
            int column = 0;
            int maxItemsPerRow = (int) Math.ceil(slayerBoss.getDrops().size() / 3.0);
            int[] maxTextWidths = new int[maxItemsPerRow];
            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
                int width = MC.fontRendererObj.getStringWidth(
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
                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            }

            switch (feature) {
                case REVENANT_SLAYER_TRACKER:
                    if (revenant == null) {
                        revenant = new EntityZombie(Utils.getDummyWorld());

                        revenant.getInventory()[0] = ItemUtils.createItemStack(Items.diamond_hoe, true);
                        revenant.getInventory()[1] = ItemUtils.createItemStack(Items.diamond_boots, false);
                        revenant.getInventory()[2] = ItemUtils.createItemStack(Items.chainmail_leggings, true);
                        revenant.getInventory()[3] = ItemUtils.createItemStack(Items.diamond_chestplate, true);
                        revenant.getInventory()[4] = ItemUtils.createSkullItemStack(
                                null,
                                null,
                                "45012ee3-29fd-42ed-908b-648c731c7457",
                                "1fc0184473fe882d2895ce7cbc8197bd40ff70bf10d3745de97b6c2a9c5fc78f"
                        );
                    }
                    GlStateManager.color(1, 1, 1, 1);
                    revenant.ticksExisted = (int) main.getScheduler().getTotalTicks();
                    drawEntity(revenant, x + 15, y + 53, -15); // left is 35
                    break;

                case TARANTULA_SLAYER_TRACKER:
                    if (tarantula == null) {
                        tarantula = new EntitySpider(Utils.getDummyWorld());
                        caveSpider = new EntityCaveSpider(Utils.getDummyWorld());

                        tarantula.riddenByEntity = caveSpider;
                        caveSpider.ridingEntity = tarantula;
                    }
                    GlStateManager.color(1, 1, 1, 1);
                    drawEntity(tarantula, x + 28, y + 38, -30);
                    drawEntity(caveSpider, x + 25, y + 23, -30);
                    break;

                case SVEN_SLAYER_TRACKER:
                    if (sven == null) {
                        sven = new EntityWolf(Utils.getDummyWorld());
                        sven.setAngry(true);
                    }
                    GlStateManager.color(1, 1, 1, 1);
                    drawEntity(sven, x + 17, y + 38, -35);
                    break;

                case VOIDGLOOM_SLAYER_TRACKER:
                    if (enderman == null) {
                        enderman = new EntityEnderman(Utils.getDummyWorld());
                        enderman.setHeldBlockState(Blocks.beacon.getBlockState().getBaseState());
                    }
                    GlStateManager.color(1, 1, 1, 1);
                    enderman.ticksExisted = (int) main.getScheduler().getTotalTicks();
                    GlStateManager.scale(.7, .7, 1);
                    drawEntity(enderman, (x + 15) / .7F, (y + 51) / .7F, -30);
                    GlStateManager.scale(1 / .7, 1 / .7, 1);
                    break;

                case INFERNO_SLAYER_TRACKER:
                    if (inferno == null) {
                        inferno = new EntityBlaze(Utils.getDummyWorld());
                        inferno.setOnFire(true);
                    }
                    GlStateManager.color(1, 1, 1, 1);
                    inferno.ticksExisted = (int) main.getScheduler().getTotalTicks();
                    drawEntity(inferno, x + 15, y + 53, -15);
                    break;

                case RIFTSTALKER_SLAYER_TRACKER:
                    if (riftstalker == null) {
                        riftstalker = new EntityOtherPlayerMP(Utils.getDummyWorld(), new GameProfile(UUID.randomUUID(), "Riftstalker")) {
                            @Override
                            public ResourceLocation getLocationSkin() {
                                return RIFTSTALKER_BLOODFIEND;
                            }
                        };
                        riftstalker.setAlwaysRenderNameTag(false);
                    }
                    GlStateManager.color(1, 1, 1, 1);
                    drawEntity(riftstalker, x + 15, y + 53, -15);
                    break;
            }

            GlStateManager.disableDepth();
            FontRendererHook.setupFeatureFont(feature);
            String text = TextUtils.abbreviate(SlayerTracker.getInstance().getSlayerKills(slayerBoss)) + " Kills";
            DrawUtils.drawText(
                    text,
                    x + textCenterX - MC.fontRendererObj.getStringWidth(text) / 2F,
                    y + entityRenderY,
                    color
            );
            FontRendererHook.endFeatureFont();

            row = 0;
            column = 0;
            float currentX = x + entityIconSpacingHorizontal + entityWidth;
            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
                if (column > 0) {
                    currentX += iconWidth + maxTextWidths[column - 1];
                }

                float currentY = y + row * (iconWidth + iconSpacingVertical);

                GlStateManager.color(1, 1, 1, 1);
                renderItem(slayerDrop.getItemStack(), currentX, currentY);

                GlStateManager.disableDepth();

                int currentColor = color;
                if (colorByRarity) {
                    currentColor = slayerDrop.getRarity().getColorCode().getColor();
                } else {
                    FontRendererHook.setupFeatureFont(feature);
                }

                DrawUtils.drawText(
                        TextUtils.abbreviate(SlayerTracker.getInstance().getDropCount(slayerDrop)),
                        currentX + iconWidth + iconTextOffset,
                        currentY + 8,
                        currentColor
                );
                if (!colorByRarity) {
                    FontRendererHook.endFeatureFont();
                }

                column++;
                if (column == maxItemsPerRow) {
                    currentX = x + entityIconSpacingHorizontal + entityWidth;
                    column = 0;
                    row++;
                }
            }
            GlStateManager.enableDepth();
        }
    }

    public void drawDragonTrackers(float scale, ButtonLocation buttonLocation) {
        Feature feature = Feature.DRAGON_STATS_TRACKER;
        if (feature.isEnabled(FeatureSetting.DRAGONS_NEST_ONLY) && !LocationUtils.isOn("Dragon's Nest")
                && buttonLocation == null) {
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

            int longestLineWidth = MC.fontRendererObj.getStringWidth(
                    Translations.getMessage("dragonTracker.recentDragons")
            );
            lines++;
            spacers++;

            spacers++;
            longestLineWidth = Math.max(
                    longestLineWidth,
                    MC.fontRendererObj.getStringWidth(Translations.getMessage("dragonTracker.dragonsSince"))
            );
            lines++;
            spacers++;

            for (DragonType dragon : recentDragons) {
                longestLineWidth = Math.max(longestLineWidth, MC.fontRendererObj.getStringWidth(dragon.getDisplayName()));
                lines++;
            }

            int longestCount = 0;
            int longestDragonsSinceLineWidth = 0;
            for (DragonsSince dragonsSince : DragonsSince.values()) {
                longestDragonsSinceLineWidth = Math.max(
                        longestDragonsSinceLineWidth,
                        MC.fontRendererObj.getStringWidth(dragonsSince.getDisplayName())
                );
                int dragonsSinceValue = DragonTracker.getInstance().getDragsSince(dragonsSince);
                longestCount = Math.max(
                        longestCount,
                        MC.fontRendererObj.getStringWidth(dragonsSinceValue == 0
                                ? never
                                : String.valueOf(dragonsSinceValue)
                        )
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
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
        }

        int color = Feature.DRAGON_STATS_TRACKER.getColor();

        if (textMode) {
            FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
            DrawUtils.drawText(Translations.getMessage("dragonTracker.recentDragons"), x, y, color);
            y += 8 + spacerHeight;
            FontRendererHook.endFeatureFont();

            for (DragonType dragon : recentDragons) {
                int currentColor = color;
                if (colorByRarity) {
                    currentColor = dragon.getColor().getColor();
                } else {
                    FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
                }

                DrawUtils.drawText(dragon.getDisplayName(), x, y, currentColor);

                if (!colorByRarity) {
                    FontRendererHook.endFeatureFont();
                }

                y += 8;
            }
            y += spacerHeight;

            FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
            color = Feature.DRAGON_STATS_TRACKER.getColor();
            DrawUtils.drawText(Translations.getMessage("dragonTracker.dragonsSince"), x, y, color);
            y += 8 + spacerHeight;
            FontRendererHook.endFeatureFont();

            for (DragonsSince dragonsSince : DragonsSince.values()) {
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.color(1, 1, 1, 1F);
                GlStateManager.disableBlend();
                GlStateManager.enableDepth();

                int currentColor = color;
                if (colorByRarity) {
                    currentColor = dragonsSince.getItemRarity().getColorCode().getColor();
                } else {
                    FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
                }

                DrawUtils.drawText(dragonsSince.getDisplayName(), x, y, currentColor);

                if (!colorByRarity) {
                    FontRendererHook.endFeatureFont();
                }

                FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
                int dragonsSinceValue = DragonTracker.getInstance().getDragsSince(dragonsSince);
                String text = dragonsSinceValue == 0 ? never : String.valueOf(dragonsSinceValue);
                DrawUtils.drawText(text, x + width - MC.fontRendererObj.getStringWidth(text), y, color);
                y += 8;
                FontRendererHook.endFeatureFont();
            }
        }
    }

    public void drawSlayerArmorProgress(float scale, ButtonLocation buttonLocation) {
        Feature feature = Feature.SLAYER_ARMOR_PROGRESS;
        float x = Feature.SLAYER_ARMOR_PROGRESS.getActualX();
        float y = Feature.SLAYER_ARMOR_PROGRESS.getActualY();

        int longest = -1;
        SlayerArmorProgress[] progresses = main.getInventoryUtils().getSlayerArmorProgresses();
        if (buttonLocation != null) progresses = DUMMY_PROGRESSES;
        for (SlayerArmorProgress progress : progresses) {
            if (progress == null) continue;

            int textWidth = MC.fontRendererObj.getStringWidth(progress.getPercent() + "% (" + progress.getDefence() + ")");
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
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

        boolean downwards = feature.getAnchorPoint().isOnTop();
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
            renderItem(progress.getItemStack(), x, fixedY);

            float currentX = x + 19;
            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(progress.getPercent() + "% (", currentX, fixedY + 5, color);
            FontRendererHook.endFeatureFont();

            currentX += MC.fontRendererObj.getStringWidth(progress.getPercent() + "% (");
            DrawUtils.drawText(progress.getDefence(), currentX, fixedY + 5, 0xFFFFFFFF);

            currentX += MC.fontRendererObj.getStringWidth(progress.getDefence());
            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(")", currentX, fixedY + 5, color);
            FontRendererHook.endFeatureFont();

            drawnCount++;
        }

        main.getUtils().restoreGLOptions();
    }

    private void drawPetDisplay(float scale, ButtonLocation buttonLocation) {
        if (main.getUtils().isOnRift()) return;

        Feature feature = Feature.PET_DISPLAY;
        PetManager.Pet newPet = main.getPetCacheManager().getCurrentPet();
        if (newPet == null) {
            return;
        } else if (pet != newPet) {
            pet = newPet;
            String skullId = newPet.getSkullId();
            String textureUrl = newPet.getTextureURL();
            if (StringUtils.isNullOrEmpty(skullId) || StringUtils.isNullOrEmpty(textureUrl)) {
                petSkull = null;
            } else {
                petSkull = ItemUtils.createSkullItemStack(null, null, skullId, textureUrl);
            }
        }

        String text = pet.getDisplayName();

        float x = Feature.PET_DISPLAY.getActualX();
        float y = Feature.PET_DISPLAY.getActualY();

        int height = 7 + MC.fontRendererObj.FONT_HEIGHT;
        int width = MC.fontRendererObj.getStringWidth(text) + 18; // + ItemStack width

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
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

        FontRendererHook.setupFeatureFont(feature);
        int color = feature.getColor();
        DrawUtils.drawText(text, x + (18 * line), y + 4, color);

        switch (style) {
            case DISPLAY_NAME:
                if (pet.getPetInfo().getHeldItemId() == null) break;

                String petDisplayName = PetManager.getInstance().getPetItemDisplayNameFromId(
                        pet.getPetInfo().getHeldItemId()
                );
                DrawUtils.drawText("Held Item: " + petDisplayName, x + (18 * line), y + 16, color);
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
                DrawUtils.drawText(displayText, x + (18 * line), y + 16, color);

                renderItem(petItemStack, x + (18 * line) + MC.fontRendererObj.getStringWidth(displayText), y + 10);
                break;
        }
        FontRendererHook.endFeatureFont();

        // render pet
        renderItem(petSkull, x, y, line);

        main.getUtils().restoreGLOptions();
    }

    public static void renderItem(ItemStack item, float x, float y) {
        renderItem(item, x, y, 1);
    }

    public static void renderItem(ItemStack item, float x, float y, float scale) {
        if (item == null) return;

        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();

        GlStateManager.pushMatrix();
        if (scale != 1) {
            GlStateManager.scale(scale, scale, 1F);
        }
        GlStateManager.translate(x / scale, y / scale, 0);
        MC.getRenderItem().renderItemIntoGUI(item, 0, 0);
        GlStateManager.popMatrix();

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableDepth();
    }

    public static void renderItemAndOverlay(ItemStack item, String name, float x, float y) {
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        MC.getRenderItem().renderItemIntoGUI(item, 0, 0);
        MC.getRenderItem().renderItemOverlayIntoGUI(MC.fontRendererObj, item, 0, 0, name);
        GlStateManager.popMatrix();

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableDepth();
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public void drawItemPickupLog(float scale, ButtonLocation buttonLocation) {
        Feature feature = Feature.ITEM_PICKUP_LOG;
        float x = feature.getActualX();
        float y = feature.getActualY();

        boolean downwards = feature.getAnchorPoint().isOnTop();
        boolean renderItemStack = feature.isEnabled(FeatureSetting.RENDER_ITEM_ON_LOG);

        int heightSpacer = renderItemStack ? 6 : 1;
        int lineHeight = MC.fontRendererObj.FONT_HEIGHT + heightSpacer; // + pixel spacer
        int height = lineHeight * DUMMY_PICKUP_LOG.size();
        int width = MC.fontRendererObj.getStringWidth("+ 1x Forceful Ember Chestplate");

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
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

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
            DrawUtils.drawText(countText, x, stringY, 0xFFFFFFFF);
            DrawUtils.drawText(
                    "§r" + itemDiff.getDisplayName(),
                    x + MC.fontRendererObj.getStringWidth(countText) + (renderItemStack ? 20 : 4),
                    stringY,
                    0xFFFFFFFF
            );
            if (renderItemStack) {
                renderItem(
                        itemDiff.getItemStack(),
                        x + MC.fontRendererObj.getStringWidth(countText) + 2,
                        stringY - heightSpacer / 2 - 1
                );
            }
            i++;
        }

        main.getUtils().restoreGLOptions();
    }

    public void drawDeployableStatus(float scale, ButtonLocation buttonLocation) {
        DeployableManager.DeployableEntry activeDeployable = DeployableManager.getInstance().getActiveDeployable();
        if (buttonLocation != null && activeDeployable == null) {
            activeDeployable = DeployableManager.DUMMY_DEPLOYABLE_ENTRY;
        }
        if (activeDeployable != null) {
            Deployable deployable = activeDeployable.getDeployable();
            int seconds = activeDeployable.getSeconds();

            DeployableDisplayStyle displayStyle = (DeployableDisplayStyle) Feature.DEPLOYABLE_STATUS_DISPLAY.getAsEnum(FeatureSetting.DEPLOYABLE_DISPLAY_STYLE);
            if (displayStyle == DeployableDisplayStyle.DETAILED) {
                drawDetailedDeployableStatus(scale, buttonLocation, deployable, seconds);
            } else {
                drawCompactDeployableStatus(scale, buttonLocation, deployable, seconds);
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
    private void drawCompactDeployableStatus(float scale, ButtonLocation buttonLocation, Deployable deployable, int seconds) {
        Feature feature = Feature.DEPLOYABLE_STATUS_DISPLAY;
        float x = feature.getActualX();
        float y = feature.getActualY();

        String secondsString = String.format("§e%ss", seconds);
        int spacing = 1;
        int iconSize = MC.fontRendererObj.FONT_HEIGHT * 3; // 3 because it looked the best
        int width = iconSize + spacing + MC.fontRendererObj.getStringWidth(secondsString);

        x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
        y = transformY(y, iconSize, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + iconSize, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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

        main.getUtils().enableStandardGLOptions();

        if (entity instanceof EntityArmorStand) {
            drawDeployableArmorStand((EntityArmorStand) entity, x + 1, y + 4);
        } else {
            MC.getTextureManager().bindTexture(deployable.getResourceLocation());
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        DrawUtils.drawText(
                secondsString,
                x + spacing + iconSize,
                y + (iconSize / 2F) - (8 / 2F),
                ColorCode.WHITE.getColor(255)
        );

        main.getUtils().restoreGLOptions();
    }

    /**
     * Displays the deployable with detailed stats about the boost you're receiving.
     * <p>
     * ---- +X ❤/s
     * |  | +X ✎/s
     * ---- +X ❁
     * XXs
     */
    private void drawDetailedDeployableStatus(float scale, ButtonLocation buttonLocation, Deployable deployable, int seconds) {
        Feature feature = Feature.DEPLOYABLE_STATUS_DISPLAY;
        float x = feature.getActualX();
        float y = feature.getActualY();

        List<String> display = new LinkedList<>();
        // Counts already long strings
        int passIndex = 0;

        if (deployable.getHealthRegen() > 0.0) {
            float maxHealth = PlayerStats.MAX_HEALTH.getValue();
            float healthRegen = (float) (maxHealth * deployable.getHealthRegen());
            if (main.getUtils().getSlayerQuest() == EnumUtils.SlayerQuest.TARANTULA_BROODFATHER
                    && main.getUtils().getSlayerQuestLevel() >= 2) {
                healthRegen *= 0.5F; // Tarantula boss 2+ reduces healing by 50%.
            }
            display.add(String.format("§c+%s ❤/s", TextUtils.formatNumber(healthRegen)));
            passIndex++;
        }

        if (deployable.getManaRegen() > 0.0) {
            float maxMana = PlayerStats.MAX_MANA.getValue();
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
                display.add(displayCopy.get(displayCopy.size() - 1));
            }
        }

        Optional<String> longestLine = display.stream().max(Comparator.comparingInt(String::length));

        int spacingBetweenLines = 1;
        int iconSize = MC.fontRendererObj.FONT_HEIGHT * 3; // 3 because it looked the best
        int iconAndSecondsHeight = iconSize + MC.fontRendererObj.FONT_HEIGHT;

        int effectsHeight = (MC.fontRendererObj.FONT_HEIGHT + spacingBetweenLines) * display.size();
        int width = iconSize + 2 + longestLine.map(MC.fontRendererObj::getStringWidth).orElseGet(() ->
                MC.fontRendererObj.getStringWidth(display.get(0))
        );
        int height = Math.max(effectsHeight, iconAndSecondsHeight);

        x = transformX(x, width, scale, feature.isEnabled(FeatureSetting.X_ALLIGNMENT));
        y = transformY(y, height, scale);

        float startY = Math.round(y + (iconAndSecondsHeight / 2F) - (effectsHeight / 2F));
        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        // move the overflowing part to the buttonLocation box
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

        main.getUtils().enableStandardGLOptions();

        if (entity instanceof EntityArmorStand) {
            drawDeployableArmorStand((EntityArmorStand) entity, x + 1, y + 4);
        } else {
            MC.getTextureManager().bindTexture(deployable.getResourceLocation());
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        String secondsString = String.format("§e%ss", seconds);
        DrawUtils.drawText(
                secondsString,
                Math.round(x + (iconSize / 2F) - (MC.fontRendererObj.getStringWidth(secondsString) / 2F)),
                y + iconSize,
                ColorCode.WHITE.getColor(255)
        );

        for (int i = 0; i < display.size(); i++) {
            DrawUtils.drawText(
                    display.get(i),
                    x + iconSize + 2,
                    startY + (i * (MC.fontRendererObj.FONT_HEIGHT + spacingBetweenLines)),
                    ColorCode.WHITE.getColor(255)
            );
        }

        main.getUtils().restoreGLOptions();
    }

    @SubscribeEvent()
    public void onRenderRemoveBars(RenderGameOverlayEvent.Pre e) {
        if (main.getUtils().isOnSkyblock() && Feature.COMPACT_TAB_LIST.isEnabled()) {
            if (e.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
                if (TabListParser.getRenderColumns() != null) {
                    e.setCanceled(true);
                    TabListRenderer.render();
                }
            }
        }

        if (e.type == RenderGameOverlayEvent.ElementType.ALL) {
            if (main.getUtils().isOnSkyblock()) {
                if (Feature.HIDE_FOOD_ARMOR_BAR.isEnabled()) {
                    GuiIngameForge.renderFood = false;
                    GuiIngameForge.renderArmor = false;
                }
                if (Feature.HIDE_HEALTH_BAR.isEnabled()) {
                    GuiIngameForge.renderHealth = Feature.HIDE_HEALTH_BAR.isEnabled(FeatureSetting.HIDE_ONLY_OUTSIDE_RIFT)
                            && main.getUtils().isOnRift();
                }
                if (Feature.HIDE_PET_HEALTH_BAR.isEnabled()) {
                    GuiIngameForge.renderHealthMount = false;
                }
            } else {
                if (Feature.HIDE_HEALTH_BAR.isEnabled()) {
                    GuiIngameForge.renderHealth = true;
                }
                if (Feature.HIDE_FOOD_ARMOR_BAR.isEnabled()) {
                    GuiIngameForge.renderArmor = true;
                }
            }
        }
    }

    @SubscribeEvent()
    public void onRender(TickEvent.RenderTickEvent e) {
        if (guiToOpen == EnumUtils.GUIType.MAIN) {
            MC.displayGuiScreen(new SkyblockAddonsGui(guiPageToOpen, guiTabToOpen));
        } else if (guiToOpen == EnumUtils.GUIType.EDIT_LOCATIONS) {
            MC.displayGuiScreen(new LocationEditGui(guiPageToOpen, guiTabToOpen));
        } else if (guiToOpen == EnumUtils.GUIType.SETTINGS) {
            if (guiFeatureToOpen == Feature.ENCHANTMENT_LORE_PARSING) {
                MC.displayGuiScreen(new EnchantmentSettingsGui(1, guiPageToOpen, guiTabToOpen));
            } else {
                MC.displayGuiScreen(new SettingsGui(guiFeatureToOpen, 1, guiPageToOpen, guiTabToOpen, null));
            }
        } else if (guiToOpen == EnumUtils.GUIType.WARP) {
            MC.displayGuiScreen(new IslandWarpGui());
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
        float minecraftScale = new ScaledResolution(MC).getScaleFactor();
        if (!xAllignment) {
            x -= width / 2F * scale;
        } else {
            // TODO x -= dummyWidth / 2 * scale (Feature refactor)
            // TODO allignment to right edge of screen
            // x -= width * scale;
        }
        x = Math.round(x * minecraftScale) / minecraftScale;
        return x / scale;
    }

    public float transformY(float y, int height, float scale) {
        float minecraftScale = new ScaledResolution(MC).getScaleFactor();
        y -= height / 2F * scale;
        y = Math.round(y * minecraftScale) / minecraftScale;
        return y / scale;
    }

    @SubscribeEvent()
    public void onRenderWorld(RenderWorldLastEvent e) {
        HealingCircleManager.renderHealingCircleOverlays();
    }

    private void drawDeployableArmorStand(EntityArmorStand deployableArmorStand, float x, float y) {
        float prevRenderYawOffset = deployableArmorStand.renderYawOffset;
        float prevPrevRenderYawOffset = deployableArmorStand.prevRenderYawOffset;

        GlStateManager.pushMatrix();

        GlStateManager.enableDepth();
        GlStateManager.enableColorMaterial();

        GlStateManager.translate(x + 12.5F, y + 50F, 50F);
        GlStateManager.scale(-25F, 25F, 25F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(22.0F, 1.0F, 0.0F, 0.0F);

        RenderManager rendermanager = MC.getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        boolean shadowsEnabled = rendermanager.isRenderShadow();
        rendermanager.setRenderShadow(false);

        deployableArmorStand.setInvisible(true);
        float yaw = System.currentTimeMillis() % 1750 / 1750F * 360F;
        deployableArmorStand.renderYawOffset = yaw;
        deployableArmorStand.prevRenderYawOffset = yaw;

        rendermanager.renderEntityWithPosYaw(deployableArmorStand, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(shadowsEnabled);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GlStateManager.popMatrix();

        deployableArmorStand.renderYawOffset = prevRenderYawOffset;
        deployableArmorStand.prevRenderYawOffset = prevPrevRenderYawOffset;
    }

    private void drawEntity(EntityLivingBase entity, float x, float y, float yaw) {
        GlStateManager.pushMatrix();

        GlStateManager.enableDepth();
        GlStateManager.translate(x, y, 50F);
        GlStateManager.scale(-25, 25, 25);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.rotate(15F, 1, 0, 0);
        RenderHelper.enableGUIStandardItemLighting();

        entity.renderYawOffset = yaw;
        entity.prevRenderYawOffset = yaw;
        entity.rotationYawHead = yaw;
        entity.prevRotationYawHead = yaw;

        RenderManager rendermanager = MC.getRenderManager();
        rendermanager.setPlayerViewY(180.0f);
        boolean shadowsEnabled = rendermanager.isRenderShadow();
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(shadowsEnabled);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.popMatrix();
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
        HYPERION(ItemUtils.createItemStack(Items.iron_sword, "§6Hyperion", "HYPERION", false)),
        VALKYRIE(ItemUtils.createItemStack(Items.iron_sword, "§6Valkyrie", "VALKYRIE", false)),
        ASTRAEA(ItemUtils.createItemStack(Items.iron_sword, "§6Astraea", "ASTRAEA", false)),
        SCYLLA(ItemUtils.createItemStack(Items.iron_sword, "§6Scylla", "SCYLLA", false)),
        BAT_WAND(new ItemStack(Blocks.red_flower, 1, 2)),
        STARRED_BAT_WAND(new ItemStack(Blocks.red_flower, 1, 2)),
        MIDAS_STAFF(ItemUtils.createItemStack(Items.golden_shovel, "§6Midas Staff", "MIDAS_STAFF", false));

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