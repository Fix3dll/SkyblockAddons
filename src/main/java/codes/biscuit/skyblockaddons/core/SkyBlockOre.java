package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.utils.LocationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.BlockStone;
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
public enum SkyBlockOre {
    // MITHRIL
    LOW_TIER_MITHRIL(
            SkyBlockOre::isLowTierMithril,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.MINESHAFT)
    ),
    MID_TIER_MITHRIL(
            state -> state.getBlock() == Blocks.prismarine,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS, Island.MINESHAFT)
    ),
    HIGH_TIER_MITHRIL(
            SkyBlockOre::isHighTierMithril,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS, Island.MINESHAFT)
    ),

    // TITANIUM
    TITANIUM(
            SkyBlockOre::isTitanium,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.MINESHAFT)
    ),

    // VANILLA ORES
    STONE(
            SkyBlockOre::isStone,
            () -> !LocationUtils.isOnGlaciteTunnelsLocation() && !LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT)
    ),
    COBBLESTONE(
            state -> state.getBlock() == Blocks.cobblestone,
            () -> !LocationUtils.isOnGlaciteTunnelsLocation() && !LocationUtils.isOn(Island.MINESHAFT)
    ),
    COAL_ORE(
            state -> state.getBlock() == Blocks.coal_ore,
            () -> LocationUtils.isOn(Island.GOLD_MINE, Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
                    || LocationUtils.isOn("Coal Mine")
    ),
    IRON_ORE(
            state -> state.getBlock() == Blocks.iron_ore,
            () -> LocationUtils.isOn(Island.GOLD_MINE, Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),
    GOLD_ORE(
            state -> state.getBlock() == Blocks.gold_ore,
            () -> LocationUtils.isOn(Island.GOLD_MINE, Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),
    LAPIS_ORE(
            state -> state.getBlock() == Blocks.lapis_ore,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),
    REDSTONE_ORE(
            state -> state.getBlock() == Blocks.redstone_ore || state.getBlock() == Blocks.lit_redstone_ore,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),
    EMERALD_ORE(
            state -> state.getBlock() == Blocks.emerald_ore,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),
    DIAMOND_ORE(
            state -> state.getBlock() == Blocks.diamond_ore,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),

    // NETHER
    NETHERRACK(
            state -> state.getBlock() == Blocks.netherrack,
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE)
    ),
    QUARTZ_ORE(
            state -> state.getBlock() == Blocks.quartz_ore,
            () -> LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.CRIMSON_ISLE)
    ),
    GLOWSTONE(
            state -> state.getBlock() == Blocks.glowstone,
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE)
    ),
    MYCELIUM(
            state -> state.getBlock() == Blocks.mycelium,
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE)
    ),
    RED_SAND(
            SkyBlockOre::isRedSand,
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE)
    ),
    SULPHUR(
            state -> state.getBlock() == Blocks.sponge,
            () -> LocationUtils.isOn(Island.CRIMSON_ISLE)
    ),

    // SPIDER'S DEN
    GRAVEL(
            state -> state.getBlock() == Blocks.gravel,
            () -> LocationUtils.isOn(Island.SPIDERS_DEN)
    ),

    // END
    END_STONE(
            state -> state.getBlock() == Blocks.end_stone,
            () -> LocationUtils.isOn(Island.THE_END)
    ),
    OBSIDIAN(
            state -> state.getBlock() == Blocks.obsidian,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.CRYSTAL_HOLLOWS, Island.THE_END)
    ),

    // HARD STONE
    HARD_STONE_HOLLOWS(
            SkyBlockOre::isHardStoneHollows,
            () -> LocationUtils.isOn(Island.CRYSTAL_HOLLOWS)
    ),
    HARD_STONE_GLACIAL(
            SkyBlockOre::isHardstoneGlacite,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT)
    ),

    // DWARVEN BLOCKS
    PURE_COAL(
            state -> state.getBlock() == Blocks.coal_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),
    PURE_IRON(
            state -> state.getBlock() == Blocks.iron_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),
    PURE_GOLD(
            state -> state.getBlock() == Blocks.gold_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),
    PURE_LAPIS(
            state -> state.getBlock() == Blocks.lapis_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),
    PURE_REDSTONE(
            state -> state.getBlock() == Blocks.redstone_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),
    PURE_EMERALD(
            state -> state.getBlock() == Blocks.emerald_block,
            () -> LocationUtils.isOn(Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),
    PURE_DIAMOND(
            state -> state.getBlock() == Blocks.diamond_block,
            () -> LocationUtils.isOn(Island.DEEP_CAVERNS, Island.DWARVEN_MINES, Island.CRYSTAL_HOLLOWS)
    ),

    // GEMSTONES
    RUBY(
            state -> isGemstoneWithColor(state, EnumDyeColor.RED),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT)
    ),
    AMBER(
            state -> isGemstoneWithColor(state, EnumDyeColor.ORANGE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT)
    ),
    AMETHYST(
            state -> isGemstoneWithColor(state, EnumDyeColor.PURPLE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT)
    ),
    JADE(
            state -> isGemstoneWithColor(state, EnumDyeColor.LIME),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT)
    ),
    SAPPHIRE(
            state -> isGemstoneWithColor(state, EnumDyeColor.LIGHT_BLUE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT)
    ),
    TOPAZ(
            state -> isGemstoneWithColor(state, EnumDyeColor.YELLOW),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT)
    ),
    JASPER(
            state -> isGemstoneWithColor(state, EnumDyeColor.MAGENTA),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.CRYSTAL_HOLLOWS, Island.MINESHAFT)
    ),
    OPAL(
            state -> isGemstoneWithColor(state, EnumDyeColor.WHITE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT, Island.CRIMSON_ISLE)
    ),
    AQUAMARINE(
            state -> isGemstoneWithColor(state, EnumDyeColor.BLUE),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT)
    ),
    CITRINE(
            state -> isGemstoneWithColor(state, EnumDyeColor.BROWN),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT)
    ),
    ONYX(
            state -> isGemstoneWithColor(state, EnumDyeColor.BLACK),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT)
    ),
    PERIDOT(
            state -> isGemstoneWithColor(state, EnumDyeColor.GREEN),
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT)
    ),

    // GLACIAL
    LOW_TIER_UMBER(
            SkyBlockOre::isLowTierUmber,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT)
    ),
    HIGH_TIER_UMBER(
            SkyBlockOre::isHighTierUmber,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT)
    ),
    LOW_TIER_TUNGSTEN(
            state -> state.getBlock() == Blocks.cobblestone,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT)
    ),
    HIGH_TIER_TUNGSTEN(
            state -> state.getBlock() == Blocks.clay,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT)
    ),
    GLACITE(
            state -> state.getBlock() == Blocks.packed_ice,
            () -> LocationUtils.isOnGlaciteTunnelsLocation() || LocationUtils.isOn(Island.MINESHAFT)
    );

    private final Function<IBlockState, Boolean> checkBlock; // FIXME minion and private island check
    private final Supplier<Boolean> checkArea;

    SkyBlockOre(Function<IBlockState, Boolean> checkBlock, Supplier<Boolean> checkArea) {
        this.checkBlock = checkBlock;
        this.checkArea = checkArea;
    }

    public static SkyBlockOre getByStateOrNull(IBlockState state) {
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

    private static boolean isGemstoneWithColor(IBlockState state, EnumDyeColor color) {
        Block block = state.getBlock();
        if (block == Blocks.stained_glass) {
            return color == state.getValue(BlockStainedGlass.COLOR);
        } else if (block == Blocks.stained_glass_pane) {
            return color == state.getValue(BlockStainedGlassPane.COLOR);
        }
        return false;
    }
}
