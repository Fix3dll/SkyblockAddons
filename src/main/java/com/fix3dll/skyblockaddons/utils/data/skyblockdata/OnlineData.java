package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.core.SkyblockRarity;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import moe.nea.libautoupdate.UpdateData;
import net.fabricmc.loader.api.SemanticVersion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

@Getter
public class OnlineData {

    @SerializedName("bannerImageURL")
    private String bannerImageURL;
    @SerializedName("bannerLink")
    private String bannerLink;
    @SerializedName("updateInfo")
    private UpdateInfo updateInfo;
    @SerializedName("languageJSONFormat")
    private String languageJSONFormat;
    @SerializedName("autoUpdateStable")
    @Getter(AccessLevel.NONE)
    private AutoUpdateData autoUpdateStable;
    @SerializedName("autoUpdateLatest")
    @Getter(AccessLevel.NONE)
    private AutoUpdateData autoUpdateLatest;

    /**
     * @return null if OnlineData is not fetched from CDN
     */
    public UpdateData getUpdateData(String updateStream) {
        if (updateStream.equalsIgnoreCase("stable")) {
            return autoUpdateStable == null ? null : autoUpdateStable.buildUpdateData();
        } else if (updateStream.equalsIgnoreCase("latest")) {
            return autoUpdateLatest == null ? null : autoUpdateLatest.buildUpdateData();
        } else {
            throw new IllegalStateException("Unexpected 'updateStream': " + updateStream);
        }
    }

    /**
     * This is the list of features in the mod that should be disabled. Features in this list will be disabled for all
     * versions of the mod v1.5.5 and above. The first key in this map is "all". It contains a list of features to be disabled
     * in all mod versions. Version numbers can be added as additional keys to disable features in specific mod versions.
     * An example of this is shown below:
     * <br><br>
     * {@code "1.5.5": [3]}
     * <br><br>
     * Versions must follow the semver format (e.g. {@code 1.6.0}) and cannot be pre-release versions (e.g. {@code 1.6.0-beta.10}).
     * Pre-release versions of the mod adhere to the disabled features list of their release version. For example, the version
     * {@code 1.6.0-beta.10} will adhere to the list with the key {@code 1.6.0}. Disabling features for unique pre-release
     * versions is not supported.
     */
    @SerializedName("disabledFeatures")
    private HashMap<String, List<Integer>> disabledFeatures;
    @SerializedName("dropSettings")
    private DropSettings dropSettings;
    @SerializedName("hypixelBrands")
    private HashSet<Pattern> hypixelBrands;

    @Getter
    public static class UpdateInfo {
        @SerializedName("latestRelease")
        private SemanticVersion latestRelease;
        @SerializedName("releaseDownload")
        private String releaseDownload;
        @SerializedName("releaseChangelog")
        private String releaseChangelog;
        @SerializedName("latestBeta")
        private SemanticVersion latestBeta;
        @SerializedName("betaDownload")
        private String betaDownload;
        @SerializedName("betaChangelog")
        private String betaChangelog;
        @SerializedName("updateNotes")
        private TreeMap<Integer, String> updateNotes = new TreeMap<>();
    }

    @Getter
    public static class DropSettings {
        @SerializedName("minimumInventoryRarity")
        private SkyblockRarity minimumInventoryRarity;
        @SerializedName("minimumHotbarRarity")
        private SkyblockRarity minimumHotbarRarity;
        @SerializedName("dontDropTheseItems")
        private List<String> dontDropTheseItems;
        @SerializedName("allowDroppingTheseItems")
        private List<String> allowDroppingTheseItems;
    }

    @Getter
    public static class AutoUpdateData {
        @SerializedName("versionName")
        private String versionName;
        @SerializedName("versionNumber")
        private JsonElement versionNumber;
        @SerializedName("sha256")
        private String sha256;
        @SerializedName("download")
        private String download;

        private UpdateData buildUpdateData() {
            return new UpdateData(this.versionName, this.versionNumber, this.sha256, this.download);
        }
    }
}