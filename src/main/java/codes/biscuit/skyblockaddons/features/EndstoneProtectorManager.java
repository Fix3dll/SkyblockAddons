package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.core.Island;
import codes.biscuit.skyblockaddons.utils.LocationUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EndstoneProtectorManager {

    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    @Getter private static boolean canDetectSkull = false;
    @Getter private static Stage minibossStage = null;
    @Getter private static int zealotCount = 0;

    private static long lastWaveStart = -1;

    public static void checkGolemStatus() {
        if (MC.theWorld != null && LocationUtils.isOn(Island.THE_END) && Feature.ENDSTONE_PROTECTOR_DISPLAY.isEnabled()) {
            World world = MC.theWorld;

            Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(-689, 5, -273)); // This is the original spawn.
            if (chunk == null || !chunk.isLoaded()) {
                canDetectSkull = false;
                return;
            }

            Stage newStage = Stage.detectStage();
            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof EntityIronGolem) {
                    newStage = Stage.GOLEM_ALIVE;
                    break;
                }
            }

            canDetectSkull = true;
            if (minibossStage != newStage) {
                int timeTaken = (int) (System.currentTimeMillis() - lastWaveStart);
                String previousStage = (minibossStage == null ? "null" : minibossStage.name());

                String zealotsKilled = "N/A";
                if (minibossStage != null) {
                    zealotsKilled = String.valueOf(zealotCount);
                }

                int totalSeconds = timeTaken / 1000;
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;

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
        } else {
            canDetectSkull = false;
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
                new ThreadFactoryBuilder().setNameFormat(SkyblockAddons.MOD_NAME + " - Endstone Protector #%d").build()
        );

        public static Stage detectStage() {
            EXECUTOR.submit(() -> {
                try {
                    World world = Minecraft.getMinecraft().theWorld;

                    if (lastStage != null && lastPos != null) {
                        if (Blocks.skull.equals(world.getBlockState(lastPos).getBlock())) {
                            return;
                        }
                    }

                    for (Stage stage : values()) {
                        if (stage.blocksUp != -1) {
                            // These 4 coordinates are the bounds of the dragon's nest.
                            for (int x = -749; x < -602; x++) {
                                for (int z = -353; z < -202; z++) {
                                    BlockPos blockPos = new BlockPos(x, 5 + stage.blocksUp, z);
                                    if (Blocks.skull.equals(world.getBlockState(blockPos).getBlock())) {
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
                    LOGGER.catching(ex);
                    // It's fine I guess, just try checking next tick...
                }
            });

            return lastStage;
        }
    }
}