package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.utils.LocationUtils;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

import java.util.Arrays;
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
            SkyblockOre::isStone,
            () -> !LocationUtils.isOnGlaciteTunnelsLocation() && !LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.BLOCK
    ),
    COBBLESTONE(
            state -> state.getBlock() == Blocks.cobblestone,
            () -> !LocationUtils.isOnGlaciteTunnelsLocation() && !LocationUtils.isOn(Island.MINESHAFT),
            BlockType.BLOCK
    ),
    COAL_ORE(
            state -> state.getBlock() == Blocks.coal_ore,
            () -> LocationUtils.isOn(Island.GOLD_MINE, Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
                    || LocationUtils.isOn("Coal Mine"),
            BlockType.ORE
    ),
    IRON_ORE(
            state -> state.getBlock() == Blocks.iron_ore,
            () -> LocationUtils.isOn(Island.GOLD_MINE, Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    GOLD_ORE(
            state -> state.getBlock() == Blocks.gold_ore,
            () -> LocationUtils.isOn(Island.GOLD_MINE, Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
                    || LocationUtils.isOn("Savanna Woodland"),
            BlockType.ORE
    ),
    LAPIS_ORE(
            state -> state.getBlock() == Blocks.lapis_ore,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    REDSTONE_ORE(
            state -> state.getBlock() == Blocks.redstone_ore || state.getBlock() == Blocks.lit_redstone_ore,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    EMERALD_ORE(
            state -> state.getBlock() == Blocks.emerald_ore,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    DIAMOND_ORE(
            state -> state.getBlock() == Blocks.diamond_ore,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),

    // OBSIDIAN SANCTUARY
    BLOCK_OF_DIAMOND(
            state -> state.getBlock() == Blocks.diamond_block,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS),
            BlockType.ORE
    ),

    // NETHER
    SULPHUR(
            state -> state.getBlock() == Blocks.sponge,
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE),
            BlockType.ORE
    ),
    QUARTZ_ORE(
            state -> state.getBlock() == Blocks.quartz_ore,
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE) || LocationUtils.isOn("Goblin Holdout"),
            BlockType.ORE
    ),
    NETHERRACK(
            state -> state.getBlock() == Blocks.netherrack,
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE),
            BlockType.BLOCK
    ),
    GLOWSTONE(
            state -> state.getBlock() == Blocks.glowstone,
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE),
            BlockType.BLOCK
    ),
    MYCELIUM(
            state -> state.getBlock() == Blocks.mycelium,
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE),
            BlockType.BLOCK
    ),
    RED_SAND(
            SkyblockOre::isRedSand,
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE),
            BlockType.BLOCK
    ),

    // SPIDER'S DEN
    GRAVEL(
            state -> state.getBlock() == Blocks.gravel,
            () -> LocationUtils.isOn(Island.SPIDERS_DEN),
            BlockType.BLOCK
    ),

    // END
    END_STONE(
            state -> state.getBlock() == Blocks.end_stone,
            () -> LocationUtils.isOn(Island.THE_END),
            BlockType.BLOCK
    ),
    OBSIDIAN(
            state -> state.getBlock() == Blocks.obsidian,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.CRYSTAL_HOLLOWS, Island.THE_END),
            BlockType.BLOCK
    ),

    // JERRY'S WORKSHOP
    ICE(
            state -> state.getBlock() == Blocks.ice,
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
            SkyblockOre::isHardstoneGlacite,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.BLOCK
    ),

    // DWARVEN BLOCKS
    PURE_COAL(
            state -> state.getBlock() == Blocks.coal_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    PURE_IRON(
            state -> state.getBlock() == Blocks.iron_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    PURE_GOLD(
            state -> state.getBlock() == Blocks.gold_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.ORE
    ),
    PURE_LAPIS(
            state -> state.getBlock() == Blocks.lapis_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    PURE_REDSTONE(
            state -> state.getBlock() == Blocks.redstone_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    PURE_EMERALD(
            state -> state.getBlock() == Blocks.emerald_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),
    PURE_DIAMOND(
            state -> state.getBlock() == Blocks.diamond_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS),
            BlockType.ORE
    ),

    // GEMSTONES
    RUBY(
            state -> isGemstoneWithColor(state, EnumDyeColor.RED),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    AMBER(
            state -> isGemstoneWithColor(state, EnumDyeColor.ORANGE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    AMETHYST(
            state -> isGemstoneWithColor(state, EnumDyeColor.PURPLE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    JADE(
            state -> isGemstoneWithColor(state, EnumDyeColor.LIME),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    SAPPHIRE(
            state -> isGemstoneWithColor(state, EnumDyeColor.LIGHT_BLUE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    TOPAZ(
            state -> isGemstoneWithColor(state, EnumDyeColor.YELLOW),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    JASPER(
            state -> isGemstoneWithColor(state, EnumDyeColor.MAGENTA),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    OPAL(
            state -> isGemstoneWithColor(state, EnumDyeColor.WHITE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT, Island.CRIMSON_ISLE),
            BlockType.GEMSTONE
    ),
    AQUAMARINE(
            state -> isGemstoneWithColor(state, EnumDyeColor.BLUE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    CITRINE(
            state -> isGemstoneWithColor(state, EnumDyeColor.BROWN),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    ONYX(
            state -> isGemstoneWithColor(state, EnumDyeColor.BLACK),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.GEMSTONE
    ),
    PERIDOT(
            state -> isGemstoneWithColor(state, EnumDyeColor.GREEN),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.GEMSTONE
    ),

    // MITHRIL
    LOW_TIER_MITHRIL(
            SkyblockOre::isLowTierMithril,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    MID_TIER_MITHRIL(
            state -> state.getBlock() == Blocks.prismarine,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    HIGH_TIER_MITHRIL(
            SkyblockOre::isHighTierMithril,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS, Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),

    // TITANIUM
    TITANIUM(
            SkyblockOre::isTitanium,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),

    // GLACIAL
    LOW_TIER_UMBER(
            SkyblockOre::isLowTierUmber,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    HIGH_TIER_UMBER(
            SkyblockOre::isHighTierUmber,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    LOW_TIER_TUNGSTEN(
            SkyblockOre::isLowTierTungsten,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    HIGH_TIER_TUNGSTEN(
            state -> state.getBlock() == Blocks.clay,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    ),
    GLACITE(
            state -> state.getBlock() == Blocks.packed_ice,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT),
            BlockType.DWARVEN_METAL
    );

    private final Function<IBlockState, Boolean> checkBlock; // FIXME minion and private island check
    private final Supplier<Boolean> checkArea;
    @Getter private final BlockType blockType;

    SkyblockOre(Function<IBlockState, Boolean> checkBlock, Supplier<Boolean> checkArea, BlockType blockType) {
        this.checkBlock = checkBlock;
        this.checkArea = checkArea;
        this.blockType = blockType;
    }

    public static SkyblockOre getByStateOrNull(IBlockState state) {
        return Arrays.stream(values())
                .filter(oreBlock -> oreBlock.checkArea.get() && oreBlock.checkBlock.apply(state))
                .findFirst()
                .orElse(null);
    }

    private static boolean isLowTierMithril(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.wool) {
            return state.getValue(BlockColored.COLOR) == EnumDyeColor.GRAY;
        } else if (block == Blocks.stained_hardened_clay) {
            return state.getValue(BlockColored.COLOR) == EnumDyeColor.CYAN;
        }
        return false;
    }

    private static boolean isHighTierMithril(IBlockState state) {
        return state.getBlock() == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.LIGHT_BLUE;
    }

    private static boolean isTitanium(IBlockState state) {
        return state.getBlock() == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH;
    }

    private static boolean isStone(IBlockState state) {
        return state.getBlock() == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE;
    }

    private static boolean isHardStoneHollows(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.wool) {
            switch (state.getValue(BlockColored.COLOR)) {
                case GRAY:
                // Goblin's Queen Den
                case GREEN:
                    return true;
                default:
                    return false;
            }
        } else if (block == Blocks.stained_hardened_clay) {
            switch (state.getValue(BlockColored.COLOR)) {
                case CYAN:
                // Goblin Holdout
                case BROWN:
                case GRAY:
                case BLACK:
                // Jungle
                case LIME:
                case GREEN:
                // Mines of Divan
                case BLUE:
                case RED:
                case SILVER:
                    return true;
                default:
                    return false;
            }
        } else if (block == Blocks.clay) {
            return true;
        } else if (block == Blocks.stonebrick) {
            // Lost Precursor City
            return true;
        } else if (block == Blocks.stone) {
//          switch (state.getValue(BlockStone.VARIANT)) {
//                case STONE:
//                // Precursor Remnants builds
//                case DIORITE:
//                case ANDESITE_SMOOTH:
//                // Mines of Divan
//                case DIORITE_SMOOTH:
//                case GRANITE_SMOOTH:
//                case GRANITE:
//                // Lost Precursor City
//                case ANDESITE:
//                    return true;
//                default:
//                    return false;
//            }
            return true;
        }
        return false;
    }

    private static boolean isHardstoneGlacite(IBlockState state) {
        Block block = state.getBlock();
        return (block == Blocks.stone && state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE) ||
                (block == Blocks.wool && state.getValue(BlockColored.COLOR) == EnumDyeColor.GRAY);
    }

    private static boolean isRedSand(IBlockState state) {
        return state.getBlock() == Blocks.sand && state.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND;
    }

    private static boolean isLowTierUmber(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.hardened_clay ||
                (block == Blocks.stained_hardened_clay && state.getValue(BlockColored.COLOR) == EnumDyeColor.BROWN);
    }

    private static boolean isHighTierUmber(IBlockState state) {
        return state.getBlock() == Blocks.double_stone_slab2 &&
                state.getValue(BlockStoneSlabNew.VARIANT) == BlockStoneSlabNew.EnumType.RED_SANDSTONE;
    }

    private static boolean isLowTierTungsten(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.cobblestone || block == Blocks.stone_stairs ||
                (block == Blocks.stone_slab && state.getValue(BlockStoneSlab.VARIANT) == BlockStoneSlab.EnumType.COBBLESTONE);
    }

    private static boolean isGemstoneWithColor(IBlockState state, EnumDyeColor color) {
        Block block = state.getBlock();
        if (block == Blocks.stained_glass) {
            return color == state.getValue(BlockStainedGlass.COLOR);
        } else if (block == Blocks.stained_glass_pane) {
            return color == state.getValue(BlockStainedGlassPane.COLOR);
        }
        return false;
    }

    public enum BlockType {
        ORE,
        BLOCK,
        DWARVEN_METAL,
        GEMSTONE
    }
}
