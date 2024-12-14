package codes.biscuit.skyblockaddons.commands;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerBoss;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerDrop;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.misc.SkyblockKeyBinding;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DevUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import com.google.common.base.CaseFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.command.*;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static codes.biscuit.skyblockaddons.core.Translations.getMessage;

//TODO: Clean this up a bit, make it less complex to add stuff

/**
 * This is the main command of SkyblockAddons. It is used to open the menu, change settings, and for developer mode functions.
 */
public class SkyblockAddonsCommand extends CommandBase {

    private static final String HEADER = "§7§m----------------§7[ §b§lSkyblockAddons §7]§7§m----------------";
    private static final String FOOTER = "§7§m-----------------------------------------------------";
    private static final String[] SUBCOMMANDS = {"help", "edit", "folder", "resetZealotCounter", "set", "slayer",
            "version", "reload", "reloadConfig", "reloadRes", "dev", "brand", "copyBlock", "copyEntity", "copySidebar",
            "copyTabList", "pd", "toggleActionBarLogging", "toggleSlayerTrackerLogging", "copyOpenGL",
            "toggleSkyBlockOreLogging"
    };

    private final SkyblockAddons main = SkyblockAddons.getInstance();

    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "skyblockaddons";
    }

    /**
     * Returns the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 0;
    }

    /**
     * Returns the aliases of this command
     */
    public List<String> getCommandAliases() {
        return Collections.singletonList("sba");
    }

    /**
     * Gets the usage string for the command. If developer mode is enabled, the developer mode usage string is added to
     * the main usage string.
     */
    public String getCommandUsage(ICommandSender sender) {
        StringBuilder builder = new StringBuilder(HEADER);
        for (Commands command : Commands.values()) {
            if (command.devMode && Feature.DEVELOPER_MODE.isDisabled()) continue;
            builder.append("\n").append(command.createMenuDescriptionLine());
        }
        builder.append("\n").append(FOOTER);

        return builder.toString();
    }

    /**
     * Returns the detailed usage for the sub-command provided with the header and footer included.
     *
     * @param subCommand the sub-command to fetch the usage of
     * @return the usage of the given sub-command
     * @throws IllegalArgumentException if there is no sub-command with the given name or the sub-command doesn't have a
     *                                  corresponding {@code SubCommandUsage}
     * @throws NullPointerException if {@code subCommand} is {@code null}
     */
    public String getSubCommandUsage(String subCommand) {
        for (String validSubCommand : SUBCOMMANDS) {
            if (subCommand.equalsIgnoreCase(validSubCommand)) {
                subCommand = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, validSubCommand);
            }
        }

        return HEADER + "\n" + Commands.valueOf(subCommand) + "\n" + FOOTER;
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        switch (args.length) {
            case 1:
                return getSubCommandTabCompletionOptions(args);
            case 2:
                if (args[0].equalsIgnoreCase("help")) {
                    return getSubCommandTabCompletionOptions(args);

                } else if (args[0].equalsIgnoreCase("set")) {
                    return getListOfStringsMatchingLastWord(args, "total", "zealots", "eyes");

                } else if (args[0].equalsIgnoreCase("slayer")) {
                    String[] slayers = new String[SlayerBoss.values().length];
                    for (int i = 0; i < SlayerBoss.values().length; i++) {
                        slayers[i] = SlayerBoss.values()[i].getMobType().toLowerCase(Locale.US);
                    }
                    return getListOfStringsMatchingLastWord(args, slayers);

                } else if (Feature.DEVELOPER_MODE.isEnabled()) {
                    if (args[0].equalsIgnoreCase("copyEntity")) {
                        return getListOfStringsMatchingLastWord(args, DevUtils.ALL_ENTITY_NAMES);
                    } else if (args[0].equalsIgnoreCase("copySidebar")) {
                        return getListOfStringsMatchingLastWord(args, "formatted");
                    }
                }
                break;
            case 3:
                if (args[0].equals("slayer")) {
                    SlayerBoss slayerBoss = SlayerBoss.getFromMobType(args[1]);

                    if (slayerBoss != null) {
                        String[] drops = new String[slayerBoss.getDrops().size() + 2];
                        drops[0] = "reset_all";
                        drops[1] = "kills";
                        int i = 2;
                        for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
                            drops[i] = slayerDrop.name().toLowerCase(Locale.US);
                            i++;
                        }
                        return getListOfStringsMatchingLastWord(args, drops);
                    }
                }
                break;
        }

        return null;
    }

    /**
     * Callback when the command is invoked
     */
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            if (args.length == 0) {
                // If there's no arguments given, open the main GUI
                main.getUtils().setFadingIn(true);
                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
                return;
            }

            if (args[0].equalsIgnoreCase("help")) {
                if (args.length == 2) {
                    try {
                        main.getUtils().sendMessage(getSubCommandUsage(args[1]), false);
                    } catch (IllegalArgumentException e) {
                        throw new CommandException(getMessage("commands.errors.wrongUsage.subCommandNotFound", args[1]));
                    }
                } else {
                    main.getUtils().sendMessage(getCommandUsage(sender), false);
                }
            } else if (args[0].equalsIgnoreCase("edit")) {
                main.getUtils().setFadingIn(false);
                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.EDIT_LOCATIONS, 0, null);

            } else if (args[0].equalsIgnoreCase("dev") || args[0].equalsIgnoreCase("nbt")) {
                SkyblockKeyBinding devModeKeyBinding = main.getDeveloperCopyNBTKey();
                Feature.DEVELOPER_MODE.setEnabled(!Feature.DEVELOPER_MODE.isEnabled());

                if (Feature.DEVELOPER_MODE.isEnabled()) {
                    main.getUtils().sendMessage(ColorCode.GREEN + getMessage("commands.responses.sba.dev.enabled",
                            GameSettings.getKeyDisplayString(devModeKeyBinding.getKeyCode())));
                } else {
                    main.getUtils().sendMessage(ColorCode.RED + getMessage("commands.responses.sba.dev.disabled"));
                }
            } else if (args[0].equalsIgnoreCase("resetZealotCounter")) {
                main.getPersistentValuesManager().resetZealotCounter();
                main.getUtils().sendMessage(ColorCode.GREEN + getMessage("commands.responses.sba.resetZealotCounter.resetSuccess"));
            } else if (args[0].equalsIgnoreCase("set")) {
                int number;

                if (args.length >= 3) {
                    number = parseInt(args[2]);
                } else {
                    throw new WrongUsageException(getMessage("commands.errors.wrongUsage.generic"));
                }

                if (args[1].equalsIgnoreCase("totalZealots") || args[1].equalsIgnoreCase("total")) {
                    main.getPersistentValuesManager().getPersistentValues().setTotalKills(number);
                    main.getPersistentValuesManager().saveValues();
                    main.getUtils().sendMessage(getMessage("commands.responses.sba.set.zealotCounter.totalZealotsSet",
                            Integer.toString(number)));
                } else if (args[1].equalsIgnoreCase("zealots")) {
                    main.getPersistentValuesManager().getPersistentValues().setKills(number);
                    main.getPersistentValuesManager().saveValues();
                    main.getUtils().sendMessage(getMessage("commands.responses.sba.set.zealotCounter.zealotsSet",
                            Integer.toString(number)));
                } else if (args[1].equalsIgnoreCase("eyes")) {
                    main.getPersistentValuesManager().getPersistentValues().setSummoningEyeCount(number);
                    main.getPersistentValuesManager().saveValues();
                    main.getUtils().sendMessage(getMessage("commands.responses.sba.set.zealotCounter.eyesSet",
                            Integer.toString(number)));
                } else {
                    throw new WrongUsageException(getMessage("sba.set.zealotCounter.wrongUsage",
                            "'zealots', 'totalZealots/total', 'eyes'"));
                }
            } else if (args[0].equalsIgnoreCase("folder")) {
                try {
                    Desktop.getDesktop().open(main.getUtils().getSBAFolder());
                } catch (IOException e) {
                    throw new CommandException(getMessage("commands.responses.sba.folder.error"), e.getMessage());
                }
            } else if (args[0].equalsIgnoreCase("warp")) {
                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.WARP);
            } else if (args[0].equalsIgnoreCase("slayer")) {
                if (args.length == 1) {
                    StringBuilder bosses = new StringBuilder();
                    for (int i = 0; i < SlayerBoss.values().length; i++) {
                        SlayerBoss slayerBoss = SlayerBoss.values()[i];
                        bosses.append("'").append(slayerBoss.getMobType().toLowerCase(Locale.US)).append("'");
                        if (i + 1 < SlayerBoss.values().length) {
                            bosses.append(", ");
                        }
                    }
                    throw new WrongUsageException(getMessage("commands.responses.sba.slayer.bossRequired", bosses.toString()));
                } else if (args.length == 2) {
                    throw new WrongUsageException(getMessage("commands.responses.sba.slayer.statRequired"));
                } else if (args.length == 3) {
                    if (args[2].equalsIgnoreCase("reset_all")) {
                        SlayerTracker.getInstance().resetAllStats(args[1]);
                    } else throw new WrongUsageException(getMessage("commands.responses.sba.slayer.numberRequired"));
                } else if (args.length == 4) {
                    try {
                        SlayerTracker.getInstance().setStatManually(args);
                    } catch (NumberFormatException e) {
                        throw new NumberInvalidException("commands.generic.num.invalid", args[3]);
                    } catch (IllegalArgumentException e) {
                        throw new WrongUsageException(e.getMessage());
                    }
                }
            } else if (args[0].equalsIgnoreCase("version")) {
                String versionString = getMessage("messages.version") + " v" + SkyblockAddons.VERSION;
                ChatComponentText versionChatComponent = new ChatComponentText(versionString);
                ChatStyle versionChatStyle = new ChatStyle().setColor(EnumChatFormatting.AQUA)
                        .setChatHoverEvent(
                                new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        new ChatComponentText(
                                                getMessage("commands.responses.sba.version.hoverText")
                                        ).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE))
                                )
                        ).setChatClickEvent(
                                new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, SkyblockAddons.VERSION)
                        );
                versionChatComponent.setChatStyle(versionChatStyle);

                /*
                 Include MAJOR.MINOR.PATCH-pre-release in the chat message and add build number if it's defined when
                 the user chooses to copy for diagnostic purposes.
                 */
                main.getUtils().sendMessage(versionChatComponent, true);
            } else if (args[0].equalsIgnoreCase("internal")) {
                if (args.length > 2) {
                    if (args[1].equalsIgnoreCase("copy")) {
                        DevUtils.copyStringToClipboard(
                                Arrays.stream(args).skip(2).collect(Collectors.joining(" ")),
                                getMessage("messages.copied"));
                    }
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                DevUtils.reloadAll();
            } else if (args[0].equalsIgnoreCase("reloadConfig")) {
                DevUtils.reloadConfig();
            } else if (args[0].equalsIgnoreCase("reloadRes")) {
                DevUtils.reloadResources();
            } else if (Feature.DEVELOPER_MODE.isEnabled()) {
                if (args[0].equalsIgnoreCase("brand")) {
                    String serverBrand = DevUtils.getServerBrand();

                    if (serverBrand != null) {
                        main.getUtils().sendMessage(getMessage("commands.responses.sba.brand.brandOutput", serverBrand));
                    } else {
                        throw new CommandException(getMessage("commands.responses.sba.brand.notFound"));
                    }
                } else if (args[0].equalsIgnoreCase("copyBlock")) {
                    DevUtils.setCopyMode(DevUtils.CopyMode.BLOCK);
                    DevUtils.copyData();

                } else if (args[0].equalsIgnoreCase("copyEntity")) {
                    try {
                        // Use default options if no options are provided and use defaults for any options that are missing.
                        if (args.length >= 3) {
                            DevUtils.setEntityNamesFromString(args[1]);
                            DevUtils.setEntityCopyRadius(parseInt(args[2]));
                        } else if (args.length == 2) {
                            DevUtils.setEntityNamesFromString(args[1]);
                            DevUtils.resetEntityCopyRadiusToDefault();
                        } else {
                            DevUtils.resetEntityNamesToDefault();
                            DevUtils.resetEntityCopyRadiusToDefault();
                        }
                        DevUtils.setCopyMode(DevUtils.CopyMode.ENTITY);
                        DevUtils.copyData();

                    } catch (IllegalArgumentException e) {
                        throw new WrongUsageException(e.getMessage());
                    }
                } else if (args[0].equalsIgnoreCase("copySidebar")) {
                    try {
                        if (args.length >= 2) {
                            DevUtils.setSidebarFormatted(parseBoolean(args[1]));
                        }
                        DevUtils.setCopyMode(DevUtils.CopyMode.SIDEBAR);
                        DevUtils.copyData();

                    } catch (NullPointerException e) {
                        throw new WrongUsageException(getMessage("commands.errors.wrongUsage.generic"));
                    }
                } else if (args[0].equalsIgnoreCase("copyTabList")) {
                    DevUtils.setCopyMode(DevUtils.CopyMode.TAB_LIST);
                    DevUtils.copyData();

                } else if (args[0].equalsIgnoreCase("copyOpenGL")) {
                    DevUtils.copyOpenGLLogs();

                } else if (args[0].equalsIgnoreCase("pd")) {
                    main.getUtils().sendMessage(EnumChatFormatting.BOLD + "Death Counts: ");
                    main.getUtils().sendMessage(EnumChatFormatting.WHITE + "Deaths: " + EnumChatFormatting.GOLD +
                            main.getDungeonManager().getDeaths());
                    main.getUtils().sendMessage(EnumChatFormatting.WHITE + "Alt Deaths: " + EnumChatFormatting.GOLD +
                            main.getDungeonManager().getAlternateDeaths());
                    main.getUtils().sendMessage(EnumChatFormatting.WHITE + "Tab Deaths: " + EnumChatFormatting.GOLD +
                            main.getDungeonManager().getPlayerListInfoDeaths());
                } else if (args[0].equalsIgnoreCase("toggleActionBarLogging")) {
                    DevUtils.setLoggingActionBarMessages(!DevUtils.isLoggingActionBarMessages());

                    if (DevUtils.isLoggingActionBarMessages()) {
                        main.getUtils().sendMessage(ColorCode.GREEN + getMessage(
                                "commands.responses.sba.toggleActionBarLogging.enabled"));
                    } else {
                        main.getUtils().sendMessage(ColorCode.RED + getMessage(
                                "commands.responses.sba.toggleActionBarLogging.disabled"));
                    }
                } else if (args[0].equalsIgnoreCase("toggleSlayerTrackerLogging")) {
                    DevUtils.setLoggingSlayerTracker(!DevUtils.isLoggingSlayerTracker());

                    if (DevUtils.isLoggingSlayerTracker()) {
                        main.getUtils().sendMessage(ColorCode.GREEN + getMessage(
                                "commands.responses.sba.toggleSlayerTrackerLogging.enabled"));
                    } else {
                        main.getUtils().sendMessage(ColorCode.RED + getMessage(
                                "commands.responses.sba.toggleSlayerTrackerLogging.disabled"));
                    }
                } else if (args[0].equalsIgnoreCase("toggleSkyBlockOreLogging")) {
                    DevUtils.setLoggingSkyBlockOre(!DevUtils.isLoggingSkyBlockOre());

                    if (DevUtils.isLoggingSkyBlockOre()) {
                        main.getUtils().sendMessage(ColorCode.GREEN + getMessage(
                                "commands.responses.sba.toggleSkyBlockOreLogging.enabled"));
                    } else {
                        main.getUtils().sendMessage(ColorCode.RED + getMessage(
                                "commands.responses.sba.toggleSkyBlockOreLogging.disabled"));
                    }
                } else {
                    throw new WrongUsageException(getMessage(
                            "commandUsage.sba.errors.wrongUsage.subCommandNotFound", args[0]));
                }
            } else {
                throw new WrongUsageException(getMessage(
                        "commandUsage.sba.errors.wrongUsage.subCommandNotFound", args[0]));
            }
        } catch (CommandException e) {
            ChatComponentTranslation errorMessage = new ChatComponentTranslation(e.getMessage(), e.getErrorObjects());
            errorMessage.getChatStyle().setColor(EnumChatFormatting.RED);

            // Intercept error handling to add our own prefix to error messages.
            throw new CommandException(Utils.MESSAGE_PREFIX + errorMessage.getFormattedText());
        }
    }

    /*
     Gets tab completion options listing all sub-commands.
     Developer mode commands are not included if developer mode is disabled.
     */
    private List<String> getSubCommandTabCompletionOptions(String[] args) {
        if (Feature.DEVELOPER_MODE.isEnabled()) {
            return getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        } else {
            return getListOfStringsMatchingLastWord(args, Arrays.copyOf(SUBCOMMANDS, 10));
        }
    }

    // This is an Enum representing options used by the sub-commands of this command.
    @AllArgsConstructor
    private enum CommandOption {
        COMMAND("Command", "commands.usage.sba.help.detailedHelp.options.command"),
        ZEALOTS("Zealots", "commands.usage.sba.set.zealotCounter.detailedHelp.options.zealots"),
        EYES("Eyes", "commands.usage.sba.set.zealotCounter.detailedHelp.options.eyes"),
        TOTAL_ZEALOTS("TotalZealots|Total", "commands.usage.sba.set.zealotCounter.detailedHelp.options.totalZealots"),
        FORMATTED("Formatted", "commands.usage.sba.copySidebar.detailedHelp.options.formatted"),
        ENTITY_NAMES("EntityNames", "commands.usage.sba.copyEntity.detailedHelp.options.entityNames"),
        RADIUS("Radius", "commands.usage.sba.copyEntity.detailedHelp.options.radius"),
        SLAYER_BOSS("Boss", "commands.usage.sba.slayer.detailedHelp.options.boss"),
        SLAYER_NUMBER("Number", "commands.usage.sba.slayer.detailedHelp.options.number"),
        SLAYER_STAT("Stat", "commands.usage.sba.slayer.detailedHelp.options.stat"),
        ;

        @Getter private final String name;
        private final String descriptionTranslationKey;

        /**
         * <p>This method returns a formatted string representation of this {@code CommandOption} object for display in a
         * sub-command help prompt. The format is as follows:</p>
         *
         * <i>§b● Option Name §7- Option Description</i>
         *
         * @return a formatted string representation of this {@code CommandOption} object
         */
        @Override
        public String toString() {
            return "§b● " + name + " §7- " + getMessage(descriptionTranslationKey);
        }
    }

    @AllArgsConstructor
    private enum Commands {
        BASE("/sba", "commands.usage.sba.base.help", null),
        HELP("/sba help [command]", "commands.usage.sba.help.help", Collections.singletonList(CommandOption.COMMAND)),
        EDIT("/sba edit", "commands.usage.sba.edit.help", null),
        SET("/sba set <zealots|eyes|totalZealots §eor§b total> <number>", "commands.usage.sba.set.zealotCounter.detailedHelp.description", Arrays.asList(CommandOption.ZEALOTS, CommandOption.EYES, CommandOption.TOTAL_ZEALOTS)),
        RESET_ZEALOT_COUNTER("/sba resetZealotCounter", "commands.usage.sba.resetZealotCounter.help", null),
        FOLDER("/sba folder", "commands.usage.sba.folder.help", null),
        SLAYER("/sba slayer <boss> <stat> <number>", "commands.usage.sba.slayer.detailedHelp.description", Arrays.asList(CommandOption.SLAYER_BOSS, CommandOption.SLAYER_STAT, CommandOption.SLAYER_NUMBER)),
        VERSION("/sba version", "commands.usage.sba.version.help", null),
        RELOAD("/sba reload", "commands.usage.sba.reload.help", null),
        RELOAD_CONFIG("/sba reloadConfig", "commands.usage.sba.reloadConfig.help", null),
        RELOAD_RES("/sba reloadRes", "commands.usage.sba.reloadRes.help", null),
        DEV("/sba dev", "commands.usage.sba.dev.detailedHelp.description", null),
        BRAND("/sba brand", "commands.usage.sba.brand.help", true,null),
        COPY_ENTITY("/sba copyEntity [entityNames] [radius: integer]", "commands.usage.sba.copyEntity.detailedHelp.description", true, Arrays.asList(CommandOption.ENTITY_NAMES, CommandOption.RADIUS)),
        COPY_SIDEBAR("/sba copySidebar [formatted: boolean]", "commands.usage.sba.copySidebar.detailedHelp.description", true, Collections.singletonList(CommandOption.FORMATTED)),
        COPY_TAB_LIST("/sba copyTabList", "commands.usage.sba.copyTabList.detailedHelp.description", true, null),
        COPY_OPENGL("/sba copyOpenGL", "commands.usage.sba.copyOpenGL.detailedHelp.description", true, null),
        COPY_BLOCK("/sba copyBlock", "commands.usage.sba.copyBlock.help", true, null),
        PD("/sba pd", "commands.usage.sba.printDeaths.help", true, null),
        TOGGLE_ACTION_BAR_LOGGING("/sba toggleActionBarLogging", "commands.usage.sba.toggleActionBarLogging.help", true, null),
        TOGGLE_SLAYER_TRACKER_LOGGING("/sba toggleSlayerTrackerLogging", "commands.usage.sba.toggleSlayerTrackerLogging.help", true, null),
        TOGGLE_SKYBLOCK_ORE_LOGGING("/sba toggleSkyBlockOreLogging", "commands.usage.sba.toggleSkyBlockOreLogging.help", true, null),
        ;

        private final String syntax;
        private final String descriptionTranslationKey;
        private final boolean devMode;
        private final List<CommandOption> options;

        Commands(String syntax, String descriptionTranslationKey, List<CommandOption> options) {
            this(syntax, descriptionTranslationKey, false, options);
        }

        public String createMenuDescriptionLine() {
            return "§b● " + this.syntax + " §7-§r "
                    + (devMode ? "§e(" + getMessage("commands.usage.sba.dev.prefix") + ")§r "  : "")
                    + getMessage(this.descriptionTranslationKey);
        }

        /**
         * <p>Returns a formatted usage string for the sub-command with the name of this Enum constant.</p>
         * <p>Example:</p>
         * <p>Usage: §b/sba help [command]§r</p>
         * <br>
         * <p>§lDescription:</p>
         * <p>§7Show this help message. If a command is provided, detailed help about that command is shown.</p>
         * <br>
         * <p>§lOptions:</p>
         * <p>§b● Command §7- the sub-command to get detailed usage for</p>
         *
         * @return a formatted String representing this {@code SubCommandUsage}
         */
        @Override
        public String toString() {
            StringBuilder usageBuilder = new StringBuilder(
                    "Usage: §b" + syntax + "§r" +
                    "\n" +
                    "\n§lDescription:" +
                    "\n§7" + getMessage(descriptionTranslationKey));

            if (options != null) {
                ListIterator<CommandOption> optionListIterator = options.listIterator();

                usageBuilder.append("\n").append("\n§lOptions:");

                while (optionListIterator.hasNext()) {
                    usageBuilder.append("\n");
                    usageBuilder.append(optionListIterator.next());
                }
            }

            return usageBuilder.toString();
        }
    }
}
