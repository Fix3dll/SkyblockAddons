package com.fix3dll.skyblockaddons.features.starlyn;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.core.Island;
import com.fix3dll.skyblockaddons.utils.ScoreboardManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class StarlynContestManager {

    private static final StarlynContestTimer MIRIA = new StarlynContestTimer(System::currentTimeMillis, System::nanoTime);
    private static final StarlynContestTimer AGATHA = new StarlynContestTimer(System::currentTimeMillis, System::nanoTime);
    private static Island observedIsland = Island.UNKNOWN;

    public static int getRemainingSeconds(boolean miria) {
        return (miria ? MIRIA : AGATHA).getRemainingSeconds();
    }

    public static StarlynContestTimer.Snapshot getSnapshot(boolean miria) {
        return (miria ? MIRIA : AGATHA).getSnapshot();
    }

    public static long getEventId(boolean miria) {
        return (miria ? MIRIA : AGATHA).getEventId();
    }

    public static void clearObservation() {
        MIRIA.clearObservation();
        AGATHA.clearObservation();
        observedIsland = Island.UNKNOWN;
    }

    public static void update() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            if (observedIsland != Island.UNKNOWN) clearObservation();
            return;
        }
        Island island = SkyblockAddons.getInstance().getUtils().getMap();
        if (island != observedIsland) {
            clearObservation();
            observedIsland = island;
        }
        if (island != Island.TORRHUS_CANYON && island != Island.MOONGLADE_MARSH) return;

        boolean miria = island == Island.TORRHUS_CANYON;
        String name = miria ? "Miria" : "Agatha";
        StarlynContestTimer timer = miria ? MIRIA : AGATHA;
        OptionalInt sidebar = StarlynContestTimer.parseRemainingSeconds(ScoreboardManager.getStrippedScoreboardLines(), name);
        if (sidebar.isPresent()) {
            timer.observe(sidebar.getAsInt());
            // An unchanged valid scoreboard still outranks a delayed tab widget.
            return;
        }

        // This reads the received tab list even when SBA's compact-tab feature is disabled.
        PlayerTabOverlay tab = mc.gui.hud.getTabList();
        List<String> lines = new ArrayList<>();
        for (PlayerInfo playerInfo : tab.getPlayerInfos()) {
            lines.add(tab.getNameForDisplay(playerInfo).getString());
        }
        if (tab.header != null) lines.addAll(List.of(tab.header.getString().split("\\n")));
        if (tab.footer != null) lines.addAll(List.of(tab.footer.getString().split("\\n")));
        OptionalInt remaining = StarlynContestTimer.parseRemainingSeconds(lines, name);
        if (remaining.isPresent()) timer.observe(remaining.getAsInt(), StarlynContestTimer.Source.TAB_LIST);
    }
}
