package com.fix3dll.skyblockaddons.utils;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.utils.data.DataUtils;
import com.mojang.blaze3d.platform.GLX;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a class of utilities for SkyblockAddons developers.
 */
public class DevUtils {
    //TODO: Add an option to log changed action bar messages only to reduce log spam

    private static final Minecraft MC = Minecraft.getInstance();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    public static final Object2ObjectOpenHashMap<String, Class<? extends Entity>> ALL_ENTITIES = new Object2ObjectOpenHashMap<>();

    // If you change this, please change it in the string "commands.usage.sba.help.copyEntity" as well.
    public static final int DEFAULT_ENTITY_COPY_RADIUS = 3;
    private static final List<Class<? extends Entity>> DEFAULT_ENTITY_NAMES = Collections.singletonList(LivingEntity.class);
    private static final boolean DEFAULT_SIDEBAR_FORMATTED = false;

    @Getter @Setter private static boolean loggingActionBarMessages = false;
    @Getter @Setter private static boolean loggingSlayerTracker = false;
    @Getter @Setter private static boolean loggingSkyBlockOre = false;
    private static CopyMode copyMode = CopyMode.ENTITY;
    private static List<Class<? extends Entity>> entityNames = DEFAULT_ENTITY_NAMES;
    private static int entityCopyRadius = DEFAULT_ENTITY_COPY_RADIUS;
    @Setter private static boolean sidebarFormatted = DEFAULT_SIDEBAR_FORMATTED;

    static {
        try {
            List<Class<?>> allEntities = ReflectionUtils.scanPackage(
                    "net.minecraft", Minecraft.class.getClassLoader(), LivingEntity.class
            );
            for (Class<?> entityClass : allEntities) {
                String entityName = entityClass.getSimpleName().replace("Entity", "");
                //noinspection unchecked
                ALL_ENTITIES.put(entityName, (Class<? extends Entity>) entityClass);
                // TODO mapped names for outside of dev env
            }
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
    }

    /**
     * Copies the objective and scores that are being displayed on a scoreboard's sidebar.
     * When copying the sidebar, the control codes (e.g. §a) are removed.
     */
    public static void copyScoreboardSideBar() {
        copyScoreboardSidebar(sidebarFormatted);
    }

    /**
     * Copies the objective and scores that are being displayed on a scoreboard's sidebar.
     * @param stripControlCodes if {@code true}, the control codes will be removed, otherwise they will be copied
     */
    private static void copyScoreboardSidebar(boolean stripControlCodes) {
        if (MC.level == null) return;

        Scoreboard scoreboard = MC.level.getScoreboard();
        if (scoreboard == null) {
            Utils.sendErrorMessage("Nothing is being displayed in the sidebar!");
            return;
        }

        Objective sideBarObjective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sideBarObjective == null) {
            Utils.sendErrorMessage("Nothing is being displayed in the sidebar!");
            return;
        }

        String title = stripControlCodes
                ? TextUtils.stripColor(sideBarObjective.getDisplayName().getString())
                : TextUtils.getFormattedText(sideBarObjective.getDisplayName(), true);
        StringBuilder stringBuilder = new StringBuilder(title).append("\n");

        scoreboard.listPlayerScores(sideBarObjective).stream()
                .filter(scoreboardEntry -> !scoreboardEntry.isHidden())
                .sorted(Gui.SCORE_DISPLAY_ORDER)
                .limit(15)
                .forEach(scoreboardEntry -> {
                    String owner = scoreboardEntry.owner();
                    Component name = scoreboardEntry.ownerName();

                    PlayerTeam team = scoreboard.getPlayersTeam(owner);
                    Component decoratedName = PlayerTeam.formatNameForTeam(team, name);

                    // return fixed name
                    String text = stripControlCodes
                            ? TextUtils.stripColor(decoratedName.getString())
                            : TextUtils.getFormattedText(decoratedName, true);
                    stringBuilder.append(text.replace(owner, ""));

                    if (!stripControlCodes) {
                        stringBuilder.append(" [").append(scoreboardEntry.value()).append("]");
                    }

                    stringBuilder.append("\n");
                });

        copyStringToClipboard(stringBuilder.toString(), ColorCode.GREEN + "Sidebar copied to clipboard!");
    }

    /**
     * Copies the NBT data of entities around the player. The classes of {@link Entity} to include and the radius
     * around the player to copy from can be customized.
     *
     * @param includedEntityClasses the classes of entities that should be included when copying NBT data
     * @param copyRadius copy the NBT data of entities inside this radius(in blocks) around the player
     */
    private static void copyEntityData(List<Class<? extends Entity>> includedEntityClasses, int copyRadius) {
        LocalPlayer player = MC.player;
        ClientLevel world = MC.level;
        if (player == null || world == null) return;

        Iterator<Entity> loadedEntitiesCopyIterator = world.entitiesForRendering().iterator();
        StringBuilder stringBuilder = new StringBuilder();

        // Copy the NBT data from the loaded entities.
        while (loadedEntitiesCopyIterator.hasNext()) {
            Entity entity = loadedEntitiesCopyIterator.next();
            CompoundTag entityData = new CompoundTag();
            boolean isPartOfIncludedClasses = false;

            // Checks to ignore entities if they're irrelevant
            if (entity.distanceTo(player) > copyRadius) {
                continue;
            }

            for (Class<?> entityClass : includedEntityClasses) {
                if (entityClass.isAssignableFrom(entity.getClass())) {
                    isPartOfIncludedClasses = true;
                }
            }

            if (!isPartOfIncludedClasses) {
                continue;
            }

            entity.saveWithoutId(entityData);

            // Add spacing before each new entry.
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append(System.lineSeparator()).append(System.lineSeparator());
            }

            stringBuilder.append("Class: ").append(entity.getClass().getSimpleName()).append(System.lineSeparator());
            if (entity.hasCustomName() || Player.class.isAssignableFrom(entity.getClass())) {
                stringBuilder.append("Name: ").append(entity.getName()).append(System.lineSeparator());
            }

            stringBuilder.append("NBT Data:").append(System.lineSeparator());
            stringBuilder.append(prettyPrintNBT(entityData));
        }

        if (!stringBuilder.isEmpty()) {
            copyStringToClipboard(stringBuilder.toString(), ColorCode.GREEN + "Entity data was copied to clipboard!");
        } else {
            Utils.sendErrorMessage("No entities matching the given parameters were found.");
        }
    }

    public static void setEntityNamesFromString(String includedEntityNames) {
        List<Class<? extends Entity>> entityClasses = getEntityClassListFromString(includedEntityNames);
        if (entityClasses == null || entityClasses.isEmpty()) {
            Utils.sendErrorMessage("The entity class list is not valid or is empty! Falling back to default.");
            resetEntityNamesToDefault();
        } else {
            entityNames = entityClasses;
        }
    }

    public static void setEntityCopyRadius(int copyRadius) {
        if (copyRadius <= 0) {
            Utils.sendErrorMessage("Radius cannot be negative! Falling back to " + DEFAULT_ENTITY_COPY_RADIUS + ".");
            resetEntityCopyRadiusToDefault();
        } else {
            entityCopyRadius = copyRadius;
        }
    }

    public static void resetEntityNamesToDefault() {
        entityNames = DEFAULT_ENTITY_NAMES;
    }

    public static void resetEntityCopyRadiusToDefault() {
        entityCopyRadius = DEFAULT_ENTITY_COPY_RADIUS;
    }

    /**
     * <p>Copies the NBT data of nearby entities using the default settings.</p>
     * <br>
     * <p>Default settings:</p>
     * <p>Included Entity Types: players, armor stands, and mobs</p>
     * <p>Radius: {@link DevUtils#DEFAULT_ENTITY_COPY_RADIUS}</p>
     * <p>Include own NBT data: {@code true}</p>
     */
    public static void copyEntityData() {
        copyEntityData(entityNames, entityCopyRadius);
    }

    /**
     * Compiles a list of entity classes from a string.
     * @param text The string to parse
     * @return The list of entities
     */
    private static List<Class<? extends Entity>> getEntityClassListFromString(String text) {
        Matcher listMatcher = Pattern.compile("(^[A-Z_]+)(?:,[A-Z_]+)*$", Pattern.CASE_INSENSITIVE).matcher(text);

        if (!listMatcher.matches()) {
            return null;
        }

        List<Class<? extends Entity>> entityClasses = new ArrayList<>();
        String[] entityNamesArray = text.split(",");

        for (String entityName : entityNamesArray) {
            if (ALL_ENTITIES.containsKey(entityName)) {
                entityClasses.add(ALL_ENTITIES.get(entityName));
            } else {
                Utils.sendErrorMessage("The entity name \"" + entityName + "\" is invalid. Skipping!");
            }
        }

        return entityClasses;
    }

    public static void copyData() {
        switch (copyMode) {
            case BLOCK:
                copyBlockData();
                break;
            case ENTITY:
                copyEntityData();
                break;
            case SIDEBAR:
                copyScoreboardSideBar();
                break;
            case TAB_LIST:
                copyTabListHeaderAndFooter();
                break;
        }
    }

    /**
     * Copies the provided NBT tag to the clipboard as a pretty-printed string.
     *
     * @param nbtTag the NBT tag to copy
     * @param message the message to show in chat when the NBT tag is copied successfully
     */
    public static void copyNBTTagToClipboard(Tag nbtTag, String message) {
        if (nbtTag == null) {
            Utils.sendErrorMessage("This item has no NBT data!");
            return;
        }
        writeToClipboard(prettyPrintNBT(nbtTag), message);
    }

    /**
     * Copies the header and footer of the tab player list to the clipboard
     * @see net.minecraft.client.gui.components.PlayerTabOverlay
     */
    public static void copyTabListHeaderAndFooter() {
        Component tabHeader = MC.gui.getTabList().header;
        Component tabFooter = MC.gui.getTabList().footer;

        if (tabHeader == null && tabFooter == null) {
            Utils.sendErrorMessage("There is no header or footer!");
            return;
        }

        StringBuilder output = new StringBuilder();

        if (tabHeader != null) {
            output.append("Header:").append("\n");
            output.append(TextUtils.getFormattedText(tabHeader));
            output.append("\n\n");
        }

        if (tabFooter != null) {
            output.append("Footer:").append("\n");
            output.append(TextUtils.getFormattedText(tabFooter));
        }

        copyStringToClipboard(
                output.toString(),
                ColorCode.GREEN + "Successfully copied the tab list header and footer to clipboard!"
        );
    }

    /**
     * Copies OpenGL logs, CPU model and GPU model to clipboard.
     * @see com.mojang.blaze3d.platform.GLX
     */
    public static void copyOpenGLLogs() {
        // TODO complete
        String gpu = GL11.glGetString(GL11.GL_RENDERER);
        String cpu = GLX._getCpuInfo();
        String output = """
                ```
                CPU: %s
                GPU: %s
                ```
                """.formatted(/*logText, */cpu, gpu);
        copyStringToClipboard(
                output,
                ColorCode.GREEN + "Successfully copied the OpenGL logs to clipboard!"
        );
    }

    /**
     * <p>Copies a string to the clipboard</p>
     * <p>Also shows the provided message in chat when successful</p>
     *
     * @param string the string to copy
     * @param successMessage the custom message to show after successful copy
     */
    public static void copyStringToClipboard(String string, String successMessage) {
        writeToClipboard(string, successMessage);
    }

    /**
     * Retrieves the server brand from the Minecraft client.
     * @return the server brand if the client is connected to a server, {@code null} otherwise
     */
    public static String getServerBrand() {
        ClientPacketListener networkHandler = MC.getConnection();

        if (!MC.isLocalServer() && networkHandler != null) {
            return networkHandler.serverBrand();
        } else {
            return null;
        }
    }

    /**
     * Copy the block data with its tile entity data if the block has one.
     */
    public static void copyBlockData() {
        HitResult crosshairTarget = MC.hitResult;

        if (!(crosshairTarget instanceof BlockHitResult blockTarget) || blockTarget.getType() == HitResult.Type.MISS
                || MC.level == null) {
            Utils.sendErrorMessage("You are not looking at a block!");
            return;
        }

        BlockPos blockPos = blockTarget.getBlockPos();

        BlockState blockState = MC.level.getBlockState(blockPos);
        if (!MC.level.isDebug()) {
            blockState = blockState.getBlock().withPropertiesOf(blockState);
        }

        BlockEntity blockEntity = MC.level.getBlockEntity(blockPos);
        CompoundTag nbt = new CompoundTag();
        if (blockEntity != null) {
            nbt.put("blockEntity", blockEntity.saveWithFullMetadata(MC.level.registryAccess()));
        } else {
            nbt.putString("id", BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString());
            nbt.putInt("x", blockPos.getX());
            nbt.putInt("y", blockPos.getY());
            nbt.putInt("z", blockPos.getZ());
        }

        for (Property<?> property : blockState.getProperties()) {
            if (blockState.hasProperty(property)) {
                Optional<?> value = blockState.getOptionalValue(property);
                nbt.putString(property.getName(), value.isPresent() ? value.get().toString() : "");
            }
        }

        writeToClipboard(prettyPrintNBT(nbt), ColorCode.GREEN + "Successfully copied the block data!");
    }

    /**
     * <p>Converts an NBT tag into a pretty-printed string.</p>
     * <p>For constant definitions, see {@link Tag}</p>
     * @param nbt the NBT tag to pretty print
     * @return pretty-printed string of the NBT data
     */
    public static String prettyPrintNBT(Tag nbt) {
        final String INDENT = "    ";

        int tagID = nbt.getId();
        StringBuilder stringBuilder = new StringBuilder();

        // Determine which type of tag it is.
        if (tagID == Tag.TAG_END) {
            stringBuilder.append('}');

        } else if (tagID == Tag.TAG_BYTE_ARRAY || tagID == Tag.TAG_INT_ARRAY) {
            stringBuilder.append('[');
            if (tagID == Tag.TAG_BYTE_ARRAY) {
                ByteArrayTag nbtByteArray = (ByteArrayTag) nbt;
                byte[] bytes = nbtByteArray.getAsByteArray();

                for (int i = 0; i < bytes.length; i++) {
                    stringBuilder.append(bytes[i]);

                    // Don't add a comma after the last element.
                    if (i < (bytes.length - 1)) {
                        stringBuilder.append(", ");
                    }
                }
            } else {
                IntArrayTag nbtIntArray = (IntArrayTag) nbt;
                int[] ints = nbtIntArray.getAsIntArray();

                for (int i = 0; i < ints.length; i++) {
                    stringBuilder.append(ints[i]);

                    // Don't add a comma after the last element.
                    if (i < (ints.length - 1)) {
                        stringBuilder.append(", ");
                    }
                }
            }
            stringBuilder.append(']');

        } else if (tagID == Tag.TAG_LIST) {
            ListTag nbtTagList = (ListTag) nbt;

            stringBuilder.append('[');
            for (int i = 0; i < nbtTagList.size(); i++) {
                Tag currentListElement = nbtTagList.get(i);

                stringBuilder.append(prettyPrintNBT(currentListElement));
//                System.out.printf("tagId: %s -> %s%n", currentListElement.getId(), currentListElement); // TODO test

                // Don't add a comma after the last element.
                if (i < (nbtTagList.size() - 1)) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append(']');

        } else if (tagID == Tag.TAG_COMPOUND) {
            CompoundTag nbtTagCompound = (CompoundTag) nbt;

            stringBuilder.append('{');
            if (!nbtTagCompound.isEmpty()) {
                Iterator<String> iterator = nbtTagCompound.keySet().iterator();

                stringBuilder.append(System.lineSeparator());

                while (iterator.hasNext()) {
                    String key = iterator.next();
                    Tag currentCompoundTagElement = nbtTagCompound.get(key);

                    stringBuilder.append(key).append(": ").append(prettyPrintNBT(currentCompoundTagElement));

                    // backpack_data deprecated?
                    if (key.contains("backpack_data") && currentCompoundTagElement instanceof ByteArrayTag) {
                        try {
                            CompoundTag backpackData = NbtIo.readCompressed(
                                    new ByteArrayInputStream(((ByteArrayTag)currentCompoundTagElement).getAsByteArray()),
                                    NbtAccounter.unlimitedHeap()
                            );

                            stringBuilder.append(",").append(System.lineSeparator());
                            stringBuilder.append(key).append("(decoded): ").append(
                                    prettyPrintNBT(backpackData));
                        } catch (IOException e) {
                            LOGGER.error("Couldn't decompress backpack data into NBT, skipping!", e);
                        }
                    }

                    // Don't add a comma after the last element.
                    if (iterator.hasNext()) {
                        stringBuilder.append(",").append(System.lineSeparator());
                    }
                }

                // Indent all lines
                String indentedString = stringBuilder.toString().replaceAll(System.lineSeparator(), System.lineSeparator() + INDENT);
                stringBuilder = new StringBuilder(indentedString);
            }

            stringBuilder.append(System.lineSeparator()).append('}');
        }
        // This includes the tags: byte, short, int, long, float, double, and string
        else {
            stringBuilder.append(nbt);
        }

        return stringBuilder.toString();
    }

    /**
     * This method reloads all of the mod's settings and resources from the corresponding files.
     */
    public static void reloadAll() {
        reloadConfig();
        reloadResources();
    }

    /**
     * This method reloads all of the mod's settings from the settings file.
     */
    public static void reloadConfig() {
        Utils.sendMessageOrElseLog("Reloading settings...", LOGGER, false);
        main.getConfigValuesManager().loadValues();
        Utils.sendMessageOrElseLog("Settings reloaded.", LOGGER, false);
    }

    /**
     * This method reloads all of the mod's resources from the corresponding files.
     */
    public static void reloadResources() {
        Utils.sendMessageOrElseLog(Translations.getMessage("messages.reloadingResources"), LOGGER, false);
        DataUtils.registerNewRemoteRequests();
        DataUtils.readLocalAndFetchOnline();
        main.getPersistentValuesManager().loadValues();
        main.getScheduler().scheduleAsyncTask(scheduledTask -> {
            if (!scheduledTask.isCanceled() && DataUtils.getExecutionServiceMetrics().getActiveConnectionCount() == 0) {
                DataUtils.onSkyblockJoined();
                PackRepository packs = MC.getResourcePackRepository();
                if (packs.isAvailable(SkyblockAddons.MOD_ID) && packs.getSelectedIds().contains(SkyblockAddons.MOD_ID)) {
                    MC.reloadResourcePacks().whenComplete((unused, throwable) -> {
                        if (throwable == null) {
                            Utils.sendMessageOrElseLog(
                                    Translations.getMessage("messages.resourcesReloaded"), LOGGER, false
                            );
                        } else {
                            Utils.sendMessageOrElseLog(
                                    throwable.getMessage(), LOGGER, false
                            );
                        }
                    });
                } else {
                    Utils.sendMessageOrElseLog(
                            Translations.getMessage("messages.resourcesReloaded"), LOGGER, false
                    );
                }
                scheduledTask.cancel();
            }
        }, 0, 2);
    }

    // Internal methods
    private static void writeToClipboard(String text, String successMessage) {
        try {
            MC.keyboardHandler.setClipboard(text);
            if (successMessage != null) {
                Utils.sendMessage(successMessage);
            }
        } catch (IllegalStateException exception) {
            Utils.sendErrorMessage("Clipboard not available!");
        }
    }

    /**
     * Sets the copy mode to a {@code CopyMode} value.
     * @param copyMode the new copy mode
     */
    public static void setCopyMode(CopyMode copyMode) {
        if (DevUtils.copyMode != copyMode) {
            DevUtils.copyMode = copyMode;
            Utils.sendMessage(ColorCode.GREEN + Translations.getMessage(
                    "messages.copyModeSet",
                    copyMode,
                    SkyblockKeyBinding.DEVELOPER_COPY_NBT.getKeyBinding().getTranslatedKeyMessage().getString()

            ));
        }
    }

    public enum CopyMode {
        BLOCK,
        ENTITY,
        ITEM,
        SIDEBAR,
        TAB_LIST
    }

}