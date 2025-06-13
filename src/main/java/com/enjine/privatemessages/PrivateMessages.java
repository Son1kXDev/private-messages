package com.enjine.privatemessages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import xyz.nucleoid.server.translations.api.language.ServerLanguageDefinition;
import xyz.nucleoid.server.translations.impl.ServerTranslations;
import xyz.nucleoid.server.translations.impl.language.LanguageReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PrivateMessages implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    static final Map<ServerPlayerEntity, ServerPlayerEntity> lastMessageSender = new HashMap<>();
    public static PrivateMessagesConfig config;

    private static void getModTranslationFromGithub() {
        var language_list = "{\"languages\": [\"ru_ru\",\"en_us\"]}";
        JsonObject jsonObject = (JsonObject) JsonParser.parseString(language_list);
        JsonArray languageArray = jsonObject.getAsJsonArray("languages");

        for (JsonElement entry : languageArray) {
            String code = entry.getAsString();
            URL languageURL = getLanguageURL(code);
            if (languageURL == null) continue;

            ServerTranslations.INSTANCE.addTranslations(code, () -> {
                try {
                    return LanguageReader.read(languageURL.openStream());
                } catch (IOException e) {
                    e.fillInStackTrace();
                }
                return null;
            });
        }
    }

    private static URL getLanguageURL(String code) {
        try {
            return new URL("../../resources/assets/private-messages/lang/" + code + ".json");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[PM] Initializing");
        LOGGER.info("[PM] Loading configuration");
        config = ConfigManager.loadConfig();
        LOGGER.info("[PM] Configuration is loaded");
        ServerLanguageDefinition language = ServerTranslations.INSTANCE.getLanguageDefinition(config.language);
        ServerTranslations.INSTANCE.setSystemLanguage(language);
        LOGGER.info("[PM] Language: {}, {}", language.code(), language.name());

        PlayerDataManager.initialize();
        GlobalCommandManager.registerCommands();
        EventManager.registerEvents();
        LOGGER.info("[PM] Initialized");
    }

}
