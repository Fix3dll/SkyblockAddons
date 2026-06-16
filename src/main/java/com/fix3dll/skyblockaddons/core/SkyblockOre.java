package com.fix3dll.skyblockaddons.core;


import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.utils.LocationUtils;
import lombok.Getter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Adapted from <a href="https://github.com/hannibal002/SkyHanni/pull/2388">hannibal002/SkyHanni</a>
 * <br>
 * Thanks to class author <a href="https://github.com/ItsEmpa">ItsEmpa</a>
 */
public enum SkyblockOre {
    // VANILLA ORES
    STONE(
            state -> state.is(Blocks.STONE),
            () -> !LocationUtils.isOnGlaciteTunnelsLocation() && !LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.BLOCK
    ),
    COBBLESTONE(
            state -> state.is(Blocks.COBBLESTONE),
            () -> !LocationUtils.isOnGlaciteTunnelsLocation() && !LocationUtils.isOn(Island.MINESHAFT),
            BlockType.BLOCK
    ),
    COAL_ORE(
            state -> state.is(Blocks.COAL_ORE),
            () -> LocationUtils.isOn(Island.GOLD_MINE, Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
                    || LocationUtils.isOn("Coal Mine"),
            BlockType.ORE
    ),
    IRON_ORE(
            state -> state.is(Blocks.IRON_ORE),
            () -> LocationUtils.isOn(Island.GOLD_MINE, Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    GOLD_ORE(
            state -> state.is(Blocks.GOLD_ORE),
            () -> LocationUtils.isOn(Island.GOLD_MINE, Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
                    || LocationUtils.isOn("Savanna Woodland"),
            BlockType.ORE
    ),
    LAPIS_ORE(
            state -> state.is(Blocks.LAPIS_ORE),
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    REDSTONE_ORE(
            state -> state.is(Blocks.REDSTONE_ORE),
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    EMERALD_ORE(
            state -> state.is(Blocks.EMERALD_ORE),
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    DIAMOND_ORE(
            state -> state.is(Blocks.DIAMOND_ORE),
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),

    // OBSIDIAN SANCTUARY
    BLOCK_OF_DIAMOND(
            state -> state.is(Blocks.DIAMOND_BLOCK),
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS),
            BlockType.ORE
    ),

    // NETHER
    SULPHUR(
            state -> state.is(Blocks.SPONGE),
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE),
            BlockType.ORE
    ),
    QUARTZ_ORE(
            state -> state.is(Blocks.NETHER_QUARTZ_ORE),
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE) || LocationUtils.isOn("Goblin Holdout"),
            BlockType.ORE
    ),
    NETHERRACK(
            state -> state.is(Blocks.NETHERRACK),
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE),
            BlockType.BLOCK
    ),
    GLOWSTONE(
            state -> state.is(Blocks.GLOWSTONE),
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE),
            BlockType.BLOCK
    ),
    MYCELIUM(
            state -> state.is(Blocks.MYCELIUM),
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE),
            BlockType.BLOCK
    ),
    RED_SAND(
            state -> state.is(Blocks.RED_SAND),
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE),
            BlockType.BLOCK
    ),

    // SPIDER'S DEN
    GRAVEL(
            state -> state.is(Blocks.GRAVEL),
            () -> LocationUtils.isOn(Island.SPIDERS_DEN),
            BlockType.BLOCK
    ),

    // END
    END_STONE(
            state -> state.is(Blocks.END_STONE),
            () -> LocationUtils.isOn(Island.THE_END),
            BlockType.BLOCK
    ),
    OBSIDIAN(
            state -> state.is(Blocks.OBSIDIAN),
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.CRYSTAL_HOLLOWS, Island.THE_END),
            BlockType.BLOCK
    ),

    // JERRY'S WORKSHOP
    ICE(
            state -> state.is(Blocks.ICE),
            () -> LocationUtils.isOn(Island.JERRYS_WORKSHOP),
            BlockType.BLOCK
    ),

    // HARD STONE
    HARD_STONE_HOLLOWS(
            SkyblockOre::isHardStoneHollows,
            () -> LocationUtils.isOn(Island.CRYSTAL_HOLLOWS),
            BlockType.BLOCK
    ),
    HARD_STONE_GLACIAL(
            // Blocks.stone changed with Blocks.monster_egg on 0.20.6 update
            state -> state.is(Blocks.INFESTED_STONE) || state.is(Blocks.WOOL.lightGray()),
            LocationUtils::isOnGlaciteTunnelsLocation,
            BlockType.BLOCK
    ),
    HARD_STONE_MINESHAFT(
            state -> state.is(Blocks.STONE) || state.is(Blocks.WOOL.lightGray()),
            () -> LocationUtils.isOn(Island.MINESHAFT),
            BlockType.BLOCK
    ),

    // DWARVEN BLOCKS
    PURE_COAL(
            state -> state.is(Blocks.COAL_BLOCK),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    PURE_IRON(
            state -> state.is(Blocks.IRON_BLOCK),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    PURE_GOLD(
            state -> state.is(Blocks.GOLD_BLOCK),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.ORE
    ),
    PURE_LAPIS(
            state -> state.is(Blocks.LAPIS_BLOCK),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    PURE_REDSTONE(
            state -> state.is(Blocks.REDSTONE_BLOCK),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    PURE_EMERALD(
            state -> state.is(Blocks.EMERALD_BLOCK),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    PURE_DIAMOND(
            state -> state.is(Blocks.DIAMOND_BLOCK),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    PURE_QUARTZ(
            state -> state.is(Blocks.QUARTZ_BLOCK),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES),
            BlockType.ORE
    ),

    // GEMSTONES
    RUBY(
            state -> state.is(Blocks.STAINED_GLASS.red()) || state.is(Blocks.STAINED_GLASS_PANE.red()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    AMBER(
            state -> state.is(Blocks.STAINED_GLASS.orange()) || state.is(Blocks.STAINED_GLASS_PANE.orange()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    AMETHYST(
            state -> state.is(Blocks.STAINED_GLASS.purple()) || state.is(Blocks.STAINED_GLASS_PANE.purple()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    JADE(
            state -> state.is(Blocks.STAINED_GLASS.lime()) || state.is(Blocks.STAINED_GLASS_PANE.lime()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    SAPPHIRE(
            state -> state.is(Blocks.STAINED_GLASS.lightBlue()) || state.is(Blocks.STAINED_GLASS_PANE.lightBlue()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    TOPAZ(
            state -> state.is(Blocks.STAINED_GLASS.yellow()) || state.is(Blocks.STAINED_GLASS_PANE.yellow()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    JASPER(
            state -> state.is(Blocks.STAINED_GLASS.magenta()) || state.is(Blocks.STAINED_GLASS_PANE.magenta()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    OPAL(
            state -> state.is(Blocks.STAINED_GLASS.white()) || state.is(Blocks.STAINED_GLASS_PANE.white()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT, Island.CRIMSON_ISLE),
            BlockType.GEMSTONE
    ),
    AQUAMARINE(
            state -> state.is(Blocks.STAINED_GLASS.blue()) || state.is(Blocks.STAINED_GLASS_PANE.blue()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    CITRINE(
            state -> state.is(Blocks.STAINED_GLASS.brown()) || state.is(Blocks.STAINED_GLASS_PANE.brown()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    ONYX(
            state -> state.is(Blocks.STAINED_GLASS.black()) || state.is(Blocks.STAINED_GLASS_PANE.black()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    PERIDOT(
            state -> state.is(Blocks.STAINED_GLASS.green()) || state.is(Blocks.STAINED_GLASS_PANE.green()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.GEMSTONE
    ),

    // MITHRIL
    LOW_TIER_MITHRIL(
            state -> state.is(Blocks.WOOL.gray()) || state.is(Blocks.DYED_TERRACOTTA.cyan()),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    MID_TIER_MITHRIL(
            state -> state.is(Blocks.PRISMARINE) || state.is(Blocks.PRISMARINE_BRICKS) || state.is(Blocks.DARK_PRISMARINE),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    HIGH_TIER_MITHRIL(
            state -> state.is(Blocks.WOOL.lightBlue()),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),

    // TITANIUM
    TITANIUM(
            state -> state.is(Blocks.POLISHED_DIORITE),
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),

    // GLACIAL
    LOW_TIER_UMBER(
            state -> state.is(Blocks.TERRACOTTA) || state.is(Blocks.DYED_TERRACOTTA.brown()),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    HIGH_TIER_UMBER(
            state -> state.is(Blocks.SMOOTH_RED_SANDSTONE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    LOW_TIER_TUNGSTEN_GLACITE(
            // Blocks.cobblestone changed with Blocks.monster_egg on 0.20.6 update
            state -> state.is(Blocks.INFESTED_COBBLESTONE),
            LocationUtils::isOnGlaciteTunnelsLocation,
            BlockType.DWARVEN_METAL
    ),
    LOW_TIER_TUNGSTEN_MINESHAFT(
            SkyblockOre::isLowTierTungstenMineshaft,
            () -> LocationUtils.isOn(Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    HIGH_TIER_TUNGSTEN(
            state -> state.is(Blocks.CLAY),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    GLACITE(
            state -> state.is(Blocks.PACKED_ICE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    );

    private final Function<BlockState, Boolean> checkBlock; // FIXME minion and private island check
    private final Supplier<Boolean> checkArea;
    @Getter
    private final BlockType blockType;

    SkyblockOre(Function<BlockState, Boolean> checkBlock, Supplier<Boolean> checkArea, BlockType blockType) {
        this.checkBlock = checkBlock;
        this.checkArea = checkArea;
        this.blockType = blockType;
    }

    private static final Set<Block> HOLLOWS_HARD_STONE_BLOCKS = Set.of(
            // wool
            Blocks.WOOL.gray(), Blocks.WOOL.green(),
            // terracotta
            Blocks.DYED_TERRACOTTA.cyan(), Blocks.DYED_TERRACOTTA.brown(), Blocks.DYED_TERRACOTTA.gray(),
            Blocks.DYED_TERRACOTTA.lime(), Blocks.DYED_TERRACOTTA.green(), Blocks.DYED_TERRACOTTA.blue(),
            Blocks.DYED_TERRACOTTA.red(), Blocks.DYED_TERRACOTTA.lightGray(),
            // plain
            Blocks.CLAY, Blocks.STONE_BRICKS,
            // stone family
            Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE,
            Blocks.POLISHED_GRANITE, Blocks.POLISHED_DIORITE, Blocks.POLISHED_ANDESITE
    );

    public static SkyblockOre getByStateOrNull(BlockState state) {
        for (SkyblockOre ore : values()) {
            if (ore.checkArea.get() && ore.checkBlock.apply(state)) {
                return ore;
            }
        }
        return null;
    }

    private static boolean isHardStoneHollows(BlockState state) {
        return HOLLOWS_HARD_STONE_BLOCKS.contains(state.getBlock());
    }

    private static boolean isLowTierTungstenMineshaft(BlockState state) {
        // TODO(TEST): Are there any variations that include cobblestone block other than the TUNG mineshaft variant?
        if (state.is(Blocks.COBBLESTONE)) {
            return true;
        } else if (state.is(Blocks.COBBLESTONE_STAIRS)) {
            return SkyblockAddons.getInstance().getUtils().getMineshaftID().startsWith("TUNG");
        } else if (state.is(Blocks.COBBLESTONE_SLAB)) {
            return SkyblockAddons.getInstance().getUtils().getMineshaftID().startsWith("TUNG");
        } else {
            return false;
        }
    }

    public enum BlockType {
        ORE,
        BLOCK,
        DWARVEN_METAL,
        GEMSTONE
    }
}