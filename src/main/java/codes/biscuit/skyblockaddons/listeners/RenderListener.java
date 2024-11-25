package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.*;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonClass;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonMilestone;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonPlayer;
import codes.biscuit.skyblockaddons.features.*;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonTracker;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonType;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonsSince;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleManager;
import codes.biscuit.skyblockaddons.features.deployables.Deployable;
import codes.biscuit.skyblockaddons.features.deployables.DeployableManager;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerBoss;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerDrop;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.features.spookyevent.CandyType;
import codes.biscuit.skyblockaddons.features.spookyevent.SpookyEventManager;
import codes.biscuit.skyblockaddons.features.tablist.TabListParser;
import codes.biscuit.skyblockaddons.features.tablist.TabListRenderer;
import codes.biscuit.skyblockaddons.gui.*;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.misc.Updater;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import codes.biscuit.skyblockaddons.mixins.hooks.FontRendererHook;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenTexturedShader;
import codes.biscuit.skyblockaddons.utils.*;
import com.mojang.authlib.GameProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.GuiNotification;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

import static codes.biscuit.skyblockaddons.utils.TextUtils.NUMBER_FORMAT;
import static net.minecraft.client.gui.Gui.icons;

public class RenderListener {

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

    private static final ResourceLocation CRITICAL = new ResourceLocation("skyblockaddons", "critical.png");
    private static final ResourceLocation RIFTSTALKER_BLOODFIEND = new ResourceLocation("skyblockaddons", "vampire.png");

    private static final ItemStack WATER_BUCKET = new ItemStack(Items.water_bucket);
    private static final ItemStack ROCK_PET = ItemUtils.createSkullItemStack(
            "§7[Lvl 100] §6Rock"
            , null
            , "988354a0-b787-3ca5-b782-d0db5e7b876a"
            , "7df8aab57136df2296c7c6f969ff25d58116fe2ec59b96a85ba4927e1f6779e6"
    );
    private static final ItemStack DOLPHIN_PET = ItemUtils.createSkullItemStack(
            "§7[Lvl 100] §6Dolphin"
            , null
            , "9001c25b-f0ff-3748-82c5-7bd117935ce2"
            , "1415d2c543e34bb88ede94d79b9427691fc9be72daad8831a9ef297180546e18"
    );
    private static final ItemStack CHEST = new ItemStack(Item.getItemFromBlock(Blocks.chest));
    private static final ItemStack SKULL = ItemUtils.createSkullItemStack(
            "Skull"
            , null
            , "c659cdd4-e436-4977-a6a7-d5518ebecfbb"
            , "1ae3855f952cd4a03c148a946e3f812a5955ad35cbcb52627ea4acd47d3081"
    );
    private static final ItemStack GREEN_CANDY = ItemUtils.createSkullItemStack(
            "Green Candy"
            , "GREEN_CANDY"
            , "e5190c90-5144-3e4e-a545-8499ea3503ca"
            , "e31c0bd76a655d5d8fea5b06daaf1fb8d8060bf0823ebbc6eb6f99c8ee5a35aa"
    );
    private static final ItemStack PURPLE_CANDY = ItemUtils.createSkullItemStack(
            "Purple Candy"
            , "PURPLE_CANDY"
            , "60a5c7bc-a65b-3772-889f-8831d4329fc4"
            , "91611d874e874e322a1199b3b7b9e934bbb0dbed587ee8fcd6ccc1b07e281651"
    );
    private static final ItemStack THUNDER_IN_A_BOTTLE = ItemUtils.createSkullItemStack(
            "§5Thunder in a Bottle",
            Collections.emptyList(),
            "THUNDER_IN_A_BOTTLE",
            "5f67bc23-bb55-35e6-8f01-b5534e4ecfca"
            , "24378b986e358555ee73f09b210d49ec13719de5ea88d75523770d31163f3aef"
    );

    private static final SlayerArmorProgress[] DUMMY_PROGRESSES = new SlayerArmorProgress[] {
            new SlayerArmorProgress(new ItemStack(Items.diamond_boots))
            , new SlayerArmorProgress(new ItemStack(Items.chainmail_leggings))
            , new SlayerArmorProgress(new ItemStack(Items.diamond_chestplate))
            , new SlayerArmorProgress(new ItemStack(Items.leather_helmet))
    };

    private static final Pattern DUNGEON_STAR_PATTERN = Pattern.compile("(?:(?:§[a-f0-9])?✪)+(?:§[a-f0-9]?[➊-➒])?");

    private static EntityArmorStand deployableDummyArmorStand;
    private static EntityZombie revenant;
    private static EntitySpider tarantula;
    private static EntityCaveSpider caveSpider;
    private static EntityWolf sven;
    private static EntityEnderman enderman;
    private static EntityBlaze inferno;
    private static EntityOtherPlayerMP riftstalker;

    private final SkyblockAddons main = SkyblockAddons.getInstance();

    @Getter @Setter private boolean predictHealth;
    @Getter @Setter private boolean predictMana;

    @Setter private boolean updateMessageDisplayed;

    @Setter
    private Feature subtitleFeature;
    @Getter @Setter private Feature titleFeature;

    @Setter private int arrowsLeft;

    @Setter private String cannotReachMobName;

    @Setter private long skillFadeOutTime = -1;
    @Setter private SkillType skill;
    @Setter private String skillText;

    @Setter
    private EnumUtils.GUIType guiToOpen;
    private int guiPageToOpen = 1;
    private EnumUtils.GuiTab guiTabToOpen = EnumUtils.GuiTab.MAIN;
    private Feature guiFeatureToOpen;

    private float maxRiftHealth = 0.0F;

    // caching
    private PetManager.Pet pet = null;
    private ItemStack petSkull = null;

    /**
     * Render overlays and warnings for clients without labymod.
     */
    @SubscribeEvent()
    public void onRenderRegular(RenderGameOverlayEvent.Post e) {
        if ((!main.isUsingLabymod() || Minecraft.getMinecraft().ingameGUI instanceof GuiIngameForge)) {
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
    public void onRenderLiving(RenderLivingEvent.Specials.Pre<EntityLivingBase> e) {
        Entity entity = e.entity;
        if (entity.hasCustomName()) {
            if (Feature.MINION_DISABLE_LOCATION_WARNING.isEnabled()) {
                if (entity.getCustomNameTag().startsWith("§cThis location isn't perfect! :(")) {
                    e.setCanceled(true);
                }
                if (entity.getCustomNameTag().startsWith("§c/!\\")) {
                    for (Entity listEntity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
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
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof LocationEditGui) && !(mc.currentScreen instanceof GuiNotification)) {
            GlStateManager.disableBlend();
            if (Feature.areEnabled(Feature.DARK_AUCTION_TIMER, Feature.SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES)) {
                float scale = main.getConfigValues().getGuiScale(Feature.DARK_AUCTION_TIMER);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                drawText(Feature.DARK_AUCTION_TIMER, scale, mc, null);
                GlStateManager.popMatrix();
            }
            if (Feature.areEnabled(Feature.FARM_EVENT_TIMER, Feature.SHOW_FARM_EVENT_TIMER_IN_OTHER_GAMES)) {
                float scale = main.getConfigValues().getGuiScale(Feature.FARM_EVENT_TIMER);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                drawText(Feature.FARM_EVENT_TIMER, scale, mc, null);
                GlStateManager.popMatrix();
            }
        }
    }

    /**
     * This renders all the title/subtitle warnings from features.
     */
    private void renderWarnings(ScaledResolution scaledResolution) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null || !main.getUtils().isOnSkyblock()) {
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
                case WARN_WHEN_FETCHUR_CHANGES:
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
                int stringWidth = mc.fontRendererObj.getStringWidth(text);

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
                        text
                        , (float) (-mc.fontRendererObj.getStringWidth(text) / 2)
                        , -20.0F
                        , main.getConfigValues().getColor(titleFeature)
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
                    if (arrowsLeft != -1) {
                        Translations.getMessage("messages.noArrowsLeft", TextUtils.formatNumber(arrowsLeft));
                    }
                    break;
            }

            if (text != null) {

                int stringWidth = mc.fontRendererObj.getStringWidth(text);

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
                        text
                        , -mc.fontRendererObj.getStringWidth(text) / 2F
                        , -23.0F
                        , main.getConfigValues().getColor(subtitleFeature)
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
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof LocationEditGui) && !(mc.currentScreen instanceof GuiNotification)) {
            GlStateManager.disableBlend();

            for (Feature feature : Feature.getGuiFeatures()) {
                if (feature.isEnabled()) {
                    if (feature == Feature.SKELETON_BAR && !main.getInventoryUtils().isWearingSkeletonHelmet())
                        continue;
                    if (feature == Feature.HEALTH_UPDATES && main.getPlayerListener().getHealthUpdate() == null)
                        continue;

                    float scale = main.getConfigValues().getGuiScale(feature);
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(scale, scale, 1);
                    feature.draw(scale, mc, null);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    /**
     * This draws all Skyblock Addons Bars, including the Health, Mana, Drill, and Skill XP bars
     *
     * @param feature        for which to render the bars
     * @param scale          the scale of the feature
     * @param mc             link to the minecraft session
     * @param buttonLocation the resizing gui, if present
     */
    public void drawBar(Feature feature, float scale, Minecraft mc, ButtonLocation buttonLocation) {
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
                if (Feature.HIDE_HEALTH_BAR_ON_RIFT.isEnabled() && main.getUtils().isOnRift())
                    return;
                fill = PlayerStats.HEALTH.getValue() / PlayerStats.MAX_HEALTH.getValue();
                break;
            default:
                return;
        }

        if (fill > 1) fill = 1;

        float x = main.getConfigValues().getActualX(feature);
        float y = main.getConfigValues().getActualY(feature);
        float scaleX = main.getConfigValues().getSizesX(feature);
        float scaleY = main.getConfigValues().getSizesY(feature);
        GlStateManager.scale(scaleX, scaleY, 1);

        x = transformXY(x, 71, scale * scaleX);
        y = transformXY(y, 5, scale * scaleY);

        // Render the button resize box if necessary
        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + 71, y, y + 5, scale, scaleX, scaleY);
        }

        SkyblockColor color = ColorUtils.getDummySkyblockColor(
                main.getConfigValues().getColor(feature)
                , main.getConfigValues().getChromaFeatures().contains(feature)
        );

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
                            main.getConfigValues().getColor(feature, textAlpha)
                            , main.getConfigValues().getChromaFeatures().contains(feature)
                    ); // so it fades out, 0.016 is the minimum alpha
                }
                break;
            case DRILL_FUEL_BAR:
                if (buttonLocation == null && !ItemUtils.isDrill(mc.thePlayer.getHeldItem())) return;
                break;
            case HEALTH_BAR:
                if (Feature.CHANGE_BAR_COLOR_FOR_POTIONS.isEnabled() && mc.thePlayer != null) {
                    if (mc.thePlayer.isPotionActive(19/* Poison */)) {
                        color = ColorUtils.getDummySkyblockColor(
                                ColorCode.DARK_GREEN.getColor()
                                , main.getConfigValues().getChromaFeatures().contains(feature)
                        );
                    } else if (mc.thePlayer.isPotionActive(20/* Wither */)) {
                        color = ColorUtils.getDummySkyblockColor(
                                ColorCode.DARK_GRAY.getColor()
                                , main.getConfigValues().getChromaFeatures().contains(feature)
                        );
                    } else if (mc.thePlayer.isPotionActive(22) /* Absorption */) {
                        if (PlayerStats.HEALTH.getValue() > PlayerStats.MAX_HEALTH.getValue()) {
                            fill = PlayerStats.MAX_HEALTH.getValue() / PlayerStats.HEALTH.getValue();
                            hasAbsorption = true;
                        }
                    }
                }

                if (main.getUtils().isOnRift()) {
                    float maxCurrentHealth = PlayerStats.MAX_RIFT_HEALTH.getValue();
                    fill = PlayerStats.HEALTH.getValue() / maxCurrentHealth;

                    if (maxCurrentHealth > maxRiftHealth)
                        maxRiftHealth = maxCurrentHealth;
                    else
                        widthScale = maxCurrentHealth / maxRiftHealth;

                    if (Float.isNaN(widthScale))
                        widthScale = 1.0F;
                }
                break;
        }

        main.getUtils().enableStandardGLOptions();
        // Draw the actual bar
        drawMultiLayeredBar(mc, color, x, y, fill, hasAbsorption, widthScale);

        main.getUtils().restoreGLOptions();
    }

    /**
     * Draws a multitextured bar:
     * Begins by coloring and rendering the empty bar.
     * Then, colors and renders the full bar up to the fraction {@param fill}.
     * Then, overlays the absorption portion of the bar in gold if the player has absorption hearts
     * Then, overlays (and does not color) an additional texture centered on the current progress of the bar.
     * Then, overlays (and does not color) a final style texture over the bar
     * @param mc link to the current minecraft session
     * @param color the color with which to render the bar
     * @param x the x position of the bar
     * @param y the y position of the bar
     * @param fill the fraction (from 0 to 1) of the bar that's full
     * @param hasAbsorption {@code true} if the player has absorption hearts
     */
    private void drawMultiLayeredBar(Minecraft mc, SkyblockColor color, float x, float y, float fill, boolean hasAbsorption, float widthScale) {
        int barHeight = 5;
        float barWidth = 71 * widthScale;
        float barFill = barWidth * fill;
        mc.getTextureManager().bindTexture(BARS);
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
        String message = updater.getMessageToRender();

        if (updater.hasUpdate() && message != null && !updateMessageDisplayed) {
            Minecraft mc = Minecraft.getMinecraft();
            String[] textList = main.getUtils().wrapSplitText(message, 36);

            int halfWidth = new ScaledResolution(mc).getScaledWidth() / 2;
            Gui.drawRect(
                    halfWidth - 110
                    , 20
                    , halfWidth + 110
                    , 53 + textList.length * 10
                    , main.getUtils().getDefaultBlue(140)
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

            main.getScheduler().schedule(Scheduler.CommandType.ERASE_UPDATE_MESSAGE, 10);

            if (!main.getUpdater().hasSentUpdateMessage()) {
                main.getUpdater().sendUpdateMessage();
            }
        }
    }

    /**
     * This renders a bar for the skeleton hat bones bar.
     */
    public void drawSkeletonBar(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        float x = main.getConfigValues().getActualX(Feature.SKELETON_BAR);
        float y = main.getConfigValues().getActualY(Feature.SKELETON_BAR);
        int bones = 0;
        if (!(mc.currentScreen instanceof LocationEditGui)) {
            for (Entity listEntity : mc.theWorld.loadedEntityList) {
                if (listEntity instanceof EntityItem
                        && listEntity.ridingEntity instanceof EntityArmorStand
                        && listEntity.ridingEntity.isInvisible()
                        && listEntity.getDistanceToEntity(mc.thePlayer) <= 8) {
                    bones++;
                }
            }
        } else {
            bones = 3;
        }
        if (bones > 3) bones = 3;

        int height = 16;
        int width = 3 * 16;

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

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
    public void drawScorpionFoilTicker(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        if (buttonLocation != null || main.getPlayerListener().getTickers() != -1) {
            float x = main.getConfigValues().getActualX(Feature.TICKER_CHARGES_DISPLAY);
            float y = main.getConfigValues().getActualY(Feature.TICKER_CHARGES_DISPLAY);

            int height = 9;
            int width = 3 * 11 + 9;

            x = transformXY(x, width, scale);
            y = transformXY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            }

            main.getUtils().enableStandardGLOptions();

            int maxTickers = (buttonLocation == null) ? main.getPlayerListener().getMaxTickers() : 4;
            for (int tickers = 0; tickers < maxTickers; tickers++) {
                mc.getTextureManager().bindTexture(TICKER_SYMBOL);
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
    public void drawIcon(float scale, Minecraft mc, ButtonLocation buttonLocation) {
        // There is no defense stat on Rift Dimension
        if (main.getUtils().isOnRift())
            return;

        if (Feature.USE_VANILLA_TEXTURE_DEFENCE.isDisabled()) {
            mc.getTextureManager().bindTexture(icons);
        } else {
            mc.getTextureManager().bindTexture(DEFENCE_VANILLA);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // The height and width of this element (box not included)
        int height = 9;
        int width = 9;
        float x = main.getConfigValues().getActualX(Feature.DEFENCE_ICON);
        float y = main.getConfigValues().getActualY(Feature.DEFENCE_ICON);
        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        main.getUtils().enableStandardGLOptions();

        if (buttonLocation == null) {
            mc.ingameGUI.drawTexturedModalRect(x, y, 34, 9, width, height);
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
    public void drawText(Feature feature, float scale, Minecraft mc, ButtonLocation buttonLocation) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        String text;
        boolean onRift = main.getUtils().isOnRift();
        int color = main.getConfigValues().getColor(feature);

        switch (feature) {
            case MANA_TEXT:
                text = TextUtils.formatNumber(PlayerStats.MANA.getValue()) + "/"
                        + TextUtils.formatNumber(PlayerStats.MAX_MANA.getValue())
                        + (Feature.MANA_TEXT_ICON.isEnabled() ? "✎" : "");
                break;

            case OVERFLOW_MANA:
                if (PlayerStats.OVERFLOW_MANA.getValue() == 0 && buttonLocation == null) return;
                text = TextUtils.formatNumber(PlayerStats.OVERFLOW_MANA.getValue()) + "ʬ";
                break;

            case HEALTH_TEXT:
                if (Feature.HIDE_HEALTH_TEXT_ON_RIFT.isEnabled() && onRift) return;

                // Dividing with 2 for show heart value instead of health value. 1 heart == 2 health
                boolean shouldHeart = Feature.HEART_INSTEAD_HEALTH_ON_RIFT.isEnabled() && onRift;

                text = TextUtils.formatNumber(PlayerStats.HEALTH.getValue() / (shouldHeart ? 2F : 1F)) + "/";
                if (main.getUtils().isOnRift()) {
                    text += TextUtils.formatNumber(PlayerStats.MAX_RIFT_HEALTH.getValue() / (shouldHeart ? 2F : 1F));
                } else {
                    text += TextUtils.formatNumber(PlayerStats.MAX_HEALTH.getValue());
                }
                if (Feature.HEALTH_TEXT_ICON.isEnabled()) {
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
                text = TextUtils.formatNumber(PlayerStats.DEFENCE.getValue()) + (Feature.DEFENCE_TEXT_ICON.isEnabled() ? "❈" : "");
                break;

            case OTHER_DEFENCE_STATS:
                text = main.getPlayerListener().getActionBarParser().getOtherDefense();
                if (buttonLocation != null && (text == null || text.isEmpty()))
                    text = "|||  T3!";
                if (text == null || text.isEmpty()) return;
                break;

            case EFFECTIVE_HEALTH_TEXT:
                if (onRift) return;
                text = TextUtils.formatNumber(
                        Math.round(PlayerStats.HEALTH.getValue() * (1 + PlayerStats.DEFENCE.getValue() / 100F))
                ) + (Feature.EFFECTIVE_HEALTH_TEXT_ICON.isEnabled() ? "❤" : "");
                break;

            case DRILL_FUEL_TEXT:
                boolean heldDrill = mc.thePlayer != null && ItemUtils.isDrill(mc.thePlayer.getHeldItem());

                if (heldDrill) {
                    text = TextUtils.formatNumber(PlayerStats.FUEL.getValue()) + "/";
                    if (Feature.ABBREVIATE_DRILL_FUEL_DENOMINATOR.isEnabled())
                        text += TextUtils.abbreviate((int) PlayerStats.MAX_FUEL.getValue());
                    else
                        text += TextUtils.formatNumber(PlayerStats.MAX_FUEL.getValue());
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
                if (mc.thePlayer != null) {
                    // 0.3xyz -> 3xy.z -> 3xy
                    int walkSpeed = (int) (mc.thePlayer.capabilities.getWalkSpeed() * 1000);
                    text = walkSpeed + "%";
                } else /* Dummy */ {
                    text = "123%";
                }
                break;

            case HEALTH_UPDATES:
                if (Feature.HIDE_HEALTH_UPDATES_ON_RIFT.isEnabled() && main.getUtils().isOnRift())
                    return;
                Float healthUpdate = main.getPlayerListener().getHealthUpdate();
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
                Calendar nextDarkAuction = Calendar.getInstance(TimeZone.getTimeZone("EST"));
                if (nextDarkAuction.get(Calendar.MINUTE) >= 55) {
                    nextDarkAuction.add(Calendar.HOUR_OF_DAY, 1);
                }
                nextDarkAuction.set(Calendar.MINUTE, 55);
                nextDarkAuction.set(Calendar.SECOND, 0);
                int differenceDAH = (int) (nextDarkAuction.getTimeInMillis() - System.currentTimeMillis());
                int minutesDAH = differenceDAH / 60000;
                int secondsDAH = (int) Math.round((double) (differenceDAH % 60000) / 1000);
                StringBuilder timestampDAH = new StringBuilder();
                if (minutesDAH < 10) {
                    timestampDAH.append("0");
                }
                timestampDAH.append(minutesDAH).append(":");
                if (secondsDAH < 10) {
                    timestampDAH.append("0");
                }
                timestampDAH.append(secondsDAH);
                text = timestampDAH.toString();
                break;

            case FARM_EVENT_TIMER:
                // The timezone of the server, to avoid problems with like timezones that are 30 minutes ahead or whatnot.
                Calendar nextFarmEvent = Calendar.getInstance(TimeZone.getTimeZone("EST"));
                if (nextFarmEvent.get(Calendar.MINUTE) >= 15) {
                    nextFarmEvent.add(Calendar.HOUR_OF_DAY, 1);
                }
                nextFarmEvent.set(Calendar.MINUTE, 15);
                nextFarmEvent.set(Calendar.SECOND, 0);
                int differenceFE = (int) (nextFarmEvent.getTimeInMillis() - System.currentTimeMillis());
                int minutesFE = differenceFE / 60000;
                int secondsFE = (int) Math.round((double) (differenceFE % 60000) / 1000);
                if (minutesFE < 40) {
                    StringBuilder timestampFE = new StringBuilder();
                    if (minutesFE < 10) {
                        timestampFE.append("0");
                    }
                    timestampFE.append(minutesFE).append(":");
                    if (secondsFE < 10) {
                        timestampFE.append("0");
                    }
                    timestampFE.append(secondsFE);
                    text = timestampFE.toString();
                } else {
                    StringBuilder timestampActive = new StringBuilder();
                    timestampActive.append("Active: ");
                    if (minutesFE - 40 < 10) {
                        timestampActive.append("0");
                    }
                    timestampActive.append(minutesFE - 40).append(":");
                    if (secondsFE < 10) {
                        timestampActive.append("0");
                    }
                    timestampActive.append(secondsFE);
                    text = timestampActive.toString();
                }
                break;

            case SKILL_DISPLAY:
                if (buttonLocation == null) {
                    text = skillText;
                    if (text == null) return;
                } else {
                    StringBuilder previewBuilder = new StringBuilder();
                    if (Feature.SHOW_SKILL_XP_GAINED.isEnabled()) {
                        previewBuilder.append("+123 ");
                    }
                    if (Feature.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP.isEnabled()) {
                        previewBuilder.append("40% ");
                    } else {
                        previewBuilder.append("(2000/5000) ");
                    }
                    if (Feature.SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL.isEnabled()) {
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
                        color = main.getConfigValues().getColor(feature, textAlpha); // so it fades out, 0.016 is the minimum alpha
                    }
                }
                break;

            case ZEALOT_COUNTER:
                if (Feature.ZEALOT_COUNTER_ZEALOT_SPAWN_AREAS_ONLY.isEnabled() &&
                        !LocationUtils.isOnZealotSpawnLocation() && buttonLocation == null) {
                    return;
                }
                text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getKills());
                break;

            case SHOW_TOTAL_ZEALOT_COUNT:
                if (Feature.SHOW_TOTAL_ZEALOT_COUNT_ZEALOT_SPAWN_AREAS_ONLY.isEnabled() &&
                        !LocationUtils.isOnZealotSpawnLocation() && buttonLocation == null) {
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
                if (Feature.SHOW_SUMMONING_EYE_COUNT_ZEALOT_SPAWN_AREAS_ONLY.isEnabled() &&
                        !LocationUtils.isOnZealotSpawnLocation() && buttonLocation == null) {
                    return;
                }
                text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount());
                break;

            case SHOW_AVERAGE_ZEALOTS_PER_EYE:
                if (Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE_ZEALOT_SPAWN_AREAS_ONLY.isEnabled() &&
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
                if ((main.getUtils().getMap() != Island.THE_END
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
                text = TextUtils.formatNumber(main.getPersistentValuesManager().getPersistentValues().getOresMined());
                break;

            case DOLPHIN_PET_TRACKER:
                if (Feature.SHOW_ONLY_HOLDING_FISHING_ROD.isEnabled() && !main.getPlayerListener().isHoldingRod())
                    return;
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

                ItemStack holdingItem = mc.thePlayer.getCurrentEquippedItem();
                String skyblockItemID = ItemUtils.getSkyblockItemID(mc.thePlayer.getHeldItem());

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
                    if (Feature.SHOW_FETCHUR_ITEM_NAME.isEnabled()) {
                        text = Translations.getMessage(
                                "messages.fetchurItem"
                                , fetchurItem.getItemStack().stackSize + "x " + fetchurItem.getItemText()
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
                    if (Feature.FIRE_FREEZE_WHEN_HOLDING.isEnabled() && !main.getPlayerListener().isHoldingFireFreeze())
                        return;

                    long fireFreezeTimer = main.getPlayerListener().getFireFreezeTimer();
                    if (fireFreezeTimer == 0) return;

                    double countdown = (fireFreezeTimer - System.currentTimeMillis()) / 1000D;

                    if (countdown > 0) {
                        text = String.format("Fire Freeze in %.2f", countdown);
                    } else {
                        if (Feature.FIRE_FREEZE_SOUND.isEnabled()) {
                            main.getUtils().playLoudSound("mob.wither.spawn", 1);
                        }
                        main.getPlayerListener().setFireFreezeTimer(0);
                        return;
                    }
                }
                break;

            case THUNDER_BOTTLE_DISPLAY:
                ItemStack emptyBottle = main.getInventoryUtils().getEmptyThunderBottle();
                boolean haveFullThunderBottle = main.getInventoryUtils().isHaveFullThunderBottle();
                final String thunderBottleCapacity = TextUtils.formatNumber(50000);

                if (buttonLocation == null && emptyBottle == null && !haveFullThunderBottle)
                    return;

                if (emptyBottle != null) {
                    text = TextUtils.formatNumber(ItemUtils.getThunderCharge(emptyBottle)) + "/" + thunderBottleCapacity;
                } else if (haveFullThunderBottle) {
                    text = "§aFull!";
                } else /*buttonLocation != null*/ {
                    text = TextUtils.formatNumber(49999) + "/" + thunderBottleCapacity;
                }
                break;

            case PET_DISPLAY:
                if (main.getUtils().isOnRift()) return;

                PetManager.Pet newPet = main.getPetCacheManager().getCurrentPet();
                if (newPet == null) {
                    return;
                } else if (pet != newPet) {
                    pet = newPet;
                    petSkull = ItemUtils.createSkullItemStack(null, null, newPet.getSkullId(), newPet.getTextureURL());
                }

                text = pet.getDisplayName();
                break;

            default:
                return;
        }

        float x = main.getConfigValues().getActualX(feature);
        float y = main.getConfigValues().getActualY(feature);

        int height = 7;
        int width = mc.fontRendererObj.getStringWidth(text);

        switch (feature) {
            case ZEALOT_COUNTER:
            case SHOW_AVERAGE_ZEALOTS_PER_EYE:
            case SHOW_TOTAL_ZEALOT_COUNT:
            case SHOW_SUMMONING_EYE_COUNT:
                width = mc.fontRendererObj.getStringWidth(text) + 18;
                height += 9;
                break;

            case ENDSTONE_PROTECTOR_DISPLAY:
                width += 18 + 2 + 16 + 2 + mc.fontRendererObj.getStringWidth(
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
                if (Feature.SHOW_FETCHUR_ITEM_NAME.isDisabled()) {
                    width += 18;
                    height += 9;
                }
                break;

            case DUNGEONS_COLLECTED_ESSENCES_DISPLAY:
                int maxNumberWidth = mc.fontRendererObj.getStringWidth("99");
                width = 18 + 2 + maxNumberWidth + 5 + 18 + 2 + maxNumberWidth;
                height = 18 * (int) Math.ceil(EssenceType.values().length / 2F);
                break;

            case SPIRIT_SCEPTRE_DISPLAY:
                width += 18 + mc.fontRendererObj.getStringWidth("12345");
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
                    width += 16 + 1 + mc.fontRendererObj.getStringWidth(TextUtils.formatNumber(green));
                }
                if (buttonLocation != null || purple > 0) {
                    if (green > 0) width += 1;
                    width += 16 + 1 + mc.fontRendererObj.getStringWidth(TextUtils.formatNumber(purple)) + 1;
                }
                height = 16 + 8;
                break;

            case PET_DISPLAY:
                width += 18;
                height += 9;
                if (main.getConfigValues().getPetItemStyle() != EnumUtils.PetItemStyle.NONE) {
                    height += 9;
                }
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

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

        switch (feature) {
            case DARK_AUCTION_TIMER:
                mc.getTextureManager().bindTexture(SIRIUS_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case FARM_EVENT_TIMER:
                mc.getTextureManager().bindTexture(FARM_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case ZEALOT_COUNTER:
                mc.getTextureManager().bindTexture(ENDERMAN_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case SHOW_TOTAL_ZEALOT_COUNT:
                mc.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case SHOW_SUMMONING_EYE_COUNT:
                mc.getTextureManager().bindTexture(SUMMONING_EYE_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case SHOW_AVERAGE_ZEALOTS_PER_EYE:
                mc.getTextureManager().bindTexture(ZEALOTS_PER_EYE_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);
                mc.getTextureManager().bindTexture(SLASH_ICON);
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
                mc.getTextureManager().bindTexture(IRON_GOLEM_ICON);
                DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();

                x += 16 + 2 + mc.fontRendererObj.getStringWidth(text) + 2;

                GlStateManager.color(1, 1, 1, 1);
                mc.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON);
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
                    amount = NUMBER_FORMAT.parse(dungeonMilestone.getValue());
                } catch (ParseException e) {
                    amount = -1;
                }
                String formattedAmount = TextUtils.formatNumber(amount);
                DrawUtils.drawText(
                        formattedAmount
                        , x + 18 + mc.fontRendererObj.getStringWidth(text) / 2F
                                - mc.fontRendererObj.getStringWidth(formattedAmount) / 2F
                        , y + 9
                        , color
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
                            none
                            , x + 16 + 2 + mc.fontRendererObj.getStringWidth(text)
                                    / 2F - mc.fontRendererObj.getStringWidth(none) / 2F
                            , y + 10
                            , color
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

                    float secretsWidth = mc.fontRendererObj.getStringWidth(String.valueOf(secrets));
                    float slashWidth = mc.fontRendererObj.getStringWidth("/");
                    float maxSecretsWidth = mc.fontRendererObj.getStringWidth(String.valueOf(maxSecrets));

                    float totalWidth = secretsWidth + slashWidth + maxSecretsWidth;

                    FontRendererHook.setupFeatureFont(feature);
                    DrawUtils.drawText(
                            "/"
                            , x + 16 + 2 + mc.fontRendererObj.getStringWidth(text)
                                    / 2F - totalWidth / 2F + secretsWidth
                            , y + 11
                            , color
                    );
                    FontRendererHook.endFeatureFont();

                    DrawUtils.drawText(
                            String.valueOf(secrets)
                            , x + 16 + 2 + mc.fontRendererObj.getStringWidth(text) / 2F - totalWidth / 2F
                            , y + 11
                            , secretsColor
                    );
                    DrawUtils.drawText(
                            String.valueOf(maxSecrets)
                            , x + 16 + 2 + mc.fontRendererObj.getStringWidth(text)
                                    / 2F - totalWidth / 2F + secretsWidth + slashWidth
                            , y + 11
                            , secretsColor
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

                ItemStack displayItem = DamageDisplayItem.getByID(ItemUtils.getSkyblockItemID(mc.thePlayer.getHeldItem()));
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
                        currentX += mc.fontRendererObj.getStringWidth(TextUtils.formatNumber(green)) + 1;
                    }

                    renderItem(PURPLE_CANDY, currentX, y);

                    currentX += 16 + 1;
                    FontRendererHook.setupFeatureFont(feature);
                    DrawUtils.drawText(TextUtils.formatNumber(purple), currentX, y + 4, color);
                    FontRendererHook.endFeatureFont();
                }

                FontRendererHook.setupFeatureFont(feature);
                text = TextUtils.formatNumber(points) + " Points";
                DrawUtils.drawText(text, x + width / 2F - mc.fontRendererObj.getStringWidth(text) / 2F, y + 16, color);
                FontRendererHook.endFeatureFont();
                break;

            case FETCHUR_TODAY:
                boolean showDwarven = Feature.SHOW_FETCHUR_ONLY_IN_DWARVENS.isDisabled()
                        || main.getUtils().getMap() == Island.DWARVEN_MINES;
                boolean showInventory = Feature.SHOW_FETCHUR_INVENTORY_OPEN_ONLY.isDisabled()
                        || Minecraft.getMinecraft().currentScreen != null;
                FetchurManager.FetchurItem fetchurItem = FetchurManager.getInstance().getCurrentFetchurItem();

                // Show if it's the gui button position, or the player hasn't given Fetchur,
                // and it shouldn't be hidden b/c of dwarven mines or inventory
                if (fetchurItem != null && (buttonLocation != null ||
                        (!FetchurManager.getInstance().hasFetchedToday() && showDwarven && showInventory))) {

                    FontRendererHook.setupFeatureFont(feature);

                    if (Feature.SHOW_FETCHUR_ITEM_NAME.isDisabled()) {
                        DrawUtils.drawText(text, x + 1, y + 4, color); // Line related to the "Fetchur wants" text
                        float offsetX = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
                        renderItemAndOverlay(
                                fetchurItem.getItemStack()
                                , String.valueOf(fetchurItem.getItemStack().stackSize)
                                , x + offsetX
                                , y
                        );
                    } else {
                        DrawUtils.drawText(text, x, y, color); // Line related to the "Fetchur wants" text
                    }
                    FontRendererHook.endFeatureFont();
                }
                break;

            case HEALTH_TEXT:
                // 22 -> Absorption
                if (mc.thePlayer != null && mc.thePlayer.isPotionActive(22)
                        && PlayerStats.HEALTH.getValue() > PlayerStats.MAX_HEALTH.getValue()) {
                    String formattedHealth = TextUtils.formatNumber(PlayerStats.HEALTH.getValue());
                    int formattedHealthWidth = mc.fontRendererObj.getStringWidth(formattedHealth);

                    color = ColorUtils.getDummySkyblockColor(
                            ColorCode.GOLD.getColor()
                            , main.getConfigValues().getChromaFeatures().contains(feature)
                    ).getColor();
                    FontRendererHook.setupFeatureFont(feature);
                    DrawUtils.drawText(formattedHealth, x, y, color);

                    color = main.getConfigValues().getColor(feature);
                    DrawUtils.drawText(
                            "/" + TextUtils.formatNumber(PlayerStats.MAX_HEALTH.getValue()) + (Feature.HEALTH_TEXT_ICON.isEnabled() ? "❤" : "")
                            , x + formattedHealthWidth
                            , y
                            , color
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
                ItemStack thunderBottle = main.getInventoryUtils().getEmptyThunderBottle();
                if (thunderBottle != null) {
                    renderItem(thunderBottle, x, y);
                } else /*buttonLocation != null || haveFullThunderBottle*/ {
                    renderItem(THUNDER_IN_A_BOTTLE, x, y);
                }
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);
                FontRendererHook.endFeatureFont();
                break;

            case PET_DISPLAY:
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(text, x + 18, y + 4, color);

                int line = 1; // maybe new lines can be added in the future?
                switch (main.getConfigValues().getPetItemStyle()) {
                    case DISPLAY_NAME:
                        if (pet.getPetInfo().getHeldItemId() == null) break;

                        String petDisplayName = PetManager.getInstance().getPetItemDisplayNameFromId(
                                pet.getPetInfo().getHeldItemId()
                        );
                        DrawUtils.drawText("Held Item: " + petDisplayName, x + 18, y + 16, color);
                        line++;
                        break;

                    case SHOW_ITEM:
                        if (pet.getPetInfo().getHeldItemId() == null) break;

                        PetManager petManager = PetManager.getInstance();
                        String petHeldItemId = pet.getPetInfo().getHeldItemId();

                        ItemStack petItemStack = petManager.getPetItemFromId(petHeldItemId);
                        Rarity petItemRarity = petManager.getPetItemRarityFromId(petHeldItemId);

                        String displayText = "Held Item:";
                        if (petHeldItemId.endsWith(petItemRarity.getLoreName())) {
                            // To recognize those with the same Item but different Rarity
                            displayText += " " + petItemRarity.getColorCode().toString() + petItemRarity.getLoreName();
                        }
                        DrawUtils.drawText(displayText, x + 18, y + 16, color);

                        renderItem(petItemStack, x + 18 + mc.fontRendererObj.getStringWidth(displayText), y + 10);
                        line++;
                        break;
                }
                FontRendererHook.endFeatureFont();

                // render pet
                renderItem(petSkull, x, y, line);
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
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
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
        Minecraft mc = Minecraft.getMinecraft();
        InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

        float currentX = x;
        float currentY;

        int maxNumberWidth;
        if (inventoryType == InventoryType.SALVAGING) {
            Set<Map.Entry<EssenceType, Integer>> entrySet = main.getDungeonManager().getSalvagedEssences().entrySet();
            if (entrySet.isEmpty()) return;

            String highestAmountStr = Collections.max(entrySet, Map.Entry.comparingByValue()).getValue().toString();
            maxNumberWidth = mc.fontRendererObj.getStringWidth(highestAmountStr);
        } else {
            maxNumberWidth = mc.fontRendererObj.getStringWidth("99");
        }

        int color = main.getConfigValues().getColor(Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY);

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
            mc.getTextureManager().bindTexture(essenceType.getResourceLocation());
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
    public void drawBaitList(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        if (!main.getPlayerListener().isHoldingRod() && buttonLocation == null) return;

        Map<BaitManager.BaitType, Integer> baits = BaitManager.getInstance().getBaitsInInventory();
        if (buttonLocation != null) {
            baits = BaitManager.DUMMY_BAITS;
        }

        int longestLineWidth = 0;
        for (Map.Entry<BaitManager.BaitType, Integer> entry : baits.entrySet()) {
            longestLineWidth = Math.max(
                    longestLineWidth
                    , mc.fontRendererObj.getStringWidth(TextUtils.formatNumber(entry.getValue()))
            );
        }

        float x = main.getConfigValues().getActualX(Feature.BAIT_LIST);
        float y = main.getConfigValues().getActualY(Feature.BAIT_LIST);

        int spacing = 1;
        int iconSize = 16;
        int width = iconSize + spacing + longestLineWidth;
        int height = iconSize * baits.size();

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
        }

        main.getUtils().enableStandardGLOptions();

        for (Map.Entry<BaitManager.BaitType, Integer> entry : baits.entrySet()) {
            if (entry.getValue() == 0) continue;

            GlStateManager.color(1, 1, 1, 1F);
            renderItem(entry.getKey().getItemStack(), x, y);

            int color = main.getConfigValues().getColor(Feature.BAIT_LIST);
            FontRendererHook.setupFeatureFont(Feature.BAIT_LIST);
            DrawUtils.drawText(
                    TextUtils.formatNumber(entry.getValue())
                    , x + iconSize + spacing
                    , y + (iconSize / 2F) - (8 / 2F)
                    , color
            );
            FontRendererHook.endFeatureFont();

            y += iconSize;
        }

        main.getUtils().restoreGLOptions();
    }

    public void drawSlayerTrackers(Feature feature, Minecraft mc, float scale, ButtonLocation buttonLocation) {
        boolean colorByRarity;
        boolean textMode;
        SlayerBoss slayerBoss;
        EnumUtils.SlayerQuest quest = main.getUtils().getSlayerQuest();

        switch (feature) {
            case REVENANT_SLAYER_TRACKER:
                if (buttonLocation == null && Feature.HIDE_WHEN_NOT_IN_CRYPTS.isEnabled()
                        && (quest != EnumUtils.SlayerQuest.REVENANT_HORROR
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.REVENANT_HORROR))) {
                    return;
                }
                colorByRarity = Feature.REVENANT_COLOR_BY_RARITY.isEnabled();
                textMode = Feature.REVENANT_TEXT_MODE.isEnabled();
                slayerBoss = SlayerBoss.REVENANT;
                break;

            case TARANTULA_SLAYER_TRACKER:
                if (buttonLocation == null && Feature.HIDE_WHEN_NOT_IN_SPIDERS_DEN.isEnabled()
                        && (quest != EnumUtils.SlayerQuest.TARANTULA_BROODFATHER
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.TARANTULA_BROODFATHER))) {
                    return;
                }
                colorByRarity = Feature.TARANTULA_COLOR_BY_RARITY.isEnabled();
                textMode = Feature.TARANTULA_TEXT_MODE.isEnabled();
                slayerBoss = SlayerBoss.TARANTULA;
                break;

            case SVEN_SLAYER_TRACKER:
                if (buttonLocation == null && Feature.HIDE_WHEN_NOT_IN_CASTLE.isEnabled()
                        && (quest != EnumUtils.SlayerQuest.SVEN_PACKMASTER
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.SVEN_PACKMASTER))) {
                    return;
                }
                colorByRarity = Feature.SVEN_COLOR_BY_RARITY.isEnabled();
                textMode = Feature.SVEN_TEXT_MODE.isEnabled();
                slayerBoss = SlayerBoss.SVEN;
                break;

            case VOIDGLOOM_SLAYER_TRACKER:
                if (buttonLocation == null && Feature.HIDE_WHEN_NOT_IN_END.isEnabled()
                        && (quest != EnumUtils.SlayerQuest.VOIDGLOOM_SERAPH
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.VOIDGLOOM_SERAPH))) {
                    return;
                }
                colorByRarity = Feature.ENDERMAN_COLOR_BY_RARITY.isEnabled();
                textMode = Feature.ENDERMAN_TEXT_MODE.isEnabled();
                slayerBoss = SlayerBoss.VOIDGLOOM;
                break;

            case INFERNO_SLAYER_TRACKER:
                if (buttonLocation == null && Feature.HIDE_WHEN_NOT_IN_CRIMSON.isEnabled()
                        && (quest != EnumUtils.SlayerQuest.INFERNO_DEMONLORD
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.INFERNO_DEMONLORD))) {
                    return;
                }
                colorByRarity = Feature.INFERNO_COLOR_BY_RARITY.isEnabled();
                textMode = Feature.INFERNO_TEXT_MODE.isEnabled();
                slayerBoss = SlayerBoss.INFERNO;
                break;

            case RIFTSTALKER_SLAYER_TRACKER:
                if (buttonLocation == null && Feature.HIDE_WHEN_NOT_IN_RIFT.isEnabled()
                        && (quest != EnumUtils.SlayerQuest.RIFTSTALKER_BLOODFIEND
                        || !LocationUtils.isOnSlayerLocation(EnumUtils.SlayerQuest.RIFTSTALKER_BLOODFIEND))) {
                    return;
                }
                colorByRarity = Feature.RIFTSTALKER_COLOR_BY_RARITY.isEnabled();
                textMode = Feature.RIFTSTALKER_TEXT_MODE.isEnabled();
                slayerBoss = SlayerBoss.RIFTSTALKER;
                break;

            default:
                return;
        }

        float x = main.getConfigValues().getActualX(feature);
        float y = main.getConfigValues().getActualY(feature);
        int color = main.getConfigValues().getColor(feature);

        if (textMode) {
            int lineHeight = 8;
            int spacer = 3;

            int lines = 0;
            int spacers = 0;

            int longestLineWidth = mc.fontRendererObj.getStringWidth(slayerBoss.getDisplayName());
            lines++;
            spacers++;

            int longestSlayerDropLineWidth = mc.fontRendererObj.getStringWidth(
                    Translations.getMessage("slayerTracker.bossesKilled")
            );
            int longestCount = mc.fontRendererObj.getStringWidth(
                    String.valueOf(SlayerTracker.getInstance().getSlayerKills(slayerBoss))
            );
            lines++;
            spacers++;

            for (SlayerDrop drop : slayerBoss.getDrops()) {
                longestSlayerDropLineWidth = Math.max(
                        longestSlayerDropLineWidth
                        , mc.fontRendererObj.getStringWidth(drop.getDisplayName())
                );
                longestCount = Math.max(
                        longestCount
                        , mc.fontRendererObj.getStringWidth(
                                String.valueOf(SlayerTracker.getInstance().getDropCount(drop))
                        )
                );
                lines++;
            }

            int width = Math.max(longestLineWidth, longestSlayerDropLineWidth + 8 + longestCount);
            int height = lines * 8 + spacer * spacers;

            x = transformXY(x, width, scale);
            y = transformXY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            }

            FontRendererHook.setupFeatureFont(feature);

            DrawUtils.drawText(slayerBoss.getDisplayName(), x, y, color);
            y += lineHeight + spacer;
            DrawUtils.drawText(Translations.getMessage("slayerTracker.bossesKilled"), x, y, color);
            String text = String.valueOf(SlayerTracker.getInstance().getSlayerKills(slayerBoss));
            DrawUtils.drawText(text, x + width - mc.fontRendererObj.getStringWidth(text), y, color);
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
                DrawUtils.drawText(text, x + width - mc.fontRendererObj.getStringWidth(text), y, currentColor);
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
                int width = mc.fontRendererObj.getStringWidth(
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

            x = transformXY(x, width, scale);
            y = transformXY(y, height, scale);

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
                                null
                                , null
                                , "45012ee3-29fd-42ed-908b-648c731c7457"
                                , "1fc0184473fe882d2895ce7cbc8197bd40ff70bf10d3745de97b6c2a9c5fc78f"
                        );
                    }
                    GlStateManager.color(1, 1, 1, 1);
                    revenant.ticksExisted = (int) main.getNewScheduler().getTotalTicks();
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
                    enderman.ticksExisted = (int) main.getNewScheduler().getTotalTicks();
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
                    inferno.ticksExisted = (int) main.getNewScheduler().getTotalTicks();
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
                    text
                    , x + textCenterX - mc.fontRendererObj.getStringWidth(text) / 2F
                    , y + entityRenderY
                    , color
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
                        TextUtils.abbreviate(SlayerTracker.getInstance().getDropCount(slayerDrop))
                        , currentX + iconWidth + iconTextOffset
                        , currentY + 8
                        , currentColor
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

    public void drawDragonTrackers(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        if (Feature.DRAGON_STATS_TRACKER_NEST_ONLY.isEnabled()
                && !LocationUtils.isOn("Dragon's Nest") && buttonLocation == null) {
            return;
        }

        List<DragonType> recentDragons = DragonTracker.getInstance().getRecentDragons();
        if (recentDragons.isEmpty() && buttonLocation != null) {
            recentDragons = DragonTracker.getDummyDragons();
        }

        boolean colorByRarity = Feature.DRAGON_STATS_TRACKER_COLOR_BY_RARITY.isEnabled();
        boolean textMode = Feature.DRAGON_STATS_TRACKER_TEXT_MODE.isEnabled();

        int spacerHeight = 3;
        String never = Translations.getMessage("dragonTracker.never");
        int width;
        int height;
        if (textMode) {
            int lines = 0;
            int spacers = 0;

            int longestLineWidth = mc.fontRendererObj.getStringWidth(
                    Translations.getMessage("dragonTracker.recentDragons")
            );
            lines++;
            spacers++;

            spacers++;
            longestLineWidth = Math.max(
                    longestLineWidth
                    , mc.fontRendererObj.getStringWidth(Translations.getMessage("dragonTracker.dragonsSince"))
            );
            lines++;
            spacers++;

            for (DragonType dragon : recentDragons) {
                longestLineWidth = Math.max(longestLineWidth, mc.fontRendererObj.getStringWidth(dragon.getDisplayName()));
                lines++;
            }

            int longestCount = 0;
            int longestDragonsSinceLineWidth = 0;
            for (DragonsSince dragonsSince : DragonsSince.values()) {
                longestDragonsSinceLineWidth = Math.max(
                        longestDragonsSinceLineWidth
                        , mc.fontRendererObj.getStringWidth(dragonsSince.getDisplayName()
                        )
                );
                int dragonsSinceValue = DragonTracker.getInstance().getDragsSince(dragonsSince);
                longestCount = Math.max(
                        longestCount
                        , mc.fontRendererObj.getStringWidth(
                                dragonsSinceValue == 0 ? never : String.valueOf(dragonsSinceValue)
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

        float x = main.getConfigValues().getActualX(Feature.DRAGON_STATS_TRACKER);
        float y = main.getConfigValues().getActualY(Feature.DRAGON_STATS_TRACKER);
        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
        }

        int color = main.getConfigValues().getColor(Feature.DRAGON_STATS_TRACKER);

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
            color = main.getConfigValues().getColor(Feature.DRAGON_STATS_TRACKER);
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
                DrawUtils.drawText(text, x + width - mc.fontRendererObj.getStringWidth(text), y, color);
                y += 8;
                FontRendererHook.endFeatureFont();
            }
        }
    }

    public void drawRevenantIndicator(float scale, Minecraft mc, ButtonLocation buttonLocation) {
        float x = main.getConfigValues().getActualX(Feature.SLAYER_INDICATOR);
        float y = main.getConfigValues().getActualY(Feature.SLAYER_INDICATOR);

        int longest = -1;
        SlayerArmorProgress[] progresses = main.getInventoryUtils().getSlayerArmorProgresses();
        if (buttonLocation != null) progresses = DUMMY_PROGRESSES;
        for (SlayerArmorProgress progress : progresses) {
            if (progress == null) continue;

            int textWidth = mc.fontRendererObj.getStringWidth(progress.getPercent() + "% (" + progress.getDefence() + ")");
            if (textWidth > longest) {
                longest = textWidth;
            }
        }
        if (longest == -1) return;

        int height = 15 * 4;
        int width = 16 + 2 + longest;

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

        EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(Feature.SLAYER_INDICATOR);
        boolean downwards = (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT);

        int color = main.getConfigValues().getColor(Feature.SLAYER_INDICATOR);

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
            FontRendererHook.setupFeatureFont(Feature.SLAYER_INDICATOR);
            DrawUtils.drawText(progress.getPercent() + "% (", currentX, fixedY + 5, color);
            FontRendererHook.endFeatureFont();

            currentX += mc.fontRendererObj.getStringWidth(progress.getPercent() + "% (");
            DrawUtils.drawText(progress.getDefence(), currentX, fixedY + 5, 0xFFFFFFFF);

            currentX += mc.fontRendererObj.getStringWidth(progress.getDefence());
            FontRendererHook.setupFeatureFont(Feature.SLAYER_INDICATOR);
            DrawUtils.drawText(")", currentX, fixedY + 5, color);
            FontRendererHook.endFeatureFont();

            drawnCount++;
        }

        main.getUtils().restoreGLOptions();
    }

    private void renderItem(ItemStack item, float x, float y) {
        renderItem(item, x, y, 1);
    }

    /**
     * The main purpose is scale the item for make it compatible for add new lines e.g. scale with two for add 2nd line
     * <br>FIXME may not be accurate, negative scales not tested, fix this mess
     */
    private void renderItem(ItemStack item, float x, float y, float scale) {
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();

        GlStateManager.pushMatrix();
        if (scale > 1) {
            GlStateManager.scale(scale, scale, 1f);
            // For save space between rendered skull and text
            // 16 = texture width, 10 = texture width without space (?)
            GlStateManager.translate(
                    -8 - Math.log(scale),// 16 / 2 = padding TODO add it as parameter
                    0, // TODO add it as parameter
                    0
            );
        }
        GlStateManager.translate(x / scale, y / scale, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(item, 0, 0);
        GlStateManager.popMatrix();

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    private void renderItemAndOverlay(ItemStack item, String name, float x, float y) {
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(item, 0, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemOverlayIntoGUI(
                Minecraft.getMinecraft().fontRendererObj, item, 0, 0, name
        );
        GlStateManager.popMatrix();

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    private static final List<ItemDiff> DUMMY_PICKUP_LOG = new ArrayList<>(
            Arrays.asList(
                    new ItemDiff(ColorCode.DARK_PURPLE + "Forceful Ember Chestplate", 1)
                    , new ItemDiff("Boat", -1)
                    , new ItemDiff(ColorCode.BLUE + "Aspect of the End", 1)
            )
    );

    public void drawItemPickupLog(float scale, ButtonLocation buttonLocation) {
        float x = main.getConfigValues().getActualX(Feature.ITEM_PICKUP_LOG);
        float y = main.getConfigValues().getActualY(Feature.ITEM_PICKUP_LOG);

        EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(Feature.ITEM_PICKUP_LOG);
        boolean downwards = anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT || anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT;

        int lineHeight = 8 + 1; // 1 pixel spacer
        int height = lineHeight * 3 - 1;
        int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth("+ 1x Forceful Ember Chestplate");

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

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
            String text = String.format("%s %sx §r%s", itemDiff.getAmount() > 0 ? "§a+" : "§c-",
                    Math.abs(itemDiff.getAmount()), itemDiff.getDisplayName());
            float stringY = y + (i * lineHeight);
            if (!downwards) {
                stringY = y + height - (i * lineHeight) - 8;
            }

            DrawUtils.drawText(text, x, stringY, 0xFFFFFFFF);
            i++;
        }

        main.getUtils().restoreGLOptions();
    }

    public void drawDeployableStatus(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        DeployableManager.DeployableEntry activeDeployable = DeployableManager.getInstance().getActiveDeployable();
        if (buttonLocation != null && activeDeployable == null) {
            activeDeployable = DeployableManager.DUMMY_POWER_ORB_ENTRY;
        }
        if (activeDeployable != null) {
            Deployable deployable = activeDeployable.getDeployable();
            int seconds = activeDeployable.getSeconds();

            EnumUtils.DeployableDisplayStyle displayStyle = main.getConfigValues().getDeployableDisplayStyle();
            if (displayStyle == EnumUtils.DeployableDisplayStyle.DETAILED) {
                drawDetailedDeployableStatus(mc, scale, buttonLocation, deployable, seconds);
            } else {
                drawCompactDeployableStatus(mc, scale, buttonLocation, deployable, seconds);
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
    private void drawCompactDeployableStatus(Minecraft mc, float scale, ButtonLocation buttonLocation, Deployable deployable, int seconds) {
        float x = main.getConfigValues().getActualX(Feature.DEPLOYABLE_STATUS_DISPLAY);
        float y = main.getConfigValues().getActualY(Feature.DEPLOYABLE_STATUS_DISPLAY);

        String secondsString = String.format("§e%ss", seconds);
        int spacing = 1;
        int iconSize = mc.fontRendererObj.FONT_HEIGHT * 3; // 3 because it looked the best
        int width = iconSize + spacing + mc.fontRendererObj.getStringWidth(secondsString);

        x = transformXY(x, width, scale);
        y = transformXY(y, iconSize, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + iconSize, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        Entity entity = null;
        DeployableManager.DeployableEntry activeDeployable = DeployableManager.getInstance().getActiveDeployable();
        if (activeDeployable != null) {
            UUID uuidOfActiveDep = activeDeployable.getUuid();
            if (uuidOfActiveDep != null) {
                entity = Utils.getEntityByUUID(uuidOfActiveDep);
            }
        }

        if (entity == null && buttonLocation != null) {
            entity = getDeployableDummyArmorStand();
        }

        main.getUtils().enableStandardGLOptions();

        if (entity instanceof EntityArmorStand) {
            drawDeployableArmorStand((EntityArmorStand) entity, x + 1, y + 4);
        } else {
            mc.getTextureManager().bindTexture(deployable.getResourceLocation());
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        DrawUtils.drawText(
                secondsString
                , x + spacing + iconSize
                , y + (iconSize / 2F) - (8 / 2F)
                , ColorCode.WHITE.getColor(255)
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
    private void drawDetailedDeployableStatus(Minecraft mc, float scale, ButtonLocation buttonLocation, Deployable deployable, int seconds) {
        float x = main.getConfigValues().getActualX(Feature.DEPLOYABLE_STATUS_DISPLAY);
        float y = main.getConfigValues().getActualY(Feature.DEPLOYABLE_STATUS_DISPLAY);

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

        if (deployable.getStrength() > 0) {
            display.add(String.format("§c+%d ❁ ", deployable.getStrength()));
        }

        if (deployable.getVitality() > 0.0) {
            double vit = deployable.getVitality();
            display.add(String.format("§4+%s ♨ ", vit % 1 == 0.0 ? Integer.toString((int) vit) : vit));
        }

        if (deployable.getMending() > 0.0) {
            double mending = deployable.getMending();
            display.add(String.format("§a+%s ☄ ", mending % 1 == 0.0 ? Integer.toString((int) mending) : mending));
        }

        if (deployable.getTrueDefense() > 0) {
            display.add(String.format("§f+%s ❂ ", deployable.getTrueDefense()));
        }

        if (deployable.getFerocity() > 0) {
            display.add(String.format("§c+%s ⫽ ", deployable.getFerocity()));
        }

        if (deployable.getBonusAttackSpeed() > 0) {
            display.add(String.format("§e+%s%% ⚔ ", deployable.getBonusAttackSpeed()));
        }

        // For better visual (maybe?)
        if (Feature.EXPAND_DEPLOYABLE_STATUS.isEnabled() && display.size() > 3) {
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
        int iconSize = mc.fontRendererObj.FONT_HEIGHT * 3; // 3 because it looked the best
        int iconAndSecondsHeight = iconSize + mc.fontRendererObj.FONT_HEIGHT;

        int effectsHeight = (mc.fontRendererObj.FONT_HEIGHT + spacingBetweenLines) * display.size();
        int width = iconSize + 2 + longestLine.map(mc.fontRendererObj::getStringWidth).orElseGet(() ->
                mc.fontRendererObj.getStringWidth(display.get(0))
        );
        int height = Math.max(effectsHeight, iconAndSecondsHeight);

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        float startY = Math.round(y + (iconAndSecondsHeight / 2f) - (effectsHeight / 2f));
        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, startY - spacingBetweenLines, startY + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        Entity entity = null;
        DeployableManager.DeployableEntry activeDeployable = DeployableManager.getInstance().getActiveDeployable();
        if (activeDeployable != null) {
            UUID uuidOfActiveDep = activeDeployable.getUuid();
            if (uuidOfActiveDep != null) {
                entity = Utils.getEntityByUUID(uuidOfActiveDep);
            }
        }

        if (entity == null && buttonLocation != null) {
            entity = getDeployableDummyArmorStand();
        }

        main.getUtils().enableStandardGLOptions();

        if (entity instanceof EntityArmorStand) {
            drawDeployableArmorStand((EntityArmorStand) entity, x + 1, y + 4);
        } else {
            mc.getTextureManager().bindTexture(deployable.getResourceLocation());
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        String secondsString = String.format("§e%ss", seconds);
        DrawUtils.drawText(
                secondsString
                , Math.round(x + (iconSize / 2F) - (mc.fontRendererObj.getStringWidth(secondsString) / 2F))
                , y + iconSize, ColorCode.WHITE.getColor(255)
        );

        for (int i = 0; i < display.size(); i++) {
            DrawUtils.drawText(
                    display.get(i)
                    , x + iconSize + 2
                    , startY + (i * (mc.fontRendererObj.FONT_HEIGHT + spacingBetweenLines)), ColorCode.WHITE.getColor(255)
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
                    GuiIngameForge.renderHealth = Feature.HIDE_ONLY_OUTSIDE_RIFT.isEnabled()
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
            Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(guiPageToOpen, guiTabToOpen));
        } else if (guiToOpen == EnumUtils.GUIType.EDIT_LOCATIONS) {
            Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(guiPageToOpen, guiTabToOpen));
        } else if (guiToOpen == EnumUtils.GUIType.SETTINGS) {
            if (guiFeatureToOpen == Feature.ENCHANTMENT_LORE_PARSING) {
                Minecraft.getMinecraft().displayGuiScreen(
                        new EnchantmentSettingsGui(
                                guiFeatureToOpen
                                , 1
                                , guiPageToOpen
                                , guiTabToOpen
                                , guiFeatureToOpen.getSettings()
                        )
                );
            } else {
                Minecraft.getMinecraft().displayGuiScreen(
                        new SettingsGui(
                                guiFeatureToOpen
                                , 1
                                , guiPageToOpen
                                , guiTabToOpen
                                , guiFeatureToOpen.getSettings()
                        )
                );
            }
        } else if (guiToOpen == EnumUtils.GUIType.WARP) {
            Minecraft.getMinecraft().displayGuiScreen(new IslandWarpGui());
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

    public float transformXY(float xy, int widthHeight, float scale) {
        float minecraftScale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        xy -= widthHeight / 2F * scale;
        xy = Math.round(xy * minecraftScale) / minecraftScale;
        return xy / scale;
    }

    @SubscribeEvent()
    public void onRenderWorld(RenderWorldLastEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        float partialTicks = e.partialTicks;

        HealingCircleManager.renderHealingCircleOverlays(partialTicks);

        if (main.getUtils().isOnSkyblock() && main.getUtils().isInDungeon()
                && (Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES.isEnabled()
                || Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY.isEnabled())
        ) {
            Entity renderViewEntity = mc.getRenderViewEntity();
            Vector3d viewPosition = Utils.getPlayerViewPosition();

            int iconSize = 25;

            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                if (renderViewEntity == entity) {
                    continue;
                }

                if (!main.getDungeonManager().getTeammates().containsKey(entity.getName())) {
                    continue;
                }

                DungeonPlayer dungeonPlayer = main.getDungeonManager().getTeammates().get(entity.getName());

                double x = MathUtils.interpolateX(entity, partialTicks);
                double y = MathUtils.interpolateY(entity, partialTicks);
                double z = MathUtils.interpolateZ(entity, partialTicks);

                x -= viewPosition.x;
                y -= viewPosition.y;
                z -= viewPosition.z;

                if (Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY.isEnabled()) {
                    y += 0.35F;
                }

                if (entity.isSneaking()) {
                    y -= 0.65F;
                }

                double distanceScale = Math.max(1, renderViewEntity.getPositionVector().distanceTo(entity.getPositionVector()) / 10F);

                if (Feature.OUTLINE_DUNGEON_TEAMMATES.isEnabled()) {
                    y += entity.height + 0.75F + (iconSize * distanceScale) / 40F;
                } else {
                    y += entity.height / 2F + 0.25F;
                }

                float f = 1.6F;
                float f1 = 0.016666668F * f;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-f1, -f1, f1);

                GlStateManager.scale(distanceScale, distanceScale, distanceScale);

                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.enableTexture2D();
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.enableAlpha();

                if (Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES.isEnabled()
                        && (!dungeonPlayer.isGhost() && (dungeonPlayer.isCritical() || dungeonPlayer.isLow()))) {
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();

                    mc.getTextureManager().bindTexture(CRITICAL);
                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                    worldrenderer.pos(-iconSize / 2F, -iconSize / 2f, 0).tex(0, 0).endVertex();
                    worldrenderer.pos(-iconSize / 2F, iconSize / 2F, 0).tex(0, 1).endVertex();
                    worldrenderer.pos(iconSize / 2F, iconSize / 2F, 0).tex(1, 1).endVertex();
                    worldrenderer.pos(iconSize / 2F, -iconSize / 2F, 0).tex(1, 0).endVertex();
                    tessellator.draw();

                    String text = "";
                    if (dungeonPlayer.isLow()) {
                        text = "LOW";
                    } else if (dungeonPlayer.isCritical()) {
                        text = "CRITICAL";
                    }

                    mc.fontRendererObj.drawString(
                            text
                            , -mc.fontRendererObj.getStringWidth(text) / 2F
                            , iconSize / 2F + 2
                            , -1
                            , true
                    );
                }

                if (!dungeonPlayer.isGhost() && Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY.isEnabled()) {
                    if (shouldRenderNameOverlay(entity)) {
                        String nameOverlay =
                                ColorCode.YELLOW + "[" + dungeonPlayer.getDungeonClass().getFirstLetter() + "] "
                                        + ColorCode.GREEN + entity.getName();
                        mc.fontRendererObj.drawString(
                                nameOverlay
                                , -mc.fontRendererObj.getStringWidth(nameOverlay) / 2F
                                , iconSize / 2F + 13
                                , -1
                                , true
                        );
                    }
                }

                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            }
        }
    }

    /**
     * Checks {@link Feature#STOP_NAME_OVERLAY_WHEN_CLOSE} conditions
     * @param teammate teammate
     * @return true if {@link Feature#STOP_NAME_OVERLAY_WHEN_CLOSE} enabled and conditions are met or disabled
     */
    private boolean shouldRenderNameOverlay(EntityPlayer teammate) {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        return Feature.STOP_NAME_OVERLAY_WHEN_CLOSE.isDisabled()
                || teammate.isSneaking()
                || teammate.getDistanceToEntity(thePlayer) > 10
                || !teammate.canEntityBeSeen(thePlayer);
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

        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
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

        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
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

    public EntityArmorStand getDeployableDummyArmorStand() {
        if (deployableDummyArmorStand != null) {
            return deployableDummyArmorStand;
        }

        deployableDummyArmorStand = new EntityArmorStand(Utils.getDummyWorld());

        ItemStack deployableItemStack = ItemUtils.createSkullItemStack(
                null
                , null
                , "3ae3572b-2679-40b4-ba50-14dd58cbbbf7"
                , "c0062cc98ebda72a6a4b89783adcef2815b483a01d73ea87b3df76072a89d13b"
        );

        deployableDummyArmorStand.setCurrentItemOrArmor(4, deployableItemStack);

        return deployableDummyArmorStand;
    }

    @AllArgsConstructor
    private enum DamageDisplayItem {
        HYPERION(ItemUtils.createItemStack(Items.iron_sword, "§6Hyperion", "HYPERION", false)),
        VALKYRIE(ItemUtils.createItemStack(Items.iron_sword, "§6Valkyrie", "VALKYRIE", false)),
        ASTRAEA(ItemUtils.createItemStack(Items.iron_sword, "§6Astraea", "ASTRAEA", false)),
        SCYLLA(ItemUtils.createItemStack(Items.iron_sword, "§6Scylla", "SCYLLA", false)),
        BAT_WAND(new ItemStack(Blocks.red_flower, 1, 2)),
        STARRED_BAT_WAND(new ItemStack(Blocks.red_flower, 1, 2)),
        MIDAS_STAFF(ItemUtils.createItemStack(Items.golden_shovel, "§6Midas Staff", "MIDAS_STAFF", false));

        private final ItemStack itemStack;

        public static ItemStack getByID(String id) {
            for (DamageDisplayItem displayItem : DamageDisplayItem.values()) {
                if (displayItem.name().equals(id))
                    return displayItem.itemStack;
            }
            return null;
        }
    }

}
