package com.enjine.privatemessages;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class PrivateMessages implements ModInitializer {
    public static PrivateMessagesConfig config;

    static final Map<ServerPlayerEntity, ServerPlayerEntity> lastMessageSender = new HashMap<>();
    static final Map<ServerPlayerEntity, Set<ServerPlayerEntity>> ignoredPlayers = new HashMap<>();
    static final Map<ServerPlayerEntity, Boolean> notificationSettings = new HashMap<>();

    @Override
    public void onInitialize() {
        config = ConfigManager.loadConfig();

        GlobalCommandManager.registerCommands();
        EventManager.registerEvents();
    }

}
