package com.fix3dll.skyblockaddons.core.updater;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.ColorCode;
import com.fix3dll.skyblockaddons.core.Translations;
import com.fix3dll.skyblockaddons.core.feature.Feature;
import com.fix3dll.skyblockaddons.utils.EnumUtils.AutoUpdateMode;
import com.fix3dll.skyblockaddons.utils.Utils;
import com.fix3dll.skyblockaddons.utils.data.skyblockdata.OnlineData;
import com.google.gson.JsonElement;
import lombok.Getter;
import moe.nea.libautoupdate.CurrentVersion;
import moe.nea.libautoupdate.PotentialUpdate;
import moe.nea.libautoupdate.UpdateContext;
import moe.nea.libautoupdate.UpdateData;
import moe.nea.libautoupdate.UpdateTarget;
import moe.nea.libautoupdate.UpdateUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This class is the SkyblockAddons updater. It checks for updates by reading version information from {@link OnlineData.UpdateInfo}.
 */
public class Updater {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(?<major>[0-9])\\.(?<minor>[0-9])\\.(?<patch>[0-9]).*");
    private static final UpdateContext AUTO_UPDATE_CONTEXT = new UpdateContext(
            new CustomUpdateSource(),
            UpdateTarget.deleteAndSaveInTheSameFolder(SkyblockAddons.class),
            CurrentVersion.ofTag(SkyblockAddons.METADATA.getVersion().toString()),
            SkyblockAddons.MOD_ID
    );

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    private SemanticVersion target = null;

    @Getter private String messageToRender;
    private String downloadLink;
    private String changelogLink;
    private String note = "";

    private boolean hasUpdate = false;
    private boolean isPatch = false;
    private boolean sentUpdateMessage = false;
    private PotentialUpdate cachedPotentialUpdate = null;
    private boolean updateLaunched = false;

    public Updater() {
        UpdateUtils.patchConnection(urlConnection ->
                urlConnection.setRequestProperty("User-Agent", Utils.USER_AGENT)
        );
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

        SemanticVersion latestRelease = null;
        SemanticVersion latestBeta = null;
        SemanticVersion current;
        try {
            current = SemanticVersion.parse(SkyblockAddons.METADATA.getVersion().toString());
        } catch (VersionParsingException e) {
            LOGGER.error("Version parsing error: {}", e.getMessage());
            return;
        }
        boolean isCurrentBeta = isBetaVersion(current);
        boolean latestReleaseExists = updateInfo.getLatestRelease() != null;
        boolean latestBetaExists = updateInfo.getLatestBeta() != null;
        int releaseDiff = 0;
        int betaDiff = 0;

        if (latestReleaseExists) {
            latestRelease = updateInfo.getLatestRelease();
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
                latestBeta = updateInfo.getLatestBeta();
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

        Status status = null;
        if (!isCurrentBeta) {
            if (releaseDiff == 0) {
                status = Status.UP_TO_DATE;
            } else if (releaseDiff < 0) {
                status = Status.AHEAD;
            } else {
                status = Status.OUTDATED;
                target = latestRelease;
            }
        } else {
            String currentVersionString = current.toString();

            // If release is newer than this beta, target release
            if (latestReleaseExists) {
                SemanticVersion currentWithoutPrerelease;
                try {
                    currentWithoutPrerelease = SemanticVersion.parse(
                            currentVersionString.substring(0, currentVersionString.indexOf('-'))
                    );

                    if (releaseDiff > 0 || latestRelease.compareTo(currentWithoutPrerelease) == 0) {
                        status = Status.OUTDATED;
                        target = latestRelease;
                    } else if (!latestBetaExists && releaseDiff < 0) {
                        status = Status.AHEAD;
                    } else if (releaseDiff == 0) {
                        LOGGER.warn("The current beta version ({}) matches the latest release version. " +
                                "There is probably something wrong with the online data.", currentVersionString);
                        status = Status.UP_TO_DATE;
                    }
                } catch (VersionParsingException e) {
                    LOGGER.error("Semantic version parsing error: ", e);
                }
            }

            if (status == null) {
                if (betaDiff == 0) {
                    status = Status.UP_TO_DATE;
                } else if (betaDiff < 0) {
                    status = Status.AHEAD;
                } else {
                    status = Status.BETA_OUTDATED;
                    target = latestBeta;
                }
            }
        }

        if (status == Status.OUTDATED || status == Status.BETA_OUTDATED) {
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
                        // Get related notes between target version and current version
                        int versionNumber = Optional.of(cachedPotentialUpdate.getUpdate())
                                .map(UpdateData::getVersionNumber)
                                .map(JsonElement::getAsInt)
                                .orElse(0);
                        if (versionNumber != 0) {
                            note = "";
                            updateInfo.getUpdateNotes().headMap(versionNumber, true).forEach((key, value) -> {
                                int buildNumber = Integer.parseInt(SkyblockAddons.BUILD_NUMBER.split("\\.")[0]);
                                if (StringUtil.isNullOrEmpty(value) || buildNumber >= key) return;
                                note += "• " + value + "\n";
                            });
                        }
                    }
                });
            }
            hasUpdate = true;

            LOGGER.info("Found an update: {}", target);

            if (status == Status.OUTDATED) {
                downloadLink = updateInfo.getReleaseDownload();
                changelogLink = updateInfo.getReleaseChangelog();
            } else {
                downloadLink = updateInfo.getBetaDownload();
                changelogLink = updateInfo.getBetaChangelog();
            }

            // It's a patch if the major & minor numbers are the same & the player isn't upgrading from a beta.
            isPatch = current.getVersionComponent(0) == target.getVersionComponent(0)
                    && current.getVersionComponent(1) == target.getVersionComponent(1)
                    && !isCurrentBeta;

            if (isPatch) {
                messageToRender = Translations.getMessage("messages.updateChecker.notificationBox.patchAvailable", target);
            } else if(status == Status.BETA_OUTDATED) {
                messageToRender = Translations.getMessage("messages.updateChecker.notificationBox.betaAvailable", target);
            } else {
                messageToRender = Translations.getMessage("messages.updateChecker.notificationBox.majorAvailable", target);
            }
        } else if (status == Status.AHEAD) {
            if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
                LOGGER.warn("The current version is newer than the latest version."
                        + " Please tell an SBA developer to update the online data.");
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

        Utils.sendMessage(Component.literal("§7§m----------------§7[ §b§lSkyblockAddons §7]§7§m----------------"), false);

        MutableComponent newUpdate = Component.literal(
                String.format("§b%s\n", Translations.getMessage("messages.updateChecker.newUpdateAvailable", targetVersion))
        );

        if (!note.isEmpty()) {
            MutableComponent versionNote = Component.literal("\n" + ColorCode.RED + note);
            newUpdate.append(versionNote);
        }

        Utils.sendMessage(newUpdate, false);

        MutableComponent autoDownloadButton;
        MutableComponent downloadButton;
        MutableComponent openModsFolderButton;
        MutableComponent changelogButton;

        autoDownloadButton = Component.literal(
                String.format("§a§l[%s]", Translations.getMessage("messages.updateChecker.autoDownloadButton"))
        );

        Object autoUpdateValue = Feature.AUTO_UPDATE.getValue();
        if (autoUpdateValue == AutoUpdateMode.STABLE || autoUpdateValue == AutoUpdateMode.LATEST) {
            AutoUpdateMode autoUpdateMode = (AutoUpdateMode) autoUpdateValue;
            UpdateData currentUpdateData = main.getOnlineData().getUpdateData(autoUpdateMode.name());
            if (cachedPotentialUpdate == null) {
                autoDownloadButton = Component.literal(
                        String.format("§8§m[%s]§r", Translations.getMessage("messages.updateChecker.autoDownloadButton"))
                ).withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(
                        "§7" + Translations.getMessage("messages.updateChecker.autoUpdateTargetNotFound")

                ))));
                autoDownloadButton.append(" ");
            } else if (currentUpdateData != null
                    && !StringUtil.isNullOrEmpty(currentUpdateData.getVersionName())
                    && !currentUpdateData.getVersionName().contains(targetVersion)) {
                autoDownloadButton = Component.literal(
                        String.format("§8§m[%s]§r", Translations.getMessage("messages.updateChecker.autoDownloadButton"))
                ).withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(
                        "§7" + Translations.getMessage("messages.updateChecker.autoUpdateTargetIsNotUpToDate")
                ))));
                autoDownloadButton.append(" ");
            } else {
                String targetVersionName = cachedPotentialUpdate.getUpdate() == null
                        ? null
                        : cachedPotentialUpdate.getUpdate().getVersionName();
                String mcVersionXYZ = SharedConstants.getCurrentVersion().name().split("-")[0];
                if (!StringUtil.isNullOrEmpty(targetVersionName) && !targetVersionName.contains(mcVersionXYZ)) {
                    autoDownloadButton = Component.literal(
                            String.format("§8§m[%s]§r", Translations.getMessage("messages.updateChecker.autoDownloadButton"))
                    ).withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal(
                            "§7" + Translations.getMessage("messages.updateChecker.targetIsNotForCurrentMinecraft", mcVersionXYZ)
                    ))));
                    autoDownloadButton.append(" ");
                } else if (Feature.FULL_AUTO_UPDATE.isEnabled()) {
                    if (!updateLaunched) {
                        Utils.sendMessage(ColorCode.YELLOW + Translations.getMessage("messages.updateChecker.autoDownloadStarted") + "\n", false);
                        launchAutoUpdate(cachedPotentialUpdate);
                    }
                    autoDownloadButton = Component.literal("");
                } else {
                    autoDownloadButton.withStyle(style -> style.withClickEvent(
                            new ClickEvent.RunCommand("/sba internal launchAutoUpdate")
                    ).withHoverEvent(new HoverEvent.ShowText(Component.literal(
                            "§7" + Translations.getMessage("messages.updateChecker.autoDownloadButtonHover", targetVersion)
                    ))));
                    autoDownloadButton.append(" ");
                }
            }
        } else {
            autoDownloadButton = Component.literal("");
        }

        downloadButton = Component.literal(
                String.format("§b[%s]", Translations.getMessage("messages.updateChecker.downloadButton"))
        );

        if (!StringUtil.isNullOrEmpty(downloadLink)) {
            downloadButton.withStyle(style -> style.withClickEvent(
                    new ClickEvent.OpenUrl(URI.create(downloadLink))
            ).withHoverEvent(new HoverEvent.ShowText(Component.literal(
                    "§7" + Translations.getMessage("messages.clickToOpenLink")
            ))));
        } else {
            downloadButton.withStyle(style -> style.withStrikethrough(true).withHoverEvent(new HoverEvent.ShowText(
                    Component.literal("§7" + Translations.getMessage("messages.updateChecker.noDownloadAvailable"))
            )));
        }
        autoDownloadButton.append(downloadButton).append(" ");

        openModsFolderButton = Component.literal(
                String.format("§e[%s]", Translations.getMessage("messages.updateChecker.openModFolderButton"))
        ).withStyle(style -> style.withClickEvent(
                new ClickEvent.RunCommand("/sba folder")
        ).withHoverEvent(
                new HoverEvent.ShowText(Component.literal("§7" + Translations.getMessage("messages.clickToOpenFolder")
        ))));
        autoDownloadButton.append(openModsFolderButton).append(" ");

        if (!StringUtil.isNullOrEmpty(changelogLink)) {
            changelogButton = Component.literal(
                    String.format(" §9[%s]", Translations.getMessage("messages.updateChecker.changelogButton"))
            ).withStyle(style -> style.withClickEvent(
                    new ClickEvent.OpenUrl(URI.create(changelogLink))
            ).withHoverEvent(new HoverEvent.ShowText(
                    Component.literal("§7" + Translations.getMessage("messages.clickToOpenLink"))
            )));
            autoDownloadButton.append(changelogButton).append(" ");
        }

        Utils.sendMessage(autoDownloadButton, false);
        Utils.sendMessage(Component.literal("§7§m--------------------------------------------------"), false);

        sentUpdateMessage = true;
    }

    /**
     * Returns whether the given version is a beta version
     *
     * @param version the version to check
     * @return {@code true} if the given version is a beta version, {@code false} otherwise
     */
    private boolean isBetaVersion(Version version) {
        String versionString = version.toString();
        return versionString.contains("a") || versionString.contains("b");
    }


    public void launchAutoUpdate() {
        if (cachedPotentialUpdate != null && !updateLaunched) {
            Utils.sendMessage(ColorCode.YELLOW + Translations.getMessage("messages.updateChecker.autoDownloadStarted"));
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

    private enum Status {
        UP_TO_DATE,
        OUTDATED,
        AHEAD,
        BETA_OUTDATED
    }

}