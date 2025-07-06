package com.fix3dll.skyblockaddons.commands;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.SkyblockKeyBinding;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerBoss;
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerDrop;
import com.fix3dll.skyblockaddons.features.slayertracker.SlayerTracker;
import com.fix3dll.skyblockaddons.utils.DevUtils;
import com.fix3dll.skyblockaddons.utils.EnumUtils;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.google.common.base.CaseFormat;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SkyblockAddonsCommand {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    private static final String HEADER = "§7§m                §7[ §b§lSkyblockAddons §7]§7§m                §r";
    private static final String FOOTER = "§7§m                                                     §r";
    private static final String[] SUBCOMMANDS = {"help", "edit", "folder", "resetZealotCounter", "set", "slayer",
            "version", "reload", "reloadConfig", "reloadRes", "dev", "brand", "copyBlock", "copyEntity", "copySidebar",
            "copyTabList", "pd", "toggleActionBarLogging", "toggleSlayerTrackerLogging", "copyOpenGL",
            "toggleSkyBlockOreLogging"
    };

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> builder = buildCommands();
            dispatcher.register(builder);
            dispatcher.register(literal("sba").executes(ctx -> {
                main.getUtils().setFadingIn(true);
                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
                return 1;
            }).redirect(builder.build()));
        });
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> buildCommands() {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(SkyblockAddons.MOD_ID).executes(ctx -> {
            main.getUtils().setFadingIn(true);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
            return 1;
        });

        // HELP
        builder.then(literal("help").executes(ctx -> {
            Utils.sendMessage(getCommandUsage(), false);
            return 1;
        }).then(argument("command", StringArgumentType.word()).suggests((ctx, suggestionsBuilder) -> {
            for (String subcommand : SUBCOMMANDS) {
                suggestionsBuilder.suggest(subcommand);
            }
            return suggestionsBuilder.buildFuture();
        }).executes(ctx -> {
            String argument = ctx.getArgument("command", String.class);
            try {
                Utils.sendMessage(getSubCommandUsage(argument), false);
                return 1;
            } catch (IllegalArgumentException e) {
                Utils.sendErrorMessage(Translations.getMessage("commands.errors.wrongUsage.subCommandNotFound", argument));
                return 0;
            }
        })));

        // EDIT
        builder.then(literal("edit").executes(ctx -> {
            main.getUtils().setFadingIn(false);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.EDIT_LOCATIONS, 0, null);
            return 1;
        }));

        // SET
        LiteralArgumentBuilder<FabricClientCommandSource> set = literal("set")
                .executes(ctx -> {
                    Utils.sendErrorMessage(Translations.getMessage("commands.errors.wrongUsage.generic"));
                    return 0;
                });
        set.then(literal("zealots")
                .then(argument("number", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            int val = ctx.getArgument("number", Integer.class);
                            main.getPersistentValuesManager().getPersistentValues().setKills(val);
                            main.getPersistentValuesManager().saveValues();
                            Utils.sendMessage(
                                    Translations.getMessage("commands.responses.sba.set.zealotCounter.zealotsSet", val)
                            );
                            return 1;
                        })));
        set.then(literal("totalZealots")
                .then(argument("number", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            int val = ctx.getArgument("number", Integer.class);
                            main.getPersistentValuesManager().getPersistentValues().setTotalKills(val);
                            main.getPersistentValuesManager().saveValues();
                            Utils.sendMessage(
                                    Translations.getMessage("commands.responses.sba.set.zealotCounter.totalZealotsSet", val)
                            );
                            return 1;
                        })));
        set.then(literal("eyes")
                .then(argument("number", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            int val = ctx.getArgument("number", Integer.class);
                            main.getPersistentValuesManager().getPersistentValues().setSummoningEyeCount(val);
                            main.getPersistentValuesManager().saveValues();
                            Utils.sendMessage(
                                    Translations.getMessage("commands.responses.sba.set.zealotCounter.eyesSet", val)
                            );
                            return 1;
                        })));
        builder.then(set);

        // RESET_ZEALOT_COUNTER
        builder.then(literal("resetZealotCounter").executes(ctx -> {
            main.getPersistentValuesManager().resetZealotCounter();
            Utils.sendMessage(ColorCode.GREEN + Translations.getMessage("commands.responses.sba.resetZealotCounter.resetSuccess"));
            return 1;
        }));

        // FOLDER
        builder.then(literal("folder").executes(ctx -> {
            Util.getPlatform().openFile(main.getUtils().getSBAFolder());
            return 1;
        }));

        // SLAYER
        builder.then(literal("slayer").executes(ctx -> {
            StringBuilder bosses = new StringBuilder();
            for (int i = 0; i < SlayerBoss.values().length; i++) {
                SlayerBoss slayerBoss = SlayerBoss.values()[i];
                bosses.append("'").append(slayerBoss.getMobType().toLowerCase(Locale.US)).append("'");
                if (i + 1 < SlayerBoss.values().length) {
                    bosses.append(", ");
                }
            }
            Utils.sendErrorMessage(
                    Translations.getMessage("commands.responses.sba.slayer.bossRequired", bosses.toString())
            );
            return 0;
        // <boss>  →  statRequired
        }).then(argument("boss", StringArgumentType.word()).suggests((ctx, suggestionsBuilder) -> {
            String remaining = suggestionsBuilder.getRemaining();
            for (SlayerBoss boss : SlayerBoss.values()) {
                String bossType = boss.getMobType();
                if (bossType.startsWith(remaining)) {
                    suggestionsBuilder.suggest(bossType);
                }
            }
            return suggestionsBuilder.buildFuture();
        }).executes(ctx -> {
            Utils.sendErrorMessage(Translations.getMessage(
                    "commands.responses.sba.slayer.bossRequired",
                    Arrays.stream(SlayerBoss.values()).map(SlayerBoss::getMobType).toList()
            ));
            return 1;
            // <boss> <stat> <value>
        }).then(argument("stat",  StringArgumentType.word()).suggests((ctx, suggestionsBuilder) -> {
            String remaining = suggestionsBuilder.getRemaining();
            if ("reset_all".startsWith(remaining)) suggestionsBuilder.suggest("reset_all");
            if ("kills".startsWith(remaining)) suggestionsBuilder.suggest("kills");
            String input = suggestionsBuilder.getInput();
            if (!input.isBlank()) {
                int lastSpace   = input.lastIndexOf(' ');
                int prevSpace   = input.lastIndexOf(' ', lastSpace - 1);
                String boss = input.substring(prevSpace + 1, lastSpace);
                SlayerBoss slayerBoss = SlayerBoss.getFromMobType(boss);
                if (slayerBoss != null) {
                    for (SlayerDrop drop : slayerBoss.getDrops()) {
                        String dropName = drop.name().toLowerCase(Locale.ENGLISH);
                        if (dropName.startsWith(remaining)) {
                            suggestionsBuilder.suggest(drop.name().toLowerCase(Locale.ENGLISH));
                        }
                    }
                }

            }
            return suggestionsBuilder.buildFuture();
        }).executes(ctx -> {
            String boss = ctx.getArgument("boss", String.class);
            String stat = ctx.getArgument("stat", String.class);
            if ("reset_all".equalsIgnoreCase(stat)) {
                SlayerTracker.getInstance().resetAllStats(boss);
                return 1;
            } else {
                Utils.sendErrorMessage(Translations.getMessage("commands.responses.sba.slayer.statRequired"));
                return 0;
            }
            // <boss> <reset_all|kills|drop>
        }).then(argument("value", IntegerArgumentType.integer()).executes(ctx -> {
            String boss = ctx.getArgument("boss", String.class);
            String stat = ctx.getArgument("stat", String.class);
            int value = ctx.getArgument("value", Integer.class);
            SlayerTracker.getInstance().setStatManually(boss, stat, value);
            return 1;
        })))));

        // VERSION
        builder.then(literal("version").executes(ctx -> {
            String versionString = Translations.getMessage("messages.version") + " v" + SkyblockAddons.METADATA.getVersion();

            MutableComponent versionText = Component.literal(versionString).withStyle(style ->
                    style.withHoverEvent(new HoverEvent.ShowText(Component.literal(
                            ColorCode.AQUA + Translations.getMessage("commands.responses.sba.version.hoverText")

                    ))).withClickEvent(
                            new ClickEvent.SuggestCommand(SkyblockAddons.METADATA.getVersion().toString())
                    ));
            Utils.sendMessage(versionText, true);

            return 1;
        }));

        // RELOAD
        builder.then(literal("reload").executes(ctx -> {
            DevUtils.reloadAll();
            return 1;
        }));

        // RELOAD_CONFIG
        builder.then(literal("reloadConfig").executes(ctx -> {
            main.getConfigValuesManager().setFirstLoad(true); // Trigger extra checks for corrupted values.
            DevUtils.reloadConfig();
            return 1;
        }));

        // RELOAD_RES
        builder.then(literal("reloadRes").executes(ctx -> {
            DevUtils.reloadResources();
            return 1;
        }));

        // DEV
        builder.then(literal("dev").executes(ctx -> {
            Feature.DEVELOPER_MODE.setEnabled(Feature.DEVELOPER_MODE.isDisabled());
            if (Feature.DEVELOPER_MODE.isEnabled()) {
                Utils.sendMessage(
                        ColorCode.GREEN + Translations.getMessage("commands.responses.sba.dev.enabled",
                                SkyblockKeyBinding.DEVELOPER_COPY_NBT.getKeyBinding().getTranslatedKeyMessage().getString()
                        )
                );
            } else {
                Utils.sendMessage(ColorCode.RED + Translations.getMessage("commands.responses.sba.dev.disabled"));
            }
            return 1;
        }));

        // BRAND
        builder.then(literal("brand").requires(rq -> Feature.DEVELOPER_MODE.isEnabled()).executes(ctx -> {
            String serverBrand = DevUtils.getServerBrand();
            if (serverBrand != null) {
                Utils.sendMessage(Translations.getMessage("commands.responses.sba.brand.brandOutput", serverBrand));
            } else {
                Utils.sendErrorMessage(Translations.getMessage("commands.responses.sba.brand.notFound"));
            }
            return 1;
        }));

        // COPY_ENTITY
        builder.then(literal("copyEntity").requires(rq -> Feature.DEVELOPER_MODE.isEnabled()).executes(ctx -> {
            DevUtils.setCopyMode(DevUtils.CopyMode.ENTITY);
            DevUtils.resetEntityCopyRadiusToDefault();
            DevUtils.resetEntityNamesToDefault();
            DevUtils.copyData();
            return 1;
        }).then(argument("entityType", StringArgumentType.word()).suggests((ctx, suggestionsBuilder) -> {
            String remaining = suggestionsBuilder.getRemaining();
            DevUtils.ALL_ENTITIES.keySet().forEach(string -> {
                if (string.startsWith(remaining)) {
                    suggestionsBuilder.suggest(string);
                }
            });
            return suggestionsBuilder.buildFuture();
        }).executes(ctx -> {
            DevUtils.setCopyMode(DevUtils.CopyMode.ENTITY);
            DevUtils.setEntityNamesFromString(ctx.getArgument("entityType", String.class));
            DevUtils.resetEntityCopyRadiusToDefault();
            DevUtils.copyData();
            return 1;
        }).then(argument("radius", IntegerArgumentType.integer()).executes(ctx -> {
            DevUtils.setCopyMode(DevUtils.CopyMode.ENTITY);
            DevUtils.setEntityNamesFromString(ctx.getArgument("entityType", String.class));
            DevUtils.setEntityCopyRadius(ctx.getArgument("radius", Integer.class));
            DevUtils.copyData();
            return 1;
        }))));

        // COPY_SIDEBAR
        builder.then(literal("copySidebar").requires(rq -> Feature.DEVELOPER_MODE.isEnabled()).executes(ctx -> {
            DevUtils.setCopyMode(DevUtils.CopyMode.SIDEBAR);
            DevUtils.copyData();
            return 1;
        }).then(argument("formatted", BoolArgumentType.bool()).executes(ctx -> {
            boolean formatted = BoolArgumentType.getBool(ctx, "formatted");
            DevUtils.setSidebarFormatted(formatted);
            DevUtils.setCopyMode(DevUtils.CopyMode.SIDEBAR);
            DevUtils.copyData();
            return 1;
        })));

        // COPY_TAB_LIST
        builder.then(literal("copyTabList").requires(rq -> Feature.DEVELOPER_MODE.isEnabled()).executes(ctx -> {
            DevUtils.setCopyMode(DevUtils.CopyMode.TAB_LIST);
            DevUtils.copyData();
            return 1;
        }));

        // COPY_BLOCK
        builder.then(literal("copyBlock").requires(rq -> Feature.DEVELOPER_MODE.isEnabled()).executes(ctx -> {
            DevUtils.setCopyMode(DevUtils.CopyMode.BLOCK);
            DevUtils.copyData();
            return 1;
        }));

        // PD
        builder.then(literal("pd").requires(rq -> Feature.DEVELOPER_MODE.isEnabled()).executes(ctx -> {
            Utils.sendMessage(ColorCode.BOLD + "Death Counts: ");
            Utils.sendMessage(ColorCode.WHITE + "Deaths: " + ColorCode.GOLD +
                    main.getDungeonManager().getDeaths());
            Utils.sendMessage(ColorCode.WHITE + "Alt Deaths: " + ColorCode.GOLD +
                    main.getDungeonManager().getAlternateDeaths());
            Utils.sendMessage(ColorCode.WHITE + "Tab Deaths: " + ColorCode.GOLD +
                    main.getDungeonManager().getPlayerListInfoDeaths());
            return 1;
        }));

        // TOGGLE_ACTION_BAR_LOGGING
        builder.then(literal("toggleActionBarLogging").requires(rq -> Feature.DEVELOPER_MODE.isEnabled()).executes(ctx -> {
            DevUtils.setLoggingActionBarMessages(!DevUtils.isLoggingActionBarMessages());

            if (DevUtils.isLoggingActionBarMessages()) {
                Utils.sendMessage(
                        ColorCode.GREEN + Translations.getMessage("commands.responses.sba.toggleActionBarLogging.enabled")
                );
            } else {
                Utils.sendMessage(
                        ColorCode.RED + Translations.getMessage("commands.responses.sba.toggleActionBarLogging.disabled")
                );
            }
            return 1;
        }));

        // TOGGLE_SLAYER_TRACKER_LOGGING
        builder.then(literal("toggleSlayerTrackerLogging").requires(rq -> Feature.DEVELOPER_MODE.isEnabled()).executes(ctx -> {
            DevUtils.setLoggingSlayerTracker(!DevUtils.isLoggingSlayerTracker());

            if (DevUtils.isLoggingSlayerTracker()) {
                Utils.sendMessage(
                        ColorCode.GREEN + Translations.getMessage("commands.responses.sba.toggleSlayerTrackerLogging.enabled")
                );
            } else {
                Utils.sendMessage(
                        ColorCode.RED + Translations.getMessage("commands.responses.sba.toggleSlayerTrackerLogging.disabled")
                );
            }
            return 1;
        }));

        // TOGGLE_SKYBLOCK_ORE_LOGGING
        builder.then(literal("toggleSkyBlockOreLogging").requires(rq -> Feature.DEVELOPER_MODE.isEnabled()).executes(ctx -> {
            DevUtils.setLoggingSkyBlockOre(!DevUtils.isLoggingSkyBlockOre());

            if (DevUtils.isLoggingSkyBlockOre()) {
                Utils.sendMessage(
                        ColorCode.GREEN + Translations.getMessage("commands.responses.sba.toggleSkyBlockOreLogging.enabled")
                );
            } else {
                Utils.sendMessage(
                        ColorCode.RED + Translations.getMessage("commands.responses.sba.toggleSkyBlockOreLogging.disabled")
                );
            }
            return 1;
        }));

        // WARP
        builder.then(literal("warp").executes(ctx -> {
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.WARP);
            return 1;
        }));

        builder.then(literal("internal").then(argument("command", StringArgumentType.word()).executes(ctx -> {
            String command = ctx.getArgument("command", String.class);
            if ("launchAutoUpdate".equalsIgnoreCase(command)) {
                main.getUpdater().launchAutoUpdate();
            }
            return 1;
        }).then(argument("arg", StringArgumentType.greedyString()).executes(ctx -> {
            String command = ctx.getArgument("command", String.class);
            String arg = ctx.getArgument("arg", String.class).toLowerCase(Locale.US);
            if ("copy".equalsIgnoreCase(command)) {
                DevUtils.copyStringToClipboard(arg, Translations.getMessage("messages.copied"));
            }
            return 1;
        }))));

        return builder;
    }

    /**
     * <p>Gets the usage string for the command. If developer mode is enabled, the developer mode usage string is added to
     * the main usage string.</p>
     */
    public static Component getCommandUsage() {
        StringBuilder builder = new StringBuilder(HEADER);
        for (Commands command : Commands.values()) {
            if (command.devMode && Feature.DEVELOPER_MODE.isDisabled()) continue;
            builder.append("\n").append(command.createMenuDescriptionLine());
        }
        builder.append("\n").append(FOOTER);

        return Component.literal(builder.toString());
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
    public static Component getSubCommandUsage(String subCommand) {
        for (String validSubCommand : SUBCOMMANDS) {
            if (subCommand.equalsIgnoreCase(validSubCommand)) {
                subCommand = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, validSubCommand);
            }
        }

        return Component.literal(HEADER + "\n" + Commands.valueOf(subCommand) + "\n" + FOOTER);
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

        @Getter
        private final String name;
        private final String descriptionTranslationKey;

        /**
         * <p>This method returns a formatted string representation of this {@code CommandOption} object for display in a
         * sub-command help prompt. The format is as follows:</p>
         * <i>§b● Option Name §7- Option Description</i>
         * @return a formatted string representation of this {@code CommandOption} object
         */
        @Override
        public String toString() {
            return "§b● " + name + " §7- " + Translations.getMessage(descriptionTranslationKey);
        }
    }

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
        BRAND("/sba brand", "commands.usage.sba.brand.help", null,true),
        COPY_ENTITY("/sba copyEntity [entityNames] [radius: integer]", "commands.usage.sba.copyEntity.detailedHelp.description", Arrays.asList(CommandOption.ENTITY_NAMES, CommandOption.RADIUS), true),
        COPY_SIDEBAR("/sba copySidebar [formatted: boolean]", "commands.usage.sba.copySidebar.detailedHelp.description", Collections.singletonList(CommandOption.FORMATTED), true),
        COPY_TAB_LIST("/sba copyTabList", "commands.usage.sba.copyTabList.detailedHelp.description", null, true),
        COPY_OPENGL("/sba copyOpenGL", "commands.usage.sba.copyOpenGL.detailedHelp.description", null, true),
        COPY_BLOCK("/sba copyBlock", "commands.usage.sba.copyBlock.help", null, true),
        PD("/sba pd", "commands.usage.sba.printDeaths.help", null, true),
        TOGGLE_ACTION_BAR_LOGGING("/sba toggleActionBarLogging", "commands.usage.sba.toggleActionBarLogging.help", null, true),
        TOGGLE_SLAYER_TRACKER_LOGGING("/sba toggleSlayerTrackerLogging", "commands.usage.sba.toggleSlayerTrackerLogging.help", null, true),
        TOGGLE_SKYBLOCK_ORE_LOGGING("/sba toggleSkyBlockOreLogging", "commands.usage.sba.toggleSkyBlockOreLogging.help", null, true),
        ;

        private final String syntax;
        private final String descriptionTranslationKey;
        private final boolean devMode;
        private final List<CommandOption> options;

        Commands(String syntax, String descriptionTranslationKey, List<CommandOption> options) {
            this(syntax, descriptionTranslationKey, options, false);
        }

        Commands(String syntax, String descriptionTranslationKey, List<CommandOption> options, boolean devMode) {
            this.syntax = syntax;
            this.descriptionTranslationKey = descriptionTranslationKey;
            this.options = options;
            this.devMode = devMode;
        }

        public String createMenuDescriptionLine() {
            return "§b● " + this.syntax + " §7-§r "
                    + (devMode ? "§e(" + Translations.getMessage("commands.usage.sba.dev.prefix") + ")§r "  : "")
                    + Translations.getMessage(this.descriptionTranslationKey);
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
                            "\n§7" + Translations.getMessage(descriptionTranslationKey));

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
