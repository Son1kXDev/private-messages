package com.enjine.privatemessages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import static com.enjine.privatemessages.PrivateMessages.LOGGER;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/private_messages.json");

    public static PrivateMessagesConfig loadConfig() {
        if (!CONFIG_FILE.exists()) {
            PrivateMessagesConfig defaultConfig = new PrivateMessagesConfig();
            saveConfig(defaultConfig);
            Locale.setDefault(new Locale("en_us"));
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            PrivateMessagesConfig config = GSON.fromJson(reader, PrivateMessagesConfig.class);
            Locale.setDefault(new Locale(config.language));
            return config;
        } catch (IOException e) {
            e.fillInStackTrace();
            LOGGER.error("{}", e.getLocalizedMessage());
            Locale.setDefault(new Locale("en_us"));
            return new PrivateMessagesConfig();
        }
    }

    public static void saveConfig(PrivateMessagesConfig config) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            LOGGER.error("{}", e.getLocalizedMessage());
            e.fillInStackTrace();
        }
    }
}
