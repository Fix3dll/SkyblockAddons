package com.fix3dll.skyblockaddons.mixin.hooks;

import com.fix3dll.skyblockaddons.SkyblockAddons;
import com.fix3dll.skyblockaddons.features.discordrpc.DiscordRPCManager;

public class DisconnectedScreenHook {

    public static void onDisconnect() {
        DiscordRPCManager discordRPCManager = SkyblockAddons.getInstance().getDiscordRPCManager();
        if (discordRPCManager.isActive()) {
            discordRPCManager.stop();
        }
    }
}
