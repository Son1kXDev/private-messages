package com.enjine.privatemessages;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import xyz.nucleoid.server.translations.api.language.ServerLanguageDefinition;
import xyz.nucleoid.server.translations.impl.ServerTranslations;

import java.util.HashMap;
import java.util.Map;

public class PrivateMessages implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    static final Map<ServerPlayerEntity, ServerPlayerEntity> lastMessageSender = new HashMap<>();
    public static PrivateMessagesConfig config;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");
        LOGGER.info("Loading configuration");
        config = ConfigManager.loadConfig();
        LOGGER.info("Configuration is loaded");
        ServerLanguageDefinition language = ServerTranslations.INSTANCE.getLanguageDefinition(config.language);
        ServerTranslations.INSTANCE.setSystemLanguage(language);
        LOGGER.info("Selected language: {}, {}", language.code(), language.name());

        PlayerDataManager.initialize();
        GlobalCommandManager.registerCommands();
        EventManager.registerEvents();
        LOGGER.info("Initialized");
    }

}
