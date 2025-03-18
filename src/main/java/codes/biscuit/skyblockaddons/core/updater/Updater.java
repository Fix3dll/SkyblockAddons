package codes.biscuit.skyblockaddons.core.updater;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.SkyblockAddonsASMTransformer;
import codes.biscuit.skyblockaddons.core.feature.Feature;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.EnumUtils.AutoUpdateMode;
import codes.biscuit.skyblockaddons.utils.Utils;
import codes.biscuit.skyblockaddons.utils.data.skyblockdata.OnlineData;
import lombok.Getter;
import moe.nea.libautoupdate.CurrentVersion;
import moe.nea.libautoupdate.PotentialUpdate;
import moe.nea.libautoupdate.UpdateContext;
import moe.nea.libautoupdate.UpdateTarget;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static codes.biscuit.skyblockaddons.core.Translations.getMessage;
import static net.minecraftforge.common.ForgeVersion.Status.*;

/**
 * This class is the SkyblockAddons updater. It checks for updates by reading version information from {@link OnlineData.UpdateInfo}.
 */
public class Updater {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(?<major>[0-9])\\.(?<minor>[0-9])\\.(?<patch>[0-9]).*");
    private static final UpdateContext AUTO_UPDATE_CONTEXT = new UpdateContext(
            new CustomUpdateSource(),
            UpdateTarget.deleteAndSaveInTheSameFolder(SkyblockAddons.class),
            CurrentVersion.ofTag(SkyblockAddons.VERSION),
            SkyblockAddons.MOD_ID
    );

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private ComparableVersion target = null;

    @Getter private String messageToRender;
    private String downloadLink;
    private String changelogLink;
    private String note;

    private boolean hasUpdate = false;
    private boolean isPatch = false;
    private boolean sentUpdateMessage = false;
    private PotentialUpdate cachedPotentialUpdate = null;
    private boolean updateLaunched = false;

    public Updater() {
        AUTO_UPDATE_CONTEXT.cleanup();
    }

    /**
     * Returns whether the update notification message has already been sent.
     *
     * @return {@code true} if the update notification message has already been sent, {@code false} otherwise
     */
    public boolean hasSentUpdateMessage() {
        return sentUpdateMessage;
    }

    /**
     * Returns whether there is an update available
     *
     * @return {@code true} if there is an update available, {@code false} otherwise.
     */
    public boolean hasUpdate() {
        return hasUpdate;
    }

    /**
     * Checks the online data for an update and sets the correct message to be displayed.
     */
    public void checkForUpdate() {
        LOGGER.info("Checking to see if an update is available...");
        OnlineData.UpdateInfo updateInfo = main.getOnlineData().getUpdateInfo();

        // Variables reset for testing update checker notifications
        sentUpdateMessage = false;
        main.getRenderListener().setUpdateMessageDisplayed(false);

        if (updateInfo == null) {
            LOGGER.error("Update check failed: Update info is null!");
            return;
        }

        ComparableVersion latestRelease = null;
        ComparableVersion latestBeta = null;
        ComparableVersion current = new ComparableVersion(SkyblockAddons.VERSION);
        boolean isCurrentBeta = isBetaVersion(current);
        boolean latestReleaseExists = updateInfo.getLatestRelease() != null && !updateInfo.getLatestRelease().isEmpty();
        boolean latestBetaExists = updateInfo.getLatestBeta() != null && !updateInfo.getLatestBeta().isEmpty();
        int releaseDiff = 0;
        int betaDiff = 0;

        if (latestReleaseExists) {
            latestRelease = new ComparableVersion(updateInfo.getLatestRelease());
            releaseDiff = latestRelease.compareTo(current);
        } else {
            if (!isCurrentBeta) {
                LOGGER.error("Update check failed: Current version is a release version and key `latestRelease` is null " +
                        "or empty.");
                return;
            } else {
                LOGGER.warn("Key `latestRelease` is null or empty, skipping!");
            }
        }

        if (isCurrentBeta) {
            if (latestBetaExists) {
                latestBeta = new ComparableVersion(updateInfo.getLatestBeta());
                betaDiff = latestBeta.compareTo(current);
            } else {
                if (latestRelease == null) {
                    LOGGER.error("Update check failed: Keys `latestRelease` and `latestBeta` are null or empty.");
                    return;
                } else {
                    LOGGER.warn("Key `latestBeta` is null or empty, skipping!");
                }
            }
        }

        ForgeVersion.Status status = null;
        if (!isCurrentBeta) {
            if (releaseDiff == 0) {
                status = UP_TO_DATE;
            } else if (releaseDiff < 0) {
                status = AHEAD;
            } else {
                status = OUTDATED;
                target = latestRelease;
            }
        } else {
            String currentVersionString = current.toString();

            // If release is newer than this beta, target release
            if (latestReleaseExists) {
                ComparableVersion currentWithoutPrerelease = new ComparableVersion(
                        currentVersionString.substring(0, currentVersionString.indexOf('-'))
                );

                if (releaseDiff > 0 || latestRelease.compareTo(currentWithoutPrerelease) == 0) {
                    status = OUTDATED;
                    target = latestRelease;
                } else if (!latestBetaExists && releaseDiff < 0) {
                    status = AHEAD;
                } else if (releaseDiff == 0) {
                    LOGGER.warn("The current beta version ({}) matches the latest release version. " +
                            "There is probably something wrong with the online data.", currentVersionString);
                    status = UP_TO_DATE;
                }
            }

            if (status == null) {
                if (betaDiff == 0) {
                    status = UP_TO_DATE;
                } else if (betaDiff < 0) {
                    status = AHEAD;
                } else {
                    status = BETA_OUTDATED;
                    target = latestBeta;
                }
            }
        }

        if (status == OUTDATED || status == BETA_OUTDATED) {
            Object autoUpdate = Feature.AUTO_UPDATE.getValue();
            if (autoUpdate == AutoUpdateMode.STABLE || autoUpdate == AutoUpdateMode.LATEST) {
                AutoUpdateMode updateStream = (AutoUpdateMode) autoUpdate;
                AUTO_UPDATE_CONTEXT.checkUpdate(updateStream.name()).whenComplete((potentialUpdate, throwable) -> {
                    if (potentialUpdate.getUpdate() == null) {
                        Utils.sendMessageOrElseLog("The automatic update check could not be completed because the Online Data was not fetched from the CDN.", LOGGER, true);
                    } else if (throwable != null) {
                        Utils.sendMessageOrElseLog("Auto update check failed!", LOGGER, true);
                        LOGGER.catching(throwable);
                    } else if (potentialUpdate.isUpdateAvailable()) {
                        cachedPotentialUpdate = potentialUpdate;
                    }
                });
            }
            hasUpdate = true;

            String currentVersion = current.toString();
            String targetVersion = target.toString();

            LOGGER.info("Found an update: {}", targetVersion);

            if (status == OUTDATED) {
                targetVersion = updateInfo.getLatestRelease();
                downloadLink = updateInfo.getReleaseDownload();
                changelogLink = updateInfo.getReleaseChangelog();
                note = updateInfo.getReleaseNote();
            } else {
                targetVersion = updateInfo.getLatestBeta();
                downloadLink = updateInfo.getBetaDownload();
                changelogLink = updateInfo.getBetaChangelog();
                note = updateInfo.getBetaNote();
            }

            try {
                Matcher currentMatcher = VERSION_PATTERN.matcher(currentVersion);
                Matcher targetMatcher = VERSION_PATTERN.matcher(targetVersion);

                // It's a patch if the major & minor numbers are the same & the player isn't upgrading from a beta.
                isPatch = currentMatcher.matches() && targetMatcher.matches() &&
                        currentMatcher.group("major").equals(targetMatcher.group("major")) &&
                        currentMatcher.group("minor").equals(targetMatcher.group("minor")) &&
                        !isCurrentBeta;
            } catch (Exception ex) {
                LOGGER.warn("Couldn't parse update version numbers... This shouldn't affect too much.", ex);
            }

            if (isPatch) {
                messageToRender = getMessage("messages.updateChecker.notificationBox.patchAvailable", targetVersion);
            } else if(status == BETA_OUTDATED) {
                messageToRender = getMessage("messages.updateChecker.notificationBox.betaAvailable", targetVersion);
            } else {
                messageToRender = getMessage("messages.updateChecker.notificationBox.majorAvailable", targetVersion);
            }
        } else if (status == AHEAD) {
            if (!SkyblockAddonsASMTransformer.isDeobfuscated()) {
                LOGGER.warn("The current version is newer than the latest version. Please tell an SBA developer to update" +
                        " the online data.");
            } else {
                LOGGER.error("The current version is newer than the latest version. You're doing something wrong.");
                LOGGER.error("Current: {}", current);
                LOGGER.error("Latest: {}", latestRelease);
                LOGGER.error("Latest Beta: {}", latestBeta);
                LOGGER.error("Release Diff: {}", releaseDiff);
                LOGGER.error("Beta Diff: {}", betaDiff);
            }
        } else {
            LOGGER.info("Up to date!");
        }
    }

    public void sendUpdateMessage() {
        if (sentUpdateMessage) {
            return;
        }

        String targetVersion = target.toString();

        Utils.sendMessage("§7§m----------------§7[ §b§lSkyblockAddons §7]§7§m----------------", false);

        ChatComponentText newUpdate = new ChatComponentText(
                String.format("§b%s\n", getMessage("messages.updateChecker.newUpdateAvailable", targetVersion))
        );

        if (note != null && !note.isEmpty()) {
            ChatComponentText versionNote = new ChatComponentText("\n" + ColorCode.RED + note + "\n");
            newUpdate.appendSibling(versionNote);
        }

        Utils.sendMessage(newUpdate, false);

        // TODO organization
        ChatComponentText autoDownloadButton;
        ChatComponentText downloadButton;
        ChatComponentText openModsFolderButton;
        ChatComponentText changelogButton;

        autoDownloadButton = new ChatComponentText(
                String.format("§a§l[%s]", getMessage("messages.updateChecker.autoDownloadButton"))
        );

        Object autoUpdateValue = Feature.AUTO_UPDATE.getValue();
        if (autoUpdateValue == AutoUpdateMode.STABLE || autoUpdateValue == AutoUpdateMode.LATEST) {
            AutoUpdateMode autoUpdateMode = (AutoUpdateMode) autoUpdateValue;
            if (cachedPotentialUpdate == null) {
                autoDownloadButton.text = String.format("§8§m[%s]§r", getMessage("messages.updateChecker.autoDownloadButton"));
                autoDownloadButton.setChatStyle(
                        autoDownloadButton.getChatStyle().setChatHoverEvent(
                                new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        new ChatComponentText("§7" + getMessage("messages.updateChecker.autoUpdateTargetNotFound"))
                                )
                        )
                );
                autoDownloadButton.appendText(" ");
            } else if (!main.getOnlineData().getUpdateData(autoUpdateMode.name()).getVersionNumber().getAsString().contains(targetVersion)) {
                autoDownloadButton.text = String.format("§8§m[%s]§r", getMessage("messages.updateChecker.autoDownloadButton"));
                autoDownloadButton.setChatStyle(
                        autoDownloadButton.getChatStyle().setChatHoverEvent(
                                new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        new ChatComponentText("§7" + getMessage("messages.updateChecker.autoUpdateTargetIsNotUpToDate"))
                                )
                        )
                );
                autoDownloadButton.appendText(" ");
            } else {
                if (Feature.FULL_AUTO_UPDATE.isEnabled()) {
                    if (!updateLaunched) {
                        Utils.sendMessage(ColorCode.YELLOW + getMessage("messages.updateChecker.autoDownloadStarted") + "\n", false);
                        launchAutoUpdate(cachedPotentialUpdate);
                    }
                    autoDownloadButton.text = "";
                } else {
                    autoDownloadButton.setChatStyle(
                            autoDownloadButton.getChatStyle().setChatClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sba internal launchAutoUpdate")
                            ).setChatHoverEvent(
                                    new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            new ChatComponentText("§7" + getMessage("messages.updateChecker.autoDownloadButtonHover", targetVersion))
                                    )
                            )
                    );
                    autoDownloadButton.appendText(" ");
                }
            }
        } else {
            autoDownloadButton.text = "";
        }

        downloadButton = new ChatComponentText(
                String.format("§b[%s]", getMessage("messages.updateChecker.downloadButton"))
        );

        if (downloadLink != null && !downloadLink.isEmpty()) {
            downloadButton.setChatStyle(
                    downloadButton.getChatStyle().setChatClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, downloadLink)
                    ).setChatHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new ChatComponentText("§7" + getMessage("messages.clickToOpenLink"))
                            )
                    )
            );
        } else {
            downloadButton.setChatStyle(
                    downloadButton.getChatStyle().setStrikethrough(true).setChatHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new ChatComponentText("§7" + getMessage("messages.updateChecker.noDownloadAvailable"))
                            )
                    )
            );
        }
        autoDownloadButton.appendSibling(downloadButton).appendText(" ");

        openModsFolderButton = new ChatComponentText(
                String.format("§e[%s]", getMessage("messages.updateChecker.openModFolderButton"))
        );
        openModsFolderButton.setChatStyle(
                openModsFolderButton.getChatStyle().setChatClickEvent(
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sba folder")
                ).setChatHoverEvent(
                        new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText("§7" + getMessage("messages.clickToOpenFolder"))
                        )
                )
        );
        autoDownloadButton.appendSibling(openModsFolderButton).appendText(" ");

        if (changelogLink != null && !changelogLink.isEmpty()) {
            changelogButton = new ChatComponentText(
                    String.format("§9[%s]", getMessage("messages.updateChecker.changelogButton"))
            );
            changelogButton.setChatStyle(
                    changelogButton.getChatStyle().setChatClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_URL, changelogLink)
                    ).setChatHoverEvent(
                            new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new ChatComponentText("§7" + getMessage("messages.clickToOpenLink"))
                            )
                    )
            );
            autoDownloadButton.appendSibling(changelogButton).appendText(" ");
        }

        Utils.sendMessage(autoDownloadButton, false);
        Utils.sendMessage("§7§m--------------------------------------------------", false);

        sentUpdateMessage = true;
    }

    /**
     * Returns whether the given version is a beta version
     * @param version the version to check
     * @return {@code true} if the given version is a beta version, {@code false} otherwise
     */
    private boolean isBetaVersion(ComparableVersion version) {
        return version.toString().contains("b");
    }

    public void launchAutoUpdate() {
        if (cachedPotentialUpdate != null && !updateLaunched) {
            Utils.sendMessage(ColorCode.YELLOW + getMessage("messages.updateChecker.autoDownloadStarted"));
            launchAutoUpdate(cachedPotentialUpdate);
        } else {
            LOGGER.warn("cachedPotentialUpdate: {}, updateLaunched: {}", cachedPotentialUpdate, updateLaunched);
        }
    }

    public void launchAutoUpdate(@NotNull PotentialUpdate potentialUpdate) {
        updateLaunched = true;
        potentialUpdate.launchUpdate().whenComplete((ignored, throwableUpdate) -> {
            if (throwableUpdate != null) {
                Utils.sendMessageOrElseLog("§cAuto update failed! See the log for more details.", LOGGER, true);
                LOGGER.catching(throwableUpdate);
            } else {
                Utils.sendMessageOrElseLog("§aThe update has been downloaded successfully. It will be installed after the reboot.", LOGGER, false);
                cachedPotentialUpdate = null;
            }
            updateLaunched = false;
        });
    }
}
