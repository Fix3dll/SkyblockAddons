package com.fix3dll.skyblockaddons.utils.data.skyblockdata;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.SkyblockMayor;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@Getter
public class ElectionData {
    private static final Logger LOGGER = SkyblockAddons.getLogger();

    @SerializedName("success")
    private boolean success = false;
    @SerializedName("lastUpdated")
    private long lastUpdated;
    @SerializedName("mayor")
    private Mayor mayor;
    @SerializedName("current")
    private Current current;

    /**
     * Checks if the given perk is active in any Mayor, Minister or Jerry's Perkpocalypse
     * @param perkName case-insensitive perk name
     * @return true if perk is active
     */
    public boolean isPerkActive(String perkName) {
        // illegal argument
        if (perkName == null || perkName.isEmpty()) {
            LOGGER.error("\"perkName\" cannot be null or empty!");
            return false;
        } else if (!success) {
            return false;
        }

        if (mayor != null) {
            for (Mayor.Perk perk : this.mayor.perks) {
                if (perkName.equalsIgnoreCase(perk.name)) {
                    return true;
                }
            }

            // If a Special Mayor is elected, then no Minister is selected.
            if (mayor.minister != null && perkName.equalsIgnoreCase(mayor.minister.name)) {
                return true;
            }
        }

        MayorJerryData mayorJerryData = SkyblockAddons.getInstance().getMayorJerryData();
        if (mayorJerryData != null && mayorJerryData.hasMayorAndActive()) {
            // Perkpocalypse mayors come with all perks active
            return mayorJerryData.getMayor() == SkyblockMayor.getByPerkName(perkName);
        }

        return false;
    }

    /**
     * Checks if the candidate is active in any Mayor, Minister or Jerry's Perkpocalypse
     * @param candidateName case-insensitive candidateName name
     * @return true if candidate is active
     */
    public boolean isCandidateActive(String candidateName) {
        // illegal argument
        if (candidateName == null || candidateName.isEmpty()) {
            LOGGER.error("\"candidateName\" cannot be null or empty!");
            return false;
        } else if (!success) {
            return false;
        }

        if (this.mayor != null) {
            if (candidateName.equalsIgnoreCase(mayor.name)) {
                return true;
            } else if (mayor.minister != null && candidateName.equalsIgnoreCase(mayor.minister.name)) {
                return true;
            }
        }

        MayorJerryData mayorJerryData = SkyblockAddons.getInstance().getMayorJerryData();
        return mayorJerryData != null && mayorJerryData.hasMayorAndActive()
                && candidateName.equalsIgnoreCase(mayorJerryData.getMayor().name());
    }

    @Getter
    public static class Mayor {
        @SerializedName("key")
        private String key;
        @SerializedName("name")
        private String name;
        @SerializedName("perks")
        private List<Perk> perks = Collections.emptyList();
        @SerializedName("minister")
        private Minister minister;
        @SerializedName("election")
        private Election election;

        @Getter
        public static class Perk {
            @SerializedName("name")
            private String name;
            @SerializedName("description")
            private String description;
        }

        @Getter
        public static class Minister {
            @SerializedName("key")
            private String key;
            @SerializedName("name")
            private String name;
            @SerializedName("perk")
            private Perk perk;
        }

        @Getter
        public static class Election {
            @SerializedName("year")
            private int year;
            @SerializedName("candidates")
            private List<Candidate> candidates;

            @Getter
            public static class Candidate {
                @SerializedName("key")
                private String key;
                @SerializedName("name")
                private String name;
                @SerializedName("perks")
                private List<Perk> perks = Collections.emptyList();
                @SerializedName("votes")
                private int votes;
            }
        }
    }

    @Getter
    public static class Current {
        @SerializedName("year")
        private int year;
        @SerializedName("candidates")
        private List<Candidate> candidates = Collections.emptyList();

        @Getter
        public static class Candidate {
            @SerializedName("key")
            private String key;
            @SerializedName("name")
            private String name;
            @SerializedName("perks")
            private List<Mayor.Perk> perks = Collections.emptyList();
            @SerializedName("votes")
            private int votes;
        }
    }
}