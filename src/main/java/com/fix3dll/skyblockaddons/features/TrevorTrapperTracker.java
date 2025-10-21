package com.fix3dll.skyblockaddons.features;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.feature.FeatureSetting;
import com.fix3dll.skyblockaddons.core.render.state.BlitAbsoluteRenderState;
import com.fix3dll.skyblockaddons.events.ClientEvents;
import com.fix3dll.skyblockaddons.events.RenderEntityOutlineEvent;
import com.fix3dll.skyblockaddons.events.RenderEvents;
import com.fix3dll.skyblockaddons.features.cooldowns.CooldownManager;
import com.fix3dll.skyblockaddons.gui.buttons.feature.ButtonLocation;
import com.fix3dll.skyblockaddons.listeners.RenderListener;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.fix3dll.skyblockaddons.utils.TextUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrevorTrapperTracker {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();

    @Getter private static final TrevorTrapperTracker instance = new TrevorTrapperTracker();

    private static final Pattern TRACKED_ANIMAL_NAME_PATTERN = Pattern.compile("\\[Lv[0-9]+](?: (?<mobType>[^a-zA-Z0-9]))? (?<rarity>[a-zA-Z]+) (?<animal>[a-zA-Z]+) .*❤");
    private static final Pattern TREVOR_FIND_ANIMAL_PATTERN = Pattern.compile("\\[NPC] Trevor: You can find your (?<rarity>[A-Z]+) animal near the [a-zA-Z ]+\\.");
    private static final Pattern ANIMAL_DIED_PATTERN = Pattern.compile("Your mob died randomly, you are rewarded [0-9]+ pelts?\\.");
    private static final Pattern ANIMAL_KILLED_PATTERN = Pattern.compile("Killing the animal rewarded you [0-9]+ pelts?\\.");
    private static final ResourceLocation TICKER_SYMBOL = SkyblockAddons.resourceLocation("tracker.png");

    private static TrackerRarity trackingAnimalRarity = null;
    private static TrackedEntity entityToOutline = null;
    @Getter private static int entityNameTagId = -1;

    public TrevorTrapperTracker() {
        ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
        ClientEvents.LIVING_ENTITY_TICK.register(this::onEntityEvent);
        ClientReceiveMessageEvents.GAME_CANCELED.register(this::onChatReceived);
        ClientReceiveMessageEvents.ALLOW_GAME.register(this::onChatReceived);
        RenderEvents.SUBMIT_ENTITY_NAME_TAG.register(this::onSubmitEntityNameTag);
        RenderEntityOutlineEvent.EVENT.register(this::onRenderEntityOutlines);
    }

    /**
     * Draws cell-service-like bars to indicate the proximity to the tracked entity
     * @param scale          the button scale
     * @param buttonLocation the button location in gui location menu
     */
    public static void drawTrackerLocationIndicator(GuiGraphics graphics, float scale, ButtonLocation buttonLocation) {
        Feature feature = Feature.TREVOR_THE_TRAPPER_FEATURES;
        if ((feature.isEnabled(FeatureSetting.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR)
                && main.getUtils().isTrackingAnimal()) || buttonLocation != null) {
            float x = feature.getActualX();
            float y = feature.getActualY();

            int height = 9;
            int width = 3 * 11 + 9;

            x = main.getRenderListener().transformX(x, width, scale, false);
            y = main.getRenderListener().transformY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(graphics, x, x + width, y, y + height, scale);
            }

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
            TextureSetup textureSetup = RenderListener.textureSetup(TICKER_SYMBOL);
            // Draw the indicator
            for (int tickers = 0; tickers < maxTickers; tickers++) {
                float uOffset= tickers < fullTickers ? 0 : 9;
                graphics.guiRenderState.submitGuiElement(
                        new BlitAbsoluteRenderState(RenderPipelines.GUI_TEXTURED, textureSetup, graphics.pose(), x + tickers * 11, y, uOffset, 0, 9, 9, 18, 9, -1, graphics.scissorStack.peek())
                );
            }
        }
    }

    private static int getFlashingTickers() {
        if (CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_HUNT") % 2000 < 1000) {
            return 0;
        }
        return 1;
    }

    public void onEntityEvent(LivingEntity livingEntity) {
        if (!isTrackerConditionsMet()) return;

        if (trackingAnimalRarity != null && livingEntity instanceof ArmorStand armorStand) {
            Component customName = livingEntity.getCustomName();
            if (customName == null) return;

            Matcher m = TRACKED_ANIMAL_NAME_PATTERN.matcher(TextUtils.stripColor(customName.getString()));
            if (m.matches()) {
                TrackerRarity rarity = TrackerRarity.getFromString(m.group("rarity"));
                if (rarity == null || !rarity.equals(trackingAnimalRarity))
                    return;
                TrackerType animalType = TrackerType.getFromString(m.group("animal"));
                if (animalType != null && shouldTrackEntity()) {
                    TrackedEntity trackedEntity = new TrackedEntity(armorStand, animalType, rarity);
                    trackedEntity.attachAnimal(
                            MC.level.getEntitiesOfClass(
                                    Animal.class,
                                    new AABB(
                                            armorStand.getX() - 2, armorStand.getY() - 2, armorStand.getZ() - 2,
                                            armorStand.getX() + 2, armorStand.getY() + 2, armorStand.getZ() + 2
                                    )
                            )
                    );
                    // TODO can be improved
                    if (trackedEntity.getAnimal() != null && MC.player != null) {
                        entityToOutline = trackedEntity;
                        if (trackedEntity.getAnimal().isInvisible() || MC.player.hasEffect(MobEffects.BLINDNESS)) {
                            entityNameTagId = -1;
                        } else {
                            entityNameTagId = armorStand.getId();
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

    private boolean onChatReceived(Component component, boolean actionBar) {
        if (LocationUtils.isOn(Island.THE_FARMING_ISLANDS) && !actionBar) {
            String stripped = TextUtils.stripColor(component.getString());
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

        return true;
    }

    public void onClientTick(Minecraft mc) {
        if (isTrackerConditionsMet()) {
            if (trackingAnimalRarity != null && CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_HUNT") == 0) {
                onQuestEnded();
            } else if (entityToOutline != null) {
                entityToOutline.cacheDistanceToPlayer();
            }
        }
    }

    private void onRenderEntityOutlines(RenderEntityOutlineEvent e) {
        if (e.getType() == RenderEntityOutlineEvent.Type.NO_XRAY) {
            if (isTrackerConditionsMet() && Feature.TREVOR_THE_TRAPPER_FEATURES.isEnabled(FeatureSetting.TREVOR_HIGHLIGHT_TRACKED_ENTITY)
                    && trackingAnimalRarity != null && entityToOutline != null && entityToOutline.getAnimal() != null
                    && !MC.player.hasEffect(MobEffects.BLINDNESS)) {
                e.queueEntityToOutline(entityToOutline.getAnimal(), entityToOutline.getRarity().getColorInt());
            }
        }
    }

    private boolean onSubmitEntityNameTag(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        Vec3 vec3 = state.nameTagAttachment;
        Component nameTag = state.nameTag;
        if (nameTag == null || vec3 == null || !isTrackerConditionsMet()) {
            return false;
        }

        Feature feature = Feature.TREVOR_THE_TRAPPER_FEATURES;

        if (feature.isEnabled(FeatureSetting.TREVOR_BETTER_NAMETAG)) {
            Entity entityNameTag = MC.level.getEntity(entityNameTagId);
            Entity cameraEntity = MC.getCameraEntity();

            if (entityNameTag != null && entityNameTag.hasCustomName() && Objects.equals(nameTag, entityNameTag.getDisplayName()) && cameraEntity != null) {
                float distanceScale = Math.max(1.0F, (float) cameraEntity.position().distanceTo(entityNameTag.position()) / 5F);

                poseStack.pushPose();
                poseStack.scale(distanceScale, distanceScale, distanceScale);
                nodeCollector.submitNameTag(poseStack, vec3, 0, nameTag, true, LightTexture.FULL_BRIGHT, state.distanceToCameraSq, cameraRenderState);
                poseStack.popPose();
                return true;
            }
        }

        if (feature.isEnabled(FeatureSetting.TREVOR_SHOW_QUEST_COOLDOWN) && CooldownManager.isOnCooldown("TREVOR_THE_TRAPPER_RETURN")) {
            String strippedEntityTag = TextUtils.stripColor(nameTag.getString());
            if (strippedEntityTag.contains("Trevor")) {
                String str = Utils.MESSAGE_PREFIX_SHORT + Translations.getMessage(
                        "messages.worldRenderedCooldownTime",
                        CooldownManager.getRemainingCooldown("TREVOR_THE_TRAPPER_RETURN") / 1000
                );
                poseStack.pushPose();
                poseStack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
                nodeCollector.submitNameTag(poseStack, vec3, 0, Component.literal(str), true, LightTexture.FULL_BRIGHT, state.distanceToCameraSq, cameraRenderState);
                poseStack.popPose();
            }
        }

        return false;
    }

    private void onQuestEnded() {
        entityToOutline = null;
        trackingAnimalRarity = null;
        entityNameTagId = -1;
    }

    public static boolean isTrackerConditionsMet() {
        return MC.level != null && MC.player != null && main.getUtils().isOnSkyblock()
                && Feature.TREVOR_THE_TRAPPER_FEATURES.isEnabled()
                && LocationUtils.isOn(Island.THE_FARMING_ISLANDS);
    }

    @Getter
    private enum TrackerType {
        COW("Cow", Cow.class),
        PIG("Pig", Pig.class),
        SHEEP("Sheep", Sheep.class),
        RABBIT("Rabbit", Rabbit.class),
        CHICKEN("Chicken", Chicken.class),
        HORSE("Horse", Horse.class);

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
        private final ArmorStand armorStand;
        private final TrackerType type;
        private final TrackerRarity rarity;
        private Entity animal;
        private double distanceToPlayer;

        public TrackedEntity(ArmorStand theArmorStand, TrackerType trackerType, TrackerRarity trackerRarity) {
            armorStand = theArmorStand;
            type = trackerType;
            rarity = trackerRarity;
            cacheDistanceToPlayer();
        }

        public void attachAnimal(List<Animal> animalList) {
            if (animalList.isEmpty()) {
                animal = null;
            }

            double minDist = Double.MAX_VALUE;
            for (Entity e : animalList) {
                // Minimize the distance between entities on the horizontal plane
                double horizDist = (e.getX() - armorStand.getX()) * (e.getX() - armorStand.getX()) + (e.getZ() - armorStand.getZ()) * (e.getZ() - armorStand.getZ());
                // p.s. posY check lower than 3 because of horse. under normal conditions 2 enough
                if (horizDist < minDist && Math.abs(e.getY() - armorStand.getY()) < 3) {
                    minDist = horizDist;
                    animal = e;
                }
            }
        }

        public void cacheDistanceToPlayer() {
            if (MC.player == null) return;

            if (animal != null) {
                distanceToPlayer = MC.player.distanceTo(animal);
            } else {
                distanceToPlayer = MC.player.distanceTo(armorStand);
            }
        }
    }

}