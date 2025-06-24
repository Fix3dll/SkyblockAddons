package com.fix3dll.skyblockaddons.features;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EndstoneProtectorManager {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    // This is the original spawn.
    private static final BlockPos ORIGIN = new BlockPos(-689, 5, -273);

    @Getter private static boolean canDetectSkull = false;
    @Getter private static Stage minibossStage = null;
    @Getter private static int zealotCount = 0;

    private static long lastWaveStart = -1;

    public static void checkGolemStatus() {
        ClientLevel level = MC.level;
        if (level== null || !LocationUtils.isOn(Island.THE_END) || Feature.ENDSTONE_PROTECTOR_DISPLAY.isDisabled()) {
            canDetectSkull = false;
            return;
        }

        LevelChunk chunk = level.getChunkSource().getChunkNow(ORIGIN.getX() >> 4, ORIGIN.getZ() >> 4);
        if (chunk == null) {
            canDetectSkull = false;
            return;
        }

        Stage newStage = Stage.detectStage();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof IronGolem) {
                newStage = Stage.GOLEM_ALIVE;
                break;
            }
        }

        canDetectSkull = true;
        if (minibossStage != newStage) {
            int timeTaken = (int) (System.currentTimeMillis() - lastWaveStart);
            int totalSeconds = timeTaken / 1000;
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            String previousStage = minibossStage == null ? "null" : minibossStage.name();
            String zealotsKilled = minibossStage == null ? "N/A"  : String.valueOf(zealotCount);

            LOGGER.info(
                    "Endstone protector stage updated from {} to {}. Your zealot kill count was {}. This took {}m {}s.",
                    previousStage, newStage.name(), zealotsKilled, minutes, seconds
            );

            if (minibossStage == Stage.GOLEM_ALIVE && newStage == Stage.NO_HEAD) {
                zealotCount = 0;
            }

            minibossStage = newStage;
            lastWaveStart = System.currentTimeMillis();
        }
    }

    public static void onKill() {
        zealotCount++;
    }

    public static void reset() {
        minibossStage = null;
        zealotCount = 0;
        canDetectSkull = false;
    }

    public enum Stage {
        NO_HEAD(-1),
        STAGE_1(0),
        STAGE_2(1),
        STAGE_3(2),
        STAGE_4(3),
        STAGE_5(4),
        GOLEM_ALIVE(-1);

        private final int blocksUp;

        Stage(int blocksUp) {
            this.blocksUp = blocksUp;
        }

        private static Stage lastStage = null;
        private static BlockPos lastPos = null;

        private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder().setNameFormat(SkyblockAddons.METADATA.getName() + " - Endstone Protector #%d").build()
        );

        public static Stage detectStage() {
            EXECUTOR.submit(() -> {
                try {
                    ClientLevel level = Minecraft.getInstance().level;
                    if (level == null) return;

                    if (lastStage != null && lastPos != null && level.getBlockState(lastPos).is(Blocks.PLAYER_HEAD)) {
                        return;
                    }

                    for (Stage stage : values()) {
                        if (stage.blocksUp != -1) {
                            // These 4 coordinates are the bounds of the dragon's nest.
                            for (int x = -749; x < -602; x++) {
                                for (int z = -353; z < -202; z++) {
                                    BlockPos blockPos = new BlockPos(x, 5 + stage.blocksUp, z);
                                    if (level.getBlockState(blockPos).is(Blocks.PLAYER_HEAD)) {
                                        lastStage = stage;
                                        lastPos = blockPos;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    lastStage = Stage.NO_HEAD;
                    lastPos = null;
                } catch (Throwable ex) {
                    LOGGER.catching(ex); // It's fine I guess, just try checking next tick...
                }
            });

            return lastStage;
        }
    }
}