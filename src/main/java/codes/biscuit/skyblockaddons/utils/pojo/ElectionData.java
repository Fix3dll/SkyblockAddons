package codes.biscuit.skyblockaddons.utils.pojo;

import lombok.Getter;

import java.util.List;

@Getter
public class ElectionData {
    private boolean success;
    private long lastUpdated;
    private Mayor mayor;
    private Current current;

    @Getter
    public static class Mayor {
        private String key;
        private String name;
        private List<Perk> perks;
        private Minister minister;
        private Election election;

        @Getter
        public static class Perk {
            private String name;
            private String description;
        }

        @Getter
        public static class Minister {
            private String key;
            private String name;
            private Perk perk;
        }

        @Getter
        public static class Election {
            private int year;
            private List<Candidate> candidates;

            @Getter
            public static class Candidate {
                private String key;
                private String name;
                private List<Perk> perks;
                private int votes;
            }
        }
    }

    @Getter
    public static class Current {
        private int year;
        private List<Candidate> candidates;

        @Getter
        public static class Candidate {
            private String key;
            private String name;
            private List<Mayor.Perk> perks;
            private int votes;
        }
    }
}
