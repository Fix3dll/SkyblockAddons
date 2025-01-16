package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.Island;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.core.feature.FeatureSetting;
import codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.gui.buttons.feature.ButtonLocation;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderLivingEvent.Specials.Pre;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrevorTrapperTracker {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final Pattern TRACKED_ANIMAL_NAME_PATTERN = Pattern.compile("\\[Lv[0-9]+] (?<rarity>[a-zA-Z]+) (?<animal>[a-zA-Z]+) .*❤");
    private static final Pattern TREVOR_FIND_ANIMAL_PATTERN = Pattern.compile("\\[NPC] Trevor: You can find your (?<rarity>[A-Z]+) animal near the [a-zA-Z ]+\\.");
    private static final Pattern ANIMAL_DIED_PATTERN = Pattern.compile("Your mob died randomly, you are rewarded [0-9]+ pelts?\\.");
    private static final Pattern ANIMAL_KILLED_PATTERN = Pattern.compile("Killing the animal rewarded you [0-9]+ pelts?\\.");
    private static final ResourceLocation TICKER_SYMBOL = new ResourceLocation("skyblockaddons", "tracker.png");

    private static TrackerRarity trackingAnimalRarity = null;
    private static TrackedEntity entityToOutline = null;
    private static int entityNameTagId = -1;

    public TrevorTrapperTracker() {
    }

    /**
     * Draws cell-service-like bars to indicate the proximity to the tracked entity
     * @param scale          the button scale
     * @param buttonLocation the button location in gui location menu
     */
    // TODO: This should not be static after the feature refactor
    public static void drawTrackerLocationIndicator(float scale, ButtonLocation buttonLocation) {
        Feature feature = Feature.TREVOR_THE_TRAPPER_FEATURES;
        if ((feature.isEnabled(FeatureSetting.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR)
                && main.getUtils().isTrackingAnimal()) || buttonLocation != null) {
            float x = main.getConfigValuesManager().getActualX(feature);
            float y = main.getConfigValuesManager().getActualY(feature);

            int height = 9;
            int width = 3 * 11 + 9;

            x = main.getRenderListener().transformX(x, width, scale, false);
            y = main.getRenderListener().transformY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            }

            main.getUtils().enableStandardGLOptions();

            int maxTickers = 4;
            int fullTickers;
            if (buttonLocation != null) {
                fullTickers = 3;
            }
            // Flash indicator on and off when it's very far away
            else if (entityToOutline == null) {
                fullTickers = getFlashingTickers();
            }
            // Progressive distances away from player
            else if (entityToOutline.getDistanceToPlayer() < 16) {
                fullTickers = 4;
            } else if (entityToOutline.getDistanceToPlayer() < 32) {
                fullTickers = 3;
            } else if (entityToOutline.getDistanceToPlayer() < 48) {
                fullTickers = 2;
            } else if (entityToOutline.getDistanceToPlayer() < 64) {
                fullTickers = 1;
            } else {
                fullTickers = getFlashingTickers();
            }
            // Draw the indicator
            for (int tickers = 0; tickers < maxTickers; tickers++) {
                MC.getTextureManager().bindTexture(TICKER_SYMBOL);
                GlStateManager.enableAlpha();
                if (tickers < fullTickers) {
                    DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 0, 0, 9, 9, 18, 9, false);
                } else {
                    DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 9, 0, 9, 9, 18, 9, false);
                }
            }

            main.getUtils().restoreGLOptions();
        }
    }

    private static int getFlashingTickers() {
        if (CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_HUNT") % 2000 < 1000) {
            return 0;
        }
        return 1;
    }

    @SubscribeEvent
    public void onEntityOutline(RenderEntityOutlineEvent e) {
        if (e.getType() == RenderEntityOutlineEvent.Type.NO_XRAY) {
            if (isTrackerConditionsMet() && Feature.TREVOR_THE_TRAPPER_FEATURES.isEnabled(FeatureSetting.TREVOR_HIGHLIGHT_TRACKED_ENTITY)
                    && trackingAnimalRarity != null && entityToOutline != null && entityToOutline.getAnimal() != null
                    && !MC.thePlayer.isPotionActive(Potion.blindness)) {
                e.queueEntityToOutline(entityToOutline.getAnimal(), entityToOutline.getRarity().getColorInt());
            }
        }
    }

    @SubscribeEvent
    public void onEntityEvent(LivingUpdateEvent e) {
        Entity entity = e.entity;
        if (isTrackerConditionsMet()) {
            if (trackingAnimalRarity != null && entity instanceof EntityArmorStand && entity.hasCustomName()) {
                Matcher m = TRACKED_ANIMAL_NAME_PATTERN.matcher(TextUtils.stripColor(entity.getCustomNameTag()));
                if (m.matches()) {
                    TrackerRarity rarity = TrackerRarity.getFromString(m.group("rarity"));
                    if (rarity == null || !rarity.equals(trackingAnimalRarity))
                        return;
                    TrackerType animalType = TrackerType.getFromString(m.group("animal"));
                        if (animalType != null && shouldTrackEntity()) {
                        TrackedEntity trackedEntity = new TrackedEntity((EntityArmorStand) entity, animalType, rarity);
                        trackedEntity.attachAnimal(
                                MC.theWorld.getEntitiesWithinAABB(
                                        EntityAnimal.class,
                                        new AxisAlignedBB(
                                                entity.posX - 2, entity.posY - 2, entity.posZ - 2,
                                                entity.posX + 2, entity.posY + 2, entity.posZ + 2
                                        )
                                )
                        );
                        // TODO can be improved
                        if (trackedEntity.getAnimal() != null) {
                            entityToOutline = trackedEntity;
                            if (trackedEntity.getAnimal().isInvisible() || MC.thePlayer.isPotionActive(Potion.blindness)) {
                                entityNameTagId = -1;
                            } else {
                                entityNameTagId = entity.getEntityId();
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean shouldTrackEntity() {
        return Feature.TREVOR_THE_TRAPPER_FEATURES.isEnabled(FeatureSetting.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR)
                || Feature.TREVOR_THE_TRAPPER_FEATURES.isEnabled(FeatureSetting.TREVOR_HIGHLIGHT_TRACKED_ENTITY)
                || Feature.TREVOR_THE_TRAPPER_FEATURES.isEnabled(FeatureSetting.TREVOR_BETTER_NAMETAG);
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent e) {
        if (main.getUtils().getMap() == Island.THE_FARMING_ISLANDS && e.type != 2) {
            String stripped = TextUtils.stripColor(e.message.getFormattedText());
            // Once the player has started the hunt, start some timers
            Matcher matcher = TREVOR_FIND_ANIMAL_PATTERN.matcher(stripped);
            if (matcher.matches()) {
                // Capitalized rarity from chat p.s. charAt(0) already upper case
                String rarity = matcher.group("rarity");
                rarity = rarity.charAt(0) + rarity.substring(1).toLowerCase(Locale.US);
                // We can use this to verify mod user's animal
                trackingAnimalRarity = TrackerRarity.getFromString(rarity);
                // The player has 10 minutes to kill the animal
                CooldownManager.put("TREVOR_THE_TRAPPER_HUNT", 600000);
                // The player has cooldown before they can receive another animal after killing the current one
                if (main.getElectionData().isPerkActive("Pelt-pocalypse")) {
                    CooldownManager.put("TREVOR_THE_TRAPPER_RETURN", 16000);
                } else {
                    CooldownManager.put("TREVOR_THE_TRAPPER_RETURN", 21000);
                }
            }
            // Once the player has killed the animal, remove the hunt timer
            else if (ANIMAL_DIED_PATTERN.matcher(stripped).matches() || ANIMAL_KILLED_PATTERN.matcher(stripped).matches()) {
                CooldownManager.remove("TREVOR_THE_TRAPPER_HUNT");
                onQuestEnded();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onNameTagRender(Pre<EntityLivingBase> e) {
        Entity entity = e.entity;
        if (isTrackerConditionsMet() && !e.isCanceled() && Feature.TREVOR_THE_TRAPPER_FEATURES.isEnabled(FeatureSetting.TREVOR_SHOW_QUEST_COOLDOWN)
                && CooldownManager.isOnCooldown("TREVOR_THE_TRAPPER_RETURN"))
        {
            String strippedEntityTag = TextUtils.stripColor(entity.getCustomNameTag());
            if (strippedEntityTag.contains("Trevor")) {
                String str = Utils.MESSAGE_PREFIX_SHORT + Translations.getMessage("messages.worldRenderedCooldownTime",
                        CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_RETURN") / 1000);
                DrawUtils.drawTextInWorld(str, e.x, e.y + entity.height + .75, e.z);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent e) {
        if (isTrackerConditionsMet() && e.phase == Phase.START && MC.thePlayer != null) {
            if (trackingAnimalRarity != null && CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_HUNT") == 0) {
                onQuestEnded();
            } else if (entityToOutline != null) {
                entityToOutline.cacheDistanceToPlayer();
            }
        }
    }

    @SubscribeEvent
    public void onRenderNameTag(RenderLivingEvent.Specials.Pre<EntityLivingBase> e) {
        if (Feature.TREVOR_THE_TRAPPER_FEATURES.isDisabled(FeatureSetting.TREVOR_BETTER_NAMETAG) || !isTrackerConditionsMet()) {
            return;
        }

        Entity entityNameTag = MC.theWorld.getEntityByID(entityNameTagId);

        // see SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY
        if (entityNameTag != null && entityNameTag.hasCustomName() && entityNameTag == e.entity) {
            Entity renderViewEntity = MC.getRenderViewEntity();

            double distanceScale = Math.max(1, renderViewEntity.getPositionVector().distanceTo(entityNameTag.getPositionVector()) / 5F);

            GlStateManager.pushMatrix();
            GlStateManager.translate(e.x, e.y + 0.5F + getMobHeight(entityNameTag), e.z);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-MC.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(MC.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-0.025, -0.025, 0.025);

            GlStateManager.scale(distanceScale, distanceScale, distanceScale);

            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.enableTexture2D();
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableAlpha();

            MC.fontRendererObj.drawString(
                    entityNameTag.getCustomNameTag(),
                    -MC.fontRendererObj.getStringWidth(entityNameTag.getCustomNameTag()) / 2F,
                    0,
                    -1,
                    true
            );
            e.setCanceled(true);

            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    private static double getMobHeight(Entity entityNameTag) {
        List<Entity> surroundingMobs = MC.theWorld.getEntitiesWithinAABB(
                EntityArmorStand.class,
                new AxisAlignedBB(
                        entityNameTag.posX - 0.1,
                        entityNameTag.posY - 3,
                        entityNameTag.posZ - 0.1,
                        entityNameTag.posX + 0.1,
                        entityNameTag.posY,
                        entityNameTag.posZ + 0.1
                )
        );
        if (!surroundingMobs.isEmpty()) {
            return surroundingMobs.get(0).height;
        } else {
            return 1.0F; // default
        }
    }

    private void onQuestEnded() {
        entityToOutline = null;
        trackingAnimalRarity = null;
        entityNameTagId = -1;
    }

    private boolean isTrackerConditionsMet() {
        return main.getUtils().isOnSkyblock() && Feature.TREVOR_THE_TRAPPER_FEATURES.isEnabled()
                && main.getUtils().getMap() == Island.THE_FARMING_ISLANDS;
    }

    @Getter
    private enum TrackerType {
        COW("Cow", EntityCow.class),
        PIG("Pig", EntityPig.class),
        SHEEP("Sheep", EntitySheep.class),
        RABBIT("Rabbit", EntityRabbit.class),
        CHICKEN("Chicken", EntityChicken.class),
        HORSE("Horse", EntityHorse.class);

        private final String name;
        private final Class<? extends Entity> clazz;

        TrackerType(String entityName, Class<? extends Entity> entityClass) {
            name = entityName;
            clazz = entityClass;
        }

        public static TrackerType getFromString(String s) {
            for (TrackerType type : values()) {
                if (type.name.equals(s)) {
                    return type;
                }
            }
            return null;
        }
    }

    @Getter
    private enum TrackerRarity {
        TRACKABLE("Trackable", ColorCode.WHITE),
        UNTRACKABLE("Untrackable", ColorCode.DARK_GREEN),
        UNDETECTED("Undetected", ColorCode.DARK_BLUE),
        ENDANGERED("Endangered", ColorCode.DARK_PURPLE),
        ELUSIVE("Elusive", ColorCode.GOLD);

        private final String nameTagName;
        private final ColorCode colorCode;
        private final int colorInt;

        TrackerRarity(String nameTag, ColorCode color) {
            nameTagName = nameTag;
            colorCode = color;
            colorInt = color.getColor();
        }

        public static TrackerRarity getFromString(String s) {
            for (TrackerRarity type : values()) {
                if (type.nameTagName.equals(s)) {
                    return type;
                }
            }
            return null;
        }
    }

    @Getter
    private static class TrackedEntity {
        private final EntityArmorStand armorStand;
        private final TrackerType type;
        private final TrackerRarity rarity;
        private Entity animal;
        private double distanceToPlayer;

        public TrackedEntity(EntityArmorStand theArmorStand, TrackerType trackerType, TrackerRarity trackerRarity) {
            armorStand = theArmorStand;
            type = trackerType;
            rarity = trackerRarity;
            cacheDistanceToPlayer();
        }

        public void attachAnimal(List<Entity> animalList) {
            if (animalList.isEmpty()) {
                animal = null;
            }

            double minDist = Double.MAX_VALUE;
            for (Entity e : animalList) {
                // Minimize the distance between entities on the horizontal plane
                double horizDist = (e.posX - armorStand.posX) * (e.posX - armorStand.posX) + (e.posZ - armorStand.posZ) * (e.posZ - armorStand.posZ);
                // p.s. posY check lower than 3 because of horse. under normal conditions 2 enough
                if (horizDist < minDist && Math.abs(e.posY - armorStand.posY) < 3) {
                    minDist = horizDist;
                    animal = e;
                }
            }
        }

        public void cacheDistanceToPlayer() {
            if (animal != null) {
                distanceToPlayer = MC.thePlayer.getDistanceToEntity(animal);
            } else {
                distanceToPlayer = MC.thePlayer.getDistanceToEntity(armorStand);
            }
        }
    }
}
