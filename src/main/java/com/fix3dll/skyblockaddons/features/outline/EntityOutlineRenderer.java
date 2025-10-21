package com.fix3dll.skyblockaddons.features.outline;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.events.RenderEntityOutlineEvent;
import com.fix3dll.skyblockaddons.mixin.extensions.EntityRenderStateExtension;
import com.mojang.blaze3d.pipeline.RenderTarget;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

import java.util.Map;

import static com.fix3dll.skyblockaddons.events.RenderEntityOutlineEvent.Type.NO_XRAY;
import static com.fix3dll.skyblockaddons.events.RenderEntityOutlineEvent.Type.XRAY;

/**
 * Class to handle all entity outlining, including xray and no-xray rendering
 * Features that include entity outlining should subscribe to the {@link RenderEntityOutlineEvent}.
 * <p>
 * See {@link ItemOutlines} for an example of how to add specific entities based on predicates
 */
public class EntityOutlineRenderer {

    @Getter private static final EntityOutlineRenderer instance = new EntityOutlineRenderer();

    private static final Minecraft MC = Minecraft.getInstance();
    private static final CachedInfo entityRenderCache = new CachedInfo();

    public EntityOutlineRenderer() {
        ClientTickEvents.START_CLIENT_TICK.register(this::onTickStart);
        RenderEntityOutlineEvent.EVENT.register(ItemOutlines::onRenderEntityOutlines);
    }

    /**
     * Colors xray and no-xray entity outlines.
     */
    public static void colorSkyblockEntityOutlines(EntityRenderState entityRenderState) {
        int entityId = ((EntityRenderStateExtension) entityRenderState).sba$getEntityId();
        if (shouldRenderEntityOutlines(entityId)) {
            RenderTarget outlineRenderTarget = MC.levelRenderer.entityOutlineTarget();
            if (outlineRenderTarget == null) return;

            // Render x-ray outlines first, ignoring the depth buffer bit
            for (Map.Entry<Entity, Integer> entityAndColor : entityRenderCache.getXrayCache().object2IntEntrySet()) {
                if (entityAndColor.getKey().getId() == entityId) {
                    entityRenderState.outlineColor = entityAndColor.getValue();
                    return;
                }
            }

            // Xray disabled by re-enabling traditional depth testing
            for (Map.Entry<Entity, Integer> entityAndColor : entityRenderCache.getNoXrayCache().object2IntEntrySet()) {
                // Test if the entity should render, given the player's instantaneous camera position
                if (entityAndColor.getKey().getId() == entityId) {
                    entityRenderState.outlineColor = entityAndColor.getValue();
                    return;
                }
            }
        }
    }

    public static boolean isRenderEntityOutlinesConditionsMet() {
        // Vanilla Conditions
        if (MC.player == null) {
            return false;
        }

        // Skyblock Conditions
        if (!SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            return false;
        }

        // Main toggle for outlines features
        return Feature.ENTITY_OUTLINES.isEnabled();
    }

    /**
     * @return {@code true} if outlines should be rendered
     */
    public static boolean shouldRenderEntityOutlines(int entityId) {
        if (!isRenderEntityOutlinesConditionsMet()) {
            return false;
        }

        // Render x-ray outlines first, ignoring the depth buffer bit
        if (!isXrayCacheEmpty()) {
            // Xray is enabled by disabling depth testing
            for (Map.Entry<Entity, Integer> entityAndColor : entityRenderCache.getXrayCache().object2IntEntrySet()) {
                if (entityAndColor.getKey().getId() == entityId) {
                    return true;
                }
            }
        }

        // Render no-xray outlines second, taking into consideration the depth bit
        if (!isNoXrayCacheEmpty()) {
            // Xray disabled by re-enabling traditional depth testing
            for (Map.Entry<Entity, Integer> entityAndColor : entityRenderCache.getNoXrayCache().object2IntEntrySet()) {
                // Test if the entity should render, given the player's instantaneous camera position
                if (entityAndColor.getKey().getId() == entityId && MC.level != null) {
                    Entity entity = MC.level.getEntity(entityId);
                    // TODO could be used depth? **RenderType
                    if (entity != null && !entity.isInvisible() && MC.player != null && MC.player.hasLineOfSight(entity)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isCacheEmpty() {
        return isXrayCacheEmpty() && isNoXrayCacheEmpty();
    }

    private static boolean isXrayCacheEmpty() {
        return entityRenderCache.xrayCache == null || entityRenderCache.xrayCache.isEmpty();
    }

    private static boolean isNoXrayCacheEmpty() {
        return entityRenderCache.noXrayCache == null || entityRenderCache.noXrayCache.isEmpty();
    }

    private static boolean isNoOutlineCacheEmpty() {
        return entityRenderCache.noOutlineCache == null || entityRenderCache.noOutlineCache.isEmpty();
    }

    private static boolean emptyLastTick = false;

    /**
     * Updates the cache at the start of every minecraft tick to improve efficiency.
     * Identifies and caches all entities in the world that should be outlined.
     * <p>
     * This works since entities are only updated once per tick, so the inclusion or exclusion of an entity
     * to be outlined can be cached each tick with no loss of data
     * @param mc Minecraft instance
     */
    private void onTickStart(Minecraft mc) {
        if (mc.level != null && isRenderEntityOutlinesConditionsMet()) {
            // These events need to be called in this specific order for the xray to have priority over the no xray
            // Get all entities to render xray outlines
            RenderEntityOutlineEvent xrayOutlineEvent = new RenderEntityOutlineEvent(XRAY, null);
            RenderEntityOutlineEvent.EVENT.invoker().onRenderEntityOutline(xrayOutlineEvent);
            // Get all entities to render no xray outlines, using pre-filtered entities (no need to test xray outlined entities)
            RenderEntityOutlineEvent noxrayOutlineEvent = new RenderEntityOutlineEvent(NO_XRAY, xrayOutlineEvent.getEntitiesToChooseFrom());
            RenderEntityOutlineEvent.EVENT.invoker().onRenderEntityOutline(noxrayOutlineEvent);
            // Cache the entities for future use
            entityRenderCache.setXrayCache(xrayOutlineEvent.getEntitiesToOutline());
            entityRenderCache.setNoXrayCache(noxrayOutlineEvent.getEntitiesToOutline());
            entityRenderCache.setNoOutlineCache(noxrayOutlineEvent.getEntitiesToChooseFrom());

            if (isCacheEmpty()) {
                RenderTarget entityOutlineTarget = mc.levelRenderer.entityOutlineTarget();
                if (!emptyLastTick && entityOutlineTarget != null) {
                    entityOutlineTarget.destroyBuffers();
                }
                emptyLastTick = true;
            } else {
                emptyLastTick = false;
            }
        } else if (!emptyLastTick) {
            entityRenderCache.setXrayCache(null);
            entityRenderCache.setNoXrayCache(null);
            entityRenderCache.setNoOutlineCache(null);
            RenderTarget entityOutlineTarget = mc.levelRenderer.entityOutlineTarget();
            if (entityOutlineTarget != null) {
                entityOutlineTarget.destroyBuffers();
            }
            emptyLastTick = true;
        }
    }

    @Getter @Setter
    private static class CachedInfo {
        private Object2IntOpenHashMap<Entity> xrayCache = null;
        private Object2IntOpenHashMap<Entity> noXrayCache = null;
        private ObjectOpenHashSet<Entity> noOutlineCache = null;
    }

}