package com.enjine.privatemessages;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class PrivateMessages implements ModInitializer {
    static final Map<ServerPlayerEntity, ServerPlayerEntity> lastMessageSender = new HashMap<>();
    public static PrivateMessagesConfig config;

    @Override
    public void onInitialize() {
        config = ConfigManager.loadConfig();

        PlayerDataManager.initialize();
        GlobalCommandManager.registerCommands();
        EventManager.registerEvents();
    }

}
