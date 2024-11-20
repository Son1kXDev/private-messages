package com.enjine.privatemessages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/private_messages.json");

    public static PrivateMessagesConfig loadConfig() {
        if (!CONFIG_FILE.exists()) {
            PrivateMessagesConfig defaultConfig = new PrivateMessagesConfig();
            saveConfig(defaultConfig);
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            return GSON.fromJson(reader, PrivateMessagesConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new PrivateMessagesConfig();
        }
    }

    public static void saveConfig(PrivateMessagesConfig config) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
